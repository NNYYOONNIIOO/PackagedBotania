package nyonio.packagedbotania.integration.jei;

import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.api.RecipeTypeRegistry;
import thelm.packagedauto.container.ContainerEncoder;
import thelm.packagedauto.inventory.InventoryEncoderPattern;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.PacketSetRecipe;
import nyonio.packagedbotania.network.PackagedBotaniaPacketHandler;
import nyonio.packagedbotania.network.packet.PacketSetRecipeWithType;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.RecipeManaInfusion;
import vazkii.botania.common.block.ModBlocks;

@JEIPlugin
public class PackagedBotaniaJEIPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        IRecipeTransferHandlerHelper transferHelper = registry.getJeiHelpers().recipeTransferHandlerHelper();
        
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
            new BotaniaTransferHandler(transferHelper), "botania.manaPool");
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
            new BotaniaTransferHandler(transferHelper), "botania.runicAltar");
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
            new BotaniaTransferHandler(transferHelper), "botania.petals");
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
            new BotaniaTransferHandler(transferHelper), "botania.elvenTrade");
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
            new BotaniaTransferHandler(transferHelper), "botaniatweaks.agglomeration");
    }

    public static class BotaniaTransferHandler implements IRecipeTransferHandler<ContainerEncoder> {

        private final IRecipeTransferHandlerHelper transferHelper;

        public BotaniaTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
            this.transferHelper = transferHelper;
        }

        @Override
        public Class<ContainerEncoder> getContainerClass() {
            return ContainerEncoder.class;
        }

        @Override
        public IRecipeTransferError transferRecipe(ContainerEncoder container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
            String category = recipeLayout.getRecipeCategory().getUid();
            Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();
            
            IRecipeType targetType = detectTargetRecipeType(category, ingredients);
            if(targetType == null) {
                return null;
            }

            Int2ObjectMap<ItemStack> map = targetType.getRecipeTransferMap(recipeLayout, category);
            if(map == null || map.isEmpty()) {
                return transferHelper.createInternalError();
            }

            if(!doTransfer) {
                return null;
            }

            IRecipeType currentType = container.patternInventory.recipeType;
            if(currentType.getName().equals(targetType.getName())) {
                PacketHandler.INSTANCE.sendToServer(new PacketSetRecipe(map));
            } else {
                InventoryEncoderPattern inv = container.patternInventory;
                inv.recipeType = targetType;
                inv.validateRecipeType();
                IntSet enabledSlots = targetType.getEnabledSlots();
                for(int i = 0; i < 90; ++i) {
                    if(!enabledSlots.contains(i)) {
                        inv.stacks.set(i, ItemStack.EMPTY);
                    }
                }
                inv.updateRecipeInfo(true);
                container.setupSlots();
                PackagedBotaniaPacketHandler.INSTANCE.sendToServer(new PacketSetRecipeWithType(map, targetType.getName()));
            }
            return null;
        }

        private IRecipeType detectTargetRecipeType(String category, Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients) {
            switch(category) {
                case "botania.manaPool":
                    return detectManaPoolType(ingredients);
                case "botania.runicAltar":
                    return RecipeTypeRegistry.getRecipeType(new ResourceLocation("packaged_botania:rune_altar"));
                case "botania.petals":
                    return RecipeTypeRegistry.getRecipeType(new ResourceLocation("packaged_botania:apothecary"));
                case "botania.elvenTrade":
                    return RecipeTypeRegistry.getRecipeType(new ResourceLocation("packaged_botania:alfheim_portal"));
                case "botaniatweaks.agglomeration":
                    return RecipeTypeRegistry.getRecipeType(new ResourceLocation("packaged_botania:terra_plate"));
                default:
                    return null;
            }
        }

        private IRecipeType detectManaPoolType(Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients) {
            ItemStack inputStack = null;
            ItemStack outputStack = null;

            for(Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : ingredients.entrySet()) {
                IGuiIngredient<ItemStack> ingredient = entry.getValue();
                if(ingredient.isInput()) {
                    ItemStack displayed = ingredient.getDisplayedIngredient();
                    if(displayed != null && !displayed.isEmpty()) {
                        if(displayed.getItem() == net.minecraft.item.Item.getItemFromBlock(ModBlocks.alchemyCatalyst) ||
                           displayed.getItem() == net.minecraft.item.Item.getItemFromBlock(ModBlocks.conjurationCatalyst)) {
                            continue;
                        }
                        if(inputStack == null) {
                            inputStack = displayed;
                        }
                    }
                } else {
                    ItemStack displayed = ingredient.getDisplayedIngredient();
                    if(displayed != null && !displayed.isEmpty()) {
                        if(outputStack == null) {
                            outputStack = displayed;
                        }
                    }
                }
            }

            if(inputStack == null) {
                return RecipeTypeRegistry.getRecipeType(new ResourceLocation("packaged_botania:mana_pool"));
            }

            for(RecipeManaInfusion recipe : BotaniaAPI.manaInfusionRecipes) {
                if(recipe.matches(inputStack)) {
                    if(outputStack != null && ItemStack.areItemsEqual(recipe.getOutput(), outputStack)) {
                        if(recipe.isAlchemy()) {
                            return RecipeTypeRegistry.getRecipeType(new ResourceLocation("packaged_botania:mana_pool_alchemy"));
                        } else if(recipe.isConjuration()) {
                            return RecipeTypeRegistry.getRecipeType(new ResourceLocation("packaged_botania:mana_pool_conjuration"));
                        } else {
                            return RecipeTypeRegistry.getRecipeType(new ResourceLocation("packaged_botania:mana_pool"));
                        }
                    }
                }
            }

            for(RecipeManaInfusion recipe : BotaniaAPI.manaInfusionRecipes) {
                if(recipe.matches(inputStack)) {
                    if(recipe.isAlchemy()) {
                        return RecipeTypeRegistry.getRecipeType(new ResourceLocation("packaged_botania:mana_pool_alchemy"));
                    } else if(recipe.isConjuration()) {
                        return RecipeTypeRegistry.getRecipeType(new ResourceLocation("packaged_botania:mana_pool_conjuration"));
                    } else {
                        return RecipeTypeRegistry.getRecipeType(new ResourceLocation("packaged_botania:mana_pool"));
                    }
                }
            }

            return RecipeTypeRegistry.getRecipeType(new ResourceLocation("packaged_botania:mana_pool"));
        }
    }
}

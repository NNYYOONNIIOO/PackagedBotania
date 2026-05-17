package nyonio.packagedbotania.recipe;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nyonio.packagedbotania.config.PackagedBotaniaConfig;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import vazkii.botania.common.block.ModBlocks;

public class RecipeTypeRuneAltar implements IRecipeType {

    public static final RecipeTypeRuneAltar INSTANCE = new RecipeTypeRuneAltar();
    public static final ResourceLocation NAME = new ResourceLocation("packaged_botania:rune_altar");
    public static final IntSet SLOTS;
    public static final List<String> CATEGORIES = Arrays.asList("botania.runicAltar");
    public static final Color COLOR = new Color(139, 139, 139);
    public static final Color COLOR_DISABLED = new Color(64, 64, 64);

    static {
        SLOTS = new IntLinkedOpenHashSet();
        SLOTS.add(9 * 1 + 2);
        for(int i = 0; i < 16; i++) {
            int row = i / 4;
            int col = i % 4;
            SLOTS.add(9 * (row + 2) + (col + 2));
        }
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public String getLocalizedName() {
        return I18n.translateToLocal("recipe.packaged_botania.rune_altar");
    }

    @Override
    public String getLocalizedNameShort() {
        return I18n.translateToLocal("recipe.packaged_botania.rune_altar.short");
    }

    @Override
    public IRecipeInfo getNewRecipeInfo() {
        return new RecipeInfoRuneAltar();
    }

    @Override
    public IntSet getEnabledSlots() {
        return SLOTS;
    }

    @Override
    public List<String> getJEICategories() {
        return CATEGORIES;
    }

    @Optional.Method(modid="jei")
    @Override
    public Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeLayout recipeLayout, String category) {
        Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
        Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();
        int[] slotArray = SLOTS.toIntArray();
        map.put(slotArray[0], PackagedBotaniaConfig.getRuneAltarJEICatalyst());
        int index = 1;
        for(Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : ingredients.entrySet()) {
            IGuiIngredient<ItemStack> ingredient = entry.getValue();
            if(ingredient.isInput() && index < 17) {
                ItemStack displayed = ingredient.getDisplayedIngredient();
                if(displayed != null && !displayed.isEmpty()) {
                    if(displayed.getItem() == net.minecraft.item.Item.getItemFromBlock(ModBlocks.runeAltar)) {
                        continue;
                    }
                    map.put(slotArray[index], displayed.copy());
                }
                index++;
            }
        }
        return map;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Object getRepresentation() {
        return new ItemStack(ModBlocks.runeAltar);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Color getSlotColor(int slot) {
        if(SLOTS.contains(slot)) {
            return COLOR;
        }
        if(slot == 85) {
            return COLOR;
        }
        return COLOR_DISABLED;
    }
}

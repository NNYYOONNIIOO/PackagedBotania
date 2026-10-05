package nyonio.packagedbotania.recipe;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.block.ItemBlockSpecialFlower;
import vazkii.botania.common.lib.LibBlockNames;

public class RecipeTypePureDaisy implements IRecipeType {

    public static final RecipeTypePureDaisy INSTANCE = new RecipeTypePureDaisy();
    public static final ResourceLocation NAME = new ResourceLocation("packaged_botania:pure_daisy");
    public static final int SLOT = 40;
    public static final IntSet SLOTS;
    public static final List<String> CATEGORIES = Collections.singletonList("botania.pureDaisy");
    public static final Color COLOR = new Color(139, 139, 139);
    public static final Color COLOR_DISABLED = new Color(64, 64, 64);

    static {
        SLOTS = new IntRBTreeSet();
        SLOTS.add(SLOT);
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public String getLocalizedName() {
        return I18n.translateToLocal("recipe.packaged_botania.pure_daisy");
    }

    @Override
    public String getLocalizedNameShort() {
        return I18n.translateToLocal("recipe.packaged_botania.pure_daisy.short");
    }

    @Override
    public IRecipeInfo getNewRecipeInfo() {
        return new RecipeInfoPureDaisy();
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
        for(Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : recipeLayout.getItemStacks().getGuiIngredients().entrySet()) {
            IGuiIngredient<ItemStack> ingredient = entry.getValue();
            if(!ingredient.isInput()) {
                continue;
            }
            ItemStack displayed = ingredient.getDisplayedIngredient();
            if(displayed != null && !displayed.isEmpty() && !isPureDaisy(displayed)) {
                map.put(SLOT, displayed.copy());
                break;
            }
        }
        return map;
    }

    protected boolean isPureDaisy(ItemStack stack) {
        Item specialFlower = Item.getItemFromBlock(ModBlocks.specialFlower);
        return stack.getItem() == specialFlower
                && LibBlockNames.SUBTILE_PUREDAISY.equals(ItemBlockSpecialFlower.getType(stack));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Object getRepresentation() {
        return ItemBlockSpecialFlower.ofType(LibBlockNames.SUBTILE_PUREDAISY);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Color getSlotColor(int slot) {
        if(SLOTS.contains(slot) || slot == 85) {
            return COLOR;
        }
        return COLOR_DISABLED;
    }
}

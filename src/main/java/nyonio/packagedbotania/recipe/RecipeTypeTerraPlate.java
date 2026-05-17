package nyonio.packagedbotania.recipe;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
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
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import vazkii.botania.common.block.ModBlocks;

public class RecipeTypeTerraPlate implements IRecipeType {

    public static final RecipeTypeTerraPlate INSTANCE = new RecipeTypeTerraPlate();
    public static final ResourceLocation NAME = new ResourceLocation("packaged_botania:terra_plate");
    public static final IntSet SLOTS;
    public static final List<String> CATEGORIES = Collections.emptyList();
    public static final Color COLOR = new Color(139, 139, 139);
    public static final Color COLOR_DISABLED = new Color(64, 64, 64);

    static {
        SLOTS = new IntLinkedOpenHashSet();
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
        return I18n.translateToLocal("recipe.packaged_botania.terra_plate");
    }

    @Override
    public String getLocalizedNameShort() {
        return I18n.translateToLocal("recipe.packaged_botania.terra_plate.short");
    }

    @Override
    public IRecipeInfo getNewRecipeInfo() {
        return new RecipeInfoTerraPlate();
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
        int[] slotArray = SLOTS.toIntArray();
        int slotIndex = 0;

        Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();
        for(Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : ingredients.entrySet()) {
            IGuiIngredient<ItemStack> ingredient = entry.getValue();
            if(ingredient.isInput() && slotIndex < 16) {
                ItemStack displayed = ingredient.getDisplayedIngredient();
                if(displayed != null && !displayed.isEmpty()) {
                    map.put(slotArray[slotIndex], displayed.copy());
                    slotIndex++;
                }
            }
        }
        return map;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Object getRepresentation() {
        return new ItemStack(ModBlocks.terraPlate);
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

package nyonio.packagedbotania.recipe;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
        Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();

        // Sort by key to ensure ingredients are processed in JEI slot order
        List<Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>>> sortedEntries = new ArrayList<>(ingredients.entrySet());
        sortedEntries.sort(Comparator.comparingInt(Map.Entry::getKey));

        // In the BotaniaTweaks agglomeration JEI layout:
        // - Recipe inputs come first (isInput=true, lower indices)
        // - Recipe output comes next (isInput=false)
        // - Multiblock structure blocks come after (original: isInput=false, replace: isInput=true)
        // We only want actual recipe inputs, which are isInput=true ingredients BEFORE the first isInput=false ingredient.
        // After the first isInput=false ingredient, any isInput=true ingredients are replace blocks, not recipe inputs.
        List<ItemStack> inputStacks = new ArrayList<>();
        boolean foundOutput = false;
        for(Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : sortedEntries) {
            IGuiIngredient<ItemStack> ingredient = entry.getValue();
            if(ingredient.isInput()) {
                if(!foundOutput) {
                    ItemStack displayed = ingredient.getDisplayedIngredient();
                    if(displayed != null && !displayed.isEmpty()) {
                        inputStacks.add(displayed.copy());
                    }
                }
            } else {
                foundOutput = true;
            }
        }

        int slotIndex = 0;
        for(int i = 0; i < inputStacks.size() && slotIndex < 16; i++) {
            map.put(slotArray[slotIndex], inputStacks.get(i));
            slotIndex++;
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

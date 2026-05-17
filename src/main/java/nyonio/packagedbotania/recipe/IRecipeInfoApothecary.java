package nyonio.packagedbotania.recipe;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import vazkii.botania.api.recipe.RecipePetals;
import thelm.packagedauto.api.IRecipeInfo;

public interface IRecipeInfoApothecary extends IRecipeInfo {

    List<ItemStack> getInputs();

    ItemStack getOutput();

    RecipePetals getRecipe();

    @Override
    default List<ItemStack> getOutputs() {
        ItemStack output = getOutput();
        return output.isEmpty() ? Collections.emptyList() : Collections.singletonList(output);
    }
}

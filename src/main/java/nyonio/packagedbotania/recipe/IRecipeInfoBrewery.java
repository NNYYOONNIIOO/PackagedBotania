package nyonio.packagedbotania.recipe;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import vazkii.botania.api.recipe.RecipeBrew;
import thelm.packagedauto.api.IRecipeInfo;

public interface IRecipeInfoBrewery extends IRecipeInfo {

    List<ItemStack> getInputs();

    ItemStack getOutput();

    ItemStack getBrewContainer();

    RecipeBrew getRecipe();

    int getMana();

    @Override
    default List<ItemStack> getOutputs() {
        ItemStack output = getOutput();
        return output.isEmpty() ? Collections.emptyList() : Collections.singletonList(output);
    }
}

package nyonio.packagedbotania.recipe;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IRecipeInfo;
import vazkii.botania.api.recipe.RecipePureDaisy;

public interface IRecipeInfoPureDaisy extends IRecipeInfo {

    ItemStack getInput();

    ItemStack getOutput();

    RecipePureDaisy getRecipe();

    @Override
    default List<ItemStack> getInputs() {
        ItemStack input = getInput();
        return input.isEmpty() ? Collections.emptyList() : Collections.singletonList(input);
    }

    @Override
    default List<ItemStack> getOutputs() {
        ItemStack output = getOutput();
        return output.isEmpty() ? Collections.emptyList() : Collections.singletonList(output);
    }
}

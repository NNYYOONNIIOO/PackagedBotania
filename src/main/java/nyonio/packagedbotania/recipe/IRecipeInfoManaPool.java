package nyonio.packagedbotania.recipe;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IRecipeInfo;

public interface IRecipeInfoManaPool extends IRecipeInfo {

    ItemStack getInput();

    ItemStack getOutput();

    int getMana();

    int getManaPoolRecipeType();

    IBlockState getCatalyst();

    vazkii.botania.api.recipe.RecipeManaInfusion getRecipe();

    @Override
    default List<ItemStack> getOutputs() {
        ItemStack output = getOutput();
        return output.isEmpty() ? Collections.emptyList() : Collections.singletonList(output);
    }
}

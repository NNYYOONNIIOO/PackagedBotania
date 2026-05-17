package nyonio.packagedbotania.recipe;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IRecipeInfo;

public interface IRecipeInfoTerraPlate extends IRecipeInfo {

    ItemStack getOutput();

    int getMana();

    IBlockState getMultiblockCenter();

    IBlockState getMultiblockEdge();

    IBlockState getMultiblockCorner();

    IBlockState getMultiblockCenterReplace();

    IBlockState getMultiblockEdgeReplace();

    IBlockState getMultiblockCornerReplace();

    @Override
    default List<ItemStack> getOutputs() {
        ItemStack output = getOutput();
        return output.isEmpty() ? Collections.emptyList() : Collections.singletonList(output);
    }
}

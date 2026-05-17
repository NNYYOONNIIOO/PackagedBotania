package nyonio.packagedbotania.recipe;

import java.util.List;

import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IRecipeInfo;

public interface IRecipeInfoAlfheimPortal extends IRecipeInfo {

    List<ItemStack> getOutputs();

    int getMana();
}

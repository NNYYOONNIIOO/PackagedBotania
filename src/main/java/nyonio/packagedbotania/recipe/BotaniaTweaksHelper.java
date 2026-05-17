package nyonio.packagedbotania.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;
import quaternary.botaniatweaks.modules.botania.recipe.AgglomerationRecipe;
import quaternary.botaniatweaks.modules.botania.recipe.AgglomerationRecipes;

public class BotaniaTweaksHelper {

    public static boolean isLoaded() {
        return Loader.isModLoaded("botania_tweaks");
    }

    public static IRecipeInfoTerraPlate findRecipe(List<ItemStack> inputs) {
        if(!isLoaded()) {
            return null;
        }
        try {
            for(AgglomerationRecipe recipe : AgglomerationRecipes.recipes) {
                if(matchItems(recipe, inputs)) {
                    return createRecipeInfo(recipe);
                }
            }
        } catch(Exception e) {
            return null;
        }
        return null;
    }

    private static boolean matchItems(AgglomerationRecipe recipe, List<ItemStack> inputs) {
        int totalInputs = recipe.recipeStacks.size() + recipe.recipeOreKeys.size();

        if(inputs.size() != totalInputs) {
            return false;
        }

        List<ItemStack> remaining = new ArrayList<>(inputs);

        for(ItemStack recipeStack : recipe.recipeStacks) {
            boolean found = false;
            for(int i = 0; i < remaining.size(); i++) {
                ItemStack input = remaining.get(i);
                if(input.isEmpty()) continue;
                if(input.getItem() == recipeStack.getItem() &&
                   (recipeStack.getItemDamage() == OreDictionary.WILDCARD_VALUE || input.getItemDamage() == recipeStack.getItemDamage()) &&
                   input.getCount() >= recipeStack.getCount()) {
                    remaining.remove(i);
                    found = true;
                    break;
                }
            }
            if(!found) {
                return false;
            }
        }

        for(String oreKey : recipe.recipeOreKeys) {
            List<ItemStack> ores = OreDictionary.getOres(oreKey, false);
            boolean found = false;
            for(int i = 0; i < remaining.size(); i++) {
                ItemStack input = remaining.get(i);
                if(input.isEmpty()) continue;
                for(ItemStack ore : ores) {
                    if(OreDictionary.itemMatches(ore, input, false)) {
                        remaining.remove(i);
                        found = true;
                        break;
                    }
                }
                if(found) break;
            }
            if(!found) {
                return false;
            }
        }

        return remaining.isEmpty();
    }

    private static IRecipeInfoTerraPlate createRecipeInfo(AgglomerationRecipe recipe) {
        RecipeInfoTerraPlate info = new RecipeInfoTerraPlate();
        info.output = recipe.recipeOutput.copy();
        info.mana = recipe.manaCost;
        info.inputs = new ArrayList<>();
        for(ItemStack stack : recipe.recipeStacks) {
            info.inputs.add(stack.copy());
        }
        info.multiblockCenter = recipe.multiblockCenter;
        info.multiblockEdge = recipe.multiblockEdge;
        info.multiblockCorner = recipe.multiblockCorner;
        info.multiblockCenterReplace = recipe.multiblockCenterReplace;
        info.multiblockEdgeReplace = recipe.multiblockEdgeReplace;
        info.multiblockCornerReplace = recipe.multiblockCornerReplace;
        return info;
    }
}

package nyonio.packagedbotania.recipe;

import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.item.ItemPackage;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.RecipeManaInfusion;
import vazkii.botania.common.block.ModBlocks;

public class RecipeInfoManaPool implements IRecipeInfoManaPool {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_ALCHEMY = 1;
    public static final int TYPE_CONJURATION = 2;

    protected ItemStack input = ItemStack.EMPTY;
    protected ItemStack output = ItemStack.EMPTY;
    protected int mana = 0;
    protected int manaPoolRecipeType = TYPE_NORMAL;
    protected RecipeManaInfusion recipe;

    public RecipeInfoManaPool setRecipe(RecipeManaInfusion recipe) {
        this.recipe = recipe;
        Object inputObj = recipe.getInput();
        if(inputObj instanceof ItemStack) {
            this.input = ((ItemStack)inputObj).copy();
        }
        this.output = recipe.getOutput().copy();
        this.mana = recipe.getManaToConsume();
        if(recipe.isAlchemy()) {
            this.manaPoolRecipeType = TYPE_ALCHEMY;
        } else if(recipe.isConjuration()) {
            this.manaPoolRecipeType = TYPE_CONJURATION;
        } else {
            this.manaPoolRecipeType = TYPE_NORMAL;
        }
        return this;
    }

    @Override
    public ItemStack getInput() {
        return input;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public int getManaPoolRecipeType() {
        return manaPoolRecipeType;
    }

    @Override
    public IBlockState getCatalyst() {
        switch(manaPoolRecipeType) {
            case TYPE_ALCHEMY:
                return ModBlocks.alchemyCatalyst.getDefaultState();
            case TYPE_CONJURATION:
                return ModBlocks.conjurationCatalyst.getDefaultState();
            default:
                return null;
        }
    }

    @Override
    public RecipeManaInfusion getRecipe() {
        return recipe;
    }

    @Override
    public boolean isValid() {
        return !input.isEmpty() && !output.isEmpty();
    }

    @Override
    public List<IPackagePattern> getPatterns() {
        if(!isValid()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new PackagePatternManaPool(this, 0));
    }

    @Override
    public List<ItemStack> getInputs() {
        return Collections.singletonList(input.copy());
    }

    @Override
    public IRecipeType getRecipeType() {
        switch(manaPoolRecipeType) {
            case TYPE_ALCHEMY:
                return RecipeTypeManaPoolAlchemy.INSTANCE;
            case TYPE_CONJURATION:
                return RecipeTypeManaPoolConjuration.INSTANCE;
            default:
                return RecipeTypeManaPool.INSTANCE;
        }
    }

    @Override
    public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
        this.input = ItemStack.EMPTY;
        this.output = ItemStack.EMPTY;
        this.mana = 0;
        this.recipe = null;
        
        if(input != null && input.size() > 40) {
            ItemStack stack = input.get(40);
            if(stack != null && !stack.isEmpty()) {
                this.input = stack.copy();
                
                int savedType = this.manaPoolRecipeType;
                
                for(RecipeManaInfusion recipe : BotaniaAPI.manaInfusionRecipes) {
                    if(recipe.matches(stack)) {
                        boolean recipeMatches = false;
                        if(savedType == TYPE_ALCHEMY && recipe.isAlchemy()) {
                            recipeMatches = true;
                        } else if(savedType == TYPE_CONJURATION && recipe.isConjuration()) {
                            recipeMatches = true;
                        } else if(savedType == TYPE_NORMAL && !recipe.isAlchemy() && !recipe.isConjuration()) {
                            recipeMatches = true;
                        }
                        
                        if(recipeMatches) {
                            setRecipe(recipe);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public Int2ObjectMap<ItemStack> getEncoderStacks() {
        Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
        if(!input.isEmpty()) {
            map.put(40, input.copy());
        }
        return map;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagCompound inputTag = nbt.getCompoundTag("Input");
        NBTTagCompound outputTag = nbt.getCompoundTag("Output");
        input = inputTag.hasKey("id") ? new ItemStack(inputTag) : ItemStack.EMPTY;
        output = outputTag.hasKey("id") ? new ItemStack(outputTag) : ItemStack.EMPTY;
        mana = nbt.getInteger("Mana");
        manaPoolRecipeType = nbt.getInteger("ManaPoolType");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("Input", input.writeToNBT(new NBTTagCompound()));
        nbt.setTag("Output", output.writeToNBT(new NBTTagCompound()));
        nbt.setInteger("Mana", mana);
        nbt.setInteger("ManaPoolType", manaPoolRecipeType);
        return nbt;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RecipeInfoManaPool) {
            RecipeInfoManaPool other = (RecipeInfoManaPool)obj;
            return ItemStack.areItemStacksEqual(input, other.input) &&
                   ItemStack.areItemStacksEqual(output, other.output) &&
                   manaPoolRecipeType == other.manaPoolRecipeType;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return input.hashCode() * 31 + output.hashCode() + manaPoolRecipeType * 997;
    }

    public static class PackagePatternManaPool implements IPackagePattern {

        protected final RecipeInfoManaPool recipeInfo;
        protected final int index;

        public PackagePatternManaPool(RecipeInfoManaPool recipeInfo, int index) {
            this.recipeInfo = recipeInfo;
            this.index = index;
        }

        @Override
        public List<ItemStack> getInputs() {
            return Collections.singletonList(recipeInfo.input.copy());
        }

        @Override
        public ItemStack getOutput() {
            return ItemPackage.makePackage(recipeInfo, index);
        }

        @Override
        public IRecipeInfo getRecipeInfo() {
            return recipeInfo;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }
}

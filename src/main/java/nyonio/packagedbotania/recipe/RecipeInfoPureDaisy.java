package nyonio.packagedbotania.recipe;

import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.item.ItemPackage;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.RecipePureDaisy;

public class RecipeInfoPureDaisy implements IRecipeInfoPureDaisy {

    protected ItemStack input = ItemStack.EMPTY;
    protected ItemStack output = ItemStack.EMPTY;
    protected RecipePureDaisy recipe;

    public RecipeInfoPureDaisy setRecipe(RecipePureDaisy recipe) {
        this.recipe = recipe;
        this.input = resolveInput(recipe.getInput());
        this.output = stackFromState(recipe.getOutputState());
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
    public RecipePureDaisy getRecipe() {
        return recipe;
    }

    @Override
    public boolean isValid() {
        if(input.isEmpty() || output.isEmpty()) {
            return false;
        }
        if(input.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
            return true;
        }

        // A wildcard input cannot be packaged because PackagedAuto packs concrete stacks.
        // Resolve it to one real variant while preserving the recipe's output.
        for(ItemStack stack : resolveVariants(recipe == null ? null : recipe.getInput())) {
            if(stack != null && !stack.isEmpty() && stack.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
                input = stack.copy();
                input.setCount(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<IPackagePattern> getPatterns() {
        if(!isValid()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new PackagePatternPureDaisy(this, 0));
    }

    @Override
    public IRecipeType getRecipeType() {
        return RecipeTypePureDaisy.INSTANCE;
    }

    @Override
    public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
        this.input = ItemStack.EMPTY;
        this.output = ItemStack.EMPTY;
        this.recipe = null;

        int slot = RecipeTypePureDaisy.SLOT;
        if(input == null || input.size() <= slot) {
            return;
        }

        ItemStack candidate = input.get(slot);
        if(candidate == null || candidate.isEmpty()) {
            return;
        }

        for(RecipePureDaisy recipe : BotaniaAPI.pureDaisyRecipes) {
            if(matchesInput(recipe.getInput(), candidate)) {
                RecipeInfoPureDaisy found = new RecipeInfoPureDaisy().setRecipe(recipe);
                if(found.isValid()) {
                    this.input = candidate.copy();
                    this.input.setCount(1);
                    this.output = found.output;
                    this.recipe = recipe;
                    return;
                }
            }
        }
    }

    @Override
    public Int2ObjectMap<ItemStack> getEncoderStacks() {
        Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
        if(!input.isEmpty()) {
            map.put(RecipeTypePureDaisy.SLOT, input.copy());
        }
        return map;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagCompound inputTag = nbt.getCompoundTag("Input");
        NBTTagCompound outputTag = nbt.getCompoundTag("Output");
        input = inputTag.hasKey("id") ? new ItemStack(inputTag) : ItemStack.EMPTY;
        output = outputTag.hasKey("id") ? new ItemStack(outputTag) : ItemStack.EMPTY;
        recipe = null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("Input", input.writeToNBT(new NBTTagCompound()));
        nbt.setTag("Output", output.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RecipeInfoPureDaisy) {
            RecipeInfoPureDaisy other = (RecipeInfoPureDaisy)obj;
            return ItemStack.areItemStacksEqual(input, other.input)
                    && ItemStack.areItemStacksEqual(output, other.output);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return input.hashCode() * 31 + output.hashCode();
    }

    protected static List<ItemStack> resolveVariants(Object input) {
        if(input instanceof String) {
            return OreDictionary.getOres((String)input, false);
        }
        ItemStack resolved = resolveInput(input);
        return resolved.isEmpty() ? Collections.emptyList() : Collections.singletonList(resolved);
    }

    protected static ItemStack resolveInput(Object input) {
        if(input instanceof ItemStack) {
            return ((ItemStack)input).copy();
        }
        if(input instanceof String) {
            for(ItemStack stack : OreDictionary.getOres((String)input, false)) {
                if(stack != null && !stack.isEmpty()) {
                    return stack.copy();
                }
            }
            return ItemStack.EMPTY;
        }
        if(input instanceof IBlockState) {
            return stackFromState((IBlockState)input);
        }
        if(input instanceof Block) {
            return stackFromState(((Block)input).getDefaultState());
        }
        if(input instanceof Item) {
            return new ItemStack((Item)input);
        }
        return ItemStack.EMPTY;
    }

    protected static ItemStack stackFromState(IBlockState state) {
        if(state == null) {
            return ItemStack.EMPTY;
        }
        Item item = Item.getItemFromBlock(state.getBlock());
        if(item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, 1, state.getBlock().getMetaFromState(state));
    }

    protected static boolean matchesInput(Object expected, ItemStack actual) {
        if(expected instanceof String) {
            for(ItemStack ore : OreDictionary.getOres((String)expected, false)) {
                if(ore != null && OreDictionary.itemMatches(ore, actual, false)) {
                    return true;
                }
            }
            return false;
        }
        ItemStack expectedStack = resolveInput(expected);
        return !expectedStack.isEmpty() && OreDictionary.itemMatches(expectedStack, actual, false);
    }

    public static class PackagePatternPureDaisy implements IPackagePattern {

        protected final RecipeInfoPureDaisy recipeInfo;
        protected final int index;

        public PackagePatternPureDaisy(RecipeInfoPureDaisy recipeInfo, int index) {
            this.recipeInfo = recipeInfo;
            this.index = index;
        }

        @Override
        public List<ItemStack> getInputs() {
            return Collections.singletonList(recipeInfo.getInput().copy());
        }

        @Override
        public ItemStack getOutput() {
            return ItemPackage.makePackage(recipeInfo, index);
        }

        @Override
        public IRecipeInfoPureDaisy getRecipeInfo() {
            return recipeInfo;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }
}

package nyonio.packagedbotania.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import mythicbotany.recipe.InfuserRecipe;
import nyonio.packagedbotania.recipe.RecipeTypeManaInfuser;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.item.ItemPackage;

public class RecipeInfoManaInfuser implements IRecipeInfoManaInfuser {

    protected List<ItemStack> inputs = new ArrayList<>();
    protected ItemStack output = ItemStack.EMPTY;
    protected int mana = 0;

    @Override
    public List<ItemStack> getInputs() {
        return Collections.unmodifiableList(inputs);
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
    public boolean isValid() {
        return !inputs.isEmpty() && !output.isEmpty() && mana > 0;
    }

    @Override
    public List<IPackagePattern> getPatterns() {
        if(!isValid()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new PackagePatternManaInfuser(this, 0));
    }

    @Override
    public IRecipeType getRecipeType() {
        return RecipeTypeManaInfuser.INSTANCE;
    }

    @Override
    public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
        this.inputs.clear();
        this.output = ItemStack.EMPTY;
        this.mana = 0;

        int[] slotArray = RecipeTypeManaInfuser.SLOTS.toIntArray();
        List<ItemStack> collectedInputs = new ArrayList<>();
        for(int i = 0; i < slotArray.length; i++) {
            ItemStack stack = input.get(slotArray[i]);
            if(stack != null && !stack.isEmpty()) {
                collectedInputs.add(stack.copy());
            }
        }
        if(collectedInputs.isEmpty()) {
            return;
        }

        for(InfuserRecipe recipe : InfuserRecipe.getRecipes()) {
            List<ItemStack> recipeInputs = recipe.getInputs();
            if(matchesRecipe(recipeInputs, collectedInputs)) {
                this.inputs = copyStacks(recipeInputs);
                this.output = recipe.getOutput().copy();
                this.mana = recipe.getMana();
                return;
            }
        }
    }

    protected boolean matchesRecipe(List<ItemStack> recipeInputs, List<ItemStack> availableInputs) {
        if(recipeInputs.isEmpty() || recipeInputs.size() != availableInputs.size()) {
            return false;
        }

        int[] remainingCounts = new int[availableInputs.size()];
        for(int i = 0; i < availableInputs.size(); i++) {
            remainingCounts[i] = availableInputs.get(i).getCount();
        }

        for(ItemStack expected : recipeInputs) {
            int needed = expected.getCount();
            for(int i = 0; i < availableInputs.size() && needed > 0; i++) {
                ItemStack actual = availableInputs.get(i);
                if(remainingCounts[i] > 0 && matchesStack(expected, actual)) {
                    int taken = Math.min(needed, remainingCounts[i]);
                    remainingCounts[i] -= taken;
                    needed -= taken;
                }
            }
            if(needed > 0) {
                return false;
            }
        }
        return true;
    }

    protected boolean matchesStack(ItemStack expected, ItemStack actual) {
        return expected.getItem() == actual.getItem()
                && (expected.getMetadata() == Short.MAX_VALUE || expected.getMetadata() == actual.getMetadata())
                && (!expected.hasTagCompound() || ItemStack.areItemStackTagsEqual(expected, actual));
    }

    protected List<ItemStack> copyStacks(List<ItemStack> stacks) {
        List<ItemStack> copies = new ArrayList<>();
        for(ItemStack stack : stacks) {
            copies.add(stack.copy());
        }
        return copies;
    }

    @Override
    public Int2ObjectMap<ItemStack> getEncoderStacks() {
        Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
        int[] slotArray = RecipeTypeManaInfuser.SLOTS.toIntArray();
        for(int i = 0; i < inputs.size() && i < slotArray.length; i++) {
            map.put(slotArray[i], inputs.get(i).copy());
        }
        return map;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        inputs.clear();
        NBTTagList inputList = nbt.getTagList("Inputs", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < inputList.tagCount(); i++) {
            NBTTagCompound tag = inputList.getCompoundTagAt(i);
            if(tag.hasKey("id")) {
                inputs.add(new ItemStack(tag));
            }
        }
        NBTTagCompound outputTag = nbt.getCompoundTag("Output");
        output = outputTag.hasKey("id") ? new ItemStack(outputTag) : ItemStack.EMPTY;
        mana = nbt.getInteger("Mana");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList inputList = new NBTTagList();
        for(ItemStack stack : inputs) {
            inputList.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("Inputs", inputList);
        nbt.setTag("Output", output.writeToNBT(new NBTTagCompound()));
        nbt.setInteger("Mana", mana);
        return nbt;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RecipeInfoManaInfuser) {
            RecipeInfoManaInfuser other = (RecipeInfoManaInfuser)obj;
            return inputs.equals(other.inputs) && ItemStack.areItemStacksEqual(output, other.output);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return inputs.hashCode() * 31 + output.hashCode();
    }

    public static class PackagePatternManaInfuser implements IPackagePattern {

        protected final RecipeInfoManaInfuser recipeInfo;
        protected final int index;

        public PackagePatternManaInfuser(RecipeInfoManaInfuser recipeInfo, int index) {
            this.recipeInfo = recipeInfo;
            this.index = index;
        }

        @Override
        public List<ItemStack> getInputs() {
            return recipeInfo.getInputs();
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

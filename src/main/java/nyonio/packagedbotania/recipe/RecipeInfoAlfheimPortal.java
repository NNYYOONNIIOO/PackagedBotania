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
import net.minecraftforge.oredict.OreDictionary;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.item.ItemPackage;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.RecipeElvenTrade;

public class RecipeInfoAlfheimPortal implements IRecipeInfoAlfheimPortal {

    protected List<ItemStack> outputs = new ArrayList<>();
    protected List<ItemStack> inputs = new ArrayList<>();
    protected int mana = 500;

    public RecipeInfoAlfheimPortal() {
    }

    @Override
    public List<ItemStack> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    @Override
    public List<ItemStack> getInputs() {
        return Collections.unmodifiableList(inputs);
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public boolean isValid() {
        return !outputs.isEmpty() && !inputs.isEmpty();
    }

    @Override
    public List<IPackagePattern> getPatterns() {
        if(!isValid()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new PackagePatternAlfheimPortal(this, 0));
    }

    @Override
    public IRecipeType getRecipeType() {
        return RecipeTypeAlfheimPortal.INSTANCE;
    }

    @Override
    public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
        this.outputs.clear();
        inputs.clear();

        int[] slotArray = RecipeTypeAlfheimPortal.SLOTS.toIntArray();
        for(int i = 0; i < 16 && i < slotArray.length; i++) {
            ItemStack stack = input.get(slotArray[i]);
            if(stack != null && !stack.isEmpty()) {
                inputs.add(stack.copy());
            }
        }

        for(RecipeElvenTrade recipe : BotaniaAPI.elvenTradeRecipes) {
            List<Object> recipeInputs = recipe.getInputs();
            List<ItemStack> remainingInputs = new ArrayList<>(inputs);

            boolean matched = true;
            for(Object recipeInput : recipeInputs) {
                boolean found = false;
                for(int i = 0; i < remainingInputs.size(); i++) {
                    ItemStack stack = remainingInputs.get(i);
                    if(stack.isEmpty()) continue;

                    if(recipeInput instanceof ItemStack) {
                        ItemStack target = (ItemStack)recipeInput;
                        if(stack.getItem() == target.getItem() && (target.getMetadata() == OreDictionary.WILDCARD_VALUE || stack.getMetadata() == target.getMetadata())) {
                            remainingInputs.remove(i);
                            found = true;
                            break;
                        }
                    } else if(recipeInput instanceof String) {
                        List<ItemStack> ores = OreDictionary.getOres((String)recipeInput);
                        for(ItemStack ore : ores) {
                            if(OreDictionary.itemMatches(ore, stack, false)) {
                                remainingInputs.remove(i);
                                found = true;
                                break;
                            }
                        }
                        if(found) break;
                    }
                }
                if(!found) {
                    matched = false;
                    break;
                }
            }

            if(matched && remainingInputs.isEmpty()) {
                this.outputs.clear();
                for(ItemStack out : recipe.getOutputs()) {
                    this.outputs.add(out.copy());
                }
                break;
            }
        }
    }

    @Override
    public Int2ObjectMap<ItemStack> getEncoderStacks() {
        Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
        int[] slotArray = RecipeTypeAlfheimPortal.SLOTS.toIntArray();
        for(int i = 0; i < inputs.size() && i < 16; i++) {
            map.put(slotArray[i], inputs.get(i).copy());
        }
        return map;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        outputs.clear();
        NBTTagList outputList = nbt.getTagList("Outputs", 10);
        for(int i = 0; i < outputList.tagCount(); i++) {
            NBTTagCompound tag = outputList.getCompoundTagAt(i);
            if(tag.hasKey("id")) {
                outputs.add(new ItemStack(tag));
            }
        }
        inputs.clear();
        NBTTagList inputList = nbt.getTagList("Inputs", 10);
        for(int i = 0; i < inputList.tagCount(); i++) {
            NBTTagCompound tag = inputList.getCompoundTagAt(i);
            if(tag.hasKey("id")) {
                inputs.add(new ItemStack(tag));
            }
        }
        mana = nbt.getInteger("Mana");
        if(mana <= 0) {
            mana = 500;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList outputList = new NBTTagList();
        for(ItemStack stack : outputs) {
            outputList.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("Outputs", outputList);

        NBTTagList inputList = new NBTTagList();
        for(ItemStack stack : inputs) {
            inputList.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("Inputs", inputList);

        nbt.setInteger("Mana", mana);
        return nbt;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RecipeInfoAlfheimPortal) {
            RecipeInfoAlfheimPortal other = (RecipeInfoAlfheimPortal)obj;
            return inputs.equals(other.inputs) && outputs.equals(other.outputs);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return inputs.hashCode() * 31 + outputs.hashCode();
    }

    public static class PackagePatternAlfheimPortal implements IPackagePattern {

        protected final RecipeInfoAlfheimPortal recipeInfo;
        protected final int index;

        public PackagePatternAlfheimPortal(RecipeInfoAlfheimPortal recipeInfo, int index) {
            this.recipeInfo = recipeInfo;
            this.index = index;
        }

        @Override
        public List<ItemStack> getInputs() {
            return recipeInfo.inputs;
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

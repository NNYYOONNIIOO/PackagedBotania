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
import net.minecraftforge.oredict.OreDictionary;
import nyonio.packagedbotania.config.PackagedBotaniaConfig;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.item.ItemPackage;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.RecipeRuneAltar;

public class RecipeInfoRuneAltar implements IRecipeInfoRuneAltar {

    protected List<ItemStack> inputs = new ArrayList<>();
    protected ItemStack output = ItemStack.EMPTY;
    protected int mana = 0;
    protected RecipeRuneAltar recipe;

    public RecipeInfoRuneAltar setRecipe(RecipeRuneAltar recipe) {
        this.recipe = recipe;
        this.inputs = new ArrayList<>();
        for(Object obj : recipe.getInputs()) {
            if(obj instanceof ItemStack) {
                this.inputs.add(((ItemStack)obj).copy());
            }
        }
        this.output = recipe.getOutput().copy();
        this.mana = recipe.getManaUsage();
        return this;
    }

    @Override
    public List<ItemStack> getInputs() {
        return inputs;
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
    public RecipeRuneAltar getRecipe() {
        return recipe;
    }

    @Override
    public boolean isValid() {
        return !inputs.isEmpty() && !output.isEmpty();
    }

    @Override
    public List<IPackagePattern> getPatterns() {
        if(!isValid()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new PackagePatternRuneAltar(this, 0));
    }

    @Override
    public IRecipeType getRecipeType() {
        return RecipeTypeRuneAltar.INSTANCE;
    }

    @Override
    public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
        this.inputs.clear();
        this.output = ItemStack.EMPTY;
        this.mana = 0;
        this.recipe = null;
        
        int[] slots = RecipeTypeRuneAltar.SLOTS.toIntArray();
        List<ItemStack> collectedInputs = new ArrayList<>();
        boolean hasCatalyst = false;
        
        for(int i = 0; i < slots.length; i++) {
            ItemStack stack = input.get(slots[i]);
            if(stack != null && !stack.isEmpty()) {
                if(PackagedBotaniaConfig.isRuneAltarCatalyst(stack)) {
                    hasCatalyst = true;
                    continue;
                }
                collectedInputs.add(stack.copy());
            }
        }
        
        if(!hasCatalyst || collectedInputs.isEmpty()) {
            return;
        }
        
        for(RecipeRuneAltar recipe : BotaniaAPI.runeAltarRecipes) {
            if(matchesRecipe(recipe, collectedInputs)) {
                setRecipe(recipe);
                this.inputs = collectedInputs;
                break;
            }
        }
    }
    
    protected boolean matchesRecipe(RecipeRuneAltar recipe, List<ItemStack> inputs) {
        List<Object> recipeInputs = new ArrayList<>(recipe.getInputs());
        List<ItemStack> availableInputs = new ArrayList<>(inputs);
        
        for(ItemStack input : inputs) {
            int stackIndex = -1;
            int oredictIndex = -1;
            
            for(int j = 0; j < recipeInputs.size(); j++) {
                Object recipeInput = recipeInputs.get(j);
                if(recipeInput instanceof String) {
                    for(ItemStack ostack : OreDictionary.getOres((String)recipeInput, false)) {
                        if(OreDictionary.itemMatches(ostack, input, false)) {
                            oredictIndex = j;
                            break;
                        }
                    }
                    if(oredictIndex != -1) break;
                } else if(recipeInput instanceof ItemStack) {
                    ItemStack recipeStack = (ItemStack)recipeInput;
                    if(recipeStack.getItem() == input.getItem() && 
                       (recipeStack.getItemDamage() == input.getItemDamage() || recipeStack.getItemDamage() == OreDictionary.WILDCARD_VALUE)) {
                        stackIndex = j;
                        break;
                    }
                }
            }
            
            if(stackIndex != -1) {
                recipeInputs.remove(stackIndex);
            } else if(oredictIndex != -1) {
                recipeInputs.remove(oredictIndex);
            } else {
                return false;
            }
        }
        
        return recipeInputs.isEmpty();
    }

    @Override
    public Int2ObjectMap<ItemStack> getEncoderStacks() {
        Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
        int[] slots = RecipeTypeRuneAltar.SLOTS.toIntArray();
        map.put(slots[0], PackagedBotaniaConfig.getRuneAltarJEICatalyst());
        for(int i = 0; i < Math.min(inputs.size(), slots.length - 1); i++) {
            map.put(slots[i + 1], inputs.get(i).copy());
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
        if(obj instanceof RecipeInfoRuneAltar) {
            RecipeInfoRuneAltar other = (RecipeInfoRuneAltar)obj;
            return inputs.equals(other.inputs) && ItemStack.areItemStacksEqual(output, other.output);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return inputs.hashCode() * 31 + output.hashCode();
    }

    public static class PackagePatternRuneAltar implements IPackagePattern {

        protected final RecipeInfoRuneAltar recipeInfo;
        protected final int index;

        public PackagePatternRuneAltar(RecipeInfoRuneAltar recipeInfo, int index) {
            this.recipeInfo = recipeInfo;
            this.index = index;
        }

        @Override
        public List<ItemStack> getInputs() {
            List<ItemStack> list = new ArrayList<>();
            list.add(PackagedBotaniaConfig.getRuneAltarJEICatalyst());
            list.addAll(recipeInfo.inputs);
            return list;
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

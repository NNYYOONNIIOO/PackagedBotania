package nyonio.packagedbotania.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.oredict.OreDictionary;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.item.ItemPackage;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.ModItems;

public class RecipeInfoTerraPlate implements IRecipeInfoTerraPlate {

    protected ItemStack output = ItemStack.EMPTY;
    protected List<ItemStack> inputs = new ArrayList<>();
    protected int mana = 500000;
    protected IBlockState multiblockCenter = ModBlocks.livingrock.getDefaultState();
    protected IBlockState multiblockEdge = Blocks.LAPIS_BLOCK.getDefaultState();
    protected IBlockState multiblockCorner = ModBlocks.livingrock.getDefaultState();
    protected IBlockState multiblockCenterReplace = null;
    protected IBlockState multiblockEdgeReplace = null;
    protected IBlockState multiblockCornerReplace = null;

    public RecipeInfoTerraPlate() {
    }

    @Override
    public ItemStack getOutput() {
        return output;
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
    public IBlockState getMultiblockCenter() {
        return multiblockCenter;
    }

    @Override
    public IBlockState getMultiblockEdge() {
        return multiblockEdge;
    }

    @Override
    public IBlockState getMultiblockCorner() {
        return multiblockCorner;
    }

    @Override
    public IBlockState getMultiblockCenterReplace() {
        return multiblockCenterReplace;
    }

    @Override
    public IBlockState getMultiblockEdgeReplace() {
        return multiblockEdgeReplace;
    }

    @Override
    public IBlockState getMultiblockCornerReplace() {
        return multiblockCornerReplace;
    }

    @Override
    public boolean isValid() {
        return !output.isEmpty() && !inputs.isEmpty();
    }

    @Override
    public List<IPackagePattern> getPatterns() {
        if(!isValid()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new PackagePatternTerraPlate(this, 0));
    }

    @Override
    public IRecipeType getRecipeType() {
        return RecipeTypeTerraPlate.INSTANCE;
    }

    @Override
    public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
        this.output = ItemStack.EMPTY;
        inputs.clear();
        resetMultiblock();

        int[] slotArray = RecipeTypeTerraPlate.SLOTS.toIntArray();
        for(int i = 0; i < 16 && i < slotArray.length; i++) {
            ItemStack stack = input.get(slotArray[i]);
            if(stack != null && !stack.isEmpty()) {
                inputs.add(stack.copy());
            }
        }

        if(inputs.isEmpty()) {
            return;
        }

        if(BotaniaTweaksHelper.isLoaded()) {
            IRecipeInfoTerraPlate btRecipe = BotaniaTweaksHelper.findRecipe(inputs);
            if(btRecipe != null) {
                this.output = btRecipe.getOutput();
                this.mana = btRecipe.getMana();
                this.multiblockCenter = btRecipe.getMultiblockCenter();
                this.multiblockEdge = btRecipe.getMultiblockEdge();
                this.multiblockCorner = btRecipe.getMultiblockCorner();
                this.multiblockCenterReplace = btRecipe.getMultiblockCenterReplace();
                this.multiblockEdgeReplace = btRecipe.getMultiblockEdgeReplace();
                this.multiblockCornerReplace = btRecipe.getMultiblockCornerReplace();
                return;
            }
        }

        boolean hasIngot = false;
        boolean hasPearl = false;
        boolean hasDiamond = false;

        for(ItemStack stack : inputs) {
            if(stack.getItem() == ModItems.manaResource) {
                int meta = stack.getItemDamage();
                if(meta == 0) hasIngot = true;
                else if(meta == 1) hasPearl = true;
                else if(meta == 2) hasDiamond = true;
            }
        }

        if(hasIngot && hasPearl && hasDiamond) {
            this.output = new ItemStack(ModItems.manaResource, 1, 4);
            this.mana = 500000;
        }
    }

    protected void resetMultiblock() {
        mana = 500000;
        multiblockCenter = ModBlocks.livingrock.getDefaultState();
        multiblockEdge = Blocks.LAPIS_BLOCK.getDefaultState();
        multiblockCorner = ModBlocks.livingrock.getDefaultState();
        multiblockCenterReplace = null;
        multiblockEdgeReplace = null;
        multiblockCornerReplace = null;
    }

    @Override
    public Int2ObjectMap<ItemStack> getEncoderStacks() {
        Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
        int[] slotArray = RecipeTypeTerraPlate.SLOTS.toIntArray();
        for(int i = 0; i < inputs.size() && i < 16; i++) {
            map.put(slotArray[i], inputs.get(i).copy());
        }
        return map;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagCompound outputTag = nbt.getCompoundTag("Output");
        output = outputTag.hasKey("id") ? new ItemStack(outputTag) : ItemStack.EMPTY;
        inputs.clear();
        NBTTagList list = nbt.getTagList("Inputs", Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            if(tag.hasKey("id")) {
                inputs.add(new ItemStack(tag));
            }
        }
        mana = nbt.getInteger("Mana");
        if(mana <= 0) {
            mana = 500000;
        }
        readMultiblockFromNBT(nbt);
    }

    protected void readMultiblockFromNBT(NBTTagCompound nbt) {
        multiblockCenter = readBlockState(nbt, "Center");
        multiblockEdge = readBlockState(nbt, "Edge");
        multiblockCorner = readBlockState(nbt, "Corner");
        multiblockCenterReplace = nbt.hasKey("CenterReplace") ? readBlockState(nbt, "CenterReplace") : null;
        multiblockEdgeReplace = nbt.hasKey("EdgeReplace") ? readBlockState(nbt, "EdgeReplace") : null;
        multiblockCornerReplace = nbt.hasKey("CornerReplace") ? readBlockState(nbt, "CornerReplace") : null;
    }

    protected IBlockState readBlockState(NBTTagCompound nbt, String prefix) {
        String name = nbt.getString(prefix + "Block");
        int meta = nbt.getInteger(prefix + "Meta");
        net.minecraft.block.Block block = net.minecraft.block.Block.getBlockFromName(name);
        if(block == null) {
            return ModBlocks.livingrock.getDefaultState();
        }
        return block.getStateFromMeta(meta);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("Output", output.writeToNBT(new NBTTagCompound()));
        NBTTagList list = new NBTTagList();
        for(ItemStack stack : inputs) {
            list.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("Inputs", list);
        nbt.setInteger("Mana", mana);
        writeMultiblockToNBT(nbt);
        return nbt;
    }

    protected void writeMultiblockToNBT(NBTTagCompound nbt) {
        writeBlockState(nbt, "Center", multiblockCenter);
        writeBlockState(nbt, "Edge", multiblockEdge);
        writeBlockState(nbt, "Corner", multiblockCorner);
        if(multiblockCenterReplace != null) {
            writeBlockState(nbt, "CenterReplace", multiblockCenterReplace);
        }
        if(multiblockEdgeReplace != null) {
            writeBlockState(nbt, "EdgeReplace", multiblockEdgeReplace);
        }
        if(multiblockCornerReplace != null) {
            writeBlockState(nbt, "CornerReplace", multiblockCornerReplace);
        }
    }

    protected void writeBlockState(NBTTagCompound nbt, String prefix, IBlockState state) {
        net.minecraft.block.Block block = state.getBlock();
        net.minecraft.util.ResourceLocation name = net.minecraft.block.Block.REGISTRY.getNameForObject(block);
        if(name != null) {
            nbt.setString(prefix + "Block", name.toString());
        }
        nbt.setInteger(prefix + "Meta", block.getMetaFromState(state));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RecipeInfoTerraPlate) {
            RecipeInfoTerraPlate other = (RecipeInfoTerraPlate)obj;
            return inputs.equals(other.inputs) && ItemStack.areItemStacksEqual(output, other.output);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return inputs.hashCode() * 31 + output.hashCode();
    }

    public static class PackagePatternTerraPlate implements IPackagePattern {

        protected final RecipeInfoTerraPlate recipeInfo;
        protected final int index;

        public PackagePatternTerraPlate(RecipeInfoTerraPlate recipeInfo, int index) {
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

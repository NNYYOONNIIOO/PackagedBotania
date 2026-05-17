package nyonio.packagedbotania.tile;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nyonio.packagedbotania.block.BlockApothecaryCrafter;
import nyonio.packagedbotania.recipe.IRecipeInfoApothecary;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.InventoryTileBase;
import vazkii.botania.common.core.handler.ModSounds;

public class TileApothecaryCrafter extends TileAE2Base implements ITickable, IPackageCraftingMachine {

    public static int energyCapacity = 5000;
    public static int energyUsage = 100;
    public static int WATER_CAPACITY = 4000;
    public static int WATER_COST = 1000;

    protected FluidTank waterTank = new FluidTank(WATER_CAPACITY) {
        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return fluid != null && fluid.getFluid() == FluidRegistry.WATER;
        }

        @Override
        public boolean canDrain() {
            return false;
        }

        @Override
        protected void onContentsChanged() {
            markDirty();
            syncTile(false);
        }
    };

    protected boolean isWorking = false;
    protected IRecipeInfoApothecary currentRecipe;
    protected int remainingProgress = 0;
    protected int energyReq = 0;

    public TileApothecaryCrafter() {
        setInventory(new InventoryTileBase(this, 18));
        setEnergyStorage(new EnergyStorage(this, energyCapacity));
    }

    @Override
    protected String getLocalizedName() {
        return I18n.translateToLocal("tile.packaged_botania.apothecary_crafter.name");
    }

    @Override
    protected ItemStack getStackRepresentation() {
        return new ItemStack(BlockApothecaryCrafter.ITEM_INSTANCE);
    }

    @Override
    public String getName() {
        return getLocalizedName();
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer getClientGuiElement(EntityPlayer player, Object... args) {
        return null;
    }

    @Override
    public Container getServerGuiElement(EntityPlayer player, Object... args) {
        return null;
    }

    @Override
    public void update() {
        if(!world.isRemote) {
            if(firstTick) {
                firstTick = false;
                onReady();
            }
            chargeEnergy();
            if(isWorking) {
                tickProcess();
                if(remainingProgress <= 0) {
                    finishProcess();
                    ejectItems();
                }
            }
            if(world.getTotalWorldTime() % 8 == 0) {
                ejectItems();
            }
        }
    }

    protected void tickProcess() {
        int energyNeeded = Math.min(energyUsage, remainingProgress);
        int energyFromStorage = energyStorage.extractEnergy(energyNeeded, false);
        int energyFromAE2 = 0;

        if(energyFromStorage < energyNeeded) {
            energyFromAE2 = extractEnergyFromNetwork(energyNeeded - energyFromStorage, false);
        }

        remainingProgress -= (energyFromStorage + energyFromAE2);
        markDirty();
    }

    protected void finishProcess() {
        if(currentRecipe == null) {
            endProcess();
            return;
        }
        for(int i = 0; i < 16; i++) {
            inventory.stacks.set(i, ItemStack.EMPTY);
        }
        inventory.stacks.set(16, ItemStack.EMPTY);
        inventory.stacks.set(17, currentRecipe.getOutput().copy());
        world.playSound(null, pos, ModSounds.altarCraft, SoundCategory.BLOCKS, 1F, 1F);
        endProcess();
    }

    protected void endProcess() {
        energyReq = 0;
        remainingProgress = 0;
        isWorking = false;
        currentRecipe = null;
        markDirty();
        syncTile(false);
    }

    protected void ejectItems() {
        ItemStack stack = inventory.stacks.get(17);
        if(!stack.isEmpty()) {
            ItemStack rem = ejectItemToNetwork(stack);
            inventory.stacks.set(17, rem);
        }
    }

    protected void chargeEnergy() {
        int energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());

        int energyFromAE2 = extractEnergyFromNetwork(energyRequest, false);
        if(energyFromAE2 > 0) {
            energyStorage.receiveEnergy(energyFromAE2, false);
            energyRequest -= energyFromAE2;
        }

        if(energyRequest > 0) {
            ItemStack energyStack = inventory.stacks.get(16);
            if(!energyStack.isEmpty() && energyStack.hasCapability(net.minecraftforge.energy.CapabilityEnergy.ENERGY, null)) {
                energyStorage.receiveEnergy(energyStack.getCapability(net.minecraftforge.energy.CapabilityEnergy.ENERGY, null).extractEnergy(energyRequest, false), false);
            }
        }
    }

    @Override
    public boolean acceptPackage(IRecipeInfo recipeInfo, List<ItemStack> stacks, EnumFacing facing) {
        if(!isBusy() && recipeInfo.isValid() && recipeInfo instanceof IRecipeInfoApothecary) {
            IRecipeInfoApothecary recipe = (IRecipeInfoApothecary)recipeInfo;

            if(getWaterAmount() >= WATER_COST) {
                currentRecipe = recipe;
                isWorking = true;
                waterTank.drain(WATER_COST, true);
                energyReq = remainingProgress = energyUsage * 10;

                for(int i = 0; i < Math.min(stacks.size(), 17); i++) {
                    inventory.stacks.set(i, stacks.get(i).copy());
                }

                markDirty();
                syncTile(false);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBusy() {
        return isWorking || !inventory.stacks.get(17).isEmpty();
    }

    public int getWaterAmount() {
        FluidStack fluid = waterTank.getFluid();
        return fluid != null ? fluid.amount : 0;
    }

    public int fillWater(int amount, boolean doFill) {
        return waterTank.fill(new FluidStack(FluidRegistry.WATER, amount), doFill);
    }

    public int getWaterCapacity() {
        return WATER_CAPACITY;
    }

    public int getScaledWater(int scale) {
        return scale * getWaterAmount() / WATER_CAPACITY;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(waterTank);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        remainingProgress = nbt.getInteger("Progress");
        energyReq = nbt.getInteger("EnergyReq");
        isWorking = nbt.getBoolean("Working");
        if(nbt.hasKey("WaterTank")) {
            waterTank.readFromNBT(nbt.getCompoundTag("WaterTank"));
        }
        currentRecipe = null;
        if(nbt.hasKey("Recipe")) {
            IRecipeInfo recipe = MiscUtil.readRecipeFromNBT(nbt.getCompoundTag("Recipe"));
            if(recipe instanceof IRecipeInfoApothecary) {
                currentRecipe = (IRecipeInfoApothecary)recipe;
            }
        }
        if(isWorking && currentRecipe == null) {
            endProcess();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Progress", remainingProgress);
        nbt.setInteger("EnergyReq", energyReq);
        nbt.setBoolean("Working", isWorking);
        nbt.setTag("WaterTank", waterTank.writeToNBT(new NBTTagCompound()));
        if(currentRecipe != null) {
            nbt.setTag("Recipe", MiscUtil.writeRecipeToNBT(new NBTTagCompound(), currentRecipe));
        }
        return nbt;
    }

    @Override
    public void readSyncNBT(NBTTagCompound nbt) {
        super.readSyncNBT(nbt);
        isWorking = nbt.getBoolean("Working");
        if(nbt.hasKey("WaterTank")) {
            waterTank.readFromNBT(nbt.getCompoundTag("WaterTank"));
        }
    }

    @Override
    public NBTTagCompound writeSyncNBT(NBTTagCompound nbt) {
        super.writeSyncNBT(nbt);
        nbt.setBoolean("Working", isWorking);
        nbt.setTag("WaterTank", waterTank.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    public int getScaledEnergy(int scale) {
        if(energyStorage.getMaxEnergyStored() <= 0) {
            return 0;
        }
        return scale * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
    }

    public int getScaledProgress(int scale) {
        if(remainingProgress <= 0 || energyReq <= 0) {
            return 0;
        }
        return scale * (energyReq - remainingProgress) / energyReq;
    }
}

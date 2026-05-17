package nyonio.packagedbotania.tile;

import java.util.List;

import com.google.common.base.Predicates;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nyonio.packagedbotania.block.BlockAlfheimPortalCrafter;
import nyonio.packagedbotania.recipe.IRecipeInfoAlfheimPortal;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.InventoryTileBase;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.common.core.handler.ModSounds;

public class TileAlfheimPortalCrafter extends TileAE2Base implements ITickable, IPackageCraftingMachine, ISparkAttachable {

    public static int energyCapacity = 5000;
    public static int energyUsage = 100;
    public static int MANA_COST = 500;

    protected int mana = 0;
    protected boolean isWorking = false;
    protected IRecipeInfoAlfheimPortal currentRecipe;
    protected int totalManaCost = 0;

    public int getManaCapacity() {
        return isWorking ? totalManaCost : 0;
    }

    public TileAlfheimPortalCrafter() {
        setInventory(new InventoryTileBase(this, 18));
        setEnergyStorage(new EnergyStorage(this, energyCapacity));
    }

    @Override
    protected String getLocalizedName() {
        return I18n.translateToLocal("tile.packaged_botania.alfheim_portal_crafter.name");
    }

    @Override
    protected ItemStack getStackRepresentation() {
        return new ItemStack(BlockAlfheimPortalCrafter.ITEM_INSTANCE);
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
                if(energyStorage.extractEnergy(energyUsage, true) >= energyUsage ||
                   extractEnergyFromNetwork(energyUsage, true) >= energyUsage) {
                    int fromStorage = energyStorage.extractEnergy(energyUsage, false);
                    if(fromStorage < energyUsage) {
                        extractEnergyFromNetwork(energyUsage - fromStorage, false);
                    }
                    requestManaFromSparks();
                }
                if(mana >= totalManaCost) {
                    finishProcess();
                    ejectItems();
                }
            }
            if(world.getTotalWorldTime() % 8 == 0) {
                ejectItems();
            }
        }
    }

    protected void requestManaFromSparks() {
        ISparkEntity spark = getAttachedSpark();
        if(spark != null) {
            List<ISparkEntity> sparkEntities = SparkHelper.getSparksAround(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            for(ISparkEntity otherSpark : sparkEntities) {
                if(spark == otherSpark) {
                    continue;
                }
                if(otherSpark.getAttachedTile() != null && otherSpark.getAttachedTile() instanceof IManaPool) {
                    otherSpark.registerTransfer(spark);
                }
            }
        }
    }

    protected void finishProcess() {
        if(currentRecipe == null) {
            endProcess();
            return;
        }
        for(int i = 0; i < 16; i++) {
            inventory.stacks.set(i, ItemStack.EMPTY);
        }
        List<ItemStack> recipeOutputs = currentRecipe.getOutputs();
        if(!recipeOutputs.isEmpty()) {
            inventory.stacks.set(17, recipeOutputs.get(0).copy());
            for(int i = 1; i < recipeOutputs.size(); i++) {
                ejectItemToNetwork(recipeOutputs.get(i).copy());
            }
        }
        world.playSound(null, pos, ModSounds.manaPoolCraft, SoundCategory.BLOCKS, 1, 1);
        endProcess();
    }

    protected void endProcess() {
        isWorking = false;
        currentRecipe = null;
        totalManaCost = 0;
        mana = 0;
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
        if(!isBusy() && recipeInfo.isValid() && recipeInfo instanceof IRecipeInfoAlfheimPortal) {
            IRecipeInfoAlfheimPortal recipe = (IRecipeInfoAlfheimPortal)recipeInfo;

            currentRecipe = recipe;
            isWorking = true;
            totalManaCost = recipe.getMana();

            for(int i = 0; i < Math.min(stacks.size(), 16); i++) {
                inventory.stacks.set(i, stacks.get(i).copy());
            }

            markDirty();
            syncTile(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBusy() {
        return isWorking || !inventory.stacks.get(17).isEmpty();
    }

    @Override
    public int getCurrentMana() {
        return isWorking ? mana : 0;
    }

    @Override
    public boolean isFull() {
        int cap = getManaCapacity();
        return cap <= 0 || mana >= cap;
    }

    @Override
    public void recieveMana(int mana) {
        if(!isWorking) {
            return;
        }
        int cap = getManaCapacity();
        this.mana = Math.max(0, Math.min(cap, this.mana + mana));
        world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
        markDirty();
        syncTile(false);
    }

    @Override
    public boolean canRecieveManaFromBursts() {
        return isWorking && !isFull();
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public void attachSpark(ISparkEntity entity) {}

    @Override
    public ISparkEntity getAttachedSpark() {
        List<Entity> sparks = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.up(), pos.up().add(1, 1, 1)), Predicates.instanceOf(ISparkEntity.class));
        if(sparks.size() == 1) {
            return (ISparkEntity)sparks.get(0);
        }
        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        if(!isWorking) {
            return true;
        }
        int cap = getManaCapacity();
        return cap <= 0 || mana >= cap;
    }

    @Override
    public int getAvailableSpaceForMana() {
        int cap = getManaCapacity();
        return Math.max(0, cap - mana);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        isWorking = nbt.getBoolean("Working");
        mana = nbt.getInteger("Mana");
        totalManaCost = nbt.getInteger("TotalManaCost");
        currentRecipe = null;
        if(nbt.hasKey("Recipe")) {
            IRecipeInfo recipe = MiscUtil.readRecipeFromNBT(nbt.getCompoundTag("Recipe"));
            if(recipe instanceof IRecipeInfoAlfheimPortal) {
                currentRecipe = (IRecipeInfoAlfheimPortal)recipe;
            }
        }
        if(isWorking && currentRecipe == null) {
            endProcess();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("Working", isWorking);
        nbt.setInteger("Mana", mana);
        nbt.setInteger("TotalManaCost", totalManaCost);
        if(currentRecipe != null) {
            nbt.setTag("Recipe", MiscUtil.writeRecipeToNBT(new NBTTagCompound(), currentRecipe));
        }
        return nbt;
    }

    @Override
    public void readSyncNBT(NBTTagCompound nbt) {
        super.readSyncNBT(nbt);
        mana = nbt.getInteger("Mana");
        isWorking = nbt.getBoolean("Working");
    }

    @Override
    public NBTTagCompound writeSyncNBT(NBTTagCompound nbt) {
        super.writeSyncNBT(nbt);
        nbt.setInteger("Mana", mana);
        nbt.setBoolean("Working", isWorking);
        return nbt;
    }

    public int getScaledEnergy(int scale) {
        if(energyStorage.getMaxEnergyStored() <= 0) {
            return 0;
        }
        return scale * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
    }

    public int getScaledMana(int scale) {
        int cap = getManaCapacity();
        return cap > 0 ? scale * mana / cap : 0;
    }
}

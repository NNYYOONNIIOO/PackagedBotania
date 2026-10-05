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
import nyonio.packagedbotania.block.BlockPureDaisyCrafter;
import nyonio.packagedbotania.recipe.IRecipeInfoPureDaisy;
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

public class TilePureDaisyCrafter extends TileAE2Base implements ITickable, IPackageCraftingMachine, ISparkAttachable {

    public static final int PARALLELISM = 8;
    public static final int BASE_WORK_TICKS = 20 * 60;
    public static final int MANA_PER_SECOND = 1000;
    public static final int MANA_PER_SPEEDUP_SECOND = 1000;
    public static int energyCapacity = 5000;
    public static int energyUsage = 100;

    protected int storedMana = 0;
    protected int remainingTicks = 0;
    protected int batchSize = 0;
    protected boolean isWorking = false;
    protected IRecipeInfoPureDaisy currentRecipe;

    public TilePureDaisyCrafter() {
        setInventory(new InventoryTileBase(this, PARALLELISM + 1));
        setEnergyStorage(new EnergyStorage(this, energyCapacity));
    }

    @Override
    protected String getLocalizedName() {
        return I18n.translateToLocal("tile.packaged_botania.pure_daisy_crafter.name");
    }

    @Override
    protected ItemStack getStackRepresentation() {
        return new ItemStack(BlockPureDaisyCrafter.ITEM_INSTANCE);
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
        if(world.isRemote) {
            return;
        }

        ensureProxyReady();
        chargeEnergy();
        if(isWorking) {
            requestManaFromSparks();
            if(energyStorage.extractEnergy(energyUsage, true) >= energyUsage
                    || extractEnergyFromNetwork(energyUsage, true) >= energyUsage) {
                int fromStorage = energyStorage.extractEnergy(energyUsage, false);
                if(fromStorage < energyUsage) {
                    extractEnergyFromNetwork(energyUsage - fromStorage, false);
                }
                consumeManaForSpeedup();
                remainingTicks--;
                if(remainingTicks <= 0) {
                    finishProcess();
                }
            }
        }

        if(world.getTotalWorldTime() % 8 == 0) {
            ejectItems();
        }
    }

    protected void requestManaFromSparks() {
        ISparkEntity spark = getAttachedSpark();
        if(spark == null) {
            return;
        }
        List<ISparkEntity> sparkEntities = SparkHelper.getSparksAround(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        for(ISparkEntity otherSpark : sparkEntities) {
            if(spark != otherSpark && otherSpark.getAttachedTile() instanceof IManaPool) {
                otherSpark.registerTransfer(spark);
            }
        }
    }

    protected void finishProcess() {
        if(currentRecipe == null || batchSize <= 0) {
            endProcess();
            return;
        }
        for(int i = 0; i < batchSize; i++) {
            inventory.stacks.set(i, currentRecipe.getOutput().copy());
        }
        world.playSound(null, pos, ModSounds.manaPoolCraft, SoundCategory.BLOCKS, 0.4F, 4F);
        endProcess();
    }

    protected void endProcess() {
        isWorking = false;
        remainingTicks = 0;
        batchSize = 0;
        currentRecipe = null;
        markDirty();
        syncTile(false);
    }

    protected void ejectItems() {
        if(isWorking) {
            return;
        }
        for(int i = 0; i < PARALLELISM; i++) {
            ItemStack stack = inventory.stacks.get(i);
            if(!stack.isEmpty()) {
                inventory.stacks.set(i, ejectItemToNetwork(stack));
            }
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
            ItemStack energyStack = inventory.stacks.get(PARALLELISM);
            if(!energyStack.isEmpty() && energyStack.hasCapability(net.minecraftforge.energy.CapabilityEnergy.ENERGY, null)) {
                energyStorage.receiveEnergy(energyStack.getCapability(net.minecraftforge.energy.CapabilityEnergy.ENERGY, null).extractEnergy(energyRequest, false), false);
            }
        }
    }

    @Override
    public boolean acceptPackage(IRecipeInfo recipeInfo, List<ItemStack> stacks, EnumFacing facing) {
        if(!(recipeInfo instanceof IRecipeInfoPureDaisy) || !recipeInfo.isValid() || stacks == null || stacks.isEmpty()) {
            return false;
        }

        IRecipeInfoPureDaisy pureDaisyRecipe = (IRecipeInfoPureDaisy)recipeInfo;
        ItemStack input = stacks.get(0);
        if(input == null || input.isEmpty()) {
            return false;
        }
        if(hasOutputItems() || batchSize >= PARALLELISM) {
            return false;
        }
        if(currentRecipe != null && !currentRecipe.equals(pureDaisyRecipe)) {
            return false;
        }

        if(currentRecipe == null) {
            currentRecipe = pureDaisyRecipe;
            isWorking = true;
            remainingTicks = calculateWorkTicks();
        }
        inventory.stacks.set(batchSize, input.copy());
        batchSize++;
        markDirty();
        syncTile(false);
        return true;
    }

    protected int calculateWorkTicks() {
        return BASE_WORK_TICKS;
    }

    protected void consumeManaForSpeedup() {
        if(remainingTicks <= 1 || storedMana < MANA_PER_SPEEDUP_SECOND) {
            return;
        }

        int availableSpeedupSeconds = storedMana / MANA_PER_SPEEDUP_SECOND;
        int requestedSpeedupSeconds = (remainingTicks + 19) / 20;
        int speedupSeconds = Math.min(availableSpeedupSeconds, requestedSpeedupSeconds);
        storedMana -= speedupSeconds * MANA_PER_SPEEDUP_SECOND;
        remainingTicks = Math.max(1, remainingTicks - speedupSeconds * 20);
    }

    protected boolean hasOutputItems() {
        if(isWorking) {
            return false;
        }
        for(int i = 0; i < PARALLELISM; i++) {
            if(!inventory.stacks.get(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBusy() {
        return hasOutputItems() || (isWorking && batchSize >= PARALLELISM);
    }

    @Override
    public int getCurrentMana() {
        return storedMana;
    }

    @Override
    public boolean isFull() {
        return !isWorking || getAvailableSpaceForMana() <= 0;
    }

    @Override
    public void recieveMana(int mana) {
        if(mana == 0) {
            return;
        }
        long nextMana = (long)storedMana + mana;
        storedMana = (int)Math.max(0L, Math.min(Integer.MAX_VALUE, nextMana));
        world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
        markDirty();
        syncTile(false);
    }

    @Override
    public boolean canRecieveManaFromBursts() {
        return isWorking && getAvailableSpaceForMana() > 0;
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
        return !isWorking || getAvailableSpaceForMana() <= 0;
    }

    @Override
    public int getAvailableSpaceForMana() {
        if(!isWorking || remainingTicks <= 0) {
            return 0;
        }
        long remainingSeconds = (remainingTicks + 19L) / 20L;
        long requestedMana = remainingSeconds * MANA_PER_SECOND;
        return (int)Math.max(0L, Math.min(Integer.MAX_VALUE, requestedMana - storedMana));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        storedMana = Math.max(0, nbt.getInteger("StoredMana"));
        remainingTicks = nbt.getInteger("RemainingTicks");
        batchSize = Math.max(0, Math.min(PARALLELISM, nbt.getInteger("BatchSize")));
        isWorking = nbt.getBoolean("Working");
        currentRecipe = null;
        if(nbt.hasKey("Recipe")) {
            IRecipeInfo recipe = MiscUtil.readRecipeFromNBT(nbt.getCompoundTag("Recipe"));
            if(recipe instanceof IRecipeInfoPureDaisy) {
                currentRecipe = (IRecipeInfoPureDaisy)recipe;
            }
        }
        if(isWorking && (currentRecipe == null || remainingTicks <= 0 || batchSize <= 0)) {
            isWorking = false;
            remainingTicks = 0;
            batchSize = 0;
            currentRecipe = null;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("StoredMana", storedMana);
        nbt.setInteger("RemainingTicks", remainingTicks);
        nbt.setInteger("BatchSize", batchSize);
        nbt.setBoolean("Working", isWorking);
        if(currentRecipe != null) {
            nbt.setTag("Recipe", MiscUtil.writeRecipeToNBT(new NBTTagCompound(), currentRecipe));
        }
        return nbt;
    }

    @Override
    public void readSyncNBT(NBTTagCompound nbt) {
        super.readSyncNBT(nbt);
        storedMana = Math.max(0, nbt.getInteger("StoredMana"));
        remainingTicks = nbt.getInteger("RemainingTicks");
        batchSize = Math.max(0, Math.min(PARALLELISM, nbt.getInteger("BatchSize")));
        isWorking = nbt.getBoolean("Working");
    }

    @Override
    public NBTTagCompound writeSyncNBT(NBTTagCompound nbt) {
        super.writeSyncNBT(nbt);
        nbt.setInteger("StoredMana", storedMana);
        nbt.setInteger("RemainingTicks", remainingTicks);
        nbt.setInteger("BatchSize", batchSize);
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
        if(!isWorking) {
            return 0;
        }
        int target = Math.max(MANA_PER_SECOND, (int)Math.min(Integer.MAX_VALUE, ((remainingTicks + 19L) / 20L) * MANA_PER_SECOND));
        return target > 0 ? scale * storedMana / target : 0;
    }
}

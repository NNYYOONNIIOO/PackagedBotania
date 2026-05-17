package nyonio.packagedbotania.tile;

import java.util.EnumSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;
import thelm.packagedauto.tile.TileBase;

@Optional.InterfaceList({
    @Optional.Interface(iface = "appeng.api.networking.IGridHost", modid = "appliedenergistics2"),
    @Optional.Interface(iface = "appeng.me.helpers.IGridProxyable", modid = "appliedenergistics2"),
    @Optional.Interface(iface = "appeng.api.networking.security.IActionHost", modid = "appliedenergistics2")
})
public abstract class TileAE2Base extends TileBase implements IGridHost, IGridProxyable, IActionHost {

    protected AENetworkProxy gridProxy;
    protected IActionSource actionSource;
    protected boolean firstTick = true;

    protected abstract ItemStack getStackRepresentation();

    @Optional.Method(modid = "appliedenergistics2")
    protected ItemStack ejectItemToNetwork(ItemStack stack) {
        if(stack.isEmpty() || !isNetworkConnected()) {
            return stack;
        }
        try {
            IGridNode node = gridProxy.getNode();
            IGrid grid = node.getGrid();
            IItemStorageChannel storageChannel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
            IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
            IMEMonitor<IAEItemStack> inventory = storageGrid.getInventory(storageChannel);
            IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
            IAEItemStack aeStack = storageChannel.createStack(stack);
            IAEItemStack rem = AEApi.instance().storage().poweredInsert(energyGrid, inventory, aeStack, actionSource, Actionable.MODULATE);
            if(rem == null || rem.getStackSize() == 0) {
                return ItemStack.EMPTY;
            }
            return rem.createItemStack();
        } catch(Exception e) {
            return stack;
        }
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public AENetworkProxy getProxy() {
        if(this.gridProxy == null) {
            this.gridProxy = new AENetworkProxy(this, "proxy", getStackRepresentation(), true);
            this.gridProxy.setValidSides(EnumSet.allOf(EnumFacing.class));
            this.gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            this.actionSource = new MachineSource(this);
        }
        return this.gridProxy;
    }

    @Optional.Method(modid = "appliedenergistics2")
    protected void onReady() {
        this.getProxy().onReady();
    }

    @Optional.Method(modid = "appliedenergistics2")
    protected boolean isNetworkConnected() {
        if(gridProxy == null || gridProxy.getNode() == null) {
            return false;
        }
        IGridNode node = gridProxy.getNode();
        return node.isActive() && node.getGrid() != null;
    }

    @Optional.Method(modid = "appliedenergistics2")
    protected int extractEnergyFromNetwork(int maxExtract, boolean simulate) {
        if(!isNetworkConnected()) {
            return 0;
        }
        try {
            IGridNode node = gridProxy.getNode();
            IGrid grid = node.getGrid();
            IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
            
            double available = energyGrid.getStoredPower();
            double toExtract = Math.min(available, maxExtract);
            
            if(simulate) {
                return (int) toExtract;
            }
            
            double extracted = energyGrid.extractAEPower(toExtract, Actionable.MODULATE, appeng.api.config.PowerMultiplier.ONE);
            return (int) extracted;
        } catch(Exception e) {
            return 0;
        }
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public void validate() {
        super.validate();
        this.getProxy().validate();
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public void invalidate() {
        super.invalidate();
        if(this.gridProxy != null) {
            this.gridProxy.invalidate();
        }
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if(this.gridProxy != null) {
            this.gridProxy.onChunkUnload();
        }
    }

    @Nullable
    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public IGridNode getGridNode(@Nonnull AEPartLocation dir) {
        return getProxy().getNode();
    }

    @Nonnull
    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public void gridChanged() {
    }

    @Nonnull
    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Nonnull
    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public IGridNode getActionableNode() {
        return getProxy().getNode();
    }

    @Optional.Method(modid = "appliedenergistics2")
    @Override
    public void securityBreak() {
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        firstTick = true;
        getProxy().readFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        getProxy().writeToNBT(nbt);
        return nbt;
    }
}

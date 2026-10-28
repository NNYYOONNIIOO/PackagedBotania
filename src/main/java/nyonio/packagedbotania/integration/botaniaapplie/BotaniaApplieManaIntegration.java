package nyonio.packagedbotania.integration.botaniaapplie;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import net.minecraftforge.fml.common.Optional;
import nyonio.ae2.ManaStack;
import nyonio.ae2.ManaStorageChannel;

public final class BotaniaApplieManaIntegration {

    private BotaniaApplieManaIntegration() {
    }

    @Optional.Method(modid = "botania_applie")
    public static int extractMana(IGrid grid, IActionSource source, int amount, boolean simulate) {
        if(grid == null || source == null || amount <= 0) {
            return 0;
        }

        IStorageGrid storage = grid.getCache(IStorageGrid.class);
        if(storage == null) {
            return 0;
        }

        IMEInventory<ManaStack> inventory = storage.getInventory(ManaStorageChannel.INSTANCE);
        if(inventory == null) {
            return 0;
        }

        Actionable mode = simulate ? Actionable.SIMULATE : Actionable.MODULATE;
        ManaStack extracted = inventory.extractItems(new ManaStack(amount), mode, source);
        if(extracted == null) {
            return 0;
        }
        return (int)Math.min((long)amount, extracted.getStackSize());
    }
}

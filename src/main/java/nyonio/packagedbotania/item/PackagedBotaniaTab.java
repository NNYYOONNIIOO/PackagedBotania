package nyonio.packagedbotania.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import nyonio.packagedbotania.PackagedBotania;
import nyonio.packagedbotania.block.BlockRuneAltarCrafter;

public class PackagedBotaniaTab extends CreativeTabs {

    public static final PackagedBotaniaTab INSTANCE = new PackagedBotaniaTab();

    public PackagedBotaniaTab() {
        super(PackagedBotania.MODID);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(BlockRuneAltarCrafter.ITEM_INSTANCE);
    }
}

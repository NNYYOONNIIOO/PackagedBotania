package nyonio.packagedbotania.item;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nyonio.packagedbotania.PackagedBotania;
import nyonio.packagedbotania.block.BlockAlfheimPortalCrafter;
import nyonio.packagedbotania.block.BlockApothecaryCrafter;
import nyonio.packagedbotania.block.BlockManaPoolCrafter;
import nyonio.packagedbotania.block.BlockRuneAltarCrafter;
import nyonio.packagedbotania.block.BlockTerraPlateCrafter;
import nyonio.packagedbotania.client.IModelRegister;

import java.util.ArrayList;
import java.util.List;

public class ItemPackagedBotania {

    public static final Item ITEM_MANA_POOL_CRAFTER = BlockManaPoolCrafter.ITEM_INSTANCE;
    public static final Item ITEM_RUNE_ALTAR_CRAFTER = BlockRuneAltarCrafter.ITEM_INSTANCE;
    public static final Item ITEM_TERRA_PLATE_CRAFTER = BlockTerraPlateCrafter.ITEM_INSTANCE;
    public static final Item ITEM_APOTHECARY_CRAFTER = BlockApothecaryCrafter.ITEM_INSTANCE;
    public static final Item ITEM_ALFHEIM_PORTAL_CRAFTER = BlockAlfheimPortalCrafter.ITEM_INSTANCE;
}

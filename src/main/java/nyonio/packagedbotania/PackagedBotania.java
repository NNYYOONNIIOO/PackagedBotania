package nyonio.packagedbotania;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import nyonio.packagedbotania.block.BlockAlfheimPortalCrafter;
import nyonio.packagedbotania.block.BlockApothecaryCrafter;
import nyonio.packagedbotania.block.BlockManaPoolCrafter;
import nyonio.packagedbotania.block.BlockRuneAltarCrafter;
import nyonio.packagedbotania.block.BlockTerraPlateCrafter;
import nyonio.packagedbotania.config.PackagedBotaniaConfig;
import nyonio.packagedbotania.event.CommonEventHandler;
import org.apache.logging.log4j.Logger;

@Mod(modid = PackagedBotania.MODID, name = PackagedBotania.NAME, version = PackagedBotania.VERSION, dependencies = PackagedBotania.DEPENDENCIES)
public class PackagedBotania
{
    public static final String MODID = "packaged_botania";
    public static final String NAME = "PackagedBotania";
    public static final String VERSION = "1.0";
    public static final String DEPENDENCIES = "required-after:botania;required-after:packagedauto;after:botania_tweaks";

    public static Logger logger;

    public static final BlockManaPoolCrafter MANA_POOL_CRAFTER = BlockManaPoolCrafter.INSTANCE;
    public static final BlockRuneAltarCrafter RUNE_ALTAR_CRAFTER = BlockRuneAltarCrafter.INSTANCE;
    public static final BlockTerraPlateCrafter TERRA_PLATE_CRAFTER = BlockTerraPlateCrafter.INSTANCE;
    public static final BlockApothecaryCrafter APOTHECARY_CRAFTER = BlockApothecaryCrafter.INSTANCE;
    public static final BlockAlfheimPortalCrafter ALFHEIM_PORTAL_CRAFTER = BlockAlfheimPortalCrafter.INSTANCE;

    @SidedProxy(
            clientSide = "nyonio.packagedbotania.client.event.ClientEventHandler",
            serverSide = "nyonio.packagedbotania.event.CommonEventHandler",
            modId = MODID)
    public static CommonEventHandler eventHandler;

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        PackagedBotaniaConfig.init(event.getSuggestedConfigurationFile());
        eventHandler.onPreInit(event);
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
        eventHandler.onInit(event);
    }
}

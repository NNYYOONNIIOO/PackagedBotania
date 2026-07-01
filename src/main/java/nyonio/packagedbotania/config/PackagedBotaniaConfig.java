package nyonio.packagedbotania.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nyonio.packagedbotania.PackagedBotania;
import vazkii.botania.common.block.ModBlocks;

public class PackagedBotaniaConfig {

    private PackagedBotaniaConfig() {}

    public static Configuration config;

    public static List<ItemStack> runeAltarCatalysts = new ArrayList<>();
    public static List<ItemStack> apothecaryCatalysts = new ArrayList<>();

    public static String[] defaultRuneAltarCatalysts = new String[]{"botania:livingrock:0"};
    public static String[] defaultApothecaryCatalysts = new String[]{
        "minecraft:wheat_seeds:0",
        "minecraft:pumpkin_seeds:0",
        "minecraft:melon_seeds:0",
        "minecraft:beetroot_seeds:0"
    };

    public static void init(File file) {
        MinecraftForge.EVENT_BUS.register(PackagedBotaniaConfig.class);
        config = new Configuration(file);
        config.load();
        init();
    }

    public static void init() {
        String[] runeAltarStrings = config.get(
            "catalysts", "rune_altar",
            defaultRuneAltarCatalysts,
            "Catalyst items for Packaged Rune Altar. First item is used for JEI import. Format: modid:itemid:metadata. Empty list uses defaults."
        ).getStringList();

        String[] apothecaryStrings = config.get(
            "catalysts", "apothecary",
            defaultApothecaryCatalysts,
            "Catalyst items for Packaged Apothecary. First item is used for JEI import. Format: modid:itemid:metadata. Empty list uses defaults."
        ).getStringList();

        runeAltarCatalysts = parseItemStacks(
            runeAltarStrings.length == 0 ? defaultRuneAltarCatalysts : runeAltarStrings
        );
        apothecaryCatalysts = parseItemStacks(
            apothecaryStrings.length == 0 ? defaultApothecaryCatalysts : apothecaryStrings
        );

        if(config.hasChanged()) {
            config.save();
        }
    }

    protected static List<ItemStack> parseItemStacks(String[] strings) {
        List<ItemStack> list = new ArrayList<>();
        for(String s : strings) {
            ItemStack stack = parseItemStack(s);
            if(!stack.isEmpty()) {
                list.add(stack);
            }
        }
        return list;
    }

    protected static ItemStack parseItemStack(String s) {
        String[] parts = s.split(":");
        if(parts.length < 2) {
            return ItemStack.EMPTY;
        }
        String modid = parts[0];
        String name = parts[1];
        int meta = 0;
        if(parts.length >= 3) {
            try {
                meta = Integer.parseInt(parts[2]);
            } catch(NumberFormatException e) {
                meta = 0;
            }
        }
        Item item = Item.REGISTRY.getObject(new ResourceLocation(modid, name));
        if(item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, 1, meta);
    }

    public static boolean isRuneAltarCatalyst(ItemStack stack) {
        // Hard-coded fallback: directly check against Botania's livingrock block
        Block livingrock = Block.getBlockFromName("botania:livingrock");
        if(livingrock != null && stack.getItem() == Item.getItemFromBlock(livingrock) && stack.getMetadata() == 0) {
            return true;
        }
        for(ItemStack catalyst : runeAltarCatalysts) {
            if(stack.getItem() == catalyst.getItem() &&
               (catalyst.getMetadata() == Short.MAX_VALUE || stack.getMetadata() == catalyst.getMetadata())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isApothecaryCatalyst(ItemStack stack) {
        for(ItemStack catalyst : apothecaryCatalysts) {
            if(stack.getItem() == catalyst.getItem() &&
               (catalyst.getMetadata() == Short.MAX_VALUE || stack.getMetadata() == catalyst.getMetadata())) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack getRuneAltarJEICatalyst() {
        if(!runeAltarCatalysts.isEmpty()) {
            return runeAltarCatalysts.get(0).copy();
        }
        // Hard-coded fallback: directly create ItemStack from Botania's livingrock block
        Block livingrock = Block.getBlockFromName("botania:livingrock");
        if(livingrock != null) {
            return new ItemStack(livingrock, 1, 0);
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getApothecaryJEICatalyst() {
        return apothecaryCatalysts.isEmpty() ? ItemStack.EMPTY : apothecaryCatalysts.get(0).copy();
    }

    @SubscribeEvent
    public static void onConfigChanged(OnConfigChangedEvent event) {
        if(event.getModID().equals(PackagedBotania.MODID)) {
            init();
        }
    }
}

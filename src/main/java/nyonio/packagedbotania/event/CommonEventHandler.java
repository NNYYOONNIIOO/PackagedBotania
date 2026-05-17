package nyonio.packagedbotania.event;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import nyonio.packagedbotania.PackagedBotania;
import nyonio.packagedbotania.block.BlockAlfheimPortalCrafter;
import nyonio.packagedbotania.block.BlockApothecaryCrafter;
import nyonio.packagedbotania.block.BlockApothecaryCrafterPart;
import nyonio.packagedbotania.block.BlockManaPoolCrafter;
import nyonio.packagedbotania.block.BlockRuneAltarCrafter;
import nyonio.packagedbotania.block.BlockTerraPlateCrafter;
import nyonio.packagedbotania.client.IModelRegister;
import nyonio.packagedbotania.recipe.RecipeTypeAlfheimPortal;
import nyonio.packagedbotania.recipe.RecipeTypeApothecary;
import nyonio.packagedbotania.recipe.RecipeTypeManaPool;
import nyonio.packagedbotania.recipe.RecipeTypeManaPoolAlchemy;
import nyonio.packagedbotania.recipe.RecipeTypeManaPoolConjuration;
import nyonio.packagedbotania.recipe.RecipeTypeRuneAltar;
import nyonio.packagedbotania.recipe.RecipeTypeTerraPlate;
import nyonio.packagedbotania.tile.TileAlfheimPortalCrafter;
import nyonio.packagedbotania.tile.TileApothecaryCrafter;
import nyonio.packagedbotania.tile.TileManaPoolCrafter;
import nyonio.packagedbotania.tile.TileRuneAltarCrafter;
import nyonio.packagedbotania.tile.TileTerraPlateCrafter;
import nyonio.packagedbotania.network.PackagedBotaniaPacketHandler;
import thelm.packagedauto.api.RecipeTypeRegistry;

import java.util.ArrayList;
import java.util.List;

public class CommonEventHandler {

    protected List<IModelRegister> modelRegisterList = new ArrayList<>();

    public void registerBlock(Block block) {
        ForgeRegistries.BLOCKS.register(block);
        if(block instanceof IModelRegister) {
            modelRegisterList.add((IModelRegister)block);
        }
    }

    public void registerItem(Item item) {
        ForgeRegistries.ITEMS.register(item);
        if(item instanceof IModelRegister) {
            modelRegisterList.add((IModelRegister)item);
        }
    }

    public void onPreInit(FMLPreInitializationEvent event) {
        registerBlocks();
        registerItems();
        registerTileEntities();
        registerRecipeTypes();
        PackagedBotaniaPacketHandler.registerPackets();
    }

    public void onInit(FMLInitializationEvent event) {
        PackagedBotania.logger.info("{} has been loaded!", PackagedBotania.NAME);
    }

    protected void registerBlocks() {
        registerBlock(BlockManaPoolCrafter.INSTANCE);
        registerBlock(BlockRuneAltarCrafter.INSTANCE);
        registerBlock(BlockTerraPlateCrafter.INSTANCE);
        registerBlock(BlockApothecaryCrafter.INSTANCE);
        registerBlock(BlockApothecaryCrafterPart.INSTANCE);
        registerBlock(BlockAlfheimPortalCrafter.INSTANCE);
    }

    protected void registerItems() {
        registerItem(BlockManaPoolCrafter.ITEM_INSTANCE);
        registerItem(BlockRuneAltarCrafter.ITEM_INSTANCE);
        registerItem(BlockTerraPlateCrafter.ITEM_INSTANCE);
        registerItem(BlockApothecaryCrafter.ITEM_INSTANCE);
        registerItem(BlockAlfheimPortalCrafter.ITEM_INSTANCE);
    }

    protected void registerTileEntities() {
        GameRegistry.registerTileEntity(TileManaPoolCrafter.class, BlockManaPoolCrafter.INSTANCE.getRegistryName());
        GameRegistry.registerTileEntity(TileRuneAltarCrafter.class, BlockRuneAltarCrafter.INSTANCE.getRegistryName());
        GameRegistry.registerTileEntity(TileTerraPlateCrafter.class, BlockTerraPlateCrafter.INSTANCE.getRegistryName());
        GameRegistry.registerTileEntity(TileApothecaryCrafter.class, BlockApothecaryCrafter.INSTANCE.getRegistryName());
        GameRegistry.registerTileEntity(TileAlfheimPortalCrafter.class, BlockAlfheimPortalCrafter.INSTANCE.getRegistryName());
    }

    protected void registerRecipeTypes() {
        RecipeTypeRegistry.registerRecipeType(RecipeTypeManaPool.INSTANCE);
        RecipeTypeRegistry.registerRecipeType(RecipeTypeManaPoolAlchemy.INSTANCE);
        RecipeTypeRegistry.registerRecipeType(RecipeTypeManaPoolConjuration.INSTANCE);
        RecipeTypeRegistry.registerRecipeType(RecipeTypeRuneAltar.INSTANCE);
        RecipeTypeRegistry.registerRecipeType(RecipeTypeTerraPlate.INSTANCE);
        RecipeTypeRegistry.registerRecipeType(RecipeTypeApothecary.INSTANCE);
        RecipeTypeRegistry.registerRecipeType(RecipeTypeAlfheimPortal.INSTANCE);
    }
}

package nyonio.packagedbotania.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nyonio.packagedbotania.item.PackagedBotaniaTab;
import nyonio.packagedbotania.tile.TileManaInfuserCrafter;

public class BlockManaInfuserCrafter extends BlockBase {

    public static final BlockManaInfuserCrafter INSTANCE = new BlockManaInfuserCrafter();
    public static final Item ITEM_INSTANCE = new ItemBlock(INSTANCE).setRegistryName("packaged_botania:mana_infuser_crafter");
    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packaged_botania:mana_infuser_crafter#normal");

    protected BlockManaInfuserCrafter() {
        super(Material.GLASS);
        setHardness(3.0F);
        setResistance(5.0F);
        setUnlocalizedName("packaged_botania.mana_infuser_crafter");
        setRegistryName("packaged_botania:mana_infuser_crafter");
        setCreativeTab(PackagedBotaniaTab.INSTANCE);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileManaInfuserCrafter();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(ITEM_INSTANCE, 0, MODEL_LOCATION);
    }
}

package nyonio.packagedbotania.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nyonio.packagedbotania.item.PackagedBotaniaTab;
import nyonio.packagedbotania.tile.TileBreweryCrafter;

public class BlockBreweryCrafter extends BlockBase {

    public static final BlockBreweryCrafter INSTANCE = new BlockBreweryCrafter();
    public static final Item ITEM_INSTANCE = new ItemBlock(INSTANCE).setRegistryName("packaged_botania:brewery_crafter");
    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packaged_botania:brewery_crafter#normal");

    protected static final AxisAlignedBB AABB = new AxisAlignedBB(0, 0, 0, 1, 1, 1);

    protected BlockBreweryCrafter() {
        super(Material.ROCK);
        setHardness(3.0F);
        setResistance(5.0F);
        setUnlocalizedName("packaged_botania.brewery_crafter");
        setRegistryName("packaged_botania:brewery_crafter");
        setCreativeTab(PackagedBotaniaTab.INSTANCE);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, net.minecraft.world.IBlockAccess source, net.minecraft.util.math.BlockPos pos) {
        return AABB;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileBreweryCrafter();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(ITEM_INSTANCE, 0, MODEL_LOCATION);
    }
}

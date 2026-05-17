package nyonio.packagedbotania.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockApothecaryCrafterPart extends BlockBase {

    public static final BlockApothecaryCrafterPart INSTANCE = new BlockApothecaryCrafterPart();
    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packaged_botania:apothecary_crafter_part#normal");
    
    protected static final AxisAlignedBB AABB = new AxisAlignedBB(0, -1, 0, 1, 1, 1);

    protected BlockApothecaryCrafterPart() {
        super(Material.GLASS);
        setHardness(3.0F);
        setResistance(5.0F);
        setUnlocalizedName("packaged_botania.apothecary_crafter_part");
        setRegistryName("packaged_botania:apothecary_crafter_part");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, net.minecraft.world.IBlockAccess source, BlockPos pos) {
        return AABB;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        BlockPos below = pos.down();
        IBlockState belowState = worldIn.getBlockState(below);
        if(belowState.getBlock() == BlockApothecaryCrafter.INSTANCE) {
            worldIn.setBlockToAir(below);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        BlockPos below = pos.down();
        IBlockState belowState = worldIn.getBlockState(below);
        if(belowState.getBlock() == BlockApothecaryCrafter.INSTANCE) {
            return belowState.getBlock().onBlockActivated(worldIn, below, belowState, playerIn, hand, facing, hitX, hitY + 1, hitZ);
        }
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        BlockPos below = pos.down();
        IBlockState belowState = worldIn.getBlockState(below);
        if(belowState.getBlock() == BlockApothecaryCrafter.INSTANCE) {
            if(!player.capabilities.isCreativeMode) {
                BlockApothecaryCrafter.INSTANCE.dropBlockAsItem(worldIn, below, belowState, 0);
            }
            worldIn.setBlockToAir(below);
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels() {
    }
}

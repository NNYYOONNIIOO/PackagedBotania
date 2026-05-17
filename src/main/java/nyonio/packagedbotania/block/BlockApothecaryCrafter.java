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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nyonio.packagedbotania.item.PackagedBotaniaTab;
import nyonio.packagedbotania.tile.TileApothecaryCrafter;

public class BlockApothecaryCrafter extends BlockBase {

    public static final BlockApothecaryCrafter INSTANCE = new BlockApothecaryCrafter();
    public static final Item ITEM_INSTANCE = new ItemBlock(INSTANCE).setRegistryName("packaged_botania:apothecary_crafter");
    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packaged_botania:apothecary_crafter#normal");
    
    protected static final AxisAlignedBB AABB = new AxisAlignedBB(0, 0, 0, 1, 2, 1);

    protected BlockApothecaryCrafter() {
        super(Material.GLASS);
        setHardness(3.0F);
        setResistance(5.0F);
        setUnlocalizedName("packaged_botania.apothecary_crafter");
        setRegistryName("packaged_botania:apothecary_crafter");
        setCreativeTab(PackagedBotaniaTab.INSTANCE);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, net.minecraft.world.IBlockAccess source, BlockPos pos) {
        return AABB;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        if(!worldIn.isRemote) {
            BlockPos above = pos.up();
            if(worldIn.isAirBlock(above)) {
                worldIn.setBlockState(above, BlockApothecaryCrafterPart.INSTANCE.getDefaultState());
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        BlockPos above = pos.up();
        IBlockState aboveState = worldIn.getBlockState(above);
        if(aboveState.getBlock() == BlockApothecaryCrafterPart.INSTANCE) {
            worldIn.setBlockToAir(above);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && worldIn.isAirBlock(pos.up());
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileApothecaryCrafter) {
            TileApothecaryCrafter apothecary = (TileApothecaryCrafter) tile;
            ItemStack heldItem = playerIn.getHeldItem(hand);
            if(!heldItem.isEmpty()) {
                if(heldItem.getItem().getRegistryName().toString().equals("minecraft:water_bucket")) {
                    int filled = apothecary.fillWater(1000, true);
                    if(filled > 0 && !playerIn.capabilities.isCreativeMode) {
                        playerIn.setHeldItem(hand, new ItemStack(net.minecraft.init.Items.BUCKET));
                    }
                    return true;
                }
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileApothecaryCrafter();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(ITEM_INSTANCE, 0, MODEL_LOCATION);
    }
}

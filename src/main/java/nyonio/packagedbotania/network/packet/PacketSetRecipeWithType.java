package nyonio.packagedbotania.network.packet;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.api.RecipeTypeRegistry;
import thelm.packagedauto.container.ContainerEncoder;
import thelm.packagedauto.inventory.InventoryEncoderPattern;
import thelm.packagedauto.network.ISelfHandleMessage;

public class PacketSetRecipeWithType implements ISelfHandleMessage<IMessage> {

    private Int2ObjectMap<ItemStack> map;
    private ResourceLocation targetTypeName;

    public PacketSetRecipeWithType() {}

    public PacketSetRecipeWithType(Int2ObjectMap<ItemStack> map, ResourceLocation targetTypeName) {
        this.map = map;
        this.targetTypeName = targetTypeName;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(map.size());
        for(Int2ObjectMap.Entry<ItemStack> entry : map.int2ObjectEntrySet()) {
            buf.writeByte(entry.getIntKey());
            MiscUtil.writeItemWithLargeCount(buf, entry.getValue());
        }
        byte[] bytes = targetTypeName.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buf.writeShort(bytes.length);
        buf.writeBytes(bytes);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readByte();
        map = new Int2ObjectOpenHashMap<>(size);
        for(int i = 0; i < size; ++i) {
            int index = buf.readUnsignedByte();
            ItemStack stack = MiscUtil.readItemWithLargeCount(buf);
            map.put(index, stack);
        }
        int length = buf.readUnsignedShort();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        targetTypeName = new ResourceLocation(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
    }

    @Override
    public IMessage onMessage(MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        WorldServer world = player.getServerWorld();
        world.addScheduledTask(()->{
            if(player.openContainer instanceof ContainerEncoder) {
                ContainerEncoder container = (ContainerEncoder)player.openContainer;
                InventoryEncoderPattern inv = container.patternInventory;
                IRecipeType targetType = RecipeTypeRegistry.getRecipeType(targetTypeName);
                if(targetType != null) {
                    inv.recipeType = targetType;
                    inv.validateRecipeType();
                    IntSet enabledSlots = targetType.getEnabledSlots();
                    for(int i = 0; i < 90; ++i) {
                        if(!enabledSlots.contains(i)) {
                            inv.stacks.set(i, ItemStack.EMPTY);
                        }
                    }
                }
                inv.setRecipe(map);
                container.setupSlots();
            }
        });
        return null;
    }
}

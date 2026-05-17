package nyonio.packagedbotania.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import nyonio.packagedbotania.PackagedBotania;
import nyonio.packagedbotania.network.packet.PacketSetRecipeWithType;
import thelm.packagedauto.network.ISelfHandleMessage;

public class PackagedBotaniaPacketHandler implements IMessageHandler<ISelfHandleMessage<? extends IMessage>, IMessage> {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(PackagedBotania.MODID);
    private static int id = 0;

    public static void registerPackets() {
        INSTANCE.registerMessage(new PackagedBotaniaPacketHandler(), PacketSetRecipeWithType.class, id++, Side.SERVER);
    }

    @Override
    public IMessage onMessage(ISelfHandleMessage<? extends IMessage> message, MessageContext ctx) {
        return message.onMessage(ctx);
    }
}

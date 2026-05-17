package nyonio.packagedbotania.client.event;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import nyonio.packagedbotania.client.IModelRegister;
import nyonio.packagedbotania.client.render.RenderTileApothecaryCrafter;
import nyonio.packagedbotania.client.render.RenderTileManaPoolCrafter;
import nyonio.packagedbotania.event.CommonEventHandler;
import nyonio.packagedbotania.tile.TileApothecaryCrafter;
import nyonio.packagedbotania.tile.TileManaPoolCrafter;

public class ClientEventHandler extends CommonEventHandler {

    @Override
    public void onPreInit(FMLPreInitializationEvent event) {
        super.onPreInit(event);
        registerModels();
        registerRenderers();
    }

    protected void registerModels() {
        for(IModelRegister model : modelRegisterList) {
            model.registerModels();
        }
    }

    protected void registerRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileManaPoolCrafter.class, new RenderTileManaPoolCrafter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileApothecaryCrafter.class, new RenderTileApothecaryCrafter());
    }
}

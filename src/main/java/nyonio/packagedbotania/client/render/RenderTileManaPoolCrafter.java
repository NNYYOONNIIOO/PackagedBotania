package nyonio.packagedbotania.client.render;

import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import nyonio.packagedbotania.tile.TileManaPoolCrafter;

public class RenderTileManaPoolCrafter extends TileEntitySpecialRenderer<TileManaPoolCrafter> {

    private static final ResourceLocation MANA_WATER = new ResourceLocation("botania:blocks/mana_water");
    private static final VertexFormat POSITION_TEX_LMAP = new VertexFormat()
            .addElement(DefaultVertexFormats.POSITION_3F)
            .addElement(DefaultVertexFormats.TEX_2F)
            .addElement(DefaultVertexFormats.TEX_2S);

    @Override
    public void render(TileManaPoolCrafter pool, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if(pool == null || !pool.getWorld().isBlockLoaded(pool.getPos(), false)) {
            return;
        }

        int mana = pool.getCurrentMana();
        int cap = TileManaPoolCrafter.MAX_MANA;
        float waterLevel = (float) mana / (float) cap * 0.4F;

        if(waterLevel <= 0) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.translate(x, y, z);

        float s = 1F / 256F * 14F;
        float v = 1F / 8F;
        float w = -v * 3.5F;

        GlStateManager.translate(0.5F, 1.5F, 0.5F);

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.translate(w, -1F - (0.43F - waterLevel), w);
        GlStateManager.rotate(90F, 1F, 0F, 0F);
        GlStateManager.scale(s, s, s);

        tryUseShader();
        TextureAtlasSprite manaWaterSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(MANA_WATER.toString());
        renderIconWithLightmap(0, 0, manaWaterSprite, 16, 16, 240);
        tryReleaseShader();

        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }

    private void tryUseShader() {
        try {
            Class<?> shaderHelperClass = Class.forName("vazkii.botania.client.core.handler.ShaderHelper");
            Method useShader = shaderHelperClass.getMethod("useShader", int.class);
            java.lang.reflect.Field manaPool = shaderHelperClass.getDeclaredField("manaPool");
            manaPool.setAccessible(true);
            int shaderId = manaPool.getInt(null);
            useShader.invoke(null, shaderId);
        } catch(Exception e) {
        }
    }

    private void tryReleaseShader() {
        try {
            Class<?> shaderHelperClass = Class.forName("vazkii.botania.client.core.handler.ShaderHelper");
            Method releaseShader = shaderHelperClass.getMethod("releaseShader");
            releaseShader.invoke(null);
        } catch(Exception e) {
        }
    }

    private void renderIconWithLightmap(int x, int y, TextureAtlasSprite icon, int width, int height, int brightness) {
        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(GL11.GL_QUADS, POSITION_TEX_LMAP);
        tessellator.getBuffer().pos(x + 0, y + height, 0).tex(icon.getMinU(), icon.getMaxV()).lightmap(brightness, brightness).endVertex();
        tessellator.getBuffer().pos(x + width, y + height, 0).tex(icon.getMaxU(), icon.getMaxV()).lightmap(brightness, brightness).endVertex();
        tessellator.getBuffer().pos(x + width, y + 0, 0).tex(icon.getMaxU(), icon.getMinV()).lightmap(brightness, brightness).endVertex();
        tessellator.getBuffer().pos(x + 0, y + 0, 0).tex(icon.getMinU(), icon.getMinV()).lightmap(brightness, brightness).endVertex();
        tessellator.draw();
    }
}

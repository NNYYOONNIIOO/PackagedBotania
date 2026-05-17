package nyonio.packagedbotania.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import org.lwjgl.opengl.GL11;
import nyonio.packagedbotania.tile.TileApothecaryCrafter;

public class RenderTileApothecaryCrafter extends TileEntitySpecialRenderer<TileApothecaryCrafter> {

    @Override
    public void render(TileApothecaryCrafter apothecary, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if(apothecary == null || !apothecary.getWorld().isBlockLoaded(apothecary.getPos(), false)) {
            return;
        }

        int waterAmount = apothecary.getWaterAmount();
        if(waterAmount < TileApothecaryCrafter.WATER_COST) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        GlStateManager.enableRescaleNormal();

        float s = 1F / 256F * 10F;
        float v = 1F / 8F;
        float w = -v * 2.5F;

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        ResourceLocation waterStill = FluidRegistry.WATER.getStill();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();
        GlStateManager.color(1F, 1F, 1F, 0.7F);
        GlStateManager.translate(w, -0.3F, w);
        GlStateManager.rotate(90F, 1F, 0F, 0F);
        GlStateManager.scale(s, s, s);

        renderIcon(0, 0, waterStill, 16, 16);

        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }

    private void renderIcon(int x, int y, ResourceLocation loc, int width, int height) {
        TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(loc.toString());
        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        tessellator.getBuffer().pos(x + 0, y + height, 0).tex(icon.getMinU(), icon.getMaxV()).endVertex();
        tessellator.getBuffer().pos(x + width, y + height, 0).tex(icon.getMaxU(), icon.getMaxV()).endVertex();
        tessellator.getBuffer().pos(x + width, y + 0, 0).tex(icon.getMaxU(), icon.getMinV()).endVertex();
        tessellator.getBuffer().pos(x + 0, y + 0, 0).tex(icon.getMinU(), icon.getMinV()).endVertex();
        tessellator.draw();
    }
}

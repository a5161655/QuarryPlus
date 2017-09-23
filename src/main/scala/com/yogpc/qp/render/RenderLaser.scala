package com.yogpc.qp.render

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.tile.TileLaser
import net.minecraft.client.model.{ModelBase, ModelRenderer}
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.Loader
import org.lwjgl.opengl.GL11

/**
  * See<br>
  * [[buildcraft.silicon.client.render.RenderLaser]]<br>
  * [[net.minecraftforge.client.model.animation.FastTESR]]
  */
object RenderLaser extends TileEntitySpecialRenderer[TileLaser] {
    val instance = this

    private val bcLoaded = Loader isModLoaded QuarryPlus.Optionals.Buildcraft_modID

    override def render(te: TileLaser, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float): Unit = {
        /*Minecraft.getMinecraft.mcProfiler.startSection("quarryplus")
        Minecraft.getMinecraft.mcProfiler.startSection("laser")
        if (bcLoaded) {
            if (te.lasers != null) {
                val tessellator = Tessellator.getInstance
                val vertexBuffer = tessellator.getBuffer
                this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
                RenderHelper.disableStandardItemLighting()
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GlStateManager.enableBlend()
                GlStateManager.disableCull()

                if (Minecraft.isAmbientOcclusionEnabled) GlStateManager.shadeModel(GL11.GL_SMOOTH)
                else GlStateManager.shadeModel(GL11.GL_FLAT)

                vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK)

                vertexBuffer.setTranslation(x - te.getPos.getX, y - te.getPos.getY, z - te.getPos.getZ)

                for (vector <- te.lasers) {
                    val side = te.getWorld.getBlockState(te.getPos).getValue(ADismCBlock.FACING)
                    val offset = new Vec3d(0.5, 0.5, 0.5).add(new Vec3d(side.getDirectionVec).scale(4 / 16D))
                    val index = if (te.getAvg <= 1) 0 else if (te.getAvg <= 2) 1 else if (te.getAvg <= 3) 2 else 3
                    val laser = new LaserData_BC8(BuildCraftLaserManager.POWERS(index), new Vec3d(te.getPos).add(offset), vector, 1 / 16D)
                    LaserRenderer_BC8.renderLaserDynamic(laser, vertexBuffer)
                }

                vertexBuffer.setTranslation(0, 0, 0)

                tessellator.draw()

                RenderHelper.enableStandardItemLighting()
            }
        } else if (te != null && te.lasers != null) {
            GL11.glPushMatrix()
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240) // TODO lightmap

            GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
            GL11.glEnable(GL11.GL_CULL_FACE)
            GL11.glEnable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glTranslated(x - te.getPos.getX, y - te.getPos.getY, z - te.getPos.getZ)
            for (l <- te.lasers) {
                if (l != null) {
                    val facing = te.getWorld.getBlockState(te.getPos).getValue(ADismCBlock.FACING)
                    renderLaser(this.rendererDispatcher.renderEngine,
                        te.getPos.getX + 0.5 + 0.3 * facing.getFrontOffsetX,
                        te.getPos.getY + 0.5 + 0.3 * facing.getFrontOffsetY,
                        te.getPos.getZ + 0.5 + 0.3 * facing.getFrontOffsetZ,
                        l.xCoord, l.yCoord, l.zCoord,
                        (te.getWorld.getWorldTime % 40).toInt, te.getTexture)
                }
            }
            GL11.glPopAttrib()
            GL11.glPopMatrix()
        }

        Minecraft.getMinecraft.mcProfiler.endSection()
        Minecraft.getMinecraft.mcProfiler.endSection()*/
    }

    private val model = new ModelBase() {}
    private val box = new Array[ModelRenderer](40)

    for (i <- box.indices) {
        box(i) = new ModelRenderer(model, box.length - i, 0)
        box(i).addBox(0, -0.5F, -0.5F, 16, 1, 1)
    }

    def renderLaser(tm: TextureManager, fx: Double, fy: Double, fz: Double, tx: Double, ty: Double, tz: Double, b: Int, tex: ResourceLocation): Unit = {
        GL11.glPushMatrix()
        GL11.glTranslated(tx, ty, tz)
        val dx = tx - fx
        val dy = ty - fy
        val dz = tz - fz
        val total = Math.sqrt(dx * dx + dy * dy + dz * dz)
        GL11.glRotatef((360 - (Math.atan2(dz, dx) * 180.0 / Math.PI + 180.0)).toFloat, 0, 1, 0)
        GL11.glRotatef((-Math.atan2(dy, Math.sqrt(total * total - dy * dy)) * 180.0 / Math.PI).toFloat, 0, 0, 1)
        tm.bindTexture(tex)
        var i = 0
        while (i <= total - 1) {
            box(b).render(1F / 16)
            GL11.glTranslated(1, 0, 0)
            i += 1
        }
        GL11.glScaled(total - i, 1, 1)
        box(b).render(1F / 16)
        GL11.glPopMatrix()
    }
}

package com.vincenthuto.hemomancy.client.data;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;

/**
 * The client's half of the Ringmaster finale's hidden crossing (2026-11-18). When the server says the crossing is now,
 * the last frame of the real tent is copied and the overworld's sky is noted; the transition screen shows that frame
 * unchanged while his Chamber loads, and the first frames there fade it out over {@link #FADE_FRAMES}. The noted sky is
 * what the copy's mask sky draws until the merge begins.
 */
public final class FinaleCrossingClientState {
	public static final int FADE_FRAMES = 8;
	/** The most frames the held frame waits for the copy's sections to be built before it fades regardless. */
	public static final int BUILD_WAIT_CAP = 60;

	/** The overworld's sky at the crossing: what the copy shows past the canvas until his mind breaks through. */
	public record OverworldSky(long dayTime, Vec3 sky, int moonPhase, float stars) {
	}

	@Nullable
	private static TextureTarget held;
	private static boolean arrived;
	private static int fadeLeft;
	private static int waited;
	@Nullable
	private static OverworldSky sky;

	private FinaleCrossingClientState() {
	}

	/** The crossing is now: keep the last frame of the real tent and the sky over it. */
	public static void capture() {
		Minecraft mc = Minecraft.getInstance();
		release();
		RenderTarget main = mc.getMainRenderTarget();
		TextureTarget copy = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
		GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
		GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, copy.frameBufferId);
		GL30.glBlitFramebuffer(0, 0, main.width, main.height, 0, 0, copy.width, copy.height, GL11.GL_COLOR_BUFFER_BIT,
				GL11.GL_NEAREST);
		main.bindWrite(true);
		// The merge's portal needs a stencil; enabling one resizes the main target, which would black out a frame
		// mid-fight. Behind the held frame nobody sees it.
		if (!main.isStencilEnabled()) main.enableStencil();
		held = copy;
		arrived = false;
		fadeLeft = FADE_FRAMES;
		waited = 0;
		ClientLevel level = mc.level;
		if (level != null && mc.gameRenderer.getMainCamera() != null) {
			float partial = com.vincenthuto.hemomancy.client.render.AnimationTime.partialTick();
			Vec3 at = mc.gameRenderer.getMainCamera().getPosition();
			sky = new OverworldSky(level.getDayTime(), level.getSkyColor(at, partial), level.getMoonPhase(),
					level.getStarBrightness(partial));
		}
	}

	/** Whether the crossing is still being hidden: the held frame is up or fading. */
	public static boolean crossing() {
		return held != null;
	}

	@Nullable
	public static OverworldSky sky() {
		return sky;
	}

	/** The new level has arrived: from the next frame the held one starts to fade. */
	public static void arrived() {
		arrived = true;
	}

	/** The transition screen draws the held frame unchanged, full screen. */
	public static boolean drawHeld(GuiGraphics graphics) {
		if (held == null) return false;
		blit(graphics, held, 1.0F);
		return true;
	}

	/** The HUD's last layer: the held frame fading out over the first frames in the copy. */
	public static void renderFade(GuiGraphics graphics) {
		if (held == null) return;
		// Arrived once the client stands in his Chamber with no screen up, whichever way the crossing screen closed.
		Minecraft mc = Minecraft.getInstance();
		if (!arrived && mc.screen == null && mc.level != null
				&& mc.level.dimension().equals(com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.CHAMBER_OF_WILL))
			arrived = true;
		if (!arrived) return;
		// Held whole until the copy is built where the camera and his opening look: a section not yet compiled shows
		// as his empty sky, which would give the crossing away.
		if (fadeLeft == FADE_FRAMES && waited < BUILD_WAIT_CAP && !built()) {
			waited++;
			blit(graphics, held, 1.0F);
			return;
		}
		if (fadeLeft <= 0) {
			release();
			return;
		}
		blit(graphics, held, fadeLeft / (float) FADE_FRAMES);
		fadeLeft--;
	}

	private static boolean built() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.levelRenderer == null) return true;
		// His copy has reached this client, and every section queued round the arrival has been built.
		var ringmaster = RingmasterFinaleClientData.ringmaster();
		return ringmaster != null && mc.levelRenderer.isSectionCompiled(ringmaster.blockPosition())
				&& mc.levelRenderer.hasRenderedAllSections();
	}

	/** Lets the held frame go; the noted sky stays for the mask until the finale ends. */
	public static void release() {
		if (held != null) held.destroyBuffers();
		held = null;
	}

	/** The finale is over: forget the noted sky as well. */
	public static void clear() {
		release();
		sky = null;
	}

	private static void blit(GuiGraphics graphics, TextureTarget target, float alpha) {
		int w = graphics.guiWidth(), h = graphics.guiHeight();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, target.getColorTextureId());
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
		var pose = graphics.pose().last().pose();
		var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		// A framebuffer's texture is upside down to the screen.
		buffer.addVertex(pose, 0, h, 0).setUv(0, 0);
		buffer.addVertex(pose, w, h, 0).setUv(1, 0);
		buffer.addVertex(pose, w, 0, 0).setUv(1, 1);
		buffer.addVertex(pose, 0, 0, 0).setUv(0, 1);
		BufferUploader.drawWithShader(buffer.buildOrThrow());
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
	}
}

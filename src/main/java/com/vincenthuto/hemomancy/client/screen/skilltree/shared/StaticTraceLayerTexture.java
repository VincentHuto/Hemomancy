package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/** Shared cache and draw lifecycle for static, pannable progress-screen trace textures. */
final class StaticTraceLayerTexture {
	private static final AtomicInteger NEXT_ID = new AtomicInteger();

	private final ResourceLocation textureLocation;
	private DynamicTexture texture;
	private Object signature;
	private int textureW;
	private int textureH;

	StaticTraceLayerTexture(String name) {
		textureLocation = Hemomancy.rloc("dynamic/" + name + "_" + NEXT_ID.incrementAndGet());
	}

	boolean needsRebuild(Object nextSignature, int contentW, int contentH) {
		int nextW = Math.max(1, contentW);
		int nextH = Math.max(1, contentH);
		if (texture != null && textureW == nextW && textureH == nextH
				&& Objects.equals(signature, nextSignature)) {
			return false;
		}
		signature = nextSignature;
		textureW = nextW;
		textureH = nextH;
		return true;
	}

	NativeImage createImage() {
		return new NativeImage(textureW, textureH, false);
	}

	void upload(NativeImage image) {
		if (texture == null || texture.getPixels() == null
				|| texture.getPixels().getWidth() != textureW
				|| texture.getPixels().getHeight() != textureH) {
			texture = new DynamicTexture(image);
			Minecraft.getInstance().getTextureManager().register(textureLocation, texture);
			return;
		}
		NativeImage target = texture.getPixels();
		for (int y = 0; y < textureH; y++) {
			for (int x = 0; x < textureW; x++) {
				target.setPixelRGBA(x, y, image.getPixelRGBA(x, y));
			}
		}
		texture.upload();
		image.close();
	}

	void render(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom) {
		render(gfx, ctx, panZoom, 1.0F);
	}

	void render(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom, float alpha) {
		if (texture == null) return;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
		PoseStack pose = gfx.pose();
		pose.pushPose();
		pose.translate(ctx.guiLeft() + panZoom.panX, ctx.guiTop() + panZoom.panY, 0);
		pose.scale(panZoom.zoom, panZoom.zoom, 1.0F);
		gfx.blit(textureLocation, 0, 0, 0, 0, textureW, textureH, textureW, textureH);
		pose.popPose();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
	}
}

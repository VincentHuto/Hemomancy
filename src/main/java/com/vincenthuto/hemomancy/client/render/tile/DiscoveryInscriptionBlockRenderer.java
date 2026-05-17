package com.vincenthuto.hemomancy.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.block.harbinger.EngramTextureCache;
import com.vincenthuto.hemomancy.common.block.shared.DiscoveryInscriptionBlock;
import com.vincenthuto.hemomancy.common.block.shared.DiscoveryInscriptionVisuals;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.DiscoveryInscriptionBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class DiscoveryInscriptionBlockRenderer implements BlockEntityRenderer<DiscoveryInscriptionBlockEntity> {
	private static final float CORE_R = 0.80f, CORE_G = 0.05f, CORE_B = 0.05f;
	private static final float BASE_ALPHA = 0.72f;
	private static final float PULSE_AMP = 0.18f;
	private static final float PULSE_SPEED = 0.04f;
	private static final float LOW_FACE = (float) DiscoveryInscriptionVisuals.LOW_FACE;
	private static final float HIGH_FACE = (float) DiscoveryInscriptionVisuals.HIGH_FACE;

	public DiscoveryInscriptionBlockRenderer(BlockEntityRendererProvider.Context ctx) {
		EngramTextureCache.loadAll();
	}

	@Override
	public void render(DiscoveryInscriptionBlockEntity be, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		BlockState state = be.getBlockState();
		if (state.getBlock() != BlockInit.blood_echo_inscription.get()) {
			return;
		}

		DiscoveryInscriptionVisuals.Face face = toVisualFace(state.getValue(DiscoveryInscriptionBlock.FACING));
		int engramIndex = be.getEngramIndex();

		EngramTextureCache.loadAll();
		boolean[][] pixels = EngramTextureCache.getPixels(engramIndex);
		int[][] colors = EngramTextureCache.getColors(engramIndex);
		if (pixels == null || colors == null) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		long tick = mc.level != null ? mc.level.getGameTime() : 0L;
		emitGlowParticles(be, pixels, colors, face, tick);

		float gameTime = tick + partialTick;
		float pulse = BASE_ALPHA + PULSE_AMP * Mth.sin(gameTime * PULSE_SPEED);
		Matrix4f mat = poseStack.last().pose();
		VertexConsumer vcCore = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE);

		for (int px = 0; px < 16; px++) {
			for (int py = 0; py < 16; py++) {
				if (!pixels[px][py]) {
					continue;
				}
				int argb = colors[px][py];
				float brightness = (((argb >> 16) & 0xFF) + ((argb >> 8) & 0xFF) + (argb & 0xFF)) / 765.0f;
				float scale = 0.55f + 0.45f * brightness;
				emitFacingPixel(vcCore, mat, face, px, py,
						CORE_R * scale, CORE_G * scale, CORE_B * scale, pulse * 0.92f);
			}
		}
	}

	private static void emitGlowParticles(DiscoveryInscriptionBlockEntity be, boolean[][] pixels, int[][] colors,
			DiscoveryInscriptionVisuals.Face face, long tick) {
		Level level = be.getLevel();
		if (level == null || !be.shouldEmitClientGlow(tick)) {
			return;
		}

		BlockPos pos = be.getBlockPos();
		for (int px = 0; px < 16; px++) {
			for (int py = 0; py < 16; py++) {
				if (!pixels[px][py]) {
					continue;
				}
				DiscoveryInscriptionVisuals.PixelCenter center = DiscoveryInscriptionVisuals.particleCenter(face, px, py);
				int argb = colors[px][py];
				int r = (argb >> 16) & 0xFF;
				int g = (argb >> 8) & 0xFF;
				int b = argb & 0xFF;
				level.addParticle(GlowParticleFactory.createData(new ParticleColor(r, g, b)),
						pos.getX() + center.x(), pos.getY() + center.y(), pos.getZ() + center.z(),
						0.0D, 0.0D, 0.0D);
			}
		}
	}

	private static DiscoveryInscriptionVisuals.Face toVisualFace(Direction facing) {
		return switch (facing) {
			case NORTH -> DiscoveryInscriptionVisuals.Face.NORTH;
			case SOUTH -> DiscoveryInscriptionVisuals.Face.SOUTH;
			case EAST -> DiscoveryInscriptionVisuals.Face.EAST;
			case WEST -> DiscoveryInscriptionVisuals.Face.WEST;
			default -> DiscoveryInscriptionVisuals.Face.SOUTH;
		};
	}

	private static void emitFacingPixel(VertexConsumer vc, Matrix4f mat, DiscoveryInscriptionVisuals.Face face,
			int px, int py, float r, float g, float b, float a) {
		float y0 = (15 - py) / 16.0f;
		float y1 = (16 - py) / 16.0f;
		float u0 = px / 16.0f;
		float u1 = (px + 1) / 16.0f;
		float um0 = (15 - px) / 16.0f;
		float um1 = (16 - px) / 16.0f;

		switch (face) {
			case SOUTH -> emitQuad(vc, mat,
					u0, y0, LOW_FACE, u1, y0, LOW_FACE,
					u1, y1, LOW_FACE, u0, y1, LOW_FACE,
					r, g, b, a);
			case NORTH -> emitQuad(vc, mat,
					um0, y0, HIGH_FACE, um1, y0, HIGH_FACE,
					um1, y1, HIGH_FACE, um0, y1, HIGH_FACE,
					r, g, b, a);
			case EAST -> emitQuad(vc, mat,
					LOW_FACE, y0, u0, LOW_FACE, y0, u1,
					LOW_FACE, y1, u1, LOW_FACE, y1, u0,
					r, g, b, a);
			case WEST -> emitQuad(vc, mat,
					HIGH_FACE, y0, um0, HIGH_FACE, y0, um1,
					HIGH_FACE, y1, um1, HIGH_FACE, y1, um0,
					r, g, b, a);
		}
	}

	private static void emitQuad(VertexConsumer vc, Matrix4f mat,
			float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3,
			float x4, float y4, float z4,
			float r, float g, float b, float a) {
		vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a);
		vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a);
		vc.addVertex(mat, x3, y3, z3).setColor(r, g, b, a);
		vc.addVertex(mat, x4, y4, z4).setColor(r, g, b, a);
	}

	@Override
	public int getViewDistance() {
		return 48;
	}
}

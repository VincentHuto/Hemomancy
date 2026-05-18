package com.vincenthuto.hemomancy.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.block.harbinger.EngramTextureCache;
import com.vincenthuto.hemomancy.common.block.inscription.DiscoveryInscriptionBlock;
import com.vincenthuto.hemomancy.common.block.inscription.DiscoveryInscriptionVisuals;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.DiscoveryInscriptionBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DiscoveryInscriptionBlockRenderer implements BlockEntityRenderer<DiscoveryInscriptionBlockEntity> {
	private static final ParticleColor DISCOVERY_GLOW_COLOR = new ParticleColor(255, 100, 0);

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
		if (pixels == null) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		long tick = mc.level != null ? mc.level.getGameTime() : 0L;
		emitGlowParticles(be, pixels, face, tick);
	}

	private static void emitGlowParticles(DiscoveryInscriptionBlockEntity be, boolean[][] pixels,
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
				level.addParticle(GlowParticleFactory.createData(DISCOVERY_GLOW_COLOR),
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

	@Override
	public int getViewDistance() {
		return 48;
	}
}

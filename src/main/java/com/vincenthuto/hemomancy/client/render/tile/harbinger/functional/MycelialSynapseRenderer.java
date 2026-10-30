package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.data.HeartNodeClientState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.HeartNodeSpreadGeometry;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.MycelialSynapseBlock;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MycelialSynapseBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lights a Mycelial Synapse's crystal bulb when the heart-node's pulse reaches it. The flare time comes from the
 * synapse's run along the threads from the heart and the pulse's speed (the spread shader's own sum), so no packet
 * is needed. Only the owner sees the flare, and only while their Pulse setting is on, as with the spread. A synapse
 * the client sees go inert plays a short wither: its lit crystal shrinks into the dull one and grey spores lift.
 */
public class MycelialSynapseRenderer implements BlockEntityRenderer<MycelialSynapseBlockEntity> {
	private static final ParticleColor ASH = new ParticleColor(150, 140, 128);
	/** Ticks a seen wither takes. */
	private static final int WITHER_TICKS = 30;
	/** How far the lit bulb stands proud of the baked one, at rest and at a full flare. */
	private static final float PROUD = 0.015F;
	private static final float FLARE_SWELL = 0.06F;
	private static final float FLARE_LIGHT = 11.0F;
	private static final float REST_LIGHT = 4.0F;

	public MycelialSynapseRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(MycelialSynapseBlockEntity synapse, float partialTick, PoseStack pose,
			MultiBufferSource buffers, int light, int overlay) {
		Level level = synapse.getLevel();
		if (level == null) return;
		BlockState state = synapse.getBlockState();
		Direction facing = state.getValue(MycelialSynapseBlock.FACING);
		if (state.getValue(MycelialSynapseBlock.WITHERED)) {
			renderWither(synapse, level, facing, partialTick, pose, buffers, light);
			return;
		}
		synapse.clientSeenActive = true;
		double flare = flare(synapse.getBlockPos(), AnimationTime.gameTime(level, (double) partialTick));
		if (flare < 0.01D) return;
		drawBulb(pose, buffers, facing, 1.0F + PROUD + FLARE_SWELL * (float) flare,
				HeartNodeParts.lift(light, REST_LIGHT + FLARE_LIGHT * (float) flare));
	}

	/** The pulse's flare at this synapse for the viewer, or 0 when they don't see one. */
	private static double flare(BlockPos pos, double time) {
		for (HeartNodeClientState.Envelope envelope : HeartNodeClientState.envelopes()) {
			if (!envelope.footprint().synapses().contains(pos)) continue;
			if (!envelope.owned() || !envelope.settings().has(HeartNodeSettings.PULSE)) return 0.0D;
			Double arrive = HeartNodeSpreadGeometry.arrivals(envelope.footprint()).get(pos);
			return arrive == null ? 0.0D : HeartNodeSpreadGeometry.flare(time, arrive);
		}
		return 0.0D;
	}

	/** The wither, once, when this client saw the synapse alive and then inert. */
	private static void renderWither(MycelialSynapseBlockEntity synapse, Level level, Direction facing,
			float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
		if (!synapse.clientSeenActive) return;
		if (synapse.clientWitheredAt == Long.MIN_VALUE) {
			synapse.clientWitheredAt = level.getGameTime();
			shedAsh(synapse, level, facing);
		}
		float age = (float) (AnimationTime.gameTime(level, (double) partialTick) - synapse.clientWitheredAt);
		if (age >= WITHER_TICKS) return;
		float left = 1.0F - HemoEasing.easeInCubic(age / WITHER_TICKS);
		// It shrinks from a little proud of the dull crystal to inside it, dimming as it goes.
		drawBulb(pose, buffers, facing, 0.97F + 0.05F * left, HeartNodeParts.lift(light, REST_LIGHT * 2.0F * left));
	}

	private static void drawBulb(PoseStack pose, MultiBufferSource buffers, Direction facing, float scale, int light) {
		pose.pushPose();
		HeartNodeParts.face(pose, facing);
		HeartNodeParts.scaleAbout(pose, HeartNodeParts.BULB_X, HeartNodeParts.BULB_Y, HeartNodeParts.BULB_Z, scale,
				scale, scale);
		HeartNodeParts.draw(pose, buffers, HeartNodeParts.BULB, light);
		pose.popPose();
	}

	private static void shedAsh(MycelialSynapseBlockEntity synapse, Level level, Direction facing) {
		BlockPos pos = synapse.getBlockPos();
		double reach = HeartNodeParts.BULB_Y - 0.5D;
		double x = pos.getX() + 0.5D + facing.getStepX() * reach;
		double y = pos.getY() + 0.5D + facing.getStepY() * reach;
		double z = pos.getZ() + 0.5D + facing.getStepZ() * reach;
		for (int i = 0; i < 6; i++) {
			level.addParticle(HemoParticleData.sporiticSpore(ASH), x + (level.random.nextDouble() - 0.5D) * 0.2D, y,
					z + (level.random.nextDouble() - 0.5D) * 0.2D, 0.0D, 0.015D + level.random.nextDouble() * 0.01D,
					0.0D);
		}
	}
}

package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.data.HeartNodeClientState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.HeartNodeMomentRenderer;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.HeartNodeBlock;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartNodeBlockEntity;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Draws the heart-node's heart so it beats ({@link HeartNodeBeat}): a double thump on the spread's clock that swells
 * it and lifts a dim inner light, quicker while its owner stands in the heart lobe and slow while they are away or a
 * stranger looks. Each beat sheds a few blood cells and a faint crimson glow. The roots, ribs and fruiting bodies are
 * the block's stage model.
 */
public class HeartNodeRenderer implements BlockEntityRenderer<HeartNodeBlockEntity> {
	private static final ParticleColor CELL = new ParticleColor(186, 18, 38);
	private static final ParticleColor GLOW = new ParticleColor(214, 40, 56);
	/** Block light the heart rests at, and how much a full thump adds. */
	private static final float REST_LIGHT = 3.0F;
	private static final float THUMP_LIGHT = 12.0F;
	/** A hard planting beat swells the heart further than its own. */
	private static final float PLANTING_STRENGTH = 1.7F;
	/** Beats are shed as motes only this close to the camera. */
	private static final double SHED_RANGE_SQR = 24.0D * 24.0D;

	public HeartNodeRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(HeartNodeBlockEntity node, float partialTick, PoseStack pose, MultiBufferSource buffers,
			int light, int overlay) {
		Level level = node.getLevel();
		if (level == null) return;
		BlockState state = node.getBlockState();
		int stage = state.hasProperty(HeartNodeBlock.STAGE) ? state.getValue(HeartNodeBlock.STAGE) : node.stage();
		double time = AnimationTime.gameTime(level, (double) partialTick);
		HeartNodeBeat.Pace pace = pace(node.getBlockPos());
		float thump = HeartNodeBeat.thump(time, pace);
		float planted = HeartNodeMomentRenderer.plantingAge(node.getBlockPos(), partialTick);
		if (planted >= 0.0F) thump = Math.max(thump, PLANTING_STRENGTH * HeartNodeBeat.plantingBeats(planted));
		float scale = HeartNodeBeat.scale(HeartNodeBeat.restingScale(stage), thump);

		pose.pushPose();
		HeartNodeParts.scaleAbout(pose, HeartNodeParts.HEART_X, HeartNodeParts.HEART_Y, HeartNodeParts.HEART_Z, scale,
				scale * (1.0F + 0.03F * thump), scale);
		HeartNodeParts.draw(pose, buffers, HeartNodeParts.HEART,
				HeartNodeParts.lift(light, REST_LIGHT + THUMP_LIGHT * Math.min(1.0F, thump)));
		pose.popPose();
		shed(node, level, time, pace, scale);
	}

	/** How fast this node's heart goes for the player looking at it. */
	private static HeartNodeBeat.Pace pace(BlockPos heart) {
		Player player = Minecraft.getInstance().player;
		Optional<HeartNodeClientState.Envelope> known = HeartNodeClientState.envelopes().stream()
				.filter(envelope -> heart.equals(envelope.footprint().heart())).findFirst();
		if (player == null || known.isEmpty()) return HeartNodeBeat.Pace.SLOW;
		HeartNodeClientState.Envelope envelope = known.get();
		BlockPos feet = player.blockPosition();
		double dx = feet.getX() - heart.getX();
		double dz = feet.getZ() - heart.getZ();
		boolean heartLobe = dx * dx + dz * dz <= HeartNodeRules.HEART_RADIUS * HeartNodeRules.HEART_RADIUS
				&& Math.abs(feet.getY() - heart.getY()) <= HeartNodeRules.VERTICAL_BAND;
		return HeartNodeBeat.pace(envelope.owned(), envelope.footprint().contains(feet), heartLobe);
	}

	/** On each beat's first thump: two or three blood cells and a faint glow from inside the cage. */
	private static void shed(HeartNodeBlockEntity node, Level level, double time, HeartNodeBeat.Pace pace,
			float scale) {
		long beat = HeartNodeBeat.beatIndex(time, pace);
		if (beat == node.lastBeatShed) return;
		boolean first = node.lastBeatShed == Long.MIN_VALUE;
		node.lastBeatShed = beat;
		BlockPos pos = node.getBlockPos();
		Minecraft mc = Minecraft.getInstance();
		if (first || mc.isPaused() || mc.gameRenderer.getMainCamera().getPosition()
				.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > SHED_RANGE_SQR) return;
		RandomSource random = level.random;
		double x = pos.getX() + HeartNodeParts.HEART_X;
		double y = pos.getY() + HeartNodeParts.HEART_Y;
		double z = pos.getZ() + HeartNodeParts.HEART_Z;
		level.addParticle(HemoParticleData.glow(GLOW), x, y, z, 0.0D, 0.004D, 0.0D);
		int cells = 2 + random.nextInt(2);
		for (int i = 0; i < cells; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double reach = 0.12D * scale;
			level.addParticle(HemoParticleData.bloodCell(CELL), x + Math.cos(angle) * reach, y + 0.05D,
					z + Math.sin(angle) * reach, Math.cos(angle) * 0.012D, 0.018D + random.nextDouble() * 0.01D,
					Math.sin(angle) * 0.012D);
		}
	}
}

package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodGougeBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.LuxTraceBlock;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import com.vincenthuto.hemomancy.common.manipulation.BloodFlowVisuals;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket;
import com.vincenthuto.hemomancy.common.world.ground.DarkFieldRules;
import com.vincenthuto.hemomancy.common.world.ground.DarkFields;
import com.vincenthuto.hemomancy.common.world.ground.GroundFunction;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import com.vincenthuto.hemomancy.common.world.ground.SwitchableLights;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Blood Eclipse as a Sculpting of Absorption (core-loop Track 6): held toward a light it can put out, it draws the light
 * in. It snuffs what has an unlit state (candles, candle cakes, campfires), puts out Lux traces and darkens lit gouge
 * runs, nearest first, a couple a pulse, each as the player's edit. Light from blocks that cannot be switched (torches,
 * glowstone, lava) is beyond it. Every light it takes is drawn into a field of dark hanging where it was aimed
 * ({@link DarkFields}, core-loop round two P8): its time is the light taken ({@link DarkFieldRules#charge}), scaled by
 * the pulse's work, so a dry pull hangs a shorter field, never none. Nothing taken, no field.
 */
public final class EclipseSnuffSculpting implements VerbSculpting {
	public static final double BLOOD_PER_PULSE = 5.0D;
	public static final int CADENCE = 10;
	public static final double REACH_SCALE = 1.5D;
	/** Lights it finds around the point it is aimed at. */
	public static final int RADIUS = 2;
	/** Lights one pulse puts out by hand. */
	public static final int PER_PULSE = 2;
	private static final ParticleColor UMBRA = new ParticleColor(40, 20, 60);
	private static final ParticleColor UMBRA_EDGE = new ParticleColor(150, 96, 196);

	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		return target instanceof VerbTarget.Block block && !snuffable(player.level(), block.pos(), 1).isEmpty();
	}

	/** Up to {@code wanted} lights that can be put out within {@link #RADIUS} of {@code around}, nearest first. */
	static List<BlockPos> snuffable(Level level, BlockPos around, int wanted) {
		List<BlockPos> found = new ArrayList<>();
		for (BlockPos pos : BlockPos.betweenClosed(around.offset(-RADIUS, -RADIUS, -RADIUS), around.offset(RADIUS, RADIUS, RADIUS))) {
			if (lit(level.getBlockState(pos))) found.add(pos.immutable());
		}
		found.sort(Comparator.comparingDouble(pos -> pos.distSqr(around)));
		return found.size() > wanted ? found.subList(0, wanted) : found;
	}

	static boolean lit(BlockState state) {
		if (state.getBlock() instanceof LuxTraceBlock) return true;
		if (state.getBlock() instanceof BloodGougeBlock) {
			return state.getValue(BloodGougeBlock.LIT) && state.getValue(BloodGougeBlock.FUNCTION) != GroundFunction.DARK;
		}
		return SwitchableLights.snuffed(state).isPresent();
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (!(target instanceof VerbTarget.Block block) || !(player.level() instanceof ServerLevel level)) return;
		int budget = Math.max(1, Mth.ceil(PER_PULSE * pulse.workScale()));
		int taken = 0;
		Vec3 hand = hand(player);
		for (BlockPos pos : snuffable(level, block.pos(), budget)) {
			int light = snuff(level, player, pos);
			if (light < 0) continue;
			taken += DarkFieldRules.charge(light);
			drawIn(level, player, Vec3.atCenterOf(pos), hand, light);
		}
		if (taken <= 0) return;
		level.playSound(null, block.pos(), SoundEvents.CANDLE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 0.7F);
		level.playSound(null, block.pos(), SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 0.35F, 0.6F);
		if (DarkFields.feed(player, Vec3.atCenterOf(block.pos()), DarkFieldRules.fieldTicks(taken, pulse.workScale())) != null) {
			HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_FIELD_OF_DARK);
		}
	}

	/**
	 * The light taken leaves its source boldly: a dark bloom where it burned, a puff of umbral mist, and a short stream
	 * of dark drawn in toward the hand. All of it is the toolkit's dark glow and umbra forms.
	 */
	private static void drawIn(ServerLevel level, ServerPlayer player, Vec3 light, Vec3 hand, int blockLight) {
		level.sendParticles(HemoParticleData.darkGlow(UMBRA), light.x, light.y, light.z, 16, 0.22D, 0.22D, 0.22D, 0.015D);
		level.sendParticles(HemoParticleData.darkGlow(UMBRA_EDGE), light.x, light.y + 0.1D, light.z, 5, 0.1D, 0.1D, 0.1D, 0.0D);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.UMBRA_MIST, light, light, 0.6D + blockLight * 0.04D, 30);
		// The light drawn in: a short dark stream from where it burned back into the hand.
		BloodFlowVisuals.from(player, light, Double.doubleToLongBits(light.x * 31.0D + light.z) ^ level.getGameTime(),
				BloodFlowPacket.Style.EXTRACTION);
		Vec3 toward = hand.subtract(light);
		double length = toward.length();
		if (length < 0.2D) return;
		Vec3 step = toward.scale(1.0D / length);
		int motes = Math.min(14, 4 + (int) (length * 2.0D));
		for (int i = 0; i < motes; i++) {
			Vec3 at = light.add(toward.scale(i / (double) motes));
			// Count 0 sends the offset as the mote's velocity: each one runs on toward the hand.
			level.sendParticles(HemoParticleData.darkGlow(i % 3 == 0 ? UMBRA_EDGE : UMBRA), at.x, at.y, at.z, 0,
					step.x, step.y, step.z, 0.12D);
		}
	}

	/** About where the drawing hand is: a little in front of the chest, toward the main hand. */
	private static Vec3 hand(ServerPlayer player) {
		Vec3 look = player.getLookAngle();
		Vec3 side = new Vec3(-look.z, 0.0D, look.x).normalize().scale(player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT ? 0.35D : -0.35D);
		return player.getEyePosition().add(look.scale(0.5D)).add(side).add(0.0D, -0.45D, 0.0D);
	}

	/**
	 * Puts out the light at {@code pos} as {@code player}'s edit and returns the block light it gave (read before the
	 * edit), or -1 when it was refused.
	 */
	private static int snuff(ServerLevel level, ServerPlayer player, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		int light = state.getLightEmission(level, pos);
		if (state.getBlock() instanceof LuxTraceBlock) return PlayerBlockEdits.lift(player, pos) != null ? light : -1;
		if (state.getBlock() instanceof BloodGougeBlock) {
			// The cell's clock is forgotten only once the edit is allowed: forgotten first, a refused snuff would still
			// put the run out at its next tick.
			if (!PlayerBlockEdits.replace(player, pos, state.setValue(BloodGougeBlock.LIT, false)
					.setValue(BloodGougeBlock.FUNCTION, GroundFunction.NONE))) return -1;
			GroundNetwork.forgetCell(level, pos);
			return light;
		}
		return SwitchableLights.snuffed(state).map(out -> PlayerBlockEdits.replace(player, pos, out)).orElse(false) ? light : -1;
	}

	@Override
	public int cadence() {
		return CADENCE;
	}

	@Override
	public double bloodPerPulse() {
		return BLOOD_PER_PULSE;
	}

	@Override
	public double reachScale() {
		return REACH_SCALE;
	}
}

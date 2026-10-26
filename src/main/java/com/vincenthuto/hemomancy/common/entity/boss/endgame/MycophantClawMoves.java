package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;

import static com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantMoves.AMBER;
import static com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantMoves.BONE;
import static com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantMoves.CRIMSON;
import static com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantMoves.SPORE;
import static com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantMoves.YOLK;

/** The claws, the belly and the tail: the Rake String, the Brood Settle, the Claw Pin, the Tail Lash and the Hand Sweep. */
final class MycophantClawMoves {
	private static final double RAKE_RADIUS = 6.0D;
	private static final double RAKE_ARC = 80.0D;
	private static final float RAKE_DAMAGE = 7.0F;
	/** The overhead slam comes down this far straight ahead, both claws a block either side of the line. */
	private static final double SLAM_FORWARD = 5.3D;
	private static final double SLAM_RADIUS = 3.0D;
	private static final float SLAM_DAMAGE = 12.0F;
	private static final double SETTLE_RADIUS = 4.5D;
	private static final float SETTLE_DAMAGE = 14.0F;
	private static final double SETTLE_SHOVE = 1.2D;
	/** A stab this close to the claw tip pins. */
	private static final double PIN_REACH = 1.8D;
	/** How far the stab can carry her toward its spot over the active ticks. */
	private static final double PIN_LUNGE = 3.5D;
	private static final float PIN_DAMAGE = 6.0F;
	private static final double TAIL_RADIUS = 6.0D;
	private static final double TAIL_ARC = 150.0D;
	private static final float TAIL_DAMAGE = 8.0F;
	private static final float SWEEP_DAMAGE = 9.0F;
	private static final double SWEEP_INNER = 4.0D;
	private static final double SWEEP_OUTER = 9.0D;
	private static final double SWEEP_ARC = 120.0D;

	private MycophantClawMoves() {
	}

	/**
	 * One tick of the claw moves. {@code tick} counts from the start of the move (the wind-up), as the controller's attack clock
	 * does; the active stage starts at {@code MycophantControllerRules.timeline(attack, phase, variant).windup()}.
	 */
	static void tick(ServerLevel level, MycophantEntity boss, MycophantController controller, MycophantAttack attack, int tick,
			BossMoveTimeline.Stage stage, boolean commit, @Nullable LivingEntity quarry) {
		int phase = boss.getPhase();
		BossMoveTimeline timeline = MycophantControllerRules.timeline(attack, phase, controller.variant());
		int active = tick - timeline.windup();
		switch (attack) {
			case RAKE_STRING -> rakeString(level, boss, controller, phase, tick, active, stage, quarry);
			case BROOD_SETTLE -> settle(level, boss, controller, tick, stage, commit, quarry);
			case CLAW_PIN -> pin(level, boss, controller, timeline, tick, stage, commit, quarry);
			case TAIL_LASH -> tail(level, boss, controller, tick, active, stage, quarry);
			case HAND_SWEEP -> handSweep(level, boss, controller, timeline, tick, active, stage, quarry);
			default -> { }
		}
	}

	// ------------------------------------------------------------------ the Rake String

	private static void rakeString(ServerLevel level, MycophantEntity boss, MycophantController controller, int phase, int tick,
			int active, BossMoveTimeline.Stage stage, @Nullable LivingEntity quarry) {
		int variant = controller.variant();
		int[] hits = MycophantStrikeRules.stringHits(variant, phase);
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			// the first claw lifts and draws back
			if (tick % 4 == 0) BossTelegraphs.cone(level, boss.position(), boss.forward(), RAKE_RADIUS, RAKE_ARC, CRIMSON);
			if (tick % 3 == 0) BossTelegraphs.glow(level, claw(boss, 0), AMBER, 2, 0.2D);
			return;
		}
		if (stage != BossMoveTimeline.Stage.ACTIVE) return;
		// she tracks a little between swings, so a string follows a player who only side-steps
		if (quarry != null) controller.face(quarry.position(), MycophantControllerRules.turnRateDegrees(phase, false));
		for (int i = 0; i < hits.length; i++) {
			boolean slam = MycophantStrikeRules.isSlam(variant, i, hits.length);
			int until = hits[i] - active;
			// the delayed slam's tell: its circle shows through the hesitation, the later rakes flash their wedge
			if (slam && until > 0 && until % 3 == 0) {
				BossTelegraphs.ring(level, slamCenter(boss), SLAM_RADIUS, AMBER, 30);
				BossTelegraphs.glow(level, boss.bodyPoint(0.0D, 3.2D, 3.0D), YOLK, 2, 0.3D);
			} else if (!slam && i >= 2 && until == 3) {
				BossTelegraphs.cone(level, boss.position(), boss.forward(), RAKE_RADIUS, RAKE_ARC, CRIMSON);
			}
			if (until != 0) continue;
			if (slam) slam(level, boss, controller, quarry, i > 0);
			else rake(level, boss, controller, quarry, i, i > 0);
		}
	}

	private static void rake(ServerLevel level, MycophantEntity boss, MycophantController controller, @Nullable LivingEntity quarry,
			int index, boolean chained) {
		Vec3 claw = claw(boss, index);
		Vec3 right = right(boss);
		// each claw rakes across her front toward the other side
		Vec3 across = boss.forward().add(right.scale(index % 2 == 0 ? 0.6D : -0.6D)).normalize();
		PacketHandler.sendClawSlash(claw, across, CRIMSON, false, 2.2F, 48.0D, level);
		BossTelegraphs.bloodCells(level, boss.bodyPoint(0.0D, 0.1D, 3.8D), CRIMSON, 14, 1.4D, 0.12D);
		level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_HURT_OTHER.get(), SoundSource.HOSTILE, 1.4F,
				0.75F + index * 0.08F);
		if (quarry == null) return;
		Vec3 to = offset(boss, quarry.position());
		if (!MycophantStrikeRules.inCone(to.x, to.z, boss.yBodyRot, RAKE_RADIUS, RAKE_ARC * 0.5D)) return;
		if (!land(boss, controller, quarry, RAKE_DAMAGE, chained)) return;
		Vec3 push = flatAway(boss, quarry.position()).add(across).normalize().scale(0.6D);
		quarry.push(push.x, 0.25D, push.z);
		quarry.hurtMarked = true;
	}

	private static void slam(ServerLevel level, MycophantEntity boss, MycophantController controller, @Nullable LivingEntity quarry,
			boolean chained) {
		Vec3 center = slamCenter(boss);
		Vec3 down = boss.forward().add(0.0D, -1.5D, 0.0D).normalize();
		PacketHandler.sendClawSlash(boss.bodyPoint(-1.0D, 1.2D, SLAM_FORWARD), down, CRIMSON, true, 2.6F, 48.0D, level);
		PacketHandler.sendClawSlash(boss.bodyPoint(1.0D, 1.2D, SLAM_FORWARD), down, CRIMSON, true, 2.6F, 48.0D, level);
		BossTelegraphs.ring(level, center, SLAM_RADIUS, CRIMSON, 36);
		BossTelegraphs.bloodCells(level, center.add(0.0D, 0.3D, 0.0D), CRIMSON, 30, 1.6D, 0.2D);
		BossTelegraphs.spores(level, center.add(0.0D, 0.4D, 0.0D), SPORE, 16, 1.4D, 0.06D);
		level.playSound(null, center.x, center.y, center.z, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 2.0F, 0.45F);
		level.playSound(null, center.x, center.y, center.z, SoundInit.ENTITY_MYCOPHANT_HURT_OTHER.get(), SoundSource.HOSTILE, 1.6F, 0.5F);
		if (quarry == null) return;
		Vec3 to = quarry.position().subtract(center);
		if (!MycophantStrikeRules.inCircle(to.x, to.z, SLAM_RADIUS) || Math.abs(to.y) > 2.5D) return;
		if (!land(boss, controller, quarry, SLAM_DAMAGE, chained)) return;
		quarry.setDeltaMovement(quarry.getDeltaMovement().multiply(0.3D, 0.0D, 0.3D).add(0.0D, 0.2D, 0.0D));
		quarry.hurtMarked = true;
	}

	private static Vec3 slamCenter(MycophantEntity boss) {
		return boss.bodyPoint(0.0D, 0.0D, SLAM_FORWARD);
	}

	/** The raking claw of swing {@code index}: left on even swings, right on odd ones. */
	private static Vec3 claw(MycophantEntity boss, int index) {
		return boss.bodyPoint(index % 2 == 0 ? -1.6D : 1.6D, 0.6D, 3.2D);
	}

	// ------------------------------------------------------------------ the Brood Settle

	private static void settle(ServerLevel level, MycophantEntity boss, MycophantController controller, int tick,
			BossMoveTimeline.Stage stage, boolean commit, @Nullable LivingEntity quarry) {
		Vec3 sac = boss.bodyPoint(0.0D, 1.6D, 0.4D);
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			// she rears up on all four claws and the sac burns amber
			boss.setGlow(0.8F + tick / 14.0F);
			if (tick % 2 == 0) BossTelegraphs.glow(level, sac, AMBER, 2 + tick / 5, 0.6D);
			if (tick % 4 == 0) BossTelegraphs.ring(level, boss.position(), SETTLE_RADIUS, AMBER, 36);
			if (tick == 2) level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_AMBIENT.get(), SoundSource.HOSTILE, 1.4F, 0.6F);
			return;
		}
		if (stage == BossMoveTimeline.Stage.RECOVERY) {
			// the belly lies on the floor: the punish window, marked by spores rising off it
			boss.setGlow(0.6F);
			if (tick % 8 == 0) BossTelegraphs.spores(level, boss.bodyPoint(0.0D, 0.6D, 0.0D), SPORE, 4, 1.6D, 0.02D);
			return;
		}
		if (!commit) return;
		BossTelegraphs.ring(level, boss.position(), SETTLE_RADIUS, YOLK, 44);
		BossTelegraphs.ring(level, boss.position(), SETTLE_RADIUS * 0.6D, AMBER, 28);
		BossTelegraphs.bloodCells(level, boss.bodyPoint(0.0D, 0.3D, 0.0D), CRIMSON, 34, 2.4D, 0.18D);
		BossTelegraphs.spores(level, boss.bodyPoint(0.0D, 0.5D, 0.0D), SPORE, 30, 2.6D, 0.08D);
		level.playSound(null, boss.blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 2.2F, 0.4F);
		level.playSound(null, boss.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.6F, 0.4F);
		if (quarry == null) return;
		Vec3 to = offset(boss, quarry.position());
		if (!MycophantStrikeRules.inCircle(to.x, to.z, SETTLE_RADIUS) || quarry.getY() - boss.getY() > 3.0D) return;
		if (!land(boss, controller, quarry, SETTLE_DAMAGE, false)) return;
		Vec3 out = flatAway(boss, quarry.position()).scale(SETTLE_SHOVE);
		quarry.setDeltaMovement(out.x, 0.4D, out.z);
		quarry.hurtMarked = true;
	}

	// ------------------------------------------------------------------ the Claw Pin

	private static void pin(ServerLevel level, MycophantEntity boss, MycophantController controller, BossMoveTimeline timeline,
			int tick, BossMoveTimeline.Stage stage, boolean commit, @Nullable LivingEntity quarry) {
		Vec3 tip = MycophantEntity.holdPoint(boss.position(), boss.yBodyRot, MycophantHoldKind.PIN);
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			// the right front claw rises and points
			Vec3 raised = boss.bodyPoint(1.6D, 2.0D, 2.8D);
			if (tick % 2 == 0) BossTelegraphs.glow(level, raised, AMBER, 2, 0.15D);
			if (tick % 3 == 0 && quarry != null) BossTelegraphs.line(level, raised, quarry.position(), CRIMSON);
			if (tick >= timeline.windup() - 6 && tick % 2 == 0 && quarry != null)
				BossTelegraphs.ring(level, quarry.position(), PIN_REACH, CRIMSON, 14);
			return;
		}
		if (stage != BossMoveTimeline.Stage.ACTIVE || boss.isHolding() || controller.hitLanded()) return;
		Vec3 aim = controller.lockedAim();
		if (aim != null) {
			// the stab carries her in, so the claw tip comes down on the spot she locked
			// how far the spot lies beyond the claw tip's reach (the pin point's forward offset, read at yaw 0)
			double tipForward = MycophantHoldKind.PIN.point(Vec3.ZERO, 0.0F).z;
			double ahead = offset(boss, aim).dot(boss.forward()) - tipForward;
			double step = Mth.clamp(ahead, 0.0D, PIN_LUNGE / Math.max(1, timeline.active()));
			if (step > 0.0D) controller.drag(boss.forward(), step);
			tip = MycophantEntity.holdPoint(boss.position(), boss.yBodyRot, MycophantHoldKind.PIN);
		}
		if (commit) {
			PacketHandler.sendClawSlash(tip.add(0.0D, 1.2D, 0.0D), boss.forward().add(0.0D, -1.0D, 0.0D).normalize(), CRIMSON, false,
					1.6F, 48.0D, level);
			level.playSound(null, tip.x, tip.y, tip.z, SoundInit.ENTITY_MYCOPHANT_HURT_OTHER.get(), SoundSource.HOSTILE, 1.4F, 1.1F);
		}
		if (quarry == null) return;
		Vec3 to = quarry.position().subtract(tip);
		if (!MycophantStrikeRules.inCircle(to.x, to.z, PIN_REACH) || Math.abs(to.y) > 2.0D) return;
		boolean survival = quarry instanceof ServerPlayer player && MycophantStrikeRules.onlySurvival(player.isSpectator(), player.isCreative());
		if (quarry instanceof ServerPlayer && !survival) return;
		if (!land(boss, controller, quarry, PIN_DAMAGE, false)) return;
		BossTelegraphs.bloodCells(level, tip.add(0.0D, 0.4D, 0.0D), CRIMSON, 16, 0.5D, 0.12D);
		BossTelegraphs.ring(level, tip, 1.2D, AMBER, 16);
		if (quarry instanceof ServerPlayer player) boss.beginHold(player, MycophantHoldKind.PIN);
	}

	// ------------------------------------------------------------------ the Tail Lash

	private static void tail(ServerLevel level, MycophantEntity boss, MycophantController controller, int tick, int active,
			BossMoveTimeline.Stage stage, @Nullable LivingEntity quarry) {
		Vec3 back = boss.forward().reverse();
		Vec3 tailTip = boss.bodyPoint(0.0D, 0.9D, -4.2D);
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			// the tendrils coil and the abdomen tilts
			if (tick % 4 == 0) BossTelegraphs.cone(level, boss.position(), back, TAIL_RADIUS, TAIL_ARC, CRIMSON);
			if (tick % 3 == 0) BossTelegraphs.spores(level, tailTip, SPORE, 4, 0.5D, 0.03D);
			if (tick == 2) level.playSound(null, boss.blockPosition(), SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 1.2F, 1.2F);
			return;
		}
		if (stage != BossMoveTimeline.Stage.ACTIVE) return;
		int[] hits = MycophantStrikeRules.tailHits(controller.variant());
		for (int i = 0; i < hits.length; i++) {
			if (active != hits[i]) continue;
			// first swing runs from her right flank across to her left, the second comes back
			Vec3 swing = right(boss).scale(i % 2 == 0 ? -1.0D : 1.0D);
			PacketHandler.sendClawSlash(tailTip, swing, CRIMSON, false, 2.4F, 48.0D, level);
			BossTelegraphs.bloodCells(level, tailTip, CRIMSON, 18, 1.8D, 0.14D);
			level.playSound(null, tailTip.x, tailTip.y, tailTip.z, SoundEvents.PHANTOM_FLAP, SoundSource.HOSTILE, 1.6F, 0.6F + i * 0.15F);
			if (quarry == null) continue;
			Vec3 to = offset(boss, quarry.position());
			if (!MycophantStrikeRules.inRearArc(to.x, to.z, boss.yBodyRot, TAIL_RADIUS, TAIL_ARC)) continue;
			// the tail runs along the floor: a jump clears it
			if (MycophantStrikeRules.clearsSweep(feetAboveGround(level, quarry))) continue;
			if (!land(boss, controller, quarry, TAIL_DAMAGE, i > 0)) return;
			Vec3 push = flatAway(boss, quarry.position()).add(swing).normalize().scale(1.1D);
			quarry.push(push.x, 0.4D, push.z);
			quarry.hurtMarked = true;
		}
	}

	// ------------------------------------------------------------------ the Hand Sweep

	private static void handSweep(ServerLevel level, MycophantEntity boss, MycophantController controller, BossMoveTimeline timeline,
			int tick, int active, BossMoveTimeline.Stage stage, @Nullable LivingEntity quarry) {
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			// the grasping hand swings out wide and low: the struck band reads between the bone and crimson arcs
			if (tick % 4 == 0) {
				BossTelegraphs.cone(level, boss.position(), boss.forward(), SWEEP_OUTER, SWEEP_ARC, CRIMSON);
				BossTelegraphs.cone(level, boss.position(), boss.forward(), SWEEP_INNER, SWEEP_ARC, BONE);
			}
			if (tick % 3 == 0) BossTelegraphs.glow(level, handAt(boss, -SWEEP_ARC * 0.5D), AMBER, 2, 0.25D);
			return;
		}
		if (stage != BossMoveTimeline.Stage.ACTIVE) return;
		// the hand crosses from her left to her right over the active ticks; it strikes what it passes over
		int span = Math.max(1, timeline.active());
		double from = -SWEEP_ARC * 0.5D + SWEEP_ARC * active / span;
		double to = -SWEEP_ARC * 0.5D + SWEEP_ARC * (active + 1) / span;
		Vec3 hand = handAt(boss, to);
		PacketHandler.sendClawSlash(hand.add(0.0D, 0.5D, 0.0D), right(boss), CRIMSON, false, 1.8F, 48.0D, level);
		BossTelegraphs.bloodCells(level, hand, CRIMSON, 6, 0.8D, 0.08D);
		if (active == 0) level.playSound(null, boss.blockPosition(), SoundEvents.PHANTOM_FLAP, SoundSource.HOSTILE, 1.8F, 0.7F);
		if (quarry == null || controller.hitLanded()) return;
		Vec3 off = offset(boss, quarry.position());
		if (!MycophantStrikeRules.inSweep(off.x, off.z, boss.yBodyRot)) return;
		double signed = MycophantStrikeRules.angleFromFacing(off.x, off.z, boss.yBodyRot) * Math.signum(off.dot(right(boss)));
		boolean passing = active == 0 ? signed <= to : signed > from && signed <= to;
		if (!passing || MycophantStrikeRules.clearsSweep(feetAboveGround(level, quarry))) return;
		if (!land(boss, controller, quarry, SWEEP_DAMAGE, false)) return;
		Vec3 push = right(boss).scale(0.9D);
		quarry.push(push.x, 0.3D, push.z);
		quarry.hurtMarked = true;
	}

	/** Where the sweeping hand is at {@code degrees} from her facing (negative to her left), a little inside the outer edge. */
	private static Vec3 handAt(MycophantEntity boss, double degrees) {
		double radians = Math.toRadians(degrees);
		Vec3 dir = boss.forward().scale(Math.cos(radians)).add(right(boss).scale(Math.sin(radians)));
		return boss.position().add(dir.scale(6.5D)).add(0.0D, 0.3D, 0.0D);
	}

	// ------------------------------------------------------------------ shared

	/**
	 * Lands a blow and marks the move's hit. A later blow of the same string cuts the quarry's hurt cooldown first, so
	 * swings a few ticks apart each land in full instead of the second being swallowed.
	 */
	private static boolean land(MycophantEntity boss, MycophantController controller, LivingEntity quarry, float damage, boolean chained) {
		if (chained) quarry.invulnerableTime = 0;
		boolean present = MycophantMoves.strike(boss, quarry, damage);
		controller.markHit();
		return present;
	}

	/** Her right, as {@link MycophantEntity#bodyPoint} has it. */
	private static Vec3 right(MycophantEntity boss) {
		Vec3 f = boss.forward();
		return new Vec3(-f.z, 0.0D, f.x);
	}

	private static Vec3 offset(MycophantEntity boss, Vec3 point) {
		return point.subtract(boss.position());
	}

	private static Vec3 flatAway(MycophantEntity boss, Vec3 point) {
		Vec3 away = point.subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
		return away.lengthSqr() < 1.0E-4D ? boss.forward() : away.normalize();
	}

	/** How high the quarry's feet are above the floor under it; a grounded quarry is at 0. */
	private static double feetAboveGround(ServerLevel level, LivingEntity quarry) {
		if (quarry.onGround()) return 0.0D;
		Vec3 feet = quarry.position();
		HitResult hit = level.clip(new ClipContext(feet, feet.subtract(0.0D, 3.0D, 0.0D), ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, CollisionContext.empty()));
		return hit.getType() == HitResult.Type.MISS ? 3.0D : feet.y - hit.getLocation().y;
	}
}

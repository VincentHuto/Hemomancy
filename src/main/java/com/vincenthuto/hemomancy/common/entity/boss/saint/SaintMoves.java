package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * What every Saint move shares: its damage (scaled by the attack attribute, so the enrage's +30% reaches every move),
 * its damage type and targeting, who its footprint strikes, and the wind-up and reveal floors.
 */
public final class SaintMoves {
	public static final int MELEE_FLOOR = 12;
	public static final int AREA_FLOOR = 20;
	/** A footprint reaching past this, or not attached to the body, makes a move AREA (resolution B-2). */
	public static final double AREA_REACH = 7.0D;

	private SaintMoves() {
	}

	/** base x attack attribute / its base value, so the enrage's +30% reaches every move. */
	public static float damage(SaintTrialEntity saint, float base) {
		AttributeInstance attack = saint.getAttribute(Attributes.ATTACK_DAMAGE);
		if (attack == null) return base;
		return scaled(base, attack.getValue(), attack.getBaseValue());
	}

	public static float scaled(float base, double value, double baseValue) {
		if (baseValue <= 1.0E-6D) return base;
		return (float) (base * value / baseValue);
	}

	/** Damage of the move's type, through {@code ManipulationCombatHelper.canHarmFor}; false if refused. */
	public static boolean hurt(SaintMoveContext ctx, LivingEntity target, float base) {
		ResourceKey<DamageType> type = ctx.move().damageType();
		return hurt(ctx.saint(), target, base, type);
	}

	/** A Saint's damage outside a move (a presence, a hazard), of {@code type} or the Saint's own when null. */
	public static boolean hurt(SaintTrialEntity saint, LivingEntity target, float base, ResourceKey<DamageType> type) {
		if (base <= 0.0F || !ManipulationCombatHelper.canHarmFor(saint, target)) return false;
		ResourceKey<DamageType> key = type != null ? type : saint.damageType();
		DamageSource source = saint.damageSources().source(key, saint);
		boolean hurt = target.hurt(source, damage(saint, base));
		if (hurt) saint.controller().markHit();
		return hurt;
	}

	/**
	 * Challengers and CHALLENGERS-allegiance companions inside the footprint now: height band and ground flag tested,
	 * occlusion tested for {@link BossFootprint.Occluded} parts, each part striking a target at most once per move (or
	 * once per {@code hitEvery} ticks).
	 */
	public static List<LivingEntity> struck(SaintMoveContext ctx) {
		BossFootprint shape = ctx.move().footprint(ctx.inputs());
		if (shape instanceof BossFootprint.None) return List.of();
		List<BossFootprint.Union.Part> parts = shape instanceof BossFootprint.Union union ? union.parts()
				: List.of(new BossFootprint.Union.Part(shape, 0));
		List<LivingEntity> candidates = candidates(ctx);
		List<LivingEntity> struck = new ArrayList<>();
		double age = ctx.age();
		SaintCombatController controller = ctx.saint().controller();
		for (int part = 0; part < parts.size(); part++) {
			BossFootprint.Union.Part piece = parts.get(part);
			if (age < piece.delay()) continue;
			double partAge = age - piece.delay();
			for (LivingEntity candidate : candidates) {
				if (!piece.shape().hits(ctx.frame(), candidate.position(), candidate.onGround(), partAge)) continue;
				if (occluded(piece.shape()) && !clearSight(ctx, candidate)) continue;
				if (!controller.mayStrike(part, candidate, ctx.tick(), ctx.move().hitEvery())) continue;
				if (!struck.contains(candidate)) struck.add(candidate);
			}
		}
		return struck;
	}

	/** Whether a block ray from the frame origin at eye height to the target's body centre is clear. */
	public static boolean clearSight(SaintMoveContext ctx, LivingEntity target) {
		Vec3 eye = ctx.frame().origin().add(0.0D, ctx.saint().getEyeHeight(), 0.0D);
		Vec3 body = target.getBoundingBox().getCenter();
		return ctx.level().clip(new ClipContext(eye, body, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
				ctx.saint())).getType() == HitResult.Type.MISS;
	}

	public static BossMoveTimeline floored(SaintMove.Kind kind, BossMoveTimeline timeline) {
		int floor = revealFloor(kind);
		if (timeline.windup() >= floor) return timeline;
		return new BossMoveTimeline(floor, timeline.active(), timeline.recovery());
	}

	/** A wind-up cut by {@code fraction} of itself (a phase's or a deed's), never under the kind's floor. */
	public static int cut(SaintMove.Kind kind, int windup, float fraction) {
		return Math.max(revealFloor(kind), Math.round(windup * (1.0F - fraction)));
	}

	/** 12 for melee, 20 for area: the least wind-up and the least time a footprint shows before its commit. */
	public static int revealFloor(SaintMove.Kind kind) {
		return kind == SaintMove.Kind.AREA ? AREA_FLOOR : MELEE_FLOOR;
	}

	/** The AREA rule (resolution B-2): any footprint not attached to the body or reaching past 7 blocks. */
	public static SaintMove.Kind kindFor(BossFootprint shape) {
		return attachedReach(shape) > AREA_REACH ? SaintMove.Kind.AREA : SaintMove.Kind.MELEE;
	}

	/** How far from the body the footprint reaches; infinite for one not attached to the body. */
	public static double attachedReach(BossFootprint shape) {
		return switch (shape) {
			case BossFootprint.Lane lane -> lane.fanDegrees() > 0.0D ? lane.length()
					: Math.hypot(lane.length(), Math.abs(lane.sideLanes() * lane.spacing()) + lane.halfWidth());
			case BossFootprint.Spokes spokes -> spokes.length();
			case BossFootprint.Cone cone -> cone.radius();
			case BossFootprint.Sweep sweep -> sweep.radius();
			case BossFootprint.Ring ring -> Math.max(ring.from(), ring.to()) + ring.band() / 2.0D;
			case BossFootprint.Disc disc -> disc.at() == BossFootprint.Anchor.ORIGIN ? disc.radius() : Double.POSITIVE_INFINITY;
			case BossFootprint.Annulus annulus -> annulus.outer();
			case BossFootprint.Cross cross -> cross.at() == BossFootprint.Anchor.ORIGIN
					? Math.hypot(cross.arm(), cross.halfWidth()) : Double.POSITIVE_INFINITY;
			case BossFootprint.Tether tether -> tether.from() == BossFootprint.End.BODY && tether.range() > 0.0D
					? tether.range() : Double.POSITIVE_INFINITY;
			case BossFootprint.Union union -> {
				double reach = 0.0D;
				for (BossFootprint.Union.Part part : union.parts()) reach = Math.max(reach, attachedReach(part.shape()));
				yield reach;
			}
			case BossFootprint.Occluded occluded -> attachedReach(occluded.shape());
			case BossFootprint.Discs discs -> Double.POSITIVE_INFINITY;
			case BossFootprint.Path path -> Double.POSITIVE_INFINITY;
			case BossFootprint.Region region -> Double.POSITIVE_INFINITY;
			case BossFootprint.None none -> 0.0D;
		};
	}

	private static boolean occluded(BossFootprint shape) {
		return shape instanceof BossFootprint.Occluded;
	}

	private static List<LivingEntity> candidates(SaintMoveContext ctx) {
		List<LivingEntity> candidates = new ArrayList<>();
		for (Player player : ctx.arena().challengers()) candidates.add(player);
		for (LivingEntity companion : ctx.level().getEntitiesOfClass(LivingEntity.class, ctx.arena().chamber(),
				entity -> entity instanceof SaintCompanion c && c.allegiance() == SaintCompanion.Allegiance.CHALLENGERS
						&& c.saintId().equals(ctx.saint().getUUID()) && entity.isAlive())) {
			candidates.add(companion);
		}
		candidates.removeIf(entity -> !ManipulationCombatHelper.canHarmFor(ctx.saint(), entity));
		return candidates;
	}
}

package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDeeds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickContext;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * The server half of each Hemorath move (design section 7): what it does on its own clock ({@code onTick}), what its
 * footprint does to whoever it strikes ({@code onHit}), the variant it starts with and the opening its recovery opens.
 * Damage goes through {@link SaintMoves#hurt} (his damage type, {@code canHarmFor}); blood leaves a player only
 * through {@link BloodAccounting} with a {@link HemorathSources} constant; pulls are velocity, never a teleport.
 */
public final class HemorathMoves {
	/** Melee damage while he is Brimming. */
	public static final float BRIMMING_MELEE = 1.2F;
	public static final float CLAW_DRAIN = 80.0F;
	public static final float CLAW_DRY_BONUS = 2.0F;
	public static final double BACKHAND_KNOCKBACK = 0.6D, HEEL_KNOCKBACK = 0.8D, PULSE_KNOCKBACK = 1.2D,
			RUPTURE_KNOCKBACK = 0.45D, RAKE_KNOCKBACK = 0.5D, RIB_KNOCKBACK = 0.7D;
	public static final int PULSE_HEMOPHAGY = 140, HALO_HEMOPHAGY = 60, RIB_HEMOPHAGY = 100;
	public static final float CHAIN_MARK_DAMAGE = 6.0F;
	public static final float SIPHON_DRAIN = 6.0F;
	public static final double SIPHON_PULL = 0.05D;
	public static final int SIPHON_EMPTY_INTERVAL = 10;
	public static final int SIPHON_SOUND_EVERY = 20;
	public static final float SIPHON_GORGED = 0.10F;
	public static final int THICKET_SLOW = 40;
	public static final int TIDE_SLOW = 40;
	public static final double GORGE_PULL = 0.12D, GORGE_PULL_SPRINTING = 0.04D, GORGE_CONTACT = 1.6D;
	public static final float GORGE_DAMAGE = 6.0F, GORGE_DRAIN = 150.0F, GORGE_PER_HEALTH = 40.0F;
	/** Wind-up beats that re-send a tracking move's points while its aim still moves. */
	public static final int POINTS_EVERY = 5;
	/** Iron Thicket's fallback discs ring the target at this radius. */
	public static final double THICKET_RING = 2.5D;
	public static final double THICKET_SPACING = 2.0D;

	private HemorathMoves() {
	}

	// ── what the picker reads ────────────────────────────────────────

	static HemorathPickRules.Situation situation(SaintPickContext ctx) {
		if (ctx.saint() instanceof HemorathEntity saint) return saint.situation(ctx);
		return new HemorathPickRules.Situation(ctx.phase(), ctx.band(), ctx.sector(), ctx.meterFraction(), 0,
				ctx.targetSecondary() * HemorathTitheRules.DEBT_SHOWN, false, 0.0F, ctx.kiteTicks(), ctx.hugTicks(),
				SaintDeeds.skipped(ctx.deedMask(), ChapelRules.Kind.DOWNFALL), false);
	}

	static int variant(HemorathMove move, SaintPickContext ctx, IntUnaryOperator roll) {
		if (!(ctx.saint() instanceof HemorathEntity saint)) return 0;
		int variant = Math.min(HemorathIronRules.MAX_PATCHES, saint.patchCount());
		boolean beholding = SaintDeeds.skipped(ctx.deedMask(), ChapelRules.Kind.BEHOLDING);
		if (move == HemorathMove.RAKING_CLAW && beholding && roll.applyAsInt(2) == 0) variant |= HemorathMove.VARIANT_HELD;
		if (move == HemorathMove.EXSANGUINATING_TIDE && saint.emptying()) variant |= HemorathMove.VARIANT_FORCED;
		if (move == HemorathMove.WELLS_ANSWER) variant |= saint.chooseWells() << HemorathMove.VARIANT_WELLS_SHIFT;
		return variant;
	}

	@Nullable
	static SaintOpening.Spec recoveryOpening(HemorathMove move, SaintMoveContext ctx) {
		if (!(ctx.saint() instanceof HemorathEntity saint)) return null;
		float max = saint.meter().max();
		return switch (move) {
			case MARROW_SIPHON -> saint.siphonEndsGorged(max) ? HemorathOpenings.GORGED : null;
			case WELLS_ANSWER -> saint.wellsDrawn() >= HemorathWellRules.GORGED_DRAW * max ? HemorathOpenings.GORGED : null;
			case RIB_BURST -> HemorathOpenings.STRAINED;
			case STAFF_FALL -> HemorathOpenings.PLANTED;
			case GORGE -> saint.gorgeReachedNoOne() ? HemorathOpenings.BROKEN_GRASP : null;
			case EXSANGUINATING_TIDE, LAST_THIRST, SPIKED_HALO, TITHE_RECKONING -> null;
			default -> saint.meter().fraction() >= HemorathMeterRules.VENT_FROM ? HemorathOpenings.STRAINED : null;
		};
	}

	// ── onTick ───────────────────────────────────────────────────────

	static void tick(HemorathMove move, SaintMoveContext ctx) {
		if (!(ctx.saint() instanceof HemorathEntity saint)) return;
		ServerLevel level = ctx.level();
		int tick = ctx.tick();
		boolean start = tick == 0;
		boolean commit = ctx.committedNow();
		int age = ctx.age();
		switch (move) {
			case RAKING_CLAW, BACKHAND -> {
				if (start) HemorathEffects.sound(saint, SoundEvents.BONE_BLOCK_HIT, 0.9F, 0.6F);
				if (commit) HemorathEffects.sound(saint, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.6F);
			}
			case STAFF_HEEL -> {
				if (start) HemorathEffects.sound(saint, SoundEvents.CHAIN_PLACE, 1.0F, 0.6F);
				if (commit) HemorathEffects.sound(saint, SoundEvents.ANVIL_LAND, 0.8F, 0.6F);
			}
			case STAFF_FALL -> {
				if (start) HemorathEffects.sound(saint, SoundEvents.CHAIN_PLACE, 1.0F, 0.6F);
				if (start || commit || ctx.stage() == BossMoveTimeline.Stage.WINDUP && tick % POINTS_EVERY == 0) {
					saint.controller().setPoints(chainMarks(ctx.frame()));
				}
				if (commit) {
					HemorathEffects.sound(saint, SoundEvents.ANVIL_LAND, 1.0F, 0.5F);
					HemorathEffects.burst(level, at(ctx.frame(), 0.0D, HemorathMove.FALL_LENGTH), HemorathVisualRules.STAFF_CRACK,
							(float) HemorathMove.FALL_LENGTH);
				}
			}
			case IRON_RAKE -> {
				if (start) HemorathEffects.sound(saint, SoundEvents.GRINDSTONE_USE, 1.0F, 0.5F);
				if (ctx.stage() == BossMoveTimeline.Stage.ACTIVE && age % 4 == 0) {
					HemorathEffects.sound(saint, SoundEvents.POINTED_DRIPSTONE_PLACE, 0.8F, 0.6F);
				}
			}
			case EMPTY_PULSE -> {
				if (start) HemorathEffects.sound(saint, SoundEvents.WARDEN_SONIC_CHARGE, 1.5F, 0.8F);
				if (commit) {
					HemorathEffects.sound(saint, SoundEvents.WARDEN_SONIC_BOOM, 1.6F, 0.7F);
					HemorathEffects.burst(level, saint.position().add(0.0D, 0.6D, 0.0D), HemorathVisualRules.PULSE_RELEASE,
							(float) HemorathMove.PULSE_RADIUS);
				}
			}
			case MARROW_RUPTURE -> {
				if (commit) HemorathEffects.sound(saint, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 1.0F, 0.6F);
			}
			case MARROW_SIPHON -> {
				if (start) {
					saint.startSiphon();
					saint.controller().pressure().resetKite();
				}
				if (commit) HemorathEffects.burst(level, saint.position().add(0.0D, 0.2D, 0.0D), HemorathVisualRules.SIPHON,
						(float) HemorathMove.SIPHON_RADIUS);
				if (ctx.stage() == BossMoveTimeline.Stage.ACTIVE && age % SIPHON_SOUND_EVERY == 0) {
					HemorathEffects.sound(saint, SoundInit.ENTITY_NAEGLEROPHAEON_DRAIN, 1.2F, 0.6F);
				}
			}
			case CHAINED_DEBT -> {
				if (start) saint.controller().pressure().resetKite();
				if (commit) HemorathEffects.sound(saint, SoundEvents.CHAIN_BREAK, 1.0F, 0.7F);
			}
			case TITHE_RECKONING -> {
				if (start) saint.beginTithe();
				int links = Math.max(1, HemorathTitheRules.links(saint.targetDebt()));
				int clench = Math.max(2, ctx.saint().controller().timeline().commitTick() / links);
				if (ctx.stage() == BossMoveTimeline.Stage.WINDUP && tick > 0 && tick % clench == 0) {
					HemorathEffects.sound(saint, SoundEvents.CHAIN_HIT, 0.8F, 0.6F);
				}
				if (commit) HemorathEffects.sound(saint, SoundEvents.IRON_DOOR_CLOSE, 1.0F, 0.5F);
			}
			case WELLS_ANSWER -> {
				int wells = HemorathMove.wells(ctx.inputs().variant());
				if (start) {
					HemorathEffects.sound(saint, SoundInit.RITE_STAFF_IMPACT, 1.0F, 0.7F);
					saint.wellsFront(wells, ctx.saint().controller().timeline().commitTick());
				}
				if (commit) {
					saint.startDraw(wells);
					HemorathEffects.sound(saint, SoundInit.RITE_OFFERING_DRAWN, 1.0F, 0.6F);
				}
				if (ctx.stage() == BossMoveTimeline.Stage.ACTIVE) saint.drawTick();
			}
			case SPIKED_HALO -> {
				if (start || commit || ctx.stage() == BossMoveTimeline.Stage.WINDUP && tick % POINTS_EVERY == 0) {
					saint.controller().setPoints(haloPath(ctx.frame()));
				}
				if (commit) {
					HemorathEffects.sound(saint, SoundEvents.TRIDENT_THROW, 1.0F, 0.5F);
					int length = ctx.saint().controller().timeline().active() + ctx.saint().controller().timeline().recovery();
					saint.openings().open(HemorathOpenings.HALOLESS, length);
				}
				if (ctx.stage() == BossMoveTimeline.Stage.ACTIVE && !HemorathHaloPath.returning(age - 1)
						&& HemorathHaloPath.returning(age)) {
					HemorathEffects.sound(saint, SoundEvents.TRIDENT_RETURN, 1.0F, 0.6F);
				}
			}
			case CROWN_FALL -> {
				if (commit) {
					HemorathEffects.sound(saint, SoundEvents.ANVIL_PLACE, 1.0F, 0.5F);
					HemorathEffects.burst(level, saint.position(), HemorathVisualRules.CROWN_SPIKES,
							(float) HemorathMove.CROWN_RADIUS);
				}
			}
			case IRON_THICKET -> {
				if (start || commit) {
					int discs = HemorathIronRules.thicketDiscs(HemorathIronRules.thicketSpend(saint.patchCount()));
					saint.controller().setPoints(saint.thicketPoints(discs, ctx.target(), ctx.frame()));
				}
				if (ctx.stage() == BossMoveTimeline.Stage.WINDUP && tick > 0 && tick % 6 == 0
						&& tick / 6 <= HemorathIronRules.thicketSpend(saint.patchCount())) {
					HemorathEffects.sound(saint, SoundEvents.IRON_TRAPDOOR_OPEN, 1.0F, 0.5F);
				}
				if (commit) {
					saint.tearPatches(HemorathIronRules.thicketSpend(saint.patchCount()), false);
					HemorathEffects.sound(saint, SoundEvents.POINTED_DRIPSTONE_LAND, 1.0F, 0.6F);
					saint.spawnHazard(HemorathEntity.HAZARD_THICKET, new BossFootprint.Discs(HemorathMove.THICKET_RADIUS, 0,
							BossFootprint.Height.STANDING), ctx.frame(), HemorathEntity.THICKET_LIFE);
				}
			}
			case EXSANGUINATING_TIDE -> {
				if (start) HemorathEffects.sound(saint, SoundEvents.BUCKET_EMPTY, 1.0F, 0.6F);
				if (commit) saint.flood(ctx.frame(), HemorathMove.forced(ctx.inputs().variant()),
						ctx.saint().controller().timeline().active());
			}
			case RIB_BURST -> {
				if (start) HemorathEffects.sound(saint, SoundInit.RITE_ESCALATE, 1.0F, 0.7F);
				if (commit) saint.ventRibs();
			}
			case GORGE -> {
				if (start) {
					saint.startGorge();
					HemorathEffects.sound(saint, SoundInit.ENTITY_VESPER_GRAB_BITE, 1.0F, 0.6F);
				}
			}
			case LAST_THIRST -> {
				if (start) saint.beginThirstDrag();
				if (commit) saint.startThirst();
			}
		}
	}

	// ── onHit ────────────────────────────────────────────────────────

	static void hit(HemorathMove move, SaintMoveContext ctx, LivingEntity target) {
		if (!(ctx.saint() instanceof HemorathEntity saint)) return;
		switch (move) {
			case RAKING_CLAW -> {
				float paid = drain(target, CLAW_DRAIN, HemorathSources.CLAW);
				float damage = melee(saint, move.baseDamage()) + (target instanceof Player && paid <= 0.0F ? CLAW_DRY_BONUS : 0.0F);
				if (saint.strike(ctx, target, damage)) {
					if (paid > 0.0F) saint.feed(paid, target instanceof Player p ? p : null, false);
					Vec3 at = target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D);
					HemorathEffects.burst(ctx.level(), at, HemorathVisualRules.MELEE, 0.7F);
					HemorathEffects.intake(ctx.level(), at, saint.chest());
					HemorathEffects.sound(saint, SoundInit.BLOOD_VIAL_INJECT, 1.0F, 0.6F);
				}
			}
			case BACKHAND -> {
				if (saint.strike(ctx, target, melee(saint, move.baseDamage()))) push(saint, target, BACKHAND_KNOCKBACK);
			}
			case STAFF_HEEL -> {
				if (saint.strike(ctx, target, melee(saint, move.baseDamage()))) push(saint, target, HEEL_KNOCKBACK);
			}
			case STAFF_FALL -> {
				double[] local = BossFootprint.Shapes.local(ctx.frame(), ctx.frame().origin(), target.position(), 0.0D);
				boolean line = local[0] >= 0.0D && local[0] <= HemorathMove.FALL_LENGTH
						&& Math.abs(local[1]) <= HemorathMove.FALL_HALF_WIDTH;
				saint.strike(ctx, target, line ? move.baseDamage() : CHAIN_MARK_DAMAGE);
			}
			case IRON_RAKE -> {
				if (saint.strike(ctx, target, move.baseDamage())) push(saint, target, RAKE_KNOCKBACK);
			}
			case EMPTY_PULSE -> {
				if (saint.strike(ctx, target, move.baseDamage())) {
					push(saint, target, PULSE_KNOCKBACK);
					target.addEffect(new MobEffectInstance(EffectInit.hemophagy, PULSE_HEMOPHAGY), saint);
				}
			}
			case MARROW_RUPTURE -> {
				if (saint.strike(ctx, target, move.baseDamage())) push(saint, target, RUPTURE_KNOCKBACK);
			}
			case MARROW_SIPHON -> siphon(saint, ctx, target);
			case CHAINED_DEBT -> {
				if (saint.strike(ctx, target, move.baseDamage())) saint.hook(target);
			}
			case TITHE_RECKONING -> saint.collectTithe(ctx, target);
			case SPIKED_HALO -> {
				if (saint.strike(ctx, target, HemorathIronRules.haloDamage(HemorathMove.patches(ctx.inputs().variant())))) {
					target.addEffect(new MobEffectInstance(EffectInit.hemophagy, HALO_HEMOPHAGY), saint);
				}
			}
			case CROWN_FALL -> saint.strike(ctx, target, move.baseDamage());
			case IRON_THICKET -> {
				if (saint.strike(ctx, target, move.baseDamage())) {
					target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, THICKET_SLOW, 1), saint);
				}
			}
			case EXSANGUINATING_TIDE -> {
				if (saint.strike(ctx, target, move.baseDamage())) {
					target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, TIDE_SLOW, 0), saint);
				}
			}
			case RIB_BURST -> {
				if (saint.strike(ctx, target, move.baseDamage())) {
					push(saint, target, RIB_KNOCKBACK);
					target.addEffect(new MobEffectInstance(EffectInit.hemophagy, RIB_HEMOPHAGY), saint);
				}
			}
			case GORGE -> gorge(saint, ctx, target);
			case WELLS_ANSWER, LAST_THIRST -> {
			}
		}
	}

	private static void siphon(HemorathEntity saint, SaintMoveContext ctx, LivingEntity target) {
		pull(saint, target, SIPHON_PULL);
		if (target instanceof Player player && HemoCapabilityAccess.getBloodVolume(player).isActive()
				&& HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() > 0.0D) {
			float paid = drain(player, SIPHON_DRAIN, HemorathSources.SIPHON);
			if (paid > 0.0F) saint.siphoned(saint.feed(paid, player, false));
			saint.bloodFell(player);
			return;
		}
		if (ctx.age() % SIPHON_EMPTY_INTERVAL == 0 && SaintMoves.hurt(saint, target, HemorathPresenceRules.EMPTY_DAMAGE, null)) {
			saint.siphoned(saint.feed(HemorathPresenceRules.METER_PER_HEALTH, target instanceof Player p ? p : null, false));
		}
	}

	private static void gorge(HemorathEntity saint, SaintMoveContext ctx, LivingEntity target) {
		double distance = target.position().distanceTo(saint.position());
		if (distance <= GORGE_CONTACT) {
			if (!saint.gorgeContact(target)) return;
			if (saint.strike(ctx, target, GORGE_DAMAGE)) {
				float paid = drain(target, GORGE_DRAIN, HemorathSources.GORGE);
				if (paid > 0.0F) {
					saint.feed(paid, target instanceof Player p ? p : null, false);
					saint.heal(paid / GORGE_PER_HEALTH);
				}
				HemorathEffects.intake(ctx.level(), target.position().add(0.0D, 1.0D, 0.0D), saint.chest());
				HemorathEffects.sound(saint, SoundEvents.GENERIC_DRINK, 0.8F, 0.5F);
			}
			return;
		}
		Vec3 away = target.position().subtract(saint.position());
		Vec3 motion = target.getDeltaMovement();
		boolean escaping = target.isSprinting() && motion.x * away.x + motion.z * away.z > 0.0D;
		pull(saint, target, escaping ? GORGE_PULL_SPRINTING : GORGE_PULL);
	}

	// ── shared pieces ────────────────────────────────────────────────

	/** A melee blow, x1.2 while he is Brimming. */
	static float melee(HemorathEntity saint, float base) {
		return saint.state() == HemorathMeterRules.State.BRIMMING ? base * BRIMMING_MELEE : base;
	}

	/** Blood a strike takes from a blood-active player; 0 from anyone else. */
	static float drain(LivingEntity target, float amount, BloodAccounting.Source source) {
		if (!(target instanceof Player player)) return 0.0F;
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		if (!volume.isActive() || volume.getBloodVolume() <= 0.0D) return 0.0F;
		return (float) BloodAccounting.pay(player, amount, source).paid();
	}

	/** Knocks a body away from him. */
	static void push(HemorathEntity saint, LivingEntity target, double strength) {
		target.knockback(strength, saint.getX() - target.getX(), saint.getZ() - target.getZ());
		target.hurtMarked = true;
	}

	/** Draws a body toward him by velocity, flat. */
	static void pull(HemorathEntity saint, LivingEntity target, double speed) {
		Vec3 to = saint.position().subtract(target.position());
		Vec3 flat = new Vec3(to.x, 0.0D, to.z);
		if (flat.lengthSqr() < 1.0E-4D) return;
		Vec3 step = flat.normalize().scale(speed);
		Vec3 motion = target.getDeltaMovement();
		target.setDeltaMovement(motion.x + step.x, motion.y, motion.z + step.z);
		target.hurtMarked = true;
	}

	/** A point {@code right} to the side and {@code along} the frame's aim from its origin. */
	static Vec3 at(FootprintFrame frame, double right, double along) {
		double[] f = BossFootprint.Shapes.forward(frame);
		return new Vec3(frame.origin().x + f[0] * along - f[1] * right, frame.floorY(),
				frame.origin().z + f[1] * along + f[0] * right);
	}

	/** Staff Fall's two chain marks either side of the line's end. */
	static List<Vec3> chainMarks(FootprintFrame frame) {
		return List.of(at(frame, -HemorathMove.CHAIN_SIDE, HemorathMove.FALL_LENGTH),
				at(frame, HemorathMove.CHAIN_SIDE, HemorathMove.FALL_LENGTH));
	}

	/** The halo's flight as world points from the frame's origin along its aim. */
	static List<Vec3> haloPath(FootprintFrame frame) {
		List<Vec3> points = new ArrayList<>();
		for (double[] p : HemorathHaloPath.points()) points.add(at(frame, p[1], p[0]));
		return points;
	}

	/** Spikes in the hazard's pulse for the standing thicket. */
	static void thicketPulse(HemorathEntity saint, BossGroundHazardEntity hazard, LivingEntity target) {
		if (SaintMoves.hurt(saint, target, HemorathEntity.THICKET_DAMAGE, null)) {
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, HemorathEntity.THICKET_PULSE + 5, 0), saint);
		}
	}
}

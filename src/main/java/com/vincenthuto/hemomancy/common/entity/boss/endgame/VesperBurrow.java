package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.encounter.VesperOrdealManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Server behaviour of the Burrow. Vesper tucks in and grips the nape, the mount plunges into the floor, a fissure trail
 * hunts the target and surfaces alternating claw rakes and tail stings, the trail falls silent while the ground
 * rumbles, and the mount erupts beneath the locked point before it gathers itself.
 */
final class VesperBurrow {
	private static final double SUBMERGED_DEPTH = VesperBurrowRules.SUBMERGED_DEPTH;
	private static final double STRIKE_DEPTH = VesperBurrowRules.STRIKE_DEPTH;

	private VesperBurrow() {
	}

	static void begin(VesperTheCrownedRefusalEntity boss, boolean fromDive) {
		VesperBurrowRules.Plan plan = fromDive ? VesperBurrowRules.Plan.dive(boss.getRandom())
				: VesperBurrowRules.Plan.ground(boss.getRandom());
		// A dive is already falling into the floor, so it skips most of the plunge.
		int startTick = fromDive ? VesperBurrowRules.DIVE_TICKS - 4 : 0;
		boss.setBurrowState(new VesperBurrowRules.State(startTick, plan, boss.getX(), boss.getZ(),
				boss.getX(), boss.getZ()));
		boss.playSound(SoundEvents.RAVAGER_ROAR, 1.6F, 0.45F);
	}

	/** One burrow tick; returns false once the burrow has finished and the mount is back in the controller's hands. */
	static boolean tick(VesperTheCrownedRefusalEntity boss, @Nullable LivingEntity target,
			@Nullable VesperOrdealManager.FlightArena arena) {
		VesperBurrowRules.State state = boss.getBurrowState();
		if (state == null) return false;
		VesperBurrowRules.Plan plan = state.plan();
		int tick = state.tick() + 1;
		VesperBurrowRules.Stage stage = VesperBurrowRules.stageAt(plan, tick);
		if (target == null && tick < VesperBurrowRules.rumbleStart(plan) && tick >= VesperBurrowRules.DIVE_TICKS) {
			state = VesperBurrowRules.onTargetLost(state);
			tick = state.tick();
			stage = VesperBurrowRules.stageAt(plan, tick);
		}
		double floorY = groundY(boss, arena != null ? arena.floorY() + 1.0D : boss.getBurrowFloorY(),
				state.x(), state.z(), target);
		ServerLevel server = boss.level() instanceof ServerLevel level ? level : null;
		boss.getNavigation().stop();
		switch (stage) {
			case DIVE -> {
				boss.setDeltaMovement(Vec3.ZERO);
				if (server != null && tick % 2 == 0) {
					Vec3 nose = boss.position().add(Vec3.directionFromRotation(0.0F, boss.getYRot()).scale(3.0D));
					VesperVisualEffects.floorChips(server, nose, 10, 1.4D);
					VesperVisualEffects.bloodCells(server, nose, VesperVisualEffects.BLOOD, 6, 1.2D, 0.2D, 1.2D, 0.08D);
				}
				if (tick == VesperBurrowRules.DIVE_TICKS - 6) submerge(boss, server);
			}
			case HUNT, STRIKE_TELL, STRIKE -> {
				if (!boss.isInvisible()) submerge(boss, server);
				Vec3 trail = new Vec3(state.x(), floorY, state.z());
				if (stage == VesperBurrowRules.Stage.HUNT && target != null) {
					Vec3 toward = target.position().subtract(trail).multiply(1.0D, 0.0D, 1.0D);
					double step = Math.min(VesperBurrowRules.TRAIL_SPEED, toward.length());
					if (step > 1.0E-4D) trail = trail.add(toward.normalize().scale(step));
					if (arena != null) trail = VesperBurrowRules.clampTrail(trail, arena.centerX(), arena.floorY(),
							arena.centerZ());
				}
				Vec3 previous = new Vec3(state.x(), floorY, state.z());
				state = state.withTrail(trail.x, trail.z);
				boolean striking = stage == VesperBurrowRules.Stage.STRIKE;
				boss.setPos(trail.x, floorY - (striking ? STRIKE_DEPTH : SUBMERGED_DEPTH), trail.z);
				boss.setInvisible(!striking);
				if (target != null) boss.faceFlat(target.position());
				if (server != null) {
					if (stage == VesperBurrowRules.Stage.HUNT && tick % 2 == 0) {
						VesperVisualEffects.floorFissure(server, previous, trail, VesperVisualEffects.BLOOD,
								boss.getId() * 31L + tick);
						VesperVisualEffects.floorChips(server, trail, 3, 0.6D);
					} else if (stage == VesperBurrowRules.Stage.STRIKE_TELL && tick % 2 == 0) {
						// The trail stalls and the ground swells: something is about to come up right here.
						int told = tick - VesperBurrowRules.DIVE_TICKS;
						double swell = 0.6D + 0.12D * (told % VesperBurrowRules.STRIKE_INTERVAL
								- (VesperBurrowRules.STRIKE_INTERVAL - VesperBurrowRules.STRIKE_TICKS
								- VesperBurrowRules.STRIKE_TELL_TICKS));
						VesperVisualEffects.telegraphRing(server, trail, swell, VesperVisualEffects.BLOOD, 16);
						VesperVisualEffects.darkGlow(server, trail, VesperVisualEffects.BLACK, 4, 0.4D, 0.1D, 0.4D, 0.02D);
						VesperVisualEffects.floorChips(server, trail, 4, swell);
					}
				}
				if (VesperBurrowRules.isStrikeImpact(plan, tick)) {
					strike(boss, server, trail, VesperBurrowRules.strikeAt(VesperBurrowRules.strikeIndexAt(tick)));
				}
			}
			case RUMBLE -> {
				// Silence: no trail, no chips. Only the ground shakes, and the eruption point locks partway through.
				boss.setPos(state.x(), floorY - SUBMERGED_DEPTH, state.z());
				boss.setInvisible(true);
				if (tick == VesperBurrowRules.lockTick(plan)) {
					Vec3 lock = target != null ? target.position() : new Vec3(state.x(), floorY, state.z());
					if (arena != null) lock = VesperBurrowRules.clampTrail(lock, arena.centerX(), arena.floorY(),
							arena.centerZ());
					state = state.withLock(lock.x, lock.z).withTrail(lock.x, lock.z);
				}
				if (tick == VesperBurrowRules.rumbleStart(plan)) boss.playSound(SoundEvents.WARDEN_EMERGE, 2.0F, 0.5F);
			}
			case ERUPTION -> {
				Vec3 lock = new Vec3(state.lockX(), floorY, state.lockZ());
				if (tick == VesperBurrowRules.eruptionStart(plan)) erupt(boss, server, lock);
				double rise = (tick - VesperBurrowRules.eruptionStart(plan) + 1.0D) / VesperBurrowRules.ERUPTION_TICKS;
				boss.setPos(lock.x, floorY - SUBMERGED_DEPTH * (1.0D - Mth.clamp(rise, 0.0D, 1.0D)), lock.z);
			}
			case RECOVERY -> {
				if (boss.noPhysics) surface(boss, floorY);
				boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
			}
			case COMPLETE -> {
				if (boss.noPhysics) surface(boss, floorY);
				boss.setBurrowState(null);
				boss.endBurrow();
				return false;
			}
		}
		boss.setBurrowState(state.withTick(tick));
		return true;
	}

	/** The ordeal floor is flat; a summoned Vesper's ground follows the terrain under the trail instead. */
	private static double groundY(VesperTheCrownedRefusalEntity boss, double recorded, double x, double z,
			@Nullable LivingEntity target) {
		if (boss.getOrdealOwner() != null) return recorded;
		int hint = target != null ? target.getBlockY() : Mth.floor(recorded);
		return EndgameBossActions.findGround(boss.level(), BlockPos.containing(x, hint, z)).getY();
	}

	private static void submerge(VesperTheCrownedRefusalEntity boss, @Nullable ServerLevel server) {
		boss.noPhysics = true;
		boss.setNoGravity(true);
		boss.setInvisible(true);
		boss.setDeltaMovement(Vec3.ZERO);
		if (server != null) {
			VesperVisualEffects.floorChips(server, boss.position(), 28, 2.2D);
			VesperVisualEffects.darkGlow(server, boss.position(), VesperVisualEffects.BLACK, 16, 1.8D, 0.3D, 1.8D, 0.05D);
		}
		boss.playSound(SoundEvents.ROOTED_DIRT_BREAK, 2.0F, 0.6F);
	}

	private static void surface(VesperTheCrownedRefusalEntity boss, double floorY) {
		boss.noPhysics = false;
		boss.setNoGravity(false);
		boss.setInvisible(false);
		boss.setPos(boss.getX(), floorY, boss.getZ());
	}

	private static void strike(VesperTheCrownedRefusalEntity boss, @Nullable ServerLevel server, Vec3 at,
			VesperBurrowRules.Strike strike) {
		VesperBurrowRules.StrikeEffect effect = VesperBurrowRules.effects(strike);
		AABB area = new AABB(at, at).inflate(VesperBurrowRules.STRIKE_RADIUS, 2.5D, VesperBurrowRules.STRIKE_RADIUS);
		for (LivingEntity living : boss.level().getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity.isAlive() && entity != boss && !(entity instanceof BoundPuppeteerSummon))) {
			boolean hurt = living.hurt(effect.armorPiercing() ? HemoDamageTypes.vesperRend(boss.level(), boss)
					: boss.damageSources().mobAttack(boss), effect.damage());
			if (!hurt) continue;
			if (effect.poisonTicks() > 0) living.addEffect(new MobEffectInstance(MobEffects.POISON, effect.poisonTicks(), 0));
			if (effect.bloodLossTicks() > 0) {
				living.addEffect(new MobEffectInstance(EffectInit.blood_loss, effect.bloodLossTicks(), 0));
			}
			living.push(0.0D, 0.45D, 0.0D);
			living.hurtMarked = true;
		}
		if (server != null) {
			VesperVisualEffects.floorChips(server, at, 18, 1.2D);
			VesperVisualEffects.bloodCells(server, at.add(0.0D, 0.6D, 0.0D), VesperVisualEffects.BLOOD,
					16, 0.6D, 0.9D, 0.6D, 0.12D);
			if (strike == VesperBurrowRules.Strike.CLAW) {
				PacketHandler.sendClawSlash(at.add(0.0D, 1.0D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D),
						VesperVisualEffects.BLOOD, false, 1.1F, 48.0D, server);
			} else {
				VesperVisualEffects.voidTendril(server, at.add(0.0D, -1.0D, 0.0D), at.add(0.0D, 2.4D, 0.0D),
						boss.tickCount * 59L);
			}
		}
		boss.playSound(strike == VesperBurrowRules.Strike.CLAW ? SoundInit.ENTITY_VESPER_GRAB_BITE.get()
				: SoundInit.ENTITY_VESPER_GRAB_PIERCE.get(), 1.4F, 0.9F);
	}

	private static void erupt(VesperTheCrownedRefusalEntity boss, @Nullable ServerLevel server, Vec3 at) {
		boss.setInvisible(false);
		AABB area = new AABB(at, at).inflate(VesperBurrowRules.ERUPTION_RADIUS, 3.0D, VesperBurrowRules.ERUPTION_RADIUS);
		for (LivingEntity living : boss.level().getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity.isAlive() && entity != boss && !(entity instanceof BoundPuppeteerSummon))) {
			if (!living.hurt(boss.damageSources().mobAttack(boss), VesperBurrowRules.ERUPTION_DAMAGE)) continue;
			Vec3 away = living.position().subtract(at).multiply(1.0D, 0.0D, 1.0D);
			if (away.lengthSqr() < 0.01D) away = new Vec3(0.3D, 0.0D, 0.0D);
			away = away.normalize().scale(0.6D);
			living.setDeltaMovement(away.x, VesperBurrowRules.ERUPTION_LAUNCH, away.z);
			living.hurtMarked = true;
		}
		if (server != null) {
			VesperVisualEffects.floorChips(server, at, 60, 3.2D);
			VesperVisualEffects.bloodCells(server, at.add(0.0D, 1.0D, 0.0D), VesperVisualEffects.BLOOD,
					60, 2.6D, 1.6D, 2.6D, 0.18D);
			VesperVisualEffects.darkGlow(server, at, VesperVisualEffects.BLACK, 36, 3.0D, 0.8D, 3.0D, 0.09D);
			VesperVisualEffects.telegraphRing(server, at, 3.4D, VesperVisualEffects.BLOOD, 40);
			for (int i = 0; i < 4; i++) {
				double angle = Mth.TWO_PI * i / 4.0D + boss.tickCount;
				Vec3 edge = at.add(Math.cos(angle) * 3.0D, 0.2D, Math.sin(angle) * 3.0D);
				VesperVisualEffects.lightning(server, at.add(0.0D, 2.5D, 0.0D), edge, false,
						boss.tickCount * 41L + i);
			}
		}
		boss.playSound(SoundEvents.GENERIC_EXPLODE.value(), 2.0F, 0.6F);
		boss.playSound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.5F);
	}
}

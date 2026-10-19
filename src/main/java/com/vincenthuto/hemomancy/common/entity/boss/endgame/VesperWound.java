package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The wounded mount fights back instead of freezing: it rooted-guards the open wound, telegraphing a bite, a tail
 * sweep, a shake-off, or turning the wound away from whoever is draining it. Also draws the drained blood stream.
 */
final class VesperWound {
	static final int GUARD_INTERVAL = 46;
	static final int GUARD_TELL = 14;

	/** Per-boss guard rhythm; not persisted, a reload simply restarts the rhythm. */
	static final class GuardState {
		int tick;
		VesperWoundRules.Guard guard = VesperWoundRules.Guard.SHAKE_OFF;
		Vec3 turnAway = Vec3.ZERO;
	}

	private VesperWound() {
	}

	static void resetGuard(VesperTheCrownedRefusalEntity boss) {
		boss.guardState().tick = 0;
	}

	static void tickGuard(VesperTheCrownedRefusalEntity boss, VesperMountController controller,
			@Nullable LivingEntity target) {
		boss.getNavigation().stop();
		boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
		if (boss.getStaggerTick() > 0) {
			boss.setStaggerTick(boss.getStaggerTick() - 1);
			return;
		}
		GuardState state = boss.guardState();
		LivingEntity drainer = drainer(boss, target);
		int phase = state.tick++ % GUARD_INTERVAL;
		if (drainer == null) return;
		if (phase == GUARD_INTERVAL - GUARD_TELL) {
			Vec3 toward = drainer.position().subtract(boss.position());
			float facing = boss.getYRot();
			float bearing = (float) (Mth.atan2(toward.z, toward.x) * Mth.RAD_TO_DEG) - 90.0F;
			state.guard = VesperWoundRules.guard(VesperWoundRules.Site.fromIndex(boss.getActiveAnchor()),
					Mth.wrapDegrees(bearing - facing));
			state.turnAway = boss.position().subtract(toward.multiply(1.0D, 0.0D, 1.0D));
			boss.playSound(SoundInit.ENTITY_VESPER_GRAB_TELEGRAPH.get(), 1.3F, 0.7F);
		}
		boolean telling = phase >= GUARD_INTERVAL - GUARD_TELL;
		if (telling) {
			tell(boss, state, drainer);
			if (state.guard == VesperWoundRules.Guard.TURN_GUARD) controller.face(state.turnAway, 11.0F);
			else controller.face(drainer.position(), 6.0F);
		} else {
			controller.face(drainer.position(), 3.0F);
		}
		if (phase == GUARD_INTERVAL - 1) act(boss, state, drainer);
	}

	/** Whoever is pulling his blood out, if still close; otherwise his current target. */
	@Nullable
	private static LivingEntity drainer(VesperTheCrownedRefusalEntity boss, @Nullable LivingEntity target) {
		if (boss.lastDrainer() != null && boss.level() instanceof ServerLevel server) {
			Player player = server.getPlayerByUUID(boss.lastDrainer());
			if (player != null && player.isAlive() && !player.isSpectator() && boss.distanceToSqr(player) < 400.0D) {
				return player;
			}
		}
		return target;
	}

	private static void tell(VesperTheCrownedRefusalEntity boss, GuardState state, LivingEntity drainer) {
		if (!(boss.level() instanceof ServerLevel server) || boss.tickCount % 2 != 0) return;
		switch (state.guard) {
			case BITE -> VesperVisualEffects.telegraphCone(server, boss.position(),
					drainer.position().subtract(boss.position()), 5.0D, 70.0D, VesperVisualEffects.BLOOD);
			case TAIL_SWEEP -> VesperVisualEffects.telegraphRing(server, boss.position(), 5.5D,
					VesperVisualEffects.BLOOD, 32);
			case SHAKE_OFF -> VesperVisualEffects.telegraphRing(server, boss.position(), 4.5D,
					VesperVisualEffects.DEEP_BLOOD, 24);
			case TURN_GUARD -> VesperVisualEffects.darkGlow(server, boss.woundCenter(), VesperVisualEffects.BLACK,
					4, 0.4D, 0.4D, 0.4D, 0.02D);
		}
	}

	private static void act(VesperTheCrownedRefusalEntity boss, GuardState state, LivingEntity drainer) {
		switch (state.guard) {
			case BITE -> {
				Vec3 facing = drainer.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
				if (facing.lengthSqr() < 1.0E-4D) facing = boss.getLookAngle();
				Vec3 dir = facing.normalize();
				for (LivingEntity living : around(boss, 5.0D)) {
					Vec3 to = living.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
					if (to.lengthSqr() < 1.0E-4D || dir.dot(to.normalize()) < Math.cos(Math.toRadians(35.0D))) continue;
					living.hurt(boss.damageSources().mobAttack(boss), 8.0F);
				}
				if (boss.level() instanceof ServerLevel server) {
					PacketHandler.sendClawSlash(boss.position().add(dir.scale(3.0D)).add(0.0D, 1.2D, 0.0D), dir,
							VesperVisualEffects.BLOOD, false, 1.0F, 48.0D, server);
				}
				boss.playSound(SoundInit.ENTITY_VESPER_GRAB_BITE.get(), 1.4F, 0.8F);
			}
			case TAIL_SWEEP -> {
				for (LivingEntity living : around(boss, 5.5D)) {
					if (!living.hurt(boss.damageSources().mobAttack(boss), 8.0F)) continue;
					push(boss, living, 1.0D);
				}
				if (boss.level() instanceof ServerLevel server) {
					VesperVisualEffects.telegraphRing(server, boss.position(), 5.5D, VesperVisualEffects.BLOOD, 48);
					VesperVisualEffects.bloodCells(server, boss.position().add(0.0D, 0.5D, 0.0D),
							VesperVisualEffects.BLOOD, 24, 3.0D, 0.3D, 3.0D, 0.1D);
				}
				boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.5F, 0.5F);
			}
			case SHAKE_OFF -> {
				for (LivingEntity living : around(boss, 4.5D)) {
					living.hurt(boss.damageSources().mobAttack(boss), 4.0F);
					push(boss, living, 1.3D);
				}
				if (boss.level() instanceof ServerLevel server) {
					VesperVisualEffects.darkGlow(server, boss.position().add(0.0D, 1.0D, 0.0D),
							VesperVisualEffects.BLACK, 30, 2.5D, 0.8D, 2.5D, 0.08D);
				}
				boss.playSound(SoundInit.ENTITY_VESPER_HURT.get(), 1.6F, 0.7F);
			}
			case TURN_GUARD -> boss.playSound(SoundInit.ENTITY_VESPER_AMBIENT.get(), 1.2F, 0.6F);
		}
	}

	private static java.util.List<LivingEntity> around(VesperTheCrownedRefusalEntity boss, double radius) {
		AABB area = boss.getBoundingBox().inflate(radius, 2.0D, radius);
		return boss.level().getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity.isAlive() && entity != boss && !(entity instanceof BoundPuppeteerSummon));
	}

	private static void push(VesperTheCrownedRefusalEntity boss, LivingEntity living, double strength) {
		Vec3 away = living.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
		if (away.lengthSqr() < 1.0E-4D) return;
		away = away.normalize().scale(strength);
		living.push(away.x, 0.35D, away.z);
		living.hurtMarked = true;
	}

	/** The blood he loses, pulled in a visible stream from the wound to the drainer's hand. */
	static void drainStream(VesperTheCrownedRefusalEntity boss, Player player) {
		if (!(boss.level() instanceof ServerLevel server) || boss.tickCount % 2 != 0) return;
		Vec3 from = boss.woundCenter();
		Vec3 to = player.getEyePosition().add(player.getLookAngle().scale(0.6D)).subtract(0.0D, 0.35D, 0.0D);
		Vec3 path = to.subtract(from);
		for (int i = 0; i < 4; i++) {
			double t = boss.getRandom().nextDouble();
			Vec3 point = from.add(path.scale(t)).add((boss.getRandom().nextDouble() - 0.5D) * 0.2D,
					(boss.getRandom().nextDouble() - 0.5D) * 0.2D, (boss.getRandom().nextDouble() - 0.5D) * 0.2D);
			Vec3 velocity = path.normalize().scale(0.35D);
			server.sendParticles(HemoParticleData.absorbedBloodCell(ParticleColor.BLOOD), point.x, point.y, point.z,
					0, velocity.x, velocity.y, velocity.z, 1.0D);
		}
		VesperVisualEffects.bloodCells(server, from, VesperVisualEffects.BLOOD, 2, 0.25D, 0.25D, 0.25D, 0.03D);
		if (boss.tickCount % 16 == 0) VesperVisualEffects.tendril(server, from, to, false, boss.tickCount * 13L);
	}
}

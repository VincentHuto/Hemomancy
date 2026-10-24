package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.entity.summon.SanguineHoundEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction.*;

/**
 * What the Evening Star inherits from every puppet the player failed to kill in phase one: one attack per surviving
 * shape, threaded through the same Crossbar he conducted them with.
 */
public final class VesperBorrowedAttacks {
	public static final int COOLDOWN_TICKS = 300;
	private static final Map<String, VesperWeaponAction> BY_SHAPE = Map.of(
			PuppeteerSummonDefinitions.GOREBOUND_HULK, BORROWED_HULK_YANK,
			PuppeteerSummonDefinitions.MARROW_SPITTER, BORROWED_MARROW_VOLLEY,
			PuppeteerSummonDefinitions.VEINWING_VULTURE, BORROWED_VULTURE_DIVE,
			PuppeteerSummonDefinitions.SCARLET_MUMMER, BORROWED_MUMMER_DECOYS,
			PuppeteerSummonDefinitions.SANGUINE_HOUND, BORROWED_HOUND_CURS,
			PuppeteerSummonDefinitions.CINDER_BELLOWS, BORROWED_BELLOWS_BREATH,
			PuppeteerSummonDefinitions.MNEMONIST_PUPPET, BORROWED_MNEMONIC_LASH);

	private VesperBorrowedAttacks() {
	}

	public static Optional<VesperWeaponAction> forShape(String shape) {
		return Optional.ofNullable(BY_SHAPE.get(shape));
	}

	public static List<VesperWeaponAction> available(List<String> inherited) {
		List<VesperWeaponAction> attacks = new ArrayList<>();
		for (String shape : inherited) forShape(shape).ifPresent(attacks::add);
		return List.copyOf(attacks);
	}

	public static boolean isBorrowed(VesperWeaponAction action) {
		return BY_SHAPE.containsValue(action);
	}

	/** Chance to slot a borrowed attack between strings; the sickles stay the focus of the last act. */
	public static float weight(VesperEveningStarAct act) {
		return act == VesperEveningStarAct.SICKLES ? 0.1F : 0.2F;
	}

	/** The Mnemonist's lash: the biggest hit the player dealt him lately, replayed at 60%, between 4 and 14. */
	public static float mnemonicDamage(float biggestRecentHit) {
		return Mth.clamp(biggestRecentHit * 0.6F, 4.0F, 14.0F);
	}

	/** At his awakening, a puppet sigil flares around him for each inherited shape, so the player sees what he took. */
	static void flareInheritance(VesperTheEveningStarEntity boss) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		List<String> inherited = boss.getInheritedPuppets();
		for (int i = 0; i < inherited.size(); i++) {
			double angle = Mth.TWO_PI * i / Math.max(1, inherited.size());
			Vec3 point = boss.position().add(Math.cos(angle) * 2.6D, 2.4D, Math.sin(angle) * 2.6D);
			ManipulationVisuals.burst(server, ManipulationVisuals.Form.MARIONETTE_ORDER, boss.getEyePosition(), point,
					0.8D, 30);
			VesperVisualEffects.telegraphRing(server, point.subtract(0.0D, 0.1D, 0.0D), 0.6D, VesperVisualEffects.BLOOD, 12);
			VesperVisualEffects.glow(server, point, VesperVisualEffects.BLOOD, 6, 0.15D, 0.15D, 0.15D, 0.0D);
		}
		if (!inherited.isEmpty()) boss.playSound(SoundEvents.CHAIN_PLACE, 1.4F, 0.5F);
	}

	// ---------------------------------------------------------------- bodies

	static void tick(VesperTheEveningStarEntity boss, LivingEntity target, VesperWeaponAction action, int tick) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		switch (action) {
			case BORROWED_HULK_YANK -> hulkYank(server, boss, target, tick);
			case BORROWED_MARROW_VOLLEY -> marrowVolley(server, boss, target, tick);
			case BORROWED_VULTURE_DIVE -> vultureDive(server, boss, target, tick);
			case BORROWED_MUMMER_DECOYS -> mummerDecoys(server, boss, tick);
			case BORROWED_HOUND_CURS -> houndCurs(server, boss, target, tick);
			case BORROWED_BELLOWS_BREATH -> bellowsBreath(server, boss, tick);
			case BORROWED_MNEMONIC_LASH -> mnemonicLash(server, boss, target, tick);
			default -> { }
		}
	}

	/** The Hulk's haul: Crossbar threads drag the target in, into a slam. */
	private static void hulkYank(ServerLevel server, VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
		if (tick < 14 && tick % 2 == 0) {
			VesperVisualEffects.telegraphLine(server, boss.position(), target.position(), VesperVisualEffects.BLOOD);
		}
		if (tick == 14 && boss.distanceTo(target) < 12.0F) {
			ManipulationVisuals.burst(server, ManipulationVisuals.Form.MARIONETTE_TETHER, boss.getEyePosition(),
					target.position().add(0.0D, 1.0D, 0.0D), 1.0D, 12);
			Vec3 toward = boss.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
			if (toward.lengthSqr() > 0.01D) {
				target.setDeltaMovement(toward.normalize().scale(1.3D).add(0.0D, 0.35D, 0.0D));
				target.hurtMarked = true;
			}
		}
		if (tick > 14 && tick < 24 && tick % 2 == 0) {
			VesperVisualEffects.telegraphRing(server, boss.position(), 4.0D, VesperVisualEffects.BLOOD, 28);
		}
		if (tick == 24) {
			for (LivingEntity victim : players(boss, boss.position(), 4.0D)) {
				victim.hurt(boss.damageSources().mobAttack(boss), 12.0F);
			}
			VesperVisualEffects.floorChips(server, boss.position(), 30, 2.4D);
			boss.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.2F, 0.7F);
		}
	}

	/** The Spitter's volley: three lobbed marrow globs that leave caustic pools for five seconds. */
	private static void marrowVolley(ServerLevel server, VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
		if (tick == 14 || tick == 18 || tick == 22) {
			double angle = boss.getRandom().nextDouble() * Mth.TWO_PI;
			Vec3 landing = target.position().add(Math.cos(angle) * 1.6D, 0.0D, Math.sin(angle) * 1.6D);
			VesperVisualEffects.telegraphLine(server, boss.getEyePosition(), landing, VesperVisualEffects.DEEP_BLOOD);
			boss.addMarrowPool(landing, server.getGameTime() + 100);
			boss.playSound(SoundEvents.LLAMA_SPIT, 1.2F, 0.6F);
		}
	}

	/** Pools persist after the volley: their stain bubbles and poisons whoever stands in it. */
	static void tickPools(VesperTheEveningStarEntity boss) {
		if (!(boss.level() instanceof ServerLevel server)) return;
		boss.marrowPools().removeIf(pool -> pool.expires() <= server.getGameTime());
		for (VesperTheEveningStarEntity.Pool pool : boss.marrowPools()) {
			if (boss.tickCount % 4 == 0) {
				VesperVisualEffects.bloodCells(server, pool.at(), VesperVisualEffects.DEEP_BLOOD, 3, 0.8D, 0.05D, 0.8D, 0.01D);
				VesperVisualEffects.darkGlow(server, pool.at(), VesperVisualEffects.BLACK, 2, 0.8D, 0.05D, 0.8D, 0.0D);
			}
			if (boss.tickCount % 10 == 0) {
				for (LivingEntity victim : players(boss, pool.at(), 1.6D)) {
					victim.hurt(boss.damageSources().indirectMagic(boss, boss), 2.0F);
					victim.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
				}
			}
		}
	}

	/** The Vulture's dive: a wing-hop up, then down on the mark. */
	private static void vultureDive(ServerLevel server, VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		Vec3 aim = boss.getLockedActionAim();
		if (tick < 18 && tick % 2 == 0) VesperVisualEffects.telegraphRing(server, aim, 3.0D, VesperVisualEffects.BLOOD, 24);
		if (tick == 8) {
			Vec3 toward = aim.subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
			Vec3 hop = toward.lengthSqr() > 0.01D ? toward.normalize().scale(0.4D) : Vec3.ZERO;
			boss.setDeltaMovement(hop.x, 1.0D, hop.z);
			boss.playSound(SoundEvents.PHANTOM_FLAP, 1.4F, 0.6F);
		}
		if (tick == 16) {
			Vec3 down = aim.subtract(boss.position());
			boss.setDeltaMovement(down.normalize().scale(1.4D));
		}
		if (tick == 18) {
			for (LivingEntity victim : players(boss, aim, 3.0D)) victim.hurt(boss.damageSources().mobAttack(boss), 11.0F);
			VesperVisualEffects.floorChips(server, aim, 20, 1.6D);
			VesperVisualEffects.bloodCells(server, aim, VesperVisualEffects.BLOOD, 20, 1.5D, 0.3D, 1.5D, 0.1D);
		}
	}

	/** The Mummer's decoys: two afterimages of him that draw the eye, then burst. */
	private static void mummerDecoys(ServerLevel server, VesperTheEveningStarEntity boss, int tick) {
		boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
		if (tick == 12) {
			boss.decoys().clear();
			for (int side = -1; side <= 1; side += 2) {
				Vec3 offset = Vec3.directionFromRotation(0.0F, boss.getYRot() + 90.0F * side).scale(3.5D);
				boss.decoys().add(boss.position().add(offset));
			}
			boss.playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.4F, 0.7F);
		}
		if (tick > 12 && tick < 40 && tick % 3 == 0) {
			for (Vec3 decoy : boss.decoys()) {
				VesperVisualEffects.glow(server, decoy.add(0.0D, 1.0D, 0.0D), VesperVisualEffects.BLOOD, 4, 0.3D, 0.8D, 0.3D, 0.0D);
				VesperVisualEffects.darkGlow(server, decoy.add(0.0D, 1.0D, 0.0D), VesperVisualEffects.BLACK, 3, 0.3D, 0.8D, 0.3D, 0.0D);
			}
		}
		if (tick == 40) {
			for (Vec3 decoy : boss.decoys()) {
				ManipulationVisuals.burst(server, ManipulationVisuals.Form.BLOOD_SPLASH, decoy, decoy, 2.5D, 16);
				for (LivingEntity victim : players(boss, decoy, 2.5D)) victim.hurt(boss.damageSources().indirectMagic(boss, boss), 8.0F);
			}
			boss.decoys().clear();
		}
	}

	/** The Hound's curs: three short-lived Blood Curs loosed on the target; no roster effect, gone in seconds. */
	private static void houndCurs(ServerLevel server, VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
		if (tick != 14) return;
		for (int i = 0; i < 3; i++) {
			SanguineHoundEntity cur = EntityInit.sanguine_hound.get().create(server);
			if (cur == null) continue;
			cur.configureEncounterCur(boss.getOrdealOwner());
			cur.getPersistentData().putBoolean(VesperEncounterPuppetEvents.PUPPET_KEY, true);
			double angle = Mth.TWO_PI * i / 3.0D;
			cur.setPos(boss.getX() + Math.cos(angle) * 1.5D, boss.getY(), boss.getZ() + Math.sin(angle) * 1.5D);
			if (target != null) cur.setTarget(target);
			server.addFreshEntity(cur);
		}
		VesperVisualEffects.bloodCells(server, boss.position().add(0.0D, 0.5D, 0.0D), VesperVisualEffects.BLOOD,
				24, 1.2D, 0.3D, 1.2D, 0.05D);
		boss.playSound(SoundEvents.WOLF_GROWL, 1.2F, 0.6F);
	}

	/** The Bellows' breath: an ember cone in pulses. */
	private static void bellowsBreath(ServerLevel server, VesperTheEveningStarEntity boss, int tick) {
		boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
		Vec3 look = boss.getLookAngle().normalize();
		Vec3 origin = boss.getEyePosition().add(look.scale(0.5D));
		if (tick < 14 && tick % 2 == 0) {
			VesperVisualEffects.telegraphCone(server, boss.position(), look, 6.0D, 50.0D, VesperVisualEffects.EMBER);
		}
		if (tick >= 14 && tick <= 30) {
			for (int i = 0; i < 5; i++) {
				Vec3 point = origin.add(look.scale(0.8D + i * 1.1D));
				VesperVisualEffects.embers(server, point, VesperVisualEffects.EMBER, 2, 0.1D * i + 0.1D, 0.08D * i + 0.1D,
						0.1D * i + 0.1D, 0.02D, 0.16F, 10);
			}
		}
		boolean pulse = tick == 14 || tick == 18 || tick == 22 || tick == 26 || tick == 30;
		if (!pulse) return;
		for (LivingEntity victim : players(boss, origin.add(look.scale(3.0D)), 4.0D)) {
			Vec3 to = victim.position().add(0.0D, 1.0D, 0.0D).subtract(origin);
			if (to.length() > 6.0D || look.dot(to.normalize()) < Math.cos(Math.toRadians(25.0D))) continue;
			victim.hurt(HemoDamageTypes.livingTorchBreath(boss.level(), boss), 3.0F);
			victim.setRemainingFireTicks(40);
		}
	}

	/** The Mnemonist's lash: a thread replaying the biggest hit the player dealt him lately. */
	private static void mnemonicLash(ServerLevel server, VesperTheEveningStarEntity boss, LivingEntity target, int tick) {
		boss.setDeltaMovement(0.0D, boss.getDeltaMovement().y, 0.0D);
		if (tick < 16 && tick % 2 == 0) {
			VesperVisualEffects.telegraphLine(server, boss.position(), target.position(), VesperVisualEffects.WHITE);
		}
		if (tick == 16 && boss.hasLineOfSight(target) && boss.distanceTo(target) < 14.0F) {
			ManipulationVisuals.burst(server, ManipulationVisuals.Form.MNEMONIC_REPLAY, boss.getEyePosition(),
					target.position().add(0.0D, 1.0D, 0.0D), 1.0D, 14);
			target.hurt(boss.damageSources().indirectMagic(boss, boss), mnemonicDamage(boss.biggestRecentHit()));
		}
	}

	private static List<LivingEntity> players(VesperTheEveningStarEntity boss, Vec3 center, double radius) {
		return boss.level().getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius, 2.5D, radius),
				entity -> entity instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator());
	}
}

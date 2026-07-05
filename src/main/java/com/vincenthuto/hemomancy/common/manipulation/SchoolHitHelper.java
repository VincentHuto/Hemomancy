package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.DuctilisLightningEffects;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.UUID;

public final class SchoolHitHelper {
	private static final String CONDUCTIVE_LAST_ARC_KEY = "hemomancy:conductive_mark_last_arc";
	private static final String GRAVE_DEBT_OWNER_KEY = "hemomancy:grave_debt_owner";
	private static final String GRAVE_DEBT_BURST_USED_KEY = "hemomancy:grave_debt_burst_used";

	private SchoolHitHelper() {
	}

	public static void markConductive(LivingEntity target, int durationTicks) {
		target.addEffect(new MobEffectInstance(EffectInit.conductive_mark, durationTicks, 0, false, true, true));
		CompoundTag data = target.getPersistentData();
		if (!data.contains(CONDUCTIVE_LAST_ARC_KEY)) {
			data.putLong(CONDUCTIVE_LAST_ARC_KEY, -1L);
		}
	}

	public static boolean tryTriggerConductiveArc(LivingEntity attacker, LivingEntity markedTarget,
			EnumBloodTendency primary, @Nullable EnumBloodTendency secondary, float sourceDamage) {
		if (!(attacker instanceof Player) || markedTarget.level().isClientSide()
				|| !markedTarget.hasEffect(EffectInit.conductive_mark)
				|| !ManipulationStatusRules.isConductiveSchool(primary, secondary)) {
			return false;
		}

		long now = markedTarget.level().getGameTime();
		CompoundTag data = markedTarget.getPersistentData();
		long lastArc = data.contains(CONDUCTIVE_LAST_ARC_KEY) ? data.getLong(CONDUCTIVE_LAST_ARC_KEY) : -1L;
		if (!ManipulationStatusRules.canTriggerConductiveArc(now, lastArc)) {
			return false;
		}

		Vec3 center = markedTarget.position().add(0.0D, markedTarget.getBbHeight() * 0.5D, 0.0D);
		int[] arcs = {0};
		markedTarget.level().getEntitiesOfClass(LivingEntity.class,
						new AABB(markedTarget.blockPosition()).inflate(ManipulationStatusRules.CONDUCTIVE_ARC_RADIUS),
						target -> target != markedTarget && target != attacker && target.isAlive()
								&& !target.isAlliedTo(attacker) && !attacker.isAlliedTo(target)
								&& target.position().distanceTo(center) <= ManipulationStatusRules.CONDUCTIVE_ARC_RADIUS)
				.stream()
				.sorted(Comparator.comparingDouble(target -> target.distanceToSqr(markedTarget)))
				.limit(ManipulationStatusRules.CONDUCTIVE_ARC_TARGETS)
				.forEach(target -> {
					DuctilisLightningEffects.conductiveArc(markedTarget, target, arcs[0]++);
					target.hurt(attacker.damageSources().magic(), ManipulationStatusRules.CONDUCTIVE_ARC_DAMAGE);
				});

		if (arcs[0] > 0) {
			data.putLong(CONDUCTIVE_LAST_ARC_KEY, now);
			markedTarget.level().playSound(null, markedTarget.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(),
					SoundSource.PLAYERS, 0.45F, 1.85F);
		}
		return arcs[0] > 0;
	}

	public static void markGraveDebt(LivingEntity target, Player caster, int durationTicks) {
		markGraveDebt(target, caster.getUUID(), durationTicks);
	}

	public static void markGraveDebt(LivingEntity target, @Nullable UUID ownerId, int durationTicks) {
		target.addEffect(new MobEffectInstance(EffectInit.grave_debt, durationTicks, 0, false, true, true));
		CompoundTag data = target.getPersistentData();
		if (ownerId != null) {
			data.putUUID(GRAVE_DEBT_OWNER_KEY, ownerId);
		} else {
			data.remove(GRAVE_DEBT_OWNER_KEY);
		}
		data.putBoolean(GRAVE_DEBT_BURST_USED_KEY, false);
	}

	public static boolean tryTriggerGraveDebtBurst(LivingEntity target, float previousHealth) {
		if (target.level().isClientSide() || !target.hasEffect(EffectInit.grave_debt)) {
			return false;
		}
		CompoundTag data = target.getPersistentData();
		if (data.getBoolean(GRAVE_DEBT_BURST_USED_KEY)
				|| !ManipulationStatusRules.crossedGraveDebtThreshold(previousHealth, target.getHealth(),
				target.getMaxHealth())) {
			return false;
		}
		data.putBoolean(GRAVE_DEBT_BURST_USED_KEY, true);

		Player owner = graveDebtOwner(target);
		Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.45D, 0.0D);
		target.level().getEntitiesOfClass(LivingEntity.class,
						new AABB(target.blockPosition()).inflate(ManipulationStatusRules.GRAVE_DEBT_RADIUS),
						victim -> victim != target && victim.isAlive()
								&& (owner == null || (!victim.isAlliedTo(owner) && !owner.isAlliedTo(victim)))
								&& victim.position().distanceTo(center) <= ManipulationStatusRules.GRAVE_DEBT_RADIUS)
				.forEach(victim -> victim.hurt(target.damageSources().magic(),
						ManipulationStatusRules.GRAVE_DEBT_BURST_DAMAGE));
		sendGraveBurst(target, center);
		return true;
	}

	public static void tryRefundGraveDebt(LivingEntity target) {
		if (target.level().isClientSide() || !target.hasEffect(EffectInit.grave_debt)) {
			return;
		}
		Player owner = graveDebtOwner(target);
		if (!(owner instanceof ServerPlayer serverPlayer)) {
			return;
		}
		HemoCapabilityAccess.getBloodVolume(serverPlayer).ifPresent(volume -> {
			if (volume.isActive()) {
				volume.fill(ManipulationStatusRules.GRAVE_DEBT_DEATH_REFUND);
				BloodVolumeEvents.syncVolume(serverPlayer, volume);
			}
		});
	}

	@Nullable
	private static Player graveDebtOwner(LivingEntity target) {
		if (!(target.level() instanceof ServerLevel serverLevel)) {
			return null;
		}
		CompoundTag data = target.getPersistentData();
		if (!data.hasUUID(GRAVE_DEBT_OWNER_KEY)) {
			return null;
		}
		UUID ownerId = data.getUUID(GRAVE_DEBT_OWNER_KEY);
		return serverLevel.getServer().getPlayerList().getPlayer(ownerId);
	}

	private static void sendGraveBurst(LivingEntity target, Vec3 center) {
		if (!(target.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		for (int i = 0; i < 34; i++) {
			serverLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(55, 120, 35)),
					center.x + (target.getRandom().nextDouble() - 0.5D) * 2.6D,
					center.y + (target.getRandom().nextDouble() - 0.5D) * 1.7D,
					center.z + (target.getRandom().nextDouble() - 0.5D) * 2.6D,
					1, 0.0D, 0.04D, 0.0D, 0.02D);
		}
		target.level().playSound(null, target.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS,
				0.65F, 1.45F);
	}
}

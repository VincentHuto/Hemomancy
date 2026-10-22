package com.vincenthuto.hemomancy.common.entity.weave;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticWeaveRules;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Each tendency's way of moving and striking. */
public final class ThreadAttacks {
	private static final BloodAccounting.Source ANIMUS_THREAD = BloodAccounting.Source.of("frayed_thread", Category.HAZARD, false, false);
	/** Preferred distance and height from the weaver, orbit speed (radians/tick) and steering speed. */
	public record Profile(double distance, double height, double orbit, double speed) {}

	private ThreadAttacks() {
	}

	public static Profile profile(EnumBloodTendency tendency) {
		return switch (tendency) {
			case ANIMUS -> new Profile(4.5, 1.6, 0.03, 0.09);
			case FLAMMEUS -> new Profile(3.0, 1.2, 0.08, 0.16);
			case CONGEATIO -> new Profile(5.0, 1.4, 0.015, 0.05);
			case MORTEM -> new Profile(3.5, 0.5, 0.02, 0.06);
			case TENEBRIS -> new Profile(4.0, 1.8, 0.04, 0.1);
			case FERRIC -> new Profile(3.5, 0.3, 0.025, 0.07);
			case LUX -> new Profile(5.5, 3.2, 0.02, 0.08);
			case DUCTILIS -> new Profile(4.5, 1.4, 0.06, 0.14);
		};
	}

	public static void perform(FrayedThreadEntity thread, Player target) {
		if (!(thread.level() instanceof ServerLevel server)) return;
		float power = SomaticWeaveRules.attackPower(thread.rank());
		EnumBloodTendency tendency = thread.tendency();
		switch (tendency) {
			case ANIMUS -> fire(server, thread, target, tendency, power, 1, 0F, 1.2F);
			case FLAMMEUS -> {
				thread.setDeltaMovement(target.getEyePosition().subtract(thread.position()).normalize().scale(0.6D));
				fire(server, thread, target, tendency, power, 1, 0F, 1.1F);
			}
			case CONGEATIO -> fire(server, thread, target, tendency, power, 3, 12F, 0.8F);
			case LUX -> {
				target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0));
				fire(server, thread, target, tendency, power, 2, 5F, 1.6F);
			}
			case MORTEM -> ThreadHazardEntity.spawn(server, thread, target.position(), tendency, 2.5F, 80, power);
			case TENEBRIS -> {
				if (thread.distanceToSqr(target) < 100) {
					target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
					target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
				}
				thread.fade(40);
				server.sendParticles(HemoParticleData.darkGlow(tendency.getColor()), thread.getX(), thread.getY(), thread.getZ(),
						24, 0.6D, 0.6D, 0.6D, 0.04D);
			}
			case FERRIC -> spikes(thread, target, power);
			case DUCTILIS -> jolt(server, thread, target, power);
		}
	}

	private static void fire(ServerLevel server, FrayedThreadEntity thread, LivingEntity target, EnumBloodTendency tendency,
			float power, int count, float spread, float speed) {
		Vec3 aim = target.getEyePosition().subtract(thread.position());
		for (int i = 0; i < count; i++) {
			ThreadBoltEntity bolt = new ThreadBoltEntity(server, thread, tendency, power);
			bolt.setPos(thread.getX(), thread.getY(), thread.getZ());
			float yaw = count == 1 ? 0F : (i - (count - 1) / 2F) * spread;
			Vec3 dir = aim.yRot((float) Math.toRadians(yaw));
			bolt.shoot(dir.x, dir.y, dir.z, speed, 2.0F);
			server.addFreshEntity(bolt);
		}
	}

	public static void onBoltHit(EnumBloodTendency tendency, FrayedThreadEntity thread, LivingEntity target, float power) {
		target.hurt(thread.damageSources().mobAttack(thread), 2.0F * power);
		switch (tendency) {
			case ANIMUS -> {
				if (target instanceof ServerPlayer player) {
					var volume = HemoCapabilityAccess.getBloodVolume(player);
					if (volume.isActive())
						BloodAccounting.pay(player, Math.min(volume.getBloodVolume(), 10.0D * power), ANIMUS_THREAD);
				}
			}
			case FLAMMEUS -> target.igniteForSeconds(2.0F);
			case CONGEATIO -> {
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
				target.setTicksFrozen(Math.min(target.getTicksFrozen() + 60, target.getTicksRequiredToFreeze() + 40));
			}
			case LUX -> target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
			default -> {
			}
		}
		markWeaveHarm(target, thread);
	}

	/** Copied from EndgameBossActions.summonGripSpikes (private there): a short ring of spikes, then a launch. */
	public static void spikes(FrayedThreadEntity thread, LivingEntity target, float power) {
		if (!(thread.level() instanceof ServerLevel server)) return;
		// Marked before the spikes rise: their harm and the landing both count, even as the first harm.
		markWeaveHarm(target, thread);
		if (thread.hunts(target)) target.getPersistentData().putLong("hemomancy:weave_launched_until",
				target.level().getGameTime() + SomaticWeaveRules.LAUNCH_WINDOW_TICKS);
		int count = power >= 2.0F ? 4 : 3;
		for (int i = 0; i < count; i++) {
			double a = i * Math.PI * 2 / count + server.random.nextDouble() * 0.5;
			BlockPos base = BlockPos.containing(target.getX() + Math.cos(a) * 1.5, target.getY(), target.getZ() + Math.sin(a) * 1.5);
			BlockPos ground = findGround(server, base);
			if (ground == null) continue;
			EntityIronSpike spike = new EntityIronSpike(EntityInit.iron_spike.get(), server, thread);
			spike.moveTo(ground.getX() + 0.5D, ground.getY(), ground.getZ() + 0.5D, server.random.nextFloat() * 360F, 0F);
			server.addFreshEntity(spike);
		}
		Vec3 away = target.position().subtract(thread.position()).multiply(1, 0, 1);
		Vec3 push = away.lengthSqr() < 1.0E-4D ? Vec3.ZERO : away.normalize().scale(0.4D);
		target.setDeltaMovement(target.getDeltaMovement().add(push).add(0.0D, 0.6D, 0.0D));
		target.hurtMarked = true;
	}

	private static void jolt(ServerLevel server, FrayedThreadEntity thread, Player target, float power) {
		if (thread.distanceToSqr(target) > 100 || !thread.hasLineOfSight(target)) return;
		markWeaveHarm(target, thread);
		target.hurt(thread.damageSources().mobAttack(thread), 1.5F * power);
		target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 6));
		Vec3 pull = thread.position().subtract(target.position()).normalize().scale(0.5D);
		target.setDeltaMovement(target.getDeltaMovement().add(pull));
		target.hurtMarked = true;
		Vec3 from = thread.position(), to = target.getEyePosition();
		for (int i = 0; i <= 10; i++) {
			Vec3 p = from.lerp(to, i / 10.0D);
			server.sendParticles(HemoParticleData.glow(thread.tendency().getColor()), p.x, p.y, p.z, 1, 0.05D, 0.05D, 0.05D, 0D);
		}
	}

	/** Scans for air over solid ground near a position, as EndgameBossActions.findGround does. */
	private static BlockPos findGround(ServerLevel server, BlockPos base) {
		for (int dy = 4; dy >= -5; dy--) {
			BlockPos p = base.above(dy);
			if (server.getBlockState(p).isAir() && server.getBlockState(p.below()).isSolid()) return p;
		}
		return null;
	}

	/**
	 * Records that a thread just harmed its weaver, so the fire, frost or poison it left counts toward the
	 * non-lethal floor. Helpers and bystanders are never marked.
	 */
	public static void markWeaveHarm(LivingEntity target, FrayedThreadEntity thread) {
		if (!(target instanceof Player) || !thread.hunts(target)) return;
		CompoundTag data = target.getPersistentData();
		data.putLong("hemomancy:weave_harm_until", target.level().getGameTime() + SomaticWeaveRules.NON_LETHAL_WINDOW_TICKS);
		data.putLong("hemomancy:weave_loom", thread.loomPos().asLong());
		data.putBoolean("hemomancy:weave_lethal", SomaticWeaveRules.lethal(thread.rank()));
	}
}

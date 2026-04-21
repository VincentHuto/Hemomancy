package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Exsanguinate — a T2 (MEDIOCRITAS) MORTEM quick manipulation that forcibly
 * drains the remaining blood from a critically wounded enemy.
 *
 * <p>Targets the nearest enemy within 10 blocks that is below 30% of their
 * maximum health. On a valid target, deals lethal damage equal to 150% of the
 * target's remaining health and restores 600 blood to the caster as the life
 * force is converted. If no qualifying target exists, the cast fails.
 *
 * <p>This is a finisher — it rewards patient, calculated combat over
 * mindless spamming.
 */
public class ExsanguinateManip extends BloodManipulation {

	private static final double RADIUS = 10.0;
	/** Target must be at or below this fraction of max health. */
	private static final float HP_THRESHOLD_FRACTION = 0.30f;
	/** Damage multiplier applied to the target's current health. */
	private static final float DRAIN_DAMAGE_MULTIPLIER = 1.5f;
	/** Blood restored to the caster per execution. */
	private static final double BLOOD_RESTORE = 600.0;

	public ExsanguinateManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		BlockPos center = player.blockPosition();
		AABB searchBox = new AABB(center).inflate(RADIUS);
		List<LivingEntity> nearby = world.getEntitiesOfClass(LivingEntity.class, searchBox,
				e -> e != player && e.isAlive());

		// Find the nearest critically wounded target
		Optional<LivingEntity> targetOpt = nearby.stream()
				.filter(e -> e.distanceTo(player) <= RADIUS)
				.filter(e -> e.getHealth() <= e.getMaxHealth() * HP_THRESHOLD_FRACTION)
				.min(Comparator.comparingDouble(e -> e.distanceTo(player)));

		if (targetOpt.isEmpty()) {
			player.displayClientMessage(
					Component.literal("§8No weakened vessel within reach."), true);
			return;
		}

		LivingEntity target = targetOpt.get();
		float drainDamage = target.getHealth() * DRAIN_DAMAGE_MULTIPLIER * (float) SkillPointHelper.getCrimsonMasteryMultiplier();
		target.hurt(world.damageSources().magic(), drainDamage);

		// Restore blood to caster
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume != null && volume.isActive()) {
			double toRestore = Math.min(BLOOD_RESTORE, volume.getMaxBloodVolume() - volume.getBloodVolume());
			volume.addBloodVolume(toRestore);
			if (player instanceof ServerPlayer serverPlayer) {
				PacketHandler.CHANNELBLOODVOLUME.send(
						PacketDistributor.PLAYER.with(() -> serverPlayer),
						new BloodVolumeServerPacket(volume));
			}
		}

		world.playSound(null, center, SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.7f, 1.8f);
		world.playSound(null, center, SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.5f, 0.7f);

		RandomSource random = world.random;
		// Dark red siphon effect between caster and target
		for (int i = 0; i < 30; i++) {
			double t = random.nextDouble();
			double px = target.getX() + (player.getX() - target.getX()) * t;
			double py = target.getEyeY() + (player.getEyeY() - target.getEyeY()) * t;
			double pz = target.getZ() + (player.getZ() - target.getZ()) * t;
			sLevel.sendParticles(
					GlowParticleFactory.createData(new ParticleColor(
							120 + random.nextFloat() * 80,
							0,
							random.nextFloat() * 20)),
					px, py, pz,
					1, 0f, 0f, 0f, 0.015f);
		}
	}
}

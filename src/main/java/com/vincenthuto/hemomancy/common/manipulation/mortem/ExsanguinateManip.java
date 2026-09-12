package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

	private Optional<LivingEntity> target(Player player) {
		AABB searchBox = new AABB(player.blockPosition()).inflate(RADIUS);
		List<LivingEntity> nearby = player.level().getEntitiesOfClass(LivingEntity.class, searchBox,
				e -> ManipulationCombatHelper.canHarm(player, e) && !com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates.NOBLOOD.test(e));

		return nearby.stream()
				.filter(e -> ManipulationCombatHelper.visible(player, e))
				.filter(e -> e.distanceTo(player) <= RADIUS)
				.filter(e -> e.getHealth() <= e.getMaxHealth() * HP_THRESHOLD_FRACTION)
				.min(Comparator.comparingDouble(e -> e.distanceTo(player)));

	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (target(player).isEmpty()) {
			player.displayClientMessage(Component.literal("No weakened blood-bearing enemy in sight."), true);
			return false;
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel sLevel)) return;

		BlockPos center = player.blockPosition();
		Optional<LivingEntity> targetOpt = target(player);

		if (targetOpt.isEmpty()) {
			player.displayClientMessage(
					Component.literal("§8No weakened vessel within reach."), true);
			return;
		}

		LivingEntity target = targetOpt.get();
		float drainDamage = target.getHealth() * DRAIN_DAMAGE_MULTIPLIER * (float) SkillPointHelper.getCrimsonMasteryMultiplier(player);
		if (!ManipulationParticles.hurt(this, target, world.damageSources().magic(),
				(drainDamage)) || target.isAlive()) return;
		HemomancyTendrilEffects.exsanguinate(player, target);
        ManipulationVisuals.burst(sLevel, ManipulationVisuals.Form.MORTEM_BURST, target.position(), target.position(), .7, 24);

		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume != null && volume.isActive()) {
			double toRestore = Math.min(BLOOD_RESTORE, volume.getMaxBloodVolume() - volume.getBloodVolume());
			volume.addBloodVolume(toRestore);
			if (player instanceof ServerPlayer serverPlayer) {
				PacketHandler.sendToPlayer(serverPlayer, new BloodVolumeServerPacket(volume));
			}
		}

		world.playSound(null, center, SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.7f, 1.8f);
		world.playSound(null, center, SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.5f, 0.7f);

	        }
    }
}

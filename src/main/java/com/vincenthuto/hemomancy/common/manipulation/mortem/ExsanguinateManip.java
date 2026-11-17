package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.*;
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
 * target's remaining health and, on a kill, returns 60% of the blood the cast
 * cost as the life force is converted. If no qualifying target exists, the cast fails.
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
	/** Share of the blood paid for the cast that an execution returns (LP-7). */
	public static final double REFUND_FRACTION = 0.6D;
	public static final String REFUND_SOURCE = "exsanguinate_refund";
	public static final String REFUND_LABEL = "blood_source.hemomancy.exsanguinate_refund";
	/** Bosses cannot be executed outright: the drain is capped at this much (before Crimson Mastery). */
	public static final float BOSS_DAMAGE_CAP = 12.0F;

	public ExsanguinateManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTemper temper,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, temper, section);
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
			player.displayClientMessage(Component.translatable("message.hemomancy.exsanguinate_manip.no_weakened_blood_bearing_enemy_in"), true);
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
					Component.translatable("message.hemomancy.exsanguinate_manip.no_weakened_vessel_within_reach"), true);
			return;
		}

		LivingEntity target = targetOpt.get();
		float mastery = (float) SkillPointHelper.getCrimsonMasteryMultiplier(player);
		float drainDamage = target.getHealth() * DRAIN_DAMAGE_MULTIPLIER * mastery;
		// SchoolCombatEvents applies the same cap again after temper affinity.
		if (HemoEntityPredicates.isBoss(target)) drainDamage = Math.min(BOSS_DAMAGE_CAP * mastery, drainDamage);
		if (!ManipulationParticles.hurt(this, target, world.damageSources().magic(),
				(drainDamage)) || target.isAlive()) return;
		HemomancyTendrilEffects.exsanguinate(player, target);
        ManipulationVisuals.burst(sLevel, ManipulationVisuals.Form.MORTEM_BURST, target.position(), target.position(), .7, 24);

		double refund = BloodManipulation.paidForCurrentCast() * REFUND_FRACTION;
		if (refund > 0.0D && player instanceof ServerPlayer serverPlayer) {
			BloodFlowLedger.applyDirectIncome(serverPlayer, HemoCapabilityAccess.getBloodVolume(serverPlayer),
					REFUND_SOURCE, REFUND_LABEL, BloodFlowContribution.Category.CAST, refund, 1);
		}

		world.playSound(null, center, SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.7f, 1.8f);
		world.playSound(null, center, SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.5f, 0.7f);

	        }
    }
}

package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LivingCircuitManip extends BloodManipulation {
	/** Each pulse arcs from every linked body to the nearest hostile within this reach. */
	public static final double ARC_RADIUS = 4.0D;
	/** Arc damage before Crimson Mastery. The arc is a reaction, so it carries no school state. */
	public static final float ARC_DAMAGE = 2.0F;

	public LivingCircuitManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	/** The nearest three allies; with no teammate in reach the circuit closes through the caster alone. */
	private List<Player> recipients(Player player) {
		List<Player> allies = player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(10),
				candidate -> candidate != player && candidate.isAlive() && !candidate.isSpectator()
						&& ManipulationCombatHelper.allied(player, candidate)).stream()
				.sorted(Comparator.comparingDouble((Player ally) -> player.distanceToSqr(ally)).thenComparing(Player::getUUID))
				.limit(3).toList();
		return allies.isEmpty() ? List.of(player) : allies;
	}


	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
        try (var schoolCast = SchoolDamage.cast(this, player, getRequiredChargeTicks() <= 0 ? 1 : heldTicks / getRequiredChargeTicks())) {

		if (!(world instanceof ServerLevel level)) return;
		float damage = ARC_DAMAGE * (float) SkillPointHelper.getCrimsonMasteryMultiplier(player);
		Set<LivingEntity> struck = new HashSet<>();
		int arcs = 0;
		for (Player ally : recipients(player)) {
			ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 25, 1, false, true));
			ally.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 25, 1, false, true));
			ManipulationReactiveEvents.armLivingCircuit(ally);
			DuctilisLightningEffects.livingCircuit(player, ally);
			LivingEntity target = arcTarget(player, level, ally, struck);
			if (target == null) continue;
			// One arc per hostile per pulse: the reaction shares the pulse's root, so a second would be refused.
			struck.add(target);
			DuctilisLightningEffects.conductiveArc(ally, target, arcs++);
			ManipulationParticles.hurt(this, target, SchoolDamage.reaction(player, SchoolDamage.context(this, player)), damage);
		}
	        }
    }

	/** The nearest hostile the linked body can see within {@link #ARC_RADIUS} that this pulse has not struck. */
	private static LivingEntity arcTarget(Player caster, ServerLevel level, Player linked, Set<LivingEntity> struck) {
		return level.getEntitiesOfClass(LivingEntity.class, linked.getBoundingBox().inflate(ARC_RADIUS),
						candidate -> !struck.contains(candidate) && candidate != linked
								&& candidate.distanceTo(linked) <= ARC_RADIUS
								&& ManipulationCombatHelper.isHostileTarget(caster, candidate)
								&& ConductionManager.visible(level, linked.getEyePosition(), candidate.getEyePosition(), caster))
				.stream()
				.min(Comparator.comparingDouble((LivingEntity candidate) -> candidate.distanceToSqr(linked))
						.thenComparing(LivingEntity::getUUID))
				.orElse(null);
	}
}

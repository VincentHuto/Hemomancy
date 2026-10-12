package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class FuneralBellManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 80;
	private static final float BASE_DAMAGE = 7.0F;
	private static final float AFFLICTION_BONUS = 2.0F;
	private static final DustParticleOptions BLOOD = new DustParticleOptions(new Vector3f(0.55F, 0.02F, 0.04F), 1.2F);

	public FuneralBellManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override public int getRequiredChargeTicks() { return CHARGE_TICKS; }

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, getRequiredChargeTicks() <= 0 ? 1 : heldTicks / getRequiredChargeTicks())) {

		if (!(world instanceof ServerLevel level)) return;
		float charge = ManipulationCastingRules.chargeFraction(heldTicks, CHARGE_TICKS);
		// Mastery deepens the bell's own toll; the Necrosis debt it collects is spent at face value.
		float mastery = (float) com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper.getCrimsonMasteryMultiplier(player);
		double radius = 4.0D + 6.0D * charge;
        ManipulationVisuals.burst(level, ManipulationVisuals.Form.BELL, player.position(), player.position(), radius, 40);
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
				new AABB(player.blockPosition()).inflate(radius),
				entity -> ManipulationCombatHelper.canHarm(player, entity))) {
			int afflictions = afflictions(target);
			boolean hit = ManipulationCombatHelper.hurt(this, player, target, level,
					(BASE_DAMAGE + AFFLICTION_BONUS * afflictions) * charge * mastery);
			if (hit && afflictions > 0) tollAfflictions(target);
			if(hit) ManipulationVisuals.burst(level,ManipulationVisuals.Form.MORTEM_BURST,
                    target.position(),target.position(),1.1,20);
		}
	        }
    }

	/** Wither, Poison, Blood Loss, Insatiable Hunger and Grave Debt each deepen the toll. */
	private static int afflictions(LivingEntity target) {
		int count = 0;
		if (target.hasEffect(MobEffects.WITHER)) count++;
		if (target.hasEffect(MobEffects.POISON)) count++;
		if (target.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.blood_loss)) count++;
		if (target.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.insatiable_hunger)) count++;
		if (target.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.grave_debt)) count++;
		return count;
	}

	/** The bell spends the lingering afflictions it rang through; Grave Debt is left to collect on its own terms. */
	private static void tollAfflictions(LivingEntity target) {
		target.removeEffect(MobEffects.WITHER);
		target.removeEffect(MobEffects.POISON);
		target.removeEffect(com.vincenthuto.hemomancy.common.init.EffectInit.blood_loss);
		target.removeEffect(com.vincenthuto.hemomancy.common.init.EffectInit.insatiable_hunger);
	}
}

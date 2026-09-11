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
	private static final DustParticleOptions BLOOD = new DustParticleOptions(new Vector3f(0.55F, 0.02F, 0.04F), 1.2F);

	public FuneralBellManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override public int getRequiredChargeTicks() { return CHARGE_TICKS; }

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
		if (!(world instanceof ServerLevel level)) return;
		float charge = ManipulationCastingRules.chargeFraction(heldTicks, CHARGE_TICKS);
		double radius = 4.0D + 6.0D * charge;
        ManipulationVisuals.burst(level, ManipulationVisuals.Form.BELL, player.position(), player.position(), radius, 40);
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
				new AABB(player.blockPosition()).inflate(radius),
				entity -> ManipulationCombatHelper.canHarm(player, entity))) {
			int statuses = 0;
			if (target.hasEffect(MobEffects.WITHER)) statuses++;
			if (target.hasEffect(MobEffects.POISON)) statuses++;
			if (target.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.blood_loss)) statuses++;
			if (target.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.grave_debt)) statuses++;
			boolean hit = ManipulationCombatHelper.hurt(this, player, target, level, (4.0F + statuses * 2.0F) * charge);
			if (statuses > 0) target.addEffect(new MobEffectInstance(MobEffects.WITHER,
					Math.round(120 * charge), Math.min(2, statuses - 1), false, true));
			if(hit) ManipulationVisuals.burst(level,ManipulationVisuals.Form.MORTEM_BURST,
                    target.position(),target.position(),.6+statuses*.18,20);
		}
	}
}

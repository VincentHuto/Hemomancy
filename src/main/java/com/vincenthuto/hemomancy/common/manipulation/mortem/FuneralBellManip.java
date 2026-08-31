package com.vincenthuto.hemomancy.common.manipulation.mortem;

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
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
				new AABB(player.blockPosition()).inflate(radius),
				entity -> entity != player && entity.isAlive() && !player.isAlliedTo(entity))) {
			int statuses = 0;
			if (target.hasEffect(MobEffects.WITHER)) statuses++;
			if (target.hasEffect(MobEffects.POISON)) statuses++;
			if (target.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.blood_loss)) statuses++;
			if (target.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.grave_debt)) statuses++;
			ManipulationCombatHelper.hurt(this, player, target, level, 2.0F + 2.0F * charge + statuses * 2.0F);
			if (statuses > 0) target.addEffect(new MobEffectInstance(MobEffects.WITHER,
					40 + Math.round(80 * charge), Math.min(2, statuses - 1), false, true));
			level.sendParticles(BLOOD, target.getX(), target.getY() + target.getBbHeight() * .5,
					target.getZ(), 12 + statuses * 6, .5, .7, .5, .03);
		}
	}
}

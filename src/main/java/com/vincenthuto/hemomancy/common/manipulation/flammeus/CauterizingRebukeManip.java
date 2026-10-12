package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CauterizingRebukeManip extends BloodManipulation {
	/** Health paid for each ailment burned out of the blood. */
	public static final float HEALTH_PER_AILMENT = 1.0F;
	/** The purge then flares outward: a direct Flammeus burst that sears nearby enemies. */
	public static final double BURST_RADIUS = 4.0D;
	public static final float BURST_DAMAGE = 3.0F;
	public static final int SEARING_TICKS = 60;

	public CauterizingRebukeManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel sLevel)) return;

		int purged = 0;
		if (player.removeEffect(MobEffects.POISON)) purged++;
		if (player.removeEffect(MobEffects.WITHER)) purged++;
        if (player.removeEffect(EffectInit.necrosis)) purged++;
		if (player.removeEffect(EffectInit.blood_loss)) purged++;
		if (purged > 0) {
			player.setHealth(Math.max(1.0F, player.getHealth() - purged * HEALTH_PER_AILMENT));
			player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0, false, true));
		}
		float burst = BURST_DAMAGE * (float) SkillPointHelper.getCrimsonMasteryMultiplier(player);
		for (LivingEntity target : sLevel.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(BURST_RADIUS),
				e -> e.distanceTo(player) <= BURST_RADIUS && ManipulationCombatHelper.isHostileTarget(player, e)))
			ManipulationParticles.hurt(this, target, world.damageSources().onFire(), burst, SEARING_TICKS, 1);
		ManipulationVisuals.burst(sLevel, ManipulationVisuals.Form.IGNITION, player.position(), player.position(), BURST_RADIUS, 14);
		world.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 1.7F);
		world.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.7F, 1.0F);
        if (purged > 0) ManipulationVisuals.attached(player, ManipulationVisuals.Form.CAUTERIZE, 1, 24, purged);
	        }
    }
}

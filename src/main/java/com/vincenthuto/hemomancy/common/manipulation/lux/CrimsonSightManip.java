package com.vincenthuto.hemomancy.common.manipulation.lux;

import com.vincenthuto.hemomancy.common.damage.*;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.BodyRefinementSkillRules;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Crimson Sight — a T2 (MEDIOCRITAS) quick manipulation that grants
 * the player Night Vision and applies Illumination to all nearby hostile mobs.
 * <p>
 * Night Vision lasts 60 seconds (1200 ticks).
 * Illumination on hostiles within 32 blocks lasts 30 seconds (600 ticks).
 * <p>
 * Ideal mid-game utility for cave exploration and mob tracking.
 */
public class CrimsonSightManip extends BloodManipulation {

	private static final int NIGHT_VISION_DURATION = 1200;
	private static final int GLOWING_DURATION = 600;
	private static final double SCAN_RADIUS = 32.0;

	public CrimsonSightManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		int brightEyed = SkillPointHelper.getBrightEyedLevel(player);
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION, 0, false, false, true));

		List<Entity> entities = world.getEntities(player, player.getBoundingBox().inflate(
				SCAN_RADIUS * BodyRefinementSkillRules.perceptionRangeMultiplier(brightEyed)));
		int glowedCount = 0;
		for (Entity entity : entities) {
			if (entity instanceof Mob mob && mob.isAlive()) {
				SchoolStates.apply(player, mob, SchoolState.ILLUMINATED,
                        BodyRefinementSkillRules.revealTicks(GLOWING_DURATION, brightEyed));
				glowedCount++;
			}
		}

		world.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5f, 1.8f);

		if (world instanceof ServerLevel sLevel) {

			ManipulationParticles.accent(sLevel, EnumBloodTendency.LUX, player.getEyePosition(), net.minecraft.world.phys.Vec3.ZERO);
		}
	        }
    }
}

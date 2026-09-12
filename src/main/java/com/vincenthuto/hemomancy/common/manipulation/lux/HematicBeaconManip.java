package com.vincenthuto.hemomancy.common.manipulation.lux;

import com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffects;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationParticles;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.BodyRefinementSkillRules;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class HematicBeaconManip extends BloodManipulation {
	private static final double BASE_RANGE = 20.0;
	private static final double RADIUS = 8.0;
	private static final int DURATION = 160;

	public HematicBeaconManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel sLevel)) return;

		int brightEyed = SkillPointHelper.getBrightEyedLevel(player);
		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player)
				* BodyRefinementSkillRules.perceptionRangeMultiplier(brightEyed);
		Vec3 eye = player.getEyePosition();
		Vec3 end = eye.add(player.getLookAngle().scale(range));
		BlockHitResult hit = world.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		Vec3 center = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();

		double radius = RADIUS * BodyRefinementSkillRules.perceptionRangeMultiplier(brightEyed);
		ManipulationReactiveEvents.createHematicBeacon(sLevel, center, radius, DURATION, player.getUUID());

		world.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7F, 1.5F);
        HemomancyTendrilEffects.luxRelease(player, center.add(0, 1.35, 0));
		ManipulationParticles.accent(sLevel, EnumBloodTendency.LUX, center.add(0, 1.35, 0), net.minecraft.world.phys.Vec3.ZERO);
	        }
    }
}

package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.damage.*;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Glacial Grasp — a T1 (HUMILIS) quick manipulation that freezes water
 * along an aimed route. Wet targets caught in the route are frozen and
 * hindered while nearby source water becomes temporary Frosted Ice.
 */
public class GlacialGraspManip extends BloodManipulation {

	private static final double BASE_RANGE = 10.0D;
	private static final double PATH_WIDTH = 2.0D;
	private static final int RADIUS = 2;

	public GlacialGraspManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel sLevel)) {
			return;
		}

		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle().normalize();
		Vec3 end = eye.add(look.scale(range));
		BlockHitResult hit = world.clip(new ClipContext(eye, end,
				ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
		Vec3 impact = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
		BlockPos center = BlockPos.containing(impact);
		RandomSource random = world.random;
		Set<BlockPos> frozenTargets = new LinkedHashSet<>();
		int frozenEntities = 0;

		for (LivingEntity target : ManipulationCombatHelper.hostileTargets(player, world, range + PATH_WIDTH)) {
			boolean wet = target.isInWaterOrBubble()
					|| world.getFluidState(target.blockPosition()).is(Fluids.WATER);
			if (!wet || ManipulationCombatHelper.distanceToSegment(target.getEyePosition(), eye, end) > PATH_WIDTH) {
				continue;
			}
			SchoolStates.apply(player, target, SchoolState.RIME, 100, target.isInWaterOrRain() ? 2 : 1);
            ManipulationVisuals.attached(target,ManipulationVisuals.Form.FROZEN_VEINS,target.getBbWidth(),100,1);
			frozenEntities++;
		}

		for (int dx = -RADIUS; dx <= RADIUS; dx++) {
			for (int dz = -RADIUS; dz <= RADIUS; dz++) {
				if (dx * dx + dz * dz > RADIUS * RADIUS) continue;
				for (int dy = -1; dy <= 1; dy++) freezeWater(sLevel, center.offset(dx, dy, dz), random, frozenTargets, player);
			}
		}
		Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
		if (horizontal.lengthSqr() > 0.001D) {
			horizontal = horizontal.normalize();
			for (int step = 1; step <= (int) Math.ceil(range); step++) {
				BlockPos path = BlockPos.containing(player.position().add(horizontal.scale(step)));
				freezeWater(sLevel, path, random, frozenTargets, player);
				freezeWater(sLevel, path.below(), random, frozenTargets, player);
			}
		}

		if (!frozenTargets.isEmpty() || frozenEntities > 0) {
            for(BlockPos frozen:frozenTargets)ManipulationVisuals.burst(sLevel,ManipulationVisuals.Form.FROST_ADVANCE,
                    Vec3.atBottomCenterOf(frozen.above()),player.position(),.45,36);
            ManipulationVisuals.burst(sLevel, ManipulationVisuals.Form.ICE, Vec3.atBottomCenterOf(center.above()), Vec3.atCenterOf(center), RADIUS, 24);
			world.playSound(null, center, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.8f, 1.3f);


		}
	        }
    }

	private static void freezeWater(ServerLevel world, BlockPos target, RandomSource random, Set<BlockPos> frozenTargets,
			Player owner) {
		BlockState state = world.getBlockState(target);
		if (!state.getFluidState().is(Fluids.WATER) || !state.getFluidState().isSource()
				|| !world.getBlockState(target.above()).isAir()) return;
		TemporaryIceManager.placeOwned(world, target, BlockInit.frozen_cruor.get().defaultBlockState(),
				60 + random.nextInt(40), owner.getUUID());
		frozenTargets.add(target.immutable());
	}
}

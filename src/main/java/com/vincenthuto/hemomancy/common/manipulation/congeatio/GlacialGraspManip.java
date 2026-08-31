package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.HemomancyTendrilEffects;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hutoslib.client.particle.data.ColorParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

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
import net.minecraft.world.level.block.Blocks;
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
			target.setTicksFrozen(Math.max(target.getTicksFrozen(), target.getTicksRequiredToFreeze() + 40));
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, true));
			frozenEntities++;
		}

		for (int dx = -RADIUS; dx <= RADIUS; dx++) {
			for (int dz = -RADIUS; dz <= RADIUS; dz++) {
				if (dx * dx + dz * dz > RADIUS * RADIUS) continue;
				for (int dy = -1; dy <= 1; dy++) freezeWater(world, center.offset(dx, dy, dz), random, frozenTargets);
			}
		}
		Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
		if (horizontal.lengthSqr() > 0.001D) {
			horizontal = horizontal.normalize();
			for (int step = 1; step <= (int) Math.ceil(range); step++) {
				BlockPos path = BlockPos.containing(player.position().add(horizontal.scale(step)));
				freezeWater(world, path, random, frozenTargets);
				freezeWater(world, path.below(), random, frozenTargets);
			}
		}

		if (!frozenTargets.isEmpty() || frozenEntities > 0) {
			HemomancyTendrilEffects.glacialGrasp(player, center, java.util.List.copyOf(frozenTargets));
			world.playSound(null, center, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.8f, 1.3f);

			for (int i = 0; i < 25; i++) {
				sLevel.sendParticles(
						new ColorParticleData(new ParticleColor(
								150 + random.nextFloat() * 105,
								200 + random.nextFloat() * 55,
								255)),
						center.getX() + 0.5 + (random.nextDouble() - 0.5) * (RADIUS * 2),
						center.getY() + 0.2,
						center.getZ() + 0.5 + (random.nextDouble() - 0.5) * (RADIUS * 2),
						1, 0f, 0.15f, 0f, 0.01f);
			}
		}
	}

	private static void freezeWater(Level world, BlockPos target, RandomSource random, Set<BlockPos> frozenTargets) {
		BlockState state = world.getBlockState(target);
		if (!state.getFluidState().is(Fluids.WATER) || !state.getFluidState().isSource()
				|| !world.getBlockState(target.above()).isAir()) return;
		world.setBlock(target, Blocks.FROSTED_ICE.defaultBlockState(), 3);
		world.scheduleTick(target, Blocks.FROSTED_ICE, 60 + random.nextInt(40));
		frozenTargets.add(target.immutable());
	}
}

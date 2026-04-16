package com.vincenthuto.hemomancy.common.manipulation.lux;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Blood Lamp — a T1 (HUMILIS) quick manipulation that places an invisible
 * light source at the block the player is looking at.
 * <p>
 * Uses vanilla's light block (level 15) placed in air adjacent to
 * the targeted surface. Great for illuminating caves and dark areas.
 * <b>Base range:</b> 16 blocks, scales with Sanguine Reach skill.
 */
public class BloodLampManip extends BloodManipulation {

	private static final double BASE_RANGE = 16.0;

	public BloodLampManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier();

		Vec3 eyePos = player.getEyePosition(1.0F);
		Vec3 lookVec = player.getViewVector(1.0F);
		Vec3 endPos = eyePos.add(lookVec.scale(range));

		BlockHitResult hitResult = world.clip(new ClipContext(
				eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

		if (hitResult.getType() == HitResult.Type.MISS) {
			player.displayClientMessage(
					Component.literal("§cLook at a block to place a blood lamp!"), true);
			return;
		}

		BlockPos lightPos = hitResult.getBlockPos().relative(hitResult.getDirection());

		BlockState existing = world.getBlockState(lightPos);
		if (!existing.isAir()) {
			player.displayClientMessage(
					Component.literal("§cCannot place a light there!"), true);
			return;
		}

		BlockState lightState = Blocks.LIGHT.defaultBlockState()
				.setValue(LightBlock.LEVEL, 15);
		world.setBlock(lightPos, lightState, 3);
		world.playSound(null, lightPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.4f);

		if (world instanceof ServerLevel sLevel) {
			double cx = lightPos.getX() + 0.5;
			double cy = lightPos.getY() + 0.5;
			double cz = lightPos.getZ() + 0.5;
			RandomSource random = world.random;

			for (int i = 0; i < 20; i++) {
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(
								200 + random.nextFloat() * 55,
								180 + random.nextFloat() * 75,
								180 + random.nextFloat() * 75)),
						cx + (random.nextDouble() - 0.5) * 0.6,
						cy + (random.nextDouble() - 0.5) * 0.6,
						cz + (random.nextDouble() - 0.5) * 0.6,
						1, 0f, 0.05f, 0f, 0.015f);
			}
		}
	}
}

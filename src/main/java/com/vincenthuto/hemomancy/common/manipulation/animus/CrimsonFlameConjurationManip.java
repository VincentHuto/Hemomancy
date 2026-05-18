package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.client.particle.factory.HitGlowParticleFactory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Crimson Flame Conjuration — a T1 (HUMILIS) quick manipulation that spawns
 * crimson flames at the block the player is looking at.
 * <p>
 * <b>Base range:</b> 16 blocks<br>
 * <b>Upgradable:</b> Range scales with the Sanguine Reach skill
 * (each level adds +15% range via {@link SkillPointHelper#getSanguineReachMultiplier()}).
 */
public class CrimsonFlameConjurationManip extends BloodManipulation {

	private static final double BASE_RANGE = 16.0;

	public CrimsonFlameConjurationManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		// Calculate effective range with Sanguine Reach skill bonus
		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);

		Vec3 eyePos = player.getEyePosition(1.0F);
		Vec3 lookVec = player.getViewVector(1.0F);
		Vec3 endPos = eyePos.add(lookVec.scale(range));

		BlockHitResult hitResult = world.clip(new ClipContext(
				eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

		if (hitResult.getType() == HitResult.Type.MISS) {
			player.displayClientMessage(
					Component.literal("§cLook at a block to conjure crimson flames!"), true);
			return;
		}

		// Place the fire on the face that was hit (e.g. on top of the block)
		Direction face = hitResult.getDirection();
		BlockPos firePos = hitResult.getBlockPos().relative(face);

		// Check that the target position is replaceable (air, tall grass, etc.)
		BlockState existing = world.getBlockState(firePos);
		if (!existing.canBeReplaced()) {
			player.displayClientMessage(
					Component.literal("§cCannot place flames there!"), true);
			return;
		}

		// Place the crimson flame block
		BlockState flameState = BlockInit.crimson_flames.get().defaultBlockState();
		world.setBlock(firePos, flameState, 3);
		world.playSound(null, firePos, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8f, 0.9f);

		// Spawn a flash of particles to sell the conjuration
		if (world instanceof ServerLevel sLevel) {
			double cx = firePos.getX() + 0.5;
			double cy = firePos.getY() + 0.5;
			double cz = firePos.getZ() + 0.5;
			RandomSource random = world.random;

			// Bright crimson flash burst — central pop
			sLevel.sendParticles(
					HitGlowParticleFactory.createData(new ParticleColor(255, 40, 20)),
					cx, cy, cz,
					6, 0.1, 0.1, 0.1, 0.01f);

			// Scattered ember glow particles — crimson/orange spread
			for (int i = 0; i < 18; i++) {
				float r = 180 + random.nextFloat() * 75;  // 180-255 red
				float g = random.nextFloat() * 60;         // 0-60 green (orange tint)
				float b = random.nextFloat() * 15;         // near-zero blue
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(r, g, b)),
						cx + (random.nextDouble() - 0.5) * 0.6,
						cy + random.nextDouble() * 0.6,
						cz + (random.nextDouble() - 0.5) * 0.6,
						1, 0f, 0.15f, 0f, 0.02f);
			}

			// Dark blood-red embers rising upward
			for (int i = 0; i < 8; i++) {
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(120, 0, 0)),
						cx + (random.nextDouble() - 0.5) * 0.4,
						cy + 0.2,
						cz + (random.nextDouble() - 0.5) * 0.4,
						1, 0f, 0.3f, 0f, 0.01f);
			}
		}
	}
}

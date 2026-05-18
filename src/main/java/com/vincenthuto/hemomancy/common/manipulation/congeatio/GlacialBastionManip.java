package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Glacial Bastion — a T2 (MEDIOCRITAS) CONGEATIO quick manipulation that
 * raises a wall of packed ice at the targeted block surface.
 *
 * <p>The wall is 3 blocks wide and 3 blocks tall, oriented perpendicular to the
 * face the player is looking at. Each ice block is scheduled to melt after
 * ~30 seconds, ensuring the manipulation is tactical rather than permanent.
 * Sanguine Reach skill extends the casting range.
 */
public class GlacialBastionManip extends BloodManipulation {

	private static final double BASE_RANGE = 20.0;
	/** Wall half-width in blocks (total width = 2 * HALF_WIDTH + 1 = 3). */
	private static final int HALF_WIDTH = 1;
	/** Wall height in blocks. */
	private static final int HEIGHT = 3;
	/** Ticks before each ice block starts to melt. */
	private static final int MELT_TICKS_BASE = 500;
	private static final int MELT_TICKS_VARIANCE = 100;

	public GlacialBastionManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
		Vec3 eyePos = player.getEyePosition(1.0F);
		Vec3 lookVec = player.getViewVector(1.0F);
		Vec3 endPos = eyePos.add(lookVec.scale(range));

		BlockHitResult hitResult = world.clip(new net.minecraft.world.level.ClipContext(
				eyePos, endPos,
				net.minecraft.world.level.ClipContext.Block.OUTLINE,
				net.minecraft.world.level.ClipContext.Fluid.NONE, player));

		if (hitResult.getType() == HitResult.Type.MISS) {
			player.displayClientMessage(Component.literal("§cNo surface to anchor the bastion!"), true);
			return;
		}

		Direction hitFace = hitResult.getDirection();
		BlockPos base = hitResult.getBlockPos().relative(hitFace);

		// Determine the lateral axis perpendicular to the hit face
		Direction lateral;
		if (hitFace.getAxis() == Direction.Axis.Y) {
			// Hit top/bottom — wall runs east-west by default
			lateral = Direction.EAST;
		} else {
			// Hit a side — wall runs along whichever horizontal isn't the hit face
			lateral = hitFace.getClockWise();
		}

		RandomSource random = world.random;
		int placed = 0;
		for (int dLat = -HALF_WIDTH; dLat <= HALF_WIDTH; dLat++) {
			for (int dUp = 0; dUp < HEIGHT; dUp++) {
				BlockPos target = base
						.relative(lateral, dLat)
						.above(dUp);
				BlockState current = world.getBlockState(target);
				if (current.isAir() || current.canBeReplaced()) {
					world.setBlock(target, Blocks.PACKED_ICE.defaultBlockState(), 3);
					world.scheduleTick(target, Blocks.PACKED_ICE,
							MELT_TICKS_BASE + random.nextInt(MELT_TICKS_VARIANCE));
					placed++;
				}
			}
		}

		if (placed > 0) {
			world.playSound(null, base, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.0f, 0.5f);
			world.playSound(null, base, SoundEvents.POWDER_SNOW_BREAK, SoundSource.PLAYERS, 0.6f, 0.8f);

			for (int i = 0; i < 30; i++) {
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(
								140 + random.nextFloat() * 80,
								200 + random.nextFloat() * 55,
								255)),
						base.getX() + 0.5 + (random.nextDouble() - 0.5) * 3,
						base.getY() + random.nextDouble() * HEIGHT,
						base.getZ() + 0.5 + (random.nextDouble() - 0.5) * 3,
						1, 0f, 0.05f, 0f, 0.01f);
			}
		}
	}
}

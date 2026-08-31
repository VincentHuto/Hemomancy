package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Glacial Rampart raises a temporary ice wall at an aimed surface.
 */
public class GlacialRampartManip extends BloodManipulation {

	private static final double BASE_RANGE = 20.0;
	private static final int HALF_WIDTH = 1;
	private static final int HEIGHT = 3;
	private static final int BASE_LIFETIME_TICKS = 500;
	private static final int LIFETIME_VARIANCE = 100;

	public GlacialRampartManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;
		if (player.isShiftKeyDown()) {
			raiseBastion(player, sLevel);
			return;
		}

		double range = BASE_RANGE * SkillPointHelper.getSanguineReachMultiplier(player);
		Vec3 eyePos = player.getEyePosition(1.0F);
		Vec3 endPos = eyePos.add(player.getViewVector(1.0F).scale(range));
		BlockHitResult hitResult = world.clip(new ClipContext(eyePos, endPos,
				ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

		if (hitResult.getType() == HitResult.Type.MISS) {
			player.displayClientMessage(Component.literal("No surface to anchor the rampart."), true);
			return;
		}

		Direction hitFace = hitResult.getDirection();
		BlockPos base = hitResult.getBlockPos().relative(hitFace);
		Direction lateral = hitFace.getAxis() == Direction.Axis.Y
				? player.getDirection().getClockWise()
				: hitFace.getClockWise();

		RandomSource random = world.random;
		int placed = 0;
		for (int dLat = -HALF_WIDTH; dLat <= HALF_WIDTH; dLat++) {
			for (int dUp = 0; dUp < HEIGHT; dUp++) {
				BlockPos target = base.relative(lateral, dLat).above(dUp);
				if (TemporaryIceManager.place(sLevel, target, Blocks.PACKED_ICE.defaultBlockState(),
						BASE_LIFETIME_TICKS + random.nextInt(LIFETIME_VARIANCE))) {
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

	private static void raiseBastion(Player player, ServerLevel level) {
		RandomSource random = level.random;
		BlockPos base = player.blockPosition();
		int placed = 0;
		for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
			int distance = Math.abs(dx) + Math.abs(dz);
			if (distance < 2 || distance > 3) continue;
			for (int dy = 0; dy < HEIGHT; dy++) {
				if (dy == HEIGHT - 1 && distance == 3 && random.nextBoolean()) continue;
				BlockPos target = base.offset(dx, dy, dz);
				if (!target.equals(base) && !target.equals(base.above()) && TemporaryIceManager.place(level, target,
						Blocks.PACKED_ICE.defaultBlockState(), BASE_LIFETIME_TICKS + random.nextInt(LIFETIME_VARIANCE))) placed++;
			}
		}
		if (placed > 0) {
			level.playSound(null, base, SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 1.0F, .5F);
			level.playSound(null, base, SoundEvents.POWDER_SNOW_BREAK, SoundSource.PLAYERS, .6F, .8F);
		} else player.displayClientMessage(Component.literal("No space for the bastion to rise."), true);
	}
}

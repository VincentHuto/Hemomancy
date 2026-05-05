package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Vital Effusion — an ANIMUS HUMILIS quick manipulation that channels life-blood
 * into nearby soil, accelerating crop and plant growth in a small area.
 *
 * The blood remembers what it is to grow. Let it remember for everything else.
 */
public class VitalEffusionManip extends BloodManipulation {

	private static final double RANGE = 12.0;
	private static final int HALF_WIDTH = 2;
	private static final int HALF_HEIGHT = 1;

	public VitalEffusionManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		Vec3 eyePos = player.getEyePosition(1.0f);
		Vec3 lookVec = player.getViewVector(1.0f);
		Vec3 endPos = eyePos.add(lookVec.scale(RANGE));

		BlockHitResult hit = world.clip(new ClipContext(
				eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		if (hit.getType() == HitResult.Type.MISS) return;

		BlockPos center = hit.getBlockPos();
		int grew = 0;

		for (BlockPos p : BlockPos.betweenClosed(
				center.offset(-HALF_WIDTH, -HALF_HEIGHT, -HALF_WIDTH),
				center.offset(HALF_WIDTH, HALF_HEIGHT, HALF_WIDTH))) {
			BlockState state = world.getBlockState(p);
			if (state.getBlock() instanceof BonemealableBlock bb
					&& bb.isValidBonemealTarget(sLevel, p, state)) {
				int growths = 1 + world.random.nextInt(3);
				for (int i = 0; i < growths; i++) {
					bb.performBonemeal(sLevel, world.random, p.immutable(), world.getBlockState(p.immutable()));
				}
				sLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
						p.getX() + 0.5, p.getY() + 1.1, p.getZ() + 0.5, 3, 0.3, 0.2, 0.3, 0.0);
				grew++;
			}
		}

		if (grew > 0) {
			world.playSound(null, center, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 0.8f, 0.9f + world.random.nextFloat() * 0.2f);
		}
	}
}

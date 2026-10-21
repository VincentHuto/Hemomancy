package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

	/** The aimed block the effusion soaks into, or null when the caster looks at nothing. */
	private static BlockPos center(Player player, Level world) {
		Vec3 eyePos = player.getEyePosition(1.0f);
		Vec3 endPos = eyePos.add(player.getViewVector(1.0f).scale(RANGE));
		BlockHitResult hit = world.clip(new ClipContext(
				eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		return hit.getType() == HitResult.Type.MISS ? null : hit.getBlockPos();
	}

	private static boolean hasGrowth(ServerLevel level, BlockPos center) {
		for (BlockPos p : BlockPos.betweenClosed(
				center.offset(-HALF_WIDTH, -HALF_HEIGHT, -HALF_WIDTH),
				center.offset(HALF_WIDTH, HALF_HEIGHT, HALF_WIDTH))) {
			BlockState state = level.getBlockState(p);
			if (state.getBlock() instanceof BonemealableBlock bb && bb.isValidBonemealTarget(level, p, state)) return true;
		}
		return false;
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		if (player.level() instanceof ServerLevel level) {
			BlockPos center = center(player, level);
			if (center == null || !hasGrowth(level, center)) {
				player.displayClientMessage(Component.literal("Nothing here can be made to grow."), true);
				return false;
			}
		}
		return super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (!(world instanceof ServerLevel sLevel)) return;

		BlockPos center = center(player, world);
		if (center == null) return;
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
				ManipulationVisuals.burst(sLevel, ManipulationVisuals.Form.GROWTH, Vec3.atBottomCenterOf(p.above()), Vec3.atCenterOf(p), .35, 24);
                grew++;
			}
		}

		if (grew > 0) {
			world.playSound(null, center, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 0.8f, 0.9f + world.random.nextFloat() * 0.2f);
		}
	        }
    }
}

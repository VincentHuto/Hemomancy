package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Sanguine Excavation — a T2 (MEDIOCRITAS) quick manipulation that mines
 * a cluster of matching blocks centred on the block the player is looking at.
 * <p>
 * Uses a simple flood-fill limited to the same block type to excavate
 * a compact cluster, then drops items naturally. Both the raycast range
 * and the excavation radius/block count scale with the Sanguine Reach skill.
 * <p>
 * Base: 9 blocks in a ~2-block radius.  At max Sanguine Reach: 27 blocks
 * in a ~4-block radius.
 */
public class SanguineExcavationManip extends BloodManipulation {

	private static final double BASE_RANGE = 24.0;
	/** Minimum blocks to excavate (no skill investment). */
	private static final int BASE_MAX_BLOCKS = 9;
	/** Maximum blocks to excavate (at max skill). */
	private static final int CAP_MAX_BLOCKS = 27;
	/** Base squared-distance limit for flood-fill from centre. */
	private static final double BASE_RADIUS_SQ = 4.0;  // ~2 blocks
	/** Maximum squared-distance limit at max skill. */
	private static final double CAP_RADIUS_SQ = 16.0;  // ~4 blocks

	public SanguineExcavationManip(String name, double cost, double alignLevel, double xpCost,
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

		Vec3 eyePos = player.getEyePosition(1.0F);
		Vec3 lookVec = player.getViewVector(1.0F);
		Vec3 endPos = eyePos.add(lookVec.scale(range));

		BlockHitResult hitResult = world.clip(new ClipContext(
				eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

		if (hitResult.getType() == HitResult.Type.MISS) {
			player.displayClientMessage(
					Component.literal("§cLook at a block to excavate!"), true);
			return;
		}

		BlockPos center = hitResult.getBlockPos();
		BlockState targetState = world.getBlockState(center);
		Block targetBlock = targetState.getBlock();

		if (targetState.getDestroySpeed(world, center) < 0) {
			return;
		}

		double reachMult = SkillPointHelper.getSanguineReachMultiplier(player);
		int maxBlocks = (int) Math.min(CAP_MAX_BLOCKS,
				BASE_MAX_BLOCKS * reachMult);
		double radiusSq = Math.min(CAP_RADIUS_SQ,
				BASE_RADIUS_SQ * reachMult * reachMult);

		List<BlockPos> toBreak = new ArrayList<>();
		Queue<BlockPos> queue = new LinkedList<>();
		queue.add(center);
		toBreak.add(center);

		while (!queue.isEmpty() && toBreak.size() < maxBlocks) {
			BlockPos current = queue.poll();
			for (BlockPos neighbor : BlockPos.betweenClosed(
					current.offset(-1, -1, -1), current.offset(1, 1, 1))) {
				BlockPos immutable = neighbor.immutable();
				if (!toBreak.contains(immutable)
						&& world.getBlockState(immutable).getBlock() == targetBlock
						&& immutable.distSqr(center) <= radiusSq) {
					toBreak.add(immutable);
					queue.add(immutable);
					if (toBreak.size() >= maxBlocks) break;
				}
			}
		}

		RandomSource random = world.random;
		for (BlockPos bp : toBreak) {
			BlockState state = world.getBlockState(bp);
			Block.dropResources(state, world, bp, world.getBlockEntity(bp), player, heldItemMainhand);
			world.destroyBlock(bp, false);

			sLevel.sendParticles(
					GlowParticleFactory.createData(new ParticleColor(53, 53 + random.nextFloat() * 50, 53)),
					bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5,
					2, 0.2, 0.2, 0.2, 0.01f);
		}

		if (!toBreak.isEmpty()) {
			world.playSound(null, center, SoundEvents.IRON_GOLEM_DAMAGE, SoundSource.PLAYERS, 0.7f, 1.4f);
		}
	}
}

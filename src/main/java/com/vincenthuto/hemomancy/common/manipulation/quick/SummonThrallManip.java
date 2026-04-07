package com.vincenthuto.hemomancy.common.manipulation.quick;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.entity.mob.BloodThrallEntity;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Summon Thrall — a manipulation that manifests a Blood Thrall at the block
 * the player is looking at. The thrall spawns between the targeted block and
 * the block above it. It starts in IDLE state with no source/dest bindings;
 * the player can later pick it up and bind it via the effigy item, or absorb
 * it back to recover blood.
 */
public class SummonThrallManip extends BloodManipulation {

	private static final double SUMMON_RANGE = 16.0;

	public SummonThrallManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		Vec3 eyePos = player.getEyePosition(1.0F);
		Vec3 lookVec = player.getViewVector(1.0F);
		Vec3 endPos = eyePos.add(lookVec.scale(SUMMON_RANGE));

		BlockHitResult hitResult = world.clip(new ClipContext(
				eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

		if (hitResult.getType() == HitResult.Type.MISS) {
			return;
		}

		// Spawn between the hit block and the one above it
		BlockPos hitBlock = hitResult.getBlockPos();
		BlockPos spawnPos = hitBlock.above();

		// Spawn with no source/dest — thrall starts IDLE until picked up and bound via the effigy item
		BloodThrallEntity thrall = new BloodThrallEntity(world, player, null, null);
		thrall.moveTo(
				spawnPos.getX() + 0.5,
				spawnPos.getY(),
				spawnPos.getZ() + 0.5,
				player.getYRot(), 0);
		world.addFreshEntity(thrall);

		world.playSound(null, spawnPos, SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.8f, 0.6f);
	}
}

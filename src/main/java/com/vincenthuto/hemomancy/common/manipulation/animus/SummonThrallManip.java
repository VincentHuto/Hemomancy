package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.entity.summon.BloodThrallEntity;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Summon Thrall — a two-step manipulation:
 * <ol>
 *   <li><b>First activation:</b> Raycasts to a block, spawns a Blood Thrall
 *       with that block as the <em>source</em>. The thrall enters AWAITING_TARGET
 *       state, and a blue venous leash renders from it to the player's hand.</li>
 *   <li><b>Second activation:</b> Raycasts to another block and sets it as the
 *       <em>destination</em>. The thrall begins its work cycle. No additional
 *       blood cost is charged for this step.</li>
 * </ol>
 */
public class SummonThrallManip extends BloodManipulation {

	private static final double SUMMON_RANGE = 16.0;

	/** Server-side map: player UUID → entity ID of the thrall awaiting target selection */
	private static final Map<UUID, Integer> PENDING_THRALLS = new HashMap<>();

	public SummonThrallManip(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	/**
	 * Returns true if the given player currently has a pending thrall awaiting
	 * target selection.
	 */
	public static boolean hasPendingThrall(UUID playerUUID) {
		return PENDING_THRALLS.containsKey(playerUUID);
	}

	/**
	 * Removes a player's pending thrall entry (called on disconnect, death, etc.)
	 */
	public static void clearPendingThrall(UUID playerUUID) {
		PENDING_THRALLS.remove(playerUUID);
	}

	@Override
	public void performAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!world.isClientSide) {
			// If the player already has a pending thrall, the second activation
			// should NOT cost blood or trigger cooldown — just call getAction directly.
			if (PENDING_THRALLS.containsKey(player.getUUID())) {
				getAction(player, world, heldItemMainhand, position);
				return;
			}
		}
		// First activation — use the normal cost/cooldown flow
		super.performAction(player, world, heldItemMainhand, position);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		Vec3 eyePos = player.getEyePosition(1.0F);
		Vec3 lookVec = player.getViewVector(1.0F);
		Vec3 endPos = eyePos.add(lookVec.scale(SUMMON_RANGE));

		BlockHitResult hitResult = world.clip(new ClipContext(
				eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

		if (hitResult.getType() == HitResult.Type.MISS) {
			player.displayClientMessage(
					Component.literal("§cLook at a block to select a position!"), true);
			return;
		}

		BlockPos hitBlock = hitResult.getBlockPos();
		UUID playerUUID = player.getUUID();

		// ── Second activation: set destination on the pending thrall ──
		if (PENDING_THRALLS.containsKey(playerUUID)) {
			int thrallId = PENDING_THRALLS.get(playerUUID);
			Entity entity = world.getEntity(thrallId);

			if (entity instanceof BloodThrallEntity thrall && thrall.isAlive() && thrall.getAwaitingTarget()) {
				// Don't allow source and dest to be the same
				if (hitBlock.equals(thrall.getSourcePos())) {
					player.displayClientMessage(
							Component.literal("§cSource and destination cannot be the same block!"), true);
					return;
				}

				// Destination must be a valid blood tile (has blood volume capability)
				BlockEntity destBe = world.getBlockEntity(hitBlock);
				if (destBe == null || !HemoCapabilityAccess.getBloodVolume(destBe).isPresent()) {
					player.displayClientMessage(
							Component.literal("§cDestination must be a block with a blood reservoir!"), true);
					return;
				}

				thrall.setDestPos(hitBlock);
				thrall.setAwaitingTarget(false);
				PENDING_THRALLS.remove(playerUUID);

				player.displayClientMessage(
						Component.literal("§9Thrall bound! §7Destination: §9" + hitBlock.toShortString()), true);
				world.playSound(null, hitBlock, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.2f);
			} else {
				// Thrall is dead or invalid — clean up and fall through to spawn a new one
				PENDING_THRALLS.remove(playerUUID);
				player.displayClientMessage(
						Component.literal("§cPrevious thrall was lost. Summoning a new one..."), true);
				spawnNewThrall(player, world, hitBlock);
			}
			return;
		}

		// ── First activation: spawn thrall with source, enter awaiting state ──
		spawnNewThrall(player, world, hitBlock);
	}

	private void spawnNewThrall(Player player, Level world, BlockPos sourceBlock) {
		// Source must be a valid blood tile (has blood volume capability)
		BlockEntity srcBe = world.getBlockEntity(sourceBlock);
		if (srcBe == null || !HemoCapabilityAccess.getBloodVolume(srcBe).isPresent()) {
			player.displayClientMessage(
					Component.literal("§cSource must be a block with a blood reservoir!"), true);
			return;
		}

		BlockPos spawnPos = sourceBlock.above();

		BloodThrallEntity thrall = new BloodThrallEntity(world, player, sourceBlock, null);
		thrall.moveTo(
				spawnPos.getX() + 0.5,
				spawnPos.getY(),
				spawnPos.getZ() + 0.5,
				player.getYRot(), 0);
		thrall.setAwaitingTarget(true);
		world.addFreshEntity(thrall);

		PENDING_THRALLS.put(player.getUUID(), thrall.getId());

		player.displayClientMessage(
				Component.literal("§4Thrall summoned! §7Source: §c" + sourceBlock.toShortString()
						+ " §7— Now select a destination block."), true);
		world.playSound(null, spawnPos, SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.8f, 0.6f);
	}
}

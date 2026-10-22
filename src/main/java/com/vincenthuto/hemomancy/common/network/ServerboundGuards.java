package com.vincenthuto.hemomancy.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Shared validation for client-to-server payloads. A serverbound handler checks what the client claims (an open menu,
 * a block or entity it is using, a number it sent) through these helpers before it acts.
 */
public final class ServerboundGuards {
	/** Squared reach used by block interactions: eight blocks to the block's centre. */
	public static final double BLOCK_REACH_SQR = 64.0D;

	private ServerboundGuards() {
	}

	/** The player's open menu if it is a {@code type} the player may still use, otherwise {@code null}. */
	@Nullable
	public static <M extends AbstractContainerMenu> M menu(Player player, Class<M> type) {
		AbstractContainerMenu open = player.containerMenu;
		return type.isInstance(open) && open.stillValid(player) ? type.cast(open) : null;
	}

	/** {@code pos} is loaded and its centre is within {@code maxDistanceSqr} of the player. */
	public static boolean withinReach(Player player, @Nullable BlockPos pos, double maxDistanceSqr) {
		return pos != null && player.level().hasChunkAt(pos)
				&& player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= maxDistanceSqr;
	}

	/** {@code pos} is within reach and holds {@code block}. */
	public static boolean nearBlock(Player player, @Nullable BlockPos pos, Block block, double maxDistanceSqr) {
		return withinReach(player, pos, maxDistanceSqr) && player.level().getBlockState(pos).is(block);
	}

	/** The block entity at {@code pos} if it is within reach and a {@code type}, otherwise {@code null}. */
	@Nullable
	public static <T extends BlockEntity> T nearBlockEntity(Player player, @Nullable BlockPos pos, Class<T> type,
			double maxDistanceSqr) {
		if (!withinReach(player, pos, maxDistanceSqr)) {
			return null;
		}
		BlockEntity blockEntity = player.level().getBlockEntity(pos);
		return type.isInstance(blockEntity) ? type.cast(blockEntity) : null;
	}

	/** The entity with {@code entityId} if it exists within {@code maxDistance} blocks of the player, otherwise {@code null}. */
	@Nullable
	public static Entity nearEntity(Player player, int entityId, double maxDistance) {
		Entity entity = player.level().getEntity(entityId);
		return entity != null && player.distanceTo(entity) <= maxDistance ? entity : null;
	}

	/** The stack in the player's main hand, else off hand, whose item is a {@code type}, otherwise empty. */
	public static ItemStack heldItem(Player player, Class<? extends Item> type) {
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = player.getItemInHand(hand);
			if (type.isInstance(stack.getItem())) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	/**
	 * A client-reported partial tick, clamped to [0, 1]. A larger value would extrapolate the player's eye past where
	 * it is and stretch reach checks; NaN and infinity become 1.
	 */
	public static float partialTick(float reported) {
		return Float.isFinite(reported) ? Math.clamp(reported, 0.0F, 1.0F) : 1.0F;
	}

	/** Every value is a finite number: no NaN and no infinity. */
	public static boolean finite(double... values) {
		for (double value : values) {
			if (!Double.isFinite(value)) {
				return false;
			}
		}
		return true;
	}
}

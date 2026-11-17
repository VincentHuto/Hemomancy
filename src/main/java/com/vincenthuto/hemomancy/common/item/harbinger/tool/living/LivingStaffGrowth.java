package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffBondHelper;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Living Staff's growth in the world (Track 8): the player's handled blood lays down rings
 * ({@link LivingStaffGrowthRules}), the Staff and every form it takes carry the ring count as the
 * {@code hemomancy:staff_rings} component so both sides read one tier and draw one set of bands, and the Staff's
 * crawl and hewing Sculptings harvest at the tier the rings have earned.
 */
public final class LivingStaffGrowth {
	private LivingStaffGrowth() {
	}

	/** The rings {@code player}'s Staff has laid down, from their handled blood (synced to the owner). */
	public static int ring(Player player) {
		if (player == null) return 0;
		return LivingStaffGrowthRules.ring(HemoCapabilityAccess.getLivingStaffProgress(player).getBloodHandled());
	}

	/** The ring count stamped on a Staff or one of its forms; 0 for an unstamped stack. */
	public static int stampedRing(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return 0;
		Integer ring = stack.get(DataComponentInit.STAFF_RINGS.get());
		return ring == null ? 0 : LivingStaffGrowthRules.clampRing(ring);
	}

	/** Stamps {@code ring} on {@code stack}; returns whether it changed. Ring 0 leaves the stack unstamped. */
	public static boolean stamp(ItemStack stack, int ring) {
		if (stack == null || stack.isEmpty()) return false;
		int clamped = LivingStaffGrowthRules.clampRing(ring);
		if (stampedRing(stack) == clamped && (clamped > 0) == stack.has(DataComponentInit.STAFF_RINGS.get())) {
			return false;
		}
		if (clamped == 0) stack.remove(DataComponentInit.STAFF_RINGS.get());
		else stack.set(DataComponentInit.STAFF_RINGS.get(), clamped);
		return true;
	}

	/** Keeps a carried Staff's (or form's) stamp equal to its holder's rings; server side only. */
	public static void refreshStamp(Player holder, ItemStack stack) {
		if (holder == null || holder.level().isClientSide) return;
		if (!HemoCapabilityAccess.getLivingStaffProgress(holder).hasLivingStaffBond()) return;
		stamp(stack, ring(holder));
	}

	/**
	 * Adds {@code handled} mL to the Staff's age and syncs it to its owner. A ring that closes now is announced and
	 * stamped on the held Staff or form at once.
	 */
	public static void record(ServerPlayer player, double handled) {
		if (player == null || !(handled > 0.0D)) return;
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player);
		int before = LivingStaffGrowthRules.ring(progress.getBloodHandled());
		progress.addBloodHandled(handled);
		int after = LivingStaffGrowthRules.ring(progress.getBloodHandled());
		LivingStaffBondHelper.syncProgress(player);
		if (after > before) {
			HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_FIRST_RING);
			player.displayClientMessage(Component.translatable("message.hemomancy.living_staff.ring_laid", after,
					LivingStaffGrowthRules.MAX_RING).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BONE_BLOCK_PLACE,
					SoundSource.PLAYERS, 0.8F, 0.6F);
			refreshStamp(player, player.getMainHandItem());
			refreshStamp(player, player.getOffhandItem());
		}
	}

	/**
	 * A plain tool of the tier {@code ring} has earned: what the Staff's crawl and hewing break with. It carries no
	 * enchantment, so their drops are plain (no Fortune, no Silk Touch).
	 */
	public static ItemStack harvestTool(int ring) {
		return new ItemStack(switch (LivingStaffGrowthRules.digTier(ring).harvest()) {
			case STONE -> Items.STONE_PICKAXE;
			case IRON -> Items.IRON_PICKAXE;
			case DIAMOND -> Items.DIAMOND_PICKAXE;
		});
	}

	public static ItemStack harvestTool(Player player) {
		return harvestTool(ring(player));
	}

	/** Whether the tier {@code ring} has earned gets drops from {@code state}. */
	public static boolean canHarvest(BlockState state, int ring) {
		return !state.requiresCorrectToolForDrops() || harvestTool(ring).isCorrectToolForDrops(state);
	}
}

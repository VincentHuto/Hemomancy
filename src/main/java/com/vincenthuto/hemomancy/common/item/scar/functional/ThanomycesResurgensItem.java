package com.vincenthuto.hemomancy.common.item.scar.functional;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.scar.ItemFungalScar;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ThanomycesResurgensItem extends ItemFungalScar {

	/** Cooldown in ticks: 15 minutes = 18,000 ticks. */
	public static final long COOLDOWN_TICKS = 18_000L;

	/** Health fraction the player is restored to on trigger (25%). */
	public static final float REFORM_FRACTION = 0.25f;

	private static final String TAG_COOLDOWN_EXPIRY = "split_husk_cooldown_expiry";

	public ThanomycesResurgensItem(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		super(properties, tendencyIn, deepenAmountIn);
	}

	/**
	 * Returns true if this scar is ready to trigger (not on cooldown).
	 * The cooldown expiry is stored as an absolute world-time tick in the item's NBT.
	 */
	public boolean isReady(ItemStack stack, long currentGameTime) {
		CompoundTag tag = stack.getOrCreateTag();
		if (!tag.contains(TAG_COOLDOWN_EXPIRY)) return true;
		return currentGameTime >= tag.getLong(TAG_COOLDOWN_EXPIRY);
	}

	/** Records the cooldown on this specific item stack. */
	public void startCooldown(ItemStack stack, long currentGameTime) {
		stack.getOrCreateTag().putLong(TAG_COOLDOWN_EXPIRY, currentGameTime + COOLDOWN_TICKS);
	}

	/** Remaining cooldown ticks, 0 if ready. */
	public long remainingCooldown(ItemStack stack, long currentGameTime) {
		CompoundTag tag = stack.getOrCreateTag();
		if (!tag.contains(TAG_COOLDOWN_EXPIRY)) return 0L;
		return Math.max(0L, tag.getLong(TAG_COOLDOWN_EXPIRY) - currentGameTime);
	}

	@Override
	public void onEquipped(LivingEntity player) {
		super.onEquipped(player);
	}

	@Override
	public void onUnequipped(LivingEntity player) {
		super.onUnequipped(player);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal("Split Husk").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("The body breaks open. Something older holds the pieces together.").withStyle(ChatFormatting.ITALIC));
		tooltip.add(Component.literal("✦ Prevents death once — reforms at 25% health").withStyle(ChatFormatting.DARK_GREEN));
		tooltip.add(Component.literal("✦ Drains all blood on trigger").withStyle(ChatFormatting.RED));
		tooltip.add(Component.literal("✦ 15 minute cooldown").withStyle(ChatFormatting.RED));
	}
}

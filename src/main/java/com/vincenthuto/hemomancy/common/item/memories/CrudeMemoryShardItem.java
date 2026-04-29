package com.vincenthuto.hemomancy.common.item.memories;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.KnownManipulationServerPacket;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * A rough echo of a HUMILIS-rank manipulation scraped from a Harbinger
 * chamber wall.  Right-clicking teaches the encoded manipulation directly,
 * costing {@link #BLOOD_COST} mL of blood.
 * <p>
 * Only works while blood is active and only for HUMILIS-rank manipulations.
 * This is the pre-Somatic-Loom path to learning one's first manipulation.
 */
public class CrudeMemoryShardItem extends Item {

	/** Blood cost to absorb a crude memory. */
	private static final double BLOOD_COST = 500.0;

	private final DeferredHolder<BloodManipulation, BloodManipulation> manip;

	public CrudeMemoryShardItem(Properties properties,
			DeferredHolder<BloodManipulation, BloodManipulation> manip) {
		super(properties.stacksTo(1));
		this.manip = manip;
	}

	public BloodManipulation getManip() {
		return manip.get();
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context,
			List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.literal(
				"An echo scraped from the wall of a Harbinger chamber. The pattern is rough but legible\u2014someone has done this before.")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		if (getManip() != null) {
			tooltip.add(Component.literal("\u00a77Encodes: \u00a4" + getManip().getProperName())
					.withStyle(ChatFormatting.DARK_RED));
			tooltip.add(Component.literal("\u00a77Blood cost to absorb: " + (int) BLOOD_COST + " mL")
					.withStyle(ChatFormatting.DARK_RED));
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) return super.use(level, player, hand);
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);

		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
				.orElse(null);
		if (volume == null || !volume.isActive()) {
			player.displayClientMessage(
					Component.literal("You lack the understanding of what you're even holding...")
							.withStyle(ChatFormatting.DARK_RED),
					true);
			return InteractionResultHolder.fail(stack);
		}

		BloodManipulation manipulation = getManip();
		if (manipulation == null || manipulation.getRank() != EnumManipulationRank.HUMILIS) {
			player.displayClientMessage(
					Component.literal("This memory is too complex to absorb raw.")
							.withStyle(ChatFormatting.DARK_RED),
					true);
			return InteractionResultHolder.fail(stack);
		}

		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
				.orElse(null);
		if (known == null) return InteractionResultHolder.fail(stack);

		LinkedHashMap<BloodManipulation, ManipLevel> knownList = known.getKnownManips();
		if (known.doesListContainName(knownList, manipulation)) {
			player.displayClientMessage(
					Component.literal("You already carry this memory.")
							.withStyle(ChatFormatting.DARK_RED),
					true);
			return InteractionResultHolder.fail(stack);
		}

		if (volume.wouldOverstrain(BLOOD_COST)) {
			player.displayClientMessage(
					Component.literal("You don't have enough blood to absorb this.")
							.withStyle(ChatFormatting.DARK_RED),
					true);
			return InteractionResultHolder.fail(stack);
		}

		// Teach the manipulation
		volume.drain(BLOOD_COST);
		PacketHandler.sendToPlayer((ServerPlayer) player,
				new com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket(volume));
		knownList.put(manipulation, ManipLevel.BLANK);
		PacketHandler.sendToPlayer((ServerPlayer) player, new KnownManipulationServerPacket(known));
		stack.shrink(1);

		player.displayClientMessage(
				Component.literal("The echo settles into your bloodstream.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);

		return InteractionResultHolder.sidedSuccess(stack, false);
	}
}

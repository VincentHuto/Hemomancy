package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationRankGates;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;

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

import java.util.List;

/**
 * A rough echo of a HUMILIS-rank manipulation scraped from a Harbinger
 * chamber wall.  Right-clicking teaches the encoded manipulation directly,
 * costing {@link #BLOOD_COST} mL of blood.
 * <p>
 * Only works while blood is active and the player has reached the manipulation's
 * rank gate. This is the pre-Somatic-Loom path to learning one's first
 * manipulation.
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
			tooltip.add(Component.literal(getManip().getProperName())
					.withStyle(ChatFormatting.DARK_RED));
			tooltip.add(Component.literal("Blood cost to absorb: " + (int) BLOOD_COST + " mL")
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
		if (manipulation == null) {
			player.displayClientMessage(
					Component.literal("This memory is too complex to absorb raw.")
							.withStyle(ChatFormatting.DARK_RED),
					true);
			return InteractionResultHolder.fail(stack);
		}
		int playerDegree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		if (!ManipulationRankGates.playerMeetsRank(playerDegree, manipulation.getRank())) {
			player.displayClientMessage(
					Component.literal("This memory requires Degree "
									+ ManipulationRankGates.minDegreeForRank(manipulation.getRank()) + ".")
							.withStyle(ChatFormatting.DARK_RED),
					true);
			return InteractionResultHolder.fail(stack);
		}

		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
				.orElse(null);
		if (known == null) return InteractionResultHolder.fail(stack);

		if (known.doesListContainName(known.getKnownManips(), manipulation)) {
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

		volume.drain(BLOOD_COST);
		PacketHandler.sendToPlayer((ServerPlayer) player,
				new BloodVolumeServerPacket(volume));
		KnownManipulationGrantHelper.learnAndEquipIfPossible(known, manipulation,
				ManipSlotHelper.getMaxSlots(player));
		PacketHandler.sendToPlayer((ServerPlayer) player, new KnownManipulationServerPacket(known));
		stack.shrink(1);

		player.displayClientMessage(
				Component.literal("The echo settles into your bloodstream.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);

		return InteractionResultHolder.sidedSuccess(stack, false);
	}
}

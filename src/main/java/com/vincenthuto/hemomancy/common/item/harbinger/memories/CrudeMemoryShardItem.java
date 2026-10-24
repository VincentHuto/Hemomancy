package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationRankGates;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
	private static final BloodAccounting.Source ABSORPTION = BloodAccounting.Source.of("crude_memory_shard", Category.TOOL, false, false);

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
		tooltip.add(Component.translatable("tooltip.hemomancy.crude_memory_shard.an_echo_scraped_from_the_wall").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		if (getManip() != null) {
			tooltip.add(getManip().getDisplayName()
					.withStyle(ChatFormatting.DARK_RED));
			if (ManipulationRetirementRules.isRetiredMemoryItem(this, getManip())) {
				tooltip.add(Component.translatable("tooltip.hemomancy.crude_memory_shard.this_echo_has_gone_dormant")
						.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
				return;
			}
			tooltip.add(HemoText.translatable("tooltip.hemomancy.crude_memory_shard.blood_cost_to_absorb_ml",
					(int) BLOOD_COST).withStyle(ChatFormatting.DARK_RED));
			tooltip.add(HemoText.translatable("tooltip.hemomancy.crude_memory_shard.right_click_with_active_blood_learning",
					ManipulationRankGates.minDegreeForRank(getManip().getRank())).withStyle(ChatFormatting.GRAY));
			tooltip.add(HemoText.translatable("tooltip.hemomancy.crude_memory_shard.casting_requires_alignment",
					(int) getManip().getAlignLevel(),
					BloodManipulation.tendencyName(getManip().getTend()),
					getManip().getBaseCostLabel(),
					getManip().getBaseCostText()).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) return super.use(level, player, hand);
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);

		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
				;
		if (volume == null || !volume.isActive()) {
			player.displayClientMessage(
					Component.translatable("message.hemomancy.crude_memory_shard_item.you_don_t_understand_what_you").withStyle(ChatFormatting.DARK_RED),
					true);
			return InteractionResultHolder.fail(stack);
		}

		BloodManipulation manipulation = getManip();
		if (ManipulationRetirementRules.isRetiredMemoryItem(this, manipulation)) {
			player.displayClientMessage(
					Component.translatable("message.hemomancy.crude_memory_shard_item.this_echo_has_gone_dormant").withStyle(ChatFormatting.DARK_GRAY),
					true);
			return InteractionResultHolder.fail(stack);
		}
		if (manipulation == null) {
			player.displayClientMessage(
					Component.translatable("message.hemomancy.crude_memory_shard_item.this_memory_is_too_complex_to").withStyle(ChatFormatting.DARK_RED),
					true);
			return InteractionResultHolder.fail(stack);
		}
		int playerDegree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		if (!ManipulationRankGates.playerMeetsRank(playerDegree, manipulation.getRank())) {
			player.displayClientMessage(
					HemoText.translatable("message.hemomancy.crude_memory_shard_item.this_memory_requires_degree",
							ManipulationRankGates.minDegreeForRank(manipulation.getRank())).withStyle(ChatFormatting.DARK_RED),
					true);
			return InteractionResultHolder.fail(stack);
		}

		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
				;
		if (known == null) return InteractionResultHolder.fail(stack);

		if (known.doesListContainName(known.getKnownManips(), manipulation)) {
			player.displayClientMessage(
					Component.translatable("message.hemomancy.crude_memory_shard_item.you_already_carry_this_memory").withStyle(ChatFormatting.DARK_RED),
					true);
			return InteractionResultHolder.fail(stack);
		}

		if (volume.wouldOverstrain(BLOOD_COST)) {
			player.displayClientMessage(
					Component.translatable("message.hemomancy.crude_memory_shard_item.you_don_t_have_enough_blood").withStyle(ChatFormatting.DARK_RED),
					true);
			return InteractionResultHolder.fail(stack);
		}

		BloodAccounting.pay(player, BLOOD_COST, ABSORPTION);
		KnownManipulationGrantHelper.learnAndEquipIfPossible(known, manipulation,
				ManipSlotHelper.getMaxSlots(player));
		KnownManipulationEvents.syncPlayerEvent(player);
		stack.shrink(1);

		player.displayClientMessage(
				Component.translatable("message.hemomancy.crude_memory_shard_item.the_echo_settles_into_your_bloodstream").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
				false);

		return InteractionResultHolder.sidedSuccess(stack, false);
	}
}

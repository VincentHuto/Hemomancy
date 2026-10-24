package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.MemoryGrantStatus;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
import com.vincenthuto.hemomancy.common.util.HemoText;
import com.vincenthuto.hutoslib.client.HLTextUtils;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class BloodMemoryItem extends Item {

	DeferredHolder<BloodManipulation, BloodManipulation> manip;

	public BloodMemoryItem(Properties properties, DeferredHolder<BloodManipulation, BloodManipulation> manip) {
		super(properties.stacksTo(1));
		this.manip = manip;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		if (getManip() != null) {
			tooltip.add(getManip().getDisplayName());
			if (isRetiredMemoryItem()) {
				tooltip.add(Component.translatable("tooltip.hemomancy.blood_memory.this_memory_has_gone_dormant")
						.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
				return;
			}
			getManip().getDrudgeAction().ifPresentOrElse(da -> {
				if (da == com.vincenthuto.hemomancy.common.manipulation.DrudgeAction.DRUDGE_UNSUPPORTED) {
					tooltip.add(Component.translatable("tooltip.hemomancy.blood_memory.cnot_usable_by_drudges"));
				} else {
					getManip().getDrudgeDescription().ifPresent(desc ->
							tooltip.add(Component.translatable("tooltip.hemomancy.blood_memory.7drudge_s", desc)));
				}
			}, () -> { /* no DrudgeAction registered yet — show nothing */ });
			tooltip.add(Component.translatable("tooltip.hemomancy.blood_memory.burn_in_a_lit_iron_brazier")
					.withStyle(ChatFormatting.DARK_GRAY));
			tooltip.add(HemoText.translatable("tooltip.hemomancy.blood_memory.learn_at_degree_cast_at_alignment",
					com.vincenthuto.hemomancy.common.manipulation.ManipulationRankGates.minDegreeForRank(getManip().getRank()),
					(int) getManip().getAlignLevel(),
					BloodManipulation.tendencyName(getManip().getTend())).withStyle(ChatFormatting.GRAY));
			tooltip.add(getManip().getBaseCostLine()
					.withStyle(ChatFormatting.DARK_RED));
			ManipulationFamilyRegistry.form(getManip().getName()).ifPresent(form ->
					tooltip.add(HemoText.translatable("tooltip.hemomancy.blood_memory.requires_mastery_stage_intermediate_memories_are",
							BloodManipulation.displayName(ManipulationFamilyRegistry.baselineId(getManip().getName())),
							form.requiredLevel()).withStyle(ChatFormatting.DARK_RED)));
		}
	}

	public BloodManipulation getManip() {
		return manip.get();
	}

	public boolean isRetiredMemoryItem() {
		return ManipulationRetirementRules.isRetiredMemoryItem(this, getManip());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getName(ItemStack stack) {
		return Component
				.literal(HLTextUtils.stringToBloody(
						HLTextUtils.convertInitToLang(HLTextUtils.getItemRegistryName(stack.getItem()))))
				.withStyle(ChatFormatting.DARK_RED);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (handIn == InteractionHand.MAIN_HAND) {
			ItemStack stack = playerIn.getItemInHand(handIn);
			if (playerIn.isCreative()) {
				if (worldIn.isClientSide) {
					return InteractionResultHolder.sidedSuccess(stack, true);
				}
				var result = KnownManipulationGrantHelper.grantCreativeMemory((ServerPlayer) playerIn, getManip(), this);
				Component message = switch (result.status()) {
					case GRANTED_EQUIPPED -> Component.translatable("message.hemomancy.blood_memory_item.the_memory_settles_into_an_open");
					case GRANTED -> Component.translatable("message.hemomancy.blood_memory_item.the_memory_settles_into_your_blood");
					case ALREADY_KNOWN -> Component.translatable("message.hemomancy.blood_memory_item.you_already_carry_this_memory");
					case RETIRED -> Component.translatable("message.hemomancy.blood_memory_item.this_memory_has_gone_dormant");
					default -> Component.translatable("message.hemomancy.blood_memory_item.the_memory_will_not_take");
				};
				playerIn.displayClientMessage(message.copy().withStyle(ChatFormatting.DARK_RED), true);
				return result.success() || result.status() == MemoryGrantStatus.ALREADY_KNOWN
						? InteractionResultHolder.sidedSuccess(stack, false)
						: InteractionResultHolder.fail(stack);
			}
			if (!worldIn.isClientSide && !playerIn.isShiftKeyDown()) {
				playerIn.displayClientMessage(Component.translatable("message.hemomancy.blood_memory_item.burn_this_memory_in_a_lit").withStyle(ChatFormatting.DARK_RED), true);
				return InteractionResultHolder.fail(stack);
			}
		}
		return super.use(worldIn, playerIn, handIn);

	}

}

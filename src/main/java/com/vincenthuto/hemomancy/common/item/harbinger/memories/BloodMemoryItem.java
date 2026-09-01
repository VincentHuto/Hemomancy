package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
			tooltip.add(Component.literal(getManip().getProperName()));
			if (isRetiredMemoryItem()) {
				tooltip.add(Component.literal("This memory has gone dormant.")
						.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
				return;
			}
			getManip().getDrudgeAction().ifPresentOrElse(da -> {
				if (da == com.vincenthuto.hemomancy.common.manipulation.DrudgeAction.DRUDGE_UNSUPPORTED) {
					tooltip.add(Component.literal("§cNot usable by Drudges"));
				} else {
					getManip().getDrudgeDescription().ifPresent(desc ->
							tooltip.add(Component.literal("§7Drudge: " + desc)));
				}
			}, () -> { /* no DrudgeAction registered yet — show nothing */ });
			tooltip.add(Component.literal("Burn in a lit Iron Brazier and absorb with Blood Absorption.")
					.withStyle(ChatFormatting.DARK_GRAY));
			ManipulationFamilyRegistry.form(getManip().getName()).ifPresent(form ->
					tooltip.add(Component.literal("Requires family mastery stage " + form.requiredLevel() + ".")
							.withStyle(ChatFormatting.DARK_RED)));
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
			if (!worldIn.isClientSide && !playerIn.isShiftKeyDown()) {
				playerIn.displayClientMessage(Component.literal(
						"Burn this memory in a lit Iron Brazier and absorb it with Blood Absorption.")
						.withStyle(ChatFormatting.DARK_RED), true);
				return InteractionResultHolder.fail(stack);
			}
		}
		return super.use(worldIn, playerIn, handIn);

	}

}

package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.Arrays;
import java.util.List;

public class LivingWeaponGraftItem extends Item {
	public LivingWeaponGraftItem(Properties properties) {
		super(properties);
	}

	@Override
	public Component getName(ItemStack stack) {
		return form(stack).graftName().withStyle(ChatFormatting.DARK_RED);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
			TooltipFlag flag) {
		LivingWeaponForm form = form(stack);
		tooltip.add(Component.translatable("tooltip.hemomancy.living_weapon_graft.a_prepared_s", form.patternName())
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("tooltip.hemomancy.living_weapon_graft.burn_it_in_an_iron_brazier", form.manipulationDisplayName())
				.withStyle(ChatFormatting.DARK_RED));
		tooltip.add(Component.translatable("tooltip.hemomancy.living_weapon_graft.hold_the_living_staff_and_channel")
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(HemoText.translatable("tooltip.hemomancy.living_weapon_graft.base_switch_ml_weapons_master_reduces",
				(int) com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormRules.BASE_HOT_SWAP_COST_ML).withStyle(ChatFormatting.DARK_RED));
	}

	public static List<ItemStack> creativeStacks() {
		return Arrays.stream(LivingWeaponForm.values())
				.map(LivingWeaponGraftData::createStack)
				.toList();
	}

	private static LivingWeaponForm form(ItemStack stack) {
		return LivingWeaponGraftData.fromStack(stack)
				.map(LivingWeaponGraftData::form)
				.orElse(LivingWeaponForm.BLADE);
	}

}

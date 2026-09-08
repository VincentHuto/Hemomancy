package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponGraftData;
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
		return Component.literal(form(stack).graftName()).withStyle(ChatFormatting.DARK_RED);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
			TooltipFlag flag) {
		LivingWeaponForm form = form(stack);
		tooltip.add(Component.literal("A prepared " + form.patternName() + ".")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("Burn it in an Iron Brazier and draw it in with Blood Absorption to remember "
						+ form.manipulationDisplayName() + ".")
				.withStyle(ChatFormatting.DARK_RED));
		tooltip.add(Component.literal("Hold the Living Staff and channel into the lit brazier. Earn the graft's activity first.")
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Base switch: "
				+ (int) com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormRules.BASE_HOT_SWAP_COST_ML
				+ " mL; Weapons Master reduces it to 50 mL. Weapon attacks cost separately.")
				.withStyle(ChatFormatting.DARK_RED));
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

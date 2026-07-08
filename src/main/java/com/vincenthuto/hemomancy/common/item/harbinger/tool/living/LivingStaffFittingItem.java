package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentType;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class LivingStaffFittingItem extends Item implements IHarbingerEquipment {
	private final int staffVisualId;

	public LivingStaffFittingItem(Properties properties, int staffVisualId) {
		super(properties.stacksTo(1));
		this.staffVisualId = staffVisualId;
	}

	public int getStaffVisualId() {
		return staffVisualId;
	}

	@Override
	public HarbingerEquipmentType getHarbingerEquipmentType() {
		return HarbingerEquipmentType.FITTING;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("item.hemomancy.living_staff_fitting.tooltip")
				.withStyle(ChatFormatting.DARK_GRAY));
	}
}

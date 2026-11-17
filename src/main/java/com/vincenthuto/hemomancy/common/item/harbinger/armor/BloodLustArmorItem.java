package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import com.vincenthuto.hemomancy.common.item.shared.armor.PlayerLayerHidingArmor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public class BloodLustArmorItem extends ArmorItem implements PlayerLayerHidingArmor {
	public static final String TAG_LINEAGE = "hemomancy:lineage";

	public enum MaskType {
		NONE,
		TENGU,
		GRINNING,
		LODESTONE,
		VELORUM
	}

	MaskType maskType;

	public BloodLustArmorItem(Holder<ArmorMaterial> materialIn, Type slot, MaskType maskType) {
		super(materialIn, slot, new Item.Properties().stacksTo(1));
		this.maskType = maskType;
	}

	public MaskType getMaskType() {
		return maskType;
	}

	public static String getLineage(ItemStack stack) {
		CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		CompoundTag tag = customData.copyTag();
		return tag.getString(TAG_LINEAGE);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.hemomancy.bloodlust_set_bonus").withStyle(ChatFormatting.DARK_RED));
		String lineage = getLineage(stack);
		if (!lineage.isEmpty()) {
			tooltip.add(Component.translatable("tooltip.hemomancy.bloodlust_lineage." + lineage)
					.withStyle(ChatFormatting.GRAY));
		}
	}
}

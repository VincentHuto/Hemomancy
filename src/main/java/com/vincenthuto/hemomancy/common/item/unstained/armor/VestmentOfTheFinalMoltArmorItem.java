package com.vincenthuto.hemomancy.common.item.unstained.armor;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;

import java.util.List;

public class VestmentOfTheFinalMoltArmorItem extends ArmorItem {
	public VestmentOfTheFinalMoltArmorItem(Holder<ArmorMaterial> material, Type slot) {
		super(material, slot, new Item.Properties().fireResistant().stacksTo(1));
	}

	@Override
	public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer,
			boolean innerModel) {
		return armorTexture("vestment_of_the_final_molt_layer_1");
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("tooltip.hemomancy.vestment_of_the_final_molt")
				.withStyle(ChatFormatting.AQUA));
	}

	private static ResourceLocation armorTexture(String name) {
		return Hemomancy.rloc("textures/models/armor/" + name + ".png");
	}
}

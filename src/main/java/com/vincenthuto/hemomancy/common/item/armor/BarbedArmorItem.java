package com.vincenthuto.hemomancy.common.item.armor;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;

import java.util.List;

import com.vincenthuto.hemomancy.client.model.armor.BarbedArmorModel;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class BarbedArmorItem extends ArmorItem implements HemoClientItemExtensionsProvider {

	public BarbedArmorItem(Holder<ArmorMaterial> materialIn, ArmorItem.Type slot) {
		super(materialIn, slot, new Item.Properties().fireResistant());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.hemomancy.barbed_set_bonus").withStyle(ChatFormatting.DARK_RED));
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,

					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.barbed_helm.get()) {
					return BarbedArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.barbed_chestplate.get()) {
					return BarbedArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.barbed_leggings.get()) {
					return BarbedArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.barbed_boots.get()) {
					return BarbedArmorModel.boots.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}
		};
	}
}
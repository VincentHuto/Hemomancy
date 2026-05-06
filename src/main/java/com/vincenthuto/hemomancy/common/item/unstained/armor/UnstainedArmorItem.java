package com.vincenthuto.hemomancy.common.item.unstained.armor;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.model.armor.UnstainedArmorModel;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

public class UnstainedArmorItem extends ArmorItem implements HemoClientItemExtensionsProvider {

	public UnstainedArmorItem(Holder<ArmorMaterial> materialIn, Type slot) {
		super(materialIn, slot, new Item.Properties().fireResistant());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.hemomancy.unstained_set_bonus").withStyle(ChatFormatting.AQUA));
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,

					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.unstained_helm.get()) {
					return UnstainedArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.unstained_chestplate.get()) {
					return UnstainedArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.unstained_leggings.get()) {
					return UnstainedArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.unstained_boots.get()) {
					return UnstainedArmorModel.boots.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}
		};
	}
}
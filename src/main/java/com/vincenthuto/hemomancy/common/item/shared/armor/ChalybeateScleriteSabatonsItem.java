package com.vincenthuto.hemomancy.common.item.shared.armor;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.model.armor.ChalybeateFortressArmorModel;
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
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

public class ChalybeateScleriteSabatonsItem extends ArmorItem implements HemoClientItemExtensionsProvider {
	public ChalybeateScleriteSabatonsItem(Holder<ArmorMaterial> material, Type slot) {
		super(material, slot, new Item.Properties().fireResistant());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context,
			List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("tooltip.hemomancy.chalybeate_sclerite_sabatons")
				.withStyle(ChatFormatting.DARK_GREEN));
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.chalybeate_sclerite_sabatons.get()) {
					return ChalybeateFortressArmorModel.boots.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}
		};
	}
}

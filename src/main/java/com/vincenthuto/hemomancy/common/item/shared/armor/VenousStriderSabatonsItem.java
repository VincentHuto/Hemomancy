package com.vincenthuto.hemomancy.common.item.shared.armor;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.model.armor.ChalybeateFortressArmorModel;
import com.vincenthuto.hemomancy.client.render.item.ModelBackedArmorItemRenderer;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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

public class VenousStriderSabatonsItem extends ArmorItem implements HemoClientItemExtensionsProvider {
	public VenousStriderSabatonsItem(Holder<ArmorMaterial> material, Type slot) {
		super(material, slot, new Item.Properties().fireResistant().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context,
			List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("tooltip.hemomancy.venous_strider_sabatons")
				.withStyle(ChatFormatting.DARK_GREEN));
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ModelBackedArmorItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.venous_strider_sabatons.get()) {
					return ChalybeateFortressArmorModel.boots.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}
}

package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;

import java.util.List;

import com.vincenthuto.hemomancy.client.model.armor.MarrowCrownModel;
import com.vincenthuto.hemomancy.client.render.item.MarrowCrownItemRenderer;
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
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class MarrowCrownArmorItem extends ArmorItem implements HemoClientItemExtensionsProvider {

	public MarrowCrownArmorItem(Holder<ArmorMaterial> materialIn, Type slot) {
		super(materialIn, slot, new Item.Properties().rarity(Rarity.EPIC));
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.hemomancy.marrow_crown_bonus").withStyle(ChatFormatting.GOLD));
	}


	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.marrow_crown.get()) {
					return MarrowCrownModel.helmet.get();
				}

				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new MarrowCrownItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}


}

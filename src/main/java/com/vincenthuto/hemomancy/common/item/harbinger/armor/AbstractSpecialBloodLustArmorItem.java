package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.shared.armor.PlayerLayerHidingArmor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;

import java.util.List;

public abstract class AbstractSpecialBloodLustArmorItem extends ArmorItem implements PlayerLayerHidingArmor {
	private final String texturePrefix;

	protected AbstractSpecialBloodLustArmorItem(Holder<ArmorMaterial> materialIn, Type slot, String texturePrefix) {
		super(materialIn, slot, new Item.Properties().stacksTo(1));
		this.texturePrefix = texturePrefix;
	}

	/** The armour texture prefix, which also picks the client model. */
	public String texturePrefix() {
		return texturePrefix;
	}

	@Override
	public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer,
			boolean innerModel) {
		return Hemomancy.rloc("textures/models/armor/" + this.texturePrefix + "_layer_"
				+ (slot == EquipmentSlot.LEGS ? "2" : "1") + ".png");
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.hemomancy.bloodlust_set_bonus").withStyle(ChatFormatting.DARK_RED));
	}
}

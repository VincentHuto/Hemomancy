package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.item.SpecialBloodLustClientExtensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

public abstract class AbstractSpecialBloodLustArmorItem extends ArmorItem implements HemoClientItemExtensionsProvider {
	private final String texturePrefix;

	protected AbstractSpecialBloodLustArmorItem(Holder<ArmorMaterial> materialIn, Type slot, String texturePrefix) {
		super(materialIn, slot, new Item.Properties().stacksTo(1));
		this.texturePrefix = texturePrefix;
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

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return SpecialBloodLustClientExtensions.create(this.texturePrefix);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotId, boolean isSelected) {
		super.inventoryTick(stack, world, entity, slotId, isSelected);
		if (world.isClientSide && entity instanceof Player player && isEquipped(player, stack)) {
			world.addParticle(DustParticleOptions.REDSTONE, player.getRandomX(0.5D), player.getY(),
					player.getRandomZ(0.5D), (world.random.nextDouble() - 0.5D) * 2.0D,
					-world.random.nextDouble(), (world.random.nextDouble() - 0.5D) * 2.0D);
		}
	}

	private static boolean isEquipped(Player player, ItemStack stack) {
		for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
				EquipmentSlot.FEET }) {
			if (ItemStack.isSameItemSameComponents(player.getItemBySlot(slot), stack)) {
				return true;
			}
		}
		return false;
	}
}

package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.model.armor.BloodLustArmorModel;
import com.vincenthuto.hemomancy.client.render.item.ModelBackedArmorItemRenderer;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

public class BloodLustArmorItem extends ArmorItem implements HemoClientItemExtensionsProvider {
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

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ModelBackedArmorItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				BloodLustArmorModel<?> model;
				if (itemStack.getItem() == ItemInit.blood_lust_helm.get()) {
					model = BloodLustArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_grinning.get()) {
					model = BloodLustArmorModel.grinning.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_tengu.get()) {
					model = BloodLustArmorModel.tengu.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_lodestone.get()) {
					model = BloodLustArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_velorum.get()) {
					model = BloodLustArmorModel.tengu.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_chest.get()) {
					model = BloodLustArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_legs.get()) {
					model = BloodLustArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_boots.get()) {
					model = BloodLustArmorModel.boots.get();
				} else {
					return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
				}
				model.setLineage(getLineage(itemStack));
				return model;
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
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

	public void setMaskType(MaskType maskType) {
		this.maskType = maskType;
	}

}

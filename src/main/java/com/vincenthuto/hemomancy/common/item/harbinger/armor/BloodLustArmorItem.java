package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.model.armor.BloodLustArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.EdaciousBloodLustArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.PhantasmalBloodLustArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.SheolicBloodLustArmorModel;
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
import net.minecraft.resources.ResourceLocation;
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
		super(materialIn, slot, new Item.Properties());
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
	public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer,
			boolean innerModel) {
		if (isEdaciousBloodLust(stack)) {
			return armorTexture("edacious_blood_lust_layer_" + (slot == EquipmentSlot.LEGS ? "2" : "1"));
		}
		if (isSheolicBloodLust(stack)) {
			return armorTexture("sheolic_blood_lust_layer_" + (slot == EquipmentSlot.LEGS ? "2" : "1"));
		}
		if (isPhantasmalBloodLust(stack)) {
			return armorTexture("phantasmal_blood_lust_layer_" + (slot == EquipmentSlot.LEGS ? "2" : "1"));
		}
		return super.getArmorTexture(stack, entity, slot, layer, innerModel);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.hemomancy.bloodlust_set_bonus").withStyle(ChatFormatting.DARK_RED));
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ModelBackedArmorItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.edacious_blood_lust_helm.get()) {
					return EdaciousBloodLustArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.edacious_blood_lust_chest.get()) {
					return EdaciousBloodLustArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.edacious_blood_lust_legs.get()) {
					return EdaciousBloodLustArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.edacious_blood_lust_boots.get()) {
					return EdaciousBloodLustArmorModel.boots.get();
				} else if (itemStack.getItem() == ItemInit.sheolic_blood_lust_helm.get()) {
					return SheolicBloodLustArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.sheolic_blood_lust_chest.get()) {
					return SheolicBloodLustArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.sheolic_blood_lust_legs.get()) {
					return SheolicBloodLustArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.sheolic_blood_lust_boots.get()) {
					return SheolicBloodLustArmorModel.boots.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm.get()) {
					return BloodLustArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_grinning.get()) {
					return BloodLustArmorModel.grinning.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_tengu.get()) {
					return BloodLustArmorModel.tengu.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_lodestone.get()) {
					return BloodLustArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_velorum.get()) {
					return BloodLustArmorModel.tengu.get();
				} else if (itemStack.getItem() == ItemInit.phantasmal_blood_lust_helm.get()) {
					return PhantasmalBloodLustArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_chest.get()) {
					return BloodLustArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.phantasmal_blood_lust_chest.get()) {
					return PhantasmalBloodLustArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_legs.get()) {
					return BloodLustArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.phantasmal_blood_lust_legs.get()) {
					return PhantasmalBloodLustArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_boots.get()) {
					return BloodLustArmorModel.boots.get();
				} else if (itemStack.getItem() == ItemInit.phantasmal_blood_lust_boots.get()) {
					return PhantasmalBloodLustArmorModel.boots.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
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

	private static boolean isEdaciousBloodLust(ItemStack stack) {
		return stack.is(ItemInit.edacious_blood_lust_helm.get()) || stack.is(ItemInit.edacious_blood_lust_chest.get())
				|| stack.is(ItemInit.edacious_blood_lust_legs.get())
				|| stack.is(ItemInit.edacious_blood_lust_boots.get());
	}

	private static boolean isSheolicBloodLust(ItemStack stack) {
		return stack.is(ItemInit.sheolic_blood_lust_helm.get()) || stack.is(ItemInit.sheolic_blood_lust_chest.get())
				|| stack.is(ItemInit.sheolic_blood_lust_legs.get()) || stack.is(ItemInit.sheolic_blood_lust_boots.get());
	}

	private static boolean isPhantasmalBloodLust(ItemStack stack) {
		return stack.is(ItemInit.phantasmal_blood_lust_helm.get())
				|| stack.is(ItemInit.phantasmal_blood_lust_chest.get())
				|| stack.is(ItemInit.phantasmal_blood_lust_legs.get())
				|| stack.is(ItemInit.phantasmal_blood_lust_boots.get());
	}

	private static ResourceLocation armorTexture(String name) {
		return Hemomancy.rloc("textures/models/armor/" + name + ".png");
	}

}

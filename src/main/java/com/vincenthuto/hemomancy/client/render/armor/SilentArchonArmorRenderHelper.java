package com.vincenthuto.hemomancy.client.render.armor;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.SilentArchonArmorModel;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class SilentArchonArmorRenderHelper {
	public static final ResourceLocation LAYER_1 = Hemomancy.rloc("textures/models/armor/silent_archon_layer_1.png");
	public static final ResourceLocation LAYER_2 = Hemomancy.rloc("textures/models/armor/silent_archon_layer_2.png");
	public static final int OVERLAY_COLOR = 0x66FFFFFF;
	public static  float OVERLAY_SCALE = 1.1F;
	private static final float TIME_CYCLE_MS = 240000.0f;

	private SilentArchonArmorRenderHelper() {
	}

	public static boolean isSilentArchon(ItemStack stack) {
		return slotFor(stack) != null;
	}

	public static EquipmentSlot slotFor(ItemStack stack) {
		Item item = stack.getItem();
		if (item == ItemInit.silent_archon_helm.get()) {
			return EquipmentSlot.HEAD;
		} else if (item == ItemInit.silent_archon_chestplate.get()) {
			return EquipmentSlot.CHEST;
		} else if (item == ItemInit.silent_archon_leggings.get()) {
			return EquipmentSlot.LEGS;
		} else if (item == ItemInit.silent_archon_boots.get()) {
			return EquipmentSlot.FEET;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends LivingEntity> SilentArchonArmorModel<T> modelForSlot(EquipmentSlot slot) {
		SilentArchonArmorModel<? extends LivingEntity> model = switch (slot) {
			case HEAD -> SilentArchonArmorModel.helmet.get();
			case CHEST -> SilentArchonArmorModel.chest.get();
			case LEGS -> SilentArchonArmorModel.legs.get();
			case FEET -> SilentArchonArmorModel.boots.get();
			default -> SilentArchonArmorModel.chest.get();
		};
		return (SilentArchonArmorModel<T>) model;
	}

	public static ResourceLocation textureForSlot(EquipmentSlot slot) {
		return slot == EquipmentSlot.LEGS ? LAYER_2 : LAYER_1;
	}

	public static RenderType overlayRenderType(ItemStack stack, EquipmentSlot slot, boolean guiClamp) {
		float seed = Math.floorMod(System.identityHashCode(stack) + slot.ordinal() * 997, 4096) / 4096.0f;
		return HemoRenderTypes.silentArchonArmorOverlay(timeSeconds(), seed, 0.25f, 1.0f,
				guiClamp ? 1.0f : 0.0f);
	}

	public static float timeSeconds() {
		return (System.currentTimeMillis() % (long) TIME_CYCLE_MS) / 1000.0f;
	}
}

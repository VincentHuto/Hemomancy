package com.vincenthuto.hemomancy.client.render.armor;

import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class SilentArchonPlayerRenderHelper {
	private static final float CORE_BURDEN = 0.62F;
	private static final float CORE_ATTUNED = 1.0F;
	private static final float CORE_FRACTAL_SCALE = 8.75F;

	private SilentArchonPlayerRenderHelper() {
	}

	public static boolean hasFullSilentArchonSet(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.silent_archon_helm.get())
				&& entity.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.silent_archon_chestplate.get())
				&& entity.getItemBySlot(EquipmentSlot.LEGS).is(ItemInit.silent_archon_leggings.get())
				&& entity.getItemBySlot(EquipmentSlot.FEET).is(ItemInit.silent_archon_boots.get());
	}

	public static RenderType playerCoreRenderType(AbstractClientPlayer player) {
		float seed = Math.floorMod(player.getUUID().hashCode(), 10007) / 10007.0F;
		return HemoRenderTypes.monolithEntitySurface(
				SilentArchonArmorRenderHelper.timeSeconds(), seed, CORE_BURDEN, CORE_ATTUNED, CORE_FRACTAL_SCALE);
	}
}

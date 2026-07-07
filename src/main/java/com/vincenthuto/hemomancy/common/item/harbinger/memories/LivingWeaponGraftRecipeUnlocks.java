package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingArsenalInventoryGuard;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class LivingWeaponGraftRecipeUnlocks {
	private LivingWeaponGraftRecipeUnlocks() {
	}

	public static boolean hasEarnedRecipeUnlock(ServerPlayer player, LivingWeaponForm form) {
		return player != null && form != null
				&& HarbingerAdvancementGranter.hasAdvancement(player, advancementId(form));
	}

	public static boolean awardRecipeUnlock(ServerPlayer player, LivingWeaponForm form) {
		if (player == null || form == null || !hasLivingStaffAccess(player)) {
			return false;
		}
		HarbingerAdvancementGranter.grantIfNotDone(player, advancementId(form));
		return true;
	}

	public static boolean hasLivingStaffAccess(ServerPlayer player) {
		if (player == null) {
			return false;
		}
		if (HemoCapabilityAccess.getKnownManipulations(player).map(known -> {
			for (BloodManipulation manipulation : known.getKnownManips().keySet()) {
				if (manipulation != null && "conjure_staff".equals(manipulation.getName())) {
					return true;
				}
			}
			return false;
		}).orElse(false)) {
			return true;
		}
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (LivingArsenalInventoryGuard.isLivingArsenalItem(stack)) {
				return true;
			}
		}
		return LivingArsenalInventoryGuard.isLivingArsenalItem(player.getMainHandItem())
				|| LivingArsenalInventoryGuard.isLivingArsenalItem(player.getOffhandItem());
	}

	private static ResourceLocation advancementId(LivingWeaponForm form) {
		return switch (form) {
		case BLADE -> Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/blade");
		case AXE -> Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/axe");
		case SPEAR -> Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/spear");
		case CLAWS -> Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/claws");
		case CROSSBOW -> Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/crossbow");
		case TORCH -> Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/torch");
		case FLAIL -> Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/flail");
		};
	}
}

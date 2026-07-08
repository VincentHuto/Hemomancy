package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class LivingStaffFittingHelper {
	public static final int WORN_VOW_VISUAL = 9;
	public static final int BARBED_VISUAL = 10;
	public static final int CHITINITE_VISUAL = 11;
	public static final int PRISMATIC_VISUAL = 12;
	public static final int CRIMSON_VESTMENT_VISUAL = 13;
	public static final int MONOLITHIC_FRAME_VISUAL = 14;
	public static final int ASSUMED_LIMB_VISUAL = 15;

	private LivingStaffFittingHelper() {
	}

	public static int staffVisualFor(Player player) {
		if (player == null) {
			return 0;
		}
		return HemoCapabilityAccess.getEquipment(player)
				.map(equipment -> {
					ItemStack stack = equipment.getStackInSlot(HarbingerEquipmentMenu.FITTING_SLOT_INDEX);
					if (stack.getItem() instanceof LivingStaffFittingItem fitting) {
						return fitting.getStaffVisualId();
					}
					return 0;
				})
				.orElse(0);
	}
}

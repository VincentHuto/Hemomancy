package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipmentItemHandler;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmRules;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class VasculariumCharmSlot extends SelectiveScarTypeSlot {
	private final boolean openedFromScarletVanity;

	public VasculariumCharmSlot(Player player, IHarbingerEquipmentItemHandler itemHandler, int slot, int x, int y,
                                boolean openedFromScarletVanity) {
		super(player, VasculariumCharmItem.class, itemHandler, slot, x, y);
		this.openedFromScarletVanity = openedFromScarletVanity;
	}

	@Override
	public boolean mayPickup(Player player) {
		ItemStack stack = getItem();
		return !stack.isEmpty()
				&& VasculariumCharmRules.canRemoveFromEquipmentMenu(openedFromScarletVanity);
	}
}

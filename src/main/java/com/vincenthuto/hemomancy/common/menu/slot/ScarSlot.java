package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipment;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipmentItemHandler;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.ItemFungalScar;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ScarSlot extends SlotItemHandler {
	int ScarSlot;
	Player player;

	public ScarSlot(Player player, IHarbingerEquipmentItemHandler itemHandler, int slot, int par4, int par5) {
		super(itemHandler, slot, par4, par5);
		this.ScarSlot = slot;
		this.player = player;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		if (stack.getItem() instanceof IHarbingerEquipment && !(stack.getItem() instanceof ItemFungalScar)
				&& !(stack.getItem() instanceof VasculariumCharmItem)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mayPickup(Player player) {
		ItemStack stack = getItem();
		if (stack.isEmpty())
			return false;

		IHarbingerEquipment mindscar = HemoCapabilityAccess.getScar(stack).orElseThrow(NullPointerException::new);
		return mindscar.canUnequip(player);
	}

	@Override
	public void onTake(Player playerIn, ItemStack stack) {
		super.onTake(playerIn, stack);
	}

	@Override
	public void set(ItemStack stack) {
		super.set(stack);
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}
}
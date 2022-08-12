package com.vincenthuto.hemomancy.containers.slot;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.charm.ICharmSlot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CharmSlot extends Slot {
	public static final ResourceLocation SLOT_BACKGROUND = new ResourceLocation(Hemomancy.MOD_ID,
			"gui/empty_charm_slot_background");

	private static Container emptyInventory = new SimpleContainer(0);
	private final ICharmSlot slot;

	public CharmSlot(ICharmSlot slot, int x, int y) {
		super(emptyInventory, 0, x, y);
		this.slot = slot;
		setBackground(InventoryMenu.BLOCK_ATLAS, SLOT_BACKGROUND);
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		if (stack.isEmpty())
			return false;
		return slot.canEquip(stack);
	}

	@Override
	@Nonnull
	public ItemStack getItem() {
		return slot.getContents();
	}

	@Override
	public void set(@Nonnull ItemStack stack) {
		slot.setContents(stack);
		this.setChanged();
	}

	@Override
	public void onQuickCraft(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn) {

	}

	@Override
	public int getMaxStackSize() {	
		return 1;
	}

	@Override
	public int getMaxStackSize(@Nonnull ItemStack stack) {
		return 1;
	}

	@Override
	public boolean mayPickup(Player playerIn) {
		return slot.canUnequip(slot.getContents());
	}

	@Override
	@Nonnull
	public ItemStack remove(int amount) {
		ItemStack itemstack = slot.getContents();

		int available = Math.min(itemstack.getCount(), amount);
		int remaining = itemstack.getCount() - available;

		ItemStack split = itemstack.copy();
		split.setCount(available);
		itemstack.setCount(remaining);

		if (remaining <= 0)
			slot.setContents(ItemStack.EMPTY);

		this.setChanged();

		return split;
	}

	public ICharmSlot getExtensionSlot() {
		return slot;
	}

	@Override
	public boolean isSameInventory(Slot other) {
		return other instanceof CharmSlot && ((CharmSlot) other).getExtensionSlot() == this.slot;
	}
}

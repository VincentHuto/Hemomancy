package com.vincenthuto.hemomancy.capa.player.charm;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.container.ICharmMenu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class CharmSlotItemHandler implements ICharmSlot {
	protected final ICharmMenu owner;
	protected final ResourceLocation slotType;
	protected final int slot;
	protected final IItemHandlerModifiable inventory;

	public CharmSlotItemHandler(ICharmMenu owner, ResourceLocation slotType, IItemHandlerModifiable inventory,
			int slot) {
		this.owner = owner;
		this.slotType = slotType;
		this.slot = slot;
		this.inventory = inventory;
	}

	@Nonnull
	@Override
	public ICharmMenu getContainer() {
		return owner;
	}

	@Nonnull
	@Override
	public ResourceLocation getType() {
		return slotType;
	}

	@Nonnull
	@Override
	public ItemStack getContents() {
		return inventory.getStackInSlot(slot);
	}

	@Override
	public void setContents(@Nonnull ItemStack stack) {
		ItemStack oldStack = getContents();
		if (oldStack == stack)
			return;
		if (!oldStack.isEmpty())
			notifyUnequip(oldStack);
		inventory.setStackInSlot(slot, stack);
		if (!stack.isEmpty())
			notifyEquip(stack);
	}

	@Override
	public void onContentsChanged() {
		owner.onContentsChanged(this);
	}

	private void notifyEquip(ItemStack stack) {
		stack.getCapability(CharmSlotCapability.INSTANCE, null).ifPresent((extItem) -> {
			extItem.onEquipped(stack, this);
		});
	}

	private void notifyUnequip(ItemStack stack) {
		stack.getCapability(CharmSlotCapability.INSTANCE, null).ifPresent((extItem) -> {
			extItem.onUnequipped(stack, this);
		});
	}

	public void onWornTick() {
		ItemStack stack = getContents();
		if (stack.isEmpty())
			return;
		stack.getCapability(CharmSlotCapability.INSTANCE, null).ifPresent((extItem) -> {
			extItem.onWornTick(stack, this);
		});
	}

}

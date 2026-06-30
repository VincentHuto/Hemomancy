package com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityKeys;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HarbingerEquipmentContainer extends ItemStackHandler implements IHarbingerEquipmentItemHandler, INBTSerializable<CompoundTag> {

	private final static int SCAR_SLOTS = 8;
	private final ItemStack[] eventPrevious = new ItemStack[SCAR_SLOTS];
	private final ItemStack[] syncPrevious = new ItemStack[SCAR_SLOTS];
	private final boolean[] renderLayerVisible = new boolean[SCAR_SLOTS];
	private boolean[] changed = new boolean[SCAR_SLOTS];
	private boolean blockEvents = false;
	private boolean EquipmentUnlocked = false;
	private LivingEntity holder;

	public HarbingerEquipmentContainer() {
		super(SCAR_SLOTS);
		this.holder = null;
		Arrays.fill(eventPrevious, ItemStack.EMPTY);
		Arrays.fill(syncPrevious, ItemStack.EMPTY);
		Arrays.fill(renderLayerVisible, true);
	}

	public HarbingerEquipmentContainer(LivingEntity player) {
		super(SCAR_SLOTS);
		this.holder = player;
		Arrays.fill(eventPrevious, ItemStack.EMPTY);
		Arrays.fill(syncPrevious, ItemStack.EMPTY);
		Arrays.fill(renderLayerVisible, true);
	}

	public LivingEntity getHolder() {
		return this.holder;
	}

	public void bindHolder(LivingEntity holder) {
		if (this.holder == holder) {
			return;
		}

		boolean firstBind = this.holder == null && holder != null;
		this.holder = holder;

		if (firstBind && !blockEvents) {
			for (int i = 0; i < getSlots(); i++) {
				ItemStack stack = getStackInSlot(i);
				if (!stack.isEmpty()) {
					IHarbingerEquipment scar = stack.getCapability(HemoCapabilityKeys.ITEM_HARBINGER_EQUIPMENT);
					if (scar != null) {
						scar.onEquipped(holder);
					}
				}
				eventPrevious[i] = stack.copy();
			}
		}
	}

	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (!this.isItemValidForSlot(slot, stack))
			return stack;
		return super.insertItem(slot, stack, simulate);
	}

	@Override
	public boolean isEventBlocked() {
		return blockEvents;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		IHarbingerEquipment mindscar = stack.getCapability(HemoCapabilityKeys.ITEM_HARBINGER_EQUIPMENT);
		if (stack.isEmpty() || mindscar == null)
			return false;
		if (holder == null)
			return mindscar.getHarbingerEquipmentType().hasSlot(slot);
		return mindscar.canEquip(holder) && mindscar.getHarbingerEquipmentType().hasSlot(slot);
	}

	@Override
	public boolean isEquipmentUnlocked() {
		return EquipmentUnlocked;
	}

	@Override
	public boolean isRenderLayerVisible(int slot) {
		if (slot < 0 || slot >= renderLayerVisible.length) return true;
		return renderLayerVisible[slot];
	}

	@Override
	public void setEquipmentUnlocked(boolean unlocked) {
		this.EquipmentUnlocked = unlocked;
	}

	@Override
	public void setRenderLayerVisible(int slot, boolean visible) {
		if (slot < 0 || slot >= renderLayerVisible.length) {
			return;
		}
		renderLayerVisible[slot] = visible;
	}

	@Override
	protected void onContentsChanged(int slot) {
		// Mark for network sync
		this.changed[slot] = true;

		// Make equip/unequip airtight: drive side effects from the capability container,
		// not from UI slots, so shift-click/drag/hotkey swaps can't bypass cleanup.
		ItemStack prev = eventPrevious[slot];
		ItemStack now = getStackInSlot(slot);

		if (!blockEvents && holder != null && !ItemStack.isSameItemSameComponents(prev, now)) {
			if (!prev.isEmpty()) {
				IHarbingerEquipment prevScar = prev.getCapability(HemoCapabilityKeys.ITEM_HARBINGER_EQUIPMENT);
				if (prevScar != null) prevScar.onUnequipped(holder);
			}
			if (!now.isEmpty()) {
				IHarbingerEquipment nowScar = now.getCapability(HemoCapabilityKeys.ITEM_HARBINGER_EQUIPMENT);
				if (nowScar != null) nowScar.onEquipped(holder);
			}
		}

		eventPrevious[slot] = now.copy();
	}

	@Override
	public void setEventBlock(boolean blockEvents) {
		this.blockEvents = blockEvents;
	}

	@Override
	public void setSize(int size) {
		if (size != SCAR_SLOTS)
			System.out.println("Cannot resize equipment container");
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = super.serializeNBT(provider);
		nbt.putBoolean("EquipmentUnlocked", EquipmentUnlocked);
		CompoundTag visibilityTag = new CompoundTag();
		for (int i = 0; i < renderLayerVisible.length; i++) {
			visibilityTag.putBoolean(Integer.toString(i), renderLayerVisible[i]);
		}
		nbt.put("RenderLayerVisibility", visibilityTag);
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		super.deserializeNBT(provider, nbt);
		this.EquipmentUnlocked = nbt.getBoolean("EquipmentUnlocked");
		Arrays.fill(renderLayerVisible, true);
		if (nbt.contains("RenderLayerVisibility")) {
			CompoundTag visibilityTag = nbt.getCompound("RenderLayerVisibility");
			for (int i = 0; i < renderLayerVisible.length; i++) {
				if (visibilityTag.contains(Integer.toString(i))) {
					renderLayerVisible[i] = visibilityTag.getBoolean(Integer.toString(i));
				}
			}
		}
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {

		super.setStackInSlot(slot, stack);
	}

	private void sync() {
		if (!(holder instanceof ServerPlayer)) {
			return;
		}

		List<Player> receivers = null;
		for (byte i = 0; i < getSlots(); i++) {
			ItemStack stack = getStackInSlot(i);
			IHarbingerEquipment equipment = stack.getCapability(HemoCapabilityKeys.ITEM_HARBINGER_EQUIPMENT);
			boolean autosync = equipment != null && equipment.willAutoSync(holder);
			if (changed[i] || autosync && !ItemStack.isSameItemSameComponents(stack, syncPrevious[i])) {
				if (receivers == null) {
					receivers = new ArrayList<>(((ServerLevel) holder.level()).players());
					receivers.add((ServerPlayer) holder);
				}
				HarbingerEquipmentEntityEventHandler.syncSlot((ServerPlayer) holder, i, stack, receivers);
				this.changed[i] = false;
				syncPrevious[i] = stack.copy();
			}
		}
	}

	@Override
	public void tick() {
		if (holder == null) {
			return;
		}
		for (int i = 0; i < getSlots(); i++) {
			ItemStack stack = getStackInSlot(i);
			if (!stack.isEmpty() && stack.getItem() != Items.AIR) {
				IHarbingerEquipment scar = stack.getCapability(HemoCapabilityKeys.ITEM_HARBINGER_EQUIPMENT);
				if (scar != null) scar.onWornTick(holder);
			}
		}
		sync();
	}
}

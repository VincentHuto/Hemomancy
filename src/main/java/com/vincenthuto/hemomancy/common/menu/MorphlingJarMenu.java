package com.vincenthuto.hemomancy.common.menu;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingIdentity;
import com.vincenthuto.hemomancy.common.item.itemhandler.MorphlingJarItemHandler;
import com.vincenthuto.hemomancy.common.menu.slot.MorphlingJarSlot;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;

public class MorphlingJarMenu extends AbstractContainerMenu {

	public static final int SCREEN_WIDTH = 232;
	public static final int SCREEN_HEIGHT = 214;
	public static final int JAR_LEFT_SLOT_X = 26;
	public static final int JAR_RIGHT_SLOT_X = 190;
	public static final int JAR_SLOT_FIRST_Y = 26;
	public static final int JAR_SLOT_SPACING = 24;
	public static final int PLAYER_INV_X = 35;
	public static final int PLAYER_INV_Y = 124;

	public int slotcount = 0;
	/** Inventory slot index, or -106 for offhand, or -200 for scar-equip slot. */
	private int slotID;
	public String itemKey = "";
	private Inventory playerInv;
	public MorphlingJarItemHandler handler;

	// ─── Constructors ────────────────────────────────────────────────────────────

	public MorphlingJarMenu(int openType, int windowId, Level world, BlockPos pos, Inventory playerInventory,
			Player playerEntity) {
		this(windowId, world, pos, playerInventory, playerEntity);
	}

	public MorphlingJarMenu(final int windowId, final Inventory playerInventory) {
		this(windowId, playerInventory.player.level(), playerInventory.player.blockPosition(), playerInventory,
				playerInventory.player);
	}

	public MorphlingJarMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory);
	}

	public MorphlingJarMenu(int windowId, Level world, BlockPos pos, Inventory playerInventory,
			Player playerEntity) {
		super(ContainerInit.morphling_jar.get(), windowId);

		playerInv = playerInventory;
		ItemStack stack = findMorphlingJar(playerEntity);

		if (stack == null || stack.isEmpty()) {
			playerEntity.closeContainer();
			return;
		}

		if (stack.getCapability(Capabilities.ItemHandler.ITEM) instanceof MorphlingJarItemHandler mjh) {
			handler = mjh;
			handler.load();
			slotcount = handler.getSlots();
			itemKey = stack.getDescriptionId();
			addMySlots();
			addPlayerSlots(playerInv);
		} else {
			playerEntity.closeContainer();
		}
	}

	// ─── Slot layout ─────────────────────────────────────────────────────────────

	/**
	 * Places morphling slots in a 3-wide grid, horizontally centred in the jar GUI.
	 * Slot origin (61, 32) keeps them inside the jar belly graphic.
	 */
	private void addMySlots() {
		if (handler == null)
			return;

		// 3 columns – jars hold 6 morphlings (3×2)
		int slotIndex = 0;

		while (slotIndex < slotcount) {
			int row = slotIndex % 3;
			int column = slotIndex / 3;
			int x = column == 0 ? JAR_LEFT_SLOT_X : JAR_RIGHT_SLOT_X;
			int y = JAR_SLOT_FIRST_Y + row * JAR_SLOT_SPACING;
			this.addSlot(new MorphlingJarSlot(handler, slotIndex, x, y));
			slotIndex++;
		}
	}

	/**
	 * Player inventory / hotbar sit below the jar slots at a fixed origin that
	 * works for the single 4-slot jar size.
	 */
	private void addPlayerSlots(Inventory playerInventory) {
		// Fits a standard 176×166 GUI: inventory at y=84, hotbar at y=142
		final int originX = PLAYER_INV_X;
		final int originY = PLAYER_INV_Y;

		// Main inventory (3 rows × 9 cols)
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, originX + col * 18, originY + row * 18));
			}
		}
		// Hotbar
		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(playerInventory, col, originX + col * 18, originY + 58));
		}
	}

	// ─── Menu behaviour ──────────────────────────────────────────────────────────

	@Override
	public void clicked(int slot, int dragType, ClickType clickTypeIn, Player player) {
		super.clicked(slot, dragType, clickTypeIn, player);
		if (slot >= 0 && clickTypeIn != ClickType.SWAP
				&& !(getSlot(slot).getItem().getItem() instanceof ItemMorphlingJar)) {
			getSlot(slot).container.setChanged();
		}
		clearEquippedMorphlingIfNoLongerInJar(player);
		if (handler != null)
			handler.save();
	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
		if (handler != null)
			handler.save();
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			result = stack.copy();
			if (index < slotcount) {
				// Jar → player inventory
				if (!this.moveItemStackTo(stack, slotcount, this.slots.size(), true))
					return ItemStack.EMPTY;
			} else {
				// Player inventory → jar
				if (!this.moveItemStackTo(stack, 0, slotcount, false))
					return ItemStack.EMPTY;
			}
			if (stack.isEmpty())
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
			clearEquippedMorphlingIfNoLongerInJar(playerIn);
			if (handler != null)
				handler.save();
		}
		return result;
	}

	@Override
	public boolean stillValid(Player playerIn) {
		// Equipped in scar slot
		if (slotID == -200) {
			return HemoCapabilityAccess.getEquipment(playerIn)
					.map(r -> r.getStackInSlot(7).getItem() instanceof ItemMorphlingJar)
					.orElse(false);
		}
		if (slotID == -106)
			return playerIn.getOffhandItem().getItem() instanceof ItemMorphlingJar;
		ItemStack s = playerIn.getInventory().getItem(slotID);
		return !s.isEmpty() && s.getItem() instanceof ItemMorphlingJar;
	}

	// ─── Accessors ───────────────────────────────────────────────────────────────

	/** Returns the number of occupied morphling slots. */
	public int getFilledSlotCount() {
		if (handler == null) return 0;
		int count = 0;
		for (int i = 0; i < slotcount; i++) {
			if (!handler.getStackInSlot(i).isEmpty()) count++;
		}
		return count;
	}

	/** Returns 0.0–1.0 representing how full the jar is. */
	public float getFillRatio() {
		return slotcount == 0 ? 0f : (float) getFilledSlotCount() / slotcount;
	}

	// ─── Helpers ─────────────────────────────────────────────────────────────────

	private void clearEquippedMorphlingIfNoLongerInJar(Player player) {
		if (handler == null || player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer))
			return;

		HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
			ItemStack equipped = cap.getEquippedMorphling();
			if (equipped.isEmpty())
				return;

			for (int i = 0; i < slotcount; i++) {
				if (MorphlingIdentity.matches(equipped, handler.getStackInSlot(i))) {
					return;
				}
			}

			cap.clearMorphling();
			LastRiteHelper.clearMorphlingRites(player);
			EquippedMorphlingEvents.syncToClient(serverPlayer);
		});
	}

	private ItemStack findMorphlingJar(Player playerEntity) {
		// 1. Main hand
		if (playerEntity.getMainHandItem().getItem() instanceof ItemMorphlingJar) {
			Inventory inv = playerEntity.getInventory();
			for (int i = 0; i <= 35; i++) {
				if (inv.getItem(i) == playerEntity.getMainHandItem()) {
					slotID = i;
					return inv.getItem(i);
				}
			}
		}
		// 2. Offhand
		if (playerEntity.getOffhandItem().getItem() instanceof ItemMorphlingJar) {
			slotID = -106;
			return playerEntity.getOffhandItem();
		}
		// 3. scar-equip slot (slot 7 in the SCARS capability)
		ItemStack EquipmentSlotJar = HemoCapabilityAccess.getEquipment(playerEntity)
				.map(r -> r.getStackInSlot(7)).orElse(ItemStack.EMPTY);
		if (EquipmentSlotJar.getItem() instanceof ItemMorphlingJar) {
			slotID = -200;
			return EquipmentSlotJar;
		}
		// 4. Anywhere in inventory
		Inventory inv = playerEntity.getInventory();
		for (int i = 0; i <= 35; i++) {
			ItemStack s = inv.getItem(i);
			if (s.getItem() instanceof ItemMorphlingJar) {
				slotID = i;
				return s;
			}
		}
		return ItemStack.EMPTY;
	}
}


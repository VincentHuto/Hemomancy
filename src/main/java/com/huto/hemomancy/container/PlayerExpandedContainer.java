package com.huto.hemomancy.container;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.huto.hemomancy.capa.rune.IRunesItemHandler;
import com.huto.hemomancy.capa.rune.RunesCapabilities;
import com.huto.hemomancy.containers.slot.SlotRune;
import com.huto.hemomancy.containers.slot.SlotRuneArmor;
import com.huto.hemomancy.containers.slot.SlotRuneOffHand;
import com.huto.hemomancy.containers.slot.SlotSelectiveRuneType;
import com.huto.hemomancy.init.ContainerInit;
import com.huto.hemomancy.item.armor.ItemArmBanner;
import com.huto.hemomancy.item.rune.ItemContractRune;
import com.huto.hemomancy.item.rune.ItemVasculariumCharm;

import net.minecraft.entity.MobEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class PlayerExpandedContainer extends AbstractContainerMenu {

	public static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[] {
			InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
			InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET };
	private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EquipmentSlot[] { EquipmentSlot.HEAD,
			EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
	private final CraftingContainer craftMatrix = new CraftingContainer(this, 2, 2);
	private final ResultContainer craftResult = new ResultContainer();
	public final boolean isLocalWorld;
	private final Player player;

	public IRunesItemHandler runes;

	public PlayerExpandedContainer(int id, Inventory playerInventory, boolean localWorld) {
		super(ContainerInit.playerrunes, id);
		this.isLocalWorld = localWorld;
		this.player = playerInventory.player;

		this.runes = this.player.getCapability(RunesCapabilities.RUNES).orElseThrow(NullPointerException::new);

		this.addSlot(
				new ResultSlot(playerInventory.player, this.craftMatrix, this.craftResult, 0, 154, 28 + 26));

		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < 2; ++j) {
				this.addSlot(new Slot(this.craftMatrix, j + i * 2, 116 + j * 18 - 18, 18 + i * 18 + 26));
			}
		}

		for (int k = 0; k < 4; ++k) {
			final EquipmentSlot equipmentslottype = VALID_EQUIPMENT_SLOTS[k];
			this.addSlot(
					new SlotRuneArmor(playerInventory, 36 + (3 - k), 8, 8 + k * 18, equipmentslottype, this.player));
		}
		this.addSlot(new SlotSelectiveRuneType(player, ItemContractRune.class, runes, 0, 77, 8));
		this.addSlot(new SlotRune(player, runes, 1, 77 + 1 * 18, 8));
		this.addSlot(new SlotRune(player, runes, 2, 77 + 2 * 18, 8));
		this.addSlot(new SlotRune(player, runes, 3, 77 + 3 * 18, 8));
		this.addSlot(new SlotSelectiveRuneType(player, ItemVasculariumCharm.class, runes, 4, 77, 26));
		this.addSlot(new SlotSelectiveRuneType(player, ItemArmBanner.class, runes, 5, 77, 44));

		for (int l = 0; l < 3; ++l) {
			for (int j1 = 0; j1 < 9; ++j1) {
				this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
		}

		this.addSlot(new SlotRuneOffHand(playerInventory, 40, 96 - 19, 62));
	}

	@Override
	public void onCraftMatrixChanged(Container par1IInventory) {
		try {

			Method onCraftChange = ObfuscationReflectionHelper.findMethod(CraftingMenu.class, "func_217066_a",
					int.class, Level.class, Player.class, CraftingContainer.class, ResultContainer.class);
			onCraftChange.invoke(null, this.windowId, this.player.world, this.player, this.craftMatrix,
					this.craftResult);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onContainerClosed(PlayerEntity player) {
		super.onContainerClosed(player);
		this.craftResult.clear();

		if (!player.level.isClientSide) {
			this.clearContainer(player, player.world, this.craftMatrix);
		}
	}

	@Override
	public boolean canInteractWith(PlayerEntity par1PlayerEntity) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			EquipmentSlotType entityequipmentslot = MobEntity.getSlotForItemStack(itemstack);

			int slotShift = runes.getSlots();

			if (index == 0) {
				if (!this.mergeItemStack(itemstack1, 9 + slotShift - 3, 45 + slotShift - 3, true)) {

					return ItemStack.EMPTY;
				}

				slot.onSlotChange(itemstack1, itemstack);
			} else if (index >= 1 && index < 5) {
				if (!this.mergeItemStack(itemstack1, 9 + slotShift - 3, 45 + slotShift - 3, false)) {

					return ItemStack.EMPTY;
				}
			} else if (index >= 5 && index < 9) {
				if (!this.mergeItemStack(itemstack1, 9 + slotShift - 3, 45 + slotShift - 3, false)) {

					return ItemStack.EMPTY;
				}
			}

			/*
			 * // runes -> inv else if (index >= 9 && index < 9 + slotShift) { if
			 * (!this.mergeItemStack(itemstack1, 9 + slotShift - 3, 45 + slotShift - 3,
			 * false)) {
			 * 
			 * return ItemStack.EMPTY;
			 * 
			 * } }
			 */
			// inv -> armor
			else if (entityequipmentslot.getSlotType() == EquipmentSlotType.Group.ARMOR
					&& !(this.inventorySlots.get(8 - entityequipmentslot.getIndex())).getHasStack()) {
				int i = 8 - entityequipmentslot.getIndex();

				if (!this.mergeItemStack(itemstack1, i, i + 1, false)) {

					return ItemStack.EMPTY;
				}

			}

			// inv -> offhand
			else if (entityequipmentslot == EquipmentSlotType.OFFHAND
					&& !(this.inventorySlots.get(45 + slotShift)).getHasStack()) {
				if (!this.mergeItemStack(itemstack1, 45 + slotShift, 46 + slotShift, false)) {

					return ItemStack.EMPTY;

				}
			}
			// inv -> rune
			/*
			 * else if (itemstack.getCapability(RunesCapabilities.ITEM_RUNE,
			 * null).isPresent()) {
			 * 
			 * IRune rune = itemstack.getCapability(RunesCapabilities.ITEM_RUNE, null)
			 * .orElseThrow(NullPointerException::new);
			 * 
			 * for (int runeSlot : rune.getRuneType().getValidSlots()) { if
			 * (rune.canEquip(this.player) && !(this.inventorySlots.get(runeSlot +
			 * 9)).getHasStack() && !this.mergeItemStack(itemstack1, runeSlot + 9, runeSlot
			 * + 10, false)) {
			 * 
			 * return ItemStack.EMPTY; } if (itemstack1.getCount() == 0) break; } } else if
			 * (index >= 9 + slotShift && index < 36 + slotShift) {
			 * 
			 * if (!this.mergeItemStack(itemstack1, 36 + slotShift - 3, 45 + slotShift - 3,
			 * false)) {
			 * 
			 * return ItemStack.EMPTY; } } else if (index >= 36 + slotShift - 3 && index <
			 * 45 + slotShift - 3) { if (!this.mergeItemStack(itemstack1, 9 + slotShift - 3,
			 * 36 + slotShift - 3, false)) {
			 * 
			 * return ItemStack.EMPTY; } } else if (!this.mergeItemStack(itemstack1, 9 +
			 * slotShift - 3, 45 + slotShift - 3, false)) {
			 * 
			 * return ItemStack.EMPTY; }
			 */
			if (itemstack1.isEmpty()) {

				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty() && !runes.isEventBlocked() && slot instanceof SlotRune
					&& itemstack.getCapability(RunesCapabilities.ITEM_RUNE, null).isPresent()) {
				@SuppressWarnings("unused")
				ItemStack finalItemstack = itemstack;
				itemstack.getCapability(RunesCapabilities.ITEM_RUNE, null)
						.ifPresent((iRune -> iRune.onEquipped(playerIn)));
			}

			ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);

			if (index == 0) {
				playerIn.dropItem(itemstack2, false);
			}
		}

		return itemstack;
	}

	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slot) {
		return slot.inventory != this.craftResult && super.canMergeSlot(stack, slot);
	}

}
package com.vincenthuto.hemomancy.container;

import com.vincenthuto.hemomancy.capa.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.capa.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.containers.slot.SlotRune;
import com.vincenthuto.hemomancy.containers.slot.SlotRuneArmor;
import com.vincenthuto.hemomancy.containers.slot.SlotRuneOffHand;
import com.vincenthuto.hemomancy.containers.slot.SlotSelectiveRuneType;
import com.vincenthuto.hemomancy.init.ContainerInit;
import com.vincenthuto.hemomancy.item.rune.ItemContractRune;
import com.vincenthuto.hemomancy.item.rune.ItemVasculariumCharm;
import com.vincenthuto.hemomancy.item.tool.ItemBloodGourd;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
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

public class PlayerExpandedContainer extends AbstractContainerMenu {

	public static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[] {
			InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
			InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET };
	private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EquipmentSlot[] { EquipmentSlot.HEAD,
			EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
	private final CraftingContainer craftMatrix = new CraftingContainer(this, 2, 2);
	private final ResultContainer craftResult = new ResultContainer();
	public final boolean isLocalLevel;
	private final Player player;

	public IRunesItemHandler runes;

	public PlayerExpandedContainer(int id, Inventory playerInventory, boolean localLevel) {
		super(ContainerInit.playerrunes, id);
		this.isLocalLevel = localLevel;
		this.player = playerInventory.player;

		this.runes = this.player.getCapability(RunesCapabilities.RUNES).orElseThrow(NullPointerException::new);

		this.addSlot(new ResultSlot(playerInventory.player, this.craftMatrix, this.craftResult, 0, 154, 28 + 26));

		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < 2; ++j) {
				this.addSlot(new Slot(this.craftMatrix, j + i * 2, 116 + j * 18 - 18, 18 + i * 18 + 26));
			}
		}

		for (int k = 0; k < 4; ++k) {
			final EquipmentSlot EquipmentSlot = VALID_EQUIPMENT_SLOTS[k];
			this.addSlot(new SlotRuneArmor(playerInventory, 36 + (3 - k), 8, 8 + k * 18, EquipmentSlot, this.player));
		}
		this.addSlot(new SlotSelectiveRuneType(player, ItemContractRune.class, runes, 0, 77, 8));
		this.addSlot(new SlotRune(player, runes, 1, 77 + 1 * 18, 8));
		this.addSlot(new SlotRune(player, runes, 2, 77 + 2 * 18, 8));
		this.addSlot(new SlotRune(player, runes, 3, 77 + 3 * 18, 8));
		this.addSlot(new SlotSelectiveRuneType(player, ItemVasculariumCharm.class, runes, 4, 77, 26));
		this.addSlot(new SlotSelectiveRuneType(player, ItemBloodGourd.class, runes, 5, 77, 44));

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
	public void removed(Player player) {
		super.removed(player);
		this.craftResult.clearContent();
		if (!player.level.isClientSide) {
			this.clearContainer(player, this.craftMatrix);
		}
	}

	@Override
	public boolean stillValid(Player p_38974_) {
		return this.player.inventoryMenu.stillValid(p_38974_);
	}

	@Override
	public void slotsChanged(Container par1IInventory) {
		super.slotsChanged(par1IInventory);
		CraftingMenu.slotChangedCraftingGrid(this, player.level, player, craftMatrix, craftResult);
//		try {
//
//			Method onCraftChange = ObfuscationReflectionHelper.findMethod(CraftingMenu.class, "slotChangedCraftingGrid",
//					int.class, Level.class, Player.class, CraftingContainer.class, ResultContainer.class);
//			onCraftChange.invoke(null, this.containerId, this.player.level, this.player, this.craftMatrix,
//					this.craftResult);
//		} catch (IllegalAccessException | InvocationTargetException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public ItemStack quickMoveStack(Player p_39723_, int p_39724_) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(p_39724_);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
			if (p_39724_ == 0) {
				if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemstack1, itemstack);
			} else if (p_39724_ >= 1 && p_39724_ < 5) {
				if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (p_39724_ >= 5 && p_39724_ < 9) {
				if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR
					&& !this.slots.get(8 - equipmentslot.getIndex()).hasItem()) {
				int i = 8 - equipmentslot.getIndex();
				if (!this.moveItemStackTo(itemstack1, i, i + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (equipmentslot == EquipmentSlot.OFFHAND && !this.slots.get(45).hasItem()) {
				if (!this.moveItemStackTo(itemstack1, 45, 46, false)) {
					return ItemStack.EMPTY;
				}
			} else if (p_39724_ >= 9 && p_39724_ < 36) {
				if (!this.moveItemStackTo(itemstack1, 36, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (p_39724_ >= 36 && p_39724_ < 45) {
				if (!this.moveItemStackTo(itemstack1, 9, 36, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(p_39723_, itemstack1);
			if (p_39724_ == 0) {
				p_39723_.drop(itemstack1, false);
			}
		}

		return itemstack;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return slot.container != this.craftResult && super.canTakeItemForPickAll(stack, slot);
	}

}

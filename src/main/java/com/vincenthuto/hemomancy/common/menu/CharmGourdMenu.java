package com.vincenthuto.hemomancy.common.menu;

import com.vincenthuto.hemomancy.common.capability.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.item.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.rune.ItemContractRune;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.menu.slot.RuneArmorSlot;
import com.vincenthuto.hemomancy.common.menu.slot.RuneOffHandSlot;
import com.vincenthuto.hemomancy.common.menu.slot.RuneSlot;
import com.vincenthuto.hemomancy.common.menu.slot.SelectiveRuneTypeSlot;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CharmGourdMenu extends AbstractContainerMenu {

	public static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[] {
			InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
			InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET };
	private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EquipmentSlot[] { EquipmentSlot.HEAD,
			EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
	public final static int GOURD_SLOT_INDEX = 6;
	public final static int CHARM_SLOT_INDEX = 5;
	private final CraftingContainer craftMatrix = new TransientCraftingContainer(this, 2, 2);
	private final ResultContainer craftResult = new ResultContainer();
	private final Player player;

	public IRunesItemHandler runes;

	public CharmGourdMenu(final int windowId, final Inventory playerInventory) {
		this(windowId, playerInventory.player.level(), playerInventory.player.blockPosition(), playerInventory,
				playerInventory.player);
	}

	public CharmGourdMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory);
	}

	public CharmGourdMenu(int windowId, Level world, BlockPos pos, Inventory playerInventory, Player playerEntity) {
		super(ContainerInit.gourd_charm_inventory.get(), windowId);
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
			this.addSlot(new RuneArmorSlot(playerInventory, 36 + (3 - k), 8, 8 + k * 18, EquipmentSlot, this.player));
		}

//		this.addSlot(new SelectiveRuneTypeSlot(player, ItemContractRune.class, runes, 0, 77, 8));
//		this.addSlot(new RuneSlot(player, runes, 1, 77 + 1 * 18, 8));
//		this.addSlot(new RuneSlot(player, runes, 2, 77 + 2 * 18, 8));
//		this.addSlot(new RuneSlot(player, runes, 3, 77 + 3 * 18, 8));
		this.addSlot(new SelectiveRuneTypeSlot(player, VasculariumCharmItem.class, runes, CHARM_SLOT_INDEX, 77, 26));
		this.addSlot(new SelectiveRuneTypeSlot(player, BloodGourdItem.class, runes, GOURD_SLOT_INDEX, 77, 44));

		for (int l = 0; l < 3; ++l) {
			for (int j1 = 0; j1 < 9; ++j1) {
				this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
		}

		this.addSlot(new RuneOffHandSlot(playerInventory, 40, 96 - 19, 62));
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return slot.container != this.craftResult && super.canTakeItemForPickAll(stack, slot);
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack = slot.getItem();
			stack = itemStack.copy();
			if (index < 3 * 9) {
				if (!this.moveItemStackTo(itemStack, 3 * 9, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack, 0, 3 * 9, false)) {
				return ItemStack.EMPTY;
			}
			if (itemStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return stack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.craftResult.clearContent();
		if (!player.level().isClientSide) {
			this.clearContainer(player, this.craftMatrix);
		}
	}

	@Override
	public void slotsChanged(Container par1IInventory) {
		super.slotsChanged(par1IInventory);
		CraftingMenu.slotChangedCraftingGrid(this, player.level(), player, craftMatrix, craftResult);
	}

	@Override
	public boolean stillValid(Player p_38974_) {
		return this.player.inventoryMenu.stillValid(p_38974_);
	}

}

package com.vincenthuto.hemomancy.common.menu;

import com.vincenthuto.hemomancy.common.capability.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.item.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.item.rune.ItemFungalRune;
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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SporeImplantMenu extends AbstractContainerMenu {

	public static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[] {
			InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
			InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET };
	private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EquipmentSlot[] { EquipmentSlot.HEAD,
			EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
	public final static int GOURD_SLOT_INDEX = 6;
	public final static int CHARM_SLOT_INDEX = 5;
	public final static int JAR_SLOT_INDEX = 7; // Index in the rune capability handler for the Morphling jar slot
	private final CraftingContainer craftMatrix = new TransientCraftingContainer(this, 2, 2);
	private final ResultContainer craftResult = new ResultContainer();
	private final Player player;

	public IRunesItemHandler runes;

	public SporeImplantMenu(final int windowId, final Inventory playerInventory) {
		this(windowId, playerInventory.player.level(), playerInventory.player.blockPosition(), playerInventory,
				playerInventory.player);
	}

	public SporeImplantMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory);
	}

	public SporeImplantMenu(int windowId, Level world, BlockPos pos, Inventory playerInventory, Player playerEntity) {
		super(ContainerInit.fungal_implantation.get(), windowId);
		this.player = playerInventory.player;

		this.runes = this.player.getCapability(RunesCapabilities.RUNES).orElseThrow(NullPointerException::new);

		// this.addSlot(new ResultSlot(playerInventory.player, this.craftMatrix,
		// this.craftResult, 0, 154, 28 + 26));

//		for (int i = 0; i < 2; ++i) {
//			for (int j = 0; j < 2; ++j) {
//				this.addSlot(new Slot(this.craftMatrix, j + i * 2, 116 + j * 18 - 18, 18 + i * 18 + 26));
//			}
//		}

		for (int k = 0; k < 4; ++k) {
			final EquipmentSlot EquipmentSlot = VALID_EQUIPMENT_SLOTS[k];
			this.addSlot(new RuneArmorSlot(playerInventory, 36 + (3 - k), 8, 8 + k * 18, EquipmentSlot, this.player));
		}

		this.addSlot(new SelectiveRuneTypeSlot(player, ItemFungalRune.class, runes, 0, 124, 36));
		this.addSlot(new RuneSlot(player, runes, 1, 106, 18));
		this.addSlot(new RuneSlot(player, runes, 2, 142, 18));
		this.addSlot(new RuneSlot(player, runes, 3, 106, 54));
		this.addSlot(new RuneSlot(player, runes, 4, 142, 54));
		this.addSlot(new SelectiveRuneTypeSlot(player, ItemMorphlingJar.class, runes, JAR_SLOT_INDEX, 77, 8));    // UI slot 9
		this.addSlot(new SelectiveRuneTypeSlot(player, VasculariumCharmItem.class, runes, CHARM_SLOT_INDEX, 77, 26)); // UI slot 10
		this.addSlot(new SelectiveRuneTypeSlot(player, BloodGourdItem.class, runes, GOURD_SLOT_INDEX, 77, 44));    // UI slot 11

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
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stackInSlot = slot.getItem();
		ItemStack originalStack = stackInSlot.copy();

		// Actual slot layout (order added in constructor):
		// 0-3      : armor (head, chest, legs, feet)
		// 4        : fungal rune slot (rune cap slot 0)
		// 5-8      : rune slots      (rune cap slots 1-4)
		// 9        : jar slot        (rune cap slot 7)
		// 10       : charm slot      (rune cap slot 5)
		// 11       : gourd slot      (rune cap slot 6)
		// 12-38    : player main inventory (27 slots)
		// 39-47    : hotbar (9 slots)
		// 48       : offhand
		final int armorStart     = 0;
		final int armorEnd       = 3;   // inclusive
		final int fungalSlotUI   = 4;
		final int runeSlotStart  = 5;
		final int runeSlotEnd    = 8;   // inclusive
		final int jarSlotUI      = 9;
		final int charmSlotUI    = 10;
		final int gourdSlotUI    = 11;
		final int containerEnd   = 12;  // first player-inv slot
		final int playerInvStart = 12;
		final int hotbarStart    = 39;
		final int hotbarEnd      = 47;  // inclusive
		final int offhandSlot    = 48;

		if (index < containerEnd) {
			// ── Moving FROM a container slot → player inventory ──
			if (!this.moveItemStackTo(stackInSlot, playerInvStart, offhandSlot + 1, true)) {
				return ItemStack.EMPTY;
			}
		} else {
			// ── Moving FROM player inventory / hotbar / offhand → container ──
			boolean moved = false;

			// Armor items → armor slots
			if (!moved) {
				for (int i = armorStart; i <= armorEnd; i++) {
					Slot armorSlot = this.slots.get(i);
					if (!armorSlot.hasItem() && armorSlot.mayPlace(stackInSlot)) {
						armorSlot.set(stackInSlot.split(stackInSlot.getMaxStackSize()));
						moved = true;
						break;
					}
				}
			}

			// Morphling jar → jar slot
			if (!moved && stackInSlot.getItem() instanceof ItemMorphlingJar) {
				Slot jarSlot = this.slots.get(jarSlotUI);
				if (!jarSlot.hasItem() && jarSlot.mayPlace(stackInSlot)) {
					jarSlot.set(stackInSlot.split(1));
					moved = true;
				}
			}

			// Vascularium charm → charm slot
			if (!moved && stackInSlot.getItem() instanceof VasculariumCharmItem) {
				Slot charmSlot = this.slots.get(charmSlotUI);
				if (!charmSlot.hasItem() && charmSlot.mayPlace(stackInSlot)) {
					charmSlot.set(stackInSlot.split(1));
					moved = true;
				}
			}

			// Blood gourd → gourd slot
			if (!moved && stackInSlot.getItem() instanceof BloodGourdItem) {
				Slot gourdSlot = this.slots.get(gourdSlotUI);
				if (!gourdSlot.hasItem() && gourdSlot.mayPlace(stackInSlot)) {
					gourdSlot.set(stackInSlot.split(1));
					moved = true;
				}
			}

			// Fungal rune → fungal slot
			if (!moved && stackInSlot.getItem() instanceof ItemFungalRune) {
				Slot fungalSlot = this.slots.get(fungalSlotUI);
				if (!fungalSlot.hasItem() && fungalSlot.mayPlace(stackInSlot)) {
					fungalSlot.set(stackInSlot.split(1));
					moved = true;
				}
			}

			// Other runes → rune slots 1-4
			if (!moved) {
				for (int i = runeSlotStart; i <= runeSlotEnd && !moved; i++) {
					Slot runeSlot = this.slots.get(i);
					if (!runeSlot.hasItem() && runeSlot.mayPlace(stackInSlot)) {
						runeSlot.set(stackInSlot.split(1));
						moved = true;
					}
				}
			}

			// Nothing matched — swap between hotbar and main inventory
			if (!moved) {
				if (index >= playerInvStart && index < hotbarStart) {
					if (!this.moveItemStackTo(stackInSlot, hotbarStart, hotbarEnd + 1, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= hotbarStart && index <= offhandSlot) {
					if (!this.moveItemStackTo(stackInSlot, playerInvStart, hotbarStart, false)) {
						return ItemStack.EMPTY;
					}
				} else {
					return ItemStack.EMPTY;
				}
			}
		}

		if (stackInSlot.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		return originalStack;
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

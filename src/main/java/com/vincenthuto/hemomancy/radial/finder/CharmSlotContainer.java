package com.vincenthuto.hemomancy.radial.finder;


import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.vincenthuto.hemomancy.network.PacketHandler;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;

public class CharmSlotContainer extends RecipeBookMenu<CraftingContainer> {

	@ObjectHolder("hemomancy:charm_slot_container")
	public static MenuType<CharmSlotContainer> TYPE;

	private final CharmSlot slotCharm;
	private final ICharmSlot extensionSlot;

	private final CraftingContainer craftingInventory = new CraftingContainer(this, 2, 2);
	private final ResultContainer craftResultInventory = new ResultContainer();
	private final Player player;

	@SuppressWarnings("unused")
	private interface SlotFactory<T extends Slot> {
		T create(ICharmSlot slot, int x, int y);
	}

	public CharmSlotContainer(int id, Inventory playerInventory) {
		super(TYPE, id);
		this.player = playerInventory.player;
		this.addSlot(
				new ResultSlot(playerInventory.player, this.craftingInventory, this.craftResultInventory, 0, 154, 28));

		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < 2; ++j) {
				this.addSlot(new Slot(this.craftingInventory, j + i * 2, 98 + j * 18, 18 + i * 18));
			}
		}

		for (int k = 0; k < 4; ++k) {
			final EquipmentSlot equipmentslottype = InventoryMenu.SLOT_IDS[k];
			this.addSlot(new Slot(playerInventory, 39 - k, 8, 8 + k * 18) {
				public int getMaxStackSize() {
					return 1;
				}

				public boolean mayPlace(ItemStack stack) {
					return stack.canEquip(equipmentslottype, player);
				}

				public boolean mayPickup(Player playerIn) {
					ItemStack itemstack = this.getItem();
					return !itemstack.isEmpty() && !playerIn.isCreative()
							&& EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.mayPickup(playerIn);
				}

				@Nullable
				@OnlyIn(Dist.CLIENT)
				public String getSlotTexture() {
					return InventoryMenu.TEXTURE_EMPTY_SLOTS[equipmentslottype.getIndex()].toString();
				}
			});
		}

		for (int l = 0; l < 3; ++l) {
			for (int j1 = 0; j1 < 9; ++j1) {
				this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
		}

		this.addSlot(new Slot(playerInventory, 40, 77, 62) {
			{
				setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
			}
		});

		CharmExtensionSlot container = playerInventory.player.getCapability(CharmExtensionSlot.CAPABILITY)
				.orElseThrow(() -> new RuntimeException("Item handler not present."));

		extensionSlot = container.getCharm();

		this.addSlot(slotCharm = new CharmSlot(CharmSlotContainer.this.extensionSlot, 77, 44));

		if (playerInventory.player.level.isClientSide) {
			PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new PacketCharmContainerSlot());
		}
	}

	@Override
	public List<RecipeBookCategories> getRecipeBookCategories() {
		return Lists.newArrayList(RecipeBookCategories.CRAFTING_SEARCH, RecipeBookCategories.CRAFTING_EQUIPMENT,
				RecipeBookCategories.CRAFTING_BUILDING_BLOCKS, RecipeBookCategories.CRAFTING_MISC,
				RecipeBookCategories.CRAFTING_REDSTONE);
	}

	@Override
	public RecipeBookType getRecipeBookType() {
		return RecipeBookType.CRAFTING;
	}

	@Override
	public boolean shouldMoveToInventory(int slot) {
		return slot != this.getResultSlotIndex();
	}

	@Override
	public void fillCraftSlotsStackedContents(StackedContents itemHelperIn) {
		this.craftingInventory.fillStackedContents(itemHelperIn);
	}

	@Override
	public void clearCraftingContent() {
		this.craftResultInventory.clearContent();
		this.craftingInventory.clearContent();
	}

	@Override
	public boolean recipeMatches(Recipe<? super CraftingContainer> recipeIn) {
		return recipeIn.matches(this.craftingInventory, this.player.level);
	}

	@Override
	public void slotsChanged(Container inventoryIn) {
		Bridge.slotChangedCraftingGridAccessor(this, this.player.level, this.player, this.craftingInventory,
				this.craftResultInventory);
	}

	private static class Bridge extends CraftingMenu {
		private Bridge(int p_39353_, Inventory p_39354_) {
			super(p_39353_, p_39354_);
			throw new IllegalStateException("Not instantiable.");
		}

		public static void slotChangedCraftingGridAccessor(AbstractContainerMenu container, Level level, Player player,
				CraftingContainer craftingInventory, ResultContainer craftResultInventory) {
			CraftingMenu.slotChangedCraftingGrid(container, level, player, craftingInventory, craftResultInventory);
		}
	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);

		this.craftResultInventory.clearContent();

		if (!playerIn.level.isClientSide) {
			this.clearContainer(playerIn, this.craftingInventory);
			CharmFinder.sendSync(playerIn);
		}
	}

	@Override
	public boolean stillValid(Player playerIn) {
		return true;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
		return slotIn.container != this.craftResultInventory && super.canTakeItemForPickAll(stack, slotIn);
	}

	@Override
	public int getResultSlotIndex() {
		return 0;
	}

	@Override
	public int getGridWidth() {
		return this.craftingInventory.getWidth();
	}

	@Override
	public int getGridHeight() {
		return this.craftingInventory.getHeight();
	}

	@Override
	public int getSize() {
		return 5;
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem()) {
			ItemStack remaining = ItemStack.EMPTY;
			ItemStack slotContents = slot.getItem();
			remaining = slotContents.copy();

			if (index == slotCharm.index) {
				if (!this.moveItemStackTo(slotContents, 9, 45, false)) {
					return ItemStack.EMPTY;
				}

				return remaining;
			} else if (slot.mayPlace(slotContents)) {
				if (!this.moveItemStackTo(slotContents, slotCharm.index, slotCharm.index + 1, false)) {
					return ItemStack.EMPTY;
				}
			}
		}

		ItemStack itemstack = ItemStack.EMPTY;
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			EquipmentSlot equipmentslottype = Mob.getEquipmentSlotForItem(itemstack);
			if (index == 0) {
				if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemstack1, itemstack);
			} else if (index >= 1 && index < 5) {
				if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 5 && index < 9) {
				if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (equipmentslottype.getType() == EquipmentSlot.Type.ARMOR
					&& !this.slots.get(8 - equipmentslottype.getIndex()).hasItem()) {
				int i = 8 - equipmentslottype.getIndex();
				if (!this.moveItemStackTo(itemstack1, i, i + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (equipmentslottype == EquipmentSlot.OFFHAND && !this.slots.get(45).hasItem()) {
				if (!this.moveItemStackTo(itemstack1, 45, 46, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 9 && index < 36) {
				if (!this.moveItemStackTo(itemstack1, 36, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 36 && index < 45) {
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

			slot.onTake(playerIn, itemstack1);
			if (index == 0) {
				playerIn.drop(itemstack1, false);
			}
		}

		return itemstack;
	}
}
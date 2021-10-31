package com.vincenthuto.hemomancy.container;

import com.vincenthuto.hemomancy.containers.slot.JuiceinatorFlaskSlot;
import com.vincenthuto.hemomancy.containers.slot.JuiceinatorFuelSlot;
import com.vincenthuto.hemomancy.init.ContainerInit;
import com.vincenthuto.hemomancy.init.RecipeTypeInit;
import com.vincenthuto.hutoslib.common.item.HLItemInit;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class MenuJuiceinator extends AbstractContainerMenu {

	public static final int INGREDIENT_SLOT = 0;
	public static final int FUEL_SLOT = 1;
	public static final int RESULT_SLOT = 2;
	public static final int SLOT_COUNT = 4;
	public static final int DATA_COUNT = 4;
	private final Container container;
	private final ContainerData data;
	protected final Level level;
	private final RecipeType<? extends AbstractCookingRecipe> recipeType;

	public MenuJuiceinator(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory);
	}

	public MenuJuiceinator(int windowIdIn, Inventory playerInventoryIn) {
		this(windowIdIn, playerInventoryIn, new SimpleContainer(4), new SimpleContainerData(4));
	}

	public MenuJuiceinator(final int windowId, final Inventory playerInventory, final Container container,
			final ContainerData containerData) {
		super(ContainerInit.juiceinator.get(), windowId);
		this.recipeType = RecipeTypeInit.juiceinator_recipe_type;
		checkContainerSize(container, 4);
		checkContainerDataCount(containerData, 4);
		this.container = container;
		this.data = containerData;
		this.level = playerInventory.player.level;
		this.addSlot(new Slot(container, 0, 56, 17));
		this.addSlot(new JuiceinatorFlaskSlot(this, container, 3, 30, 35));
		this.addSlot(new JuiceinatorFuelSlot(this, container, 1, 56, 53));
		this.addSlot(new FurnaceResultSlot(playerInventory.player, container, 2, 116, 35));
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
		}
		this.addDataSlots(containerData);
	}

	public void fillCraftSlotsStackedContents(StackedContents p_38976_) {
		if (this.container instanceof StackedContentsCompatible) {
			((StackedContentsCompatible) this.container).fillStackedContents(p_38976_);
		}
	}

	public void clearCraftingContent() {
		this.getSlot(0).set(ItemStack.EMPTY);
		this.getSlot(2).set(ItemStack.EMPTY);
	}

	public boolean recipeMatches(Recipe<? super Container> p_38980_) {
		return p_38980_.matches(this.container, this.level);
	}

	public int getResultSlotIndex() {
		return 2;
	}

	public int getGridWidth() {
		return 1;
	}

	public int getGridHeight() {
		return 1;
	}

	public int getSize() {
		return 4;
	}

	@Override
	public boolean stillValid(Player p_38974_) {
		return this.container.stillValid(p_38974_);
	}

	@Override
	public ItemStack quickMoveStack(Player p_38986_, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index == 2) {
				if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (index != 1 && index != 0) {
				if (this.canSmelt(itemstack1)) {
					if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
						return ItemStack.EMPTY;
					}
				} else if (this.isFuel(itemstack1)) {
					if (!this.moveItemStackTo(itemstack1, 1, 3, false)) {
						return ItemStack.EMPTY;
					}
				} else if (itemstack1.getItem() == HLItemInit.cured_clay_flask.get()) {
					if (!this.moveItemStackTo(itemstack1, 1, 4, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= 3 && index < 30) {
					if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= 30 && index < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
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

			slot.onTake(p_38986_, itemstack1);
		}
		return itemstack;
	}

	@SuppressWarnings("unchecked")
	protected boolean canSmelt(ItemStack p_38978_) {
		return this.level.getRecipeManager().getRecipeFor((RecipeType<AbstractCookingRecipe>) this.recipeType,
				new SimpleContainer(p_38978_), this.level).isPresent();
	}

	public boolean isFuel(ItemStack p_38989_) {
		return net.minecraftforge.common.ForgeHooks.getBurnTime(p_38989_, this.recipeType) > 0;
	}

	public int getBurnProgress() {
		int i = this.data.get(2);
		int j = this.data.get(3);
		return j != 0 && i != 0 ? i * 24 / j : 0;
	}

	public int getLitProgress() {
		int i = this.data.get(1);
		if (i == 0) {
			i = 200;
		}
		return this.data.get(0) * 13 / i;
	}

	public boolean isLit() {
		return this.data.get(0) > 0;
	}

	public boolean shouldMoveToInventory(int p_150463_) {
		return p_150463_ != 1;
	}

	@Override
	public void setData(int p_38855_, int p_38856_) {
		super.setData(p_38855_, p_38856_);
	}
}

package com.vincenthuto.hemomancy.common.menu;

import java.util.Objects;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.menu.slot.GhastlyAlembicFlaskSlot;
import com.vincenthuto.hemomancy.common.recipe.GhastlyAlembicRecipe;
import com.vincenthuto.hemomancy.common.tile.crafting.GhastlyAlembicBlockEntity;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;

import net.minecraft.network.FriendlyByteBuf;
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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Ghastly Alembic menu — 3 container slots + player inventory.
 * <ul>
 *   <li>Slot 0 (index 0) = Input ingredient</li>
 *   <li>Slot 1 (index 1) = Flask (cured clay flasks)</li>
 *   <li>Slot 2 (index 2) = Result output</li>
 * </ul>
 * Container data: [0] = heated (0/1), [1] = cookingProgress, [2] = cookingTotalTime
 */
public class GhastlyAlembicMenu extends AbstractContainerMenu {

	public static final int INGREDIENT_SLOT = 0;
	public static final int FLASK_SLOT      = 1;
	public static final int RESULT_SLOT     = 2;
	public static final int CATALYST_SLOT   = 3;
	public static final int SLOT_COUNT      = 4;
	public static final int DATA_COUNT      = 3;

	// Crafting area height — matches the screen's layout
	public static final int CRAFT_AREA_HEIGHT = 80;

	// Player inventory starts after the 4 container slots
	private static final int INV_START  = SLOT_COUNT;        // 4
	private static final int INV_END    = INV_START + 27;    // 31
	private static final int HOTBAR_END = INV_END + 9;       // 40

	private final GhastlyAlembicBlockEntity container;
	private final ContainerData data;
	protected final Level level;
	private final GhastlyAlembicBlockEntity te;

	// ---- Constructors ----

	private static GhastlyAlembicBlockEntity getBlockEntity(final Inventory playerInv, final FriendlyByteBuf buf) {
		Objects.requireNonNull(playerInv, "playerInventory cannot be null");
		Objects.requireNonNull(buf, "data cannot be null");
		final BlockEntity tileAtPos = playerInv.player.level().getBlockEntity(buf.readBlockPos());
		if (tileAtPos instanceof GhastlyAlembicBlockEntity be) {
			return be;
		}
		throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
	}

	/** Network constructor (client side) */
	public GhastlyAlembicMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory, getBlockEntity(playerInventory, data));
	}

	/** Server-side convenience (no explicit data container) */
	public GhastlyAlembicMenu(int windowId, Inventory playerInventory, GhastlyAlembicBlockEntity blockEntity) {
		this(windowId, playerInventory, blockEntity, new SimpleContainerData(DATA_COUNT));
	}

	/** Full constructor */
	public GhastlyAlembicMenu(final int windowId, final Inventory playerInventory, final GhastlyAlembicBlockEntity container,
			final ContainerData containerData) {
		super(ContainerInit.ghastly_alembic.get(), windowId);
		this.te = container;
		checkContainerSize(container, SLOT_COUNT);
		checkContainerDataCount(containerData, DATA_COUNT);
		this.container = container;
		this.data = containerData;
		this.level = playerInventory.player.level();

		// Container slots — positions designed for custom-rendered screen
		// Input slot: left-center of crafting area
		this.addSlot(new Slot(container, INGREDIENT_SLOT, 44, 32));
		// Flask slot: underneath the blood volume bar (extraction zone)
		this.addSlot(new GhastlyAlembicFlaskSlot(this, container, FLASK_SLOT, 155, 58));
		// Result slot: right of crafting area
		this.addSlot(new FurnaceResultSlot(playerInventory.player, container, RESULT_SLOT, 134, 32));
		// Catalyst slot: top-left corner of the crafting area
		this.addSlot(new Slot(container, CATALYST_SLOT, 8, 8));

		// Player inventory (3 rows) — pushed down below crafting area
		int invY = CRAFT_AREA_HEIGHT + 14;
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
			}
		}
		// Hotbar
		int hotbarY = invY + 58;
		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, hotbarY));
		}

		this.addDataSlots(containerData);
	}

	// ---- Data accessors ----

	/** Whether the Ghastly Alembic is currently being heated (fire below) */
	public boolean isHeated() {
		return this.data.get(0) > 0;
	}

	/** Progress scaled 0–24 for rendering */
	public int getBurnProgress() {
		int progress = this.data.get(1);
		int total = this.data.get(2);
		return total != 0 && progress != 0 ? progress * 24 / total : 0;
	}

	/** Whether actively cooking */
	public boolean isCooking() {
		return this.data.get(1) > 0 && isHeated();
	}

	public GhastlyAlembicBlockEntity getTe() {
		return te;
	}

	// ---- Recipe helpers ----

	protected boolean canSmelt(ItemStack stack) {
		// Check if any ghastly alembic recipe accepts this item as its main ingredient
		return GhastlyAlembicRecipe.getAllRecipes(this.level)
				.stream()
				.anyMatch(r -> r.getIngredient().test(stack));
	}

	public boolean isFlask(ItemStack stack) {
		return stack.getItem() == HLItemInit.cured_clay_flask.get();
	}

	// ---- Shift-click logic ----

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack copy = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) return copy;

		ItemStack slotStack = slot.getItem();
		copy = slotStack.copy();

		// Moving FROM container slots to player inventory
		if (index == RESULT_SLOT) {
			if (!this.moveItemStackTo(slotStack, INV_START, HOTBAR_END, true)) return ItemStack.EMPTY;
			slot.onQuickCraft(slotStack, copy);
		} else if (index == INGREDIENT_SLOT || index == FLASK_SLOT || index == CATALYST_SLOT) {
			if (!this.moveItemStackTo(slotStack, INV_START, HOTBAR_END, false)) return ItemStack.EMPTY;
		}
		// Moving FROM player inventory to container
		else {
			if (this.canSmelt(slotStack)) {
				if (!this.moveItemStackTo(slotStack, INGREDIENT_SLOT, INGREDIENT_SLOT + 1, false)) return ItemStack.EMPTY;
			} else if (this.isFlask(slotStack)) {
				if (!this.moveItemStackTo(slotStack, FLASK_SLOT, FLASK_SLOT + 1, false)) return ItemStack.EMPTY;
			} else if (index >= INV_START && index < INV_END) {
				// Try catalyst slot first, then hotbar
				if (!this.moveItemStackTo(slotStack, CATALYST_SLOT, CATALYST_SLOT + 1, false)) {
					if (!this.moveItemStackTo(slotStack, INV_END, HOTBAR_END, false)) return ItemStack.EMPTY;
				}
			} else if (index >= INV_END && index < HOTBAR_END) {
				if (!this.moveItemStackTo(slotStack, INV_START, INV_END, false)) return ItemStack.EMPTY;
			}
		}

		if (slotStack.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		if (slotStack.getCount() == copy.getCount()) return ItemStack.EMPTY;
		slot.onTake(player, slotStack);
		return copy;
	}

	// ---- Misc ----

	public void clearCraftingContent() {
		this.getSlot(INGREDIENT_SLOT).set(ItemStack.EMPTY);
		this.getSlot(RESULT_SLOT).set(ItemStack.EMPTY);
	}

	public void fillCraftSlotsStackedContents(StackedContents contents) {
		((StackedContentsCompatible) this.container).fillStackedContents(contents);
	}

	public int getGridWidth() { return 1; }
	public int getGridHeight() { return 1; }
	public int getResultSlotIndex() { return RESULT_SLOT; }
	public int getSize() { return SLOT_COUNT; }

	public boolean recipeMatches(Recipe<? super GhastlyAlembicBlockEntity> recipe) {
		return recipe.matches(this.container, this.level);
	}

	public boolean shouldMoveToInventory(int slotIndex) {
		return slotIndex != RESULT_SLOT;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}
}

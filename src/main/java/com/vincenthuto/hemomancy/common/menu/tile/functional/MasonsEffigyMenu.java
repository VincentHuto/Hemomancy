package com.vincenthuto.hemomancy.common.menu.tile.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.tile.functional.AnastomoticBrazierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MasonsEffigyMenu extends AbstractContainerMenu {
	public static final int MAX_SELECTED_SCARS = 4;
	public static final int PLAYER_INV_START = 0;
	public static final int PLAYER_INV_SLOTS = 36;
	public static final int PLAYER_INV_X = 32;
	public static final int PLAYER_INV_Y = 150;

	private final Player player;
	private final BlockPos pos;
	private final List<ResourceLocation> openingKnownScarIds;
	private final List<ResourceLocation> openingActiveScarIds;
	private final boolean hasOpeningSnapshot;

	public MasonsEffigyMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
		this(windowId, playerInventory, readOpeningData(data));
	}

	public MasonsEffigyMenu(int windowId, Inventory playerInventory, BlockPos pos) {
		this(windowId, playerInventory, pos, List.of(), List.of(), false);
	}

	private MasonsEffigyMenu(int windowId, Inventory playerInventory, OpeningData openingData) {
		this(windowId, playerInventory, openingData.pos(), openingData.known(), openingData.active(), true);
	}

	public MasonsEffigyMenu(int windowId, Inventory playerInventory, BlockPos pos,
			List<ResourceLocation> openingKnownScarIds, List<ResourceLocation> openingActiveScarIds) {
		this(windowId, playerInventory, pos, openingKnownScarIds, openingActiveScarIds, true);
	}

	private MasonsEffigyMenu(int windowId, Inventory playerInventory, BlockPos pos,
			List<ResourceLocation> openingKnownScarIds, List<ResourceLocation> openingActiveScarIds,
			boolean hasOpeningSnapshot) {
		super(ContainerInit.mason_effigy.get(), windowId);
		this.player = playerInventory.player;
		this.pos = Objects.requireNonNull(pos);
		this.openingKnownScarIds = List.copyOf(openingKnownScarIds);
		this.openingActiveScarIds = List.copyOf(openingActiveScarIds);
		this.hasOpeningSnapshot = hasOpeningSnapshot;

		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
			}
		}
		for (int col = 0; col < 9; ++col) {
			addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * 18, PLAYER_INV_Y + 58));
		}
	}

	public BlockPos getPos() {
		return pos;
	}

	public int getMaxSelectableScars() {
		return Math.min(MAX_SELECTED_SCARS, AnastomoticBrazierBlockEntity.getMaxActiveScars(player));
	}

	public List<ResourceLocation> getOpeningKnownScarIds() {
		return openingKnownScarIds;
	}

	public List<ResourceLocation> getOpeningActiveScarIds() {
		return openingActiveScarIds;
	}

	public List<ResourceLocation> getKnownScarIds() {
		if (hasOpeningSnapshot) {
			return openingKnownScarIds;
		}
		List<ResourceLocation> ids = HemoCapabilityAccess.getScarState(player)
				.map(scars -> {
					List<ResourceLocation> current = new ArrayList<>(scars.getKnownCerebralScars());
					current.sort(Comparator.comparing(ResourceLocation::toString));
					return current;
				})
				.orElseGet(List::of);
		return ids;
	}

	public List<ResourceLocation> getActiveScarIds() {
		if (hasOpeningSnapshot) {
			return openingActiveScarIds;
		}
		List<ResourceLocation> ids = HemoCapabilityAccess.getScarState(player)
				.map(scars -> {
					List<ResourceLocation> current = new ArrayList<>(scars.getActiveCerebralScars());
					current.sort(Comparator.comparing(ResourceLocation::toString));
					return current;
				})
				.orElseGet(List::of);
		return ids;
	}

	private static List<ResourceLocation> readIds(FriendlyByteBuf data) {
		int count = data.readVarInt();
		List<ResourceLocation> ids = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			ids.add(data.readResourceLocation());
		}
		return ids;
	}

	private static OpeningData readOpeningData(FriendlyByteBuf data) {
		List<ResourceLocation> known = readIds(data);
		List<ResourceLocation> active = readIds(data);
		return new OpeningData(known, active, data.readBlockPos());
	}

	private record OpeningData(List<ResourceLocation> known, List<ResourceLocation> active, BlockPos pos) {
	}

	@Override
	public boolean stillValid(Player player) {
		return player.level().getBlockState(pos).is(BlockInit.mason_effigy.get())
				&& player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (slot == null || !slot.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getItem();
		ItemStack copy = stack.copy();
		if (index < 27) {
			if (!moveItemStackTo(stack, 27, 36, false)) {
				return ItemStack.EMPTY;
			}
		} else if (!moveItemStackTo(stack, 0, 27, false)) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return copy;
	}
}

package com.vincenthuto.hemomancy.common.menu.tile.functional;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SpecimenContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public class PhlebotomistsCabinetMenu extends AbstractContainerMenu {
    private final SpecimenContainer cabinet;
    private final SimpleContainer icons = new SimpleContainer(9);
    private final int[] counts = new int[9];

    public PhlebotomistsCabinetMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(ContainerInit.phlebotomists_cabinet.get(), id, inventory, buffer);
    }
    public PhlebotomistsCabinetMenu(MenuType<?> type, int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(type, id, inventory, findContainer(inventory, buffer));
    }
    private static SpecimenContainer findContainer(Inventory inventory, FriendlyByteBuf buffer) {
        var be = inventory.player.level().getBlockEntity(buffer.readBlockPos());
        return be instanceof SpecimenContainer container ? container : null;
    }
    public PhlebotomistsCabinetMenu(int id, Inventory inventory, SpecimenContainer cabinet) {
        this(ContainerInit.phlebotomists_cabinet.get(), id, inventory, cabinet);
    }
    public PhlebotomistsCabinetMenu(MenuType<?> type, int id, Inventory inventory, SpecimenContainer cabinet) {
        super(type, id);
        this.cabinet = cabinet;
        for (int i = 0; i < 9; i++) {
            final int cell = i;
            addSlot(new Slot(icons, i, 10 + i % 3 * 54, 22 + i / 3 * 36) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return false; }
            });
            addDataSlot(new DataSlot() {
                @Override public int get() { return counts[cell]; }
                @Override public void set(int value) { counts[cell] = value; }
            });
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 144 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 202));
        refresh();
        if (cabinet != null) cabinet.startOpen(inventory.player);
    }
    public boolean isCabinet(SpecimenContainer other) { return cabinet == other; }
    public int capacity() { return cabinet == null ? 64 : cabinet.storage().capacity(); }
    public int count(int cell) { return counts[cell]; }
    @Override public boolean stillValid(Player player) {
        return cabinet != null && cabinet.available() && !cabinet.blockEntity().isRemoved() && player.isAlive() && !player.isSpectator()
                && cabinet.blockEntity().getLevel() == player.level()
                && player.level().getBlockEntity(cabinet.blockEntity().getBlockPos()) == cabinet.blockEntity()
                && player.distanceToSqr(cabinet.blockEntity().getBlockPos().getCenter()) <= 64;
    }
    private void refresh() {
        if (cabinet == null || cabinet.blockEntity().getLevel() == null || cabinet.blockEntity().getLevel().isClientSide) return;
        for (int i = 0; i < 9; i++) {
            icons.setItem(i, cabinet.storage().getStackInSlot(i));
            counts[i] = cabinet.storage().count(i);
        }
    }
    @Override public void broadcastChanges() { refresh(); super.broadcastChanges(); }
    @Override public void removed(Player player) {
        super.removed(player);
        if (cabinet != null) cabinet.stopOpen(player);
    }
    @Override public boolean canDragTo(Slot slot) { return slot.index >= 9; }
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) { return slot.index >= 9; }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        if (!stillValid(player) || player.containerMenu != this) return;
        if (slot >= 0 && slot < 9) {
            // Only the server owns virtual quantities. Slot and data sync correct the client snapshot.
            if (player.level().isClientSide || !cabinet.beginTransfer()) return;
            try {
                if (type == ClickType.QUICK_MOVE && (button == 0 || button == 1)) quickMoveStack(player, slot);
                else if (type == ClickType.PICKUP && (button == 0 || button == 1)) {
                    var carried = getCarried();
                    if (carried.isEmpty()) {
                        var extracted = cabinet.storage().extractItem(slot, 1, false);
                        setCarried(extracted);
                        if (!extracted.isEmpty()) recordTransfer(player, false);
                    } else if (cabinet.storage().insertItem(slot, carried.copyWithCount(1), false).isEmpty()) {
                        carried.shrink(1);
                        recordTransfer(player, true);
                    }
                }
                // Swap, clone, throw, collect-all and quick-craft never operate on virtual cells.
                broadcastChanges();
            } finally { cabinet.endTransfer(); }
            return;
        }
        if (slot >= slots.size() || slot < -999) return;
        super.clicked(slot, button, type, player);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (player.level().isClientSide || !stillValid(player) || player.containerMenu != this || index < 0 || index >= slots.size())
            return ItemStack.EMPTY;
        if (!cabinet.beginTransfer()) return ItemStack.EMPTY;
        try {
            var storage = cabinet.storage();
            if (index < 9) {
                while (storage.count(index) > 0) {
                    var vial = storage.extractItem(index, 1, true);
                    if (!moveItemStackTo(vial, 9, slots.size(), true)) break;
                    storage.extractItem(index, 1, false);
                    recordTransfer(player, false);
                }
            } else {
                var source = slots.get(index);
                var stack = source.getItem();
                int target = storage.findInsertionSlot(stack);
                if (target < 0) return ItemStack.EMPTY;
                if (storage.insertItem(target, stack.copyWithCount(1), false).isEmpty()) {
                    stack.shrink(1);
                    source.setChanged();
                    recordTransfer(player, true);
                }
            }
            refresh();
            // Prevent vanilla's repeated quick-move loop from reinterpreting a virtual stack.
            return ItemStack.EMPTY;
        } finally { cabinet.endTransfer(); }
    }
    private void recordTransfer(Player player, boolean inserted) {
        if (cabinet instanceof com.vincenthuto.hemomancy.common.tile.harbinger.functional.PhlebotomistsCabinetBlockEntity)
            com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge.cabinetTransfer(player, inserted);
    }

}

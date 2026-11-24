package com.vincenthuto.hemomancy.common.menu.tile.crafting;

import com.vincenthuto.hemomancy.common.menu.HemoContainerMenu;
import com.vincenthuto.hemomancy.common.enchanting.ResonantForgeRules;
import com.vincenthuto.hemomancy.common.enchanting.ResonantForgeTransfer;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.item.harbinger.CylinderMedia;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ResonantForgeMenu extends HemoContainerMenu {
    /**
     * Slot places on the screen's drawing of the machine as the worker sees it: the hammer bay side-on at the left
     * (pattern drum at the back, anvil under the head, the finished-work tray between them), the grinder bay
     * face-on at the right (tool rest at the stone's inner rim, recording drum above, the cut cylinder beside it).
     */
    public static final int WIDTH = 232, INVENTORY_TOP = 150;
    public static final int[] ANVIL_SLOT = {76, 64}, PATTERN_SLOT = {16, 46}, HAMMERED_SLOT = {54, 100};
    public static final int[] REST_SLOT = {148, 82}, RECORD_SLOT = {160, 44}, GROUND_SLOT = {148, 102}, CUT_SLOT = {206, 44};
    private final ResonantForgeBlockEntity forge;
    private final ContainerData data;

    public ResonantForgeMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, (ResonantForgeBlockEntity) inventory.player.level().getBlockEntity(buffer.readBlockPos()),
                new SimpleContainerData(10));
    }

    public ResonantForgeMenu(int id, Inventory inventory, ResonantForgeBlockEntity forge, ContainerData data) {
        super(ContainerInit.resonant_forge.get(), id);
        this.forge = forge;
        this.data = data;
        addSlot(input(forge, ResonantForgeBlockEntity.APPLICATION_ITEM, ANVIL_SLOT[0], ANVIL_SLOT[1], false));
        addSlot(input(forge, ResonantForgeBlockEntity.APPLICATION_CYLINDER, PATTERN_SLOT[0], PATTERN_SLOT[1], true));
        addSlot(output(forge, ResonantForgeBlockEntity.APPLICATION_OUTPUT, HAMMERED_SLOT[0], HAMMERED_SLOT[1]));
        addSlot(input(forge, ResonantForgeBlockEntity.GRINDING_ITEM, REST_SLOT[0], REST_SLOT[1], false));
        addSlot(input(forge, ResonantForgeBlockEntity.GRINDING_CYLINDER, RECORD_SLOT[0], RECORD_SLOT[1], true));
        addSlot(output(forge, ResonantForgeBlockEntity.GRINDING_EQUIPMENT_OUTPUT, GROUND_SLOT[0], GROUND_SLOT[1]));
        addSlot(output(forge, ResonantForgeBlockEntity.GRINDING_CYLINDER_OUTPUT, CUT_SLOT[0], CUT_SLOT[1]));
        addPlayerInventory(inventory, (WIDTH - 162) / 2, INVENTORY_TOP + 8);
        addDataSlots(data);
    }

    private Slot input(Container container, int slot, int x, int y, boolean cylinder) {
        return new Slot(container, slot, x, y) {
            @Override public boolean mayPlace(ItemStack stack) {
                return forge.idle() && (cylinder ? CylinderMedia.supported(stack)
                        : !CylinderMedia.supported(stack) && stack.getCount() == 1);
            }
            @Override public boolean mayPickup(Player player) { return forge.idle(); }
            @Override public int getMaxStackSize() { return 1; }
        };
    }

    private Slot output(Container container, int slot, int x, int y) {
        return new Slot(container, slot, x, y) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return forge.idle(); }
        };
    }

    public ResonantForgeBlockEntity forge() { return forge; }
    public int progress() { return data.get(0); }
    public int totalTicks() { return data.get(1); }
    public int blood() { return data.get(2); }
    public int capacity() { return data.get(3); }
    /** The tank as its bar draws it: fresh blood over clot (core-loop Track 3). */
    public com.vincenthuto.hemomancy.common.tile.ReservoirReading reading() { return com.vincenthuto.hemomancy.common.tile.ReservoirReading.ofMenu(blood(), capacity(), forge); }
    public int hammerUses() { return data.get(4); }
    public int wheelUses() { return data.get(5); }
    public int tier() { return data.get(6); }
    public int operation() { return data.get(7); }
    public int status() { return data.get(8); }
    public boolean masterMode() { return data.get(9) != 0; }

    public int applicationCost() {
        var pattern = forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER)
                .get(DataComponentInit.RESONANT_PATTERN.get());
        return pattern == null || pattern.isEmpty() ? 0 : ResonantForgeRules.hammeringCost(pattern);
    }

    public int grindingCost() {
        var complete = ResonantForgeTransfer.read(forge.getItem(ResonantForgeBlockEntity.GRINDING_ITEM));
        var pattern = forge.selection().isBlank() ? complete : ResonantForgeRules.select(complete, forge.selection());
        if (pattern == null || pattern.isEmpty()) return 0;
        return ResonantForgeRules.grindingCost(pattern)
                + (forge.masterMode() ? ResonantForgeRules.MASTER_SURCHARGE : 0);
    }

    public ItemStack applicationPreview() {
        if (forge.getLevel() == null) return ItemStack.EMPTY;
        var pattern = forge.getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER)
                .get(DataComponentInit.RESONANT_PATTERN.get());
        if (pattern == null) return ItemStack.EMPTY;
        var preview = ResonantForgeTransfer.apply(forge.getItem(ResonantForgeBlockEntity.APPLICATION_ITEM),
                pattern, forge.getLevel().registryAccess());
        return preview.success() ? preview.equipment() : ItemStack.EMPTY;
    }

    public ResonantForgeTransfer.Capture grindingPreview() {
        if (forge.getLevel() == null) return ResonantForgeTransfer.capture(ItemStack.EMPTY,
                net.minecraft.core.RegistryAccess.EMPTY, "", false);
        return ResonantForgeTransfer.capture(forge.getItem(ResonantForgeBlockEntity.GRINDING_ITEM),
                forge.getLevel().registryAccess(), forge.selection(), forge.masterMode());
    }

    public List<String> selections() {
        List<String> result = new ArrayList<>();
        result.add("");
        var pattern = ResonantForgeTransfer.read(forge.getItem(ResonantForgeBlockEntity.GRINDING_ITEM));
        pattern.enchantments().forEach(entry -> result.add(entry.enchantment()));
        if (!pattern.curse().isBlank()) result.add(ResonantForgeRules.CURSE_SELECTION);
        return result;
    }

    @Override public boolean clickMenuButton(Player player, int button) {
        if (!stillValid(player)) return false;
        if (button >= 0 && button <= 2) {
            boolean started = switch (button) {
                case 0 -> forge.startApply();
                case 1 -> forge.startGrinding();
                default -> forge.startStabilizing();
            };
            // Only the player whose press actually started the operation is credited with its machine use.
            if (started) forge.setOperator(player.getUUID());
            return started;
        }
        return switch (button) {
            case 3 -> forge.cancelOperation();
            case 4 -> forge.toggleMasterMode();
            default -> {
                if (button < 100) yield false;
                List<String> options = selections();
                int index = button - 100;
                yield index < options.size() && forge.setSelection(options.get(index));
            }
        };
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !forge.idle()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        if (index < ResonantForgeBlockEntity.SLOT_COUNT) {
            if (!moveItemStackTo(original, ResonantForgeBlockEntity.SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else if (CylinderMedia.supported(original)) {
            boolean blank = CylinderMedia.blank(original.copyWithCount(1));
            if (!blank && !original.has(DataComponentInit.RESONANT_PATTERN.get()))
                return ItemStack.EMPTY;
            if (blank) {
                if (!moveItemStackTo(original, ResonantForgeBlockEntity.GRINDING_CYLINDER,
                        ResonantForgeBlockEntity.GRINDING_CYLINDER + 1, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(original, ResonantForgeBlockEntity.APPLICATION_CYLINDER,
                    ResonantForgeBlockEntity.APPLICATION_CYLINDER + 1, false)
                    && !moveItemStackTo(original, ResonantForgeBlockEntity.GRINDING_CYLINDER,
                    ResonantForgeBlockEntity.GRINDING_CYLINDER + 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(original, ResonantForgeBlockEntity.APPLICATION_ITEM,
                ResonantForgeBlockEntity.APPLICATION_ITEM + 1, false)
                && !moveItemStackTo(original, ResonantForgeBlockEntity.GRINDING_ITEM,
                ResonantForgeBlockEntity.GRINDING_ITEM + 1, false)) return ItemStack.EMPTY;
        if (original.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override public boolean stillValid(Player player) { return forge != null && forge.stillValid(player); }
}

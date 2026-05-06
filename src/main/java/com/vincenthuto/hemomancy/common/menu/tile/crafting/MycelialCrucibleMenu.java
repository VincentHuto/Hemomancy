package com.vincenthuto.hemomancy.common.menu.tile.crafting;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.item.harbinger.EnzymeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.RecycledEnzymeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.ItemImmatureFungalScar;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.menu.slot.IncubatorCatalystSlot;
import com.vincenthuto.hemomancy.common.tile.crafting.MycelialCrucibleBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class MycelialCrucibleMenu extends AbstractContainerMenu {

    // ── Slot indices ──────────────────────────────────────────────────────────
    public static final int CENTER_SLOT      = MycelialCrucibleBlockEntity.SLOT_CENTER;
    public static final int ENZYME_SLOT_1    = MycelialCrucibleBlockEntity.SLOT_ENZYME_START;
    public static final int ENZYME_SLOT_2    = MycelialCrucibleBlockEntity.SLOT_ENZYME_START + 1;
    public static final int ENZYME_SLOT_3    = MycelialCrucibleBlockEntity.SLOT_ENZYME_START + 2;
    public static final int ENZYME_SLOT_4    = MycelialCrucibleBlockEntity.SLOT_ENZYME_END;
    public static final int OUTPUT_SLOT      = MycelialCrucibleBlockEntity.SLOT_OUTPUT;
    public static final int BLOOD_SLOT       = MycelialCrucibleBlockEntity.SLOT_BLOOD;
    public static final int FLASK_OUT_SLOT   = MycelialCrucibleBlockEntity.SLOT_FLASK_OUTPUT;
    public static final int SLOT_COUNT       = MycelialCrucibleBlockEntity.INVENTORY_SIZE;
    /** progress, totalTime, mode, matureProgress, matureThreshold */
    public static final int DATA_COUNT       = 5;

    // ── Fields ────────────────────────────────────────────────────────────────
    private final MycelialCrucibleBlockEntity te;
    private final ContainerData dataAccess;
    protected final Level level;

    // ── Constructors ──────────────────────────────────────────────────────────
    public MycelialCrucibleMenu(int windowId, Inventory playerInv, FriendlyByteBuf data) {
        this(windowId, playerInv, getTE(playerInv, data));
    }

    public MycelialCrucibleMenu(int windowId, Inventory playerInv, MycelialCrucibleBlockEntity te) {
        this(windowId, playerInv, te, new SimpleContainerData(DATA_COUNT));
    }

    public MycelialCrucibleMenu(int windowId, Inventory playerInv,
            MycelialCrucibleBlockEntity te, ContainerData data) {
        super(ContainerInit.mycelial_crucible.get(), windowId);
        this.te         = te;
        this.dataAccess = data;
        this.level      = playerInv.player.level();

        checkContainerSize(te, SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        // Center slot — accepts recipe seeds (Phase 1) and immature scars (Phase 2 input)
        this.addSlot(new Slot(te, CENTER_SLOT, 80, 40) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isCenterInput(stack);
            }
        });

        // Enzyme slots
        this.addSlot(new IncubatorCatalystSlot(te, ENZYME_SLOT_1, 80,  6));   // Top
        this.addSlot(new IncubatorCatalystSlot(te, ENZYME_SLOT_2, 114, 40));  // Right
        this.addSlot(new IncubatorCatalystSlot(te, ENZYME_SLOT_3, 80, 74));   // Bottom
        this.addSlot(new IncubatorCatalystSlot(te, ENZYME_SLOT_4, 46, 40));   // Left

        // Output slot
        this.addSlot(new FurnaceResultSlot(playerInv.player, te, OUTPUT_SLOT, 148, 40));

        // Blood flask input
        this.addSlot(new Slot(te, BLOOD_SLOT, 8, 74) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BloodyFlaskItem
                        || stack.getItem() instanceof BloodGourdItem;
            }
        });

        // Empty flask output
        this.addSlot(new Slot(te, FLASK_OUT_SLOT, 26, 74) {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }
        });

        // Player inventory
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 9; ++j)
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 124 + i * 18));

        // Player hotbar
        for (int k = 0; k < 9; ++k)
            this.addSlot(new Slot(playerInv, k, 8 + k * 18, 182));

        this.addDataSlots(data);
    }

    private boolean isCenterInput(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof ItemImmatureFungalScar) return true;
        return level.getRecipeManager()
                .getAllRecipesFor(RecipeInit.fungal_scar_cultivation_type.get())
                .stream()
                .anyMatch(holder -> stack.getItem() == holder.value().getSeedItemStack().getItem());
    }

    private static MycelialCrucibleBlockEntity getTE(Inventory inv, FriendlyByteBuf buf) {
        Objects.requireNonNull(inv);
        Objects.requireNonNull(buf);
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        if (be instanceof MycelialCrucibleBlockEntity te) return te;
        throw new IllegalStateException("Block entity is not a MycelialCrucibleBlockEntity: " + be);
    }

    // ── Data accessors ────────────────────────────────────────────────────────
    public int getCraftProgress()     { return dataAccess.get(0); }
    public int getCraftTotalTime()    { return dataAccess.get(1); }
    public int getMode()              { return dataAccess.get(2); }
    public int getMatureProgress()    { return dataAccess.get(3); }
    public int getMatureThreshold()   { return dataAccess.get(4); }

    /** Returns 0–24 scaled craft progress for the ring. */
    public int getScaledCraftProgress() {
        int p = getCraftProgress();
        int t = getCraftTotalTime();
        return t != 0 && p != 0 ? p * 24 / t : 0;
    }

    public boolean isCrafting()       { return getCraftProgress() > 0; }

    public MycelialCrucibleBlockEntity getTe() { return te; }

    @Override
    public void broadcastChanges() {
        te.sendUpdates();
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) { return te.stillValid(player); }

    // ── Quick move (shift-click) ──────────────────────────────────────────────
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return result;

        ItemStack slotStack = slot.getItem();
        result = slotStack.copy();

        if (index < SLOT_COUNT) {
            // Block → player
            if (!this.moveItemStackTo(slotStack, SLOT_COUNT, SLOT_COUNT + 36, true))
                return ItemStack.EMPTY;
            if (index == OUTPUT_SLOT) slot.onQuickCraft(slotStack, result);
        } else {
            // Player → block slots
            if (isCenterInput(slotStack)) {
                if (!this.moveItemStackTo(slotStack, CENTER_SLOT, CENTER_SLOT + 1, false))
                    return ItemStack.EMPTY;
            } else if (slotStack.getItem() instanceof BloodyFlaskItem
                    || slotStack.getItem() instanceof BloodGourdItem) {
                if (!this.moveItemStackTo(slotStack, BLOOD_SLOT, BLOOD_SLOT + 1, false))
                    return ItemStack.EMPTY;
            } else if (slotStack.getItem() instanceof EnzymeItem
                    || slotStack.getItem() instanceof RecycledEnzymeItem) {
                if (!this.moveItemStackTo(slotStack, ENZYME_SLOT_1, ENZYME_SLOT_4 + 1, false))
                    return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(slotStack, ENZYME_SLOT_1, ENZYME_SLOT_4 + 1, false)) {
                    if (index < SLOT_COUNT + 27) {
                        if (!this.moveItemStackTo(slotStack, SLOT_COUNT + 27, SLOT_COUNT + 36, false))
                            return ItemStack.EMPTY;
                    } else {
                        if (!this.moveItemStackTo(slotStack, SLOT_COUNT, SLOT_COUNT + 27, false))
                            return ItemStack.EMPTY;
                    }
                }
            }
        }

        if (slotStack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (slotStack.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, slotStack);
        return result;
    }
}

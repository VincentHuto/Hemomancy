package com.vincenthuto.hemomancy.common.menu.tile.crafting;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.tile.crafting.MycelialLanternBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class MycelialLanternMenu extends AbstractContainerMenu {

    public static final int CULTURE_SLOT = MycelialLanternBlockEntity.SLOT_CULTURE;
    public static final int BLOOD_SLOT = MycelialLanternBlockEntity.SLOT_BLOOD;
    public static final int OUTPUT_SLOT = MycelialLanternBlockEntity.SLOT_OUTPUT;
    public static final int EMPTY_CONTAINER_SLOT = MycelialLanternBlockEntity.SLOT_EMPTY_CONTAINER;
    public static final int SLOT_COUNT = MycelialLanternBlockEntity.INVENTORY_SIZE;
    public static final int DATA_COUNT = 4;

    private final MycelialLanternBlockEntity te;
    private final ContainerData dataAccess;
    private final Level level;

    public MycelialLanternMenu(int windowId, Inventory playerInv, FriendlyByteBuf data) {
        this(windowId, playerInv, getTE(playerInv, data));
    }

    public MycelialLanternMenu(int windowId, Inventory playerInv, MycelialLanternBlockEntity te) {
        this(windowId, playerInv, te, new SimpleContainerData(DATA_COUNT));
    }

    public MycelialLanternMenu(int windowId, Inventory playerInv, MycelialLanternBlockEntity te, ContainerData data) {
        super(ContainerInit.mycelial_lantern.get(), windowId);
        this.te = te;
        this.dataAccess = data;
        this.level = playerInv.player.level();

        checkContainerSize(te, SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.addSlot(new Slot(te, CULTURE_SLOT, 80, 44) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isCulture(stack);
            }
        });

        this.addSlot(new Slot(te, BLOOD_SLOT, 8, 84) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isBloodContainer(stack);
            }
        });

        this.addSlot(new FurnaceResultSlot(playerInv.player, te, OUTPUT_SLOT, 134, 48));

        this.addSlot(new Slot(te, EMPTY_CONTAINER_SLOT, 30, 84) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 116 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInv, k, 8 + k * 18, 174));
        }

        this.addDataSlots(data);
    }

    private static MycelialLanternBlockEntity getTE(Inventory inv, FriendlyByteBuf buf) {
        Objects.requireNonNull(inv);
        Objects.requireNonNull(buf);
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        if (be instanceof MycelialLanternBlockEntity te) return te;
        throw new IllegalStateException("Block entity is not a MycelialLanternBlockEntity: " + be);
    }

    private boolean isCulture(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return level.getRecipeManager().getAllRecipesFor(RecipeInit.enzyme_fruiting_type.get())
                .stream()
                .anyMatch(holder -> holder.value().matchesCulture(stack));
    }

    private boolean isBloodContainer(ItemStack stack) {
        return stack.getItem() instanceof BloodyFlaskItem || stack.getItem() instanceof BloodGourdItem;
    }

    public int getCraftProgress() {
        return dataAccess.get(0);
    }

    public int getCraftTotalTime() {
        return dataAccess.get(1);
    }

    public int getBloodVolume() {
        return dataAccess.get(2);
    }

    public int getMaxBloodVolume() {
        return dataAccess.get(3);
    }

    public int getScaledCraftProgress() {
        int total = getCraftTotalTime();
        int progress = getCraftProgress();
        return total != 0 && progress != 0 ? progress * 24 / total : 0;
    }

    public MycelialLanternBlockEntity getTe() {
        return te;
    }

    @Override
    public void broadcastChanges() {
        te.sendUpdates();
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return te.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return result;

        ItemStack slotStack = slot.getItem();
        result = slotStack.copy();

        if (index < SLOT_COUNT) {
            if (!this.moveItemStackTo(slotStack, SLOT_COUNT, SLOT_COUNT + 36, true)) return ItemStack.EMPTY;
            if (index == OUTPUT_SLOT) slot.onQuickCraft(slotStack, result);
        } else if (isCulture(slotStack)) {
            if (!this.moveItemStackTo(slotStack, CULTURE_SLOT, CULTURE_SLOT + 1, false)) return ItemStack.EMPTY;
        } else if (isBloodContainer(slotStack)) {
            if (!this.moveItemStackTo(slotStack, BLOOD_SLOT, BLOOD_SLOT + 1, false)) return ItemStack.EMPTY;
        } else if (index < SLOT_COUNT + 27) {
            if (!this.moveItemStackTo(slotStack, SLOT_COUNT + 27, SLOT_COUNT + 36, false)) return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(slotStack, SLOT_COUNT, SLOT_COUNT + 27, false)) {
            return ItemStack.EMPTY;
        }

        if (slotStack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (slotStack.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, slotStack);
        return result;
    }
}

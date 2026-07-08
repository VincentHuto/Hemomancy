package com.vincenthuto.hemomancy.common.menu;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipmentItemHandler;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFittingItem;
import com.vincenthuto.hemomancy.common.menu.slot.EquipmentArmorSlot;
import com.vincenthuto.hemomancy.common.menu.slot.ScarOffHandSlot;
import com.vincenthuto.hemomancy.common.menu.slot.SelectiveEquipmentTypeSlot;
import com.vincenthuto.hemomancy.common.menu.slot.VasculariumCharmSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HarbingerEquipmentMenu extends AbstractContainerMenu {

    public static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{
            InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
            InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};
    public final static int GOURD_SLOT_INDEX = 6;
    public final static int CHARM_SLOT_INDEX = 5;
    public final static int JAR_SLOT_INDEX = 7; // Index in the scar capability handler for the Morphling jar slot
    public final static int FITTING_SLOT_INDEX = 8;
    public final static int JAR_MENU_SLOT = 4;
    public final static int CHARM_MENU_SLOT = 5;
    public final static int GOURD_MENU_SLOT = 6;
    public final static int FITTING_MENU_SLOT = 7;
    private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD,
            EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final Player player;
    private final boolean openedFromScarletVanity;

    public IHarbingerEquipmentItemHandler equipment;

    public HarbingerEquipmentMenu(final int windowId, final Inventory playerInventory) {
        this(windowId, playerInventory.player.level(), playerInventory.player.blockPosition(), playerInventory,
                playerInventory.player, false);
    }

    public HarbingerEquipmentMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
        this(windowId, playerInventory.player.level(), playerInventory.player.blockPosition(), playerInventory,
                playerInventory.player, readOpenedFromScarletVanity(data));
    }

    public HarbingerEquipmentMenu(int windowId, Level world, BlockPos pos, Inventory playerInventory, Player playerEntity) {
        this(windowId, world, pos, playerInventory, playerEntity, false);
    }

    public HarbingerEquipmentMenu(int windowId, Level world, BlockPos pos, Inventory playerInventory, Player playerEntity,
            boolean openedFromScarletVanity) {
        super(ContainerInit.gourd_charm_inventory.get(), windowId);
        this.player = playerInventory.player;
        this.openedFromScarletVanity = openedFromScarletVanity;

        this.equipment = HemoCapabilityAccess.requireEquipment(this.player);

        for (int k = 0; k < 4; ++k) {
            final EquipmentSlot EquipmentSlot = VALID_EQUIPMENT_SLOTS[k];
            this.addSlot(new EquipmentArmorSlot(playerInventory, 36 + (3 - k), 18, 18 + k * 20, EquipmentSlot, this.player));
        }

//		this.addSlot(new SelectiveEquipmentTypeSlot(player, ItemFungalScar.class, scars, 0, 77, 8));
//		this.addSlot(new ScarSlot(player, scars, 1, 77 + 1 * 18, 8));
//		this.addSlot(new ScarSlot(player, scars, 2, 77 + 2 * 18, 8));
//		this.addSlot(new ScarSlot(player, scars, 3, 77 + 3 * 18, 8));
        this.addSlot(new SelectiveEquipmentTypeSlot(player, ItemMorphlingJar.class, equipment, JAR_SLOT_INDEX, 176, 14));
        this.addSlot(new VasculariumCharmSlot(player, equipment, CHARM_SLOT_INDEX, 176, 38,
                this.openedFromScarletVanity));
        this.addSlot(new SelectiveEquipmentTypeSlot(player, BloodGourdItem.class, equipment, GOURD_SLOT_INDEX, 176, 62));
        this.addSlot(new SelectiveEquipmentTypeSlot(player, LivingStaffFittingItem.class, equipment,
                FITTING_SLOT_INDEX, 176, 86));

        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 26 + j1 * 18, 116 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 26 + i1 * 18, 174));
        }

        this.addSlot(new ScarOffHandSlot(playerInventory, 40, 52, 78));
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        ItemStack originalStack = stackInSlot.copy();

        final int armorStart = 0;
        final int armorEnd = 3;
        final int jarSlotUI = JAR_MENU_SLOT;
        final int charmSlotUI = CHARM_MENU_SLOT;
        final int gourdSlotUI = GOURD_MENU_SLOT;
        final int fittingSlotUI = FITTING_MENU_SLOT;
        final int containerEnd = 8;
        final int playerInvStart = 8;
        final int hotbarStart = 35;
        final int hotbarEnd = 43;
        final int offhandSlot = 44;

        if (index == jarSlotUI || index == charmSlotUI || index == gourdSlotUI || index == fittingSlotUI) {
            if (!slot.mayPickup(playerIn)) {
                return ItemStack.EMPTY;
            }
            // ── Moving FROM a scar slot → player inventory (explicit, avoids offhand) ──
            // Handle scar slotItemHandler slots explicitly to prevent items landing in offhand.
            boolean placed = false;
            // Try hotbar first (prefer hotbar for quick access)
            for (int i = hotbarStart; i <= hotbarEnd; i++) {
                Slot target = this.slots.get(i);
                if (!target.hasItem() && target.mayPlace(stackInSlot)) {
                    target.set(stackInSlot.copy());
                    slot.set(ItemStack.EMPTY);
                    placed = true;
                    break;
                }
            }
            // Then try main inventory
            if (!placed) {
                for (int i = playerInvStart; i < hotbarStart; i++) {
                    Slot target = this.slots.get(i);
                    if (!target.hasItem() && target.mayPlace(stackInSlot)) {
                        target.set(stackInSlot.copy());
                        slot.set(ItemStack.EMPTY);
                        placed = true;
                        break;
                    }
                }
            }
            if (!placed) {
                return ItemStack.EMPTY;
            }
            return originalStack;
        } else if (index < containerEnd) {
            // ── Moving FROM a non-scar container slot → player inventory (exclude offhand) ──
            if (!this.moveItemStackTo(stackInSlot, playerInvStart, hotbarEnd + 1, true)) {
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

            // Living Staff fitting -> fitting slot
            if (!moved && stackInSlot.getItem() instanceof LivingStaffFittingItem) {
                Slot fittingSlot = this.slots.get(fittingSlotUI);
                if (!fittingSlot.hasItem() && fittingSlot.mayPlace(stackInSlot)) {
                    fittingSlot.set(stackInSlot.split(1));
                    moved = true;
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
    public boolean stillValid(Player p_38974_) {
        return this.player.inventoryMenu.stillValid(p_38974_);
    }

    public boolean isOpenedFromScarletVanity() {
        return openedFromScarletVanity;
    }

    private static boolean readOpenedFromScarletVanity(FriendlyByteBuf data) {
        return data != null && data.readableBytes() > 0 && data.readBoolean();
    }

}

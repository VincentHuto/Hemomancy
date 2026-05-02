package com.vincenthuto.hemomancy.common.menu;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.scar.IScarsItemHandler;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.menu.slot.ScarArmorSlot;
import com.vincenthuto.hemomancy.common.menu.slot.ScarOffHandSlot;
import com.vincenthuto.hemomancy.common.menu.slot.SelectiveScarTypeSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class HarbingerEquipmentMenu extends AbstractContainerMenu {

    public static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{
            InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
            InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};
    public final static int GOURD_SLOT_INDEX = 6;
    public final static int CHARM_SLOT_INDEX = 5;
    public final static int JAR_SLOT_INDEX = 7; // Index in the scar capability handler for the Morphling jar slot
    private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD,
            EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final CraftingContainer craftMatrix = new TransientCraftingContainer(this, 2, 2);
    private final ResultContainer craftResult = new ResultContainer();
    private final Player player;

    public IScarsItemHandler scars;

    public HarbingerEquipmentMenu(final int windowId, final Inventory playerInventory) {
        this(windowId, playerInventory.player.level(), playerInventory.player.blockPosition(), playerInventory,
                playerInventory.player);
    }

    public HarbingerEquipmentMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
        this(windowId, playerInventory);
    }

    public HarbingerEquipmentMenu(int windowId, Level world, BlockPos pos, Inventory playerInventory, Player playerEntity) {
        super(ContainerInit.gourd_charm_inventory.get(), windowId);
        this.player = playerInventory.player;

        this.scars = HemoCapabilityAccess.requireScars(this.player);

        this.addSlot(new ResultSlot(playerInventory.player, this.craftMatrix, this.craftResult, 0, 154, 28 + 26));

        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                this.addSlot(new Slot(this.craftMatrix, j + i * 2, 116 + j * 18 - 18, 18 + i * 18 + 26));
            }
        }

        for (int k = 0; k < 4; ++k) {
            final EquipmentSlot EquipmentSlot = VALID_EQUIPMENT_SLOTS[k];
            this.addSlot(new ScarArmorSlot(playerInventory, 36 + (3 - k), 8, 8 + k * 18, EquipmentSlot, this.player));
        }

//		this.addSlot(new SelectiveScarTypeSlot(player, ItemFungalScar.class, scars, 0, 77, 8));
//		this.addSlot(new ScarSlot(player, scars, 1, 77 + 1 * 18, 8));
//		this.addSlot(new ScarSlot(player, scars, 2, 77 + 2 * 18, 8));
//		this.addSlot(new ScarSlot(player, scars, 3, 77 + 3 * 18, 8));
        this.addSlot(new SelectiveScarTypeSlot(player, ItemMorphlingJar.class, scars, JAR_SLOT_INDEX, 77, 8));    // UI slot 9
        this.addSlot(new SelectiveScarTypeSlot(player, VasculariumCharmItem.class, scars, CHARM_SLOT_INDEX, 77, 26)); // UI slot 10
        this.addSlot(new SelectiveScarTypeSlot(player, BloodGourdItem.class, scars, GOURD_SLOT_INDEX, 77, 44));    // UI slot 11

        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }

        this.addSlot(new ScarOffHandSlot(playerInventory, 40, 96 - 19, 62));
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
        // 0        : result
        // 1-4      : craft grid (2x2)
        // 5-8      : armor (head, chest, legs, feet)
        // 9        : jar slot   (scar cap slot 7)
        // 10       : charm slot (scar cap slot 5)
        // 11       : gourd slot (scar cap slot 6)
        // 12-38    : player main inventory (27 slots)
        // 39-47    : hotbar (9 slots)
        // 48       : offhand
        final int armorStart = 5;
        final int armorEnd = 8;   // inclusive
        final int jarSlotUI = 9;
        final int charmSlotUI = 10;
        final int gourdSlotUI = 11;
        final int containerEnd = 12;  // first player-inv slot
        final int playerInvStart = 12;
        final int hotbarStart = 39;
        final int hotbarEnd = 47;  // inclusive
        final int offhandSlot = 48;

        if (index == jarSlotUI || index == charmSlotUI || index == gourdSlotUI) {
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
        if (!player.level().isClientSide) {
            ItemStack result = ItemStack.EMPTY;
            Level level = player.level();
            java.util.Optional<RecipeHolder<CraftingRecipe>> recipe = level.getServer().getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, this.craftMatrix.asCraftInput(), level);
            if (recipe.isPresent()) {
                RecipeHolder<CraftingRecipe> holder = recipe.get();
                this.craftResult.setRecipeUsed(holder);
                result = holder.value().assemble(this.craftMatrix.asCraftInput(), level.registryAccess());
                if (!result.isItemEnabled(level.enabledFeatures())) {
                    result = ItemStack.EMPTY;
                }
            }
            this.craftResult.setItem(0, result);
            this.setRemoteSlot(0, result);
        }
    }

    @Override
    public boolean stillValid(Player p_38974_) {
        return this.player.inventoryMenu.stillValid(p_38974_);
    }

}

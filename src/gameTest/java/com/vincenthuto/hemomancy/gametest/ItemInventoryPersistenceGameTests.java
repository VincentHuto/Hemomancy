package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.item.itemhandler.*;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.LivingSyringeMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;

@GameTestHolder("item_inventory_validation")
@PrefixGameTestTemplate(false)
public final class ItemInventoryPersistenceGameTests {
    @GameTest(template = "empty")
    public static void staffHasNoInventoryCapability(GameTestHelper helper) {
        var staff = new ItemStack(ItemInit.living_staff.get());
        helper.assertTrue(staff.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.ITEM) == null,
                "The Staff still exposes the obsolete morphling inventory");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void countOnlyReplacementPersists(GameTestHelper helper) {
        for (int family = 0; family < 3; family++) {
            var backing = new ItemStack(Items.STICK);
            var inventory = inventory(family, backing);
            inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 2));
            inventory.save();
            inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 5));
            inventory.save();
            helper.assertTrue(inventory(family, backing.copy()).getStackInSlot(0).getCount() == 5,
                    "Family " + family + " lost a count-only replacement");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void simulatedAndEmptyTransfersDoNotWrite(GameTestHelper helper) {
        for (int family = 0; family < 3; family++) {
            var backing = new ItemStack(Items.STICK);
            var inventory = inventory(family, backing);
            inventory.insertItem(0, new ItemStack(Items.DIAMOND), true);
            inventory.extractItem(0, 1, true);
            inventory.extractItem(0, 1, false);
            inventory.save();
            helper.assertTrue(!backing.has(DataComponents.CUSTOM_DATA), "Family " + family + " wrote an unchanged inventory");
            inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 2));
            inventory.save();
            var before = backing.get(DataComponents.CUSTOM_DATA);
            inventory.extractItem(0, 1, true);
            inventory.insertItem(0, new ItemStack(Items.DIAMOND), true);
            inventory.insertItem(0, new ItemStack(Items.EMERALD), false);
            inventory.save();
            helper.assertTrue(backing.get(DataComponents.CUSTOM_DATA) == before, "Family " + family + " rewrote unchanged contents");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyInventoryAndRegistryComponentsRoundTrip(GameTestHelper helper) {
        for (int family = 0; family < 3; family++) {
            var contents = new ItemStack(Items.DIAMOND_SWORD);
            contents.enchant(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                    .getHolderOrThrow(Enchantments.UNBREAKING), 2);
            contents.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Preserved"));
            var legacy = new ItemStackHandler(2);
            legacy.setStackInSlot(0, contents);
            var root = new CompoundTag();
            root.putString("Unrelated", "keep me");
            var saved = legacy.serializeNBT(helper.getLevel().registryAccess());
            saved.putInt("Size", 999);
            root.put("Inventory", saved);
            var backing = new ItemStack(Items.STICK);
            backing.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
            var inventory = inventory(family, backing);
            helper.assertTrue(inventory.getSlots() == 2, "Saved Size overrode family capacity");
            helper.assertTrue(ItemStack.matches(contents, inventory.getStackInSlot(0)), "Legacy components lost");
            inventory.setStackInSlot(1, new ItemStack(Items.EMERALD, 3));
            inventory.save();
            var reloaded = inventory(family, backing.copy());
            helper.assertTrue(ItemStack.matches(contents, reloaded.getStackInSlot(0)), "Registry components lost on save");
            helper.assertTrue(reloaded.getStackInSlot(1).getCount() == 3, "Second slot lost");
            helper.assertTrue(backing.get(DataComponents.CUSTOM_DATA).copyTag().getString("Unrelated").equals("keep me"), "Unrelated data lost");
            inventory.extractItem(1, 2, false);
            inventory.save();
            helper.assertTrue(inventory(family, backing).getStackInSlot(1).getCount() == 1, "Real extraction did not persist");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void explicitReloadClearsRemovedInventory(GameTestHelper helper) {
        for (int family = 0; family < 3; family++) {
            var backing = new ItemStack(Items.STICK);
            var inventory = inventory(family, backing);
            inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND));
            inventory.save();
            backing.remove(DataComponents.CUSTOM_DATA);
            inventory.load();
            inventory.save();
            helper.assertTrue(inventory.getStackInSlot(0).isEmpty(), "Family " + family + " retained stale contents");
            helper.assertTrue(!backing.has(DataComponents.CUSTOM_DATA), "Reload dirtied the inventory");
        }
        helper.succeed();
    }



    @GameTest(template = "empty")
    public static void inPlaceEditsAndLoadOncePreservePendingChanges(GameTestHelper helper) {
        for (int family = 0; family < 3; family++) {
            var backing = new ItemStack(Items.STICK);
            var inventory = inventory(family, backing);
            var handler = inventory;
            handler.setStackInSlot(0, new ItemStack(Items.DIAMOND, 2));
            handler.save();
            var held = handler.getStackInSlot(0);
            held.setCount(4);
            handler.setStackInSlot(0, held);
            handler.loadIfNotLoaded();
            handler.save();
            helper.assertTrue(inventory(family, backing).getStackInSlot(0).getCount() == 4,
                    "In-place count or loadIfNotLoaded discarded pending changes");
            held.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Changed"));
            handler.setDirty();
            handler.save();
            helper.assertTrue(ItemStack.matches(held, inventory(family, backing.copy()).getStackInSlot(0)),
                    "Explicit dirty marker did not preserve in-place component edits");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nestedInventoryRejectionAndJarIdentityStayUnchanged(GameTestHelper helper) {
        for (int family = 0; family < 3; family++) {
            var backing = new ItemStack(Items.STICK);
            var inventory = inventory(family, backing);
            var nested = new ItemStack(Items.DIAMOND);
            var root = new CompoundTag();
            root.put("Inventory", new CompoundTag());
            nested.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
            helper.assertTrue(inventory.insertItem(0, nested, false) == nested, "Nested NBT inventory accepted");
            var jar = new ItemStack(ItemInit.morphling_jar.get());
            helper.assertTrue(inventory.insertItem(0, jar, false) == jar, "Nested capability inventory accepted");
            inventory.save();
            helper.assertTrue(!backing.has(DataComponents.CUSTOM_DATA), "Rejected insertion wrote data");
        }
        var backing = new ItemStack(ItemInit.morphling_jar.get());
        var first = new MorphlingJarItemHandler(backing, 6);
        helper.assertTrue(first.hasSameBackingStack(new MorphlingJarItemHandler(backing, 6)), "Same jar identity lost");
        helper.assertTrue(!first.hasSameBackingStack(new MorphlingJarItemHandler(backing.copy(), 6)), "Copied jar shares identity");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void syringeMenuFlushesOnClose(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var syringe = new ItemStack(ItemInit.living_syringe.get());
        player.getInventory().setItem(player.getInventory().selected, syringe);
        var syringeMenu = new LivingSyringeMenu(2, player.getInventory());
        syringeMenu.handler.setStackInSlot(0, new ItemStack(Items.EMERALD));
        syringeMenu.removed(player);
        helper.assertTrue(inventory(0, syringe.copy()).getStackInSlot(0).is(Items.EMERALD), "Syringe menu did not save");
        helper.succeed();
    }

    private static StackBackedItemHandler inventory(int family, ItemStack backing) {
        StackBackedItemHandler inventory = switch (family) {
                        case 0 -> new LivingSyringeItemHandler(backing, 2);
            case 1 -> new MorphlingJarItemHandler(backing, 2);
            case 2 -> new ScarBinderItemHandler(backing, 2);
            default -> throw new IllegalArgumentException("Unknown family " + family);
        };
        inventory.load();
        return inventory;
    }
}

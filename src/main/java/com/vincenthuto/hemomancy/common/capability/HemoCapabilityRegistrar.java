package com.vincenthuto.hemomancy.common.capability;

import com.vincenthuto.hemomancy.common.capability.player.scar.IScar;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.scar.ItemScarBinder;
import com.vincenthuto.hemomancy.common.item.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.item.tool.living.LivingSyringeItem;
import com.vincenthuto.hemomancy.common.itemhandler.LivingStaffItemHandler;
import com.vincenthuto.hemomancy.common.itemhandler.LivingSyringeItemHandler;
import com.vincenthuto.hemomancy.common.itemhandler.MorphlingJarItemHandler;
import com.vincenthuto.hemomancy.common.itemhandler.ScarBinderItemHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

public final class HemoCapabilityRegistrar {
    private HemoCapabilityRegistrar() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(HemoCapabilityRegistrar::onRegisterCapabilities);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        // ── Player capabilities via attachment types ──
        event.registerEntity(HemoCapabilityKeys.BLOOD_VOLUME, Player.class,
                (player, ctx) -> player.getData(HemoAttachmentTypes.BLOOD_VOLUME));
        event.registerEntity(HemoCapabilityKeys.BLOOD_TENDENCY, Player.class,
                (player, ctx) -> player.getData(HemoAttachmentTypes.BLOOD_TENDENCY));
        event.registerEntity(HemoCapabilityKeys.VASCULAR_SYSTEM, Player.class,
                (player, ctx) -> player.getData(HemoAttachmentTypes.VASCULAR_SYSTEM));
        event.registerEntity(HemoCapabilityKeys.INITIATORY_DEGREE, Player.class,
                (player, ctx) -> player.getData(HemoAttachmentTypes.INITIATORY_DEGREE));
        event.registerEntity(HemoCapabilityKeys.UNSTAINED_PROGRESS, Player.class,
                (player, ctx) -> player.getData(HemoAttachmentTypes.UNSTAINED_PROGRESS));
        event.registerEntity(HemoCapabilityKeys.EQUIPPED_MORPHLING, Player.class,
                (player, ctx) -> player.getData(HemoAttachmentTypes.EQUIPPED_MORPHLING));
        event.registerEntity(HemoCapabilityKeys.KNOWN_MANIPULATIONS, Player.class,
                (player, ctx) -> player.getData(HemoAttachmentTypes.KNOWN_MANIPULATIONS));
        event.registerEntity(HemoCapabilityKeys.WHITE_HUMOR_VOLUME, Player.class,
                (player, ctx) -> player.getData(HemoAttachmentTypes.WHITE_HUMOR_VOLUME));
        event.registerEntity(HemoCapabilityKeys.VISCERAL_ORGANS, Player.class,
                (player, ctx) -> player.getData(HemoAttachmentTypes.VISCERAL_ORGANS));
        event.registerEntity(HemoCapabilityKeys.SCARS, Player.class,
                (player, ctx) -> player.getData(HemoAttachmentTypes.SCARS));

        // ── IScar item capabilities — register each item that implements IScar ──
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof IScar) {
                event.registerItem(HemoCapabilityKeys.ITEM_SCAR,
                        (stack, ctx) -> (IScar) stack.getItem(), item);
            }
        }

        // ── MorphlingJar IItemHandler capability ──
        event.registerItem(Capabilities.ItemHandler.ITEM,
                (stack, ctx) -> {
                    MorphlingJarItemHandler handler = new MorphlingJarItemHandler(stack, 6);
                    handler.loadIfNotLoaded();
                    return handler;
                },
                ItemInit.morphling_jar.get());

        // ── LivingStaff IItemHandler capability ──
        event.registerItem(Capabilities.ItemHandler.ITEM,
                (stack, ctx) -> {
                    LivingStaffItemHandler handler = new LivingStaffItemHandler(stack, 1);
                    handler.loadIfNotLoaded();
                    return handler;
                },
                ItemInit.living_staff.get());

        // ── ItemScarBinder IItemHandler capability ──
        event.registerItem(Capabilities.ItemHandler.ITEM,
                (stack, ctx) -> {
                    ScarBinderItemHandler handler = new ScarBinderItemHandler(stack, 18);
                    handler.loadIfNotLoaded();
                    return handler;
                },
                ItemInit.scar_binder.get());
        event.registerItem(Capabilities.ItemHandler.ITEM,
                (stack, ctx) -> {
                    ScarBinderItemHandler handler = new ScarBinderItemHandler(stack, 27);
                    handler.loadIfNotLoaded();
                    return handler;
                },
                ItemInit.scar_binder_upgraded.get());

        // ── LivingSyringe IItemHandler capability ──
        event.registerItem(Capabilities.ItemHandler.ITEM,
                (stack, ctx) -> {
                    LivingSyringeItemHandler handler = new LivingSyringeItemHandler(stack, 1);
                    handler.loadIfNotLoaded();
                    return handler;
                },
                ItemInit.living_syringe.get());

        // ── IItemHandler for WorldlyContainer block entities ──
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                BlockEntityInit.ghastly_alembic.get(),
                (be, facing) -> facing == null ? null : new SidedInvWrapper(be, facing));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                BlockEntityInit.pallid_retort.get(),
                (be, facing) -> facing == null ? null : new SidedInvWrapper(be, facing));
    }
}

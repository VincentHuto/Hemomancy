package com.vincenthuto.hemomancy.common.event.worldevent;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.worldgen.VesperOrdealManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

/**
 * Keeps players, placed blocks, and tossed items inside their owner's Chamber of
 * Will cell. V1 chambers are caster-only, so owner and occupant are the same
 * UUID, but the owner tag keeps the recovery path ready for future guests.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ChamberOfWillEvents {
    private static final String OWNER_KEY = "hemomancy:chamber_of_will_owner";

    private ChamberOfWillEvents() {}

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!level.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) return;

        ChamberOfWillManager manager = ChamberOfWillManager.get(player.getServer());
        BlockPos cell = manager.cellPos(manager.idFor(player.getUUID()));
        int radius = manager.radiusFor(player.getUUID());

        BlockPos pos = event.getPos();
        if (pos.getX() < cell.getX() - radius || pos.getX() > cell.getX() + radius
                || pos.getZ() < cell.getZ() - radius || pos.getZ() > cell.getZ() + radius
                || pos.getY() < cell.getY()) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("chat.hemomancy.chamber_of_will.block_out_of_bounds"), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) return;
        if (!ChamberPlayerSafeguardRules.shouldApply(player.isCreative(), player.isSpectator(),
                VesperOrdealManager.isActive(serverPlayer))) return;

        ChamberOfWillManager manager = ChamberOfWillManager.get(serverPlayer.getServer());
        BlockPos cell = manager.cellPos(manager.idFor(player.getUUID()));
        int radius = manager.radiusFor(player.getUUID());

        double minX = cell.getX() - radius + 0.5;
        double maxX = cell.getX() + radius + 0.5;
        double minZ = cell.getZ() - radius + 0.5;
        double maxZ = cell.getZ() + radius + 0.5;

        double px = player.getX();
        double pz = player.getZ();
        double py = player.getY();

        boolean changed = false;
        double nx = px, nz = pz;

        if (px < minX) { nx = minX; changed = true; }
        if (px > maxX) { nx = maxX; changed = true; }
        if (pz < minZ) { nz = minZ; changed = true; }
        if (pz > maxZ) { nz = maxZ; changed = true; }

        if (py < cell.getY() - 8) {
            ChamberOfWillManager.rescuePlayerToCell(serverPlayer, (ServerLevel) player.level(), cell, radius);
            return;
        }

        if (changed) {
            player.setDeltaMovement(Vec3.ZERO);
            serverPlayer.teleportTo(nx, py, nz);
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (event.getEntity() == null || event.getPlayer() == null) return;
        ItemEntity item = event.getEntity();
        if (item.level().isClientSide) return;
        if (!item.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) return;
        item.getPersistentData().putUUID(OWNER_KEY, event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item)) return;
        if (item.level().isClientSide) return;
        if (!item.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) return;

        ServerLevel level = (ServerLevel) item.level();
        CompoundTag data = item.getPersistentData();
        if (!data.contains(OWNER_KEY)) return;
        UUID ownerId = data.getUUID(OWNER_KEY);
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null) return;

        ChamberOfWillManager manager = ChamberOfWillManager.get(level.getServer());
        BlockPos cell = manager.cellPos(manager.idFor(owner.getUUID()));

        if (item.getY() < cell.getY() - 8) {
            ItemStack stack = item.getItem().copy();
            if (owner.getInventory().add(stack)) {
                item.discard();
                return;
            }
            item.setPos(owner.getX(), owner.getY() + 1.0, owner.getZ());
            item.setDefaultPickUpDelay();
            item.getPersistentData().putUUID(OWNER_KEY, owner.getUUID());
        }
    }
}

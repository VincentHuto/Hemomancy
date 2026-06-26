package com.vincenthuto.hemomancy.common.event.worldevent;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.PocketDimensionManager;
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
 * Prevents players from building or moving outside their pocket cell boundaries and
 * returns tossed items to their owner if they fall into the void.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class PocketDimensionEvents {
    private static final String OWNER_KEY = "hemomancy:owner";

    private PocketDimensionEvents() {}

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (!level.dimension().equals(PocketDimensionManager.POCKET_DIMENSION)) return;

        PocketDimensionManager manager = PocketDimensionManager.get(player.getServer());
        BlockPos cell = manager.cellPos(manager.idFor(player.getUUID()));

        BlockPos pos = event.getPos();
        int r = PocketDimensionManager.ROOM_RADIUS;
        if (pos.getX() < cell.getX() - r || pos.getX() > cell.getX() + r
                || pos.getZ() < cell.getZ() - r || pos.getZ() > cell.getZ() + r
                || pos.getY() < cell.getY()) {
            // Cancel placement outside the cell
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("chat.hemomancy.pocket.block_out_of_bounds"), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (player.isCreative() || player.isSpectator()) return;

        if (!player.level().dimension().equals(PocketDimensionManager.POCKET_DIMENSION)) return;

        PocketDimensionManager manager = PocketDimensionManager.get(serverPlayer.getServer());
        BlockPos cell = manager.cellPos(manager.idFor(player.getUUID()));

        double minX = cell.getX() - PocketDimensionManager.ROOM_RADIUS + 0.5;
        double maxX = cell.getX() + PocketDimensionManager.ROOM_RADIUS - 0.5+1;
        double minZ = cell.getZ() - PocketDimensionManager.ROOM_RADIUS + 0.5;
        double maxZ = cell.getZ() + PocketDimensionManager.ROOM_RADIUS - 0.5 +1;

        double px = player.getX();
        double pz = player.getZ();
        double py = player.getY();

        boolean changed = false;
        double nx = px, nz = pz;

        if (px < minX) { nx = minX; changed = true; }
        if (px > maxX) { nx = maxX; changed = true; }
        if (pz < minZ) { nz = minZ; changed = true; }
        if (pz > maxZ) { nz = maxZ; changed = true; }

        // If the player fell through the floor far below, snap them up into the cell
        if (py < cell.getY() - 8) {
            PocketDimensionManager.rescuePlayerToCell(serverPlayer, (ServerLevel) player.level(), cell);
            return;
        }

        if (changed) {
            // Stop motion and clamp position inside the cell so the player cannot move past the wall.
            player.setDeltaMovement(Vec3.ZERO);
            serverPlayer.teleportTo(nx, py, nz);
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (event.getEntity() == null || event.getPlayer() == null) return;
        ItemEntity item = event.getEntity();
        if (item.level().isClientSide) return;
        if (!item.level().dimension().equals(PocketDimensionManager.POCKET_DIMENSION)) return;
        CompoundTag data = item.getPersistentData();
        data.putUUID(OWNER_KEY, event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item)) return;
        if (item.level().isClientSide) return;
        if (!item.level().dimension().equals(PocketDimensionManager.POCKET_DIMENSION)) return;

        ServerLevel level = (ServerLevel) item.level();

        // find cell for the owner if available, otherwise skip
        CompoundTag data = item.getPersistentData();
        if (!data.contains(OWNER_KEY)) return;
        UUID ownerId = data.getUUID(OWNER_KEY);
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null) return;

        PocketDimensionManager manager = PocketDimensionManager.get(level.getServer());
        BlockPos cell = manager.cellPos(manager.idFor(owner.getUUID()));

        // If item fell far below the cell, return to owner.
        if (item.getY() < cell.getY() - 8) {
            ItemStack stack = item.getItem().copy();
            // Try to add to inventory
            if (owner.getInventory().add(stack)) {
                item.discard();
                return;
            }
            // If no room, teleport item to owner's feet so they can pick it up
            item.setPos(owner.getX(), owner.getY() + 1.0, owner.getZ());
            item.setDefaultPickUpDelay();
            // update persistent owner tag in case it was lost
            item.getPersistentData().putUUID(OWNER_KEY, owner.getUUID());
        }
    }
}

package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Recreates the "one shared void dimension sliced into far-apart per-player cells" design.
 * <p>
 * Collision avoidance: every player UUID is mapped to a sequential integer id; that id maps to a fixed
 * world position {@code (0, FLOOR_Y, POCKET_SPACING * id)}. Because each cell is {@link #POCKET_SPACING}
 * blocks apart along Z, players never overlap. A periodic {@link #tick(Level)} guard snaps stray players
 * back into their cell.
 * <p>
 * Persistence: this is a {@link SavedData} attached to the overworld's data storage, so the uuid -> id map,
 * the next id counter, and each player's return point all survive across sessions. Rooms are real blocks in
 * a saved world and are only generated once.
 */
public class PocketDimensionManager extends SavedData {
    public static final ResourceKey<Level> POCKET_DIMENSION = ResourceKey.create(Registries.DIMENSION, Hemomancy.rloc("pocket_dimension"));

    /** Distance between adjacent player cells along the Z axis. */
    public static final int POCKET_SPACING = 256;
    /** Y level the room floor is placed at. */
    public static final int FLOOR_Y = 2;
    /** Half-width of the room floor (room is (ROOM_RADIUS * 2 + 1) blocks square). */
    public static final int ROOM_RADIUS = 4;
    /** Interior height of the room. */
    public static final int ROOM_HEIGHT = 5;

    private static final String DATA_NAME = "isolatedpocketdimension_data";

    private int nextId = 0;
    private final Map<UUID, Integer> ids = new HashMap<>();
    private final Map<UUID, ReturnPoint> returnPoints = new HashMap<>();

    public PocketDimensionManager() {
    }

    public static PocketDimensionManager get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new Factory<>(PocketDimensionManager::new, PocketDimensionManager::load, null),
                DATA_NAME);
    }

    public static void teleport(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        get(server).teleportInternal(player);
    }

    private void teleportInternal(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        ServerLevel pocketLevel = server.getLevel(POCKET_DIMENSION);
        if (pocketLevel == null) {
            Hemomancy.LOGGER.error("Pocket dimension '{}' is not loaded; check the dimension json.", POCKET_DIMENSION.location());
            return;
        }

        if (player.level().dimension().equals(POCKET_DIMENSION)) {
            sendPlayerHome(server, player);
        } else {
            sendPlayerToPocket(player, pocketLevel);
        }
    }

    private void sendPlayerToPocket(ServerPlayer player, ServerLevel pocketLevel) {
        // Remember where the player came from so the return trip lands them back here.
        returnPoints.put(player.getUUID(), new ReturnPoint(
                player.level().dimension().location(),
                player.getX(), player.getY(), player.getZ(), player.getYRot()));
        setDirty();

        int id = idFor(player.getUUID());
        BlockPos cell = cellPos(id);
        generateRoom(pocketLevel, cell);

        Vec3 dest = new Vec3(cell.getX() + 0.5, cell.getY() + 1, cell.getZ() + 0.5);
        player.stopRiding();
        player.changeDimension(new DimensionTransition(pocketLevel, dest, Vec3.ZERO, player.getYRot(), player.getXRot(), DimensionTransition.DO_NOTHING));
    }

    private void sendPlayerHome(MinecraftServer server, ServerPlayer player) {
        ReturnPoint rp = returnPoints.get(player.getUUID());
        if (rp != null) {
            ServerLevel dest = server.getLevel(ResourceKey.create(Registries.DIMENSION, rp.dimension));
            if (dest != null) {
                player.stopRiding();
                player.changeDimension(new DimensionTransition(dest, new Vec3(rp.x, rp.y, rp.z), Vec3.ZERO, rp.yRot, player.getXRot(), DimensionTransition.DO_NOTHING));
                return;
            }
        }
        // Fallback: drop the player at the overworld spawn.
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        player.stopRiding();
        player.changeDimension(new DimensionTransition(overworld, new Vec3(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5), Vec3.ZERO, 0f, player.getXRot(), DimensionTransition.DO_NOTHING));
    }

    public int idFor(UUID uuid) {
        Integer id = ids.get(uuid);
        if (id == null) {
            id = nextId++;
            ids.put(uuid, id);
            setDirty();
        }
        return id;
    }

    public BlockPos cellPos(int id) {
        return new BlockPos(0, FLOOR_Y, POCKET_SPACING * id);
    }

    private void generateRoom(ServerLevel level, BlockPos center) {
        // Only build once: if the floor already exists this is a returning visit.
        BlockState existing = level.getBlockState(center);
        if (!existing.isAir() && !existing.is(Blocks.WATER)) {
            return;
        }

        BlockState wall = Blocks.AIR.defaultBlockState();
        BlockState floor =  BlockInit.blood_wood_planks.get().defaultBlockState();
        BlockState light = BlockInit.sporite_crystal.get().defaultBlockState();

        for (int x = -ROOM_RADIUS; x <= ROOM_RADIUS; x++) {
            for (int z = -ROOM_RADIUS; z <= ROOM_RADIUS; z++) {
                level.setBlockAndUpdate(center.offset(x, 0, z), floor);
                level.setBlockAndUpdate(center.offset(x, ROOM_HEIGHT, z), wall);
                boolean edge = Math.abs(x) == ROOM_RADIUS || Math.abs(z) == ROOM_RADIUS;
                if (edge) {
                    for (int y = 1; y < ROOM_HEIGHT; y++) {
                        level.setBlockAndUpdate(center.offset(x, y, z), wall);
                    }
                }
            }
        }

        int lit = ROOM_RADIUS - 1;
        level.setBlockAndUpdate(center.offset(lit, 0, lit), light);
        level.setBlockAndUpdate(center.offset(-lit, 0, lit), light);
        level.setBlockAndUpdate(center.offset(lit, 0, -lit), light);
        level.setBlockAndUpdate(center.offset(-lit, 0, -lit), light);
    }

    /**
     * Keeps non-creative players from wandering out of their cell into a neighbour's. Called every tick from
     * the level tick event, but only acts periodically and only inside the pocket dimension.
     */
    public static void tick(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!serverLevel.dimension().equals(POCKET_DIMENSION)) {
            return;
        }
        if (serverLevel.getGameTime() % 40 != 0) {
            return;
        }

        MinecraftServer server = serverLevel.getServer();
        PocketDimensionManager manager = get(server);
        for (ServerPlayer player : serverLevel.players()) {
            if (player.isCreative() || player.isSpectator()) {
                continue;
            }
            BlockPos cell = manager.cellPos(manager.idFor(player.getUUID()));
            int bound = ROOM_RADIUS + 1;
            boolean outOfBounds = player.getX() < cell.getX() - bound || player.getX() > cell.getX() + bound
                    || player.getZ() < cell.getZ() - bound || player.getZ() > cell.getZ() + bound
                    || player.getY() < cell.getY() - 8;
            if (outOfBounds) {
                rescuePlayerToCell(player, serverLevel, cell);
            }
        }
    }

    public static void rescuePlayerToCell(ServerPlayer player, ServerLevel level, BlockPos cell) {
        Vec3 destination = findSafeCellDestination(level, cell);
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        player.teleportTo(destination.x, destination.y, destination.z);
    }

    private static Vec3 findSafeCellDestination(ServerLevel level, BlockPos cell) {
        for (int radius = 0; radius <= ROOM_RADIUS; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) != radius) {
                        continue;
                    }
                    BlockPos floor = cell.offset(x, 0, z);
                    if (isSafeLanding(level, floor)) {
                        return new Vec3(floor.getX() + 0.5, floor.getY() + 1, floor.getZ() + 0.5);
                    }
                }
            }
        }

        // Last-resort recovery: if the whole cell floor is gone or blocked, rebuild one safe tile.
        level.setBlockAndUpdate(cell, Blocks.BONE_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(cell.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(cell.above(2), Blocks.AIR.defaultBlockState());
        return new Vec3(cell.getX() + 0.5, cell.getY() + 1, cell.getZ() + 0.5);
    }

    private static boolean isSafeLanding(ServerLevel level, BlockPos floor) {
        BlockPos feet = floor.above();
        BlockPos head = floor.above(2);
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(head).getCollisionShape(level, head).isEmpty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("nextId", nextId);

        ListTag idList = new ListTag();
        ids.forEach((uuid, id) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", uuid);
            entry.putInt("id", id);
            idList.add(entry);
        });
        tag.put("ids", idList);

        ListTag returnList = new ListTag();
        returnPoints.forEach((uuid, rp) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", uuid);
            entry.putString("dim", rp.dimension.toString());
            entry.putDouble("x", rp.x);
            entry.putDouble("y", rp.y);
            entry.putDouble("z", rp.z);
            entry.putFloat("yRot", rp.yRot);
            returnList.add(entry);
        });
        tag.put("returns", returnList);

        return tag;
    }

    public static PocketDimensionManager load(CompoundTag tag, HolderLookup.Provider registries) {
        PocketDimensionManager manager = new PocketDimensionManager();
        manager.nextId = tag.getInt("nextId");

        for (Tag t : tag.getList("ids", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            manager.ids.put(entry.getUUID("uuid"), entry.getInt("id"));
        }

        for (Tag t : tag.getList("returns", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            manager.returnPoints.put(entry.getUUID("uuid"), new ReturnPoint(
                    ResourceLocation.parse(entry.getString("dim")),
                    entry.getDouble("x"), entry.getDouble("y"), entry.getDouble("z"), entry.getFloat("yRot")));
        }

        return manager;
    }

    private record ReturnPoint(ResourceLocation dimension, double x, double y, double z, float yRot) {
    }
}

package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncChamberOfWill;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages The Chamber of Will as one shared void dimension sliced into far-apart
 * per-player cells. Each chamber keeps saved owner state so size and sky theme
 * can grow with Harbinger progression without changing the dimension layout.
 */
public class ChamberOfWillManager extends SavedData {
    public static final ResourceKey<Level> CHAMBER_OF_WILL = ResourceKey.create(
            Registries.DIMENSION, Hemomancy.rloc("chamber_of_will"));

    public static final ResourceLocation THEME_WILL_DEFAULT = Hemomancy.rloc("will_default");
    public static final ResourceLocation THEME_MNEMONIC_LOWTIDE = Hemomancy.rloc("mnemonic_lowtide");
    public static final ResourceLocation THEME_ARCHON_REVELATION = Hemomancy.rloc("archon_revelation");
    public static final ResourceLocation THEME_QLIPHOTH_COMMUNION = Hemomancy.rloc("qliphoth_communion");
    public static final ResourceLocation THEME_SILENT_ARCHON = Hemomancy.rloc("silent_archon");
    public static final ResourceLocation THEME_APOTHEOS = Hemomancy.rloc("apotheos");
    private static final List<ResourceLocation> ORDERED_SKY_THEMES = List.of(
            THEME_WILL_DEFAULT,
            THEME_MNEMONIC_LOWTIDE,
            THEME_ARCHON_REVELATION,
            THEME_QLIPHOTH_COMMUNION,
            THEME_SILENT_ARCHON,
            THEME_APOTHEOS);

    public static final int CHAMBER_SPACING = 256;
    public static final int FLOOR_Y = 2;
    public static final int BASE_ROOM_RADIUS = 4;
    public static final int ROOM_HEIGHT = 5;
    public static final int MAX_ROOM_TIER = 3;

    private static final String DATA_NAME = "chamber_of_will_data";
    private int nextId = 0;
    private final Map<UUID, Integer> ids = new HashMap<>();
    private final Map<UUID, ReturnPoint> returnPoints = new HashMap<>();
    private final Map<UUID, ChamberState> chamberStates = new HashMap<>();
    private final Map<UUID, ResourceLocation> skyThemeOverrides = new HashMap<>();

    public ChamberOfWillManager() {
    }

    public static ChamberOfWillManager get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new Factory<>(ChamberOfWillManager::new, ChamberOfWillManager::load, null),
                DATA_NAME);
    }

    public static void teleport(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        if (player.level().dimension().equals(CHAMBER_OF_WILL)) {
            get(server).exitChamber(player);
        } else {
            get(server).enterChamber(player);
        }
    }

    public static void enterRefuge(ServerPlayer player) {
        teleport(player);
    }

    public void enterChamber(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        ServerLevel chamberLevel = server.getLevel(CHAMBER_OF_WILL);
        if (chamberLevel == null) {
            Hemomancy.LOGGER.error("Chamber of Will dimension '{}' is not loaded; check the dimension json.",
                    CHAMBER_OF_WILL.location());
            return;
        }

        returnPoints.put(player.getUUID(), new ReturnPoint(
                player.level().dimension().location(),
                player.getX(), player.getY(), player.getZ(), player.getYRot()));
        refreshProgressionState(player);

        BlockPos cell = cellPos(idFor(player.getUUID()));
        ensureRoom(chamberLevel, player.getUUID());

        Vec3 dest = new Vec3(cell.getX() + 0.5, cell.getY() + 1, cell.getZ() + 0.5);
        player.stopRiding();
        player.changeDimension(new DimensionTransition(chamberLevel, dest, Vec3.ZERO,
                player.getYRot(), player.getXRot(), DimensionTransition.DO_NOTHING));
        syncTo(player);
        setDirty();
    }

    public void exitChamber(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        ReturnPoint rp = returnPoints.get(player.getUUID());
        if (rp != null) {
            ServerLevel dest = server.getLevel(ResourceKey.create(Registries.DIMENSION, rp.dimension));
            if (dest != null) {
                player.stopRiding();
                player.changeDimension(new DimensionTransition(dest, new Vec3(rp.x, rp.y, rp.z), Vec3.ZERO,
                        rp.yRot, player.getXRot(), DimensionTransition.DO_NOTHING));
                return;
            }
        }

        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        player.stopRiding();
        player.changeDimension(new DimensionTransition(overworld,
                new Vec3(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5),
                Vec3.ZERO, 0f, player.getXRot(), DimensionTransition.DO_NOTHING));
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
        return new BlockPos(0, FLOOR_Y, CHAMBER_SPACING * id);
    }

    public ChamberState getChamberState(UUID owner) {
        return chamberStates.computeIfAbsent(owner,
                ignored -> new ChamberState(0, THEME_WILL_DEFAULT));
    }

    public int radiusFor(UUID owner) {
        return radiusForTier(getChamberState(owner).tier());
    }

    public static int radiusForTier(int tier) {
        return BASE_ROOM_RADIUS + Math.max(0, Math.min(MAX_ROOM_TIER, tier)) * 2;
    }

    public static List<ResourceLocation> orderedSkyThemes() {
        return ORDERED_SKY_THEMES;
    }

    public static boolean isKnownSkyTheme(ResourceLocation theme) {
        return ORDERED_SKY_THEMES.contains(theme);
    }

    public ResourceLocation setSkyThemeOverride(ServerPlayer player, ResourceLocation skyTheme) {
        if (!isKnownSkyTheme(skyTheme)) {
            return null;
        }
        skyThemeOverrides.put(player.getUUID(), skyTheme);
        refreshProgressionState(player);
        syncTo(player);
        setDirty();
        return skyTheme;
    }

    public ResourceLocation cycleSkyThemeOverride(ServerPlayer player, int direction) {
        ResourceLocation current = skyThemeOverrides.getOrDefault(
                player.getUUID(), getChamberState(player.getUUID()).skyTheme());
        int currentIndex = ORDERED_SKY_THEMES.indexOf(current);
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        int nextIndex = Math.floorMod(currentIndex + direction, ORDERED_SKY_THEMES.size());
        return setSkyThemeOverride(player, ORDERED_SKY_THEMES.get(nextIndex));
    }

    public ResourceLocation clearSkyThemeOverride(ServerPlayer player) {
        skyThemeOverrides.remove(player.getUUID());
        refreshProgressionState(player);
        syncTo(player);
        setDirty();
        return getChamberState(player.getUUID()).skyTheme();
    }

    public void refreshProgressionState(ServerPlayer player) {
        ChamberState current = getChamberState(player.getUUID());
        ChamberState next = applySkyThemeOverride(player, progressionState(player));
        if (!current.equals(next)) {
            chamberStates.put(player.getUUID(), next);
            setDirty();
        }
    }

    private ChamberState applySkyThemeOverride(ServerPlayer player, ChamberState state) {
        ResourceLocation override = skyThemeOverrides.get(player.getUUID());
        if (override != null && isKnownSkyTheme(override)) {
            return new ChamberState(state.tier(), override);
        }
        return state;
    }

    private static ChamberState progressionState(ServerPlayer player) {
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
        boolean qliphothDone = HemoCapabilityAccess.getInitiatoryDegree(player)
                .map(deg -> deg.isQliphothCommunionDone() || deg.getTotalPomesConsumed() >= 9)
                .orElse(false);
        boolean qliphothStarted = qliphothDone
                || qliphothPomeCount(player) > 0
                || hasActiveQliphothBloom(player);
        String archonChoice = player.getPersistentData().getString(FungalGardenTravelHelper.ARCHON_CHOICE_KEY);

        if (degree >= 8) {
            return new ChamberState(3, THEME_APOTHEOS);
        }
        if (FungalGardenTravelHelper.ARCHON_CHOICE_SILENCE.equals(archonChoice) && degree >= 7) {
            return new ChamberState(2, THEME_SILENT_ARCHON);
        }
        if (qliphothStarted) {
            return new ChamberState(2, THEME_QLIPHOTH_COMMUNION);
        }
        if (degree >= 7) {
            return new ChamberState(1, THEME_ARCHON_REVELATION);
        }
        if (degree >= 6 && HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(player)) {
            return new ChamberState(1, THEME_MNEMONIC_LOWTIDE);
        }
        return new ChamberState(0, THEME_WILL_DEFAULT);
    }

    private static int qliphothPomeCount(ServerPlayer player) {
        return HemoCapabilityAccess.getInitiatoryDegree(player)
                .map(deg -> deg.isQliphothCommunionDone() ? 9 : deg.getTotalPomesConsumed())
                .orElse(0);
    }

    private static boolean hasActiveQliphothBloom(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        QliphothBloomSavedData data = QliphothBloomSavedData.get(server.overworld());
        UUID owner = player.getUUID();
        for (QliphothBloomSavedData.BloomEntry bloom : data.getBlooms()) {
            if (owner.equals(bloom.ownerUUID())) {
                return true;
            }
        }
        return false;
    }

    public void ensureRoom(ServerLevel level, UUID owner) {
        BlockPos center = cellPos(idFor(owner));
        int radius = radiusFor(owner);

        BlockState floor = BlockInit.blood_wood_planks.get().defaultBlockState();
        BlockState light = BlockInit.sporite_crystal.get().defaultBlockState();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos floorPos = center.offset(x, 0, z);
                BlockState existing = level.getBlockState(floorPos);
                if (existing.isAir() || existing.is(Blocks.WATER)) {
                    level.setBlockAndUpdate(floorPos, floor);
                }

                for (int y = 1; y <= ROOM_HEIGHT; y++) {
                    BlockPos airPos = center.offset(x, y, z);
                    if (level.getBlockState(airPos).is(Blocks.WATER)) {
                        level.setBlockAndUpdate(airPos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        int lit = Math.max(1, radius - 1);
        setLightIfReplaceable(level, center.offset(lit, 0, lit), light);
        setLightIfReplaceable(level, center.offset(-lit, 0, lit), light);
        setLightIfReplaceable(level, center.offset(lit, 0, -lit), light);
        setLightIfReplaceable(level, center.offset(-lit, 0, -lit), light);
    }

    private static void setLightIfReplaceable(ServerLevel level, BlockPos pos, BlockState light) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(BlockInit.blood_wood_planks.get()) || state.is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, light);
        }
    }

    public static void tick(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!serverLevel.dimension().equals(CHAMBER_OF_WILL)) {
            return;
        }
        if (serverLevel.getGameTime() % 40 != 0) {
            return;
        }

        MinecraftServer server = serverLevel.getServer();
        ChamberOfWillManager manager = get(server);
        for (ServerPlayer player : serverLevel.players()) {
            manager.refreshProgressionState(player);
            manager.ensureRoom(serverLevel, player.getUUID());
            manager.syncTo(player);

            if (player.isCreative() || player.isSpectator()) {
                continue;
            }
            BlockPos cell = manager.cellPos(manager.idFor(player.getUUID()));
            int bound = manager.radiusFor(player.getUUID()) + 1;
            boolean outOfBounds = player.getX() < cell.getX() - bound || player.getX() > cell.getX() + bound
                    || player.getZ() < cell.getZ() - bound || player.getZ() > cell.getZ() + bound
                    || player.getY() < cell.getY() - 8;
            if (outOfBounds) {
                rescuePlayerToCell(player, serverLevel, cell, manager.radiusFor(player.getUUID()));
            }
        }
    }

    public void syncTo(ServerPlayer player) {
        ChamberState state = getChamberState(player.getUUID());
        PacketHandler.sendToPlayer(player, new PacketSyncChamberOfWill(
                state.skyTheme(), state.tier(), radiusFor(player.getUUID()), qliphothPomeCount(player)));
    }

    public static void syncFor(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            get(server).syncTo(player);
        }
    }

    public static void rescuePlayerToCell(ServerPlayer player, ServerLevel level, BlockPos cell, int radius) {
        Vec3 destination = findSafeCellDestination(level, cell, radius);
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        player.teleportTo(destination.x, destination.y, destination.z);
    }

    private static Vec3 findSafeCellDestination(ServerLevel level, BlockPos cell, int maxRadius) {
        for (int radius = 0; radius <= maxRadius; radius++) {
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

        ListTag stateList = new ListTag();
        chamberStates.forEach((uuid, state) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", uuid);
            entry.putInt("tier", state.tier());
            entry.putString("skyTheme", state.skyTheme().toString());
            stateList.add(entry);
        });
        tag.put("states", stateList);

        ListTag overrideList = new ListTag();
        skyThemeOverrides.forEach((uuid, theme) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", uuid);
            entry.putString("skyTheme", theme.toString());
            overrideList.add(entry);
        });
        tag.put("skyThemeOverrides", overrideList);

        return tag;
    }

    public static ChamberOfWillManager load(CompoundTag tag, HolderLookup.Provider registries) {
        ChamberOfWillManager manager = new ChamberOfWillManager();
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

        for (Tag t : tag.getList("states", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            ResourceLocation theme = entry.contains("skyTheme")
                    ? ResourceLocation.parse(entry.getString("skyTheme"))
                    : THEME_WILL_DEFAULT;
            manager.chamberStates.put(entry.getUUID("uuid"),
                    new ChamberState(entry.getInt("tier"), theme));
        }

        for (Tag t : tag.getList("skyThemeOverrides", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            try {
                ResourceLocation theme = ResourceLocation.parse(entry.getString("skyTheme"));
                if (isKnownSkyTheme(theme)) {
                    manager.skyThemeOverrides.put(entry.getUUID("uuid"), theme);
                }
            } catch (RuntimeException ignored) {
            }
        }

        return manager;
    }

    public record ChamberState(int tier, ResourceLocation skyTheme) {
        public ChamberState {
            tier = Math.max(0, Math.min(MAX_ROOM_TIER, tier));
            if (skyTheme == null) {
                skyTheme = THEME_WILL_DEFAULT;
            }
        }
    }

    private record ReturnPoint(ResourceLocation dimension, double x, double y, double z, float yRot) {
    }
}

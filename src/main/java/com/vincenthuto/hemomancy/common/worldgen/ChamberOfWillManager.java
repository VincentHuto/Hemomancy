package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.client.particle.data.BloodCellData;
import com.vincenthuto.hemomancy.client.particle.data.SerpentParticleData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.entity.utility.ArborOfWillEntity;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncChamberOfWill;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncVesperFightScene;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hemomancy.common.mission.HarbingerChapterProgression;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public static final ResourceLocation THEME_VESPER_FIGHT = Hemomancy.rloc("vesper_fight");
    public static final ResourceLocation THEME_MYCOPHANT_NURSERY = Hemomancy.rloc("mycophant_nursery");
    private static final List<ResourceLocation> ORDERED_SKY_THEMES = List.of(
            THEME_WILL_DEFAULT,
            THEME_MNEMONIC_LOWTIDE,
            THEME_ARCHON_REVELATION,
            THEME_QLIPHOTH_COMMUNION,
            THEME_SILENT_ARCHON,
            THEME_APOTHEOS);
    private static final List<ResourceLocation> COMMAND_SKY_THEMES = List.of(
            THEME_WILL_DEFAULT,
            THEME_MNEMONIC_LOWTIDE,
            THEME_ARCHON_REVELATION,
            THEME_QLIPHOTH_COMMUNION,
            THEME_SILENT_ARCHON,
            THEME_APOTHEOS,
            THEME_VESPER_FIGHT,
            THEME_MYCOPHANT_NURSERY);

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
    private final Set<UUID> unrestrictedSkyThemeOverrides = new HashSet<>();
    private final Map<UUID, Integer> builtRadii = new HashMap<>();

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

		rememberReturnPoint(player);
        refreshProgressionState(player);

        BlockPos cell = cellPos(idFor(player.getUUID()));
        ensureRoom(chamberLevel, player.getUUID());

        Vec3 dest = arborEntryDestination(cell);
        player.stopRiding();
        player.changeDimension(new DimensionTransition(chamberLevel, dest, Vec3.ZERO,
                player.getYRot(), player.getXRot(), DimensionTransition.DO_NOTHING));
        syncTo(player);
        setDirty();
    }

	public void rememberReturnPoint(ServerPlayer player) {
		returnPoints.put(player.getUUID(), new ReturnPoint(
				player.level().dimension().location(),
				player.getX(), player.getY(), player.getZ(), player.getYRot()));
		setDirty();
	}

    public void exitChamber(ServerPlayer player) {
        MinecraftServer server = player.getServer();
		VesperOrdealManager.abandonAttempt(player);
        if (HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 6) {
            HarbingerAdvancementGranter.grantIfNotDone(player,
                    HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED);
            HarbingerChapterProgression.tryCompleteLivingCovenant(player);
        }
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
        return ChamberProgressionRules.radiusForTier(tier);
    }

    public static List<ResourceLocation> orderedSkyThemes() {
        return ORDERED_SKY_THEMES;
    }

    public static List<ResourceLocation> commandSkyThemes() {
        return COMMAND_SKY_THEMES;
    }

    public static boolean isKnownSkyTheme(ResourceLocation theme) {
        return COMMAND_SKY_THEMES.contains(theme);
    }

    public ResourceLocation setSkyThemeOverride(ServerPlayer player, ResourceLocation skyTheme) {
        if (!isKnownSkyTheme(skyTheme)) {
            return null;
        }
        skyThemeOverrides.put(player.getUUID(), skyTheme);
        unrestrictedSkyThemeOverrides.add(player.getUUID());
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
        unrestrictedSkyThemeOverrides.remove(player.getUUID());
        refreshProgressionState(player);
        syncTo(player);
        setDirty();
        return getChamberState(player.getUUID()).skyTheme();
    }

    public ProgressionRefresh refreshProgressionState(ServerPlayer player) {
        ChamberState current = getChamberState(player.getUUID());
        ChamberState next = applySkyThemeOverride(player, progressionState(player));
        ChamberProgressionRules.Refresh comparison = ChamberProgressionRules.compare(
                toRuleState(current), toRuleState(next));
        if (!current.equals(next)) {
            chamberStates.put(player.getUUID(), next);
            setDirty();
        }
        return new ProgressionRefresh(comparison.tierChanged(), comparison.radiusIncreased(),
                comparison.themeChanged());
    }

    private ChamberState applySkyThemeOverride(ServerPlayer player, ChamberState state) {
        ResourceLocation override = skyThemeOverrides.get(player.getUUID());
        if (override != null && isKnownSkyTheme(override)
                && (unrestrictedSkyThemeOverrides.contains(player.getUUID())
                || availableSkyThemes(player).contains(override))) {
            return new ChamberState(state.tier(), override);
        }
        if (override != null) {
            skyThemeOverrides.remove(player.getUUID());
            unrestrictedSkyThemeOverrides.remove(player.getUUID());
            setDirty();
        }
        return state;
    }

    private static ChamberState progressionState(ServerPlayer player) {
        ChamberProgressionRules.State state = ChamberProgressionRules.stateFor(progressionFacts(player));
        return new ChamberState(state.tier(), Hemomancy.rloc(state.theme()));
    }

    private static ChamberProgressionRules.Facts progressionFacts(ServerPlayer player) {
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
        boolean qliphothDone = HemoCapabilityAccess.getInitiatoryDegree(player)
                .map(deg -> deg.isQliphothCommunionDone() || deg.getTotalPomesConsumed() >= 9)
                .orElse(false);
        boolean qliphothStarted = qliphothDone
                || qliphothPomeCount(player) > 0
                || hasActiveQliphothBloom(player);
        EnumArchonPath archonPath = HemoCapabilityAccess.getInitiatoryDegree(player)
                .map(deg -> deg.getArchonPath())
                .orElse(EnumArchonPath.NONE);

        return new ChamberProgressionRules.Facts(degree,
                HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(player), qliphothStarted,
                archonPath == EnumArchonPath.SILENT_ARCHON);
    }

    public List<ResourceLocation> availableSkyThemes(ServerPlayer player) {
        return ChamberProgressionRules.availableThemes(progressionFacts(player)).stream()
                .map(Hemomancy::rloc)
                .toList();
    }

    public ResourceLocation cycleAvailableSkyTheme(ServerPlayer player) {
        List<ResourceLocation> available = availableSkyThemes(player);
        if (available.size() <= 1) return null;
        ResourceLocation current = getChamberState(player.getUUID()).skyTheme();
        int nextIndex = com.vincenthuto.hemomancy.common.event.worldevent.OrbOfPerspectiveRules
                .nextThemeIndex(available.indexOf(current), available.size());
        ResourceLocation next = available.get(nextIndex);
        skyThemeOverrides.put(player.getUUID(), next);
        unrestrictedSkyThemeOverrides.remove(player.getUUID());
        refreshProgressionState(player);
        syncTo(player);
        setDirty();
        return next;
    }

    private static ChamberProgressionRules.State toRuleState(ChamberState state) {
        return new ChamberProgressionRules.State(state.tier(), state.skyTheme().getPath());
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

    public RoomGrowth ensureRoom(ServerLevel level, UUID owner) {
        BlockPos center = cellPos(idFor(owner));
        int requestedRadius = radiusFor(owner);
        int previousRadius = builtRadii.getOrDefault(owner, -1);
        int radius = ChamberExpansionRules.nextBuiltRadius(previousRadius, requestedRadius);

        ensureArborOfWill(level, owner);
        if (radius <= previousRadius) {
            return RoomGrowth.NONE;
        }

        BlockState floor = BlockInit.blood_wood_planks.get().defaultBlockState();
        BlockState light = BlockInit.sporite_crystal.get().defaultBlockState();

        if (previousRadius >= BASE_ROOM_RADIUS) {
            for (ChamberExpansionRules.Offset offset : ChamberExpansionRules.markerOffsets(previousRadius)) {
                BlockPos marker = center.offset(offset.x(), 0, offset.z());
                BlockState state = level.getBlockState(marker);
                if (state.is(BlockInit.sporite_crystal.get())) {
                    level.setBlockAndUpdate(marker, floor);
                }
            }
        } else {
            for (int migratedRadius = BASE_ROOM_RADIUS; migratedRadius < radius; migratedRadius += 2) {
                for (ChamberExpansionRules.Offset offset : ChamberExpansionRules.markerOffsets(migratedRadius)) {
                    BlockPos marker = center.offset(offset.x(), 0, offset.z());
                    BlockState state = level.getBlockState(marker);
                    if (state.is(BlockInit.sporite_crystal.get())) {
                        level.setBlockAndUpdate(marker, floor);
                    }
                }
            }
        }

        for (ChamberExpansionRules.Offset offset : ChamberExpansionRules.floorBand(previousRadius, radius)) {
            BlockPos floorPos = center.offset(offset.x(), 0, offset.z());
            BlockState existing = level.getBlockState(floorPos);
            if (existing.isAir()) {
                level.setBlockAndUpdate(floorPos, floor);
            }
        }

        for (ChamberExpansionRules.Offset offset : ChamberExpansionRules.markerOffsets(radius)) {
            setLightIfReplaceable(level, center.offset(offset.x(), 0, offset.z()), light);
        }
        builtRadii.put(owner, radius);
        setDirty();
        return new RoomGrowth(previousRadius, radius);
    }

    /** Reconciles the owner's single persistent presentation anchor at the exact cell centre. */
    public ArborOfWillEntity ensureArborOfWill(ServerLevel level, UUID owner) {
        BlockPos center = cellPos(idFor(owner));
        Vec3 position = new Vec3(center.getX() + 0.5D, center.getY() + 1.0D, center.getZ() + 0.5D);
        AABB search = new AABB(position, position).inflate(3.0D, 2.0D, 3.0D);
        List<ArborOfWillEntity> owned = level.getEntitiesOfClass(ArborOfWillEntity.class, search,
                arbor -> arbor.ownerId().filter(owner::equals).isPresent());
        ArborOfWillEntity arbor = owned.stream().findFirst().orElse(null);
        for (int i = 1; i < owned.size(); i++) owned.get(i).discard();
        if (arbor == null) {
            arbor = EntityInit.arbor_of_will.get().create(level);
            if (arbor == null) throw new IllegalStateException("Unable to create Arbor of Will anchor");
            arbor.moveTo(position.x, position.y, position.z, 0.0F, 0.0F);
            level.addFreshEntity(arbor);
        } else if (arbor.position().distanceToSqr(position) > 0.01D) {
            arbor.teleportTo(position.x, position.y, position.z);
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player != null) arbor.configure(player, radiusFor(owner), qliphothPomeCount(player));
        return arbor;
    }

    private static Vec3 arborEntryDestination(BlockPos cell) {
        return new Vec3(cell.getX() + 0.5D, cell.getY() + 1.0D, cell.getZ() + 3.0D);
    }

    private static void setLightIfReplaceable(ServerLevel level, BlockPos pos, BlockState light) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(BlockInit.blood_wood_planks.get())) {
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
			if (MycophantEncounterManager.tickArenaPlayer(player, serverLevel)) {
				continue;
			}
			if (VesperOrdealManager.tickArenaPlayer(player, serverLevel)) {
				continue;
			}
            ProgressionRefresh refresh = manager.refreshProgressionState(player);
            RoomGrowth growth = manager.ensureRoom(serverLevel, player.getUUID());
            if (refresh.radiusIncreased() && growth.expandedFromExistingRoom()) {
                playExpansionEffects(serverLevel, manager.cellPos(manager.idFor(player.getUUID())), growth);
            }
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
        if (THEME_VESPER_FIGHT.equals(state.skyTheme())) {
            PacketHandler.sendToPlayer(player,
                    PacketSyncVesperFightScene.activate(cellPos(idFor(player.getUUID()))));
        } else if (!VesperOrdealManager.isActive(player)) {
            PacketHandler.sendToPlayer(player, PacketSyncVesperFightScene.clearScene());
        }
    }

    public static void syncFor(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            ChamberOfWillManager manager = get(server);
            ProgressionRefresh refresh = manager.refreshProgressionState(player);
            if (player.level() instanceof ServerLevel chamberLevel
                    && chamberLevel.dimension().equals(CHAMBER_OF_WILL)) {
                RoomGrowth growth = manager.ensureRoom(chamberLevel, player.getUUID());
                if (refresh.radiusIncreased() && growth.expandedFromExistingRoom()) {
                    playExpansionEffects(chamberLevel,
                            manager.cellPos(manager.idFor(player.getUUID())), growth);
                }
            }
            manager.syncTo(player);
        }
    }

    public static ProgressionRefresh refreshProgressionNow(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return ProgressionRefresh.NONE;
        ChamberOfWillManager manager = get(server);
        ProgressionRefresh refresh = manager.refreshProgressionState(player);
        if (!refresh.changed()) return refresh;

        if (refresh.radiusIncreased() && player.level() instanceof ServerLevel chamberLevel
                && chamberLevel.dimension().equals(CHAMBER_OF_WILL)) {
            RoomGrowth growth = manager.ensureRoom(chamberLevel, player.getUUID());
            if (growth.expandedFromExistingRoom()) {
                playExpansionEffects(chamberLevel,
                        manager.cellPos(manager.idFor(player.getUUID())), growth);
            }
        }
        manager.syncTo(player);
        return refresh;
    }

    private static void playExpansionEffects(ServerLevel level, BlockPos center, RoomGrowth growth) {
        int radius = growth.currentRadius();
        for (int step = -radius; step <= radius; step += 2) {
            double x = center.getX() + step + 0.5D;
            double z = center.getZ() + radius + 0.5D;
            level.sendParticles(new BloodCellData(131, 0, 0),
                    x, center.getY() + 0.25D, z, 1, 0.08D, 0.06D, 0.08D, 0.01D);
            level.sendParticles(new SerpentParticleData(70, 0, 12),
                    center.getX() + radius + 0.5D, center.getY() + 0.2D, center.getZ() + step + 0.5D,
                    1, 0.05D, 0.03D, 0.05D, 0.005D);
        }
        level.playSound(null, center, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.65F, 0.7F);
        level.playSound(null, center, SoundEvents.TUFF_PLACE, SoundSource.BLOCKS, 0.45F, 0.8F);
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

        ListTag unrestrictedOverrideList = new ListTag();
        for (UUID uuid : unrestrictedSkyThemeOverrides) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", uuid);
            unrestrictedOverrideList.add(entry);
        }
        tag.put("unrestrictedSkyThemeOverrides", unrestrictedOverrideList);

        ListTag builtRadiusList = new ListTag();
        builtRadii.forEach((uuid, radius) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", uuid);
            entry.putInt("radius", radius);
            builtRadiusList.add(entry);
        });
        tag.put("builtRadii", builtRadiusList);

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

        for (Tag t : tag.getList("unrestrictedSkyThemeOverrides", Tag.TAG_COMPOUND)) {
            manager.unrestrictedSkyThemeOverrides.add(((CompoundTag) t).getUUID("uuid"));
        }

        for (Tag t : tag.getList("builtRadii", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            manager.builtRadii.put(entry.getUUID("uuid"),
                    Math.max(BASE_ROOM_RADIUS, entry.getInt("radius")));
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

    public record ProgressionRefresh(boolean tierChanged, boolean radiusIncreased, boolean themeChanged) {
        public static final ProgressionRefresh NONE = new ProgressionRefresh(false, false, false);

        public boolean changed() {
            return tierChanged || themeChanged;
        }
    }

    public record RoomGrowth(int previousRadius, int currentRadius) {
        public static final RoomGrowth NONE = new RoomGrowth(BASE_ROOM_RADIUS, BASE_ROOM_RADIUS);

        public boolean expandedFromExistingRoom() {
            return previousRadius >= BASE_ROOM_RADIUS && currentRadius > previousRadius;
        }
    }

    private record ReturnPoint(ResourceLocation dimension, double x, double y, double z, float yRot) {
    }
}

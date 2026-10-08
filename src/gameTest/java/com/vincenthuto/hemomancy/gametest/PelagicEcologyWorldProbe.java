package com.vincenthuto.hemomancy.gametest;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import java.nio.file.*;
import java.util.*;

/** Native spawning in real seeded chunks, retaining cadence, mob caps and placement checks. */
public final class PelagicEcologyWorldProbe {
    private final GameTestHelper helper;
    private final ServerLevel level;
    private final List<PelagicWorldValidation.Site> sites;
    private final JsonArray results = new JsonArray();
    private FakePlayer observer;
    private int index, ticks, cohort;
    private final Set<UUID> seen = new HashSet<>();
    private final Map<String, Integer> arrivals = new TreeMap<>();
    private final Map<String, Integer> homeArrivals = new TreeMap<>();
    private final Map<String, Integer> neighborArrivals = new TreeMap<>();
    private final List<Integer> counts = new ArrayList<>();
    private final JsonArray reloads = new JsonArray();
    private Set<UUID> savedPopulation;
    private int reload, unloadWait;
    private PelagicEcologyWorldProbe(GameTestHelper helper, List<PelagicWorldValidation.Site> candidates) {
        this.helper = helper; level = helper.getLevel(); sites = new ArrayList<>();
        for (var layer : PelagicLayer.values()) {
            // Use the natural fossil and vent sites for substrate-dependent communities.
            var matching = candidates.stream().filter(s -> s.layer() == layer).toList();
            sites.add(layer == PelagicLayer.CARRION || layer == PelagicLayer.HYDROTHERMAL ? matching.getLast() : matching.getFirst());
        }
    }
    public static void start(GameTestHelper helper, List<PelagicWorldValidation.Site> sites) {
        new PelagicEcologyWorldProbe(helper, sites).visit();
    }
    private void visit() {
        if (index == sites.size()) { finish(); return; }
        var site = sites.get(index);
        arrivals.clear(); homeArrivals.clear(); neighborArrivals.clear(); counts.clear(); seen.clear(); ticks = cohort = 0;
        level.random.setSeed(level.getSeed() + index * 7919L);
        int observerY = switch (site.layer()) {
            case SHORE -> 64; case REEF, OPEN -> 46; case TWILIGHT -> 24; case MIDNIGHT -> 0; case CARRION -> -20; case HYDROTHERMAL -> -44;
        };
        observer = new FakePlayer(level, new GameProfile(UUID.nameUUIDFromBytes(("pelagic-" + index).getBytes(java.nio.charset.StandardCharsets.UTF_8)), "PelagicObserver"));
        observer.setGameMode(GameType.CREATIVE); observer.setNoGravity(true);
        observer.moveTo(site.x() + 48.5, observerY, site.z() + .5);
        level.addNewPlayer(observer);
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            level.setChunkForced((site.x() >> 4) + dx, (site.z() >> 4) + dz, true);
        LogUtils.getLogger().info("PELAGIC_ECOLOGY observing {} at {},{}", site.layer(), site.x(), site.z());
        helper.runAfterDelay(25, this::tick);
    }
    private AABB bounds(PelagicWorldValidation.Site site) { return new AABB(site.x()-56, -64, site.z()-56, site.x()+56, 80, site.z()+56); }
    private void tick() {
        try {
            var site = sites.get(index);
            var chunkMap = level.getChunkSource().chunkMap;
            helper.assertTrue(!chunkMap.getPlayersCloseForSpawning(new ChunkPos(site.x() >> 4, site.z() >> 4)).isEmpty(), "Observer must participate in native local mob caps");
            // One pass per tick, including vanilla's slower persistent-category cadence.
            {
                var state = NaturalSpawner.createState(289, level.getAllEntities(), (packed, consumer) -> {
                    var chunk = level.getChunkSource().getChunkNow(ChunkPos.getX(packed), ChunkPos.getZ(packed));
                    if (chunk != null) consumer.accept(chunk);
                }, new LocalMobCapCalculator(chunkMap));
                var spawningChunks = new ArrayList<net.minecraft.world.level.chunk.LevelChunk>();
                for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
                    spawningChunks.add(level.getChunk((site.x() >> 4) + dx, (site.z() >> 4) + dz));
                // ServerChunkCache shuffles before spawning; fixed order starves later habitat chunks.
                net.minecraft.Util.shuffle(spawningChunks, level.random);
                for (var chunk : spawningChunks)
                    NaturalSpawner.spawnForChunk(level, chunk, state, true, false, level.getGameTime() % 400 == 0);
            }
            var inhabitants = level.getEntitiesOfClass(Mob.class, bounds(site));
            for (var mob : inhabitants) if (seen.add(mob.getUUID())) {
                arrivals.merge(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString(), 1, Integer::sum);
                var species = species(mob);
                if (species != null) {
                    var actual = PelagicHabitat.layer(level, mob.blockPosition());
                    var target = actual == species.home ? homeArrivals : neighborArrivals;
                    target.merge(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath(), 1, Integer::sum);
                }
            }
            if (ticks % 200 == 0) counts.add(inhabitants.size());
            if (++ticks < 1000) { helper.runAfterDelay(1, this::tick); return; }
            // Independent native cohorts prevent an early full cap from hiding rarer home species.
            if (++cohort < 4 || cohort < 12 && !homeObserved(site.layer())) {
                inhabitants.forEach(Mob::discard); ticks = 0;
                helper.runAfterDelay(5, this::tick); return;
            }
            inspect(site, inhabitants);
            helper.assertTrue(homeObserved(site.layer()), "Missing natural home population " + site.layer() + ": " + homeArrivals);
            if (index == sites.size() - 1) { beginReload(inhabitants); return; }
            observer.discard();
            for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
                level.setChunkForced((site.x() >> 4) + dx, (site.z() >> 4) + dz, false);
            // Remove only this disposable probe's observed mobs so subsequent sites have a fresh global cap.
            inhabitants.forEach(Mob::discard);
            index++; helper.runAfterDelay(5, this::visit);
        } catch (Exception e) { throw new RuntimeException("Ecology probe " + index, e); }
    }
    private static PelagicHabitatRules.Species species(Mob mob) {
        return switch (BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath()) {
            case "chiton" -> PelagicHabitatRules.Species.CHITON;
            case "hemolymphopoda" -> PelagicHabitatRules.Species.HEMOLYMPHOPODA;
            case "barbed_urchin" -> PelagicHabitatRules.Species.URCHIN;
            case "pelagic_herring" -> PelagicHabitatRules.Species.HERRING;
            case "pyrosome" -> PelagicHabitatRules.Species.PYROSOME;
            case "mnemonic_whale" -> PelagicHabitatRules.Species.WHALE;
            case "prism_cuttle" -> PelagicHabitatRules.Species.CUTTLE;
            case "siphonophore" -> PelagicHabitatRules.Species.SIPHONOPHORE;
            case "blood_lantern_jelly" -> PelagicHabitatRules.Species.LANTERN;
            case "bloody_belly_comb_jelly" -> PelagicHabitatRules.Species.COMB_JELLY;
            case "hagfish" -> PelagicHabitatRules.Species.HAGFISH;
            case "chalybeate_snail" -> PelagicHabitatRules.Species.SNAIL;
            case "vampire_squid" -> PelagicHabitatRules.Species.VAMPIRE_SQUID;
            default -> null;
        };
    }
    private boolean homeObserved(PelagicLayer layer) {
        var expected = switch (layer) {
            case SHORE -> List.of("chiton", "hemolymphopoda");
            case REEF -> List.of("barbed_urchin");
            case OPEN -> List.of("pelagic_herring", "pyrosome", "mnemonic_whale");
            case TWILIGHT -> List.of("prism_cuttle", "siphonophore");
            case MIDNIGHT -> List.of("blood_lantern_jelly", "bloody_belly_comb_jelly");
            case CARRION -> List.of("hagfish");
            case HYDROTHERMAL -> List.of("chalybeate_snail");
        };
        return homeArrivals.keySet().containsAll(expected);
    }
    private List<Mob> reloadPopulation() {
        var site = sites.getLast();
        return level.getEntitiesOfClass(Mob.class, bounds(site)).stream().filter(m -> {
            var p = m.chunkPosition();
            return Math.abs(p.x - (site.x() >> 4)) <= 2 && Math.abs(p.z - (site.z() >> 4)) <= 2;
        }).toList();
    }
    private void beginReload(List<Mob> inhabitants) {
        // Freeze this disposable cohort only while measuring serialization, so migration is not counted as duplication.
        inhabitants.forEach(m -> { m.setNoAi(true); m.setNoGravity(true); m.setDeltaMovement(0, 0, 0); });
        savedPopulation = reloadPopulation().stream().map(Entity::getUUID).collect(java.util.stream.Collectors.toSet());
        observer.discard();
        unforce();
    }
    private void unforce() {
        var site = sites.getLast();
        for (int dx=-2; dx<=2; dx++) for (int dz=-2; dz<=2; dz++)
            level.setChunkForced((site.x() >> 4)+dx, (site.z() >> 4)+dz, false);
        level.getChunkSource().save(true);
        unloadWait = 0; helper.runAfterDelay(100, this::awaitUnloaded);
    }
    private void awaitUnloaded() {
        var site = sites.getLast(); boolean unloaded = true;
        for (int dx=-2; dx<=2; dx++) for (int dz=-2; dz<=2; dz++)
            unloaded &= level.getChunkSource().getChunkNow((site.x() >> 4)+dx, (site.z() >> 4)+dz) == null;
        if (!unloaded && ++unloadWait < 20) { helper.runAfterDelay(50, this::awaitUnloaded); return; }
        helper.assertTrue(unloaded, "Reload probe must actually unload all 25 vent chunks");
        for (int dx=-2; dx<=2; dx++) for (int dz=-2; dz<=2; dz++) {
            int x=(site.x() >> 4)+dx, z=(site.z() >> 4)+dz;
            level.setChunkForced(x,z,true); level.getChunk(x,z);
        }
        helper.runAfterDelay(100, () -> {
            var restored = reloadPopulation();
            var ids = restored.stream().map(Entity::getUUID).collect(java.util.stream.Collectors.toSet());
            helper.assertTrue(ids.equals(savedPopulation), "Chunk reload lost or accumulated inhabitants: before=" + savedPopulation.size() + " after=" + ids.size());
            var row = new JsonObject(); row.addProperty("cycle", ++reload); row.addProperty("confirmedUnloadedChunks", 25);
            row.addProperty("sameEntityUUIDs", true); row.addProperty("inhabitants", ids.size()); reloads.add(row); writeReport();
            if (reload < 3) unforce(); else finish();
        });
    }
    private void inspect(PelagicWorldValidation.Site site, List<Mob> inhabitants) {
        var row = new JsonObject(); row.addProperty("layer", site.layer().id); row.addProperty("x", site.x()); row.addProperty("z", site.z());
        row.add("arrivals", new Gson().toJsonTree(arrivals)); row.add("populationSamples", new Gson().toJsonTree(counts));
        row.add("homeArrivals", new Gson().toJsonTree(homeArrivals)); row.add("neighborArrivals", new Gson().toJsonTree(neighborArrivals)); row.addProperty("cohorts", cohort);
        row.add("categoryCounts", new Gson().toJsonTree(inhabitants.stream().collect(java.util.stream.Collectors.groupingBy(m -> m.getType().getCategory().getName(), java.util.stream.Collectors.counting()))));
        var persistent = inhabitants.stream().filter(Mob::isPersistenceRequired).count(); row.addProperty("persistentInhabitants", persistent);
        var growths = new TreeMap<String,Integer>(); var suitable = new TreeMap<String,Integer>();
        for (int x = site.x()-24; x < site.x()+24; x++) for (int z = site.z()-24; z < site.z()+24; z++) {
            for (int y = -53; y <= 73; y++) {
                var pos = new BlockPos(x,y,z); var block = level.getBlockState(pos);
                String id = BuiltInRegistries.BLOCK.getKey(block.getBlock()).toString();
                if (id.endsWith("hematic_algal_crust") || id.endsWith("tidepool_anemone") || id.endsWith("bone_worm_colony") || id.endsWith("giant_tube_worm_colony")) growths.merge(id,1,Integer::sum);
                if ((x & 1) != 0 || (z & 1) != 0 || !(block.isAir() || block.is(Blocks.WATER))) continue;
                for (var species : PelagicHabitatRules.Species.values())
                    if (species.home == site.layer() && PelagicHabitat.suitable(level,pos,species)) suitable.merge(species.name(),1,Integer::sum);
            }
        }
        row.add("growthBlocks", new Gson().toJsonTree(growths)); row.add("homeHabitatCandidates", new Gson().toJsonTree(suitable)); results.add(row);
        LogUtils.getLogger().info("PELAGIC_ECOLOGY {} arrivals={} growths={} habitats={} populations={}",site.layer(),arrivals,growths,suitable,counts);
        writeReport();
    }
    private void writeReport() {
        try {
            var report = new JsonObject(); report.addProperty("seed", level.getSeed());
            report.addProperty("method", "Native NaturalSpawner with native global/local caps, shuffled chunk order and cadence; one spawn pass per tick; 1000 ordinary AI ticks per independent cohort; at least four cohorts; no injected fauna. GameTest server runs ticks without real-time pacing. Final vent cohort frozen only for actual unload/reload identity checks.");
            report.add("sites", results);
            report.add("ventChunkReloads", reloads);
            Files.writeString(Path.of("pelagic-ecology-report.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    private void finish() { writeReport(); helper.succeed(); }
}

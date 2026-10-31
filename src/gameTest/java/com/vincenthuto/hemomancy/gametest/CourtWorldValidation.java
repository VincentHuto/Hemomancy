package com.vincenthuto.hemomancy.gametest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtLandmarkCells;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtPlacementRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtDiscoveryProgress;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatCandidates;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatSearch;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatLayout;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSite;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtWorldProbe;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLandmarks;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicTerrainSampler;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.neoforged.neoforge.gametest.GameTestHolder;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

/**
 * Brined Court placement on a fresh seeded noise Overworld: every Court start loaded from the sets' potential chunks
 * (structure-starts status only), checked against its house's biome and band, the cell and exclusion rules, the
 * landmark spacing and region budget; then a JFR-recorded run of full chunk generation along the sea.
 */
// The holder only puts the generator in the scan; NeoForge filters generated tests by their template namespace.
@GameTestHolder("court_worldgen_validation")
public final class CourtWorldValidation {
	/** Half-width of the square whose Court outposts and faction landmarks are all loaded and compared. */
	private static final int OUTPOST_HALF = 6144;
	/** Seats are sparse; their potential chunks are loaded over a wider square. */
	private static final int SEAT_HALF = 8192;
	/** Further out, Seats are found by the predicate alone and then loaded to confirm. */
	private static final int SEAT_SEARCH_HALF = 32768;
	private static final int REGION = 512;
	private static final int COURT_PER_REGION = 2;
	private static final int LANDMARKS_PER_COURT_REGION = 4;
	private static final double COURT_WORKER_SHARE = .02;
	private static final long SEAT_SEARCH_MILLIS = 2000;
	private static final int SEAT_SEARCH_RINGS = 8;
	/** Track 5's budget for the Relay Chart's off-thread search from the origin. */
	private static final long CHART_SEARCH_MILLIS = 2000;

	private record Landmark(ResourceLocation id, ChunkPos start, BlockPos centre, @Nullable CourtSite site) {
	}

	private CourtWorldValidation() {
	}

	@GameTestGenerator
	public static Collection<TestFunction> tests() {
		if (System.getProperty("hemomancy.court.validationSeed") == null) return List.of();
		return List.of(new TestFunction("court_worldgen", "court_placement", "court_worldgen_validation:room",
				12000, 0, true, CourtWorldValidation::inspect));
	}

	private static void inspect(GameTestHelper h) {
		try {
			run(h);
		} catch (Exception e) {
			throw new RuntimeException("Court worldgen validation failed", e);
		}
	}

	private static void run(GameTestHelper h) throws Exception {
		ServerLevel level = h.getLevel();
		var chunkSource = level.getChunkSource();
		h.assertTrue(PelagicWorldgen.context(chunkSource.randomState()) != null,
				"Pelagic hooks must be installed in the real Overworld noise pipeline");
		long seed = level.getSeed();
		h.assertTrue(seed == Long.getLong("hemomancy.court.validationSeed", 42), "Requested seed must be used");
		boolean reverse = System.getProperty("hemomancy.court.validationOrder", "forward").equals("reverse");
		var probe = CourtWorldProbe.of(level.registryAccess(), chunkSource.getGenerator().getBiomeSource(),
				chunkSource.randomState(), seed);
		var sets = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
		var structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
		var seats = (RandomSpreadStructurePlacement) sets.getOrThrow(key(CourtSeatCandidates.SEATS)).placement();
		var outposts = (RandomSpreadStructurePlacement) sets.getOrThrow(key(CourtSeatCandidates.OUTPOSTS)).placement();
		var factionPlacements = new ArrayList<RandomSpreadStructurePlacement>();
		var factionStructures = new HashSet<ResourceLocation>();
		for (var id : CourtLandmarkCells.EXCLUDED_SETS) {
			var set = sets.get(id);
			if (set == null || !(set.placement() instanceof RandomSpreadStructurePlacement spread)) continue;
			factionPlacements.add(spread);
			set.structures().forEach(entry -> factionStructures.add(entry.structure().unwrapKey().orElseThrow().location()));
		}

		var report = new JsonObject();
		report.addProperty("seed", seed);
		report.addProperty("order", reverse ? "reverse" : "forward");

		// Every chunk where a Court or faction start can exist.
		var chunks = new LinkedHashSet<ChunkPos>();
		addPotential(chunks, seats, seed, SEAT_HALF);
		addPotential(chunks, outposts, seed, OUTPOST_HALF);
		for (var placement : factionPlacements) addPotential(chunks, placement, seed, OUTPOST_HALF + 256);
		var order = new ArrayList<>(chunks);
		if (reverse) Collections.reverse(order);
		var landmarks = new LinkedHashMap<String, Landmark>();
		long scanStart = System.nanoTime();
		for (var chunk : order) collect(level, probe, structures, chunk, factionStructures, landmarks);
		report.addProperty("structureStartChunks", order.size());
		report.addProperty("structureStartSeconds", (System.nanoTime() - scanStart) / 1e9);

		// Seats past the loaded square: the predicate finds them, the real start confirms.
		for (var seat : List.of(CourtSite.LIGHTHOUSE_GARDEN, CourtSite.VENT_SANCTUM)) {
			if (landmarks.values().stream().anyMatch(l -> l.site() == seat)) continue;
			var predicted = nearestPredicted(probe, seats, seed, seat, SEAT_SEARCH_HALF);
			if (predicted != null) collect(level, probe, structures, predicted, factionStructures, landmarks);
		}

		var court = landmarks.values().stream().filter(l -> l.site() != null).toList();
		var factions = landmarks.values().stream().filter(l -> l.site() == null).toList();
		var sites = new JsonArray();
		for (var landmark : court) sites.add(checkSite(h, level, probe, seed, landmark));
		report.add("courtSites", sites);
		report.addProperty("factionLandmarks", factions.size());

		report.add("funnel", funnel(probe, outposts, seats, seed));
		Files.writeString(Path.of("court-report.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));

		var perSite = new EnumMap<CourtSite, Integer>(CourtSite.class);
		court.forEach(l -> perSite.merge(l.site(), 1, Integer::sum));
		report.add("perSite", new GsonBuilder().create().toJsonTree(perSite));
		h.assertTrue(perSite.getOrDefault(CourtSite.LIGHTHOUSE_GARDEN, 0) > 0, "No Lighthouse Garden found");
		h.assertTrue(perSite.getOrDefault(CourtSite.VENT_SANCTUM, 0) > 0, "No Vent Sanctum found");
		long outpostKinds = perSite.keySet().stream().filter(s -> s.tier() == CourtSite.Tier.OUTPOST).count();
		h.assertTrue(outpostKinds >= 3, "Expected at least three outpost kinds, found " + perSite);

		checkOneOutpostPerCell(h, court, outposts);
		checkSeatExclusion(h, court, seats, seed);
		report.add("spacing", checkSpacing(h, court, factions));
		report.add("regions", checkRegions(h, court, factions));
		report.add("seatSearch", checkSeatSearch(h, level, court));
		report.add("chartSearch", checkChartSearch(h, level));
		report.add("discoveryBounds", checkDiscoveryBounds(h, level, court));
		report.add("controls", checkControls(h, level, probe, landmarks, seats, outposts, seed));
		report.add("travel", travel(h, level, court));
		report.add("seats", CourtSeatValidation.check(h, level, probe, structures, court.stream()
				.filter(l -> l.site().tier() == CourtSite.Tier.SEAT)
				.map(l -> new CourtSeatValidation.Seat(l.site(), l.start(), l.centre(), l.id())).toList()));
		var nearestSeats = new ArrayList<BlockPos>();
		for (var seat : List.of(CourtSite.LIGHTHOUSE_GARDEN, CourtSite.VENT_SANCTUM))
			court.stream().filter(l -> l.site() == seat).map(Landmark::centre)
					.min(Comparator.comparingLong(p -> (long) p.getX() * p.getX() + (long) p.getZ() * p.getZ()))
					.ifPresent(nearestSeats::add);
		report.add("outposts", CourtOutpostValidation.check(h, level, structures, court.stream()
				.filter(l -> l.site().tier() == CourtSite.Tier.OUTPOST)
				.map(l -> new CourtOutpostValidation.Outpost(l.site(), l.start(), l.centre(), l.id())).toList(), nearestSeats));

		Files.writeString(Path.of("court-report.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
		LogUtils.getLogger().info("COURT validation passed for seed {} ({}): {}", seed, reverse ? "reverse" : "forward", perSite);
		h.succeed();
	}

	private static net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.StructureSet> key(
			ResourceLocation id) {
		return net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE_SET, id);
	}

	private static void addPotential(Set<ChunkPos> chunks, RandomSpreadStructurePlacement placement, long seed, int half) {
		int spacing = placement.spacing();
		int min = Math.floorDiv(Math.floorDiv(-half, 16), spacing), max = Math.floorDiv(Math.floorDiv(half, 16), spacing);
		for (int cx = min; cx <= max; cx++) for (int cz = min; cz <= max; cz++) {
			var chunk = placement.getPotentialStructureChunk(seed, cx * spacing, cz * spacing);
			if (Math.abs(chunk.getMiddleBlockX()) <= half && Math.abs(chunk.getMiddleBlockZ()) <= half) chunks.add(chunk);
		}
	}

	private static void collect(ServerLevel level, CourtWorldProbe probe, net.minecraft.core.Registry<Structure> structures,
			ChunkPos chunk, Set<ResourceLocation> factionStructures, Map<String, Landmark> landmarks) {
		var access = level.getChunk(chunk.x, chunk.z, ChunkStatus.STRUCTURE_STARTS, true);
		for (Map.Entry<Structure, StructureStart> entry : access.getAllStarts().entrySet()) {
			StructureStart start = entry.getValue();
			if (!start.isValid()) continue;
			var id = structures.getKey(entry.getKey());
			if (id == null) continue;
			CourtSite site = entry.getKey() instanceof CourtStructure court ? court.site() : null;
			if (site == null && !factionStructures.contains(id)) continue;
			BlockPos centre;
			if (site != null) {
				// The marker, read back from the start piece's frame (CourtSeatLayout): every site is centred on its
				// marker column; the Garden and the Tide Refuge are pinned to the sea surface, so their marker is the
				// floor under that column.
				var box = start.getPieces().get(0).getBoundingBox();
				int x = (box.minX() + box.maxX()) / 2, z = (box.minZ() + box.maxZ()) / 2;
				centre = switch (site) {
					case VENT_SANCTUM -> CourtSeatLayout.sanctumMarker(box);
					case LIGHTHOUSE_GARDEN, TIDE_REFUGE -> new BlockPos(x, probe.column(x, z).floorY() + 1, z);
					default -> CourtSeatLayout.outpostCentre(site, box);
				};
				if (site == CourtSite.LIGHTHOUSE_GARDEN && box.minY() != CourtSeatLayout.GARDEN_WATERLINE_Y - CourtSeatLayout.GARDEN_DEPTH)
					throw new IllegalStateException("The Lighthouse Garden must be pinned to the sea surface: " + box);
				if (site == CourtSite.TIDE_REFUGE && box.minY() != CourtSeatLayout.GARDEN_WATERLINE_Y - CourtSeatLayout.REFUGE_DEPTH)
					throw new IllegalStateException("The Tide Refuge must be pinned to the sea surface: " + box);
			} else {
				centre = start.getBoundingBox().getCenter();
			}
			landmarks.putIfAbsent(id + "@" + chunk, new Landmark(id, chunk, centre, site));
		}
	}

	@Nullable
	private static ChunkPos nearestPredicted(CourtWorldProbe probe, RandomSpreadStructurePlacement seats, long seed,
			CourtSite site, int half) {
		int spacing = seats.spacing();
		int max = Math.floorDiv(Math.floorDiv(half, 16), spacing);
		for (int ring = 0; ring <= max; ring++) {
			for (int cx = -ring; cx <= ring; cx++) for (int cz = -ring; cz <= ring; cz++) {
				if (Math.max(Math.abs(cx), Math.abs(cz)) != ring) continue;
				var chunk = seats.getPotentialStructureChunk(seed, cx * spacing, cz * spacing);
				if (CourtPlacementRules.stub(site, probe, seed, chunk.getMiddleBlockX(), chunk.getMiddleBlockZ()).isPresent())
					return chunk;
			}
		}
		return null;
	}

	private static JsonObject checkSite(GameTestHelper h, ServerLevel level, CourtWorldProbe probe, long seed, Landmark site) {
		var marker = site.centre();
		String where = site.site().id() + " at " + marker.toShortString();
		var predicted = CourtPlacementRules.stub(site.site(), probe, seed, site.start().getMiddleBlockX(),
				site.start().getMiddleBlockZ());
		h.assertTrue(predicted.isPresent() && predicted.get().equals(marker),
				"The predicate must predict the generated marker: " + where + " vs " + predicted);
		h.assertTrue(CourtPlacementRules.markerLayer(site.site(), probe.layerAt(marker.getX(), marker.getY(), marker.getZ()))
						&& CourtPlacementRules.markerLayer(site.site(), probe.layerAt(marker.getX(), marker.getY() + 1, marker.getZ())),
				"Marker must stand in its house's biome and band: " + where);
		var biome = level.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(
				net.minecraft.core.QuartPos.fromBlock(marker.getX()), net.minecraft.core.QuartPos.fromBlock(marker.getY()),
				net.minecraft.core.QuartPos.fromBlock(marker.getZ()), level.getChunkSource().randomState().sampler());
		h.assertTrue(CourtPlacementRules.markerLayer(site.site(), CourtWorldProbe.layerOf(biome)), "Biome source disagrees at " + where);
		var column = probe.column(marker.getX(), marker.getZ());
		h.assertTrue(column.influence() >= .999 && marker.getY() == column.floorY() + 1, "Marker must rest on the floor: " + where);
		boolean cold = probe.coldSurface(marker.getX(), marker.getZ());
		if (site.site().climate() == CourtSite.SurfaceClimate.COLD_OR_FROZEN) h.assertTrue(cold, "Still Bell under warm water: " + where);
		if (site.site().climate() == CourtSite.SurfaceClimate.TEMPERATE_OR_WARM) h.assertTrue(!cold, "Screened Cleft under cold water: " + where);
		if (site.site() == CourtSite.VENT_SANCTUM) {
			var vent = probe.terrain().ventSite(Math.floorDiv(marker.getX(), PelagicTerrainSampler.VENT_CELL),
					Math.floorDiv(marker.getZ(), PelagicTerrainSampler.VENT_CELL));
			h.assertTrue(vent.x() == marker.getX() && vent.z() == marker.getZ()
							&& PelagicLandmarks.hostsVentField(vent, probe.column(vent.x(), vent.z())),
					"Vent Sanctum must sit on a seeded vent-field centre: " + where);
		}
		var json = new JsonObject();
		json.addProperty("site", site.site().id());
		json.addProperty("x", marker.getX());
		json.addProperty("y", marker.getY());
		json.addProperty("z", marker.getZ());
		json.addProperty("coldSurface", cold);
		return json;
	}

	/** How many potential cells each rule step keeps, per site: habitat, screen (floor and footprint), then exclusion. */
	private static JsonObject funnel(CourtWorldProbe probe, RandomSpreadStructurePlacement outposts,
			RandomSpreadStructurePlacement seats, long seed) {
		var json = new JsonObject();
		for (var site : CourtSite.values()) {
			var cells = new LinkedHashSet<ChunkPos>();
			addPotential(cells, site.tier() == CourtSite.Tier.SEAT ? seats : outposts, seed,
					site.tier() == CourtSite.Tier.SEAT ? SEAT_HALF : OUTPOST_HALF);
			int habitat = 0, screened = 0, kept = 0;
			for (var chunk : cells) {
				int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
				if (site != CourtSite.VENT_SANCTUM && !CourtPlacementRules.habitat(site, probe, x, z)) continue;
				habitat++;
				if (CourtPlacementRules.screen(site, probe, seed, x, z).isEmpty()) continue;
				screened++;
				if (CourtPlacementRules.stub(site, probe, seed, x, z).isPresent()) kept++;
			}
			var row = new JsonObject();
			if (site == CourtSite.WAKE_NURSERY) row.add("habitatCells", nurseryCells(probe, cells));
			row.addProperty("cells", cells.size());
			row.addProperty("habitat", habitat);
			row.addProperty("screened", screened);
			row.addProperty("kept", kept);
			json.add(site.id(), row);
		}
		return json;
	}

	/** Each nursery habitat cell's centre floor and the shallowest floor its search grid reaches, for tuning. */
	private static JsonArray nurseryCells(CourtWorldProbe probe, java.util.Set<ChunkPos> cells) {
		var array = new JsonArray();
		for (var chunk : cells) {
			int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
			if (!CourtPlacementRules.habitat(CourtSite.WAKE_NURSERY, probe, x, z)) continue;
			int best = Integer.MIN_VALUE;
			int r = CourtPlacementRules.NURSERY_SEARCH_RADIUS;
			for (int dx = -r; dx <= r; dx += 16) for (int dz = -r; dz <= r; dz += 16) {
				if (!CourtPlacementRules.habitat(CourtSite.WAKE_NURSERY, probe, x + dx, z + dz)) continue;
				best = Math.max(best, probe.column(x + dx, z + dz).floorY());
			}
			var cell = new JsonObject();
			cell.addProperty("x", x);
			cell.addProperty("z", z);
			cell.addProperty("floor", probe.column(x, z).floorY());
			cell.addProperty("influence", probe.column(x, z).influence());
			cell.addProperty("shallowestInSearch", best);
			cell.addProperty("screened", CourtPlacementRules.screen(CourtSite.WAKE_NURSERY, probe, probe.seed(), x, z).isPresent());
			array.add(cell);
		}
		return array;
	}

	private static void checkOneOutpostPerCell(GameTestHelper h, List<Landmark> court, RandomSpreadStructurePlacement outposts) {
		var cells = new HashMap<Long, Landmark>();
		for (var landmark : court) {
			if (landmark.site().tier() != CourtSite.Tier.OUTPOST) continue;
			long cell = ChunkPos.asLong(Math.floorDiv(landmark.start().x, outposts.spacing()),
					Math.floorDiv(landmark.start().z, outposts.spacing()));
			var other = cells.put(cell, landmark);
			h.assertTrue(other == null, "Two outposts in one cell: " + landmark + " and " + other);
		}
	}

	private static void checkSeatExclusion(GameTestHelper h, List<Landmark> court, RandomSpreadStructurePlacement seats, long seed) {
		int spacing = seats.spacing();
		for (var landmark : court) {
			if (landmark.site().tier() != CourtSite.Tier.OUTPOST) continue;
			var start = landmark.start();
			for (int cx = Math.floorDiv(start.x - 6, spacing); cx <= Math.floorDiv(start.x + 6, spacing); cx++)
				for (int cz = Math.floorDiv(start.z - 6, spacing); cz <= Math.floorDiv(start.z + 6, spacing); cz++) {
					var seat = seats.getPotentialStructureChunk(seed, cx * spacing, cz * spacing);
					h.assertTrue(Math.max(Math.abs(seat.x - start.x), Math.abs(seat.z - start.z)) > 6,
							"Outpost inside the Seat exclusion zone: " + landmark);
				}
		}
	}

	private static JsonObject checkSpacing(GameTestHelper h, List<Landmark> court, List<Landmark> factions) {
		double nearest = Double.MAX_VALUE;
		int pairs = 0;
		var all = new ArrayList<Landmark>(court);
		all.addAll(factions);
		for (var a : court) {
			// Faction landmarks are only complete inside the outpost square.
			if (Math.max(Math.abs(a.centre().getX()), Math.abs(a.centre().getZ())) > OUTPOST_HALF - 128) continue;
			for (var b : all) {
				if (a == b) continue;
				double distance = Math.hypot(a.centre().getX() - b.centre().getX(), a.centre().getZ() - b.centre().getZ());
				nearest = Math.min(nearest, distance);
				pairs++;
				h.assertTrue(distance >= CourtPlacementRules.LANDMARK_RADIUS,
						"Landmarks closer than 96 blocks: " + a + " and " + b + " (" + Math.round(distance) + ")");
			}
		}
		var json = new JsonObject();
		json.addProperty("pairsChecked", pairs);
		json.addProperty("nearestCourtPair", nearest == Double.MAX_VALUE ? -1 : Math.round(nearest));
		return json;
	}

	private static JsonObject checkRegions(GameTestHelper h, List<Landmark> court, List<Landmark> factions) {
		var courtCounts = new HashMap<Long, Integer>();
		var allCounts = new HashMap<Long, Integer>();
		for (var landmark : court) {
			if (Math.max(Math.abs(landmark.centre().getX()), Math.abs(landmark.centre().getZ())) > OUTPOST_HALF) continue;
			courtCounts.merge(region(landmark), 1, Integer::sum);
		}
		for (var landmark : factions) allCounts.merge(region(landmark), 1, Integer::sum);
		int maxCourt = 0, maxAll = 0;
		for (var entry : courtCounts.entrySet()) {
			int total = entry.getValue() + allCounts.getOrDefault(entry.getKey(), 0);
			maxCourt = Math.max(maxCourt, entry.getValue());
			maxAll = Math.max(maxAll, total);
			h.assertTrue(entry.getValue() <= COURT_PER_REGION, "Too many Court sites in region " + ChunkPos.getX(entry.getKey())
					+ "," + ChunkPos.getZ(entry.getKey()) + ": " + entry.getValue());
			h.assertTrue(total <= LANDMARKS_PER_COURT_REGION, "Too many landmarks in Court region "
					+ ChunkPos.getX(entry.getKey()) + "," + ChunkPos.getZ(entry.getKey()) + ": " + total);
		}
		var json = new JsonObject();
		json.addProperty("courtRegions", courtCounts.size());
		json.addProperty("maxCourtPerRegion", maxCourt);
		json.addProperty("maxLandmarksPerCourtRegion", maxAll);
		return json;
	}

	private static long region(Landmark landmark) {
		return ChunkPos.asLong(Math.floorDiv(landmark.centre().getX(), REGION), Math.floorDiv(landmark.centre().getZ(), REGION));
	}

	/**
	 * Brined Court Track 5: the Relay Chart's search finishes within its budget on this fresh seed, on the background
	 * pool rather than the server thread. The test itself blocks the server thread while it waits, so only the
	 * off-thread stage is awaited (the hand-back to the server thread would wait behind this test).
	 */
	private static JsonObject checkChartSearch(GameTestHelper h, ServerLevel level) throws Exception {
		long start = System.nanoTime();
		var found = CourtSeatSearch.offThread(level, BlockPos.ZERO).get(CHART_SEARCH_MILLIS, java.util.concurrent.TimeUnit.MILLISECONDS);
		long millis = (System.nanoTime() - start) / 1_000_000;
		Thread searched = CourtSeatSearch.lastSearchThread();
		h.assertTrue(searched != null && searched != level.getServer().getRunningThread(),
				"The chart search must not run on the server thread: " + searched);
		h.assertTrue(millis <= CHART_SEARCH_MILLIS, "Chart search took " + millis + " ms");
		var json = new JsonObject();
		json.addProperty("millis", millis);
		json.addProperty("rings", CourtSeatSearch.MAX_RINGS);
		json.addProperty("thread", searched.getName());
		found.ifPresent(pos -> {
			json.addProperty("x", pos.getX());
			json.addProperty("z", pos.getZ());
		});
		return json;
	}

	/** Track 5: the discovery tick's structure lookup sees a player standing in each kind of generated Court site. */
	private static JsonObject checkDiscoveryBounds(GameTestHelper h, ServerLevel level, List<Landmark> court) {
		var json = new JsonObject();
		var seen = EnumSet.noneOf(CourtSite.class);
		for (var landmark : court) {
			if (!seen.add(landmark.site())) continue;
			var access = level.getChunk(landmark.start().x, landmark.start().z, ChunkStatus.STRUCTURE_STARTS, true);
			BlockPos inside = access.getAllStarts().entrySet().stream()
					.filter(entry -> entry.getKey() instanceof CourtStructure && entry.getValue().isValid())
					.map(entry -> entry.getValue().getPieces().get(0).getBoundingBox().getCenter())
					.findFirst().orElse(null);
			h.assertTrue(inside != null, "No Court start at " + landmark.start());
			boolean found = level.structureManager().getStructureWithPieceAt(inside, CourtDiscoveryProgress.COURT).isValid();
			h.assertTrue(found, "The Court tag lookup must see a player inside " + landmark.id() + " at " + inside);
			json.addProperty(landmark.site().name().toLowerCase(Locale.ROOT), inside.toShortString());
		}
		return json;
	}

	private static JsonObject checkSeatSearch(GameTestHelper h, ServerLevel level, List<Landmark> court) {
		long start = System.nanoTime();
		var found = CourtSeatCandidates.nearest(level, BlockPos.ZERO, SEAT_SEARCH_RINGS);
		long millis = (System.nanoTime() - start) / 1_000_000;
		h.assertTrue(millis <= SEAT_SEARCH_MILLIS, "Seat search took " + millis + " ms");
		var json = new JsonObject();
		json.addProperty("millis", millis);
		json.addProperty("rings", SEAT_SEARCH_RINGS);
		if (found.isPresent()) {
			var pos = found.get();
			json.addProperty("x", pos.getX());
			json.addProperty("z", pos.getZ());
			if (Math.max(Math.abs(pos.getX()), Math.abs(pos.getZ())) <= SEAT_HALF - 1024) {
				h.assertTrue(court.stream().anyMatch(l -> l.site() == CourtSite.LIGHTHOUSE_GARDEN && l.centre().equals(pos)),
						"Seat search must name a generated Lighthouse Garden: " + pos);
			}
		}
		return json;
	}

	private static JsonObject checkControls(GameTestHelper h, ServerLevel level, CourtWorldProbe probe,
			Map<String, Landmark> landmarks, RandomSpreadStructurePlacement seats, RandomSpreadStructurePlacement outposts,
			long seed) {
		var occupied = new HashMap<ChunkPos, Landmark>();
		landmarks.values().stream().filter(l -> l.site() != null).forEach(l -> occupied.put(l.start(), l));
		var potential = new LinkedHashSet<ChunkPos>();
		addPotential(potential, seats, seed, SEAT_HALF);
		addPotential(potential, outposts, seed, OUTPOST_HALF);
		var surfaces = level.getChunkSource().getGenerator().getBiomeSource();
		int inland = 0;
		for (var chunk : potential) {
			int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
			if (!inland(probe, surfaces, level, x, z)) continue;
			// A Seat may anchor up to SEAT_REACH from an inland cell centre, but its marker must stand in the sea.
			var site = occupied.get(chunk);
			h.assertTrue(site == null || !inland(probe, surfaces, level, site.centre().getX(), site.centre().getZ()),
					"Inland Court site: " + site);
			inland++;
		}
		h.assertTrue(inland >= 10, "Need at least ten inland control cells, found " + inland);

		ServerLevel nether = level.getServer().getLevel(Level.NETHER);
		int netherChecked = 0;
		if (nether != null) {
			var netherSource = nether.getChunkSource();
			h.assertTrue(CourtWorldProbe.of(nether.registryAccess(), netherSource.getGenerator().getBiomeSource(),
					netherSource.randomState(), nether.getSeed()) == null, "The Nether must not install the Court probe");
			int count = 0;
			for (var chunk : potential) {
				if (count++ >= 24) break;
				var access = nether.getChunk(chunk.x, chunk.z, ChunkStatus.STRUCTURE_STARTS, true);
				for (var structure : access.getAllStarts().keySet())
					h.assertTrue(!(structure instanceof CourtStructure), "Court site generated in the Nether at " + chunk);
				netherChecked++;
			}
		}
		h.assertTrue(netherChecked > 0, "Other-dimension control did not run");
		var json = new JsonObject();
		json.addProperty("inlandCells", inland);
		json.addProperty("netherChunks", netherChecked);
		return json;
	}

	private static boolean inland(CourtWorldProbe probe, net.minecraft.world.level.biome.BiomeSource surfaces,
			ServerLevel level, int x, int z) {
		if (probe.column(x, z).influence() != 0) return false;
		var surface = surfaces.getNoiseBiome(net.minecraft.core.QuartPos.fromBlock(x), net.minecraft.core.QuartPos.fromBlock(63),
				net.minecraft.core.QuartPos.fromBlock(z), level.getChunkSource().randomState().sampler());
		return !surface.is(BiomeTags.IS_OCEAN) && !surface.is(BiomeTags.IS_BEACH);
	}

	/**
	 * Full chunk generation along a straight line through the sea, about one chunk column at a time, recorded with JFR.
	 * The Court's placement code must take a small share of the generation workers' samples.
	 */
	private static JsonObject travel(GameTestHelper h, ServerLevel level, List<Landmark> court) throws Exception {
		int seconds = Integer.getInteger("hemomancy.court.travelSeconds", 45);
		var outposts = court.stream().filter(l -> l.site().tier() == CourtSite.Tier.OUTPOST)
				.sorted(Comparator.comparingLong(l -> (long) l.centre().getX() * l.centre().getX()
						+ (long) l.centre().getZ() * l.centre().getZ())).toList();
		var from = outposts.isEmpty() ? BlockPos.ZERO : outposts.get(0).centre();
		int startX = (from.getX() >> 4) - 24, laneZ = from.getZ() >> 4;
		Path recordingFile = Path.of("court-travel.jfr");
		int columns = 0, generated = 0;
		try (var recording = new jdk.jfr.Recording()) {
			recording.enable("jdk.ExecutionSample").withPeriod(Duration.ofMillis(10)).withStackTrace();
			recording.start();
			long deadline = System.nanoTime() + seconds * 1_000_000_000L;
			while (System.nanoTime() < deadline) {
				int cx = startX + columns++;
				for (int dz = -3; dz <= 3; dz++) {
					level.getChunk(cx, laneZ + dz);
					generated++;
				}
			}
			recording.stop();
			recording.dump(recordingFile);
		}
		int workerSamples = 0, courtSamples = 0, structureSamples = 0;
		var threads = new TreeMap<String, Integer>();
		for (var event : jdk.jfr.consumer.RecordingFile.readAllEvents(recordingFile)) {
			if (!event.getEventType().getName().equals("jdk.ExecutionSample")) continue;
			var thread = event.getThread("sampledThread");
			String name = thread == null ? "?" : thread.getJavaName();
			threads.merge(name == null ? "?" : name.replaceAll("\\d+$", "#"), 1, Integer::sum);
			if (name == null || !name.startsWith("Worker-Main")) continue;
			workerSamples++;
			var stack = event.getStackTrace();
			if (stack == null) continue;
			boolean courtFrame = false, structureFrame = false;
			for (var frame : stack.getFrames()) {
				String type = frame.getMethod().getType().getName();
				if (type.startsWith("com.vincenthuto.hemomancy.common.worldgen.structure.court.")) courtFrame = true;
				if (frame.getMethod().getName().equals("createStructures")) structureFrame = true;
			}
			if (courtFrame) courtSamples++;
			if (structureFrame) structureSamples++;
		}
		double share = workerSamples == 0 ? 0 : courtSamples / (double) workerSamples;
		h.assertTrue(workerSamples > 100, "The travel recording captured too few worker samples: " + workerSamples);
		h.assertTrue(share <= COURT_WORKER_SHARE, "Court placement took " + Math.round(share * 1000) / 10.0
				+ "% of generation-worker samples");
		// Seats and outposts are checked piece by piece in CourtSeatValidation and CourtOutpostValidation.
		int markersPlaced = 0;
		for (var landmark : court) {
			var marker = landmark.centre();
			if (landmark.site().tier() != CourtSite.Tier.OUTPOST) continue;
			if (!level.hasChunk(marker.getX() >> 4, marker.getZ() >> 4)) continue;
			markersPlaced++;
		}
		var json = new JsonObject();
		json.addProperty("seconds", seconds);
		json.addProperty("columns", columns);
		json.addProperty("chunksRequested", generated);
		json.addProperty("laneZ", laneZ * 16);
		json.addProperty("workerSamples", workerSamples);
		json.addProperty("courtSamples", courtSamples);
		json.addProperty("structureStartSamples", structureSamples);
		json.addProperty("courtShare", share);
		json.addProperty("markersPlacedInFullChunks", markersPlaced);
		json.add("samplesByThread", new GsonBuilder().create().toJsonTree(threads));
		return json;
	}
}

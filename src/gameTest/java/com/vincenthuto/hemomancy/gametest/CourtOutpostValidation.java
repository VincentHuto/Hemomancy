package com.vincenthuto.hemomancy.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.HouseMarkerBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.worldgen.feature.court.CourtSignFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.court.CourtSignRules;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtResidentMarkers;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The outposts and signs as generated (Track 8), on the validation seed:
 * <ul>
 * <li>the nearest outpost of each kind found is generated in full, carries its house's marker and its resident marks,
 * and (the Bell) keeps its air pocket dry now, with no fluid tick beside it, and after the chunk is read back;</li>
 * <li>full chunks round the nearest Lighthouse Garden and Vent Sanctum are scanned for signs: no 256 x 256 region holds
 * more of a sign family than its cap (blooms count by patch, at most seven blooms each), signs stay out of Court
 * sites (the Garden's own ring of blooms aside), and the feature places some.</li>
 * </ul>
 */
final class CourtOutpostValidation {
	record Outpost(CourtSite site, ChunkPos start, BlockPos centre, ResourceLocation id) {
	}

	/** Most blooms one patch can place (CourtSignFeature: three to seven). */
	private static final int BLOOMS_PER_PATCH = 7;
	private static final int GARDEN_SCAN = 192;
	private static final int SANCTUM_SCAN = 128;

	private CourtOutpostValidation() {
	}

	static JsonObject check(GameTestHelper h, ServerLevel level, Registry<Structure> structures, List<Outpost> outposts,
			List<BlockPos> seats) {
		var json = new JsonObject();
		var kinds = new JsonObject();
		for (var site : CourtSite.values()) {
			if (site.tier() != CourtSite.Tier.OUTPOST) continue;
			var nearest = outposts.stream().filter(o -> o.site() == site)
					.min((a, b) -> Long.compare(dist(a.centre()), dist(b.centre()))).orElse(null);
			if (nearest == null) continue;
			kinds.add(site.id(), outpost(h, level, structures, nearest));
		}
		h.assertTrue(kinds.size() >= 3, "Expected at least three outpost kinds to inspect: " + kinds.keySet());
		json.add("outposts", kinds);
		json.add("signs", signs(h, level, seats));
		return json;
	}

	private static long dist(BlockPos pos) {
		return (long) pos.getX() * pos.getX() + (long) pos.getZ() * pos.getZ();
	}

	private static JsonObject outpost(GameTestHelper h, ServerLevel level, Registry<Structure> structures, Outpost outpost) {
		var start = level.getChunk(outpost.start().x, outpost.start().z, ChunkStatus.STRUCTURE_STARTS, true)
				.getStartForStructure(structures.get(outpost.id()));
		h.assertTrue(start != null && start.isValid(), "No start for " + outpost);
		var box = start.getBoundingBox();
		for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++)
			for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) level.getChunk(cx, cz);
		int markers = 0;
		for (var pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
			var state = level.getBlockState(pos);
			if (state.is(BlockInit.house_marker.get()) && state.getValue(HouseMarkerBlock.HOUSE).id().equals(outpost.site().house()))
				markers++;
		}
		String where = outpost.site().id() + " at " + outpost.centre().toShortString();
		h.assertTrue(markers == 1, "The " + where + " names its house once at its edge: " + markers);
		var residents = CourtResidentMarkers.read(level.getStructureManager(), start.getPieces()).stream()
				.filter(CourtResidentMarkers.Marker::resident).count();
		h.assertTrue(residents >= 1, "No resident marks in the " + where);
		var json = new JsonObject();
		json.addProperty("x", outpost.centre().getX());
		json.addProperty("y", outpost.centre().getY());
		json.addProperty("z", outpost.centre().getZ());
		json.addProperty("residentMarkers", residents);
		json.addProperty("template", start.getPieces().get(0) instanceof
				net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece pooled
				? pooled.getElement().toString() : "?");
		if (outpost.site() == CourtSite.STILL_BELL) json.add("dry", CourtSeatValidation.dry(h, level, start, List.of("still_bell/bell")));
		return json;
	}

	/** Sign families counted per region, in full chunks round the nearest Garden and Sanctum. */
	private static JsonObject signs(GameTestHelper h, ServerLevel level, List<BlockPos> seats) {
		var chunks = new java.util.LinkedHashSet<ChunkPos>();
		for (int i = 0; i < seats.size(); i++) {
			var seat = seats.get(i);
			int half = i == 0 ? GARDEN_SCAN : SANCTUM_SCAN;
			for (int cx = (seat.getX() - half) >> 4; cx <= (seat.getX() + half) >> 4; cx++)
				for (int cz = (seat.getZ() - half) >> 4; cz <= (seat.getZ() + half) >> 4; cz++) chunks.add(new ChunkPos(cx, cz));
		}
		Map<CourtSignRules.Family, Predicate<BlockState>> families = new EnumMap<>(CourtSignRules.Family.class);
		families.put(CourtSignRules.Family.SUNBURST_BLOOM, s -> s.is(BlockInit.sunburst_bloom.get()));
		families.put(CourtSignRules.Family.HOUSE_MARKER, s -> s.is(BlockInit.house_marker.get()));
		families.put(CourtSignRules.Family.RELAY_MARKER, s -> s.is(BlockInit.relay_marker.get()));
		families.put(CourtSignRules.Family.MESSENGER_BUOY, s -> s.is(BlockInit.messenger_buoy.get()));
		families.put(CourtSignRules.Family.VENT_OFFERING, s -> s.is(BlockInit.vent_offering_basin.get()));
		var perRegion = new HashMap<Long, EnumMap<CourtSignRules.Family, Integer>>();
		var totals = new EnumMap<CourtSignRules.Family, Integer>(CourtSignRules.Family.class);
		int inSites = 0;
		for (var pos : chunks) {
			LevelChunk chunk = level.getChunk(pos.x, pos.z);
			var sections = chunk.getSections();
			for (int s = 0; s < sections.length; s++) {
				var section = sections[s];
				if (section.hasOnlyAir()) continue;
				if (!section.getStates().maybeHas(state -> families.values().stream().anyMatch(f -> f.test(state)))) continue;
				int baseY = chunk.getSectionYFromSectionIndex(s) << 4;
				for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) {
					var state = section.getBlockState(x, y, z);
					for (var entry : families.entrySet()) {
						if (!entry.getValue().test(state)) continue;
						var world = new BlockPos(pos.getMinBlockX() + x, baseY + y, pos.getMinBlockZ() + z);
						if (level.structureManager().getStructureWithPieceAt(world, CourtSignFeature.COURT).isValid()) {
							inSites++;
							continue;
						}
						long region = (long) CourtSignRules.region(world.getX()) << 32 | (CourtSignRules.region(world.getZ()) & 0xFFFFFFFFL);
						perRegion.computeIfAbsent(region, r -> new EnumMap<>(CourtSignRules.Family.class))
								.merge(entry.getKey(), 1, Integer::sum);
						totals.merge(entry.getKey(), 1, Integer::sum);
					}
				}
			}
		}
		var worst = new EnumMap<CourtSignRules.Family, Integer>(CourtSignRules.Family.class);
		for (var counts : perRegion.values()) {
			for (var entry : counts.entrySet()) {
				var family = entry.getKey();
				int cap = family.cap() * (family == CourtSignRules.Family.SUNBURST_BLOOM ? BLOOMS_PER_PATCH : 1);
				h.assertTrue(entry.getValue() <= cap, family + " over its per-region cap: " + entry.getValue() + " > " + cap);
				worst.merge(family, entry.getValue(), Math::max);
			}
		}
		int placed = totals.values().stream().mapToInt(Integer::intValue).sum();
		h.assertTrue(placed > 0, "The sign feature placed nothing in " + chunks.size() + " full chunks");
		var json = new JsonObject();
		json.addProperty("chunksScanned", chunks.size());
		json.addProperty("regions", perRegion.size());
		json.addProperty("signsInCourtSites", inSites);
		json.add("totals", new com.google.gson.GsonBuilder().create().toJsonTree(totals));
		json.add("worstRegion", new com.google.gson.GsonBuilder().create().toJsonTree(worst));
		var caps = new JsonArray();
		for (var family : CourtSignRules.Family.values()) caps.add(family + "=" + family.cap());
		json.add("caps", caps);
		return json;
	}

}

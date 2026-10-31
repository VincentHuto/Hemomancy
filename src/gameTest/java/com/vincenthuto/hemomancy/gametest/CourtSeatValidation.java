package com.vincenthuto.hemomancy.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtResidentMarkers;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatLayout;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSite;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtWorldProbe;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLandmarks;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicTerrainSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The two Seats as generated (Track 7), on the nearest Lighthouse Garden and Vent Sanctum of the validation seed: their
 * chunks are fully generated, then
 * <ul>
 * <li>every template air cell under the sea surface is still dry, has no pending fluid tick beside it in the chunk as
 * it would be saved, and is still air when that saved chunk is read back (the reload);</li>
 * <li>the Garden carries its lamp and its undercroft piece (petrified followers in place);</li>
 * <li>the Sanctum's chimneys are unchanged, block for block, as the terrain planned them, and each wears its crown.</li>
 * </ul>
 */
final class CourtSeatValidation {
	record Seat(CourtSite site, ChunkPos start, BlockPos marker, ResourceLocation id) {
	}

	private static final List<String> TEMPLATES = List.of("lighthouse_garden/garden", "lighthouse_garden/undercroft",
			"vent_sanctum/sanctum", "vent_sanctum/body");
	private static final int SEA_TOP = 62;

	private CourtSeatValidation() {
	}

	static JsonObject check(GameTestHelper h, ServerLevel level, CourtWorldProbe probe, Registry<Structure> structures,
			List<Seat> seats) throws Exception {
		var json = new JsonObject();
		for (var kind : List.of(CourtSite.LIGHTHOUSE_GARDEN, CourtSite.VENT_SANCTUM)) {
			var seat = seats.stream().filter(s -> s.site() == kind)
					.min((a, b) -> Long.compare(dist(a.marker()), dist(b.marker()))).orElse(null);
			h.assertTrue(seat != null, "No " + kind.id() + " to inspect");
			var structure = structures.get(seat.id());
			var start = level.getChunk(seat.start().x, seat.start().z, ChunkStatus.STRUCTURE_STARTS, true)
					.getStartForStructure(structure);
			h.assertTrue(start != null && start.isValid(), "No start for " + seat);
			var box = start.getBoundingBox();
			for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++)
				for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) level.getChunk(cx, cz);
			var report = new JsonObject();
			report.addProperty("x", seat.marker().getX());
			report.addProperty("y", seat.marker().getY());
			report.addProperty("z", seat.marker().getZ());
			report.addProperty("pieces", start.getPieces().size());
			report.add("dry", dry(h, level, start, TEMPLATES));
			var markers = CourtResidentMarkers.read(level.getStructureManager(), start.getPieces());
			report.addProperty("residentMarkers", markers.stream().filter(CourtResidentMarkers.Marker::resident).count());
			if (kind == CourtSite.LIGHTHOUSE_GARDEN) report.add("garden", garden(h, level, start, seat));
			else report.add("sanctum", sanctum(h, level, probe, seat));
			json.add(kind.id(), report);
		}
		return json;
	}

	private static long dist(BlockPos pos) {
		return (long) pos.getX() * pos.getX() + (long) pos.getZ() * pos.getZ();
	}

	/**
	 * Every template air cell at or under the sea surface: dry now, no fluid tick queued beside it, dry after reload.
	 * {@code templates} names the pieces' templates under hemomancy:brined_court/ (also the outposts', Track 8).
	 */
	static JsonObject dry(GameTestHelper h, ServerLevel level, StructureStart start, List<String> templates) {
		var manager = level.getStructureManager();
		var byChunk = new HashMap<ChunkPos, Set<BlockPos>>();
		int cells = 0;
		for (var piece : start.getPieces()) {
			if (!(piece instanceof PoolElementStructurePiece pooled)) continue;
			StructureTemplate template = null;
			for (var name : templates) {
				var candidate = manager.getOrCreate(ResourceLocation.fromNamespaceAndPath("hemomancy", "brined_court/" + name));
				if (candidate.getSize(pooled.getRotation()).equals(pooled.getElement().getSize(manager, pooled.getRotation()))) {
					template = candidate;
					break;
				}
			}
			if (template == null) continue;
			var settings = new StructurePlaceSettings().setRotation(pooled.getRotation());
			for (var info : template.filterBlocks(pooled.getPosition(), settings, Blocks.AIR)) {
				var pos = info.pos();
				if (pos.getY() > SEA_TOP) continue;
				cells++;
				// Dry, not necessarily empty: later decoration may grow grass or lichen in an open-air cell.
				h.assertTrue(level.getFluidState(pos).isEmpty(),
						"A Seat air pocket flooded at " + pos.toShortString() + ": " + level.getBlockState(pos));
				byChunk.computeIfAbsent(new ChunkPos(pos), c -> new HashSet<>()).add(pos);
			}
		}
		h.assertTrue(cells > 0, "No air pocket under the sea to check");
		var info = new RegionStorageInfo(level.getServer().getWorldData().getLevelName(), level.dimension(), "chunk");
		int reread = 0;
		for (var entry : byChunk.entrySet()) {
			var chunk = level.getChunk(entry.getKey().x, entry.getKey().z);
			CompoundTag tag = ChunkSerializer.write(level, chunk);
			var near = new HashSet<BlockPos>();
			for (var pos : entry.getValue()) {
				near.add(pos);
				for (var side : net.minecraft.core.Direction.values()) near.add(pos.relative(side));
			}
			for (Tag tick : tag.getList("fluid_ticks", Tag.TAG_COMPOUND)) {
				var t = (CompoundTag) tick;
				var pos = new BlockPos(t.getInt("x"), t.getInt("y"), t.getInt("z"));
				if (level.getFluidState(pos).isEmpty()) continue;           // a stale tick on a cell that is now air
				h.assertTrue(!near.contains(pos), "A fluid tick is queued beside a Seat air pocket at " + pos.toShortString());
			}
			var back = ChunkSerializer.read(level, level.getPoiManager(), info, entry.getKey(), tag);
			for (var pos : entry.getValue()) {
				h.assertTrue(back.getFluidState(pos).isEmpty(),
						"A Seat air pocket reads back wet at " + pos.toShortString());
				reread++;
			}
		}
		var json = new JsonObject();
		json.addProperty("airCellsUnderSea", cells);
		json.addProperty("chunks", byChunk.size());
		json.addProperty("rereadDry", reread);
		return json;
	}

	private static JsonObject garden(GameTestHelper h, ServerLevel level, StructureStart start, Seat seat) {
		var box = start.getBoundingBox();
		int lamps = 0, followers = 0, hearts = 0, marimoMoss = 0;
		for (var pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
			var state = level.getBlockState(pos);
			if (state.is(BlockInit.sunburst_lamp.get())) lamps++;
			if (state.is(BlockInit.petrified_follower.get())) followers++;
			if (state.is(BlockInit.marimo_heart.get())) hearts++;
			if (state.is(BlockInit.marimo_moss.get())) marimoMoss++;
		}
		// Track 10: Marimo's presence on the undercroft's island, one heart in a mass threaded up the walls.
		h.assertTrue(hearts == 1, "The undercroft should hold exactly one Marimo's Heart: " + hearts);
		h.assertTrue(marimoMoss >= 40, "Marimo's moss mass and threads are missing: " + marimoMoss);
		h.assertTrue(start.getPieces().size() >= 2, "The undercroft piece did not attach under the Garden");
		h.assertTrue(lamps >= 2, "The lighthouse has no living lamp");
		h.assertTrue(followers >= 6, "The undercroft's lowest walls have no petrified followers: " + followers);
		var json = new JsonObject();
		json.addProperty("lamps", lamps);
		json.addProperty("petrifiedFollowers", followers);
		json.addProperty("marimoHearts", hearts);
		json.addProperty("marimoMoss", marimoMoss);
		return json;
	}

	/** The chimneys exactly as the terrain planned them, and a crown on each mouth. */
	private static JsonObject sanctum(GameTestHelper h, ServerLevel level, CourtWorldProbe probe, Seat seat) {
		var marker = seat.marker();
		var vent = probe.terrain().ventSite(Math.floorDiv(marker.getX(), PelagicTerrainSampler.VENT_CELL),
				Math.floorDiv(marker.getZ(), PelagicTerrainSampler.VENT_CELL));
		var chimneys = PelagicLandmarks.chimneys(vent, probe::column);
		var planned = new HashMap<BlockPos, PelagicLandmarks.Material>();
		for (int cx = (marker.getX() - 24) >> 4; cx <= (marker.getX() + 24) >> 4; cx++)
			for (int cz = (marker.getZ() - 24) >> 4; cz <= (marker.getZ() + 24) >> 4; cz++)
				for (var voxel : PelagicLandmarks.plan(probe.terrain(), cx, cz, probe::column)) {
					var pos = new BlockPos(voxel.x(), voxel.y(), voxel.z());
					if (chimneys.stream().anyMatch(c -> c.contains(pos.getX(), pos.getY(), pos.getZ())))
						planned.put(pos, voxel.material());
				}
		h.assertTrue(!planned.isEmpty(), "No chimney voxels planned under the Sanctum");
		int unchanged = 0;
		for (var entry : planned.entrySet()) {
			var expected = entry.getValue() == PelagicLandmarks.Material.MAGMA ? Blocks.MAGMA_BLOCK : Blocks.SMOOTH_BASALT;
			var actual = level.getBlockState(entry.getKey());
			h.assertTrue(actual.is(expected), "The Sanctum changed a chimney at " + entry.getKey().toShortString()
					+ ": " + actual + " instead of " + expected);
			unchanged++;
		}
		int crowns = 0;
		for (var chimney : chimneys) {
			var crown = new BlockPos(chimney.x(), chimney.mouthY() + 1, chimney.z());
			if (level.getBlockState(crown).is(BlockInit.hydra_crown.get())) crowns++;
		}
		h.assertTrue(crowns == chimneys.size(), "Every head wears a crown: " + crowns + " of " + chimneys.size());
		var json = new JsonObject();
		json.addProperty("chimneys", chimneys.size());
		json.addProperty("chimneyVoxelsUnchanged", unchanged);
		json.addProperty("crowns", crowns);
		var list = new JsonArray();
		for (var chimney : chimneys) list.add(chimney.x() + "," + chimney.ground() + "," + chimney.z() + " h" + chimney.height());
		json.add("chimneyPositions", list);
		return json;
	}
}

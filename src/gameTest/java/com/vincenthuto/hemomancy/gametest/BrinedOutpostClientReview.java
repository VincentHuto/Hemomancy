package com.vincenthuto.hemomancy.gametest;

import com.mojang.blaze3d.platform.NativeImage;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.CourtFacingFixtureBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.CourtFixtureBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.HouseMarkerBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.SunburstBloomBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtLandmarkCells;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatCandidates;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatLayout;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSite;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtWorldProbe;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLayer;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicWorldgen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * With hemomancy.brinedOutpostReview, creates a fresh seeded normal world (Pelagic worldgen on), finds the nearest of
 * each inner-house outpost by the placement predicate and generates it where it stands naturally (an outpost with no
 * natural site in range, as the rare Wake nursery often is, is placed through its own start pool on a matching floor
 * near the origin and its folder says so), then flies a fixed path round each: the approach, the entrance and the
 * interior. A signs pass follows: the Lighthouse Garden's bloom ring from the surface, a gallery of every sign block
 * (each bloom variant, all eight house markers, a relay marker, a buoy and a vent offering) and the first
 * feature-placed sign of each family found in the generated chunks. Every stop is shot by day, by night unaided and by
 * night with night vision. Screenshots go to {@code <gameDir>/screenshots/<folder>/<pass>-<nn>-<stop>.png} (gameDir
 * build/brined-outpost-review). Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class BrinedOutpostClientReview {
	private static final String WORLD = "BrinedOutpostReview";
	private static final int FIRST_WAIT = 160;
	private static final int WAIT = 40;

	/** A camera stop in an outpost's own frame: offsets from its centre column at its start row, turned with it. */
	private record Stop(String name, double ex, double ey, double ez, double lx, double ly, double lz) {
	}

	private record Shot(String folder, String name, Vec3 eye, Vec3 look, long time, boolean nightVision) {
	}

	private static final Map<CourtSite, List<Stop>> STOPS = Map.of(
			CourtSite.TIDE_REFUGE, List.of(
					new Stop("approach_from_sea", 6, 7, 26, 0, 2, 0),
					new Stop("from_the_surface_far", 22, 14, 28, 0, 2, 0),
					new Stop("braced_entrance", 0, 1.6, 12, 0, 1, 5),
					new Stop("holdfast_masses", 2.2, 1.2, 9.5, -1, 0.5, 7),
					new Stop("pocket_pool", 0, 4.5, 4, 0, -2, 0),
					new Stop("safe_passage", -6, 1.7, -3.5, 4, 1.6, -3.5),
					new Stop("shelter_interior", 1, 2.6, -6.5, -3, 1.5, -8.5)),
			CourtSite.WAKE_NURSERY, List.of(
					new Stop("approach", 0, 8, -17, 0, 0, 0),
					new Stop("nest_cups", 3.5, 3, 3.5, 0, 0.5, 0),
					new Stop("from_above", 0.5, 13, 1, 0, 0, 0)),
			CourtSite.LINE_CAMP, List.of(
					new Stop("approach_up_the_gully", 0, 7, 19, 0, 7, 0),
					new Stop("pods", 0, 6.5, 6, 0, 6, 0),
					new Stop("lines_from_below", 0, 1.5, -3, 0, 12, 1),
					new Stop("climbing_line", -4.5, 4, 3.5, -6, 9, -6),
					new Stop("from_above", 0, 17, 4, 0, 6, 0)),
			CourtSite.SCREENED_CLEFT, List.of(
					new Stop("approach", 4, 4, 15, 0, 2, 0),
					new Stop("the_face", 0, 1.8, 5, 0, 1.4, -1),
					new Stop("behind_the_veil", 0, 1.6, -2.6, 0, 1, -7),
					new Stop("receiving_room", -3, 2.8, -4.6, 2, 0.5, -8),
					new Stop("closed_door", 2.5, 1.6, -6.5, 6, 1.2, -6.5)),
			CourtSite.STILL_BELL, List.of(
					new Stop("approach", 0, 5, -15, 0, 4, 0),
					new Stop("under_the_rim", 0, 0.8, -7.5, 0, 1, 0),
					new Stop("air_pocket", 0, 4.6, 2, 0, 5.5, -3),
					new Stop("ledge_and_bed", 2.5, 4.4, 2.5, -2.5, 3.3, -2),
					new Stop("sleepers_in_the_ring", 0, 4.4, -1, 3.5, 6.5, 0)),
			CourtSite.WHALE_FALL, List.of(
					new Stop("approach", 15, 10, -17, 0, 2, 0),
					new Stop("broadside", -15, 5, 0, 0, 3, 0),
					new Stop("under_the_ribs", 0, 1.6, -3, 0, 1, 8),
					new Stop("skull_and_jaws", 1, 2.5, 17, 0, 1.5, 11),
					new Stop("rooted_elder", -3, 1.5, 15, -3, 1, 12)));

	private enum Phase { CREATE, ENTER, BUILD, SHOOT, DONE }

	private static Phase phase = Phase.CREATE;
	private static int ticks;
	private static int shot;
	private static int shotTick;
	private static volatile List<Shot> shots;

	private static long seed() {
		return Long.getLong("hemomancy.brinedOutpostReview.seed", 42L);
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.brinedOutpostReview")) return;
		var mc = Minecraft.getInstance();
		switch (phase) {
			case CREATE -> {
				if (mc.level == null && mc.getOverlay() == null
						&& (mc.screen instanceof TitleScreen || mc.screen instanceof AccessibilityOnboardingScreen)) {
					mc.options.onboardAccessibility = false;
					mc.options.save();
					createWorld(mc);
					phase = Phase.ENTER;
				}
			}
			case ENTER -> {
				if (mc.player != null && mc.getSingleplayerServer() != null && ++ticks > 40) {
					mc.options.pauseOnLostFocus = false;
					mc.options.hideGui = true;
					mc.options.gamma().set(0.5D);
					var server = mc.getSingleplayerServer();
					var uuid = mc.player.getUUID();
					phase = Phase.BUILD;
					server.execute(() -> {
						var player = server.getPlayerList().getPlayer(uuid);
						var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
						for (String cmd : new String[] {"gamemode spectator", "gamerule doDaylightCycle false",
								"weather clear", "gamerule doWeatherCycle false", "gamerule doMobSpawning false"})
							server.getCommands().performPrefixedCommand(source, cmd);
						shots = plan(player.serverLevel());
						Hemomancy.LOGGER.info("BRINED_OUTPOST_REVIEW {} shots queued", shots.size());
					});
				}
			}
			case BUILD -> {
				if (shots != null) {
					phase = shots.isEmpty() ? Phase.DONE : Phase.SHOOT;
					shotTick = 0;
				}
			}
			case SHOOT -> shoot(mc);
			case DONE -> {
				Hemomancy.LOGGER.info("BRINED_OUTPOST_REVIEW captured {}", shot);
				mc.stop();
			}
		}
	}

	private static void shoot(Minecraft mc) {
		Shot current = shots.get(shot);
		boolean first = shot == 0 || !shots.get(shot - 1).folder().equals(current.folder());
		int wait = first ? FIRST_WAIT : WAIT;
		if (shotTick++ == 0) {
			var server = mc.getSingleplayerServer();
			var uuid = mc.player.getUUID();
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				player.serverLevel().setDayTime(current.time());
				if (current.nightVision()) {
					player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60, 0, false, false));
				} else {
					player.removeEffect(MobEffects.NIGHT_VISION);
				}
				Vec3 eye = current.eye();
				Vec3 delta = current.look().subtract(eye);
				float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
				float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance()));
				player.teleportTo(player.serverLevel(), eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
			});
			return;
		}
		if (shotTick < wait) return;
		Path out = mc.gameDirectory.toPath().resolve("screenshots").resolve(current.folder()).resolve(current.name() + ".png");
		try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
			Files.createDirectories(out.getParent());
			image.writeToFile(out);
		} catch (IOException exception) {
			Hemomancy.LOGGER.error("BRINED_OUTPOST_REVIEW could not save {}", out, exception);
		}
		Hemomancy.LOGGER.info("BRINED_OUTPOST_REVIEW shot {}/{} at {}", current.folder(), current.name(), current.eye());
		shotTick = 0;
		if (++shot >= shots.size()) phase = Phase.DONE;
	}

	private static void createWorld(Minecraft mc) {
		Path probe = mc.gameDirectory.toPath().resolve("config/theoneprobe-common.toml");
		try {
			if (Files.exists(probe)) Files.writeString(probe, Files.readString(probe).replace("needsProbe = 3", "needsProbe = 2"));
		} catch (IOException exception) {
			Hemomancy.LOGGER.warn("BRINED_OUTPOST_REVIEW could not hide The One Probe's tooltip", exception);
		}
		Path save = mc.gameDirectory.toPath().resolve("saves").resolve(WORLD);
		if (Files.exists(save)) {
			try (Stream<Path> files = Files.walk(save)) {
				for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(file);
			} catch (IOException exception) {
				throw new IllegalStateException("Could not clear the old review world " + save, exception);
			}
		}
		mc.createWorldOpenFlows().createFreshLevel(WORLD,
				new LevelSettings(WORLD, GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, new GameRules(),
						WorldDataConfiguration.DEFAULT),
				new WorldOptions(seed(), true, false), WorldPresets::createNormalWorldDimensions, mc.screen);
	}

	private static List<Shot> plan(ServerLevel level) {
		var out = new ArrayList<Shot>();
		var source = level.getChunkSource();
		var probe = CourtWorldProbe.of(level.registryAccess(), source.getGenerator().getBiomeSource(), source.randomState(),
				level.getSeed());
		if (PelagicWorldgen.context(source.randomState()) == null || probe == null) {
			Hemomancy.LOGGER.error("BRINED_OUTPOST_REVIEW MISSING Pelagic worldgen is off in the review world");
			return out;
		}
		var set = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).get(CourtSeatCandidates.OUTPOSTS);
		var spread = CourtLandmarkCells.Spread.of((RandomSpreadStructurePlacement) set.placement(), level.getSeed(), 0, 0);
		var visited = new ArrayList<BlockPos>();
		for (var site : CourtSite.values()) {
			if (site.tier() != CourtSite.Tier.OUTPOST) continue;
			String folder = site.id();
			BoundingBox pieceBox = null;
			Rotation rotation = Rotation.NONE;
			BlockPos marker = null;
			var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
					.getOrThrow(ResourceKey.create(Registries.STRUCTURE, Hemomancy.rloc(site.id())));
			// Nearest predicted cells first; the set's exclusion zone can still refuse a predicted cell, so confirm
			// each against the real start and move on (a few tries at most).
			int tries = 0;
			for (int ring = 0; ring <= 24 && pieceBox == null && tries < 8; ring++) {
				for (int i = -ring; i <= ring && pieceBox == null && tries < 8; i++) for (int j = -ring; j <= ring; j++) {
					if (Math.max(Math.abs(i), Math.abs(j)) != ring) continue;
					var chunk = spread.potential().of(i, j);
					var stub = com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtPlacementRules.stub(site, probe,
							level.getSeed(), chunk.getMiddleBlockX(), chunk.getMiddleBlockZ());
					if (stub.isEmpty()) continue;
					tries++;
					level.getChunk(stub.get());
					StructureStart start = level.structureManager().getStructureWithPieceAt(stub.get(), structure);
					Hemomancy.LOGGER.info("BRINED_OUTPOST_REVIEW SEARCH {} predicted {} start={}", site.id(), stub.get(), start.isValid());
					if (!start.isValid()) continue;
					BoundingBox box = start.getBoundingBox();
					for (int cx = (box.minX() >> 4) - 1; cx <= (box.maxX() >> 4) + 1; cx++)
						for (int cz = (box.minZ() >> 4) - 1; cz <= (box.maxZ() >> 4) + 1; cz++) level.getChunk(cx, cz);
					var piece = start.getPieces().getFirst();
					pieceBox = piece.getBoundingBox();
					rotation = piece.getRotation();
					marker = stub.get();
					break;
				}
			}
			if (pieceBox == null) {
				// No natural site within range: place the start pool on a matching floor near the origin.
				var spot = fallbackSpot(level, probe, site);
				if (spot == null) {
					Hemomancy.LOGGER.error("BRINED_OUTPOST_REVIEW MISSING no {} and no floor to place one", site.id());
					continue;
				}
				for (int cx = (spot.getX() >> 4) - 2; cx <= (spot.getX() >> 4) + 2; cx++)
					for (int cz = (spot.getZ() >> 4) - 2; cz <= (spot.getZ() >> 4) + 2; cz++) level.getChunk(cx, cz);
				var pool = level.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL)
						.getHolderOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL,
								Hemomancy.rloc("brined_court/" + site.id() + "/start_pool")));
				JigsawPlacement.generateJigsaw(level, pool, Hemomancy.rloc("court_outpost_start"), 1,
						CourtSeatLayout.start(site, spot), false);
				int half = 13;
				pieceBox = new BoundingBox(spot.getX() - half, spot.getY() - CourtSeatLayout.outpostBelow(site), spot.getZ() - half,
						spot.getX() + half, spot.getY() + 20, spot.getZ() + half);
				rotation = Rotation.NONE;
				marker = spot;
				folder = site.id() + "_placed";
			}
			BlockPos centreBlock;
			if (folder.endsWith("_placed")) {
				centreBlock = site == CourtSite.TIDE_REFUGE
						? new BlockPos(marker.getX(), CourtSeatLayout.GARDEN_WATERLINE_Y, marker.getZ()) : marker;
				rotation = detectRotation(level, site, centreBlock);
			} else {
				centreBlock = CourtSeatLayout.outpostCentre(site, pieceBox);
			}
			Vec3 centre = new Vec3(centreBlock.getX() + .5, centreBlock.getY(), centreBlock.getZ() + .5);
			visited.add(centreBlock);
			Hemomancy.LOGGER.info("BRINED_OUTPOST_REVIEW BUILT {} marker={} centre={} rotation={} box={}", folder, marker,
					centre, rotation, pieceBox);
			addShots(out, folder, STOPS.get(site), centre, rotation, false);
		}
		signs(level, probe, out, visited);
		return out;
	}

	/** A placed outpost's rotation is NONE (generateJigsaw rolls one, so read it back from its house marker's facing). */
	private static Rotation detectRotation(ServerLevel level, CourtSite site, BlockPos centre) {
		for (var pos : BlockPos.betweenClosed(centre.offset(-14, -14, -14), centre.offset(14, 14, 14))) {
			var state = level.getBlockState(pos);
			if (!state.is(BlockInit.house_marker.get())) continue;
			var authored = switch (site) {
				case TIDE_REFUGE, LINE_CAMP, SCREENED_CLEFT -> Direction.SOUTH;
				default -> Direction.NORTH;
			};
			var facing = state.getValue(CourtFacingFixtureBlock.FACING);
			for (var rotation : Rotation.values()) if (rotation.rotate(authored) == facing) return rotation;
		}
		return Rotation.NONE;
	}

	/** A floor the site's predicate would accept near the origin, ignoring spacing and exclusions: for review only. */
	private static BlockPos fallbackSpot(ServerLevel level, CourtWorldProbe probe, CourtSite site) {
		for (int ring = 0; ring <= 96; ring++) {
			for (int i = -ring; i <= ring; i++) for (int j = -ring; j <= ring; j++) {
				if (Math.max(Math.abs(i), Math.abs(j)) != ring) continue;
				int x = i * 32, z = j * 32;
				if (!com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtPlacementRules.habitat(site, probe, x, z)) continue;
				var column = probe.column(x, z);
				if (column.influence() < .999) continue;
				int y = column.floorY() + 1;
				// The nursery's banks are rare enough that none may lie near the origin: any open-ocean column will do.
				if (site != CourtSite.WAKE_NURSERY && probe.layerAt(x, y, z) != site.layer()) continue;
				return new BlockPos(x, y, z);
			}
		}
		return null;
	}

	private static void addShots(List<Shot> out, String folder, List<Stop> stops, Vec3 centre, Rotation rotation,
			boolean absolute) {
		for (var pass : List.of(new Object[] {"day", 6000L, false}, new Object[] {"night", 18000L, false},
				new Object[] {"night_vision", 18000L, true})) {
			for (int i = 0; i < stops.size(); i++) {
				var stop = stops.get(i);
				Vec3 eye = absolute ? new Vec3(stop.ex(), stop.ey(), stop.ez()) : frame(centre, rotation, stop.ex(), stop.ey(), stop.ez());
				Vec3 look = absolute ? new Vec3(stop.lx(), stop.ly(), stop.lz()) : frame(centre, rotation, stop.lx(), stop.ly(), stop.lz());
				out.add(new Shot(folder, String.format(Locale.ROOT, "%s-%02d-%s", pass[0], i, stop.name()), eye, look,
						(Long) pass[1], (Boolean) pass[2]));
			}
		}
	}

	/**
	 * The signs pass: the Garden's bloom ring, a gallery of every sign block on a deep floor near the origin, and the
	 * first feature-placed sign of each family in the chunks generated so far.
	 */
	private static void signs(ServerLevel level, CourtWorldProbe probe, List<Shot> out, List<BlockPos> visited) {
		var stops = new ArrayList<Stop>();
		var garden = CourtSeatCandidates.nearest(level, CourtSite.LIGHTHOUSE_GARDEN, BlockPos.ZERO, 16).orElse(null);
		if (garden != null) {
			for (int cx = (garden.getX() >> 4) - 2; cx <= (garden.getX() >> 4) + 2; cx++)
				for (int cz = (garden.getZ() >> 4) - 2; cz <= (garden.getZ() >> 4) + 2; cz++) level.getChunk(cx, cz);
			stops.add(new Stop("garden_bloom_ring", garden.getX() + 24, 70, garden.getZ() + 24, garden.getX() + 14, 62,
					garden.getZ() + 14));
		}
		// Natural signs in what has been generated round the outposts (a placed outpost's own marker excluded).
		var found = new java.util.LinkedHashMap<String, BlockPos>();
		for (var centre : visited) {
			int r = 64;
			for (int cx = (centre.getX() - r) >> 4; cx <= (centre.getX() + r) >> 4; cx++)
				for (int cz = (centre.getZ() - r) >> 4; cz <= (centre.getZ() + r) >> 4; cz++) {
					var chunk = level.getChunk(cx, cz);
					for (int bx = 0; bx < 16; bx++) for (int bz = 0; bz < 16; bz++) for (int by = -55; by <= 64; by++) {
						var pos = new BlockPos(chunk.getPos().getMinBlockX() + bx, by, chunk.getPos().getMinBlockZ() + bz);
						var state = chunk.getBlockState(pos);
						String kind = state.is(BlockInit.relay_marker.get()) ? "relay_marker"
								: state.is(BlockInit.messenger_buoy.get()) ? "messenger_buoy"
								: state.is(BlockInit.sunburst_bloom.get()) ? "sunburst_bloom"
								: state.is(BlockInit.house_marker.get()) ? "house_marker"
								: state.is(BlockInit.vent_offering_basin.get()) ? "vent_offering" : null;
						if (kind == null || found.containsKey(kind)) continue;
						if (kind.equals("house_marker") && (level.structureManager()
								.getStructureWithPieceAt(pos, com.vincenthuto.hemomancy.common.worldgen.feature.court.CourtSignFeature.COURT).isValid()
								|| visited.stream().anyMatch(v -> v.distManhattan(pos) < 40)))
							continue;
						found.put(kind, pos.immutable());
					}
				}
		}
		for (var entry : found.entrySet()) {
			var p = entry.getValue();
			boolean surface = p.getY() >= 61;
			stops.add(new Stop("natural_" + entry.getKey(), p.getX() + 3.5, p.getY() + (surface ? 3 : 1.6), p.getZ() + 3.5,
					p.getX() + .5, p.getY() + .4, p.getZ() + .5));
			Hemomancy.LOGGER.info("BRINED_OUTPOST_REVIEW SIGN natural {} at {}", entry.getKey(), p);
		}
		// The gallery is built after the natural scan, so the scan never finds it.
		var gallery = gallery(level, probe);
		if (gallery != null) {
			double x = gallery.getX(), y = gallery.getY(), z = gallery.getZ();
			stops.add(new Stop("gallery_house_markers", x + 8, y + 3, z + 6, x + 8, y + .5, z));
			stops.add(new Stop("gallery_relay_and_offering", x + 19, y + 2.5, z + 5, x + 19, y + .8, z));
			stops.add(new Stop("gallery_surface", x + 4, 65.5, z + 6, x + 4, 62.5, z));
			stops.add(new Stop("gallery_buoy_from_below", x + 10, 56, z + 5, x + 10, 61, z));
		}
		Hemomancy.LOGGER.info("BRINED_OUTPOST_REVIEW signs: garden={} gallery={} natural={}", garden, gallery, found.keySet());
		addShots(out, "signs", stops, Vec3.ZERO, Rotation.NONE, true);
	}

	/** Every sign block in a row on a deep open floor near the origin, with blooms and a buoy on the surface above. */
	private static BlockPos gallery(ServerLevel level, CourtWorldProbe probe) {
		for (int ring = 4; ring <= 64; ring++) {
			for (int i = -ring; i <= ring; i++) for (int j = -ring; j <= ring; j++) {
				if (Math.max(Math.abs(i), Math.abs(j)) != ring) continue;
				int x = i * 24, z = j * 24;
				var column = probe.column(x, z);
				if (column.influence() < .999 || probe.layerAt(x, 50, z) != PelagicLayer.OPEN) continue;
				if (column.floorY() > 44) continue;
				for (int cx = (x >> 4) - 1; cx <= ((x + 24) >> 4) + 1; cx++)
					for (int cz = (z >> 4) - 1; cz <= (z >> 4) + 1; cz++) level.getChunk(cx, cz);
				int y = column.floorY() + 1;
				// A flat stone shelf so every piece stands level.
				for (int dx = -1; dx <= 22; dx++) for (int dz = -2; dz <= 2; dz++) {
					level.setBlock(new BlockPos(x + dx, y - 1, z + dz), Blocks.SMOOTH_STONE.defaultBlockState(), 2);
					for (int dy = 0; dy < 4; dy++)
						level.setBlock(new BlockPos(x + dx, y + dy, z + dz), Blocks.WATER.defaultBlockState(), 2);
				}
				int dx = 0;
				for (var house : CourtHouse.values()) {
					level.setBlock(new BlockPos(x + dx, y, z), BlockInit.house_marker.get().defaultBlockState()
							.setValue(HouseMarkerBlock.HOUSE, house).setValue(CourtFacingFixtureBlock.FACING, Direction.SOUTH)
							.setValue(CourtFixtureBlock.WATERLOGGED, true), 2);
					dx += 2;
				}
				level.setBlock(new BlockPos(x + 18, y, z), BlockInit.relay_marker.get().defaultBlockState()
						.setValue(CourtFixtureBlock.WATERLOGGED, true), 2);
				level.setBlock(new BlockPos(x + 20, y, z), BlockInit.vent_offering_basin.get().defaultBlockState()
						.setValue(CourtFixtureBlock.WATERLOGGED, true).setValue(CourtFacingFixtureBlock.FACING, Direction.SOUTH), 2);
				int bx = 0;
				for (var variant : SunburstBloomBlock.Variant.values()) {
					for (int turn = 0; turn < 3; turn++) {
						var pos = new BlockPos(x + bx, 63, z + (turn - 1));
						if (level.getBlockState(pos.below()).is(Blocks.WATER) && level.getBlockState(pos).isAir())
							level.setBlock(pos, BlockInit.sunburst_bloom.get().defaultBlockState()
									.setValue(SunburstBloomBlock.VARIANT, variant).setValue(SunburstBloomBlock.TURN, turn), 2);
					}
					bx += 3;
				}
				var buoy = new BlockPos(x + 10, 62, z);
				if (level.getBlockState(buoy).is(Blocks.WATER))
					level.setBlock(buoy, BlockInit.messenger_buoy.get().defaultBlockState()
							.setValue(CourtFixtureBlock.WATERLOGGED, true), 2);
				return new BlockPos(x, y, z);
			}
		}
		return null;
	}

	/** A point in an outpost's own frame (x east, z south from its centre column, y up from its start row) turned. */
	private static Vec3 frame(Vec3 centre, Rotation rotation, double x, double y, double z) {
		double rx = x, rz = z;
		switch (rotation) {
			case CLOCKWISE_90 -> {
				rx = -z;
				rz = x;
			}
			case CLOCKWISE_180 -> {
				rx = -x;
				rz = -z;
			}
			case COUNTERCLOCKWISE_90 -> {
				rx = z;
				rz = -x;
			}
			default -> {
			}
		}
		return centre.add(rx, y, rz);
	}
}

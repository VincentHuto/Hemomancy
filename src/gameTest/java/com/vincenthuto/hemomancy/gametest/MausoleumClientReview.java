package com.vincenthuto.hemomancy.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorRules;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.mausoleum.WardStrikePacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * With hemomancy.mausoleumReview, builds a fresh deep superflat world, spawns each Saint's complex (or the one named by
 * hemomancy.mausoleumReview.saint) 200 blocks apart through {@link MausoleumAssembly}, then walks the camera stops in
 * {@code mausoleum/review-stops.json} and saves one screenshot per stop to {@code <gameDir>/<saint>/}. A stop whose piece
 * did not generate, or a dropped mandatory piece, is logged as MAUSOLEUM_REVIEW MISSING. A stop's piece may be a list of
 * alternatives (a wing's two approach layouts): the first that generated is used.
 *
 * <p>With hemomancy.mausoleumReview.natural, it creates a normal world instead, finds each Saint's nearest naturally
 * generated complex and walks the same stops there (into {@code <gameDir>/natural/<saint>/}), logging how the entrance
 * sits on the terrain the beardifier left. Excluded from release jars.
 *
 * <p>A stop may set the viewer's degree first ({@code "degree"}), and may make the ward answer before its shot
 * ({@code "action"}: {@code "strike"} on a wall face, {@code "threshold"} at the ward line), sent {@code "lead"} ticks
 * before the screenshot so the strike's ring is caught mid-flight. Its {@code "block"} and {@code "face"} are in the
 * piece's template coordinates and turn with it.
 *
 * <p>A stop may also put the ring door in a state before its shot ({@code "door"}: the controller's {@code "block"} in
 * template coordinates, its lit {@code "nodes"}, a {@code "phase"} and how far into it, {@code "tick"}), written into the
 * door's saved state {@code "lead"} ticks before the screenshot so a ring is caught mid-turn or the blood mid-channel.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MausoleumClientReview {
	private static final String FLAT_WORLD = "MausoleumReview";
	private static final String NATURAL_WORLD = "MausoleumNatural";
	private static final int SPACING = 200;
	private static final int FIRST_WAIT = 140;
	private static final int WAIT = 45;

	private static final int DEFAULT_LEAD = 6;

	private record Shot(String folder, String name, Vec3 eye, Vec3 look, int degree, @Nullable Action action,
			@Nullable DoorState door) {}

	/** The ring door at {@code block} put in a state: lit nodes (turning since the write), a phase and its tick. */
	private record DoorState(BlockPos block, int nodes, String phase, int tick, int lead) {}

	/** The ward answering before a shot: a strike of {@code kind} on {@code face} of {@code block}, in the world. */
	private record Action(WardStrikePacket.Kind kind, BlockPos block, Direction face, int lead) {}

	private enum Phase { CREATE, ENTER, BUILD, SHOOT, DONE }

	private static Phase phase = Phase.CREATE;
	private static int ticks;
	private static int shot;
	private static int shotTick;
	private static volatile List<Shot> shots;

	private static boolean natural() {
		return Boolean.getBoolean("hemomancy.mausoleumReview.natural");
	}

	private static List<EnumSaintType> saints() {
		String chosen = System.getProperty("hemomancy.mausoleumReview.saint", "all");
		return Arrays.stream(EnumSaintType.values())
				.filter(saint -> chosen.equals("all") || saint.name().equalsIgnoreCase(chosen)).toList();
	}

	private static long seed() {
		return Long.getLong("hemomancy.mausoleumReview.seed", 20261028L);
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.mausoleumReview")) return;
		var mc = Minecraft.getInstance();
		switch (phase) {
			case CREATE -> {
				// A fresh game directory opens on the accessibility onboarding screen, not the title screen.
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
					mc.options.gamma().set(1.0D);
					var server = mc.getSingleplayerServer();
					var uuid = mc.player.getUUID();
					phase = Phase.BUILD;
					server.execute(() -> {
						var player = server.getPlayerList().getPlayer(uuid);
						var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
						for (String cmd : new String[] {"gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
								"weather clear", "gamerule doWeatherCycle false", "gamerule doMobSpawning false"})
							server.getCommands().performPrefixedCommand(source, cmd);
						shots = natural() ? locateNatural(player.serverLevel()) : buildFlat(player.serverLevel());
						Hemomancy.LOGGER.info("MAUSOLEUM_REVIEW {} stops queued", shots.size());
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
				Hemomancy.LOGGER.info("MAUSOLEUM_REVIEW captured {}", shot);
				mc.stop();
			}
		}
	}

	private static void shoot(Minecraft mc) {
		Shot current = shots.get(shot);
		boolean firstOfFolder = shot == 0 || !shots.get(shot - 1).folder().equals(current.folder());
		int wait = firstOfFolder ? FIRST_WAIT : WAIT;
		if (shotTick++ == 0) {
			var server = mc.getSingleplayerServer();
			var uuid = mc.player.getUUID();
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				if (current.degree() >= 0) {
					server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4)
							.withSuppressedOutput(), "hemo degree set " + current.degree());
				}
				Vec3 eye = current.eye();
				Vec3 delta = current.look().subtract(eye);
				float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
				float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance()));
				player.teleportTo(player.serverLevel(), eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
			});
			return;
		}
		Action action = current.action();
		if (action != null && shotTick == wait - action.lead()) {
			var server = mc.getSingleplayerServer();
			var uuid = mc.player.getUUID();
			server.execute(() -> PacketHandler.sendToPlayer(server.getPlayerList().getPlayer(uuid),
					WardStrikePacket.strike(action.block(), action.face(), action.kind())));
		}
		DoorState door = current.door();
		if (door != null && shotTick == wait - door.lead()) {
			var server = mc.getSingleplayerServer();
			var uuid = mc.player.getUUID();
			server.execute(() -> setDoor(server.getPlayerList().getPlayer(uuid).serverLevel(), door, uuid));
		}
		if (shotTick < wait) return;
		Path out = mc.gameDirectory.toPath().resolve(current.folder()).resolve(current.name() + ".png");
		try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
			Files.createDirectories(out.getParent());
			image.writeToFile(out);
		} catch (IOException exception) {
			Hemomancy.LOGGER.error("MAUSOLEUM_REVIEW could not save {}", out, exception);
		}
		Hemomancy.LOGGER.info("MAUSOLEUM_REVIEW shot {}/{} at {}", current.folder(), current.name(), current.eye());
		shotTick = 0;
		if (++shot >= shots.size()) phase = Phase.DONE;
	}

	private static void createWorld(Minecraft mc) {
		hideProbeTooltip(mc.gameDirectory.toPath().resolve("config/theoneprobe-common.toml"));
		String name = natural() ? NATURAL_WORLD : FLAT_WORLD;
		Path save = mc.gameDirectory.toPath().resolve("saves").resolve(name);
		if (Files.exists(save)) {
			try (Stream<Path> files = Files.walk(save)) {
				for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(file);
			} catch (IOException exception) {
				throw new IllegalStateException("Could not clear the old review world " + save, exception);
			}
		}
		mc.createWorldOpenFlows().createFreshLevel(name,
				new LevelSettings(name, GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, new GameRules(),
						WorldDataConfiguration.DEFAULT),
				new WorldOptions(seed(), natural(), false),
				natural() ? WorldPresets::createNormalWorldDimensions : MausoleumClientReview::deepFlat, mc.screen);
	}

	/** The One Probe draws its tooltip even with the GUI hidden; in the review's own game directory it needs a probe. */
	private static void hideProbeTooltip(Path config) {
		try {
			if (Files.exists(config)) {
				Files.writeString(config, Files.readString(config).replace("needsProbe = 3", "needsProbe = 2"));
			}
		} catch (IOException exception) {
			Hemomancy.LOGGER.warn("MAUSOLEUM_REVIEW could not hide The One Probe's tooltip", exception);
		}
	}

	/** Superflat with a hundred blocks of stone under the grass, so the complex's lower pieces have ground to sit in. */
	private static WorldDimensions deepFlat(RegistryAccess access) {
		WorldDimensions dimensions = access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT)
				.value().createWorldDimensions();
		var plains = access.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
		var settings = new FlatLevelGeneratorSettings(Optional.of(HolderSet.direct()), plains, List.of())
				.withBiomeAndLayers(List.of(new FlatLayerInfo(1, Blocks.BEDROCK), new FlatLayerInfo(100, Blocks.STONE),
						new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(1, Blocks.GRASS_BLOCK)),
						Optional.of(HolderSet.direct()), plains);
		return dimensions.replaceOverworldGenerator(access, new FlatLevelSource(settings));
	}

	private static List<Shot> buildFlat(ServerLevel level) {
		List<Shot> out = new ArrayList<>();
		List<EnumSaintType> saints = saints();
		for (int i = 0; i < saints.size(); i++) {
			EnumSaintType saint = saints.get(i);
			int x = i * SPACING;
			level.getChunk(x >> 4, 0);
			BlockPos surface = new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, 0), 0);
			var layout = MausoleumAssembly.layout(level, saint, surface, seed());
			if (layout.isEmpty()) {
				Hemomancy.LOGGER.error("MAUSOLEUM_REVIEW MISSING {} start piece", saint);
				continue;
			}
			MausoleumAssembly.build(level, layout.get());
			String folder = saint.name().toLowerCase(Locale.ROOT);
			report(folder, layout.get().byTemplate(), surface);
			out.addAll(stops(folder, layout.get().byTemplate()));
		}
		return out;
	}

	private static List<Shot> locateNatural(ServerLevel level) {
		List<Shot> out = new ArrayList<>();
		var generator = level.getChunkSource().getGenerator();
		for (EnumSaintType saint : saints()) {
			var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
					.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE,
							Hemomancy.rloc("mausoleum_" + saint.name().toLowerCase(Locale.ROOT))));
			var found = generator.findNearestMapStructure(level, HolderSet.direct(structure), BlockPos.ZERO, 100, false);
			if (found == null) {
				Hemomancy.LOGGER.error("MAUSOLEUM_REVIEW MISSING {} no natural complex within range", saint);
				continue;
			}
			ChunkPos startChunk = new ChunkPos(found.getFirst());
			var start = level.getChunk(startChunk.x, startChunk.z).getStartForStructure(structure.value());
			if (start == null || !start.isValid()) {
				Hemomancy.LOGGER.error("MAUSOLEUM_REVIEW MISSING {} start not in chunk {}", saint, startChunk);
				continue;
			}
			BoundingBox box = start.getBoundingBox();
			for (int cx = (box.minX() >> 4) - 1; cx <= (box.maxX() >> 4) + 1; cx++)
				for (int cz = (box.minZ() >> 4) - 1; cz <= (box.maxZ() >> 4) + 1; cz++) level.getChunk(cx, cz);
			Map<String, List<PoolElementStructurePiece>> byTemplate = new LinkedHashMap<>();
			for (StructurePiece piece : start.getPieces()) {
				if (piece instanceof PoolElementStructurePiece pool) {
					byTemplate.computeIfAbsent(MausoleumAssembly.templateName(pool), n -> new ArrayList<>()).add(pool);
				}
			}
			String folder = "natural/" + saint.name().toLowerCase(Locale.ROOT);
			measureNaturalEntrance(level, folder, byTemplate.get("top").getFirst());
			report(folder, byTemplate, null);
			out.addAll(stops(folder, byTemplate));
		}
		return out;
	}

	/**
	 * Track 1's ground check: the ground height around the entrance piece against its first template row, both from the
	 * built world (beardifier applied) and from the bare noise the start was projected onto.
	 */
	private static void measureNaturalEntrance(ServerLevel level, String folder, PoolElementStructurePiece top) {
		BoundingBox box = top.getBoundingBox();
		int cx = (box.minX() + box.maxX()) / 2, cz = (box.minZ() + box.maxZ()) / 2;
		int noise = level.getChunkSource().getGenerator().getFirstFreeHeight(cx, cz, Heightmap.Types.WORLD_SURFACE_WG,
				level, level.getChunkSource().randomState());
		List<String> ring = new ArrayList<>();
		for (int[] offset : new int[][] {{-3, 0}, {3, 0}, {0, -3}, {0, 3}, {-6, 0}, {6, 0}, {0, -6}, {0, 6}}) {
			int x = offset[0] < 0 ? box.minX() + offset[0] : offset[0] > 0 ? box.maxX() + offset[0] : cx;
			int z = offset[1] < 0 ? box.minZ() + offset[1] : offset[1] > 0 ? box.maxZ() + offset[1] : cz;
			ring.add("(" + offset[0] + "," + offset[1] + ")=" + (level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1)
					+ ":" + level.getBlockState(new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1, z))
					.getBlock().getDescriptionId().replace("block.minecraft.", ""));
		}
		Hemomancy.LOGGER.info("MAUSOLEUM_REVIEW GROUND {} topRow0={} noiseFirstFree={} rotation={} groundTopBlockY {}",
				folder, box.minY(), noise, top.getRotation(), String.join(" ", ring));
	}

	private static void report(String folder, Map<String, List<PoolElementStructurePiece>> byTemplate, BlockPos surface) {
		var top = byTemplate.get("top").getFirst();
		Hemomancy.LOGGER.info("MAUSOLEUM_REVIEW BUILT {} pieces={} topRow0={} surfaceFirstFree={} rotation={}", folder,
				byTemplate.keySet(), top.getBoundingBox().minY(), surface, top.getRotation());
		for (String piece : MausoleumAssemblyGameTests.missing(byTemplate)) {
			Hemomancy.LOGGER.error("MAUSOLEUM_REVIEW MISSING {} piece {}", folder, piece);
		}
	}

	private static List<Shot> stops(String folder, Map<String, List<PoolElementStructurePiece>> byTemplate) {
		List<Shot> out = new ArrayList<>();
		String saint = folder.substring(folder.lastIndexOf('/') + 1);
		JsonArray stops = readStops();
		for (int i = 0; i < stops.size(); i++) {
			JsonObject stop = stops.get(i).getAsJsonObject();
			if (stop.has("saints") && !stop.getAsJsonArray("saints").toString().contains("\"" + saint + "\"")) continue;
			String name = stop.get("name").getAsString();
			List<String> named = new ArrayList<>();
			if (stop.get("piece").isJsonArray()) {
				stop.getAsJsonArray("piece").forEach(piece -> named.add(piece.getAsString()));
			} else {
				named.add(stop.get("piece").getAsString());
			}
			var pieces = named.stream().map(byTemplate::get).filter(found -> found != null).findFirst().orElse(null);
			if (pieces == null) {
				Hemomancy.LOGGER.error("MAUSOLEUM_REVIEW MISSING {} stop {} (no {} piece)", folder, name, named);
				continue;
			}
			var piece = pieces.getFirst();
			out.add(new Shot(folder, String.format(Locale.ROOT, "%02d-%s", i, name),
					MausoleumAssembly.toWorld(piece, vec(stop.getAsJsonArray("eye"))),
					MausoleumAssembly.toWorld(piece, vec(stop.getAsJsonArray("look"))),
					stop.has("degree") ? stop.get("degree").getAsInt() : -1, action(stop, piece), door(stop, piece)));
		}
		return out;
	}

	/** Writes the door's state through its saved tag, as a loaded save would carry it, and sends it to the client. */
	private static void setDoor(ServerLevel level, DoorState state, java.util.UUID viewer) {
		if (!(level.getBlockEntity(state.block()) instanceof RingDoorBlockEntity door)) {
			Hemomancy.LOGGER.error("MAUSOLEUM_REVIEW MISSING ring door at {}", state.block());
			return;
		}
		CompoundTag tag = door.saveWithoutMetadata(level.registryAccess());
		long now = level.getGameTime();
		tag.putInt(RingDoorBlockEntity.TAG_NODES, state.nodes());
		long[] starts = new long[RingDoorRules.RINGS];
		// The newest node is still turning when the shot is taken; the earlier ones have long since settled.
		for (int ring = 0; ring < state.nodes(); ring++) starts[ring] = ring == state.nodes() - 1 ? now : 1;
		tag.putLongArray(RingDoorBlockEntity.TAG_TURN_START, starts);
		tag.putString(RingDoorBlockEntity.TAG_PHASE, state.phase());
		tag.putInt(RingDoorBlockEntity.TAG_PHASE_TICK, state.tick());
		tag.putUUID(RingDoorBlockEntity.TAG_PROJECTOR, viewer);
		door.loadWithComponents(tag, level.registryAccess());
		BlockEntitySync.markAndSync(door);
		// The viewer projects: give them the blood each ring takes, so a projection never breaks between stops.
		var player = level.getServer().getPlayerList().getPlayer(viewer);
		if (player != null && state.phase().equals("PROJECTING")) {
			var volume = HemoCapabilityAccess.getBloodVolume(player);
			volume.setActive(true);
			volume.setBloodVolume(Math.max(volume.getBloodVolume(), 3000));
		}
		Hemomancy.LOGGER.info("MAUSOLEUM_REVIEW door at {} set {} nodes, {} tick {}", state.block(), state.nodes(),
				state.phase(), state.tick());
	}

	@Nullable
	private static DoorState door(JsonObject stop, PoolElementStructurePiece piece) {
		if (!stop.has("door")) return null;
		JsonObject door = stop.getAsJsonObject("door");
		Vec3 block = vec(door.getAsJsonArray("block")).add(0.5, 0.5, 0.5);
		return new DoorState(BlockPos.containing(MausoleumAssembly.toWorld(piece, block)),
				door.has("nodes") ? door.get("nodes").getAsInt() : 0,
				door.has("phase") ? door.get("phase").getAsString() : "IDLE",
				door.has("tick") ? door.get("tick").getAsInt() : 0,
				door.has("lead") ? door.get("lead").getAsInt() : DEFAULT_LEAD);
	}

	@Nullable
	private static Action action(JsonObject stop, PoolElementStructurePiece piece) {
		if (!stop.has("action")) return null;
		WardStrikePacket.Kind kind = switch (stop.get("action").getAsString()) {
			case "strike" -> WardStrikePacket.Kind.BREAK;
			case "threshold" -> WardStrikePacket.Kind.THRESHOLD;
			default -> throw new IllegalStateException("Unknown review action " + stop.get("action"));
		};
		Vec3 block = vec(stop.getAsJsonArray("block")).add(0.5, 0.5, 0.5);
		Direction face = piece.getRotation().rotate(Direction.byName(stop.get("face").getAsString()));
		int lead = stop.has("lead") ? stop.get("lead").getAsInt() : DEFAULT_LEAD;
		return new Action(kind, BlockPos.containing(MausoleumAssembly.toWorld(piece, block)), face, lead);
	}

	private static JsonArray readStops() {
		try (var stream = MausoleumClientReview.class.getResourceAsStream("/mausoleum/review-stops.json")) {
			return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject()
					.getAsJsonArray("stops");
		} catch (IOException exception) {
			throw new IllegalStateException("Could not read the mausoleum review stops", exception);
		}
	}

	private static Vec3 vec(JsonArray values) {
		return new Vec3(values.get(0).getAsDouble(), values.get(1).getAsDouble(), values.get(2).getAsDouble());
	}
}

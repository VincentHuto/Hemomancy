package com.vincenthuto.hemomancy.gametest;

import com.mojang.blaze3d.platform.NativeImage;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatCandidates;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatLayout;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSite;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.VentSanctumHeads;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicWorldgen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
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
import java.util.stream.Stream;

/**
 * With hemomancy.brinedSeatReview, creates a fresh seeded normal world (Pelagic worldgen on), finds the nearest
 * Lighthouse Garden and Vent Sanctum through {@link CourtSeatCandidates#nearest}, generates them where they stand
 * naturally, and flies a fixed camera path round each: the approach (from the sea surface for the Garden, from the
 * depths for the Sanctum), every named part, the undercroft descent to Marimo's pool, and the threshold. Every stop is
 * shot three times: by day and by night unaided, and by night with night vision for the geometry. Screenshots go to
 * {@code <gameDir>/screenshots/<site>/<pass>-<nn>-<stop>.png} (gameDir build/brined-seat-review). Excluded from
 * release jars.
 *
 * <p>Last, Brined Court Track 5's chart shots ({@code screenshots/relay_chart/}): the player stands 140 blocks south
 * of the Garden holding a Relay Chart drawn there (its search runs off the server thread) with a Brine-Sealed Letter in
 * the off hand, facing the Garden (the lodestone vignette shows) and facing away (it doesn't).
 * {@code hemomancy.brinedSeatReview.chartOnly} skips the Seat stops.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class BrinedSeatClientReview {
	private static final String WORLD = "BrinedSeatReview";
	private static final int FIRST_WAIT = 160;
	private static final int WAIT = 40;

	/** A camera stop in a Seat's own frame: offsets from its centre column, turned with the start piece. */
	private record Stop(String name, double ex, double ey, double ez, double lx, double ly, double lz) {
	}

	private record Shot(String folder, String name, Vec3 eye, Vec3 look, long time, boolean nightVision, boolean chart) {
		Shot(String folder, String name, Vec3 eye, Vec3 look, long time, boolean nightVision) {
			this(folder, name, eye, look, time, nightVision, false);
		}
	}

	private static final int CHART_WAIT = 100;

	private static final List<Stop> GARDEN = List.of(
			new Stop("approach_from_sea", 0, 8, -46, 0, 18, 0),
			new Stop("lighthouse_from_reef", 30, 3, 30, 0, 20, 0),
			new Stop("landing_and_door", 3, 6, -22, 0, 9, -4),
			new Stop("terraces_from_south", 0, 7, 26, 0, 3, 10),
			new Stop("terraces_underwater", 4, -3, 25, 0, -2, 14),
			new Stop("arbors", 14, 7, 6, 9, 4, 9),
			new Stop("communal_tables", -4, 8, 14, 0, 6, 9),
			new Stop("guest_house_interior", -11, 6.6, 1, -16, 5.5, -2),
			new Stop("lantern_room", 2.5, 31.6, 2.5, 0, 31, 0),
			new Stop("undercroft_hatch", 11, 9.6, -6, 8.5, 6, -1),
			new Stop("undercroft_first_tier", 9, 4.6, 0, -5, 0, 0),
			new Stop("undercroft_descent", -8, 0.6, 6, 0, -9, 0),
			new Stop("undercroft_deep_tier", 8, -7.4, -4, -6, -10, 2),
			new Stop("marimo_pool", 0, -6, 9, 0, -12, 0),
			new Stop("petrified_followers", -4, -10.4, 4, -10, -10.5, 8));

	private static final List<Stop> SANCTUM = List.of(
			new Stop("approach_from_the_depths", 0, 20, -34, 0, 0, 0),
			new Stop("processional_ring", 18, 4, 18, 0, 2, 0),
			new Stop("heads_close", 6, 6, -10, 0, 4, 0),
			new Stop("hall_of_preparation_outside", 30, 6, 8, 21, 3, 0),
			new Stop("hall_of_preparation", 24, 3.6, 0, 18, 3, 0),
			new Stop("moon_pool", 22.5, 4.6, 2, 18.5, 2, -0.5),
			new Stop("the_body", 10, 6, 44, 0, -1, 34));

	private enum Phase { CREATE, ENTER, BUILD, SHOOT, DONE }

	private static Phase phase = Phase.CREATE;
	private static int ticks;
	private static int shot;
	private static int shotTick;
	private static volatile List<Shot> shots;

	private static long seed() {
		return Long.getLong("hemomancy.brinedSeatReview.seed", 42L);
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.brinedSeatReview")) return;
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
						Hemomancy.LOGGER.info("BRINED_SEAT_REVIEW {} shots queued", shots.size());
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
				Hemomancy.LOGGER.info("BRINED_SEAT_REVIEW captured {}", shot);
				mc.stop();
			}
		}
	}

	private static void shoot(Minecraft mc) {
		Shot current = shots.get(shot);
		boolean first = shot == 0 || !shots.get(shot - 1).folder().equals(current.folder());
		int wait = current.chart() ? CHART_WAIT : first ? FIRST_WAIT : WAIT;
		if (shotTick++ == 0) {
			var server = mc.getSingleplayerServer();
			var uuid = mc.player.getUUID();
			if (current.chart()) mc.options.hideGui = false;
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				if (current.chart()) holdChart(player);
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
			Hemomancy.LOGGER.error("BRINED_SEAT_REVIEW could not save {}", out, exception);
		}
		Hemomancy.LOGGER.info("BRINED_SEAT_REVIEW shot {}/{} at {}", current.folder(), current.name(), current.eye());
		shotTick = 0;
		if (++shot >= shots.size()) phase = Phase.DONE;
	}

	/** Creative and flying, a Relay Chart in hand (drawn once, where the player first stands) and the letter beside it. */
	private static void holdChart(net.minecraft.server.level.ServerPlayer player) {
		if (player.gameMode.getGameModeForPlayer() != GameType.CREATIVE) {
			player.setGameMode(GameType.CREATIVE);
			player.getAbilities().flying = true;
			player.onUpdateAbilities();
		}
		var hand = player.getMainHandItem();
		if (!(hand.getItem() instanceof com.vincenthuto.hemomancy.common.item.harbinger.RelayChartItem)) {
			player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,
					new net.minecraft.world.item.ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.brine_sealed_letter.get()));
			hand = new net.minecraft.world.item.ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.relay_chart.get());
			player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, hand);
		}
		var aimed = hand;
		player.server.tell(new net.minecraft.server.TickTask(player.server.getTickCount() + 5, () -> {
			if (com.vincenthuto.hemomancy.common.item.harbinger.CovenantWaybillItem.target(aimed, player.level()) == null)
				aimed.use(player.level(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
		}));
	}

	private static void createWorld(Minecraft mc) {
		// The One Probe draws its tooltip even with the GUI hidden unless it needs a probe item.
		Path probe = mc.gameDirectory.toPath().resolve("config/theoneprobe-common.toml");
		try {
			if (Files.exists(probe)) Files.writeString(probe, Files.readString(probe).replace("needsProbe = 3", "needsProbe = 2"));
		} catch (IOException exception) {
			Hemomancy.LOGGER.warn("BRINED_SEAT_REVIEW could not hide The One Probe's tooltip", exception);
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
		var chartShots = new ArrayList<Shot>();
		if (PelagicWorldgen.context(level.getChunkSource().randomState()) == null) {
			Hemomancy.LOGGER.error("BRINED_SEAT_REVIEW MISSING Pelagic worldgen is off in the review world");
			return out;
		}
		for (var site : List.of(CourtSite.LIGHTHOUSE_GARDEN, CourtSite.VENT_SANCTUM)) {
			var marker = CourtSeatCandidates.nearest(level, site, BlockPos.ZERO, 32).orElse(null);
			if (marker == null) {
				Hemomancy.LOGGER.error("BRINED_SEAT_REVIEW MISSING no {} within 32 rings", site.id());
				continue;
			}
			var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
					.getOrThrow(ResourceKey.create(Registries.STRUCTURE, Hemomancy.rloc(site.id())));
			level.getChunk(marker);
			StructureStart start = level.structureManager().getStructureWithPieceAt(marker, structure);
			if (!start.isValid()) {
				Hemomancy.LOGGER.error("BRINED_SEAT_REVIEW MISSING {} start round {}", site.id(), marker);
				continue;
			}
			BoundingBox box = start.getBoundingBox();
			for (int cx = (box.minX() >> 4) - 2; cx <= (box.maxX() >> 4) + 2; cx++)
				for (int cz = (box.minZ() >> 4) - 2; cz <= (box.maxZ() >> 4) + 2; cz++) level.getChunk(cx, cz);
			var piece = start.getPieces().getFirst();
			Rotation rotation = piece.getRotation();
			var pieceBox = piece.getBoundingBox();
			int cx = (pieceBox.minX() + pieceBox.maxX()) / 2, cz = (pieceBox.minZ() + pieceBox.maxZ()) / 2;
			double baseY = site == CourtSite.LIGHTHOUSE_GARDEN ? CourtSeatLayout.GARDEN_WATERLINE_Y
					: CourtSeatLayout.sanctumMarker(pieceBox).getY();
			Vec3 centre = new Vec3(cx + .5, baseY, cz + .5);
			Hemomancy.LOGGER.info("BRINED_SEAT_REVIEW BUILT {} marker={} centre={} rotation={} pieces={}", site.id(), marker,
					centre, rotation, start.getPieces().size());
			if (site == CourtSite.LIGHTHOUSE_GARDEN) {
				Vec3 stand = new Vec3(centre.x, 76, centre.z - 140);
				var toward = new Shot("relay_chart", "00-facing_the_garden", stand, new Vec3(centre.x, 76, centre.z), 6000L, false, true);
				var away = new Shot("relay_chart", "01-facing_away", stand, stand.add(0, 0, -50), 6000L, false, true);
				chartShots.add(toward);
				chartShots.add(away);
			}
			if (Boolean.getBoolean("hemomancy.brinedSeatReview.chartOnly")) continue;
			var stops = new ArrayList<>(site == CourtSite.LIGHTHOUSE_GARDEN ? GARDEN : SANCTUM);
			if (site == CourtSite.VENT_SANCTUM) stops.addAll(thresholdStops(level, pieceBox, centre, rotation));
			for (var pass : List.of(new Object[] {"day", 6000L, false}, new Object[] {"night", 18000L, false},
					new Object[] {"night_vision", 18000L, true})) {
				for (int i = 0; i < stops.size(); i++) {
					var stop = stops.get(i);
					boolean absolute = stop.name().startsWith("threshold");
					Vec3 eye = absolute ? new Vec3(stop.ex(), stop.ey(), stop.ez()) : frame(centre, rotation, stop.ex(), stop.ey(), stop.ez());
					Vec3 look = absolute ? new Vec3(stop.lx(), stop.ly(), stop.lz()) : frame(centre, rotation, stop.lx(), stop.ly(), stop.lz());
					out.add(new Shot(site.id(), String.format(Locale.ROOT, "%s-%02d-%s", pass[0], i, stop.name()), eye, look,
							(Long) pass[1], (Boolean) pass[2]));
				}
			}
		}
		return withChart(out, chartShots);
	}

	/** The threshold walkway and the tallest head, in world space: they follow the terrain's chimneys. */
	private static List<Stop> thresholdStops(ServerLevel level, BoundingBox startPiece, Vec3 centre, Rotation rotation) {
		var geology = PelagicWorldgen.context(level.getChunkSource().randomState());
		var spots = CourtStructure.heads(geology, startPiece);
		var crowns = spots.stream().filter(s -> s.kind() == VentSanctumHeads.Kind.CROWN).toList();
		var walk = spots.stream().filter(s -> s.kind() == VentSanctumHeads.Kind.WALK_TOP).toList();
		if (crowns.isEmpty() || walk.isEmpty()) return List.of();
		var top = walk.stream().max(Comparator.comparingInt(s -> s.pos().getY())).orElseThrow().pos();
		var foot = walk.stream().min(Comparator.comparingInt(s -> s.pos().getY())).orElseThrow().pos();
		var crown = crowns.stream().min(Comparator.comparingDouble(s -> s.pos().distSqr(top))).orElseThrow().pos();
		return List.of(
				new Stop("threshold_walkway", foot.getX() + .5, foot.getY() + 2.6, foot.getZ() + .5, crown.getX() + .5,
						crown.getY() + .5, crown.getZ() + .5),
				new Stop("threshold_mouth", top.getX() + .5, top.getY() + 2.2, top.getZ() + .5, crown.getX() + .5,
						crown.getY(), crown.getZ() + .5));
	}

	private static List<Shot> withChart(List<Shot> out, List<Shot> chartShots) {
		out.addAll(chartShots);
		return out;
	}

	/** A point in a Seat's own frame (x east, z south from its centre column, y up from its base) turned with it. */
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

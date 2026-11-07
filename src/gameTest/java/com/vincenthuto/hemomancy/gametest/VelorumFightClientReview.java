package com.vincenthuto.hemomancy.gametest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneDirector;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.HarbingerSaintEncounterHooks;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumDevoteeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumOpeningRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumStillnessRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.IntConsumer;

/**
 * Opt-in native review of Velorum on the Saint kit ({@code runBossPresentationReviewClient -PvelorumFightReview=true}),
 * in his real chamber: the mausoleum's {@code velorum/chamber} template is placed on the stage, so the congregation, the
 * rime lanterns, the blue-ice ring and the tomb are the generated ones. The fight runs in order: the chamber at rest, the
 * rise out of the sarcophagus (the encounter's own spawn), phase 1's moves each forced over its whole timeline, the three
 * openings (the Gutter after the Long Night and fed by held Projection, the Overheld Invocation struck out of Bid to
 * Kneel, the Bow with held Absorption on the seam), a preserved player, the meter under the bar at five states, a
 * devotee woken by held Projection, the 60% floor with its Bow and card, phase 2's moves, the 25% floor with its card
 * and the Endless Hour (every rime lantern dark), phase 3's moves, a forced Call to Prayer with hostile devotees, the
 * ending and the devotees walking home, and the chamber checked block for block against the template.
 * <p>
 * A survival quarry (with a player-info entry, so the client draws them) is the target; the review player is a Creative
 * challenger whose own stillness is held at zero outside the HUD stops. Minimal particles, Bright gamma, no hand.
 * Stills: {@code velorum-fight-NN-<scene>-TTT.png} in the game dir; the report is {@code operator/velorum-fight-review.json}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class VelorumFightClientReview {
	/** A camera: the eye and the point it looks at, both chamber-local. */
	private record View(double ex, double ey, double ez, double tx, double ty, double tz) {
	}

	private record Scene(String name, int length, View view, boolean card, int[] captures, Runnable enter,
			IntConsumer during) {
	}

	/** The chamber's local origin in the review world. */
	private static final BlockPos ORIGIN = new BlockPos(1883, 199, 587);
	private static final BlockPos TOMB = ORIGIN.offset(30, 4, 13);
	/** Where Velorum is stood for every move: the centre of the floor's blue-ice ring, facing north (the camera). */
	private static final double BX = 16.5D, BZ = 13.5D;
	/** The aisle figure the devotee scene wakes: the north row, x 14. */
	private static final BlockPos FIGURE = ORIGIN.offset(14, 1, 4);
	// the floor runs x 9 to 23 and z 6 to 20; the aisles behind the pillar rows hold the kneeling congregation at z 4 and
	// 22; the entrance passage is x 0 to 8, the dais and the tomb x 27 on
	private static final View REST = new View(4.5, 7.5, 13.5, 26.5, 2.0, 13.5);
	private static final View RISE = new View(23.0, 5.0, 13.5, 30.5, 5.6, 13.5);
	// the close and wide views aim a little beside him, so the crosshair's block-and-entity readout stays off the stills
	private static final View CLOSE = new View(14.6, 3.0, 9.4, 17.6, 2.5, 13.5);
	// the high views keep the camera's body clear of the north arcade (blocks at z 5): inside it the client pushes the
	// camera out over several ticks and the first stills come out dark
	private static final View MID = new View(16.5, 8.0, 6.5, 16.5, 1.0, 14.0);
	private static final View HIGH = new View(16.5, 7.8, 6.6, 16.5, 1.0, 15.5);
	private static final View PORTRAIT = new View(14.6, 3.2, 10.0, 16.5, 2.8, 13.5);
	/** From his left side, to see the rear arc of the Hanging Lantern and the quarry behind him. */
	private static final View SIDE = new View(9.5, 3.8, 14.5, 16.5, 1.8, 14.5);
	private static final View QUARRY = new View(14.8, 3.0, 7.4, 17.0, 1.3, 10.5);
	private static final View AISLE = new View(18.6, 3.4, 9.8, 14.5, 1.6, 4.5);
	private static final View WIDE = new View(6.0, 8.0, 13.5, 21.0, 1.0, 13.5);
	private static final View ENDING = new View(13.8, 4.0, 9.0, 16.5, 2.2, 13.5);
	/** Three-quarters from his front left, for the limb-driven moves: the lantern, the claw and the staff in profile. */
	private static final View THREE_QUARTER = new View(12.9, 3.1, 10.1, 17.3, 2.0, 13.9);
	/** A player's eye on the floor, off his front left, looking across the lanes and the rings. */
	private static final View LOW = new View(11.0, 2.62, 8.4, 17.0, 1.0, 10.5);
	/** High over the west of the floor, to see a whole field or a lane run out and the hazard it leaves. */
	private static final View FAR = new View(9.0, 9.5, 11.5, 17.0, 0.5, 11.5);
	/**
	 * The Procession hunts the challenger he cannot see, and in the review that is the camera itself, so its road runs
	 * under the camera's feet across his line: this view stands high over the south half of the floor and looks almost
	 * straight down onto the road as it runs across the frame.
	 */
	private static final View ROAD = new View(16.5, 8.5, 16.8, 16.5, 0.5, 18.4);
	/**
	 * High behind him looking north over his shoulder to the quarry and the north aisle's figures: the Veins of Frost run
	 * from those figures to the quarry, and the Shadow Step rises among them, so both ends of each read in one frame.
	 */
	private static final View NORTH = new View(16.5, 8.5, 16.2, 16.5, 0.5, 7.0);
	/** Across the line from him to the quarry, from the west: the javelins fly across the frame, form to lodge. */
	private static final View ACROSS = new View(9.8, 4.4, 10.2, 17.0, 1.2, 10.2);
	private static final IntConsumer NONE = tick -> {
	};
	private static final JsonObject RESULTS = new JsonObject();
	private static final JsonArray REACHED = new JsonArray();
	private static final List<Scene> SCENES = new ArrayList<>();
	private static final Map<BlockPos, BlockState> AUTHORED = new HashMap<>();
	private static volatile int scene = -1, tick;
	private static volatile boolean done;
	private static boolean launched;
	private static int clientScene = -1, nextCapture, closing, preparation, rewardsBefore, shots;
	private static int minFps = Integer.MAX_VALUE;
	/** The quarry's stillness held at most this in the current scene (the scenes that preserve them raise it). */
	private static float quarryCeiling = 55.0F;
	/** The camera's own stillness, held at this every tick (the HUD scenes set it). */
	private static float cameraStillness;
	private static MinecraftServer server;
	private static ServerPlayer player;
	private static ServerPlayer quarry;
	private static EmbeddedChannel channel;
	private static VelorumEntity boss;
	private static ParticleStatus originalParticles;
	private static double originalGamma = 0.5D;
	private static Path report;

	private VelorumFightClientReview() {
	}

	@SubscribeEvent
	public static void client(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.velorumFightReview")) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null && mc.screen instanceof AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
		}
		if (!launched && mc.player != null && mc.level != null && mc.getSingleplayerServer() != null) {
			launched = true;
			server = mc.getSingleplayerServer();
			originalParticles = mc.options.particles().get();
			mc.options.particles().set(switch (System.getProperty("hemomancy.velorumFightReview.particles", "all")) {
				case "minimal" -> ParticleStatus.MINIMAL;
				case "decreased" -> ParticleStatus.DECREASED;
				default -> ParticleStatus.ALL;
			});
			// bright gamma, as the mausoleum review: the chamber is lit only by its rime lanterns and sea lanterns
			originalGamma = mc.options.gamma().get();
			mc.options.gamma().set(1.0D);
			mc.options.pauseOnLostFocus = false;
			mc.options.hideGui = false;
			report = mc.gameDirectory.toPath().resolve("operator/velorum-fight-review.json");
			UUID id = mc.player.getUUID();
			server.execute(() -> {
				try {
					player = server.getPlayerList().getPlayer(id);
					prepare();
				} catch (Exception error) {
					fail(error);
				}
			});
		}
		if (!launched) return;
		int current = scene, frame = tick;
		// the cards keep their own director; every other scene keeps the fixed review camera
		if (current < 0 || current >= SCENES.size() || !SCENES.get(current).card()) BossSceneDirector.INSTANCE.clear();
		if (current >= 0 && current < SCENES.size()) {
			if (current != clientScene) {
				if (clientScene >= 0 && clientScene < SCENES.size()) {
					// frame cost: the lowest frame rate the client showed in the scene, and how many stills it missed
					Hemomancy.LOGGER.info("VELORUM_FIGHT_REVIEW fps {} min {} missed {}", SCENES.get(clientScene).name(), minFps,
							SCENES.get(clientScene).captures().length - nextCapture);
				}
				clientScene = current;
				nextCapture = 0;
				minFps = Integer.MAX_VALUE;
			}
			if (frame > 4) minFps = Math.min(minFps, mc.getFps());
			int[] captures = SCENES.get(current).captures();
			if (nextCapture < captures.length && frame >= captures[nextCapture]) {
				nextCapture++;
				shots++;
				String name = String.format("velorum-fight-%02d-%s-%03d.png", current, SCENES.get(current).name(), frame);
				Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), message -> {
				});
			}
		}
		if (done && ++closing > 24) {
			mc.options.particles().set(originalParticles);
			mc.options.gamma().set(originalGamma);
			mc.stop();
		}
	}

	/** The camera sees the body, not the review player's hand. */
	@SubscribeEvent
	public static void hand(RenderHandEvent event) {
		if (Boolean.getBoolean("hemomancy.velorumFightReview") && launched && !done) event.setCanceled(true);
	}

	private static void prepare() {
		if (player == null) throw new IllegalStateException("Review player is missing");
		ServerLevel level = player.serverLevel();
		forceChunks(level, true);
		level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, new AABB(ORIGIN).inflate(48),
				e -> !(e instanceof Player)).forEach(net.minecraft.world.entity.Entity::discard);
		var template = level.getStructureManager().getOrCreate(Hemomancy.rloc("mausoleum/velorum/chamber"));
		template.placeInWorld(level, ORIGIN, ORIGIN, new StructurePlaceSettings(), level.random, 2);
		if (!(level.getBlockState(TOMB).getBlock() instanceof SaintSarcophagusBlock))
			throw new IllegalStateException("the chamber template has no sarcophagus at " + TOMB);
		level.setDayTime(18000);
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String command : new String[]{"gamerule doDaylightCycle false", "gamerule naturalRegeneration false",
				"gamerule doMobSpawning false", "gamerule doFireTick false", "weather clear", "difficulty normal"}) {
			server.getCommands().performPrefixedCommand(source, command);
		}
		player.setGameMode(GameType.CREATIVE);
		// an empty hand: the camera sees the body, not a held item
		player.getInventory().selected = 8;
		player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(8));
		view(REST);
		preparation = 40;
	}

	private static void forceChunks(ServerLevel level, boolean force) {
		for (int cx = ORIGIN.getX() >> 4; cx <= (ORIGIN.getX() + 36) >> 4; cx++) {
			for (int cz = ORIGIN.getZ() >> 4; cz <= (ORIGIN.getZ() + 26) >> 4; cz++) level.setChunkForced(cx, cz, force);
		}
	}

	private static void setup() {
		ServerLevel level = player.serverLevel();
		// the chamber as it stands once its blocks have ticked (the sarcophagus lays its fillers on its first tick)
		AUTHORED.clear();
		for (BlockPos pos : BlockPos.betweenClosed(ORIGIN, ORIGIN.offset(36, 16, 26))) {
			AUTHORED.put(pos.immutable(), level.getBlockState(pos));
		}
		var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "StillQuarry"), false);
		quarry = new ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		channel = new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(server, connection, quarry, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
			}
		};
		quarry.setGameMode(GameType.SURVIVAL);
		quarry.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0D);
		quarry.setHealth(1000.0F);
		place(quarry, 22.0, 13.5);
		// a player-info entry first, so the client draws the quarry and the rime on them
		player.connection.send(net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
				.createPlayerInitializing(List.of(quarry)));
		level.addNewPlayer(quarry);
		var blood = HemoCapabilityAccess.getBloodVolume(quarry);
		blood.setActive(true);
		blood.setMaxBloodVolume(10_000);
		blood.setBloodVolume(10_000);
		buildScenes();
		scene = 0;
		tick = 0;
		Hemomancy.LOGGER.info("VELORUM_FIGHT_REVIEW started in the chamber, particles {}, {} scenes",
				System.getProperty("hemomancy.velorumFightReview.particles", "all"), SCENES.size());
	}

	// ── the scenes ───────────────────────────────────────────────────

	private static void buildScenes() {
		SCENES.clear();
		SCENES.add(new Scene("chamber_at_rest", 16, REST, false, new int[]{10}, () -> {
		}, NONE));
		SCENES.add(new Scene("rise", 90, RISE, false, new int[]{8, 30, 52, 74, 88}, VelorumFightClientReview::rise, NONE));
		SCENES.add(new Scene("idle_hour", 36, CLOSE, false, new int[]{14, 32}, () -> reset(17.0, 10.0), NONE));
		SCENES.add(new Scene("portrait", 24, PORTRAIT, false, new int[]{8, 20}, () -> reset(18.5, 9.0), NONE));
		for (VelorumMove move : List.of(VelorumMove.STAFF_TOLL, VelorumMove.LANTERN_SWEEP, VelorumMove.HANGING_LANTERN,
				VelorumMove.CRUOR_RELEASE, VelorumMove.STILL_FLOOR, VelorumMove.BID_TO_KNEEL, VelorumMove.SET_IN_PLACE,
				VelorumMove.SILENCE_DRAW, VelorumMove.ARRESTED_PULSE)) {
			moveScene(move, 1, "");
		}
		// Arrested Pulse on a walking challenger: each beat passes and the wrist frost cracks off
		SCENES.add(new Scene("pulse_pass", 140, QUARRY, false, every(3, 140), () -> {
			reset(15.0, 10.4);
			force(VelorumMove.ARRESTED_PULSE);
		}, t -> {
			// a slow pace back and forth across his front, a little over walking pace (0.16 a tick)
			double run = (t * 0.16D) % 10.0D;
			double x = 15.0D + (run < 5.0D ? run : 10.0D - run);
			quarry.moveTo(ORIGIN.getX() + x, ORIGIN.getY() + 1, ORIGIN.getZ() + 10.4D, 180.0F, 0.0F);
			if (boss.stillness().marked(quarry)) RESULTS.addProperty("pulse_marked", true);
		}));
		// the Long Night always ends in the Guttering Lantern; the pillar lanterns go dark
		SCENES.add(new Scene("long_night", 90, MID, false, every(3, 90), () -> {
			reset(17.0, 7.0);
			force(VelorumMove.LONG_NIGHT);
		}, t -> {
			if (t == 44) RESULTS.addProperty("long_night_pillars_dark", darkLanterns());
		}));
		// the Long Night from high over the floor: the dark runs out into four pools in the pillars' shadow, and the Gutter
		// floods it to his feet
		SCENES.add(new Scene("dark_pools_gutter_flood", 150, FAR, false, every(3, 150), () -> {
			reset(17.0, 7.0);
			force(VelorumMove.LONG_NIGHT);
		}, NONE));
		SCENES.add(new Scene("gutter_fed", 60, CLOSE, false, new int[]{6, 24, 44}, () -> {
			reset(17.0, 10.5);
			boss.openings().open(VelorumOpeningRules.GUTTER, VelorumOpeningRules.GUTTER.ticks());
		}, t -> {
			if (t >= 4 && boss.openings().isOpen()) {
				look(quarry, boss.position().add(0.0D, 1.6D, 0.0D));
				SaintChannelRouter.tryProject(quarry.level(), quarry, 8.0D, 5.0D, false);
			}
		}));
		// Bid to Kneel struck three times in its hold: the Overheld Invocation
		SCENES.add(new Scene("overheld", 90, CLOSE, false, new int[]{20, 34, 40, 52, 76}, () -> {
			reset(17.0, 8.0);
			force(VelorumMove.BID_TO_KNEEL);
		}, t -> {
			if (boss.controller().move() == VelorumMove.BID_TO_KNEEL
					&& boss.controller().stage() == BossMoveTimeline.Stage.ACTIVE && t % 3 == 0) strike(4.0F);
		}));
		SCENES.add(new Scene("bow_absorbed", 70, CLOSE, false, new int[]{6, 20, 40, 62}, () -> {
			reset(17.0, 10.5);
			boss.openings().open(VelorumOpeningRules.BOW, VelorumOpeningRules.BOW.ticks());
		}, t -> {
			if (t >= 4 && boss.openings().isOpen()) {
				look(quarry, boss.position().add(0.0D, 1.9D, 0.0D));
				SaintChannelRouter.tryAbsorb(quarry.level(), quarry, 8.0D, 5.0D, false);
			}
		}));
		SCENES.add(new Scene("preserved_player", 44, QUARRY, false, new int[]{4, 20, 40}, () -> {
			reset(17.0, 10.0);
			quarryCeiling = VelorumStillnessRules.MAX;
			boss.meter().add(quarry, VelorumStillnessRules.MAX);
		}, NONE));
		for (int fill : new int[]{0, 40, 70, 95}) {
			SCENES.add(new Scene("hud_" + fill, 14, CLOSE, false, new int[]{10}, () -> {
				reset(17.0, 10.0);
				cameraStillness = fill;
			}, NONE));
		}
		SCENES.add(new Scene("hud_halted", 14, CLOSE, false, new int[]{10}, () -> {
			reset(17.0, 10.0);
			cameraStillness = 55.0F;
			boss.openings().open(VelorumOpeningRules.GUTTER, 40);
		}, NONE));
		// a frozen figure in the north aisle woken by held Projection; the devotee thaws, stands and walks to him
		SCENES.add(new Scene("devotee_wake", 120, AISLE, false, new int[]{10, 30, 44, 56, 76, 110}, () -> {
			reset(17.0, 10.0);
			place(quarry, 15.5, 8.6);
		}, t -> {
			if (t >= 4 && t < 60 && !player.serverLevel().getBlockState(FIGURE).isAir()) {
				look(quarry, Vec3.atCenterOf(FIGURE));
				SaintChannelRouter.tryProject(quarry.level(), quarry, 8.0D, 5.0D, false);
			}
			if (t == 100) RESULTS.addProperty("devotee_woken", devotees().stream().anyMatch(d -> !d.hostile()));
		}));
		// the 60% floor: blows outside an opening stop at it, he bows, and the card plays as the Bow ends
		SCENES.add(new Scene("floor_sixty", 140, CLOSE, true, new int[]{4, 24, 50, 76, 90, 110, 130}, () -> {
			reset(17.0, 10.0);
			strikeToFloor();
		}, t -> {
			if (t == 139) RESULTS.addProperty("phase_two", boss.phases().phase() == 2);
		}));
		SCENES.add(new Scene("idle_congregation", 30, PORTRAIT, false, new int[]{20}, () -> reset(18.5, 9.0), NONE));
		for (VelorumMove move : List.of(VelorumMove.LANTERN_SWEEP, VelorumMove.RECALL, VelorumMove.PROCESSION,
				VelorumMove.SPLINTER_CROWN, VelorumMove.CRUOR_JAVELINS, VelorumMove.VEINS_OF_FROST, VelorumMove.UMBRAL_STEP,
				VelorumMove.GLOAM_CUTS)) {
			moveScene(move, 2, "p2_");
		}
		// the javelins at a challenger standing behind a frozen figure: the shafts shatter on its rime
		SCENES.add(new Scene("javelins_on_cover", 80, ACROSS, false, every(3, 80), () -> {
			reset(17.5, 6.8);
			Vec3 behind = behindFigure();
			if (behind != null) place(quarry, behind.x, behind.z);
			force(VelorumMove.CRUOR_JAVELINS);
		}, NONE));
		// the 25% floor: the Bow, the Endless Hour's card, and the Hour itself at the ring's centre (every rime lantern
		// dark), closing unbroken into the Gutter at half length
		SCENES.add(new Scene("floor_quarter", 130, CLOSE, true, every(3, 130), () -> {
			reset(17.0, 10.0);
			strikeToFloor();
		}, t -> {
			if (t == 129) RESULTS.addProperty("phase_three", boss.phases().phase() == 3);
		}));
		SCENES.add(new Scene("endless_hour", 300, WIDE, false, new int[]{10, 40, 80, 140, 200, 250, 275, 296}, () -> {
			place(quarry, 17.0, 9.0);
			quarryCeiling = 45.0F;
		}, t -> {
			if (boss.hourHolding()) {
				RESULTS.addProperty("hour_held", true);
				if (!RESULTS.has("hour_lanterns_dark")) RESULTS.addProperty("hour_lanterns_dark", darkLanterns());
			}
			// the quarry keeps moving in a slow circle, as the counterplay asks
			double angle = t * 0.03D;
			quarry.moveTo(ORIGIN.getX() + BX + Math.cos(angle) * 4.5D, ORIGIN.getY() + 1, ORIGIN.getZ() + BZ - Math.sin(angle) * 4.5D);
		}));
		for (VelorumMove move : List.of(VelorumMove.HOLLOW_TOLL, VelorumMove.CRUOR_RELEASE, VelorumMove.STAFF_TOLL,
				VelorumMove.GLOAM_CUTS, VelorumMove.CRUOR_JAVELINS)) {
			moveScene(move, 3, "p3_");
		}
		// the Eclipse unbroken: the lights out, the red seam grid, the shapes standing out of the figures and reaching; the
		// quarry held at 70 so the light seeping back preserves them
		SCENES.add(new Scene("eclipse_unbroken", 250, WIDE, false, every(3, 250), () -> {
			reset(17.5, 7.2);
			quarryCeiling = 70.0F;
			force(VelorumMove.ECLIPSE);
		}, t -> {
			if (boss.eclipseHolding()) {
				RESULTS.addProperty("eclipse_held", true);
				if (!RESULTS.has("eclipse_lanterns_dark")) RESULTS.addProperty("eclipse_lanterns_dark", darkLanterns());
			}
			if (boss.stillness().preserved(quarry)) RESULTS.addProperty("eclipse_preserved", true);
		}));
		// from a player's eye on the floor: the floor must stay readable by its red seam grid; relit late by Projection
		SCENES.add(new Scene("eclipse_dark_low", 180, LOW, false, every(3, 180), () -> {
			reset(15.5, 7.6);
			force(VelorumMove.ECLIPSE);
		}, t -> relight(t, 116)));
		// the shapes in the north aisle reaching for a challenger who stands among them
		SCENES.add(new Scene("eclipse_reach", 215, AISLE, false, every(3, 215), () -> {
			reset(16.0, 6.6);
			force(VelorumMove.ECLIPSE);
		}, t -> relight(t, 156)));
		// relit early by held Projection into his closed claw: the light bursts through, the dark tears back, the Gutter
		SCENES.add(new Scene("eclipse_relit", 160, CLOSE, false, every(3, 160), () -> {
			reset(17.0, 10.5);
			force(VelorumMove.ECLIPSE);
		}, t -> relight(t, 66)));
		// a forced challenger: Call to Prayer raises figures against them, and the risen grip the quarry
		SCENES.add(new Scene("forced_call", 110, MID, false, every(3, 110), () -> {
			reset(17.0, 8.5);
			boss.setAwakening("FORCED", List.of());
			force(VelorumMove.CALL_TO_PRAYER);
		}, t -> {
			if (t == 108) RESULTS.addProperty("hostiles_risen", devotees().stream().filter(VelorumDevoteeEntity::hostile).count());
		}));
		// the ending: a blow inside the Gutter fails his body; the frost falls and the devotees walk home
		SCENES.add(new Scene("ending", 96, ENDING, false, new int[]{4, 14, 28, 44, 64, 90}, () -> {
			reset(17.0, 10.0);
			rewardsBefore = residuum();
			boss.openings().open(VelorumOpeningRules.GUTTER, VelorumOpeningRules.GUTTER.ticks());
			strike(100_000.0F);
		}, NONE));
		SCENES.add(new Scene("ending_walk", 130, WIDE, false, new int[]{10, 40, 70, 100, 125}, () -> {
		}, NONE));
		SCENES.add(new Scene("restored", 24, REST, false, new int[]{20}, () -> {
		}, NONE));
		// -PvelorumReviewOnly: the rise (which wakes him), the named scenes, and the floors that turn the phase before a
		// kept scene of a later phase, for quick visual rounds
		if (!ONLY.isEmpty()) {
			List<Scene> kept = new ArrayList<>();
			for (int i = 0; i < SCENES.size(); i++) {
				Scene candidate = SCENES.get(i);
				boolean named = ONLY.stream().anyMatch(candidate.name()::contains);
				boolean turn = (candidate.name().equals("floor_sixty") && keptBetween(i, null))
						|| (candidate.name().equals("floor_quarter") && keptBetween(i, null));
				if (i <= 1 || named || turn) kept.add(candidate);
			}
			SCENES.clear();
			SCENES.addAll(kept);
		}
	}

	private static final List<String> ONLY = java.util.Arrays.stream(
			System.getProperty("hemomancy.velorumFightReview.only", "").split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();

	/** Whether a scene named by the filter comes after {@code from} (and before the scene named {@code until}). */
	private static boolean keptBetween(int from, String until) {
		for (int i = from + 1; i < SCENES.size(); i++) {
			String name = SCENES.get(i).name();
			if (until != null && name.equals(until)) return false;
			if (ONLY.stream().anyMatch(name::contains)) return true;
		}
		return false;
	}

	/** Ticks a move scene waits after its camera moves before the move is forced, so its first frames are not black. */
	private static final int LEAD = 6;
	/** Moves a limb drives: shot again from three-quarters, so the lantern, claw and staff read against the cut. */
	private static final java.util.Set<VelorumMove> SIDE_MOVES = java.util.EnumSet.of(VelorumMove.STAFF_TOLL,
			VelorumMove.LANTERN_SWEEP, VelorumMove.HANGING_LANTERN, VelorumMove.CRUOR_RELEASE, VelorumMove.SET_IN_PLACE,
			VelorumMove.HOLLOW_TOLL, VelorumMove.BID_TO_KNEEL, VelorumMove.SILENCE_DRAW, VelorumMove.ARRESTED_PULSE,
			VelorumMove.CRUOR_JAVELINS, VelorumMove.VEINS_OF_FROST, VelorumMove.UMBRAL_STEP, VelorumMove.GLOAM_CUTS);
	/** Moves whose floor work a player meets at eye height: shot again from the floor. */
	private static final java.util.Set<VelorumMove> LOW_MOVES = java.util.EnumSet.of(VelorumMove.CRUOR_RELEASE,
			VelorumMove.STILL_FLOOR, VelorumMove.HOLLOW_TOLL, VelorumMove.STAFF_TOLL, VelorumMove.GLOAM_CUTS,
			VelorumMove.CRUOR_JAVELINS);
	/** Moves whose field is wider than the near cameras: shot again from high over the floor. */
	private static final java.util.Set<VelorumMove> FAR_MOVES = java.util.EnumSet.of(VelorumMove.STILL_FLOOR,
			VelorumMove.HOLLOW_TOLL, VelorumMove.CRUOR_RELEASE, VelorumMove.VEINS_OF_FROST, VelorumMove.UMBRAL_STEP);

	/** Capture ticks every {@code step} from the first, so wind-up, commit, follow-through and settle each land in a frame. */
	private static int[] every(int step, int length) {
		int[] ticks = new int[(length - 2) / step + 1];
		for (int i = 0; i < ticks.length; i++) ticks[i] = 1 + i * step;
		return ticks;
	}

	/** A forced move from its own camera, then again from three-quarters, from the floor and from high, as it asks. */
	private static void moveScene(VelorumMove move, int phase, String prefix) {
		moveScene(move, phase, prefix, "", view(move));
		if (SIDE_MOVES.contains(move)) moveScene(move, phase, prefix, "_side", THREE_QUARTER);
		if (LOW_MOVES.contains(move)) moveScene(move, phase, prefix, "_low", LOW);
		if (FAR_MOVES.contains(move)) moveScene(move, phase, prefix, "_far", FAR);
	}

	private static View view(VelorumMove move) {
		return switch (move) {
			case STAFF_TOLL, LANTERN_SWEEP -> CLOSE;
			case SET_IN_PLACE, ARRESTED_PULSE -> QUARRY;
			case UMBRAL_STEP, VEINS_OF_FROST -> NORTH;
			case GLOAM_CUTS -> ACROSS;
			case HANGING_LANTERN -> SIDE;
			// since each scene hands him the quarry, the road runs toward the quarry in the north half, not under the camera
			case PROCESSION -> NORTH;
			case CRUOR_RELEASE, SPLINTER_CROWN -> HIGH;
			case CRUOR_JAVELINS -> ACROSS;
			default -> MID;
		};
	}

	/** One forced move shot every third tick over its whole timeline: tell, commit, follow-through and settle. */
	private static void moveScene(VelorumMove move, int phase, String prefix, String suffix, View view) {
		BossMoveTimeline timeline = SaintControllerRules.effective(move.kind(), move.timeline(new SaintMoveInputs(phase, 0, 0, 2)));
		int w = timeline.windup(), a = timeline.active(), r = timeline.recovery();
		int start = LEAD;
		// the Black-Ice lanes stand as a hazard for 60 ticks past the move: the scene runs on to see it grey and heal
		// Arrested Pulse beats 30, 60 and 90 past its commit; the javelins lodge 40 and shatter over 10; the Step's pools stay
		int tail = switch (move) {
			case CRUOR_RELEASE, CRUOR_JAVELINS -> 66;
			case ARRESTED_PULSE -> 100;
			case UMBRAL_STEP, GLOAM_CUTS -> 50;
			default -> Math.min(r, 34) + 10;
		};
		int length = Math.min(start + w + a + tail, 200);
		int[] captures = every(3, length - LEAD);
		for (int i = 0; i < captures.length; i++) captures[i] += LEAD;
		double[] quarryAt = switch (move) {
			case STAFF_TOLL -> new double[]{17.4, 11.3};
			case LANTERN_SWEEP -> new double[]{18.0, 10.4};
			case HANGING_LANTERN -> new double[]{16.8, 16.0};
			case SET_IN_PLACE -> new double[]{17.0, 10.0};
			case ARRESTED_PULSE -> new double[]{17.2, 10.4};
			case GLOAM_CUTS -> new double[]{16.8, 9.0};
			case STILL_FLOOR, SPLINTER_CROWN, HOLLOW_TOLL -> new double[]{18.5, 8.5};
			case RECALL -> new double[]{12.0, 9.0};
			case PROCESSION -> new double[]{20.5, 8.0};
			default -> new double[]{17.5, 6.8};
		};
		Runnable stage = () -> {
			reset(quarryAt[0], quarryAt[1]);
			if (move == VelorumMove.SET_IN_PLACE) {
				quarryCeiling = VelorumStillnessRules.MAX;
				boss.meter().add(quarry, VelorumStillnessRules.MAX);
			}
			if (move == VelorumMove.STILL_FLOOR || move == VelorumMove.BID_TO_KNEEL) quarryCeiling = 70.0F;
		};
		SCENES.add(new Scene(prefix + move.key() + suffix, length, view, false, captures, stage, t -> {
			if (t == LEAD) {
				stage.run();
				force(move);
			}
			// the Pulse's still challenger stands where the beats find them: a burst's blow would otherwise throw them out
			// of frame before the next beat
			if (move == VelorumMove.ARRESTED_PULSE && t > LEAD) place(quarry, quarryAt[0], quarryAt[1]);
		}));
	}

	// ── beats ────────────────────────────────────────────────────────

	/** The encounter's own spawn out of the sarcophagus, as the block runs it, then the block cleared. */
	private static void rise() {
		ServerLevel level = player.serverLevel();
		place(quarry, 22.0, 13.5);
		if (!HarbingerSaintEncounterHooks.spawnSaintBoss(level, TOMB, quarry, EnumSaintType.VELORUM))
			throw new IllegalStateException("the sarcophagus did not wake Velorum");
		level.setBlockAndUpdate(TOMB, Blocks.AIR.defaultBlockState());
		boss = level.getEntitiesOfClass(VelorumEntity.class, new AABB(TOMB).inflate(6)).stream().findFirst()
				.orElseThrow(() -> new IllegalStateException("Velorum did not rise"));
		RESULTS.addProperty("rise_bound_to_tomb", TOMB.equals(boss.getTombPos()));
		RESULTS.addProperty("congregation_found", boss.congregation().figures().size());
	}

	/** Velorum at the ring's centre facing north, quiet, no opening; the quarry at (x, z) with a little stillness. */
	private static void reset(double quarryX, double quarryZ) {
		boss.controller().cancel();
		boss.openings().close();
		// every scene forces its move: a one-tick opening of no kind restarts his opening clock, so his own state never
		// calls the Long Night across the review's long run of forced moves (the dark pool that rose late in pass 2)
		boss.openings().open(new SaintOpening.Spec(-1, "review_reset", 1, 1.0F, 0, 0, java.util.Set.of()), 1);
		boss.openings().close();
		boss.controller().delay(100_000);
		boss.moveTo(origin(BX, 1.0, BZ), 180.0F, 0.0F);
		boss.setYHeadRot(180.0F);
		boss.yBodyRot = 180.0F;
		boss.setDeltaMovement(Vec3.ZERO);
		boss.stillness().release(boss, quarry);
		place(quarry, quarryX, quarryZ);
		quarry.setHealth(1000.0F);
		quarry.removeAllEffects();
		HemoCapabilityAccess.getBloodVolume(quarry).setBloodVolume(10_000);
		quarryCeiling = 55.0F;
		cameraStillness = 0.0F;
		// the stillest challenger in sight is his target: the quarry, never the camera
		setStillness(quarry, 12.0F);
		targetQuarry();
		standAsideAllies();
		look(quarry, boss.position().add(0.0D, 2.0D, 0.0D));
	}

	/** From {@code from} ticks on, the quarry holds Blood Projection on him; in the Eclipse's hold it feeds the cage. */
	private static void relight(int t, int from) {
		if (boss.eclipseRelit()) RESULTS.addProperty("eclipse_relit", true);
		if (t < from || !boss.eclipseHolding()) return;
		look(quarry, boss.position().add(0.0D, 2.4D, 0.0D));
		SaintChannelRouter.tryProject(quarry.level(), quarry, 8.0D, 5.0D, false);
	}

	/** A spot 1.2 blocks behind the north aisle figure nearest (17.5, 4), away from him, chamber-local; or null. */
	private static Vec3 behindFigure() {
		ServerLevel level = player.serverLevel();
		Vec3 best = null;
		double bestDistance = Double.MAX_VALUE;
		for (BlockPos pos : BlockPos.betweenClosed(ORIGIN.offset(9, 1, 2), ORIGIN.offset(24, 1, 8))) {
			if (!level.getBlockState(pos).is(BlockInit.frozen_devotee.get())) continue;
			double x = pos.getX() - ORIGIN.getX() + 0.5D, z = pos.getZ() - ORIGIN.getZ() + 0.5D;
			double d = Math.hypot(x - 17.5D, z - 4.0D);
			if (d < bestDistance) {
				bestDistance = d;
				best = new Vec3(x, 0.0D, z);
			}
		}
		if (best == null) return null;
		Vec3 away = new Vec3(best.x - BX, 0.0D, best.z - BZ).normalize();
		return best.add(away.scale(1.2D));
	}

	/**
	 * He keeps a target in sight for good, and the HUD scenes raise the camera's own stillness, so after them his target
	 * was the camera and every later move (the Pulse's mark, the javelins, the veins) went at the lens. Each scene hands
	 * his controller the quarry again; review only.
	 */
	private static void targetQuarry() {
		try {
			java.lang.reflect.Field target = SaintCombatController.class.getDeclaredField("target");
			target.setAccessible(true);
			target.set(boss.controller(), quarry);
		} catch (ReflectiveOperationException error) {
			throw new IllegalStateException("the review could not hand him the quarry", error);
		}
	}

	/**
	 * A devotee the review woke and then Recall stilled stands at his feet for the rest of the fight, in front of every
	 * near camera, and catches the quarry's Projection meant for him; each scene stands it at the floor's south-west
	 * corner instead. Review only: its state is untouched.
	 */
	private static void standAsideAllies() {
		for (VelorumDevoteeEntity devotee : devotees()) {
			if (devotee.hostile() || devotee.distanceToSqr(boss) > 5.0D * 5.0D) continue;
			devotee.moveTo(ORIGIN.getX() + 10.5D, ORIGIN.getY() + 1, ORIGIN.getZ() + 19.0D, 135.0F, 0.0F);
			devotee.setDeltaMovement(Vec3.ZERO);
		}
	}

	private static void force(VelorumMove move) {
		boss.controller().start(move, SaintCombatController.Cut.IMMEDIATE);
	}

	private static void strike(float amount) {
		boss.invulnerableTime = 0;
		boss.hurt(boss.damageSources().playerAttack(quarry), amount);
	}

	/** Blows outside an opening until he bows at the next phase floor (the Still Guard holds his health on it). */
	private static void strikeToFloor() {
		for (int i = 0; i < 60 && !boss.openings().isOpen(); i++) strike(60.0F);
		SaintOpening.Spec open = boss.openings().current();
		RESULTS.addProperty("floor_bow_at_" + Math.round(boss.getHealth() / boss.getMaxHealth() * 100.0F),
				open != null && open.id() == VelorumOpeningRules.BOW_ID);
	}

	private static void setStillness(Player body, float value) {
		boss.meter().add(body, value - boss.meter().player(body).value());
	}

	private static List<VelorumDevoteeEntity> devotees() {
		return player.serverLevel().getEntitiesOfClass(VelorumDevoteeEntity.class, new AABB(ORIGIN).expandTowards(36, 16, 26));
	}

	/** Rime lanterns the template laid that are now packed ice (dark). */
	private static int darkLanterns() {
		int dark = 0;
		ServerLevel level = player.serverLevel();
		for (var entry : AUTHORED.entrySet()) {
			if (entry.getValue().is(BlockInit.rime_lantern.get()) && level.getBlockState(entry.getKey()).is(Blocks.PACKED_ICE)) dark++;
		}
		return dark;
	}

	private static Vec3 origin(double x, double y, double z) {
		return new Vec3(ORIGIN.getX() + x, ORIGIN.getY() + y, ORIGIN.getZ() + z);
	}

	/** Stands a player on the boss floor at chamber-local (x, z). */
	private static void place(ServerPlayer body, double x, double z) {
		body.moveTo(ORIGIN.getX() + x, ORIGIN.getY() + 1, ORIGIN.getZ() + z, 180.0F, 0.0F);
		body.setDeltaMovement(Vec3.ZERO);
	}

	private static void look(ServerPlayer body, Vec3 at) {
		body.lookAt(EntityAnchorArgument.Anchor.EYES, at);
	}

	/** Puts the review player's eye on the view's eye, looking at its point, flying. */
	private static void view(View view) {
		ServerLevel level = player.serverLevel();
		double dx = view.tx() - view.ex(), dy = view.ty() - view.ey(), dz = view.tz() - view.ez();
		float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
		float pitch = (float) -(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * Mth.RAD_TO_DEG);
		player.getAbilities().flying = true;
		player.onUpdateAbilities();
		player.teleportTo(level, ORIGIN.getX() + view.ex(), ORIGIN.getY() + view.ey() - player.getEyeHeight(),
				ORIGIN.getZ() + view.ez(), yaw, pitch);
		player.setYHeadRot(yaw);
	}

	// ── the server tick ──────────────────────────────────────────────

	@SubscribeEvent
	public static void server(ServerTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.velorumFightReview") || event.getServer() != server || done) return;
		if (preparation > 0) {
			if (--preparation == 0) {
				try {
					setup();
				} catch (Exception error) {
					fail(error);
				}
			}
			return;
		}
		if (scene < 0) return;
		try {
			if (quarry != null && !quarry.isRemoved()) {
				quarry.doTick();
				if (quarry.getHealth() < 500.0F) quarry.setHealth(1000.0F);
			}
			Scene current = SCENES.get(scene);
			if (tick == 0) {
				current.enter().run();
				view(current.view());
			}
			current.during().accept(tick);
			// the camera shows the Long Night's narrowed sight once, at its commit; otherwise its view stays clear
			if (!(current.name().equals("long_night") && tick >= 30 && tick < 44)) player.removeEffect(SchoolState.OBSCURED.effect());
			if (boss != null && !boss.isRemoved() && !boss.ending()) {
				// the camera stands still all review: its own stillness stays where the scene puts it
				setStillness(player, cameraStillness);
				if (boss.meter().player(quarry).value() > quarryCeiling && !boss.stillness().preserved(quarry)) {
					setStillness(quarry, quarryCeiling);
				}
				if (boss.controller().move() instanceof VelorumMove running
						&& boss.controller().stage() == BossMoveTimeline.Stage.ACTIVE) RESULTS.addProperty(running.key(), true);
				SaintOpening.Spec open = boss.openings().current();
				if (open != null) RESULTS.addProperty("opening_" + open.key(), true);
				if (boss.stillness().preserved(quarry)) RESULTS.addProperty("quarry_preserved", true);
			}
			if (++tick >= current.length()) {
				JsonObject stop = new JsonObject();
				stop.addProperty("stop", current.name());
				boolean alive = boss != null && !boss.isRemoved();
				stop.addProperty("phase", alive ? boss.phases().phase() : -1);
				stop.addProperty("health", alive ? boss.getHealth() / boss.getMaxHealth() : -1.0F);
				stop.addProperty("quarry_stillness", alive ? boss.meter().player(quarry).value() : -1.0F);
				REACHED.add(stop);
				if (current.name().equals("ending_walk")) {
					boolean pass = boss.isRemoved() && boss.hasTrialEnded() && residuum() > rewardsBefore;
					RESULTS.addProperty("ending_rewarded", pass);
					if (!pass) Hemomancy.LOGGER.warn("VELORUM_FIGHT_REVIEW ending: removed {} ended {} rewards {} -> {}",
							boss.isRemoved(), boss.hasTrialEnded(), rewardsBefore, residuum());
				}
				if (current.name().equals("restored")) restoredCheck();
				if (++scene >= SCENES.size()) finish();
				else tick = 0;
			}
		} catch (Exception error) {
			fail(error);
		}
	}

	/** Every block in the chamber box is back as the template laid it, and the tomb is closed again. */
	private static void restoredCheck() {
		ServerLevel level = player.serverLevel();
		int changed = 0;
		JsonArray first = new JsonArray();
		for (var entry : AUTHORED.entrySet()) {
			BlockState now = level.getBlockState(entry.getKey());
			if (!now.equals(entry.getValue())) {
				changed++;
				if (first.size() < 12) first.add(entry.getKey().subtract(ORIGIN).toShortString() + " " + entry.getValue()
						+ " -> " + now);
			}
		}
		boolean tomb = level.getBlockState(TOMB).getBlock() instanceof SaintSarcophagusBlock;
		RESULTS.addProperty("arena_cells_changed", changed);
		RESULTS.add("arena_changes", first);
		RESULTS.addProperty("tomb_closed", tomb);
		RESULTS.addProperty("devotees_left", devotees().size());
	}

	private static void finish() {
		JsonArray missing = new JsonArray();
		for (VelorumMove move : VelorumMove.values()) {
			if (move == VelorumMove.ENDLESS_HOUR && RESULTS.has("hour_held")) continue;
			if (!RESULTS.has(move.key())) missing.add(move.key());
		}
		RESULTS.add("moves_never_active", missing);
		RESULTS.add("stops", REACHED);
		RESULTS.addProperty("stills", shots);
		cleanup();
		RESULTS.addProperty("passed", missing.isEmpty() && RESULTS.has("ending_rewarded")
				&& RESULTS.get("ending_rewarded").getAsBoolean() && RESULTS.get("arena_cells_changed").getAsInt() == 0);
		writeReport();
		done = true;
		Hemomancy.LOGGER.info("VELORUM_FIGHT_REVIEW_OK {}", RESULTS);
		Hemomancy.LOGGER.info("VELORUM_FIGHT_REVIEW captured {}", shots);
	}

	/** The residuum, or its fragment (a forced awakening pays only the fragment). */
	private static boolean reward(net.minecraft.world.item.ItemStack stack) {
		return stack.is(ItemInit.hallowed_residuum_velorum.get()) || stack.is(ItemInit.hallowed_residuum_fragment_velorum.get());
	}

	private static int residuum() {
		int count = 0;
		for (ServerPlayer body : new ServerPlayer[]{player, quarry}) {
			if (body == null) continue;
			count += body.getInventory().items.stream().filter(VelorumFightClientReview::reward).mapToInt(stack -> stack.getCount()).sum();
		}
		if (player != null) {
			count += player.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
							new AABB(ORIGIN).expandTowards(36, 16, 26), item -> reward(item.getItem()))
					.stream().mapToInt(item -> item.getItem().getCount()).sum();
		}
		return count;
	}

	private static void cleanup() {
		if (boss != null && !boss.isRemoved()) boss.discard();
		if (player != null) forceChunks(player.serverLevel(), false);
		if (quarry != null) {
			if (player != null) player.connection.send(new net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket(
					List.of(quarry.getUUID())));
			quarry.discard();
		}
		if (channel != null) channel.finishAndReleaseAll();
	}

	private static void fail(Exception error) {
		RESULTS.addProperty("passed", false);
		RESULTS.addProperty("error", error.toString());
		RESULTS.add("stops", REACHED);
		Hemomancy.LOGGER.error("VELORUM_FIGHT_REVIEW_FAILED", error);
		cleanup();
		writeReport();
		done = true;
		Hemomancy.LOGGER.info("VELORUM_FIGHT_REVIEW captured {}", shots);
	}

	private static void writeReport() {
		try {
			Files.createDirectories(report.getParent());
			Files.writeString(report, new GsonBuilder().setPrettyPrinting().create().toJson(RESULTS));
		} catch (Exception error) {
			Hemomancy.LOGGER.error("Could not save the Velorum review report", error);
		}
	}
}

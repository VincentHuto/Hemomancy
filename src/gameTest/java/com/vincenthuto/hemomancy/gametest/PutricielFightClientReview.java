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
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielAbsolutionRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielColonisationRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielPhase;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielHazard;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BogRevenantEntity;
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
 * Putriciel's fight review ({@code -PputricielFightReview=true} on the boss presentation review client, design section
 * 16), in their real chamber: the mausoleum's {@code putriciel/chamber} template is placed on the stage, so the pyres,
 * the spore shelves, the bog bodies and the tomb are the generated ones. A mock survival player is the quarry (full
 * blood, a player-info entry so the client draws them and the growth on them); the review player is a Creative
 * challenger, so the bar, the censer and the colonisation bracket reach the HUD. The fight runs in order, as a player
 * would meet it: the chamber at rest, the rise and the room taken (pyres out, shelves bloomed), every Rot I move forced
 * through the controller at its tell, its active peak and its recovery (with the Fouled Chain and the Spent Sacs, the
 * latter absorbed by the quarry), the Kindling tremor, the Ignition, every fire move, the first Offering accepted by a
 * blow and the second by held Projection through the router, the Embering into Rot II, the Mould Communion broken, the
 * second interval's two Offerings, Rot III with the Litany and Downfall deeds (a faithful rising, the Censer Orbit, the
 * Bloom of Rot over the ash), colonisation 0 to 20 and the HUD censer, the third interval's Full Purge and the fifth
 * Offering, the ending, and the chamber afterwards, checked block for block. The report in
 * {@code operator/putriciel-fight-review.json} lists what each stop reached.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class PutricielFightClientReview {
	/** A camera: the eye and the point it looks at, both chamber-local. */
	private record View(double ex, double ey, double ez, double tx, double ty, double tz) {
	}

	private record Scene(String name, int length, View view, boolean card, int[] captures, Runnable enter,
			IntConsumer during) {
	}

	/** The chamber's local origin in the review world. */
	private static final BlockPos ORIGIN = new BlockPos(1783, 199, 547);
	private static final BlockPos TOMB = ORIGIN.offset(30, 4, 13);
	private static final double ZC = 13.5D;
	/** Where Putriciel is stood for each move, facing west down the boss floor. */
	private static final double BOSS_X = 19.5D;
	// the nave runs x 9 to 26 and z 6 to 20 under a vault (9 high at its sides, 14 at its crown); the aisles behind the
	// pillar rows at z 5 and 21 hold the floor pyres and the bog bodies; the entrance passage is x 0 to 8, the dais x 27 on
	private static final View REST = new View(4.5, 7.5, 13.5, 24.5, 2.0, 13.5);
	private static final View RISE = new View(23.5, 5.2, 13.5, 30.5, 5.8, 13.5);
	private static final View ROOM = new View(12.5, 7.5, 18.5, 22.0, 1.0, 5.0);
	private static final View CLOSE = new View(16.5, 3.8, 6.6, 17.0, 2.2, 13.5);
	private static final View MID = new View(16.5, 8.0, 7.0, 16.5, 1.0, 14.0);
	/** Close on the body from its front left, for the model, the texel density and the palette. */
	private static final View PORTRAIT = new View(15.5, 3.2, 10.0, 19.5, 2.6, 13.5);
	/** Close on the open palm in front of the kneeling body, for the Offering's geometry. */
	/** Close on the quarry, for the growth colonisation lays on a player. */
	private static final View QUARRY = new View(14.0, 2.9, 10.8, 16.6, 1.6, 13.5);
	private static final View PALM = new View(16.2, 3.0, 10.6, 18.8, 2.9, 13.5);
	private static final View WIDE = new View(8.5, 7.5, 13.5, 20.5, 1.0, 13.5);
	/** Three-quarters on from the front left, where a swung staff, a thrown censer or a reaching palm is not foreshortened. */
	private static final View SIDE = new View(11.5, 4.4, 6.8, 16.5, 1.8, 13.5);
	/** A player's eye on the boss floor (1.62 above it), three-quarters on, where a trench's walls and ooze must read. */
	private static final View LOW = new View(13.0, 2.62, 10.0, 18.5, 1.0, 13.5);
	/** High behind the far quarry, the whole run from the body to them down the frame. */
	private static final View FAR = new View(9.5, 8.5, 11.0, 16.0, 0.5, 13.5);
	/** High under the crown behind the quarry, for the fields and rings that reach eight to twelve blocks. */
	private static final View HIGH = new View(11.0, 10.0, 13.5, 20.5, 0.5, 13.5);
	private static final View FAITHFUL = new View(16.5, 4.0, 10.5, 15.0, 1.2, 1.5);
	private static final View ENDING = new View(14.0, 4.4, 13.5, 19.5, 2.4, 13.5);
	// revision 2026-11-06: the palette from the front and from behind, the body fire close at three-quarters and behind
	private static final View PALETTE_FRONT = new View(14.5, 3.0, 13.5, 19.5, 2.6, 13.5);
	private static final View PALETTE_BACK = new View(23.0, 3.4, 15.5, 19.5, 2.6, 13.5);
	private static final View BODY_FIRE = new View(16.2, 3.6, 10.2, 19.5, 2.5, 13.5);
	private static final View BACK = new View(23.2, 4.2, 15.8, 19.5, 2.8, 13.5);
	// revision polish: the burning body square on from the front and from its right side, and the floor round it
	private static final View FIRE_FRONT = new View(14.3, 3.1, 13.5, 19.5, 2.7, 13.5);
	private static final View FIRE_SIDE = new View(19.0, 3.4, 7.8, 19.5, 2.7, 13.5);
	private static final View SLUDGE_OVER = new View(12.0, 6.8, 8.0, 15.5, 1.0, 13.5);
	/** Where the burn is watched: a player's eye west of the pools the second Ignition lights, and high over the floor. */
	private static final View BURN_LOW = new View(11.5, 3.0, 13.5, 18.0, 0.8, 13.5);
	private static final View SPREAD = new View(9.5, 8.0, 13.5, 18.5, 0.5, 13.5);
	/** The quarry's z for the next reset: the laying rot moves each aim at their own patch of floor, so their pools stand apart. */
	private static double quarryZ = ZC;
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
	private static MinecraftServer server;
	private static ServerPlayer player;
	private static ServerPlayer quarry;
	private static EmbeddedChannel channel;
	private static PutricielEntity boss;
	private static ParticleStatus originalParticles;
	private static double originalGamma = 0.5D;
	private static Path report;

	private PutricielFightClientReview() {
	}

	@SubscribeEvent
	public static void client(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.putricielFightReview")) return;
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
			mc.options.particles().set(switch (System.getProperty("hemomancy.putricielFightReview.particles", "all")) {
				case "minimal" -> ParticleStatus.MINIMAL;
				case "decreased" -> ParticleStatus.DECREASED;
				default -> ParticleStatus.ALL;
			});
			// bright gamma, as the mausoleum review: the chamber is lit only by its pyres and shelves
			originalGamma = mc.options.gamma().get();
			mc.options.gamma().set(1.0D);
			mc.options.pauseOnLostFocus = false;
			mc.options.hideGui = false;
			report = mc.gameDirectory.toPath().resolve("operator/putriciel-fight-review.json");
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
				clientScene = current;
				nextCapture = 0;
			}
			int[] captures = SCENES.get(current).captures();
			if (nextCapture < captures.length && frame >= captures[nextCapture]) {
				nextCapture++;
				shots++;
				String name = String.format("putriciel-fight-%02d-%s-%03d.png", current, SCENES.get(current).name(), frame);
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
		if (Boolean.getBoolean("hemomancy.putricielFightReview") && launched && !done) event.setCanceled(true);
	}

	private static void prepare() {
		if (player == null) throw new IllegalStateException("Review player is missing");
		ServerLevel level = player.serverLevel();
		forceChunks(level, true);
		level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, new AABB(ORIGIN).inflate(48),
				e -> !(e instanceof net.minecraft.world.entity.player.Player)).forEach(net.minecraft.world.entity.Entity::discard);
		var template = level.getStructureManager().getOrCreate(Hemomancy.rloc("mausoleum/putriciel/chamber"));
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
		var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "RotQuarry"), false);
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
		place(quarry, 22.0, ZC);
		// a player-info entry first, so the client draws the quarry and the growth on them
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
		Hemomancy.LOGGER.info("PUTRICIEL_FIGHT_REVIEW started in the chamber, particles {}, {} scenes",
				System.getProperty("hemomancy.putricielFightReview.particles", "all"), SCENES.size());
	}

	// ── the scenes ───────────────────────────────────────────────────

	private static void buildScenes() {
		SCENES.clear();
		SCENES.add(new Scene("chamber_at_rest", 16, REST, false, new int[]{10}, () -> {
		}, NONE));
		SCENES.add(new Scene("rise", 64, RISE, false, new int[]{8, 26, 44, 58}, PutricielFightClientReview::rise, NONE));
		SCENES.add(new Scene("room_taken", 46, ROOM, false, new int[]{6, 18, 42}, () -> {
		}, t -> {
			if (t == 44) RESULTS.addProperty("room_taken", litPyres() == 0 && bloomedShelves() > 0);
		}));
		SCENES.add(new Scene("rot_i_idle", 34, CLOSE, false, new int[]{14, 30}, () -> reset(0.1F, 15.0), NONE));
		SCENES.add(new Scene("portrait_rot", 24, PORTRAIT, false, new int[]{8, 20}, () -> reset(0.1F, 12.0), NONE));
		// revision 2026-11-06: the colour pass in the rot, front, three-quarters and back
		SCENES.add(new Scene("palette_portrait_front", 20, PALETTE_FRONT, false, new int[]{8, 16}, () -> reset(0.1F, 9.5), NONE));
		SCENES.add(new Scene("palette_portrait_three_quarters", 20, PORTRAIT, false, new int[]{8, 16}, () -> reset(0.1F, 9.5), NONE));
		SCENES.add(new Scene("palette_portrait_back", 20, PALETTE_BACK, false, new int[]{8, 16}, () -> reset(0.1F, 9.5), NONE));
		for (PutricielMove move : List.of(PutricielMove.CENSER_SWING, PutricielMove.CENSER_BACKSWING, PutricielMove.CENSER_DROP,
				PutricielMove.OFFERING_GRASP, PutricielMove.ROT_BREATH, PutricielMove.SAC_SWELL, PutricielMove.SHELF_VOLLEY,
				PutricielMove.ROOT_SNARE)) {
			moveScene(move, PutricielPhase.ROT_I.number());
			if (PutricielSludgeRules.LAYERS.contains(move)) sludgeAfter(move);
		}
		// revision 2026-11-06: the sludge the Volley, the Drop, the Grasp and a missed Snare leave, from the side and the floor
		SCENES.add(new Scene("sludge_rot", 30, SIDE, false, new int[]{4, 12, 26}, () -> {
			reset(0.1F, 9.5);
			laySludge(0);
		}, NONE));
		SCENES.add(new Scene("sludge_rot_low", 24, LOW, false, new int[]{8, 20}, () -> reset(0.1F, 9.5), NONE));
		SCENES.add(new Scene("kindling_tremor", 36, CLOSE, false, new int[]{14, 32}, () -> reset(0.8F, 15.0), NONE));
		// the Kindling fills: the interval starts at the boundary, the Ignition runs, the purifier's card shows
		SCENES.add(new Scene("ignition", 84, MID, true, new int[]{6, 20, 34, 42, 56, 80}, () -> {
			reset(0.5F, 14.0);
			boss.meter().set(boss.meter().max());
		}, t -> {
			if (boss.controller().move() == PutricielMove.IGNITION) RESULTS.addProperty("ignition_ran", true);
		}));
		// the fire spreading on over the pooled rot after the Ignition's ring: runners, the boil, the char, the ash
		SCENES.add(new Scene("sludge_burn", 120, HIGH, false, every(3, 120), () -> {
		}, t -> {
			if (boss.meter().fraction() < 0.7F) boss.meter().set(boss.meter().max() * 0.9F);
			int burning = hazards(PutricielHazard.SLUDGE_FIRE), runners = hazards(PutricielHazard.FIRE_RUNNER);
			if (burning > 0) RESULTS.addProperty("sludge_burning", Math.max(burning, RESULTS.has("sludge_burning")
					? RESULTS.get("sludge_burning").getAsInt() : 0));
			if (runners > 0) RESULTS.addProperty("sludge_runners", true);
			if (t == 118) RESULTS.addProperty("sludge_left_after_burn", hazards(PutricielHazard.ROT_SLUDGE));
		}));
		SCENES.add(new Scene("burning_idle", 24, CLOSE, false, new int[]{8, 20}, () -> fireReset(0.95F, 15.0), NONE));
		SCENES.add(new Scene("portrait_fire", 24, PORTRAIT, false, new int[]{8, 20}, () -> fireReset(0.95F, 12.0), NONE));
		for (PutricielMove move : List.of(PutricielMove.PURGING_SWEEP, PutricielMove.CENSER_CAST,
				PutricielMove.CAUTERISING_LANE, PutricielMove.PYRE_HEART, PutricielMove.PYRE_LINES)) {
			moveScene(move, PutricielPhase.INTERVAL.number());
		}
		// revision 2026-11-06: the body fire following motion: the Purging Sweep's swing and the stride, from the side
		moveScene(PutricielMove.PURGING_SWEEP, PutricielPhase.INTERVAL.number(), "_body_fire_motion", SIDE);
		SCENES.add(new Scene("body_fire_motion_stride", 40, SIDE, false, every(3, 40), () -> fireReset(0.95F, 9.0), NONE));
		SCENES.add(new Scene("hud_mid_burn", 12, CLOSE, false, new int[]{8}, () -> fireReset(0.8F, 15.0), NONE));
		// the first Offering, taken by a blow
		SCENES.add(new Scene("offering_one_struck", 70, PALM, false, new int[]{8, 22, 34, 41, 46, 60},
				() -> fireReset(0.6F, 16.5), t -> offering(t, 36, false)));
		// the second, taken by held Projection into the palm through the router
		SCENES.add(new Scene("offering_two_poured", 90, PALM, false, new int[]{10, 28, 44, 60, 70},
				() -> fireReset(0.3F, 16.5), t -> offering(t, 22, true)));
		SCENES.add(new Scene("embering_into_rot_ii", 130, MID, true, new int[]{8, 24, 44, 64, 84, 104, 126}, () -> {
			fireReset(0.0F, 14.0);
			boss.meter().set(0.0F);
		}, t -> {
			if (boss.stage() == PutricielStage.EMBERING) RESULTS.addProperty("embering_ran", true);
		}));
		SCENES.add(new Scene("rot_ii_idle", 30, CLOSE, false, new int[]{24}, () -> reset(0.1F, 15.0), t -> {
			if (t == 28) RESULTS.addProperty("rot_ii", boss.phases().phase() == PutricielPhase.ROT_II.number());
		}));
		moveScene(PutricielMove.MOULD_COMMUNION, PutricielPhase.ROT_II.number());
		// revision 2026-11-06: the second Ignition close at three-quarters, every 4 ticks as the fire climbs the body
		SCENES.add(new Scene("body_fire_ignition", 64, BODY_FIRE, false, every(3, 64), () -> {
			reset(0.5F, 14.0);
			// pools beside the body, so the ring and the body light them in this shot and the next
			laySludge(1);
			boss.meter().set(boss.meter().max());
		}, NONE));
		// the pools the second Ignition lit, boiling, crusting and crumbling to ash, from a player's eye
		SCENES.add(new Scene("sludge_burn_low", 72, BURN_LOW, false, every(3, 72), () -> {
			// out of the camera's line to the pools
			place(quarry, 15.0, 5.8);
		}, t -> {
			if (boss.meter().fraction() < 0.7F) boss.meter().set(boss.meter().max() * 0.9F);
		}));
		// the second interval, quickly: the Ignition, two Offerings taken by blows, the Embering into Rot III
		SCENES.add(new Scene("ignition_two", 70, MID, false, new int[]{40}, () -> {
			reset(0.5F, 14.0);
			boss.meter().set(boss.meter().max());
		}, NONE));
		SCENES.add(new Scene("body_fire_back", 24, BACK, false, new int[]{6, 14, 22}, () -> fireReset(0.95F, 15.0), NONE));
		SCENES.add(new Scene("body_fire_front", 24, FIRE_FRONT, false, new int[]{6, 14, 22}, () -> fireReset(0.95F, 9.0), NONE));
		SCENES.add(new Scene("body_fire_side", 24, FIRE_SIDE, false, new int[]{6, 14, 22}, () -> fireReset(0.95F, 9.0), NONE));
		SCENES.add(new Scene("offering_three_struck", 56, CLOSE, false, new int[]{34, 44},
				() -> fireReset(0.6F, 16.5), t -> offering(t, 36, false)));
		SCENES.add(new Scene("offering_four_struck", 56, CLOSE, false, new int[]{34, 44},
				() -> fireReset(0.3F, 16.5), t -> offering(t, 36, false)));
		// revision 2026-11-06: the second Embering close, every 4 ticks from the gutter to the regrowth
		SCENES.add(new Scene("embering_dieback", 84, BODY_FIRE, false, every(3, 84), () -> {
			fireReset(0.0F, 14.0);
			boss.meter().set(0.0F);
		}, t -> {
			if (boss.stage() == PutricielStage.EMBERING) RESULTS.addProperty("embering_dieback", true);
		}));
		SCENES.add(new Scene("embering_into_rot_iii", 110, MID, false, new int[]{100}, () -> {
			fireReset(0.0F, 14.0);
			boss.meter().set(0.0F);
		}, NONE));
		// Rot III under the Litany and Downfall deeds (a forced awakening)
		SCENES.add(new Scene("rot_iii_idle", 30, CLOSE, false, new int[]{24}, () -> {
			boss.setAwakening("FORCED", List.of("LITANY", "DOWNFALL"));
			reset(0.1F, 15.0);
		}, t -> {
			if (t == 28) RESULTS.addProperty("rot_iii", boss.phases().phase() == PutricielPhase.ROT_III.number());
		}));
		SCENES.add(new Scene("faithful_rising", 90, FAITHFUL, false, new int[]{30, 50, 80}, () -> {
			reset(0.1F, 13.0);
			place(quarry, 15.0, 4.5);
			force(PutricielMove.ROOT_SNARE);
		}, t -> {
			if (!faithful().isEmpty()) RESULTS.addProperty("faithful_raised", true);
		}));
		moveScene(PutricielMove.CENSER_ORBIT, PutricielPhase.ROT_III.number());
		moveScene(PutricielMove.BLOOM_OF_ROT, PutricielPhase.ROT_III.number());
		sludgeAfter(PutricielMove.BLOOM_OF_ROT);
		for (int value : new int[]{0, 5, 10, 15, 20}) {
			SCENES.add(new Scene("colonisation_" + value, 16, QUARRY, false, new int[]{value == 20 ? 3 : 12}, () -> {
				reset(0.1F, 16.5);
				boss.meter().secondary(quarry, value, PutricielColonisationRules.MAX);
				boss.meter().secondary(player, value, PutricielColonisationRules.MAX);
			}, NONE));
		}
		SCENES.add(new Scene("hud_censer_0", 12, CLOSE, false, new int[]{8}, () -> reset(0.0F, 15.0), NONE));
		SCENES.add(new Scene("hud_censer_50", 12, CLOSE, false, new int[]{8}, () -> reset(0.5F, 15.0), NONE));
		// a full censer starts the third interval
		// polish: the third Ignition from high over the floor, with pools laid all round the body before it, so the ring,
		// the runners from the pyres and from burning pools, the body and the fallback each light one, every third tick
		// until the last of them is ash
		SCENES.add(new Scene("hud_censer_100_ignition_three", 210, SPREAD, false, every(3, 210), () -> {
			reset(0.5F, 15.0);
			laySludge(2);
			boss.meter().set(boss.meter().max());
		}, t -> {
			if (boss.meter().fraction() < 0.7F && boss.stage() == PutricielStage.BURNING) boss.meter().set(boss.meter().max() * 0.9F);
			if (hazards(PutricielHazard.FIRE_RUNNER) > 0) RESULTS.addProperty("sludge_runners_three", true);
			if (t == 208) RESULTS.addProperty("sludge_left_after_burn_three", hazards(PutricielHazard.ROT_SLUDGE));
		}));
		// four absolutions stand: the Full Purge, then the fifth Offering under the Litany glyph
		SCENES.add(new Scene("full_purge", 120, HIGH, false, every(3, 120), () -> {
			fireReset(0.6F, 13.0);
			// a filtered run has not stood four absolutions, so the purge is forced
			if (!ONLY.isEmpty()) force(PutricielMove.FULL_PURGE);
		}, t -> {
			if (boss.controller().move() == PutricielMove.FULL_PURGE
					&& boss.controller().stage() == BossMoveTimeline.Stage.ACTIVE) RESULTS.addProperty(PutricielMove.FULL_PURGE.key(), true);
		}));
		SCENES.add(new Scene("offering_five_litany", 140, PALM, false, new int[]{6, 16, 26, 32}, () -> {
			place(quarry, 16.5, ZC);
			look(quarry, boss.palmPoint());
		}, t -> offering(t, 26, boss.glyph() == PutricielAbsolutionRules.Glyph.POUR)));
		SCENES.add(new Scene("ending", 150, ENDING, false, new int[]{4, 14, 26, 40, 56, 72, 88, 98, 120, 146}, () -> {
			// out of the camera's line to the body
			place(quarry, 16.0, 9.0);
		}, t -> {
			if (boss.ending()) RESULTS.addProperty("ending_began", true);
		}));
		SCENES.add(new Scene("restored", 24, WIDE, false, new int[]{20}, () -> {
		}, NONE));
		// -PputricielReviewOnly: the rise (which wakes them), the named scenes, and the scenes that turn the stage before
		// a kept one (the Ignition before the fire, the Embering before Rot II and III), for quick visual rounds
		if (!ONLY.isEmpty()) {
			List<Scene> kept = new ArrayList<>();
			for (int i = 0; i < SCENES.size(); i++) {
				Scene candidate = SCENES.get(i);
				boolean named = ONLY.stream().anyMatch(candidate.name()::contains);
				boolean turn = (candidate.name().equals("ignition") && keptBetween(i, "embering_into_rot_ii"))
						|| (candidate.name().equals("embering_into_rot_ii") && keptBetween(i, null));
				if (i <= 1 || named || turn) kept.add(candidate);
			}
			SCENES.clear();
			SCENES.addAll(kept);
		}
	}

	private static final List<String> ONLY = java.util.Arrays.stream(
			System.getProperty("hemomancy.putricielFightReview.only", "").split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();

	/** Whether a scene named by the filter comes after {@code from} and before the scene named {@code until}. */
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
	private static final java.util.Set<PutricielMove> SIDE_MOVES = java.util.EnumSet.of(PutricielMove.CENSER_SWING,
			PutricielMove.CENSER_BACKSWING, PutricielMove.CENSER_DROP, PutricielMove.OFFERING_GRASP, PutricielMove.ROOT_SNARE,
			PutricielMove.CENSER_ORBIT, PutricielMove.PURGING_SWEEP, PutricielMove.CENSER_CAST, PutricielMove.CAUTERISING_LANE);
	private static final java.util.Set<PutricielMove> LOW_MOVES = java.util.EnumSet.of(PutricielMove.OFFERING_GRASP,
			PutricielMove.ROOT_SNARE, PutricielMove.PURGING_SWEEP, PutricielMove.CAUTERISING_LANE, PutricielMove.PYRE_LINES);

	private static View view(PutricielMove move) {
		return switch (move) {
			case CENSER_SWING, CENSER_BACKSWING -> CLOSE;
			case SHELF_VOLLEY, ROOT_SNARE -> FAR;
			case SAC_SWELL, BLOOM_OF_ROT, PYRE_LINES -> HIGH;
			default -> MID;
		};
	}

	/** Capture ticks every {@code step} from the first, so wind-up, commit, follow-through and settle each land in a frame. */
	private static int[] every(int step, int length) {
		int[] ticks = new int[(length - 2) / step + 1];
		for (int i = 0; i < ticks.length; i++) ticks[i] = 1 + i * step;
		return ticks;
	}

	/** A forced move over its whole timeline from its own camera, then again from three-quarters and from the floor. */
	private static void moveScene(PutricielMove move, int phase) {
		moveScene(move, phase, "", view(move));
		if (SIDE_MOVES.contains(move)) moveScene(move, phase, "_side", SIDE);
		if (LOW_MOVES.contains(move)) moveScene(move, phase, "_low", LOW);
	}

	/** One forced move shot every third tick: its tell, commit, follow-through and its recovery (the opening, if any). */
	private static void moveScene(PutricielMove move, int phase, String suffix, View view) {
		BossMoveTimeline timeline = SaintControllerRules.effective(move.kind(),
				move.timeline(new SaintMoveInputs(phase, 0, 0, 2)));
		int w = timeline.windup(), a = timeline.active(), r = timeline.recovery();
		int start = LEAD;
		int length = Math.min(170, start + w + a + Math.min(r, 34) + 10);
		int[] captures = every(3, length - LEAD);
		for (int i = 0; i < captures.length; i++) captures[i] += LEAD;
		double quarryX = switch (move) {
			case CENSER_SWING, CENSER_BACKSWING, PYRE_HEART, CENSER_ORBIT -> 16.5;
			case MOULD_COMMUNION -> 11.0;
			case SHELF_VOLLEY, ROOT_SNARE -> 10.5;
			case CENSER_DROP, CENSER_CAST, ROT_BREATH, PYRE_LINES, CAUTERISING_LANE -> 12.5;
			default -> 14.5;
		};
		boolean fire = move.fire();
		Runnable stage = () -> {
			quarryZ = switch (move) {
				case CENSER_DROP -> 9.5;
				case OFFERING_GRASP -> 17.5;
				case SHELF_VOLLEY -> 15.0;
				case ROOT_SNARE -> 12.0;
				default -> ZC;
			};
			if (fire) fireReset(0.95F, quarryX);
			else reset(move == PutricielMove.BLOOM_OF_ROT ? 0.2F : 0.1F, quarryX);
			quarryZ = ZC;
		};
		SCENES.add(new Scene((fire ? "fire_" : "rot_") + move.key() + suffix, length, view, false, captures, stage, t -> {
			if (t == LEAD) {
				stage.run();
				force(move);
			}
			if (t < LEAD) return;
			if (move == PutricielMove.SAC_SWELL && boss.openings().isOpen(PutricielOpenings.SPENT_SACS)) {
				// the quarry draws the rot out of the spent sacs with held Absorption
				look(quarry, boss.getBoundingBox().getCenter());
				SaintChannelRouter.tryAbsorb(quarry.level(), quarry, 6.0D, 4.0D, false);
				RESULTS.addProperty("spent_sacs_absorbed_kindling", boss.meter().value());
			}
			if (move == PutricielMove.MOULD_COMMUNION && t == start + w + 12) {
				// a blow past 14 per challenger (two here: the camera counts) breaks the Communion
				strike(30.0F);
			}
			if (fire && t > start && boss.meter().fraction() < 0.7F) boss.meter().set(boss.meter().max() * 0.9F);
		}));
	}

	// ── beats ────────────────────────────────────────────────────────

	/** The encounter's own spawn out of the sarcophagus, as the block runs it, then the block cleared. */
	private static void rise() {
		ServerLevel level = player.serverLevel();
		place(quarry, 22.0, ZC);
		if (!HarbingerSaintEncounterHooks.spawnSaintBoss(level, TOMB, quarry, EnumSaintType.PUTRICIEL))
			throw new IllegalStateException("the sarcophagus did not wake Putriciel");
		level.setBlockAndUpdate(TOMB, Blocks.AIR.defaultBlockState());
		boss = level.getEntitiesOfClass(PutricielEntity.class, new AABB(TOMB).inflate(6)).stream().findFirst()
				.orElseThrow(() -> new IllegalStateException("Putriciel did not rise"));
		RESULTS.addProperty("rise_bound_to_tomb", TOMB.equals(boss.getTombPos()));
	}

	/** Putriciel on the boss floor facing west, quiet, no opening, the Kindling at {@code fill}; the quarry at x. */
	private static void reset(float fill, double quarryX) {
		boss.controller().cancel();
		boss.openings().close();
		boss.controller().delay(100_000);
		boss.moveTo(origin(BOSS_X, 1.0, ZC), 90.0F, 0.0F);
		boss.setYHeadRot(90.0F);
		boss.yBodyRot = 90.0F;
		boss.setDeltaMovement(Vec3.ZERO);
		boss.meter().set(fill * boss.meter().max());
		boss.meter().secondary(quarry, 0.0F, PutricielColonisationRules.MAX);
		boss.meter().secondary(player, 0.0F, PutricielColonisationRules.MAX);
		place(quarry, quarryX, quarryZ);
		quarry.setHealth(1000.0F);
		quarry.removeAllEffects();
		quarry.clearFire();
		HemoCapabilityAccess.getBloodVolume(quarry).setBloodVolume(10_000);
		look(quarry, boss.position().add(0.0D, 2.0D, 0.0D));
	}

	/** As {@link #reset}, in the fire: the quarry carries a little growth so the interval hunts them, not the camera. */
	private static void fireReset(float fill, double quarryX) {
		reset(fill, quarryX);
		boss.meter().secondary(quarry, 2.0F, PutricielColonisationRules.MAX);
	}

	/**
	 * Pools of each laying move's shape, laid where the camera of the stop will see them: set 0 the four shapes in front
	 * of the body (Rot I), set 1 beside the body for the second Ignition's ring, set 2 all round it for the third (two in
	 * the ring's reach, two beyond it for the runners and the fallback). Each set stands clear of the ash the sets
	 * before it leave.
	 */
	private static void laySludge(int set) {
		List<PutricielSludgeRules.Patch> patches = switch (set) {
			case 1 -> List.of(
					PutricielSludgeRules.Patch.disc(origin(17.0, 1.0, 9.6), PutricielSludgeRules.VOLLEY_RADIUS),
					PutricielSludgeRules.Patch.disc(origin(16.8, 1.0, 17.4), PutricielSludgeRules.VOLLEY_RADIUS),
					PutricielSludgeRules.Patch.runnel(origin(21.6, 1.0, 16.5), origin(21.6, 1.0, 10.5), PutricielSludgeRules.RUNNEL_HALF_WIDTH));
			case 2 -> List.of(
					PutricielSludgeRules.Patch.disc(origin(21.5, 1.0, 8.0), PutricielSludgeRules.VOLLEY_RADIUS),
					PutricielSludgeRules.Patch.disc(origin(21.0, 1.0, 18.3), PutricielSludgeRules.VOLLEY_RADIUS),
					PutricielSludgeRules.Patch.disc(origin(13.0, 1.0, 18.0), PutricielSludgeRules.SNARE_RADIUS),
					PutricielSludgeRules.Patch.runnel(origin(17.0, 1.0, 7.0), origin(14.0, 1.0, 8.2), PutricielSludgeRules.RUNNEL_HALF_WIDTH));
			default -> List.of(
					PutricielSludgeRules.Patch.disc(origin(14.5, 1.0, 10.5), PutricielSludgeRules.VOLLEY_RADIUS),
					PutricielSludgeRules.Patch.disc(origin(13.0, 1.0, 16.5), PutricielSludgeRules.DROP_RADIUS),
					PutricielSludgeRules.Patch.runnel(origin(BOSS_X - PutricielSludgeRules.RUNNEL_FROM, 1.0, ZC),
							origin(BOSS_X - PutricielSludgeRules.RUNNEL_TO, 1.0, ZC), PutricielSludgeRules.RUNNEL_HALF_WIDTH),
					PutricielSludgeRules.Patch.disc(origin(11.0, 1.0, 9.0), PutricielSludgeRules.SNARE_RADIUS));
		};
		int laid = 0;
		for (PutricielSludgeRules.Patch patch : patches) {
			if (boss.laySludge(patch) != PutricielSludgeRules.Outcome.SKIP) laid++;
		}
		RESULTS.addProperty("sludge_laid_" + set, laid);
		RESULTS.addProperty("sludge_pools_" + set, boss.sludgePools().size());
	}

	/** The floor after a laying rot move, from above: what the move left, and the pool count it left. */
	private static void sludgeAfter(PutricielMove move) {
		SCENES.add(new Scene("sludge_after_" + move.key(), 18, SLUDGE_OVER, false, new int[]{6, 16}, () -> {
			boss.controller().cancel();
			boss.controller().delay(100_000);
		}, t -> {
			if (t == 16) RESULTS.addProperty("sludge_after_" + move.key(), hazards(PutricielHazard.ROT_SLUDGE));
		}));
	}

	private static int hazards(PutricielHazard kind) {
		int count = 0;
		if (boss.arena() == null) return 0;
		for (net.minecraft.world.entity.Entity entity : boss.arena().owned()) {
			if (entity instanceof com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity hazard
					&& hazard.kind() == kind.id() && !hazard.fading()) count++;
		}
		return count;
	}

	private static void force(PutricielMove move) {
		boss.controller().start(move, SaintCombatController.Cut.IMMEDIATE);
	}

	/** Watches an Offering; at {@code act} (or later, while the palm is open) the quarry strikes it or pours into it. */
	private static void offering(int t, int act, boolean pour) {
		SaintOpening.Spec open = boss.openings().current();
		boolean palm = open != null && open.id() == PutricielOpenings.OFFERING_ID;
		if (palm && boss.glyph() != PutricielAbsolutionRules.Glyph.NONE) RESULTS.addProperty("litany_glyph", boss.glyph().name());
		if (!palm || t < act) return;
		if (pour) {
			look(quarry, boss.palmPoint());
			SaintChannelRouter.tryProject(quarry.level(), quarry, 6.0D, 5.0D, true);
		} else {
			strike(8.0F);
		}
	}

	private static void strike(float amount) {
		boss.invulnerableTime = 0;
		boss.hurt(boss.damageSources().playerAttack(quarry), amount);
	}

	private static int litPyres() {
		int lit = 0;
		for (BlockPos pos : BlockPos.betweenClosed(ORIGIN, ORIGIN.offset(36, 16, 26))) {
			BlockState state = player.serverLevel().getBlockState(pos);
			if (state.hasProperty(com.vincenthuto.hemomancy.common.block.harbinger.decoration.RotPyreBlock.LIT)
					&& state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.decoration.RotPyreBlock.LIT)) lit++;
		}
		return lit;
	}

	private static int bloomedShelves() {
		int bloomed = 0;
		for (BlockPos pos : BlockPos.betweenClosed(ORIGIN, ORIGIN.offset(36, 16, 26))) {
			BlockState state = player.serverLevel().getBlockState(pos);
			if (state.hasProperty(com.vincenthuto.hemomancy.common.block.harbinger.decoration.SporeShelfBlock.BLOOM)
					&& state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.decoration.SporeShelfBlock.BLOOM)) bloomed++;
		}
		return bloomed;
	}

	private static List<BogRevenantEntity> faithful() {
		return player.serverLevel().getEntitiesOfClass(BogRevenantEntity.class, new AABB(ORIGIN).expandTowards(36, 16, 26));
	}

	private static Vec3 origin(double x, double y, double z) {
		return new Vec3(ORIGIN.getX() + x, ORIGIN.getY() + y, ORIGIN.getZ() + z);
	}

	/** Stands a player on the boss floor at chamber-local (x, z). */
	private static void place(ServerPlayer body, double x, double z) {
		body.moveTo(ORIGIN.getX() + x, ORIGIN.getY() + 1, ORIGIN.getZ() + z, -90.0F, 0.0F);
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
		if (!Boolean.getBoolean("hemomancy.putricielFightReview") || event.getServer() != server || done) return;
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
				if (current.name().equals("ending")) rewardsBefore = residuum();
			}
			current.during().accept(tick);
			// the fifth absolution hands straight over to the ending scene, so its first beats are seen
			if (current.name().equals("offering_five_litany") && boss.ending()) {
				tick = current.length() - 1;
			}
			if (boss != null && !boss.isRemoved()) {
				if (boss.controller().move() instanceof PutricielMove running
						&& boss.controller().stage() == BossMoveTimeline.Stage.ACTIVE) RESULTS.addProperty(running.key(), true);
				SaintOpening.Spec open = boss.openings().current();
				if (open != null) RESULTS.addProperty("opening_" + open.key(), true);
				if (boss.stage() == PutricielStage.ROT && quarry.getRemainingFireTicks() > 0 && current.name().startsWith("rot_")) {
					RESULTS.addProperty("burned_in_rot", current.name());
				}
			}
			if (++tick >= current.length()) {
				JsonObject stop = new JsonObject();
				stop.addProperty("stop", current.name());
				boolean alive = boss != null && !boss.isRemoved();
				stop.addProperty("stage", alive ? boss.stage().name() : "-");
				stop.addProperty("phase", alive ? boss.phases().phase() : -1);
				stop.addProperty("kindling", alive ? boss.meter().fraction() : -1.0F);
				stop.addProperty("absolutions", alive ? boss.absolutions() : -1);
				REACHED.add(stop);
				if (current.name().equals("ending")) {
					boolean pass = boss.isRemoved() && boss.hasTrialEnded() && residuum() == rewardsBefore + 1;
					RESULTS.addProperty("ending_rewarded", pass);
					if (!pass) Hemomancy.LOGGER.warn("PUTRICIEL_FIGHT_REVIEW ending: removed {} ended {} rewards {} -> {}",
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
	}

	private static void finish() {
		JsonArray missing = new JsonArray();
		for (PutricielMove move : PutricielMove.values()) {
			if (move == PutricielMove.OFFERING || move == PutricielMove.EMBERING || !ONLY.isEmpty()) continue;
			if (!RESULTS.has(move.key())) missing.add(move.key());
		}
		RESULTS.add("moves_never_active", missing);
		RESULTS.add("stops", REACHED);
		RESULTS.addProperty("stills", shots);
		cleanup();
		RESULTS.addProperty("passed", missing.isEmpty() && RESULTS.has("ending_rewarded")
				&& RESULTS.get("ending_rewarded").getAsBoolean() && RESULTS.get("arena_cells_changed").getAsInt() == 0
				&& !RESULTS.has("burned_in_rot"));
		writeReport();
		done = true;
		Hemomancy.LOGGER.info("PUTRICIEL_FIGHT_REVIEW_OK {}", RESULTS);
		Hemomancy.LOGGER.info("PUTRICIEL_FIGHT_REVIEW captured {}", shots);
	}

	/** The residuum, or its fragment (a forced awakening, as this review's Rot III is, pays only the fragment). */
	private static boolean reward(net.minecraft.world.item.ItemStack stack) {
		return stack.is(ItemInit.hallowed_residuum_putriciel.get()) || net.minecraft.core.registries.BuiltInRegistries.ITEM
				.getKey(stack.getItem()).getPath().equals("hallowed_residuum_fragment_putriciel");
	}

	private static int residuum() {
		int count = 0;
		for (ServerPlayer body : new ServerPlayer[]{player, quarry}) {
			if (body == null) continue;
			count += body.getInventory().items.stream().filter(PutricielFightClientReview::reward).mapToInt(stack -> stack.getCount()).sum();
		}
		// Putriciel's loot falls on the chamber floor, not into an inventory
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
		Hemomancy.LOGGER.error("PUTRICIEL_FIGHT_REVIEW_FAILED", error);
		cleanup();
		writeReport();
		done = true;
		Hemomancy.LOGGER.info("PUTRICIEL_FIGHT_REVIEW captured {}", shots);
	}

	private static void writeReport() {
		try {
			Files.createDirectories(report.getParent());
			Files.writeString(report, new GsonBuilder().setPrettyPrinting().create().toJson(RESULTS));
		} catch (Exception error) {
			Hemomancy.LOGGER.error("Could not save the Putriciel review report", error);
		}
	}
}

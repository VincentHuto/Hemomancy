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
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.ContainmentAnchorEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeFragmentEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeIntegrityRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeRestraintState;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.IntConsumer;

/**
 * Seraphae's fight review ({@code -PseraphaeFightReview=true} on the boss presentation review client, design section
 * 16), in her real chamber: the mausoleum's {@code seraphae/chamber} template is placed on the stage, so the dais, the
 * four binding anchors, the two aisle reserves, the rank pillars and the healed stone are the generated ones. A mock
 * survival player is her patient (wounded, with a full blood volume, so the hold, the growth, the projection and the
 * absorption run through the real router and blood accounting); the review player is a Creative challenger, so the
 * integrity bar and her meter reach the HUD. The stops: the rise out of the sarcophagus (the encounter's own spawn), the
 * idle, the glide and the strain at her chain's end, every move forced through the controller from a camera chosen for
 * it, the hold healing, growing and bursting, the Recoil, a rebind and a reserve wake by held projection, the Fracture's
 * fragments drawn in, captured and struck into the Condensing, both phase cards with the radiance spread, the Lapse, the
 * HUD growth line, the Radiance Flood, the ending at 10, 40, 75, 95 and 130, and the chamber afterwards, checked block for
 * block against the template. The report in {@code operator/seraphae-fight-review.json} lists what each stop reached.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SeraphaeFightClientReview {
	/** A camera: the eye and the point it looks at, chamber-local, shifted along x by the scene's base. */
	private record View(double ex, double ey, double ez, double tx, double ty, double tz) {
	}

	private record Scene(String name, int length, View view, boolean card, int[] captures, Runnable enter,
			IntConsumer during) {
	}

	/** The chamber's local origin in the review world. */
	private static final BlockPos ORIGIN = new BlockPos(1783, 199, 547);
	private static final BlockPos TOMB = ORIGIN.offset(30, 4, 13);
	private static final double ZC = 13.5D;
	private static final View RISE = new View(21.5, 5.4, 13.5, 30.5, 5.6, 13.5);
	// the cameras look south across the boss floor, so east (her side, toward the dais) is screen-left: each one
	// centres a little west of her standing spot so she sits left of centre and the patient right of it
	private static final View CLOSE = new View(19.5, 3.6, 5.6, 19.5, 2.4, 13.5);
	private static final View MID = new View(19.0, 9.0, 5.8, 18.5, 1.0, 14.0);
	private static final View FAR = new View(16.0, 8.0, 5.8, 16.0, 1.5, 14.5);
	private static final View DAIS = new View(19.5, 8.5, 6.4, 28.5, 3.0, 13.5);
	private static final View RESERVE = new View(20.5, 3.4, 2.6, 26.5, 1.6, 3.6);
	private static final View WIDE = new View(4.5, 9.5, 13.5, 22.5, 1.0, 13.5);
	private static final View FLOOD = new View(12.5, 7.5, 13.5, 26.0, 3.0, 13.5);
	private static final View ENDING = new View(15.5, 6.0, 13.5, 29.5, 5.0, 13.5);
	// three-quarters on from her front-left, so a wing's or a hand's arc runs across the frame, not toward the camera
	private static final View SIDE = new View(14.0, 4.6, 6.5, 18.5, 1.6, 13.8);
	// a player's eye on the boss floor (1.62 above it), three-quarters on, where a furrow's cut and lips must read
	private static final View LOW = new View(13.0, 2.62, 9.5, 20.0, 1.0, 14.0);
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
	private static ServerPlayer patient;
	private static EmbeddedChannel channel;
	private static SeraphaeEntity boss;
	private static ParticleStatus originalParticles;
	private static double originalGamma = 0.5D;
	private static Path report;
	/** The x the scene's camera shifts by: her standing spot on the boss floor. */
	private static double base = 21.5D;

	private SeraphaeFightClientReview() {
	}

	@SubscribeEvent
	public static void client(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.seraphaeFightReview")) return;
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
			mc.options.particles().set(switch (System.getProperty("hemomancy.seraphaeFightReview.particles", "all")) {
				case "minimal" -> ParticleStatus.MINIMAL;
				case "decreased" -> ParticleStatus.DECREASED;
				default -> ParticleStatus.ALL;
			});
			// bright gamma, as the mausoleum review: the chamber is lit only by its own lights
			originalGamma = mc.options.gamma().get();
			mc.options.gamma().set(1.0D);
			mc.options.pauseOnLostFocus = false;
			mc.options.hideGui = false;
			report = mc.gameDirectory.toPath().resolve("operator/seraphae-fight-review.json");
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
				String name = String.format("seraphae-fight-%02d-%s-%03d.png", current, SCENES.get(current).name(), frame);
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

	/** The camera sees her, not the review player's hand. */
	@SubscribeEvent
	public static void hand(RenderHandEvent event) {
		if (Boolean.getBoolean("hemomancy.seraphaeFightReview") && launched && !done) event.setCanceled(true);
	}

	private static void prepare() {
		if (player == null) throw new IllegalStateException("Review player is missing");
		ServerLevel level = player.serverLevel();
		forceChunks(level, true);
		level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, new AABB(ORIGIN).inflate(48),
				e -> !(e instanceof net.minecraft.world.entity.player.Player)).forEach(net.minecraft.world.entity.Entity::discard);
		var template = level.getStructureManager().getOrCreate(Hemomancy.rloc("mausoleum/seraphae/chamber"));
		template.placeInWorld(level, ORIGIN, ORIGIN, new StructurePlaceSettings(), level.random, 2);
		if (!(level.getBlockState(TOMB).getBlock() instanceof SaintSarcophagusBlock))
			throw new IllegalStateException("the chamber template has no sarcophagus at " + TOMB);
		level.setDayTime(18000);
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String command : new String[]{"gamerule doDaylightCycle false", "gamerule naturalRegeneration false",
				"gamerule doMobSpawning false", "weather clear", "difficulty normal"}) {
			server.getCommands().performPrefixedCommand(source, command);
		}
		player.setGameMode(GameType.CREATIVE);
		// an empty hand: the camera sees the body, not a held item
		player.getInventory().selected = 8;
		player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(8));
		view(RISE);
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
		var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "SeraphaePatient"), false);
		patient = new ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		channel = new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(server, connection, patient, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
			}
		};
		patient.setGameMode(GameType.SURVIVAL);
		patient.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0D);
		patient.setHealth(900.0F);
		place(patient, 24.0, ZC);
		// a player-info entry first, so the client draws the patient and the geometry tied to it (hold, thread, growth)
		player.connection.send(net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
				.createPlayerInitializing(List.of(patient)));
		level.addNewPlayer(patient);
		var blood = HemoCapabilityAccess.getBloodVolume(patient);
		blood.setActive(true);
		blood.setMaxBloodVolume(10_000);
		blood.setBloodVolume(10_000);
		buildScenes();
		scene = 0;
		tick = 0;
		Hemomancy.LOGGER.info("SERAPHAE_FIGHT_REVIEW started in the chamber, particles {}, {} scenes",
				System.getProperty("hemomancy.seraphaeFightReview.particles", "all"), SCENES.size());
	}

	// ── the scenes ───────────────────────────────────────────────────

	private static void buildScenes() {
		SCENES.clear();
		SCENES.add(new Scene("rise", 96, RISE, true, new int[]{10, 30, 50, 70, 92}, SeraphaeFightClientReview::rise, NONE));
		SCENES.add(new Scene("idle", 30, CLOSE, false, new int[]{24}, () -> reset(30.0F, 16.5), NONE));
		SCENES.add(new Scene("glide", 40, MID, false, every(4, 40), () -> {
			reset(30.0F, 9.5);
			boss.moveTo(origin(25.5, 1.4, ZC), 90.0F, 0.0F);
		}, NONE));
		SCENES.add(new Scene("strain", 70, WIDE, false, new int[]{40, 66}, () -> reset(30.0F, 4.5), NONE));
		// phase I moves at integrity 30
		phaseMoves(1, 30.0F);
		SCENES.add(new Scene("hold", 150, CLOSE, false, every(6, 150), () -> {
			reset(30.0F, base - 4.5);
			patient.setHealth(996.0F);
			force(SeraphaeMove.LAYING_ON_OF_HANDS);
		}, t -> {
			if (boss.bossHold().holding()) RESULTS.addProperty("hold_caught", true);
			if (boss.bossHold().holding() && boss.growth(patient) >= 4) RESULTS.addProperty("hold_grew", true);
			if (t == 40) RESULTS.addProperty("hold_at_40", "move " + boss.syncedMoveId() + " stage " + boss.controller().stage()
					+ " lane " + SeraphaeMove.laneLength(boss.syncedMoveVariant()) + " gap "
					+ String.format("%.2f", boss.position().distanceTo(patient.position())) + " held " + boss.syncedHoldPlayer());
		}));
		SCENES.add(new Scene("recoil", 80, MID, false, every(4, 80), () -> {
			reset(30.0F, 30.5 - boss.syncedLeash() - 4.0);
			force(SeraphaeMove.LAYING_ON_OF_HANDS);
		}, t -> opening("recoil", SeraphaeOpenings.RECOIL_ID)));
		// growth never lands on a Creative player, so the viewer's own growth line is shown in Survival (flying and
		// invulnerable, so the burst at ten cannot hurt the camera), then the viewer goes back to Creative
		for (int growth : new int[]{0, 4, 8, 10}) {
			SCENES.add(new Scene("hud_growth_" + growth, 14, CLOSE, false, new int[]{growth == 10 ? 2 : 10}, () -> {
				reset(30.0F, 16.5);
				viewerSurvival(true);
				boss.addGrowth(player, -boss.growth(player));
				if (growth > 0) boss.addGrowth(player, growth);
			}, NONE));
		}
		SCENES.add(new Scene("phase_restraint", 70, WIDE, true, new int[]{8, 30, 66}, () -> {
			viewerSurvival(false);
			reset(30.0F, 16.5);
			boss.meter().set(45.0F);
			boss.controller().delay(0);
		}, t -> RESULTS.addProperty("phase_two", boss.phases().phase() >= 2)));
		phaseMoves(2, 50.0F);
		SCENES.add(new Scene("rebind", 80, DAIS, false, new int[]{8, 32, 64, 74}, SeraphaeFightClientReview::slipOne,
				SeraphaeFightClientReview::rebindTick));
		SCENES.add(new Scene("reserve", 100, RESERVE, false, new int[]{40, 86, 96}, () -> {
			reset(50.0F, 16.5);
			place(patient, 23.5, 4.4);
		}, SeraphaeFightClientReview::reserveTick));
		SCENES.add(new Scene("fragments", 130, CLOSE, false, every(6, 130), () -> {
			reset(50.0F, base - 7.0);
			force(SeraphaeMove.FRACTURE);
		}, SeraphaeFightClientReview::fragmentTick));
		SCENES.add(new Scene("phase_escaping_light", 70, WIDE, true, new int[]{8, 30, 66}, () -> {
			reset(50.0F, 16.5);
			boss.meter().set(80.0F);
			boss.controller().delay(0);
		}, t -> RESULTS.addProperty("phase_three", boss.phases().phase() >= 3)));
		phaseMoves(3, 80.0F);
		SCENES.add(new Scene("lapse", 70, CLOSE, false, every(5, 70), () -> {
			reset(80.0F, base - 6.0);
			boss.openings().open(SeraphaeOpenings.LAPSE, 0);
		}, t -> opening("lapse", SeraphaeOpenings.LAPSE_ID)));
		SCENES.add(new Scene("radiance_flood", 90, FLOOD, false, every(4, 90), () -> {
			reset(80.0F, 16.5);
			// off the camera's line, so the patient does not stand between the camera and her
			place(patient, 16.5, ZC + 5.0);
			boss.meter().set(0.0F);
			boss.controller().delay(0);
		}, t -> {
			if (boss.syncedMoveId() == SeraphaeMove.RADIANCE_FLOOD.id()) RESULTS.addProperty("flood_ran", true);
		}));
		SCENES.add(new Scene("ending", 150, ENDING, true, every(5, 150), () -> {
			reset(80.0F, 17.5);
			rewardsBefore = residuum();
			boss.meter().set(99.5F);
			boss.meter().add(1.0F, patient);
		}, NONE));
		SCENES.add(new Scene("restored", 40, WIDE, false, new int[]{36}, () -> {
		}, NONE));
		// -PseraphaeReviewOnly: the rise (which wakes her) and the named scenes only, for quick visual rounds
		if (!ONLY.isEmpty()) {
			List<Scene> kept = new ArrayList<>();
			for (int i = 0; i < SCENES.size(); i++) {
				Scene candidate = SCENES.get(i);
				if (i == 0 || ONLY.stream().anyMatch(candidate.name()::contains)) kept.add(candidate);
			}
			SCENES.clear();
			SCENES.addAll(kept);
		}
	}

	private static final List<String> ONLY = java.util.Arrays.stream(
			System.getProperty("hemomancy.seraphaeFightReview.only", "").split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();

	/** Every move first seen in {@code phase}, forced from a camera chosen for it, shot every third tick over its timeline. */
	private static void phaseMoves(int phase, float integrity) {
		for (SeraphaeMove move : SeraphaeMove.values()) {
			if (move == SeraphaeMove.RADIANCE_FLOOD || firstPhase(move) != phase) continue;
			moveScene(move, move.key(), view(move), integrity, phase);
			// the limb-driven moves again from three-quarters, where the swing is not foreshortened toward the camera
			if (SIDE_MOVES.contains(move)) moveScene(move, move.key() + "_side", SIDE, integrity, phase);
			// the furrowed lanes again from a player's eye on the floor
			if (LOW_MOVES.contains(move)) moveScene(move, move.key() + "_low", LOW, integrity, phase);
		}
	}

	/** Ticks a move scene waits after its camera moves before the move is forced. */
	private static final int LEAD = 6;
	private static final java.util.Set<SeraphaeMove> SIDE_MOVES = java.util.EnumSet.of(SeraphaeMove.LAYING_ON_OF_HANDS,
			SeraphaeMove.NERVE_LASH, SeraphaeMove.COUNTER_LASH, SeraphaeMove.WING_GRASP, SeraphaeMove.CONVULSION,
			SeraphaeMove.FRACTURE, SeraphaeMove.HEALED_GROUND);
	private static final java.util.Set<SeraphaeMove> LOW_MOVES = java.util.EnumSet.of(SeraphaeMove.LAYING_ON_OF_HANDS,
			SeraphaeMove.AXON_LANCE);

	private static void moveScene(SeraphaeMove move, String name, View view, float integrity, int phase) {
		BossMoveTimeline timeline = move.timeline(new SaintMoveInputs(phase, 0, 0, 2));
		int length = Math.min(170, timeline.total() + 14) + LEAD;
		int[] captures = every(3, length - LEAD);
		for (int i = 0; i < captures.length; i++) captures[i] += LEAD;
		Runnable stage = () -> {
			reset(integrity, targetX(move));
			if (move == SeraphaeMove.WING_GRASP) place(patient, base - 2.0, ZC + 4.0);
			if (move == SeraphaeMove.CHAIN_WRENCH) place(patient, 25.5, 9.0);
		};
		SCENES.add(new Scene(name, length, view, false, captures, stage, t -> {
			// the camera has moved and the chunks redrawn by now, so the first frames are not black; the stage is set
			// again, since a hold left from the scene before can draw the patient in during the lead
			if (t == LEAD) {
				stage.run();
				force(move);
			}
			if (boss.controller().move() == move && boss.controller().stage() == BossMoveTimeline.Stage.ACTIVE) {
				RESULTS.addProperty(move.key(), true);
			}
		}));
	}

	/** Capture ticks every {@code step} from the first, so wind-up, commit, follow-through and settle each land in a frame. */
	private static int[] every(int step, int length) {
		int[] ticks = new int[(length - 2) / step + 1];
		for (int i = 0; i < ticks.length; i++) ticks[i] = 1 + i * step;
		return ticks;
	}

	private static int firstPhase(SeraphaeMove move) {
		for (int phase = 1; phase <= 3; phase++) if (move.inPhase(phase)) return phase;
		return 3;
	}

	/** Where the patient stands for a move: inside its favoured band, west of her along the centre line. */
	private static double targetX(SeraphaeMove move) {
		return switch (move) {
			case NERVE_LASH, COUNTER_LASH, CONVULSION, CONVULSION_WIDE -> base - 3.0;
			case LAYING_ON_OF_HANDS, WING_GRASP, FRACTURE -> base - 5.5;
			case REGROWTH_KNOT, HEALED_GROUND -> base - 8.0;
			case AXON_LANCE, AXON_MIRROR, UNCLOSING_EYE -> base - 11.0;
			case TRANSFUSION_THREAD -> base - 14.0;
			case STRAINING_REACH -> 30.5 - boss.syncedLeash() - 5.0;
			default -> base - 5.0;
		};
	}

	private static View view(SeraphaeMove move) {
		return switch (move) {
			case NERVE_LASH, COUNTER_LASH, LAYING_ON_OF_HANDS, FRACTURE -> CLOSE;
			case AXON_LANCE, AXON_MIRROR, TRANSFUSION_THREAD, UNCLOSING_EYE, STRAINING_REACH -> FAR;
			// from high in the west, where every chain line to the anchors lies across the frame
			case CHAIN_WRENCH, EXTRACTION -> WIDE;
			default -> MID;
		};
	}

	// ── beats ────────────────────────────────────────────────────────

	/** The encounter's own spawn out of the sarcophagus, as the block runs it, then the block cleared. */
	private static void rise() {
		ServerLevel level = player.serverLevel();
		place(patient, 22.0, ZC);
		if (!HarbingerSaintEncounterHooks.spawnSaintBoss(level, TOMB, patient, EnumSaintType.SERAPHAE))
			throw new IllegalStateException("the sarcophagus did not wake Seraphae");
		level.setBlockAndUpdate(TOMB, Blocks.AIR.defaultBlockState());
		boss = level.getEntitiesOfClass(SeraphaeEntity.class, new AABB(TOMB).inflate(6)).stream().findFirst()
				.orElseThrow(() -> new IllegalStateException("Seraphae did not rise"));
		RESULTS.addProperty("rise_bound_to_tomb", TOMB.equals(boss.getTombPos()));
	}

	/**
	 * Puts her on the boss floor at her standing spot (inside her chain for this integrity), quiet, with no opening, at
	 * the given integrity, and the patient wounded at chamber-local x on the centre line.
	 */
	private static void reset(float integrity, double patientX) {
		boss.meter().set(integrity);
		double leash = com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeLeashRules.length(integrity, 0, 0);
		base = 30.5 - Math.min(9.0, leash - 1.5);
		boss.controller().cancel();
		boss.openings().close();
		boss.controller().delay(100_000);
		boss.moveTo(origin(base, 1.0 + SeraphaeEntity.HOVER, ZC), 90.0F, 0.0F);
		boss.setDeltaMovement(Vec3.ZERO);
		boss.addGrowth(patient, -boss.growth(patient));
		place(patient, patientX, ZC);
		patient.setHealth(900.0F);
		patient.removeAllEffects();
		HemoCapabilityAccess.getBloodVolume(patient).setBloodVolume(10_000);
		look(patient, boss.position().add(0.0D, SeraphaeEntity.CHEST, 0.0D));
	}

	private static void viewerSurvival(boolean survival) {
		if (survival == (player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL)) return;
		player.setGameMode(survival ? GameType.SURVIVAL : GameType.CREATIVE);
		player.getAbilities().mayfly = true;
		player.getAbilities().flying = true;
		player.getAbilities().invulnerable = true;
		player.onUpdateAbilities();
	}

	private static void force(SeraphaeMove move) {
		boss.controller().start(move, SaintCombatController.Cut.IMMEDIATE);
	}

	private static void opening(String key, int id) {
		if (boss.syncedOpening() == id) RESULTS.addProperty("opening_" + key, true);
	}

	private static void slipOne() {
		reset(50.0F, 16.5);
		ContainmentAnchorEntity slipped = boss.anchors().stream().filter(a -> !a.isHeld()).findFirst().orElse(null);
		if (slipped == null) {
			slipped = boss.anchors().stream().filter(a -> a.kind() == ContainmentAnchorEntity.Kind.BINDING)
					.min(Comparator.comparingDouble(a -> a.distanceToSqr(boss))).orElseThrow();
			slipped.slip();
		}
		Vec3 crown = slipped.crown();
		Vec3 from = crown.subtract(boss.position()).multiply(1, 0, 1).normalize().scale(-3.0D);
		patient.moveTo(crown.x + from.x, slipped.getY(), crown.z + from.z);
		look(patient, crown);
	}

	/** Held Projection on the slipped anchor through the real router, 4 blood a tick at reach 5.5. */
	private static void rebindTick(int t) {
		if (t < 4) return;
		ContainmentAnchorEntity target = boss.anchors().stream().filter(a -> !a.isHeld())
				.min(Comparator.comparingDouble(a -> a.distanceToSqr(patient))).orElse(null);
		if (target != null) {
			look(patient, target.crown());
			SaintChannelRouter.tryProject(patient.level(), patient, 5.5D, 4.0D, false);
			RESULTS.addProperty("rebind_progress", target.progress());
		} else {
			RESULTS.addProperty("rebound", true);
		}
		opening("seized", SeraphaeOpenings.SEIZED_ID);
	}

	/** Held Projection on the nearer aisle reserve until it wakes. */
	private static void reserveTick(int t) {
		if (t < 4) return;
		Vec3 home = origin(26.5, 2.0, 3.5);
		look(patient, home);
		SaintChannelRouter.tryProject(patient.level(), patient, 5.5D, 4.0D, false);
		long reserves = boss.anchors().stream().filter(a -> a.kind() == ContainmentAnchorEntity.Kind.RESERVE).count();
		if (reserves > 0) RESULTS.addProperty("reserve_woken", true);
		RESULTS.addProperty("reserves_held", SeraphaeRestraintState.reservesHeld(boss.syncedRestraints()));
	}

	/** The Fracture's fragments: one drawn in by held Absorption, one led to an anchor, the rest struck apart. */
	private static void fragmentTick(int t) {
		List<SeraphaeFragmentEntity> fragments = fragments();
		if (!fragments.isEmpty()) RESULTS.addProperty("fragments", Math.max(fragments.size(),
				RESULTS.has("fragments") ? RESULTS.get("fragments").getAsInt() : 0));
		if (t >= 44 && t < 80 && !fragments.isEmpty()) {
			SeraphaeFragmentEntity nearest = fragments.stream().min(Comparator.comparingDouble(f -> f.distanceToSqr(patient)))
					.orElseThrow();
			look(patient, nearest.position().add(0.0D, 0.4D, 0.0D));
			SaintChannelRouter.tryAbsorb(patient.level(), patient, 6.0D, 4.0D, false);
		}
		if (t == 80 && fragments.size() > 1) {
			ContainmentAnchorEntity anchor = boss.anchors().stream().filter(ContainmentAnchorEntity::isHeld).findFirst().orElse(null);
			if (anchor != null) {
				SeraphaeFragmentEntity led = fragments.get(0);
				led.moveTo(anchor.getX() + 1.0D, anchor.getY() + 1.0D, anchor.getZ());
			}
		}
		if (t == 104) {
			for (SeraphaeFragmentEntity fragment : fragments) {
				fragment.resolve(SeraphaeIntegrityRules.FragmentEnd.STRUCK, patient.position());
			}
		}
		opening("condensing", SeraphaeOpenings.CONDENSING_ID);
	}

	private static List<SeraphaeFragmentEntity> fragments() {
		return boss.level().getEntitiesOfClass(SeraphaeFragmentEntity.class, boss.getBoundingBox().inflate(24.0D),
				f -> boss.getUUID().equals(f.getParentId()) && f.isAlive());
	}

	private static Vec3 origin(double x, double y, double z) {
		return new Vec3(ORIGIN.getX() + x, ORIGIN.getY() + y, ORIGIN.getZ() + z);
	}

	/** Stands a player on the boss floor at chamber-local (x, z). */
	private static void place(ServerPlayer body, double x, double z) {
		body.moveTo(ORIGIN.getX() + x, ORIGIN.getY() + 1, ORIGIN.getZ() + z, 0, 0);
		body.setDeltaMovement(Vec3.ZERO);
	}

	private static void look(ServerPlayer body, Vec3 at) {
		body.lookAt(EntityAnchorArgument.Anchor.EYES, at);
	}

	/** Puts the review player's eye on the view's eye (shifted by her standing spot), looking at its point, flying. */
	private static void view(View view) {
		ServerLevel level = player.serverLevel();
		double shift = view == CLOSE || view == MID || view == FAR || view == SIDE || view == LOW ? base - 21.5D : 0.0D;
		double dx = view.tx() - view.ex(), dy = view.ty() - view.ey(), dz = view.tz() - view.ez();
		float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
		float pitch = (float) -(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * Mth.RAD_TO_DEG);
		player.getAbilities().flying = true;
		player.onUpdateAbilities();
		player.teleportTo(level, ORIGIN.getX() + view.ex() + shift, ORIGIN.getY() + view.ey() - player.getEyeHeight(),
				ORIGIN.getZ() + view.ez(), yaw, pitch);
		player.setYHeadRot(yaw);
	}

	// ── the server tick ──────────────────────────────────────────────

	@SubscribeEvent
	public static void server(ServerTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.seraphaeFightReview") || event.getServer() != server || done) return;
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
			if (patient != null && !patient.isRemoved()) patient.doTick();
			Scene current = SCENES.get(scene);
			if (tick == 0) {
				current.enter().run();
				view(current.view());
			}
			current.during().accept(tick);
			if (++tick >= current.length()) {
				JsonObject stop = new JsonObject();
				stop.addProperty("stop", current.name());
				stop.addProperty("integrity", boss.isRemoved() ? -1.0F : boss.meter().value());
				stop.addProperty("phase", boss.isRemoved() ? -1 : boss.phases().phase());
				REACHED.add(stop);
				if (current.name().equals("ending")) {
					boolean pass = boss.isRemoved() && boss.hasTrialEnded() && residuum() == rewardsBefore + 1;
					RESULTS.addProperty("ending_rewarded", pass);
					if (!pass) Hemomancy.LOGGER.warn("SERAPHAE_FIGHT_REVIEW ending: removed {} ended {} rewards {} -> {}",
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
		for (SeraphaeMove move : SeraphaeMove.values()) {
			if (move == SeraphaeMove.RADIANCE_FLOOD || !ONLY.isEmpty()) continue;
			if (!RESULTS.has(move.key())) missing.add(move.key());
		}
		RESULTS.add("moves_never_active", missing);
		RESULTS.add("stops", REACHED);
		RESULTS.addProperty("stills", shots);
		cleanup();
		RESULTS.addProperty("passed", missing.isEmpty() && (!ONLY.isEmpty() || RESULTS.has("ending_rewarded")
				&& RESULTS.get("ending_rewarded").getAsBoolean() && RESULTS.get("arena_cells_changed").getAsInt() == 0));
		writeReport();
		done = true;
		Hemomancy.LOGGER.info("SERAPHAE_FIGHT_REVIEW_OK {}", RESULTS);
		Hemomancy.LOGGER.info("SERAPHAE_FIGHT_REVIEW captured {}", shots);
	}

	private static int residuum() {
		int count = 0;
		for (ServerPlayer body : new ServerPlayer[]{player, patient}) {
			if (body == null) continue;
			count += body.getInventory().items.stream().filter(stack -> stack.is(ItemInit.hallowed_residuum_seraphae.get()))
					.mapToInt(stack -> stack.getCount()).sum();
		}
		return count;
	}

	private static void cleanup() {
		if (boss != null && !boss.isRemoved()) boss.discard();
		if (player != null) forceChunks(player.serverLevel(), false);
		if (patient != null) {
			if (player != null) player.connection.send(new net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket(
					List.of(patient.getUUID())));
			patient.discard();
		}
		if (channel != null) channel.finishAndReleaseAll();
	}

	private static void fail(Exception error) {
		RESULTS.addProperty("passed", false);
		RESULTS.addProperty("error", error.toString());
		RESULTS.add("stops", REACHED);
		Hemomancy.LOGGER.error("SERAPHAE_FIGHT_REVIEW_FAILED", error);
		cleanup();
		writeReport();
		done = true;
		Hemomancy.LOGGER.info("SERAPHAE_FIGHT_REVIEW captured {}", shots);
	}

	private static void writeReport() {
		try {
			Files.createDirectories(report.getParent());
			Files.writeString(report, new GsonBuilder().setPrettyPrinting().create().toJson(RESULTS));
		} catch (Exception error) {
			Hemomancy.LOGGER.error("Could not save the Seraphae review report", error);
		}
	}
}

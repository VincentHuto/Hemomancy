package com.vincenthuto.hemomancy.gametest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneDirector;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.HarbingerSaintEncounterHooks;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathOpenings;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * Opt-in native review of Hemorath on the combat kit ({@code -PhemorathFightReview=true} on
 * {@code bossPresentationReviewClient}, design section 16), in his real chamber: the mausoleum's {@code chamber}
 * template is placed on the stage, so the wells, the gutters, the pit and the tomb are the generated ones. The stops:
 * the rise out of the sarcophagus (the encounter's own spawn), the idle in his three meter states, the gutters showing
 * a brimming meter, his patches at 0, 4 and 9, every move forced through the controller over its whole timeline
 * (melee from close, area moves from above the boss floor, the Wells Answer from across the pit toward a well pair),
 * the Tithe paid mid-wind-up, every opening, the HUD meter at four fills, the phase-1 overload with its card and tide,
 * the Last Thirst and the ending from the door, and the chamber after the ending, checked block for block against the
 * template. A mock challenger stands in front of him as the quarry; the review player is a Creative challenger, so the
 * meter reaches their HUD and nothing drains them. Particles are All by default, as a player sees them
 * ({@code -PhemorathReviewParticles=minimal} checks that every telegraph still reads without them). The report
 * records one flag per move that reached its active span.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class HemorathFightClientReview {
	/** A camera: the eye and the point it looks at, both chamber-local. */
	private record View(double ex, double ey, double ez, double tx, double ty, double tz) {
	}

	private record Scene(String name, int length, View view, boolean card, Runnable enter, IntConsumer during, int every) {
		Scene(String name, int length, View view, boolean card, Runnable enter, IntConsumer during) {
			this(name, length, view, card, enter, during, 6);
		}
	}

	/** The chamber's local origin in the review world: its pit centre lands on (1700.5, 200, 520.5). */
	private static final BlockPos ORIGIN = new BlockPos(1683, 199, 507);
	private static final BlockPos TOMB = ORIGIN.offset(30, 4, 13);
	private static final double PIT_X = 17.5, PIT_Z = 13.5;
	private static final View RISE = new View(22.5, 4.0, 13.5, 30.5, 5.8, 13.5);
	private static final View CLOSE = new View(17.5, 3.6, 6.6, 17.5, 3.0, 13.5);
	private static final View MID = new View(17.5, 7.2, 6.4, 17.5, 1.0, 15.0);
	// from the side, so the north pair's streams run across the frame instead of hiding behind him
	private static final View WELLS = new View(11.0, 5.5, 14.5, 17.5, 2.0, 5.0);
	private static final View WIDE = new View(4.5, 8.5, 13.5, 19.5, 1.0, 13.5);
	private static final View SIDE = new View(10.0, 4.2, 8.5, 17.5, 2.4, 13.5);
	// a player's eye on the boss floor (1.62 above it), three-quarters on, where a trench's walls and slabs must read
	private static final View LOW = new View(9.0, 2.62, 8.0, 15.5, 1.0, 13.5);
	private static final IntConsumer NONE = tick -> {
	};
	private static final JsonObject RESULTS = new JsonObject();
	private static final List<Scene> SCENES = new ArrayList<>();
	private static final Map<BlockPos, BlockState> AUTHORED = new HashMap<>();
	private static volatile int scene = -1, tick;
	private static volatile boolean done;
	private static boolean launched;
	private static int capturedScene = -1, capturedTick = -1, closing, preparation, rewardsBefore;
	private static MinecraftServer server;
	private static ServerPlayer player;
	private static ServerPlayer quarry;
	private static io.netty.channel.embedded.EmbeddedChannel quarryChannel;
	private static HemorathEntity boss;
	private static ParticleStatus originalParticles;
	private static double originalGamma = 0.5D;
	private static Path report;

	private HemorathFightClientReview() {
	}

	@SubscribeEvent
	public static void client(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.hemorathFightReview")) return;
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
			mc.options.particles().set(switch (System.getProperty("hemomancy.hemorathFightReview.particles", "all")) {
				case "minimal" -> ParticleStatus.MINIMAL;
				case "decreased" -> ParticleStatus.DECREASED;
				default -> ParticleStatus.ALL;
			});
			// Bright gamma, as the mausoleum review: the chamber is lit only by its sconces and ceiling lights
			originalGamma = mc.options.gamma().get();
			mc.options.gamma().set(1.0D);
			mc.options.pauseOnLostFocus = false;
			mc.options.hideGui = false;
			report = mc.gameDirectory.toPath().resolve("operator/hemorath-fight-review.json");
			var id = mc.player.getUUID();
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
		// the cards have their own review; this fixture keeps a fixed camera on the body, the meter and the geometry,
		// except where a scene shows its card
		if (current < 0 || current >= SCENES.size() || !SCENES.get(current).card()) BossSceneDirector.INSTANCE.clear();
		// moves are shot every third tick, so a strike's wind-up, commit, follow-through and settle each land in a frame
		if (current >= 0 && current < SCENES.size() && frame % SCENES.get(current).every() == SCENES.get(current).every() / 2
				&& (capturedScene != current || capturedTick != frame)) {
			capturedScene = current;
			capturedTick = frame;
			String name = String.format("hemorath-fight-%02d-%s-%03d.png", current, SCENES.get(current).name(), frame);
			Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), message -> {
			});
		}
		if (done && ++closing > 24) {
			mc.options.particles().set(originalParticles);
			mc.options.gamma().set(originalGamma);
			mc.stop();
		}
	}

	/** The camera sees the body, not the review player's hand. */
	@SubscribeEvent
	public static void hand(net.neoforged.neoforge.client.event.RenderHandEvent event) {
		if (Boolean.getBoolean("hemomancy.hemorathFightReview") && launched && !done) event.setCanceled(true);
	}

	private static void prepare() {
		if (player == null) throw new IllegalStateException("Review player is missing");
		ServerLevel level = player.serverLevel();
		for (int cx = ORIGIN.getX() >> 4; cx <= (ORIGIN.getX() + 36) >> 4; cx++) {
			for (int cz = ORIGIN.getZ() >> 4; cz <= (ORIGIN.getZ() + 26) >> 4; cz++) level.setChunkForced(cx, cz, true);
		}
		level.getEntitiesOfClass(HemorathEntity.class, new AABB(ORIGIN).inflate(48)).forEach(actor -> actor.discard());
		var template = level.getStructureManager().getOrCreate(Hemomancy.rloc("mausoleum/hemorath/chamber"));
		template.placeInWorld(level, ORIGIN, ORIGIN, new StructurePlaceSettings(), level.random, 2);
		if (!(level.getBlockState(TOMB).getBlock() instanceof SaintSarcophagusBlock))
			throw new IllegalStateException("the chamber template has no sarcophagus at " + TOMB);
		AUTHORED.clear();
		for (BlockPos pos : BlockPos.betweenClosed(ORIGIN, ORIGIN.offset(36, 16, 26))) {
			BlockState state = level.getBlockState(pos);
			if (state.is(BlockInit.blood_gutter.get()) || state.is(BlockInit.blood_basin.get()) || state.is(BlockInit.venous_basin.get()))
				AUTHORED.put(pos.immutable(), state);
		}
		level.setDayTime(18000);
		player.setGameMode(GameType.CREATIVE);
		// an empty hand: the camera sees the body, not a held item
		player.getInventory().selected = 8;
		player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(8));
		view(RISE);
		preparation = 40;
	}

	private static void setup() {
		ServerLevel level = player.serverLevel();
		var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
				new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "MarrowQuarry"), false);
		quarry = new ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation());
		var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
		quarryChannel = new io.netty.channel.embedded.EmbeddedChannel(connection);
		new net.minecraft.server.network.ServerGamePacketListenerImpl(server, connection, quarry, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
			}
		};
		quarry.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);
		quarry.setHealth(1000);
		place(quarry, 21.5, 11.0);
		level.addNewPlayer(quarry);
		var blood = HemoCapabilityAccess.getBloodVolume(quarry);
		blood.setActive(true);
		blood.setMaxBloodVolume(10_000);
		blood.setBloodVolume(10_000);
		buildScenes();
		scene = 0;
		tick = 0;
		Hemomancy.LOGGER.info("HEMORATH_FIGHT_REVIEW started in the chamber, particles {}, {} wells, {} set pieces",
				System.getProperty("hemomancy.hemorathFightReview.particles", "all"), AUTHORED.values().stream().filter(state -> state.is(BlockInit.blood_basin.get())).count(), AUTHORED.size());
	}

	private static void buildScenes() {
		SCENES.clear();
		SCENES.add(new Scene("rise", 84, RISE, false, HemorathFightClientReview::rise, NONE));
		SCENES.add(new Scene("idle_sunken", 36, CLOSE, false, () -> meter(0.1F), NONE));
		SCENES.add(new Scene("idle_filling", 36, CLOSE, false, () -> meter(0.5F), NONE));
		SCENES.add(new Scene("idle_brimming", 36, CLOSE, false, () -> meter(0.8F), NONE));
		SCENES.add(new Scene("gutters_brimming", 48, WIDE, false, () -> meter(0.9F), NONE));
		SCENES.add(new Scene("patches_0", 18, CLOSE, false, () -> patches(0), NONE));
		SCENES.add(new Scene("patches_4", 18, CLOSE, false, () -> patches(4), NONE));
		SCENES.add(new Scene("patches_9", 18, CLOSE, false, () -> patches(9), NONE));
		for (HemorathMove move : HemorathMove.values()) {
			if (move == HemorathMove.LAST_THIRST) continue;
			int phase = move.inPhase(1) ? 1 : 2;
			View view = move == HemorathMove.WELLS_ANSWER ? WELLS : move.kind() == SaintMove.Kind.AREA ? MID : CLOSE;
			IntConsumer during = move != HemorathMove.WELLS_ANSWER ? NONE : t -> {
				if (t == 34) Hemomancy.LOGGER.info("HEMORATH_FIGHT_REVIEW wells mask {} at {}, levels {} {} {} {}",
						HemorathMove.wells(boss.view().inputs().variant()), boss.syncedWellPositions(), boss.syncedWell(0),
						boss.syncedWell(1), boss.syncedWell(2), boss.syncedWell(3));
			};
			SCENES.add(new Scene(move.key(), length(move, phase), view, false, () -> force(move, phase), during, 3));
		}
		// the melee strikes again from three-quarters, where the swing's arc is not foreshortened toward the camera
		for (HemorathMove move : new HemorathMove[]{HemorathMove.RAKING_CLAW, HemorathMove.BACKHAND, HemorathMove.STAFF_HEEL,
				HemorathMove.STAFF_FALL, HemorathMove.CROWN_FALL}) {
			int phase = move.inPhase(1) ? 1 : 2;
			SCENES.add(new Scene(move.key() + "_side", length(move, phase), SIDE, false, () -> force(move, phase), NONE, 3));
		}
		// the trenches (pass 2b) again from a player's eye on the floor
		for (HemorathMove move : new HemorathMove[]{HemorathMove.IRON_RAKE, HemorathMove.RIB_BURST}) {
			int phase = move.inPhase(1) ? 1 : 2;
			SCENES.add(new Scene(move.key() + "_low", length(move, phase), LOW, false, () -> force(move, phase), NONE, 3));
		}
		SCENES.add(new Scene("tithe_paid", length(HemorathMove.TITHE_RECKONING, 1), MID, false,
				() -> force(HemorathMove.TITHE_RECKONING, 1), t -> {
					if (t == 8) {
						boss.tithe().pay(quarry.getUUID(), boss.tithe().debt(quarry.getUUID()));
						boss.tithe().clear(quarry.getUUID());
					}
				}));
		for (var spec : HemorathOpenings.ALL) {
			SCENES.add(new Scene("open_" + spec.key(), Math.min(60, spec.ticks() + 6), CLOSE, false, () -> open(spec), NONE));
		}
		SCENES.add(new Scene("hud_0", 12, CLOSE, false, () -> meter(0.0F), NONE));
		SCENES.add(new Scene("hud_35", 12, CLOSE, false, () -> meter(0.36F), NONE));
		SCENES.add(new Scene("hud_70", 12, CLOSE, false, () -> meter(0.71F), NONE));
		SCENES.add(new Scene("hud_95", 24, CLOSE, false, () -> meter(0.95F), NONE));
		SCENES.add(new Scene("vessel_empties", 150, MID, true, () -> {
			reset(1);
			boss.feed(boss.meter().max(), quarry, false);
		}, NONE));
		SCENES.add(new Scene("last_thirst", 170, WIDE, false, () -> {
			reset(2);
			boss.setHealth(boss.getMaxHealth() * 0.19F);
			boss.controller().delay(0);
		}, NONE));
		SCENES.add(new Scene("ending", 140, MID, false, () -> {
			reset(2);
			rewardsBefore = residuum();
			boss.hurt(player.serverLevel().damageSources().playerAttack(quarry), 100_000);
		}, NONE));
		SCENES.add(new Scene("restored", 24, WIDE, false, () -> {
		}, NONE));
		// -PhemorathReviewOnly: the rise (which wakes him) and the named scenes only, for quick visual rounds
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
			System.getProperty("hemomancy.hemorathFightReview.only", "").split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();

	/** The scene's length: the move's whole timeline in that phase, plus a little. */
	private static int length(HemorathMove move, int phase) {
		BossMoveTimeline timeline = move.timeline(new com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs(phase,
				0, 0, 1));
		return Math.min(170, timeline.total() + 12);
	}

	/** The encounter's own spawn out of the sarcophagus, as the block runs it, then the block cleared. */
	private static void rise() {
		ServerLevel level = player.serverLevel();
		if (!HarbingerSaintEncounterHooks.spawnSaintBoss(level, TOMB, quarry, EnumSaintType.HEMORATH))
			throw new IllegalStateException("the sarcophagus did not wake Hemorath");
		level.setBlockAndUpdate(TOMB, Blocks.AIR.defaultBlockState());
		boss = level.getEntitiesOfClass(HemorathEntity.class, new AABB(TOMB).inflate(6)).stream().findFirst()
				.orElseThrow(() -> new IllegalStateException("Hemorath did not rise"));
		RESULTS.addProperty("rise_bound_to_tomb", TOMB.equals(boss.getTombPos()));
	}

	private static void reset(int phase) {
		boss.moveTo(ORIGIN.getX() + PIT_X, ORIGIN.getY() + 1, ORIGIN.getZ() + PIT_Z, 180, 0);
		boss.setHealth(boss.getMaxHealth() * (phase >= 2 ? 0.45F : 0.95F));
		boss.openings().close();
		boss.controller().delay(100_000);
		place(quarry, 17.5, 9.0);
		quarry.setHealth(quarry.getMaxHealth());
		HemoCapabilityAccess.getBloodVolume(quarry).setBloodVolume(10_000);
	}

	private static void meter(float fraction) {
		reset(1);
		boss.meter().set(fraction * boss.meter().max());
	}

	private static void patches(int count) {
		meter(0.4F);
		boss.forcePatches(count);
	}

	private static void force(HemorathMove move, int phase) {
		reset(phase);
		boss.meter().set((move == HemorathMove.RIB_BURST || move == HemorathMove.EXSANGUINATING_TIDE ? 0.8F : 0.4F)
				* boss.meter().max());
		if (move == HemorathMove.IRON_THICKET || move == HemorathMove.IRON_RAKE || move == HemorathMove.SPIKED_HALO) boss.forcePatches(6);
		if (move == HemorathMove.TITHE_RECKONING) boss.tithe().accrue(quarry.getUUID(), 500, false);
		if (move == HemorathMove.CHAINED_DEBT || move == HemorathMove.SPIKED_HALO || move == HemorathMove.IRON_RAKE
				|| move == HemorathMove.MARROW_SIPHON || move == HemorathMove.GORGE) place(quarry, 10.5, 13.5);
		else if (move.kind() == SaintMove.Kind.MELEE) place(quarry, 17.5, 11.0);
		// the Wells Answer draws the pair the camera faces (the north pair, wells 0 and 1)
		if (move == HemorathMove.WELLS_ANSWER) {
			boss.wells().credit(0, 1000.0F);
			boss.wells().credit(1, 1000.0F);
		}
		boss.controller().start(move, SaintCombatController.Cut.IMMEDIATE);
	}

	private static void open(com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening.Spec spec) {
		meter(spec == HemorathOpenings.GORGED || spec == HemorathOpenings.STRAINED ? 0.8F : 0.4F);
		boss.openings().open(spec, 0);
	}

	/** Stands an entity on the boss floor at chamber-local (x, z). */
	private static void place(ServerPlayer body, double x, double z) {
		body.moveTo(ORIGIN.getX() + x, ORIGIN.getY() + 1, ORIGIN.getZ() + z, 0, 0);
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

	@SubscribeEvent
	public static void server(ServerTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.hemorathFightReview") || event.getServer() != server || done) return;
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
			if (quarry != null && !quarry.isRemoved()) quarry.doTick();
			Scene current = SCENES.get(scene);
			if (tick == 0) {
				view(current.view());
				current.enter().run();
			}
			current.during().accept(tick);
			if (boss != null && !boss.isRemoved() && boss.controller().move() instanceof HemorathMove running
					&& boss.controller().stage() == BossMoveTimeline.Stage.ACTIVE) {
				RESULTS.addProperty(running.key(), true);
			}
			if (++tick >= current.length()) {
				if (current.name().equals("ending")) {
					boolean pass = boss.isRemoved() && boss.hasTrialEnded() && residuum() == rewardsBefore + 1;
					RESULTS.addProperty("ending_rewarded", pass);
					if (!pass) throw new IllegalStateException("the ending did not end the trial once with the reward");
				}
				if (current.name().equals("restored")) restoredCheck();
				if (current.name().equals("vessel_empties")) {
					boolean pass = boss.phases().phase() == 2 && !boss.hasTrialEnded();
					RESULTS.addProperty("vessel_empties_phase_two", pass);
					if (!pass) throw new IllegalStateException("the first full meter did not empty him into phase 2");
				}
				if (current.name().equals("last_thirst")) RESULTS.addProperty("last_thirst_ran", boss.thirstUsed());
				if (++scene >= SCENES.size()) finish();
				else tick = 0;
			}
		} catch (Exception error) {
			fail(error);
		}
	}

	/** Every gutter and basin is back as the template laid it, and the tomb is closed again. */
	private static void restoredCheck() {
		ServerLevel level = player.serverLevel();
		int changed = 0;
		for (var entry : AUTHORED.entrySet()) {
			if (!level.getBlockState(entry.getKey()).equals(entry.getValue())) changed++;
		}
		boolean tomb = level.getBlockState(TOMB).getBlock() instanceof SaintSarcophagusBlock;
		RESULTS.addProperty("arena_cells_changed", changed);
		RESULTS.addProperty("tomb_closed", tomb);
		if (changed > 0 || !tomb) throw new IllegalStateException("the chamber was not put back: " + changed
				+ " set pieces changed, tomb closed " + tomb);
	}

	private static void finish() {
		for (HemorathMove move : HemorathMove.values()) {
			if (move == HemorathMove.LAST_THIRST || move == HemorathMove.GORGE || !ONLY.isEmpty()) continue;
			if (!RESULTS.has(move.key())) throw new IllegalStateException("move never reached its active span: " + move.key());
		}
		cleanup();
		RESULTS.addProperty("passed", true);
		writeReport();
		done = true;
		Hemomancy.LOGGER.info("HEMORATH_FIGHT_REVIEW_OK {}", RESULTS);
		Hemomancy.LOGGER.info("HEMORATH_FIGHT_REVIEW captured {}", SCENES.size());
	}

	private static int residuum() {
		return player.getInventory().items.stream().filter(stack -> stack.is(ItemInit.hallowed_residuum_hemorath.get()))
				.mapToInt(stack -> stack.getCount()).sum()
				+ quarry.getInventory().items.stream().filter(stack -> stack.is(ItemInit.hallowed_residuum_hemorath.get()))
				.mapToInt(stack -> stack.getCount()).sum();
	}

	private static void cleanup() {
		if (boss != null && !boss.isRemoved()) boss.discard();
		if (player != null) {
			ServerLevel level = player.serverLevel();
			for (int cx = ORIGIN.getX() >> 4; cx <= (ORIGIN.getX() + 36) >> 4; cx++) {
				for (int cz = ORIGIN.getZ() >> 4; cz <= (ORIGIN.getZ() + 26) >> 4; cz++) level.setChunkForced(cx, cz, false);
			}
		}
		if (quarry != null) quarry.discard();
		if (quarryChannel != null) quarryChannel.finishAndReleaseAll();
	}

	private static void fail(Exception error) {
		RESULTS.addProperty("passed", false);
		RESULTS.addProperty("error", error.toString());
		Hemomancy.LOGGER.error("HEMORATH_FIGHT_REVIEW_FAILED", error);
		cleanup();
		writeReport();
		done = true;
	}

	private static void writeReport() {
		try {
			Files.createDirectories(report.getParent());
			Files.writeString(report, new GsonBuilder().setPrettyPrinting().create().toJson(RESULTS));
		} catch (Exception error) {
			Hemomancy.LOGGER.error("Could not save Hemorath review report", error);
		}
	}
}

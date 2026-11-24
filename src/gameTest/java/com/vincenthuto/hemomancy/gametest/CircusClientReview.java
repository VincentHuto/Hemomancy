package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusSiteStateService;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.gametest.playthrough.PlaythroughSites;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * With hemomancy.circusReview, films the Crimson Troupe as numbered frame sequences for video: the performers' wound-up
 * defense moves against a husk, the puppets on the Marionette Crossbar, the pavilion at night, the perception overlay at
 * four acclimation scores, the site kit sampler (scene {@code kit}), the rebuilt pavilion by day (scene {@code pavilion}),
 * the five camps and the abandoned stage by day and night, then the ruin and the tenancy laid on the pavilions (scene
 * {@code sites}),
 * and both finales (Succession, Liberation) played by the client player on real copies of the
 * shipped {@code crimson_troupe_pavilion}. The server runs at {@link #RATE} ticks a second, so every game tick gets
 * one captured frame (written as {@code <scene>-<nnnnn>.png} into the game dir's screenshots). The camera is a
 * client-only armor stand moved each client tick, so the owner of the finale stays a real survival player.
 * {@code -PcircusReviewOnly=performers,succession} keeps the named scenes. Run with circusReviewClient; drop
 * {"op":"create","name":"CircusReview","seed":12345} after startup. The log ends with {@code CIRCUS_REVIEW captured N}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CircusClientReview {
	private static final String PROPERTY = "hemomancy.circusReview";
	private static final int RATE = 6;
	private static final int Y = 170;
	private static final Set<ResourceLocation> HUD_TRIM = Set.of(VanillaGuiLayers.HOTBAR, VanillaGuiLayers.PLAYER_HEALTH,
			VanillaGuiLayers.ARMOR_LEVEL, VanillaGuiLayers.FOOD_LEVEL, VanillaGuiLayers.AIR_LEVEL,
			VanillaGuiLayers.EXPERIENCE_BAR, VanillaGuiLayers.EXPERIENCE_LEVEL, VanillaGuiLayers.CROSSHAIR,
			VanillaGuiLayers.SELECTED_ITEM_NAME, VanillaGuiLayers.EFFECTS, VanillaGuiLayers.VEHICLE_HEALTH,
			VanillaGuiLayers.JUMP_METER);

	// --- shared between the server thread (writes) and the client thread (reads) ---------------------------------
	private static volatile Cam cam;
	private static volatile String recording;
	/** 0: no GUI, 1: GUI with the vanilla HUD trimmed (boss bar, chat, mod layers), 2: the full HUD. */
	private static volatile int gui;
	private static volatile boolean finished;

	// --- client --------------------------------------------------------------------------------------------------
	private static boolean launched;
	private static int captured, closing, frame;
	private static String lastRecording;
	private static ArmorStand stand;
	private static Cam lastCam;
	private static int camTicks;
	private static Vec3 eye, look;
	private static float yaw, pitch;

	// --- server --------------------------------------------------------------------------------------------------
	private static MinecraftServer server;
	private static UUID playerId;
	private static int prep = -1;
	private static final List<Scene> SCENES = new ArrayList<>();
	private static int sceneIndex = -1, sceneTick;
	private static BlockPos arena, siteA, siteB, kitSite, campSite;
	private static String camKey;

	private CircusClientReview() {
	}

	// =============================================================================================================
	// Client
	// =============================================================================================================

	/**
	 * The review takes stills, not input: a click on the window would attack whatever the crosshair finds, and from a
	 * detached camera that can be the player's own body, which the server answers by disconnecting. Clicks are drained
	 * before the game reads them.
	 */
	@SubscribeEvent
	public static void swallowClicks(ClientTickEvent.Pre event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		while (mc.options.keyAttack.consumeClick()) {
		}
		while (mc.options.keyUse.consumeClick()) {
		}
		mc.options.keyAttack.setDown(false);
		mc.options.keyUse.setDown(false);
	}

	@SubscribeEvent
	public static void client(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("circus-review")) return;
		if (mc.level == null && mc.screen instanceof AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
		}
		if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
		if (!launched) {
			launched = true;
			server = mc.getSingleplayerServer();
			playerId = mc.player.getUUID();
			mc.options.pauseOnLostFocus = false;
			mc.options.particles().set(ParticleStatus.ALL);
			mc.options.setCameraType(CameraType.FIRST_PERSON);
			mc.options.tutorialStep = net.minecraft.client.tutorial.TutorialSteps.NONE;
			mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
			prep = 0;
			Hemomancy.LOGGER.info("CIRCUS_REVIEW started");
		}
		if (mc.screen != null && !(mc.screen instanceof ChatScreen)) mc.setScreen(null);
		mc.getToasts().clear();
		int guiMode = gui;
		mc.options.hideGui = guiMode == 0;
		if (finished) {
			if (++closing > 60) {
				Hemomancy.LOGGER.info("CIRCUS_REVIEW captured {}", captured);
				mc.stop();
			}
			return;
		}
		Cam now = cam;
		if (now instanceof PlayerView) {
			if (mc.getCameraEntity() != mc.player) mc.setCameraEntity(mc.player);
			mc.gameRenderer.setRenderHand(true);
			if (now != lastCam) camTicks = 0;
			else camTicks++;
			lastCam = now;
			PlayerView view = (PlayerView) now;
			float y = (float) (view.yaw() + view.swing() * Math.sin(camTicks / view.period()));
			mc.player.yRotO = mc.player.getYRot();
			mc.player.xRotO = mc.player.getXRot();
			mc.player.setYRot(y);
			mc.player.setXRot(view.pitch());
			mc.player.yHeadRot = y;
		} else if (now != null) {
			driveStand(mc, now);
		}
		String rec = recording;
		if (rec != null) {
			if (!rec.equals(lastRecording)) {
				frame = 0;
				mc.gui.getChat().clearMessages(false);
			}
			Screenshot.grab(mc.gameDirectory, String.format(Locale.ROOT, "%s-%05d.png", rec, frame++),
					mc.getMainRenderTarget(), message -> {});
			captured++;
		}
		lastRecording = rec;
	}

	private static void driveStand(Minecraft mc, Cam now) {
		if (stand == null || stand.isRemoved() || stand.level() != mc.level) {
			stand = new ArmorStand(EntityType.ARMOR_STAND, mc.level);
			stand.setInvisible(true);
			stand.setNoGravity(true);
			stand.setPos(mc.player.position());
			mc.level.addEntity(stand);
			lastCam = null;
		}
		if (mc.getCameraEntity() != stand) mc.setCameraEntity(stand);
		if (mc.options.getCameraType() != CameraType.FIRST_PERSON) mc.options.setCameraType(CameraType.FIRST_PERSON);
		mc.gameRenderer.setRenderHand(false);
		boolean cut = now != lastCam;
		camTicks = cut ? 0 : camTicks + 1;
		lastCam = now;
		Vec3[] wanted = now.at(mc.level, camTicks);
		if (wanted == null) return;
		Vec3 prevEye = eye;
		float prevYaw = yaw, prevPitch = pitch;
		if (cut || eye == null || !now.smooth()) {
			eye = wanted[0];
			look = wanted[1];
		} else {
			eye = eye.lerp(wanted[0], 0.22D);
			look = look.lerp(wanted[1], 0.3D);
		}
		Vec3 d = look.subtract(eye);
		float wantedYaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
		yaw = prevEye == null || cut ? wantedYaw : prevYaw + Mth.wrapDegrees(wantedYaw - prevYaw);
		pitch = (float) -Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
		double lift = stand.getEyeHeight();
		stand.setPos(eye.x, eye.y - lift, eye.z);
		stand.setYRot(yaw);
		stand.setXRot(pitch);
		stand.yHeadRot = yaw;
		if (cut || prevEye == null) {
			stand.setOldPosAndRot();
		} else {
			stand.xo = stand.xOld = prevEye.x;
			stand.yo = stand.yOld = prevEye.y - lift;
			stand.zo = stand.zOld = prevEye.z;
			stand.yRotO = prevYaw;
			stand.xRotO = prevPitch;
		}
	}

	/** The owner's name tag would float over every shot of the finale (the camera is not the player). */
	@SubscribeEvent
	public static void hideOwnName(net.neoforged.neoforge.client.event.RenderNameTagEvent event) {
		if (Boolean.getBoolean(PROPERTY) && event.getEntity() == Minecraft.getInstance().player)
			event.setCanRender(net.neoforged.neoforge.common.util.TriState.FALSE);
	}

	@SubscribeEvent
	public static void trimHud(RenderGuiLayerEvent.Pre event) {
		if (!Boolean.getBoolean(PROPERTY) || gui != 1) return;
		if (HUD_TRIM.contains(event.getName())) event.setCanceled(true);
	}

	// =============================================================================================================
	// Cameras (computed on the client from its own entities)
	// =============================================================================================================

	private sealed interface Cam permits Fixed, Watch, Follow, Orbit, Two, Path, PlayerView, Across {
		Vec3[] at(Level level, int t);

		default boolean smooth() {
			return false;
		}
	}

	private record Fixed(Vec3 eye, Vec3 look) implements Cam {
		public Vec3[] at(Level level, int t) {
			return new Vec3[] { eye, look };
		}
	}

	/** A fixed eye that keeps one entity in the middle of the frame. */
	private record Watch(Vec3 eye, int id, double height) implements Cam {
		public Vec3[] at(Level level, int t) {
			Entity e = level.getEntity(id);
			return e == null ? null : new Vec3[] { eye, e.position().add(0, height, 0) };
		}

		public boolean smooth() {
			return true;
		}
	}

	private record Follow(int id, Vec3 offset, double height) implements Cam {
		public Vec3[] at(Level level, int t) {
			Entity e = level.getEntity(id);
			return e == null ? null : new Vec3[] { e.position().add(offset), e.position().add(0, height, 0) };
		}

		public boolean smooth() {
			return true;
		}
	}

	/** Circles a point (or an entity when {@code id >= 0}) at a radius, looking at it. */
	private record Orbit(Vec3 center, int id, double radius, double height, double angle, double degPerTick,
			double lookHeight) implements Cam {
		public Vec3[] at(Level level, int t) {
			Vec3 c = center;
			if (id >= 0) {
				Entity e = level.getEntity(id);
				if (e == null) return null;
				c = e.position();
			}
			double a = Math.toRadians(angle + degPerTick * t);
			return new Vec3[] { c.add(Math.sin(a) * radius, height, -Math.cos(a) * radius), c.add(0, lookHeight, 0) };
		}

		public boolean smooth() {
			return id >= 0;
		}
	}

	/** Two entities side-on: the eye stands off the line between them on a fixed side. */
	private record Two(int a, int b, double distance, double height, Vec3 side) implements Cam {
		public Vec3[] at(Level level, int t) {
			Entity ea = level.getEntity(a), eb = level.getEntity(b);
			if (ea == null || eb == null) return null;
			Vec3 mid = ea.position().add(eb.position()).scale(0.5D);
			Vec3 line = eb.position().subtract(ea.position()).multiply(1, 0, 1);
			Vec3 perp = line.lengthSqr() < 0.01D ? side : new Vec3(-line.z, 0, line.x).normalize();
			if (perp.dot(side) < 0) perp = perp.scale(-1);
			double span = Math.sqrt(line.lengthSqr());
			return new Vec3[] { mid.add(perp.scale(distance + span * 0.35D)).add(0, height, 0), mid.add(0, 1.2D, 0) };
		}

		public boolean smooth() {
			return true;
		}
	}

	/**
	 * The eye over the apron on the far side of the ring from one entity (the circling Strongman), looking across the
	 * ring at him: the fight in the foreground, the colossus behind it, whatever his phase. Rises and falls over
	 * {@code span} ticks between {@code low} and {@code high}.
	 */
	private record Across(int id, Vec3 centre, double radius, double low, double high, int span, double lookHeight)
			implements Cam {
		public Vec3[] at(Level level, int t) {
			Entity e = level.getEntity(id);
			if (e == null) return null;
			Vec3 toward = e.position().subtract(centre).multiply(1, 0, 1);
			if (toward.lengthSqr() < 1.0E-4D) return null;
			toward = toward.normalize();
			double rise = Math.sin(Math.PI * Math.min(1.0D, t / (double) span));
			Vec3 eye = centre.subtract(toward.scale(radius)).add(0, low + (high - low) * rise * rise, 0);
			return new Vec3[] { eye, centre.add(toward.scale(radius * 0.7D)).add(0, lookHeight, 0) };
		}

		public boolean smooth() {
			return true;
		}
	}

	private record Key(int tick, Vec3 eye, Vec3 look) {
	}

	/** A Catmull-Rom path through keyed eyes and look points. */
	private record Path(List<Key> keys) implements Cam {
		public Vec3[] at(Level level, int t) {
			int last = keys.size() - 1;
			if (t >= keys.get(last).tick()) return new Vec3[] { keys.get(last).eye(), keys.get(last).look() };
			int i = 0;
			while (i < last - 1 && keys.get(i + 1).tick() <= t) i++;
			Key k0 = keys.get(Math.max(0, i - 1)), k1 = keys.get(i), k2 = keys.get(i + 1), k3 = keys.get(Math.min(last, i + 2));
			double u = (t - k1.tick()) / (double) (k2.tick() - k1.tick());
			return new Vec3[] { spline(k0.eye(), k1.eye(), k2.eye(), k3.eye(), u),
					spline(k0.look(), k1.look(), k2.look(), k3.look(), u) };
		}

		private static Vec3 spline(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double u) {
			double u2 = u * u, u3 = u2 * u;
			return p0.scale(-0.5D * u3 + u2 - 0.5D * u).add(p1.scale(1.5D * u3 - 2.5D * u2 + 1.0D))
					.add(p2.scale(-1.5D * u3 + 2.0D * u2 + 0.5D * u)).add(p3.scale(0.5D * u3 - 0.5D * u2));
		}
	}

	/** The player's own first-person view, panned slowly about a yaw. */
	private record PlayerView(float yaw, float pitch, float swing, float period) implements Cam {
		public Vec3[] at(Level level, int t) {
			return null;
		}
	}

	// =============================================================================================================
	// Server
	// =============================================================================================================

	@SubscribeEvent
	public static void serverTick(ServerTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY) || event.getServer() != server || prep < 0 || finished) return;
		ServerPlayer player = server.getPlayerList().getPlayer(playerId);
		if (player == null) return;
		try {
			if (sceneIndex < 0) {
				prepare(player);
				return;
			}
			Scene scene = SCENES.get(sceneIndex);
			if (sceneTick == 0) {
				recording = null;
				camKey = null;
				gui = scene.gui();
				command(player, "tick rate 20");
				scene.start(player);
				Hemomancy.LOGGER.info("CIRCUS_REVIEW scene {} begins", scene.name());
			}
			int t = sceneTick++ - Scene.SETTLE;
			if (t < 0) {
				scene.settle(player, t + Scene.SETTLE);
				return;
			}
			if (t == 0) {
				command(player, "tick rate " + scene.rate());
				recording = scene.name();
			}
			boolean done = scene.tick(player, t) || t >= scene.maxTicks();
			if (done) {
				Hemomancy.LOGGER.info("CIRCUS_REVIEW scene {} ends after {} ticks", scene.name(), t);
				recording = null;
				scene.end(player);
				sceneTick = 0;
				if (++sceneIndex >= SCENES.size()) finish(player);
			}
		} catch (Exception error) {
			Hemomancy.LOGGER.error("CIRCUS_REVIEW_FAILED", error);
			recording = null;
			sceneTick = 0;
			if (sceneIndex < 0 || ++sceneIndex >= SCENES.size()) finish(player);
		}
	}

	private static void finish(ServerPlayer player) {
		recording = null;
		command(player, "tick rate 20");
		finished = true;
	}

	private static void prepare(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		int p = prep++;
		if (p == 0) {
			for (String cmd : new String[] { "gamerule doDaylightCycle false", "gamerule doWeatherCycle false",
					"gamerule doMobSpawning false", "gamerule doInsomnia false", "gamerule doFireTick false",
					"gamerule doPatrolSpawning false", "gamerule doTraderSpawning false",
					"gamerule sendCommandFeedback false", "gamerule announceAdvancements false",
					"weather clear", "time set 6000", "difficulty normal", "team add review",
					"team modify review nametagVisibility never", "team join review @s" })
				command(player, cmd);
			BlockPos spawn = player.blockPosition();
			arena = new BlockPos(spawn.getX() - 200, Y, spawn.getZ());
			siteA = new BlockPos(spawn.getX() - 32, Y, spawn.getZ() - 32);
			siteB = new BlockPos(spawn.getX() + 200, Y, spawn.getZ() - 32);
			kitSite = new BlockPos(spawn.getX() - 32, Y, spawn.getZ() + 200);
			campSite = new BlockPos(spawn.getX() - 32, Y, spawn.getZ() + 320);
			player.setGameMode(GameType.SURVIVAL);
			armUp(player, -1);
		} else if (p == 2) {
			buildArena(level);
		} else if (p == 4) {
			buildPavilion(level, siteA);
		} else if (p == 6) {
			buildPavilion(level, siteB);
		} else if (p == 7) {
			buildKit(level, kitSite);
		} else if (p == 9) {
			buildCamps(level, campSite);
		} else if (p == 10) {
			player.teleportTo(level, siteA.getX() + 32.5D, Y + 1, siteA.getZ() + 10.5D, 0.0F, 10.0F);
		} else if (p == 80) {
			castPavilion(level, siteA);
			castPavilion(level, siteB);
			var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
					.get(Hemomancy.rloc("circus_pavilion"));
			PlaythroughSites.installStructure(level, structure, pavilionBox(siteA));
			PlaythroughSites.installStructure(level, structure, pavilionBox(siteB));
			String only = System.getProperty("hemomancy.circusReview.only", "");
			List<String> wanted = only.isBlank() ? List.of() : Arrays.asList(only.split(","));
			for (Scene scene : List.of(new Performers(), new Puppets(), new Bellows(), new Lanterns(), new Perception(),
					new ChamberFinale(null), new ChamberFinale(com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.Ending.KILLED),
					new ChamberFinale(com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.Ending.ABSORBED), new Kit(), new Pavilion(), new Sites())) {
				if (wanted.isEmpty() || wanted.contains(scene.name())) SCENES.add(scene);
			}
			// One ordeal scene per professor with an ordeal: "ordeal" is the Strongman's, the rest "ordeal_<role>" by the
			// role without its circus_ prefix (ordeal_acrobat, ordeal_knife_thrower, ...).
			for (var professor : com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor.all()) {
				Scene scene = new Ordeal(professor);
				if (wanted.isEmpty() || wanted.contains(scene.name())) SCENES.add(scene);
			}
			command(player, "tick rate " + RATE);
			sceneIndex = 0;
			sceneTick = 0;
			if (SCENES.isEmpty()) finish(player);
		}
	}

	// --- sites ---------------------------------------------------------------------------------------------------

	private static void force(ServerLevel level, BlockPos min, BlockPos max) {
		for (int cx = (min.getX() >> 4) - 1; cx <= (max.getX() >> 4) + 1; cx++)
			for (int cz = (min.getZ() >> 4) - 1; cz <= (max.getZ() >> 4) + 1; cz++)
				level.setChunkForced(cx, cz, true);
	}

	private static void fill(ServerLevel level, BlockPos a, BlockPos b, BlockState state) {
		for (BlockPos pos : BlockPos.betweenClosed(a, b)) level.setBlock(pos, state, Block.UPDATE_CLIENTS);
	}

	private static void buildArena(ServerLevel level) {
		BlockPos min = arena.offset(-4, 0, -4), max = arena.offset(44, 0, 44);
		force(level, min, max);
		fill(level, min.below(), max.below(), Blocks.DIRT.defaultBlockState());
		fill(level, min, max, BlockInit.polished_venous_stone_bricks.get().defaultBlockState());
		fill(level, min.above(), max.above(20), Blocks.AIR.defaultBlockState());
		// a tent-cloth backdrop behind the subjects, so the telegraphs read against something
		fill(level, arena.offset(0, 1, 26), arena.offset(40, 5, 26), Blocks.RED_WOOL.defaultBlockState());
	}

	/**
	 * A pavilion site's cells. A site's position is the big top's core origin; since the side rings (2026-11-16) the
	 * template starts {@link #MARGIN} cells west of it, so every scene coordinate stays a core one.
	 */
	private static BoundingBox pavilionBox(BlockPos origin) {
		return BoundingBox.fromCorners(origin.offset(-MARGIN, 0, 0), origin.offset(64 + MARGIN, 22, 64));
	}

	private static final int MARGIN = 8;

	private static void buildPavilion(ServerLevel level, BlockPos origin) {
		BlockPos min = origin.offset(-8 - MARGIN, 0, -8), max = origin.offset(72 + MARGIN, 0, 72);
		force(level, min, max);
		fill(level, min.below(2), max.below(), Blocks.DIRT.defaultBlockState());
		fill(level, min, max, Blocks.GRASS_BLOCK.defaultBlockState());
		fill(level, min.above(), max.above(24), Blocks.AIR.defaultBlockState());
		var template = level.getStructureManager().get(Hemomancy.rloc("crimson_troupe_pavilion")).orElseThrow();
		BlockPos corner = origin.offset(-MARGIN, 0, 0);
		template.placeInWorld(level, corner, corner, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
		CircusPavilionSavedData.get(level).removeSite(level, origin.offset(32, 1, 32));
	}

	/** The Circus site kit's sampler (chunk 15 T7) on a grass field, for the kit scene. */
	private static void buildKit(ServerLevel level, BlockPos origin) {
		BlockPos min = origin.offset(-8, 0, -8), max = origin.offset(74, 0, 56);
		force(level, min, max);
		fill(level, min.below(2), max.below(), Blocks.DIRT.defaultBlockState());
		fill(level, min, max, Blocks.GRASS_BLOCK.defaultBlockState());
		fill(level, min.above(), max.above(26), Blocks.AIR.defaultBlockState());
		var template = level.getStructureManager()
				.get(ResourceLocation.fromNamespaceAndPath("crimson_troupe_validation", "circus_kit_sampler")).orElseThrow();
		template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
	}

	private static final List<String> CAMPS = List.of("air", "blades", "pageantry", "embers", "memory");

	/**
	 * Chunk 15 T10: the five teaching camps in a row 40 blocks apart and the abandoned stage after them, on a grass
	 * field; each template's row 1 lands on the field, as worldgen lands it on the top ground block.
	 */
	private static void buildCamps(ServerLevel level, BlockPos origin) {
		BlockPos min = origin.offset(-24, 0, -24), max = origin.offset(240, 0, 56);
		force(level, min, max);
		fill(level, min.below(2), max.below(), Blocks.DIRT.defaultBlockState());
		fill(level, min, max, Blocks.GRASS_BLOCK.defaultBlockState());
		fill(level, min.above(), max.above(40), Blocks.AIR.defaultBlockState());
		for (int i = 0; i <= CAMPS.size(); i++) {
			String name = i < CAMPS.size() ? "troupe_camp_" + CAMPS.get(i) : "troupe_abandoned_stage";
			BlockPos at = origin.offset(i * 40, -1, 0);
			level.getStructureManager().get(Hemomancy.rloc(name)).orElseThrow()
					.placeInWorld(level, at, at, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
		}
	}

	/** Places any troupe member the template did not bring on its template mark, as the playthrough harness does. */
	private static void castPavilion(ServerLevel level, BlockPos origin) {
		Object[][] marks = {
				{ EntityInit.circus_ringmaster.get(), new Vec3(32.5, 8, 45.5) },
				{ EntityInit.impaled_procession.get(), new Vec3(32.5, 1, 32.5) },
				{ EntityInit.circus_fire_eater.get(), new Vec3(28.5, 1, 28.5) },
				{ EntityInit.circus_acrobat.get(), new Vec3(36.5, 1, 28.5) },
				{ EntityInit.circus_stilt_walker.get(), new Vec3(28.5, 1, 36.5) },
				{ EntityInit.circus_knife_thrower.get(), new Vec3(36.5, 1, 36.5) },
				{ EntityInit.circus_threadkeeper.get(), new Vec3(8.5, 1, 7.5) },
				{ EntityInit.circus_understudy.get(), new Vec3(56.5, 1, 53.5) },
				{ EntityInit.circus_strongman.get(), new Vec3(8.5, 1, 26.5) },
				{ EntityInit.circus_beast_tamer.get(), new Vec3(57.5, 1, 26.5) } };
		AABB box = AABB.of(pavilionBox(origin)).inflate(2.0D);
		for (Object[] mark : marks) {
			EntityType<?> type = (EntityType<?>) mark[0];
			if (!level.getEntitiesOfClass(Entity.class, box, e -> e.isAlive() && e.getType() == type).isEmpty()) continue;
			Vec3 at = Vec3.atLowerCornerOf(origin).add((Vec3) mark[1]);
			Hemomancy.LOGGER.info("CIRCUS_REVIEW the template did not bring {}; placed on its mark {}", type, at);
			Entity entity = type.create(level);
			entity.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
			if (entity instanceof Mob mob) {
				mob.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(at)), MobSpawnType.STRUCTURE, null);
				mob.setPersistenceRequired();
			}
			level.addFreshEntity(entity);
		}
	}

	private static <T extends Entity> T find(ServerLevel level, BlockPos origin, Class<T> type) {
		return level.getEntitiesOfClass(type, AABB.of(pavilionBox(origin)).inflate(2.0D), Entity::isAlive).stream()
				.findFirst().orElse(null);
	}

	// --- helpers -------------------------------------------------------------------------------------------------

	private static void command(ServerPlayer player, String cmd) {
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		server.getCommands().performPrefixedCommand(source, cmd);
	}

	private static Vec3 at(BlockPos origin, double x, double y, double z) {
		return new Vec3(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
	}

	private static void setCam(String key, Supplier<Cam> next) {
		if (key.equals(camKey)) return;
		camKey = key;
		cam = next.get();
	}

	/** Resistance V, regeneration, saturation and fire resistance; Strength at {@code strength} when it is 0 or more. */
	private static void armUp(ServerPlayer player, int strength) {
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200000, 4, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200000, 2, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 200000, 0, false, false));
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200000, 0, false, false));
		player.removeEffect(MobEffects.DAMAGE_BOOST);
		if (strength >= 0) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200000, strength, false, false));
		player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		player.setHealth(player.getMaxHealth());
	}

	private static void nightVision(ServerPlayer player, boolean on) {
		if (on) player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200000, 0, false, false));
		else player.removeEffect(MobEffects.NIGHT_VISION);
	}

	private static void invisible(ServerPlayer player, boolean on) {
		if (on) player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200000, 0, false, false));
		else player.removeEffect(MobEffects.INVISIBILITY);
	}

	private static void place(ServerPlayer player, Vec3 feet, Vec3 lookAt) {
		Vec3 eyes = feet.add(0, player.getEyeHeight(), 0);
		Vec3 d = lookAt.subtract(eyes);
		float yaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
		float pitch = (float) -Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
		player.teleportTo(player.serverLevel(), feet.x, feet.y, feet.z, yaw, pitch);
		player.setYHeadRot(yaw);
	}

	private static void strike(ServerPlayer player, Entity target) {
		player.swing(InteractionHand.MAIN_HAND, true);
		player.attack(target);
	}

	/** Rewrites the Circus attachment's route, challenges and acclimation (the review's own reset between scenes). */
	private static void progress(ServerPlayer player, CircusRouteRules.Route route, int challenges, int acclimation) {
		var progress = HemoCapabilityAccess.circusProgress(player);
		CompoundTag tag = progress.serializeNBT(player.registryAccess());
		tag.putBoolean("Discovered", true);
		tag.putString("Route", route == CircusRouteRules.Route.NEUTRAL ? "" : route.serializedName());
		tag.putInt("Challenges", challenges);
		tag.putInt("Acclimation", acclimation);
		progress.deserializeNBT(player.registryAccess(), tag);
		CircusPlayerProgress.syncPerception(player);
	}

	private static void clearArena(ServerLevel level) {
		level.getEntities((Entity) null, new AABB(arena).inflate(48.0D),
				e -> !(e instanceof Player)).forEach(Entity::discard);
	}

	// =============================================================================================================
	// Scenes
	// =============================================================================================================

	private abstract static class Scene {
		/** Ticks after the player is placed before the first frame, so the client has the chunks. */
		static final int SETTLE = 120;

		abstract String name();

		/** See {@link #gui}. */
		int gui() {
			return 0;
		}

		abstract int maxTicks();

		/**
		 * The tick rate while recording: slowed so every tick is a frame for stills, or the real 20 for a fight whose
		 * timing and weight are what is being judged.
		 */
		int rate() {
			return RATE;
		}

		abstract void start(ServerPlayer player);

		void settle(ServerPlayer player, int t) {
		}

		/** One recorded tick; true when the scene is over. */
		abstract boolean tick(ServerPlayer player, int t);

		void end(ServerPlayer player) {
		}
	}

	// --- 1: the performers' wound-up defense moves ---------------------------------------------------------------

	private record Act(String name, Supplier<EntityType<? extends CircusPerformerEntity>> type, double gap, int length,
			boolean wall) {
	}

	private static final class Performers extends Scene {
		private final List<Act> acts = List.of(
				new Act("acrobat_vault", EntityInit.circus_acrobat::get, 4.0D, 165, false),
				new Act("acrobat_lunge", EntityInit.circus_acrobat::get, 4.0D, 170, true),
				new Act("fire_eater", EntityInit.circus_fire_eater::get, 3.5D, 245, false),
				new Act("stilt_walker", EntityInit.circus_stilt_walker::get, 3.0D, 215, false),
				new Act("knife_thrower", EntityInit.circus_knife_thrower::get, 6.0D, 285, false),
				new Act("strongman", EntityInit.circus_strongman::get, 3.0D, 150, false));
		private int act = -1, actTick;
		private CircusPerformerEntity performer;
		private Husk target;
		private Vec3 targetSpot;
		private static Method setThreat;

		String name() {
			return "performers";
		}

		int maxTicks() {
			return 2000;
		}

		void start(ServerPlayer player) {
			nightVision(player, true);
			invisible(player, true);
			command(player, "time set 6000");
			place(player, at(arena, 20.5, 1, 4.5), at(arena, 20.5, 2, 20.5));
			clearArena(player.serverLevel());
			setCam("perf-wait", () -> new Fixed(at(arena, 20.5, 3, 12), at(arena, 20.5, 2, 20.5)));
		}

		boolean tick(ServerPlayer player, int t) {
			ServerLevel level = player.serverLevel();
			if (act < 0 || actTick >= acts.get(act).length()) {
				if (++act >= acts.size()) return true;
				actTick = 0;
				begin(level, acts.get(act));
			}
			Act current = acts.get(act);
			if (actTick == 8) threaten(performer, target);
			if (target != null && target.isAlive()) {
				if (target.position().distanceToSqr(targetSpot) > 0.09D) target.teleportTo(targetSpot.x, targetSpot.y, targetSpot.z);
				Vec3 d = performer.position().subtract(target.position());
				float yaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
				target.getLookControl().setLookAt(performer, 30.0F, 30.0F);
				if (actTick < 3) {
					target.setYRot(yaw);
					target.setYHeadRot(yaw);
					target.setYBodyRot(yaw);
				}
				target.setHealth(target.getMaxHealth());
				if (target.getRemainingFireTicks() > 0 && actTick % 30 == 0) target.clearFire();
			}
			if (actTick == current.length() - 1) Hemomancy.LOGGER.info("CIRCUS_REVIEW act {} done", current.name());
			actTick++;
			return false;
		}

		private void begin(ServerLevel level, Act a) {
			clearArena(level);
			fill(level, arena.offset(0, 1, 18), arena.offset(40, 4, 24), Blocks.AIR.defaultBlockState());
			Vec3 p = at(arena, 16.5, 1, 20.5), q = p.add(a.gap(), 0, 0);
			if (a.wall()) {
				BlockPos tq = BlockPos.containing(q);
				fill(level, tq.offset(1, 0, -1), tq.offset(3, 2, 1), Blocks.RED_WOOL.defaultBlockState());
			}
			performer = a.type().get().create(level);
			performer.moveTo(p.x, p.y, p.z, -90.0F, 0.0F);
			performer.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(p)), MobSpawnType.STRUCTURE, null);
			performer.setPersistenceRequired();
			level.addFreshEntity(performer);
			target = EntityType.HUSK.create(level);
			// a still dummy that keeps facing the performer: AI on (so the body follows the look), every goal removed
			target.goalSelector.removeAllGoals(goal -> true);
			target.targetSelector.removeAllGoals(goal -> true);
			target.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);
			target.setPersistenceRequired();
			target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500.0D);
			target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
			target.setHealth(500.0F);
			target.moveTo(q.x, q.y, q.z, 90.0F, 0.0F);
			level.addFreshEntity(target);
			targetSpot = q;
			Vec3 mid = p.add(q).scale(0.5D);
			double distance = a.gap() > 5 ? 6.5D : 5.0D;
			setCam("perf-" + a.name(), () -> new Fixed(mid.add(0, 1.5D, -distance), mid.add(0, 1.0D, 0)));
			Hemomancy.LOGGER.info("CIRCUS_REVIEW act {} begins at frame {}", a.name(), sceneTick - SETTLE);
		}

		private static void threaten(CircusPerformerEntity performer, LivingEntity target) {
			try {
				if (setThreat == null) {
					setThreat = CircusPerformerEntity.class.getDeclaredMethod("setThreat", LivingEntity.class);
					setThreat.setAccessible(true);
				}
				setThreat.invoke(performer, target);
			} catch (ReflectiveOperationException error) {
				throw new IllegalStateException(error);
			}
		}

		void end(ServerPlayer player) {
			clearArena(player.serverLevel());
			fill(player.serverLevel(), arena.offset(0, 1, 18), arena.offset(40, 4, 24), Blocks.AIR.defaultBlockState());
		}
	}

	// --- 4: the puppets ----------------------------------------------------------------------------------------------

	private static final class Puppets extends Scene {
		private static final String[] SHAPES = { PuppeteerSummonDefinitions.MARROW_SPITTER,
				PuppeteerSummonDefinitions.VEINWING_VULTURE, PuppeteerSummonDefinitions.SCARLET_MUMMER,
				PuppeteerSummonDefinitions.GOREBOUND_HULK };
		private final int[] ids = new int[4];

		String name() {
			return "puppets";
		}

		int maxTicks() {
			return 680;
		}

		void start(ServerPlayer player) {
			ServerLevel level = player.serverLevel();
			clearArena(level);
			nightVision(player, true);
			invisible(player, false);
			command(player, "time set 6000");
			place(player, at(arena, 20.5, 1, 14.5), at(arena, 20.5, 1.6, 24.5));
			for (String shape : SHAPES)
				HemoCapabilityAccess.getKnownSummons(player).learn(PuppeteerSummonDefinitions.byName(shape).orElseThrow());
			HemoCapabilityAccess.getSkillProgress(player).setSkill(SkillPointInit.skill_puppet_skein, EnumSkillStates.UNLOCKED, 3);
			ItemStack crossbar = new ItemStack(ItemInit.marionette_crossbar.get());
			player.setItemInHand(InteractionHand.MAIN_HAND, crossbar);
			MarionetteCrossbarItem.bindCrossbar(player.getMainHandItem(), player);
			MarionetteCrossbarItem.addThread(player.getMainHandItem(), 10000);
			setCam("pup-wide", () -> new Fixed(at(arena, 27.5, 3.4, 10.0), at(arena, 19.5, 1.3, 19.0)));
		}

		void settle(ServerPlayer player, int t) {
			place(player, at(arena, 20.5, 1, 14.5), at(arena, 20.5, 1.6, 24.5));
		}

		boolean tick(ServerPlayer player, int t) {
			ServerLevel level = player.serverLevel();
			ItemStack crossbar = player.getMainHandItem();
			if (t >= 6 && t <= 36 && (t - 6) % 10 == 0) {
				int i = (t - 6) / 10;
				MarionetteCrossbarItem.prepareSelectedSummon(crossbar, player, SHAPES[i]);
				MarionetteCrossbarItem.addThread(crossbar, 10000);
				MarionetteCrossbarItem.callOrRecallSelectedSummon(crossbar, player);
				List<Mob> active = MarionetteCrossbarItem.activeSummonsForOwner(player);
				Hemomancy.LOGGER.info("CIRCUS_REVIEW summoned {}: {} active", SHAPES[i], active.size());
				for (Mob mob : active) {
					mob.setInvulnerable(true); // kept for the close-ups after the fight
					String name = summonName(mob);
					for (int s = 0; s < SHAPES.length; s++) if (SHAPES[s].equals(name)) ids[s] = mob.getId();
				}
			}
			if (t == 370) {
				level.getEntitiesOfClass(Husk.class, new AABB(arena).inflate(48.0D)).forEach(Entity::discard);
				// the close-ups: each puppet held still on its own mark round the owner, so the orbit sees it clear
				double[][] marks = { { 3.5, 0, 3.0 }, { -3.5, 1.2, 3.0 }, { -4.0, 0, -2.5 }, { 4.5, 0, -2.5 } };
				for (int i = 0; i < ids.length; i++) {
					if (!(level.getEntity(ids[i]) instanceof Mob mob)) continue;
					Vec3 mark = at(arena, 20.5, 1, 14.5).add(marks[i][0], marks[i][1], marks[i][2]);
					mob.setNoAi(true);
					mob.setDeltaMovement(Vec3.ZERO);
					mob.teleportTo(mark.x, mark.y, mark.z);
					mob.setYRot(0.0F);
					mob.setYHeadRot(0.0F);
					mob.setYBodyRot(0.0F);
				}
			}
			if (t == 60 || t == 180 || t == 270) {
				for (int i = 0; i < (t == 60 ? 4 : 3); i++) {
					Husk husk = EntityType.HUSK.create(level);
					Vec3 at = at(arena, 15.5 + i * 3.5, 1, 24.5);
					husk.moveTo(at.x, at.y, at.z, 180.0F, 0.0F);
					husk.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(at)), MobSpawnType.COMMAND, null);
					husk.setPersistenceRequired();
					husk.setTarget(player);
					level.addFreshEntity(husk);
				}
			}
			if (t % 20 == 0) place(player, at(arena, 20.5, 1, 14.5), at(arena, 20.5, 1.6, 24.5));
			if (t == 380 && ids[0] != 0) setCam("pup-juggler", () -> new Orbit(Vec3.ZERO, ids[0], 2.6D, 1.1D, 200.0D, 2.0D, 0.9D));
			if (t == 480 && ids[1] != 0) setCam("pup-vulture", () -> new Orbit(Vec3.ZERO, ids[1], 2.6D, 0.8D, 160.0D, 2.0D, 0.5D));
			if (t == 580) setCam("pup-crossbar", () -> new Orbit(Vec3.ZERO, player.getId(), 1.6D, 1.35D, 150.0D, 0.9D, 1.15D));
			if (t >= 370 && t % 20 == 0) place(player, at(arena, 20.5, 1, 14.5), at(arena, 20.5, 1.6, 24.5));
			return t >= 660;
		}

		void end(ServerPlayer player) {
			for (Mob mob : MarionetteCrossbarItem.activeSummonsForOwner(player)) mob.discard();
			clearArena(player.serverLevel());
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		}
	}

	private static final class Bellows extends Scene {
		private com.vincenthuto.hemomancy.common.entity.summon.CinderBellowsEntity puppet;

		String name() { return "bellows"; }

		int maxTicks() { return 125; }

		void start(ServerPlayer player) {
			ServerLevel level = player.serverLevel();
			clearArena(level);
			nightVision(player, true);
			command(player, "time set 6000");
			Vec3 mark = at(arena, 20.5, 1, 20.5);
			place(player, mark.add(2, 1, 5), mark.add(0, 1.05, 0));
			puppet = EntityInit.cinder_bellows.get().create(level);
			puppet.hemomancy$setTrialSummon(true);
			puppet.setNoAi(true);
			puppet.setInvulnerable(true);
			puppet.moveTo(mark.x, mark.y, mark.z, 0, 0);
			level.addFreshEntity(puppet);
			setCam("bellows-breath", () -> new Fixed(mark.add(1.9, 1.5, 3.25), mark.add(0, 1, 0.7)));
		}

		boolean tick(ServerPlayer player, int t) {
			puppet.setDemonstrationCycle(t < 12 || t >= 112 ? 0 : t - 11);
			return t >= 120;
		}

		void end(ServerPlayer player) {
			puppet.discard();
		}
	}

	/** The summon name a bound puppet carries. */
	private static String summonName(Mob mob) {
		return mob instanceof com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon summon
				? summon.hemomancy$getSummonName() : "";
	}

	// --- 5: the pavilion at night --------------------------------------------------------------------------------

	private static final class Lanterns extends Scene {
		String name() {
			return "lanterns";
		}

		int maxTicks() {
			return 700;
		}

		void start(ServerPlayer player) {
			nightVision(player, false);
			invisible(player, true);
			command(player, "time set 18000");
			place(player, at(siteA, 32.5, 1, 20.5), at(siteA, 32.5, 2, 32.5));
			List<Key> keys = new ArrayList<>();
			keys.add(new Key(0, at(siteA, 32.5, 8, 0.5), at(siteA, 32.5, 3, 20)));
			keys.add(new Key(110, at(siteA, 32.5, 3.4, 17), at(siteA, 32.5, 3.5, 32.5)));
			Vec3 c = at(siteA, 32.5, 0, 32.5);
			for (int i = 0; i <= 12; i++) {
				double a = Math.toRadians(i * 30.0D);
				keys.add(new Key(160 + i * 30, c.add(-Math.sin(a) * 9.0D, 3.5D, -Math.cos(a) * 9.0D), c.add(0, 4.0D, 0)));
			}
			keys.add(new Key(580, at(siteA, 25.5, 6.5, 25.0), at(siteA, 33.0, 6.5, 40.0)));
			keys.add(new Key(650, at(siteA, 39.5, 6.8, 25.0), at(siteA, 26.0, 6.0, 39.0)));
			setCam("lanterns", () -> new Path(keys));
		}

		boolean tick(ServerPlayer player, int t) {
			return t >= 660;
		}

		void end(ServerPlayer player) {
			command(player, "time set 6000");
			invisible(player, false);
		}
	}

	// --- the rebuilt pavilion by day: the midway, the big top, both galleries, the box, the backyard -----------------

	private static final class Pavilion extends Scene {
		String name() {
			return "pavilion";
		}

		int maxTicks() {
			return 680;
		}

		void start(ServerPlayer player) {
			nightVision(player, false);
			invisible(player, true);
			command(player, "time set 6000");
			place(player, at(siteA, 32.5, 1, 2.5), at(siteA, 32.5, 4, 13));
			List<Key> keys = new ArrayList<>();
			keys.add(new Key(0, at(siteA, 32.5, 9, -14), at(siteA, 32.5, 6, 14)));
			keys.add(new Key(50, at(siteA, 32.5, 3, 6), at(siteA, 32.5, 3, 20)));
			keys.add(new Key(90, at(siteA, 32.5, 3.2, 16), at(siteA, 32.5, 4, 40)));
			keys.add(new Key(140, at(siteA, 18.5, 4, 24), at(siteA, 40, 8, 46)));
			keys.add(new Key(190, at(siteA, 20.5, 9.6, 48.5), at(siteA, 32.5, 8.5, 45)));
			keys.add(new Key(240, at(siteA, 31.2, 9.6, 46.4), at(siteA, 32.5, 2, 30)));
			keys.add(new Key(290, at(siteA, 46.5, 7.6, 15.5), at(siteA, 25, 6, 15)));
			keys.add(new Key(340, at(siteA, 44.5, 12, 22), at(siteA, 20, 8, 40)));
			keys.add(new Key(390, at(siteA, 66, 10, 66), at(siteA, 44, 3, 55)));
			keys.add(new Key(440, at(siteA, 70, 14, 34), at(siteA, 40, 5, 34)));
			// The side rings (2026-11-16): over the Beast Tamer's from the north, then across to the Strongman's.
			keys.add(new Key(500, at(siteA, 63, 13, 13), at(siteA, 63, 1, 32)));
			keys.add(new Key(560, at(siteA, 63, 13, 13), at(siteA, 63, 1, 32)));
			keys.add(new Key(600, at(siteA, 1, 13, 13), at(siteA, 1, 1, 32)));
			keys.add(new Key(660, at(siteA, 1, 13, 13), at(siteA, 1, 1, 32)));
			setCam("pavilion", () -> new Path(keys));
		}

		boolean tick(ServerPlayer player, int t) {
			return t >= 670;
		}

		void end(ServerPlayer player) {
			invisible(player, false);
		}
	}

	// --- chunk 15 T10, T11: the camps and the stage by day and night, then the ruin and the tenancy ------------------

	private static final class Sites extends Scene {
		String name() {
			return "sites";
		}

		int maxTicks() {
			return 640;
		}

		void start(ServerPlayer player) {
			nightVision(player, false);
			invisible(player, true);
			command(player, "time set 6000");
			place(player, at(campSite, 13, 1, 34), at(campSite, 13, 1, 13));
			List<Key> keys = new ArrayList<>();
			for (int i = 0; i < CAMPS.size(); i++) {
				double x = i * 40;
				keys.add(new Key(i * 50, at(campSite, x + 13, 10, 36), at(campSite, x + 13, 2, 14)));
				keys.add(new Key(i * 50 + 30, at(campSite, x + 1, 8, 27), at(campSite, x + 14, 2, 11)));
			}
			keys.add(new Key(250, at(campSite, 206, 8, 24), at(campSite, 207, 2, 6)));
			keys.add(new Key(290, at(campSite, 197, 5, 12), at(campSite, 205, 3, 5)));
			// night: the air camp, the embers camp and the stage again, from near their lamps
			keys.add(new Key(310, at(campSite, 2, 5, 18), at(campSite, 13, 2, 15)));
			keys.add(new Key(360, at(campSite, 141, 5, 24), at(campSite, 133, 2, 13)));
			keys.add(new Key(410, at(campSite, 199, 5, 9), at(campSite, 205, 4, 5)));
			// by day: the ruin laid over one pavilion, then the successor's banner behind the podium in the other
			keys.add(new Key(450, at(siteB, 20.5, 7, 20.5), at(siteB, 32.5, 2, 32.5)));
			keys.add(new Key(520, at(siteB, 44.5, 9, 44.5), at(siteB, 24, 12, 26)));
			keys.add(new Key(560, at(siteA, 32.5, 11.5, 38.5), at(siteA, 32.5, 10, 47)));
			keys.add(new Key(630, at(siteA, 32.5, 10.4, 44.8), at(siteA, 32.5, 10.2, 47)));
			setCam("sites", () -> new Path(keys));
		}

		boolean tick(ServerPlayer player, int t) {
			if (t == 300) command(player, "time set 18000");
			if (t == 430) {
				command(player, "time set 6000");
				place(player, at(siteB, 20.5, 1, 20.5), at(siteB, 32.5, 2, 32.5));
				ServerLevel level = player.serverLevel();
				CircusSiteStateService.begin(level, siteB.offset(32, 1, 32), CircusSiteStateService.State.RUIN, null);
				CircusSiteStateService.begin(level, siteA.offset(32, 1, 32), CircusSiteStateService.State.TENANCY,
						player.getGameProfile().getName());
			}
			if (t == 545) place(player, at(siteA, 32.5, 8, 44.5), at(siteA, 32.5, 10, 47));
			return t >= 635;
		}

		void end(ServerPlayer player) {
			invisible(player, false);
		}
	}

	// --- the site kit: every set piece on the sampler, by day ------------------------------------------------------

	private static final class Kit extends Scene {
		String name() {
			return "kit";
		}

		int maxTicks() {
			return 400;
		}

		void start(ServerPlayer player) {
			nightVision(player, false);
			invisible(player, true);
			command(player, "time set 6000");
			place(player, at(kitSite, 33, 1, 24), at(kitSite, 33, 2, 24));
			List<Key> keys = new ArrayList<>();
			// close passes: the ring, the box and gallery, the seating, the tower; the wagon, cookhouse, tent, booth and
			// wreck; then the camps' apparatus
			keys.add(new Key(0, at(kitSite, 11, 4, 6), at(kitSite, 11, 1, 13)));
			keys.add(new Key(40, at(kitSite, 11, 9, 10), at(kitSite, 11, 7, 23)));
			keys.add(new Key(80, at(kitSite, 5, 3, 18), at(kitSite, 10, 2, 20)));
			keys.add(new Key(110, at(kitSite, 20, 6, 12), at(kitSite, 25, 4, 13)));
			keys.add(new Key(150, at(kitSite, 34, 4, 1), at(kitSite, 34, 2, 7)));
			keys.add(new Key(180, at(kitSite, 43, 4, 0), at(kitSite, 43, 2, 4)));
			keys.add(new Key(210, at(kitSite, 51, 4, 0), at(kitSite, 51, 2, 6)));
			keys.add(new Key(240, at(kitSite, 33, 4, 12), at(kitSite, 36, 3, 17)));
			keys.add(new Key(270, at(kitSite, 50, 5, 14), at(kitSite, 55, 2, 19)));
			keys.add(new Key(300, at(kitSite, 40, 4, 30), at(kitSite, 42, 1, 34)));
			keys.add(new Key(330, at(kitSite, 49, 5, 31), at(kitSite, 52, 1, 36)));
			keys.add(new Key(360, at(kitSite, 58, 4, 31), at(kitSite, 58, 1, 36)));
			setCam("kit", () -> new Path(keys));
		}

		boolean tick(ServerPlayer player, int t) {
			return t >= 370;
		}

		void end(ServerPlayer player) {
			invisible(player, false);
		}
	}

	// --- 6: the perception overlay -------------------------------------------------------------------------------

	private static final class Perception extends Scene {
		String name() {
			return "perception";
		}

		int gui() {
			return 2;
		}

		int maxTicks() {
			return 900;
		}

		void start(ServerPlayer player) {
			nightVision(player, false);
			invisible(player, false);
			command(player, "time set 6000");
			progress(player, CircusRouteRules.Route.NEUTRAL, 0, 0);
			// In the ring below the box, so the upper gallery's rails and decks, the rigging and the curb are in view.
			place(player, at(siteA, 32.5, 1, 36.5), at(siteA, 32.5, 8, 47));
			cam = new PlayerView(0.0F, -14.0F, 30.0F, 45.0F);
			camKey = "perception";
		}

		/**
		 * The phantom show (2026-11-16): each stage from the ring floor by day, the full show at night, from the upper
		 * gallery, then the ruin's hollow echo on the other pavilion.
		 */
		boolean tick(ServerPlayer player, int t) {
			int score = t < 110 ? 0 : t < 230 ? 300 : t < 350 ? 700 : 1000;
			if (t == 0 || t == 110 || t == 230 || t == 350) {
				progress(player, CircusRouteRules.Route.NEUTRAL, 0, score);
				Hemomancy.LOGGER.info("CIRCUS_REVIEW perception {} at frame {} inside {}", score, t,
						HemoCapabilityAccess.circusProgress(player).inside());
			}
			if (t == 470) command(player, "time set 18000");
			if (t == 580) {
				place(player, at(siteA, 31.2, 8, 46.4), at(siteA, 32.5, 2, 30));
				setCam("perception-gallery", () -> new PlayerView(175.0F, 20.0F, 25.0F, 60.0F));
			}
			if (t == 690) {
				command(player, "time set 6000");
				CircusSiteStateService.begin(player.serverLevel(), siteB.offset(32, 1, 32),
						CircusSiteStateService.State.RUIN, null);
				place(player, at(siteB, 20.5, 1, 20.5), at(siteB, 32.5, 2, 32.5));
				setCam("perception-ruin", () -> new PlayerView(-45.0F, -8.0F, 25.0F, 50.0F));
			}
			if (t == 760) Hemomancy.LOGGER.info("CIRCUS_REVIEW perception ruin inside {} ruined {}",
					HemoCapabilityAccess.circusProgress(player).inside(),
					HemoCapabilityAccess.circusProgress(player).ruined());
			return t >= 860;
		}
	}

	// --- the Chamber ordeal (2026-11-17) ---------------------------------------------------------------------------

	/**
	 * The Gorebound Hulk's ordeal on the Strongman's stage in the Chamber of Will: the trial's floor is laid in the
	 * arena, the rite started and handed to the ordeal, then the stage is filmed: the true-self Hulk, the Strongman
	 * circling over the ring and his telegraphed interventions. Late on the Hulk is felled, for the win and the return.
	 */
	private static final class Ordeal extends Scene {
		private final com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor professor;
		private com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite rite;
		private java.util.UUID puppet;

		Ordeal(com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor professor) {
			this.professor = professor;
		}

		String name() {
			return professor == com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor.STRONGMAN ? "ordeal"
					: "ordeal_" + professor.role().replaceFirst("^circus_", "");
		}
		@Override
		int rate() {
			// Filmed at full speed: the wind-ups and impacts are judged in real time.
			return 20;
		}


		int maxTicks() {
			return 760;
		}

		void start(ServerPlayer player) {
			ServerLevel level = player.serverLevel();
			HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(4);
			var blood = HemoCapabilityAccess.getBloodVolume(player);
			blood.setActive(true);
			blood.setMaxBloodVolume(5000.0D);
			blood.setBloodVolume(5000.0D);
			armUp(player, -1);
			command(player, "time set 18000");
			// A trial puppet despawns on Peaceful, as it does in the open; the ordeal needs a fighting world.
			command(player, "difficulty normal");
			var id = Hemomancy.rloc("cardinal_rite/puppeteer_trial_" + professor.summon());
			var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(level, id);
			BlockPos focus = arena.offset(0, 1, 0);
			com.vincenthuto.hemomancy.common.network.PlaceStructurePacket.placeLayeredCardinalRite(level, focus, recipe, player);
			var station = com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher.find(level, focus, recipe).orElseThrow();
			rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.puppeteerTrial(player.getUUID(), focus, id,
					recipe.getRiteType().getSize());
			rite.setMatchedFloor(station.floor().id(), station.floorMatch().getForwards(), station.floorMatch().getUp());
			com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level).startRite(rite);
			// The fighter's own bound crossbar in the seat, so a won ordeal pays out as it does in play.
			ItemStack seat = new ItemStack(ItemInit.marionette_crossbar.get());
			MarionetteCrossbarItem.bindCrossbar(seat, player);
			java.util.UUID crossbarId = MarionetteCrossbarItem.getCrossbarId(seat);
			if (level.getBlockEntity(focus) instanceof com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity seatFocus) {
				seatFocus.extractMedium();
				seatFocus.insertMedium(null, seat);
			}
			player.teleportTo(focus.getX() + 0.5D, focus.getY() + 1.0D, focus.getZ() + 2.5D);
			var definition = PuppeteerSummonDefinitions.byName(professor.summon()).orElseThrow();
			var begun = com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealDirector.begin(player, level, rite,
					professor,
					(chamber, at) -> com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonFactory
							.createTrial(definition, chamber, player, at).filter(chamber::addFreshEntity));
			Hemomancy.LOGGER.info("CIRCUS_REVIEW ordeal begun {} in {}", begun.isPresent(), player.level().dimension().location());
			begun.ifPresent(mob -> {
				puppet = mob.getUUID();
				rite.beginPuppeteerTrial(professor.summon(), puppet, crossbarId);
			});
			// Watched from outside the ring, not from the fighter's eyes: from over the apron across the ring from the
			// Strongman, so the fight is in front and he wades behind it, the eye rising and settling over the scene.
			var stage = com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealDirector.ordeal(player.getUUID())
					.map(o -> o.stage()).orElse(focus);
			centre = net.minecraft.world.phys.Vec3.atBottomCenterOf(stage).add(0, 1, 0);
			// After begin the player is on the stage, so their level is the Chamber the Strongman circles in.
			Entity strongman = com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealDirector.ordeal(player.getUUID())
					.flatMap(o -> o.professorEntity()).map(player.serverLevel()::getEntity).orElse(null);
			cam = strongman == null ? new Fixed(centre.add(10, 6, 10), centre)
					: new Across(strongman.getId(), centre, 12.0D, 4.0D, 11.0D, 740, 4.0D);
			camKey = "ordeal";
		}

		private net.minecraft.world.phys.Vec3 centre;

		boolean tick(ServerPlayer player, int t) {
			if (centre != null && t < 600 && player.level() instanceof ServerLevel stageLevel
					&& stageLevel.dimension().equals(com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.CHAMBER_OF_WILL)) {
				// The fighter fights: circling the Hulk under its own motion, stepping in to hit it, hopping now and then
				// over the waves, and kept on its feet so the scene runs to the end.
				Entity hulk = puppet == null ? null : stageLevel.getEntity(puppet);
				boolean held = hulk != null && professor.puppetMoves().holding(hulk.getUUID(), stageLevel.getGameTime());
				if (hulk != null && !held) {
					// Driven here, not by the client: step toward a point circling the Hulk, carry whatever knockback
					// the fight gave (the server's motion), fall under gravity, hop now and then, then place.
					double a = t * 0.03D;
					boolean pressing = t % 60 >= 40;
					double radius = pressing ? 2.0D : 4.5D;
					net.minecraft.world.phys.Vec3 want = hulk.position().add(Math.cos(a) * radius, 0.0D, Math.sin(a) * radius);
					net.minecraft.world.phys.Vec3 step = want.subtract(player.position()).multiply(1, 0, 1);
					if (step.length() > 0.32D) step = step.normalize().scale(0.32D);
					net.minecraft.world.phys.Vec3 motion = player.getDeltaMovement();
					double floorY = centre.y, y = player.getY();
					double vy = y <= floorY + 1.0E-3D && t % 45 == 0 ? 0.42D : motion.y - 0.08D;
					y += vy;
					if (y < floorY) {
						y = floorY;
						vy = 0.0D;
					}
					double x = player.getX() + step.x + motion.x, z = player.getZ() + step.z + motion.z;
					// Inside the ring, as the ordeal's own clamp keeps a real fighter.
					double ox = x - centre.x, oz = z - centre.z, out = Math.sqrt(ox * ox + oz * oz), limit = 8.5D;
					if (out > limit) {
						x = centre.x + ox / out * limit;
						z = centre.z + oz / out * limit;
					}
					player.setDeltaMovement(motion.x * 0.7D, vy, motion.z * 0.7D);
					net.minecraft.world.phys.Vec3 d = hulk.position().add(0, 1.2D, 0).subtract(x, y + player.getEyeHeight(), z);
					float yaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
					float pitch = (float) -Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
					player.teleportTo(stageLevel, x, y, z, yaw, pitch);
					if (pressing && t % 8 == 0 && player.distanceTo(hulk) < 3.5D) strike(player, hulk);
				}
				if (player.getHealth() < 10.0F) player.setHealth(player.getMaxHealth());
				// Weaken the Hulk in two steps so both of the Strongman's ramps (and their lines) are seen.
				if (hulk instanceof net.minecraft.world.entity.LivingEntity living && (t == 160 || t == 340))
					living.setHealth(living.getMaxHealth() * (t == 160 ? 0.55F : 0.25F));
			}
			if (t % 100 == 0) {
				var o = com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealDirector.ordeal(player.getUUID());
				Entity h = puppet == null || !(player.level() instanceof ServerLevel l) ? null : l.getEntity(puppet);
				Hemomancy.LOGGER.info("CIRCUS_REVIEW ordeal t {} at {} in {} below {} stage {} hulk {}", t, player.blockPosition(),
						player.level().dimension().location(), player.level().getBlockState(player.blockPosition().below()),
						o.map(x -> x.stage()).orElse(null), h == null ? "none" : h.blockPosition() + " " + h.isAlive());
			}
			if (t == 600 && puppet != null && player.level() instanceof ServerLevel chamber) {
				Entity hulk = chamber.getEntity(puppet);
				if (hulk != null) hulk.kill();
			}
			if (t == 700) Hemomancy.LOGGER.info("CIRCUS_REVIEW ordeal ended home {} rite {}", player.level().dimension().location(),
					rite.getPhase());
			return t >= 740;
		}
	}

	// --- 2 and 3: the finales ------------------------------------------------------------------------------------

	private abstract static class Finale extends Scene {
		BlockPos origin;
		CircusRingmasterEntity ringmaster;
		CircusCarouselEntity carousel;
		CircusPavilionStateRules.Phase phase = CircusPavilionStateRules.Phase.IDLE;
		int phaseStart, settledAt = -1;

		int gui() {
			return 1;
		}

		int maxTicks() {
			return 2200;
		}

		void start(ServerPlayer player) {
			ServerLevel level = player.serverLevel();
			nightVision(player, true);
			invisible(player, false);
			command(player, "time set 6000");
			armUp(player, -1);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.NETHERITE_SWORD));
			ringmaster = find(level, origin, CircusRingmasterEntity.class);
			carousel = find(level, origin, CircusCarouselEntity.class);
			place(player, at(origin, 32.5, 1, 24.5), at(origin, 32.5, 4, 37.5));
			setCam("fin-wait", () -> new Fixed(at(origin, 21.5, 7.5, 17.5), at(origin, 32.5, 3.5, 32.5)));
		}

		void settle(ServerPlayer player, int t) {
			// The player never sends a move (the camera is not the player), so entity tracking is only re-checked when
			// the player changes section: step out a section and back once the pavilion's chunks have arrived, or the
			// still carousel and perched Ringmaster stay untracked (undrawn) on a pavilion the player has just reached.
			if (t == SETTLE - 30) place(player, at(origin, 32.5, 1, 6.5), at(origin, 32.5, 4, 37.5));
			if (t == SETTLE - 15) place(player, at(origin, 32.5, 1, 24.5), at(origin, 32.5, 4, 37.5));
			if (t == SETTLE - 2) {
				progress(player, CircusRouteRules.Route.NEUTRAL, 0, 1000);
				// D-30: the finale rechecks the pact gate, Degree 4 and one school report.
				HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(Math.max(4,
						HemoCapabilityAccess.getPlayerDegreeNumber(player)));
				com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress.state(player)
						.putBoolean("quest.empty_ring", true);
				CircusPlayerProgress.completeChallenge(player, 0, 0);
				CircusPlayerProgress.syncPerception(player);
			}
		}

		CircusPavilionSavedData.Site site(ServerPlayer player) {
			return CircusPavilionSavedData.get(player.serverLevel()).site(player.serverLevel(), carousel.encounterOrigin());
		}
	}

	/**
	 * The finale in his Chamber (2026-11-18): begun from his dialogue at the real pavilion, filmed from the player's own
	 * eyes at the real 20 ticks a second, through the hidden crossing into the copy. Logs the frame the crossing falls on,
	 * so the frames either side of it can be compared.
	 */
	private static final class ChamberFinale extends Finale {
		/** Phase 1's moves, forced one after another so the film shows each (Task 9); then he is worn down to the reveal. */
		private static final String[] RAFTER_MOVES = {"rafter_volley", "string_lights", "burning_spotlight", "audience_rush",
				"horse_volley", "horse_spiral", "pole_slam", "rafter_volley"};
		private static final int MOVE_GAP = 10;
		/** The moves wait until the merge has come in to the walls (FinaleRules.VEIL_OPENING_TICKS), so they play under it. */
		private static final int MOVES_FROM = 110;
		/** Null for the whole fight; KILLED or ABSORBED for the two endings, brought quickly to the kneel (Task 11). */
		private final com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.Ending ending;
		private int kneelAt = -1, endedAt = -1;
		private boolean crossed;
		private int raftersAt = -1, moveIndex = -1, idleSince = -1, drainAt = -1;
		private String running = "none";
		private int moveStart;
		/** Phase 2's beats, called in turn once he is down (Task 10): SWOOP sends a swoop, CHARGE fills his charge. */
		private static final String[] DESCENT_BEATS = {"staff_strike", "carousel_swing", "echo_cue", "echo_cue", "echo_cue", "SWOOP",
				"CHARGE", "crimson_cascade", "marionette_pull", "staff_strike"};
		/** The three professors this review fighter has passed: their summons, granted the way an ordeal grants them. */
		private static final List<com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor> PASSED = List.of(
				com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor.STRONGMAN,
				com.vincenthuto.hemomancy.common.encounter.ordeal.KnifeThrowerChoreography.PROFESSOR,
				com.vincenthuto.hemomancy.common.encounter.ordeal.FireEaterChoreography.PROFESSOR);
		private int descentIndex = -1, beatIdle = -1, swoopAt = -1, revealAt = -1, finishedAt = -1;
		private float drainFrom = -1.0F;
		private Cam wide;

		ChamberFinale(com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.Ending ending) {
			this.ending = ending;
		}

		String name() {
			return ending == null ? "finale" : ending == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.Ending.KILLED ? "finale_kill" : "finale_absorb";
		}

		@Override
		int rate() {
			return 20;
		}

		@Override
		int maxTicks() {
			return ending == null ? 2600 : 700;
		}

		/** Phase 1: the fighter stands in the ring and each of his moves is called in turn, filmed from over the curb. */
		private void rafters(ServerPlayer player, int t, com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.Finale finale) {
			BlockPos stage = finale.stage();
			if (raftersAt < 0) {
				raftersAt = t;
				Vec3 centre = Vec3.atBottomCenterOf(stage);
				place(player, centre.add(-3.0D, 0.0D, -4.0D), centre.add(0.0D, 1.0D, 0.0D));
				Hemomancy.LOGGER.info("CIRCUS_REVIEW finale rafters at t {}", t);
				// The review's reused world keeps bodies from ordeals cut off by earlier runs' closes on this same stage
				// grid: clear every creature but his copy and the carousel's captives, so the film shows only the finale.
				Entity wheel = player.serverLevel().getEntity(finale.carousel());
				for (Entity stray : player.serverLevel().getEntities((Entity) null, new net.minecraft.world.phys.AABB(stage).inflate(40.0D),
						e2 -> e2 instanceof LivingEntity && !(e2 instanceof ServerPlayer) && !e2.getUUID().equals(finale.ringmaster())
								&& (wheel == null || e2.getVehicle() != wheel)))
					stray.discard();
			}
			// While the merge comes in, the opening's pan of the tent's sides runs on, and he holds his moves.
			if (finale.phaseTicks() < MOVES_FROM) {
				com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.hold(player.getUUID(), MOVE_GAP + 5);
				return;
			}
			if (player.getHealth() < player.getMaxHealth() * 0.5F) player.setHealth(player.getMaxHealth());
			var moves = finale.moves();
			String now = moves.move().id();
			if (!now.equals(running)) {
				Hemomancy.LOGGER.info("CIRCUS_REVIEW finale move {} -> {} at t {}", running, now, t);
				running = now;
				moveStart = t;
				Vec3 centre = Vec3.atBottomCenterOf(stage);
				// The volley from behind the fighter, framing him and one fake (the first volley the first fake, the second
				// the other); the rest from low over the curb, the fighter in the foreground.
				boolean carousel = now.startsWith("horse") || now.equals("pole_slam");
				Vec3 feet = player.position();
				if (now.equals("rafter_volley") && player.serverLevel().getEntity(finale.ringmaster()) instanceof CircusRingmasterEntity copy) {
					// From high over the seats across the ring from him, so he and his fakes stand in the far half of the frame.
					Vec3 toward = new Vec3(copy.getX() - centre.x, 0.0D, copy.getZ() - centre.z).normalize();
					// Turned off the line through him, so the eye never sits on the opposite perch.
					Vec3 back = toward.yRot((float) Math.toRadians(32.0D));
					cam = new Fixed(centre.subtract(back.scale(15.0D)).add(0.0D, 12.5D, 0.0D), centre.add(toward.scale(4.0D)).add(0.0D, 5.0D, 0.0D));
					wide = cam;
				} else if (!now.equals("none")) {
					// Low enough that the frame's top runs out past the merge's edge into his dark.
					cam = carousel ? new Fixed(centre.add(-5.0D, 6.0D, -14.0D), centre.add(0.0D, 2.0D, 2.0D))
							: new Fixed(centre.add(-7.0D, 6.0D, -14.0D), feet.add(1.0D, 1.5D, 4.0D));
				}
			}
			// Through the volley's gather, a close look at him (the first volley) or at a fake (the second): the tell.
			if (now.equals("rafter_volley") && player.serverLevel().getEntity(finale.ringmaster()) instanceof CircusRingmasterEntity him) {
				Vec3 subject = moveIndex == 0 ? him.position() : him.fakeAt(0);
				if (t - moveStart == 5 && subject != null) {
					Vec3 centre = Vec3.atBottomCenterOf(stage);
					Vec3 in = new Vec3(centre.x - subject.x, 0.0D, centre.z - subject.z).normalize();
					cam = new Fixed(subject.add(in.scale(6.5D)).add(in.yRot(1.2F).scale(2.0D)).add(0.0D, -1.0D, 0.0D), subject.add(0.0D, 3.0D, 0.0D));
				}
				if (t - moveStart == 15 && wide != null) cam = wide;
			}
			if (moves.move() != com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.Move.NONE) {
				idleSince = -1;
				return;
			}
			if (idleSince < 0) idleSince = t;
			if (t - idleSince < MOVE_GAP) {
				com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.hold(player.getUUID(), MOVE_GAP + 5);
				return;
			}
			if (moveIndex + 1 < RAFTER_MOVES.length) {
				moveIndex++;
				// He is worn down across the phase, from full to just over half, so the merge comes in through the
				// galleries as it would in a fight (FinaleRules.veilStrength strengthens as he is worn down).
				if (player.serverLevel().getEntity(finale.ringmaster()) instanceof CircusRingmasterEntity worn)
					worn.setHealth(worn.getMaxHealth() * (1.0F - 0.44F * moveIndex / (RAFTER_MOVES.length - 1)));
				// Back to the fighter's mark, so every move is aimed at the same spot in frame (a blow may have thrown them).
				Vec3 centre = Vec3.atBottomCenterOf(stage);
				place(player, centre.add(-3.0D, 0.0D, -4.0D), centre.add(0.0D, 1.0D, 0.0D));
				com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.forceNext(player.getUUID(),
						RAFTER_MOVES[moveIndex]);
				idleSince = -1;
			} else if (drainAt < 0) {
				drainAt = t;
				Hemomancy.LOGGER.info("CIRCUS_REVIEW finale drain at t {}", t);
			}
		}

		void start(ServerPlayer player) {
			origin = ending == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.Ending.ABSORBED ? siteB : siteA;
			// The reused world keeps every earlier run's cast where those runs' sites lay; this run's sites move with the
			// player, so older carousels and Ringmasters can stand here keyed to other origins. Keep only this run's.
			ServerLevel here = player.serverLevel();
			BlockPos cell = origin.offset(32, 1, 32);
			Vec3 perchAt = at(origin, 32.5, 8, 45.5);
			for (CircusCarouselEntity old : here.getEntitiesOfClass(CircusCarouselEntity.class, new AABB(origin).inflate(96.0D),
					c -> !c.encounterOrigin().equals(cell) || c.blockPosition().distManhattan(cell) > 1)) {
				List.copyOf(old.getPassengers()).forEach(Entity::discard);
				old.discard();
			}
			List<CircusRingmasterEntity> masters = here.getEntitiesOfClass(CircusRingmasterEntity.class, new AABB(origin).inflate(96.0D),
					r -> !r.stageCopy());
			masters.stream().sorted(java.util.Comparator.comparingDouble(r -> r.distanceToSqr(perchAt))).skip(1).forEach(Entity::discard);
			super.start(player);
			// The review's sites move with where the last run left the player, so an older run's settled site can lie under
			// this one: forget every site record round it, so the finale begins on a clean pavilion.
			ServerLevel level = player.serverLevel();
			for (CircusCarouselEntity any : level.getEntitiesOfClass(CircusCarouselEntity.class, new AABB(origin).inflate(96.0D)))
				CircusPavilionSavedData.get(level).removeSite(level, any.encounterOrigin());
		}

		@Override
		void settle(ServerPlayer player, int t) {
			super.settle(player, t);
			if (t != SETTLE - 1) return;
			// Exactly three professors passed: their summons known, as each ordeal teaches its own. The finale reads them
			// through FinaleEchoes.passed, so only these three echoes perform and are called.
			var known = HemoCapabilityAccess.getKnownSummons(player);
			known.setKnownSummonNames(List.of());
			for (var professor : PASSED)
				com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions.byName(professor.summon())
						.ifPresent(known::learn);
			com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents.sync(player, known);
			// Blood that runs, so his bats have something to steal.
			var blood = HemoCapabilityAccess.getBloodVolume(player);
			blood.setActive(true);
			blood.setMaxBloodVolume(3000.0D);
			blood.setBloodVolume(3000.0D);
			Hemomancy.LOGGER.info("CIRCUS_REVIEW finale passed {}", known.getKnownSummonNames());
			if (ending == null) return;
			// The kneel's prompt is shown once per player: forget it, so each ending's film shows it.
			com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress.state(player)
					.remove(com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.PROMPT_SEEN);
			// The absorbing fighter has earned Succession's acts; the killing one has only the attention act.
			if (ending == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.Ending.ABSORBED)
				for (int c = 0; c < 5; c++) CircusPlayerProgress.completeChallenge(player, c, 0);
			Hemomancy.LOGGER.info("CIRCUS_REVIEW {} fighter acclimation {} challenges {}", name(), CircusPlayerProgress.acclimation(player),
					Integer.toBinaryString(CircusPlayerProgress.challenges(player)));
		}

		/**
		 * The two endings: he is worn straight down to the reveal, brought to his knees on the ring, and once the kneel
		 * settles the fighter strikes (finale_kill) or channels Blood Absorption at him (finale_absorb); then the real
		 * pavilion is filmed with its ending laid on it.
		 */
		private boolean endingTick(ServerPlayer player, int t, java.util.Optional<com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.Finale> under) {
			if (under.isPresent() && player.serverLevel().getEntity(under.get().ringmaster()) instanceof CircusRingmasterEntity copy) {
				var finale = under.get();
				Vec3 centre = Vec3.atBottomCenterOf(finale.stage());
				var phase = finale.phase();
				if (player.getHealth() < player.getMaxHealth() * 0.5F) player.setHealth(player.getMaxHealth());
				com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.hold(player.getUUID(), 20);
				if (phase == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase.RAFTERS && finale.phaseTicks() == 20) {
					place(player, centre.add(-3.0D, 0.0D, -4.0D), centre.add(0.0D, 1.0D, 0.0D));
					copy.setHealth(copy.getMaxHealth() * 0.48F);
				}
				if (phase == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase.REVEAL && revealAt < 0) {
					revealAt = t;
					cam = new Watch(centre.add(-7.0D, 5.0D, -12.0D), copy.getId(), 2.5D);
				}
				// Down on the ring, a blow that would kill him brings him to his knees.
				if (phase == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase.DESCENT && finale.phaseTicks() == 30) {
					Hemomancy.LOGGER.info("CIRCUS_REVIEW {} lethal blow at t {}", name(), t);
					copy.hurt(player.damageSources().playerAttack(player), 1000.0F);
				}
				if (phase == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase.KNEEL && kneelAt < 0) {
					kneelAt = t;
					Hemomancy.LOGGER.info("CIRCUS_REVIEW {} kneel at t {}", name(), t);
					Vec3 him = copy.position();
					Vec3 out = new Vec3(player.getX() - him.x, 0.0D, player.getZ() - him.z);
					out = out.lengthSqr() < 0.01D ? new Vec3(0.0D, 0.0D, -1.0D) : out.normalize();
					place(player, him.add(out.scale(3.0D)), him.add(0.0D, 1.5D, 0.0D));
					Vec3 side = new Vec3(-out.z, 0.0D, out.x);
					// Side on to the pair, the eye level with the middle between them, so he and the fighter stand at the
					// same distance and his true self's size reads against the fighter's.
					cam = new Fixed(him.add(out.scale(1.5D)).add(side.scale(7.0D)).add(0.0D, 2.2D, 0.0D), him.add(out.scale(1.5D)).add(0.0D, 1.6D, 0.0D));
				}
				if (kneelAt >= 0) {
					int k = t - kneelAt;
					if (ending == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.Ending.KILLED) {
						// Once the kneel has settled, the fighter's own blow.
						if (k == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.KNEEL_SETTLE_TICKS + 40) {
							Hemomancy.LOGGER.info("CIRCUS_REVIEW finale_kill strikes at t {}", t);
							strike(player, copy);
						}
					} else if (k >= com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.KNEEL_SETTLE_TICKS + 30) {
						// The fighter channels Blood Absorption at him, as the item's use tick does.
						if (k % 4 == 0) player.swing(InteractionHand.OFF_HAND, true);
						com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterAbsorption.tryAbsorb(player.level(), player, 6.0D, 1.0F);
					}
				}
			}
			boolean home = crossed && !com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.isInFinale(player)
					&& !player.level().dimension().equals(com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.CHAMBER_OF_WILL);
			if (home && endedAt < 0 && kneelAt >= 0) {
				endedAt = t;
				CircusPavilionSavedData.Site site = site(player);
				Hemomancy.LOGGER.info("CIRCUS_REVIEW {} home at t {}: outcome {} route {} pattern {} retired {}", name(), t, site.outcome(),
						CircusPlayerProgress.route(player), HemoCapabilityAccess.getKnownSummons(player).getKnownSummonNames()
								.contains(com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions.RINGMASTER_PATTERN),
						ringmaster.isAlive() && ringmaster.retired());
				Hemomancy.LOGGER.info("CIRCUS_REVIEW {} thread ripper known {}", name(), HemoCapabilityAccess.getKnownManipulations(player)
						
						.getKnownManips().keySet().stream().anyMatch(m -> m == com.vincenthuto.hemomancy.common.init.ManipulationInit.thread_ripper.get()
								|| m.getName().equals(com.vincenthuto.hemomancy.common.init.ManipulationInit.thread_ripper.get().getName())));
				Vec3 perch = ringmaster.position();
				int id = ringmaster.getId();
				cam = ringmaster.isAlive() ? new Watch(perch.add(-6.0D, -2.0D, -9.0D), id, 1.5D)
						: new Fixed(at(origin, 40.5, 30.0, -16.0), at(origin, 40.5, 2.0, 30.0));
			}
			if (t == 20 && !crossed && !com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.isInFinale(player)) return true;
			return endedAt >= 0 && t - endedAt >= 120;
		}

		/** Phase 2: once he is down, each beat in turn, filmed wide over the curb so the dark and its echoes show. */
		private void descent(ServerPlayer player, int t, com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.Finale finale) {
			Vec3 centre = Vec3.atBottomCenterOf(finale.stage());
			var moves = finale.moves();
			if (player.getHealth() < player.getMaxHealth() * 0.5F) player.setHealth(player.getMaxHealth());
			String now = moves.move().id();
			if (!now.equals(running)) {
				Hemomancy.LOGGER.info("CIRCUS_REVIEW finale move {} -> {} at t {} (charge {}, echoing {})", running, now, t,
						finale.swarm().charge(), moves.echoing());
				running = now;
			}
			if (moves.move() != com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.Move.NONE) {
				beatIdle = -1;
				return;
			}
			com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.hold(player.getUUID(), MOVE_GAP + 5);
			if (beatIdle < 0) beatIdle = t;
			if (t - beatIdle < MOVE_GAP + 5) return;
			String beat = descentIndex + 1 < DESCENT_BEATS.length ? DESCENT_BEATS[descentIndex + 1] : null;
			if (beat == null) {
				if (finishedAt < 0) finishedAt = t;
				return;
			}
			// One echo at a time: the next cue waits for the last to fade.
			if (beat.equals("echo_cue") && moves.echoing()) return;
			if (beat.equals("SWOOP")) {
				if (swoopAt < 0) {
					swoopAt = t;
					com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleSwarm.swoopNow(player.getUUID());
					Hemomancy.LOGGER.info("CIRCUS_REVIEW finale swoop at t {}", t);
					cam = new Fixed(centre.add(-4.0D, 4.0D, -11.5D), player.position().add(0.0D, 3.0D, 2.0D));
				}
				if (t - swoopAt < 70) return;
				Hemomancy.LOGGER.info("CIRCUS_REVIEW finale swoop done, charge {} at t {}", finale.swarm().charge(), t);
			} else if (beat.equals("CHARGE")) {
				com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleSwarm.setCharge(player.getUUID(), 100);
				Hemomancy.LOGGER.info("CIRCUS_REVIEW finale charged at t {}", t);
			} else {
				Vec3 mark = centre.add(-3.0D, 0.0D, -4.0D);
				place(player, mark, centre.add(0.0D, 1.0D, 0.0D));
				com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.forceNext(player.getUUID(), beat);
				// From over the north curb, just inside the island, behind the fighter's shoulder: him, the fighter, the floor's
				// telegraphs and the dark past the island's edge in one frame.
				cam = new Fixed(centre.add(-4.0D, 5.0D, -11.5D), mark.add(1.5D, 0.5D, 3.0D));
			}
			descentIndex++;
			beatIdle = -1;
		}

		boolean tick(ServerPlayer player, int t) {
			if (t == 0) {
				cam = new PlayerView(0.0F, 8.0F, 0.0F, 45.0F);
				// The settle's section-step can leave the player out of his reach (over 24 blocks from the carousel), which
				// was the open "finale begun false": stand them where the finale is asked for.
				Vec3 carouselAt = carousel.position();
				place(player, carouselAt.add(-7.5D, 0.0D, -7.5D), carouselAt.add(0.0D, 4.0D, 0.0D));
				// A reused world can keep the site held by a run that never ended: release it first.
				CircusPavilionSavedData.get(player.serverLevel()).resetOwned(player.getUUID());
				ringmaster.handleChoice(player, CircusRingmasterEntity.EVENT_FINALE);
				Hemomancy.LOGGER.info("CIRCUS_REVIEW {} begun {}", name(),
						com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.isInFinale(player));
			}
			// Not begun: nothing to film.
			if (t == 20 && !crossed && !com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector
					.isInFinale(player)) return true;
			boolean inChamber = player.level().dimension().equals(com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.CHAMBER_OF_WILL);
			if (inChamber && !crossed) {
				crossed = true;
				Hemomancy.LOGGER.info("CIRCUS_REVIEW crossing at t {}", t);
			}
			// Once across, pan the tent's sides so the merge's seams show past the walls.
			if (t == 60) cam = new PlayerView(0.0F, 2.0F, 80.0F, 240.0F);
			var under = com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.finale(player.getUUID());
			if (ending != null) return endingTick(player, t, under);
			if (crossed && under.isPresent() && under.get().phase()
					== com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase.RAFTERS && drainAt < 0)
				rafters(player, t, under.get());
			int drain = drainAt < 0 ? Integer.MAX_VALUE / 2 : drainAt;
			// Wear him down from where the rafter moves left him to just past half: the reveal.
			if (crossed && t >= drain && t <= drain + 30 && under.isPresent()
					&& player.serverLevel().getEntity(under.get().ringmaster()) instanceof CircusRingmasterEntity worn) {
				if (drainFrom < 0.0F) drainFrom = worn.getHealth() / worn.getMaxHealth();
				worn.setHealth(worn.getMaxHealth() * com.vincenthuto.hemomancy.common.util.HemoEasing.lerp(drainFrom, 0.48F, (t - drain) / 30.0F));
			}
			// The reveal, watched from over the curb: his leap from the rigging and his true self.
			if (under.isPresent() && under.get().phase() == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase.REVEAL && revealAt < 0) {
				revealAt = t;
				Hemomancy.LOGGER.info("CIRCUS_REVIEW finale reveal at t {}", t);
				Vec3 centre = Vec3.atBottomCenterOf(under.get().stage());
				int id = player.serverLevel().getEntity(under.get().ringmaster()).getId();
				cam = new Watch(centre.add(-7.0D, 5.0D, -12.0D), id, 2.5D);
			}
			if (under.isPresent() && under.get().phase() == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase.DESCENT) descent(player, t, under.get());
			int end = finishedAt < 0 ? Integer.MAX_VALUE / 2 : finishedAt + 40;
			if (t % 100 == 0)
				Hemomancy.LOGGER.info("CIRCUS_REVIEW finale t {} at {} in {} phase {}", t, player.blockPosition(),
						player.level().dimension().location(), com.vincenthuto.hemomancy.common.encounter.ordeal.finale
								.RingmasterFinaleDirector.finale(player.getUUID()).map(f -> f.phase().name()).orElse("none"));
			if (t == Math.min(maxTicks(), end) - 20)
				com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.end(player, null);
			return t >= Math.min(maxTicks(), end);
		}
	}

}

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
 * four acclimation scores, and both finales (Succession, Liberation) played by the client player on real copies of the
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
	private static BlockPos arena, siteA, siteB;
	private static String camKey;

	private CircusClientReview() {
	}

	// =============================================================================================================
	// Client
	// =============================================================================================================

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

	private sealed interface Cam permits Fixed, Watch, Follow, Orbit, Two, Path, PlayerView {
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
				command(player, "tick rate " + RATE);
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
			player.setGameMode(GameType.SURVIVAL);
			armUp(player, -1);
		} else if (p == 2) {
			buildArena(level);
		} else if (p == 4) {
			buildPavilion(level, siteA);
		} else if (p == 6) {
			buildPavilion(level, siteB);
		} else if (p == 8) {
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
			for (Scene scene : List.of(new Performers(), new Puppets(), new Lanterns(), new Perception(),
					new Succession(), new Liberation())) {
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

	private static BoundingBox pavilionBox(BlockPos origin) {
		return BoundingBox.fromCorners(origin, origin.offset(64, 13, 64));
	}

	private static void buildPavilion(ServerLevel level, BlockPos origin) {
		BlockPos min = origin.offset(-8, 0, -8), max = origin.offset(72, 0, 72);
		force(level, min, max);
		fill(level, min.below(2), max.below(), Blocks.DIRT.defaultBlockState());
		fill(level, min, max, Blocks.GRASS_BLOCK.defaultBlockState());
		fill(level, min.above(), max.above(24), Blocks.AIR.defaultBlockState());
		var template = level.getStructureManager().get(Hemomancy.rloc("crimson_troupe_pavilion")).orElseThrow();
		template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
		CircusPavilionSavedData.get(level).removeSite(level, origin.offset(32, 1, 32));
	}

	/** Places any troupe member the template did not bring on its template mark, as the playthrough harness does. */
	private static void castPavilion(ServerLevel level, BlockPos origin) {
		Object[][] marks = {
				{ EntityInit.circus_ringmaster.get(), new Vec3(32.5, 8, 37.5) },
				{ EntityInit.impaled_procession.get(), new Vec3(32.5, 1, 32.5) },
				{ EntityInit.circus_fire_eater.get(), new Vec3(28.5, 1, 28.5) },
				{ EntityInit.circus_acrobat.get(), new Vec3(36.5, 1, 28.5) },
				{ EntityInit.circus_stilt_walker.get(), new Vec3(28.5, 1, 36.5) },
				{ EntityInit.circus_knife_thrower.get(), new Vec3(36.5, 1, 36.5) },
				{ EntityInit.circus_threadkeeper.get(), new Vec3(8.5, 1, 7.5) },
				{ EntityInit.circus_understudy.get(), new Vec3(56.5, 1, 55.5) },
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

	// --- 6: the perception overlay -------------------------------------------------------------------------------

	private static final class Perception extends Scene {
		String name() {
			return "perception";
		}

		int gui() {
			return 2;
		}

		int maxTicks() {
			return 520;
		}

		void start(ServerPlayer player) {
			nightVision(player, true);
			invisible(player, false);
			command(player, "time set 6000");
			progress(player, CircusRouteRules.Route.NEUTRAL, 0, 0);
			place(player, at(siteA, 32.5, 1, 19.5), at(siteA, 32.5, 2, 32.5));
			cam = new PlayerView(0.0F, 8.0F, 22.0F, 45.0F);
			camKey = "perception";
		}

		boolean tick(ServerPlayer player, int t) {
			int score = t < 110 ? 0 : t < 230 ? 300 : t < 350 ? 700 : 1000;
			if (t == 0 || t == 110 || t == 230 || t == 350) {
				progress(player, CircusRouteRules.Route.NEUTRAL, 0, score);
				Hemomancy.LOGGER.info("CIRCUS_REVIEW perception {} at frame {} inside {}", score, t,
						HemoCapabilityAccess.circusProgress(player).inside());
			}
			return t >= 470;
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

		abstract CircusRouteRules.Route route();

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
				CircusPlayerProgress.chooseRoute(player, route());
				for (int c = 0; c < 5; c++) {
					if (route() == CircusRouteRules.Route.SUCCESSION || c < 2) CircusPlayerProgress.completeChallenge(player, c, 0);
				}
				CircusPlayerProgress.syncPerception(player);
			}
		}

		CircusPavilionSavedData.Site site(ServerPlayer player) {
			return CircusPavilionSavedData.get(player.serverLevel()).site(player.serverLevel(), carousel.encounterOrigin());
		}

		List<CircusPerformerEntity> troupe(ServerPlayer player) {
			return player.serverLevel().getEntitiesOfClass(CircusPerformerEntity.class,
					new AABB(carousel.encounterOrigin()).inflate(20.0D),
					p -> p.isAlive() && p.participatesInFinale());
		}

		boolean tick(ServerPlayer player, int t) {
			if (t == 0) {
				ringmaster.handleChoice(player, CircusRingmasterEntity.EVENT_FINALE);
				Hemomancy.LOGGER.info("CIRCUS_REVIEW {} finale begins: owner {} phase {}", name(),
						player.getUUID().equals(site(player).activeOwner()), site(player).phase());
			}
			CircusPavilionSavedData.Site site = site(player);
			if (site.phase() != phase) {
				Hemomancy.LOGGER.info("CIRCUS_REVIEW {} phase {} -> {} at frame {}", name(), phase, site.phase(), t);
				phase = site.phase();
				phaseStart = t;
			}
			int local = t - phaseStart;
			if (site.outcome() != CircusPavilionStateRules.Outcome.NEUTRAL) {
				if (settledAt < 0) {
					settledAt = t;
					Hemomancy.LOGGER.info("CIRCUS_REVIEW {} settles as {} at frame {}",
							name(), site.outcome(), t);
					Vec3 rm = ringmaster.position();
					Vec3 away = player.position().subtract(rm).multiply(1, 0, 1);
					Vec3 side = away.lengthSqr() < 0.01D ? new Vec3(-1, 0, 0) : new Vec3(-away.z, 0, away.x).normalize();
					setCam("fin-after", () -> new Fixed(rm.add(side.scale(5.5D)).add(0, 2.4D, 0), rm.add(0, 1.2D, 0)));
				}
				if (t - settledAt == 45 && ringmaster.isAlive()) {
					int id = ringmaster.getId();
					setCam("fin-perch", () -> new Watch(at(origin, 34.5, 6.5, 40.0), id, 1.8D));
				}
				return t - settledAt >= 120;
			}
			if (phase == CircusPavilionStateRules.Phase.RAFTERS) rafters(player, local);
			else if (phase == CircusPavilionStateRules.Phase.CAROUSEL) carousel(player, local);
			else if (phase == CircusPavilionStateRules.Phase.DESCENT) descent(player, local);
			return false;
		}

		void rafters(ServerPlayer player, int local) {
			if (local < 64) {
				setCam("raft-wide", () -> new Fixed(at(origin, 21.5, 7.5, 17.5), at(origin, 32.5, 4.0, 33.5)));
			} else if (local < 112) {
				setCam("raft-two", () -> new Fixed(at(origin, 34.5, 3.2, 20.5), at(origin, 32.5, 6.5, 37.5)));
			} else {
				int id = ringmaster.getId();
				setCam("raft-rm", () -> new Watch(at(origin, 34.5, 6.5, 40.0), id, 2.0D));
			}
		}

		abstract void carousel(ServerPlayer player, int local);

		/** Ticks into the descent the carousel shot is held (Liberation keeps it for the collapse). */
		int holdCarousel() {
			return 0;
		}

		void descent(ServerPlayer player, int local) {
			if (local == 0) armUp(player, 1);
			if (local < holdCarousel()) return;
			int a = player.getId(), b = ringmaster.getId();
			setCam("descent", () -> new Two(a, b, 4.2D, 1.8D, new Vec3(-1, 0, 0)));
			if (local >= 100 && local % 15 == 0 && ringmaster.isAlive()) {
				Vec3 feet = player.position();
				if (ringmaster.distanceTo(player) >= 4.5D) {
					Vec3 away = player.position().subtract(ringmaster.position()).multiply(1, 0, 1);
					feet = ringmaster.position().add(away.lengthSqr() < 0.01D ? new Vec3(0, 0, -2.2D) : away.normalize().scale(2.2D));
				}
				place(player, feet, ringmaster.position().add(0, 1.4D, 0));
				strike(player, ringmaster);
			}
		}
	}

	private static final class Succession extends Finale {
		private CircusPerformerEntity downing;

		String name() {
			return "succession";
		}

		CircusRouteRules.Route route() {
			return CircusRouteRules.Route.SUCCESSION;
		}

		void start(ServerPlayer player) {
			origin = siteA;
			super.start(player);
		}

		void carousel(ServerPlayer player, int local) {
			if (local < 110) {
				setCam("car-high", () -> new Fixed(at(origin, 32.5, 6.0, 19.5), at(origin, 32.5, 1.5, 32.0)));
				return;
			}
			setCam("car-orbit", () -> new Orbit(at(origin, 32.5, 1, 32.5), -1, 10.0D, 4.5D, 200.0D, 0.6D, 0.8D));
			if (local == 110) armUp(player, 4);
			if (local % 14 != 0) return;
			if (downing == null || !downing.isAlive() || downing.isDowned()) {
				downing = troupe(player).stream().filter(p -> !p.isDowned()).findFirst().orElse(null);
				if (downing == null) return;
			}
			Vec3 from = downing.position().subtract(player.position()).multiply(1, 0, 1);
			Vec3 stand = downing.position().subtract(from.lengthSqr() < 0.01D ? new Vec3(1.6, 0, 0)
					: from.normalize().scale(1.6D));
			place(player, new Vec3(stand.x, downing.getY(), stand.z), downing.position().add(0, 1.2D, 0));
			strike(player, downing);
		}
	}

	private static final class Liberation extends Finale {
		String name() {
			return "liberation";
		}

		CircusRouteRules.Route route() {
			return CircusRouteRules.Route.LIBERATION;
		}

		int holdCarousel() {
			return 45;
		}

		void start(ServerPlayer player) {
			origin = siteB;
			super.start(player);
		}

		void carousel(ServerPlayer player, int local) {
			Vec3 hub = carousel.position();
			setCam("lib-car", () -> new Fixed(hub.add(-6.5D, 5.0D, -6.5D), hub.add(0, 2.0D, 0)));
			for (int rider = 0; rider < 3; rider++) {
				if (local == 40 + rider * 40) sever(player, rider);
				if (local == 170 + rider * 30) anchor(player, rider);
			}
		}

		private void sever(ServerPlayer player, int rider) {
			BloodDrunkPuppeteerEntity captive = carousel.getPassengers().stream()
					.filter(BloodDrunkPuppeteerEntity.class::isInstance).map(BloodDrunkPuppeteerEntity.class::cast)
					.filter(c -> c.getCarouselHorse() == rider).findFirst().orElse(null);
			if (captive == null) {
				Hemomancy.LOGGER.warn("CIRCUS_REVIEW no captive on horse {}", rider);
				return;
			}
			Vec3 out = captive.position().subtract(carousel.position()).multiply(1, 0, 1).normalize();
			place(player, carousel.position().add(out.scale(4.3D)), captive.position().add(0, 1.0D, 0));
			player.swing(InteractionHand.MAIN_HAND, true);
			var result = player.interactOn(captive, InteractionHand.MAIN_HAND);
			Hemomancy.LOGGER.info("CIRCUS_REVIEW sever horse {}: {} severed mask {}", rider, result, site(player).severedMask());
		}

		private void anchor(ServerPlayer player, int horse) {
			CircusCarouselRules.HorsePose pose = CircusCarouselRules.horsePose(carousel.getYRot(), horse);
			Vec3 point = new Vec3(carousel.getX() + pose.x(), carousel.getY() + 1.1D + pose.bob(), carousel.getZ() + pose.z());
			Vec3 out = new Vec3(pose.x(), 0, pose.z()).normalize();
			place(player, carousel.position().add(out.scale(4.0D)), point);
			strike(player, carousel);
			Hemomancy.LOGGER.info("CIRCUS_REVIEW anchor {}: broken mask {} phase {}", horse, site(player).brokenMask(),
					site(player).phase());
		}
	}
}

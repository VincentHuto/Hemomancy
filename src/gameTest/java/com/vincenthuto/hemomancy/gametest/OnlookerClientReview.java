package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PlayerStateSync;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingService;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffGrowth;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.animus.TendonLineSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.common.world.ground.DarkFields;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Core-loop round two P11: how another player sees a caster. Two clients. The host (hemomancy.onlookerReview, run
 * onlookerReviewClient in build/onlooker-host) opens a disposable world, publishes it on port 25581 and logs
 * ONLOOKER_SERVER_READY; the observer (hemomancy.onlookerObserver, run onlookerObserverClient in
 * build/onlooker-observer) joins it. The host's player is the caster, a real survival player at the fifth Degree who
 * holds the use key; the server moves the observer, a spectator, to a camera spot beside and then in front of the
 * caster for each scene and tells it by a system message when to grab a frame (ONLOOKER_SHOT name, ONLOOKER_DONE at
 * the end). Scenes: plain Projection at base reach and through a five-ring Staff with Sanguine Reach onto a far block;
 * Lignum Mortis on a log and Pyretic Forge's Absorption at a brazier; the 2026-11-12 held forms at Taut Line 2 and Deep
 * Brand 2: Blood Lash flicked and swept through {@code LashCamera} as the caster's mouse would, Gloam Laceration's faint
 * lash, Hematic Flare's beam, Synaptic Jolt on a creature, Blood Eclipse's carried dome, a Cruor Javelin drawn and
 * thrown and Blood Needle Lance's crown of needles; Venous Cling on a wall (crawling, with cleats) and under a
 * ceiling; the Tendon Line thrown and hauled; and Blood Eclipse's field of dark from outside. Start both through
 * tools/review/review-onlooker.ps1. {@code hemomancy.onlookerReview.only} runs only the scenes whose names start with
 * it. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class OnlookerClientReview {
	private static final String HOST = "hemomancy.onlookerReview";
	private static final String OBSERVER = "hemomancy.onlookerObserver";
	public static final int PORT = 25581;
	private static final String SHOT = "ONLOOKER_SHOT ";
	private static final String DONE = "ONLOOKER_DONE";
	private static final int SETUP_TICK = 40;
	private static final int SCENE_TICKS = 100;
	private static final int PRESS_TICK = 12;
	/** Ticks the observer is given to arrive at a camera spot before its frame is grabbed. */
	private static final int SETTLE = 12;
	private static final int HOST_TIMEOUT = 20 * 60 * 6;

	private static int ticks;
	private static int scene = -1;
	private static int sceneStart;
	private static int doneAt = -1;
	private static boolean published;
	private static BlockPos row;
	/** The sweep's offset last tick: the harness turns the mouse by the difference. */
	private static float lastOffset;
	private static List<Scene> scenes;

	// The observer's side.
	private static String pending;
	private static int frames;
	private static boolean connected;

	/**
	 * One staged scene. {@code stand} and {@code aim} are offsets from the row. The use key is held from
	 * {@link #PRESS_TICK} until {@code release} (0 for no use); while held the view sweeps {@code sweep} degrees each
	 * side; {@code crawl} holds forward. {@code captures} are local ticks; even ones are taken from the side, odd ones
	 * from in front. {@code scene} restages the world (server side), or is null to keep the last scene's state.
	 */
	private record Scene(String name, String manipulation, Verb verb, Supplier<Item> held, Vec3 stand, Vec3 aim, int release,
			float sweep, boolean crawl, int[] captures, BiConsumer<ServerLevel, ServerPlayer> scene) {
	}

	private static final Vec3 NEAR_STONE = new Vec3(0.5, 0.5, 5.0);
	private static final Vec3 FAR_STONE = new Vec3(0.5, 0.5, 11.0);
	private static final Vec3 LOG = new Vec3(-6.5, 1.5, 5.5);
	private static final Vec3 BRAZIER = new Vec3(-12.5, 0.4, 5.5);
	private static final Vec3 HUSK = new Vec3(8.5, 0, 7.5);
	private static final Vec3 PIG = new Vec3(14.5, 0, 6.5);
	private static final Vec3 WALL_FACE = new Vec3(20.0, 1.5, 4.5);
	private static final Vec3 CEILING_FACE = new Vec3(26.5, 5.0, 3.5);
	private static final Vec3 TENDON_FACE = new Vec3(-18.5, 5.5, 14.0);
	private static final Vec3 FIELD = new Vec3(-26.5, 0.5, 8.5);

	private static List<Scene> all() {
		return List.of(
				new Scene("reach-bare", "blood_shot", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(0.5, 0, 1.0),
						NEAR_STONE, 70, 0, false, new int[]{40, 58}, OnlookerClientReview::stones),
				new Scene("reach-staff", "blood_shot", Verb.PROJECT, ItemInit.living_staff::get, new Vec3(0.5, 0, 1.0),
						FAR_STONE, 70, 0, false, new int[]{40, 58}, OnlookerClientReview::staffReach),
				new Scene("lignum-log", "lignum_mortis", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(-6.5, 0, 2.0),
						LOG, 70, 0, false, new int[]{36, 54}, OnlookerClientReview::logs),
				new Scene("brazier-absorb", "pyretic_forge", Verb.ABSORB, ItemInit.blood_absorption::get, new Vec3(-12.5, 0, 2.5),
						BRAZIER, 70, 0, false, new int[]{40, 58}, OnlookerClientReview::brazier),
				new Scene("lash-blood", "blood_lash", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(8.5, 0, 2.0),
						HUSK.add(0, 1.2, 0), 80, 32, false, new int[]{34, 52}, OnlookerClientReview::strandSkills),
				new Scene("lash-gloam-faint", "gloam_laceration", Verb.PROJECT, ItemInit.blood_projection::get,
						new Vec3(8.5, 0, 2.0), HUSK.add(0, 1.2, 0), 80, 32, false, new int[]{34, 52}, OnlookerClientReview::strandSkills),
				new Scene("flare-beam", "hematic_flare", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(8.5, 0, 2.0),
						HUSK.add(0, 1.0, 0), 86, 0, false, new int[]{44, 62}, OnlookerClientReview::strandSkills),
				new Scene("jolt-creature", "synaptic_jolt", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(8.5, 0, 2.0),
						HUSK.add(0, 0.8, 0), 80, 0, false, new int[]{36, 54}, OnlookerClientReview::strandSkills),
				new Scene("eclipse-dome", "blood_eclipse", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(8.5, 0, 2.0),
						HUSK.add(0, 0.8, 0), 85, 0, false, new int[]{50, 70}, OnlookerClientReview::strandSkills),
				new Scene("javelin-flight", "cruor_javelin", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(8.5, 0, 2.0),
						HUSK.add(0, 1.0, 0), 52, 0, false, new int[]{40, 54}, OnlookerClientReview::strandSkills),
				new Scene("needle-volley", "blood_needle_lance", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(8.5, 0, 2.0),
						HUSK.add(0, 1.0, 0), 80, 0, false, new int[]{44, 62}, OnlookerClientReview::strandSkills),
				new Scene("cling-wall", "venous_cling", Verb.PROJECT, () -> Items.AIR, new Vec3(20.0 - 0.31, 1.4, 4.5),
						WALL_FACE.add(0, 1.4, 0), 0, 0, true, new int[]{30, 56}, OnlookerClientReview::wall),
				new Scene("cling-ceiling", "venous_cling", Verb.PROJECT, () -> Items.AIR, new Vec3(26.5, 5.0 - 1.81, 2.0),
						CEILING_FACE, 0, 0, false, new int[]{30, 48}, OnlookerClientReview::ceiling),
				new Scene("tendon-throw", "tendon_line", Verb.PROJECT, ItemInit.blood_projection::get, new Vec3(-18.5, 0, 7.5),
						TENDON_FACE, 24, 0, false, new int[]{40, 58}, OnlookerClientReview::tendonWall),
				new Scene("tendon-haul", "tendon_line", Verb.ABSORB, ItemInit.blood_absorption::get, new Vec3(-18.5, 0, 7.5),
						new Vec3(-12.0, 1.6, 7.5), 40, 0, false, new int[]{18, 30}, null),
				new Scene("dark-field", "blood_eclipse", Verb.ABSORB, () -> Items.AIR, new Vec3(-26.5, 0, 2.0),
						FIELD, 0, 0, false, new int[]{40, 58}, OnlookerClientReview::field));
	}

	// ── The host ──

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (Boolean.getBoolean(OBSERVER)) {
			observerTick();
			return;
		}
		if (!Boolean.getBoolean(HOST)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("onlooker-host")) return;
		if (mc.level == null && mc.screen instanceof AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
		}
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		MinecraftServer server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		ticks++;
		if (ticks > HOST_TIMEOUT) {
			Hemomancy.LOGGER.warn("ONLOOKER_REVIEW host timed out at scene {}", scene);
			mc.stop();
			return;
		}
		if (ticks == SETUP_TICK) {
			server.setUsesAuthentication(false);
			published = server.publishServer(GameType.SURVIVAL, true, PORT);
			if (!published) throw new IllegalStateException("Cannot publish the onlooker review world on " + PORT);
			Hemomancy.LOGGER.info("ONLOOKER_SERVER_READY port={}", PORT);
			server.execute(() -> stage(server.getPlayerList().getPlayer(uuid), server));
		}
		if (!published || row == null) return;
		if (doneAt >= 0) {
			// The observer stops itself on DONE; the host follows once it has gone, or after a grace.
			if (server.getPlayerList().getPlayerCount() < 2 || ticks - doneAt > 200) {
				Hemomancy.LOGGER.info("ONLOOKER_REVIEW host finished");
				mc.stop();
			}
			return;
		}
		if (scene < 0) {
			if (server.getPlayerList().getPlayerCount() < 2) return;
			scene = 0;
			sceneStart = ticks + 80;
			Hemomancy.LOGGER.info("ONLOOKER_REVIEW observer joined");
			return;
		}
		if (ticks < sceneStart) return;
		int local = ticks - sceneStart;
		if (local == 0) {
			if (scene >= scenes().size()) {
				doneAt = ticks;
				server.execute(() -> observer(server, uuid).sendSystemMessage(Component.literal(DONE)));
				return;
			}
			Scene current = scenes().get(scene);
			mc.options.setCameraType(CameraType.FIRST_PERSON);
			server.execute(() -> pose(server.getPlayerList().getPlayer(uuid), observer(server, uuid), current));
		}
		if (scene >= scenes().size()) return;
		Scene current = scenes().get(scene);
		if (local == 2) lastOffset = 0.0F;
		if (local == PRESS_TICK && current.release() > 0) {
			KeyMapping.click(mc.options.keyUse.getKey());
			KeyMapping.set(mc.options.keyUse.getKey(), true);
		}
		if (current.sweep() > 0 && local > PRESS_TICK + 4 && local < current.release()) {
			// One tick of mouse through LashCamera, as a hand would: the first turn is the lash's flick, the rest swing its
			// whip aim while the view keeps its capped share.
			float offset = current.sweep() * (float) Math.sin((local - PRESS_TICK) * 0.35D);
			com.vincenthuto.hemomancy.client.player.LashCamera.frame(0.05D);
			com.vincenthuto.hemomancy.client.player.LashCamera.turn(mc.player, (offset - lastOffset) / 0.15D, 0.0D);
			mc.player.setYHeadRot(mc.player.getYRot());
			lastOffset = offset;
		}
		if (current.crawl()) {
			if (local == PRESS_TICK + 4) KeyMapping.set(mc.options.keyUp.getKey(), true);
			if (local == PRESS_TICK + 22) KeyMapping.set(mc.options.keyUp.getKey(), false);
		}
		if (local == current.release() && current.release() > 0) KeyMapping.set(mc.options.keyUse.getKey(), false);
		int[] captures = current.captures();
		for (int i = 0; i < captures.length; i++) {
			boolean front = i % 2 == 1;
			int index = i;
			if (local == captures[i] - SETTLE) {
				server.execute(() -> placeObserver(server.getPlayerList().getPlayer(uuid), observer(server, uuid), current, front));
			}
			if (local == captures[i]) {
				String name = current.name() + "-" + (front ? "front" : "side") + (index > 1 ? "-" + index : "");
				server.execute(() -> observer(server, uuid).sendSystemMessage(Component.literal(SHOT + name)));
				Hemomancy.LOGGER.info("ONLOOKER_REVIEW host shot {} using={} strand={}", name, mc.player.isUsingItem(),
						HemoCapabilityAccess.getTendonAnchor(mc.player).isSet());
			}
		}
		if (local == SCENE_TICKS - 1) {
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			KeyMapping.set(mc.options.keyUp.getKey(), false);
			scene++;
			sceneStart = ticks + 1;
		}
	}

	private static List<Scene> scenes() {
		if (scenes == null) {
			String only = System.getProperty(HOST + ".only", "");
			scenes = only.isEmpty() ? all() : all().stream().filter(s -> s.name().startsWith(only)).toList();
		}
		return scenes;
	}

	private static ServerPlayer observer(MinecraftServer server, java.util.UUID host) {
		return server.getPlayerList().getPlayers().stream().filter(p -> !p.getUUID().equals(host)).findFirst().orElseThrow();
	}

	/** A long floor with every scene's stage, the caster's blood, Degree, skills and memories. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		ServerLevel level = player.serverLevel();
		row = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty easy", "time set 6000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-32, -1, -6), row.offset(32, 10, 18)))
			level.setBlock(pos, pos.getY() < row.getY() ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
		level.getEntitiesOfClass(Entity.class, new AABB(row).inflate(40.0D), entity -> !(entity instanceof ServerPlayer))
				.forEach(Entity::discard);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(6000.0D);
		volume.setBloodVolume(6000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(5);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		for (EnumBloodTemper school : EnumBloodTemper.values()) temperament.setTemperAlignment(school, 20.0F);
		BloodTemperamentEvents.syncTemperament(player, temperament);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		List<String> memories = List.of("blood_shot", "lignum_mortis", "pyretic_forge", "blood_lash", "gloam_laceration",
				"hematic_flare", "synaptic_jolt", "cruor_javelin", "blood_needle_lance", "venous_cling", "tendon_line",
				"blood_eclipse");
		for (String name : memories) {
			known.getKnownManips().put(ManipulationInit.getByName(name), new ManipLevel(3, 0));
			com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry.form(name).ifPresent(form ->
					known.getKnownManips().merge(ManipulationInit.getByName(
							com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry.baselineId(name)),
							new ManipLevel(form.requiredLevel(), 0),
							(had, now) -> had.getCurrentLevel() >= now.getCurrentLevel() ? had : now));
		}
		known.setEquippedManipNames(new ArrayList<>(memories));
		KnownManipulationEvents.syncPlayerEvent(player);
		var staff = HemoCapabilityAccess.getLivingStaffProgress(player);
		staff.setLivingStaffBond(true);
		if (staff.getBloodHandled() < 800_000.0D) staff.addBloodHandled(800_000.0D);
		player.setItemSlot(EquipmentSlot.FEET, CleatsFixtures.fitted(ItemInit.hematic_iron_boots.get()));
		for (PlayerStateSync.Entry entry : PlayerStateSync.Entry.values()) entry.send(player);
		Hemomancy.LOGGER.info("ONLOOKER_REVIEW staged at {}", row);
	}

	/** Selects, arms, places and aims the caster for one scene, restages it, and makes the observer a spectator. */
	private static void pose(ServerPlayer player, ServerPlayer observer, Scene scene) {
		ServerLevel level = player.serverLevel();
		ClingService.release(player, null);
		observer.setGameMode(GameType.SPECTATOR);
		skill(player, SkillPointInit.skill_sanguine_reach, 0);
		skill(player, SkillPointInit.skill_second_strand, 0);
		if (scene.scene() != null) scene.scene().accept(level, player);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.setSelectedManip(ManipulationInit.getByName(scene.manipulation()));
		known.setActiveVerb(scene.verb());
		KnownManipulationEvents.syncPlayerEvent(player);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setBloodVolume(5500.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		TemperFixtures.carry(player);
		player.setHealth(player.getMaxHealth());
		player.getInventory().selected = 0;
		ItemStack held = new ItemStack(scene.held().get());
		if (held.is(ItemInit.living_staff.get())) LivingStaffGrowth.stamp(held, 5);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		Vec3 eye = at(scene.stand()).add(0.0D, 1.62D, 0.0D);
		Vec3 target = at(scene.aim());
		double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
		player.teleportTo(level, eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0F;
		if (scene.name().startsWith("cling")) {
			boolean ceiling = scene.name().contains("ceiling");
			Vec3 face = at(ceiling ? CEILING_FACE : WALL_FACE);
			BlockPos block = ceiling ? BlockPos.containing(face.x, face.y + 0.5D, face.z) : BlockPos.containing(face.x + 0.5D, face.y, face.z);
			ClingService.root(player, block, ceiling ? Direction.DOWN : Direction.WEST);
		}
		placeObserver(player, observer, scene, false);
	}

	/**
	 * The observer's camera spot: six blocks off the caster's right, level with the middle of what they do, or in front
	 * of them and a little to the side; looking at the middle point.
	 */
	private static void placeObserver(ServerPlayer caster, ServerPlayer observer, Scene scene, boolean front) {
		Vec3 eye = caster.getEyePosition();
		Vec3 target = at(scene.aim());
		Vec3 forward = target.subtract(eye).multiply(1, 0, 1);
		forward = forward.lengthSqr() < 1.0E-4D ? new Vec3(0, 0, 1) : forward.normalize();
		Vec3 right = new Vec3(-forward.z, 0, forward.x);
		Vec3 middle = eye.lerp(target, scene.name().startsWith("cling") || scene.name().startsWith("dark") ? 0.2D : 0.45D)
				.add(0, -0.4D, 0);
		Vec3 camera = front
				? eye.add(forward.scale(scene.name().startsWith("cling") ? -3.5D : 3.0D)).add(right.scale(2.5D)).add(0, 0.6D, 0)
				: middle.add(right.scale(6.0D)).add(forward.scale(-0.5D)).add(0, 1.0D, 0);
		if (scene.name().startsWith("dark")) camera = at(FIELD).add(0, 1.2D, -11.0D).add(right.scale(front ? 4.0D : -1.0D));
		if (scene.name().startsWith("cling")) {
			// A clinger faces the wall or the ceiling: the views are from either side of them, a little back, and from
			// under a ceiling rather than in it.
			camera = eye.add(right.scale(front ? -4.5D : 4.5D)).add(forward.scale(-3.0D));
			camera = new Vec3(camera.x, scene.name().contains("ceiling") ? row.getY() + 1.6D : eye.y + 0.3D, camera.z);
			middle = eye.add(0, -0.5D, 0);
		}
		boolean lookAtCaster = front && !scene.name().startsWith("dark") && !scene.name().startsWith("cling");
		Vec3 look = (lookAtCaster ? eye.add(0, -0.4D, 0) : middle).subtract(camera);
		float yaw = (float) Math.toDegrees(Math.atan2(-look.x, look.z));
		float pitch = (float) -Math.toDegrees(Math.atan2(look.y, Math.hypot(look.x, look.z)));
		observer.teleportTo(caster.serverLevel(), camera.x, camera.y - observer.getEyeHeight(), camera.z, yaw, pitch);
	}

	private static void skill(ServerPlayer player, com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint point,
			int level) {
		HemoCapabilityAccess.getSkillProgress(player).setSkill(point, level > 0 ? EnumSkillStates.UNLOCKED : EnumSkillStates.LOCKED, level);
	}

	private static void syncSkills(ServerPlayer player) {
		for (PlayerStateSync.Entry entry : PlayerStateSync.Entry.values()) entry.send(player);
	}

	// ── Scenes ──

	private static void stones(ServerLevel level, ServerPlayer player) {
		level.setBlock(BlockPos.containing(at(NEAR_STONE)), Blocks.STONE.defaultBlockState(), 3);
		level.setBlock(BlockPos.containing(at(FAR_STONE)), Blocks.STONE.defaultBlockState(), 3);
	}

	/** The far stone through a five-ring Staff and Sanguine Reach at its top level. */
	private static void staffReach(ServerLevel level, ServerPlayer player) {
		stones(level, player);
		level.setBlock(BlockPos.containing(at(NEAR_STONE)), Blocks.AIR.defaultBlockState(), 3);
		skill(player, SkillPointInit.skill_sanguine_reach, 3);
		syncSkills(player);
	}

	private static void logs(ServerLevel level, ServerPlayer player) {
		BlockPos base = BlockPos.containing(at(LOG)).atY(row.getY());
		for (int y = 0; y < 4; y++) level.setBlock(base.above(y), Blocks.OAK_LOG.defaultBlockState(), 3);
	}

	private static void brazier(ServerLevel level, ServerPlayer player) {
		BlockPos at = BlockPos.containing(at(BRAZIER));
		level.setBlock(at, BlockInit.iron_brazier.get().defaultBlockState().setValue(BrazierBlock.RITUAL_PHASE, 1), 3);
		if (level.getBlockEntity(at) instanceof IronBrazierBlockEntity brazier) {
			brazier.setPooledBlood(BrazierBlock.BLOOD_TO_LIGHT);
			brazier.insertOffering(null, new ItemStack(Items.OAK_LOG));
		}
	}

	/** A still husk to strike, and the strand skills the brief asks for: Taut Line 2 and Deep Brand 2. */
	private static void strandSkills(ServerLevel level, ServerPlayer player) {
		level.getEntitiesOfClass(Mob.class, new AABB(row).inflate(40.0D)).forEach(Entity::discard);
		still(level, EntityType.HUSK, HUSK, 180.0F);
		skill(player, SkillPointInit.skill_taut_line, 2);
		skill(player, SkillPointInit.skill_deep_brand, 2);
		syncSkills(player);
	}

	private static void wall(ServerLevel level, ServerPlayer player) {
		for (int x = 20; x <= 21; x++)
			for (int y = 0; y <= 10; y++)
				for (int z = 0; z <= 9; z++) level.setBlock(row.offset(x, y, z), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 3);
	}

	private static void ceiling(ServerLevel level, ServerPlayer player) {
		for (int x = 23; x <= 30; x++)
			for (int z = -1; z <= 8; z++)
				for (int y = 5; y <= 6; y++) level.setBlock(row.offset(x, y, z), Blocks.DEEPSLATE.defaultBlockState(), 3);
	}

	private static void tendonWall(ServerLevel level, ServerPlayer player) {
		BlockPos face = BlockPos.containing(at(TENDON_FACE));
		for (BlockPos pos : BlockPos.betweenClosed(face.offset(-2, -face.getY() + row.getY(), 0), face.offset(2, 3, 0)))
			level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 3);
		HemoCapabilityAccess.getTendonAnchor(player).clear();
		TendonLineSculpting.sync(player);
	}

	/** A lit room of lanterns and a field of dark over it, fed as Blood Eclipse's snuff leaves it. */
	private static void field(ServerLevel level, ServerPlayer player) {
		BlockPos center = BlockPos.containing(at(FIELD)).atY(row.getY());
		for (int x = -5; x <= 5; x++) for (int y = 0; y <= 3; y++) level.setBlock(center.offset(x, y, 5), Blocks.STONE_BRICKS.defaultBlockState(), 2);
		for (int z = -4; z <= 4; z += 2) {
			level.setBlock(center.offset(-5, 0, z), Blocks.LANTERN.defaultBlockState(), 2);
			level.setBlock(center.offset(5, 0, z), Blocks.LANTERN.defaultBlockState(), 2);
		}
		DarkFields.logout(player);
		DarkFields.feed(player, Vec3.atCenterOf(center), 900);
	}

	private static void still(ServerLevel level, EntityType<? extends Mob> type, Vec3 offset, float yaw) {
		Mob mob = type.create(level);
		if (mob == null) return;
		Vec3 at = at(offset);
		mob.moveTo(at.x, row.getY(), at.z, yaw, 0.0F);
		mob.setNoAi(true);
		mob.setPersistenceRequired();
		mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(400.0D);
		mob.setHealth(400.0F);
		level.addFreshEntity(mob);
	}

	private static Vec3 at(Vec3 offset) {
		return new Vec3(row.getX() + offset.x, row.getY() + offset.y, row.getZ() + offset.z);
	}

	// ── The observer ──

	@SubscribeEvent
	public static void chat(ClientChatReceivedEvent event) {
		if (!Boolean.getBoolean(OBSERVER)) return;
		String text = event.getMessage().getString();
		if (text.startsWith(SHOT)) {
			pending = text.substring(SHOT.length()).trim();
			event.setCanceled(true);
		} else if (text.equals(DONE)) {
			pending = DONE;
			event.setCanceled(true);
		}
	}

	private static void observerTick() {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("onlooker-observer")) return;
		mc.options.pauseOnLostFocus = false;
		if (mc.level == null || mc.player == null) {
			if (connected) {
				Hemomancy.LOGGER.info("ONLOOKER_REVIEW captured {} (host left)", frames);
				mc.stop();
			}
			return;
		}
		if (!connected) Hemomancy.LOGGER.info("ONLOOKER_REVIEW observer connected");
		connected = true;
		mc.options.hideGui = true;
		mc.options.setCameraType(CameraType.FIRST_PERSON);
		if (mc.screen != null) mc.setScreen(null);
		if (pending == null) return;
		String name = pending;
		pending = null;
		if (name.equals(DONE)) {
			Hemomancy.LOGGER.info("ONLOOKER_REVIEW captured {}", frames);
			mc.stop();
			return;
		}
		Screenshot.grab(mc.gameDirectory, String.format(java.util.Locale.ROOT, "onlooker-%02d-%s.png", frames, name),
				mc.getMainRenderTarget(), message -> {});
		frames++;
		Hemomancy.LOGGER.info("ONLOOKER_REVIEW observer shot {}", name);
	}
}

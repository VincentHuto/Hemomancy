package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingService;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.animus.LivingLandSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * With hemomancy.livingLandReview, stages core-loop Track 9 in a disposable world, captures it into
 * build/living-land-client-review/screenshots and stops. A real survival player: Living Land's floor teeth, wall
 * lances and ceiling fall at a husk, in first person and from the side; the patch rising into a step with nothing near;
 * Venous Cling rooted to a wall and hanging under a ceiling, from the side, with the pose and the rooting veins; and
 * the two memories and the Hematic Cleats in item frames. Run with livingLandReviewClient and drop
 * {"op":"create","name":"LivingLand","seed":"1"} into operator/inbox after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LivingLandClientReview {
	private static final String PROPERTY = "hemomancy.livingLandReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 90;
	private static final int PRESS_TICK = 12;
	private static int ticks;
	private static int shot;
	private static int captured;
	private static BlockPos row;
	/** The side camera's entity id for a side shot, or -1. Written on the server thread, read on the client. */
	private static volatile int cameraId = -1;

	/**
	 * One staged moment: where the player stands and looks (offsets from the row), what they hold, which memory is
	 * selected, whether Projection is held from {@link #PRESS_TICK} to {@code release}, whether a side camera looks back
	 * at them (and from which side), the local ticks to capture at, and the scene (null keeps the last one).
	 */
	private record Shot(String name, Vec3 stand, Vec3 aim, Supplier<Item> held, String selected, int release,
			boolean side, double sideSign, int[] captures, BiConsumer<ServerLevel, ServerPlayer> scene) {
	}

	private static final Vec3 FLOOR_PATCH = new Vec3(0.5, 0.0, 4.5);
	private static final Vec3 WALL_FACE = new Vec3(6.0, 1.5, 4.5);
	private static final Vec3 CEILING_FACE = new Vec3(-5.0, 5.0, 3.5);

	private static final List<Shot> SHOTS = List.of(
			new Shot("floor-teeth", new Vec3(0.5, 0, 1.2), FLOOR_PATCH, ItemInit.blood_projection::get, "living_land", 80,
					false, 1, new int[]{20, 35, 37, 46}, LivingLandClientReview::floorScene),
			new Shot("floor-teeth-side", new Vec3(0.5, 0, 1.2), FLOOR_PATCH, ItemInit.blood_projection::get, "living_land", 80,
					true, 1, new int[]{35, 37, 47}, LivingLandClientReview::floorScene),
			new Shot("wall-lances", new Vec3(1.5, 0, 4.5), WALL_FACE, ItemInit.blood_projection::get, "living_land", 80,
					false, 1, new int[]{35, 37, 47}, LivingLandClientReview::wallScene),
			new Shot("wall-lances-side", new Vec3(1.5, 0, 4.5), WALL_FACE, ItemInit.blood_projection::get, "living_land", 80,
					true, -1, new int[]{35, 37, 47}, LivingLandClientReview::wallScene),
			new Shot("ceiling-crush", new Vec3(-5.0, 0, 0.8), CEILING_FACE, ItemInit.blood_projection::get, "living_land", 80,
					false, 1, new int[]{35, 37, 47}, LivingLandClientReview::ceilingScene),
			new Shot("ceiling-crush-side", new Vec3(-5.0, 0, 0.8), CEILING_FACE, ItemInit.blood_projection::get, "living_land", 80,
					true, 1, new int[]{35, 37, 47}, LivingLandClientReview::ceilingScene),
			new Shot("raise-step", new Vec3(0.5, 0, 1.2), FLOOR_PATCH, ItemInit.blood_projection::get, "living_land", 80,
					false, 1, new int[]{40, 60, 78}, LivingLandClientReview::floorSceneEmpty),
			new Shot("raise-settled", new Vec3(0.5, 0, 1.2), FLOOR_PATCH, ItemInit.blood_projection::get, "living_land", 0,
					false, 1, new int[]{10}, null),
			new Shot("cling-wall-side", new Vec3(6.0 - 0.31, 1.4, 6.5), new Vec3(6.0, 2.9, 6.5), () -> net.minecraft.world.item.Items.AIR,
					"venous_cling", 0, true, 1, new int[]{30, 60}, LivingLandClientReview::clingWall),
			new Shot("cling-wall-climb-front", new Vec3(6.0 - 0.31, 1.4, 6.5), new Vec3(6.0, 2.9, 6.5), () -> net.minecraft.world.item.Items.AIR,
					"venous_cling", 0, true, 1, new int[]{14, 50}, LivingLandClientReview::clingWall),
			new Shot("cling-wall-cleats", new Vec3(6.0 - 0.31, 1.4, 6.5), new Vec3(6.0, 2.9, 6.5), () -> net.minecraft.world.item.Items.AIR,
					"venous_cling", 0, true, -1, new int[]{30}, LivingLandClientReview::clingWallWithCleats),
			new Shot("cling-ceiling-side", new Vec3(-5.0, 5.0 - 1.81, 5.5), new Vec3(-5.0, 5.0, 7.5), () -> net.minecraft.world.item.Items.AIR,
					"venous_cling", 0, true, 1, new int[]{30, 60}, LivingLandClientReview::clingCeiling),
			new Shot("frames", new Vec3(0.5, 0, 9.0), new Vec3(0.5, 1.5, 11.9), () -> net.minecraft.world.item.Items.AIR, null, 0,
					false, 1, new int[]{20}, LivingLandClientReview::frames));

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("living-land-client-review")) return;
		if (mc.level == null && mc.screen instanceof AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
		}
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		MinecraftServer server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		if (++ticks == SETUP_TICK) server.execute(() -> stage(server.getPlayerList().getPlayer(uuid), server));
		if (ticks < FIRST_SHOT_TICK || row == null) return;
		int local = (ticks - FIRST_SHOT_TICK) % SHOT_TICKS;
		if (local == 0) {
			if (shot == SHOTS.size()) {
				Hemomancy.LOGGER.info("LIVING_LAND_REVIEW captured {}", captured);
				mc.stop();
				return;
			}
			Shot current = SHOTS.get(shot);
			mc.options.setCameraType(CameraType.FIRST_PERSON);
			mc.options.hideGui = false;
			server.execute(() -> pose(server.getPlayerList().getPlayer(uuid), current));
		}
		if (shot >= SHOTS.size()) return;
		Shot current = SHOTS.get(shot);
		if (local == 6 && current.side() && cameraId >= 0 && mc.level.getEntity(cameraId) != null) {
			mc.setCameraEntity(mc.level.getEntity(cameraId));
			mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
		}
		if (local == 8) mc.getToasts().clear();
		KeyMapping use = mc.options.keyUse;
		if (local == PRESS_TICK && current.release() > 0) {
			KeyMapping.click(use.getKey());
			KeyMapping.set(use.getKey(), true);
		}
		if (local == current.release() && current.release() > 0) KeyMapping.set(use.getKey(), false);
		// A climb holds forward while rooted: the owner's client lays it onto the wall.
		if (current.name().contains("climb")) {
			if (local == PRESS_TICK + 4) KeyMapping.set(mc.options.keyUp.getKey(), true);
			if (local == 48) KeyMapping.set(mc.options.keyUp.getKey(), false);
		}
		for (int at : current.captures()) {
			if (local != at) continue;
			Screenshot.grab(mc.gameDirectory, "livingland-" + shot + "-" + current.name() + "-" + at + ".png",
					mc.getMainRenderTarget(), message -> {});
			captured++;
			ServerPlayer serverPlayer = server.getPlayerList().getPlayer(uuid);
			Hemomancy.LOGGER.info("LIVING_LAND_REVIEW shot {} {} at {} using={} strikes={} raised={} cling={}", shot,
					current.name(), at, mc.player.isUsingItem(),
					serverPlayer == null ? -1 : LivingLandSculpting.strikes(serverPlayer).size(),
					serverPlayer == null ? -1 : LivingLandSculpting.raised(serverPlayer),
					HemoCapabilityAccess.getCling(mc.player).surface());
		}
		if (local == SHOT_TICKS - 1) {
			mc.setCameraEntity(mc.player);
			KeyMapping.set(use.getKey(), false);
			KeyMapping.set(mc.options.keyUp.getKey(), false);
			shot++;
		}
	}

	/** A clear room, the player's blood, Living Land and Venous Cling known and equipped. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		ServerLevel level = player.serverLevel();
		row = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty easy", "time set 6000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-13, -4, -3), row.offset(14, 9, 14)))
			level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		level.getEntitiesOfClass(Entity.class, new AABB(row).inflate(20.0D), entity -> !(entity instanceof ServerPlayer))
				.forEach(Entity::discard);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(6000.0D);
		volume.setBloodVolume(6000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		temperament.setTemperAlignment(EnumBloodTemper.ANIMUS, 30.0F);
		BloodTemperamentEvents.syncTemperament(player, temperament);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		List<String> equipped = List.of("living_land", "venous_cling", "conjure_staff");
		for (String name : equipped) known.getKnownManips().put(ManipulationInit.getByName(name), new ManipLevel(3, 0));
		known.setEquippedManipNames(new ArrayList<>(equipped));
		KnownManipulationEvents.syncPlayerEvent(player);
		Hemomancy.LOGGER.info("LIVING_LAND_REVIEW staged at {}", row);
	}

	/** Selects, arms, places and aims the player for one shot, restages its scene and sets up its side camera. */
	private static void pose(ServerPlayer player, Shot shot) {
		ServerLevel level = player.serverLevel();
		ClingService.release(player, null);
		if (shot.scene() != null) shot.scene().accept(level, player);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.setSelectedManip(shot.selected() == null ? BloodManipulation.BLANK : ManipulationInit.getByName(shot.selected()));
		known.setActiveVerb(Verb.PROJECT);
		KnownManipulationEvents.syncPlayerEvent(player);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setBloodVolume(5500.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		TemperFixtures.carry(player);
		player.setHealth(player.getMaxHealth());
		player.getInventory().selected = 0;
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(shot.held().get()));
		Vec3 eye = at(shot.stand()).add(0.0D, 1.62D, 0.0D);
		Vec3 target = at(shot.aim());
		double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
		player.teleportTo(level, eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0F;
		cameraId = -1;
		level.getEntitiesOfClass(ArmorStand.class, new AABB(row).inflate(24.0D)).forEach(Entity::discard);
		if (shot.name().startsWith("cling")) rootFor(player, shot);
		// Under a ceiling the camera looks up from below it, so the ceiling does not stand in the way.
		if (shot.side()) sideCamera(level, eye, target, shot.sideSign(), shot.name().contains("ceiling") ? -1.6D : 1.2D,
				shot.name().contains("front"));
	}

	/** Roots the player to the face the shot looks at, as a held Venous Cling would. */
	private static void rootFor(ServerPlayer player, Shot shot) {
		Vec3 aim = at(shot.aim());
		boolean ceiling = shot.name().contains("ceiling");
		BlockPos block = ceiling ? BlockPos.containing(aim.x, aim.y + 0.5D, aim.z) : BlockPos.containing(aim.x + 0.5D, aim.y, aim.z);
		ClingService.root(player, block, ceiling ? Direction.DOWN : Direction.WEST);
	}

	/** A camera off to one side, looking back at the player and what lies ahead of them. */
	private static void sideCamera(ServerLevel level, Vec3 eye, Vec3 target, double sign, double lift, boolean front) {
		Vec3 forward = target.subtract(eye).multiply(1, 0, 1);
		forward = forward.lengthSqr() < 1.0E-4D ? new Vec3(0, 0, 1) : forward.normalize();
		Vec3 right = new Vec3(-forward.z, 0, forward.x).scale(sign);
		Vec3 focus = eye.add(target.subtract(eye).scale(0.5D)).add(0, -0.6D, 0);
		// A front view stands back from what the player faces (a wall they cling to), a little to one side.
		Vec3 camera = front ? focus.add(right.scale(2.0D)).add(forward.scale(-4.5D)).add(0, lift + 0.6D, 0)
				: focus.add(right.scale(5.0D)).add(forward.scale(-1.0D)).add(0, lift, 0);
		double cx = focus.x - camera.x, cy = focus.y - camera.y, cz = focus.z - camera.z;
		float camYaw = (float) Math.toDegrees(Math.atan2(-cx, cz));
		float camPitch = (float) -Math.toDegrees(Math.atan2(cy, Math.hypot(cx, cz)));
		// The third-person camera sits four blocks behind its entity: stand the entity four blocks on.
		Vec3 look = new Vec3(cx, cy, cz).normalize();
		Vec3 standAt = camera.add(look.scale(4.0D));
		ArmorStand stand = new ArmorStand(level, standAt.x, standAt.y, standAt.z);
		stand.setInvisible(true);
		stand.setNoGravity(true);
		stand.moveTo(standAt.x, standAt.y - stand.getEyeHeight(), standAt.z, camYaw, camPitch);
		stand.setYHeadRot(camYaw);
		stand.yHeadRotO = camYaw;
		stand.setXRot(camPitch);
		stand.xRotO = camPitch;
		level.addFreshEntity(stand);
		cameraId = stand.getId();
	}

	private static void clearHusks(ServerLevel level) {
		level.getEntitiesOfClass(Husk.class, new AABB(row).inflate(20.0D)).forEach(Entity::discard);
	}

	private static void husk(ServerLevel level, Vec3 offset) {
		Husk husk = EntityType.HUSK.create(level);
		Vec3 feet = at(offset);
		husk.moveTo(feet.x, feet.y, feet.z, 180.0F, 0.0F);
		husk.setNoAi(true);
		husk.getAttribute(Attributes.MAX_HEALTH).setBaseValue(400.0D);
		husk.setHealth(400.0F);
		husk.setPersistenceRequired();
		level.addFreshEntity(husk);
	}

	/** A patch of grass over dirt and stone in the floor, three deep, and a husk across it. */
	private static void floorScene(ServerLevel level, ServerPlayer player) {
		floorSceneEmpty(level, player);
		husk(level, new Vec3(0.5, 0.0, 7.5));
	}

	private static void floorSceneEmpty(ServerLevel level, ServerPlayer player) {
		clearHusks(level);
		for (int x = -4; x <= 4; x++) {
			for (int z = 2; z <= 10; z++) {
				for (int y = 0; y <= 4; y++) level.setBlock(at(x, y, z), Blocks.AIR.defaultBlockState(), 3);
				level.setBlock(at(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
				level.setBlock(at(x, -2, z), Blocks.DIRT.defaultBlockState(), 3);
				level.setBlock(at(x, -3, z), Blocks.STONE.defaultBlockState(), 3);
				level.setBlock(at(x, -4, z), Blocks.STONE.defaultBlockState(), 3);
			}
		}
	}

	/** A mossy cobblestone wall three thick to the east, and a husk beside its face. */
	private static void wallScene(ServerLevel level, ServerPlayer player) {
		clearHusks(level);
		buildWall(level);
		husk(level, new Vec3(4.0, 0.0, 6.5));
	}

	private static void buildWall(ServerLevel level) {
		for (int x = 6; x <= 8; x++) {
			for (int y = 0; y <= 6; y++) {
				for (int z = 1; z <= 9; z++) {
					BlockState state = (x + y + z) % 5 == 0 ? Blocks.COBBLESTONE.defaultBlockState()
							: Blocks.MOSSY_COBBLESTONE.defaultBlockState();
					level.setBlock(at(x, y, z), state, 3);
				}
			}
		}
	}

	/** A deepslate ceiling low over the west of the room, and a husk under it. */
	private static void ceilingScene(ServerLevel level, ServerPlayer player) {
		clearHusks(level);
		buildCeiling(level);
		husk(level, new Vec3(-5.0, 0.0, 5.5));
	}

	private static void buildCeiling(ServerLevel level) {
		for (int x = -9; x <= -1; x++) {
			for (int z = -1; z <= 10; z++) {
				for (int y = 5; y <= 7; y++) level.setBlock(at(x, y, z), Blocks.DEEPSLATE.defaultBlockState(), 3);
			}
		}
	}

	private static void clingWall(ServerLevel level, ServerPlayer player) {
		clearHusks(level);
		buildWall(level);
		player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
	}

	private static void clingWallWithCleats(ServerLevel level, ServerPlayer player) {
		clingWall(level, player);
		player.setItemSlot(EquipmentSlot.FEET, CleatsFixtures.fitted(ItemInit.hematic_iron_boots.get()));
	}

	private static void clingCeiling(ServerLevel level, ServerPlayer player) {
		clearHusks(level);
		buildCeiling(level);
		player.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
	}

	/** The two memories and the Hematic Cleats in item frames on a wall. */
	private static void frames(ServerLevel level, ServerPlayer player) {
		clearHusks(level);
		BlockPos wall = BlockPos.containing(at(0.5, 0.0, 12.0)).atY(row.getY());
		level.getEntitiesOfClass(ItemFrame.class, new AABB(wall).inflate(6.0D)).forEach(Entity::discard);
		List<Item> items = List.of(ItemInit.memory_living_land.get(), ItemInit.memory_venous_cling.get(), ItemInit.hematic_cleats.get());
		for (int i = 0; i < items.size(); i++) {
			BlockPos block = wall.offset(i - 1, 1, 0);
			for (int y = 0; y <= 2; y++) level.setBlock(block.atY(row.getY() + y), Blocks.STONE_BRICKS.defaultBlockState(), 3);
			ItemFrame frame = new ItemFrame(level, block.north(), Direction.NORTH);
			frame.setItem(new ItemStack(items.get(i)), false);
			level.addFreshEntity(frame);
		}
	}

	private static BlockPos at(int x, int y, int z) {
		return row.offset(x, y, z);
	}

	private static Vec3 at(Vec3 offset) {
		return new Vec3(row.getX() + offset.x, row.getY() + offset.y, row.getZ() + offset.z);
	}

	private static Vec3 at(double x, double y, double z) {
		return at(new Vec3(x, y, z));
	}
}

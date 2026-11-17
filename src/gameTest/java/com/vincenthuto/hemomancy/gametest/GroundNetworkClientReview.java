package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.BloodGutterBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.VenousBasinBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodStainBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.LuxTraceBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.tile.harbinger.ground.CatalystFeederBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.ground.VeinNodeBlockEntity;
import com.vincenthuto.hemomancy.common.world.ground.DarkFields;
import com.vincenthuto.hemomancy.common.world.ground.GroundFunction;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunRules;
import com.vincenthuto.hemomancy.common.world.ground.VeinLedger;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.MultifaceBlock;
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
 * With hemomancy.groundNetworkReview, stages core-loop Track 6's ground network in a closed dark room of a disposable
 * world, captures it into build/ground-network-client-review/screenshots and stops. A real survival player: stains at
 * their three ages and a fresh one laid by plain Projection; a dormant gouge route; Vascular Dowsing cutting a gouge;
 * the route lit with glowstone from the off hand; a row of runs lit with each function; Hematic Beacon spreading Lux
 * traces; a living vein between two tanks; a worked basin pouring down its gutters; a catalyst feeder by a lit run;
 * Blood Eclipse putting candles out; and Sanguine Magnetism drawing dropped items. Round two P8 adds: a hot run burning
 * a furnace, smoker and blast furnace beside it (an empty furnace stays cold); Blood Eclipse's field of dark seen from
 * outside in a lit room and from inside (its fog); two Earthen Veins joined by a living vein, with the travel display
 * open showing the carried price; and water running over a living vein run, one vein lying under a source. Run with
 * groundNetworkReviewClient and drop {"op":"create","name":"GroundReview","seed":"1"} into operator/inbox after
 * startup ({"op":"open","name":"GroundReview"} once the save exists). Twenty-two captures. Excluded from release
 * jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class GroundNetworkClientReview {
	private static final String PROPERTY = "hemomancy.groundNetworkReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 80;
	private static final int PRESS_TICK = 12;
	private static int ticks;
	private static int shot;
	private static int captured;
	private static BlockPos row;

	/**
	 * One staged moment. Offsets are from the row (the player's feet at setup; the floor is one below). {@code use}
	 * holds the use key from {@link #PRESS_TICK} to {@code release}; {@code scene} restages the room (server side), or
	 * is null to keep the last shot's; {@code seeing} gives night vision for shots of unlit things.
	 */
	private record Shot(String name, String manipulation, Verb verb, Supplier<Item> held, Supplier<ItemStack> offHand,
			Vec3 stand, Vec3 aim, boolean use, int release, boolean seeing, int[] captures,
			BiConsumer<ServerLevel, ServerPlayer> scene) {
	}

	private static final Vec3 STAND = new Vec3(0.5, 0, 1.5);

	private static final List<Shot> SHOTS = List.of(
			new Shot("stains", null, Verb.PROJECT, ItemInit.blood_projection::get, () -> ItemStack.EMPTY,
					STAND, new Vec3(0.5, 0, 5.5), true, 50, true, new int[]{40}, GroundNetworkClientReview::stains),
			new Shot("gouge-dormant", null, Verb.PROJECT, () -> Items.AIR, () -> ItemStack.EMPTY,
					STAND, new Vec3(0.5, 0.5, 7.5), false, 0, true, new int[]{20}, GroundNetworkClientReview::route),
			new Shot("gouge-cut", "vascular_dowsing", Verb.PROJECT, ItemInit.blood_projection::get, () -> ItemStack.EMPTY,
					STAND, new Vec3(0.5, 0, 4.5), true, 60, true, new int[]{34}, null),
			new Shot("gouge-lit", null, Verb.PROJECT, ItemInit.blood_projection::get, () -> new ItemStack(Items.GLOWSTONE_DUST, 4),
					STAND, new Vec3(0.5, 0, 6.5), true, 22, false, new int[]{60}, null),
			new Shot("functions", null, Verb.PROJECT, () -> Items.AIR, () -> ItemStack.EMPTY,
					new Vec3(0.5, 3, -1.5), new Vec3(0.5, 0, 5.0), false, 0, false, new int[]{40},
					GroundNetworkClientReview::functions),
			new Shot("lux-trace", "hematic_beacon", Verb.PROJECT, ItemInit.blood_projection::get, () -> ItemStack.EMPTY,
					STAND, new Vec3(0.5, 0, 6.5), true, 72, false, new int[]{40, 74}, GroundNetworkClientReview::clearRoom),
			new Shot("vein", null, Verb.PROJECT, () -> Items.AIR, () -> ItemStack.EMPTY,
					STAND, new Vec3(0.5, 0.3, 5.5), false, 0, true, new int[]{50}, GroundNetworkClientReview::vein),
			new Shot("basin", null, Verb.PROJECT, () -> Items.AIR, () -> ItemStack.EMPTY,
					STAND, new Vec3(0.5, 0.3, 6.0), false, 0, true, new int[]{50}, GroundNetworkClientReview::basin),
			new Shot("feeder", null, Verb.PROJECT, () -> Items.AIR, () -> ItemStack.EMPTY,
					STAND, new Vec3(0.5, 0.3, 5.5), false, 0, false, new int[]{30}, GroundNetworkClientReview::feeder),
			new Shot("eclipse", "blood_eclipse", Verb.ABSORB, ItemInit.blood_absorption::get, () -> ItemStack.EMPTY,
					STAND, new Vec3(0.5, 0.2, 5.5), true, 72, false, new int[]{10, 17, 25, 60}, GroundNetworkClientReview::candles),
			new Shot("item-draw", "sanguine_magnetism", Verb.ABSORB, ItemInit.blood_absorption::get, () -> ItemStack.EMPTY,
					STAND, new Vec3(-0.5, 0.15, 6.7), true, 72, true, new int[]{18, 36}, GroundNetworkClientReview::items),
			new Shot("furnaces", null, Verb.PROJECT, () -> Items.AIR, () -> ItemStack.EMPTY,
					STAND, new Vec3(0.5, 0.4, 6.0), false, 0, false, new int[]{50}, GroundNetworkClientReview::furnaces),
			new Shot("field-outside", null, Verb.ABSORB, () -> Items.AIR, () -> ItemStack.EMPTY,
					STAND, new Vec3(0.5, 0.6, 9.5), false, 0, false, new int[]{40}, GroundNetworkClientReview::darkField),
			new Shot("field-inside", null, Verb.ABSORB, () -> Items.AIR, () -> ItemStack.EMPTY,
					new Vec3(0.5, 0, 10.5), new Vec3(0.5, 1.2, 1.5), false, 0, false, new int[]{40}, null),
			new Shot("vein-travel", null, Verb.PROJECT, () -> Items.AIR, () -> ItemStack.EMPTY,
					new Vec3(-3.5, 1, 4.0), new Vec3(0.5, 0.2, 6.5), false, 0, true, new int[]{20, 50},
					GroundNetworkClientReview::veinTravel),
			new Shot("vein-water", null, Verb.PROJECT, () -> Items.AIR, () -> ItemStack.EMPTY,
					new Vec3(0.5, 1, 9.0), new Vec3(0.5, 0, 4.5), false, 0, false, new int[]{60}, GroundNetworkClientReview::water));

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("ground-network-client-review")) return;
		if (mc.level == null && mc.screen instanceof AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
		}
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
		mc.getToasts().clear();
		MinecraftServer server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		if (++ticks == SETUP_TICK) server.execute(() -> stage(server.getPlayerList().getPlayer(uuid), server));
		if (ticks < FIRST_SHOT_TICK || row == null) return;
		int local = (ticks - FIRST_SHOT_TICK) % SHOT_TICKS;
		if (local == 0) {
			if (shot == SHOTS.size()) {
				Hemomancy.LOGGER.info("GROUND_NETWORK_REVIEW captured {}", captured);
				mc.stop();
				return;
			}
			Shot current = SHOTS.get(shot);
			if (mc.screen != null) mc.setScreen(null);
			mc.options.setCameraType(CameraType.FIRST_PERSON);
			mc.options.hideGui = false;
			server.execute(() -> pose(server.getPlayerList().getPlayer(uuid), current));
		}
		if (shot >= SHOTS.size()) return;
		Shot current = SHOTS.get(shot);
		if (local == PRESS_TICK && current.name().equals("vein-travel")) lookAtFirstMarker(mc);
		if (local == PRESS_TICK && current.use()) {
			KeyMapping.click(mc.options.keyUse.getKey());
			KeyMapping.set(mc.options.keyUse.getKey(), true);
		}
		if (local == current.release() && current.use()) KeyMapping.set(mc.options.keyUse.getKey(), false);
		for (int at : current.captures()) {
			if (local != at) continue;
			Screenshot.grab(mc.gameDirectory, "ground-" + shot + "-" + current.name() + "-" + at + ".png",
					mc.getMainRenderTarget(), message -> {});
			if (current.name().equals("vein-water")) {
				Hemomancy.LOGGER.info("GROUND_NETWORK_REVIEW water cells: shore {} under {} light {}", mc.level.getBlockState(cell(0, 0, 6)),
						mc.level.getBlockState(cell(0, 0, 4)), mc.level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, cell(0, 0, 4)));
			}
			captured++;
			Hemomancy.LOGGER.info("GROUND_NETWORK_REVIEW shot {} {} at {} using={}", shot, current.name(), at,
					mc.player.isUsingItem());
		}
		if (local == SHOT_TICKS - 1) {
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			shot++;
		}
	}

	/** Turns the camera onto the travel display's first destination marker, so its tooltip shows the price. */
	private static void lookAtFirstMarker(Minecraft mc) {
		var selection = com.vincenthuto.hemomancy.client.data.EarthenVeinTravelClientState.selection();
		if (selection == null || mc.player == null) {
			Hemomancy.LOGGER.info("GROUND_NETWORK_REVIEW no travel display open");
			return;
		}
		Vec3 source = Vec3.atBottomCenterOf(selection.source()).add(0.0D, 0.72D, 0.0D);
		var markers = com.vincenthuto.hemomancy.client.render.world.EarthenVeinDestinationLayout.layout(source,
				selection.source(), selection.sourceDimension(), selection.destinations());
		if (markers.isEmpty()) return;
		mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, markers.getFirst().center());
		Hemomancy.LOGGER.info("GROUND_NETWORK_REVIEW travel marker {} costs {}", markers.getFirst().destination().name(),
				markers.getFirst().destination().cost());
	}

	/** A closed room of venous stone, the player's blood, the four ground memories equipped, the Third Degree. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		row = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 18000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false", "gamerule randomTickSpeed 0"})
			server.getCommands().performPrefixedCommand(source, cmd);
		ServerLevel level = player.serverLevel();
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-12, -1, -4), row.offset(14, 6, 14))) {
			boolean shell = pos.getY() == row.getY() - 1 || pos.getY() == row.getY() + 6 || pos.getX() == row.getX() - 12
					|| pos.getX() == row.getX() + 14 || pos.getZ() == row.getZ() - 4 || pos.getZ() == row.getZ() + 14;
			level.setBlock(pos, !shell ? Blocks.AIR.defaultBlockState() : pos.getY() == row.getY() - 1
					? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
		}
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000.0D);
		volume.setBloodVolume(4500.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		temperament.setTemperAlignment(EnumBloodTemper.LUX, 30.0F);
		temperament.setTemperAlignment(EnumBloodTemper.TENEBRIS, 30.0F);
		temperament.setTemperAlignment(EnumBloodTemper.FERRIC, 40.0F);
		BloodTemperamentEvents.syncTemperament(player, temperament);
		TemperFixtures.carry(player);
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(5);
		player.getInventory().clearContent();
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		List<String> four = List.of("vascular_dowsing", "hematic_beacon", "blood_eclipse", "sanguine_magnetism");
		for (String name : four) known.getKnownManips().put(ManipulationInit.getByName(name), new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>(four));
		KnownManipulationEvents.syncPlayerEvent(player);
		Hemomancy.LOGGER.info("GROUND_NETWORK_REVIEW staged at {}", row);
	}

	/** Selects, arms, places and aims the player for one shot, and restages its scene. */
	private static void pose(ServerPlayer player, Shot shot) {
		ServerLevel level = player.serverLevel();
		if (shot.scene() != null) shot.scene().accept(level, player);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.setSelectedManip(shot.manipulation() == null ? BloodManipulation.BLANK : ManipulationInit.getByName(shot.manipulation()));
		known.setActiveVerb(shot.verb());
		KnownManipulationEvents.syncPlayerEvent(player);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setBloodVolume(4500.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		player.removeEffect(MobEffects.NIGHT_VISION);
		if (shot.seeing()) player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
		player.setHealth(player.getMaxHealth());
		player.getInventory().selected = 0;
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(shot.held().get()));
		player.setItemInHand(InteractionHand.OFF_HAND, shot.offHand().get());
		Vec3 eye = at(shot.stand()).add(0.0D, 1.62D, 0.0D);
		Vec3 target = at(shot.aim());
		double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
		// A perch under a raised stand, so a survival player stays where the shot wants the camera.
		if (shot.stand().y > 0.0D) level.setBlock(BlockPos.containing(at(shot.stand())).below(), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 3);
		player.teleportTo(level, eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0F;
	}

	// ── Scenes ──

	/** Empties the room's air and its floor of anything a scene laid, and takes away loose items. */
	private static void clearRoom(ServerLevel level, ServerPlayer player) {
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-11, 0, -3), row.offset(13, 5, 13))) {
			if (!level.getBlockState(pos).isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
		}
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-11, -1, -3), row.offset(13, -1, 13))) {
			level.setBlock(pos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
		}
		level.getEntitiesOfClass(Entity.class, new AABB(row).inflate(16.0D), entity -> !(entity instanceof ServerPlayer))
				.forEach(Entity::discard);
	}

	private static BlockState mark(net.minecraft.world.level.block.Block block, Direction face) {
		return block.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(face), true);
	}

	private static BlockPos cell(int x, int y, int z) {
		return row.offset(x, y, z);
	}

	private static void stains(ServerLevel level, ServerPlayer player) {
		clearRoom(level, player);
		int[][] floor = {{-2, 3}, {-1, 3}, {-1, 4}, {1, 3}, {2, 3}, {2, 4}, {3, 3}};
		int[] ages = {GroundRunRules.WET, GroundRunRules.WET, GroundRunRules.WET, GroundRunRules.TACKY, GroundRunRules.TACKY,
				GroundRunRules.DRY, GroundRunRules.DRY};
		for (int i = 0; i < floor.length; i++) {
			level.setBlock(cell(floor[i][0], 0, floor[i][1]), mark(BlockInit.blood_stain.get(), Direction.DOWN)
					.setValue(BloodStainBlock.AGE, ages[i]), 2);
		}
		// A wall behind, splashed.
		for (int x = -3; x <= 3; x++) for (int y = 0; y <= 2; y++) level.setBlock(cell(x, y, 8), Blocks.STONE_BRICKS.defaultBlockState(), 2);
		level.setBlock(cell(-1, 1, 7), mark(BlockInit.blood_stain.get(), Direction.SOUTH), 2);
		level.setBlock(cell(1, 0, 7), mark(BlockInit.blood_stain.get(), Direction.SOUTH).setValue(BloodStainBlock.AGE, GroundRunRules.TACKY), 2);
	}

	/** A gouge route across the floor and up a wall at its end. */
	private static void route(ServerLevel level, ServerPlayer player) {
		clearRoom(level, player);
		for (int x = -3; x <= 3; x++) for (int y = 0; y <= 3; y++) level.setBlock(cell(x, y, 9), Blocks.STONE_BRICKS.defaultBlockState(), 2);
		for (int x = -3; x <= 2; x++) level.setBlock(cell(x, 0, 6), mark(BlockInit.blood_gouge.get(), Direction.DOWN), 2);
		for (int z = 6; z <= 8; z++) level.setBlock(cell(2, 0, z), mark(BlockInit.blood_gouge.get(), Direction.DOWN), 2);
		for (int y = 0; y <= 2; y++) {
			BlockPos at = cell(2, y, 8);
			BlockState there = level.getBlockState(at);
			BlockState gouge = there.is(BlockInit.blood_gouge.get()) ? there : BlockInit.blood_gouge.get().defaultBlockState();
			level.setBlock(at, gouge.setValue(MultifaceBlock.getFaceProperty(Direction.SOUTH), true), 2);
		}
	}

	/** Seven short runs side by side, each lit with one function. */
	private static void functions(ServerLevel level, ServerPlayer player) {
		clearRoom(level, player);
		GroundFunction[] functions = {GroundFunction.NONE, GroundFunction.LIGHT, GroundFunction.HOT, GroundFunction.COLD,
				GroundFunction.PULL, GroundFunction.FEED, GroundFunction.POINT};
		for (int i = 0; i < functions.length; i++) {
			int x = -6 + i * 2;
			for (int z = 3; z <= 7; z++) level.setBlock(cell(x, 0, z), mark(BlockInit.blood_gouge.get(), Direction.DOWN), 2);
			GroundNetwork.pulse(level, cell(x, 0, 3), functions[i], 2400,
					functions[i] == GroundFunction.POINT ? cell(x, 0, 40) : null, player.getUUID());
		}
		// Beside the hot run (x -2) an unlit campfire; between the feeding (x 4) and pointing (x 6) runs, wheat.
		level.setBlock(cell(-1, 0, 7), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false), 2);
		level.setBlock(cell(5, -1, 5), Blocks.FARMLAND.defaultBlockState(), 2);
		level.setBlock(cell(5, 0, 5), Blocks.WHEAT.defaultBlockState(), 2);
		ItemEntity ore = new ItemEntity(level, cell(-2, 0, 5).getX() + 0.5D, row.getY() + 0.05D, cell(-2, 0, 5).getZ() + 0.5D,
				new ItemStack(Items.RAW_IRON, 4));
		ore.setDeltaMovement(Vec3.ZERO);
		ore.setPickUpDelay(32767);
		level.addFreshEntity(ore);
	}

	/** Two scriptoriums joined by a living vein between two nodes, the left one full. */
	private static void vein(ServerLevel level, ServerPlayer player) {
		clearRoom(level, player);
		BlockPos a = cell(-3, 0, 5);
		BlockPos b = cell(4, 0, 5);
		level.setBlock(a, BlockInit.enzymatic_scriptorium.get().defaultBlockState(), 3);
		level.setBlock(b, BlockInit.enzymatic_scriptorium.get().defaultBlockState(), 3);
		level.setBlock(a.east(), BlockInit.vein_node.get().defaultBlockState(), 3);
		level.setBlock(b.west(), BlockInit.vein_node.get().defaultBlockState(), 3);
		((VeinNodeBlockEntity) level.getBlockEntity(a.east())).setOwner(player.getUUID(), level.getGameTime());
		((VeinNodeBlockEntity) level.getBlockEntity(b.west())).setOwner(player.getUUID(), level.getGameTime());
		for (int x = -1; x <= 2; x++) {
			level.setBlock(cell(x, 0, 5), mark(BlockInit.living_vein.get(), Direction.DOWN), 3);
			VeinLedger.get(level).claim(level, cell(x, 0, 5), player.getUUID());
		}
		ReservoirBlood.settled(level.getBlockEntity(a)).fill(500.0D);
	}

	/** A worked basin, full, pouring east down a run of worked gutters to a scriptorium. */
	private static void basin(ServerLevel level, ServerPlayer player) {
		clearRoom(level, player);
		for (int x = -1; x <= 3; x++) {
			level.setBlock(cell(x, -1, 6), BlockInit.blood_gutter.get().defaultBlockState().setValue(BloodGutterBlock.WORKED, true)
					.setValue(BloodGutterBlock.EAST, x < 3).setValue(BloodGutterBlock.WEST, x > -1), 3);
		}
		level.setBlock(cell(3, 0, 7), BlockInit.enzymatic_scriptorium.get().defaultBlockState(), 3);
		BlockPos basin = cell(-2, 0, 6);
		level.setBlock(basin, BlockInit.venous_basin.get().defaultBlockState().setValue(VenousBasinBlock.WORKED, true)
				.setValue(VenousBasinBlock.FACING, Direction.EAST), 3);
		((com.vincenthuto.hemomancy.common.tile.harbinger.decoration.VenousBasinBlockEntity) level.getBlockEntity(basin))
				.configure().fill(1800.0D);
	}

	/** A short run lit with glowstone, a stocked feeder beside it. */
	private static void feeder(ServerLevel level, ServerPlayer player) {
		clearRoom(level, player);
		for (int x = -2; x <= 2; x++) level.setBlock(cell(x, 0, 5), mark(BlockInit.blood_gouge.get(), Direction.DOWN), 2);
		BlockPos feeder = cell(0, 0, 6);
		level.setBlock(feeder, BlockInit.catalyst_feeder.get().defaultBlockState(), 3);
		((CatalystFeederBlockEntity) level.getBlockEntity(feeder)).stock().insertItem(0, new ItemStack(Items.GLOWSTONE_DUST, 16), false);
		GroundNetwork.pulse(level, cell(-2, 0, 5), GroundFunction.LIGHT, 7200, null);
	}

	/** Three lit candles and a Lux trace beside them. */
	private static void candles(ServerLevel level, ServerPlayer player) {
		clearRoom(level, player);
		for (int x = -1; x <= 1; x++) {
			level.setBlock(cell(x, 0, 5), Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true)
					.setValue(CandleBlock.CANDLES, 3), 3);
		}
		for (int x = -1; x <= 1; x++) {
			level.setBlock(cell(x, 0, 6), mark(BlockInit.lux_trace.get(), Direction.DOWN).setValue(LuxTraceBlock.FADE, 0), 3);
		}
	}

	/** Items scattered over wet stains and bare floor. */
	private static void items(ServerLevel level, ServerPlayer player) {
		DarkFields.logout(player);
		clearRoom(level, player);
		for (int z = 4; z <= 8; z++) level.setBlock(cell(-1, 0, z), mark(BlockInit.blood_stain.get(), Direction.DOWN), 2);
		Item[] loot = {Items.COBBLESTONE, Items.RAW_IRON, Items.BONE, Items.STRING, Items.COAL, Items.FLINT};
		for (int i = 0; i < loot.length; i++) {
			int x = i % 2 == 0 ? -1 : 2;
			ItemEntity item = new ItemEntity(level, row.getX() + x + 0.5D, row.getY() + 0.1D, row.getZ() + 5.5D + i * 0.6D,
					new ItemStack(loot[i], 3));
			item.setDeltaMovement(Vec3.ZERO);
			level.addFreshEntity(item);
		}
	}

	/**
	 * A hot run across the floor with a furnace, smoker and blast furnace behind it, each loaded and with no fuel, and an
	 * empty furnace at the end that stays cold.
	 */
	private static void furnaces(ServerLevel level, ServerPlayer player) {
		DarkFields.logout(player);
		clearRoom(level, player);
		for (int x = -3; x <= 3; x++) level.setBlock(cell(x, 0, 5), mark(BlockInit.blood_gouge.get(), Direction.DOWN), 2);
		net.minecraft.world.level.block.Block[] kinds = {Blocks.FURNACE, Blocks.SMOKER, Blocks.BLAST_FURNACE, Blocks.FURNACE};
		Item[] inputs = {Items.RAW_IRON, Items.BEEF, Items.RAW_GOLD, null};
		for (int i = 0; i < kinds.length; i++) {
			BlockPos at = cell(-3 + i * 2, 0, 6);
			level.setBlock(at, kinds[i].defaultBlockState(), 3);
			if (inputs[i] != null && level.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity furnace) {
				furnace.setItem(0, new ItemStack(inputs[i], 16));
			}
		}
		GroundNetwork.pulse(level, cell(-3, 0, 5), GroundFunction.HOT, 2400, null, player.getUUID());
	}

	/**
	 * A lit room (lanterns along the walls) with candles at its far end, and a field of dark hanging over them, as Blood
	 * Eclipse leaves it: 800 ticks, radius 5, its edge three blocks in front of the camera.
	 */
	private static void darkField(ServerLevel level, ServerPlayer player) {
		DarkFields.logout(player);
		clearRoom(level, player);
		for (int x = -5; x <= 5; x++) for (int y = 0; y <= 3; y++) level.setBlock(cell(x, y, 13), Blocks.STONE_BRICKS.defaultBlockState(), 2);
		for (int z = 0; z <= 12; z += 3) {
			level.setBlock(cell(-5, 0, z), Blocks.LANTERN.defaultBlockState(), 2);
			level.setBlock(cell(5, 0, z), Blocks.LANTERN.defaultBlockState(), 2);
		}
		for (int x = -1; x <= 1; x++) {
			level.setBlock(cell(x, 0, 9), Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3), 3);
		}
		level.setBlock(cell(2, 0, 11), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false), 3);
		DarkFields.feed(player, Vec3.atCenterOf(cell(0, 0, 9)), 800);
	}

	/**
	 * Two stented Earthen Veins the player has claimed, on a raised floor, joined by a living vein; the travel display is
	 * opened from the near one, so it shows the carried price.
	 */
	private static void veinTravel(ServerLevel level, ServerPlayer player) {
		DarkFields.logout(player);
		clearRoom(level, player);
		BlockPos near = cell(-3, 0, 6);
		BlockPos far = cell(4, 0, 6);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getVeinList().clear();
		for (BlockPos pos : new BlockPos[] {near, far}) {
			level.setBlock(pos, BlockInit.earthen_vein.get().defaultBlockState()
					.setValue(com.vincenthuto.hemomancy.common.block.harbinger.functional.EarthenVeinBlock.STENTED, true), 3);
			var vein = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.EarthenVeinBlockEntity) level.getBlockEntity(pos);
			vein.setLoc(new com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation(
					pos == near ? "Cellar" : "Gallery", level.dimension().location(), pos));
			vein.setHasTicked(true);
			known.getVeinList().add(vein.getLoc());
		}
		for (int x = -2; x <= 3; x++) {
			level.setBlock(cell(x, 0, 6), mark(BlockInit.living_vein.get(), Direction.DOWN), 3);
			VeinLedger.get(level).claim(level, cell(x, 0, 6), player.getUUID());
		}
		KnownManipulationEvents.syncPlayerEvent(player);
		// After the pose has placed the player within reach of the near opening.
		player.getServer().execute(() -> com.vincenthuto.hemomancy.common.world.vein.EarthenVeinTravelManager.begin(player, near));
	}

	/**
	 * A pool walled on three sides whose fourth side is a lit living vein run: the water runs over the vein and on across
	 * the floor beyond, and the vein stays lit under it (CQ-44, NQ-02-4). A short second vein lies under the pool, under
	 * a source and lit. Water breaks a gouge, so only veins are set in the water.
	 */
	private static void water(ServerLevel level, ServerPlayer player) {
		com.vincenthuto.hemomancy.common.world.vein.EarthenVeinTravelManager.logout(player);
		clearRoom(level, player);
		for (int x = -4; x <= 4; x++) level.setBlock(cell(x, 0, 2), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
		for (int z = 2; z <= 7; z++) {
			level.setBlock(cell(-4, 0, z), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
			level.setBlock(cell(4, 0, z), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
		}
		for (int x = -3; x <= 3; x++) level.setBlock(cell(x, 0, 6), mark(BlockInit.living_vein.get(), Direction.DOWN), 3);
		for (int x = -1; x <= 1; x++) {
			level.setBlock(cell(x, 0, 4), mark(BlockInit.living_vein.get(), Direction.DOWN)
					.setValue(com.vincenthuto.hemomancy.common.block.harbinger.ground.LivingVeinBlock.WATER,
							com.vincenthuto.hemomancy.common.block.harbinger.ground.LivingVeinBlock.SOURCE), 3);
		}
		level.setBlock(cell(-2, 0, 3), Blocks.WATER.defaultBlockState(), 3);
		level.setBlock(cell(2, 0, 3), Blocks.WATER.defaultBlockState(), 3);
		GroundNetwork.pulse(level, cell(-3, 0, 6), GroundFunction.LIGHT, 2400, null, player.getUUID());
		GroundNetwork.pulse(level, cell(-1, 0, 4), GroundFunction.LIGHT, 2400, null, player.getUUID());
	}

	private static Vec3 at(Vec3 offset) {
		return new Vec3(row.getX() + offset.x, row.getY() + offset.y, row.getZ() + offset.z);
	}
}

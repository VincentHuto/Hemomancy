package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.HumoralBarometerBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.OssuaryClockBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodStainBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.event.DailyLoopEvents;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodMoon;
import com.vincenthuto.hemomancy.common.world.answer.BloodSpilledLedger;
import com.vincenthuto.hemomancy.common.world.answer.WorldAnswer;
import com.vincenthuto.hemomancy.common.world.answer.WorldAnswerRules;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunRules;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * With hemomancy.dailyLoopReview, stages core-loop Track 10 in a closed room of a disposable world, captures it into
 * build/daily-loop-client-review/screenshots and stops. A real survival player: the Humoral Barometer and the Ossuary
 * Clock on a quiet waning crescent, over seething ground at the full moon, and under a Blood Moon; stained sod among
 * grass and old stains; a vampire bat drawn down to drink wet stains under a roof; a hungry player eating a steak (the
 * meal line); and Well Rested on the HUD. Run with dailyLoopReviewClient and drop
 * {"op":"create","name":"DailyLoopReview","seed":"1"} into operator/inbox after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class DailyLoopClientReview {
	private static final String PROPERTY = "hemomancy.dailyLoopReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 100;
	private static final int PRESS_TICK = 12;
	/** A waning crescent night (day 3) and a full-moon night (day 8), both at midnight. */
	private static final long CRESCENT_NIGHT = 3L * 24_000L + 18_000L;
	private static final long FULL_MOON_NIGHT = 8L * 24_000L + 18_000L;
	private static int ticks;
	private static int shot;
	private static int captured;
	private static BlockPos row;

	/** One staged moment, as in {@link FarmingClientReview}. */
	private record Shot(String name, Supplier<Item> held, Vec3 stand, Vec3 aim, boolean use, int release, int[] captures,
			BiConsumer<ServerLevel, ServerPlayer> scene) {
	}

	private static final List<Shot> SHOTS = List.of(
			new Shot("instruments-quiet", () -> Items.AIR, new Vec3(0.5, 0, 1.6), new Vec3(0.5, 1.4, 4.0), false, 0,
					new int[]{30}, (level, player) -> instruments(level, player, CRESCENT_NIGHT, 0.0D, false)),
			new Shot("instruments-seething", () -> Items.AIR, new Vec3(0.5, 0, 1.6), new Vec3(0.5, 1.4, 4.0), false, 0,
					new int[]{30}, (level, player) -> instruments(level, player, FULL_MOON_NIGHT,
							WorldAnswerRules.bandFloor(WorldAnswerRules.SEETHING), false)),
			new Shot("instruments-risen", () -> Items.AIR, new Vec3(0.5, 0, 1.6), new Vec3(0.5, 1.4, 4.0), false, 0,
					new int[]{30}, (level, player) -> instruments(level, player, FULL_MOON_NIGHT,
							WorldAnswerRules.bandFloor(WorldAnswerRules.SEETHING), true)),
			new Shot("instrument-closeup", () -> Items.AIR, new Vec3(-0.5, 0.4, 3.0), new Vec3(-0.5, 1.6, 4.0), false, 0,
					new int[]{30}, (level, player) -> instruments(level, player, FULL_MOON_NIGHT,
							WorldAnswerRules.bandFloor(WorldAnswerRules.SEETHING), false)),
			new Shot("stained-sod", () -> Items.AIR, new Vec3(0.5, 0, 0.5), new Vec3(0.5, -1.0, 4.5), false, 0,
					new int[]{30}, DailyLoopClientReview::sod),
			new Shot("scavenger", () -> Items.AIR, new Vec3(0.5, 0, 0.5), new Vec3(0.5, 0.2, 4.5), false, 0,
					new int[]{20, 45, 70}, DailyLoopClientReview::scavenger),
			new Shot("meal", () -> Items.COOKED_BEEF, new Vec3(0.5, 0, 0.5), new Vec3(0.5, 1.0, 4.5), true, 60,
					new int[]{30, 52}, DailyLoopClientReview::hungry),
			new Shot("well-rested", () -> Items.AIR, new Vec3(0.5, 0, 0.5), new Vec3(0.5, 1.0, 4.5), false, 0,
					new int[]{20}, DailyLoopClientReview::rested));

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("daily-loop-client-review")) return;
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
				Hemomancy.LOGGER.info("DAILY_LOOP_REVIEW captured {}", captured);
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
		if (local == PRESS_TICK && current.use()) {
			KeyMapping.click(mc.options.keyUse.getKey());
			KeyMapping.set(mc.options.keyUse.getKey(), true);
		}
		if (local == current.release() && current.use()) KeyMapping.set(mc.options.keyUse.getKey(), false);
		for (int at : current.captures()) {
			if (local != at) continue;
			Screenshot.grab(mc.gameDirectory, "daily-loop-" + shot + "-" + current.name() + "-" + at + ".png",
					mc.getMainRenderTarget(), message -> {});
			captured++;
			Hemomancy.LOGGER.info("DAILY_LOOP_REVIEW shot {} {} at {}", shot, current.name(), at);
		}
		if (local == SHOT_TICKS - 1) {
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			shot++;
		}
	}

	/** A closed room of deepslate and the player's awake blood. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		row = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 18000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false", "gamerule randomTickSpeed 0"})
			server.getCommands().performPrefixedCommand(source, cmd);
		ServerLevel level = player.serverLevel();
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-12, -5, -4), row.offset(14, 8, 14))) {
			boolean shell = pos.getY() <= row.getY() - 1 || pos.getY() == row.getY() + 8 || pos.getX() == row.getX() - 12
					|| pos.getX() == row.getX() + 14 || pos.getZ() == row.getZ() - 4 || pos.getZ() == row.getZ() + 14;
			level.setBlock(pos, !shell ? Blocks.AIR.defaultBlockState() : pos.getY() <= row.getY() - 1
					? Blocks.POLISHED_DEEPSLATE.defaultBlockState() : Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
		}
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000.0D);
		volume.setBloodVolume(2500.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(3);
		player.getInventory().clearContent();
		Hemomancy.LOGGER.info("DAILY_LOOP_REVIEW staged at {}", row);
	}

	private static void pose(ServerPlayer player, Shot shot) {
		ServerLevel level = player.serverLevel();
		player.removeEffect(com.vincenthuto.hemomancy.common.init.EffectInit.well_rested);
		player.getFoodData().setFoodLevel(20);
		player.getFoodData().setSaturation(5.0F);
		shot.scene().accept(level, player);
		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, false));
		player.setHealth(player.getMaxHealth());
		player.getInventory().selected = 0;
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(shot.held().get(), 4));
		player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		Vec3 eye = at(shot.stand()).add(0.0D, 1.62D, 0.0D);
		Vec3 target = at(shot.aim());
		double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
		player.teleportTo(level, eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0F;
	}

	// ── Scenes ──

	private static void clearRoom(ServerLevel level) {
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-11, 0, -3), row.offset(13, 7, 13))) {
			if (!level.getBlockState(pos).isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
		}
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-11, -4, -3), row.offset(13, -1, 13))) {
			level.setBlock(pos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
		}
		level.getEntitiesOfClass(Entity.class, new AABB(row).inflate(16.0D), entity -> !(entity instanceof ServerPlayer))
				.forEach(Entity::discard);
		BloodSpilledLedger.set(level, row, 0.0D);
		setMoon(level, false);
	}

	private static BlockPos cell(int x, int y, int z) {
		return row.offset(x, y, z);
	}

	private static void setMoon(ServerLevel level, boolean active) {
		BloodMoonSavedData moon = BloodMoonSavedData.get(level.getServer().overworld());
		if (active) moon.start(level.getGameTime() + 6_000L);
		else moon.stop();
		PacketHandler.sendToAll(new PacketSyncBloodMoon(active));
	}

	/**
	 * A wall of bricks with the barometer hung on it beside the clock, the night set, the count where they stand set,
	 * and both instruments read at once (their own tick would take five seconds).
	 */
	private static void instruments(ServerLevel level, ServerPlayer player, long dayTime, double count, boolean bloodMoon) {
		clearRoom(level);
		level.setDayTime(dayTime);
		for (int x = -3; x <= 4; x++) {
			for (int y = 0; y <= 3; y++) level.setBlock(cell(x, y, 5), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), 2);
		}
		for (int x = -3; x <= 4; x++) level.setBlock(cell(x, 3, 3), Blocks.LIGHT.defaultBlockState(), 2);
		BlockPos barometer = cell(-1, 1, 4);
		level.setBlock(barometer, BlockInit.humoral_barometer.get().defaultBlockState()
				.setValue(HumoralBarometerBlock.FACING, Direction.SOUTH), 3);
		BlockPos clock = cell(1, 0, 4);
		BlockState lower = BlockInit.ossuary_clock.get().defaultBlockState().setValue(OssuaryClockBlock.FACING, Direction.NORTH);
		level.setBlock(clock, lower, 3);
		level.setBlock(clock.above(), lower.setValue(OssuaryClockBlock.HALF, DoubleBlockHalf.UPPER), 3);
		BloodSpilledLedger.set(level, barometer, count);
		setMoon(level, bloodMoon);
		HumoralBarometerBlock.read(level, barometer, level.getBlockState(barometer));
		OssuaryClockBlock.read(level, clock, level.getBlockState(clock));
		Hemomancy.LOGGER.info("DAILY_LOOP_REVIEW instruments: day {} phase {} band {} omen {} chance {}", dayTime,
				level.getMoonPhase(), level.getBlockState(barometer).getValue(HumoralBarometerBlock.PRESSURE),
				level.getBlockState(clock.above()).getValue(OssuaryClockBlock.OMEN), OssuaryClockBlock.chanceTonight(level, clock));
	}

	private static BlockState stain(int age) {
		return BlockInit.blood_stain.get().defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true)
				.setValue(BloodStainBlock.AGE, age);
	}

	/** A lawn sunk into the floor: grass, a few patches of stained sod where stains wore away, and two stains left. */
	private static void sod(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		level.setDayTime(6_000L);
		for (int x = -4; x <= 5; x++) {
			for (int z = 2; z <= 8; z++) level.setBlock(cell(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
		}
		int[][] sod = {{-1, 4}, {0, 4}, {0, 5}, {1, 5}, {2, 6}, {-2, 6}, {3, 3}, {1, 7}};
		for (int[] at : sod) level.setBlock(cell(at[0], -1, at[1]), BlockInit.stained_sod.get().defaultBlockState(), 2);
		level.setBlock(cell(1, 0, 4), stain(GroundRunRules.DRY), 2);
		level.setBlock(cell(-1, 0, 5), stain(GroundRunRules.TACKY), 2);
		for (int x = -3; x <= 4; x += 3) level.setBlock(cell(x, 3, 5), Blocks.LIGHT.defaultBlockState(), 2);
	}

	/** Wet stains on the floor of the roofed room, and the bat their scent draws down over seething ground. */
	private static void scavenger(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		level.setDayTime(18_000L);
		int[][] stains = {{-1, 4}, {0, 5}, {1, 4}, {2, 6}, {-2, 6}};
		for (int[] at : stains) level.setBlock(cell(at[0], 0, at[1]), stain(GroundRunRules.WET), 2);
		BlockPos centre = cell(0, 0, 5);
		BloodSpilledLedger.set(level, centre, WorldAnswerRules.bandFloor(WorldAnswerRules.SEETHING));
		RandomSource random = RandomSource.create(5L);
		Entity bat = null;
		for (int i = 0; i < 32 && bat == null; i++) bat = WorldAnswer.drawScavenger(level, centre, random);
		Hemomancy.LOGGER.info("DAILY_LOOP_REVIEW scavenger drawn: {}", bat);
	}

	/** A hungry body with a steak in hand. */
	private static void hungry(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		player.getFoodData().setFoodLevel(8);
		player.getFoodData().setSaturation(0.0F);
	}

	/** Waking from a full night. */
	private static void rested(ServerLevel level, ServerPlayer player) {
		clearRoom(level);
		DailyLoopEvents.rest(player);
	}

	private static Vec3 at(Vec3 offset) {
		return new Vec3(row.getX() + offset.x, row.getY() + offset.y, row.getZ() + offset.z);
	}
}

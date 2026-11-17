package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperRules;
import com.vincenthuto.hemomancy.common.entity.summon.EntityIronWall;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.TemporaryIceManager;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
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
 * With hemomancy.secondSculptingsReview, stages core-loop round two P4's second wave of Sculptings in a disposable world,
 * captures them into build/second-sculptings-client-review/screenshots and stops. A real survival player holds the use
 * key: Sanguine Mending on a dropped pickaxe, an iron golem and a chipped anvil; Crimson Flame Conjuration kindling a
 * ring of candles round a campfire; Furnace Veins lighting a bank of three furnaces from one stream; Glacial Grasp
 * sweeping a frozen path across a pond (HUD price in view) and Absorption thawing it; both Ramparts swept along the
 * ground (third person); Carrion Communion drawing rotten flesh and a bone block. A shot with a sweep turns the client
 * player's view from its aim to its sweep end while the key is held. Run with secondSculptingsReviewClient and drop
 * {"op":"create","name":"Sculptings2Review","seed":"1"} into operator/inbox after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SecondSculptingsClientReview {
	private static final String PROPERTY = "hemomancy.secondSculptingsReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 112;
	private static final int PRESS_TICK = 12;
	private static int ticks;
	private static int shot;
	private static int captured;
	private static BlockPos row;

	/**
	 * One staged moment. {@code stand}, {@code aim} and {@code sweepTo} are offsets from the row; with a sweep the
	 * view turns from {@code aim} to {@code sweepTo} between the press and {@code release}. {@code captures} are the
	 * shot's local ticks to grab a frame at. {@code scene} restages what this shot works on (server side), or is null
	 * to keep the last shot's aftermath.
	 */
	private record Shot(String name, String manipulation, Verb verb, Supplier<Item> held, Vec3 stand, Vec3 aim, Vec3 sweepTo,
			int release, CameraType camera, int[] captures, BiConsumer<ServerLevel, ServerPlayer> scene) {
	}

	private static final Vec3 TOOL = new Vec3(-8.5, 0.15, 4.5);
	private static final Vec3 GOLEM = new Vec3(-4.5, 1.3, 5.5);
	private static final Vec3 ANVIL = new Vec3(-0.5, 0.5, 4.5);
	private static final Vec3 CAMPFIRE = new Vec3(3.5, 0.25, 5.5);
	private static final Vec3 FURNACES = new Vec3(8.5, 0.5, 5.5);
	private static final Vec3 POND = new Vec3(-6.5, 0.85, 14.5);
	private static final Vec3 FLESH = new Vec3(1.5, 0.15, 13.5);
	private static final Vec3 BONE = new Vec3(4.5, 0.5, 13.5);
	private static final int WALL_Z = 22;

	private static final List<Shot> SHOTS = List.of(
			new Shot("mending-tool", "sanguine_mending", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(-8.5, 0, 1.5), TOOL, null, 70, CameraType.FIRST_PERSON, new int[]{40}, SecondSculptingsClientReview::tool),
			new Shot("mending-golem", "sanguine_mending", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(-4.5, 0, 1.0), GOLEM, null, 70, CameraType.FIRST_PERSON, new int[]{40}, SecondSculptingsClientReview::golem),
			new Shot("mending-anvil", "sanguine_mending", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(-0.5, 0, 1.5), ANVIL, null, 108, CameraType.FIRST_PERSON, new int[]{40, 106}, SecondSculptingsClientReview::anvil),
			new Shot("kindling", "crimson_flame_conjuration", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(3.5, 0, 1.5), CAMPFIRE, null, 60, CameraType.FIRST_PERSON, new int[]{26, 60}, SecondSculptingsClientReview::candles),
			new Shot("furnace-bank", "furnace_veins", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(8.5, 0, 1.5), FURNACES, null, 70, CameraType.FIRST_PERSON, new int[]{34, 68}, SecondSculptingsClientReview::furnaces),
			new Shot("freeze-path", "glacial_grasp", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(-6.5, 0, 9.5), POND.add(0, 0, -2.5), POND.add(0, 0, 3.0), 76, CameraType.FIRST_PERSON, new int[]{50, 76},
					SecondSculptingsClientReview::pond),
			new Shot("thaw-path", "glacial_grasp", Verb.ABSORB, ItemInit.blood_absorption::get,
					new Vec3(-6.5, 0, 9.5), POND.add(0, 0.15, -1.5), POND.add(0, 0.15, 2.0), 76, CameraType.FIRST_PERSON, new int[]{40, 80},
					null),
			new Shot("glacial-rampart", "glacial_rampart", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(0.5, 0, WALL_Z - 6.5), new Vec3(-3.5, -0.02, WALL_Z + 0.5), new Vec3(4.5, -0.02, WALL_Z + 0.5), 60,
					CameraType.THIRD_PERSON_BACK, new int[]{40, 72}, SecondSculptingsClientReview::wallFloor),
			new Shot("ferric-rampart", "ferric_rampart", Verb.PROJECT, ItemInit.blood_projection::get,
					new Vec3(8.5, 0, WALL_Z - 6.5), new Vec3(4.5, -0.02, WALL_Z + 0.5), new Vec3(12.5, -0.02, WALL_Z + 0.5), 70,
					CameraType.THIRD_PERSON_BACK, new int[]{40, 80}, SecondSculptingsClientReview::ironFloor),
			new Shot("carrion-flesh", "carrion_communion", Verb.ABSORB, ItemInit.blood_absorption::get,
					new Vec3(1.5, 0, 10.0), FLESH, null, 60, CameraType.FIRST_PERSON, new int[]{30}, SecondSculptingsClientReview::flesh),
			new Shot("carrion-bone", "carrion_communion", Verb.ABSORB, ItemInit.blood_absorption::get,
					new Vec3(4.5, 0, 10.0), BONE, null, 60, CameraType.FIRST_PERSON, new int[]{30, 56}, SecondSculptingsClientReview::bone));

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("second-sculptings-client-review")) return;
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
				Hemomancy.LOGGER.info("SECOND_SCULPTINGS_REVIEW captured {}", captured);
				mc.stop();
				return;
			}
			Shot current = SHOTS.get(shot);
			mc.options.setCameraType(current.camera());
			mc.options.hideGui = false;
			server.execute(() -> pose(server.getPlayerList().getPlayer(uuid), current));
		}
		if (shot >= SHOTS.size()) return;
		Shot current = SHOTS.get(shot);
		if (local == PRESS_TICK) {
			KeyMapping.click(mc.options.keyUse.getKey());
			KeyMapping.set(mc.options.keyUse.getKey(), true);
		}
		if (current.sweepTo() != null && local >= PRESS_TICK && local <= current.release()) {
			float progress = Mth.clamp((local - PRESS_TICK) / (float) Math.max(1, current.release() - PRESS_TICK - 10), 0.0F, 1.0F);
			look(mc, at(current.stand()).add(0.0D, 1.62D, 0.0D), at(current.aim()).lerp(at(current.sweepTo()), progress));
		}
		if (current.sweepTo() != null && local > current.release()) {
			// After a sweep, look back at its middle so the whole of what it left is in frame.
			look(mc, at(current.stand()).add(0.0D, 1.62D, 0.0D), at(current.aim()).lerp(at(current.sweepTo()), 0.5D));
		}
		if (local == current.release()) KeyMapping.set(mc.options.keyUse.getKey(), false);
		if (local == SHOT_TICKS - 2) server.execute(() -> report(server.getPlayerList().getPlayer(uuid), current));
		for (int at : current.captures()) {
			if (local != at) continue;
			Screenshot.grab(mc.gameDirectory, "second-sculptings-" + shot + "-" + current.name() + "-" + at + ".png",
					mc.getMainRenderTarget(), message -> {});
			captured++;
			Hemomancy.LOGGER.info("SECOND_SCULPTINGS_REVIEW shot {} {} at {} using={}", shot, current.name(), at, mc.player.isUsingItem());
		}
		if (local == SHOT_TICKS - 1) {
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			shot++;
		}
	}

	/** Logs what a wall shot left standing on the wall line, so a short wall is told from a camera that missed it. */
	private static void report(ServerPlayer player, Shot shot) {
		if (player == null) return;
		ServerLevel level = player.serverLevel();
		if (shot.scene() != null && "mending-anvil".equals(shot.name())) {
			Hemomancy.LOGGER.info("SECOND_SCULPTINGS_REVIEW mending-anvil left {}", level.getBlockState(BlockPos.containing(at(ANVIL))).getBlock());
		}
		if (shot.sweepTo() == null) return;
		int ice = 0;
		for (int x = -14; x <= 16; x++) {
			for (int y = 0; y < 3; y++) {
				if (level.getBlockState(row.offset(x, y, WALL_Z)).is(BlockInit.frozen_cruor.get())) ice++;
			}
		}
		int iron = level.getEntitiesOfClass(EntityIronWall.class, new AABB(row).inflate(30.0D)).size();
		Hemomancy.LOGGER.info("SECOND_SCULPTINGS_REVIEW {} left ice={} on the wall line, iron segments={}", shot.name(), ice, iron);
	}

	/** Turns the client player's view on {@code target}; the server hears it with the next movement packet. */
	private static void look(Minecraft mc, Vec3 eye, Vec3 target) {
		double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
		mc.player.setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
		mc.player.setXRot((float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz))));
		mc.player.yHeadRot = mc.player.getYRot();
	}

	/** A flat floor, the player's blood, Temper and the seven memories, equipped. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		ServerLevel level = player.serverLevel();
		row = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 6000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-14, -2, -3), row.offset(16, 10, 26)))
			level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		level.getEntitiesOfClass(Entity.class, new AABB(row).inflate(30.0D), entity -> !(entity instanceof ServerPlayer))
				.forEach(Entity::discard);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000.0D);
		volume.setBloodVolume(5000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		for (EnumBloodTemper school : EnumBloodTemper.values()) temperament.setTemperAlignment(school, 70.0F);
		BloodTemperamentEvents.syncTemperament(player, temperament);
		var temper = HemoCapabilityAccess.getBloodTemper(player);
		for (EnumBloodTemper school : EnumBloodTemper.values()) temper.set(school, TemperRules.MAX_CAP, TemperRules.MAX_CAP);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		List<String> seven = List.of("sanguine_mending", "crimson_flame_conjuration", "furnace_veins", "glacial_grasp",
				"glacial_rampart", "ferric_rampart", "carrion_communion");
		for (String name : seven) {
			known.getKnownManips().put(ManipulationInit.getByName(name), new ManipLevel(1, 0));
			// A family form needs its baseline mastered far enough to be absorbed.
			ManipulationFamilyRegistry.form(name).ifPresent(form -> known.getKnownManips().put(
					ManipulationInit.getByName(ManipulationFamilyRegistry.baselineId(name)), new ManipLevel(form.requiredLevel(), 0)));
		}
		known.setEquippedManipNames(new ArrayList<>(seven));
		KnownManipulationEvents.syncPlayerEvent(player);
		Hemomancy.LOGGER.info("SECOND_SCULPTINGS_REVIEW staged at {}", row);
	}

	/** Selects, arms, places and aims the player for one shot, and restages its scene. */
	private static void pose(ServerPlayer player, Shot shot) {
		ServerLevel level = player.serverLevel();
		if (shot.scene() != null) shot.scene().accept(level, player);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.setSelectedManip(ManipulationInit.getByName(shot.manipulation()));
		known.setActiveVerb(shot.verb());
		KnownManipulationEvents.syncPlayerEvent(player);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setBloodVolume(4500.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		player.setHealth(player.getMaxHealth() - 6.0F);
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
	}

	private static ItemEntity lay(ServerLevel level, Vec3 at, ItemStack stack) {
		ItemEntity item = new ItemEntity(level, at.x, row.getY() + 0.05D, at.z, stack);
		item.setNoGravity(true);
		item.setDeltaMovement(Vec3.ZERO);
		item.setUnlimitedLifetime();
		item.setPickUpDelay(32767);
		level.addFreshEntity(item);
		return item;
	}

	private static void tool(ServerLevel level, ServerPlayer player) {
		ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
		pick.setDamageValue(900);
		lay(level, at(TOOL), pick);
	}

	private static void golem(ServerLevel level, ServerPlayer player) {
		IronGolem golem = EntityType.IRON_GOLEM.create(level);
		if (golem == null) return;
		Vec3 at = at(GOLEM);
		golem.moveTo(at.x, row.getY(), at.z, 180.0F, 0.0F);
		golem.setNoAi(true);
		level.addFreshEntity(golem);
		golem.setHealth(40.0F);
	}

	private static void anvil(ServerLevel level, ServerPlayer player) {
		level.setBlock(BlockPos.containing(at(ANVIL)), Blocks.DAMAGED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.EAST), 3);
	}

	/** A campfire in a ring of unlit candles. */
	private static void candles(ServerLevel level, ServerPlayer player) {
		BlockPos fire = BlockPos.containing(at(CAMPFIRE));
		level.setBlock(fire, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false), 3);
		for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
			if (dx == 0 && dz == 0) continue;
			level.setBlock(fire.offset(dx * 2, 0, dz * 2), Blocks.RED_CANDLE.defaultBlockState()
					.setValue(CandleBlock.CANDLES, 1 + Math.abs(dx + dz) % 3).setValue(CandleBlock.LIT, false), 3);
		}
	}

	/** Three blast furnaces side by side, each with raw iron to cook. */
	private static void furnaces(ServerLevel level, ServerPlayer player) {
		BlockPos middle = BlockPos.containing(at(FURNACES));
		for (int dx = -1; dx <= 1; dx++) {
			BlockPos pos = middle.offset(dx, 0, 0);
			level.setBlock(pos, (dx == 0 ? Blocks.FURNACE : Blocks.BLAST_FURNACE).defaultBlockState()
					.setValue(AbstractFurnaceBlock.FACING, Direction.NORTH), 3);
			if (level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace) furnace.setItem(0, new ItemStack(Items.RAW_IRON, 8));
		}
	}

	/** A pond of still water, five wide and seven long, walled and floored in stone brick. */
	private static void pond(ServerLevel level, ServerPlayer player) {
		BlockPos center = BlockPos.containing(at(POND));
		for (int dx = -3; dx <= 3; dx++) for (int dz = -4; dz <= 4; dz++) {
			BlockPos pos = center.offset(dx, 0, dz);
			boolean rim = Math.abs(dx) == 3 || Math.abs(dz) == 4;
			level.setBlock(pos.below(), BlockInit.polished_venous_stone_bricks.get().defaultBlockState(), 3);
			level.setBlock(pos, rim ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState() : Blocks.WATER.defaultBlockState(), 3);
		}
	}

	private static void wallFloor(ServerLevel level, ServerPlayer player) {
		TemporaryIceManager.clearOwned(level, new AABB(row).inflate(30.0D), player.getUUID());
	}

	private static void ironFloor(ServerLevel level, ServerPlayer player) {
		level.getEntitiesOfClass(EntityIronWall.class, new AABB(row).inflate(30.0D)).forEach(Entity::discard);
	}

	private static void flesh(ServerLevel level, ServerPlayer player) {
		Vec3 at = at(FLESH);
		lay(level, at, new ItemStack(Items.ROTTEN_FLESH, 6));
		lay(level, at.add(0.4D, 0.0D, 0.3D), new ItemStack(Items.BONE, 3));
	}

	private static void bone(ServerLevel level, ServerPlayer player) {
		BlockPos at = BlockPos.containing(at(BONE));
		level.setBlock(at, Blocks.BONE_BLOCK.defaultBlockState(), 3);
		level.setBlock(at.east(), Blocks.SKELETON_SKULL.defaultBlockState(), 3);
	}

	private static Vec3 at(Vec3 offset) {
		return new Vec3(row.getX() + offset.x, row.getY() + offset.y, row.getZ() + offset.z);
	}
}

package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffBondHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffGrowth;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffGrowthRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ferric.LodeDrawSculpting;
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
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
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
 * With hemomancy.staffMiningReview, stages core-loop Track 8 in a disposable world, captures it into
 * build/staff-mining-client-review/screenshots and stops. A real survival player: six Living Staffs in item frames,
 * from no rings to five; the held Staff young and old, from the side; the pickaxe form in first person digging and
 * from the side; the Staff morphing into the pickaxe; Lode Draw crawling an iron lode in a wall and the lode broken on
 * release; Hematic Hewing cutting a tunnel and, sneaking, a face round a chest it leaves standing. Run with
 * staffMiningReviewClient and drop {"op":"create","name":"StaffMining","seed":"1"} into operator/inbox after startup.
 * Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class StaffMiningClientReview {
	private static final String PROPERTY = "hemomancy.staffMiningReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 80;
	private static final int PRESS_TICK = 12;
	private static int ticks;
	private static int shot;
	private static int captured;
	private static BlockPos row;
	/** The side camera's entity id for a side shot, or -1. Written on the server thread, read on the client. */
	private static volatile int cameraId = -1;

	private enum Key { NONE, USE, ATTACK }

	/**
	 * One staged moment: where the player stands and looks (offsets from the row), what they hold, how many mL the
	 * Staff has handled, which memory is selected, which key is held from {@link #PRESS_TICK} to {@code release},
	 * whether they sneak, whether a side camera looks back at them, the local ticks to capture at, and the scene
	 * (null keeps the last shot's aftermath).
	 */
	private record Shot(String name, Vec3 stand, Vec3 aim, Supplier<Item> held, double handled, String selected, Key key,
			int release, boolean sneak, boolean side, int[] captures, BiConsumer<ServerLevel, ServerPlayer> scene) {
	}

	private static final Vec3 FRAMES = new Vec3(-1.0, 1.5, 7.9);
	private static final Vec3 LODE_FACE = new Vec3(-8.05, 1.5, 4.5);
	private static final Vec3 HEW_FACE = new Vec3(9.95, 1.5, 4.5);
	private static final Vec3 DIG_FACE = new Vec3(4.5, 0.9, 9.95);

	private static final List<Shot> SHOTS = List.of(
			new Shot("rings-in-frames", new Vec3(-1.0, 0, 2.2), FRAMES, null, 0, null, Key.NONE, 0, false, false,
					new int[]{30}, StaffMiningClientReview::frames),
			new Shot("staff-young-side", new Vec3(4.5, 0, 2.5), new Vec3(4.5, 1.2, 7.5), ItemInit.living_staff::get, 0,
					null, Key.NONE, 0, false, true, new int[]{34}, null),
			new Shot("staff-five-rings-side", new Vec3(4.5, 0, 2.5), new Vec3(4.5, 1.2, 7.5), ItemInit.living_staff::get,
					800_000, null, Key.NONE, 0, false, true, new int[]{34}, null),
			new Shot("pickaxe-digging", new Vec3(4.5, 0, 7.5), DIG_FACE, ItemInit.living_pickaxe::get, 30_000, null,
					Key.ATTACK, 70, false, false, new int[]{20, 46}, StaffMiningClientReview::digWall),
			new Shot("pickaxe-side", new Vec3(4.5, 0, 2.5), new Vec3(4.5, 1.2, 7.5), ItemInit.living_pickaxe::get,
					30_000, null, Key.NONE, 0, false, true, new int[]{34}, null),
			new Shot("staff-morphs-to-pickaxe", new Vec3(4.5, 0, 2.5), new Vec3(4.5, 1.2, 7.5), ItemInit.living_staff::get,
					30_000, null, Key.NONE, 0, false, true, new int[]{27, 33, 44}, null),
			new Shot("lode-draw-held", new Vec3(-5.5, 0, 4.5), LODE_FACE, ItemInit.living_staff::get, 0, "lode_draw",
					Key.USE, 64, false, false, new int[]{36, 56}, StaffMiningClientReview::lodeWall),
			new Shot("lode-draw-released", new Vec3(-5.5, 0, 4.5), LODE_FACE, ItemInit.living_staff::get, 0, "lode_draw",
					Key.NONE, 0, false, false, new int[]{12}, null),
			new Shot("hewing-tunnel", new Vec3(7.5, 0, 4.5), HEW_FACE, ItemInit.living_staff::get, 0, "hematic_hewing",
					Key.USE, 40, false, false, new int[]{18, 50}, StaffMiningClientReview::hewWall),
			// The cell hand, which starts a channel while sneaking (a sneaking Staff stays idle until the hold has begun).
			new Shot("hewing-face-round-a-chest", new Vec3(7.5, 0, 4.5), HEW_FACE, ItemInit.blood_projection::get, 0,
					"hematic_hewing", Key.USE, 24, true, false, new int[]{36}, StaffMiningClientReview::hewWallWithChest));

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("staff-mining-client-review")) return;
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
				Hemomancy.LOGGER.info("STAFF_MINING_REVIEW captured {}", captured);
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
			// Third person behind the stand, so the player's own first-person hand is not drawn over the shot.
			mc.setCameraEntity(mc.level.getEntity(cameraId));
			mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
		}
		if (local == 8) mc.getToasts().clear();
		KeyMapping held = current.key() == Key.USE ? mc.options.keyUse : current.key() == Key.ATTACK ? mc.options.keyAttack : null;
		if (local == PRESS_TICK - 2 && current.sneak()) KeyMapping.set(mc.options.keyShift.getKey(), true);
		if (local == PRESS_TICK && held != null) {
			KeyMapping.click(held.getKey());
			KeyMapping.set(held.getKey(), true);
		}
		if (local == current.release() && held != null) KeyMapping.set(held.getKey(), false);
		if (local == 20 && current.name().equals("staff-morphs-to-pickaxe")) {
			server.execute(() -> LivingStaffWeaponFormHelper.applySelection(server.getPlayerList().getPlayer(uuid),
					ManipulationInit.conjure_pickaxe.get()));
		}
		for (int at : current.captures()) {
			if (local != at) continue;
			Screenshot.grab(mc.gameDirectory, "staffmining-" + shot + "-" + current.name() + "-" + at + ".png",
					mc.getMainRenderTarget(), message -> {});
			captured++;
			ServerPlayer serverPlayer = server.getPlayerList().getPlayer(uuid);
			Hemomancy.LOGGER.info("STAFF_MINING_REVIEW shot {} {} at {} using={} held={} rings={} draw={}", shot,
					current.name(), at, mc.player.isUsingItem(), mc.player.getMainHandItem(),
					LivingStaffGrowth.stampedRing(mc.player.getMainHandItem()),
					serverPlayer == null ? -1 : LodeDrawSculpting.drawSize(serverPlayer));
		}
		if (local == SHOT_TICKS - 1) {
			mc.setCameraEntity(mc.player);
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			KeyMapping.set(mc.options.keyAttack.getKey(), false);
			KeyMapping.set(mc.options.keyShift.getKey(), false);
			shot++;
		}
	}

	/** A clear stone room, the player's blood, the bond and the mining memories, equipped. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		ServerLevel level = player.serverLevel();
		row = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 6000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-13, -1, -3), row.offset(14, 9, 13)))
			level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		level.getEntitiesOfClass(Entity.class, new AABB(row).inflate(18.0D), entity -> !(entity instanceof ServerPlayer))
				.forEach(Entity::discard);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000.0D);
		volume.setBloodVolume(5000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		temperament.setTemperAlignment(EnumBloodTemper.FERRIC, 30.0F);
		BloodTemperamentEvents.syncTemperament(player, temperament);
		HemoCapabilityAccess.getLivingStaffProgress(player).setLivingStaffBond(true);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		List<String> equipped = List.of("lode_draw", "hematic_hewing", "conjure_pickaxe", "conjure_staff");
		for (String name : equipped) known.getKnownManips().put(ManipulationInit.getByName(name), new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>(equipped));
		KnownManipulationEvents.syncPlayerEvent(player);
		Hemomancy.LOGGER.info("STAFF_MINING_REVIEW staged at {}", row);
	}

	/** Selects, arms, places and aims the player for one shot, restages its scene and sets up its side camera. */
	private static void pose(ServerPlayer player, Shot shot) {
		ServerLevel level = player.serverLevel();
		if (shot.scene() != null) shot.scene().accept(level, player);
		HemoCapabilityAccess.getLivingStaffProgress(player).setBloodHandled(shot.handled());
		LivingStaffBondHelper.syncProgress(player);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.setSelectedManip(shot.selected() == null ? com.vincenthuto.hemomancy.common.manipulation.BloodManipulation.BLANK
				: ManipulationInit.getByName(shot.selected()));
		known.setActiveVerb(Verb.PROJECT);
		KnownManipulationEvents.syncPlayerEvent(player);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setBloodVolume(4500.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		TemperFixtures.carry(player);
		player.setHealth(player.getMaxHealth());
		player.getInventory().selected = 0;
		ItemStack held = shot.held() == null ? ItemStack.EMPTY : new ItemStack(shot.held().get());
		LivingStaffGrowth.refreshStamp(player, held);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
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
		if (shot.side()) {
			// In front of the player and off to their right, looking back at the hand and what it holds.
			Vec3 forward = target.subtract(eye).multiply(1, 0, 1).normalize();
			Vec3 right = new Vec3(-forward.z, 0, forward.x);
			Vec3 hand = eye.add(forward.scale(0.4D)).add(right.scale(0.35D)).add(0, -0.55D, 0);
			Vec3 camera = hand.add(forward.scale(2.1D)).add(right.scale(1.5D)).add(0, 0.3D, 0);
			double cx = hand.x - camera.x, cy = hand.y - camera.y, cz = hand.z - camera.z;
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
	}

	/** Six Living Staffs in item frames on a wall, from no growth rings to five. */
	private static void frames(ServerLevel level, ServerPlayer player) {
		BlockPos wall = BlockPos.containing(at(FRAMES)).atY(row.getY());
		level.getEntitiesOfClass(ItemFrame.class, new AABB(wall).inflate(6.0D)).forEach(Entity::discard);
		for (int i = 0; i < 6; i++) {
			BlockPos block = wall.offset(i - 3, 1, 0);
			for (int y = 0; y <= 2; y++) level.setBlock(block.atY(row.getY() + y), Blocks.STONE_BRICKS.defaultBlockState(), 3);
			ItemFrame frame = new ItemFrame(level, block.north(), Direction.NORTH);
			ItemStack staff = new ItemStack(ItemInit.living_staff.get());
			LivingStaffGrowth.stamp(staff, i);
			frame.setItem(staff, false);
			frame.setRotation(1);
			level.addFreshEntity(frame);
		}
		Hemomancy.LOGGER.info("STAFF_MINING_REVIEW frames rings 0..{}", LivingStaffGrowthRules.MAX_RING);
	}

	/** A low stone face to dig into with the pickaxe form. */
	private static void digWall(ServerLevel level, ServerPlayer player) {
		BlockPos face = BlockPos.containing(at(DIG_FACE)).atY(row.getY());
		for (BlockPos pos : BlockPos.betweenClosed(face.offset(-2, 0, 0), face.offset(2, 2, 1)))
			level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
		level.setBlock(face, Blocks.IRON_ORE.defaultBlockState(), 3);
	}

	/** A stone wall with an iron lode running up through it, deepslate iron at its foot. */
	private static void lodeWall(ServerLevel level, ServerPlayer player) {
		BlockPos face = BlockPos.containing(at(LODE_FACE).add(-0.2, 0, 0)).atY(row.getY());
		level.getEntitiesOfClass(ItemEntity.class, new AABB(face).inflate(5.0D)).forEach(Entity::discard);
		for (BlockPos pos : BlockPos.betweenClosed(face.offset(-1, 0, -3), face.offset(0, 4, 3)))
			level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
		int[][] lode = { { 0, 1, 0 }, { 0, 1, 1 }, { 0, 2, 1 }, { 0, 2, 2 }, { 0, 3, 2 }, { 0, 0, 0 }, { 0, 1, -1 } };
		for (int[] offset : lode) {
			level.setBlock(face.offset(offset[0], offset[1], offset[2]),
					(offset[1] == 0 ? Blocks.DEEPSLATE_IRON_ORE : Blocks.IRON_ORE).defaultBlockState(), 3);
		}
		level.setBlock(face.offset(0, 3, -2), Blocks.COAL_ORE.defaultBlockState(), 3);
	}

	/** A plain stone wall three thick to hew. */
	private static void hewWall(ServerLevel level, ServerPlayer player) {
		BlockPos face = BlockPos.containing(at(HEW_FACE).add(0.2, 0, 0)).atY(row.getY());
		level.getEntitiesOfClass(ItemEntity.class, new AABB(face).inflate(5.0D)).forEach(Entity::discard);
		for (BlockPos pos : BlockPos.betweenClosed(face.offset(0, 0, -3), face.offset(2, 3, 3)))
			level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
	}

	/** The same wall with a chest set into the face's lower corner, which the cut must leave standing. */
	private static void hewWallWithChest(ServerLevel level, ServerPlayer player) {
		hewWall(level, player);
		BlockPos face = BlockPos.containing(at(HEW_FACE).add(0.2, 0, 0)).atY(row.getY());
		level.setBlock(face.offset(0, 0, -1), Blocks.CHEST.defaultBlockState(), 3);
	}

	private static Vec3 at(Vec3 offset) {
		return new Vec3(row.getX() + offset.x, row.getY() + offset.y, row.getZ() + offset.z);
	}
}

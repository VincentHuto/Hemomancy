package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * With hemomancy.ringDoorReview, stands two ring doors on a floor in a disposable world, three completed Chapels
 * behind them, and has the viewer really hold Blood Projection on each: the left door as a Silentium (the blood pour,
 * the spread turning to monolith, the rings filling, the sink) and the right as an Apotheos (the hyphae, the cracks,
 * the burst). Each stage is captured first-person and from behind into build/ring-door-review/screenshots, then the
 * client stops. Run with ringDoorReviewClient and drop {"op":"create","name":"RingDoorReview"} into operator/inbox after
 * startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class RingDoorClientReview {
	private static final int PLACE = 40, FIRST = 80;
	/** Ticks of holding before each shot, per door; the door is full at 200. */
	private static final int[] SILENTIUM_SHOTS = {20, 70, 130, 190, 215};
	private static final int[] APOTHEOS_SHOTS = {6, 40, 110, 185, 203};
	/** How long each door's run lasts, holding throughout. */
	private static final int RUN = 230;
	private static int ticks;
	private static BlockPos row;

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.ringDoorReview")) return;
		var mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("ring-door-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.options.hideGui = true;
		var server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		ticks++;
		if (ticks == PLACE) server.execute(() -> place(server.getPlayerList().getPlayer(uuid)));
		if (ticks < FIRST || row == null) return;
		int run = (ticks - FIRST) / RUN, local = (ticks - FIRST) % RUN;
		if (run >= 2) {
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			Hemomancy.LOGGER.info("RING_DOOR_REVIEW captured {}", SILENTIUM_SHOTS.length + APOTHEOS_SHOTS.length);
			mc.stop();
			return;
		}
		boolean apotheos = run == 1;
		if (local == 0) {
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			server.execute(() -> stand(server.getPlayerList().getPlayer(uuid), apotheos));
		}
		// Start holding once the teleport has landed.
		if (local == 6) {
			KeyMapping.click(mc.options.keyUse.getKey());
			KeyMapping.set(mc.options.keyUse.getKey(), true);
		}
		int held = local - 6;
		for (int at : apotheos ? APOTHEOS_SHOTS : SILENTIUM_SHOTS) {
			String name = (apotheos ? "apotheos-" : "silentium-") + at;
			if (held == at) {
				Screenshot.grab(mc.gameDirectory, name + "-first.png", mc.getMainRenderTarget(), message -> {});
				mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			}
			if (held == at + 2) {
				Screenshot.grab(mc.gameDirectory, name + "-behind.png", mc.getMainRenderTarget(), message -> {});
				mc.options.setCameraType(CameraType.FIRST_PERSON);
				logDoor(mc, apotheos, at);
			}
		}
	}

	private static void logDoor(Minecraft mc, boolean apotheos, int at) {
		BlockPos door = door(apotheos);
		if (mc.level.getBlockEntity(door) instanceof RingDoorBlockEntity controller) {
			Hemomancy.LOGGER.info("RING_DOOR_REVIEW {} at {}: phase {}, charge {}, path {}, feeding {}, origin {}",
					apotheos ? "apotheos" : "silentium", at, controller.phase(), controller.charge(), controller.path(),
					controller.feeding(), controller.origin());
		}
	}

	private static BlockPos door(boolean apotheos) {
		return row.offset(apotheos ? 8 : 0, 0, 0);
	}

	private static void place(net.minecraft.server.level.ServerPlayer player) {
		var server = player.getServer();
		var level = player.serverLevel();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"time set 18000", "gamerule doDaylightCycle false", "weather clear",
				"gamerule doMobSpawning false", "difficulty peaceful"})
			server.getCommands().performPrefixedCommand(source, cmd);
		player.setGameMode(GameType.SURVIVAL);
		BlockPos at = player.blockPosition().offset(0, 0, 8);
		for (BlockPos pos : BlockPos.betweenClosed(at.offset(-8, -1, -8), at.offset(16, 7, 8)))
			level.setBlock(pos, pos.getY() < at.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		for (int x = -6; x <= 14; x += 4) level.setBlock(at.offset(x, 6, -4), Blocks.LIGHT.defaultBlockState(), 3);
		row = at;
		for (boolean apotheos : new boolean[]{false, true}) {
			BlockPos door = door(apotheos);
			level.setBlock(door, BlockInit.ring_door.get().defaultBlockState().setValue(RingDoorBlock.FACING,
					Direction.NORTH), 3);
			if (level.getBlockState(door).getBlock() instanceof RingDoorBlock block)
				block.placeFillers(level, door, level.getBlockState(door));
			if (level.getBlockEntity(door) instanceof RingDoorBlockEntity controller) controller.markFillersPlaced();
		}
		// Three Chapels behind the doors, each seen through, so the Silentium's nodes are lit.
		var items = ChapelFocusBlockEntity.LITANY_ITEMS;
		for (int slot = 0; slot < 3; slot++) {
			BlockPos focus = at.offset(slot * 3, 0, 5);
			level.setBlock(focus, BlockInit.chapel_focus.get().defaultBlockState(), 3);
			if (!(level.getBlockEntity(focus) instanceof ChapelFocusBlockEntity chapel)) continue;
			chapel.configure(ChapelRules.Kind.LITANY, slot);
			chapel.start(null, 31L + slot);
			for (int index : ChapelRules.litanyOrder(31L + slot)) chapel.offer(new ItemStack(items[index]), null);
		}
		Hemomancy.LOGGER.info("RING_DOOR_REVIEW placed doors at {}", at);
	}

	/** The viewer made the door's path, holding Blood Projection with blood to spare, three and a half blocks out. */
	private static void stand(net.minecraft.server.level.ServerPlayer player, boolean apotheos) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		degree.setDegreeNumber(8);
		degree.setArchonPath(apotheos ? EnumArchonPath.APOTHEOS : EnumArchonPath.SILENTIUM);
		com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents.syncDegree(player, degree);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000);
		volume.setBloodVolume(4000);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
		BlockPos door = door(apotheos);
		// A little off the door's middle, so the spread starts off-centre.
		double x = door.getX() + .5, y = door.getY() + .2, z = door.getZ() - 3.2;
		double fx = door.getX() + .9, fy = door.getY() + 2.6, fz = door.getZ() + .3;
		float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
		float pitch = (float) -Math.toDegrees(Math.atan2(fy - (y + player.getEyeHeight()), Math.hypot(fx - x, fz - z)));
		player.teleportTo(player.serverLevel(), x, y, z, yaw, pitch);
	}
}

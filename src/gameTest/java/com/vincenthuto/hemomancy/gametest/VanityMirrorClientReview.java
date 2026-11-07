package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.VanityMirrorView;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.ScarletVanityBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.UUID;

/**
 * With hemomancy.vanityMirrorReview (run runVanityMirrorReviewClient, then drop {"op":"create","name":"VanityReview","seed":1}
 * into the operator inbox), stands two Scarlet Vanities in a small room in the sky, one facing south and one facing
 * east, dresses the player, and captures the mirror from in front, at an angle, close up, in third person, in the dark,
 * from behind and on the east-facing vanity, to build/vanity-mirror-review/screenshots. Logs VANITY_MIRROR_REVIEW
 * PASS/FAIL per check. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class VanityMirrorClientReview {
	private static final BlockPos SOUTH_VANITY = new BlockPos(0, 151, 0);
	private static final BlockPos EAST_VANITY = new BlockPos(-6, 151, 2);
	private static final int STAGE = 40;
	private static final int BUILD = STAGE + 40;
	private static final int FIRST_SHOT = 160;
	private static final int GAP = 40;
	/** Each shot: name, feet x, y, z, yaw, pitch, camera, dark, the vanity whose glass should be live (or null). */
	private static final Shot[] SHOTS = {
			new Shot("1-front", 0.5, 151.0, 3.2, 180f, 12f, CameraType.FIRST_PERSON, false, SOUTH_VANITY),
			new Shot("2-angle", 2.6, 151.0, 2.6, 140f, 14f, CameraType.FIRST_PERSON, false, SOUTH_VANITY),
			new Shot("3-close", 0.5, 151.0, 1.6, 180f, 20f, CameraType.FIRST_PERSON, false, SOUTH_VANITY),
			new Shot("4-third-person", 0.5, 151.0, 2.4, 180f, 10f, CameraType.THIRD_PERSON_BACK, false, SOUTH_VANITY),
			new Shot("5-model-side", 3.5, 152.5, 0.5, 90f, 15f, CameraType.FIRST_PERSON, false, null),
			new Shot("6-dark", 0.5, 151.0, 3.2, 180f, 12f, CameraType.FIRST_PERSON, true, SOUTH_VANITY),
			new Shot("7-behind", 0.5, 151.0, -2.5, 0f, 10f, CameraType.FIRST_PERSON, false, null),
			new Shot("8-east-facing", -3.0, 151.0, 2.5, 90f, 12f, CameraType.FIRST_PERSON, false, EAST_VANITY),
	};
	private static final int DONE = FIRST_SHOT + SHOTS.length * GAP + 20;
	private static int ticks;

	private VanityMirrorClientReview() {}

	private record Shot(String name, double x, double y, double z, float yaw, float pitch, CameraType camera,
			boolean dark, BlockPos live) {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.vanityMirrorReview")) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("vanity-mirror-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.options.hideGui = true;
		MinecraftServer server = mc.getSingleplayerServer();
		UUID uuid = mc.player.getUUID();
		ticks++;
		if (ticks == STAGE) server.execute(() -> stage(server.getPlayerList().getPlayer(uuid)));
		if (ticks == BUILD) server.execute(() -> build(server.getPlayerList().getPlayer(uuid)));
		for (int i = 0; i < SHOTS.length; i++) {
			Shot shot = SHOTS[i];
			int at = FIRST_SHOT + i * GAP;
			if (ticks == at - 30) {
				mc.options.setCameraType(shot.camera());
				server.execute(() -> frame(server.getPlayerList().getPlayer(uuid), shot));
			}
			if (ticks == at) {
				capture(mc, "vanity-mirror-" + shot.name() + ".png");
				if (shot.live() != null) {
					report(VanityMirrorView.textureFor(shot.live()) != 0, shot.name() + ": the glass holds a reflection");
				} else if (shot.name().startsWith("7")) {
					report(VanityMirrorView.textureFor(SOUTH_VANITY) == 0, shot.name() + ": nothing is reflected from behind");
				}
			}
		}
		if (ticks == DONE) {
			mc.options.setCameraType(CameraType.FIRST_PERSON);
			Hemomancy.LOGGER.info("VANITY_MIRROR_REVIEW captured {}", SHOTS.length);
			mc.stop();
		}
	}

	/** Clear weather at noon, no mobs, the room's chunks force-loaded, and the player dressed to be seen. */
	private static void stage(ServerPlayer player) {
		if (player == null) return;
		run(player, "gamemode creative", "difficulty peaceful", "gamerule doMobSpawning false",
				"gamerule doDaylightCycle false", "weather clear", "time set noon", "forceload add -32 -32 32 32",
				"item replace entity @s armor.chest with minecraft:diamond_chestplate",
				"item replace entity @s weapon.mainhand with minecraft:iron_sword");
		player.teleportTo(player.serverLevel(), 0.5, 151.0, 3.5, 180f, 10f);
		player.getAbilities().flying = true;
		player.onUpdateAbilities();
		Hemomancy.LOGGER.info("VANITY_MIRROR_REVIEW staged");
	}

	/** A plank floor and back wall with lanterns, and the two vanities placed as a player would. */
	private static void build(ServerPlayer player) {
		if (player == null) return;
		run(player, "fill -10 150 -4 6 150 8 minecraft:dark_oak_planks",
				"fill -10 151 -1 6 154 -1 minecraft:stripped_dark_oak_log",
				"setblock 3 151 -0 minecraft:lantern", "setblock -3 151 0 minecraft:lantern",
				"setblock -9 151 6 minecraft:lantern");
		ServerLevel level = player.serverLevel();
		boolean south = place(level, SOUTH_VANITY, Direction.SOUTH);
		boolean east = place(level, EAST_VANITY, Direction.EAST);
		report(south && east, "both vanities stand two blocks tall");
	}

	private static boolean place(ServerLevel level, BlockPos pos, Direction facing) {
		BlockState lower = BlockInit.scarlet_vanity.get().defaultBlockState().setValue(ScarletVanityBlock.FACING, facing);
		level.setBlock(pos, lower, Block.UPDATE_ALL);
		lower.getBlock().setPlacedBy(level, pos, lower, null, net.minecraft.world.item.ItemStack.EMPTY);
		BlockState upper = level.getBlockState(pos.above());
		return upper.is(BlockInit.scarlet_vanity.get()) && upper.getValue(ScarletVanityBlock.HALF) == DoubleBlockHalf.UPPER;
	}

	private static void run(ServerPlayer player, String... commands) {
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String command : commands) player.server.getCommands().performPrefixedCommand(source, command);
	}

	private static void frame(ServerPlayer player, Shot shot) {
		if (player == null) return;
		run(player, shot.dark() ? "time set midnight" : "time set noon");
		player.teleportTo(player.serverLevel(), shot.x(), shot.y(), shot.z(), shot.yaw(), shot.pitch());
	}

	static void capture(Minecraft mc, String name) {
		Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), message -> {});
		Hemomancy.LOGGER.info("VANITY_MIRROR_REVIEW captured {}", name);
	}

	static void report(boolean pass, String check) {
		Hemomancy.LOGGER.info("VANITY_MIRROR_REVIEW {} {}", pass ? "PASS" : "FAIL", check);
	}
}

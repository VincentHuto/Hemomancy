package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.FaneBoundaryClientData;
import com.vincenthuto.hemomancy.client.render.world.FaneGroundRenderer;
import com.vincenthuto.hemomancy.common.event.worldevent.FaneBoundaryRelation;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncFaneBoundaries;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;
import java.util.UUID;

/**
 * With hemomancy.faneGroundReview (run faneGroundReviewClient, then drop {"op":"create","name":"FaneGroundReview"}
 * into the operator inbox), lays a grass platform in the sky, hands the client one fane over it (a heart and a stake
 * chain of two stakes off its east side) every tick, so the server's own fane sync never clears it, and captures the SURFACE style from
 * inside, at the edge, from above, in the handoff and from afar, by night, in each relation, then the DOME style for
 * comparison, to build/fane-ground-review/screenshots. Logs FANE_GROUND_REVIEW PASS/FAIL per check. Excluded from
 * release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class FaneGroundClientReview {
	private static final BlockPos HEART = new BlockPos(0, 151, 0);
	/** Two Hematic Stakes in a chain off the heart's east side, at the server's stake radius. */
	private static final BlockPos STAKE = new BlockPos(60, 151, 0);
	private static final BlockPos SECOND_STAKE = new BlockPos(108, 151, 18);
	private static final float STAKE_RADIUS = 32.0F;
	private static final UUID OWNER = UUID.nameUUIDFromBytes("hemomancy:fane_ground_review".getBytes());
	private static final int STAGE = 40;
	/** The platform is laid once its force-loaded chunks are in. */
	private static final int BUILD = STAGE + 60;
	private static final int FIRST_SHOT = 200;
	private static final int GAP = 50;
	/** Each shot: name, camera x, y, z, yaw, pitch, relation, night, dome style. */
	private static final Shot[] SHOTS = {
			new Shot("1-member-inside", 0.5, 152.0, 8.5, 0f, 28f, FaneBoundaryRelation.MEMBER, false, false),
			new Shot("2-member-edge", 0.5, 152.0, 34.5, 0f, 32f, FaneBoundaryRelation.MEMBER, false, false),
			new Shot("3-member-above", 0.5, 182.0, 30.5, 180f, 55f, FaneBoundaryRelation.MEMBER, false, false),
			new Shot("4-member-handoff", 0.5, 158.0, 62.5, 180f, 12f, FaneBoundaryRelation.MEMBER, false, false),
			new Shot("5-member-afar", 0.5, 170.0, 112.5, 180f, 8f, FaneBoundaryRelation.MEMBER, false, false),
			new Shot("6-member-night", 0.5, 152.0, 8.5, 0f, 28f, FaneBoundaryRelation.MEMBER, true, false),
			new Shot("7-member-stake-lobe", 30.5, 160.0, 0.5, -90f, 30f, FaneBoundaryRelation.MEMBER, true, false),
			new Shot("7b-member-stake-join-above", 36.5, 175.0, 20.5, 0f, 89f, FaneBoundaryRelation.MEMBER, true, false),
			new Shot("7c-member-stake-chain-above", 84.5, 175.0, 9.5, 0f, 89f, FaneBoundaryRelation.MEMBER, true, false),
			new Shot("7d-member-join-ground", 36.5, 153.0, -6.5, 0f, 22f, FaneBoundaryRelation.MEMBER, true, false),
			new Shot("8-rival-inside", 0.5, 152.0, 8.5, 0f, 28f, FaneBoundaryRelation.RIVAL_ELDER, true, false),
			new Shot("9-outsider-inside", 0.5, 152.0, 8.5, 0f, 28f, FaneBoundaryRelation.OUTSIDER, true, false),
			new Shot("10-outsider-edge", 0.5, 152.0, 34.5, 0f, 32f, FaneBoundaryRelation.OUTSIDER, true, false),
			new Shot("11-mundane-inside", 0.5, 152.0, 8.5, 0f, 28f, FaneBoundaryRelation.MUNDANE_OUTSIDER, true, false),
			new Shot("12-dome-style-inside", 0.5, 152.0, 8.5, 0f, 28f, FaneBoundaryRelation.MEMBER, true, true),
	};
	private static final int DONE = FIRST_SHOT + SHOTS.length * GAP + 20;
	private static int ticks;
	private static FaneBoundaryRelation relation = FaneBoundaryRelation.MEMBER;

	private FaneGroundClientReview() {}

	private record Shot(String name, double x, double y, double z, float yaw, float pitch,
			FaneBoundaryRelation relation, boolean night, boolean dome) {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.faneGroundReview")) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("fane-ground-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.options.hideGui = true;
		MinecraftServer server = mc.getSingleplayerServer();
		UUID uuid = mc.player.getUUID();
		ticks++;
		if (ticks >= STAGE) {
			FaneBoundaryClientData.setEntries(List.of(
					new PacketSyncFaneBoundaries.Entry(HEART, List.of(STAKE, SECOND_STAKE), STAKE_RADIUS, OWNER,
							relation)));
		}
		if (ticks == STAGE) server.execute(() -> stage(server.getPlayerList().getPlayer(uuid)));
		if (ticks == BUILD) server.execute(() -> build(server.getPlayerList().getPlayer(uuid)));
		for (int i = 0; i < SHOTS.length; i++) {
			Shot shot = SHOTS[i];
			int at = FIRST_SHOT + i * GAP;
			if (ticks == at - 35) {
				relation = shot.relation();
				HemoClientConfig.FANE_BOUNDARY_STYLE.set(shot.dome() ? HemoClientConfig.FaneBoundaryStyle.DOME
						: HemoClientConfig.FaneBoundaryStyle.SURFACE);
				server.execute(() -> frame(server.getPlayerList().getPlayer(uuid), shot));
			}
			if (ticks == at) {
				capture(mc, "fane-ground-" + shot.name() + ".png");
				if (i == 0) {
					int faces = FaneGroundRenderer.keptFaces(HEART);
					report(faces > 0, "the ground inside the fane is drawn (" + faces + " floor faces kept)");
				}
				if (shot.dome()) {
					report(FaneGroundRenderer.keptFaces(HEART) == -1, "the DOME style draws no ground");
				}
			}
		}
		if (ticks == DONE) {
			HemoClientConfig.FANE_BOUNDARY_STYLE.set(HemoClientConfig.FaneBoundaryStyle.SURFACE);
			Hemomancy.LOGGER.info("FANE_GROUND_REVIEW captured {}", SHOTS.length);
			mc.stop();
		}
	}

	/** Clear weather, no mobs, and the platform's chunks force-loaded so the fills can reach them. */
	private static void stage(ServerPlayer player) {
		if (player == null) return;
		run(player, "gamemode spectator", "difficulty peaceful", "gamerule doMobSpawning false",
				"gamerule doDaylightCycle false", "weather clear", "time set noon", "forceload add -64 -64 152 64");
		player.teleportTo(player.serverLevel(), 0.5, 160.0, 0.5, 0f, 30f);
		Hemomancy.LOGGER.info("FANE_GROUND_REVIEW staged");
	}

	/** A grass platform at y 150 under the heart and both stake lobes, with a few blocks to stand on. */
	private static void build(ServerPlayer player) {
		if (player == null) return;
		run(player, "fill -56 150 -56 44 150 56 minecraft:grass_block",
				"fill 45 150 -56 144 150 56 minecraft:grass_block",
				"fill -6 151 14 -2 152 18 minecraft:stone_bricks",
				"fill 8 151 -10 9 154 -9 minecraft:stone_bricks",
				"fill -20 151 20 -12 151 26 minecraft:red_carpet");
		boolean laid = player.serverLevel().getBlockState(new BlockPos(140, 150, 50)).is(Blocks.GRASS_BLOCK)
				&& player.serverLevel().getBlockState(new BlockPos(-50, 150, -50)).is(Blocks.GRASS_BLOCK);
		report(laid, "the review platform is laid out to the second stake");
	}

	private static void run(ServerPlayer player, String... commands) {
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String command : commands) player.server.getCommands().performPrefixedCommand(source, command);
	}

	private static void frame(ServerPlayer player, Shot shot) {
		if (player == null) return;
		ServerLevel level = player.serverLevel();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		player.server.getCommands().performPrefixedCommand(source, shot.night() ? "time set midnight" : "time set noon");
		player.teleportTo(level, shot.x(), shot.y(), shot.z(), shot.yaw(), shot.pitch());
	}

	static void capture(Minecraft mc, String name) {
		Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), message -> {});
		Hemomancy.LOGGER.info("FANE_GROUND_REVIEW captured {}", name);
	}

	static void report(boolean pass, String check) {
		Hemomancy.LOGGER.info("FANE_GROUND_REVIEW {} {}", pass ? "PASS" : "FAIL", check);
	}
}

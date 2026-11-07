package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneDirector;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.boss.SaintMeterPacket;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Opt-in native review of the Saint kit's client half ({@code -PsaintKitReview=true} on the boss presentation review
 * client): two Saints (Hemorath, Velorum) stand on a dark stage with their bars shown, the client is sent a
 * {@link SaintMeterPacket} naming Hemorath so the meter layer draws under his bar, and ground hazards in every
 * footprint shape lie round each Saint so the telegraph pass paints them in its default look and each Saint's
 * families. Shots land in the game dir's screenshots as {@code saint-kit-<nn>-<view>.png}; the log ends with
 * {@code SAINT_KIT_REVIEW captured N}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SaintKitClientReview {
	private static final String PROPERTY = "hemomancy.saintKitReview";
	private static final double X = 1800.5D, Y = 200.0D, Z = 520.5D;
	/** Shot ticks and the camera for each: eye, look point. */
	private static final int[] SHOTS = {50, 70, 90, 110, 130};
	private static final Vec3[][] VIEWS = {
			{new Vec3(X, Y + 14.0D, Z - 16.0D), new Vec3(X, Y, Z)},
			{new Vec3(X - 10.0D, Y + 9.0D, Z - 10.0D), new Vec3(X - 3.0D, Y, Z - 2.0D)},
			{new Vec3(X + 10.0D, Y + 9.0D, Z - 10.0D), new Vec3(X + 5.0D, Y, Z)},
			{new Vec3(X, Y + 24.0D, Z - 2.0D), new Vec3(X, Y, Z)},
			{new Vec3(X, Y + 3.0D, Z - 13.0D), new Vec3(X, Y + 1.0D, Z)}};
	private static final String[] NAMES = {"overview", "hemorath", "velorum", "top", "low"};

	private static volatile boolean launched, done;
	private static volatile int tick = -1;
	private static int captured, closing, preparation;
	private static MinecraftServer server;
	private static ServerPlayer player;
	private static final List<Entity> ACTORS = new ArrayList<>();
	private static SaintTrialEntity hemorath;

	private SaintKitClientReview() {
	}

	@SubscribeEvent
	public static void client(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!launched && mc.player != null && mc.level != null && mc.getSingleplayerServer() != null) {
			launched = true;
			server = mc.getSingleplayerServer();
			mc.options.pauseOnLostFocus = false;
			mc.options.hideGui = false;
			var id = mc.player.getUUID();
			server.execute(() -> {
				player = server.getPlayerList().getPlayer(id);
				prepare();
			});
		}
		if (!launched) return;
		BossSceneDirector.INSTANCE.clear();
		int now = tick;
		for (int i = 0; i < SHOTS.length; i++) {
			if (now == SHOTS[i] + 4 && captured == i) {
				captured++;
				Screenshot.grab(mc.gameDirectory, String.format("saint-kit-%02d-%s.png", i, NAMES[i]),
						mc.getMainRenderTarget(), message -> {});
			}
		}
		if (done && ++closing > 30) {
			Hemomancy.LOGGER.info("SAINT_KIT_REVIEW captured {}", captured);
			mc.stop();
		}
	}

	@SubscribeEvent
	public static void server(ServerTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY) || event.getServer() != server || done || player == null) return;
		try {
			if (preparation > 0) {
				if (--preparation == 0) setup();
				return;
			}
			if (tick < 0) return;
			tick++;
			// the meter names Hemorath: a per-player value that climbs, a secondary that falls, a countdown now and then
			if (tick % 5 == 0 && hemorath != null) {
				float value = 20.0F + (tick % 100) * 0.6F;
				PacketHandler.sendToPlayer(player, new SaintMeterPacket(hemorath.getId(), value, 100.0F,
						Math.max(0.0F, 1.0F - tick / 150.0F), 1.0F, 0x1, tick % 60 < 20 ? 20 : 0));
			}
			for (int i = 0; i < SHOTS.length; i++) {
				if (tick == SHOTS[i]) {
					Vec3 eye = VIEWS[i][0], look = VIEWS[i][1];
					player.teleportTo(player.serverLevel(), eye.x, eye.y, eye.z, yaw(eye, look), pitch(eye, look));
				}
			}
			if (tick > SHOTS[SHOTS.length - 1] + 8) finish();
		} catch (Exception error) {
			Hemomancy.LOGGER.error("SAINT_KIT_REVIEW_FAILED", error);
			finish();
		}
	}

	private static void prepare() {
		ServerLevel level = player.serverLevel();
		forceStage(level, true);
		player.setGameMode(GameType.CREATIVE);
		player.getAbilities().flying = true;
		player.onUpdateAbilities();
		player.teleportTo(level, X, Y + 14.0D, Z - 16.0D, 0.0F, 40.0F);
		preparation = 40;
	}

	private static void setup() {
		ServerLevel level = player.serverLevel();
		AABB stage = new AABB(X - 20, Y - 2, Z - 20, X + 20, Y + 16, Z + 20);
		level.getEntitiesOfClass(Entity.class, stage, entity -> !(entity instanceof ServerPlayer)).forEach(Entity::discard);
		for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(X - 18, Y - 1, Z - 18), BlockPos.containing(X + 18, Y - 1, Z + 18)))
			level.setBlock(pos, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
		for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(X - 18, Y, Z - 18), BlockPos.containing(X + 18, Y + 12, Z + 18)))
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
		level.setDayTime(18000);
		hemorath = saint(level, EntityInit.hemorath.get().create(level), new Vec3(X - 5.0D, Y, Z));
		SaintTrialEntity velorum = saint(level, EntityInit.velorum.get().create(level), new Vec3(X + 6.0D, Y, Z + 2.0D));
		double y = Y;
		// Hemorath's ring: lanes, a cone, a ring wave, a cross and a disc
		hazard(level, hemorath, new BossFootprint.Lane(9.0D, 0.5D, 1, 2.0D, 0.0D, 0.0D, 0, BossFootprint.Height.STANDING),
				new Vec3(X - 5.0D, y, Z - 1.0D), new Vec3(X - 5.0D, y, Z - 10.0D));
		hazard(level, hemorath, new BossFootprint.Cone(1.0D, 4.5D, 70.0D, 0.0D, BossFootprint.Height.STANDING),
				new Vec3(X - 6.0D, y, Z + 1.0D), new Vec3(X - 12.0D, y, Z + 4.0D));
		hazard(level, hemorath, new BossFootprint.Ring(0.5D, 6.0D, 0.8D, 3, 30, 90, BossFootprint.Height.JUMPABLE),
				new Vec3(X - 5.0D, y, Z + 7.0D), new Vec3(X - 5.0D, y, Z + 10.0D));
		hazard(level, hemorath, new BossFootprint.Cross(BossFootprint.Anchor.ORIGIN, 2.5D, 0.45D, BossFootprint.Height.STANDING),
				new Vec3(X - 13.0D, y, Z - 6.0D), new Vec3(X - 13.0D, y, Z - 9.0D));
		hazard(level, hemorath, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, 1.6D, BossFootprint.Height.STANDING),
				new Vec3(X - 1.0D, y, Z - 6.0D), new Vec3(X - 1.0D, y, Z - 9.0D));
		// Velorum's: an annulus of turning arcs, spokes, a sweep and a path
		hazard(level, velorum, new BossFootprint.Annulus(2.0D, 3.5D, 3, 60.0D, 1.0D, 160, BossFootprint.Height.STANDING),
				new Vec3(X + 6.0D, y, Z + 2.0D), new Vec3(X + 6.0D, y, Z + 6.0D));
		hazard(level, velorum, new BossFootprint.Spokes(5, 0.35D, 1.0D, 5.0D, 0.0D, 0.0D, BossFootprint.Height.STANDING),
				new Vec3(X + 12.0D, y, Z - 6.0D), new Vec3(X + 12.0D, y, Z - 2.0D));
		hazard(level, velorum, new BossFootprint.Sweep(0.5D, 5.0D, 120.0D, 24.0D, true, 120, BossFootprint.Height.STANDING),
				new Vec3(X + 4.0D, y, Z - 6.0D), new Vec3(X + 4.0D, y, Z - 12.0D));
		Vec3 a = new Vec3(X + 10.0D, y, Z + 8.0D);
		List<Vec3> path = List.of(a, a.add(3.0D, 0.0D, -3.0D), a.add(6.0D, 0.0D, 0.0D), a.add(6.0D, 0.0D, 4.0D));
		BossGroundHazardEntity walker = BossGroundHazardEntity.spawn(level, velorum, 1,
				new BossFootprint.Path(0.5D, 0.15D, 3.0D, 2, 7.0D, BossFootprint.Height.STANDING),
				FootprintFrame.simple(a, a.add(0, 0, -1), a, y).withPoints(path), 2000, 20);
		ACTORS.add(walker);
		player.teleportTo(level, X, Y + 14.0D, Z - 16.0D, 0.0F, 40.0F);
		tick = 0;
		Hemomancy.LOGGER.info("SAINT_KIT_REVIEW started");
	}

	private static SaintTrialEntity saint(ServerLevel level, SaintTrialEntity saint, Vec3 at) {
		saint.setNoAi(true);
		saint.setInvulnerable(true);
		saint.moveTo(at.x, at.y, at.z, 180.0F, 0.0F);
		level.addFreshEntity(saint);
		ACTORS.add(saint);
		return saint;
	}

	private static void hazard(ServerLevel level, Entity owner, BossFootprint shape, Vec3 origin, Vec3 aim) {
		FootprintFrame frame = new FootprintFrame(origin, aim, aim, origin.y, List.of(), LongSets.EMPTY_SET, List.of(),
				new AABB(origin, origin).inflate(8.0D), new AABB(origin, origin).inflate(16.0D));
		ACTORS.add(BossGroundHazardEntity.spawn(level, owner, 1, shape, frame, 2000, 20));
	}

	private static float yaw(Vec3 eye, Vec3 look) {
		return (float) Math.toDegrees(Math.atan2(-(look.x - eye.x), look.z - eye.z));
	}

	private static float pitch(Vec3 eye, Vec3 look) {
		double dx = look.x - eye.x, dz = look.z - eye.z;
		return (float) -Math.toDegrees(Math.atan2(look.y - eye.y, Math.sqrt(dx * dx + dz * dz)));
	}

	private static void forceStage(ServerLevel level, boolean force) {
		for (int cx = -2; cx <= 2; cx++) for (int cz = -2; cz <= 2; cz++) {
			level.setChunkForced(((int) X >> 4) + cx, ((int) Z >> 4) + cz, force);
		}
	}

	private static void finish() {
		if (player != null) forceStage(player.serverLevel(), false);
		for (Entity actor : ACTORS) actor.discard();
		ACTORS.clear();
		if (player != null) PacketHandler.sendToPlayer(player, SaintMeterPacket.clear(hemorath == null ? -1 : hemorath.getId()));
		done = true;
	}
}

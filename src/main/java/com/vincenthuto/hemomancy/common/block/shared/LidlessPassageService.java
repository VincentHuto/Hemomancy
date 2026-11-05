package com.vincenthuto.hemomancy.common.block.shared;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.lidless.LidlessPassageStatePacket;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.ClientBridge;
import com.vincenthuto.hemomancy.common.util.HeldTraveller;
import com.vincenthuto.hemomancy.common.util.StandingSpaces;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The crossing behind a Lidless doorway. A player who steps into a {@link LidlessPortal} is pinned at its centre;
 * their forward and back keys move a progress number along a crossing of the configured length, and reaching either
 * end puts them out standing in the cell on that side. Session state only: nothing is written to the player.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LidlessPassageService {
	/**
	 * What waits at the far end: the world beyond a door, the Qliphoth Seed in its black hole, or at either end of a
	 * Chamber gateway another dimension (GATE from the husk into the Chamber, RETURN back out).
	 */
	public enum Mode {
		DOOR, SEED, GATE, RETURN;

		/** Whether reaching the far end takes the player out of this level instead of landing them in it. */
		public boolean leavesLevel() {
			return this == GATE || this == RETURN;
		}
	}

	/** A walk through a Chamber gateway, each way. */
	public static final int GATEWAY_LENGTH = 6;

	/** Movement speed 0.1 is 4.317 blocks a second: 0.216 a tick. */
	private static final double SPEED_TO_BLOCKS_PER_TICK = 2.16;
	private static final double SPRINT = 1.3;
	private static final double SNEAK = 0.3;
	/** In seed mode the pull: retreat falls to a quarter pace by the end. */
	private static final double SEED_RETREAT_LOSS = 0.75;
	private static final int INPUT_STALE_TICKS = 4;
	private static final double DRIFT_TOLERANCE_SQUARED = 0.04;
	private static final ParticleColor MOTE = new ParticleColor(120, 8, 8);
	private static final Map<UUID, Session> SESSIONS = new HashMap<>();
	/** Doorways offered this tick, started from the player tick: a crossing never begins inside a move-packet handler. */
	private static final Map<UUID, LidlessPortal> OFFERS = new HashMap<>();
	private static long nextToken;

	private LidlessPassageService() {
	}

	/** Whether this entity could step into a doorway right now: a living, unmounted, non-spectator player not mid-Axonal. */
	public static boolean mayCross(Entity entity) {
		return entity instanceof Player player && player.isAlive() && !player.isSpectator() && !player.isPassenger()
				&& !player.isVehicle() && !AxonalTransductionManager.isTraveling(player);
	}

	/** Whether the player is mid-crossing: the server asks its sessions, a client its synced crossing set. */
	public static boolean isCrossing(Player player) {
		if (player instanceof ServerPlayer) return SESSIONS.containsKey(player.getUUID());
		return ClientBridge.get().isCrossingLidlessDoor(player);
	}

	/** How far along the crossing the player is, in blocks; 0 when not crossing. */
	public static double progress(ServerPlayer player) {
		Session session = SESSIONS.get(player.getUUID());
		return session == null ? 0 : session.progress;
	}

	/** The token the crossing player's input must carry; 0 when not crossing. */
	public static long token(ServerPlayer player) {
		Session session = SESSIONS.get(player.getUUID());
		return session == null ? 0L : session.token;
	}

	/**
	 * A player whose body overlaps a doorway. The crossing starts in the player tick, not here: a block's
	 * {@code entityInside} also runs inside the move-packet handler, which then moves the player to the packet's
	 * position and would undo the pin.
	 */
	public static void offer(ServerPlayer player, LidlessPortal portal) {
		if (portal == null || isCrossing(player) || !mayCross(player) || !portal.admits(player)) return;
		OFFERS.put(player.getUUID(), portal);
	}

	private static void begin(ServerPlayer player, LidlessPortal portal) {
		if (isCrossing(player) || !mayCross(player) || !portal.admits(player) || !portal.stillValid(player.serverLevel())) return;
		Session session = new Session(player.serverLevel(), portal, portal.towardFront(player), ++nextToken,
				player.isNoGravity(), portal.length(), portal.theme(player));
		SESSIONS.put(player.getUUID(), session);
		player.stopFallFlying();
		for (Mob mob : player.serverLevel().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(64)))
			if (mob.getTarget() == player) mob.setTarget(null);
		ManipulationChannelManager.stop(player, false);
		player.stopUsingItem();
		pin(player, session);
		sync(player, session, true, true);
		threshold(session, player);
	}

	public static void input(ServerPlayer player, long token, int direction, boolean sprinting, boolean sneaking) {
		Session session = SESSIONS.get(player.getUUID());
		if (session == null || session.token != token) return;
		session.input = Math.clamp(direction, -1, 1);
		session.sprinting = sprinting;
		session.sneaking = sneaking;
		session.lastInputTick = player.level().getGameTime();
	}

	@SubscribeEvent
	public static void beforeTick(PlayerTickEvent.Pre event) {
		if (event.getEntity() instanceof ServerPlayer player && isCrossing(player)) HeldTraveller.hold(player);
	}

	@SubscribeEvent
	public static void afterTick(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player) tick(player);
	}

	public static void tick(ServerPlayer player) {
		Session session = SESSIONS.get(player.getUUID());
		if (session == null) {
			LidlessPortal offered = OFFERS.remove(player.getUUID());
			if (offered != null) begin(player, offered);
			return;
		}
		if (!player.isAlive()) {
			end(player, false, "death");
			return;
		}
		if (player.serverLevel() != session.level || !session.portal.stillValid(session.level)) {
			end(player, true, "door_gone");
			return;
		}
		if (player.position().distanceToSqr(session.expected) > DRIFT_TOLERANCE_SQUARED) {
			end(player, true, "drift");
			return;
		}
		if (!mayCross(player)) {
			end(player, true, "ineligible");
			return;
		}
		int direction = player.level().getGameTime() - session.lastInputTick > INPUT_STALE_TICKS ? 0 : session.input;
		// Forward is where the player looks: facing back toward the entry, the forward key walks back.
		Vec3 passageForward = Vec3.atLowerCornerOf(session.farCell().subtract(session.anchor()));
		if (Vec3.directionFromRotation(0.0F, player.getYRot()).dot(passageForward) < 0) direction = -direction;
		if (direction != 0) {
			double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED) * SPEED_TO_BLOCKS_PER_TICK
					* (session.sprinting ? SPRINT : session.sneaking ? SNEAK : 1.0);
			if (direction < 0 && session.mode() == Mode.SEED) {
				speed *= 1.0 - SEED_RETREAT_LOSS * (session.progress / session.length);
			}
			session.progress = Math.clamp(session.progress + direction * speed, 0, session.length);
			if (direction < 0) session.movedBack = true;
		}
		if (session.progress >= session.length) {
			if (session.mode().leavesLevel()) {
				arrive(player, session);
				return;
			}
			if (exit(player, session, session.farCell(), session.mode() == Mode.SEED)) return;
		}
		if (session.progress <= 0 && session.movedBack && exit(player, session, session.entryCell(), true)) return;
		pin(player, session);
		sync(player, session, true, false);
	}

	/** Puts the player out standing in {@code cell}; false when the cell cannot take them and the crossing holds. */
	private static boolean exit(ServerPlayer player, Session session, BlockPos cell, boolean recover) {
		Vec3 landing = landing(player, session, cell, recover);
		if (landing == null) return false;
		boolean far = cell.equals(session.farCell());
		leave(player, session, landing, far);
		return true;
	}

	/**
	 * Where to put the player out at {@code cell}. A door's far-side exit takes only the cell itself (an unsafe far
	 * cell holds the crossing); the entry-side exit, a seed crossing's end and every recovery search around the cell,
	 * then around the other side, then around the world spawn, so a player is never left standing in the doorway to
	 * be taken again.
	 */
	private static Vec3 landing(ServerPlayer player, Session session, BlockPos cell, boolean searchAround) {
		Vec3 feet = Vec3.atBottomCenterOf(cell);
		if (StandingSpaces.safe(player, session.level, feet)) return feet;
		if (!searchAround) return null;
		Vec3 near = StandingSpaces.near(player, session.level, cell.below());
		if (near == null) near = StandingSpaces.near(player, session.level, session.anchor().relative(
				cell.equals(session.farCell()) ? session.facing().getOpposite() : session.facing()).below());
		if (near == null) {
			BlockPos spawn = session.level.getSharedSpawnPos();
			session.level.getChunkAt(spawn);
			near = StandingSpaces.near(player, session.level, spawn);
		}
		return near != null ? near : Vec3.atBottomCenterOf(session.anchor().above(2));
	}

	/** The far end of a crossing that leaves the level: the doorway takes the player from where they stand. */
	private static void arrive(ServerPlayer player, Session session) {
		SESSIONS.remove(player.getUUID());
		release(player, session.noGravityBefore);
		sync(player, session, false, true);
		threshold(session, player);
		session.portal.completed(player, session.towardFront);
	}

	private static void leave(ServerPlayer player, Session session, Vec3 landing, boolean reachedFar) {
		SESSIONS.remove(player.getUUID());
		release(player, session.noGravityBefore);
		player.teleportTo(landing.x, landing.y, landing.z);
		settle(player);
		if (reachedFar) session.portal.completed(player, session.towardFront);
		sync(player, session, false, true);
		threshold(session, player);
	}

	/**
	 * Ends the crossing: in place when {@code recover} is false (death, dimension change), else back on the entry
	 * side through the landing rules.
	 */
	public static void end(ServerPlayer player, boolean recover) {
		end(player, recover, "ended");
	}

	private static void end(ServerPlayer player, boolean recover, String reason) {
		Session session = SESSIONS.remove(player.getUUID());
		if (session == null) return;
		Hemomancy.LOGGER.debug("Lidless crossing of {} ends ({}) at {} (expected {}), progress {}", player.getScoreboardName(),
				reason, player.position(), session.expected, session.progress);
		release(player, session.noGravityBefore);
		if (recover && player.isAlive() && player.serverLevel() == session.level) {
			Vec3 landing = landing(player, session, session.entryCell(), true);
			player.teleportTo(landing.x, landing.y, landing.z);
		}
		// A player on their way out is about to be saved and removed; the chunk tracker no longer holds them.
		if (!"logout".equals(reason)) settle(player);
		sync(player, session, false, true);
	}

	/** Called by {@code TransientServerStateEvents} when the player logs out. */
	public static void logout(ServerPlayer player) {
		OFFERS.remove(player.getUUID());
		end(player, true, "logout");
	}

	/** Called by {@code TransientServerStateEvents} when a server starts and stops. */
	public static void clearSessionState() {
		SESSIONS.clear();
		OFFERS.clear();
	}

	/** The first pin is a teleport the client must accept; the per-tick re-pin only sets the position. */
	private static void pin(ServerPlayer player, Session session) {
		Vec3 center = Vec3.atBottomCenterOf(session.anchor());
		if (session.expected == Vec3.ZERO) player.teleportTo(center.x, center.y, center.z);
		else player.setPos(center);
		session.expected = player.position();
		settle(player);
		HeldTraveller.hold(player);
	}

	private static void release(ServerPlayer player, boolean noGravityBefore) {
		player.noPhysics = player.isSpectator();
		player.setNoGravity(noGravityBefore);
		player.setDeltaMovement(Vec3.ZERO);
		player.resetFallDistance();
	}

	private static void settle(ServerPlayer player) {
		if (player.connection == null) return;
		// The vanilla connection tick restores firstGood after PlayerTick.Post.
		player.connection.resetPosition();
		// Only a player the chunk map tracks can be moved through it; a captured test player never registered there.
		var chunkMap = player.serverLevel().getChunkSource().chunkMap;
		if (chunkMap.getPlayers(player.chunkPosition(), false).contains(player)) {
			player.serverLevel().getChunkSource().move(player);
		}
	}

	private static LidlessPassageStatePacket state(ServerPlayer player, Session session, boolean active) {
		return new LidlessPassageStatePacket(player.level().dimension().location(), player.getId(), session.token,
				active, session.anchor(), session.facing(), session.towardFront, session.mode(), session.theme,
				(float) session.progress, session.length);
	}

	private static void sync(ServerPlayer player, Session session, boolean active, boolean tracking) {
		LidlessPassageStatePacket packet = state(player, session, active);
		if (tracking) PacketHandler.sendTracking(player, packet);
		else PacketHandler.sendToPlayer(player, packet);
	}

	/** The sound and motes of a body passing the panel. */
	private static void threshold(Session session, ServerPlayer player) {
		Vec3 panel = Vec3.atCenterOf(session.anchor()).add(0, 0.5, 0);
		session.level.sendParticles(HemoParticleData.glow(MOTE), panel.x, panel.y, panel.z, 8, 0.25, 0.6, 0.25, 0);
		session.level.playSound(null, session.anchor(), SoundInit.ITEM_SYNAPTIC_STEP_USE.get(), SoundSource.BLOCKS, 0.7F, 0.6F);
	}

	@SubscribeEvent
	public static void incomingDamage(LivingIncomingDamageEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && isCrossing(player)
				&& !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void changeTarget(LivingChangeTargetEvent event) {
		if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer player && isCrossing(player))
			event.setNewAboutToBeSetTarget(null);
	}

	@SubscribeEvent
	public static void onTracking(PlayerEvent.StartTracking event) {
		if (event.getEntity() instanceof ServerPlayer observer && event.getTarget() instanceof ServerPlayer crosser) {
			Session session = SESSIONS.get(crosser.getUUID());
			if (session != null) PacketHandler.sendToPlayer(observer, state(crosser, session, true));
		}
	}

	@SubscribeEvent
	public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) end(player, false);
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) end(player, false);
	}

	private static final class Session {
		final ServerLevel level;
		final LidlessPortal portal;
		final boolean towardFront;
		final long token;
		final boolean noGravityBefore;
		final float length;
		final LidlessTheme theme;
		double progress;
		boolean movedBack;
		Vec3 expected = Vec3.ZERO;
		int input;
		boolean sprinting;
		boolean sneaking;
		long lastInputTick = Long.MIN_VALUE / 2;

		Session(ServerLevel level, LidlessPortal portal, boolean towardFront, long token, boolean noGravityBefore,
				int length, LidlessTheme theme) {
			this.level = level;
			this.portal = portal;
			this.towardFront = towardFront;
			this.token = token;
			this.noGravityBefore = noGravityBefore;
			this.length = length;
			this.theme = theme;
		}

		BlockPos anchor() {
			return portal.anchor();
		}

		Direction facing() {
			return portal.facing();
		}

		Mode mode() {
			return portal.mode();
		}

		BlockPos farCell() {
			return anchor().relative(towardFront ? facing() : facing().getOpposite());
		}

		BlockPos entryCell() {
			return anchor().relative(towardFront ? facing().getOpposite() : facing());
		}
	}
}

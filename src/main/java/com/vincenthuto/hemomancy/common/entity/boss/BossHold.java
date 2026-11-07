package com.vincenthuto.hemomancy.common.entity.boss;

import com.vincenthuto.hemomancy.common.util.HeldTraveller;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A boss holding one player against its body (Seraphae's Laying On of Hands), shaped on the Mycophant's hold: a point
 * in the body's frame, a slack before the server pins the player back to it, an optional input lock, a length, and a
 * break rule. The carrier syncs the held player, offset and slack ({@link Sync}); the client pins the local player
 * within the slack. A player held by one boss, travelling through a held crossing, spectating or in creative is
 * refused. The set of held players is session state ({@code TransientServerStateEvents}).
 */
public final class BossHold {
	/** offset is forward, up, right in the body's frame; slack in blocks before the server pins. */
	public record Spec(int maxTicks, Vec3 offset, double slack, boolean lockInput) {
	}

	public enum Reason { DONE, BROKEN, LOST, CANCELLED }

	@FunctionalInterface
	public interface Breaks {
		boolean breaks(ServerPlayer held, int heldTicks);
	}

	/** How the carrier syncs the hold to its clients. */
	public interface Sync {
		/** {@code playerId} -1 when released; a negative slack means input is not locked. */
		void hold(int playerId, Vec3 offset, float slack);
	}

	private static final Set<UUID> HELD = new HashSet<>();

	private final Entity body;
	private final Sync sync;
	private final Consumer<Reason> released;
	@Nullable
	private UUID held;
	@Nullable
	private Spec spec;
	@Nullable
	private Breaks breaks;
	private int heldTicks;

	public BossHold(Entity body, Sync sync, Consumer<Reason> released) {
		this.body = body;
		this.sync = sync;
		this.released = released;
	}

	/** Refuses a spectator, a creative player, a player already held, and a held traveller. */
	public boolean tryHold(ServerPlayer player, Spec spec, Breaks breaks) {
		if (holding() || refuses(player.isSpectator(), player.isCreative(), HELD.contains(player.getUUID()),
				HeldTraveller.isHeld(player))) return false;
		this.held = player.getUUID();
		this.spec = spec;
		this.breaks = breaks;
		this.heldTicks = 0;
		HELD.add(held);
		sync.hold(player.getId(), spec.offset(), spec.lockInput() ? (float) spec.slack() : -(float) Math.max(0.01D, spec.slack()));
		return true;
	}

	/** The pure refusal rule. */
	public static boolean refuses(boolean spectator, boolean creative, boolean alreadyHeld, boolean travelling) {
		return spectator || creative || alreadyHeld || travelling;
	}

	/** The pure end rule: LOST when the player is gone or dead, BROKEN by the break rule, DONE at maxTicks. */
	@Nullable
	public static Reason ends(boolean present, boolean broke, int heldTicks, int maxTicks) {
		if (!present) return Reason.LOST;
		if (broke) return Reason.BROKEN;
		return heldTicks >= maxTicks ? Reason.DONE : null;
	}

	/** True when the player has drifted past the slack and the server pins them back. */
	public static boolean pinned(Vec3 player, Vec3 point, double slack) {
		return player.distanceTo(point) > slack;
	}

	public void tick() {
		if (held == null || spec == null || !(body.level() instanceof ServerLevel level)) return;
		// the hold's own level: a held player must be here anyway, and it also finds a player added to the level alone
		// (a GameTest or review patient), which the server's player list never sees
		ServerPlayer player = level.getPlayerByUUID(held) instanceof ServerPlayer found ? found : null;
		boolean present = player != null && player.isAlive() && !player.isRemoved() && player.level() == level
				&& !body.isRemoved();
		heldTicks++;
		Reason end = ends(present, present && breaks != null && breaks.breaks(player, heldTicks), heldTicks,
				spec.maxTicks());
		if (end != null) {
			release(end);
			return;
		}
		Vec3 pin = point();
		if (pinned(player.position(), pin, spec.slack())) {
			player.teleportTo(pin.x, pin.y, pin.z);
			player.setDeltaMovement(Vec3.ZERO);
			player.hurtMarked = true;
		}
	}

	public void release(Reason reason) {
		if (held == null) return;
		HELD.remove(held);
		held = null;
		spec = null;
		breaks = null;
		heldTicks = 0;
		sync.hold(-1, Vec3.ZERO, -1.0F);
		released.accept(reason);
	}

	public boolean holding() {
		return held != null;
	}

	@Nullable
	public UUID held() {
		return held;
	}

	public int heldTicks() {
		return heldTicks;
	}

	public Vec3 point() {
		return point(body.position(), body.getYRot(), spec == null ? Vec3.ZERO : spec.offset());
	}

	/** The pin point: {@code offset} forward, up and right of {@code feet} facing {@code yawDegrees}. Shared with the client. */
	public static Vec3 point(Vec3 feet, float yawDegrees, Vec3 offset) {
		double yaw = Math.toRadians(yawDegrees);
		double fx = -Math.sin(yaw), fz = Math.cos(yaw);
		double rx = -fz, rz = fx;
		return new Vec3(feet.x + fx * offset.x + rx * offset.z, feet.y + offset.y, feet.z + fz * offset.x + rz * offset.z);
	}

	public static boolean isHeld(UUID player) {
		return HELD.contains(player);
	}

	public static void clearSessionState() {
		HELD.clear();
	}

	public static void logout(UUID player) {
		HELD.remove(player);
	}
}

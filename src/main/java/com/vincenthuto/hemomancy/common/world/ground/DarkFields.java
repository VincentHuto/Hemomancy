package com.vincenthuto.hemomancy.common.world.ground;

import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Fields of dark (core-loop round two P8): the light Blood Eclipse's Absorption takes hangs where it was aimed. Inside a
 * field every creature's sight is cut (Obscured, refreshed every half second; bosses see through it as they see through
 * Obscured), and its caster standing in it is hidden from a creature more than four blocks off, as a Veiled body is
 * ({@code SchoolAwareness.canPerceive}); a field hides no one else (CQ-41), and an Illuminated body is still seen. A
 * player inside sees it as fog. One field per
 * player, session state cleared through {@code TransientServerStateEvents}: it lasts at most two minutes and is not
 * saved, so a restart lets it lift. Numbers are {@link DarkFieldRules}.
 */
public final class DarkFields {
	/** How often a field does its work, in ticks. */
	public static final int WORK_INTERVAL = 10;
	/** How long a field's Obscured lasts on a creature, refreshed each work tick while it stays inside. */
	public static final int OBSCURED_TICKS = 30;
	/** How often a field's cue is resent, so a late arrival sees it. */
	public static final int RESEND_INTERVAL = 20;

	/** A player's field: where it hangs and the game time it lifts at. */
	public record Field(ResourceKey<Level> dimension, Vec3 centre, long until) {
		public long remaining(long now) {
			return Math.max(0L, until - now);
		}

		public double radius(long now) {
			return DarkFieldRules.radius(remaining(now));
		}
	}

	private static final Map<UUID, Field> FIELDS = new HashMap<>();

	/** A dome of dark a player carries (Blood Eclipse's held Projection): where they stand now, how wide, and until when. */
	public record Carried(ResourceKey<Level> dimension, Vec3 centre, double radius, long until) {
	}

	private static final Map<UUID, Carried> CARRIED = new HashMap<>();

	/**
	 * Keeps {@code player}'s carried dome up for {@code ticks} more, {@code radius} wide, where they stand now. It hides
	 * what stands in it exactly as a hung field does; the Sculpting that carries it lays the Obscured itself.
	 */
	public static void carry(ServerPlayer player, double radius, int ticks) {
		CARRIED.put(player.getUUID(), new Carried(player.level().dimension(), player.position().add(0.0D, 1.0D, 0.0D), radius,
				player.level().getGameTime() + ticks));
	}

	/** {@code player}'s carried dome is let go. */
	public static void drop(Player player) {
		CARRIED.remove(player.getUUID());
	}

	/** {@code player}'s carried dome, while it is up. */
	@Nullable
	public static Carried carried(Player player) {
		Carried dome = CARRIED.get(player.getUUID());
		return dome != null && dome.dimension() == player.level().dimension() && dome.until() > player.level().getGameTime()
				? dome : null;
	}

	private DarkFields() {
	}

	/**
	 * Feeds {@code ticks} of dark taken at {@code at} into {@code player}'s field: added to it when {@code at} lies
	 * inside it, otherwise a new field there (the old one lifts). Returns the field, or null when nothing was fed.
	 */
	@Nullable
	public static Field feed(ServerPlayer player, Vec3 at, int ticks) {
		if (ticks <= 0 || !(player.level() instanceof ServerLevel level)) return null;
		long now = level.getGameTime();
		Field old = live(player.getUUID(), level.dimension(), now);
		Field field;
		if (old != null && DarkFieldRules.joins(old.centre().distanceTo(at), old.radius(now))) {
			field = new Field(old.dimension(), old.centre(), now + DarkFieldRules.extended(old.remaining(now), ticks));
		} else {
			Field previous = FIELDS.get(player.getUUID());
			if (previous != null) lift(level.getServer() == null ? null : level.getServer().getLevel(previous.dimension()), previous);
			field = new Field(level.dimension(), at, now + DarkFieldRules.extended(0L, ticks));
		}
		FIELDS.put(player.getUUID(), field);
		show(level, field, now);
		return field;
	}

	/** {@code player}'s field, while it hangs. */
	@Nullable
	public static Field of(Player player) {
		return live(player.getUUID(), player.level().dimension(), player.level().getGameTime());
	}

	@Nullable
	private static Field live(UUID owner, ResourceKey<Level> dimension, long now) {
		Field field = FIELDS.get(owner);
		return field != null && field.dimension() == dimension && field.until() > now ? field : null;
	}

	/**
	 * Whether {@code body} is hidden by dark: inside any carried dome, or inside the hung field it cast itself. Server
	 * side only; asked every creature tick, so it returns at once while no field hangs.
	 */
	public static boolean covers(LivingEntity body) {
		if (FIELDS.isEmpty() && CARRIED.isEmpty() || body.level().isClientSide()) return false;
		long now = body.level().getGameTime();
		ResourceKey<Level> dimension = body.level().dimension();
		for (Carried dome : CARRIED.values()) {
			if (dome.dimension() != dimension || dome.until() <= now) continue;
			if (DarkFieldRules.inside(body.position().distanceTo(dome.centre()), dome.radius())) return true;
		}
		Field own = live(body.getUUID(), dimension, now);
		return own != null && DarkFieldRules.inside(body.position().distanceTo(own.centre()), own.radius(now));
	}

	/** A level's work tick: lift what has run out, dim the creatures inside the rest, and resend their cues. */
	public static void tick(ServerLevel level) {
		if (FIELDS.isEmpty()) return;
		long now = level.getGameTime();
		if (now % WORK_INTERVAL != 0) return;
		Iterator<Field> fields = FIELDS.values().iterator();
		while (fields.hasNext()) {
			Field field = fields.next();
			if (field.dimension() != level.dimension()) continue;
			if (field.until() <= now) {
				fields.remove();
				lift(level, field);
				continue;
			}
			double radius = field.radius(now);
			Vec3 centre = field.centre();
			for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(centre, centre).inflate(radius),
					mob -> mob.isAlive() && DarkFieldRules.inside(mob.position().distanceTo(centre), radius))) {
				SchoolStates.apply(null, mob, SchoolState.OBSCURED, OBSCURED_TICKS, 1);
			}
			if (now % RESEND_INTERVAL == 0) show(level, field, now);
		}
	}

	private static void show(ServerLevel level, Field field, long now) {
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.UMBRAL_FIELD, field.centre(), field.centre(), field.radius(now),
				(int) field.remaining(now));
	}

	/** A field lifts: its cue retires on every client that sees it. */
	private static void lift(@Nullable ServerLevel level, Field field) {
		if (level != null) ManipulationVisuals.burst(level, ManipulationVisuals.Form.UMBRAL_FIELD, field.centre(), field.centre(), 0.0D, 0);
	}

	// ── Session state ──

	public static void clearSessionState() {
		CARRIED.clear();
		FIELDS.clear();
	}

	public static void logout(Player player) {
		CARRIED.remove(player.getUUID());
		Field field = FIELDS.remove(player.getUUID());
		if (field != null && player.level() instanceof ServerLevel level && level.getServer() != null) {
			lift(level.getServer().getLevel(field.dimension()), field);
		}
	}
}

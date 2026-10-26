package com.vincenthuto.hemomancy.client.render.cinematic;

import java.util.List;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * A camera move around a boss: keys in scene ticks, each placing the camera at an angle around the boss, a distance, a
 * height, and the height on the boss it looks at. Between keys the camera eases; before the first and after the last
 * it holds. Angle 0 is the boss's facing when the client first had it ({@link #of}), or the player's side of the boss
 * ({@link #toward}), which suits a boss that has not turned to the player yet; 180 is the far side either way.
 */
public record CinematicShot(List<Key> keys, boolean fromViewer) {
	public record Key(float tick, float yawDeg, float distance, float height, float lookHeight) {
	}

	/** Keys laid out from the boss's own facing. */
	public static CinematicShot of(Key... keys) {
		return new CinematicShot(List.of(keys), false);
	}

	/** Keys laid out from the line between the boss and the player watching. */
	public static CinematicShot toward(Key... keys) {
		return new CinematicShot(List.of(keys), true);
	}

	/** The anchor yaw that puts angle 0 on {@code viewer}'s side of {@code boss}. */
	public static float yawToward(Vec3 boss, Vec3 viewer) {
		return (float) (Mth.atan2(-(viewer.x - boss.x), viewer.z - boss.z) * Mth.RAD_TO_DEG);
	}

	public Key at(float t) {
		Key first = keys.get(0);
		if (t <= first.tick()) return first;
		for (int i = 1; i < keys.size(); i++) {
			Key next = keys.get(i);
			if (t <= next.tick()) {
				Key prev = keys.get(i - 1);
				float x = HemoEasing.smoothstep(HemoEasing.clamp01((t - prev.tick()) / (next.tick() - prev.tick())));
				return new Key(t, Mth.lerp(x, prev.yawDeg(), next.yawDeg()), Mth.lerp(x, prev.distance(), next.distance()),
						Mth.lerp(x, prev.height(), next.height()), Mth.lerp(x, prev.lookHeight(), next.lookHeight()));
			}
		}
		return keys.get(keys.size() - 1);
	}

	/** The camera's position for a key around a boss at {@code boss} facing {@code bossYaw} (Minecraft yRot). */
	public static Vec3 position(Key key, Vec3 boss, float bossYaw) {
		float yaw = (bossYaw + key.yawDeg()) * Mth.DEG_TO_RAD;
		return new Vec3(boss.x - Mth.sin(yaw) * key.distance(), boss.y + key.height(), boss.z + Mth.cos(yaw) * key.distance());
	}

	/** Minecraft yaw and pitch (degrees) from {@code camera} looking at {@code target}. */
	public static float[] lookAngles(Vec3 camera, Vec3 target) {
		double dx = target.x - camera.x, dy = target.y - camera.y, dz = target.z - camera.z;
		double flat = Math.sqrt(dx * dx + dz * dz);
		float yaw = (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
		float pitch = (float) (-Mth.atan2(dy, flat) * Mth.RAD_TO_DEG);
		return new float[] { yaw, pitch };
	}
}

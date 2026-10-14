package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldMath;
import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The sound bed of the nearest deepening rite: a drone from its first step, the floor's heartbeat once the creep
 * comes, and wind late on. Strain lowers its pitch, still intervals silence it, and a collapse cuts it off.
 */
public final class RiteSoundBed {
	private static RiteLoopSound drone;
	private static RiteLoopSound storm;
	private static BlockPos following;

	private RiteSoundBed() {
	}

	public static void tick(Minecraft mc) {
		if (mc.level == null || mc.player == null) {
			stopAll();
			return;
		}
		if (mc.isPaused()) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		RiteWorldState.World world = nearest(RiteWorldState.worlds(), camera);
		if (world == null || !world.center().equals(following)) {
			if (drone != null) drone.fadeOut();
			if (storm != null) storm.fadeOut();
			drone = null;
			storm = null;
			following = world == null ? null : world.center();
			if (world == null) return;
		}
		BlockPos center = world.center();
		double x = center.getX() + 0.5D;
		double y = center.getY() + 1.5D;
		double z = center.getZ() + 0.5D;
		double dx = x - camera.x;
		double dz = z - camera.z;
		float weight = RiteWorldMath.viewerWeight(Math.sqrt(dx * dx + dz * dz), world.footprint());
		float ending = RiteWorldMath.soundEnding(world.ending(), world.endingAge());
		float level = world.level(1.0F);
		float pitch = RiteWorldMath.strainPitch(world.strain());
		drone = steer(mc, drone, SoundInit.RITE_AMBIENT_DRONE.get(),
				RiteWorldMath.droneVolume(level, weight, world.still()) * ending, pitch, x, y, z);
		storm = steer(mc, storm, SoundInit.RITE_AMBIENT_STORM.get(),
				RiteWorldMath.stormVolume(level, world.total(), weight, world.still()) * ending, pitch, x, y + 6.0D, z);
		float heart = RiteWorldMath.heartbeatVolume(level, world.total(), weight, world.still()) * ending;
		if (heart > 0.01F && mc.level.getGameTime() % beatPeriod(world.phase()) == 0) {
			mc.getSoundManager().play(heartbeat(SoundInit.RITE_AMBIENT_HEARTBEAT.get(), x, y, z, heart, pitch));
		}
	}

	public static void stopAll() {
		Minecraft mc = Minecraft.getInstance();
		if (drone != null) mc.getSoundManager().stop(drone);
		if (storm != null) mc.getSoundManager().stop(storm);
		drone = null;
		storm = null;
		following = null;
	}

	/** One heartbeat thump, positioned for panning but not cut off by distance: its volume carries the falloff. */
	static SimpleSoundInstance heartbeat(SoundEvent event, double x, double y, double z, float volume, float pitch) {
		return new SimpleSoundInstance(event.getLocation(), SoundSource.AMBIENT, volume, pitch,
				SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, x, y, z, false);
	}

	/** A loop starts again when it stopped itself or the sound engine dropped it (a reload or a lost device). */
	static boolean needsStart(RiteLoopSound sound, boolean playing) {
		return sound == null || sound.isStopped() || !playing;
	}

	/** The floor's strong beats come every 36, 24 or 18 ticks at rest, during a rite, and in its hardest phases. */
	static int beatPeriod(CardinalRitePhase phase) {
		int tempo = CardinalRiteFloorVisualRules.riteTempo(phase == null ? "" : phase.name());
		return tempo >= CardinalRiteFloorVisualRules.TEMPO_INTENSE ? 18
				: tempo == CardinalRiteFloorVisualRules.TEMPO_ACTIVE ? 24 : 36;
	}

	/** The nearest rite that has taken a step and is within earshot. */
	static RiteWorldState.World nearest(List<RiteWorldState.World> worlds, Vec3 camera) {
		RiteWorldState.World best = null;
		double bestDistance = Double.MAX_VALUE;
		for (RiteWorldState.World world : worlds) {
			if (world.stage() <= 0) continue;
			double dx = world.center().getX() + 0.5D - camera.x;
			double dz = world.center().getZ() + 0.5D - camera.z;
			double distance = Math.sqrt(dx * dx + dz * dz);
			if (RiteWorldMath.viewerWeight(distance, world.footprint()) <= 0.0F) continue;
			if (distance < bestDistance) {
				best = world;
				bestDistance = distance;
			}
		}
		return best;
	}

	private static RiteLoopSound steer(Minecraft mc, RiteLoopSound sound, SoundEvent event, float volume, float pitch,
			double x, double y, double z) {
		if (needsStart(sound, sound != null && mc.getSoundManager().isActive(sound))) {
			if (volume <= 0.01F) return null;
			sound = new RiteLoopSound(event, x, y, z);
			mc.getSoundManager().play(sound);
		}
		sound.steer(volume, pitch, x, y, z);
		return sound;
	}
}

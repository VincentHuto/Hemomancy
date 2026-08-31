package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.client.render.world.BloodBindingTendrilGeometry;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BloodBindingTendrilClientState {
	private static final Map<Long, Entry> ENTRIES = new HashMap<>();
	private static ClientLevel activeLevel;

	private BloodBindingTendrilClientState() {
	}

	public static void start(int casterId, int targetId, int durationTicks, long seed) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return;
		if (level != activeLevel) {
			ENTRIES.clear();
			activeLevel = level;
		}
		long now = level.getGameTime();
		ENTRIES.put(key(casterId, targetId),
				new Entry(casterId, targetId, seed, now, now + Math.max(1, durationTicks)));
	}

	public static void tick() {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null || level != activeLevel) {
			ENTRIES.clear();
			activeLevel = level;
		}
		if (level == null) return;

		long now = level.getGameTime();
		ENTRIES.values().removeIf(entry -> {
			Entity caster = level.getEntity(entry.casterId);
			if (!(caster instanceof LivingEntity livingCaster) || !livingCaster.isAlive()) return true;

			if (entry.retractionStart < 0L) {
				Entity entity = level.getEntity(entry.targetId);
				if (entity instanceof LivingEntity target) {
					entry.lastTargetFeet = target.position();
					entry.lastTargetHeight = target.getBbHeight();
					boolean bound = target.isAlive() && target.hasEffect(EffectInit.blood_binding);
					entry.updateRetraction(now, target.isAlive(), bound);
				} else if (entry.lastTargetFeet != null) {
					entry.updateRetraction(now, false, false);
				}
			}
			return entry.expired(now);
		});
	}

	public static List<Snapshot> snapshots(float partialTick) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null || ENTRIES.isEmpty()) return List.of();
		float now = level.getGameTime() + partialTick;
		List<Snapshot> snapshots = new ArrayList<>(ENTRIES.size());
		for (Entry entry : ENTRIES.values()) {
			Entity caster = level.getEntity(entry.casterId);
			if (caster == null || entry.lastTargetFeet == null) continue;
			Vec3 targetFeet = entry.lastTargetFeet;
			if (entry.retractionStart < 0L) {
				Entity target = level.getEntity(entry.targetId);
				if (target != null) targetFeet = target.getPosition(partialTick);
			}
			float retractionTicks = entry.retractionStart < 0L ? 0.0F : now - entry.retractionStart;
			snapshots.add(new Snapshot(caster.getPosition(partialTick), targetFeet,
					entry.lastTargetHeight, entry.seed, now - entry.startTick, retractionTicks));
		}
		return List.copyOf(snapshots);
	}

	private static long key(int casterId, int targetId) {
		return ((long) casterId << 32) ^ (targetId & 0xffffffffL);
	}

	static final class Entry {
		private final int casterId;
		private final int targetId;
		private final long seed;
		private final long startTick;
		private final long activeUntil;
		private Vec3 lastTargetFeet;
		private double lastTargetHeight = 1.8D;
		private long retractionStart = -1L;
		private boolean bindingObserved;

		Entry(int casterId, int targetId, long seed, long startTick, long activeUntil) {
			this.casterId = casterId;
			this.targetId = targetId;
			this.seed = seed;
			this.startTick = startTick;
			this.activeUntil = activeUntil;
		}

		boolean updateRetraction(long now, boolean targetAlive, boolean bound) {
			if (bound) bindingObserved = true;
			if (retractionStart < 0L
					&& (!targetAlive || (!bound && (bindingObserved || now >= activeUntil)))) {
				retractionStart = now;
			}
			return retractionStart >= 0L;
		}

		boolean expired(long now) {
			return retractionStart >= 0L
					&& now - retractionStart >= BloodBindingTendrilGeometry.RETRACTION_TICKS;
		}
	}

	public record Snapshot(Vec3 casterFeet, Vec3 targetFeet, double targetHeight,
			long seed, float ageTicks, float retractionTicks) {
	}
}

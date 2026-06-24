package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSanguineFormationProjection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ActiveSanguineFormationProjectionClientData {
	private static final Map<String, ProjectionEntry> ACTIVE_PROJECTIONS = new LinkedHashMap<>();

	private ActiveSanguineFormationProjectionClientData() {
	}

	public static void upsert(BlockPos pos, Direction face, float progress, int visibleTicks,
			PacketSanguineFormationProjection.State state, boolean attuned, long seed) {
		String key = key(pos, face, seed);
		if (state == PacketSanguineFormationProjection.State.COMPLETE
				|| state == PacketSanguineFormationProjection.State.FAILURE) {
			ACTIVE_PROJECTIONS.remove(key);
			return;
		}
		float clampedProgress = Math.max(0.0F, Math.min(1.0F, progress));
		int clampedVisibleTicks = Math.max(1, visibleTicks);
		ProjectionEntry existing = ACTIVE_PROJECTIONS.get(key);
		if (existing != null) {
			existing.refresh(clampedProgress, clampedVisibleTicks, state, attuned);
			return;
		}
		ACTIVE_PROJECTIONS.put(key, new ProjectionEntry(pos.immutable(), face, clampedProgress,
				clampedVisibleTicks, state, attuned, seed));
	}

	public static List<ProjectionEntry> getActiveProjections() {
		return new ArrayList<>(ACTIVE_PROJECTIONS.values());
	}

	public static void tick() {
		Iterator<ProjectionEntry> iterator = ACTIVE_PROJECTIONS.values().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().tick()) {
				iterator.remove();
			}
		}
	}

	private static String key(BlockPos pos, Direction face, long seed) {
		return pos.asLong() + ":" + face.getName() + ":" + seed;
	}

	public static final class ProjectionEntry {
		private final BlockPos pos;
		private final Direction face;
		private final long seed;
		private float progress;
		private int initialVisibleTicks;
		private PacketSanguineFormationProjection.State state;
		private boolean attuned;
		private int remainingTicks;
		private int ageTicks;
		private float visibility;
		private boolean refreshedThisTick;

		private ProjectionEntry(BlockPos pos, Direction face, float progress, int visibleTicks,
				PacketSanguineFormationProjection.State state, boolean attuned, long seed) {
			this.pos = pos;
			this.face = face;
			this.progress = progress;
			this.initialVisibleTicks = visibleTicks;
			this.remainingTicks = visibleTicks;
			this.state = state;
			this.attuned = attuned;
			this.seed = seed;
			this.visibility = state == PacketSanguineFormationProjection.State.FORMING ? 0.35F : 1.0F;
			this.refreshedThisTick = true;
		}

		private void refresh(float progress, int visibleTicks, PacketSanguineFormationProjection.State state,
				boolean attuned) {
			this.progress = progress;
			this.initialVisibleTicks = visibleTicks;
			this.remainingTicks = visibleTicks;
			this.state = state;
			this.attuned = attuned;
			this.refreshedThisTick = true;
			if (state != PacketSanguineFormationProjection.State.FORMING) {
				this.visibility = 1.0F;
			}
		}

		public BlockPos getPos() {
			return pos;
		}

		public Direction getFace() {
			return face;
		}

		public float getProgress() {
			return progress;
		}

		public PacketSanguineFormationProjection.State getState() {
			return state;
		}

		public boolean isAttuned() {
			return attuned;
		}

		public long getSeed() {
			return seed;
		}

		public float getAgeProgress(float partialTick) {
			return Math.min(1.0F, (ageTicks + partialTick) / 10.0F);
		}

		public float getVisibility(float partialTick) {
			if (state != PacketSanguineFormationProjection.State.FORMING) {
				return 1.0F - getFadeProgress(partialTick);
			}
			return Math.max(0.0F, Math.min(1.0F, visibility));
		}

		public float getFadeProgress(float partialTick) {
			if (state == PacketSanguineFormationProjection.State.FORMING) {
				return 0.0F;
			}
			float elapsed = initialVisibleTicks - remainingTicks + partialTick;
			return Math.max(0.0F, Math.min(1.0F, elapsed / initialVisibleTicks));
		}

		private boolean tick() {
			ageTicks++;
			if (state == PacketSanguineFormationProjection.State.FORMING) {
				if (refreshedThisTick) {
					visibility = Math.min(1.0F, visibility + 0.25F);
				} else {
					visibility = Math.max(0.0F, visibility - 0.34F);
				}
				refreshedThisTick = false;
			}
			remainingTicks--;
			return remainingTicks <= 0 || (state == PacketSanguineFormationProjection.State.FORMING
					&& visibility <= 0.01F);
		}
	}
}

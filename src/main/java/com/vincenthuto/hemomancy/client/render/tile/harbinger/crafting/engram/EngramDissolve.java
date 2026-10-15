package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram;

import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.SomaticLoomEffectRules;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * A weave broken before its climax (cancelled at the loom, or abandoned) leaves its engram to come apart where it lay
 * rather than vanish: each frame a live engram is drawn its state is kept, and once the loom stops weaving without
 * having reached its climax, the engram, its glyphs, the echo and any dome are drawn from that last state while they
 * dissolve over {@link #TICKS}. A weave woven home has already drawn back into the loom at its climax, so it leaves
 * nothing to dissolve.
 */
public final class EngramDissolve {
	public static final int TICKS = 50;
	/** What the engram looked like the last frame it was alive. */
	public static final class Snapshot {
		private EngramBoard board;
		private float unfurl;
		private boolean climax;
		private long seen;
		private long start = -1;
		private long lastTick = -1;
		@Nullable private BloodManipulation manip;
		@Nullable private ResourceLocation theme;
		private float domeRadius;

		public EngramBoard board() { return board; }
		public float unfurl() { return unfurl; }
		@Nullable public BloodManipulation manip() { return manip; }
		@Nullable public ResourceLocation theme() { return theme; }
		public float domeRadius() { return domeRadius; }
	}

	private static final Map<BlockPos, Snapshot> SNAPSHOTS = new HashMap<>();

	private EngramDissolve() {
	}

	/**
	 * Each frame the loom is drawn: keeps a live engram's state, and returns how far a broken one has dissolved
	 * (0 to 1), or -1 when there is nothing dissolving here.
	 */
	public static float update(SomaticLoomBlockEntity te, float partialTick, float domeRadius) {
		Level level = te.getLevel();
		if (level == null) return -1F;
		long now = level.getGameTime();
		BlockPos pos = te.getBlockPos();
		if (te.isWeaving() && te.getEngram() != null) {
			Snapshot s = SNAPSHOTS.computeIfAbsent(pos.immutable(), p -> new Snapshot());
			s.board = te.getEngram();
			s.unfurl = EngramSigilRenderer.unfurl(te, partialTick);
			s.climax = te.isInClimax();
			s.seen = now;
			s.start = -1;
			s.manip = te.getWeaveManipulation();
			s.theme = te.getWeaverTheme();
			s.domeRadius = domeRadius;
			return -1F;
		}
		Snapshot s = SNAPSHOTS.get(pos);
		if (s == null) return -1F;
		if (s.start < 0) {
			if (!SomaticLoomEffectRules.engramDissolves(s.climax, now - s.seen)) {
				SNAPSHOTS.remove(pos);
				return -1F;
			}
			s.start = now;
		}
		float progress = (now - s.start + partialTick) / TICKS;
		if (progress >= 1F) {
			SNAPSHOTS.remove(pos);
			return -1F;
		}
		if (s.lastTick != now) {
			// Once a tick, not once a frame: the engram sheds what it is made of as it comes apart.
			EngramBeatEffects.dissolving(te, s.board, progress, s.lastTick < 0);
			s.lastTick = now;
		}
		return progress;
	}

	/** How far this loom's broken engram has dissolved (0 to 1), or -1; read-only, for renderers drawn elsewhere. */
	public static float progress(BlockPos pos, Level level, float partialTick) {
		Snapshot s = SNAPSHOTS.get(pos);
		if (s == null || s.start < 0) return -1F;
		return Math.min(1F, (level.getGameTime() - s.start + partialTick) / TICKS);
	}

	@Nullable
	public static Snapshot snapshot(BlockPos pos) {
		return SNAPSHOTS.get(pos);
	}

	public static void clear() {
		SNAPSHOTS.clear();
	}
}

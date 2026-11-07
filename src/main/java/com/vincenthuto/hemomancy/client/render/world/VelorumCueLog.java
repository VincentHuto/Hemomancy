package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEffects;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * The Velorum beats his synced state cannot show and his pass draws in its own materials (revision 2026-11-06): a stain,
 * a pulse mark and its beats, a figure's veins, the Shadow Step's two ends, the Eclipse's light going out and coming back,
 * a shape's reach. Each arrives as a {@code VELORUM_RIME} cue; {@link VelorumRimeGeometry#draw} hands it here the first
 * frame it is drawn, with the time it was born, and {@link VelorumTelegraphPass} reads the live ones every frame in every
 * phase. The same packet born at the same tick is one entry; a stain re-sent at the same place is a new one. Times are
 * the visuals' frame time.
 */
final class VelorumCueLog {
	private static final int MAX = 256;
	private static final List<Entry> LIVE = new ArrayList<>();
	private static Object world;

	/** One logged beat: its packet, its beat and the entity it is about (or -1), and when it was born. */
	record Entry(ManipulationVisualPacket packet, int kind, int entity, double born) {
		float age(double now) {
			return (float) (now - born);
		}

		boolean live(double now) {
			return now - born < packet.ticks();
		}
	}

	private VelorumCueLog() {
	}

	/** Whether the pass draws this beat (and so the rime geometry should log it rather than draw it). */
	static boolean logs(int kind) {
		return kind == VelorumEffects.THAW || kind >= VelorumEffects.STAIN;
	}

	/** Logs a cue the first time it is drawn, {@code age} ticks into its life at frame time {@code time}. */
	static void note(ManipulationVisualPacket packet, double time, float age) {
		double born = time - age;
		for (Entry entry : LIVE) if (Math.abs(entry.born() - born) < 1.5D && entry.packet().equals(packet)) return;
		if (LIVE.size() >= MAX) LIVE.remove(0);
		int count = packet.count();
		LIVE.add(new Entry(packet, VelorumVisualRules.kind(count), VelorumVisualRules.entity(count), born));
	}

	/** Forgets everything when the world changes, and anything past its life. */
	static void begin(Object level, double now) {
		if (level != world) {
			LIVE.clear();
			world = level;
		}
		LIVE.removeIf(entry -> now - entry.born() > entry.packet().ticks() + 2 || now < entry.born() - 2.0D);
	}

	/** The live beats of one kind, oldest first. */
	static List<Entry> of(int kind, double now) {
		List<Entry> out = new ArrayList<>();
		for (Entry entry : LIVE) if (entry.kind() == kind && entry.live(now)) out.add(entry);
		return out;
	}

	/** The newest live beat of a kind about one entity, or null. */
	static Entry latest(int kind, int entity, double now) {
		Entry best = null;
		for (Entry entry : LIVE) {
			if (entry.kind() != kind || entry.entity() != entity || !entry.live(now)) continue;
			if (best == null || entry.born() > best.born()) best = entry;
		}
		return best;
	}

	/** The newest beat of a kind about one entity born after {@code after}, live or not yet pruned, or null. */
	static Entry since(int kind, int entity, double after) {
		Entry best = null;
		for (Entry entry : LIVE) {
			if (entry.kind() != kind || entry.entity() != entity || entry.born() < after) continue;
			if (best == null || entry.born() > best.born()) best = entry;
		}
		return best;
	}

	static void clear() {
		LIVE.clear();
	}
}

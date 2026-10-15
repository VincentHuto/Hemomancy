package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram;

import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * What this client's own staff is doing to an engram this frame: where it points (the exact point and the node it
 * snaps to, and whether threading the held lane there would be taken), and which fragment its gaze is resting on
 * long enough to be heard. Kept for the one local player only.
 */
public final class EngramClientState {
	/** The local aim: loom, node (or -1), sigil-space point, and the rule's answer for threading the held lane there. */
	public record Aim(BlockPos loom, int node, double sx, double sz, EngramWeaveRules.Step step) {}

	/** A fragment heard: which loom, which fragment, and when the hearing began. */
	public record Listen(BlockPos loom, int fragment, float since) {}

	private static Aim aim;
	private static Listen listen;
	/** Fragments whose hearing is due to play on the echo; the loom's renderer takes them. */
	private static final List<Listen> HEARD = new ArrayList<>();

	private EngramClientState() {
	}

	public static Aim aim() { return aim; }
	public static void setAim(Aim next) { aim = next; }

	public static Listen listen() { return listen; }

	/** The gaze rests on a fragment; once it has rested `LISTEN_TICKS`, the fragment's beat plays once. */
	public static void gaze(BlockPos loom, int fragment, float now) {
		if (fragment < 0) {
			listen = null;
			return;
		}
		if (listen == null || !listen.loom().equals(loom) || listen.fragment() != fragment) {
			listen = new Listen(loom.immutable(), fragment, now);
			return;
		}
		if (now - listen.since() >= EngramWeaveRules.LISTEN_TICKS && !Float.isNaN(listen.since())) {
			HEARD.add(listen);
			// Heard once per resting gaze: NaN marks it played until the gaze moves on.
			listen = new Listen(listen.loom(), listen.fragment(), Float.NaN);
		}
	}

	/** How strongly a loom is listening now (0 to 1), for the feelers and the heard glyph. */
	public static float listening(BlockPos loom, int fragment) {
		return listen != null && listen.loom().equals(loom) && (fragment < 0 || listen.fragment() == fragment) ? 1f : 0f;
	}

	/** Hearings due on this loom, taken once. */
	public static List<Listen> takeHeard(BlockPos loom) {
		List<Listen> out = new ArrayList<>();
		HEARD.removeIf(heard -> {
			if (!heard.loom().equals(loom)) return false;
			out.add(heard);
			return true;
		});
		return out;
	}

	public static void clear() {
		aim = null;
		listen = null;
		HEARD.clear();
	}

	/** Leaving a world: every engram this client was drawing, aiming at or tracing is forgotten. */
	public static void clearAll() {
		clear();
		EngramSigilRenderer.clear();
		EngramGlyphs.clear();
		EngramDissolve.clear();
		com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.ActiveEngramLooms.clearClient();
		com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramTracing.clearClient();
	}
}

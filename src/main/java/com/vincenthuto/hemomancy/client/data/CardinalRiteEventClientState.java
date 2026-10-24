package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldState;
import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RingTear;
import com.vincenthuto.hemomancy.common.rite.RingOpeningMath;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.CardinalRiteEventPacket;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client record of recent Cardinal Rite beats. Renderers ask how long ago a beat
 * happened at a rite centre and shape their effect from that age. Terminal beats
 * keep a {@link Ghost} of the rite's last snapshot so its ending can play out after
 * the server stops syncing it.
 */
public final class CardinalRiteEventClientState {
	/** Beats older than this are forgotten. */
	public static final int EVENT_LIFETIME_TICKS = 200;
	/** How long a finished rite's ghost lingers for its ending. */
	public static final int GHOST_LIFETIME_TICKS = 80;

	private static final List<RiteEvent> EVENTS = new ArrayList<>();
	private static final List<Ghost> GHOSTS = new ArrayList<>();
	private static long clientTicks;

	public record RiteEvent(BlockPos center, CardinalRiteEventKind kind, int index, String style,
			List<Vec3> points, ItemStack item, long receivedTick) {
		public float age(float partialTick) {
			return clientTicks - receivedTick + partialTick;
		}
	}

	public record Ghost(ActiveRiteData.RiteEntry entry, CardinalRiteEventKind kind, String style,
			long startTick) {
		public float age(float partialTick) {
			return clientTicks - startTick + partialTick;
		}

		public BlockPos center() {
			return entry.getCenter();
		}
	}

	private CardinalRiteEventClientState() {
	}

	public static void accept(CardinalRiteEventPacket packet) {
		RiteEvent event = new RiteEvent(packet.center(), packet.kind(), packet.index(), packet.style(),
				packet.points(), packet.item(), clientTicks);
		EVENTS.add(event);
		if (packet.kind() == CardinalRiteEventKind.RING_OPENED) {
			// The world surges with the ring; deeper rings surge it further.
			RiteWorldState.ringOpened(packet.center(), RingOpeningMath.fogSurge(
					RingOpeningMath.depth(packet.index(), RingTear.ringsOf(packet.style()))));
		}
		if (packet.kind().terminal()) {
			ActiveRiteData.RiteEntry ended = null;
			for (ActiveRiteData.RiteEntry entry : ActiveRiteClientData.getActiveRites()) {
				if (entry.getCenter().equals(packet.center())) {
					ended = entry;
					break;
				}
			}
			// The snapshot that drops a rite can arrive before its ending beat.
			if (ended == null) ended = ActiveRiteClientData.recentlyEnded(packet.center());
			if (ended != null) {
				GHOSTS.removeIf(ghost -> ghost.center().equals(packet.center()));
				GHOSTS.add(new Ghost(ended, packet.kind(), packet.style(), clientTicks));
			}
		}
	}

	public static void tick() {
		var minecraft = net.minecraft.client.Minecraft.getInstance();
		// Beats age with the world: a paused game must not skip a rite's ending.
		if (minecraft.level == null || minecraft.isPaused()) return;
		clientTicks++;
		EVENTS.removeIf(event -> clientTicks - event.receivedTick() > EVENT_LIFETIME_TICKS);
		GHOSTS.removeIf(ghost -> clientTicks - ghost.startTick() > GHOST_LIFETIME_TICKS);
	}

	public static void clear() {
		EVENTS.clear();
		GHOSTS.clear();
	}

	/** Beats at a rite centre, oldest first. */
	public static List<RiteEvent> recent(BlockPos center) {
		List<RiteEvent> matches = new ArrayList<>();
		for (RiteEvent event : EVENTS) {
			if (event.center().equals(center)) matches.add(event);
		}
		return matches;
	}

	/** The newest beat of a kind at a rite centre, or {@code null}. */
	public static RiteEvent latest(BlockPos center, CardinalRiteEventKind kind) {
		for (int i = EVENTS.size() - 1; i >= 0; i--) {
			RiteEvent event = EVENTS.get(i);
			if (event.kind() == kind && event.center().equals(center)) return event;
		}
		return null;
	}

	/** Age in ticks of the newest beat of a kind at a centre, or -1 when none is remembered. */
	public static float latestAge(BlockPos center, CardinalRiteEventKind kind, float partialTick) {
		RiteEvent event = latest(center, kind);
		return event == null ? -1.0F : event.age(partialTick);
	}

	public static List<RiteEvent> all() {
		return Collections.unmodifiableList(EVENTS);
	}

	public static List<Ghost> ghosts() {
		return Collections.unmodifiableList(GHOSTS);
	}

	/** The ghost of a rite that just ended at this centre, or {@code null}. */
	public static Ghost ghostAt(BlockPos center) {
		for (Ghost ghost : GHOSTS) {
			if (ghost.center().equals(center)) return ghost;
		}
		return null;
	}
}

package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.network.heartnode.HeartNodeEnvelopePacket;
import com.vincenthuto.hemomancy.common.world.HeartNodeFootprint;
import com.vincenthuto.hemomancy.common.world.HeartNodeRelation;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The heart-node sanctums the server has told this client about: those near the player in the Gardens, each with how
 * the player stands to it. They belong to the level they were sent in, and go on logout or a change of level. A
 * sanctum whose node withered is kept a little longer, apart from the live ones, so its spread can recede.
 */
public final class HeartNodeClientState {
	private static final List<Envelope> ENVELOPES = new ArrayList<>();
	private static final List<Withering> WITHERING = new ArrayList<>();
	/** Sanctums the last packets dropped, kept briefly in case the news of their withering comes after. */
	private static final List<Withering> DROPPED = new ArrayList<>();
	private static final int DROPPED_KEEP_TICKS = 40;
	@Nullable
	private static ClientLevel world;

	private HeartNodeClientState() {}

	/** One sanctum as the client holds it, with the footprint geometry rebuilt for drawing and lookups. */
	public record Envelope(HeartNodeFootprint footprint, HeartNodeSettings settings, HeartNodeRelation relation,
			long plantedAt, int stage) {
		public boolean owned() {
			return relation == HeartNodeRelation.OWNER;
		}
	}

	/** A sanctum whose node withered at game time {@code since}, as it last stood. */
	public record Withering(Envelope envelope, long since) {
		public BlockPos heart() {
			return envelope.footprint().heart();
		}
	}

	public static void accept(List<HeartNodeEnvelopePacket.Envelope> envelopes) {
		checkLevel();
		ClientLevel level = Minecraft.getInstance().level;
		long now = level == null ? 0L : level.getGameTime();
		for (Envelope old : ENVELOPES) {
			boolean kept = envelopes.stream().anyMatch(next -> next.heart().equals(old.footprint().heart()));
			if (!kept) DROPPED.add(new Withering(old, now));
		}
		ENVELOPES.clear();
		world = level;
		for (HeartNodeEnvelopePacket.Envelope envelope : envelopes) {
			ENVELOPES.add(new Envelope(new HeartNodeFootprint(envelope.owner(), envelope.heart(), envelope.synapses()),
					new HeartNodeSettings(envelope.settingsBits(), envelope.intensity()), envelope.relation(),
					envelope.plantedAt(), envelope.stage()));
		}
	}

	public static List<Envelope> envelopes() {
		checkLevel();
		return List.copyOf(ENVELOPES);
	}

	/** The player's own sanctum, if it is near. */
	public static Optional<Envelope> own() {
		return envelopes().stream().filter(Envelope::owned).findFirst();
	}

	/** The sanctum {@code pos} lies in, if any is known. */
	public static Optional<Envelope> at(BlockPos pos) {
		return envelopes().stream().filter(envelope -> envelope.footprint().contains(pos)).findFirst();
	}

	/**
	 * The node at {@code heart} withered at {@code now}: its sanctum, live or just dropped, starts receding. A sanctum
	 * already withering, or one this client never knew, is left as it is.
	 */
	public static void wither(BlockPos heart, long now) {
		checkLevel();
		if (WITHERING.stream().anyMatch(withering -> withering.heart().equals(heart))) return;
		Envelope envelope = ENVELOPES.stream().filter(live -> heart.equals(live.footprint().heart())).findFirst()
				.orElseGet(() -> DROPPED.stream().map(Withering::envelope)
						.filter(gone -> heart.equals(gone.footprint().heart())).findFirst().orElse(null));
		if (envelope != null) WITHERING.add(new Withering(envelope, now));
	}

	/** The sanctums still receding at {@code now}, each for {@code ticks} after its node withered. */
	public static List<Withering> withering(long now, int ticks) {
		checkLevel();
		DROPPED.removeIf(gone -> now - gone.since() > DROPPED_KEEP_TICKS || gone.since() > now);
		if (WITHERING.isEmpty()) return List.of();
		WITHERING.removeIf(withering -> now - withering.since() >= ticks || withering.since() > now);
		return List.copyOf(WITHERING);
	}

	public static void clear() {
		ENVELOPES.clear();
		WITHERING.clear();
		DROPPED.clear();
		world = null;
	}

	private static void checkLevel() {
		if (world != Minecraft.getInstance().level) clear();
	}
}

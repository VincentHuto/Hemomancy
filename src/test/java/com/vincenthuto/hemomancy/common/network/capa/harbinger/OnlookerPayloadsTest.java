package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.tile.ReservoirReadout;
import com.vincenthuto.hemomancy.support.EnglishText;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round two P11's payloads (protocol 49): the Tendon Line's anchor now names its owner so trackers can draw it, an
 * onlooker's verb presence, and the owner's refusal mark. Each survives the wire whole.
 */
class OnlookerPayloadsTest {
	private static <T> T roundTrip(StreamCodec<FriendlyByteBuf, T> codec, T packet) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		codec.encode(buf, packet);
		T decoded = codec.decode(buf);
		assertEquals(0, buf.readableBytes(), "every byte read");
		return decoded;
	}

	@Test
	void aRootedAnchorKeepsItsOwnerFaceAndBlock() {
		PacketSyncTendonAnchor rooted = new PacketSyncTendonAnchor(4127, true, new BlockPos(-31, 64, 900), Direction.WEST);
		assertEquals(rooted, roundTrip(PacketSyncTendonAnchor.STREAM_CODEC, rooted));
	}

	@Test
	void aLooseAnchorStillNamesItsOwner() {
		PacketSyncTendonAnchor decoded = roundTrip(PacketSyncTendonAnchor.STREAM_CODEC, PacketSyncTendonAnchor.loose(88));
		assertFalse(decoded.set());
		assertEquals(88, decoded.entityId());
	}

	@Test
	void verbPresenceCarriesBothReachesTheVerbAndTheSelection() {
		PacketSyncVerbPresence presence = new PacketSyncVerbPresence(12, 13.5F, 13.5F, Verb.ABSORB, "lignum_mortis");
		assertEquals(presence, roundTrip(PacketSyncVerbPresence.STREAM_CODEC, presence));
		PacketSyncVerbPresence none = new PacketSyncVerbPresence(3, 5.0F, 5.0F, Verb.PROJECT, "");
		assertEquals(none, roundTrip(PacketSyncVerbPresence.STREAM_CODEC, none));
	}

	@Test
	void aRefusalCarriesItsTicks() {
		assertEquals(new VerbRefusalPacket(24), roundTrip(VerbRefusalPacket.STREAM_CODEC, new VerbRefusalPacket(24)));
	}

	@Test
	void everyStalledReasonReadsAsStalledAndKeepsItsStoredId() {
		for (ReservoirReadout.Link link : ReservoirReadout.Link.values()) {
			assertEquals(link, ReservoirReadout.Link.byId(link.ordinal()));
			// CN-9: kin standing in for an offline owner draws, like the owner.
			boolean drawing = link == ReservoirReadout.Link.DRAWING || link == ReservoirReadout.Link.KIN_DRAWING;
			boolean stalled = link != ReservoirReadout.Link.NONE && !drawing;
			assertEquals(stalled, link.stalled(), link.name());
			assertEquals(drawing, link.drawing(), link.name());
			assertEquals(stalled ? ReservoirReadout.Feed.STALLED
					: drawing ? ReservoirReadout.Feed.DRAWING : ReservoirReadout.Feed.NONE,
					ReservoirReadout.Feed.of(link));
		}
		assertEquals(ReservoirReadout.Link.NONE, ReservoirReadout.Link.byId(-1));
		assertEquals(ReservoirReadout.Link.NONE, ReservoirReadout.Link.byId(99));
		assertTrue(ReservoirReadout.Link.OUT_OF_REACH.key().equals("out_of_reach"));
		assertEquals(ReservoirReadout.Link.values().length - 1, ReservoirReadout.Link.KIN_DRAWING.ordinal(),
				"a new link state is appended, since the tank stores the ordinal");
		assertTrue(EnglishText.of("message.hemomancy.living_blood.feed.drawing.kin").contains("kin"));
		assertTrue(EnglishText.of("screen.hemomancy.blood_volume_bar_widget.drawing.kin").contains("kin"));
	}

	@Test
	void everyStalledReasonIsWordedOnTheScreenAndTheGauge() {
		for (ReservoirReadout.Link link : ReservoirReadout.Link.values()) {
			if (!link.stalled()) continue;
			assertTrue(EnglishText.of("screen.hemomancy.blood_volume_bar_widget.stalled." + link.key()).startsWith("Sutured, but stalled"));
			assertTrue(EnglishText.of("message.hemomancy.living_blood.feed.stalled." + link.key()).startsWith("Sutured, but stalled"));
		}
		assertEquals("Sutured, but stalled: its owner is below the third Degree.",
				EnglishText.of("screen.hemomancy.blood_volume_bar_widget.stalled.below_degree"));
		assertEquals("Refused", EnglishText.of("hud.hemomancy.verb_channel.refused"));
	}
}

package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PacketSyncActiveRitesEscalationTest {
	static ActiveRiteData.RiteEntry entry() {
		return new ActiveRiteData.RiteEntry(new BlockPos(4, 64, -9), 5, 0.5D,
				ResourceLocation.parse("hemomancy:cardinal_rite/escalation_test"), false,
				"ORDEAL", 0, 0, 1, 1, 2, 0, 0, 0, 0, 0, "",
				List.of(), List.of(), List.of());
	}

	static ActiveRiteData.RiteEntry roundTrip(ActiveRiteData.RiteEntry entry) {
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
		PacketSyncActiveRites.encode(buffer, new PacketSyncActiveRites(List.of(entry)));
		ActiveRiteData.RiteEntry copy = PacketSyncActiveRites.decode(buffer).entries().getFirst();
		assertEquals(0, buffer.readableBytes(), "the decoder reads every byte the encoder wrote");
		return copy;
	}

	@Test
	void escalationRoundTrips() {
		ActiveRiteData.RiteEntry copy = roundTrip(entry().withEscalation(3, 6));

		assertEquals(3, copy.getEscalation());
		assertEquals(6, copy.getEscalationTotal());
	}

	@Test
	void problemsAndSocketMarkersRoundTrip() {
		ActiveRiteData.RiteEntry entry = entry().withEscalation(1, 4).withGuidance(
				List.of(new com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.RiteProblem(
						com.vincenthuto.hemomancy.common.rite.CardinalRiteProblems.ALLIES, List.of("3", "1"))),
				List.of(new ActiveRiteData.SocketMarker(new BlockPos(10, 65, -9), "bastion")));

		ActiveRiteData.RiteEntry copy = roundTrip(entry);

		assertEquals(entry.getProblems(), copy.getProblems());
		assertEquals(entry.getSocketMarkers(), copy.getSocketMarkers());
		assertEquals(1, copy.getEscalation());
	}

	@Test
	void stageIsClampedToItsTotal() {
		ActiveRiteData.RiteEntry entry = entry().withEscalation(9, 4);

		assertEquals(4, entry.getEscalation());
		assertEquals(0, entry().withEscalation(-2, 4).getEscalation());
	}
	@Test
	void ringsRoundTripAndUnsyncedEntriesStayFlat() {
		ActiveRiteData.RiteEntry copy = roundTrip(entry().withRings(2, new int[] {50, 50, 50, 50, 63, 63, 0, 0}));

		assertEquals(2, copy.getOpenRings());
		assertEquals(63, copy.anchorRequirement(4));
		org.junit.jupiter.api.Assertions.assertTrue(copy.anchorDormant(6));
		org.junit.jupiter.api.Assertions.assertFalse(copy.anchorDormant(0));

		ActiveRiteData.RiteEntry unsynced = entry();
		assertEquals(unsynced.getTotalRings(), unsynced.getOpenRings());
		assertEquals(50, unsynced.anchorRequirement(3));
		org.junit.jupiter.api.Assertions.assertFalse(unsynced.anchorDormant(3));
	}
}

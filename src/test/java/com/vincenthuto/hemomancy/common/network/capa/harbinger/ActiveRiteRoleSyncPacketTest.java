package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilAnatomy;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ActiveRiteRoleSyncPacketTest {
	@Test
	void packetRoundTripsBoundaryAndSigilNodeKindsAndRoles() {
		var boundary = new ActiveRiteData.SanguineBlob(0, 0, 0, 0.2F,
				0xAA0000, 1L, 0.4F,
				ActiveRiteData.NodeKind.BOUNDARY_ANCHOR,
				IchorianSigilAnatomy.Role.JOINT);
		var sigil = new ActiveRiteData.SanguineBlob(1, 0, 0, 0.16F,
				0x42D9D2, 2L, 1.0F,
				ActiveRiteData.NodeKind.SIGIL_NODE,
				IchorianSigilAnatomy.Role.GANGLION);
		var entry = new ActiveRiteData.RiteEntry(BlockPos.ZERO, 3, 0,
				ResourceLocation.parse("hemomancy:sync_test"), false,
				"CONSECRATION", 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, "",
				List.of(), List.of(), List.of(boundary, sigil));
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

		PacketSyncActiveRites.encode(buffer, new PacketSyncActiveRites(List.of(entry)));
		var blobs = PacketSyncActiveRites.decode(buffer).entries().getFirst().getSanguineBlobs();

		assertEquals(ActiveRiteData.NodeKind.BOUNDARY_ANCHOR, blobs.get(0).kind());
		assertEquals(IchorianSigilAnatomy.Role.GANGLION, blobs.get(1).role());
	}

	@Test
	void packetRoundTripsSigilSegmentIdentityAndStage() {
		var segment = new ActiveRiteData.SigilSegment(1, 64.1, 2, 3, 65.1, 2,
				0xAA1122, "hemomancy:reservoir@0", 2, 5, true);
		var entry = new ActiveRiteData.RiteEntry(BlockPos.ZERO, 3, 0,
				ResourceLocation.parse("hemomancy:sigil_sync_test"), false,
				"CONSECRATION", 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, "",
				List.of(), List.of(segment), List.of());
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

		PacketSyncActiveRites.encode(buffer, new PacketSyncActiveRites(List.of(entry)));
		var decoded = PacketSyncActiveRites.decode(buffer).entries().getFirst().getSigilSegments();

		assertEquals(List.of(segment), decoded);
		var copy = decoded.getFirst();
		assertEquals("hemomancy:reservoir@0", copy.sigilKey());
		assertEquals(2, copy.linkIndex());
		assertEquals(5, copy.linkCount());
		assertEquals(true, copy.awakened());
		assertEquals(64.1, copy.startY(), 0.0);
		assertEquals(0xAA1122, copy.color());
	}

	@Test
	void legacyBlobConstructorFallsBackToJointRole() {
		var blob = new ActiveRiteData.SanguineBlob(0, 0, 0, 0.1F, 0, 1L);

		assertEquals(ActiveRiteData.NodeKind.SIGIL_NODE, blob.kind());
		assertEquals(IchorianSigilAnatomy.Role.JOINT, blob.role());
	}

	@Test
	void packetRoundTripsCancellationTicks() {
		var entry = new ActiveRiteData.RiteEntry(BlockPos.ZERO, 3, 0,
				ResourceLocation.parse("hemomancy:cancel_sync_test"), false,
				"ORDEAL", 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, "",
				4.0F, List.of(), List.of(), List.of(), List.of(),
				true, UUID.randomUUID(), 37);
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

		PacketSyncActiveRites.encode(buffer, new PacketSyncActiveRites(List.of(entry)));
		var decoded = PacketSyncActiveRites.decode(buffer).entries().getFirst();

		assertEquals(37, decoded.getCancellationTicks());
	}

	@Test
	void packetRoundTripsTheServerAuthoredStormAtmosphere() {
		var entry = atmosphereEntry("storm", true, true);
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

		PacketSyncActiveRites.encode(buffer, new PacketSyncActiveRites(List.of(entry)));
		var decoded = PacketSyncActiveRites.decode(buffer).entries().getFirst();

		assertEquals("storm", decoded.getFogProfile());
		assertEquals(true, decoded.hasFogLightning());
		assertEquals(true, decoded.hasBoundaryDome());
	}

	@Test
	void packetRoundTripsAuthoritativePhaseTicks() {
		var entry = new ActiveRiteData.RiteEntry(BlockPos.ZERO, 9, 0.975D,
				ResourceLocation.parse("hemomancy:finale_sync_test"), false,
				"CULMINATION", 0, 0, 3, 3, 3, 0, 600, 0, 0, -1, "",
				9.0F, List.of(), List.of(), List.of(), List.of(),
				true, UUID.randomUUID(), 0, 40, "storm", true, true, 27);
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

		PacketSyncActiveRites.encode(buffer, new PacketSyncActiveRites(List.of(entry)));
		var decoded = PacketSyncActiveRites.decode(buffer).entries().getFirst();

		assertEquals(27, decoded.getPhaseTicks());
		assertEquals(27.0F, decoded.phaseRenderTicks(0.0F), 0.0001F);
	}

	private static ActiveRiteData.RiteEntry atmosphereEntry(
			String fogProfile, boolean lightning, boolean dome) {
		return new ActiveRiteData.RiteEntry(BlockPos.ZERO, 9, 0,
				ResourceLocation.parse("hemomancy:cardinal_rite/pallid_shadow"), false,
				"CONSECRATION", 0, 0, 3, 0, 3, 0, 600, 0, 0, -1, "",
				9.0F, List.of(), List.of(), List.of(), List.of(),
				true, UUID.randomUUID(), 0, 40, fogProfile, lightning, dome);
	}
}

package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilAnatomy;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilSyncData;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IchorianKnowledgeSyncPacketTest {
	@Test
	void syncCarriesDefinitionsNeededByRemoteClientRitesTab() {
		ResourceLocation id = ResourceLocation.parse("hemomancy:test_shape");
		IchorianSigilDefinition definition = new IchorianSigilDefinition(id,
				IchorianSigilDefinition.Kind.SUPPORT, 2, 0xAA2244, "Test", "Purpose",
				7, 50, List.of(new IchorianSigilDefinition.Node(1, 2)));
		IchorianSigilRegistry.reload(Map.of(id, definition));

		IchorianSigilSyncData packetData = IchorianSigilSyncData.capture();

		assertEquals(definition, packetData.definitions().get(id));
	}

	@Test
	void definitionCodecRoundTripsGroundTopologyAndAnatomy() {
		ResourceLocation id = ResourceLocation.parse("hemomancy:test_anatomy");
		IchorianSigilAnatomy anatomy = new IchorianSigilAnatomy(
				new Vec3(0, 0, -1),
				new IchorianSigilAnatomy.Animation(
						IchorianSigilAnatomy.Style.ARTERIAL_FORK, 1.2F, 0.9F, 0.2F),
				List.of(
						new IchorianSigilAnatomy.Landmark(0, new Vec3(0, 0, -0.5),
								IchorianSigilAnatomy.Role.EYE, 0.13F),
						new IchorianSigilAnatomy.Landmark(1, Vec3.ZERO,
								IchorianSigilAnatomy.Role.ORGAN, 0.18F)),
				List.of(new IchorianSigilAnatomy.Vessel(0, 1, 0.07F)),
				List.of());
		IchorianSigilDefinition definition = new IchorianSigilDefinition(id,
				IchorianSigilDefinition.Kind.RESPONSE, 2, 0xAA2244, "Test", "Purpose",
				7, 50,
				List.of(new IchorianSigilDefinition.Node(0, -1),
						new IchorianSigilDefinition.Node(0, 1)),
				List.of(new IchorianSigilDefinition.Connection(0, 1)),
				Optional.of(anatomy));
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

		PacketSyncIchorianKnowledge.writeDefinition(buffer, definition);
		IchorianSigilDefinition decoded = PacketSyncIchorianKnowledge.readDefinition(buffer);

		assertEquals(definition, decoded);
	}

	@Test
	void definitionCodecRoundTripsLegacyFallback() {
		IchorianSigilDefinition definition = new IchorianSigilDefinition(
				ResourceLocation.parse("hemomancy:legacy"),
				IchorianSigilDefinition.Kind.SUPPORT, 1, 0, "Legacy", "Purpose",
				0, 0, List.of(new IchorianSigilDefinition.Node(0, 0)));
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

		PacketSyncIchorianKnowledge.writeDefinition(buffer, definition);

		assertEquals(definition, PacketSyncIchorianKnowledge.readDefinition(buffer));
	}
}

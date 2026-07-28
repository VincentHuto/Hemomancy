package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.rite.IchorianKnowledge;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilSyncData;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
}

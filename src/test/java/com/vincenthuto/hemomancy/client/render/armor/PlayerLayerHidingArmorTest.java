package com.vincenthuto.hemomancy.client.render.armor;

import com.vincenthuto.hemomancy.common.item.harbinger.armor.AbstractSpecialBloodLustArmorItem;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.BloodLustArmorItem;
import com.vincenthuto.hemomancy.common.item.shared.armor.BarbedArmorItem;
import com.vincenthuto.hemomancy.common.item.shared.armor.ChitiniteArmorItem;
import com.vincenthuto.hemomancy.common.item.shared.armor.PlayerLayerHidingArmor;
import com.vincenthuto.hemomancy.common.item.shared.armor.PrismaticArmorItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlayerLayerHidingArmorTest {
	@Test
	void markerControlsPlayerLayerHiding() {
		assertTrue(PlayerLayerHidingArmor.hidesPlayerLayers(new PlayerLayerHidingArmor() {}));
		assertFalse(PlayerLayerHidingArmor.hidesPlayerLayers(new Object()));
	}

	@Test
	void allOversizedArmorFamiliesCarryTheMarker() {
		assertTrue(PlayerLayerHidingArmor.class.isAssignableFrom(BarbedArmorItem.class));
		assertTrue(PlayerLayerHidingArmor.class.isAssignableFrom(ChitiniteArmorItem.class));
		assertTrue(PlayerLayerHidingArmor.class.isAssignableFrom(PrismaticArmorItem.class));
		assertTrue(PlayerLayerHidingArmor.class.isAssignableFrom(BloodLustArmorItem.class));
		assertTrue(PlayerLayerHidingArmor.class.isAssignableFrom(AbstractSpecialBloodLustArmorItem.class));
	}
}

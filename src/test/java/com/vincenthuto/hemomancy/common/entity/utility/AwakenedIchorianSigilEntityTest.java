package com.vincenthuto.hemomancy.common.entity.utility;

import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilMotion;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The entity's {@code hasHover} hands its save to {@link AwakenedIchorianSigilMotion#hasSavedHover}; the check is read
 * there so this test loads no entity class (an entity's synced-data accessors need the registries, and the full suite
 * cannot promise them a clean bootstrap).
 */
final class AwakenedIchorianSigilEntityTest {
	@Test
	void aFormSavedBeforeTheHoverHasNoHoverPoint() {
		CompoundTag old = new CompoundTag();
		old.putString("SigilId", "hemomancy:reservoir");
		old.putLong("RiteCenter", 0L);
		old.putFloat("OrbitRadius", 2.5F);
		old.putFloat("StartingAngle", 1.2F);

		assertFalse(AwakenedIchorianSigilMotion.hasSavedHover(old),
				"an orbiting form from an old save discards itself instead of hovering at the origin");
	}

	@Test
	void aHoveringFormKeepsItsPoint() {
		CompoundTag tag = new CompoundTag();
		tag.putDouble("HoverX", 10.0D);
		tag.putDouble("HoverY", 66.2D);
		tag.putDouble("HoverZ", 20.5D);

		assertTrue(AwakenedIchorianSigilMotion.hasSavedHover(tag));
	}
}

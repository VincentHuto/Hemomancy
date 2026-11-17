package com.vincenthuto.hemomancy.common.manipulation.ferric;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class CellHandFormSwapSourceTest {

	private CellHandFormSwapSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String helper = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/CellHandFormHelper.java");
		String changePacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/ChangeSelectedManipPacket.java");
		String updatePacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/UpdateCurrentManipPacket.java");
		String selection = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/verb/VerbSelection.java");

		// Core-loop Track 1 (CL-2): the hand follows the active verb, switched by the verb key, never a selection.
		assertContains("cell hand helper maps a verb to its hand", helper, "public static Item itemFor(Verb verb)");
		assertContains("cell hand helper only swaps existing cell hands", helper, "isCellHandForm(held)");
		assertContains("cell hand helper swaps main hand item", helper, "player.setItemInHand(InteractionHand.MAIN_HAND");
		assertContains("cell hand helper maps absorption item", helper, "ItemInit.blood_absorption.get()");
		assertContains("cell hand helper maps projection item", helper, "ItemInit.blood_projection.get()");
		assertContains("switching the verb swaps a held cell hand", selection, "CellHandFormHelper.applyVerb(player, verb)");
		assertNotContains("cycling selection no longer swaps cell hands", changePacket, "CellHandFormHelper");
		assertNotContains("radial selection no longer swaps cell hands", updatePacket, "CellHandFormHelper");
	}
}

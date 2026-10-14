package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** A two-ring rite through its ordeal, as the server saves it. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CardinalRiteRingGatingGameTests {
	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "rite_ring_gating")
	public static void theSecondRingSleepsThenHoldsTheFirstWaveThroughASave(GameTestHelper helper) {
		var provider = helper.getLevel().registryAccess();
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), helper.absolutePos(BlockPos.ZERO),
				Hemomancy.rloc("cardinal_rite/ring_gating_fixture"), 1200, 5, 5, false, 2, 8);
		rite.configureRings(new int[] {0, 0, 0, 0, 1, 1, 1, 1}, false);

		helper.assertFalse(rite.fillAnchor(4, 63), "ring 1 sleeps through Consecration");
		for (int i = 0; i < 4; i++) rite.fillAnchor(i, 50);
		helper.assertTrue(rite.enterInscription(), "ring 0 alone consecrates");
		helper.assertTrue(rite.sealAltar(false) && rite.getPhase() == CardinalRitePhase.ORDEAL, "sealed into the ordeal");
		helper.assertTrue(rite.getOpenRings() == 2, "the first wave wakes ring 1");
		helper.assertFalse(rite.areAnchorsConsecrated(), "the wave cannot clear while ring 1 hungers");
		rite.fillAnchor(4, 63);

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize(provider), provider);

		helper.assertTrue(copy.getOpenRings() == 2 && copy.getAnchorBloodMl()[4] == 63, "the hold survives the save");
		helper.assertTrue(copy.nextRingToAnnounce() == 1, "the waking had not been announced before the save");
		helper.assertFalse(copy.areAnchorsConsecrated(), "still held after loading");
		for (int i = 5; i < 8; i++) copy.fillAnchor(i, 63);
		helper.assertTrue(copy.areAnchorsConsecrated(), "fed, the wave may clear");
		helper.succeed();
	}
}

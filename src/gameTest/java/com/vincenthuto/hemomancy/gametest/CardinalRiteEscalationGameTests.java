package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEscalation;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** The world stage of a rite saved with real offerings, as the server saves it. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CardinalRiteEscalationGameTests {
	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "rite_escalation")
	public static void offeringsKeepTheirProcessionStepThroughASave(GameTestHelper helper) {
		var provider = helper.getLevel().registryAccess();
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), helper.absolutePos(BlockPos.ZERO),
				Hemomancy.rloc("cardinal_rite/escalation_fixture"), 1200, 5, 4, false, 0, 4);
		rite.captureOfferingItinerary(List.of(new ActiveCardinalRite.RiteOffering(
				helper.absolutePos(new BlockPos(2, 0, 0)), new ItemStack(Items.BONE), true)));
		for (int i = 0; i < 4; i++) rite.fillAnchor(i, 50);
		CardinalRiteEscalation.update(rite);

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize(provider), provider);

		helper.assertTrue(copy.getEscalationStage() == 1, "the counted ring survives the save");
		helper.assertTrue(copy.getEscalationTotal() == 4, "the saved offering keeps its procession step");
		copy.enterInscription();
		copy.sealAltar(false);
		helper.assertTrue(CardinalRiteEscalation.update(copy) && copy.getEscalationStage() == 3,
				"after loading, the seal and the procession step together");
		helper.succeed();
	}
}

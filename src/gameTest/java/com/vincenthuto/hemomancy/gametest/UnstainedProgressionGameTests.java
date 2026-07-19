package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class UnstainedProgressionGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";
	private UnstainedProgressionGameTests() {}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void exactClarityRiteGatesLoad(GameTestHelper helper) {
		CardinalRiteRecipe glassLungs = CardinalRiteRecipe.getRiteByLocation(helper.getLevel(),
				Hemomancy.rloc("cardinal_rite/glass_lungs"));
		CardinalRiteRecipe moonWashed = CardinalRiteRecipe.getRiteByLocation(helper.getLevel(),
				Hemomancy.rloc("cardinal_rite/moon_washed_copper"));
		helper.assertTrue(glassLungs != null && glassLungs.getRequiredClarity() == 50f,
				"Glass Lungs must load its exact Clarity 50 gate");
		helper.assertTrue(moonWashed != null && moonWashed.getRequiredClarity() == 75f,
				"Moon-Washed Copper must load its exact Clarity 75 gate");
		helper.assertTrue(glassLungs.getResult().is(ItemInit.lethean_chalice.get()),
				"Glass Lungs must produce the registered Lethean Chalice");
		helper.assertTrue(moonWashed.getResult().is(ItemInit.pale_silver_bell.get()),
				"Moon-Washed Copper must produce a Pale Silver Bell");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void quietWorkStationsRegistered(GameTestHelper helper) {
		helper.assertTrue(!BlockInit.stillwater_condenser.get().asItem().getDefaultInstance().isEmpty(),
				"Stillwater Condenser block item must be registered");
		helper.assertTrue(!BlockInit.verdigris_lattice.get().asItem().getDefaultInstance().isEmpty(),
				"Verdigris Lattice block item must be registered");
		helper.succeed();
	}
}

package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemoryPrimingRules;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.ThelemicMemoryTinctureItem;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** One item per Thelemic Memory: a flask and a jug differ only in the doses they hold and the vessel they return. */
@GameTestHolder("hardening_validation")
@PrefixGameTestTemplate(false)
public final class TinctureDoseGameTests {
	private TinctureDoseGameTests() {
	}

	private static ItemStack drinkAll(GameTestHelper helper, CapturedPlayer captured, ItemStack stack, int doses) {
		ItemStack held = stack;
		for (int dose = 1; dose < doses; dose++) {
			held = held.getItem().finishUsingItem(held, helper.getLevel(), captured.player);
			helper.assertTrue(held.getItem() instanceof ThelemicMemoryTinctureItem, "dose " + dose + " of " + doses + " emptied the vessel");
		}
		return held.getItem().finishUsingItem(held, helper.getLevel(), captured.player);
	}

	@GameTest(template = "empty", batch = "tincture_doses")
	public static void aFlaskHoldsThreeDosesAndReturnsAFlask(GameTestHelper helper) {
		ItemStack flask = new ItemStack(ItemInit.tincture_second_pulse.get());
		helper.assertTrue(!ThelemicMemoryTinctureItem.isJug(flask) && ThelemicMemoryTinctureItem.maximumDoses(flask) == ThelemicMemoryPrimingRules.FLASK_DOSES, "an unmarked tincture is a flask");
		try (var captured = CapturedPlayer.create(helper)) {
			ItemStack empty = drinkAll(helper, captured, flask, ThelemicMemoryPrimingRules.FLASK_DOSES);
			helper.assertTrue(empty.is(HLItemInit.cured_clay_flask.get()), "a flask returned " + empty);
		}
		helper.succeed();
	}

	@GameTest(template = "empty", batch = "tincture_doses")
	public static void aJugHoldsSixDosesAndReturnsAJug(GameTestHelper helper) {
		ItemStack jug = ThelemicMemoryTinctureItem.jugOf(ItemInit.tincture_second_pulse.get());
		helper.assertTrue(ThelemicMemoryTinctureItem.isJug(jug) && ThelemicMemoryTinctureItem.maximumDoses(jug) == ThelemicMemoryPrimingRules.JUG_DOSES, "not a jug");
		try (var captured = CapturedPlayer.create(helper)) {
			ItemStack empty = drinkAll(helper, captured, jug, ThelemicMemoryPrimingRules.JUG_DOSES);
			helper.assertTrue(empty.is(ItemInit.cured_clay_jug.get()), "a jug returned " + empty);
			helper.assertTrue(HemoCapabilityAccess.thelemicMemory(captured.player).reserveTicks(ThelemicMemory.SECOND_PULSE) >= ThelemicMemoryPrimingRules.JUG_DOSES * ThelemicMemoryPrimingRules.TICKS_PER_DOSE - ThelemicMemoryPrimingRules.TICKS_PER_DOSE,
					"six doses did not add six doses of reserve");
		}
		helper.succeed();
	}

	@GameTest(template = "empty", batch = "tincture_doses")
	public static void aJugStaysAJugPartWayThroughAndIsNamedOne(GameTestHelper helper) {
		ItemStack jug = ThelemicMemoryTinctureItem.jugOf(ItemInit.tincture_second_pulse.get());
		try (var captured = CapturedPlayer.create(helper)) {
			ItemStack held = jug.getItem().finishUsingItem(jug, helper.getLevel(), captured.player);
			helper.assertTrue(ThelemicMemoryTinctureItem.isJug(held) && ((ThelemicMemoryTinctureItem) held.getItem()).remainingDoses(held) == ThelemicMemoryPrimingRules.JUG_DOSES - 1, "a part-drunk jug forgot itself");
			var name = (TranslatableContents) held.getHoverName().getContents();
			helper.assertTrue(name.getKey().equals("item.hemomancy.tincture_second_pulse.jug"), "named " + name.getKey());
		}
		helper.succeed();
	}

	@GameTest(template = "empty", batch = "tincture_doses")
	public static void thereIsOneTinctureItemPerMemory(GameTestHelper helper) {
		long tinctures = BuiltInRegistries.ITEM.stream().filter(ThelemicMemoryTinctureItem.class::isInstance).count();
		helper.assertTrue(tinctures == ThelemicMemory.values().length, tinctures + " tincture items");
		helper.succeed();
	}
}

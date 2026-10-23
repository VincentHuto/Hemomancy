package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Step 9 (G-4): the heal-modifier table and the one listener that reads a heal. Each reducing effect scales a
 * 2.5-point heal. Unstained restraint purity (a heal of 2 or more while the blood is dormant) reads the requested
 * amount, before any modifier, as it did before step 9 made that order explicit.
 */
@GameTestHolder("combat_targeting_validation")
@PrefixGameTestTemplate(false)
public final class HealModifierGameTests {
	private static final String NAMESPACE = "combat_targeting_validation";

	private HealModifierGameTests() {
	}

	@GameTest(templateNamespace = NAMESPACE, template = "combat_targeting_room", batch = "heal_modifiers", timeoutTicks = 60)
	public static void eachHealModifierScalesAndPurityReadsTheResult(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			List<String> rows = new ArrayList<>();
			rows.add(heal(h, null));
			rows.add(heal(h, EffectInit.necrosis));
			rows.add(heal(h, EffectInit.insatiable_hunger));
			rows.add(heal(h, EffectInit.hemophagy));
			rows.add(heal(h, EffectInit.impaired_recovery));
			String actual = String.join(", ", rows);
			Hemomancy.LOGGER.info("HEAL_MODIFIERS {}", actual);
			h.assertTrue(actual.equals("none 2.5 purity, necrosis 1.875 purity, insatiable_hunger 0.625 purity, "
					+ "hemophagy 0.625 purity, impaired_recovery 1.25 purity"), "heal modifiers changed: " + actual);
			h.succeed();
		});
	}

	@GameTest(templateNamespace = NAMESPACE, template = "combat_targeting_room", batch = "heal_modifiers", timeoutTicks = 60)
	public static void stackedHealModifiersMultiply(GameTestHelper h) {
		h.runAfterDelay(2, () -> {
			ServerPlayer player = DuctilisGameTests.player(h);
			try {
				player.setHealth(10.0F);
				player.addEffect(new MobEffectInstance(EffectInit.necrosis, 200));
				player.addEffect(new MobEffectInstance(EffectInit.impaired_recovery, 200));
				player.heal(8.0F);
				h.assertTrue(Math.abs(player.getHealth() - 13.0F) < 1.0E-4F, "necrosis x impaired recovery: " + player.getHealth());
				player.removeEffect(EffectInit.necrosis);
				player.addEffect(new MobEffectInstance(EffectInit.hemophagy, 200));
				player.addEffect(new MobEffectInstance(EffectInit.insatiable_hunger, 200));
				player.setHealth(10.0F);
				player.heal(8.0F);
				h.assertTrue(Math.abs(player.getHealth() - 11.0F) < 1.0E-4F,
						"insatiable hunger suppresses hemophagy, times impaired recovery: " + player.getHealth());
				h.succeed();
			} finally {
				player.discard();
			}
		});
	}

	private static String heal(GameTestHelper h, Holder<MobEffect> effect) {
		ServerPlayer player = DuctilisGameTests.player(h);
		try {
			HemoCapabilityAccess.getBloodVolume(player).setActive(false);
			var progress = HemoCapabilityAccess.getUnstainedProgress(player);
			progress.setBegunPurification(true);
			progress.setPurity(0.0F);
			player.setHealth(10.0F);
			if (effect != null) player.addEffect(new MobEffectInstance(effect, 200));
			player.heal(2.5F);
			String name = effect == null ? "none" : effect.unwrapKey().orElseThrow().location().getPath();
			return name + " " + (player.getHealth() - 10.0F) + (progress.getPurity() > 0.0F ? " purity" : "");
		} finally {
			player.discard();
		}
	}
}

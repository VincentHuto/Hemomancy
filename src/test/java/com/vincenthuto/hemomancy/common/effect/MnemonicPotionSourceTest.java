package com.vincenthuto.hemomancy.common.effect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MnemonicPotionSourceTest {
	private static final Path EFFECT_INIT = Path.of("src/main/java/com/vincenthuto/hemomancy/common/init/EffectInit.java");
	private static final Path BLOOD_MANIPULATION = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java");
	private static final Path LANG = Path.of("src/main/resources/assets/hemomancy/lang/en_us.json");
	private static final Path MOB_EFFECT_TEXTURES = Path.of("src/main/resources/assets/hemomancy/textures/mob_effect");

	private MnemonicPotionSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String effectInit = Files.readString(EFFECT_INIT).replace("\r\n", "\n");
		String bloodManipulation = Files.readString(BLOOD_MANIPULATION).replace("\r\n", "\n");
		String lang = Files.readString(LANG).replace("\r\n", "\n");

		assertContains("registers Mnemonic Whispers effect", effectInit, "mnemonic_whispers");
		assertContains("registers Mnemonic Screams effect", effectInit, "mnemonic_screams");
		assertContains("registers Mnemonic Whispers potion", effectInit, "potion_of_mnemonic_whispers");
		assertContains("brews Whispers from ambergris", effectInit, "ItemInit.mnemonic_ambergris.get()");
		assertContains("applies Screams on repeated Whispers drinking", effectInit,
				"shouldApplyScreamsOnWhispersDrink");
		assertContains("records pre-existing Whispers before drink finishes", effectInit,
				"onMnemonicPotionDrinkStart");
		assertContains("removes Whispers when Screams is applied", effectInit,
				"removeEffect(mnemonic_whispers");
		assertContains("cooldown uses Mnemonic Whispers", bloodManipulation,
				"MnemonicPotionRules.manipulationCooldownMultiplier");
		assertContains("cost uses Mnemonic Screams", bloodManipulation,
				"MnemonicPotionRules.manipulationCostMultiplier");
		assertContains("Whispers lang", lang, "\"effect.hemomancy.mnemonic_whispers\"");
		assertContains("Screams lang", lang, "\"effect.hemomancy.mnemonic_screams\"");
		assertContains("Whispers potion lang", lang,
				"\"item.minecraft.potion.effect.potion_of_mnemonic_whispers\"");
		assertTextureExists("Mnemonic Whispers icon", "mnemonic_whispers.png");
		assertTextureExists("Mnemonic Screams icon", "mnemonic_screams.png");
		assertTextureExists("Blood Drunkenness icon", "blood_drunkenness.png");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertTextureExists(String label, String fileName) {
		if (!Files.exists(MOB_EFFECT_TEXTURES.resolve(fileName))) {
			throw new AssertionError(label + ": missing " + MOB_EFFECT_TEXTURES.resolve(fileName));
		}
	}
}

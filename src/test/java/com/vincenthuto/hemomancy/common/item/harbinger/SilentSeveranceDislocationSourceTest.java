package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SilentSeveranceDislocationSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCES_ROOT = Path.of("src/main/resources");
	private static final Path DOCS_ROOT = Path.of("docs");

	private SilentSeveranceDislocationSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String effectInit = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/init/EffectInit.java"));
		String handler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/SilentArchonArmorAbilityHandler.java"));
		String lang = read(RESOURCES_ROOT.resolve(
				"assets/hemomancy/lang/en_us.json"));
		String docs = read(DOCS_ROOT.resolve("HEMOMANCY_REFERENCE.md"));

		assertContains("effect registered", effectInit, "monolithic_dislocation");
		assertContains("effect is harmful", effectInit, "MobEffectCategory.HARMFUL");
		assertContains("effect translated", lang,
				"\"effect.hemomancy.monolithic_dislocation\": \"Monolithic Dislocation\"");

		assertContains("severance duration constant", handler, "SILENT_SEVERANCE_EFFECT_TICKS");
		assertContains("severance applies new effect", handler,
				"new MobEffectInstance(EffectInit.monolithic_dislocation");
		assertDoesNotContain("severance removed darkness", handler,
				"new MobEffectInstance(MobEffects.DARKNESS");
		assertDoesNotContain("severance removed weakness", handler,
				"new MobEffectInstance(MobEffects.WEAKNESS");
		assertDoesNotContain("severance removed slowness", handler,
				"new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN");
		assertDoesNotContain("severance removed blood loss", handler,
				"new MobEffectInstance(EffectInit.blood_loss");

		assertContains("incoming dislocation hook", handler,
				"negateMonolithicDislocationIncomingDamage");
		assertContains("outgoing dislocation hook", handler,
				"suppressMonolithicDislocationOffense");
		assertContains("incoming damage event import", handler,
				"LivingIncomingDamageEvent");
		assertContains("early incoming damage hook", handler,
				"onLivingIncomingDamage(LivingIncomingDamageEvent event)");
		assertContains("early incoming damage cancel", handler,
				"event.setCanceled(true)");
		assertContains("early incoming damage amount clear", handler,
				"event.setAmount(0.0F)");
		assertContains("incoming checks victim effect", handler,
				"event.getEntity().hasEffect(EffectInit.monolithic_dislocation)");
		assertContains("outgoing checks attacker effect", handler,
				"attacker.hasEffect(EffectInit.monolithic_dislocation)");
		assertContains("outgoing checks direct source too", handler,
				"source.getDirectEntity()");
		assertContains("incoming allows special damage", handler,
				"isAllowedIntangibleDamage(event.getSource())");
		assertContains("living weapon allowance is reused", handler, "isLivingWeapon");
		assertContains("living weapon carrier allowance", handler, "CombatWeaponCarrierProjectile");
		assertContains("blood projectile allowance", handler, "isHemomancyProjectile(direct)");
		assertContains("damage is suppressed", handler, "event.setNewDamage(0.0F);");

		assertContains("docs mention dislocation", docs, "Monolithic Dislocation");
		assertContains("docs mention incoming exceptions", docs, "blood manipulations and living weapons");
		assertContains("docs mention offense lock", docs, "cannot deal damage");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": still contains " + forbidden);
		}
	}
}

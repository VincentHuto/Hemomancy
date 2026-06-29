package com.vincenthuto.hemomancy.common.item.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PhantasmalMasqueradeSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private PhantasmalMasqueradeSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String entityInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/EntityInit.java"));
		String handler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/armor/ability/FinalBloodlustArmorAbilityHandler.java"));
		String echo = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/summon/PhantasmalEchoEntity.java"));
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/entity/summon/PhantasmalEchoRenderer.java"));
		String clientEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/ClientEvents.java"));
		String damageType = read(RESOURCE_ROOT.resolve("data/hemomancy/damage_type/phantasmal_echo.json"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertContains("entity registered", entityInit, "phantasmal_echo");
		assertContains("entity attributes registered", entityInit, "PhantasmalEchoEntity.setAttributes");
		assertContains("handler creates echo", handler, "new PhantasmalEchoEntity");
		assertContains("handler assigns modes", handler, "PhantasmalEchoEntity.BehaviorMode");
		assertContains("handler copies owner armor", handler, "configureFromOwner");

		assertContains("echo owner sync", echo, "DATA_OWNER_UUID");
		assertContains("echo behavior enum", echo, "enum BehaviorMode");
		assertContains("echo aggressive mode", echo, "AGGRESSIVE");
		assertContains("echo fleeing mode", echo, "FLEEING");
		assertContains("echo circling mode", echo, "CIRCLING");
		assertContains("echo mirroring mode", echo, "MIRRORING");
		assertContains("echo burst on hit", echo, "burstSmokeCloud");
		assertContains("echo natural expiration", echo, "dissolveQuietly");
		assertContains("echo blindness", echo, "MobEffects.BLINDNESS");
		assertContains("echo confusion", echo, "MobEffects.CONFUSION");
		assertContains("echo slowness", echo, "MobEffects.MOVEMENT_SLOWDOWN");
		assertContains("echo target reset", echo, "monster.setTarget(null)");
		assertContains("echo custom damage", echo, "HemoDamageTypes.phantasmalEcho");

		assertContains("renderer registered", clientEvents, "EntityInit.phantasmal_echo");
		assertContains("renderer class", renderer, "PhantasmalEchoRenderer");
		assertContains("renderer uses humanoid armor layer", renderer, "HumanoidArmorLayer");
		assertContains("renderer translucent", renderer, "RenderType.entityTranslucent");

		assertContains("damage type id", damageType, "phantasmal_echo");
		assertContains("lang ability name", lang, "\"ability.hemomancy.masquerade_of_the_forgotten\":");
		assertContains("lang death message", lang, "\"death.attack.phantasmal_echo\":");
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
}

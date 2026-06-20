package com.vincenthuto.hemomancy.common.entity.mob.monster;

import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodDrunkPuppeteerBehaviorSourceTest {
	private BloodDrunkPuppeteerBehaviorSourceTest() {
	}

	public static void main(String[] args) throws Exception {
		String puppeteerEntity = read("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/BloodDrunkPuppeteerEntity.java");
		assertDoesNotContain("wild puppeteer should command dolls instead of meleeing directly",
				puppeteerEntity, "new MeleeAttackGoal(this");
		assertContains("wild puppeteer should keep distance while commanding dolls",
				puppeteerEntity, "new AvoidEntityGoal<>(this, Player.class");

		String dollEntity = read("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/EnthralledDollEntity.java");
		assertContains("summoned dolls should mirror the puppeteer's target",
				dollEntity, "LivingEntity puppeteerTarget = puppeteer.getTarget();");
		assertContains("summoned dolls should navigate directly toward the puppeteer's target",
				dollEntity, "getNavigation().moveTo(puppeteerTarget");
		assertContains("summoned dolls should inherit the puppeteer's target when spawned",
				puppeteerEntity, "doll.setTarget(getTarget());");

		String threadRenderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/PuppeteerThreadRenderer.java");
		assertContains("world thread renderer should render wild puppeteer doll tethers",
				threadRenderer, "renderThreadToWildDoll");
		assertContains("world thread renderer should recognize puppeteer-summoned dolls",
				threadRenderer, "entity instanceof EnthralledDollEntity doll");
		assertContains("world thread renderer should anchor wild doll threads to the puppeteer",
				threadRenderer, "findPuppeteerOwner");

		String dollRenderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/entity/mob/monster/EnthralledDollRenderer.java");
		assertDoesNotContain("doll renderer should not keep a second fishing-line style tether renderer",
				dollRenderer, "RenderType.lineStrip()");
		assertDoesNotContain("doll renderer should not use the fishing hook texture for puppeteer strings",
				dollRenderer, "textures/entity/fishing_hook.png");
	}

	private static String read(String path) throws Exception {
		return Files.readString(Path.of(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": still contains " + unexpected);
		}
	}
}

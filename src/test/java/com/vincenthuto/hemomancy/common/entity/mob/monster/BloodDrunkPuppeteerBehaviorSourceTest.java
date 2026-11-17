package com.vincenthuto.hemomancy.common.entity.mob.monster;

import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class BloodDrunkPuppeteerBehaviorSourceTest {
	private BloodDrunkPuppeteerBehaviorSourceTest() {
	}

	@Test
	void allChecksPass() throws Exception {
		String puppeteerEntity = read("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/BloodDrunkPuppeteerEntity.java");
		assertNotContains("wild puppeteer should command dolls instead of meleeing directly",
				puppeteerEntity, "new MeleeAttackGoal(this");
		assertContains("wild puppeteer should keep distance while commanding dolls",
				puppeteerEntity, "new AvoidEntityGoal<>(this, Player.class");

		String dollEntity = read("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/EnthralledDollEntity.java");
		assertContains("summoned dolls should mirror their puppeteer owner's target",
				dollEntity, "LivingEntity ownerTarget = owner.getTarget();");
		assertContains("summoned dolls should navigate directly toward the puppeteer's target",
				dollEntity, "getNavigation().moveTo(ownerTarget");
		assertContains("summoned dolls should inherit the puppeteer's target when spawned",
				puppeteerEntity, "doll.setTarget(getTarget());");

		String threadRenderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/PuppeteerThreadRenderer.java");
		assertContains("world thread renderer should render wild puppeteer doll tethers",
				threadRenderer, "renderThreadToWildDoll");
		assertContains("world thread renderer should recognize puppeteer-summoned dolls",
				threadRenderer, "entity instanceof EnthralledDollEntity doll");
		assertContains("world thread renderer should anchor wild doll threads to the puppeteer",
				threadRenderer, "controller instanceof BloodDrunkPuppeteerEntity");

		String dollRenderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/entity/mob/monster/EnthralledDollRenderer.java");
		assertNotContains("doll renderer should not keep a second fishing-line style tether renderer",
				dollRenderer, "RenderType.lineStrip()");
		assertNotContains("doll renderer should not use the fishing hook texture for puppeteer strings",
				dollRenderer, "textures/entity/fishing_hook.png");
	}
}

package com.vincenthuto.hemomancy.common.item.harbinger.memories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingWeaponGraftUnlockSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingWeaponGraftUnlockSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftRecipeUnlockEvents.java");
		String helper = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftRecipeUnlocks.java");
		String schoolHitHelper = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/SchoolHitHelper.java");
		String entityEffects = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/EntityManipulationEffects.java");

		assertContains("unlock helper has blade advancement", helper, "living_weapon_graft/blade");
		assertContains("unlock helper has flail advancement", helper, "living_weapon_graft/flail");
		assertContains("unlock helper checks advancement", helper, "hasEarnedRecipeUnlock");
		assertContains("unlock helper awards recipe unlock", helper, "awardRecipeUnlock");
		assertContains("unlock helper requires staff access", helper, "hasLivingStaffAccess");

		assertContains("unlock events observes deaths", events, "LivingDeathEvent");
		assertContains("blade unlock checks high blood", events, "bloodFraction(player) >= 0.75D");
		assertContains("spear unlock checks glowing target", events, "MobEffects.GLOWING");
		assertContains("claws unlock checks invisibility or darkness", events, "isClawsAmbush");
		assertContains("torch unlock checks burning target", events, "isOnFire()");
		assertContains("flail unlock checks slowed or frozen target", events, "isFlailControlled");
		assertContains("unlock events require living arsenal kills", events, "isLivingArsenalKill");

		assertContains("conductive arcs notify unlocks", schoolHitHelper, "LivingWeaponGraftRecipeUnlockEvents.onConductiveArcTriggered");
		assertContains("grave debt notifies axe unlock", schoolHitHelper, "onAxeAlignedManipulation");
		assertContains("entity effects notify axe for hemorrhage", entityEffects, "onAxeAlignedManipulation(context.caster())");
		assertContains("entity effects notify spear for lux reveal", entityEffects, "onSpearAlignedManipulation(context.caster())");
		assertContains("entity effects notify torch for ignition", entityEffects, "onTorchAlignedManipulation(context.caster())");
		assertContains("entity effects notify flail for cold control", entityEffects, "onFlailAlignedManipulation(context.caster())");
	}

	private static String read(String path) throws IOException {
		Path absolute = ROOT.resolve(path);
		if (!Files.exists(absolute)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(absolute).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}

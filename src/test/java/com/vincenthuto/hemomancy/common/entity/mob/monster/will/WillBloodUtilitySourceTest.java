package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WillBloodUtilitySourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private WillBloodUtilitySourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String will = read("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillEntity.java");
		String helper = read("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillBloodUtilityInteractions.java");
		String absorption = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodAbsorptionItem.java");
		String projection = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java");
		String staff = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffItem.java");
		String phase = read("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillPhase.java");
		String rules = read("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillAbsorptionRules.java");
		String config = read("src/main/java/com/vincenthuto/hemomancy/config/HemoServerConfig.java");

		assertContains("Will has a dedicated absorbing phase", phase, "ABSORBING");
		assertContains("Will exposes blood absorption resolution", will, "absorbWithBlood");
		assertContains("Will absorption tracks an owning channeler", will, "DATA_ABSORPTION_OWNER_UUID");
		assertContains("Will absorption syncs progress for render feedback", will, "DATA_ABSORPTION_PROGRESS");
		assertContains("Will absorption uses progress stage rules", will, "WillAbsorptionRules.stageForProgress");
		assertContains("Will absorption enters the absorbing phase", will, "setPhase(WillPhase.ABSORBING)");
		assertContains("Will absorption ticks its interruption grace", will, "tickAbsorptionStruggle()");
		assertContains("Will absorption failure escapes angry", will, "escapeAbsorptionAngry()");
		assertContains("Will absorption escape suppresses immediate refalter", will, "absorptionRageTicks");
		assertContains("Will absorption rage targets the absorber", will, "setTarget(player)");
		assertContains("Will absorption clamps lethal absorbing damage", will,
				"if (getPhase() == WillPhase.ABSORBING)");
		assertContains("Will absorption grants tendency", will, "addTendencyAlignment(getSchool(), 3.0F)");
		assertContains("Will absorption completes immediately at the end of the channel", will,
				"completeAbsorption(player)");
		assertContains("Will absorption completion drops rewards immediately", will,
				"dropAbsorptionCompletionLoot()");
		assertContains("Will absorption completion emits final glow pulse", will,
				"spawnAbsorptionCompletionPulse()");
		assertContains("Will absorption completion removes the Will immediately", will, "discard();");
		assertNotContains("Will absorption no longer uses a second dissolve duration", will,
				"BLOOD_ABSORPTION_DISSOLVE_TICKS");
		assertNotContains("Will absorption no longer syncs a dissolve state", will,
				"DATA_ABSORPTION_DISSOLVE");
		assertNotContains("Will absorption does not start a second dissolve state",
				methodBody(will, "public float absorbWithBlood"), "startDissolving(");
		assertContains("Will absorption does not use ordinary dissolve loot",
				methodBody(will, "private void completeAbsorption"), "dropAbsorptionCompletionLoot();");
		assertNotContains("Will absorption no longer drains health as progress",
				methodBody(will, "public float absorbWithBlood"), "setHealth(remainingHealth)");
		assertNotContains("Will absorption no longer resolves through final heart",
				methodBody(will, "public float absorbWithBlood"), "if (remainingHealth > 1.0F)");
		assertNotContains("Will absorption helper no longer has a health-drain constant", helper,
				"BLOOD_ABSORPTION_HEALTH_PER_TICK");
		assertContains("Will absorption helper uses bare progress rules", helper,
				"WillAbsorptionRules.bareProgressPerTick()");
		assertContains("Will absorption helper passes progress amount to the Will", helper,
				"will.absorbWithBlood(player, progressPerTick)");
		assertContains("Will exposes faltering or absorbing particle target state", will,
				"canBloodAbsorptionDrawParticles");
		assertContains("Will absorption particles include absorbing phase", will,
				"getPhase() == WillPhase.ABSORBING");
		assertContains("Will absorption completion pulse uses the Will glow particle", will,
				"WillAbsorptionGlowParticleFactory.createPulseData(ParticleColor.BLACK)");
		assertContains("Will projection subtracts tendency", will, "addTendencyAlignment(getSchool(), -3.0F)");
		assertContains("Will projection banishes without dissolve loot", will, "projectBanishWithBlood");
		assertContains("Will projection creates a burst cloud", will, "spawnProjectedBanishmentBurst");
		assertContains("Will utility helper finds faltering absorption targets", helper, "tryAbsorbFalteringWill");
		assertContains("Will utility helper finds looked-at projection targets", helper, "tryProjectBanishFalteringWill");
		assertContains("Will utility helper ray traces projection", helper, "ProjectileUtil.getEntityHitResult");
		assertContains("Will utility helper respects projection block occlusion", helper, "user.pick(PROJECTION_RANGE");
		assertContains("Will utility helper shortens projection ray at blocks", helper, "blockTrace.getLocation()");
		assertContains("Blood Absorption checks Will utility before mob drain", absorption, "tryAbsorbFalteringWill");
		assertContains("Blood Absorption excludes Wills from normal mob drain", absorption, "!(target instanceof WillEntity)");
		assertContains("Blood Projection checks Will utility before block projection", projection, "tryProjectBanishFalteringWill");
		assertContains("Living Staff absorption checks Will utility before generic targets", staff, "tryAbsorbFalteringWill");
		assertContains("Living Staff absorption passes staff focus to Will absorption", staff, "tryAbsorbFalteringWill(pLevel, player,");
		assertContains("Living Staff absorption uses focus-scaled Will progress", staff, "WillAbsorptionRules.staffProgressPerTick(focus)");
		assertContains("Living Staff projection remains covered by BloodProjectionItem", staff, "BloodProjectionItem.projectFromEntity");
		assertContains("Will absorption progress required is configurable", config, "willAbsorptionProgressRequired");
		assertContains("Will absorption grace is configurable", config, "willAbsorptionGraceTicks");
		assertContains("Will absorption escape health is configurable", config, "willAbsorptionEscapeHealthFraction");
		assertContains("Will absorption rage is configurable", config, "willAbsorptionRageTicks");
		assertContains("Will absorption rules expose progress required", rules, "progressRequired");
		assertContains("Will absorption rules expose controlled rage defaults", rules, "CONTROLLED_RAGE_TICKS");
	}

	private static String read(String path) throws IOException {
		Path resolved = ROOT.resolve(path);
		if (!Files.exists(resolved)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(resolved).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertNotContains(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + " (unexpected '" + forbidden + "')");
		}
	}

	private static String methodBody(String text, String signatureStart) {
		int start = text.indexOf(signatureStart);
		if (start < 0) {
			throw new AssertionError("missing method " + signatureStart);
		}
		int brace = text.indexOf('{', start);
		if (brace < 0) {
			throw new AssertionError("missing method body " + signatureStart);
		}
		int depth = 0;
		for (int i = brace; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '{') {
				depth++;
			} else if (c == '}') {
				depth--;
				if (depth == 0) {
					return text.substring(brace, i + 1);
				}
			}
		}
		throw new AssertionError("unterminated method body " + signatureStart);
	}
}

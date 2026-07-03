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

		assertContains("Will exposes blood absorption resolution", will, "absorbFalteringWithBlood");
		assertContains("Will absorption grants tendency", will, "addTendencyAlignment(getSchool(), 3.0F)");
		assertContains("Will absorption uses an extended dissolve duration", will,
				"BLOOD_ABSORPTION_DISSOLVE_TICKS");
		assertContains("Will absorption dissolve state is synced to clients for particles", will,
				"DATA_ABSORPTION_DISSOLVE");
		assertContains("Will dissolve ticks are synced for client flicker", will,
				"DATA_DISSOLVE_TICKS");
		assertContains("Will dissolve duration is synced for client flicker", will,
				"DATA_DISSOLVE_DURATION");
		assertContains("Will exposes dissolve progress for render flicker", will,
				"getDissolveProgress");
		assertContains("Will absorption starts the longer dissolve", will,
				"startDissolving(false, BLOOD_ABSORPTION_DISSOLVE_TICKS, true)");
		assertContains("Will absorption dissolve drops item rewards at disappearance", will,
				"if (entityData.get(DATA_ABSORPTION_DISSOLVE)) dropAbsorptionDissolveLoot();");
		assertContains("Will absorption has a separate disappearance reward helper", will,
				"private void dropAbsorptionDissolveLoot()");
		assertNotContains("Will absorption does not spawn item loot at health threshold",
				methodBody(will, "public float absorbFalteringWithBlood"), "spawnAtLocation(");
		assertContains("Will absorption drains health before resolving", will,
				"absorbFalteringWithBlood(ServerPlayer player, float amount)");
		assertContains("Will absorption leaves a partly drained Will alive if channel stops", will,
				"setHealth(remainingHealth)");
		assertContains("Will absorption only resolves when drained through the last heart", will,
				"if (remainingHealth > 1.0F)");
		assertContains("Will absorption helper uses a gradual per-tick drain", helper,
				"BLOOD_ABSORPTION_HEALTH_PER_TICK");
		assertContains("Will absorption helper passes drain amount to the Will", helper,
				"absorbFalteringWithBlood(player, BLOOD_ABSORPTION_HEALTH_PER_TICK)");
		assertContains("Will exposes faltering or absorption-dissolving particle target state", will,
				"canBloodAbsorptionDrawParticles");
		assertContains("Will absorption dissolve emits final glow pulse", will,
				"spawnAbsorptionDissolvePulse");
		assertContains("Will absorption dissolve pulse uses the Will glow particle", will,
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
		assertContains("Living Staff projection remains covered by BloodProjectionItem", staff, "BloodProjectionItem.projectFromEntity");
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

package com.vincenthuto.hemomancy.common.entity.mob.monster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DesiccantStingSourceTest {
	private static final Path ENTITY_SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/DesiccantEntity.java");
	private static final Path MODEL_SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/monster/DesiccantModel.java");
	private static final Path HEMOMANCY_REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

	private DesiccantStingSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String entity = read(ENTITY_SOURCE);
		String model = read(MODEL_SOURCE);
		String reference = read(HEMOMANCY_REFERENCE);

		assertContains("synced sting accessor", entity, "DATA_STING_TICKS");
		assertContains("sting duration constant", entity, "STING_ANIMATION_TICKS");
		assertContains("sting blood drain constant", entity, "STING_BLOOD_DRAIN");
		assertContains("blood loss effect", entity, "EffectInit.blood_loss");
		assertContains("active blood gate", entity, "volume.isActive()");
		assertContains("player blood volume access", entity, "HemoCapabilityAccess.getBloodVolume(player)");
		assertContains("sting drain", entity, "volume.drain(STING_BLOOD_DRAIN)");
		assertContains("blood volume sync", entity, "BloodVolumeEvents.syncVolume(serverPlayer, volume)");
		assertContains("sting attack start", entity, "startStingAttack()");
		assertContains("sting timer tick", entity, "setStingTicks(stingTicks - 1)");

		assertContains("separate telson model part", model, "private final ModelPart telson");
		assertContains("telson child", model, "getChild(\"telson\")");
		assertContains("sting progress drives model", model, "entity.getStingProgress()");
		assertContains("telson inflates", model, "telson.xScale");
		assertContains("telson red tint", model, "TELSON_STING_COLOR");

		assertContains("reference documents blood loss", reference, "Blood Loss");
		assertContains("reference documents blood sap", reference, "250 ml");
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

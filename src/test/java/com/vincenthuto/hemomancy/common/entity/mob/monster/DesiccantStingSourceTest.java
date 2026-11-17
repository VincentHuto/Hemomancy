package com.vincenthuto.hemomancy.common.entity.mob.monster;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class DesiccantStingSourceTest {
	private static final Path ENTITY_SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/DesiccantEntity.java");
	private static final Path MODEL_SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/DesiccantModel.java");
	private static final Path HEMOMANCY_REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

	private DesiccantStingSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String entity = read(ENTITY_SOURCE);
		String model = read(MODEL_SOURCE);
		String reference = read(HEMOMANCY_REFERENCE);

		assertContains("synced sting accessor", entity, "DATA_STING_TICKS");
		assertContains("sting duration constant", entity, "STING_ANIMATION_TICKS");
		assertContains("sting blood drain constant", entity, "STING_BLOOD_DRAIN");
		assertContains("blood loss effect", entity, "EffectInit.blood_loss");
		assertContains("active blood gate", entity, "volume.isActive()");
		assertContains("player blood volume access", entity, "HemoCapabilityAccess.getBloodVolume(player)");
		assertContains("sting drain is paid and synced through BloodAccounting", entity,
				"BloodAccounting.pay(serverPlayer, STING_BLOOD_DRAIN, STING)");
		assertContains("sting attack start", entity, "startStingAttack()");
		assertContains("sting timer tick", entity, "setStingTicks(stingTicks - 1)");

		assertContains("separate telson model part", model, "private final ModelPart telson");
		assertContains("telson child", model, "getChild(\"telson\")");
		assertContains("sting progress drives model", model, "entity.getStingProgress()");
		assertContains("telson inflates", model, "telson.xScale");

		assertContains("reference documents blood loss", reference, "Blood Loss");
		assertContains("reference documents blood sap", reference, "250 ml");
	}
}

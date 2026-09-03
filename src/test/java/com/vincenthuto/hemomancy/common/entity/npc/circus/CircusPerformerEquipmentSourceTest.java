package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.CircusKnifeProjectileEntity;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusPerformerEquipmentSourceTest {
	@Test
	void fireEaterCarriesTheRealLivingTorch() throws IOException {
		String entity = source("common/entity/npc/circus/CircusFireEaterEntity.java");
		String renderer = source("client/render/entity/npc/CircusPerformerRenderer.java");
		assertTrue(entity.contains("ItemInit.living_torch.get()"));
		assertTrue(entity.contains("EquipmentSlot.MAINHAND"));
		assertTrue(renderer.contains("ItemInHandLayer"));
	}

	@Test
	void thrownCircusKnivesAreBloodNeedleProjectiles() {
		assertTrue(BloodNeedleEntity.class.isAssignableFrom(CircusKnifeProjectileEntity.class));
	}

	private static String source(String relativePath) throws IOException {
		return Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/" + relativePath));
	}
}

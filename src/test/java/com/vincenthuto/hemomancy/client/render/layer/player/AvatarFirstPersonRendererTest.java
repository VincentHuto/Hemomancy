package com.vincenthuto.hemomancy.client.render.layer.player;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvatarFirstPersonRendererTest {
	@Test
	void rendersOnlyAnActiveAvatarInFirstPerson() {
		assertTrue(AvatarFirstPersonRenderer.shouldRender(true, "summon_avatar"));
		assertTrue(AvatarFirstPersonRenderer.shouldRender(true, "summon_avatar_complete"));
		assertFalse(AvatarFirstPersonRenderer.shouldRender(false, "summon_avatar_complete"));
		assertFalse(AvatarFirstPersonRenderer.shouldRender(true, ""));
		assertFalse(AvatarFirstPersonRenderer.shouldRender(true, "blood_shot"));
	}

	@Test
	void completeAvatarRendersItsHelmet() {
		assertFalse(AvatarFirstPersonRenderer.shouldRenderHelmet(3));
		assertTrue(AvatarFirstPersonRenderer.shouldRenderHelmet(4));
	}

	@Test
	void manifestedArmsRenderHeldItems() {
		assertFalse(AvatarFirstPersonRenderer.shouldRenderHeldItems(0));
		assertTrue(AvatarFirstPersonRenderer.shouldRenderHeldItems(1));
		assertTrue(AvatarFirstPersonRenderer.shouldRenderHeldItems(4));
	}

	@Test
	void manifestationTintStaysReadableNearTheCamera() {
		int exterior = BloodAvatarLayer.BLOOD_TRANSITION_COLOR;
		int firstPerson = BloodAvatarLayer.FIRST_PERSON_BLOOD_TRANSITION_COLOR;

		assertTrue((exterior >>> 24) <= 0x80);
		assertTrue((firstPerson >>> 24) <= 0x40);
		assertTrue(((firstPerson >>> 16) & 0xFF) < 0xB0);
	}

	@Test
	void avatarShellDoesNotWriteDepthOverHeldItems() throws IOException {
		String source = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/init/RenderTypeInit.java"));
		int start = source.indexOf("firstPersonEnergySwirl");
		int end = source.indexOf("public static RenderType", start + 1);
		String method = source.substring(start, end);
		assertTrue(method.contains("setWriteMaskState(COLOR_WRITE)"), method);
		assertFalse(method.contains("COLOR_DEPTH_WRITE"), method);
	}

	@Test
	void avatarLegStageRendersItsAuthoredFeet() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/model/armor/BloodAvatarModel.java"));

		assertTrue(source.contains("rightFoot.copyFrom(rightLeg)"), source);
		assertTrue(source.contains("leftFoot.copyFrom(leftLeg)"), source);
		assertTrue(source.contains("rightFoot.render(poseStack"), source);
		assertTrue(source.contains("leftFoot.render(poseStack"), source);
	}
}

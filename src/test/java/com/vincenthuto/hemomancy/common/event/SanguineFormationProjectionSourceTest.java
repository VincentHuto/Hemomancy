package com.vincenthuto.hemomancy.common.event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SanguineFormationProjectionSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private SanguineFormationProjectionSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String projectionItem = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java");
		String riteEvents = read("src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java");
		String packetHandler = read("src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java");
		String clientEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String handler = read("src/main/java/com/vincenthuto/hemomancy/common/event/SanguineFormationProjectionHandler.java");
		String clientData = read("src/main/java/com/vincenthuto/hemomancy/client/data/ActiveSanguineFormationProjectionClientData.java");
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/world/SanguineFormationProjectionRenderer.java");
		String skillPointInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/SkillPointInit.java");
		String coreBranch = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/CoreSkillBranch.java");
		String skillHelper = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/skill/SkillPointHelper.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		String tag = read("src/main/resources/data/hemomancy/tags/block/sanguine_formation_projectors.json");

		assertContains("projection item tries sanguine formation after structure feed", projectionItem,
				"SanguineFormationProjectionHandler.tryProjectAtLookTarget");
		assertBefore("formation projection should not steal structure projection targets", projectionItem,
				"BloodStructureFeedManager.feedStructure", "SanguineFormationProjectionHandler.tryProjectAtLookTarget");
		assertContains("server tick expires partial sanguine formation progress", riteEvents,
				"SanguineFormationProjectionHandler.tick(sLevel)");
		assertContains("sanguine formation packet is registered", packetHandler,
				"PacketSanguineFormationProjection.TYPE");
		assertContains("client ticks active sanguine formation projection visuals", clientEvents,
				"ActiveSanguineFormationProjectionClientData.tick()");
		assertContains("client renders sanguine formation projection visuals", clientEvents,
				"SanguineFormationProjectionRenderer.render");
		assertContains("successful formation crafts emit a glow particle burst", handler,
				"spawnSuccessBurst(level, spawnPos)");
		assertContains("success burst uses HutosLib glow particles", handler,
				"GlowParticleFactory.createData");
		assertContains("success burst uses Harbinger blood-red glow colors", handler,
				"new ParticleColor(185, 0, 0)");
		assertNotContains("success burst should not use vanilla glow particles", handler,
				"ParticleTypes.GLOW");
		assertNotContains("success burst should not use unstained-looking end rod particles", handler,
				"ParticleTypes.END_ROD");
		assertContains("failed formations emit a darker HutosLib glow fizz", handler,
				"spawnFailureFizz(level, faceCenter(key.pos, key.face))");
		assertContains("failure fizz uses dark blood glow colors", handler,
				"new ParticleColor(70, 0, 0)");
		assertContains("failure fizz particles move slowly", handler,
				"0.018D");
		assertContains("forming visuals expire shortly after projection stops", handler,
				"FORMING_VISIBLE_TICKS = 6");
		assertContains("completion packet clears the active orb immediately", clientData,
				"state == PacketSanguineFormationProjection.State.COMPLETE");
		assertContains("failure packet clears failed orb immediately", clientData,
				"state == PacketSanguineFormationProjection.State.FAILURE");
		assertContains("forming packet refreshes the existing orb instead of replacing it", clientData,
				"existing.refresh");
		assertContains("failure state resets later forming progress instead of pinning at full", clientData,
				"this.progress = progress");
		assertContains("forming visuals track refreshed ticks to prevent stop-cast brightening", clientData,
				"refreshedThisTick");
		assertContains("renderer reads stable client visibility instead of restarting age fade", renderer,
				"entry.getVisibility");
		assertContains("renderer coalesces size from projection progress", renderer,
				"coalesced = progress * progress");
		assertContains("renderer makes the core denser as progress increases", renderer,
				"coreA = (0.38F + 0.52F * progress)");
		assertNotContains("renderer should not keep shrinking after completion", renderer,
				"completionShrink");
		assertContains("skill init exposes sanguine crystallization", skillPointInit,
				"skill_sanguine_crystallization");
		assertContains("core branch registers sanguine crystallization", coreBranch,
				"new SkillPoint(48, \"skill_sanguine_crystallization\"");
		assertContains("core branch gates sanguine crystallization at degree one", coreBranch,
				"setRequiredDegree(1)");
		assertContains("skill helper exposes sanguine crystallization level", skillHelper,
				"getSanguineCrystallizationLevel");
		assertContains("lang has sanguine crystallization name", lang,
				"skill.hemomancy.skill_sanguine_crystallization");
		assertContains("lang explains projection acquisition", lang,
				"Project blood onto solid blocks to condense it into sanguine formation");
		assertContains("tag includes venous stone", tag,
				"hemomancy:venous_stone");
		assertContains("tag includes placed blood-stained stone", tag,
				"hemomancy:placed_blood_stained_stone");
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

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (unexpected '" + unexpected + "')");
		}
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0) {
			throw new AssertionError(label + " (missing first marker '" + first + "')");
		}
		if (secondIndex < 0) {
			throw new AssertionError(label + " (missing second marker '" + second + "')");
		}
		if (firstIndex > secondIndex) {
			throw new AssertionError(label + " (expected '" + first + "' before '" + second + "')");
		}
	}
}

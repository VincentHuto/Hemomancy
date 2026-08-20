package com.vincenthuto.hemomancy.common.event;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class TransientServerStateLifecycleTest {
	private static final List<String> REQUIRED_RESETS = List.of(
			"BloodManipulation.clearSessionState()",
			"StillArt.clearSessionState()",
			"SporiticThuribleResonanceState.clearSessionState()",
			"BloodwoodGrowthHandler.clearSessionState()",
			"EquippedMorphlingEvents.clearSessionState()",
			"ChummedWatersAreaManager.clearSessionState()",
			"BlackVeilCovenantManager.clearSessionState()",
			"TemporaryIceManager.clearSessionState()",
			"SanguineFormationProjectionHandler.clear()",
			"BloodStructureFeedManager.clear()",
			"HematicSalvageEvents.clearSessionState()",
			"RootedStateHelper.clearSessionState()",
			"ConserveStateHelper.clearSessionState()",
			"MuscleMemoryEvents.clearSessionState()",
			"MuscleMemoryWorldEvents.clearSessionState()");

	@Test
	void reportedCooldownMapsDiscardPriorWorldState() throws Exception {
		assertResetClears("com.vincenthuto.hemomancy.common.manipulation.BloodManipulation");
		assertResetClears("com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt");
	}

	@Test
	void bothServerBoundariesResetEveryAuditedOwner() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/event/TransientServerStateEvents.java"));
		assertTrue(source.contains("onServerAboutToStart(ServerAboutToStartEvent"));
		assertTrue(source.contains("onServerStopped(ServerStoppedEvent"));
		for (String reset : REQUIRED_RESETS) {
			assertTrue(source.contains(reset), "missing session reset: " + reset);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void assertResetClears(String className) throws Exception {
		Class<?> owner = Class.forName(className);
		Field field = owner.getDeclaredField("UNIVERSAL_COOLDOWN_MAP");
		field.setAccessible(true);
		Map map = (Map) field.get(null);
		map.put("prior-world", 99_999L);

		Method clear = owner.getMethod("clearSessionState");
		clear.invoke(null);
		assertTrue(map.isEmpty(), className + " retained the prior world's timestamp");
	}
}

package com.vincenthuto.hemomancy.common.event.worldevent;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class OrbOfPerspectiveIntegrationSourceTest {
	@Test
	void tossThresholdOwnershipOneShotEncounterAndReturnContracts() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/ChamberOfWillEvents.java"));
		assertTrue(source.contains("void onItemToss(ItemTossEvent event)"));
		assertTrue(source.contains("putUUID(OWNER_KEY, event.getPlayer().getUUID())"));
		assertTrue(source.contains("remove(HANDLED_KEY)"));
		assertTrue(source.indexOf("belowOrbPlane") < source.indexOf("belowRescuePlane(item.getY()"));
		assertTrue(source.contains("insideAllocatedCell(item.getX(), item.getZ()"));
		assertTrue(source.contains("!ChamberBoundaryRules.insidePlatform"));
		assertTrue(source.contains("VesperOrdealManager.isActive(owner)"));
		assertTrue(source.contains("MycophantEncounterManager.isActive(owner)"));
		assertTrue(source.indexOf("data.putBoolean(HANDLED_KEY, true)")
				< source.indexOf("manager.cycleAvailableSkyTheme(owner)"));
		assertTrue(source.contains("item.setUnlimitedLifetime()"));
		assertTrue(source.contains("item.setItem(ItemStack.EMPTY)"));
		assertTrue(source.contains("owner.getInventory().add(returned)"));
		assertTrue(source.contains("item.setItem(returned)"));
		assertTrue(source.contains("item.setDefaultPickUpDelay()"));
	}
}

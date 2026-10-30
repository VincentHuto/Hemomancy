package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

/**
 * A heart-node is the planter's personal Fane in the Gardens and a Founding Fane is a bloodline's: they never share
 * range, members, buffs or data, so neither system's events read the other's saved data.
 */
class HeartNodeFaneSeparationGuardrailTest {
	@Test
	void theFoundingFaneNeverReadsHeartNodeData() {
		String fane = SourceAssertions.readMainJava("common/event/worldevent/FoundingFaneEvents.java");
		SourceAssertions.assertNotContains("Founding Fane events", fane, "HeartNode");
	}

	@Test
	void theHeartNodeNeverReadsFoundingFaneData() {
		for (String file : new String[] {"common/event/HeartNodeEvents.java", "common/event/HeartNodeEnvelopeSync.java",
				"common/world/HeartNodeSavedData.java"}) {
			String source = SourceAssertions.readMainJava(file);
			SourceAssertions.assertNotContains(file, source, "FoundingFaneSavedData");
			SourceAssertions.assertNotContains(file, source, "FaneFootprint");
			SourceAssertions.assertNotContains(file, source, "Bloodline");
		}
	}
}

package com.vincenthuto.hemomancy.common.capability.block.vein;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** A claimed vein keeps its UUID across sync and reload, which Earthen Vein travel validation relies on. */
class VeinIdentitySourceTest {
	@Test
	void claimedVeinSyncPreservesIdentityUsedByTravelValidation() {
		String source = SourceAssertions.readMainJava("common/capability/block/vein/VeinLocation.java");

		assertTrue(source.contains("buf.writeUUID(getUUID())"), "vein sync drops the claimed UUID");
		assertTrue(source.contains("new VeinLocation(buf.readUUID()"), "vein sync regenerates the claimed UUID");
	}

	@Test
	void reloadDoesNotReplacePersistedVeinIdentity() {
		String source = SourceAssertions.readMainJava("common/tile/harbinger/functional/EarthenVeinBlockEntity.java");
		int firstTick = source.indexOf("if (!te.hasTicked)");
		int firstTickEnd = source.indexOf("te.setHasTicked(true)", firstTick);
		String initialization = source.substring(firstTick, firstTickEnd);

		assertTrue(initialization.contains("if (te.getLoc() == VeinLocation.BLANK)"),
				"vein reload path must only create a location when no persisted location exists");
	}
}

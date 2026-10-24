package com.vincenthuto.hemomancy.common.item.harbinger.bloodline;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertShowsEnglish;

class BloodlineFoundingCanonSourceTest {
	@Test
	void onlyTheFoundingRiteNamesABloodline() {
		String ledger = SourceAssertions.readMainJava("common/item/harbinger/bloodline/UnsignedLedgerItem.java");
		String rites = SourceAssertions.readMainJava("common/rite/harbinger/HarbingerCardinalRiteEvents.java")
				+ SourceAssertions.readMainJava("common/rite/harbinger/CardinalRiteCompletionEffects.java");
		assertNotContains("the ledger does not found a bloodline", ledger, "new Bloodline(bloodLineName");
		assertShowsEnglish("the ledger points to the founding rite", ledger,
				"Only the Bloodline Founding rite can write the first name");
		assertContains("the founding rite records the founding", rites, "degree.setHasFoundedBloodline(true)");
	}
}

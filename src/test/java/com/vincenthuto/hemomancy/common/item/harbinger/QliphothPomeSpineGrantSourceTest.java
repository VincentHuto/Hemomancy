package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertShowsEnglish;

class QliphothPomeSpineGrantSourceTest {
	@Test
	void communionGrantsTheSpineThroughPendingDelivery() {
		String source = SourceAssertions.readMainJava("common/item/harbinger/QliphothPomeItem.java");
		String delivery = SourceAssertions.readMainJava("common/rite/harbinger/QliphothBloomEvents.java");
		String bloom = SourceAssertions.readMainJava("common/block/harbinger/functional/QliphothBloomBlock.java");
		assertShowsEnglish("the bloom tells a Spine-less player where to look", bloom,
				"Spine: visit the Gardens. Lost it? Sneak-use empty-handed.");
		assertContains("the pome grants by count and the granted flag", source,
				"QliphothPomeRules.shouldGrantFungalSpine(count, degree.hasFungalSpineGranted())");
		assertContains("the pome hands off to the pending delivery", source, "QliphothBloomEvents.deliverPendingFungalSpine(player)");
		assertContains("delivery needs completed Communion", delivery, "degree.isQliphothCommunionDone() && !degree.hasFungalSpineGranted()");
		assertContains("delivery counts a carried Spine",
				delivery, "player -> player.getInventory().countItem(ItemInit.fungal_spine.get()) > 0");
		assertContains("delivery records the grant", delivery, "degree.setFungalSpineGranted(true)");
		assertContains("delivery goes through PendingDelivery", delivery, "PendingDelivery.deliver(player, FUNGAL_SPINE);");
		assertNotContains("the unique Spine never becomes a world drop", source, "player.drop(spine");
	}
}

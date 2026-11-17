package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

/**
 * Lignum Mortis after core-loop Track 2: a Sculpting of Projection over the shared {@code CrawlSession}, felling only
 * through {@code PlayerBlockEdits.breakBlock}, with the old R channel kept on the same crawl until review.
 */
class LignumMortisWiringTest {
	@Test
	void theSculptingCrawlsWhileBloodFlowsAndFellsOnRelease() {
		String sculpting = SourceAssertions.readMainJava("common/manipulation/mortem/LignumMortisSculpting.java");
		String init = SourceAssertions.readMainJava("common/init/ManipulationInit.java");
		SourceAssertions.assertContains("attached as a Projection Sculpting", init,
				".setProjectionSculpting(Sculptings::lignumMortis)");
		SourceAssertions.assertContains("the crawl advances on a paid pulse", sculpting, "session.advance(level,");
		SourceAssertions.assertContains("letting go fells", sculpting, "if (released) fell(player);");
		SourceAssertions.assertContains("a finished crawl fells at once", sculpting, "if (session.isDone())");
	}

	@Test
	void bothPathsShareTheCrawlAndFellThroughTheEditHelper() {
		String manipulation = SourceAssertions.readMainJava("common/manipulation/mortem/LignumMortisManip.java");
		String crawl = SourceAssertions.readMainJava("common/manipulation/verb/CrawlSession.java");
		SourceAssertions.assertContains("the R channel uses the shared crawl", manipulation, "new CrawlSession(");
		SourceAssertions.assertContains("only an explicit release fells on R", manipulation, "if (released && sessionLevel != null");
		SourceAssertions.assertContains("felling goes through PlayerBlockEdits", crawl, "PlayerBlockEdits.breakBlock(player, pos, tool)");
		SourceAssertions.assertNotContains("no raw destroyBlock in the felling", manipulation, "destroyBlock(");
		SourceAssertions.assertNotContains("no raw drop resources in the felling", manipulation, "dropResources(");
	}

	@Test
	void bloodFeedKeepsLegacyCallersAndAddsStableChannelFeeds() {
		String packet = SourceAssertions.readMainJava("common/network/capa/harbinger/PacketBloodStructureFeed.java");
		String clientData = SourceAssertions.readMainJava("client/data/ActiveBloodStructureFeedClientData.java");
		SourceAssertions.assertContains("legacy feed", packet, "this(positions, progress, visibleTicks, clear, 0L)");
		SourceAssertions.assertContains("channel id", packet, "buf.writeLong(msg.channelId)");
		SourceAssertions.assertContains("channel key", clientData, "channelId == 0L ? key(positions) : channelKey(channelId)");
	}

	@Test
	void castingUsesOrganicTendrilsInsteadOfLightning() {
		String manipulation = SourceAssertions.readMainJava("common/manipulation/mortem/LignumMortisManip.java");
		String visuals = SourceAssertions.readMainJava("common/manipulation/BloodFlowVisuals.java");
		SourceAssertions.assertContains("lignum tendril", manipulation, "BloodFlowVisuals.lignum");
		SourceAssertions.assertContains("tendril anchor", visuals, "new TendrilAnchor.Point(target)");
		SourceAssertions.assertContains("lignum style", visuals, "Style.LIGNUM");
		SourceAssertions.assertNotContains("no lightning", manipulation, "LightningTesterSpawner");
	}
}

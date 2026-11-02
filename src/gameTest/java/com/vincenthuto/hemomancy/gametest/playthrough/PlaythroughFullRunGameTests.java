package com.vincenthuto.hemomancy.gametest.playthrough;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * One player from a brand-new D0 to Degree 5 in a single test, with nothing between the bands but play. Each band's
 * {@code *ChainFromPreviousBand} test starts from a {@link PlaythroughStates} fixture, so drift between what one band
 * leaves and what the next one needs is invisible to them; here the player that leaves a band is the one that starts
 * the next. The bands run as {@link PlaythroughClock#segment segments}: each lays its own sites on the template floor
 * when its turn comes ({@link PlaythroughSites#clearForNextBand} clears the band before), plays its real steps, and
 * ends with the same comparison against the matching fixture state its own chain test makes.
 *
 * <p>{@link PlaythroughStates} is used only for {@code freshD0}; world and site setup and blood top-ups between bands
 * are labelled {@code ARRANGE} inside the chains. Required = false until it has been stable for a few runs.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughFullRunGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_full_run";

	private PlaythroughFullRunGameTests() {
	}

	/**
	 * D0 to D5 on one player: the Hermit and the Vicar's ceremony, the four First Bloodcraft proofs and the claim, First
	 * Separation and Concentrated Blood, the Woven Vessel, the Blood Shot weave and the Sanguine Brotherhood, then the
	 * Vein-Mason chapter and the Crimson Lodge. At every Degree boundary the player is compared with the fixture state
	 * the next band starts from.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 30000, batch = BATCH, required = false)
	public static void harbingerD0ToD5(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 6));
		PlaythroughStates.freshD0(player);
		PlaythroughClock.start(helper)
				.segment("D0 to D1", clock -> PlaythroughNeophyteGameTests.chain(helper, player, clock))
				.then(() -> boundary(helper, player, 1))
				.segment("D1 to D2", clock -> PlaythroughVotaryGameTests.chain(helper, player, clock))
				.then(() -> boundary(helper, player, 2))
				.segment("D2 to D3", clock -> PlaythroughInitiateGameTests.chain(helper, player, clock))
				.then(() -> boundary(helper, player, 3))
				.segment("D3 to D4", clock -> PlaythroughAdeptGameTests.chain(helper, player, clock))
				.then(() -> boundary(helper, player, 4))
				.segment("D4 to D5", clock -> PlaythroughAdeptGameTests.lodgeChain(helper, player, clock))
				.then(() -> {
					helper.assertTrue(player.degree() == 5, "the run ends at Degree 5, not " + player.degree());
					PlaythroughLog.note(helper, "full run", "ended at D5 with " + player.player().getInventory().items
							.stream().filter(stack -> !stack.isEmpty()).map(stack -> stack.getCount() + "x " + stack.getItem())
							.toList());
				})
				.run();
	}

	/** Between bands: the degree reached, a note of what the player carries on, and the floor cleared for the next. */
	private static void boundary(GameTestHelper helper, PlaythroughPlayer player, int degree) {
		helper.assertTrue(player.degree() == degree, "the band ends at Degree " + degree + ", not " + player.degree());
		PlaythroughLog.note(helper, "full run", "D" + degree + " reached; blood " + Math.round(player.blood())
				+ " mL; carried " + player.player().getInventory().items.stream().filter(stack -> !stack.isEmpty())
				.map(stack -> stack.getCount() + "x " + stack.getItem()).toList());
		PlaythroughSites.clearForNextBand(helper); // ARRANGE: the next band lays its own sites on the same floor
	}
}

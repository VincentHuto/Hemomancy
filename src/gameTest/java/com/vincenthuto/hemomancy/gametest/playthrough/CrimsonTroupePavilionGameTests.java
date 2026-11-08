package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.common.capability.PendingDelivery;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The Circus proofs the alpha gate runs (audit 2026-11-06, T-2), played through the playthrough harness in
 * {@code crimson_troupe_validation}: both real finales on the real pavilion (Succession with acts 2 to 4 ARRANGE),
 * discovery and passive acclimation on the real pavilion for living, dead, spectator and D0 visitors, the finale
 * rewards' {@code PendingDelivery} claim across death and a full pack, and a Ringmaster lost mid-finale releasing the
 * site. The bodies are {@link PlaythroughCrimsonTroupeGameTests}'s; the long band stays in {@code playthrough_validation}.
 * <p>
 * {@code playthrough_empty} is a copy of the playthrough suite's 32 x 12 x 24 {@code empty} template, which the
 * harness's stand height and site offsets assume.
 */
@GameTestHolder("crimson_troupe_validation")
@PrefixGameTestTemplate(false)
public final class CrimsonTroupePavilionGameTests {
	private static final String EMPTY = "playthrough_empty";
	private static final String BATCH = "crimson_troupe_play";
	private static final String PAVILION = "crimson_troupe_play_pavilion";
	private static final String LOST = "hemomancy.circus.finale.ringmaster_lost";

	private CrimsonTroupePavilionGameTests() {
	}

	/** The Succession finale on the real pavilion: rafters, the troupe downed, the descent, the Pattern and Topper. */
	@GameTest(template = EMPTY, timeoutTicks = 3000, batch = PAVILION + "_succession")
	public static void successionFinaleOnThePavilion(GameTestHelper helper) {
		PlaythroughCrimsonTroupeGameTests.successionFinale(helper, false);
	}

	/** The Liberation finale on the real pavilion: captives severed, anchors struck, Thread Ripper, Topper, ruin. */
	@GameTest(template = EMPTY, timeoutTicks = 2400, batch = PAVILION + "_liberation")
	public static void liberationFinaleOnThePavilion(GameTestHelper helper) {
		PlaythroughCrimsonTroupeGameTests.liberationFinale(helper);
	}

	/**
	 * AGENTS.md: pavilion observation and passive acclimation need a living, non-spectator player. Dead and spectator
	 * ticks add nothing and sync the player outside, earlier discovery is kept, a D0 visitor with inactive blood
	 * discovers and acclimates, and a camp or the abandoned stage is not the pavilion.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = PAVILION + "_discovery")
	public static void observationNeedsALivingNonSpectator(GameTestHelper helper) {
		PlaythroughCrimsonTroupeGameTests.pavilionDiscoveryAndAcclimation(helper);
	}

	/**
	 * The finale rewards are a {@code PendingDelivery} claim: owed while dead, kept through the death copy, delivered
	 * once on the respawn and never again; a full pack keeps the claim until a slot is free.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void finaleRewardsWaitForALivingPlayer(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughCrimsonTroupeGameTests.d5(helper, 8, 12);
		PlaythroughClock.start(helper)
				.then(() -> {
					p.die();
					helper.assertTrue(p.player().isDeadOrDying(), "the player is dead");
					CircusPlayerProgress.oweTopper(p.player()); // ARRANGE: the finale settled as the owner fell
					driverTick(p);
					helper.assertTrue(p.count(ItemInit.ringmaster_topper.get()) == 0 && owed(p) == 1,
							"a dead player keeps the Topper owed: owed " + owed(p));
					p.respawn();
					helper.assertTrue(p.count(ItemInit.ringmaster_topper.get()) == 1 && owed(p) == 0,
							"the respawn delivers the Topper once: carried " + p.count(ItemInit.ringmaster_topper.get())
									+ ", owed " + owed(p));
					driverTick(p);
					PendingDelivery.deliverAll(p.player());
					helper.assertTrue(p.count(ItemInit.ringmaster_topper.get()) == 1, "the Topper is never given twice");
					p.fillInventory(); // ARRANGE: a full pack
					CircusPlayerProgress.oweRipperMemory(p.player()); // ARRANGE: Liberation could not teach the memory
					helper.assertTrue(p.count(ItemInit.memory_thread_ripper.get()) == 0 && owed(p) == 1,
							"a full pack keeps the memory owed: owed " + owed(p));
					p.player().getInventory().setItem(0, ItemStack.EMPTY); // ARRANGE: a slot freed
					driverTick(p);
					driverTick(p);
					helper.assertTrue(p.count(ItemInit.memory_thread_ripper.get()) == 1 && owed(p) == 0,
							"the next living retry delivers the memory once: carried "
									+ p.count(ItemInit.memory_thread_ripper.get()) + ", owed " + owed(p));
				})
				.run();
	}

	/**
	 * A Ringmaster lost mid-finale (B-4) releases the site with a line to its owner: no owner, IDLE, NEUTRAL, the
	 * route kept and nothing awarded. Whether the site should settle or a Ringmaster return is decision CT-7.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH + "_ring")
	public static void lostRingmasterReleasesTheSite(GameTestHelper helper) {
		PlaythroughCrimsonTroupeGameTests.Ring ring = PlaythroughCrimsonTroupeGameTests.miniRing(helper);
		PlaythroughPlayer p = PlaythroughCrimsonTroupeGameTests.d5(helper, 16, 14);
		p.joinLevel();
		PlaythroughCrimsonTroupeGameTests.armUp(p, false); // ARRANGE
		PlaythroughStates.acclimated(p, 500); // ARRANGE
		PlaythroughStates.pavilionRoute(p, CircusRouteRules.Route.LIBERATION, 0b11); // ARRANGE
		CircusRingmasterEntity rm = ring.ringmaster();
		int[] mark = new int[1];
		PlaythroughClock.start(helper)
				.idle(2)
				.then(() -> {
					p.moveTo(ring.stand());
					p.talkTo(rm);
					p.choose(rm, CircusRingmasterEntity.EVENT_FINALE);
					helper.assertTrue(p.uuid().equals(PlaythroughCrimsonTroupeGameTests.site(ring.carousel()).activeOwner()),
							"the owner holds the site in the finale");
					mark[0] = p.mark();
					PlaythroughNpcs.kill(rm); // ARRANGE: the Ringmaster is lost (an admin kill)
				})
				.idle(2)
				.then(() -> {
					CircusPavilionSavedData.Site site = PlaythroughCrimsonTroupeGameTests.site(ring.carousel());
					helper.assertTrue(site.activeOwner() == null && site.phase() == CircusPavilionStateRules.Phase.IDLE
									&& site.outcome() == CircusPavilionStateRules.Outcome.NEUTRAL,
							"the lost Ringmaster releases the site unsettled: owner " + site.activeOwner() + ", phase "
									+ site.phase() + ", outcome " + site.outcome());
					helper.assertTrue(p.chatSince(mark[0]).stream().anyMatch(line -> line.has(LOST)),
							"the owner is told the Ringmaster is gone: " + p.chatSince(mark[0]));
					helper.assertTrue(CircusPlayerProgress.route(p.player()) == CircusRouteRules.Route.LIBERATION
									&& p.count(ItemInit.ringmaster_topper.get()) == 0 && owed(p) == 0,
							"the route is kept and nothing is awarded");
				})
				.run();
	}

	/** One server tick of the living (or dead) player on a {@code PendingDelivery} driver tick. */
	private static void driverTick(PlaythroughPlayer p) {
		p.player().tickCount = (p.player().tickCount / 20 + 1) * 20 - 1;
		PlaythroughCrimsonTroupeGameTests.live(p);
	}

	private static int owed(PlaythroughPlayer p) {
		return CircusPlayerProgress.FINALE_REWARDS.owed().apply(p.player()).stream().mapToInt(ItemStack::getCount).sum();
	}
}

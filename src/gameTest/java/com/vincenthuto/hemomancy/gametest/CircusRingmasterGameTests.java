package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformanceController;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusAcrobatEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperEncounterPuppetEvents;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFireEaterEntity;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonFactory;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.ThreadRipperManip;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

/** The Ringmaster's site, act and save rules, in the Circus batch so {@code runCrimsonTroupeGameTestServer} runs them. */
@GameTestHolder("crimson_troupe_validation")
@PrefixGameTestTemplate(false)
@SuppressWarnings("removal")
public final class CircusRingmasterGameTests {
	private static final TicketType<UUID> CAROUSEL_FIXTURE_TICKET =
			TicketType.create("hemomancy_carousel_fixture", UUID::compareTo);

	private CircusRingmasterGameTests() {
	}

	@GameTest(template = "empty", timeoutTicks = 120)
	public static void carouselSpawnsThreeBoundPuppeteerCaptives(GameTestHelper helper) {
		CircusCarouselEntity carousel = EntityInit.impaled_procession.get().create(helper.getLevel());
		helper.assertTrue(carousel != null, "Carousel fixture must create");
		carousel.setPos(helper.absolutePos(new BlockPos(4, 2, 4)).getCenter());
		helper.getLevel().addFreshEntity(carousel);
		var chunks = new java.util.HashSet<ChunkPos>();
		for (int x : new int[]{-4, 4}) for (int z : new int[]{-4, 4})
			chunks.add(new ChunkPos(carousel.blockPosition().offset(x, 0, z)));
		var source = helper.getLevel().getChunkSource();
		UUID ticketOwner = carousel.getUUID();
		chunks.forEach(chunk -> source.addRegionTicket(CAROUSEL_FIXTURE_TICKET, chunk, 2, ticketOwner, true));
		Runnable cleanup = () -> {
			java.util.List.copyOf(carousel.getPassengers()).forEach(net.minecraft.world.entity.Entity::discard);
			carousel.discard();
			chunks.forEach(chunk -> source.removeRegionTicket(CAROUSEL_FIXTURE_TICKET, chunk, 2, ticketOwner, true));
		};
		helper.testInfo.addListener(new GameTestListener() {
			public void testStructureLoaded(GameTestInfo test) { }
			public void testPassed(GameTestInfo test, GameTestRunner runner) { cleanup.run(); }
			public void testFailed(GameTestInfo test, GameTestRunner runner) { cleanup.run(); }
			public void testAddedForRerun(GameTestInfo oldTest, GameTestInfo newTest, GameTestRunner runner) { }
		});

		helper.startSequence().thenWaitUntil(() -> helper.assertTrue(!carousel.isRemoved() && carousel.tickCount > 0,
				"Carousel fixture must receive its first natural entity tick"))
				.thenIdle(3).thenExecute(() -> {
			String passengers = carousel.getPassengers().stream()
					.map(entity -> entity.getType().toString() + ":" + entity.isRemoved())
					.collect(java.util.stream.Collectors.joining(", "));
			helper.assertTrue(carousel.getPassengers().size() == 3
					&& carousel.getPassengers().stream().allMatch(BloodDrunkPuppeteerEntity.class::isInstance),
					"Carousel must carry three Blood Drunk Puppeteer captives; found "
							+ carousel.getPassengers().size() + " [" + passengers + "] at "
							+ helper.getLevel().getDifficulty());
			helper.assertTrue(carousel.getPassengers().stream()
					.allMatch(captive -> captive.getY() > carousel.getY() + 2.0D),
					"Carousel captives must sit above the horse bodies instead of standing on the platform: ticks="
							+ carousel.tickCount + ", indexed=" + (helper.getLevel().getEntity(carousel.getUUID()) == carousel)
							+ ", entitiesLoaded=" + helper.getLevel().areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(carousel.blockPosition()))
							+ ", pos=" + carousel.position() + ", riders=" + carousel.getPassengers().stream()
							.map(captive -> "ticks=" + captive.tickCount + ", pos=" + captive.position()
									+ ", indexed=" + (helper.getLevel().getEntity(captive.getUUID()) == captive))
							.collect(java.util.stream.Collectors.joining("; ")));
			helper.assertTrue(carousel.getPassengers().stream().allMatch(captive -> Math.abs(Math.hypot(
					captive.getX() - carousel.getX(), captive.getZ() - carousel.getZ())
					- CircusCarouselRules.HORSE_RADIUS - 0.55D) < 0.01D),
					"Carousel captives must sit behind the poles so their hands meet them");
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(EnthralledDollEntity.class,
					new AABB(carousel.blockPosition()).inflate(8.0D)).isEmpty(),
					"Carousel captives must not summon combat dolls");
		}).thenSucceed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void carouselCaptiveHorseSlotSyncsToClients(GameTestHelper helper) {
		BloodDrunkPuppeteerEntity serverCaptive = EntityInit.blood_drunk_puppeteer.get().create(helper.getLevel());
		BloodDrunkPuppeteerEntity clientCopy = EntityInit.blood_drunk_puppeteer.get().create(helper.getLevel());
		helper.assertTrue(serverCaptive != null && clientCopy != null, "Puppeteer fixtures must create");
		serverCaptive.bindToCarousel(2);
		var changes = serverCaptive.getEntityData().packDirty();
		if (changes != null) clientCopy.getEntityData().assignValues(changes);
		helper.assertTrue(clientCopy.getCarouselHorse() == 2,
				"Carousel horse slot must cross the server-to-client entity-data boundary");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void perchedRingmasterCannotBeMovedOrHarmed(GameTestHelper helper) {
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		Zombie attacker = EntityType.ZOMBIE.create(helper.getLevel());
		helper.assertTrue(ringmaster != null && attacker != null, "Ringmaster fixture must create");
		ringmaster.setPos(helper.absolutePos(new BlockPos(4, 4, 4)).getCenter());
		attacker.setPos(helper.absolutePos(new BlockPos(5, 2, 4)).getCenter());
		helper.getLevel().addFreshEntity(ringmaster);
		helper.getLevel().addFreshEntity(attacker);
		var position = ringmaster.position();
		float health = ringmaster.getHealth();
		ringmaster.push(1.0D, 1.0D, 1.0D);
		boolean hurt = ringmaster.hurt(ringmaster.damageSources().mobAttack(attacker), 8.0F);

		helper.runAfterDelay(5, () -> {
			helper.assertTrue(!hurt && ringmaster.getHealth() == health,
					"Perched Ringmaster must ignore ordinary damage");
			helper.assertTrue(ringmaster.position().equals(position) && ringmaster.getDeltaMovement().lengthSqr() == 0.0D,
					"Perched Ringmaster must remain fixed to the rafter");
			helper.assertTrue(ringmaster.isPickable() && !ringmaster.isAttackable() && ringmaster.getTarget() == null,
					"Perched Ringmaster must allow dialogue without exposing combat");
			ringmaster.discard();
			attacker.discard();
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void inactiveRingmasterPositionDoesNotDependOnAudienceAcclimation(GameTestHelper helper) {
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		CircusCarouselEntity carousel = EntityInit.impaled_procession.get().create(helper.getLevel());
		helper.assertTrue(ringmaster != null && carousel != null, "Circus fixtures must create");
		BlockPos perch = helper.absolutePos(new BlockPos(4, 5, 4));
		ringmaster.setPos(perch.getX() + 0.5D, perch.getY(), perch.getZ() + 0.5D);
		carousel.setPos(helper.absolutePos(new BlockPos(4, 2, 4)).getCenter());
		helper.getLevel().addFreshEntity(carousel);
		helper.getLevel().addFreshEntity(ringmaster);
		ServerPlayer audience = connectedPlayer(helper, "circus-audience",
				new BlockPos(perch.getX() + 2, perch.getY(), perch.getZ()));
		CircusPlayerProgress.addAcclimation(audience, 500);
		helper.getLevel().addNewPlayer(audience);

		ringmaster.tick();

		boolean stayedAtPerch = ringmaster.blockPosition().equals(perch);
		audience.discard();
		ringmaster.discard();
		carousel.getPassengers().forEach(net.minecraft.world.entity.Entity::discard);
		carousel.discard();
		helper.assertTrue(stayedAtPerch,
				"A player's acclimation must not move the shared Ringmaster entity for every nearby client");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void failedControlledActReleasesSiteAndCanBeRetried(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer player = connectedPlayer(helper, "circus-retry", origin);
		CircusPlayerProgress.chooseRoute(player, CircusRouteRules.Route.SUCCESSION);
		helper.assertTrue(CircusPerformanceController.beginNext(player, origin),
				"The first controlled act must begin through the production controller");

		player.setHealth(0.0F);
		CircusPerformanceController.onPlayerTick(new PlayerTickEvent.Post(player));
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());
		helper.assertTrue(data.site(helper.getLevel(), origin).activeOwner() == null
				&& data.site(helper.getLevel(), origin).phase() == CircusPavilionStateRules.Phase.IDLE,
				"Death during a controlled act must release the site without settling an outcome");
		helper.assertTrue(CircusPlayerProgress.route(player) == CircusRouteRules.Route.SUCCESSION
				&& CircusPlayerProgress.challenges(player) == 0,
				"A failed act must retain the route and must not grant challenge credit");

		player.setHealth(player.getMaxHealth());
		helper.assertTrue(CircusPerformanceController.beginNext(player, origin),
				"The same player must be able to retry after the failed attempt cleans up");
		player.setHealth(0.0F);
		CircusPerformanceController.onPlayerTick(new PlayerTickEvent.Post(player));
		player.discard();
		data.removeSite(helper.getLevel(), origin);
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void neutralPlayerCanBeginAttentionAct(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer player = connectedPlayer(helper, "circus-neutral-act", origin);
		CircusPlayerProgress.addAcclimation(player, 500);

		helper.assertTrue(CircusPerformanceController.beginNext(player, origin),
				"The attention act must be available before the player chooses a route");
		CircusPavilionSavedData.Site site = CircusPavilionSavedData.get(helper.getLevel())
				.site(helper.getLevel(), origin);
		helper.assertTrue(site.route() == CircusRouteRules.Route.NEUTRAL
				&& site.phase() == CircusPavilionStateRules.Phase.PERFORMANCE,
				"The pre-pact attention act must not silently choose Succession");

		player.setHealth(0.0F);
		CircusPerformanceController.onPlayerTick(new PlayerTickEvent.Post(player));
		player.discard();
		CircusPavilionSavedData.get(helper.getLevel()).removeSite(helper.getLevel(), origin);
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void routeChoiceRequiresCompletedAttentionAct(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer player = connectedPlayer(helper, "circus-pact-order", origin);
		CircusPlayerProgress.addAcclimation(player, 500);
		// ARRANGE: the D-30 pact gate (Degree 4 and one school report) is met; pactGateNamesWhatIsMissing covers it.
		com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(4);
		com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress.state(player)
				.putBoolean("quest.empty_ring", true);
		CircusCarouselEntity carousel = EntityInit.impaled_procession.get().create(helper.getLevel());
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		helper.assertTrue(carousel != null && ringmaster != null, "Circus fixtures must create");
		carousel.setPos(origin.getCenter());
		ringmaster.setPos(origin.getCenter().add(0.0D, 3.0D, 2.0D));
		helper.getLevel().addFreshEntity(carousel);
		helper.getLevel().addFreshEntity(ringmaster);

		ringmaster.handleChoice(player, CircusRingmasterEntity.EVENT_ACCEPT);
		helper.assertTrue(CircusPlayerProgress.route(player) == CircusRouteRules.Route.NEUTRAL,
				"A forged confirmation must not choose Succession before the attention act");
		CircusPlayerProgress.completeChallenge(player, 0, 150);
		ringmaster.handleChoice(player, CircusRingmasterEntity.EVENT_ACCEPT);
		helper.assertTrue(CircusPlayerProgress.route(player) == CircusRouteRules.Route.SUCCESSION,
				"Completing the attention act must unlock the warned route choice");

		player.discard();
		carousel.getPassengers().forEach(net.minecraft.world.entity.Entity::discard);
		carousel.discard();
		ringmaster.discard();
		CircusPavilionSavedData.get(helper.getLevel()).removeSite(helper.getLevel(), origin);
		helper.succeed();
	}

	/**
	 * D-30, D-31, NQ-15-6 B: the pact needs Degree 4 and any one school report, checked again at the finale; the Unstained
	 * may take Liberation only, on The Missing Understudy report with no Degree.
	 */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void pactGateNamesWhatIsMissing(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		CircusCarouselEntity carousel = EntityInit.impaled_procession.get().create(helper.getLevel());
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		helper.assertTrue(carousel != null && ringmaster != null, "Circus fixtures must create");
		carousel.setPos(origin.getCenter());
		ringmaster.setPos(origin.getCenter().add(0.0D, 3.0D, 2.0D));
		helper.getLevel().addFreshEntity(carousel);
		helper.getLevel().addFreshEntity(ringmaster);
		var degree = (java.util.function.BiConsumer<ServerPlayer, Integer>) (p, d) ->
				com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getInitiatoryDegree(p).setDegreeNumber(d);
		var school = (java.util.function.Function<ServerPlayer, CompoundTag>)
				com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress::state;

		ServerPlayer harbinger = connectedPlayer(helper, "circus-pact-gate", origin);
		CircusPlayerProgress.addAcclimation(harbinger, 500);
		CircusPlayerProgress.completeChallenge(harbinger, 0, 0);
		degree.accept(harbinger, 3);
		school.apply(harbinger).putBoolean("quest.wrong_audience", true);
		helper.assertTrue(CircusRingmasterEntity.pactGate(harbinger, CircusRouteRules.Route.SUCCESSION)
				== CircusRouteRules.PactGate.NEEDS_DEGREE, "a Degree 3 visitor with a report is refused for the Degree");
		ringmaster.handleChoice(harbinger, CircusRingmasterEntity.EVENT_ACCEPT);
		ringmaster.handleChoice(harbinger, CircusRingmasterEntity.EVENT_REJECT);
		helper.assertTrue(CircusPlayerProgress.route(harbinger) == CircusRouteRules.Route.NEUTRAL,
				"a forged pact below Degree 4 must not choose a route");
		degree.accept(harbinger, 4);
		school.apply(harbinger).remove("quest.wrong_audience");
		helper.assertTrue(CircusRingmasterEntity.pactGate(harbinger, CircusRouteRules.Route.LIBERATION)
				== CircusRouteRules.PactGate.NEEDS_REPORT, "a Degree 4 visitor with no report is refused for the report");
		ringmaster.handleChoice(harbinger, CircusRingmasterEntity.EVENT_REJECT);
		helper.assertTrue(CircusPlayerProgress.route(harbinger) == CircusRouteRules.Route.NEUTRAL,
				"a forged pact with no school report must not choose a route");
		school.apply(harbinger).putBoolean("quest.wrong_audience", true);
		ringmaster.handleChoice(harbinger, CircusRingmasterEntity.EVENT_REJECT);
		helper.assertTrue(CircusPlayerProgress.route(harbinger) == CircusRouteRules.Route.LIBERATION,
				"Degree 4 and any one report open the pact");
		school.apply(harbinger).remove("quest.wrong_audience");
		ringmaster.handleChoice(harbinger, CircusRingmasterEntity.EVENT_FINALE);
		helper.assertTrue(CircusPavilionSavedData.get(helper.getLevel()).site(helper.getLevel(), origin).activeOwner() == null,
				"the finale rechecks the gate and refuses a visitor who no longer meets it");

		ServerPlayer unstained = connectedPlayer(helper, "circus-pact-cleansed", origin);
		CircusPlayerProgress.addAcclimation(unstained, 500);
		CircusPlayerProgress.completeChallenge(unstained, 0, 0);
		com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getUnstainedProgress(unstained).setBaselineRestored(true);
		degree.accept(unstained, 0);
		school.apply(unstained).putBoolean("quest.empty_ring", true);
		ringmaster.handleChoice(unstained, CircusRingmasterEntity.EVENT_REJECT);
		helper.assertTrue(CircusPlayerProgress.route(unstained) == CircusRouteRules.Route.NEUTRAL,
				"an Unstained Liberator needs The Missing Understudy, not any report");
		school.apply(unstained).putBoolean("quest.missing_understudy", true);
		ringmaster.handleChoice(unstained, CircusRingmasterEntity.EVENT_ACCEPT);
		helper.assertTrue(CircusPlayerProgress.route(unstained) == CircusRouteRules.Route.NEUTRAL,
				"the Unstained may never take Succession");
		ringmaster.handleChoice(unstained, CircusRingmasterEntity.EVENT_REJECT);
		helper.assertTrue(CircusPlayerProgress.route(unstained) == CircusRouteRules.Route.LIBERATION,
				"an Unstained Liberator at Degree 0 with The Missing Understudy may take Liberation");
		ringmaster.handleChoice(unstained, CircusRingmasterEntity.EVENT_REPAIR);
		helper.assertTrue(CircusPlayerProgress.route(unstained) == CircusRouteRules.Route.LIBERATION,
				"an Unstained Liberator cannot repair the route into Succession");

		harbinger.discard();
		unstained.discard();
		carousel.getPassengers().forEach(net.minecraft.world.entity.Entity::discard);
		carousel.discard();
		ringmaster.discard();
		CircusPavilionSavedData.get(helper.getLevel()).removeSite(helper.getLevel(), origin);
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void expiredControlledActCannotAwardProgress(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer staleOwner = connectedPlayer(helper, "circus-stale-owner", origin);
		ServerPlayer successor = connectedPlayer(helper, "circus-successor", origin);
		CircusPlayerProgress.chooseRoute(staleOwner, CircusRouteRules.Route.SUCCESSION);
		CircusPlayerProgress.completeChallenge(staleOwner, 0, 0);
		CircusPlayerProgress.completeChallenge(staleOwner, 1, 0);
		helper.assertTrue(CircusPerformanceController.beginNext(staleOwner, origin),
				"The controlled act fixture must begin");
		com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.circusProgress(staleOwner).setActTicks(199);
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());
		data.reset(helper.getLevel(), origin, staleOwner.getUUID());
		helper.assertTrue(data.beginPerformance(helper.getLevel(), origin, successor.getUUID(),
				CircusRouteRules.Route.SUCCESSION), "A successor must be able to claim the released pavilion");

		CircusPerformanceController.onPlayerTick(new PlayerTickEvent.Post(staleOwner));
		helper.assertTrue(CircusPlayerProgress.challenges(staleOwner) == 0b00011,
				"An expired act must not award challenge credit from stale player data");
		helper.assertTrue(CircusPerformanceController.activeChallenge(staleOwner) < 0,
				"An expired act must clear the stale player timer");
		helper.assertTrue(successor.getUUID().equals(data.site(helper.getLevel(), origin).activeOwner()),
				"Cleaning stale player data must not release the successor's attempt");

		staleOwner.discard();
		successor.discard();
		data.removeSite(helper.getLevel(), origin);
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void controlledPerformerOwnershipSurvivesEntityReload(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer owner = connectedPlayer(helper, "circus-reload-owner", origin);
		ServerPlayer intruder = connectedPlayer(helper, "circus-reload-intruder", origin.offset(1, 0, 0));
		CircusAcrobatEntity original = EntityInit.circus_acrobat.get().create(helper.getLevel());
		CircusAcrobatEntity loaded = EntityInit.circus_acrobat.get().create(helper.getLevel());
		helper.assertTrue(original != null && loaded != null, "Performer fixtures must create");
		original.beginFinale(owner, false);
		CompoundTag stored = new CompoundTag();
		original.addAdditionalSaveData(stored);
		loaded.readAdditionalSaveData(stored);
		loaded.setPos(origin.getCenter());
		helper.getLevel().addFreshEntity(loaded);

		boolean intruderHit = loaded.hurt(loaded.damageSources().playerAttack(intruder), 1.0F);
		helper.assertTrue(!intruderHit,
				"A reloaded controlled performer must still reject damage from anyone except its act owner");

		owner.discard();
		intruder.discard();
		original.discard();
		loaded.discard();
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void pavilionOwnershipAndOutcomeSurviveSavedDataRoundTrip(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		UUID owner = UUID.randomUUID();
		UUID intruder = UUID.randomUUID();
		CircusPavilionSavedData data = new CircusPavilionSavedData();

		helper.assertTrue(data.begin(helper.getLevel(), origin, owner, CircusRouteRules.Route.SUCCESSION),
				"An unsettled pavilion must accept its first owner");
		helper.assertTrue(!data.begin(helper.getLevel(), origin, intruder, CircusRouteRules.Route.LIBERATION),
				"A second player must not steal an active pavilion");
		helper.assertTrue(data.complete(helper.getLevel(), origin, owner,
				CircusPavilionStateRules.Outcome.SUCCESSION), "The active owner must be able to settle the pavilion");

		CompoundTag saved = data.save(new CompoundTag(), helper.getLevel().registryAccess());
		CircusPavilionSavedData.Site restored = CircusPavilionSavedData
				.load(saved, helper.getLevel().registryAccess()).site(helper.getLevel(), origin);
		helper.assertTrue(restored.activeOwner() == null && owner.equals(restored.completionOwner())
				&& restored.route() == CircusRouteRules.Route.SUCCESSION
				&& restored.outcome() == CircusPavilionStateRules.Outcome.SUCCESSION
				&& restored.phase() == CircusPavilionStateRules.Phase.COMPLETE,
				"Completion owner, route, outcome, and phase must survive a server save/reload");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void missingPavilionOwnerExpiresAfterGracePeriod(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		UUID disconnectedOwner = UUID.randomUUID();
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());
		helper.assertTrue(data.begin(helper.getLevel(), origin, disconnectedOwner,
				CircusRouteRules.Route.SUCCESSION), "Pavilion attempt must begin");
		CircusCarouselEntity carousel = EntityInit.impaled_procession.get().create(helper.getLevel());
		helper.assertTrue(carousel != null, "Carousel fixture must create");
		carousel.setPos(origin.getCenter());
		helper.getLevel().addFreshEntity(carousel);

		for (int tick = 0; tick < 199; tick++) carousel.tick();
		helper.assertTrue(disconnectedOwner.equals(data.site(helper.getLevel(), origin).activeOwner()),
				"A brief disconnect must retain the active attempt");
		carousel.tick();
		helper.assertTrue(data.site(helper.getLevel(), origin).activeOwner() == null
				&& data.site(helper.getLevel(), origin).phase() == CircusPavilionStateRules.Phase.IDLE,
				"An owner absent for 200 loaded carousel ticks must release the pavilion for retry");

		carousel.getPassengers().forEach(net.minecraft.world.entity.Entity::discard);
		carousel.discard();
		data.removeSite(helper.getLevel(), origin);
		helper.succeed();
	}

	/** D-34: in DESCENT a puppet's blow counts as its owner's; a stranger's blow, or their puppet's, does not. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void ownersPuppetHitCountsInDescent(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer owner = connectedPlayer(helper, "circus-puppet-owner", origin.offset(2, 0, 0));
		ServerPlayer stranger = connectedPlayer(helper, "circus-puppet-stranger", origin.offset(-2, 0, 0));
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		helper.assertTrue(ringmaster != null, "Ringmaster fixture must create");
		ringmaster.setPos(origin.getCenter());
		CompoundTag saved = new CompoundTag();
		ringmaster.addAdditionalSaveData(saved);
		saved.putLong("CircusOrigin", origin.asLong());
		ringmaster.readAdditionalSaveData(saved);
		helper.getLevel().addFreshEntity(ringmaster);
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());
		data.begin(helper.getLevel(), origin, owner.getUUID(), CircusRouteRules.Route.LIBERATION);
		data.setPhase(helper.getLevel(), origin, CircusPavilionStateRules.Phase.DESCENT);
		var definition = PuppeteerSummonDefinitions.byName(PuppeteerSummonDefinitions.GOREBOUND_HULK).orElseThrow();
		var puppet = PuppeteerSummonFactory.create(definition, helper.getLevel(), owner, UUID.randomUUID(), 0).orElseThrow();
		var strangers = PuppeteerSummonFactory.create(definition, helper.getLevel(), stranger, UUID.randomUUID(), 0).orElseThrow();
		puppet.setPos(origin.offset(1, 0, 0).getCenter());
		strangers.setPos(origin.offset(-1, 0, 0).getCenter());
		helper.getLevel().addFreshEntity(puppet);
		helper.getLevel().addFreshEntity(strangers);

		float health = ringmaster.getHealth();
		helper.assertTrue(!ringmaster.hurt(ringmaster.damageSources().playerAttack(stranger), 2.0F)
				&& !ringmaster.hurt(ringmaster.damageSources().mobAttack(strangers), 2.0F)
				&& ringmaster.getHealth() == health, "A stranger, or a stranger's puppet, must not wound him in DESCENT");
		helper.assertTrue(ringmaster.hurt(ringmaster.damageSources().mobAttack(puppet), 2.0F)
				&& ringmaster.getHealth() < health, "The owner's puppet must wound him in DESCENT");

		puppet.discard();
		strangers.discard();
		ringmaster.discard();
		owner.discard();
		stranger.discard();
		data.removeSite(helper.getLevel(), origin);
		helper.succeed();
	}

	/** D-34: in a finale the Fire Breather's cone burns its owner's body and spares a bystander beside it. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void finaleConeSparesABystander(GameTestHelper helper) throws ReflectiveOperationException {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer owner = connectedPlayer(helper, "circus-cone-owner", origin.offset(-3, 0, 0));
		var definition = PuppeteerSummonDefinitions.byName(PuppeteerSummonDefinitions.GOREBOUND_HULK).orElseThrow();
		var body = PuppeteerSummonFactory.create(definition, helper.getLevel(), owner, UUID.randomUUID(), 0).orElseThrow();
		Villager bystander = EntityType.VILLAGER.create(helper.getLevel());
		CircusFireEaterEntity breather = EntityInit.circus_fire_eater.get().create(helper.getLevel());
		helper.assertTrue(bystander != null && breather != null, "Cone fixtures must create");
		body.setNoAi(true);
		body.setPos(origin.getX() + 3.5D, origin.getY(), origin.getZ() + 0.5D);
		bystander.setNoAi(true);
		bystander.setPos(origin.getX() + 3.5D, origin.getY(), origin.getZ() + 1.5D);
		breather.setNoAi(true);
		breather.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);
		helper.getLevel().addFreshEntity(body);
		helper.getLevel().addFreshEntity(bystander);
		helper.getLevel().addFreshEntity(breather);
		breather.beginFinale(owner, false);
		var tickDefense = CircusFireEaterEntity.class.getDeclaredMethod("tickDefense", LivingEntity.class);
		var strikeDefense = CircusFireEaterEntity.class.getDeclaredMethod("strikeDefense", LivingEntity.class);
		tickDefense.setAccessible(true);
		strikeDefense.setAccessible(true);
		tickDefense.invoke(breather, body);
		float bodyHealth = body.getHealth();
		strikeDefense.invoke(breather, body);

		helper.assertTrue(body.getHealth() < bodyHealth, "The cone must still burn the body its finale's owner holds");
		helper.assertTrue(bystander.getHealth() == bystander.getMaxHealth() && !bystander.isOnFire(),
				"The cone must spare a bystander during the finale");
		breather.discard();
		bystander.discard();
		body.discard();
		owner.discard();
		helper.succeed();
	}

	/** D-36: a vacant site survives a save and reload, and the Waybill reads it as done. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void vacantSiteSurvivesSavedDataRoundTrip(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		CircusPavilionSavedData data = new CircusPavilionSavedData();
		data.site(helper.getLevel(), origin);
		helper.assertTrue(!data.isCompletedNear(helper.getLevel(), origin), "A fresh site must read as unfinished");
		data.setVacant(helper.getLevel(), origin, true);
		helper.assertTrue(data.isCompletedNear(helper.getLevel(), origin), "A vacant site must read as done for the Waybill");
		CircusPavilionSavedData restored = CircusPavilionSavedData.load(
				data.save(new CompoundTag(), helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
		helper.assertTrue(restored.site(helper.getLevel(), origin).vacant()
				&& restored.site(helper.getLevel(), origin).outcome() == CircusPavilionStateRules.Outcome.NEUTRAL
				&& restored.isCompletedNear(helper.getLevel(), origin), "Vacancy must survive a save and reload");
		helper.succeed();
	}

	/** D-35: Thread Ripper treats Vesper's encounter bodies as protected; an unmarked body is not. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void threadRipperSparesVespersBodies(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer caster = connectedPlayer(helper, "circus-ripper", origin);
		Zombie vesperBody = EntityType.ZOMBIE.create(helper.getLevel());
		Zombie plain = EntityType.ZOMBIE.create(helper.getLevel());
		helper.assertTrue(vesperBody != null && plain != null, "Body fixtures must create");
		vesperBody.getPersistentData().putBoolean(VesperEncounterPuppetEvents.PUPPET_KEY, true);
		helper.assertTrue(VesperEncounterPuppetEvents.isEncounterPuppet(vesperBody)
				&& ThreadRipperManip.protectedBody(caster, vesperBody), "Vesper's body must be protected from Thread Ripper");
		helper.assertTrue(!ThreadRipperManip.protectedBody(caster, plain), "An unmarked body must not be protected");
		caster.discard();
		helper.succeed();
	}

	private static ServerPlayer connectedPlayer(GameTestHelper helper, String name, BlockPos position) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), name), false);
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				cookie.gameProfile(), ClientInformation.createDefault());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override public void send(Packet<?> packet) { }
		};
		player.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
		helper.getLevel().addNewPlayer(player);
		return player;
	}
}

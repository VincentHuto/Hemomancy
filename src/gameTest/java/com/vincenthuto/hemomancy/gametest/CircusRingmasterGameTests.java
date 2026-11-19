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

import java.util.List;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.CircusFinaleSettlement;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;

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
		// The attention act opens on acclimation (no route is chosen before it since 2026-11-18).
		CircusPlayerProgress.addAcclimation(player, 500);
		helper.assertTrue(CircusPerformanceController.beginNext(player, origin),
				"The first controlled act must begin through the production controller");

		player.setHealth(0.0F);
		CircusPerformanceController.onPlayerTick(new PlayerTickEvent.Post(player));
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());
		helper.assertTrue(data.site(helper.getLevel(), origin).activeOwner() == null
				&& data.site(helper.getLevel(), origin).phase() == CircusPavilionStateRules.Phase.IDLE,
				"Death during a controlled act must release the site without settling an outcome");
		helper.assertTrue(CircusPlayerProgress.route(player) == CircusRouteRules.Route.NEUTRAL
				&& CircusPlayerProgress.challenges(player) == 0,
				"A failed act must settle nothing and must not grant challenge credit");

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

	/**
	 * 2026-11-18: no route is chosen before the finale. His dialogue's finale event is refused until the attention act is
	 * done and the pact gate is open; a forged event before then holds nothing.
	 */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void finaleGateNeedsTheAttentionActAndThePact(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer player = connectedPlayer(helper, "circus-finale-gate", origin);
		CircusPlayerProgress.addAcclimation(player, 500);
		CircusCarouselEntity carousel = EntityInit.impaled_procession.get().create(helper.getLevel());
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		helper.assertTrue(carousel != null && ringmaster != null, "Circus fixtures must create");
		carousel.setPos(origin.getCenter());
		ringmaster.setPos(origin.getCenter().add(0.0D, 3.0D, 2.0D));
		helper.getLevel().addFreshEntity(carousel);
		helper.getLevel().addFreshEntity(ringmaster);
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());

		ringmaster.handleChoice(player, CircusRingmasterEntity.EVENT_FINALE);
		helper.assertTrue(data.site(helper.getLevel(), carousel.encounterOrigin()).activeOwner() == null
				&& !RingmasterFinaleDirector.isInFinale(player), "A forged finale before the attention act must hold nothing");
		CircusPlayerProgress.completeChallenge(player, 0, 150);
		ringmaster.handleChoice(player, CircusRingmasterEntity.EVENT_FINALE);
		helper.assertTrue(data.site(helper.getLevel(), carousel.encounterOrigin()).activeOwner() == null
				&& !RingmasterFinaleDirector.isInFinale(player), "Without Degree 4 and a school report the finale stays shut");
		helper.assertTrue(CircusPlayerProgress.route(player) == CircusRouteRules.Route.NEUTRAL,
				"Asking for the finale never chooses a route");

		player.discard();
		carousel.getPassengers().forEach(net.minecraft.world.entity.Entity::discard);
		carousel.discard();
		ringmaster.discard();
		data.removeSite(helper.getLevel(), origin);
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
		// 2026-11-18: no route is chosen before the fight; the gate decides whether the finale opens at all.
		helper.assertTrue(CircusRingmasterEntity.pactGate(harbinger) == CircusRouteRules.PactGate.NEEDS_DEGREE,
				"a Degree 3 visitor with a report is refused for the Degree");
		ringmaster.handleChoice(harbinger, CircusRingmasterEntity.EVENT_FINALE);
		helper.assertTrue(CircusPavilionSavedData.get(helper.getLevel()).site(helper.getLevel(), origin).activeOwner() == null,
				"a forged finale below Degree 4 must not begin");
		degree.accept(harbinger, 4);
		school.apply(harbinger).remove("quest.wrong_audience");
		helper.assertTrue(CircusRingmasterEntity.pactGate(harbinger) == CircusRouteRules.PactGate.NEEDS_REPORT,
				"a Degree 4 visitor with no report is refused for the report");
		ringmaster.handleChoice(harbinger, CircusRingmasterEntity.EVENT_FINALE);
		helper.assertTrue(CircusPavilionSavedData.get(helper.getLevel()).site(helper.getLevel(), origin).activeOwner() == null,
				"a forged finale with no school report must not begin");
		school.apply(harbinger).putBoolean("quest.wrong_audience", true);
		helper.assertTrue(CircusRingmasterEntity.pactGate(harbinger) == CircusRouteRules.PactGate.OPEN,
				"Degree 4 and any one report open the finale");
		helper.assertTrue(CircusPlayerProgress.route(harbinger) == CircusRouteRules.Route.NEUTRAL,
				"opening the gate chooses no route");

		ServerPlayer unstained = connectedPlayer(helper, "circus-pact-cleansed", origin);
		CircusPlayerProgress.addAcclimation(unstained, 1000);
		CircusPlayerProgress.completeChallenge(unstained, 0, 0);
		com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getUnstainedProgress(unstained).setBaselineRestored(true);
		degree.accept(unstained, 0);
		school.apply(unstained).putBoolean("quest.empty_ring", true);
		helper.assertTrue(CircusRingmasterEntity.pactGate(unstained) == CircusRouteRules.PactGate.NEEDS_UNDERSTUDY,
				"the Unstained need The Missing Understudy, not any report");
		school.apply(unstained).putBoolean("quest.missing_understudy", true);
		helper.assertTrue(CircusRingmasterEntity.pactGate(unstained) == CircusRouteRules.PactGate.OPEN,
				"the Unstained at Degree 0 with The Missing Understudy may fight him");
		helper.assertTrue(!CircusRouteRules.mayAbsorb(CircusPlayerProgress.acclimation(unstained), 0b1_1111, true),
				"the Unstained may never take his place");

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
		CircusPlayerProgress.completeChallenge(staleOwner, 0, 0);
		// The doll act after the attention act opens through the finale's gate: Degree 4 and one school report.
		com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getInitiatoryDegree(staleOwner).setDegreeNumber(4);
		com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress.state(staleOwner)
				.putBoolean("quest.empty_ring", true);
		helper.assertTrue(CircusPerformanceController.beginNext(staleOwner, origin),
				"The controlled act fixture must begin");
		com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.circusProgress(staleOwner).setActTicks(199);
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());
		data.reset(helper.getLevel(), origin, staleOwner.getUUID());
		helper.assertTrue(data.beginPerformance(helper.getLevel(), origin, successor.getUUID(),
				CircusRouteRules.Route.NEUTRAL), "A successor must be able to claim the released pavilion");

		CircusPerformanceController.onPlayerTick(new PlayerTickEvent.Post(staleOwner));
		helper.assertTrue(CircusPlayerProgress.challenges(staleOwner) == 0b00001,
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

		helper.assertTrue(data.begin(helper.getLevel(), origin, owner, CircusRouteRules.Route.NEUTRAL),
				"An unsettled pavilion must accept its first owner");
		helper.assertTrue(!data.begin(helper.getLevel(), origin, intruder, CircusRouteRules.Route.NEUTRAL),
				"A second player must not steal an active pavilion");
		helper.assertTrue(data.complete(helper.getLevel(), origin, owner,
				CircusPavilionStateRules.Outcome.SUCCESSION), "The active owner must be able to settle the pavilion");

		CompoundTag saved = data.save(new CompoundTag(), helper.getLevel().registryAccess());
		CircusPavilionSavedData.Site restored = CircusPavilionSavedData
				.load(saved, helper.getLevel().registryAccess()).site(helper.getLevel(), origin);
		helper.assertTrue(restored.activeOwner() == null && owner.equals(restored.completionOwner())
				&& restored.route() == CircusRouteRules.Route.NEUTRAL
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
				CircusRouteRules.Route.NEUTRAL), "Pavilion attempt must begin");
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

	/**
	 * 2026-11-18: while a finale in his Chamber holds his site, the real Ringmaster stands still and untouchable, and
	 * anyone who speaks to him gets the line that he is elsewhere. The finale is held here without a Chamber, as its
	 * director holds a begun one.
	 */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void heldSiteFreezesTheRealRingmaster(GameTestHelper helper) throws ReflectiveOperationException {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		List<Component> heard = new java.util.ArrayList<>();
		ServerPlayer visitor = listeningPlayer(helper, "circus-held-visitor", origin.offset(2, 0, 0), heard);
		CircusPlayerProgress.addAcclimation(visitor, 500);
		CircusCarouselEntity carousel = EntityInit.impaled_procession.get().create(helper.getLevel());
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		helper.assertTrue(carousel != null && ringmaster != null, "Circus fixtures must create");
		carousel.setPos(origin.getCenter());
		ringmaster.setPos(origin.getCenter().add(0.0D, 3.0D, 2.0D));
		helper.getLevel().addFreshEntity(carousel);
		helper.getLevel().addFreshEntity(ringmaster);
		UUID owner = UUID.randomUUID();
		hold(helper, owner, carousel.encounterOrigin());
		try {
			helper.assertTrue(RingmasterFinaleDirector.holds(helper.getLevel().dimension(), carousel.encounterOrigin()),
					"The finale holds the site");
			var perch = ringmaster.position();
			ringmaster.tick();
			ringmaster.tick();
			helper.assertTrue(ringmaster.position().distanceTo(perch) < 1.0E-3D, "The held Ringmaster does not move");
			helper.assertTrue(!ringmaster.hurt(ringmaster.damageSources().playerAttack(visitor), 4.0F)
					&& ringmaster.getHealth() == ringmaster.getMaxHealth(), "Nobody harms the held Ringmaster");
			ringmaster.interact(visitor, InteractionHand.MAIN_HAND);
			helper.assertTrue(heard.stream().anyMatch(line -> line.getContents() instanceof TranslatableContents contents
					&& contents.getKey().equals("hemomancy.circus.finale.elsewhere")),
					"Speaking to him gets the elsewhere line: " + heard);
		} finally {
			release(owner);
		}
		visitor.discard();
		carousel.getPassengers().forEach(net.minecraft.world.entity.Entity::discard);
		carousel.discard();
		ringmaster.discard();
		CircusPavilionSavedData.get(helper.getLevel()).removeSite(helper.getLevel(), origin);
		helper.succeed();
	}

	/**
	 * A kill at his kneel, laid on the real site as the finale's return lays it: the pavilion is ruined, the route is
	 * Liberation's, Thread Ripper is learnt and the real Ringmaster dies; the troupe's finale state clears.
	 */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void killAtTheKneelRuinsTheSite(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer player = connectedPlayer(helper, "circus-kill-ending", origin);
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(4);
		Fixture fixture = settlementFixture(helper, origin, player);
		CircusFinaleSettlement.apply(helper.getLevel(), fixture.carousel.encounterOrigin(), player, FinaleRules.Ending.KILLED);
		CircusPavilionSavedData.Site site = CircusPavilionSavedData.get(helper.getLevel())
				.site(helper.getLevel(), fixture.carousel.encounterOrigin());
		helper.assertTrue(site.outcome() == CircusPavilionStateRules.Outcome.RUIN && site.activeOwner() == null,
				"The kill ruins the site: " + site.outcome());
		helper.assertTrue(CircusPlayerProgress.route(player) == CircusRouteRules.Route.LIBERATION_COMPLETE,
				"The route is Liberation's");
		helper.assertTrue(HemoCapabilityAccess.getKnownManipulations(player).getKnownManips()
				.containsKey(ManipulationInit.thread_ripper.get()) || player.getInventory().contains(new net.minecraft.world.item.ItemStack(
						com.vincenthuto.hemomancy.common.init.ItemInit.memory_thread_ripper.get())),
				"Thread Ripper is learnt, or owed when it cannot be yet");
		helper.assertTrue(!fixture.ringmaster.isAlive(), "The real Ringmaster dies with the kill");
		cleanUp(helper, origin, player, fixture);
		helper.succeed();
	}

	/**
	 * Absorbing him at his kneel, laid on the real site: the pavilion stands as the player's tenancy, the route is
	 * Succession's, the Ringmaster Pattern is known, and the real Ringmaster lives on, retired, offering no finale.
	 */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void absorbingAtTheKneelRetiresTheRealRingmaster(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer player = connectedPlayer(helper, "circus-absorb-ending", origin);
		Fixture fixture = settlementFixture(helper, origin, player);
		CircusFinaleSettlement.apply(helper.getLevel(), fixture.carousel.encounterOrigin(), player, FinaleRules.Ending.ABSORBED);
		CircusPavilionSavedData.Site site = CircusPavilionSavedData.get(helper.getLevel())
				.site(helper.getLevel(), fixture.carousel.encounterOrigin());
		helper.assertTrue(site.outcome() == CircusPavilionStateRules.Outcome.SUCCESSION && site.activeOwner() == null,
				"Absorbing him keeps the site as a tenancy: " + site.outcome());
		helper.assertTrue(CircusPlayerProgress.route(player) == CircusRouteRules.Route.SUCCESSION_COMPLETE,
				"The route is Succession's");
		helper.assertTrue(HemoCapabilityAccess.getKnownSummons(player).getKnownSummonNames()
				.contains(PuppeteerSummonDefinitions.RINGMASTER_PATTERN), "The Ringmaster Pattern is known");
		helper.assertTrue(fixture.ringmaster.isAlive() && fixture.ringmaster.retired(), "The real Ringmaster lives on, retired");
		CompoundTag saved = new CompoundTag();
		fixture.ringmaster.addAdditionalSaveData(saved);
		helper.assertTrue(saved.getBoolean("CircusRetired"), "His retirement is saved");
		cleanUp(helper, origin, player, fixture);
		helper.succeed();
	}

	private record Fixture(CircusCarouselEntity carousel, CircusRingmasterEntity ringmaster) {
	}

	/** A carousel and the real Ringmaster over it, the site held by {@code player} as a begun finale holds it. */
	private static Fixture settlementFixture(GameTestHelper helper, BlockPos origin, ServerPlayer player) {
		CircusCarouselEntity carousel = EntityInit.impaled_procession.get().create(helper.getLevel());
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		helper.assertTrue(carousel != null && ringmaster != null, "Circus fixtures must create");
		carousel.setPos(origin.getCenter());
		ringmaster.setPos(origin.getCenter().add(0.0D, 3.0D, 2.0D));
		helper.getLevel().addFreshEntity(carousel);
		helper.getLevel().addFreshEntity(ringmaster);
		helper.assertTrue(CircusPavilionSavedData.get(helper.getLevel()).begin(helper.getLevel(), carousel.encounterOrigin(),
				player.getUUID(), CircusRouteRules.Route.NEUTRAL), "The finale holds the site for its owner");
		return new Fixture(carousel, ringmaster);
	}

	private static void cleanUp(GameTestHelper helper, BlockPos origin, ServerPlayer player, Fixture fixture) {
		player.discard();
		fixture.carousel.getPassengers().forEach(net.minecraft.world.entity.Entity::discard);
		fixture.carousel.discard();
		fixture.ringmaster.discard();
		CircusPavilionSavedData.get(helper.getLevel()).removeSite(helper.getLevel(), fixture.carousel.encounterOrigin());
	}

	/**
	 * F1: the real site is usually unloaded when the finale settles. A kill laid while he is not in the level still ends
	 * him, and frees the troupe, the first time he ticks; the ending line comes from the settlement itself.
	 */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void killSettledWhileHeIsAwayEndsHimWhenHeLoads(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		List<Component> heard = new java.util.ArrayList<>();
		ServerPlayer player = listeningPlayer(helper, "circus-away-kill", origin, heard);
		CircusCarouselEntity carousel = EntityInit.impaled_procession.get().create(helper.getLevel());
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		var performer = EntityInit.circus_fire_eater.get().create(helper.getLevel());
		helper.assertTrue(carousel != null && ringmaster != null && performer != null, "Circus fixtures must create");
		carousel.setPos(origin.getCenter());
		helper.getLevel().addFreshEntity(carousel);
		performer.setPos(origin.getCenter().add(4.0D, 0.0D, 0.0D));
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());
		BlockPos site = carousel.encounterOrigin();
		helper.assertTrue(data.begin(helper.getLevel(), site, player.getUUID(), CircusRouteRules.Route.NEUTRAL), "held");
		// Neither he nor his troupe is in the level as the ending is laid.
		CircusFinaleSettlement.apply(helper.getLevel(), site, player, FinaleRules.Ending.KILLED);
		helper.assertTrue(heard.stream().anyMatch(line -> line.getContents() instanceof TranslatableContents contents
				&& contents.getKey().equals("hemomancy.circus.finale.complete.liberation")),
				"The settlement tells the player the ending: " + heard);
		ringmaster.setPos(origin.getCenter().add(0.0D, 3.0D, 2.0D));
		helper.getLevel().addFreshEntity(ringmaster);
		helper.getLevel().addFreshEntity(performer);
		performer.tick();
		ringmaster.tick();
		ringmaster.tick();
		helper.assertTrue(!ringmaster.isAlive(), "A ruined site's Ringmaster dies when he next ticks");
		helper.assertTrue(performer.isRemoved(), "A ruined site's finale troupe leaves when it next ticks");
		player.discard();
		carousel.getPassengers().forEach(net.minecraft.world.entity.Entity::discard);
		carousel.discard();
		ringmaster.discard();
		performer.discard();
		data.removeSite(helper.getLevel(), site);
		helper.succeed();
	}

	/** F1: absorbing him while he is away retires him when he next ticks, and he offers nobody the finale again. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void absorbSettledWhileHeIsAwayRetiresHimWhenHeLoads(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer player = connectedPlayer(helper, "circus-away-absorb", origin);
		List<Component> heard = new java.util.ArrayList<>();
		ServerPlayer visitor = listeningPlayer(helper, "circus-away-visit", origin.offset(2, 0, 0), heard);
		CircusPlayerProgress.addAcclimation(visitor, 500);
		CircusPlayerProgress.completeChallenge(visitor, 0, 0);
		HemoCapabilityAccess.getInitiatoryDegree(visitor).setDegreeNumber(4);
		com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress.state(visitor)
				.putBoolean("quest.empty_ring", true);
		CircusCarouselEntity carousel = EntityInit.impaled_procession.get().create(helper.getLevel());
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		helper.assertTrue(carousel != null && ringmaster != null, "Circus fixtures must create");
		carousel.setPos(origin.getCenter());
		helper.getLevel().addFreshEntity(carousel);
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());
		BlockPos site = carousel.encounterOrigin();
		helper.assertTrue(data.begin(helper.getLevel(), site, player.getUUID(), CircusRouteRules.Route.NEUTRAL), "held");
		CircusFinaleSettlement.apply(helper.getLevel(), site, player, FinaleRules.Ending.ABSORBED);
		ringmaster.setPos(origin.getCenter().add(0.0D, 3.0D, 2.0D));
		helper.getLevel().addFreshEntity(ringmaster);
		ringmaster.tick();
		helper.assertTrue(ringmaster.isAlive() && ringmaster.retired(), "A succeeded site's Ringmaster is retired when he next ticks");
		ringmaster.handleChoice(visitor, CircusRingmasterEntity.EVENT_FINALE);
		helper.assertTrue(heard.stream().anyMatch(line -> line.getContents() instanceof TranslatableContents contents
				&& contents.getKey().equals("hemomancy.circus.finale.settled")), "A settled site's finale is refused aloud: " + heard);
		player.discard();
		visitor.discard();
		carousel.getPassengers().forEach(net.minecraft.world.entity.Entity::discard);
		carousel.discard();
		ringmaster.discard();
		data.removeSite(helper.getLevel(), site);
		helper.succeed();
	}

	/** F2: a login after a crash mid-finale releases the site it held, so the finale can be begun again. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void loginAfterACrashReleasesTheSite(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer player = connectedPlayer(helper, "circus-crash-login", origin);
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());
		helper.assertTrue(data.begin(helper.getLevel(), origin, player.getUUID(), CircusRouteRules.Route.NEUTRAL), "held");
		RingmasterFinaleDirector.markActive(player, helper.getLevel().dimension(), origin,
				net.minecraft.world.level.block.Rotation.NONE, origin.offset(0, 0, 20));
		RingmasterFinaleDirector.onLogin(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(player));
		helper.assertTrue(data.site(helper.getLevel(), origin).activeOwner() == null, "The crashed finale's site is released");
		helper.assertTrue(data.begin(helper.getLevel(), origin, player.getUUID(), CircusRouteRules.Route.NEUTRAL),
				"The finale can be begun again");
		player.discard();
		data.removeSite(helper.getLevel(), origin);
		helper.succeed();
	}

	/** F3: a finale asked of a busy or a settled site says so, before anything else is tried. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void beginNamesABusyOrSettledSite(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		List<Component> heard = new java.util.ArrayList<>();
		ServerPlayer player = listeningPlayer(helper, "circus-begin-refused", origin, heard);
		CircusCarouselEntity carousel = EntityInit.impaled_procession.get().create(helper.getLevel());
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		helper.assertTrue(carousel != null && ringmaster != null, "Circus fixtures must create");
		carousel.setPos(origin.getCenter());
		ringmaster.setPos(origin.getCenter().add(0.0D, 3.0D, 2.0D));
		helper.getLevel().addFreshEntity(carousel);
		helper.getLevel().addFreshEntity(ringmaster);
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());
		BlockPos site = carousel.encounterOrigin();
		UUID other = UUID.randomUUID();
		data.begin(helper.getLevel(), site, other, CircusRouteRules.Route.NEUTRAL);
		helper.assertTrue(!RingmasterFinaleDirector.begin(player, ringmaster, carousel), "A busy site refuses");
		helper.assertTrue(said(heard, "hemomancy.circus.finale.busy"), "The busy site is named: " + heard);
		data.complete(helper.getLevel(), site, other, CircusPavilionStateRules.Outcome.RUIN);
		heard.clear();
		helper.assertTrue(!RingmasterFinaleDirector.begin(player, ringmaster, carousel), "A settled site refuses");
		helper.assertTrue(said(heard, "hemomancy.circus.finale.settled"), "The settled site is named: " + heard);
		player.discard();
		carousel.getPassengers().forEach(net.minecraft.world.entity.Entity::discard);
		carousel.discard();
		ringmaster.discard();
		data.removeSite(helper.getLevel(), site);
		helper.succeed();
	}

	private static boolean said(List<Component> heard, String key) {
		return heard.stream().anyMatch(line -> line.getContents() instanceof TranslatableContents contents
				&& contents.getKey().equals(key));
	}

	/**
	 * F5: the crossing raises a frozen stand-in for each finale troupe member at the real site, at its mapped place on
	 * the stage (the overworld stands in for the Chamber: the raise takes its stage level as an argument).
	 */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void standInsRaiseOnePerTroupeMember(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		var fire = EntityInit.circus_fire_eater.get().create(helper.getLevel());
		var acrobat = EntityInit.circus_acrobat.get().create(helper.getLevel());
		var strongman = EntityInit.circus_strongman.get().create(helper.getLevel());
		helper.assertTrue(fire != null && acrobat != null && strongman != null, "Circus fixtures must create");
		fire.setPos(origin.getCenter().add(4.0D, 0.0D, 0.0D));
		acrobat.setPos(origin.getCenter().add(-4.0D, 0.0D, 0.0D));
		strongman.setPos(origin.getCenter().add(0.0D, 0.0D, 4.0D));
		helper.getLevel().addFreshEntity(fire);
		helper.getLevel().addFreshEntity(acrobat);
		helper.getLevel().addFreshEntity(strongman);
		BlockPos stage = origin.offset(0, 0, 30);
		List<com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity> raised =
				com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleStandIns.raise(helper.getLevel(), helper.getLevel(),
						origin, stage, net.minecraft.world.level.block.Rotation.NONE);
		try {
			helper.assertTrue(raised.size() == 2, "One stand-in per finale troupe member (the faculty Strongman stays): " + raised.size());
			for (var standIn : raised) {
				var real = standIn.getType() == fire.getType() ? fire : acrobat;
				helper.assertTrue(standIn.getType() == real.getType(), "A stand-in is its member's kind");
				helper.assertTrue(standIn.isNoAi() && standIn.getTags().contains(RingmasterFinaleDirector.COPY_TAG),
						"A stand-in is frozen and swept with the stage's copies");
				var at = com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleStage.toCopy(real.position(), origin, stage,
						net.minecraft.world.level.block.Rotation.NONE);
				helper.assertTrue(standIn.position().distanceTo(at) < 1.0E-3D, "A stand-in stands at its member's mapped place");
			}
		} finally {
			raised.forEach(net.minecraft.world.entity.Entity::discard);
			fire.discard();
			acrobat.discard();
			strongman.discard();
		}
		helper.succeed();
	}

	/** F5: while the finale holds the site, its real finale troupe stands still as the Ringmaster and carousel do. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void heldSiteFreezesTheRealTroupe(GameTestHelper helper) throws ReflectiveOperationException {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer owner = connectedPlayer(helper, "circus-held-troupe", origin.offset(2, 0, 0));
		var fire = EntityInit.circus_fire_eater.get().create(helper.getLevel());
		helper.assertTrue(fire != null, "Circus fixtures must create");
		// A strip of floor to walk on, the performer standing on it.
		for (int x = 2; x <= 10; x++) helper.setBlock(new BlockPos(x, 1, 4), net.minecraft.world.level.block.Blocks.STONE);
		BlockPos stand = helper.absolutePos(new BlockPos(8, 2, 4));
		fire.setPos(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D);
		helper.getLevel().addFreshEntity(fire);
		fire.setOnGround(true);
		fire.tick();
		fire.setOnGround(true);
		hold(helper, owner.getUUID(), origin);
		try {
			// A walk under way (its own, or a chase) is stopped on the held member's next tick; an unheld one keeps walking.
			BlockPos to = fire.blockPosition().offset(-3, 0, 0);
			fire.getNavigation().moveTo(to.getX() + 0.5D, to.getY(), to.getZ() + 0.5D, 1.0D);
			helper.assertTrue(fire.getNavigation().isInProgress(), "Fixture: the walk must be under way before the tick");
			fire.tick();
			helper.assertTrue(fire.getNavigation().isDone(), "A held troupe member does not walk: it stands where it is");
		} finally {
			release(owner.getUUID());
			fire.discard();
			owner.discard();
		}
		helper.succeed();
	}

	/** Holds the site at {@code origin} as a begun finale holds it, without a Chamber: one entry in the director's table. */
	@SuppressWarnings("unchecked")
	private static void hold(GameTestHelper helper, UUID owner, BlockPos origin) throws ReflectiveOperationException {
		var constructor = RingmasterFinaleDirector.Finale.class.getDeclaredConstructor(net.minecraft.resources.ResourceKey.class,
				BlockPos.class, net.minecraft.world.level.block.Rotation.class, BlockPos.class);
		constructor.setAccessible(true);
		var finale = constructor.newInstance(helper.getLevel().dimension(), origin, net.minecraft.world.level.block.Rotation.NONE, origin);
		var field = RingmasterFinaleDirector.class.getDeclaredField("FINALES");
		field.setAccessible(true);
		((java.util.Map<UUID, RingmasterFinaleDirector.Finale>) field.get(null)).put(owner, finale);
	}

	@SuppressWarnings("unchecked")
	private static void release(UUID owner) throws ReflectiveOperationException {
		var field = RingmasterFinaleDirector.class.getDeclaredField("FINALES");
		field.setAccessible(true);
		((java.util.Map<UUID, RingmasterFinaleDirector.Finale>) field.get(null)).remove(owner);
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

	/** A connected player whose system chat lines land in {@code heard}. */
	private static ServerPlayer listeningPlayer(GameTestHelper helper, String name, BlockPos position, List<Component> heard) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), name), false);
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				cookie.gameProfile(), ClientInformation.createDefault());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override public void send(Packet<?> packet) {
				if (packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat) heard.add(chat.content());
			}

			// A system message is sent with a listener (ServerPlayer.sendSystemMessage), through this overload.
			@Override public void send(Packet<?> packet, @javax.annotation.Nullable net.minecraft.network.PacketSendListener listener) {
				send(packet);
			}
		};
		player.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
		helper.getLevel().addNewPlayer(player);
		return player;
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

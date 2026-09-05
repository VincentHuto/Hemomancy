package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.circus.CircusPerformanceController;
import com.vincenthuto.hemomancy.common.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.circus.CircusPavilionStateRules;
import com.vincenthuto.hemomancy.common.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusAcrobatEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
@SuppressWarnings("removal")
public final class CircusRingmasterGameTests {
	private CircusRingmasterGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 40)
	public static void carouselSpawnsThreeBoundPuppeteerCaptives(GameTestHelper helper) {
		CircusCarouselEntity carousel = EntityInit.circus_carousel.get().create(helper.getLevel());
		helper.assertTrue(carousel != null, "Carousel fixture must create");
		carousel.setPos(helper.absolutePos(new BlockPos(4, 2, 4)).getCenter());
		helper.getLevel().addFreshEntity(carousel);
		carousel.tick();
		int initialPassengers = carousel.getPassengers().size();

		helper.runAfterDelay(3, () -> {
			String passengers = carousel.getPassengers().stream()
					.map(entity -> entity.getType().toString() + ":" + entity.isRemoved())
					.collect(java.util.stream.Collectors.joining(", "));
			helper.assertTrue(carousel.getPassengers().size() == 3
					&& carousel.getPassengers().stream().allMatch(BloodDrunkPuppeteerEntity.class::isInstance),
					"Carousel must carry three Blood Drunk Puppeteer captives; found "
							+ carousel.getPassengers().size() + " [" + passengers + "] after initially spawning "
							+ initialPassengers + " at "
							+ helper.getLevel().getDifficulty());
			helper.assertTrue(carousel.getPassengers().stream()
					.allMatch(captive -> captive.getY() > carousel.getY() + 2.0D),
					"Carousel captives must sit above the horse bodies instead of standing on the platform");
			helper.assertTrue(carousel.getPassengers().stream().allMatch(captive -> Math.abs(Math.hypot(
					captive.getX() - carousel.getX(), captive.getZ() - carousel.getZ())
					- CircusCarouselRules.HORSE_RADIUS - 0.55D) < 0.01D),
					"Carousel captives must sit behind the poles so their hands meet them");
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(EnthralledDollEntity.class,
					new AABB(carousel.blockPosition()).inflate(8.0D)).isEmpty(),
					"Carousel captives must not summon combat dolls");
			carousel.getPassengers().forEach(net.minecraft.world.entity.Entity::discard);
			carousel.discard();
			helper.succeed();
		});
	}

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 40)
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

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 40)
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

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 40)
	public static void inactiveRingmasterPositionDoesNotDependOnAudienceAcclimation(GameTestHelper helper) {
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		CircusCarouselEntity carousel = EntityInit.circus_carousel.get().create(helper.getLevel());
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

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 40)
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

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 40)
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

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 40)
	public static void routeChoiceRequiresCompletedAttentionAct(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer player = connectedPlayer(helper, "circus-pact-order", origin);
		CircusPlayerProgress.addAcclimation(player, 500);
		CircusCarouselEntity carousel = EntityInit.circus_carousel.get().create(helper.getLevel());
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

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 40)
	public static void expiredControlledActCannotAwardProgress(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		ServerPlayer staleOwner = connectedPlayer(helper, "circus-stale-owner", origin);
		ServerPlayer successor = connectedPlayer(helper, "circus-successor", origin);
		CircusPlayerProgress.chooseRoute(staleOwner, CircusRouteRules.Route.SUCCESSION);
		CircusPlayerProgress.completeChallenge(staleOwner, 0, 0);
		CircusPlayerProgress.completeChallenge(staleOwner, 1, 0);
		helper.assertTrue(CircusPerformanceController.beginNext(staleOwner, origin),
				"The controlled act fixture must begin");
		staleOwner.getPersistentData().putInt("hemomancy.circus_challenge_ticks", 199);
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());
		data.reset(helper.getLevel(), origin, staleOwner.getUUID());
		helper.assertTrue(data.beginPerformance(helper.getLevel(), origin, successor.getUUID(),
				CircusRouteRules.Route.SUCCESSION), "A successor must be able to claim the released pavilion");

		CircusPerformanceController.onPlayerTick(new PlayerTickEvent.Post(staleOwner));
		helper.assertTrue(CircusPlayerProgress.challenges(staleOwner) == 0b00011,
				"An expired act must not award challenge credit from stale player data");
		helper.assertTrue(staleOwner.getPersistentData().getInt("hemomancy.circus_active_challenge") == 0,
				"An expired act must clear the stale player timer");
		helper.assertTrue(successor.getUUID().equals(data.site(helper.getLevel(), origin).activeOwner()),
				"Cleaning stale player data must not release the successor's attempt");

		staleOwner.discard();
		successor.discard();
		data.removeSite(helper.getLevel(), origin);
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 40)
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

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 40)
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

	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 40)
	public static void missingPavilionOwnerExpiresAfterGracePeriod(GameTestHelper helper) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		UUID disconnectedOwner = UUID.randomUUID();
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());
		helper.assertTrue(data.begin(helper.getLevel(), origin, disconnectedOwner,
				CircusRouteRules.Route.SUCCESSION), "Pavilion attempt must begin");
		CircusCarouselEntity carousel = EntityInit.circus_carousel.get().create(helper.getLevel());
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

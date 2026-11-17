package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VampireBatEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusAcrobatEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFireEaterEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterMoves.LentAct;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterMoves.Move;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingLock;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Chunk 15 T13 to T15: the Ringmaster's finale on the boss kit (the intro lock, the bar's CAROUSEL objective, the one
 * phase writer, reloads, the leash), his five mental moves and his knives and staff (each lands on its commit tick and
 * spares a bystander), and the closing beat (the Pattern after the yield, a relog during it, Liberation's collapse).
 * The Ringmaster is ticked by hand, outside the level's own tick, so each count is exact.
 */
@GameTestHolder("crimson_troupe_validation")
@PrefixGameTestTemplate(false)
@SuppressWarnings("removal")
public final class CircusRingmasterFinaleGameTests {
	private CircusRingmasterFinaleGameTests() {
	}

	/** A finale fixture: the carousel at the origin, the site begun in {@code phase}, the owner and the Ringmaster. */
	private record Finale(GameTestHelper helper, BlockPos origin, CircusCarouselEntity carousel,
			CircusRingmasterEntity ringmaster, ServerPlayer owner, CircusPavilionSavedData data) {
		CircusPavilionSavedData.Site site() {
			return data.site(helper.getLevel(), origin);
		}

		void tick(int times) {
			for (int i = 0; i < times; i++) ringmaster.tick();
		}

		void cleanup(Entity... others) {
			for (Entity other : others) other.discard();
			helper.getLevel().getEntitiesOfClass(Entity.class, new AABB(origin).inflate(32.0D),
					entity -> entity instanceof VampireBatEntity || entity instanceof net.minecraft.world.entity.projectile.Projectile
							|| entity instanceof com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity)
					.forEach(Entity::discard);
			carousel.getPassengers().forEach(Entity::discard);
			carousel.discard();
			owner.discard();
			data.removeSite(helper.getLevel(), origin);
		}
	}

	private static Finale finale(GameTestHelper helper, CircusRouteRules.Route route,
			CircusPavilionStateRules.Phase phase, BlockPos ringmasterAt, BlockPos ownerAt) {
		BlockPos origin = helper.absolutePos(new BlockPos(4, 2, 4));
		CircusCarouselEntity carousel = EntityInit.impaled_procession.get().create(helper.getLevel());
		CircusRingmasterEntity ringmaster = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		helper.assertTrue(carousel != null && ringmaster != null, "Circus fixtures must create");
		carousel.setPos(origin.getCenter());
		helper.getLevel().addFreshEntity(carousel);
		BlockPos at = helper.absolutePos(ringmasterAt);
		ringmaster.setPos(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D);
		ServerPlayer owner = player(helper, UUID.randomUUID(), "circus-finale-owner", helper.absolutePos(ownerAt));
		CircusPavilionSavedData data = CircusPavilionSavedData.get(helper.getLevel());
		helper.assertTrue(data.begin(helper.getLevel(), origin, owner.getUUID(), route), "the finale must begin");
		data.setPhase(helper.getLevel(), origin, phase);
		return new Finale(helper, origin, carousel, ringmaster, owner, data);
	}

	/** DESCENT, ticked once so he knows his site; he waits a second before he picks a move of his own. */
	private static Finale descent(GameTestHelper helper, CircusRouteRules.Route route) {
		Finale finale = finale(helper, route, CircusPavilionStateRules.Phase.DESCENT, new BlockPos(4, 2, 7), new BlockPos(4, 2, 9));
		finale.tick(1);
		return finale;
	}

	// --- T13: the kit plumbing -----------------------------------------------------------------------------------------

	/** Audit B-6: the 60-tick opening holds the knives, the swarm, the troupe and the bar until it ends. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void introHoldsTheFinaleUntilItEnds(GameTestHelper helper) {
		Finale f = finale(helper, CircusRouteRules.Route.SUCCESSION, CircusPavilionStateRules.Phase.RAFTERS,
				new BlockPos(4, 8, 4), new BlockPos(4, 2, 9));
		CircusAcrobatEntity acrobat = EntityInit.circus_acrobat.get().create(helper.getLevel());
		helper.assertTrue(acrobat != null, "Acrobat fixture must create");
		acrobat.setPos(f.origin().offset(3, 0, 0).getCenter());
		helper.getLevel().addFreshEntity(acrobat);
		int ticks = 0;
		do {
			f.tick(1);
			ticks++;
			if (!f.ringmaster().inIntro()) break;
			helper.assertTrue(f.ringmaster().bar().viewers().isEmpty(), "no bar during the opening (tick " + ticks + ")");
			helper.assertTrue(acrobat.getActState() != CircusPerformerEntity.ActState.ALERT, "the troupe waits for the opening");
			helper.assertTrue(f.ringmaster().move() == Move.NONE, "no knife during the opening");
		} while (ticks < 100);
		helper.assertTrue(ticks == CircusRingmasterEntity.INTRO_TICKS, "the opening lasts 60 ticks: " + ticks);
		helper.assertTrue(f.ringmaster().bar().viewers().contains(f.owner()), "the bar shows when the opening ends");
		helper.assertTrue(!helper.getLevel().getEntitiesOfClass(VampireBatEntity.class, new AABB(f.origin()).inflate(16.0D)).isEmpty(),
				"the swarm comes when the opening ends");
		f.tick(1);
		helper.assertTrue(acrobat.getActState() == CircusPerformerEntity.ActState.ALERT, "the troupe joins after the opening");
		f.cleanup(acrobat);
		helper.succeed();
	}

	/** CAROUSEL's objective is on the bar: the troupe still standing, or the anchors left. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void carouselBarShowsTheObjective(GameTestHelper helper) {
		Finale f = finale(helper, CircusRouteRules.Route.SUCCESSION, CircusPavilionStateRules.Phase.CAROUSEL,
				new BlockPos(4, 8, 4), new BlockPos(4, 2, 9));
		CircusAcrobatEntity acrobat = EntityInit.circus_acrobat.get().create(helper.getLevel());
		CircusFireEaterEntity breather = EntityInit.circus_fire_eater.get().create(helper.getLevel());
		helper.assertTrue(acrobat != null && breather != null, "performer fixtures must create");
		acrobat.setPos(f.origin().offset(3, 0, 0).getCenter());
		breather.setPos(f.origin().offset(-3, 0, 0).getCenter());
		helper.getLevel().addFreshEntity(acrobat);
		helper.getLevel().addFreshEntity(breather);
		f.tick(1);
		f.owner().invulnerableTime = 0;
		acrobat.hurt(acrobat.damageSources().playerAttack(f.owner()), 1000.0F);
		f.tick(1);
		helper.assertTrue(acrobat.isDowned(), "the struck performer is down");
		assertBar(helper, f.ringmaster(), "hemomancy.circus.finale.bar.troupe", 1, 2, 0.5F);
		f.cleanup(acrobat, breather);

		Finale l = finale(helper, CircusRouteRules.Route.LIBERATION, CircusPavilionStateRules.Phase.CAROUSEL,
				new BlockPos(4, 8, 4), new BlockPos(4, 2, 9));
		l.data().setCarouselProgress(helper.getLevel(), l.origin(), 0b111, 0b001);
		l.tick(1);
		assertBar(helper, l.ringmaster(), "hemomancy.circus.finale.bar.anchors", 2, 3, 2.0F / 3.0F);
		l.cleanup();
		helper.succeed();
	}

	/** B-7: the carousel only reports itself destroyed; the Ringmaster writes DESCENT, so Liberation's card plays. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void theRingmasterIsTheOneWriterOfThePhase(GameTestHelper helper) {
		Finale f = finale(helper, CircusRouteRules.Route.LIBERATION, CircusPavilionStateRules.Phase.RAFTERS,
				new BlockPos(4, 8, 4), new BlockPos(4, 2, 9));
		helper.assertTrue(!f.carousel().severCaptive(f.owner(), 0), "no thread is cut in the rafters");
		f.data().setPhase(helper.getLevel(), f.origin(), CircusPavilionStateRules.Phase.CAROUSEL);
		f.data().setCarouselProgress(helper.getLevel(), f.origin(), 0b111, 0b111);
		f.carousel().tick();
		helper.assertTrue(f.carousel().isDestroyed(), "the carousel reads its broken anchors");
		helper.assertTrue(f.site().phase() == CircusPavilionStateRules.Phase.CAROUSEL, "the carousel does not write DESCENT");
		f.tick(1);
		helper.assertTrue(f.site().phase() == CircusPavilionStateRules.Phase.DESCENT, "the Ringmaster moves the finale to DESCENT");
		f.cleanup();
		helper.succeed();
	}

	/** A reload mid-RAFTERS keeps the opening where it was; one mid-DESCENT keeps the acts the troupe lent him. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void reloadMidRaftersAndMidDescent(GameTestHelper helper) {
		Finale f = finale(helper, CircusRouteRules.Route.SUCCESSION, CircusPavilionStateRules.Phase.RAFTERS,
				new BlockPos(4, 8, 4), new BlockPos(4, 2, 9));
		f.tick(30);
		CircusRingmasterEntity loaded = reload(helper, f.ringmaster());
		helper.assertTrue(loaded.inIntro(), "the opening survives a reload");
		f.data().setPhase(helper.getLevel(), f.origin(), CircusPavilionStateRules.Phase.DESCENT);
		loaded.lend(LentAct.FLAME);
		loaded.lend(LentAct.VAULT);
		CircusRingmasterEntity again = reload(helper, loaded);
		helper.assertTrue(again.lentActs().equals(List.of(LentAct.VAULT, LentAct.FLAME)), "the lent acts survive: " + again.lentActs());
		f.cleanup();
		helper.succeed();
	}

	/** The arena's leash: a holder out of the ring keeps the site through the grace, then loses it. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void aStrayingOwnerLosesTheSiteAfterTheGrace(GameTestHelper helper) {
		Finale f = finale(helper, CircusRouteRules.Route.SUCCESSION, CircusPavilionStateRules.Phase.DESCENT,
				new BlockPos(4, 2, 7), new BlockPos(4, 2, 9));
		for (int i = 0; i < 300; i++) f.carousel().tick();
		helper.assertTrue(f.owner().getUUID().equals(f.site().activeOwner()), "an owner in the ring keeps it");
		f.owner().setPos(f.owner().getX() + 30.0D, f.owner().getY(), f.owner().getZ());
		for (int i = 0; i < 199; i++) f.carousel().tick();
		helper.assertTrue(f.owner().getUUID().equals(f.site().activeOwner()), "the grace keeps the site for 199 ticks");
		f.carousel().tick();
		helper.assertTrue(f.site().activeOwner() == null, "past the grace the site is released");
		f.cleanup();
		helper.succeed();
	}

	// --- T15: the moves --------------------------------------------------------------------------------------------

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void staffStrikeLandsOnItsCommitTickAndSparesABystander(GameTestHelper helper) {
		Finale f = descent(helper, CircusRouteRules.Route.SUCCESSION);
		ServerPlayer stranger = player(helper, UUID.randomUUID(), "circus-staff-stranger", helper.absolutePos(new BlockPos(4, 2, 9)));
		stranger.setPos(f.owner().getX() + 0.4D, f.owner().getY(), f.owner().getZ() - 0.6D);
		clearInvulnerability(stranger);
		landsOnCommit(helper, f, Move.STAFF_STRIKE, ignored -> { });
		helper.assertTrue(stranger.getHealth() == stranger.getMaxHealth(), "the staff spares a bystander in its wedge");
		stranger.discard();
		f.cleanup();
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void knifeBoardLandsInsideAndADashBreaksIt(GameTestHelper helper) {
		Finale f = descent(helper, CircusRouteRules.Route.SUCCESSION);
		landsOnCommit(helper, f, Move.KNIFE_BOARD, ignored -> { });
		f.cleanup();
		Finale g = descent(helper, CircusRouteRules.Route.SUCCESSION);
		clearInvulnerability(g.owner());
		float health = g.owner().getHealth();
		g.ringmaster().forceMove(Move.KNIFE_BOARD, g.owner());
		g.tick(10);
		g.owner().setPos(g.owner().getX() + 3.0D, g.owner().getY(), g.owner().getZ());
		g.tick(15);
		helper.assertTrue(g.owner().getHealth() == health, "a dash out of the outline breaks the Board");
		g.cleanup();
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void spotlightRootsOnlyAPlayerWhoLooksAway(GameTestHelper helper) {
		Finale f = descent(helper, CircusRouteRules.Route.SUCCESSION);
		// The Ringmaster stands north of the owner; yaw 180 looks north at him, yaw 0 looks away.
		f.owner().setYRot(180.0F);
		f.owner().setXRot(0.0F);
		f.ringmaster().forceMove(Move.SPOTLIGHT, f.owner());
		f.tick(Move.SPOTLIGHT.timeline().windup() + 10);
		helper.assertTrue(!MorphlingLock.isPinned(f.owner()), "a player who holds his eye is not rooted");
		f.cleanup();
		Finale g = descent(helper, CircusRouteRules.Route.SUCCESSION);
		g.owner().setYRot(0.0F);
		g.owner().setXRot(0.0F);
		g.ringmaster().forceMove(Move.SPOTLIGHT, g.owner());
		g.tick(Move.SPOTLIGHT.timeline().windup() - 1);
		helper.assertTrue(!MorphlingLock.isPinned(g.owner()), "nothing roots through the wind-up");
		g.tick(2);
		helper.assertTrue(MorphlingLock.isPinned(g.owner()), "looking away in the light roots the player");
		g.cleanup();
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void sleightSetsTwoDoublesAndOnlyHeStrikes(GameTestHelper helper) {
		Finale f = descent(helper, CircusRouteRules.Route.SUCCESSION);
		landsOnCommit(helper, f, Move.SLEIGHT, tick -> {
			if (tick == 1) helper.assertTrue(f.ringmaster().doubles().size() == 2, "two doubles stand with him");
		});
		f.cleanup();
		Finale g = descent(helper, CircusRouteRules.Route.SUCCESSION);
		g.ringmaster().forceMove(Move.SLEIGHT, g.owner());
		g.tick(5);
		clearInvulnerability(g.owner());
		helper.assertTrue(g.ringmaster().hurt(g.ringmaster().damageSources().playerAttack(g.owner()), 2.0F),
				"the real one bleeds");
		helper.assertTrue(g.ringmaster().doubles().isEmpty() && g.ringmaster().move() == Move.NONE,
				"found among his doubles, the trick breaks");
		g.cleanup();
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 60)
	public static void cueBringsEveryStandingPerformerDownOnOneBeat(GameTestHelper helper) {
		Finale f = descent(helper, CircusRouteRules.Route.SUCCESSION);
		CircusAcrobatEntity acrobat = EntityInit.circus_acrobat.get().create(helper.getLevel());
		CircusFireEaterEntity breather = EntityInit.circus_fire_eater.get().create(helper.getLevel());
		helper.assertTrue(acrobat != null && breather != null, "performer fixtures must create");
		acrobat.setPos(f.origin().offset(2, 0, 0).getCenter());
		breather.setPos(f.origin().offset(-2, 0, 0).getCenter());
		helper.getLevel().addFreshEntity(acrobat);
		helper.getLevel().addFreshEntity(breather);
		acrobat.beginFinale(f.owner(), false);
		breather.beginFinale(f.owner(), false);
		long called = helper.getLevel().getGameTime();
		f.ringmaster().forceMove(Move.CUE, f.owner());
		helper.assertTrue(acrobat.awaitingCue() && breather.awaitingCue(), "both performers answer the Cue");
		long beat = called + Move.CUE.timeline().windup();
		helper.runAfterDelay(Move.CUE.timeline().windup() + 6, () -> {
			helper.assertTrue(acrobat.lastStrikeTime() == beat && breather.lastStrikeTime() == beat,
					"both strike on the called beat " + beat + ": " + acrobat.lastStrikeTime() + ", " + breather.lastStrikeTime());
			f.cleanup(acrobat, breather);
			helper.succeed();
		});
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void stringsCutPerformsALentActOnce(GameTestHelper helper) {
		Finale f = descent(helper, CircusRouteRules.Route.SUCCESSION);
		f.ringmaster().lend(LentAct.STILTS);
		landsOnCommit(helper, f, Move.STRINGS_CUT, ignored -> { });
		helper.assertTrue(f.ringmaster().lentActs().isEmpty(), "a lent act is used once");
		f.cleanup();
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void knifeLeavesOnItsCommitTick(GameTestHelper helper) {
		Finale f = descent(helper, CircusRouteRules.Route.SUCCESSION);
		f.ringmaster().forceMove(Move.KNIFE_THROW, f.owner());
		f.tick(Move.KNIFE_THROW.timeline().windup());
		helper.assertTrue(helper.getLevel().getEntitiesOfClass(
				com.vincenthuto.hemomancy.common.entity.projectile.CircusKnifeProjectileEntity.class,
				new AABB(f.origin()).inflate(16.0D)).isEmpty(), "no knife leaves before the commit tick");
		f.tick(1);
		helper.assertTrue(!helper.getLevel().getEntitiesOfClass(
				com.vincenthuto.hemomancy.common.entity.projectile.CircusKnifeProjectileEntity.class,
				new AABB(f.origin()).inflate(16.0D)).isEmpty(), "the knife leaves on the commit tick");
		f.cleanup();
		helper.succeed();
	}

	// --- T14: the closing beat -------------------------------------------------------------------------------------

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void successionYieldGrantsThePatternWhenTheBeatEnds(GameTestHelper helper) {
		Finale f = descent(helper, CircusRouteRules.Route.SUCCESSION);
		f.tick(1);
		helper.assertTrue(f.ringmaster().hurt(f.ringmaster().damageSources().playerAttack(f.owner()), 1000.0F), "the last blow lands");
		helper.assertTrue(f.site().phase() == CircusPavilionStateRules.Phase.CURTAIN && f.ringmaster().isAlive(),
				"the last blow opens the yield and he lives");
		helper.assertTrue(!f.ringmaster().hasTopper(), "the topper comes off");
		helper.assertTrue(!knowsPattern(f.owner()), "nothing is granted on the hit tick");
		f.tick(CircusRingmasterEntity.YIELD_TICKS - 1);
		helper.assertTrue(!knowsPattern(f.owner()) && f.site().outcome() == CircusPavilionStateRules.Outcome.NEUTRAL,
				"nothing is granted before the beat ends");
		f.tick(1);
		helper.assertTrue(knowsPattern(f.owner()) && f.site().outcome() == CircusPavilionStateRules.Outcome.SUCCESSION,
				"the Pattern is granted when the beat ends");
		f.cleanup();
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void aRelogDuringTheYieldStillCompletes(GameTestHelper helper) {
		Finale f = descent(helper, CircusRouteRules.Route.SUCCESSION);
		f.tick(1);
		f.ringmaster().hurt(f.ringmaster().damageSources().playerAttack(f.owner()), 1000.0F);
		f.tick(20);
		UUID id = f.owner().getUUID();
		helper.getLevel().removePlayerImmediately(f.owner(), Entity.RemovalReason.UNLOADED_WITH_PLAYER);
		f.tick(CircusRingmasterEntity.YIELD_TICKS);
		for (int i = 0; i < 300; i++) f.carousel().tick();
		helper.assertTrue(id.equals(f.site().activeOwner()) && f.site().phase() == CircusPavilionStateRules.Phase.CURTAIN,
				"the beat waits for its owner and nothing releases the site");
		ServerPlayer back = player(helper, id, "circus-finale-owner", f.origin().offset(0, 0, 5));
		f.tick(1);
		helper.assertTrue(knowsPattern(back) && f.site().outcome() == CircusPavilionStateRules.Outcome.SUCCESSION,
				"the returning owner gets the Pattern");
		back.discard();
		f.cleanup();
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 40)
	public static void liberationCollapsesBeforeTheRuin(GameTestHelper helper) {
		Finale f = descent(helper, CircusRouteRules.Route.LIBERATION);
		f.tick(1);
		helper.assertTrue(f.ringmaster().hurt(f.ringmaster().damageSources().playerAttack(f.owner()), 1000.0F), "the last blow lands");
		helper.assertTrue(f.site().phase() == CircusPavilionStateRules.Phase.CURTAIN && f.ringmaster().isAlive(),
				"the last blow opens the collapse");
		f.tick(CircusRingmasterEntity.COLLAPSE_TICKS - 1);
		helper.assertTrue(f.site().outcome() == CircusPavilionStateRules.Outcome.NEUTRAL, "no ruin before the collapse ends");
		f.tick(1);
		helper.assertTrue(f.site().outcome() == CircusPavilionStateRules.Outcome.RUIN && !f.ringmaster().isAlive(),
				"the collapse ends in the ruin");
		f.cleanup();
		helper.succeed();
	}

	// --- helpers ---------------------------------------------------------------------------------------------------

	/** Runs {@code move} on the owner and asserts its hit lands on the commit tick, not before. */
	private static void landsOnCommit(GameTestHelper helper, Finale f, Move move, Consumer<Integer> each) {
		clearInvulnerability(f.owner());
		float health = f.owner().getHealth();
		f.ringmaster().forceMove(move, f.owner());
		int commit = move.timeline().commitTick();
		helper.assertTrue(commit >= move.floor(), move + " winds up at least " + move.floor());
		for (int tick = 0; tick < commit; tick++) {
			f.tick(1);
			each.accept(tick + 1);
			helper.assertTrue(f.owner().getHealth() == health, move + " lands early, on tick " + tick);
		}
		f.tick(1);
		helper.assertTrue(f.owner().getHealth() < health, move + " lands on its commit tick (" + commit + ")");
	}

	private static void assertBar(GameTestHelper helper, CircusRingmasterEntity ringmaster, String key, int a, int b,
			float progress) {
		helper.assertTrue(ringmaster.bar().name().getContents() instanceof TranslatableContents contents
				&& contents.getKey().equals(key) && contents.getArgs().length == 2
				&& Integer.valueOf(a).equals(contents.getArgs()[0]) && Integer.valueOf(b).equals(contents.getArgs()[1]),
				"the bar reads " + key + " " + a + "/" + b + ": " + ringmaster.bar().name());
		helper.assertTrue(Math.abs(ringmaster.bar().progress() - progress) < 1.0E-4F,
				"the bar's progress is " + progress + ": " + ringmaster.bar().progress());
	}

	private static boolean knowsPattern(ServerPlayer player) {
		return HemoCapabilityAccess.getKnownSummons(player).getKnownSummonNames()
				.contains(PuppeteerSummonDefinitions.RINGMASTER_PATTERN);
	}

	private static CircusRingmasterEntity reload(GameTestHelper helper, CircusRingmasterEntity ringmaster) {
		CompoundTag saved = ringmaster.saveWithoutId(new CompoundTag());
		CircusRingmasterEntity loaded = EntityInit.circus_ringmaster.get().create(helper.getLevel());
		helper.assertTrue(loaded != null, "Ringmaster must create");
		loaded.load(saved);
		return loaded;
	}

	/** A fresh player ignores all damage for its first 60 ticks, and these players are never ticked. */
	private static void clearInvulnerability(ServerPlayer player) {
		player.invulnerableTime = 0;
		try {
			var field = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
			field.setAccessible(true);
			field.setInt(player, 0);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static ServerPlayer player(GameTestHelper helper, UUID id, String name, BlockPos position) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(id, name), false);
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

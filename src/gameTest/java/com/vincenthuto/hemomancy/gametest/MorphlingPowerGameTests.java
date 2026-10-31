package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve;
import com.vincenthuto.hemomancy.common.entity.utility.MorphlingFieldEntity;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.*;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import com.vincenthuto.hemomancy.mixin.core.ServerPlayerSpawnGraceAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.function.Consumer;

/**
 * Brined Court Track 2, loaded: each strain's ladder through its real hooks on a server player, with the shared pieces
 * (home, stillness, Stung, the lock, damage share, strands, regrowth, the Second Head, field patches, the synced
 * active state). A test player is never ticked by a connection, so {@link #drive} runs the equipped tick the way
 * {@code EquippedMorphlingEvents} does.
 */
@GameTestHolder("morphling_strain_validation")
@PrefixGameTestTemplate(false)
public final class MorphlingPowerGameTests {
	static final String NAMESPACE = "morphling_strain_validation";
	private static final String EMPTY = "empty";
	private static final float[] POWER = { 0.0F, 10.0F, 30.0F, 60.0F, 100.0F };

	private MorphlingPowerGameTests() {
	}

	// --- Hushbell -------------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void hushbellSlowsHungerAndFoldsShutWhenLow(GameTestHelper h) {
		ServerPlayer player = host(h);
		ItemStack bell = equip(player, MorphlingStrains.HUSHBELL, 4);
		IMorphling morphling = (IMorphling) bell.getItem();
		h.assertTrue(Math.abs(morphling.exhaustionMultiplier(player, bell) - 0.5F) < 1.0E-4F, "Hush at Apex halves hunger");
		h.assertTrue(morphling.resistsEffect(player, bell, MobEffects.POISON), "Hush keeps poison off");
		player.setHealth(7.0F);
		morphling.onEquippedHurt(player, bell, player.damageSources().generic(), 1.0F, true);
		h.assertTrue(MorphlingLock.isPinned(player) && Paralysis.blocksActions(player), "Fold Shut roots and seals the host");
		h.assertTrue(player.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() == 3, "Fold Shut is Resistance IV");
		boolean home = MorphlingHomeConditions.isHome(player, MorphlingStrains.HUSHBELL);
		int expected = home ? HushbellRules.FOLD_HOME_TICKS : HushbellRules.FOLD_TICKS;
		h.assertTrue(player.getEffect(EffectInit.pinned).getDuration() == expected, "Fold Shut lasts " + expected + " here");
		player.setHealth(6.0F);
		morphling.onEquippedHurt(player, bell, player.damageSources().generic(), 1.0F, true);
		h.assertTrue(MorphlingActiveState.has(morphling.activeFlags(player, bell), MorphlingActiveState.FOLD_SHUT),
				"Fold Shut shows its active bit");
		done(h, player);
	}

	@GameTest(template = EMPTY, timeoutTicks = 140)
	public static void hushbellCryptobiosisSealsThenUnfolds(GameTestHelper h) {
		ServerPlayer player = host(h);
		ItemStack bell = equip(player, MorphlingStrains.HUSHBELL, 5);
		LastRiteHelper.armForMorphling(player, bell);
		double before = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
		player.setHealth(0.0F);
		((IMorphling) bell.getItem()).onEquippedHurt(player, bell, player.damageSources().generic(), 30.0F, true);
		h.assertTrue(player.getHealth() >= HushbellRules.CRYPTOBIOSIS_HEALTH, "Cryptobiosis restores 10 health");
		h.assertTrue(before - HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() >= HushbellRules.CRYPTOBIOSIS_COST - 1.0D,
				"Cryptobiosis pays 650 mL");
		h.assertTrue(MorphlingLock.isPinned(player) && MorphlingLock.blocksActions(player), "the sealed bell cannot move or act");
		drive(h, player, bell, HushbellRules.CRYPTOBIOSIS_SEALED_TICKS + 5, () -> {
			h.assertTrue(!MorphlingLock.blocksActions(player), "the bell unfolds");
			h.assertTrue(player.hasEffect(MobEffects.ABSORPTION) && player.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() == 2,
					"unfolding grants Resistance III and Absorption III");
		});
	}

	// --- Marrowplume ----------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void marrowplumeFeedsAndFeedsMoreNearBone(GameTestHelper h) {
		ServerPlayer away = host(h);
		ServerPlayer home = host(h);
		ItemStack plumeAway = equip(away, MorphlingStrains.MARROWPLUME, 0);
		ItemStack plumeHome = equip(home, MorphlingStrains.MARROWPLUME, 0);
		h.setBlock(new BlockPos(4, 2, 4), Blocks.BONE_BLOCK);
		away.moveTo(h.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)).add(40.0D, 0.0D, 0.0D));
		home.moveTo(h.absoluteVec(new Vec3(3.5D, 2.0D, 3.5D)));
		away.setHealth(10.0F);
		home.setHealth(10.0F);
		h.assertTrue(MorphlingHomeConditions.isHome(home, MorphlingStrains.MARROWPLUME), "bone blocks are Marrowplume's home");
		boolean awayHome = MorphlingHomeConditions.isHome(away, MorphlingStrains.MARROWPLUME);
		drive(h, List.of(away, home), List.of(plumeAway, plumeHome), MarrowplumeRules.PROC_TICKS + 1, () -> {
			float awayGain = away.getHealth() - 10.0F;
			float homeGain = home.getHealth() - 10.0F;
			h.assertTrue(Math.abs(awayGain - MarrowplumeRules.feeding(0, awayHome)) < 0.01F,
					"Marrow Feeding heals " + MarrowplumeRules.feeding(0, awayHome) + ": " + awayGain);
			h.assertTrue(Math.abs(homeGain - MarrowplumeRules.feeding(0, true)) < 0.01F, "home feeds half again: " + homeGain);
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void marrowplumeKillsLeaveBoneAndAWhaleFall(GameTestHelper h) {
		ServerPlayer player = host(h);
		ItemStack plume = equip(player, MorphlingStrains.MARROWPLUME, 5);
		Mob zombie = mob(h, EntityType.ZOMBIE, new Vec3(4.5D, 2.0D, 2.5D));
		((IMorphling) plume.getItem()).onEquippedKill(player, plume, zombie, true);
		boolean bone = !h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
				zombie.getBoundingBox().inflate(2.0D), item -> item.getItem().is(Items.BONE)).isEmpty();
		h.assertTrue(bone, "Fall's Inheritance always adds a bone");
		List<MorphlingFieldEntity> fields = h.getLevel().getEntitiesOfClass(MorphlingFieldEntity.class,
				zombie.getBoundingBox().inflate(2.0D));
		h.assertTrue(fields.size() == 1 && fields.getFirst().kind() == MorphlingFieldEntity.Kind.WHALE_FALL,
				"a 20-health kill leaves a Whale Fall at Primal");
		h.assertTrue(!fields.getFirst().shouldBeSaved(), "a field patch never saves");
		((IMorphling) plume.getItem()).onEquippedKill(player, plume, zombie, true);
		h.assertTrue(h.getLevel().getEntitiesOfClass(MorphlingFieldEntity.class, zombie.getBoundingBox().inflate(2.0D)).size() == 1,
				"Whale Fall waits out its 30-second cooldown");
		zombie.discard();
		done(h, player);
	}

	// --- Mermaid's Purse -----------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void mermaidsPurseBanksAndSharesWithTheSchool(GameTestHelper h) {
		ServerPlayer player = host(h);
		ItemStack purse = equip(player, MorphlingStrains.MERMAIDS_PURSE, 3);
		Mob zombie = mob(h, EntityType.ZOMBIE, new Vec3(4.5D, 2.0D, 2.5D));
		double banked = BorrowedBloodReserve.get(player);
		((IMorphling) purse.getItem()).onEquippedAttack(player, purse, zombie, 5.0F);
		h.assertTrue(BorrowedBloodReserve.get(player) > banked, "Feed Banking stores strike blood");
		player.setHealth(5.0F);
		drive(h, player, purse, 11, () -> {
			h.assertTrue(player.getHealth() >= 5.0F + MermaidsPurseRules.schoolHeal(3) - 0.01F,
					"School's Share heals a failing host alone: " + player.getHealth());
			zombie.discard();
		});
	}

	// --- Ink Cap --------------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void inkCapVeilsAndDissolves(GameTestHelper h) {
		ServerPlayer player = host(h);
		ItemStack ink = equip(player, MorphlingStrains.INK_CAP, 5);
		IMorphling morphling = (IMorphling) ink.getItem();
		boolean home = MorphlingHomeConditions.isHome(player, MorphlingStrains.INK_CAP);
		double veil = morphling.visibilityMultiplier(player, ink);
		h.assertTrue(Math.abs(veil - (home ? 0.5D : 0.65D)) < 1.0E-6D, "Veil at Primal, home=" + home + ": " + veil);
		h.assertTrue(morphling.tryUse(player, InteractionHand.MAIN_HAND, ink, h.getLevel()), "Deliquesce starts");
		h.assertTrue(player.hasEffect(MobEffects.INVISIBILITY) && Paralysis.blocksActions(player)
				&& InkCapMorphlingItem.isDissolved(player), "the dissolved host is unseen and cannot act");
		h.assertTrue(!MorphlingLock.isPinned(player), "Deliquesce never roots");
		MorphlingLock.onBlockedAttack(player);
		h.assertTrue(!InkCapMorphlingItem.isDissolved(player) && !Paralysis.blocksActions(player), "attacking ends it early");
		h.assertTrue(!morphling.tryUse(player, InteractionHand.MAIN_HAND, ink, h.getLevel()), "40-second cooldown");
		done(h, player);
	}

	// --- Holdfast -------------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 140)
	public static void holdfastGripsStoneAndTheHoldingLetsGo(GameTestHelper h) {
		ServerPlayer player = host(h);
		ItemStack hold = equip(player, MorphlingStrains.HOLDFAST, 5);
		IMorphling morphling = (IMorphling) hold.getItem();
		morphling.onEquippedServerTick(player, hold);
		h.assertTrue(Math.abs(player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) - 0.6D) < 1.0E-6D, "Grip at Primal");
		h.assertTrue(Math.abs(morphling.breakSpeedMultiplier(player, hold, Blocks.STONE.defaultBlockState()) - 1.6F) < 1.0E-4F,
				"stone breaks faster");
		h.assertTrue(morphling.breakSpeedMultiplier(player, hold, Blocks.DIRT.defaultBlockState()) == 1.0F, "dirt does not");
		Mob zombie = mob(h, EntityType.ZOMBIE, new Vec3(5.5D, 2.0D, 2.5D));
		h.assertTrue(morphling.tryUse(player, InteractionHand.MAIN_HAND, hold, h.getLevel()), "The Holding starts");
		h.assertTrue(MorphlingLock.isPinned(zombie) && MorphlingLock.isPinned(player), "The Holding pins host and hostile");
		drive(h, player, hold, HoldfastRules.HOLDING_PIN_TICKS + 2, () -> {
			h.assertTrue(zombie.getHealth() < zombie.getMaxHealth(), "the release shockwave hurts the pinned hostile");
			MorphlingPowers.clearHost(player);
			h.assertTrue(player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) == 0.0D, "taking it off drops Grip");
			zombie.discard();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void holdfastBreakwaterTakesAQuarterForBlood(GameTestHelper h) {
		ServerPlayer host = host(h);
		ServerPlayer ally = host(h);
		team(h, host, ally);
		ItemStack hold = equip(host, MorphlingStrains.HOLDFAST, 4);
		host.setShiftKeyDown(true);
		MorphlingAnchorState.settle(host, true, HoldfastRules.BREAKWATER_ANCHOR_TICKS);
		h.assertTrue(HoldfastMorphlingItem.isBreakwater(host, hold), "two anchored seconds raise the Breakwater");
		double blood = HemoCapabilityAccess.getBloodVolume(host).getBloodVolume();
		ally.hurt(h.getLevel().damageSources().generic(), 8.0F);
		h.assertTrue(Math.abs(host.getHealth() - 18.0F) < 0.01F, "the host takes a quarter: " + host.getHealth());
		h.assertTrue(Math.abs(blood - HemoCapabilityAccess.getBloodVolume(host).getBloodVolume() - 4.0D) < 0.01D,
				"at 2 mL per point");
		h.assertTrue(Math.abs(ally.getHealth() - 14.0F) < 0.01F, "the ally keeps the rest: " + ally.getHealth());
		host.setHealth(6.0F);
		ally.setHealth(20.0F);
		ally.invulnerableTime = 0;
		ally.hurt(h.getLevel().damageSources().generic(), 8.0F);
		h.assertTrue(host.getHealth() == 6.0F, "never below 30% of the host");
		done(h, host, ally);
	}

	// --- Dead Man's Rope ------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void deadMansRopeCatchesFallsAndSharesAcrossTheColony(GameTestHelper h) {
		ServerPlayer host = host(h);
		ServerPlayer first = host(h);
		ServerPlayer second = host(h);
		team(h, host, first, second);
		IMorphling rope = (IMorphling) MorphlingStrains.DEAD_MANS_ROPE.item().get();
		ItemStack developing = stack(MorphlingStrains.DEAD_MANS_ROPE, 2);
		h.assertTrue(rope.onEquippedFall(host, developing, 12.0F) && !rope.onEquippedFall(host, developing, 13.0F),
				"Lifeline holds 12 blocks at Developing");
		ItemStack mature = equip(host, MorphlingStrains.DEAD_MANS_ROPE, 3);
		h.assertTrue(rope.onEquippedSneakUse(host, mature, first, null) && MorphlingLinks.count(host) == 1, "Strand Link ties");
		rope.onEquippedSneakUse(host, mature, second, null);
		h.assertTrue(MorphlingLinks.count(host) == 1, "one strand at Mature");
		MorphlingLinks.clear(host);
		ItemStack apex = equip(host, MorphlingStrains.DEAD_MANS_ROPE, 4);
		rope.onEquippedSneakUse(host, apex, first, null);
		rope.onEquippedSneakUse(host, apex, second, null);
		rope.onEquippedSneakUse(host, apex, null, first.blockPosition().below());
		h.assertTrue(MorphlingLinks.count(host) == 3, "three strands at Apex, a block among them");
		first.hurt(h.getLevel().damageSources().generic(), 10.0F);
		h.assertTrue(Math.abs(second.getHealth() - 19.0F) < 0.01F && Math.abs(host.getHealth() - 19.0F) < 0.01F,
				"Colony spreads a fifth over the other two: " + second.getHealth() + "/" + host.getHealth());
		h.assertTrue(Math.abs(first.getHealth() - 12.0F) < 0.01F, "the struck partner keeps the rest: " + first.getHealth());
		h.assertTrue(MorphlingLinks.releasePartner(host, first) && MorphlingLinks.count(host) == 2, "the partner can let go");
		done(h, host, first, second);
	}

	// --- Kinbud ---------------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 260)
	public static void kinbudStingsAndRegrowsAShareOfAWound(GameTestHelper h) {
		ServerPlayer player = host(h);
		ItemStack bud = equip(player, MorphlingStrains.KINBUD, 2);
		IMorphling morphling = (IMorphling) bud.getItem();
		Mob skeleton = mob(h, EntityType.SKELETON, new Vec3(4.5D, 2.0D, 2.5D));
		morphling.onEquippedAttack(player, bud, skeleton, 2.0F);
		h.assertTrue(skeleton.hasEffect(EffectInit.stung), "Stung lands on undead");
		boolean home = MorphlingHomeConditions.isHome(player, MorphlingStrains.KINBUD);
		player.setHealth(15.0F);
		morphling.onEquippedHurt(player, bud, player.damageSources().generic(), 5.0F, true);
		float queued = MorphlingRegrowthQueue.peek(player.getUUID()).pending();
		h.assertTrue(Math.abs(queued - 5.0F * (float) KinbudRules.mendingShare(2, home)) < 1.0E-4F, "a share is queued: " + queued);
		drive(h, player, bud, KinbudRules.REGROWTH_TICKS + 2, () -> {
			h.assertTrue(Math.abs(player.getHealth() - (15.0F + queued)) < 0.05F, "regrown over ten seconds: " + player.getHealth());
			skeleton.discard();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void kinbudBudsASecondHeadInAFight(GameTestHelper h) {
		ServerPlayer player = host(h);
		ItemStack bud = equip(player, MorphlingStrains.KINBUD, 4);
		IMorphling morphling = (IMorphling) bud.getItem();
		Mob zombie = mob(h, EntityType.ZOMBIE, new Vec3(4.5D, 2.0D, 2.5D));
		morphling.onEquippedAttack(player, bud, zombie, 1.0F);
		float before = zombie.getHealth();
		drive(h, player, bud, 3, () -> {
			h.assertTrue(KinbudSecondHead.heads(player) == 1, "a head buds after a hit");
			h.assertTrue(MorphlingActiveState.has(MorphlingPowers.activeFlags(player, morphling, bud),
					MorphlingActiveState.SECOND_HEAD), "the head rides the synced active state");
			h.assertTrue(zombie.getHealth() < before, "the head strikes the nearest hostile");
			ItemStack primal = equip(player, MorphlingStrains.KINBUD, 5);
			h.assertTrue(morphling.tryUse(player, InteractionHand.MAIN_HAND, primal, h.getLevel()), "Kindled opens");
			boolean home = MorphlingHomeConditions.isHome(player, MorphlingStrains.KINBUD);
			h.assertTrue(MorphlingRegrowthQueue.peek(player.getUUID()).kindledBudget() == KinbudRules.kindledCap(home),
					"Kindled caps regrowth at " + KinbudRules.kindledCap(home));
			zombie.discard();
		});
	}

	// --- Sunburst -------------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void sunburstSetsATableAloneAndAnchorsAnArbor(GameTestHelper h) {
		ServerPlayer player = host(h);
		ItemStack sun = equip(player, MorphlingStrains.SUNBURST, 5);
		IMorphling morphling = (IMorphling) sun.getItem();
		player.getFoodData().setFoodLevel(0);
		player.setHealth(5.0F);
		h.assertTrue(morphling.waivesUpkeep(player, sun, true) == false, "Puppet Interference restores upkeep");
		double blood = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
		h.assertTrue(morphling.tryUse(player, InteractionHand.MAIN_HAND, sun, h.getLevel()), "Arbor anchors");
		h.assertTrue(blood - HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() >= SunburstRules.ARBOR_COST - 1.0D,
				"Arbor pays 400 mL");
		drive(h, player, sun, 21, () -> {
			h.assertTrue(player.getFoodData().getFoodLevel() >= SunburstRules.SHARED_TABLE_ALONE_FOOD,
					"Shared Table feeds a failing host alone: " + player.getFoodData().getFoodLevel());
			h.assertTrue(player.hasEffect(MobEffects.REGENERATION), "the Arbor shelters its host");
			h.assertTrue(MorphlingActiveState.has(morphling.activeFlags(player, sun), MorphlingActiveState.ARBOR), "Arbor bit");
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void fieldPatchesDespawnOnTime(GameTestHelper h) {
		ServerPlayer player = host(h);
		Vec3 at = h.absoluteVec(new Vec3(4.5D, 2.0D, 4.5D));
		MorphlingFieldEntity bloom = MorphlingFieldEntity.spawn(h.getLevel(), player, MorphlingFieldEntity.Kind.BLOOM,
				at.x, at.y, at.z, 5.0F, 20);
		h.runAfterDelay(25, () -> {
			h.assertTrue(bloom.isRemoved(), "a Bloom ends with its lifetime");
			done(h, player);
		});
	}

	// --- Helpers --------------------------------------------------------------------------------------------------

	static ServerPlayer host(GameTestHelper h) {
		ServerPlayer player = DuctilisGameTests.player(h);
		((ServerPlayerSpawnGraceAccessor) player).hemomancy$setSpawnInvulnerableTime(0);
		var blood = HemoCapabilityAccess.getBloodVolume(player);
		blood.setMaxBloodVolume(5000.0D);
		blood.setBloodVolume(5000.0D);
		blood.setActive(true);
		player.setHealth(player.getMaxHealth());
		return player;
	}

	static ItemStack stack(MorphlingStrain strain, int stage) {
		ItemStack stack = new ItemStack(strain.item().get());
		CompoundTag tag = new CompoundTag();
		tag.putFloat("EnzymePower", POWER[Math.min(stage, 4)]);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		if (stage >= 5) MorphlingItem.setPrimalized(stack);
		return stack;
	}

	static ItemStack equip(ServerPlayer player, MorphlingStrain strain, int stage) {
		ItemStack stack = stack(strain, stage);
		HemoCapabilityAccess.getEquippedMorphling(player).setEquippedMorphling(stack);
		return stack;
	}

	static Mob mob(GameTestHelper h, EntityType<? extends Mob> type, Vec3 relative) {
		Mob mob = type.create(h.getLevel());
		mob.setNoAi(true);
		mob.moveTo(h.absoluteVec(relative));
		h.getLevel().addFreshEntity(mob);
		return mob;
	}

	static void team(GameTestHelper h, ServerPlayer... players) {
		var board = h.getLevel().getScoreboard();
		var team = board.addPlayerTeam("morphling_" + players[0].getId());
		for (ServerPlayer player : players) board.addPlayerToTeam(player.getScoreboardName(), team);
	}

	private static void drive(GameTestHelper h, ServerPlayer player, ItemStack stack, int ticks, Runnable then) {
		drive(h, List.of(player), List.of(stack), ticks, then);
	}

	/** Runs the equipped tick each game tick, as EquippedMorphlingEvents would for a connected player. */
	private static void drive(GameTestHelper h, List<ServerPlayer> players, List<ItemStack> stacks, int ticks, Runnable then) {
		for (int t = 1; t <= ticks; t++) {
			h.runAfterDelay(t, () -> {
				for (int i = 0; i < players.size(); i++) {
					ServerPlayer player = players.get(i);
					ItemStack stack = HemoCapabilityAccess.getEquippedMorphling(player).getEquippedMorphling();
					if (stack.isEmpty()) stack = stacks.get(i);
					MorphlingAnchorState.update(player);
					((IMorphling) stack.getItem()).onEquippedServerTick(player, stack);
				}
			});
		}
		h.runAfterDelay(ticks + 1, () -> {
			then.run();
			done(h, players.toArray(ServerPlayer[]::new));
		});
	}

	/** Tears the test's players down and removes only their own team, so parallel tests keep theirs. */
	static void done(GameTestHelper h, ServerPlayer... players) {
		var board = h.getLevel().getScoreboard();
		for (ServerPlayer player : players) {
			var team = board.getPlayersTeam(player.getScoreboardName());
			if (team != null && team.getName().startsWith("morphling_")) board.removePlayerTeam(team);
			MorphlingPowers.clearHost(player);
			MorphlingSession.clear(player.getUUID());
			MorphlingAnchorState.logout(player.getUUID());
			HemoCapabilityAccess.getEquippedMorphling(player).clearMorphling();
			player.discard();
		}
		h.succeed();
	}
}

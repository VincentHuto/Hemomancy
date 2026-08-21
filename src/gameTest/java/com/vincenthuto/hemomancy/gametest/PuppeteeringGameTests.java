package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.menu.PuppeteersSpindleMenu;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonFactory;
import com.vincenthuto.hemomancy.common.summon.PuppeteerCrossbarCommands;
import com.vincenthuto.hemomancy.common.tile.crafting.PuppeteersSpindleBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
@SuppressWarnings("removal")
public final class PuppeteeringGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private PuppeteeringGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void spindleConvertsWholeThreadItemsToEightCharge(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		helper.getLevel().setBlockAndUpdate(pos, BlockInit.puppeteers_spindle.get().defaultBlockState());
		PuppeteersSpindleBlockEntity spindle = (PuppeteersSpindleBlockEntity) helper.getLevel().getBlockEntity(pos);
		spindle.setItem(PuppeteersSpindleBlockEntity.SLOT_THREAD,
				new ItemStack(ItemInit.puppeteering_thread.get(), 1));

		PuppeteersSpindleBlockEntity.serverTick(helper.getLevel(), pos,
				helper.getLevel().getBlockState(pos), spindle);

		helper.assertTrue(spindle.getThreadBuffer() == 8, "One physical Thread must become eight charge");
		helper.assertTrue(spindle.getItem(PuppeteersSpindleBlockEntity.SLOT_THREAD).isEmpty(),
				"Accepted Thread item must be consumed");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void attunementOwnsCrossbarAndAppliesBoundCommandCapacity(GameTestHelper helper) {
		ServerPlayer owner = testPlayer(helper);
		ServerPlayer stranger = testPlayer(helper);
		try {
			learnVulture(owner);
			learnVulture(stranger);
			HemoCapabilityAccess.requireSkillProgress(owner).setSkill(
					SkillPointInit.skill_bound_command, EnumSkillStates.UNLOCKED, 3);
			ItemStack crossbar = new ItemStack(ItemInit.marionette_crossbar.get());

			helper.assertTrue(MarionetteCrossbarItem.bindCrossbar(crossbar, owner),
					"Owner must be able to attune a fresh Crossbar");
			helper.assertTrue(!MarionetteCrossbarItem.bindCrossbar(crossbar, stranger),
					"A second player must not overwrite Crossbar ownership");
			helper.assertTrue(MarionetteCrossbarItem.getThreadCapacity(crossbar) == 352,
					"Bound Command III must raise capacity to 352");
			MarionetteCrossbarItem.addThread(crossbar, 400);
			helper.assertTrue(MarionetteCrossbarItem.getThread(crossbar) == 352,
					"Thread charge must clamp to the owner-scaled capacity");
			helper.succeed();
		} finally {
			removePlayer(owner);
			removePlayer(stranger);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void spindleAutoAttunesAndRowSelectionPreparesShape(GameTestHelper helper) {
		ServerPlayer owner = testPlayer(helper);
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		helper.getLevel().setBlockAndUpdate(pos, BlockInit.puppeteers_spindle.get().defaultBlockState());
		PuppeteersSpindleBlockEntity spindle = (PuppeteersSpindleBlockEntity) helper.getLevel().getBlockEntity(pos);
		ItemStack crossbar = new ItemStack(ItemInit.marionette_crossbar.get());
		owner.setItemInHand(InteractionHand.MAIN_HAND, crossbar);
		crossbar.getItem().use(helper.getLevel(), owner, InteractionHand.MAIN_HAND);
		helper.assertTrue(MarionetteCrossbarItem.isBoundTo(crossbar, owner),
				"The first right-click with a fresh Crossbar must attune it even before a shape is learned");
		PuppeteerSummonDefinition vulture = learnVulture(owner);
		owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		spindle.setItem(PuppeteersSpindleBlockEntity.SLOT_CROSSBAR, crossbar);

		PuppeteersSpindleMenu menu = new PuppeteersSpindleMenu(1, owner.getInventory(), spindle);
		helper.assertTrue(menu.prepareSelection(owner, vulture.name()),
				"Clicking a learned shape must prepare it without a separate confirmation button");
		helper.assertTrue(vulture.name().equals(MarionetteCrossbarItem.getSelectedSummonName(crossbar)),
				"The clicked learned shape must become the Crossbar's prepared shape");
		removePlayer(owner);
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void silentClaimedWillDoesNotConsumeTheShapedBodySlot(GameTestHelper helper) {
		ServerPlayer owner = testPlayer(helper);
		Mob claimedWill = null;
		try {
			PuppeteerSummonDefinition definition = learnVulture(owner);
			owner.getPersistentData().putString(FungalGardenTravelHelper.ARCHON_CHOICE_KEY,
					FungalGardenTravelHelper.ARCHON_CHOICE_SILENCE);
			ItemStack crossbar = attunedCrossbar(owner, 100);
			owner.setItemInHand(InteractionHand.MAIN_HAND, crossbar);

			claimedWill = EntityInit.will.get().create(helper.getLevel());
			BoundPuppeteerSummon claimed = (BoundPuppeteerSummon) claimedWill;
			claimed.hemomancy$setOwnerUUID(owner.getUUID());
			claimed.hemomancy$setCrossbarUUID(MarionetteCrossbarItem.ensureCrossbarId(crossbar));
			claimed.hemomancy$setSummonName("claimed_will");
			BoundSummonBehavior.bindOwnerSession(claimedWill, owner);
			helper.getLevel().addFreshEntity(claimedWill);

			MarionetteCrossbarItem.callOrRecallSelectedSummon(crossbar, owner);
			java.util.List<Mob> active = MarionetteCrossbarItem.activeSummonsForOwner(owner);
			helper.assertTrue(active.size() == 2,
					"A claimed-Will bonus body must not consume the owner's unused shaped-body slot");
			helper.assertTrue(active.stream().anyMatch(body -> definition.name().equals(
					((BoundPuppeteerSummon) body).hemomancy$getSummonName())),
					"The normal shaped body must be callable after claiming the Will first");
			helper.succeed();
		} finally {
			for (Mob body : MarionetteCrossbarItem.activeSummonsForOwner(owner)) {
				body.discard();
			}
			if (claimedWill != null) claimedWill.discard();
			removePlayer(owner);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void unboundTrialFormsRemainHostileToPlayers(GameTestHelper helper) {
		ServerPlayer caster = testPlayer(helper);
		Mob trial = null;
		try {
			PuppeteerSummonDefinition definition = PuppeteerSummonDefinitions
					.byName(PuppeteerSummonDefinitions.VEINWING_VULTURE).orElseThrow();
			trial = PuppeteerSummonFactory.createTrial(definition, helper.getLevel(), caster,
					helper.absolutePos(new BlockPos(2, 2, 2))).orElseThrow();
			helper.assertTrue(trial.canAttack(caster),
					"Unbound trial forms must be able to attack players despite controlled-body safety rules");
			helper.succeed();
		} finally {
			if (trial != null) trial.discard();
			removePlayer(caster);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void autonomousRetaliationAllowsFollowModeToAcquireNearbyHostiles(GameTestHelper helper) {
		ServerPlayer owner = testPlayer(helper);
		Mob summon = null;
		Zombie target = null;
		try {
			PuppeteerSummonDefinition definition = learnVulture(owner);
			ItemStack crossbar = attunedCrossbar(owner, 100);
			owner.setItemInHand(InteractionHand.MAIN_HAND, crossbar);
			HemoCapabilityAccess.requireSkillProgress(owner).setSkill(
					SkillPointInit.skill_autonomous_retaliation, EnumSkillStates.UNLOCKED, 1);
			summon = PuppeteerSummonFactory.create(definition, helper.getLevel(), owner,
					MarionetteCrossbarItem.ensureCrossbarId(crossbar), 0).orElseThrow();
			helper.getLevel().addFreshEntity(summon);
			target = EntityType.ZOMBIE.create(helper.getLevel());
			target.setPos(owner.getX() + 3.0, owner.getY(), owner.getZ());
			helper.getLevel().addFreshEntity(target);
			owner.setLastHurtByMob(target);

			helper.assertTrue(BoundSummonBehavior.commonServerTick(summon,
					(BoundPuppeteerSummon) summon, owner), "Bound summon must remain active");
			helper.assertTrue(summon.getTarget() == target,
					"Default Follow mode must defend the owner by acquiring a nearby hostile");
			helper.succeed();
		} finally {
			if (summon != null) summon.discard();
			if (target != null) target.discard();
			removePlayer(owner);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodNeedlePersistsWithoutAnEmptyPickupStack(GameTestHelper helper) {
		BloodNeedleEntity needle = new BloodNeedleEntity(helper.getLevel(), 1.0, 2.0, 1.0);
		needle.addAdditionalSaveData(new CompoundTag());
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80)
	public static void veinwingVultureFlightControllerPursuesAssignedTarget(GameTestHelper helper) {
		ServerPlayer owner = testPlayer(helper);
		PuppeteerSummonDefinition definition = learnVulture(owner);
		Mob summon = PuppeteerSummonFactory.createTrial(definition, helper.getLevel(), owner,
				helper.absolutePos(new BlockPos(1, 2, 5))).orElseThrow();
		summon.setTarget(owner);
		helper.getLevel().addFreshEntity(summon);
		double startingDistance = summon.distanceTo(owner);

		helper.runAfterDelay(50, () -> {
			try {
				helper.assertTrue(summon.getTarget() == owner && summon.distanceTo(owner) < startingDistance - 2.0,
						"Veinwing Vulture flight controller must pursue its assigned target; target="
								+ (summon.getTarget() == owner) + ", startDistance=" + startingDistance
								+ ", finalDistance=" + summon.distanceTo(owner) + ", pos=" + summon.position());
				helper.succeed();
			} finally {
				summon.discard();
				removePlayer(owner);
			}
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80)
	public static void marrowSpitterFiresBloodShotsInsteadOfArrows(GameTestHelper helper) {
		ServerPlayer owner = testPlayer(helper);
		PuppeteerSummonDefinition definition = PuppeteerSummonDefinitions
				.byName(PuppeteerSummonDefinitions.MARROW_SPITTER).orElseThrow();
		Mob spitter = PuppeteerSummonFactory.createTrial(definition, helper.getLevel(), owner,
				helper.absolutePos(new BlockPos(1, 3, 6))).orElseThrow();
		Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
		helper.assertTrue(target != null, "Marrow Spitter test target must spawn");
		target.setPos(net.minecraft.world.phys.Vec3.atCenterOf(helper.absolutePos(new BlockPos(12, 2, 6))));
		target.setNoAi(true);
		helper.getLevel().addFreshEntity(target);
		spitter.setTarget(target);
		net.minecraft.world.phys.Vec3 startingPosition = spitter.position();
		helper.getLevel().addFreshEntity(spitter);
		((net.minecraft.world.entity.monster.RangedAttackMob) spitter).performRangedAttack(target, 1.0F);
		boolean firedBloodShot = !helper.getLevel().getEntitiesOfClass(BloodShotEntity.class,
				spitter.getBoundingBox().inflate(32.0),
				shot -> shot.getOwner() == spitter).isEmpty();

		helper.runAfterDelay(50, () -> {
			try {
				helper.assertTrue(firedBloodShot, "Marrow Spitter must fire BloodShot projectiles");
				helper.assertTrue(spitter.position().distanceTo(startingPosition) > 0.5,
						"Marrow Spitter must hover around its player anchor instead of remaining ground-locked");
				helper.assertTrue(!spitter.isOnFire(), "Marrow Spitter must not ignite in sunlight");
				helper.succeed();
			} finally {
				spitter.discard();
				target.discard();
				removePlayer(owner);
			}
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 100)
	public static void goreboundHulkSurvivesDaylightAndPursuesItsTarget(GameTestHelper helper) {
		assertGroundPuppetPursuesTarget(helper, PuppeteerSummonDefinitions.GOREBOUND_HULK,
				"Gorebound Hulk");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 100)
	public static void mnemonistSurvivesDaylightAndPursuesItsTarget(GameTestHelper helper) {
		assertGroundPuppetPursuesTarget(helper, PuppeteerSummonDefinitions.MNEMONIST_PUPPET,
				"Mnemonist Puppet");
	}

	private static void assertGroundPuppetPursuesTarget(GameTestHelper helper, String summonName, String displayName) {
		for (int x = 0; x <= 2; x++) for (int z = 0; z <= 6; z++) {
			helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 0, z)), Blocks.STONE.defaultBlockState());
		}
		ServerPlayer caster = testPlayer(helper);
		Player target = helper.makeMockPlayer(GameType.SURVIVAL);
		target.setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(1, 1, 1))));
		helper.assertTrue(helper.getLevel().addFreshEntity(target), displayName + " trial target must spawn");
		PuppeteerSummonDefinition definition = PuppeteerSummonDefinitions.byName(summonName).orElseThrow();
		Mob puppet = PuppeteerSummonFactory.createTrial(definition, helper.getLevel(), caster,
				helper.absolutePos(new BlockPos(1, 1, 5))).orElseThrow();
		puppet.setTarget(target);
		double startingDistance = puppet.distanceTo(target);
		helper.assertTrue(helper.getLevel().addFreshEntity(puppet), displayName + " fixture entity must spawn");
		try {
			for (int i = 0; i < 5; i++) helper.getLevel().tickNonPassenger(puppet);
			helper.assertTrue(puppet.getNavigation().moveTo(target, 1.0),
					displayName + " must have a walkable path to its trial target");
			for (int i = 5; i < 60; i++) helper.getLevel().tickNonPassenger(puppet);
			helper.assertTrue(puppet.isAlive(), displayName + " must remain alive during combat");
			helper.assertTrue(!puppet.isOnFire(), displayName + " must not ignite in sunlight");
			helper.assertTrue(puppet.getTarget() == target,
					displayName + " must retain its assigned hostile target");
			helper.assertTrue(puppet.distanceTo(target) < startingDistance - 0.75,
					displayName + " must navigate toward its target; startDistance=" + startingDistance
							+ ", finalDistance=" + puppet.distanceTo(target) + ", pos=" + puppet.position());
			helper.succeed();
		} finally {
			puppet.discard();
			target.discard();
			removePlayer(caster);
			for (int x = 0; x <= 2; x++) for (int z = 0; z <= 6; z++) {
				helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(x, 0, z)), Blocks.AIR.defaultBlockState());
			}
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void prepareDoesNotSpawnAndEntityOwnsUpkeepAndFocus(GameTestHelper helper) {
		ServerPlayer owner = testPlayer(helper);
		Mob first = null;
		Mob second = null;
		Zombie target = null;
		try {
			PuppeteerSummonDefinition definition = learnVulture(owner);
			ItemStack firstCrossbar = attunedCrossbar(owner, 100);
			int beforePrepare = MarionetteCrossbarItem.getThread(firstCrossbar);
			helper.assertTrue(MarionetteCrossbarItem.prepareSelectedSummon(firstCrossbar, owner,
					definition.name()), "Known shape must prepare successfully");
			helper.assertTrue(MarionetteCrossbarItem.activeSummonsForOwner(owner).isEmpty(),
					"Spindle preparation must not spawn a summon");
			helper.assertTrue(MarionetteCrossbarItem.getThread(firstCrossbar) == beforePrepare,
					"Preparation must not consume call charge");

			owner.setItemInHand(InteractionHand.MAIN_HAND, firstCrossbar);
			first = PuppeteerSummonFactory.create(definition, helper.getLevel(), owner,
					MarionetteCrossbarItem.ensureCrossbarId(firstCrossbar), 0).orElseThrow();
			helper.assertTrue(first.isPersistenceRequired(),
					"Player-bound bodies must bypass vanilla distance and random despawn");
			helper.getLevel().addFreshEntity(first);
			BoundPuppeteerSummon firstBound = (BoundPuppeteerSummon) first;
			first.getPersistentData().putLong("HemomancyNextUpkeepGameTime",
					Math.max(1L, helper.getLevel().getGameTime()));
			helper.assertTrue(BoundSummonBehavior.commonServerTick(first, firstBound, owner),
					"Fed summon must survive its upkeep tick");
			helper.assertTrue(MarionetteCrossbarItem.getThread(firstCrossbar) == 82,
					"Veinwing must deduct its own 18-charge upkeep");

			ItemStack secondCrossbar = attunedCrossbar(owner, 100);
			second = PuppeteerSummonFactory.create(definition, helper.getLevel(), owner,
					MarionetteCrossbarItem.ensureCrossbarId(secondCrossbar), 0).orElseThrow();
			helper.getLevel().addFreshEntity(second);
			target = EntityType.ZOMBIE.create(helper.getLevel());
			target.setPos(owner.getX() + 2.0, owner.getY(), owner.getZ());
			helper.getLevel().addFreshEntity(target);

			helper.assertTrue(MarionetteCrossbarItem.focusTarget(owner, firstCrossbar, target) == 1,
					"Focus command must affect only summons on the commanding Crossbar");
			helper.assertTrue(first.getTarget() == target, "Matching summon must focus the marked hostile");
			helper.assertTrue(second.getTarget() != target, "Other Crossbar summons must remain unaffected");
			helper.succeed();
		} finally {
			if (first != null) first.discard();
			if (second != null) second.discard();
			if (target != null) target.discard();
			removePlayer(owner);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void crossingDimensionsDeliberatelyUnravelsSummon(GameTestHelper helper) {
		ServerPlayer owner = testPlayer(helper);
		Mob summon = null;
		try {
			PuppeteerSummonDefinition definition = learnVulture(owner);
			ItemStack crossbar = attunedCrossbar(owner, 100);
			owner.setItemInHand(InteractionHand.MAIN_HAND, crossbar);
			summon = PuppeteerSummonFactory.create(definition, helper.getLevel(), owner,
					MarionetteCrossbarItem.ensureCrossbarId(crossbar), 0).orElseThrow();
			helper.getLevel().addFreshEntity(summon);
			ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
			owner.setServerLevel(nether);

			helper.assertTrue(!BoundSummonBehavior.commonServerTick(summon,
					(BoundPuppeteerSummon) summon, owner), "Cross-dimension summon must stop ticking");
			helper.assertTrue(summon.isRemoved(), "Cross-dimension summon must deliberately unravel");
			helper.succeed();
		} finally {
			if (summon != null) summon.discard();
			removePlayer(owner);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void rotatedOwnerSessionInvalidatesPreviouslyBoundBodies(GameTestHelper helper) {
		ServerPlayer owner = testPlayer(helper);
		Mob summon = null;
		try {
			PuppeteerSummonDefinition definition = learnVulture(owner);
			ItemStack crossbar = attunedCrossbar(owner, 100);
			owner.setItemInHand(InteractionHand.MAIN_HAND, crossbar);
			summon = PuppeteerSummonFactory.create(definition, helper.getLevel(), owner,
					MarionetteCrossbarItem.ensureCrossbarId(crossbar), 0).orElseThrow();
			helper.getLevel().addFreshEntity(summon);

			BoundSummonBehavior.rotateOwnerSession(owner);

			helper.assertTrue(!BoundSummonBehavior.commonServerTick(summon,
					(BoundPuppeteerSummon) summon, owner), "A body from an earlier owner session must stop ticking");
			helper.assertTrue(summon.isRemoved(), "Logout, respawn, or dimension session rotation must unravel it");
			helper.succeed();
		} finally {
			if (summon != null) summon.discard();
			removePlayer(owner);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void reloadedBodiesCannotExceedThePlayerWideCap(GameTestHelper helper) {
		ServerPlayer owner = testPlayer(helper);
		Mob oldest = null;
		Mob overflow = null;
		try {
			PuppeteerSummonDefinition definition = learnVulture(owner);
			ItemStack crossbar = attunedCrossbar(owner, 100);
			owner.setItemInHand(InteractionHand.MAIN_HAND, crossbar);
			oldest = PuppeteerSummonFactory.create(definition, helper.getLevel(), owner,
					MarionetteCrossbarItem.ensureCrossbarId(crossbar), 0).orElseThrow();
			overflow = PuppeteerSummonFactory.create(definition, helper.getLevel(), owner,
					MarionetteCrossbarItem.ensureCrossbarId(crossbar), 0).orElseThrow();
			oldest.getPersistentData().putLong("HemomancyBoundAtGameTime", 1L);
			overflow.getPersistentData().putLong("HemomancyBoundAtGameTime", 2L);
			oldest.tickCount = 0;
			overflow.tickCount = 20;
			helper.getLevel().addFreshEntity(oldest);
			helper.getLevel().addFreshEntity(overflow);

			helper.assertTrue(BoundSummonBehavior.commonServerTick(oldest,
					(BoundPuppeteerSummon) oldest, owner), "The returning older tether must retain its cap slot");
			helper.assertTrue(overflow.isRemoved(),
					"Reload reconciliation must actively unravel a newer incumbent that is already ticking");
			helper.assertTrue(oldest.isAlive(), "The oldest loaded tether must remain alive");
			helper.succeed();
		} finally {
			if (oldest != null) oldest.discard();
			if (overflow != null) overflow.discard();
			removePlayer(owner);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void hotSwapRecallsOnlyTheFormerPreparedCohort(GameTestHelper helper) {
		ServerPlayer owner = testPlayer(helper);
		Mob oldPrepared = null;
		Mob otherShape = null;
		try {
			PuppeteerSummonDefinition vulture = learnVulture(owner);
			PuppeteerSummonDefinition spitter = PuppeteerSummonDefinitions
					.byName(PuppeteerSummonDefinitions.MARROW_SPITTER).orElseThrow();
			HemoCapabilityAccess.requireKnownSummons(owner).learn(spitter);
			HemoCapabilityAccess.requireSkillProgress(owner).setSkill(
					SkillPointInit.skill_skein_transposition, EnumSkillStates.UNLOCKED, 1);
			HemoCapabilityAccess.requireSkillProgress(owner).setSkill(
					SkillPointInit.skill_puppet_skein, EnumSkillStates.UNLOCKED, 3);
			ItemStack crossbar = attunedCrossbar(owner, 100);
			owner.setItemInHand(InteractionHand.MAIN_HAND, crossbar);
			UUID id = MarionetteCrossbarItem.ensureCrossbarId(crossbar);
			oldPrepared = PuppeteerSummonFactory.create(vulture, helper.getLevel(), owner, id, 0).orElseThrow();
			otherShape = PuppeteerSummonFactory.create(spitter, helper.getLevel(), owner, id, 0).orElseThrow();
			helper.getLevel().addFreshEntity(oldPrepared);
			helper.getLevel().addFreshEntity(otherShape);
			int before = MarionetteCrossbarItem.getThread(crossbar);
			int cost = MarionetteCrossbarItem.summonThreadCost(owner, spitter);

			helper.assertTrue(PuppeteerCrossbarCommands.hotSwap(owner, crossbar, spitter.name()),
					"Skein Transposition must permit a learned affordable replacement");
			helper.assertTrue(oldPrepared.isRemoved(), "The former prepared cohort must be recalled");
			helper.assertTrue(otherShape.isAlive(), "Another shape on the same Crossbar must remain active");
			helper.assertTrue(MarionetteCrossbarItem.getThread(crossbar) == before - cost,
					"A successful swap must spend the normal adjusted call charge exactly once");
			helper.assertTrue(spitter.name().equals(MarionetteCrossbarItem.getSelectedSummonName(crossbar)),
					"A successful swap must commit the selected replacement shape");
			helper.succeed();
		} finally {
			for (Mob body : MarionetteCrossbarItem.activeSummonsForOwner(owner)) body.discard();
			if (oldPrepared != null) oldPrepared.discard();
			if (otherShape != null) otherShape.discard();
			removePlayer(owner);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void failedHotSwapPreservesOldBodyPreparationAndCharge(GameTestHelper helper) {
		ServerPlayer owner = testPlayer(helper);
		Mob oldPrepared = null;
		try {
			PuppeteerSummonDefinition vulture = learnVulture(owner);
			PuppeteerSummonDefinition spitter = PuppeteerSummonDefinitions
					.byName(PuppeteerSummonDefinitions.MARROW_SPITTER).orElseThrow();
			HemoCapabilityAccess.requireKnownSummons(owner).learn(spitter);
			HemoCapabilityAccess.requireSkillProgress(owner).setSkill(
					SkillPointInit.skill_skein_transposition, EnumSkillStates.UNLOCKED, 1);
			ItemStack crossbar = attunedCrossbar(owner, 0);
			owner.setItemInHand(InteractionHand.MAIN_HAND, crossbar);
			UUID id = MarionetteCrossbarItem.ensureCrossbarId(crossbar);
			oldPrepared = PuppeteerSummonFactory.create(vulture, helper.getLevel(), owner, id, 0).orElseThrow();
			helper.getLevel().addFreshEntity(oldPrepared);

			helper.assertTrue(!PuppeteerCrossbarCommands.hotSwap(owner, crossbar, spitter.name()),
					"Insufficient charge must reject the complete transaction");
			helper.assertTrue(oldPrepared.isAlive(), "A rejected swap must preserve the old body");
			helper.assertTrue(vulture.name().equals(MarionetteCrossbarItem.getSelectedSummonName(crossbar)),
					"A rejected swap must preserve preparation");
			helper.assertTrue(MarionetteCrossbarItem.getThread(crossbar) == 0,
					"A rejected swap must preserve charge");
			helper.succeed();
		} finally {
			if (oldPrepared != null) oldPrepared.discard();
			removePlayer(owner);
		}
	}

	private static PuppeteerSummonDefinition learnVulture(ServerPlayer player) {
		PuppeteerSummonDefinition definition = PuppeteerSummonDefinitions
				.byName(PuppeteerSummonDefinitions.VEINWING_VULTURE).orElseThrow();
		HemoCapabilityAccess.requireKnownSummons(player).learn(definition);
		return definition;
	}

	private static ItemStack attunedCrossbar(ServerPlayer owner, int charge) {
		ItemStack stack = new ItemStack(ItemInit.marionette_crossbar.get());
		if (!MarionetteCrossbarItem.bindCrossbar(stack, owner)) {
			throw new IllegalStateException("Test Crossbar failed to attune");
		}
		HemoCapabilityAccess.getKnownSummons(owner).map(known -> known.getKnownSummonNames()).orElse(java.util.List.of())
				.stream().findFirst().ifPresent(name ->
						MarionetteCrossbarItem.prepareSelectedSummon(stack, owner, name));
		MarionetteCrossbarItem.addThread(stack, charge);
		return stack;
	}

	private static ServerPlayer testPlayer(GameTestHelper helper) {
		ServerPlayer player = new ServerPlayer(
				helper.getLevel().getServer(),
				helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "puppeteer-test-player"),
				ClientInformation.createDefault()) {
			@Override
			protected ItemCooldowns createItemCooldowns() {
				// Avoid ServerItemCooldowns packets for this deliberately detached fixture.
				return new ItemCooldowns();
			}

			@Override
			public void displayClientMessage(Component message, boolean overlay) {
				// Detached GameTest players have no negotiated client connection.
			}
		};
		BlockPos position = helper.absolutePos(new BlockPos(1, 2, 1));
		player.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
		return player;
	}

	private static void removePlayer(ServerPlayer player) {
		if (player != null) {
			player.discard();
		}
	}
}

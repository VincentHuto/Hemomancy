package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillOrigin;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillPhase;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSelection;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ManipCooldownPacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserBlockEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Core-loop Track 1: a stub Sculpting run through the real verb chains with a real player. It claims each of the five
 * target kinds, pays through {@code BloodAccounting}, respects paralysis, the alignment gate and an inactive volume,
 * starts no cooldown, and loses to Cardinal Rites and Wills. With nothing selected, or a Sculpting that does not claim,
 * the plain verb runs as before. Each test borrows its own registered manipulation and puts its Sculpting back after.
 */
@GameTestHolder("sculpting_validation")
@PrefixGameTestTemplate(false)
public final class SculptingValidationGameTests {
	private static final String EMPTY = "bastion/mobs/empty";
	private static final double PRICE = 15.0D;
	/** Lending swaps a registered manipulation's Sculpting for the whole server, so those tests run in a batch of their own. */
	private static final String LENT_BATCH = "sculpting_lent";

	private SculptingValidationGameTests() {
	}

	/** A Sculpting that records what it was handed. */
	private static final class StubSculpting implements VerbSculpting {
		private final Set<VerbTargetKind> wants;
		private final Predicate<VerbTarget> claims;
		private final boolean roots;
		final List<VerbTarget> targets = new ArrayList<>();
		final List<SculptingPulse> pulses = new ArrayList<>();
		int begun;
		int ended;
		/** How many pulses had run when the first {@code begin} came; a Sculpting begins before it works. */
		int pulsesAtFirstBegin = -1;

		StubSculpting(Set<VerbTargetKind> wants, Predicate<VerbTarget> claims, boolean roots) {
			this.wants = wants;
			this.claims = claims;
			this.roots = roots;
		}

		static StubSculpting claimingAll(VerbTargetKind... kinds) {
			return new StubSculpting(Set.of(kinds), target -> true, false);
		}

		@Override
		public Set<VerbTargetKind> wants() {
			return wants;
		}

		@Override
		public boolean claims(Player player, VerbTarget target) {
			return claims.test(target);
		}

		@Override
		public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
			targets.add(target);
			pulses.add(pulse);
		}

		@Override
		public void begin(ServerPlayer player, VerbTarget target) {
			if (begun == 0) pulsesAtFirstBegin = pulses.size();
			begun++;
		}

		@Override
		public void end(ServerPlayer player, boolean released) {
			ended++;
		}

		@Override
		public double bloodPerPulse() {
			return PRICE;
		}

		@Override
		public boolean roots() {
			return roots;
		}
	}

	/** Lends {@code stub} to the registered manipulation {@code name} for {@code verb}; closing puts the old one back. */
	private record Lent(BloodManipulation manipulation, Verb verb, Optional<VerbSculpting> previous) implements AutoCloseable {
		static Lent lend(String name, Verb verb, VerbSculpting stub) {
			BloodManipulation manipulation = ManipulationInit.getByName(name);
			Lent lent = new Lent(manipulation, verb, manipulation.getSculpting(verb));
			if (verb == Verb.PROJECT) manipulation.setProjectionSculpting(() -> stub);
			else manipulation.setAbsorptionSculpting(() -> stub);
			return lent;
		}

		@Override
		public void close() {
			VerbSculpting old = previous.orElse(null);
			if (verb == Verb.PROJECT) manipulation.setProjectionSculpting(old == null ? null : () -> old);
			else manipulation.setAbsorptionSculpting(old == null ? null : () -> old);
		}
	}

	/** Active blood at {@code blood}, both verbs known, and {@code manipulation} known, equipped and selected. */
	private static void prepare(ServerPlayer player, String manipulation, double blood) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setBloodVolume(blood);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		if (manipulation != null) {
			BloodManipulation registered = ManipulationInit.getByName(manipulation);
			known.getKnownManips().put(registered, new ManipLevel(1, 0));
			known.setEquippedManipNames(new ArrayList<>(List.of(manipulation)));
			known.setSelectedManip(registered);
		} else {
			known.setSelectedManip(BloodManipulation.BLANK);
		}
	}

	/** Stands the player's feet at {@code feet} and turns their eyes on {@code at}. */
	private static void aim(ServerPlayer player, Vec3 feet, Vec3 at) {
		player.setPos(feet.x, feet.y, feet.z);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, at);
	}

	private static Vec3 feet(GameTestHelper helper, int x, int z) {
		return Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(x, 1, z)));
	}

	private static double project(ServerPlayer player) {
		return BloodProjectionItem.projectFromEntity(player.level(), player, 5.0D, 100.0D);
	}

	private static void absorbTick(ServerPlayer player) {
		ItemStack hand = new ItemStack(ItemInit.blood_absorption.get());
		hand.getItem().onUseTick(player.level(), player, hand, hand.getUseDuration(player));
	}

	private static void clear(GameTestHelper helper, int fromX, int toX, int z) {
		for (int x = fromX; x <= toX; x++) {
			for (int y = 1; y <= 3; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
			helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
		}
	}

	// ── The five target kinds ──

	@GameTest(template = EMPTY, batch = LENT_BATCH, timeoutTicks = 40)
	public static void aClaimedBlockIsSculptedAheadOfTheCondenser(GameTestHelper helper) {
		clear(helper, 1, 6, 2);
		BlockPos cauldron = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(cauldron, Blocks.CAULDRON.defaultBlockState(), 3);
		StubSculpting stub = StubSculpting.claimingAll(VerbTargetKind.BLOCK);
		try (CapturedPlayer captured = CapturedPlayer.create(helper);
				Lent lent = Lent.lend("hematic_flare", Verb.PROJECT, stub)) {
			ServerPlayer player = captured.player;
			prepare(player, "hematic_flare", 1000.0D);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(cauldron));
			double spent = project(player);
			helper.assertTrue(stub.pulses.size() == 1, "the stub did not pulse once: " + stub.pulses.size());
			helper.assertTrue(stub.targets.getFirst() instanceof VerbTarget.Block block && block.pos().equals(cauldron),
					"the stub was handed " + stub.targets.getFirst() + ", not the cauldron");
			helper.assertTrue(helper.getLevel().getBlockState(cauldron).is(Blocks.CAULDRON),
					"the Condenser took the cauldron ahead of the claiming Sculpting (CL-4)");
			double paid = stub.pulses.getFirst().paid();
			helper.assertTrue(paid > 0.0D && Math.abs(spent - paid) < 1.0E-6D
							&& Math.abs(HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() - (1000.0D - paid)) < 1.0E-6D,
					"the pulse paid " + paid + ", the chain reported " + spent);
			helper.assertTrue(stub.begun == 1, "a paid pulse did not open the session");
			helper.assertTrue(stub.pulsesAtFirstBegin == 0, "the Sculpting's begin ran after its first pulse");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = LENT_BATCH, timeoutTicks = 40)
	public static void aClaimedFluidIsSculpted(GameTestHelper helper) {
		clear(helper, 1, 6, 2);
		BlockPos water = helper.absolutePos(new BlockPos(4, 0, 2));
		helper.getLevel().setBlock(water, Blocks.WATER.defaultBlockState(), 3);
		StubSculpting stub = StubSculpting.claimingAll(VerbTargetKind.FLUID);
		try (CapturedPlayer captured = CapturedPlayer.create(helper);
				Lent lent = Lent.lend("sanguine_ignition", Verb.PROJECT, stub)) {
			ServerPlayer player = captured.player;
			prepare(player, "sanguine_ignition", 1000.0D);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(water).add(0.0D, 0.4D, 0.0D));
			project(player);
			helper.assertTrue(stub.targets.size() == 1 && stub.targets.getFirst() instanceof VerbTarget.Fluid fluid
					&& fluid.pos().equals(water), "the stub was not handed the water: " + stub.targets);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = LENT_BATCH, timeoutTicks = 40)
	public static void aClaimedCreatureIsSculptedInsteadOfDrained(GameTestHelper helper) {
		clear(helper, 1, 6, 2);
		Pig pig = helper.spawn(EntityType.PIG, new BlockPos(4, 1, 2));
		pig.setNoAi(true);
		StubSculpting stub = StubSculpting.claimingAll(VerbTargetKind.CREATURE);
		try (CapturedPlayer captured = CapturedPlayer.create(helper);
				Lent lent = Lent.lend("crimson_flame_conjuration", Verb.ABSORB, stub)) {
			ServerPlayer player = captured.player;
			prepare(player, "crimson_flame_conjuration", 500.0D);
			aim(player, feet(helper, 1, 2), pig.getBoundingBox().getCenter());
			float health = pig.getHealth();
			absorbTick(player);
			helper.assertTrue(stub.targets.size() == 1 && stub.targets.getFirst() instanceof VerbTarget.Creature creature
					&& creature.living() == pig, "the stub was not handed the pig: " + stub.targets);
			helper.assertTrue(pig.getHealth() == health, "plain Absorption drained the claimed creature");
		} finally {
			pig.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = LENT_BATCH, timeoutTicks = 40)
	public static void claimedItemsAreSculptedOnAFullBody(GameTestHelper helper) {
		clear(helper, 1, 6, 2);
		ServerLevel level = helper.getLevel();
		Vec3 ground = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 1, 2)));
		ItemEntity seeds = new ItemEntity(level, ground.x, ground.y, ground.z, new ItemStack(Items.WHEAT_SEEDS, 3));
		seeds.setDeltaMovement(Vec3.ZERO);
		seeds.setNoGravity(true);
		level.addFreshEntity(seeds);
		StubSculpting stub = StubSculpting.claimingAll(VerbTargetKind.ITEMS);
		try (CapturedPlayer captured = CapturedPlayer.create(helper);
				Lent lent = Lent.lend("hemorrhage", Verb.ABSORB, stub)) {
			ServerPlayer player = captured.player;
			prepare(player, "hemorrhage", 0.0D);
			IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
			volume.setBloodVolume(volume.getMaxBloodVolume());
			aim(player, feet(helper, 1, 2), seeds.getBoundingBox().getCenter());
			absorbTick(player);
			helper.assertTrue(stub.targets.size() == 1 && stub.targets.getFirst() instanceof VerbTarget.Items items
					&& items.items().contains(seeds), "the stub was not handed the seeds on a full body: " + stub.targets);
		} finally {
			seeds.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = LENT_BATCH, timeoutTicks = 40)
	public static void openAirIsSculptedAtFullReach(GameTestHelper helper) {
		StubSculpting stub = StubSculpting.claimingAll(VerbTargetKind.AIR);
		try (CapturedPlayer captured = CapturedPlayer.create(helper);
				Lent lent = Lent.lend("void_shroud", Verb.PROJECT, stub)) {
			ServerPlayer player = captured.player;
			prepare(player, "void_shroud", 1000.0D);
			Vec3 feet = feet(helper, 3, 3).add(0.0D, 4.0D, 0.0D);
			aim(player, feet, feet.add(0.0D, 20.0D, 0.0D));
			project(player);
			helper.assertTrue(stub.targets.size() == 1 && stub.targets.getFirst() instanceof VerbTarget.Air air
							&& Math.abs(air.point().distanceTo(player.getEyePosition()) - 5.5D) < 1.0E-3D,
					"the stub was not handed open air at 5.5 blocks: " + stub.targets);
		}
		helper.succeed();
	}

	// ── The plain verb when nothing claims ──

	@GameTest(template = EMPTY, batch = LENT_BATCH, timeoutTicks = 40)
	public static void withoutAClaimThePlainVerbRunsAsBefore(GameTestHelper helper) {
		clear(helper, 1, 6, 2);
		clear(helper, 1, 6, 5);
		BlockPos first = helper.absolutePos(new BlockPos(4, 1, 2));
		BlockPos second = helper.absolutePos(new BlockPos(4, 1, 5));
		helper.getLevel().setBlock(first, Blocks.CAULDRON.defaultBlockState(), 3);
		helper.getLevel().setBlock(second, Blocks.CAULDRON.defaultBlockState(), 3);
		StubSculpting stub = new StubSculpting(Set.of(VerbTargetKind.BLOCK), target -> target instanceof VerbTarget.Block block
				&& block.state().is(Blocks.STONE), false);
		try (CapturedPlayer captured = CapturedPlayer.create(helper);
				Lent lent = Lent.lend("gloam_laceration", Verb.PROJECT, stub)) {
			ServerPlayer player = captured.player;
			prepare(player, null, 1000.0D);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(first));
			helper.assertTrue(SculptingDispatcher.trySculpt(player, Verb.PROJECT).outcome() == SculptingDispatcher.Outcome.NONE,
					"nothing selected still reached a Sculpting");
			project(player);
			helper.assertTrue(helper.getLevel().getBlockEntity(first) instanceof SanguineCondenserBlockEntity,
					"with nothing selected, Projection no longer seats a Condenser in a cauldron");

			prepare(player, "gloam_laceration", 1000.0D);
			aim(player, feet(helper, 1, 5), Vec3.atCenterOf(second));
			helper.assertTrue(SculptingDispatcher.trySculpt(player, Verb.PROJECT).outcome() == SculptingDispatcher.Outcome.PASSED,
					"a Sculpting that only takes stone claimed a cauldron");
			project(player);
			helper.assertTrue(helper.getLevel().getBlockEntity(second) instanceof SanguineCondenserBlockEntity,
					"a Sculpting that did not claim kept the cauldron from the plain verb");
			helper.assertTrue(stub.pulses.isEmpty(), "a Sculpting that did not claim was pulsed");
		}
		helper.succeed();
	}

	// ── Gates and payment ──

	@GameTest(template = EMPTY, batch = LENT_BATCH, timeoutTicks = 40)
	public static void theCastGatesHoldForSculptings(GameTestHelper helper) {
		clear(helper, 1, 6, 2);
		clear(helper, 1, 6, 4);
		clear(helper, 1, 6, 6);
		helper.setBlock(new BlockPos(4, 1, 2), Blocks.STONE);
		helper.setBlock(new BlockPos(4, 1, 4), Blocks.STONE);
		helper.setBlock(new BlockPos(4, 1, 6), Blocks.STONE);
		StubSculpting paralysed = StubSculpting.claimingAll(VerbTargetKind.BLOCK);
		StubSculpting unaligned = StubSculpting.claimingAll(VerbTargetKind.BLOCK);
		StubSculpting dormant = StubSculpting.claimingAll(VerbTargetKind.BLOCK);
		try (CapturedPlayer first = CapturedPlayer.create(helper);
				CapturedPlayer second = CapturedPlayer.create(helper);
				CapturedPlayer third = CapturedPlayer.create(helper);
				Lent a = Lent.lend("synaptic_jolt", Verb.PROJECT, paralysed);
				Lent b = Lent.lend("hematic_riposte", Verb.PROJECT, unaligned);
				Lent c = Lent.lend("hemolymphal_pulse", Verb.PROJECT, dormant)) {
			prepare(first.player, "synaptic_jolt", 1000.0D);
			first.player.addEffect(new MobEffectInstance(EffectInit.disrupted, 200));
			aim(first.player, feet(helper, 1, 2), Vec3.atCenterOf(helper.absolutePos(new BlockPos(4, 1, 2))));
			project(first.player);
			helper.assertTrue(paralysed.pulses.isEmpty()
							&& HemoCapabilityAccess.getBloodVolume(first.player).getBloodVolume() == 1000.0D,
					"a paralysed player's Sculpting pulsed or took blood");

			prepare(second.player, "hematic_riposte", 1000.0D);
			helper.assertTrue(HemoCapabilityAccess.getBloodTemperament(second.player)
					.getAlignmentByTemper(ManipulationInit.getByName("hematic_riposte").getTend())
							< ManipulationInit.getByName("hematic_riposte").getAlignLevel(),
					"the fixture player already meets Hematic Riposte's alignment");
			aim(second.player, feet(helper, 1, 4), Vec3.atCenterOf(helper.absolutePos(new BlockPos(4, 1, 4))));
			project(second.player);
			helper.assertTrue(unaligned.pulses.isEmpty()
							&& HemoCapabilityAccess.getBloodVolume(second.player).getBloodVolume() == 1000.0D,
					"the alignment gate let an unaligned Sculpting pulse");

			prepare(third.player, "hemolymphal_pulse", 1000.0D);
			HemoCapabilityAccess.getBloodVolume(third.player).setActive(false);
			aim(third.player, feet(helper, 1, 6), Vec3.atCenterOf(helper.absolutePos(new BlockPos(4, 1, 6))));
			SculptingDispatcher.Result result = SculptingDispatcher.trySculpt(third.player, Verb.PROJECT);
			helper.assertTrue(result.claimed() && result.bloodPaid() == 0.0D && dormant.pulses.isEmpty(),
					"a dormant volume's Sculpting pulsed: " + result);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = LENT_BATCH, timeoutTicks = 120)
	public static void aHeldSculptingPulsesOnCadenceAndCreditsUseWithoutACooldown(GameTestHelper helper) {
		clear(helper, 1, 6, 2);
		BlockPos stone = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(stone, Blocks.STONE.defaultBlockState(), 3);
		StubSculpting stub = StubSculpting.claimingAll(VerbTargetKind.BLOCK);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		Lent lent = Lent.lend("blood_rush", Verb.PROJECT, stub);
		ServerPlayer player = captured.player;
		prepare(player, "blood_rush", 3000.0D);
		aim(player, feet(helper, 1, 2), Vec3.atCenterOf(stone));
		BloodManipulation rush = ManipulationInit.getByName("blood_rush");
		double xpBefore = HemoCapabilityAccess.getKnownManipulations(player).getManipLevel(rush).getXp();
		int[] ticks = {0};
		helper.onEachTick(() -> {
			if (ticks[0]++ < 85) project(player);
		});
		helper.runAtTickTime(95, () -> {
			try {
				helper.assertTrue(stub.pulses.size() == 9, "85 held ticks pulsed " + stub.pulses.size() + " times, not 9");
				for (int i = 0; i < stub.pulses.size(); i++) {
					helper.assertTrue(stub.pulses.get(i).index() == i, "pulse " + i + " carried index " + stub.pulses.get(i).index());
				}
				double xp = HemoCapabilityAccess.getKnownManipulations(player).getManipLevel(rush).getXp();
				helper.assertTrue(xp >= xpBefore + 1.0D, "80 ticks of paid work credited no use: " + xpBefore + " to " + xp);
				helper.assertTrue(!rush.isOnCooldown(player), "a Sculpting pulse started the manipulation's cooldown");
				helper.assertTrue(captured.sent(ManipCooldownPacket.class).isEmpty(), "a Sculpting pulse sent a cooldown packet");
				helper.assertTrue(SculptingSessions.isActive(player.getUUID()) && stub.begun == 1,
						"the held session did not stay open once");
				SculptingSessions.end(player, true);
				helper.assertTrue(stub.ended == 1 && !SculptingSessions.isActive(player.getUUID()),
						"letting go did not end the session");
			} finally {
				lent.close();
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, batch = LENT_BATCH, timeoutTicks = 60)
	public static void theCastPresentationFollowsTheChannelNotTheTarget(GameTestHelper helper) {
		clear(helper, 1, 6, 2);
		BlockPos lower = helper.absolutePos(new BlockPos(4, 1, 2));
		BlockPos upper = helper.absolutePos(new BlockPos(4, 2, 2));
		helper.getLevel().setBlock(lower, Blocks.STONE.defaultBlockState(), 3);
		helper.getLevel().setBlock(upper, Blocks.STONE.defaultBlockState(), 3);
		StubSculpting stub = StubSculpting.claimingAll(VerbTargetKind.BLOCK);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		Lent lent = Lent.lend("sanguine_mending", Verb.PROJECT, stub);
		ServerPlayer player = captured.player;
		prepare(player, "sanguine_mending", 3000.0D);
		Vec3 feet = feet(helper, 1, 2);
		// Snapshots: [begun, ended, pulses, active] after each tick of a held channel.
		List<int[]> seen = new ArrayList<>();
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > 32) return;
			aim(player, feet, Vec3.atCenterOf(t == 0 ? lower : upper));
			if (t == 12) player.addEffect(new MobEffectInstance(EffectInit.disrupted, 200));
			if (t == 22) player.removeEffect(EffectInit.disrupted);
			project(player);
			seen.add(new int[] {stub.begun, stub.ended, stub.pulses.size(), SculptingSessions.isActive(player.getUUID()) ? 1 : 0});
		});
		helper.runAtTickTime(40, () -> {
			try {
				int[] first = seen.get(0);
				helper.assertTrue(first[0] == 1 && first[2] == 1 && first[3] == 1, "the first paid pulse did not open the session");
				int[] moved = seen.get(1);
				helper.assertTrue(moved[1] == 1 && moved[0] == 1 && moved[3] == 1,
						"moving to a new target did not end the old work while keeping the presentation: begun "
								+ moved[0] + ", ended " + moved[1] + ", active " + moved[3]);
				int[] second = seen.get(10);
				helper.assertTrue(second[0] == 2 && second[2] == 2 && second[3] == 1,
						"the new target did not begin on its first paid pulse");
				int[] refused = seen.get(20);
				helper.assertTrue(refused[2] == 2 && refused[3] == 0,
						"a refused pulse worked or kept the cast presentation up");
				int[] resumed = seen.get(30);
				helper.assertTrue(resumed[2] == 3 && resumed[3] == 1 && resumed[0] == 2,
						"a paid pulse after the refusal did not bring the presentation back on the same session");
				SculptingSessions.end(player, true);
				helper.assertTrue(stub.ended == 2 && !SculptingSessions.isActive(player.getUUID()),
						"letting go did not end the session");
			} finally {
				lent.close();
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, batch = LENT_BATCH, timeoutTicks = 40)
	public static void aSculptingDecidesItsOwnRooting(GameTestHelper helper) {
		clear(helper, 1, 6, 2);
		helper.setBlock(new BlockPos(4, 1, 2), Blocks.STONE);
		StubSculpting free = StubSculpting.claimingAll(VerbTargetKind.BLOCK);
		try (CapturedPlayer captured = CapturedPlayer.create(helper);
				Lent lent = Lent.lend("blood_shot", Verb.ABSORB, free)) {
			ServerPlayer player = captured.player;
			prepare(player, null, 500.0D);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(helper.absolutePos(new BlockPos(4, 1, 2))));
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_absorption.get()));
			player.startUsingItem(InteractionHand.MAIN_HAND);
			double plain = VerbChannel.movementMultiplier(player);
			helper.assertTrue(plain < 1.0D, "plain Absorption no longer roots an unskilled player: " + plain);
			prepare(player, "blood_shot", 500.0D);
			helper.assertTrue(VerbChannel.movementMultiplier(player) == 1.0D,
					"a claiming Sculpting that does not root still held the player");
			player.stopUsingItem();
		}
		helper.succeed();
	}

	// ── Earlier claims win ──

	@GameTest(template = EMPTY, batch = LENT_BATCH, timeoutTicks = 40)
	public static void aFalteringWillStillTakesTheProjection(GameTestHelper helper) {
		clear(helper, 1, 6, 2);
		WillEntity will = helper.spawn(EntityInit.will.get(), new BlockPos(4, 1, 2));
		will.configure(WillOrigin.BROKEN, EnumBloodTemper.LUX, 1, null, false);
		will.setPhase(WillPhase.FALTERING);
		StubSculpting stub = StubSculpting.claimingAll(VerbTargetKind.CREATURE, VerbTargetKind.BLOCK);
		try (CapturedPlayer captured = CapturedPlayer.create(helper);
				Lent lent = Lent.lend("vital_effusion", Verb.PROJECT, stub)) {
			ServerPlayer player = captured.player;
			prepare(player, "vital_effusion", 2000.0D);
			aim(player, feet(helper, 1, 2), will.getBoundingBox().getCenter());
			double spent = project(player);
			helper.assertTrue(stub.pulses.isEmpty(), "a Sculpting took a projection the faltering Will claims");
			helper.assertTrue(spent > 0.0D && will.getPhase() == WillPhase.DISSOLVING,
					"the Will was not banished ahead of the Sculpting: spent " + spent + ", phase " + will.getPhase());
		} finally {
			will.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = LENT_BATCH, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesTheProjection(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var center = helper.absolutePos(new BlockPos(12, 70, 12));
		var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
				level, Hemomancy.rloc("cardinal_rite/apotheos_rite"));
		StubSculpting stub = StubSculpting.claimingAll(VerbTargetKind.BLOCK);
		try (CapturedPlayer captured = CapturedPlayer.create(helper).online();
				Lent lent = Lent.lend("glacial_grasp", Verb.PROJECT, stub)) {
			ServerPlayer actor = captured.player;
			var rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(
					actor.getUUID(), center, recipe.getId(), 3600, 9, 7, false, 6, 32);
			for (int i = 0; i < 32; i++) rite.fillAnchor(i, 50);
			helper.assertTrue(rite.enterInscription(), "the fixture rite did not enter inscription");
			var id = Hemomancy.rloc("bastion");
			var sigil = com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry.get(id);
			var socket = recipe.getCeremony().supportSockets().getFirst();
			var occupied = new java.util.HashSet<BlockPos>();
			for (var anchor : recipe.getCeremony().anchors()) occupied.add(new BlockPos(anchor.x(), 0, anchor.z()));
			var placement = com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules.resolveSupportPlacement(
					new BlockPos(socket.x(), 0, socket.z()), sigil.nodes(), occupied);
			var node = sigil.nodes().getFirst();
			rite.setSigilProgress(id.toString(), sigil.nodes().size());
			rite.awakenSigil(id.toString());
			var saved = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level);
			saved.startRite(rite);
			prepare(actor, "glacial_grasp", 2000.0D);
			HemoCapabilityAccess.getInitiatoryDegree(actor).setDegreeNumber(7);
			var floor = center.offset(placement.getX() + (int) Math.round(node.x()), socket.y(),
					placement.getZ() + (int) Math.round(node.z()));
			var backdrop = floor.offset(0, 1, 3);
			var oldFloor = level.getBlockState(floor);
			var oldBackdrop = level.getBlockState(backdrop);
			level.setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
			level.setBlock(backdrop, Blocks.STONE.defaultBlockState(), 3);
			var aim = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTargetGeometry.sigilAimPoint(
					center, floor.above(), placement.getX(), placement.getZ(), node.x(), node.z());
			actor.setPos(aim.x, aim.y - actor.getEyeHeight(), aim.z - 2);
			actor.lookAt(EntityAnchorArgument.Anchor.EYES, aim);
			try {
				helper.assertTrue(SculptingDispatcher.currentClaim(actor, Verb.PROJECT).isPresent(),
						"the fixture's Sculpting would not claim the stone behind the sigil, so the test proves nothing");
				for (int tick = 0; tick < 3; tick++) project(actor);
				helper.assertTrue(stub.pulses.isEmpty(), "a Sculpting took a projection the awakened sigil claims");
				helper.assertTrue(HemoCapabilityAccess.getBloodVolume(actor).getBloodVolume() == 2000.0D,
						"the rite's claim moved blood");
			} finally {
				saved.removeRite(actor.getUUID());
				level.setBlock(floor, oldFloor, 3);
				level.setBlock(backdrop, oldBackdrop, 3);
			}
		}
		helper.succeed();
	}

	// ── The verb key ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aWheelPickIsThePlainVerbAndPutsTheMemoryDown(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "blood_lash", 3000.0D);
			var known = HemoCapabilityAccess.getKnownManipulations(player);
			helper.assertTrue(known.getSelectedManip() != BloodManipulation.BLANK,
					"the fixture selected nothing, so the test proves nothing");
			player.getInventory().selected = 0;
			player.getInventory().setItem(0, ItemStack.EMPTY);
			VerbSelection.choose(player, Verb.ABSORB);
			helper.assertTrue(VerbChannel.activeVerb(player) == Verb.ABSORB, "the wheel pick did not set Absorption");
			helper.assertTrue(player.getMainHandItem().is(ItemInit.blood_absorption.get()),
					"the wheel pick did not give an empty hand the Absorption hand");
			helper.assertTrue(known.getSelectedManip() == BloodManipulation.BLANK,
					"the wheel pick left a memory selected, so the verb is still sculpted");
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.ABSORB).isEmpty(),
					"a Sculpting still claims after the plain verb was picked");
			double afterConjure = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
			VerbSelection.choose(player, Verb.PROJECT);
			helper.assertTrue(VerbChannel.activeVerb(player) == Verb.PROJECT
							&& player.getMainHandItem().is(ItemInit.blood_projection.get()),
					"picking Projection did not turn the held hand");
			helper.assertTrue(HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() == afterConjure,
					"turning the hand from the wheel cost blood");
			VerbSelection.choose(player, Verb.PROJECT);
			helper.assertTrue(VerbChannel.activeVerb(player) == Verb.PROJECT, "picking the active verb again changed it");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theVerbKeyConjuresSwitchesAndDispels(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, null, 3000.0D);
			player.getInventory().selected = 0;
			player.getInventory().setItem(0, ItemStack.EMPTY);
			helper.assertTrue(VerbChannel.activeVerb(player) == Verb.PROJECT, "Projection is not the first verb");
			VerbSelection.press(player);
			helper.assertTrue(player.getMainHandItem().is(ItemInit.blood_projection.get()),
					"the verb key did not conjure the Projection hand into an empty hand");
			double afterConjure = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
			helper.assertTrue(afterConjure < 3000.0D, "conjuring the hand cost nothing");
			VerbSelection.press(player);
			helper.assertTrue(VerbChannel.activeVerb(player) == Verb.ABSORB
							&& player.getMainHandItem().is(ItemInit.blood_absorption.get()),
					"the verb key did not switch the held hand to Absorption");
			helper.assertTrue(HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() == afterConjure,
					"switching the verb cost blood");
			player.setShiftKeyDown(true);
			VerbSelection.press(player);
			player.setShiftKeyDown(false);
			helper.assertTrue(player.getMainHandItem().isEmpty(), "a sneaking press did not let the hand go");
			helper.assertTrue(HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() > afterConjure,
					"letting the hand go refunded nothing");
			player.getInventory().setItem(0, new ItemStack(ItemInit.living_staff.get()));
			VerbSelection.press(player);
			helper.assertTrue(VerbChannel.activeVerb(player) == Verb.PROJECT
							&& player.getMainHandItem().is(ItemInit.living_staff.get()),
					"the verb key did not switch the Staff's verb");
		}
		helper.succeed();
	}
}

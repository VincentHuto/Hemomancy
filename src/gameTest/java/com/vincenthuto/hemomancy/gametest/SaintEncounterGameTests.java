package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.ContainmentAnchorBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.RotPyreBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.SporeShelfBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.ContainmentAnchorEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeState;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.EnumCorpusState;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/**
 * A Saint cannot be killed: every ending rolls its loot table once and closes it back into its sarcophagus, once. An
 * unnamed sarcophagus holds no Saint until a relic names it. Seraphae is a trial Saint whose containment is unchanged.
 */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SaintEncounterGameTests {
	private static final BlockPos TOMB = new BlockPos(8, 2, 8);

	private SaintEncounterGameTests() {
	}

	@GameTest(templateNamespace = "hemomancy", template = "empty", timeoutTicks = 40)
	public static void unassignedSarcophagusRefusesAllButARelic(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		BlockPos tomb = helper.absolutePos(TOMB);
		VelorumEntity boss = null;
		try {
			BlockState state = placeTomb(helper, tomb);
			SaintSarcophagusBlockEntity sarcophagus = tile(helper, tomb);
			for (int tick = 0; tick < 5; tick++) sarcophagus.tick();
			helper.assertTrue(!sarcophagus.isSaintTypeAssigned() && sarcophagus.getSaintType() == null,
					"A placed sarcophagus picked a Saint at random");

			state.useWithoutItem(helper.getLevel(), player, hit(tomb));
			ItemStack paste = new ItemStack(ItemInit.foul_paste.get(), 2);
			state.useItemOn(paste, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit(tomb));
			state.useItemOn(new ItemStack(Items.DIRT), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit(tomb));
			helper.assertTrue(paste.getCount() == 2, "Foul Paste was spent on a sarcophagus with no Saint");
			helper.assertTrue(helper.getLevel().getBlockState(tomb).is(BlockInit.saint_sarcophagus.get())
					&& !sarcophagus.isOpen() && !sarcophagus.isConsecrated()
					&& sarcophagus.getCorpusState() == EnumCorpusState.DORMANT && !sarcophagus.isSaintTypeAssigned(),
					"An unassigned sarcophagus did not stay inert");
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(SaintTrialEntity.class, new AABB(tomb).inflate(4)).isEmpty(),
					"An unassigned sarcophagus woke a Saint");

			ItemStack relic = new ItemStack(ItemInit.saint_relic_velorum.get());
			state.useItemOn(relic, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit(tomb));
			boss = velorumAt(helper, tomb);
			helper.assertTrue(boss != null && tomb.equals(boss.getTombPos()),
					"A relic did not name and wake the sarcophagus's Saint");
			helper.assertTrue(relic.isEmpty(), "The relic was not spent");
			helper.succeed();
		} finally {
			if (boss != null) boss.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "hemomancy", template = "empty", timeoutTicks = 120)
	public static void killedSaintReturnsToItsTombUnrewarded(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		BlockPos tomb = helper.absolutePos(TOMB);
		BlockState state = placeTomb(helper, tomb);
		state.useItemOn(new ItemStack(ItemInit.saint_relic_velorum.get()), helper.getLevel(), player,
				InteractionHand.MAIN_HAND, hit(tomb));
		VelorumEntity boss = velorumAt(helper, tomb);
		helper.assertTrue(boss != null, "The relic did not wake Velorum");
		helper.assertTrue(!boss.shouldDespawnInPeaceful() && !boss.canUsePortal(false),
				"A Saint can leave its trial without ending it");

		// /kill is no ending: the body clears after its death pose and only then goes back into the tomb
		boss.kill();
		helper.assertFalse(boss.hasTrialEnded(), "/kill ended the trial like a fall");
		helper.succeedWhen(() -> {
			helper.assertTrue(boss.isRemoved() && boss.hasTrialEnded(), "/kill did not send Velorum back to the tomb");
			assertDormantTomb(helper, tomb, EnumSaintType.VELORUM);
			helper.assertTrue(dropped(helper, tomb, ItemInit.hallowed_residuum_velorum.get()) == 0,
					"/kill rewarded the trial");
			player.discard();
		});
	}

	@GameTest(templateNamespace = "hemomancy", template = "empty", timeoutTicks = 40)
	public static void velorumFallClosesTheTombOnceAndRewardsOnce(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		BlockPos tomb = helper.absolutePos(TOMB);
		VelorumEntity boss = null;
		try {
			BlockState state = placeTomb(helper, tomb);
			state.useItemOn(new ItemStack(ItemInit.saint_relic_velorum.get()), helper.getLevel(), player,
					InteractionHand.MAIN_HAND, hit(tomb));
			boss = velorumAt(helper, tomb);
			helper.assertTrue(boss != null, "The relic did not wake Velorum");
			helper.assertTrue(helper.getLevel().getBlockState(tomb).isAir(), "The encounter did not clear the sarcophagus");

			DamageSource blow = boss.damageSources().playerAttack(player);
			boss.die(blow);
			helper.assertTrue(boss.hasTrialEnded(), "Velorum's fall did not end the trial");
			assertDormantTomb(helper, tomb, EnumSaintType.VELORUM);
			helper.assertTrue(dropped(helper, tomb, ItemInit.hallowed_residuum_velorum.get()) == 1,
					"Velorum's fall did not drop exactly one residuum");
			int quintessence = dropped(helper, tomb, ItemInit.sanguine_quintessence.get());
			helper.assertTrue(quintessence >= 1 && quintessence <= 2, "Velorum's table did not roll its quintessence");

			CompoundTag saved = new CompoundTag();
			boss.addAdditionalSaveData(saved);
			helper.assertTrue(saved.getBoolean("SaintTrialEnded"), "The ended trial is not saved");

			// a second fall, a discard, or a reloaded copy must not reward or restore again
			helper.getLevel().setBlock(tomb, Blocks.AIR.defaultBlockState(), 3);
			boss.die(blow);
			boss.discard();
			VelorumEntity reloaded = EntityInit.velorum.get().create(helper.getLevel());
			reloaded.setPos(boss.position());
			reloaded.readAdditionalSaveData(saved);
			reloaded.die(blow);
			reloaded.discard();
			helper.assertTrue(helper.getLevel().getBlockState(tomb).isAir(), "The tomb was restored a second time");
			helper.assertTrue(dropped(helper, tomb, ItemInit.hallowed_residuum_velorum.get()) == 1,
					"Velorum's reward rolled a second time");
			helper.succeed();
		} finally {
			if (boss != null) boss.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "hemomancy", template = "empty", timeoutTicks = 40)
	public static void hemorathOverloadRewardsTheVictorAndClosesTheTomb(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		BlockPos tomb = helper.absolutePos(TOMB);
		HemorathEntity boss = EntityInit.hemorath.get().create(helper.getLevel());
		helper.assertTrue(boss != null, "Hemorath could not be created");
		try {
			BlockState state = clearTombSpace(helper, tomb);
			boss.setPos(Vec3.atBottomCenterOf(tomb));
			boss.bindTomb(tomb, state);
			player.setPos(boss.position());
			var blood = HemoCapabilityAccess.getBloodVolume(player);
			blood.addDamage(20.0D);

			boss.addAbsorbedBlood(player, HemorathEntity.OVERLOAD_THRESHOLD);
			for (int tick = 0; tick < 60 && !boss.isRemoved(); tick++) boss.aiStep();
			helper.assertTrue(boss.isRemoved() && boss.hasTrialEnded(), "Hemorath's overload did not end the trial");
			helper.assertTrue(carried(player, ItemInit.hallowed_residuum_hemorath.get()) == 1,
					"The overload did not put exactly one residuum in the victor's inventory");
			helper.assertTrue(dropped(helper, tomb, ItemInit.hallowed_residuum_hemorath.get()) == 0,
					"Hemorath's residuum dropped instead of going to the victor");
			helper.assertTrue(blood.getBloodDebt() == 0.0D, "The victor's blood debt was not settled");
			assertDormantTomb(helper, tomb, EnumSaintType.HEMORATH);
			helper.succeed();
		} finally {
			boss.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "hemomancy", template = "empty", timeoutTicks = 40)
	public static void seraphaeIsATrialSaintWithItsContainmentUnchanged(GameTestHelper helper) {
		ServerPlayer player = player(helper);
		BlockPos tomb = helper.absolutePos(TOMB);
		SeraphaeEntity boss = EntityInit.seraphae.get().create(helper.getLevel());
		helper.assertTrue(boss != null, "Seraphae could not be created");
		try {
			helper.assertTrue(SaintTrialEntity.class.isInstance(boss) && boss.saintType() == EnumSaintType.SERAPHAE,
					"Seraphae is not a trial Saint");
			BlockState state = clearTombSpace(helper, tomb);
			boss.setPos(Vec3.atBottomCenterOf(tomb));
			boss.bindTomb(tomb, state);

			int stable = SeraphaeState.STABLE.duration();
			helper.assertTrue(boss.getSeraphaeState() == SeraphaeState.STABLE && boss.getStateTimer() == stable
					&& boss.getContainmentIntegrity() == 50.0F, "Seraphae does not start stable at half integrity");
			boss.aiStep();
			helper.assertTrue(Math.abs(boss.getContainmentIntegrity() - (50.0F - 0.01F)) < 1.0E-4F,
					"Seraphae's integrity decay changed: " + boss.getContainmentIntegrity());
			helper.assertTrue(boss.getStateTimer() == stable - 1, "Seraphae's state clock changed");
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(ContainmentAnchorEntity.class,
					new AABB(tomb).inflate(32), anchor -> boss.getUUID().equals(anchor.getParentId())).size() == 4,
					"Seraphae did not place her four anchors");

			boss.hurt(boss.damageSources().playerAttack(player), 10000.0F);
			helper.assertTrue(boss.isAlive() && boss.getHealth() == 1.0F, "Seraphae lost her health floor");

			CompoundTag saved = new CompoundTag();
			boss.addAdditionalSaveData(saved);
			for (String key : new String[] { "ContainmentIntegrity", "SeraphaeState", "StateTimer", "AnchorsPlaced",
					"Defeated", "SaintTombPos", "SaintTombState", "SaintTrialEnded" }) {
				helper.assertTrue(saved.contains(key), "Seraphae no longer saves " + key);
			}
			// the rise is saved only while it runs, as for every trial Saint
			helper.assertTrue(!saved.contains("SaintRise"), "Seraphae saved a finished rise");
			SeraphaeEntity reloaded = EntityInit.seraphae.get().create(helper.getLevel());
			reloaded.readAdditionalSaveData(saved);
			helper.assertTrue(reloaded.getContainmentIntegrity() == boss.getContainmentIntegrity()
					&& reloaded.getStateTimer() == boss.getStateTimer() && tomb.equals(reloaded.getTombPos()),
					"Seraphae's save did not round-trip");
			reloaded.discard();

			// fragments captured through anchors raise integrity five points each; full containment chains her
			for (int i = 0; i < 11; i++) boss.onFragmentContained(true);
			boss.aiStep();
			helper.assertTrue(boss.isRemoved() && boss.hasTrialEnded(), "Full containment did not end Seraphae's trial");
			assertDormantTomb(helper, tomb, EnumSaintType.SERAPHAE);
			// her residuum drops at her feet unless a real player stands in the arena to receive it
			helper.assertTrue(dropped(helper, tomb, ItemInit.hallowed_residuum_seraphae.get())
					+ helper.getLevel().players().stream().mapToInt(viewer -> carried(viewer, ItemInit.hallowed_residuum_seraphae.get())).sum() == 1,
					"Seraphae's containment did not give exactly one residuum");
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(ContainmentAnchorEntity.class,
					new AABB(tomb).inflate(32), anchor -> boss.getUUID().equals(anchor.getParentId())).isEmpty(),
					"Seraphae's anchors outlived her containment");
			helper.succeed();
		} finally {
			boss.discard();
			player.discard();
		}
	}

	/**
	 * In her chamber the fight's anchors wake from the four whole block anchors round the tomb (R1): each stands on its
	 * block's cell, the block clears while it stands, and it is built again when the anchor goes, unless the cell is
	 * taken. Snapped anchors and anchors out of reach stay as they are.
	 */
	@GameTest(templateNamespace = "hemomancy", template = "empty", timeoutTicks = 40)
	public static void seraphaeWakesTheChambersBlockAnchorsAndRestoresThem(GameTestHelper helper) {
		BlockPos tomb = helper.absolutePos(TOMB);
		BlockPos[] fight = { tomb.offset(-2, 1, -6), tomb.offset(-2, 1, 6), tomb.offset(3, 0, -4), tomb.offset(3, 0, 4) };
		BlockPos snapped = tomb.offset(0, 0, -3);
		BlockPos aisle = tomb.offset(11, 0, 0);
		SeraphaeEntity boss = EntityInit.seraphae.get().create(helper.getLevel());
		helper.assertTrue(boss != null, "Seraphae could not be created");
		try {
			BlockState state = clearTombSpace(helper, tomb);
			for (BlockPos pos : fight) placeAnchorBlock(helper, pos, false);
			placeAnchorBlock(helper, snapped, true);
			placeAnchorBlock(helper, aisle, false);
			boss.setPos(Vec3.atBottomCenterOf(tomb));
			boss.bindTomb(tomb, state);
			boss.aiStep();

			var anchors = helper.getLevel().getEntitiesOfClass(ContainmentAnchorEntity.class, new AABB(tomb).inflate(32),
					anchor -> boss.getUUID().equals(anchor.getParentId()));
			helper.assertTrue(anchors.size() == 4, "Seraphae did not wake four anchors: " + anchors.size());
			for (BlockPos pos : fight) {
				helper.assertTrue(anchors.stream().anyMatch(anchor -> pos.equals(anchor.getHome())
						&& anchor.position().equals(Vec3.atBottomCenterOf(pos))), "No fight anchor stands on the block anchor at " + pos);
				helper.assertTrue(helper.getLevel().getBlockState(pos).isAir() && helper.getLevel().getBlockState(pos.above()).isAir(),
						"The woken block anchor at " + pos + " did not clear");
			}
			helper.assertTrue(isWholeAnchor(helper, snapped, true), "A snapped anchor woke");
			helper.assertTrue(isWholeAnchor(helper, aisle, false), "An anchor out of reach woke");

			// one cell is taken while its anchor stands: that block stays gone; the rest come back when she goes
			BlockPos taken = fight[0];
			helper.getLevel().setBlock(taken, Blocks.STONE.defaultBlockState(), 3);
			boss.discard();
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(ContainmentAnchorEntity.class, new AABB(tomb).inflate(32),
					anchor -> boss.getUUID().equals(anchor.getParentId())).isEmpty(), "Seraphae's anchors outlived her");
			for (int i = 1; i < fight.length; i++) {
				helper.assertTrue(isWholeAnchor(helper, fight[i], false), "The block anchor at " + fight[i] + " was not restored");
			}
			helper.assertTrue(helper.getLevel().getBlockState(taken).is(Blocks.STONE)
					&& helper.getLevel().getBlockState(taken.above()).isAir(), "An anchor was restored over a taken cell");
			helper.succeed();
		} finally {
			boss.discard();
		}
	}

	/**
	 * Putriciel's phases light his chamber: burning, its Rot Pyres are lit and its Spore Shelves dim; the absolution
	 * window gutters the pyres and blooms the shelves, and closing it turns them back. However he goes, the chamber is
	 * left burning, except a cell taken since. The tomb faces east from the template's west edge, so the chamber's box
	 * (37 x 17 x 27 round it) stays inside this test's spacing; which lights fall outside the box is
	 * {@code PutricielChamberLightingRulesTest}'s.
	 */
	@GameTest(templateNamespace = "hemomancy", template = "empty", timeoutTicks = 40)
	public static void putricielsPhasesAlternateTheChambersPyresAndSporeShelves(GameTestHelper helper) {
		BlockPos tomb = helper.absolutePos(new BlockPos(4, 2, 12));
		// the dais corners ahead of the tomb's foot, and two shelves further down the hall
		BlockPos[] pyres = { tomb.offset(4, 0, -3), tomb.offset(4, 0, 3) };
		BlockPos[] shelves = { tomb.offset(9, 2, -6), tomb.offset(9, 2, 6) };
		DrivenPutriciel boss = new DrivenPutriciel(helper.getLevel());
		try {
			for (BlockPos pos : BlockPos.betweenClosed(tomb.offset(-2, 0, -2), tomb.offset(2, 2, 2))) {
				helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			}
			BlockState state = BlockInit.saint_sarcophagus.get().defaultBlockState().setValue(SaintSarcophagusBlock.FACING, Direction.EAST);
			for (BlockPos pos : pyres) placePyre(helper, pos);
			for (BlockPos pos : shelves) placeShelf(helper, pos);
			boss.setPos(Vec3.atBottomCenterOf(tomb));
			boss.bindTomb(tomb, state);
			helper.assertTrue(helper.getLevel().addFreshEntity(boss), "Putriciel could not be added");
			boss.aiStep(); // the fight's first tick finds the lights; burning leaves them as they stand
			assertChamberLighting(helper, pyres, shelves, true, "at the fight's start");

			boss.open();
			assertChamberLighting(helper, pyres, shelves, false, "in the absolution window");
			boss.close();
			assertChamberLighting(helper, pyres, shelves, true, "after the window closed");

			// he goes with the window open and one shelf's cell taken: the rest of the chamber is left burning
			boss.open();
			BlockPos taken = shelves[0];
			helper.getLevel().setBlock(taken, Blocks.STONE.defaultBlockState(), 3);
			boss.discard();
			for (BlockPos pos : pyres) helper.assertTrue(isPyre(helper, pos, true), "The pyre at " + pos + " was not relit");
			helper.assertTrue(isShelf(helper, shelves[1], false), "The shelf at " + shelves[1] + " was left in bloom");
			helper.assertTrue(helper.getLevel().getBlockState(taken).is(Blocks.STONE), "A taken cell was overwritten");
			helper.succeed();
		} finally {
			boss.discard();
		}
	}

	/** Putriciel with his absolution window opened and closed on demand. */
	private static final class DrivenPutriciel extends PutricielEntity {
		DrivenPutriciel(Level level) {
			super(EntityInit.putriciel.get(), level);
		}

		void open() {
			openAbsolutionWindow((ServerLevel) level());
		}

		void close() {
			endAbsolutionWindow((ServerLevel) level());
		}
	}

	private static void assertChamberLighting(GameTestHelper helper, BlockPos[] pyres, BlockPos[] shelves, boolean burning,
			String when) {
		for (BlockPos pos : pyres) {
			helper.assertTrue(isPyre(helper, pos, burning), "The pyre at " + pos + " is not " + (burning ? "lit" : "guttered") + " " + when);
		}
		for (BlockPos pos : shelves) {
			helper.assertTrue(isShelf(helper, pos, !burning), "The shelf at " + pos + " is not " + (burning ? "dim" : "in bloom") + " " + when);
		}
	}

	private static void placePyre(GameTestHelper helper, BlockPos pos) {
		helper.getLevel().setBlock(pos, BlockInit.rot_pyre.get().defaultBlockState().setValue(RotPyreBlock.LIT, true), 3);
	}

	/** A dim shelf with stone at its back, in case it hangs from a wall. */
	private static void placeShelf(GameTestHelper helper, BlockPos pos) {
		BlockState shelf = BlockInit.spore_shelf.get().defaultBlockState().setValue(SporeShelfBlock.BLOOM, false);
		shelf.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING)
				.ifPresent(facing -> helper.getLevel().setBlock(pos.relative(facing.getOpposite()), Blocks.STONE.defaultBlockState(), 3));
		helper.getLevel().setBlock(pos, shelf, 3);
	}

	private static boolean isPyre(GameTestHelper helper, BlockPos pos, boolean lit) {
		BlockState state = helper.getLevel().getBlockState(pos);
		return state.is(BlockInit.rot_pyre.get()) && state.getValue(RotPyreBlock.LIT) == lit;
	}

	private static boolean isShelf(GameTestHelper helper, BlockPos pos, boolean bloom) {
		BlockState state = helper.getLevel().getBlockState(pos);
		return state.is(BlockInit.spore_shelf.get()) && state.getValue(SporeShelfBlock.BLOOM) == bloom;
	}

	private static void placeAnchorBlock(GameTestHelper helper, BlockPos lower, boolean snapped) {
		BlockState anchor = BlockInit.containment_anchor_block.get().defaultBlockState()
				.setValue(ContainmentAnchorBlock.SNAPPED, snapped);
		helper.getLevel().setBlock(lower, anchor.setValue(ContainmentAnchorBlock.HALF, DoubleBlockHalf.LOWER), 3);
		helper.getLevel().setBlock(lower.above(), anchor.setValue(ContainmentAnchorBlock.HALF, DoubleBlockHalf.UPPER), 3);
	}

	private static boolean isWholeAnchor(GameTestHelper helper, BlockPos lower, boolean snapped) {
		BlockState bottom = helper.getLevel().getBlockState(lower);
		BlockState top = helper.getLevel().getBlockState(lower.above());
		return bottom.is(BlockInit.containment_anchor_block.get()) && top.is(BlockInit.containment_anchor_block.get())
				&& bottom.getValue(ContainmentAnchorBlock.HALF) == DoubleBlockHalf.LOWER
				&& top.getValue(ContainmentAnchorBlock.HALF) == DoubleBlockHalf.UPPER
				&& bottom.getValue(ContainmentAnchorBlock.SNAPPED) == snapped;
	}

	/** A south-facing sarcophagus with no Saint, placed the way the world places it (block, then fillers). */
	private static BlockState placeTomb(GameTestHelper helper, BlockPos tomb) {
		BlockState state = clearTombSpace(helper, tomb);
		helper.getLevel().setBlock(tomb, state, 3);
		((SaintSarcophagusBlock) state.getBlock()).placeFillers(helper.getLevel(), tomb, state);
		return state;
	}

	/** Clears the tomb's cells and returns the sarcophagus state that would stand there. */
	private static BlockState clearTombSpace(GameTestHelper helper, BlockPos tomb) {
		for (BlockPos pos : BlockPos.betweenClosed(tomb.offset(-1, 0, -2), tomb.offset(1, 2, 2))) {
			helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		}
		return BlockInit.saint_sarcophagus.get().defaultBlockState().setValue(SaintSarcophagusBlock.FACING, Direction.SOUTH);
	}

	private static SaintSarcophagusBlockEntity tile(GameTestHelper helper, BlockPos tomb) {
		if (helper.getLevel().getBlockEntity(tomb) instanceof SaintSarcophagusBlockEntity sarcophagus) return sarcophagus;
		throw new net.minecraft.gametest.framework.GameTestAssertException("No sarcophagus at " + tomb);
	}

	private static void assertDormantTomb(GameTestHelper helper, BlockPos tomb, EnumSaintType saint) {
		helper.assertTrue(helper.getLevel().getBlockState(tomb).is(BlockInit.saint_sarcophagus.get()),
				"The sarcophagus was not restored");
		SaintSarcophagusBlockEntity sarcophagus = tile(helper, tomb);
		helper.assertTrue(sarcophagus.getSaintType() == saint, "The restored tomb holds the wrong Saint");
		helper.assertTrue(sarcophagus.getCorpusState() == EnumCorpusState.DORMANT && !sarcophagus.isOpen()
				&& !sarcophagus.isConsecrated() && !sarcophagus.hasSampleYielded() && !sarcophagus.isOnCooldown()
				&& sarcophagus.getOfferingBloodVolume() == 0, "The restored tomb is not dormant and closed");
	}

	private static VelorumEntity velorumAt(GameTestHelper helper, BlockPos tomb) {
		var found = helper.getLevel().getEntitiesOfClass(VelorumEntity.class, new AABB(tomb).inflate(3));
		return found.isEmpty() ? null : found.get(0);
	}

	private static int dropped(GameTestHelper helper, BlockPos tomb, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(tomb).inflate(6),
				drop -> drop.isAlive() && drop.getItem().is(item)).stream().mapToInt(drop -> drop.getItem().getCount()).sum();
	}

	private static int carried(net.minecraft.world.entity.player.Player player, Item item) {
		return player.getInventory().items.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
	}

	private static BlockHitResult hit(BlockPos tomb) {
		return new BlockHitResult(Vec3.atCenterOf(tomb), Direction.UP, tomb, false);
	}

	private static ServerPlayer player(GameTestHelper helper) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), "saint-encounter-test"), false);
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				cookie.gameProfile(), cookie.clientInformation());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
			}
		};
		player.setPos(helper.absolutePos(TOMB.offset(2, 0, 0)).getCenter());
		return player;
	}
}

package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

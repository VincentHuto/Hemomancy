package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelWaveEvents;
import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeFragmentEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * FX-18: a Chapel trial survives a reload, says when it is cooling down, waits on a wave it cannot see, keeps its wave
 * in Peaceful, and its waves leave nothing to farm. Seraphae's wave is her own light fragments (NQ-08-23).
 */
@GameTestHolder("registration_validation")
@PrefixGameTestTemplate(false)
public final class ChapelRobustnessGameTests {
	private ChapelRobustnessGameTests() {
	}

	private static ChapelFocusBlockEntity focus(GameTestHelper helper, BlockPos relative, ChapelRules.Kind kind) {
		BlockPos pos = helper.absolutePos(relative);
		helper.getLevel().setBlockAndUpdate(pos, BlockInit.chapel_focus.get().defaultBlockState());
		ChapelFocusBlockEntity focus = (ChapelFocusBlockEntity) helper.getLevel().getBlockEntity(pos);
		focus.configure(kind, 0);
		return focus;
	}

	private static void tick(GameTestHelper helper, ChapelFocusBlockEntity focus, int ticks) {
		for (int i = 0; i < ticks; i++) {
			ChapelFocusBlockEntity.serverTick(helper.getLevel(), focus.getBlockPos(), focus.getBlockState(), focus);
		}
	}

	private static CompoundTag saved(GameTestHelper helper, ChapelFocusBlockEntity focus) {
		return focus.saveWithoutMetadata(helper.getLevel().registryAccess());
	}

	private static void clearWaves(GameTestHelper helper) {
		AABB box = new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16);
		helper.getLevel().getEntitiesOfClass(Mob.class, box).forEach(Mob::discard);
	}

	@GameTest(template = "empty")
	public static void aReloadKeepsTheTrialTimerAndTheFailCooldown(GameTestHelper helper) {
		ChapelFocusBlockEntity repose = focus(helper, new BlockPos(1, 1, 1), ChapelRules.Kind.REPOSE);
		repose.start(null, 1L);
		tick(helper, repose, 7);
		ChapelFocusBlockEntity reloaded = focus(helper, new BlockPos(3, 1, 1), ChapelRules.Kind.REPOSE);
		reloaded.loadWithComponents(saved(helper, repose), helper.getLevel().registryAccess());
		helper.assertTrue(saved(helper, reloaded).getInt("Timer") == 7,
				"a reload keeps the trial's timer: " + saved(helper, reloaded).getInt("Timer"));

		ChapelFocusBlockEntity litany = focus(helper, new BlockPos(1, 1, 3), ChapelRules.Kind.LITANY);
		long seed = 7L;
		litany.start(null, seed);
		Item wanted = ChapelFocusBlockEntity.LITANY_ITEMS[ChapelRules.litanyOrder(seed).get(0)];
		Item wrong = wanted == ChapelFocusBlockEntity.LITANY_ITEMS[0]
				? ChapelFocusBlockEntity.LITANY_ITEMS[1] : ChapelFocusBlockEntity.LITANY_ITEMS[0];
		litany.offer(new ItemStack(wrong), null);
		tick(helper, litany, 3);
		ChapelFocusBlockEntity cooling = focus(helper, new BlockPos(3, 1, 3), ChapelRules.Kind.LITANY);
		cooling.loadWithComponents(saved(helper, litany), helper.getLevel().registryAccess());
		int cooldown = saved(helper, cooling).getInt("Cooldown");
		helper.assertTrue(cooldown == ChapelRules.FAIL_COOLDOWN_TICKS - 3,
				"a reload keeps the fail cooldown: " + cooldown);

		// The cooldown speaks: a D8 challenger who tries again too soon is told so, and the trial does not begin.
		List<Component> told = new ArrayList<>();
		ServerPlayer challenger = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "chapel_cooldown")) {
			@Override
			public void displayClientMessage(Component message, boolean overlay) {
				told.add(message);
			}
		};
		HemoCapabilityAccess.getInitiatoryDegree(challenger).setDegreeNumber(8);
		helper.assertFalse(cooling.begin(challenger), "a Chapel cooling down from a failure began a trial");
		helper.assertTrue(told.stream().anyMatch(line -> line.getContents() instanceof TranslatableContents key
						&& key.getKey().equals("message.hemomancy.chapel.cooldown")),
				"the cooldown did not tell the challenger: " + told.stream().map(Component::getString).toList());
		helper.assertTrue(cooling.phase() == ChapelFocusBlockEntity.Phase.IDLE, "the cooling Chapel left its rest");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void aWaveMobInAnUnloadedChunkIsWaitedOnAndAMissingOneInALoadedChunkIsDead(GameTestHelper helper) {
		ChapelFocusBlockEntity far = focus(helper, new BlockPos(1, 1, 1), ChapelRules.Kind.DOWNFALL);
		far.loadWithComponents(downfall(helper, far, new ChunkPos(100_000, 100_000)), helper.getLevel().registryAccess());
		tick(helper, far, 20);
		helper.assertTrue(saved(helper, far).getInt("Step") == 1,
				"a wave mob whose chunk is not loaded holds the next wave back");

		ChapelFocusBlockEntity near = focus(helper, new BlockPos(5, 1, 5), ChapelRules.Kind.DOWNFALL);
		near.loadWithComponents(downfall(helper, near, new ChunkPos(near.getBlockPos())), helper.getLevel().registryAccess());
		tick(helper, near, 20);
		helper.assertTrue(saved(helper, near).getInt("Step") == 2,
				"a wave mob missing from a loaded chunk is dead, so the next wave rises");
		clearWaves(helper);
		helper.succeed();
	}

	/** A Downfall one wave in, its wave one mob that is nowhere to be found, last seen in {@code seen}. */
	private static CompoundTag downfall(GameTestHelper helper, ChapelFocusBlockEntity focus, ChunkPos seen) {
		CompoundTag tag = saved(helper, focus);
		tag.putString("Phase", ChapelFocusBlockEntity.Phase.ACTIVE.name());
		tag.putInt("Step", 1);
		CompoundTag mob = new CompoundTag();
		mob.putUUID("Id", UUID.randomUUID());
		mob.putLong("Chunk", seen.toLong());
		ListTag wave = new ListTag();
		wave.add(mob);
		tag.put("Wave", wave);
		return tag;
	}

	@GameTest(template = "empty")
	public static void chapelWaveMobsLeaveNoLootOrExperience(GameTestHelper helper) {
		ChapelFocusBlockEntity focus = focus(helper, new BlockPos(4, 1, 4), ChapelRules.Kind.DOWNFALL);
		focus.start(null, 1L);
		AABB box = new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16);
		List<Mob> wave = helper.getLevel().getEntitiesOfClass(Mob.class, box,
				mob -> mob.getTags().contains(ChapelWaveEvents.TAG));
		helper.assertTrue(wave.size() == ChapelRules.DOWNFALL_WAVE_SIZE, "the first wave rises tagged: " + wave.size());
		Mob control = EntityInit.hematic_construct.get().spawn(helper.getLevel(), helper.absolutePos(new BlockPos(4, 2, 16)),
				MobSpawnType.EVENT);
		// A plain monster despawns at once when the nearest player is over 128 blocks off, and in a full run that player
		// is another test's: the control then left no orb (FX-18 flake).
		control.setPersistenceRequired();
		Player killer = helper.makeMockPlayer(GameType.SURVIVAL);
		wave.get(0).hurt(helper.getLevel().damageSources().playerAttack(killer), 1000.0F);
		control.hurt(helper.getLevel().damageSources().playerAttack(killer), 1000.0F);
		helper.assertTrue(wave.get(0).isDeadOrDying() && control.isDeadOrDying(), "both blows were lethal");
		// Loot and experience fall in the dying tick, so they are counted now, before an orb can drift, merge or be
		// picked up by another test's player (the FX-18 flake: the control's orb went missing over a 30-tick wait).
		AABB waveSpot = wave.get(0).getBoundingBox().inflate(2);
		AABB controlSpot = control.getBoundingBox().inflate(2);
		boolean controlOrb = !helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, controlSpot).isEmpty();
		boolean waveOrb = !helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, waveSpot).isEmpty();
		boolean waveLoot = !helper.getLevel().getEntitiesOfClass(ItemEntity.class, waveSpot).isEmpty();
		clearWaves(helper);
		helper.getLevel().getEntitiesOfClass(ExperienceOrb.class, box).forEach(ExperienceOrb::discard);
		helper.getLevel().getEntitiesOfClass(ItemEntity.class, box).forEach(ItemEntity::discard);
		helper.assertTrue(controlOrb, "an ordinary construct killed by a player drops experience (control)");
		helper.assertFalse(waveOrb, "a Chapel wave mob drops no experience");
		helper.assertFalse(waveLoot, "a Chapel wave mob drops no loot");
		helper.succeed();
	}

	/** A Downfall focus told it is {@code saint}'s, as the generator writes it. */
	private static ChapelFocusBlockEntity downfallOf(GameTestHelper helper, BlockPos relative, String saint) {
		ChapelFocusBlockEntity focus = focus(helper, relative, ChapelRules.Kind.DOWNFALL);
		CompoundTag tag = saved(helper, focus);
		tag.putString("Saint", saint);
		focus.loadWithComponents(tag, helper.getLevel().registryAccess());
		return focus;
	}

	private static List<Mob> risen(GameTestHelper helper, ChapelFocusBlockEntity focus) {
		AABB around = new AABB(focus.getBlockPos()).inflate(3);
		return helper.getLevel().getEntitiesOfClass(Mob.class, around, mob -> mob.getTags().contains(ChapelWaveEvents.TAG));
	}

	/** NQ-08-23 A: Seraphae's Downfall raises her light fragments, which hunt without her and end only when struck. */
	@GameTest(template = "empty")
	public static void seraphaesDownfallRaisesHerLightFragments(GameTestHelper helper) {
		ChapelFocusBlockEntity focus = downfallOf(helper, new BlockPos(4, 2, 4), "SERAPHAE");
		focus.start(null, 1L);
		List<Mob> wave = risen(helper, focus);
		try {
			helper.assertTrue(wave.size() == ChapelRules.DOWNFALL_WAVE_SIZE
					&& wave.stream().allMatch(mob -> mob instanceof SeraphaeFragmentEntity fragment && fragment.isChapelWave()),
					"Seraphae's Downfall did not raise her fragments: " + wave);
			// A fragment of her fight with no Seraphae goes after 40 ticks; a Chapel's stays for its wave.
			for (int i = 0; i < 45; i++) wave.forEach(Mob::tick);
			helper.assertTrue(wave.stream().noneMatch(Mob::isRemoved), "a Chapel fragment left with no Seraphae to return to");
			Player striker = helper.makeMockPlayer(GameType.SURVIVAL);
			wave.get(0).hurt(helper.getLevel().damageSources().playerAttack(striker), 1.0F);
			helper.assertTrue(wave.get(0).isRemoved(), "a blow did not strike a Chapel fragment apart");
			helper.succeed();
		} finally {
			wave.forEach(Mob::discard);
		}
	}

	/** NQ-08-22 A: a Downfall's wave does not despawn in Peaceful, so the trial cannot finish itself. */
	@GameTest(template = "empty")
	public static void aPeacefulDownfallDoesNotFinishItself(GameTestHelper helper) {
		ChapelFocusBlockEntity hemorath = downfallOf(helper, new BlockPos(4, 2, 4), "HEMORATH");
		ChapelFocusBlockEntity seraphae = downfallOf(helper, new BlockPos(12, 2, 12), "SERAPHAE");
		hemorath.start(null, 1L);
		seraphae.start(null, 1L);
		List<Mob> wave = new ArrayList<>(risen(helper, hemorath));
		wave.addAll(risen(helper, seraphae));
		Difficulty before = helper.getLevel().getDifficulty();
		try {
			helper.getLevel().getServer().setDifficulty(Difficulty.PEACEFUL, true);
			wave.forEach(Mob::checkDespawn);
			tick(helper, hemorath, 20);
			tick(helper, seraphae, 20);
		} finally {
			helper.getLevel().getServer().setDifficulty(before, true);
		}
		try {
			helper.assertTrue(wave.size() == 2 * ChapelRules.DOWNFALL_WAVE_SIZE, "both Downfalls raised a wave: " + wave.size());
			helper.assertTrue(wave.stream().noneMatch(Mob::isRemoved), "a Chapel wave despawned in Peaceful");
			for (ChapelFocusBlockEntity focus : List.of(hemorath, seraphae)) {
				helper.assertTrue(!focus.complete() && saved(helper, focus).getInt("Step") == 1,
						"a Peaceful Downfall moved on without its wave falling");
			}
			helper.succeed();
		} finally {
			wave.forEach(Mob::discard);
		}
	}

	@GameTest(template = "empty")
	public static void aRecollectionCystCannotBeBrokenOrPushed(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
		var state = BlockInit.recollection_cyst.get().defaultBlockState();
		helper.assertTrue(state.getDestroySpeed(helper.getLevel(), pos) < 0, "a survival player cannot break a cyst");
		helper.assertTrue(state.getPistonPushReaction() == PushReaction.BLOCK, "a piston cannot push or pop a cyst");
		helper.succeed();
	}
}

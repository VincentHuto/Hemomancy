package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtPresences;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.court.HydraRisingHead;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.network.boss.BossScenePacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.MarimoHeartBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtResidentSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Brined Court Track 10, Tier 1: the presences. Marimo's moss and heart refuse breaking and blasts outside Creative and
 * are no mob; his first-sight scene plays once per player. Hydra's rising head can't be hurt by any source, is never a
 * target and is gone after its rise; the Head-tender calls it once per player, from the tallest crown near them. The
 * poles' set pieces refuse breaking only inside a Court site.
 */
@GameTestHolder("court_presence_validation")
@PrefixGameTestTemplate(false)
public final class CourtPresenceGameTests {
	private static final String EMPTY = "empty";
	private static final BlockPos STAND = new BlockPos(4, 2, 4);
	private static final BlockPos HEART = new BlockPos(8, 2, 8);
	private static final BlockPos TENDER = new BlockPos(12, 2, 12);
	private static final BlockPos CROWN = new BlockPos(18, 5, 12);

	private CourtPresenceGameTests() {
	}

	// --- Marimo -----------------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void marimoRefusesBreakingAndBlasts(GameTestHelper h) {
		h.setBlock(HEART, BlockInit.marimo_heart.get());
		h.setBlock(HEART.east(), BlockInit.marimo_moss.get());
		h.setBlock(HEART.west(), BlockInit.marimo_moss.get());
		try (CapturedPlayer captured = player(h)) {
			ServerPlayer player = captured.player;
			player.setGameMode(GameType.SURVIVAL);
			for (BlockPos rel : List.of(HEART, HEART.east())) {
				BlockState state = h.getBlockState(rel);
				h.assertTrue(state.getDestroySpeed(h.getLevel(), h.absolutePos(rel)) < 0.0F, "Marimo's moss is unbreakable");
				h.assertFalse(player.gameMode.destroyBlock(h.absolutePos(rel)), "a survival player can't break " + state);
				h.assertTrue(h.getBlockState(rel).is(state.getBlock()), state + " is still there after the attempt");
			}
			Vec3 blast = Vec3.atCenterOf(h.absolutePos(HEART.above()));
			h.getLevel().explode(null, blast.x, blast.y, blast.z, 4.0F, Level.ExplosionInteraction.TNT);
			h.assertBlockPresent(BlockInit.marimo_heart.get(), HEART);
			h.assertBlockPresent(BlockInit.marimo_moss.get(), HEART.east());
			h.assertBlockPresent(BlockInit.marimo_moss.get(), HEART.west());
			h.assertTrue(h.getBlockEntity(HEART) instanceof MarimoHeartBlockEntity, "the heart keeps its block entity");
			player.setGameMode(GameType.CREATIVE);
			h.assertTrue(player.gameMode.destroyBlock(h.absolutePos(HEART.west())), "Creative may take it away, for building");
			h.assertBlockPresent(Blocks.AIR, HEART.west());
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void marimoIsNoMobAndHisSceneIsSeenOnce(GameTestHelper h) {
		h.setBlock(HEART, BlockInit.marimo_heart.get());
		try (CapturedPlayer captured = player(h)) {
			ServerPlayer player = captured.player;
			h.assertFalse(CourtLife.hasSeenPresence(player, CourtPresences.MARIMO), "a new player hasn't seen Marimo");
			h.assertTrue(CourtPresences.marimoFirstSight(player, h.absolutePos(HEART)), "the first sight plays");
			h.assertFalse(CourtPresences.marimoFirstSight(player, h.absolutePos(HEART)), "and never twice");
			List<BossScenePacket> scenes = captured.sent(BossScenePacket.class);
			h.assertTrue(scenes.size() == 1 && scenes.get(0).scene().equals(BossPresentationIds.SCENE_MARIMO_PRESENCE),
					"one Marimo scene was sent: " + scenes);
			h.assertTrue(scenes.get(0).entityId() < 0, "Marimo's scene frames no entity: he is moss");
			h.assertTrue(CourtLife.hasSeenPresence(player, CourtPresences.MARIMO), "the sight is recorded");
			h.runAfterDelay(25, () -> {
				List<LivingEntity> mobs = h.getLevel().getEntitiesOfClass(LivingEntity.class,
						new AABB(h.absolutePos(HEART)).inflate(4.0D));
				h.assertTrue(mobs.isEmpty(), "the heart is no mob and spawns none: " + mobs);
				h.succeed();
			});
		}
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void polesSetPiecesRefuseBreakingOnlyInsideASite(GameTestHelper h) {
		for (BlockState state : List.of(BlockInit.petrified_follower.get().defaultBlockState(),
				BlockInit.hydra_crown.get().defaultBlockState(), BlockInit.kindled_tissue.get().defaultBlockState())) {
			h.assertTrue(CourtPresences.refusesBreak(state, true, false), state + " is refused inside a Court site");
			h.assertFalse(CourtPresences.refusesBreak(state, false, false), state + " is decor outside one");
			h.assertFalse(CourtPresences.refusesBreak(state, true, true), state + " gives way in Creative");
		}
		BlockState moss = BlockInit.marimo_moss.get().defaultBlockState();
		h.assertTrue(CourtPresences.refusesBreak(moss, false, false), "Marimo's moss is refused everywhere");
		h.assertFalse(CourtPresences.refusesBreak(Blocks.MOSS_BLOCK.defaultBlockState(), true, false),
				"ordinary moss in a site is not the pole");
		h.succeed();
	}

	// --- Hydra ------------------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void hydraHeadIsNeverHurtNorATarget(GameTestHelper h) {
		try (CapturedPlayer captured = player(h)) {
			ServerPlayer player = captured.player;
			ServerLevel level = h.getLevel();
			HydraRisingHead head = HydraRisingHead.rise(level, Vec3.atBottomCenterOf(h.absolutePos(CROWN)), player);
			var sources = level.damageSources();
			for (DamageSource source : List.of(sources.playerAttack(player), sources.magic(), sources.generic(),
					sources.explosion(null, null), sources.inFire(), sources.fellOutOfWorld(), sources.genericKill(),
					sources.thrown(player, player))) {
				h.assertFalse(head.hurt(source, 1000.0F), "the head takes no " + source.getMsgId());
			}
			h.assertTrue(head.isAlive(), "the head is still rising after every source");
			h.assertFalse(head.isPickable(), "no hitbox: nothing can pick it");
			h.assertFalse(head.isAttackable(), "nothing can attack it");
			h.assertTrue(head.skipAttackInteraction(player), "a swing at it does nothing");
			h.assertFalse((Object) head instanceof LivingEntity, "no living target selector can ever choose it");
			h.assertFalse(head.shouldBeSaved(), "it is never written to the world");
			Vec3 blast = head.position();
			level.explode(null, blast.x, blast.y, blast.z, 4.0F, Level.ExplosionInteraction.NONE);
			h.assertTrue(head.isAlive(), "an explosion leaves it");
			head.discard();
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = HydraRisingHead.LENGTH + 40)
	public static void hydraHeadRisesReachesAndWithdraws(GameTestHelper h) {
		Vec3 reach = new Vec3(4.0D, 3.0D, 0.0D);
		h.assertTrue(HydraRisingHead.pose(0, reach).crown().length() < 1.0E-6, "it starts in the chimney mouth");
		h.assertTrue(Math.abs(HydraRisingHead.pose(HydraRisingHead.RISE, reach).crown().y - HydraRisingHead.RISE_HEIGHT) < 1.0E-3,
				"it stands at the top of its rise");
		HydraRisingHead.Pose over = HydraRisingHead.pose(HydraRisingHead.RISE + HydraRisingHead.REACH, reach);
		h.assertTrue(over.crown().distanceTo(reach) < 1.0E-3 && over.turn() == 1.0F, "the crown hangs inverted over the player");
		h.assertTrue(HydraRisingHead.pose(HydraRisingHead.LENGTH, reach).crown().length() < 1.0E-6, "it goes back down the chimney");
		try (CapturedPlayer captured = player(h)) {
			HydraRisingHead head = HydraRisingHead.rise(h.getLevel(), Vec3.atBottomCenterOf(h.absolutePos(CROWN)), captured.player);
			h.runAfterDelay(HydraRisingHead.LENGTH + 5, () -> {
				h.assertTrue(head.isRemoved(), "the head is gone once it has withdrawn");
				h.succeed();
			});
		}
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void headTenderCallsTheTallestHeadOncePerPlayer(GameTestHelper h) {
		h.setBlock(CROWN, BlockInit.hydra_crown.get());
		h.setBlock(CROWN.offset(-3, -2, 2), BlockInit.hydra_crown.get());
		BlockPos at = h.absolutePos(TENDER);
		h.assertTrue(CourtResidentSpawner.place(h.getLevel(), CourtRole.HEAD_TENDER, at, Direction.SOUTH.toYRot(), "threshold"),
				"the Head-tender has a member type");
		CourtMemberEntity tender = h.getLevel().getEntitiesOfClass(CourtMemberEntity.class, new AABB(at).inflate(1.5D),
				member -> member.role() == CourtRole.HEAD_TENDER).get(0);
		tender.setNoAi(true);
		try (CapturedPlayer captured = player(h)) {
			ServerPlayer player = captured.player;
			player.moveTo(at.getX() + 3.5D, at.getY(), at.getZ() + 0.5D);
			player.tickCount = 20;
			CourtPresences.onPlayerTick(new PlayerTickEvent.Post(player));
			List<HydraRisingHead> heads = heads(h);
			h.assertTrue(heads.size() == 1, "one head rose for the player: " + heads.size());
			h.assertTrue(heads.get(0).blockPosition().equals(h.absolutePos(CROWN)), "from the tallest crown");
			h.assertTrue(CourtLife.hasSeenPresence(player, CourtPresences.HYDRA), "the sight is recorded");
			h.assertTrue(captured.sent(BossScenePacket.class).stream()
					.anyMatch(packet -> packet.scene().equals(BossPresentationIds.SCENE_HYDRA_RISE)
							&& packet.entityId() == heads.get(0).getId()), "Hydra's scene frames the rising head");
			h.assertFalse(captured.chat.isEmpty(), "the Head-tender speaks as it calls");
			player.tickCount = 40;
			CourtPresences.onPlayerTick(new PlayerTickEvent.Post(player));
			h.assertTrue(heads(h).size() == 1, "and it is called once per player, never again");
			heads(h).forEach(HydraRisingHead::discard);
			tender.discard();
		}
		h.succeed();
	}

	private static List<HydraRisingHead> heads(GameTestHelper h) {
		return h.getLevel().getEntitiesOfClass(HydraRisingHead.class, new AABB(h.absolutePos(CROWN)).inflate(1.5D));
	}

	private static CapturedPlayer player(GameTestHelper h) {
		CapturedPlayer captured = CapturedPlayer.create(h).online();
		BlockPos stand = h.absolutePos(STAND);
		captured.player.moveTo(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D);
		return captured;
	}
}

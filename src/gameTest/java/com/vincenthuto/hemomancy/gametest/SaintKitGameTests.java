package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDamageTypes;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintEnding;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintKit;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLocomotion;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMeter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPhaseTable;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPresence;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.network.boss.SaintMeterPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * The Saint combat kit with real entities (Kit API section 22): the arena puts an edited cell back on {@code endTrial},
 * on removal, after a reload and on the leash reset; owned entities and hazards go with it; the channel router passes
 * when the Saint declines the channel and a Saint's body refuses the ordinary drain; the meter payload reaches
 * a challenger and {@code CLEAR} follows their leaving. A kit Saint here is a bare {@link SaintTrialEntity} with a
 * one-move test kit; with no tomb its arena is the fallback box round it.
 */
@GameTestHolder("saint_kit_validation")
@PrefixGameTestTemplate(false)
public final class SaintKitGameTests {
	private static final String TEMPLATE = "empty";
	private static final BlockPos SAINT_AT = new BlockPos(4, 2, 4);
	private static final BlockPos CELL = new BlockPos(6, 2, 4);

	private SaintKitGameTests() {
	}

	/** The one move of the test kit: never picked, never strikes. */
	enum TestMove implements SaintMove {
		IDLE;

		@Override
		public int id() {
			return 1;
		}

		@Override
		public String key() {
			return "idle";
		}

		@Override
		public Kind kind() {
			return Kind.MELEE;
		}

		@Override
		public Aim aim() {
			return Aim.TRACK;
		}

		@Override
		public boolean inPhase(int phase) {
			return true;
		}

		@Override
		public BossMoveTimeline timeline(SaintMoveInputs in) {
			return new BossMoveTimeline(12, 1, 1);
		}

		@Override
		public BossFootprint footprint(SaintMoveInputs in) {
			return BossFootprint.None.INSTANCE;
		}

		@Override
		public int weight(SaintPickContext ctx) {
			return 0;
		}

		@Override
		public int cooldown(SaintPickContext ctx) {
			return 0;
		}
	}

	/** A Saint on a minimal kit, in Hemorath's body type. */
	static final class KitSaint extends SaintTrialEntity {
		/** Its own damage-taken factor, for the pipeline case. */
		float factor = 1.0F;

		KitSaint(Level level) {
			super(EntityInit.hemorath.get(), level, 0, new BossBarBinding("entity.hemomancy.hemorath",
					BossPresentationIds.FRAME_HEMORATH, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS));
		}

		@Override
		public EnumSaintType saintType() {
			return EnumSaintType.HEMORATH;
		}

		@Override
		protected SaintKit createKit() {
			return new SaintKit(List.of(TestMove.IDLE), SaintLocomotion.STILL,
					new SaintMeter(SaintMeter.Owner.SAINT, SaintMeter.fixed(100.0F)), SaintPresence.NONE,
					SaintPhaseTable.single("", BossEvent.BossBarColor.RED), SaintDamageTypes.HEMORATH,
					SaintKit.CHALLENGER_SCALING, SaintEnding.Spec.IMMEDIATE);
		}

		void endNow() {
			beginEnding(SaintEnding.Spec.IMMEDIATE, null, () -> true);
		}

		@Override
		protected float damageTakenFactor(com.vincenthuto.hemomancy.common.entity.boss.saint.SaintHit hit) {
			return factor;
		}
	}

	private static KitSaint kitSaint(GameTestHelper h) {
		clearStage(h);
		KitSaint saint = new KitSaint(h.getLevel());
		saint.setNoGravity(true);
		saint.moveTo(h.absoluteVec(Vec3.atBottomCenterOf(SAINT_AT)));
		h.getLevel().addFreshEntity(saint);
		return saint;
	}

	private static void clearStage(GameTestHelper h) {
		for (BlockPos pos : BlockPos.betweenClosed(h.absolutePos(new BlockPos(1, 1, 1)), h.absolutePos(new BlockPos(12, 6, 12)))) {
			h.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	/** Ticks the Saint's own server step until it is removed, at most {@code limit} times. */
	private static void tickUntilRemoved(SaintTrialEntity saint, int limit) {
		for (int i = 0; i < limit && !saint.isRemoved(); i++) saint.aiStep();
	}

	private static void sweepItems(GameTestHelper h) {
		AABB stage = new AABB(h.absolutePos(BlockPos.ZERO)).inflate(24.0D);
		h.getLevel().getEntitiesOfClass(ItemEntity.class, stage).forEach(Entity::discard);
	}

	// ── the arena's restore contract ─────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 40)
	public static void endTrialPutsTheEditedCellBackAndExpiresTheHazards(GameTestHelper h) {
		KitSaint saint = kitSaint(h);
		saint.aiStep();
		SaintArena arena = saint.arena();
		h.assertTrue(arena != null && !arena.chamberFound(), "a kit Saint with no tomb resolves the fallback arena");
		BlockPos cell = h.absolutePos(CELL);
		BlockState original = h.getLevel().getBlockState(cell);
		h.assertTrue(arena.set(cell, Blocks.POLISHED_DEEPSLATE.defaultBlockState()) && arena.changed(cell),
				"the arena records the edit");
		BossGroundHazardEntity hazard = hazard(h, saint);
		saint.endNow();
		h.assertTrue(saint.ending() && hazard.fading(), "the ending expires the Saint's hazards");
		tickUntilRemoved(saint, 10);
		h.assertTrue(saint.isRemoved() && saint.hasTrialEnded(), "the immediate ending ran endTrial and discarded the Saint");
		h.assertTrue(h.getLevel().getBlockState(cell).equals(original), "endTrial put the edited cell back");
		h.assertTrue(hazard.isRemoved(), "the owned hazard went with the trial");
		sweepItems(h);
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 40)
	public static void removalPutsTheCellBackAndDiscardsOwnedEntities(GameTestHelper h) {
		KitSaint saint = kitSaint(h);
		saint.aiStep();
		BlockPos cell = h.absolutePos(CELL);
		BlockState original = h.getLevel().getBlockState(cell);
		saint.arena().set(cell, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
		BossGroundHazardEntity hazard = hazard(h, saint);
		Pig pig = EntityType.PIG.create(h.getLevel());
		pig.moveTo(h.absoluteVec(new Vec3(7.5D, 2.0D, 7.5D)));
		h.getLevel().addFreshEntity(pig);
		saint.arena().own(pig, null);
		saint.discard();
		h.assertTrue(h.getLevel().getBlockState(cell).equals(original), "removal put the edited cell back");
		h.assertTrue(hazard.isRemoved() && pig.isRemoved(), "owned entities and hazards go with the Saint");
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 40)
	public static void aReloadedSaintRestoresItsChamberOnItsFirstTick(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		KitSaint saint = kitSaint(h);
		saint.aiStep();
		BlockPos cell = h.absolutePos(CELL);
		BlockState original = level.getBlockState(cell);
		saint.arena().set(cell, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
		CompoundTag saved = new CompoundTag();
		saint.saveWithoutId(saved);
		saint.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
		h.assertTrue(level.getBlockState(cell).is(Blocks.POLISHED_DEEPSLATE),
				"nothing is written while the Saint unloads with its chamber");
		KitSaint reloaded = new KitSaint(level);
		reloaded.load(saved);
		reloaded.setNoGravity(true);
		level.addFreshEntity(reloaded);
		reloaded.aiStep();
		h.assertTrue(level.getBlockState(cell).equals(original), "the saved record restored on the first tick after load");
		h.assertTrue(!reloaded.isRemoved() && reloaded.arena() != null && reloaded.arena().clean(),
				"a Saint loaded mid-fight carries on with a clean chamber");
		reloaded.discard();
		h.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 40, batch = "saint_kit_leash")
	public static void anEmptyChamberResetsTheTrialUnrewarded(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		KitSaint saint = kitSaint(h);
		BlockPos tomb = h.absolutePos(new BlockPos(9, 2, 9));
		BlockState sarcophagus = BlockInit.saint_sarcophagus.get().defaultBlockState()
				.setValue(SaintSarcophagusBlock.FACING, Direction.WEST);
		saint.bindTomb(tomb, sarcophagus);
		saint.aiStep();
		h.assertTrue(saint.arena() != null && saint.arena().chamberFound(), "a bound tomb resolves the chamber");
		BlockPos cell = h.absolutePos(CELL);
		BlockState original = level.getBlockState(cell);
		saint.arena().set(cell, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
		tickUntilRemoved(saint, SaintArena.EMPTY_RESET_TICKS + 5);
		h.assertTrue(saint.isRemoved(), "600 ticks with no challenger in the chamber reset the trial");
		h.assertTrue(level.getBlockState(cell).equals(original), "the leash reset put the edited cell back");
		h.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(tomb).inflate(16.0D)).isEmpty(),
				"the reset dropped no loot");
		h.assertTrue(level.getBlockState(tomb).is(BlockInit.saint_sarcophagus.get()), "the Saint went back into its tomb");
		for (BlockPos pos : BlockPos.betweenClosed(tomb.offset(-3, 0, -3), tomb.offset(3, 3, 3))) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
		}
		h.succeed();
	}

	// ── the router and the drain ─────────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 40)
	public static void theRouterPassesWhenTheSaintDeclinesTheChannel(GameTestHelper h) {
		KitSaint saint = kitSaint(h);
		ServerPlayer player = DuctilisGameTests.player(h);
		player.moveTo(saint.position().add(2.0D, 0.0D, 0.0D));
		player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, saint.getEyePosition());
		try {
			for (int i = 0; i < 3; i++) saint.aiStep();
			h.assertTrue(saint.arena() != null, "a kit Saint resolves its arena on its first tick");
			SaintChannelRouter.Result absorb = SaintChannelRouter.tryAbsorb(h.getLevel(), player, 6.0D, 5.0D, false);
			SaintChannelRouter.Result project = SaintChannelRouter.tryProject(h.getLevel(), player, 6.0D, 5.0D, false);
			h.assertTrue(!absorb.handled() && !project.handled() && absorb.blood() == 0.0D && project.blood() == 0.0D,
					"the router passes when the Saint declines, so the item's own paths run as before");
			h.succeed();
		} finally {
			saint.discard();
			player.discard();
		}
	}

	/**
	 * The damage pipeline's factor reaches health: NeoForge spends the damage container's amount, not the one passed to
	 * {@code actuallyHurt}, so a factor applied only to the argument changed nothing (found 2026-11-06).
	 */
	@GameTest(template = TEMPLATE, timeoutTicks = 40)
	public static void theDamageFactorReachesHealth(GameTestHelper h) {
		KitSaint saint = kitSaint(h);
		try {
			saint.factor = 0.5F;
			float before = saint.getHealth();
			saint.hurt(h.getLevel().damageSources().magic(), 8.0F);
			h.assertTrue(Math.abs(before - saint.getHealth() - 4.0F) < 0.01F, "a factor of a half takes 4 of 8: "
					+ (before - saint.getHealth()));
			saint.factor = 0.0F;
			saint.invulnerableTime = 0;
			float held = saint.getHealth();
			saint.hurt(h.getLevel().damageSources().magic(), 8.0F);
			h.assertTrue(saint.getHealth() == held, "a factor of 0 takes nothing: " + (held - saint.getHealth()));
			h.succeed();
		} finally {
			saint.discard();
		}
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 40)
	public static void aSaintsBodyRefusesTheOrdinaryDrain(GameTestHelper h) {
		clearStage(h);
		SaintTrialEntity velorum = EntityInit.velorum.get().create(h.getLevel());
		velorum.setNoAi(true);
		velorum.moveTo(h.absoluteVec(Vec3.atBottomCenterOf(SAINT_AT)));
		h.getLevel().addFreshEntity(velorum);
		KitSaint kit = new KitSaint(h.getLevel());
		kit.setNoGravity(true);
		kit.moveTo(h.absoluteVec(new Vec3(8.5D, 2.0D, 8.5D)));
		h.getLevel().addFreshEntity(kit);
		Pig pig = EntityType.PIG.create(h.getLevel());
		pig.moveTo(h.absoluteVec(new Vec3(2.5D, 2.0D, 8.5D)));
		h.getLevel().addFreshEntity(pig);
		ServerPlayer player = DuctilisGameTests.player(h);
		try {
			h.assertTrue(!BloodAbsorptionItem.isValidAbsorptionTarget(player, velorum), "Velorum's body refuses the drain");
			h.assertTrue(!BloodAbsorptionItem.isValidAbsorptionTarget(player, kit), "a kit Saint refuses the drain");
			h.assertTrue(BloodAbsorptionItem.isValidAbsorptionTarget(player, pig), "an ordinary mob is still drained");
			h.succeed();
		} finally {
			velorum.discard();
			kit.discard();
			pig.discard();
			player.discard();
		}
	}

	// ── the meter payload ────────────────────────────────────────────

	@GameTest(template = TEMPLATE, timeoutTicks = 80)
	public static void theMeterReachesAChallengerAndClearFollowsTheirLeaving(GameTestHelper h) {
		List<SaintMeterPacket> received = new ArrayList<>();
		ServerPlayer player = DuctilisGameTests.player(h, packet -> {
			if (packet instanceof ClientboundCustomPayloadPacket custom && custom.payload() instanceof SaintMeterPacket meter) {
				received.add(meter);
			}
		});
		KitSaint saint = kitSaint(h);
		player.moveTo(saint.position().add(2.0D, 0.0D, 0.0D));
		h.startSequence()
				.thenIdle(6)
				.thenExecute(() -> {
					h.assertTrue(saint.arena() != null && saint.arena().challengers().contains(player),
							"the player stands in the chamber");
					h.assertTrue(received.stream().anyMatch(p -> !p.clears() && p.saintId() == saint.getId()),
							"the challenger was sent the meter on joining: " + received);
					h.assertTrue(saint.meterSync().sentTo(player.getUUID()), "the sync remembers the challenger");
					player.moveTo(saint.position().add(60.0D, 0.0D, 0.0D));
				})
				.thenIdle(4)
				.thenExecute(() -> {
					h.assertTrue(!received.isEmpty() && received.get(received.size() - 1).clears()
							&& received.get(received.size() - 1).saintId() == saint.getId(),
							"CLEAR followed the challenger's leaving: " + received);
					h.assertTrue(!saint.meterSync().sentTo(player.getUUID()), "the sync forgot the challenger");
					player.moveTo(saint.position().add(2.0D, 0.0D, 0.0D));
				})
				.thenIdle(4)
				.thenExecute(() -> {
					h.assertTrue(saint.meterSync().sentTo(player.getUUID()), "the challenger rejoined the roster");
					received.clear();
					saint.discard();
					h.assertTrue(received.stream().anyMatch(SaintMeterPacket::clears), "removal clears every challenger");
					player.discard();
				})
				.thenSucceed();
	}

	private static BossGroundHazardEntity hazard(GameTestHelper h, SaintTrialEntity saint) {
		Vec3 at = h.absoluteVec(new Vec3(7.5D, 2.0D, 4.5D));
		BossGroundHazardEntity hazard = BossGroundHazardEntity.spawn(h.getLevel(), saint, 1,
				new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, 1.5D, BossFootprint.Height.STANDING),
				FootprintFrame.simple(at, at.add(0.0D, 0.0D, 1.0D), at, at.y), 400, 10);
		saint.arena().own(hazard, null);
		return hazard;
	}
}

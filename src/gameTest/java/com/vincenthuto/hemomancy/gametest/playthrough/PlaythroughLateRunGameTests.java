package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The Harbinger path's second stage on one player: from a fresh Degree 5 (where {@code harbingerD0ToD5} ends) through
 * the bloodline and the Fane, Degree 6 and the Living Covenant, Degree 7 and Communion, the revelation choice, and
 * into Degree 8 on each road. Each band's own chain test starts from a {@link PlaythroughStates} fixture; here the
 * player who leaves one band is the one who starts the next, so drift between what a band leaves and what the next
 * one needs shows up. At every boundary the player is compared with the band's checkpoint
 * ({@link PlaythroughStates#assertAt}), their pack and blood are logged, the outpost teachers are talked to (every line
 * has English, no dead end), and the floor is cleared for the next band, keeping what a player keeps in the world:
 * their Fane's heart and their pledged recruit.
 *
 * <p>ARRANGE lines are labelled: blood top-ups, tools, the outpost residents, the pome ripening and the steps the
 * GameTest server cannot host (the Chamber of Will return, the Vesper ordeal, Gardens travel), each logged as proof
 * owed. The Bloom of the Qliphoth is level-global, so each road runs in a batch of its own. Required = false until
 * the run has been stable for a few runs.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughLateRunGameTests {
	private static final String EMPTY = "empty";
	/** The Apotheos road. The Silentium road has its own batch: two Blooms 37 blocks apart would refuse each other. */
	private static final String BATCH = "playthrough_late_run";
	private static final String SILENT_BATCH = "playthrough_late_run_silentium";
	private static final int LATE_TICKS = 72000;

	/** The grand floor of the Rite of Apotheos (the Endgame band's origin), and where the four teachers stand after. */
	private static final BlockPos GRAND_ORIGIN = new BlockPos(16, 3, 12);
	private static final String WARD_REFUSED = "message.hemomancy.mausoleum_ward.refused";

	private static final PlaythroughNpcs.Role[] WING = {PlaythroughNpcs.Role.VICAR, PlaythroughNpcs.Role.ALCHEMIST,
			PlaythroughNpcs.Role.MNEMONIST};

	private PlaythroughLateRunGameTests() {
	}

	/** D5 to D8 Apotheos: the line, the Fane, D6, the Living Covenant, D7, Communion, the choice, the Rite of Apotheos. */
	@GameTest(template = EMPTY, timeoutTicks = LATE_TICKS, batch = BATCH, required = false)
	public static void harbingerD5ToApotheos(GameTestHelper helper) {
		PlaythroughPlayer p = start(helper);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		toTheChoice(helper, p, clock);
		clock.segment("D7 to D8 Apotheos", c -> PlaythroughEndgameGameTests.apotheosChain(helper, p, c,
						helper.absolutePos(GRAND_ORIGIN), PlaythroughSites.stand(helper, 26, 20)))
				.then(() -> finish(helper, p, PlaythroughStates.Checkpoint.APOTHEOS_D8))
				.run();
	}

	/**
	 * D5 to D8 Silentium: as the Apotheos road to the choice, then silence chosen, the Severance cut, the Vesper
	 * victory (ARRANGE: the ordeal is in the Chamber of Will), the Memory delivered and drawn in, and a Saint
	 * mausoleum's ward crossed.
	 */
	@GameTest(template = EMPTY, timeoutTicks = LATE_TICKS, batch = SILENT_BATCH, required = false)
	public static void harbingerD5ToSilentium(GameTestHelper helper) {
		PlaythroughPlayer p = start(helper);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		toTheChoice(helper, p, clock);
		// Where the Archon chain grew the Bloom: its floor's Focus one above that origin, the root two above the Focus.
		BlockPos bloomRoot = PlaythroughSites.farOrigin(helper, 48).offset(8, -5, 8).above(3);
		clock.segment("D7 to D8 Silentium", c -> PlaythroughEndgameGameTests.silentiumChain(helper, p, c, bloomRoot,
						PlaythroughSites.stand(helper, 24, 18)))
				.then(() -> {
					finish(helper, p, PlaythroughStates.Checkpoint.SILENTIUM_D8);
					talkRound(helper, p, "D8 Silentium");
				})
				.segment("a Saint mausoleum's ward", c -> crossWard(helper, p, c))
				.run();
	}

	// =================================================================================================================
	// The shared road to the revelation choice
	// =================================================================================================================

	/** ARRANGE: where the D0-D5 run ends, with a rested pool and the Staff and Projection carried. */
	private static PlaythroughPlayer start(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 6));
		PlaythroughStates.illuminatusD5(p); // ARRANGE: the D0-D5 run's end state
		for (Item tool : List.of(ItemInit.living_staff.get(), ItemInit.blood_projection.get())) {
			if (p.count(tool) == 0) p.give(new ItemStack(tool)); // ARRANGE: the D0-D5 run carries both
		}
		PlaythroughStates.blood(p, 1.0E7D); // ARRANGE: a rested pool
		// ARRANGE: a netherite sword for the rite ordeals' puppeteers (the harness's attack applies its damage).
		if (p.count(Items.NETHERITE_SWORD) == 0) p.give(new ItemStack(Items.NETHERITE_SWORD));
		return p;
	}

	/**
	 * The steps from D5 to the revelation choice, each band's chain in its own segment with a boundary between:
	 * the line and the Fane (D5), the Bloodline Covenant (D6), the Living Covenant, the Rite of the Hematic Order (D7),
	 * Communion and the Spine's forced return.
	 */
	private static void toTheChoice(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock) {
		Entity[] recruit = new Entity[1];
		clock.then(() -> talkRound(helper, p, "D5 start"))
				.segment("D5 line and Fane", c -> PlaythroughIlluminatusGameTests.chain(helper, p, c))
				.then(() -> {
					recruit[0] = recruit(helper, p);
					boundary(helper, p, PlaythroughStates.Checkpoint.COVENANT_D5);
				})
				.segment("D5 to D6", c -> PlaythroughSanctifiedGameTests.chain(helper, p, c))
				.then(() -> boundary(helper, p, PlaythroughStates.Checkpoint.SANCTIFIED_D6))
				.segment("the Living Covenant", c -> PlaythroughSanctifiedGameTests.livingCovenantChain(helper, p, c,
						recruit[0], () -> clear(helper, p)))
				.then(() -> boundary(helper, p, PlaythroughStates.Checkpoint.LIVING_COVENANT_D6))
				.segment("D6 to the revelation", c -> PlaythroughArchonGameTests.chain(helper, p, c, recruit[0]))
				.then(() -> boundary(helper, p, PlaythroughStates.Checkpoint.CHOICE_PENDING_D7));
	}

	/** The line's pledged recruit, summoned into the Fane by the D5 chain: the one Vicar it pledged. */
	private static Entity recruit(GameTestHelper helper, PlaythroughPlayer p) {
		Bloodline line = PlaythroughStates.lineOf(p);
		helper.assertTrue(line != null, "the D5 chain leaves a line");
		List<Entity> pledged = helper.getLevel().getEntities((Entity) null, helper.getBounds().inflate(16.0D),
				entity -> line.hasNpcMember(entity.getUUID()));
		helper.assertTrue(pledged.size() == 1, "the line has one pledged recruit in the Fane, found " + pledged.size());
		PlaythroughLog.note(helper, "late run", "the pledged recruit is " + PlaythroughPlayer.describe(pledged.get(0)));
		return pledged.get(0);
	}

	// =================================================================================================================
	// Boundaries
	// =================================================================================================================

	/** Between bands: the checkpoint, the pack and pool, the teachers talked to, then the floor cleared. */
	private static void boundary(GameTestHelper helper, PlaythroughPlayer p, PlaythroughStates.Checkpoint checkpoint) {
		PlaythroughStates.assertAt(helper, p, checkpoint);
		PlaythroughLog.note(helper, "late run", checkpoint + " reached; blood " + Math.round(p.blood()) + " mL; carried "
				+ carried(p));
		talkRound(helper, p, checkpoint.toString());
		clear(helper, p);
	}

	/** The run's end: the checkpoint and what the player carries. */
	private static void finish(GameTestHelper helper, PlaythroughPlayer p, PlaythroughStates.Checkpoint checkpoint) {
		PlaythroughStates.assertAt(helper, p, checkpoint);
		PlaythroughLog.note(helper, "late run", "ended at " + checkpoint + "; blood " + Math.round(p.blood()) + " mL; carried "
				+ carried(p));
	}

	/**
	 * ARRANGE: an outpost wing (Vicar, Alchemist, Mnemonist) laid for a moment; the player talks to each as a player
	 * would and every line shown has English and no option leads nowhere. The lines and offers are logged, then the
	 * residents go.
	 */
	private static void talkRound(GameTestHelper helper, PlaythroughPlayer p, String when) {
		PlaythroughSites.OutpostWing wing = PlaythroughSites.outpostWing(helper, PlaythroughSites.stand(helper, 4, 4), WING);
		Vec3 back = p.player().position();
		try {
			for (PlaythroughNpcs.Role role : WING) {
				Entity npc = wing.get(role);
				p.approach(npc);
				String label = "the " + role + " at " + when;
				DialogueView view = p.talkTo(npc);
				PlaythroughLog.note(helper, "late run", label + ": lines " + view.lineKeys() + " offers " + view.shownEvents());
				view.assertLangKeys(helper, label).assertNoDeadEnds(helper, label);
			}
		} finally {
			for (PlaythroughNpcs.Role role : WING) wing.get(role).discard();
			p.moveTo(back);
		}
	}

	/** ARRANGE: the template floor cleared for the next band, keeping the player's Fane heart and pledged recruits. */
	private static void clear(GameTestHelper helper, PlaythroughPlayer p) {
		ServerLevel level = helper.getLevel();
		Set<BlockPos> keepBlocks = new HashSet<>();
		BlockPos heart = FoundingFaneSavedData.get(level).getHeart(p.uuid());
		if (heart != null) keepBlocks.add(heart.immutable());
		Set<UUID> keepEntities = new HashSet<>();
		Bloodline line = PlaythroughStates.lineOf(p);
		if (line != null) {
			for (Entity entity : level.getEntities((Entity) null, helper.getBounds().inflate(8.0D),
					entity -> line.hasNpcMember(entity.getUUID()))) {
				keepEntities.add(entity.getUUID());
			}
		}
		PlaythroughSites.clearForNextBand(helper, keepBlocks, keepEntities);
	}

	private static List<String> carried(PlaythroughPlayer p) {
		List<String> out = new ArrayList<>();
		for (ItemStack stack : p.player().getInventory().items) {
			if (!stack.isEmpty()) out.add(stack.getCount() + "x " + stack.getItem());
		}
		return out;
	}

	// =================================================================================================================
	// After Silentium: a Saint mausoleum's ward
	// =================================================================================================================

	/**
	 * Hemorath's complex (ARRANGE: assembled far off, as worldgen would), its ward anchor left a few ticks to settle,
	 * then the D8 Silentium steps onto the threshold line and is not turned back.
	 */
	private static void crossWard(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock) {
		PlaythroughSites.Mausoleum m = PlaythroughSites.mausoleum(helper, EnumSaintType.HEMORATH, 11L);
		clock.idle(3).then(() -> {
			List<WardAnchorBlockEntity> anchors = wardAnchors(helper.getLevel(), m);
			helper.assertTrue(!anchors.isEmpty() && anchors.get(0).settled() && anchors.get(0).threshold() != null,
					"the threshold hall's ward anchor is settled: " + anchors.size() + " anchors");
			AABB slab = anchors.get(0).threshold();
			Vec3 onLine = new Vec3(slab.getCenter().x, slab.minY, slab.getCenter().z);
			p.moveTo(onLine); // ARRANGE: walked to the mausoleum
			int mark = p.mark();
			p.tickOnce();
			List<String> said = new ArrayList<>();
			for (Outcome.ChatLine line : p.chatSince(mark)) said.addAll(line.keys());
			PlaythroughLog.note(helper, "late run", "the Silentium at the ward line: said " + said + ", at "
					+ p.player().position());
			helper.assertFalse(said.contains(WARD_REFUSED), "the ward does not refuse a D8 Silentium; said " + said);
			helper.assertTrue(p.player().position().distanceTo(onLine) < 0.01D, "the D8 is not moved back from the line");
		});
	}

	private static List<WardAnchorBlockEntity> wardAnchors(ServerLevel level, PlaythroughSites.Mausoleum m) {
		List<WardAnchorBlockEntity> found = new ArrayList<>();
		var box = m.box();
		ChunkPos min = new ChunkPos(SectionPos.blockToSectionCoord(box.minX()), SectionPos.blockToSectionCoord(box.minZ()));
		ChunkPos max = new ChunkPos(SectionPos.blockToSectionCoord(box.maxX()), SectionPos.blockToSectionCoord(box.maxZ()));
		ChunkPos.rangeClosed(min, max).forEach(chunk -> {
			for (BlockEntity entity : List.copyOf(level.getChunk(chunk.x, chunk.z).getBlockEntities().values())) {
				if (entity instanceof WardAnchorBlockEntity anchor && box.isInside(entity.getBlockPos())) found.add(anchor);
			}
		});
		return found;
	}
}

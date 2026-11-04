package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.DegreeProgression;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.VeinMasonScarLesson;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.alchemist.ConcentratedBlood;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.rite.TempleOathRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusDiscoveryProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtDiscoveryProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffBondHelper;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.encounter.VesperOrdealManager;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.UnsignedLedgerItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterProgression;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import java.util.ArrayList;
import java.util.List;
import com.vincenthuto.hemomancy.gametest.PlaythroughFingerprints;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * ARRANGE only: the documented end state of each band, so a band's test can start there without replaying the game.
 * Each state builds on the one before it (calling it when the player is below it) and says exactly what it sets. It
 * goes through {@link DegreeProgression#advance} and the real grant and record helpers wherever they exist, never
 * {@code setDegreeNumber}. The band classes' {@code *ChainFromPreviousBand} tests replay the real steps instead, to
 * catch drift between a state here and play.
 */
public final class PlaythroughStates {
	private PlaythroughStates() {
	}

	/**
	 * Degree 0, blood inactive, the vascular slot empty, no Hemomancy advancements: the state
	 * {@link PlaythroughPlayer#create} leaves. Asserts it, so a test that reuses a player learns it is not fresh.
	 */
	public static void freshD0(PlaythroughPlayer player) {
		GameTestHelper helper = player.helper();
		ServerPlayer server = player.player();
		HemoCapabilityAccess.getEquipment(server).setStackInSlot(5, ItemStack.EMPTY);
		var blood = HemoCapabilityAccess.getBloodVolume(server);
		if (blood.isActive()) {
			blood.setActive(false);
			BloodVolumeEvents.syncVolume(server, blood);
		}
		helper.assertTrue(player.degree() == 0, "a fresh player is degree 0, not " + player.degree());
		helper.assertTrue(PlaythroughFingerprints.earned(server).isEmpty(),
				"a fresh player has no Hemomancy advancements: " + PlaythroughFingerprints.earned(server));
	}

	/**
	 * The D0 claim, done by fixture: the Hermit met (the right-click's {@code markMetHermit}), his blessing, the charm in the vascular slot, blood woken
	 * ({@code EarlyInitiation.activate}, which earns {@code the_first_awakening}), the heart claimed from
	 * {@code hermit} in both the root and the {@code PlayerPersisted} homes ({@code TempleOathRules.recordHeartClaim}),
	 * and the covenant waybill (the temple's lodestone) given. A null {@code hermit} stands for a temple whose Hermit
	 * is not in the test.
	 */
	public static void charmBearerD0(PlaythroughPlayer player, @Nullable Entity hermit) {
		ServerPlayer server = player.player();
		freshD0(player);
		UUID hermitId = hermit == null ? UUID.randomUUID() : hermit.getUUID();
		// A real charm-bearer has right-clicked a Hermit, which is what records the meeting (F-6).
		DialogueHubFactory.markMetVicar(server);
		TempleOathRules.bless(server, hermitId);
		HemoCapabilityAccess.getEquipment(server).setStackInSlot(5, new ItemStack(ItemInit.charm_of_vascularium.get()));
		TempleOathRules.recordHeartClaim(server, hermitId);
		EarlyInitiation.activate(server);
		EarlyInitiation.give(server, new ItemStack(ItemInit.harbinger_lodestone.get()));
	}

	/** {@link #charmBearerD0} with the temple's Display marked claimed by the player, as the touch marks it. */
	public static void charmBearerD0(PlaythroughPlayer player, PlaythroughSites.BloodTemple temple) {
		charmBearerD0(player, temple.hermit());
		temple.displayEntity(player.player().serverLevel()).claim(player.uuid());
	}

	/**
	 * The end of the Vicar's initiation: degree 1 through {@code DegreeProgression.advance} (the degree advancement,
	 * the Liber entries and the two D1 utilities come with it), the sanguine conduit, four starter flasks with
	 * {@code initiation_blood_claimed}, the assignment ledger, and {@code hermit_road_ledger_granted}. Runs
	 * {@link #charmBearerD0} first when the player has no charm.
	 */
	public static void neophyteD1(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		if (!player.hasCharm()) charmBearerD0(player, (Entity) null);
		DegreeProgression.advance(server, 1);
		EarlyInitiation.give(server, new ItemStack(ItemInit.sanguine_conduit.get()));
		grant(server, Hemomancy.rloc("hemomancy/initiation_blood_claimed"));
		EarlyInitiation.give(server, new ItemStack(ItemInit.bloody_flask.get(), 4));
		EarlyInitiation.give(server, new ItemStack(ItemInit.harbinger_assignment_ledger.get()));
		grant(server, HarbingerAdvancementGranter.ADV_HERMIT_ROAD_LEDGER_GRANTED);
	}

	/**
	 * The Vicar's First Bloodcraft claimed: the four D1 proofs recorded through their recorders (500 mL absorbed, a
	 * formation, the venous stone, a Liber Sanguinum structure), the claim marked, the promotion to degree 2 through
	 * {@code FirstBloodcraftAssignment.promote}, and the reward kit put in the inventory. Runs {@link #neophyteD1}
	 * first when the player is below degree 1.
	 */
	public static void votaryD2(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		if (player.degree() < 1) neophyteD1(player);
		FirstBloodcraftAssignment.recordAbsorption(server, 500.0D);
		FirstBloodcraftAssignment.recordFormation(server);
		FirstBloodcraftAssignment.recordVenousStone(server);
		FirstBloodcraftAssignment.recordStructure(server, new ItemStack(ItemInit.liber_sanguinum.get()));
		FirstBloodcraftAssignment.markClaimed(server);
		player.helper().assertTrue(FirstBloodcraftAssignment.promote(server), "the D1 claim promotes to degree 2");
		for (ItemStack stack : FirstBloodcraftAssignment.rewardStacks()) EarlyInitiation.give(server, stack);
	}

	/**
	 * First Separation claimed and Concentrated Blood settled: briefed ({@code markBriefed}) with its two vials
	 * delivered, the started and complete spin advancements, the tools claimed and delivered
	 * ({@code claimRewards}), the Alembic's {@code distill} use recorded, then the Concentrated injection and a
	 * completed rest ({@code ConcentratedBlood.inject}, {@code completeSleep}), which promotes to degree 3. Runs
	 * {@link #votaryD2} first when the player is below degree 2.
	 */
	public static void initiateD3(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		GameTestHelper helper = player.helper();
		if (player.degree() < 2) votaryD2(player);
		FirstSeparationAssignment.markBriefed(server);
		FirstSeparationAssignment.giveBriefingSupplies(server);
		grant(server, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED);
		grant(server, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE);
		helper.assertTrue(FirstSeparationAssignment.claimRewards(server), "First Separation can be claimed");
		HemoCapabilityAccess.stationUpgrades(server).recordUse(UpgradeStation.ALEMBIC, StationUpgradeCatalog.DISTILL);
		helper.assertTrue(ConcentratedBlood.inject(server), "Concentrated Blood can be injected");
		helper.assertTrue(ConcentratedBlood.completeSleep(server, true), "a completed rest settles Concentrated Blood");
	}

	/**
	 * The Woven Vessel chapter done and the Sanguine Brotherhood passed: the Mnemonist's turn-in, the first weave and
	 * the finished vessel advancements, degree 4 through {@code DegreeProgression.advance}, and the Brotherhood's Liber
	 * unlock. No items: the kit, the Loom and the woven memory are the band's to make. Runs {@link #initiateD3} first
	 * when the player is below degree 3.
	 */
	public static void adeptD4(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		if (player.degree() < 3) initiateD3(player);
		grant(server, HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE);
		grant(server, HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE);
		grant(server, HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_FINISHED);
		player.helper().assertTrue(DegreeProgression.advance(server, 4), "the Brotherhood promotes to degree 4");
		LiberKnowledgeHelper.unlockForRite(server, "cardinal_rite/sanguine_brotherhood");
	}

	/**
	 * The Vein-Mason chapter done, the Crimson Lodge passed and the continuation claimed: the Mason's Respite
	 * directive (advancement and map), the first lesson's scar known and active, the lesson, carve, learn, Effigy
	 * pattern, loadout and continuation-ready advancements, degree 5 through {@code DegreeProgression.advance} with
	 * {@code crimson_lodge_consecrated} and the Lodge's Liber unlock, then the continuation kit (pattern, blank,
	 * catalyst, four motif papers) in the inventory and {@code vein_mason_reward_claimed}. Runs {@link #adeptD4} first
	 * when the player is below degree 4.
	 */
	public static void illuminatusD5(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		if (player.degree() < 4) adeptD4(player);
		grant(server, HarbingerAdvancementGranter.ADV_VICAR_MASONS_RESPITE_DIRECTIVE);
		EarlyInitiation.give(server, new ItemStack(ItemInit.masons_respite_map.get()));
		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.forPlayer(server);
		var scars = HemoCapabilityAccess.getScarState(server);
		scars.addKnownCerebralScar(lesson.patternScarId());
		scars.activateCerebralScar(lesson.patternScarId());
		grant(server, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON);
		grant(server, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_CARVED);
		grant(server, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_LEARNED);
		grant(server, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN);
		grant(server, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT);
		grant(server, HarbingerAdvancementGranter.ADV_VEIN_MASON_CONTINUATION_READY);
		player.helper().assertTrue(DegreeProgression.advance(server, 5), "the Crimson Lodge promotes to degree 5");
		grant(server, HarbingerAdvancementGranter.ADV_CRIMSON_LODGE_CONSECRATED);
		LiberKnowledgeHelper.unlockForRite(server, "cardinal_rite/illuminatus_rite");
		VeinMasonScarLesson.Lesson continuation = VeinMasonScarLesson.continuationForPlayer(server);
		EarlyInitiation.give(server, continuation.patternStack());
		EarlyInitiation.give(server, new ItemStack(ItemInit.scar_blank.get()));
		EarlyInitiation.give(server, new ItemStack(continuation.catalyst()));
		EarlyInitiation.give(server, new ItemStack(ItemInit.runic_motif_paper.get(), 4));
		grant(server, HarbingerAdvancementGranter.ADV_VEIN_MASON_REWARD_CLAIMED);
	}

	// =============================================================================================================
	// Late game (D5 onward). ARRANGE only, each on the one before; every one registers its saved-data clean-up (the
	// player's own lines, Fanes and Blooms are also cleared by PlaythroughCleanup.worldOwnedBy).
	// =============================================================================================================

	/** The bloodline name the Founding rite gives (code authoritative; NPC-6 logs that it is English). */
	public static String lineName(PlaythroughPlayer player) {
		return player.player().getName().getString() + "'s Blood Line";
	}

	/**
	 * A founded bloodline at D5, as the Rite of Bloodline Founding leaves it ({@code presignBloodlineLedger}): a line
	 * registered in {@code BloodlineSavedData} with the player as leader, set on their blood volume, the founder flag,
	 * {@code blood_is_bound}, and the signed ledger (state and line written on it) in the inventory. Runs
	 * {@link #illuminatusD5} first when the player is below degree 5. Returns the registered line.
	 */
	public static Bloodline foundedLineD5(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		if (player.degree() < 5) illuminatusD5(player);
		BloodlineSavedData data = BloodlineSavedData.get(server.server.overworld());
		Bloodline line = new Bloodline(lineName(player), server.getUUID(),
				new UUID(server.getUUID().getMostSignificantBits(), server.serverLevel().getGameTime()), new ArrayList<>());
		data.registerBloodline(line);
		PlaythroughCleanup.onEnd(player.helper(), () -> data.disbandBloodline(line.getBloodlineUUID()));
		var volume = HemoCapabilityAccess.getBloodVolume(server);
		volume.setBloodLine(line);
		BloodVolumeEvents.syncVolume(server, volume);
		var degree = HemoCapabilityAccess.getInitiatoryDegree(server);
		degree.setHasFoundedBloodline(true);
		InitiatoryDegreeEvents.syncDegree(server, degree);
		grant(server, HarbingerAdvancementGranter.ADV_BLOOD_IS_BOUND);
		EarlyInitiation.give(server, signedLedger(line));
		return line;
	}

	/** A signed bloodline ledger for {@code line}, as the Founding rite shapes its result. */
	public static ItemStack signedLedger(Bloodline line) {
		ItemStack ledger = new ItemStack(ItemInit.unsigned_ancestral_ledger.get());
		CompoundTag tag = new CompoundTag();
		tag.putBoolean(UnsignedLedgerItem.TAG_STATE, true);
		tag.put(UnsignedLedgerItem.TAG_BLOODLINE, line.serialize());
		ledger.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return ledger;
	}

	/** The registered line {@code player} belongs to, or null. */
	@Nullable
	public static Bloodline lineOf(PlaythroughPlayer player) {
		Bloodline local = HemoCapabilityAccess.getBloodVolume(player.player()).getBloodLine();
		if (!local.isValid()) return null;
		return BloodlineSavedData.get(player.player().server.overworld()).getBloodline(local.getBloodlineUUID());
	}

	/**
	 * {@code player} a member of {@code leader}'s registered line, as a signed-ledger join leaves them
	 * ({@code BloodlineSavedData.addMember}, the line set on their blood volume and synced). Their degree is not
	 * touched: a member's degree is the test's call. The leader needs a line ({@link #foundedLineD5}).
	 */
	public static Bloodline memberOf(PlaythroughPlayer player, PlaythroughPlayer leader) {
		Bloodline line = lineOf(leader);
		player.helper().assertTrue(line != null && line.getLeaderUUID().equals(leader.uuid()),
				"the leader has a registered line to join");
		BloodlineSavedData data = BloodlineSavedData.get(player.player().server.overworld());
		Bloodline joined = data.addMember(line.getBloodlineUUID(), player.uuid());
		var volume = HemoCapabilityAccess.getBloodVolume(player.player());
		volume.setBloodLine(joined);
		BloodVolumeEvents.syncVolume(player.player(), volume);
		var leaderVolume = HemoCapabilityAccess.getBloodVolume(leader.player());
		leaderVolume.setBloodLine(joined);
		BloodVolumeEvents.syncVolume(leader.player(), leaderVolume);
		PlaythroughCleanup.onEnd(player.helper(), () -> data.removeMember(joined.getBloodlineUUID(), player.uuid()));
		return joined;
	}

	/**
	 * The covenant written at D5, as the Founding Fane rite leaves its founder ({@code completeFoundingFane}): a
	 * founded line ({@link #foundedLineD5} when the player has none), the Consecrated Bloodwell at {@code heartPos}
	 * consecrated for the line's leader, and {@code founding_fane_established} and {@code covenant_written_in_place}.
	 * A member's covenant comes from a deposit instead: give a member the advancement with {@link #covenantProof}.
	 */
	public static BlockPos covenantD5(PlaythroughPlayer player, BlockPos heartPos) {
		ServerPlayer server = player.player();
		if (lineOf(player) == null) foundedLineD5(player);
		PlaythroughSites.fane(player, heartPos);
		grant(server, HarbingerAdvancementGranter.ADV_FOUNDING_FANE_ESTABLISHED);
		covenantProof(player);
		return heartPos.immutable();
	}

	/** ARRANGE: only the D5 chapter proof, {@code covenant_written_in_place} (a member's Bloodwell deposit). */
	public static void covenantProof(PlaythroughPlayer player) {
		grant(player.player(), HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE);
	}

	/**
	 * {@code npc} recruited into {@code player}'s line as {@code recruit_harbinger} records it: an NPC member with its
	 * entity type and outpost key ({@code BloodlineSavedData.addNpcMember}), the updated line set and synced.
	 */
	public static Bloodline recruited(PlaythroughPlayer player, Entity npc) {
		Bloodline line = lineOf(player);
		player.helper().assertTrue(line != null, "the player has a line to recruit into");
		BloodlineSavedData data = BloodlineSavedData.get(player.player().server.overworld());
		ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(npc.getType());
		String outpost = HarbingerRecruitmentRules.findOutpostKey(npc);
		Bloodline updated = data.addNpcMember(line.getBloodlineUUID(), npc.getUUID(), type, outpost);
		var volume = HemoCapabilityAccess.getBloodVolume(player.player());
		volume.setBloodLine(updated);
		BloodVolumeEvents.syncVolume(player.player(), volume);
		return updated;
	}

	/**
	 * Degree 6 (Sanctified), as the sanctified rite promotes ({@code DegreeProgression.advance}). Runs
	 * {@link #covenantD5} first (with its Fane at {@code heartPos}) when the covenant is not written.
	 */
	public static void sanctifiedD6(PlaythroughPlayer player, BlockPos heartPos) {
		if (!player.has(HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE.getPath())) covenantD5(player, heartPos);
		if (player.degree() < 6) {
			player.helper().assertTrue(DegreeProgression.advance(player.player(), 6), "the sanctified rite promotes to degree 6");
		}
	}

	/** {@link #sanctifiedD6} with the Fane's heart at the player's home, two blocks east and three up. */
	public static void sanctifiedD6(PlaythroughPlayer player) {
		sanctifiedD6(player, defaultHeart(player));
	}

	/**
	 * The Living Covenant at D6: {@code chamber_returned} (ARRANGE: the Chamber of Will is not loaded on the GameTest
	 * server; proof owed by {@code chamberReviewClient}), {@code chamber_rite_attuned} and the visit service's
	 * attunement, {@code covenant_throne_bound} and {@code covenant_vigil_completed}, then
	 * {@code living_covenant_complete} through {@code HarbingerChapterProgression.tryCompleteLivingCovenant}.
	 */
	public static void livingCovenantD6(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		if (player.degree() < 6) sanctifiedD6(player);
		grant(server, HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED); // ARRANGE (NOT RUN: Chamber of Will not loaded)
		grant(server, HarbingerAdvancementGranter.ADV_CHAMBER_RITE_ATTUNED);
		ChamberVisitService.access(server).setAttuned(true);
		grant(server, HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND);
		grant(server, HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED);
		HarbingerChapterProgression.tryCompleteLivingCovenant(server);
		player.helper().assertTrue(player.has(HarbingerAdvancementGranter.ADV_LIVING_COVENANT_COMPLETE.getPath()),
				"the three parts complete the Living Covenant");
	}

	/** Degree 7 (Archon), as the archon rite promotes. Runs {@link #livingCovenantD6} first when it is not complete. */
	public static void archonD7(PlaythroughPlayer player) {
		if (!player.has(HarbingerAdvancementGranter.ADV_LIVING_COVENANT_COMPLETE.getPath())) livingCovenantD6(player);
		if (player.degree() < 7) {
			player.helper().assertTrue(DegreeProgression.advance(player.player(), 7), "the archon rite promotes to degree 7");
		}
	}

	/**
	 * Qliphoth Communion at D7: nine pomes recorded from a Bloom (the real one at {@code bloomRoot}, built with
	 * {@link PlaythroughSites#qliphothBloom} in the test level; null records them against a Bloom id with no block,
	 * so the test needs no Bloom and no own batch), the total pome count, Communion done, then one real
	 * {@code PendingDelivery} tick, which delivers the Fungal Spine (it must land). Runs {@link #archonD7} first.
	 * Returns the Bloom, or null.
	 */
	@Nullable
	public static QliphothBloomSavedData.BloomEntry communedD7(PlaythroughPlayer player, @Nullable BlockPos bloomRoot) {
		ServerPlayer server = player.player();
		if (player.degree() < 7) archonD7(player);
		QliphothBloomSavedData.BloomEntry bloom = bloomRoot == null ? null : PlaythroughSites.qliphothBloom(player, bloomRoot);
		UUID bloomId = bloom == null ? UUID.randomUUID() : bloom.bloomId();
		long origin = bloom == null ? server.blockPosition().asLong() : bloom.center().asLong();
		var degree = HemoCapabilityAccess.getInitiatoryDegree(server);
		var blooms = QliphothBloomSavedData.get(server.server.overworld());
		for (int husk = degree.getPomesConsumedFromBloom(bloomId); husk < 9; husk++) {
			degree.recordPomeConsumed(bloomId);
			degree.incrementTotalPomesConsumed();
			if (bloom != null) blooms.incrementPomesDropped(bloom);
		}
		degree.setQliphothCommunionDone(true);
		InitiatoryDegreeEvents.syncDegree(server, degree);
		player.tickOnce(); // the 20-tick PendingDelivery driver: the Spine is owed now
		player.helper().assertTrue(degree.hasFungalSpineGranted() && player.count(ItemInit.fungal_spine.get()) > 0,
				"Communion's Fungal Spine is delivered on the delivery tick; owed " + player.owed(QliphothBloomEvents.FUNGAL_SPINE));
		return bloom;
	}

	/** {@link #communedD7} with no Bloom block. */
	public static void communedD7(PlaythroughPlayer player) {
		communedD7(player, null);
	}

	/**
	 * The revelation witnessed and its choice pending, as the forced return from the first Spine projection leaves
	 * it ({@code performForcedProjectionReturn}): {@code setFungalRevelationWitnessed} and the
	 * {@code REVELATION_CHOICE_PENDING} root key. Runs {@link #communedD7} (no Bloom) first.
	 */
	public static void choicePendingD7(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		if (!HemoCapabilityAccess.getInitiatoryDegree(server).isQliphothCommunionDone()) communedD7(player);
		var degree = HemoCapabilityAccess.getInitiatoryDegree(server);
		degree.setFungalRevelationWitnessed(true);
		server.getPersistentData().putBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING, true);
		InitiatoryDegreeEvents.syncDegree(server, degree);
	}

	/** Silence chosen ({@code archon_choice_silence}): the choice key removed and the path SILENT_PENDING. */
	public static void silentPendingD7(PlaythroughPlayer player) {
		choose(player, EnumArchonPath.SILENT_PENDING);
	}

	/** The Eighth Degree chosen ({@code archon_choice_eighth_degree}): the path APOTHEOS_PENDING. */
	public static void apotheosPendingD7(PlaythroughPlayer player) {
		choose(player, EnumArchonPath.APOTHEOS_PENDING);
	}

	private static void choose(PlaythroughPlayer player, EnumArchonPath path) {
		ServerPlayer server = player.player();
		var degree = HemoCapabilityAccess.getInitiatoryDegree(server);
		if (degree.getArchonPath() == path) return;
		if (!degree.hasWitnessedFungalRevelation()) choicePendingD7(player);
		server.getPersistentData().remove(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING);
		degree.setArchonPath(path);
		InitiatoryDegreeEvents.syncDegree(server, degree);
	}

	/**
	 * Vesper beaten at D7 on the Silent path, as {@code VesperOrdealManager.completeVictory} leaves it (ARRANGE: the
	 * ordeal runs in the Chamber of Will; proof owed by {@code vesperReviewClient}): {@code vesper_defeated}, the
	 * owner's Bloom severed then sealed when they have one, and the Memory of Vesper claim
	 * ({@code hemomancy:vesper_memory_pending} under PlayerPersisted) queued, not yet delivered. Runs
	 * {@link #silentPendingD7} first.
	 */
	public static void vesperBeatenD7(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		if (HemoCapabilityAccess.getInitiatoryDegree(server).getArchonPath() != EnumArchonPath.SILENT_PENDING) {
			silentPendingD7(player);
		}
		var blooms = QliphothBloomSavedData.get(server.server.overworld());
		for (var bloom : List.copyOf(blooms.getBlooms())) {
			if (!bloom.ownerUUID().equals(server.getUUID())) continue;
			blooms.severBloom(bloom);
			blooms.sealBloom(bloom);
		}
		grant(server, HarbingerAdvancementGranter.ADV_VESPER_DEFEATED);
		CompoundTag persisted = server.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
		persisted.putBoolean(VESPER_MEMORY_PENDING, true);
		server.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
	}

	/** {@code VesperOrdealManager}'s Memory claim key (under PlayerPersisted). */
	public static final String VESPER_MEMORY_PENDING = "hemomancy:vesper_memory_pending";

	/** Degree 8 Apotheos, as the apotheos rite promotes. Runs {@link #apotheosPendingD7} first. */
	public static void apotheosD8(PlaythroughPlayer player) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player.player());
		if (degree.getArchonPath() != EnumArchonPath.APOTHEOS_PENDING && degree.getArchonPath() != EnumArchonPath.APOTHEOS) {
			apotheosPendingD7(player);
		}
		if (player.degree() < 8) {
			player.helper().assertTrue(DegreeProgression.advance(player.player(), 8, EnumArchonPath.APOTHEOS),
					"the apotheos rite promotes to degree 8");
		}
	}

	/**
	 * Degree 8 Silentium, as absorbing the Memory of Vesper at a lit Iron Brazier leaves it
	 * ({@code LivingWeaponGraftRite}): the Memory claim settled (delivered and drawn in), the staff progress's Vesper
	 * memory awakened and the Vesper sickle known when the player has a Living Staff bond, then
	 * {@code DegreeProgression.advance(8, SILENTIUM)}. Runs {@link #vesperBeatenD7} first.
	 */
	public static void silentiumD8(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		if (!player.has(HarbingerAdvancementGranter.ADV_VESPER_DEFEATED.getPath())) vesperBeatenD7(player);
		CompoundTag persisted = server.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
		persisted.remove(VESPER_MEMORY_PENDING);
		server.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
		var progress = HemoCapabilityAccess.getLivingStaffProgress(server);
		if (progress != null && progress.hasLivingStaffBond() && !progress.isVesperMemoryAwakened()) {
			progress.awakenVesperMemory();
			LivingStaffBondHelper.ensureVesperSickleKnown(server);
			LivingStaffBondHelper.syncProgress(server);
		}
		if (player.degree() < 8) {
			player.helper().assertTrue(DegreeProgression.advance(server, 8, EnumArchonPath.SILENTIUM),
					"absorbing the Memory promotes to degree 8 Silentium");
		}
	}

	// --- The Brined Court and the Crimson Troupe ------------------------------------------------------------------

	/** The Court known by {@code route} ({@code CourtDiscoveryProgress.markKnown}). */
	public static void courtKnown(PlaythroughPlayer player, CourtProgress.Route route) {
		CourtDiscoveryProgress.markKnown(player.player(), route);
	}

	/**
	 * {@code standing} with {@code house} as acts leave it: the Court known (FOUND) when it is not, then the house's
	 * {@code visit} act for GUEST, and a custom act ({@code playthrough_custom}) on top for FRIEND, each through
	 * {@code CourtLife.recordAct}. STRANGER changes nothing.
	 */
	public static void courtStanding(PlaythroughPlayer player, CourtHouse house, CourtStandingRules.Standing standing) {
		ServerPlayer server = player.player();
		if (!CourtDiscoveryProgress.isKnown(server)) courtKnown(player, CourtProgress.Route.FOUND);
		if (standing.level() >= CourtStandingRules.Standing.GUEST.level()) {
			CourtLife.recordAct(server, house, CourtLife.VISIT_ACT, CourtStandingRules.ActKind.VISIT);
		}
		if (standing.level() >= CourtStandingRules.Standing.FRIEND.level()) {
			CourtLife.recordAct(server, house, "playthrough_custom", CourtStandingRules.ActKind.CUSTOM);
		}
		player.helper().assertTrue(CourtLife.standing(server, house) == standing,
				"standing with " + house + " is " + standing + ", not " + CourtLife.standing(server, house));
	}

	/** The pavilion discovered and the Circus lesson {@code summon} taught ({@code CircusApprenticeshipProgress.teach}). */
	public static void circusKnownShape(PlaythroughPlayer player, String summon) {
		CircusDiscoveryProgress.markDiscovered(player.player());
		player.helper().assertTrue(CircusApprenticeshipProgress.teach(player.player(), summon),
				"the Circus lesson " + summon + " can be taught at degree " + player.degree());
	}

	/** The pavilion discovered and acclimation raised to at least {@code score}. */
	public static void acclimated(PlaythroughPlayer player, int score) {
		CircusDiscoveryProgress.markDiscovered(player.player());
		int now = CircusPlayerProgress.acclimation(player.player());
		if (now < score) CircusPlayerProgress.addAcclimation(player.player(), score - now);
	}

	/**
	 * A chosen pavilion route with the challenges in {@code challengeMask} (bits 0-4) done, each through
	 * {@code CircusPlayerProgress.completeChallenge} with no acclimation of its own.
	 */
	public static void pavilionRoute(PlaythroughPlayer player, CircusRouteRules.Route route, int challengeMask) {
		ServerPlayer server = player.player();
		CircusDiscoveryProgress.markDiscovered(server);
		CircusPlayerProgress.chooseRoute(server, route);
		for (int challenge = 0; challenge < 5; challenge++) {
			if ((challengeMask & (1 << challenge)) != 0) CircusPlayerProgress.completeChallenge(server, challenge, 0);
		}
	}

	// --- Checkpoints -----------------------------------------------------------------------------------------------

	/** The late states a playthrough run compares against, in path order. */
	public enum Checkpoint {
		FOUNDED_LINE_D5, COVENANT_D5, SANCTIFIED_D6, LIVING_COVENANT_D6, ARCHON_D7, COMMUNED_D7, CHOICE_PENDING_D7,
		SILENT_PENDING_D7, APOTHEOS_PENDING_D7, VESPER_BEATEN_D7, APOTHEOS_D8, SILENTIUM_D8
	}

	/**
	 * The facts {@code checkpoint}'s fixture sets, checked on {@code player} (degree, path, Communion and pomes, the
	 * listed advancements, bloodline role and Fane heart, the carried key items). Every fact that does not hold is
	 * listed; empty when the player is at the checkpoint. Play and fixture are held to this one list.
	 */
	public static List<String> missingAt(PlaythroughPlayer player, Checkpoint checkpoint) {
		List<String> missing = new ArrayList<>();
		ServerPlayer server = player.player();
		var degree = HemoCapabilityAccess.getInitiatoryDegree(server);
		int ordinal = checkpoint.ordinal();
		boolean silent = checkpoint == Checkpoint.SILENT_PENDING_D7 || checkpoint == Checkpoint.VESPER_BEATEN_D7
				|| checkpoint == Checkpoint.SILENTIUM_D8;
		boolean apotheos = checkpoint == Checkpoint.APOTHEOS_PENDING_D7 || checkpoint == Checkpoint.APOTHEOS_D8;
		int wantDegree = switch (checkpoint) {
			case FOUNDED_LINE_D5, COVENANT_D5 -> 5;
			case SANCTIFIED_D6, LIVING_COVENANT_D6 -> 6;
			case APOTHEOS_D8, SILENTIUM_D8 -> 8;
			default -> 7;
		};
		if (player.degree() != wantDegree) missing.add("degree " + wantDegree + " (is " + player.degree() + ")");
		Bloodline line = lineOf(player);
		if (line == null) missing.add("a registered bloodline");
		else if (!line.getLeaderUUID().equals(player.uuid())) missing.add("leads the bloodline");
		if (!degree.hasFoundedBloodline()) missing.add("founder flag");
		need(player, missing, HarbingerAdvancementGranter.ADV_BLOOD_IS_BOUND);
		if (ordinal >= Checkpoint.COVENANT_D5.ordinal()) {
			need(player, missing, HarbingerAdvancementGranter.ADV_FOUNDING_FANE_ESTABLISHED);
			need(player, missing, HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE);
			BlockPos heart = FoundingFaneSavedData.get(server.serverLevel()).getHeart(player.uuid());
			if (heart == null) missing.add("a Fane heart");
			else if (!server.serverLevel().getBlockState(heart).is(BlockInit.consecrated_bloodwell.get())) {
				missing.add("the Consecrated Bloodwell at " + heart.toShortString());
			}
		}
		if (ordinal >= Checkpoint.SANCTIFIED_D6.ordinal()) need(player, missing, HarbingerAdvancementGranter.ADV_DEGREE_6_SANCTIFIED);
		if (ordinal >= Checkpoint.LIVING_COVENANT_D6.ordinal()) {
			need(player, missing, HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED);
			need(player, missing, HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND);
			need(player, missing, HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED);
			need(player, missing, HarbingerAdvancementGranter.ADV_LIVING_COVENANT_COMPLETE);
		}
		if (ordinal >= Checkpoint.ARCHON_D7.ordinal()) need(player, missing, HarbingerAdvancementGranter.ADV_DEGREE_7_ARCHON);
		if (ordinal >= Checkpoint.COMMUNED_D7.ordinal()) {
			if (!degree.isQliphothCommunionDone()) missing.add("Qliphoth Communion done");
			if (degree.getTotalPomesConsumed() < 9) missing.add("nine pomes (has " + degree.getTotalPomesConsumed() + ")");
			if (!degree.hasFungalSpineGranted()) missing.add("the Fungal Spine granted");
		}
		if (ordinal >= Checkpoint.CHOICE_PENDING_D7.ordinal() && !degree.hasWitnessedFungalRevelation()) {
			missing.add("the revelation witnessed");
		}
		boolean pendingKey = server.getPersistentData().getBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING);
		EnumArchonPath path = degree.getArchonPath();
		if (checkpoint == Checkpoint.CHOICE_PENDING_D7) {
			if (!pendingKey) missing.add("the revelation choice pending");
			if (path != EnumArchonPath.NONE) missing.add("no path yet (is " + path + ")");
		}
		if (checkpoint == Checkpoint.SILENT_PENDING_D7 || checkpoint == Checkpoint.VESPER_BEATEN_D7) {
			if (path != EnumArchonPath.SILENT_PENDING) missing.add("path SILENT_PENDING (is " + path + ")");
		}
		if (checkpoint == Checkpoint.APOTHEOS_PENDING_D7 && path != EnumArchonPath.APOTHEOS_PENDING) {
			missing.add("path APOTHEOS_PENDING (is " + path + ")");
		}
		if ((silent || apotheos) && pendingKey) missing.add("the revelation choice settled");
		if (checkpoint == Checkpoint.VESPER_BEATEN_D7 || checkpoint == Checkpoint.SILENTIUM_D8) {
			need(player, missing, HarbingerAdvancementGranter.ADV_VESPER_DEFEATED);
		}
		if (checkpoint == Checkpoint.VESPER_BEATEN_D7
				&& player.owed(VesperOrdealManager.MEMORY).isEmpty() && player.count(ItemInit.memory_of_vesper.get()) == 0) {
			missing.add("the Memory of Vesper owed or carried");
		}
		if (checkpoint == Checkpoint.APOTHEOS_D8) {
			if (path != EnumArchonPath.APOTHEOS) missing.add("path APOTHEOS (is " + path + ")");
			need(player, missing, HarbingerAdvancementGranter.ADV_DEGREE_8_APOTHEOS);
		}
		if (checkpoint == Checkpoint.SILENTIUM_D8) {
			if (path != EnumArchonPath.SILENTIUM) missing.add("path SILENTIUM (is " + path + ")");
			need(player, missing, HarbingerAdvancementGranter.ADV_DEGREE_8_SILENTIUM);
		}
		return missing;
	}

	/** Fails the test naming the first facts of {@code checkpoint} the player lacks ({@link #missingAt}). */
	public static void assertAt(GameTestHelper helper, PlaythroughPlayer player, Checkpoint checkpoint) {
		List<String> missing = missingAt(player, checkpoint);
		PlaythroughLog.note(helper, "checkpoint " + checkpoint + (missing.isEmpty() ? " holds" : " lacks " + missing));
		helper.assertTrue(missing.isEmpty(), "the player is not at " + checkpoint + ": lacks " + missing);
	}

	private static void need(PlaythroughPlayer player, List<String> missing, ResourceLocation advancement) {
		if (!player.has(advancement.getPath())) missing.add(advancement.getPath());
	}

	/** The Fane heart a checkpoint uses when the test names none: four blocks east of the player's home, on the floor. */
	public static BlockPos defaultHeart(PlaythroughPlayer player) {
		return player.home().east(4);
	}

	/** ARRANGE: fills the player's blood to {@code ml} (the pool's maximum at most). */
	public static void blood(PlaythroughPlayer player, double ml) {
		var volume = HemoCapabilityAccess.getBloodVolume(player.player());
		volume.setBloodVolume(Math.min(ml, volume.getMaxBloodVolume()));
		BloodVolumeEvents.syncVolume(player.player(), volume);
	}

	private static void grant(ServerPlayer player, ResourceLocation advancement) {
		HarbingerAdvancementGranter.grantIfNotDone(player, advancement);
	}
}

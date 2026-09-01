package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.StillArtInit;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public final class UnstainedJourneyChecks {
	private UnstainedJourneyChecks() { }

	public static UnstainedJourneyResult verify(ServerPlayer player, UnstainedJourneyStage stage, BlockPos origin) {
		var progress = HemoCapabilityAccess.requireUnstainedProgress(player);
		List<String> unmet = new ArrayList<>();
		switch (stage) {
			case NOVITIATE_GATHER_REMEDIES -> observances(unmet, progress,
					UnstainedObservances.Observance.NOVITIATE_GATHER_REMEDIES);
			case NOVITIATE_GENTLE_SEPARATION -> observances(unmet, progress,
					UnstainedObservances.Observance.NOVITIATE_GENTLE_SEPARATION);
			case NOVITIATE_STILLWATER_LABOR -> observances(unmet, progress,
					UnstainedObservances.Observance.NOVITIATE_STILLWATER_LABOR);
			case NOVITIATE_CLEAN_LABOR -> observances(unmet, progress,
					UnstainedObservances.Observance.NOVITIATE_CLEAN_LABOR);
			case NOVITIATE_SHELTER_AFFLICTED -> observances(unmet, progress,
					UnstainedObservances.Observance.NOVITIATE_SHELTER_AFFLICTED);
			case PODIUM_SUPPRESSION -> require(unmet, progress.isInfectionSuppressed(),
					"Use the Hemolytic Solution on the Unstained Podium to suppress the infection.");
			case LETHEAN_BAPTISM -> {
				require(unmet, progress.hasBegunPurification() && progress.getPurity() >= 5f,
						"Complete the prepared Rite of Lethean Baptism.");
				require(unmet, !hasItem(player, origin, ItemInit.absolution_dagger.get()),
						"Lethean Baptism must not grant the pledge dagger.");
			}
			case GHOST_PIPE_OBSERVANCE -> observances(unmet, progress,
					UnstainedObservances.Observance.GATHER_GHOST_PIPE);
			case TAINTED_ACOLYTE_OBSERVANCES -> observances(unmet, progress,
					UnstainedObservances.Observance.WEAVE_WREATH,
					UnstainedObservances.Observance.PREPARE_HEMOLYTIC);
			case SILVER_VEIL -> {
				require(unmet, progress.getPurity() >= 49f, "Complete the prepared Rite of the Silver Veil.");
				require(unmet, player.hasEffect(EffectInit.verdigris_aura),
						"The Silver Veil did not apply Verdigris Aura.");
			}
			case CLEANSING_OBSERVANCES -> observances(unmet, progress,
					UnstainedObservances.Observance.CONDENSE_STILL_WATERS,
					UnstainedObservances.Observance.PLATE_THE_WARD);
			case PALLID_ICON_OBSERVANCE -> observances(unmet, progress,
					UnstainedObservances.Observance.BEAR_PALLID_ICON);
			case SILTHMERE_REMEMBRANCE -> require(unmet, progress.isPurified(),
					"Complete Silthmere's Remembrance to reach 100 Purity.");
			case CLOSED_VEIN -> require(unmet, progress.isBaselineRestored()
					&& !progress.hasClarityUnlocked()
					&& !HemoCapabilityAccess.requireBloodVolume(player).isActive(),
					"Complete Closed Vein to restore a clean, unpledged baseline.");
			case CONSECRATED_COPPER_OBSERVANCE -> observances(unmet, progress,
					UnstainedObservances.Observance.CONSECRATE_COPPER);
			case CLARITY_PREPARED -> require(unmet, progress.isClarityPrepared(),
					"Use Consecrated Copper on the Unstained Podium.");
			case CLARITY_ASCENSION -> {
				require(unmet, progress.hasClarityUnlocked() && !progress.isClarityPrepared(),
						"Complete the prepared Rite of Clarity Ascension.");
				require(unmet, HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber() == 0,
						"Clarity Ascension did not clear the Harbinger degree.");
				require(unmet, hasItem(player, origin, ItemInit.absolution_dagger.get()),
						"Clarity Ascension did not grant the pledge dagger.");
				require(unmet, knows(player, StillArtInit.silver_rebuke.get())
						&& knows(player, StillArtInit.lethean_mute.get()),
						"Awakened Still Arts were not granted.");
			}
			case GLASS_LUNGS -> require(unmet, hasItem(player, origin, ItemInit.lethean_chalice.get()),
					"Complete Glass Lungs and pick up the Lethean Chalice.");
			case CHALICE_OBSERVANCE -> observances(unmet, progress,
					UnstainedObservances.Observance.OFFER_CHALICE);
			case DISCERNING -> require(unmet, progress.getClarity() >= 50f
					&& knows(player, StillArtInit.still_pulse.get())
					&& knows(player, StillArtInit.pale_diagnosis.get()),
					"Wait for the Discerning milestone and its Still Arts.");
			case PALE_VIGIL -> require(unmet, progress.getClarity() >= 75f
					&& player.hasEffect(EffectInit.silver_ward) && player.hasEffect(EffectInit.verdigris_aura),
					"Complete the Pale Vigil and receive both wards.");
			case MOON_WASHED_COPPER -> require(unmet,
					hasItem(player, origin, ItemInit.pale_silver_bell.get()),
					"Complete Moon-Washed Copper and pick up the Pale Silver Bell.");
			case PALE_WATCH_OBSERVANCE -> observances(unmet, progress,
					UnstainedObservances.Observance.RING_THE_PALE_WATCH);
			case RESOLUTE -> require(unmet, progress.getClarity() >= 75f
					&& knows(player, StillArtInit.quietus_bell.get())
					&& knows(player, StillArtInit.pale_intercession.get()),
					"Wait for the Resolute milestone and its Still Arts.");
			case ENLIGHTENED -> require(unmet, progress.isEnlightened()
					&& knows(player, StillArtInit.autoimmune_edge.get())
					&& hasItem(player, origin, ItemInit.vestment_of_the_final_molt.get()),
					"Use Hemolytic Plating on the Podium and receive the Enlightened Still Art and Vestment.");
			case LETHEAN_FONT -> {
				require(unmet, player.hasEffect(EffectInit.silver_ward) && player.hasEffect(EffectInit.verdigris_aura),
						"Complete the Lethean Font and receive both hour-long wards.");
				require(unmet, hasItem(player, origin, ItemInit.pallid_icon.get()),
						"The Lethean Font did not produce its Pallid Icon.");
			}
			case COMPLETE -> { return new UnstainedJourneyResult(true, stage,
					"Unstained checkpoints complete; ready to restore the snapshot."); }
		}
		return unmet.isEmpty() ? new UnstainedJourneyResult(true, stage, "All checkpoint conditions passed.")
				: UnstainedJourneyResult.fail(stage, String.join("\n", unmet));
	}

	private static void observances(List<String> unmet,
			com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress progress,
			UnstainedObservances.Observance... observances) {
		for (UnstainedObservances.Observance observance : observances) {
			require(unmet, (progress.getClaimedObservances() & observance.mask()) != 0,
					"Accept and fulfill " + observance.name().toLowerCase(java.util.Locale.ROOT) + ".");
		}
	}

	private static boolean knows(ServerPlayer player,
			com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArt art) {
		return HemoCapabilityAccess.requireKnownStillArts(player).isKnown(art);
	}

	private static boolean hasItem(ServerPlayer player, BlockPos origin, Item item) {
		if (player.getInventory().contains(stack -> stack.is(item))) return true;
		return !HemoJourneyFixtures.fixtureLevel(player).getEntitiesOfClass(ItemEntity.class,
				HemoJourneyFixtures.bounds(origin), entity -> entity.getItem().is(item)).isEmpty();
	}

	private static void require(List<String> unmet, boolean condition, String message) {
		if (!condition) unmet.add(message);
	}
}

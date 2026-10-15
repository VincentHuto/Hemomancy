package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow;
import com.vincenthuto.hemomancy.common.succession.SuccessionRites;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * The one way an active Cardinal Rite ends. Every outcome releases the same
 * things in the same order: rite-bound threats, station and succession escrow,
 * NPC allies, the daemon, and the planted Living Staff. Only a completed rite
 * keeps its station-upgrade result; everything else returns its escrow.
 */
public final class CardinalRiteTermination {
	public enum Outcome {
		/** The rite resolved and its result was applied. */
		COMPLETED(CardinalRiteEventKind.COMPLETED),
		/** Instability or a failed ordeal collapsed it. */
		COLLAPSED(CardinalRiteEventKind.COLLAPSED),
		/** Its floor, braziers or structure were broken under it. */
		BROKEN(CardinalRiteEventKind.BROKEN),
		/** The caster drew it back into the staff. */
		CANCELLED(CardinalRiteEventKind.CANCELLED),
		/** The caster died. */
		DIED(CardinalRiteEventKind.COLLAPSED),
		/** The caster stayed away past the disconnect grace. */
		ABANDONED(CardinalRiteEventKind.COLLAPSED),
		/** Its recipe no longer exists. */
		RETIRED(CardinalRiteEventKind.CANCELLED),
		/** A final check refused the result; nothing was granted. */
		REFUSED(CardinalRiteEventKind.CANCELLED);

		private final CardinalRiteEventKind beat;

		Outcome(CardinalRiteEventKind beat) {
			this.beat = beat;
		}

		public CardinalRiteEventKind beat() {
			return beat;
		}
	}

	private CardinalRiteTermination() {
	}

	/**
	 * Releases everything the rite holds and tells nearby clients how it ended.
	 * The caller removes the rite from saved data and resyncs.
	 *
	 * @param caster the caster when online in a usable state, else {@code null};
	 *               an absent caster's Living Staff goes to the recovery queue
	 * @param style  culmination style for a completed rite, otherwise empty
	 */
	public static void end(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite, Outcome outcome,
			String style) {
		// A rite ends once: lethal backlash can reach the death handler before the caller finishes.
		if (!rite.markEnded()) return;
		CardinalRiteOrdealEngine.clearThreats(level, rite);
		if (outcome != Outcome.COMPLETED) {
			StationUpgradeRites.recover(level, rite);
			WorkingCraftRites.recover(level, rite);
		}
		RiteOfferingEscrow.clearShown(level, rite);
		StationUpgradeRites.cleanup(level, rite);
		SuccessionRites.cleanup(level, rite);
		CardinalRiteAllyService.returnNpcAlliesToFane(level, rite);
		discardDaemon(level, rite);
		returnStaff(level, caster, rite);
		// Unstained rites keep their own still-water presentation; blood-rite endings are Harbinger-only.
		var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		if (recipe == null || !recipe.isUnstained()) {
			CardinalRiteBeats.send(level, rite, outcome.beat(), style == null ? "" : style);
		}
	}

	public static void end(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite, Outcome outcome) {
		end(level, caster, rite, outcome, "");
	}

	private static void returnStaff(ServerLevel level, ServerPlayer caster, ActiveCardinalRite rite) {
		if (!rite.hasEscrowedStaff()) return;
		if (caster != null) {
			CardinalRiteStaffEscrow.restore(caster, rite);
			return;
		}
		ItemStack staff = rite.releaseEscrowedStaff(level.registryAccess());
		if (staff.isEmpty()) return;
		UUID recoveryId = UUID.nameUUIDFromBytes((rite.getPlayerUUID() + ":" + rite.getRecipeId() + ":"
				+ rite.getCenterPos().asLong() + ":staff").getBytes(StandardCharsets.UTF_8));
		CardinalRiteSavedData.get(level.getServer().overworld())
				.addRecovery(rite.getPlayerUUID(), recoveryId, List.of(staff));
	}

	static void discardDaemon(ServerLevel level, ActiveCardinalRite rite) {
		for (HumanitySpriteEntity sprite : level.getEntitiesOfClass(
				HumanitySpriteEntity.class, new AABB(rite.getCenterPos()).inflate(128.0D),
				entity -> entity.isBoundToRite(rite.getPlayerUUID()))) {
			sprite.discard();
		}
	}
}

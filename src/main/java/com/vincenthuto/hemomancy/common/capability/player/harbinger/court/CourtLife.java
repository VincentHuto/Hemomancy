package com.vincenthuto.hemomancy.common.capability.player.harbinger.court;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.PrimalMorphlingRules;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtStructure;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;

/**
 * Court life (Brined Court Track 11): house membership, standing, the Lightkeeper's cultivation lesson, the Giving Light
 * transgression record, the Kindled Champion title and the shunning of Unstained visitors. This is the service Track 9's
 * dialogue and Track 10's fights call; the pure rules are {@link CourtMembershipRules}, {@link CourtStandingRules},
 * {@link CourtLessonRules} and {@link CourtChampionRules}. Every write syncs the owner's {@link CourtProgress}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CourtLife {
	/** Foods brought to the Giving Light as amends (T2, T5). */
	public static final TagKey<Item> GIVING_LIGHT_OFFERING = ItemTags.create(Hemomancy.rloc("court_offering/giving_light"));
	/** The act a site visit counts as, once per house. */
	public static final String VISIT_ACT = "visit";
	public static final int VISIT_INTERVAL_TICKS = 20;

	private CourtLife() {
	}

	// --- Reads ---------------------------------------------------------------------------------------------------

	public static CourtProgress progress(Player player) {
		return CourtDiscoveryProgress.progress(player);
	}

	/**
	 * Every house shuns a player on the Unstained path as a walking biohazard (L8): no food, rest, gifts, lessons,
	 * membership or standing, and residents keep their distance. They are never attacked.
	 */
	public static boolean isShunned(Player player) {
		return ProgressionGates.isUnstained(player);
	}

	@Nullable
	public static CourtHouse house(Player player) {
		return progress(player).house();
	}

	public static boolean isMember(Player player, CourtHouse house) {
		return progress(player).house() == house;
	}

	public static CourtStandingRules.Standing standing(Player player, CourtHouse house) {
		return progress(player).standing(house);
	}

	/** Whether {@code house} turns the player away: the Giving Light while a transgression is outstanding. */
	public static boolean turnsAway(Player player, CourtHouse house) {
		return CourtMembershipRules.turnsAway(house, progress(player).hasTransgression());
	}

	public static boolean hasLesson(Player player) {
		return progress(player).lessonTaught();
	}

	public static boolean hasTransgression(Player player) {
		return progress(player).hasTransgression();
	}

	public static boolean isChampion(Player player) {
		return progress(player).champion();
	}

	public static boolean isOffering(ItemStack stack) {
		return !stack.isEmpty() && stack.is(GIVING_LIGHT_OFFERING);
	}

	/** A Giving Light offering in the main hand: the Warden holds off and reconciliation opens (T2). */
	public static boolean holdsOffering(Player player) {
		return isOffering(player.getItemInHand(InteractionHand.MAIN_HAND));
	}

	/** The house whose Seat or outpost has a piece at {@code pos}, or null outside every Court site. */
	@Nullable
	public static CourtHouse houseAt(ServerLevel level, BlockPos pos) {
		StructureStart start = level.structureManager().getStructureWithPieceAt(pos, CourtDiscoveryProgress.COURT);
		if (!start.isValid() || !(start.getStructure() instanceof CourtStructure court)) return null;
		return CourtHouse.byId(court.site().house());
	}

	/**
	 * The lesson's bonding multiplier for feeding {@code strain} where the player stands: {@link CourtLessonRules#MULTIPLIER}
	 * inside the strain's own house's site once the lesson is taught, otherwise 1. A shunned player never has it.
	 */
	public static double lessonMultiplier(ServerPlayer player, ItemStack strain) {
		CourtProgress progress = progress(player);
		if (!progress.lessonTaught() || isShunned(player)) return 1.0D;
		CourtHouse strainHouse = MorphlingStrains.of(strain).map(MorphlingStrain::house).orElse(null);
		if (strainHouse == null) return 1.0D;
		return CourtLessonRules.multiplier(true, strainHouse, houseAt(player.serverLevel(), player.blockPosition()));
	}

	// --- Membership ----------------------------------------------------------------------------------------------

	/**
	 * The house's lead or senior role asks the player in (Track 9 dialogue). Joins, switches (spending the one switch
	 * and dropping the house left behind to guest) or refuses; the player is told either way.
	 */
	public static CourtMembershipRules.Outcome tryJoin(ServerPlayer player, CourtHouse target) {
		CourtProgress progress = progress(player);
		CourtHouse previous = progress.house();
		CourtMembershipRules.Outcome outcome = CourtMembershipRules.join(previous, progress.switchUsed(), target,
				ProgressionGates.hasDegree(player, CourtMembershipRules.MINIMUM_DEGREE), progress.standing(target),
				isShunned(player), turnsAway(player, target));
		if (outcome.changed()) {
			if (outcome == CourtMembershipRules.Outcome.SWITCHED && previous != null) {
				progress.setStanding(previous, CourtStandingRules.afterLeaving(progress.standing(previous)));
			}
			progress.setHouse(target, outcome == CourtMembershipRules.Outcome.SWITCHED);
			CourtDiscoveryProgress.sync(player);
		}
		MutableComponent message = previous != null && outcome == CourtMembershipRules.Outcome.SWITCHED
				? Component.translatable(outcome.translationKey(), previous.displayName(), target.displayName())
				: Component.translatable(outcome.translationKey(), target.displayName());
		player.displayClientMessage(message.withStyle(outcome.changed() ? ChatFormatting.DARK_AQUA : ChatFormatting.GRAY), false);
		return outcome;
	}

	// --- Standing ------------------------------------------------------------------------------------------------

	/**
	 * Counts {@code actId} toward standing with {@code house}, once. Returns true when standing rose. A shunned player,
	 * an act already counted and a Giving Light act while that house turns the player away all change nothing; a
	 * refused act is not recorded, so it can count later.
	 */
	public static boolean recordAct(ServerPlayer player, CourtHouse house, String actId, CourtStandingRules.ActKind kind) {
		CourtProgress progress = progress(player);
		if (!CourtStandingRules.actCounts(progress.actCounted(house, actId), isShunned(player), turnsAway(player, house))) {
			return false;
		}
		CourtStandingRules.Standing before = progress.standing(house);
		CourtStandingRules.Standing after = CourtStandingRules.afterAct(before, kind);
		progress.countAct(house, actId);
		progress.setStanding(house, after);
		CourtDiscoveryProgress.sync(player);
		if (after == before) return false;
		player.displayClientMessage(Component.translatable("hemomancy.court.standing.raised",
				Component.translatable(after.translationKey()), house.displayName()).withStyle(ChatFormatting.DARK_AQUA), false);
		return true;
	}

	/** Being at a house's site makes a stranger its guest (the visit counts once per house). */
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % VISIT_INTERVAL_TICKS != 0) return;
		if (!player.isAlive() || player.isSpectator()) return;
		CourtProgress progress = progress(player);
		if (!progress.known()) return;
		CourtHouse house = houseAt(player.serverLevel(), player.blockPosition());
		if (house == null || progress.actCounted(house, VISIT_ACT)) return;
		recordAct(player, house, VISIT_ACT, CourtStandingRules.ActKind.VISIT);
	}

	// --- The lesson ----------------------------------------------------------------------------------------------

	/** The Lightkeeper teaches cultivation as partnership (Track 9 dialogue at the Garden). */
	public static CourtLessonRules.Outcome teachLesson(ServerPlayer player) {
		CourtProgress progress = progress(player);
		CourtLessonRules.Outcome outcome = CourtLessonRules.teach(progress.lessonTaught(),
				ProgressionGates.hasDegree(player, CourtLessonRules.MINIMUM_DEGREE), isShunned(player),
				turnsAway(player, CourtHouse.GIVING_LIGHT));
		if (outcome.taught()) {
			progress.setLessonTaught(true);
			CourtDiscoveryProgress.sync(player);
		}
		player.displayClientMessage(Component.translatable(outcome.translationKey())
				.withStyle(outcome.taught() ? ChatFormatting.DARK_AQUA : ChatFormatting.GRAY), false);
		return outcome;
	}

	// --- Transgression and amends --------------------------------------------------------------------------------

	/**
	 * Records a wrong against the Giving Light ({@code actId} names it, for the chorus). Drops Giving Light standing to
	 * stranger; the record survives death until amends. Whether the act wakes the Bloom Warden is Track 10's call.
	 */
	public static void recordTransgression(ServerPlayer player, String actId) {
		CourtProgress progress = progress(player);
		progress.recordTransgression(actId, player.serverLevel().getGameTime());
		progress.setStanding(CourtHouse.GIVING_LIGHT, CourtStandingRules.afterTransgression());
		CourtDiscoveryProgress.sync(player);
	}

	/**
	 * Amends with the Warden or the Lightkeeper: with a transgression outstanding and an offering in the main hand,
	 * consumes one offering, clears the record and restores Giving Light standing to guest. Returns false and changes
	 * nothing otherwise.
	 */
	public static boolean reconcile(ServerPlayer player) {
		CourtProgress progress = progress(player);
		if (!progress.hasTransgression() || !holdsOffering(player)) return false;
		player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
		progress.clearTransgression();
		progress.setStanding(CourtHouse.GIVING_LIGHT, CourtStandingRules.afterAmends(progress.standing(CourtHouse.GIVING_LIGHT)));
		CourtDiscoveryProgress.sync(player);
		player.displayClientMessage(Component.translatable("hemomancy.court.amends").withStyle(ChatFormatting.DARK_AQUA), false);
		return true;
	}

	// --- The presences (Track 10) -------------------------------------------------------------------------------

	public static boolean hasSeenPresence(Player player, String presence) {
		return progress(player).seenPresence(presence);
	}

	/** Records a presence's first sight; true the first time only, so its scene plays once per player. */
	public static boolean markPresenceSeen(ServerPlayer player, String presence) {
		if (!progress(player).markPresenceSeen(presence)) return false;
		CourtDiscoveryProgress.sync(player);
		return true;
	}

	// --- The Kindled Champion ------------------------------------------------------------------------------------

	/** Whether any strain the player wears or carries is at Apex or beyond. */
	public static boolean hasApexStrain(Player player) {
		return CourtDiscoveryProgress.highestStrainMaturity(player) >= PrimalMorphlingRules.APEX_LEVEL;
	}

	/** Whether the Head-tender may offer the usurpation now, or why not. */
	public static CourtChampionRules.Gate championGate(ServerPlayer player) {
		CourtProgress progress = progress(player);
		return CourtChampionRules.challenge(progress.known(), isShunned(player), progress.champion(), hasApexStrain(player),
				progress.standing(CourtHouse.KINDLING), player.serverLevel().getGameTime(), progress.usurpRetryAt());
	}

	/** The victor of the usurpation takes the title (recognition only, T3) and the retry wait is cleared. */
	public static void grantChampion(ServerPlayer player) {
		CourtProgress progress = progress(player);
		progress.setChampion(true, player.serverLevel().getGameTime());
		progress.setUsurpRetryAt(0L);
		CourtDiscoveryProgress.sync(player);
	}

	/** A later elder's challenge (deferred) takes the title back. */
	public static void revokeChampion(ServerPlayer player) {
		progress(player).setChampion(false, 0L);
		CourtDiscoveryProgress.sync(player);
	}

	/** A lost usurpation (the player died) starts the wait; it survives death and relog. */
	public static void recordUsurpLoss(ServerPlayer player) {
		progress(player).setUsurpRetryAt(CourtChampionRules.retryAfterLoss(player.serverLevel().getGameTime()));
		CourtDiscoveryProgress.sync(player);
	}
}

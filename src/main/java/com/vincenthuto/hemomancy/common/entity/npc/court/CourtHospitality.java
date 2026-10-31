package com.vincenthuto.hemomancy.common.entity.npc.court;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole.Offer;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtMemberDialogue;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * What Court members give, with no currency: food and rest, a one-time welcome gift per house, accepting an offering
 * as the house's custom, the Lightkeeper's lesson and a lead's welcome into the house. Each call is the dialogue
 * event's server side and checks everything again: the role offers it, the player isn't shunned or turned away, and
 * the thing is still there to give. A shunned player gets nothing.
 */
public final class CourtHospitality {
	/** The standing act a house's welcome gift records, once per house, so a gift is never taken twice. */
	public static final String GIFT_ACT = "welcome_gift";
	public static final int FOOD_HUNGER = 6;
	public static final float FOOD_SATURATION = 0.6F;
	/** What a rest restores, at once: the member's shade and quiet, not a potion. */
	public static final float REST_HEAL = 8.0F;
	/**
	 * Stone for the passages: the Holding Stone's custom is sharing the upkeep, one block of stone carried in and handed
	 * over. Every house without its own tag (the two Seats) takes the Garden's food offering.
	 */
	public static final TagKey<Item> HOLDING_STONE_OFFERING = ItemTags.create(Hemomancy.rloc("court_offering/holding_stone"));
	/** The catch, shared: the Shared Wake's custom is a raw fish brought to the nursery for the young. */
	public static final TagKey<Item> SHARED_WAKE_OFFERING = ItemTags.create(Hemomancy.rloc("court_offering/shared_wake"));
	/** Line for the camp: the Woven Current's custom is bringing fibre to mend and lengthen the lines between the walls. */
	public static final TagKey<Item> WOVEN_CURRENT_OFFERING = ItemTags.create(Hemomancy.rloc("court_offering/woven_current"));
	/** Ink for the screens: the Veiled Fold's custom is a gift given without a name, ink to keep the door's screen dark. */
	public static final TagKey<Item> VEILED_FOLD_OFFERING = ItemTags.create(Hemomancy.rloc("court_offering/veiled_fold"));
	/** Warmth for the bell: the Still Bell's custom is a guest's share of its upkeep, wool against the cold. */
	public static final TagKey<Item> STILL_BELL_OFFERING = ItemTags.create(Hemomancy.rloc("court_offering/still_bell"));
	/** Remains for the fall: Last Marrow's custom is bringing what is left of the dead, so nothing goes to waste. */
	public static final TagKey<Item> LAST_MARROW_OFFERING = ItemTags.create(Hemomancy.rloc("court_offering/last_marrow"));

	private CourtHospitality() {
	}

	/** The standing act id for a house's own custom (Track 11 leaves the ids to the residents). */
	public static String customAct(CourtHouse house) {
		return switch (house) {
			case GIVING_LIGHT -> "custom_food";
			case HOLDING_STONE -> "custom_promise";
			case SHARED_WAKE -> "custom_catch";
			case WOVEN_CURRENT -> "custom_line";
			case VEILED_FOLD -> "custom_silence";
			case STILL_BELL -> "custom_watch";
			case LAST_MARROW -> "custom_tending";
			case KINDLING -> "custom_vent_offering";
		};
	}

	/** The role's welcome gift; the house records it once whichever role hands it over. */
	public static ItemStack gift(CourtRole role) {
		return switch (role) {
			case GARDENER -> new ItemStack(Items.GLOW_BERRIES, 6);
			case HEAD_TENDER -> new ItemStack(Items.SEA_PICKLE, 4);
			case KINDLED_ELDER -> new ItemStack(Items.GLOW_INK_SAC, 2);
			case BRACED_ELDER -> new ItemStack(Items.KELP, 8);
			case NURSERY_TENDER -> new ItemStack(Items.COOKED_SALMON, 4);
			case LAMP_COLONY -> new ItemStack(Items.LEAD, 2);
			case VEILED_RESIDENT -> new ItemStack(Items.TINTED_GLASS, 4);
			case BELLWARDEN -> new ItemStack(Items.DRIED_KELP_BLOCK, 2);
			case ROOTED_ELDER -> new ItemStack(Items.BONE_MEAL, 12);
			default -> new ItemStack(Items.DRIED_KELP, 8);
		};
	}

	/** Whether the player holds, in the main hand, what this house takes as its custom. */
	public static boolean holdsCustom(Player player, CourtHouse house) {
		TagKey<Item> custom = customTag(house);
		if (custom == null) return CourtLife.holdsOffering(player);
		ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
		return !held.isEmpty() && held.is(custom);
	}

	/** The house's own custom tag, or null for a house that takes the Garden's food. */
	@javax.annotation.Nullable
	public static TagKey<Item> customTag(CourtHouse house) {
		return switch (house) {
			case HOLDING_STONE -> HOLDING_STONE_OFFERING;
			case SHARED_WAKE -> SHARED_WAKE_OFFERING;
			case WOVEN_CURRENT -> WOVEN_CURRENT_OFFERING;
			case VEILED_FOLD -> VEILED_FOLD_OFFERING;
			case STILL_BELL -> STILL_BELL_OFFERING;
			case LAST_MARROW -> LAST_MARROW_OFFERING;
			default -> null;
		};
	}

	/** The hub option that offers the house's custom ({@code hemomancy.dialogue.court.option.<key>}). */
	public static String customOption(CourtHouse house) {
		return switch (house) {
			case HOLDING_STONE -> "offer_stone";
			case SHARED_WAKE -> "offer_catch";
			case WOVEN_CURRENT -> "offer_line";
			case VEILED_FOLD -> "offer_ink";
			case STILL_BELL -> "offer_wool";
			case LAST_MARROW -> "offer_remains";
			default -> "offer";
		};
	}

	/** Handles one member event; true when something was given or recorded. */
	public static boolean handle(ServerPlayer player, CourtMemberEntity member, String eventId) {
		CourtRole role = member.role();
		CourtHouse house = role.house();
		if (CourtLife.isShunned(player)) return refuse(player);
		boolean turnedAway = CourtLife.turnsAway(player, house);
		return switch (eventId) {
			case CourtMemberDialogue.FOOD -> !turnedAway && role.offers(Offer.FOOD) && feed(player) || refuse(player);
			case CourtMemberDialogue.REST -> !turnedAway && role.offers(Offer.REST) && rest(player, member) || refuse(player);
			case CourtMemberDialogue.GIFT -> !turnedAway && role.offers(Offer.GIFT) && give(player, member) || refuse(player);
			case CourtMemberDialogue.OFFER -> !turnedAway && role.offers(Offer.CUSTOM) && offer(player, house) || refuse(player);
			case CourtMemberDialogue.AMENDS -> (role.offers(Offer.FOOD) || role.offers(Offer.LESSON))
					&& CourtLife.reconcile(player) || refuse(player);
			case CourtMemberDialogue.LESSON -> role.offers(Offer.LESSON) && CourtLife.teachLesson(player).taught();
			case CourtMemberDialogue.JOIN -> role.offers(Offer.JOIN) && CourtLife.tryJoin(player, house).changed();
			default -> false;
		};
	}

	private static boolean feed(ServerPlayer player) {
		if (!player.getFoodData().needsFood()) return false;
		player.getFoodData().eat(FOOD_HUNGER, FOOD_SATURATION);
		return true;
	}

	private static boolean rest(ServerPlayer player, CourtMemberEntity member) {
		if (!member.tryRest(player)) {
			player.displayClientMessage(Component.translatable("hemomancy.court.rest.recent").withStyle(ChatFormatting.GRAY), false);
			return false;
		}
		player.heal(REST_HEAL);
		player.removeEffect(MobEffects.WEAKNESS);
		return true;
	}

	private static boolean give(ServerPlayer player, CourtMemberEntity member) {
		CourtHouse house = member.role().house();
		if (CourtLife.progress(player).actCounted(house, GIFT_ACT)) return false;
		CourtLife.recordAct(player, house, GIFT_ACT, CourtStandingRules.ActKind.VISIT);
		ItemStack gift = gift(member.role());
		player.getInventory().add(gift);
		if (!gift.isEmpty()) member.spawnAtLocation(gift);
		return true;
	}

	/** The house's custom: one offering from the main hand. Accepted every time; standing counts it once. */
	private static boolean offer(ServerPlayer player, CourtHouse house) {
		if (!holdsCustom(player, house)) return false;
		player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
		CourtLife.recordAct(player, house, customAct(house), CourtStandingRules.ActKind.CUSTOM);
		return true;
	}

	private static boolean refuse(ServerPlayer player) {
		player.displayClientMessage(Component.translatable("hemomancy.court.refused").withStyle(ChatFormatting.GRAY), false);
		return false;
	}
}

package com.vincenthuto.hemomancy.common.entity.npc.court;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrain;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Every Brined Court role, shallow to deep: two per house plus a named lead at each Seat. A role is an NPC type, not a
 * character (the leads excepted). It fixes the house, the stage of the house strain the member wears, the body, how it
 * moves, whether it defends home against hostiles, what it can give, and which resident marks in the Seat and outpost
 * templates ({@code hemomancy:court_resident/<mark>}) it stands on.
 */
public enum CourtRole {
	GARDENER("gardener", CourtHouse.GIVING_LIGHT, Slot.A, 3, Body.HUMANOID, Behaviour.MOBILE, false,
			EnumSet.of(Offer.FOOD, Offer.REST, Offer.GIFT, Offer.CUSTOM, Offer.DIRECTIONS),
			List.of("gardener", "guest_host", "undercroft_tender")),
	ARBOR("arbor", CourtHouse.GIVING_LIGHT, Slot.B, 5, Body.HUMANOID, Behaviour.ANCHORED, false,
			EnumSet.of(Offer.REST, Offer.DIRECTIONS),
			List.of("arbor_anchored", "arbor_withdrawing", "undercroft_anchored", "undercroft_anchored_deep")),
	LIGHTKEEPER("lightkeeper", CourtHouse.GIVING_LIGHT, Slot.LEAD, 4, Body.HUMANOID, Behaviour.MOBILE, false,
			EnumSet.of(Offer.FOOD, Offer.LESSON, Offer.JOIN, Offer.DIRECTIONS), List.of("lamp_keeper")),
	TIDEWARDEN("tidewarden", CourtHouse.HOLDING_STONE, Slot.A, 3, Body.HUMANOID, Behaviour.MOBILE, true,
			EnumSet.of(Offer.REST, Offer.CUSTOM, Offer.DIRECTIONS), List.of("refuge_keeper")),
	BRACED_ELDER("braced_elder", CourtHouse.HOLDING_STONE, Slot.B, 5, Body.HUMANOID, Behaviour.ANCHORED, false,
			EnumSet.of(Offer.GIFT, Offer.JOIN, Offer.DIRECTIONS), List.of("holdfast_braced")),
	EDGE_SWIMMER("edge_swimmer", CourtHouse.SHARED_WAKE, Slot.A, 4, Body.ALTERED, Behaviour.SWIMMER, true,
			EnumSet.of(Offer.DIRECTIONS), List.of()),
	NURSERY_TENDER("nursery_tender", CourtHouse.SHARED_WAKE, Slot.B, 3, Body.ALTERED, Behaviour.SWIMMER, false,
			EnumSet.of(Offer.GIFT, Offer.JOIN, Offer.CUSTOM, Offer.DIRECTIONS), List.of("wake_tender")),
	LINESMAN("linesman", CourtHouse.WOVEN_CURRENT, Slot.A, 3, Body.HUMANOID, Behaviour.MOBILE, false,
			EnumSet.of(Offer.CUSTOM, Offer.DIRECTIONS), List.of("line_tender")),
	LAMP_COLONY("lamp_colony", CourtHouse.WOVEN_CURRENT, Slot.B, 5, Body.HUMANOID, Behaviour.ANCHORED, false,
			EnumSet.of(Offer.GIFT, Offer.JOIN, Offer.DIRECTIONS), List.of("pod_keeper")),
	HOST("host", CourtHouse.VEILED_FOLD, Slot.A, 3, Body.ALTERED, Behaviour.MOBILE, false,
			EnumSet.of(Offer.REST, Offer.CUSTOM, Offer.DIRECTIONS), List.of("cleft_host")),
	VEILED_RESIDENT("veiled_resident", CourtHouse.VEILED_FOLD, Slot.B, 4, Body.ALTERED, Behaviour.MOBILE, false,
			EnumSet.of(Offer.GIFT, Offer.JOIN), List.of("cleft_veiled")),
	BELLWARDEN("bellwarden", CourtHouse.STILL_BELL, Slot.A, 3, Body.ALTERED, Behaviour.MOBILE, false,
			EnumSet.of(Offer.REST, Offer.GIFT, Offer.CUSTOM, Offer.JOIN, Offer.DIRECTIONS), List.of("bell_keeper")),
	SLEEPER("sleeper", CourtHouse.STILL_BELL, Slot.B, 5, Body.ALTERED, Behaviour.SLEEPER, false,
			EnumSet.noneOf(Offer.class), List.of("bell_sleeper")),
	MARROW_TENDER("marrow_tender", CourtHouse.LAST_MARROW, Slot.A, 3, Body.HUMANOID, Behaviour.MOBILE, false,
			EnumSet.of(Offer.FOOD, Offer.REST, Offer.CUSTOM, Offer.DIRECTIONS), List.of("marrow_carer")),
	ROOTED_ELDER("rooted_elder", CourtHouse.LAST_MARROW, Slot.B, 5, Body.NEW_BODY, Behaviour.ANCHORED, false,
			EnumSet.of(Offer.GIFT, Offer.JOIN), List.of("marrow_rooted")),
	OFFERING_BEARER("offering_bearer", CourtHouse.KINDLING, Slot.A, 3, Body.HUMANOID, Behaviour.MOBILE, false,
			EnumSet.of(Offer.CUSTOM, Offer.DIRECTIONS), List.of("kindled_preparer")),
	KINDLED_ELDER("kindled_elder", CourtHouse.KINDLING, Slot.B, 5, Body.NEW_BODY, Behaviour.MOBILE, false,
			EnumSet.of(Offer.GIFT), List.of("kindled_elder")),
	HEAD_TENDER("head_tender", CourtHouse.KINDLING, Slot.LEAD, 4, Body.HUMANOID, Behaviour.MOBILE, false,
			EnumSet.of(Offer.GIFT, Offer.JOIN, Offer.DIRECTIONS), List.of());

	/** A house's first role, its second, or the Seat's named lead. */
	public enum Slot { A, B, LEAD }

	/** The jointed Court humanoid, the same rig with per-house body changes, or a generator-built body. */
	public enum Body { HUMANOID, ALTERED, NEW_BODY }

	/**
	 * How the member moves: walks its home ground, holds a fixed anchor (turn to face only), swims with the school,
	 * or lies sealed in its sleep.
	 */
	public enum Behaviour { MOBILE, ANCHORED, SWIMMER, SLEEPER }

	/** What a member can do for a visitor; trading is left for a later pass. */
	public enum Offer { FOOD, REST, GIFT, CUSTOM, LESSON, JOIN, DIRECTIONS }

	private final String id;
	private final CourtHouse house;
	private final Slot slot;
	private final int stage;
	private final Body body;
	private final Behaviour behaviour;
	private final boolean defends;
	private final Set<Offer> offers;
	private final List<String> marks;

	CourtRole(String id, CourtHouse house, Slot slot, int stage, Body body, Behaviour behaviour, boolean defends,
			Set<Offer> offers, List<String> marks) {
		this.id = id;
		this.house = house;
		this.slot = slot;
		this.stage = stage;
		this.body = body;
		this.behaviour = behaviour;
		this.defends = defends;
		this.offers = offers;
		this.marks = marks;
	}

	public String id() {
		return id;
	}

	/** The entity type id ({@code hemomancy:court_<role>}). */
	public String entityId() {
		return "court_" + id;
	}

	public CourtHouse house() {
		return house;
	}

	public Slot slot() {
		return slot;
	}

	/** The maturity of the house strain this role wears (0 Unfed .. 5 Primal). */
	public int stage() {
		return stage;
	}

	public Body body() {
		return body;
	}

	public Behaviour behaviour() {
		return behaviour;
	}

	public boolean anchored() {
		return behaviour == Behaviour.ANCHORED || behaviour == Behaviour.SLEEPER;
	}

	/** Whether the role fights hostiles near home (never players). */
	public boolean defends() {
		return defends;
	}

	public boolean offers(Offer offer) {
		return offers.contains(offer);
	}

	public Set<Offer> offers() {
		return offers;
	}

	/** The resident marks this role stands on. */
	public List<String> marks() {
		return marks;
	}

	/** The house strain this role wears. */
	public MorphlingStrain strain() {
		return MorphlingStrains.ALL.stream().filter(strain -> strain.house() == house).findFirst().orElseThrow();
	}

	/** The role's spoken name ("Gardener"). */
	public Component displayName() {
		return Component.translatable("entity.hemomancy." + entityId());
	}

	/** The role that stands on a resident mark, or null for a mark no role claims. */
	@Nullable
	public static CourtRole forMark(String mark) {
		for (CourtRole role : values()) if (role.marks.contains(mark)) return role;
		return null;
	}

	/** The role with this id, or null. */
	@Nullable
	public static CourtRole byId(String id) {
		for (CourtRole role : values()) if (role.id.equals(id)) return role;
		return null;
	}

	public static List<CourtRole> of(CourtHouse house) {
		return java.util.Arrays.stream(values()).filter(role -> role.house == house).toList();
	}
}

package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Which side each dialogue speaker belongs to, keyed by the speaker id the hub and {@code dialogue_inquiry/} use. The
 * side decides who may trade and which prices are legal (docs/archive/tracks/npc_trade/00-README.md). A Java table, not data: a
 * datapack can add offers, not change who trades with whom.
 */
public final class NpcTradeSides {
	public enum Side { HARBINGER, UNSTAINED, COURT, CIRCUS }

	/** The Ringmaster's speaker id. Performers speak as their role id ({@code CircusPerformerEntity.roleId()}). */
	public static final String CIRCUS_RINGMASTER = "circus_ringmaster";

	/**
	 * Outpost goods ({@code hemomancy:trade/stained}): an Unstained NPC refuses any stack in this tag, even from a player
	 * it would otherwise trade with, so an undecided player carrying blood-work hears about it at the card.
	 */
	public static final TagKey<Item> STAINED = ItemTags.create(Hemomancy.rloc("trade/stained"));

	private static final Map<String, Side> SIDES = build();

	private NpcTradeSides() {}

	private static Map<String, Side> build() {
		Map<String, Side> sides = new LinkedHashMap<>();
		for (String id : new String[] { "vicar", "alchemist", "artificer", "mnemonist", "hermit",
				"cicatrix_anchorite", "voyager", "votary_wayfarer" }) {
			sides.put(id, Side.HARBINGER);
		}
		for (String id : new String[] { "acolyte", "zealot", "guardian", "scout" }) {
			sides.put(id, Side.UNSTAINED);
		}
		for (CourtRole role : CourtRole.values()) sides.put(role.entityId(), Side.COURT);
		sides.put(CIRCUS_RINGMASTER, Side.CIRCUS);
		for (String id : new String[] { "circus_acrobat", "circus_fire_eater", "circus_knife_thrower",
				"circus_stilt_walker", "circus_strongman", "circus_beast_tamer", "circus_threadkeeper",
				"circus_understudy" }) {
			sides.put(id, Side.CIRCUS);
		}
		return Map.copyOf(sides);
	}

	public static Optional<Side> side(String speakerId) {
		return Optional.ofNullable(speakerId == null ? null : SIDES.get(speakerId));
	}

	public static boolean is(String speakerId, Side side) {
		return side(speakerId).orElse(null) == side;
	}

	/**
	 * Whether this speaker turns the player away before any card: an Unstained NPC refuses anyone at D1 or above
	 * (Q-2). Checked when cards are built and again when a trade is requested.
	 */
	public static boolean refuses(String speakerId, Player player) {
		return is(speakerId, Side.UNSTAINED) && ProgressionGates.hasDegree(player, 1);
	}

	/** Whether this speaker refuses {@code stack} as stained: an Unstained speaker and a stack in {@link #STAINED}. */
	public static boolean refusesStained(String speakerId, ItemStack stack) {
		return is(speakerId, Side.UNSTAINED) && !stack.isEmpty() && stack.is(STAINED);
	}

	/** The speaker's own refusal line for {@link #refuses}. */
	public static String refusalKey(String speakerId) {
		return "hemomancy.trade." + speakerId + ".refuse_harbinger";
	}

	/** Every speaker in the table. */
	public static Map<String, Side> all() {
		return SIDES;
	}
}

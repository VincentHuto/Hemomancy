package com.vincenthuto.hemomancy.common.capability.player.harbinger.court;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A player's durable Brined Court progress: whether the Court is known, which route told them first, whether a
 * Brined Votary has already dropped their one letter, and whether a Voyager has issued a Relay Chart. Read and change
 * it through {@link CourtDiscoveryProgress}; the owner's copy is synced through {@code PlayerStateSync}.
 *
 * <p>Court life (Brined Court Track 11) adds the player's house and whether their one switch is spent, a standing per
 * house with the acts already counted toward it, whether the Lightkeeper's cultivation lesson is taught, the Giving
 * Light transgression record, the Kindled Champion title and the usurp retry time. Change those through
 * {@link CourtLife}. The retry time lives here, not in {@code PlayerCooldowns}, because those reset on death and death
 * is how the Champion fight is lost.
 *
 * <p>Track 10 adds which Court presences (Marimo, Hydra) the player has had the first-sight scene of.
 */
public class CourtProgress implements INBTSerializable<CompoundTag> {
	private static final String KNOWN = "Known";
	private static final String KNOWN_BY = "KnownBy";
	private static final String LETTER_DROPPED = "LetterDropped";
	private static final String CHART_ISSUED = "ChartIssued";
	private static final String HOUSE = "House";
	private static final String SWITCH_USED = "SwitchUsed";
	private static final String STANDING = "Standing";
	private static final String STANDING_ACTS = "StandingActs";
	private static final String LESSON_TAUGHT = "LessonTaught";
	private static final String TRANSGRESSION = "Transgression";
	private static final String TRANSGRESSION_ACT = "Act";
	private static final String TRANSGRESSION_TIME = "GameTime";
	private static final String TRANSGRESSION_COUNT = "TransgressionCount";
	private static final String CHAMPION = "Champion";
	private static final String CHAMPION_SINCE = "ChampionSince";
	private static final String USURP_RETRY_AT = "UsurpRetryAt";
	private static final String PRESENCES_SEEN = "PresencesSeen";

	/** How the player first learned of the Court. Saved by name; append, never rename. */
	public enum Route {
		VOYAGER, LETTER, ALCHEMIST, FOUND;

		public String serializedName() {
			return name().toLowerCase(Locale.ROOT);
		}

		@Nullable
		public static Route fromSerializedName(String name) {
			for (Route route : values()) if (route.serializedName().equals(name)) return route;
			return null;
		}
	}

	private boolean known;
	@Nullable
	private Route knownBy;
	private boolean letterDropped;
	private boolean chartIssued;
	@Nullable
	private CourtHouse house;
	private boolean switchUsed;
	private final Map<CourtHouse, Integer> standing = new EnumMap<>(CourtHouse.class);
	private final Map<CourtHouse, Set<String>> standingActs = new EnumMap<>(CourtHouse.class);
	private boolean lessonTaught;
	/** The outstanding Giving Light transgression's act id, or null when there is none. */
	@Nullable
	private String transgressionAct;
	private long transgressionTime;
	private int transgressionCount;
	private boolean champion;
	private long championSince;
	private long usurpRetryAt;
	/** The Court presences (Marimo, Hydra) whose first-sight scene this player has had (Track 10). */
	private final Set<String> presencesSeen = new HashSet<>();

	public boolean known() {
		return known;
	}

	/** The first route; null while the Court is unknown. */
	@Nullable
	public Route knownBy() {
		return knownBy;
	}

	public boolean letterDropped() {
		return letterDropped;
	}

	public boolean chartIssued() {
		return chartIssued;
	}

	void setKnown(Route route) {
		known = true;
		knownBy = route;
	}

	void setLetterDropped(boolean letterDropped) {
		this.letterDropped = letterDropped;
	}

	void setChartIssued(boolean chartIssued) {
		this.chartIssued = chartIssued;
	}

	/** The player's house, or null before their one free entry. */
	@Nullable
	public CourtHouse house() {
		return house;
	}

	/** True once the one free switch is spent; membership is fixed after it. */
	public boolean switchUsed() {
		return switchUsed;
	}

	public CourtStandingRules.Standing standing(CourtHouse house) {
		return CourtStandingRules.Standing.ofLevel(standing.getOrDefault(house, 0));
	}

	/** Whether {@code actId} has already been counted toward standing with {@code house}. */
	public boolean actCounted(CourtHouse house, String actId) {
		Set<String> acts = standingActs.get(house);
		return acts != null && acts.contains(actId);
	}

	public boolean lessonTaught() {
		return lessonTaught;
	}

	public boolean hasTransgression() {
		return transgressionAct != null;
	}

	/** The act id of the outstanding Giving Light transgression, or null. */
	@Nullable
	public String transgressionAct() {
		return transgressionAct;
	}

	/** Game time of the outstanding transgression; 0 without one. */
	public long transgressionTime() {
		return transgressionTime;
	}

	/** Every Giving Light transgression ever recorded, reconciled or not. */
	public int transgressionCount() {
		return transgressionCount;
	}

	public boolean champion() {
		return champion;
	}

	/** Game time the Kindled Champion title was won; 0 without it. */
	public long championSince() {
		return championSince;
	}

	/** The game time before which a lost usurpation can't be retried; 0 when there's no wait. */
	public long usurpRetryAt() {
		return usurpRetryAt;
	}

	/** Whether this player has had {@code presence}'s first-sight scene. */
	public boolean seenPresence(String presence) {
		return presencesSeen.contains(presence);
	}

	/** Records {@code presence}'s first sight; false when it was already recorded. */
	boolean markPresenceSeen(String presence) {
		return presencesSeen.add(presence);
	}

	void setHouse(@Nullable CourtHouse house, boolean switchUsed) {
		this.house = house;
		this.switchUsed = switchUsed;
	}

	void setStanding(CourtHouse house, CourtStandingRules.Standing value) {
		if (value == CourtStandingRules.Standing.STRANGER) standing.remove(house);
		else standing.put(house, value.level());
	}

	void countAct(CourtHouse house, String actId) {
		standingActs.computeIfAbsent(house, ignored -> new HashSet<>()).add(actId);
	}

	void setLessonTaught(boolean lessonTaught) {
		this.lessonTaught = lessonTaught;
	}

	void recordTransgression(String actId, long gameTime) {
		transgressionAct = actId;
		transgressionTime = gameTime;
		transgressionCount++;
	}

	void clearTransgression() {
		transgressionAct = null;
		transgressionTime = 0L;
	}

	void setChampion(boolean champion, long since) {
		this.champion = champion;
		this.championSince = champion ? since : 0L;
	}

	void setUsurpRetryAt(long gameTime) {
		usurpRetryAt = Math.max(0L, gameTime);
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean(KNOWN, known);
		if (knownBy != null) tag.putString(KNOWN_BY, knownBy.serializedName());
		tag.putBoolean(LETTER_DROPPED, letterDropped);
		tag.putBoolean(CHART_ISSUED, chartIssued);
		if (house != null) tag.putString(HOUSE, house.id());
		tag.putBoolean(SWITCH_USED, switchUsed);
		CompoundTag standingTag = new CompoundTag();
		standing.forEach((key, level) -> standingTag.putInt(key.id(), level));
		tag.put(STANDING, standingTag);
		CompoundTag actsTag = new CompoundTag();
		standingActs.forEach((key, acts) -> {
			ListTag list = new ListTag();
			acts.stream().sorted().forEach(act -> list.add(StringTag.valueOf(act)));
			actsTag.put(key.id(), list);
		});
		tag.put(STANDING_ACTS, actsTag);
		tag.putBoolean(LESSON_TAUGHT, lessonTaught);
		if (transgressionAct != null) {
			CompoundTag transgression = new CompoundTag();
			transgression.putString(TRANSGRESSION_ACT, transgressionAct);
			transgression.putLong(TRANSGRESSION_TIME, transgressionTime);
			tag.put(TRANSGRESSION, transgression);
		}
		tag.putInt(TRANSGRESSION_COUNT, transgressionCount);
		tag.putBoolean(CHAMPION, champion);
		if (champion) tag.putLong(CHAMPION_SINCE, championSince);
		if (usurpRetryAt > 0L) tag.putLong(USURP_RETRY_AT, usurpRetryAt);
		if (!presencesSeen.isEmpty()) {
			ListTag seen = new ListTag();
			presencesSeen.stream().sorted().forEach(presence -> seen.add(StringTag.valueOf(presence)));
			tag.put(PRESENCES_SEEN, seen);
		}
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		known = tag.getBoolean(KNOWN);
		knownBy = known ? Route.fromSerializedName(tag.getString(KNOWN_BY)) : null;
		if (known && knownBy == null) knownBy = Route.FOUND;
		letterDropped = tag.getBoolean(LETTER_DROPPED);
		chartIssued = tag.getBoolean(CHART_ISSUED);
		house = tag.contains(HOUSE, Tag.TAG_STRING) ? CourtHouse.byId(tag.getString(HOUSE)) : null;
		switchUsed = tag.getBoolean(SWITCH_USED);
		standing.clear();
		CompoundTag standingTag = tag.getCompound(STANDING);
		for (String key : standingTag.getAllKeys()) {
			CourtHouse standingHouse = CourtHouse.byId(key);
			if (standingHouse != null) setStanding(standingHouse, CourtStandingRules.Standing.ofLevel(standingTag.getInt(key)));
		}
		standingActs.clear();
		CompoundTag actsTag = tag.getCompound(STANDING_ACTS);
		for (String key : actsTag.getAllKeys()) {
			CourtHouse actHouse = CourtHouse.byId(key);
			if (actHouse == null) continue;
			ListTag list = actsTag.getList(key, Tag.TAG_STRING);
			for (int i = 0; i < list.size(); i++) countAct(actHouse, list.getString(i));
		}
		lessonTaught = tag.getBoolean(LESSON_TAUGHT);
		if (tag.contains(TRANSGRESSION, Tag.TAG_COMPOUND)) {
			CompoundTag transgression = tag.getCompound(TRANSGRESSION);
			transgressionAct = transgression.getString(TRANSGRESSION_ACT);
			transgressionTime = transgression.getLong(TRANSGRESSION_TIME);
		} else {
			clearTransgression();
		}
		transgressionCount = tag.getInt(TRANSGRESSION_COUNT);
		champion = tag.getBoolean(CHAMPION);
		championSince = champion ? tag.getLong(CHAMPION_SINCE) : 0L;
		usurpRetryAt = Math.max(0L, tag.getLong(USURP_RETRY_AT));
		presencesSeen.clear();
		ListTag seen = tag.getList(PRESENCES_SEEN, Tag.TAG_STRING);
		for (int i = 0; i < seen.size(); i++) presencesSeen.add(seen.getString(i));
	}
}

package com.vincenthuto.hemomancy.common.capability.player.harbinger.circus;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Set;
import java.util.TreeSet;

/**
 * A player's durable circus progress: pavilion discovery, acclimation, route, challenges, milestones and the
 * apprenticeship record. Read it through {@link CircusPlayerProgress}, {@code CircusDiscoveryProgress} and
 * {@link CircusApprenticeshipProgress}. The live act timers stay with {@link CircusPerformanceController}.
 */
public class CircusProgress implements INBTSerializable<CompoundTag> {
	private static final String DISCOVERED = "Discovered";
	private static final String ACCLIMATION = "Acclimation";
	private static final String ROUTE = "Route";
	private static final String ROUTE_REPAIRED = "RouteRepaired";
	private static final String CHALLENGES = "Challenges";
	private static final String MILESTONES = "Milestones";
	private static final String APPRENTICESHIP = "Apprenticeship";

	private boolean discovered;
	private int acclimation;
	private String route = "";
	private boolean routeRepaired;
	private int challenges;
	private final Set<String> milestones = new TreeSet<>();
	private CompoundTag apprenticeship = new CompoundTag();

	public boolean discovered() {
		return discovered;
	}

	public void setDiscovered(boolean discovered) {
		this.discovered = discovered;
	}

	/** The raw stored score; {@link CircusPlayerProgress#acclimation} clamps it, as the root key was. */
	int acclimation() {
		return acclimation;
	}

	void setAcclimation(int acclimation) {
		this.acclimation = acclimation;
	}

	String route() {
		return route;
	}

	void setRoute(String route) {
		this.route = route == null ? "" : route;
	}

	boolean routeRepaired() {
		return routeRepaired;
	}

	void setRouteRepaired(boolean routeRepaired) {
		this.routeRepaired = routeRepaired;
	}

	int challenges() {
		return challenges;
	}

	void setChallenges(int challenges) {
		this.challenges = challenges;
	}

	boolean hasMilestone(String milestone) {
		return milestones.contains(milestone);
	}

	void addMilestone(String milestone) {
		milestones.add(milestone);
	}

	/** The apprenticeship record itself, edited in place by {@link CircusApprenticeshipProgress}. */
	CompoundTag apprenticeship() {
		return apprenticeship;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean(DISCOVERED, discovered);
		tag.putInt(ACCLIMATION, acclimation);
		tag.putString(ROUTE, route);
		tag.putBoolean(ROUTE_REPAIRED, routeRepaired);
		tag.putInt(CHALLENGES, challenges);
		ListTag milestoneList = new ListTag();
		milestones.forEach(milestone -> milestoneList.add(StringTag.valueOf(milestone)));
		tag.put(MILESTONES, milestoneList);
		tag.put(APPRENTICESHIP, apprenticeship.copy());
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		discovered = tag.getBoolean(DISCOVERED);
		acclimation = tag.getInt(ACCLIMATION);
		route = tag.getString(ROUTE);
		routeRepaired = tag.getBoolean(ROUTE_REPAIRED);
		challenges = tag.getInt(CHALLENGES);
		milestones.clear();
		ListTag milestoneList = tag.getList(MILESTONES, Tag.TAG_STRING);
		for (int i = 0; i < milestoneList.size(); i++) milestones.add(milestoneList.getString(i));
		apprenticeship = tag.getCompound(APPRENTICESHIP).copy();
	}
}

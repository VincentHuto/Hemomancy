package com.vincenthuto.hemomancy.common.capability.player.harbinger.circus;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * A player's durable circus progress: pavilion discovery, acclimation, route, challenges, milestones and the
 * apprenticeship record, the finale rewards still owed and the act the player is performing. Read it through
 * {@link CircusPlayerProgress}, {@code CircusDiscoveryProgress}, {@link CircusApprenticeshipProgress} and
 * {@code CircusPerformanceController}. The act is saved with the player (it survives a relog) but is cleared on death
 * by {@code CircusPlayerProgress.onPlayerClone}. Whether the player stands inside a pavilion is never saved; it and
 * the acclimation score raise the perception dirty flag that {@code PlayerStateSync} flushes.
 */
public class CircusProgress implements INBTSerializable<CompoundTag> {
	private static final String DISCOVERED = "Discovered";
	private static final String ACCLIMATION = "Acclimation";
	private static final String ROUTE = "Route";
	private static final String ROUTE_REPAIRED = "RouteRepaired";
	private static final String CHALLENGES = "Challenges";
	private static final String MILESTONES = "Milestones";
	private static final String APPRENTICESHIP = "Apprenticeship";
	private static final String PENDING_TOPPER = "PendingTopper";
	private static final String PENDING_RIPPER_MEMORY = "PendingRipperMemory";
	private static final String ACT = "Act";
	private static final String ACT_CHALLENGE = "Challenge";
	private static final String ACT_TICKS = "Ticks";
	private static final String ACT_ORIGIN = "Origin";
	private static final String ACT_SUBJECT = "Subject";

	private boolean discovered;
	private int acclimation;
	private String route = "";
	private boolean routeRepaired;
	private int challenges;
	private final Set<String> milestones = new TreeSet<>();
	private CompoundTag apprenticeship = new CompoundTag();
	private int pendingTopper;
	private int pendingRipperMemory;
	/** The act being performed, as challenge index + 1; 0 when no act runs. */
	private int actChallenge;
	private int actTicks;
	private long actOrigin;
	private UUID actSubject;
	private transient boolean inside;
	private transient boolean perceptionDirty;

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
		if (this.acclimation != acclimation) perceptionDirty = true;
		this.acclimation = acclimation;
	}

	/** Whether the player stood inside a pavilion at the last discovery check; never saved. */
	public boolean inside() {
		return inside;
	}

	void setInside(boolean inside) {
		if (this.inside != inside) perceptionDirty = true;
		this.inside = inside;
	}

	public boolean isPerceptionDirty() {
		return perceptionDirty;
	}

	void clearPerceptionDirty() {
		perceptionDirty = false;
	}

	int pendingTopper() {
		return pendingTopper;
	}

	void setPendingTopper(int count) {
		pendingTopper = Math.max(0, count);
	}

	int pendingRipperMemory() {
		return pendingRipperMemory;
	}

	void setPendingRipperMemory(int count) {
		pendingRipperMemory = Math.max(0, count);
	}

	/** The running act's challenge index, or -1 when none runs. */
	public int actChallenge() {
		return actChallenge - 1;
	}

	public int actTicks() {
		return actTicks;
	}

	public void setActTicks(int ticks) {
		actTicks = ticks;
	}

	public long actOrigin() {
		return actOrigin;
	}

	/** The act's watched subject (the attention trial's spider), or {@code null}. */
	public UUID actSubject() {
		return actSubject;
	}

	public void setActSubject(UUID subject) {
		actSubject = subject;
	}

	public void beginAct(int challenge, long origin) {
		actChallenge = challenge + 1;
		actTicks = 0;
		actOrigin = origin;
		actSubject = null;
	}

	public void clearAct() {
		actChallenge = 0;
		actTicks = 0;
		actOrigin = 0L;
		actSubject = null;
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
		tag.putInt(PENDING_TOPPER, pendingTopper);
		tag.putInt(PENDING_RIPPER_MEMORY, pendingRipperMemory);
		if (actChallenge > 0) {
			CompoundTag act = new CompoundTag();
			act.putInt(ACT_CHALLENGE, actChallenge);
			act.putInt(ACT_TICKS, actTicks);
			act.putLong(ACT_ORIGIN, actOrigin);
			if (actSubject != null) act.putUUID(ACT_SUBJECT, actSubject);
			tag.put(ACT, act);
		}
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
		pendingTopper = Math.max(0, tag.getInt(PENDING_TOPPER));
		pendingRipperMemory = Math.max(0, tag.getInt(PENDING_RIPPER_MEMORY));
		CompoundTag act = tag.getCompound(ACT);
		actChallenge = Math.max(0, act.getInt(ACT_CHALLENGE));
		actTicks = act.getInt(ACT_TICKS);
		actOrigin = act.getLong(ACT_ORIGIN);
		actSubject = act.hasUUID(ACT_SUBJECT) ? act.getUUID(ACT_SUBJECT) : null;
	}
}

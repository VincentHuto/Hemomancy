package com.vincenthuto.hemomancy.common.entity.npc.circus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;

public final class CircusPavilionSavedData extends SavedData {
	private static final String DATA_NAME = "hemomancy_circus_pavilions";
	private static final Factory<CircusPavilionSavedData> FACTORY =
			new Factory<>(CircusPavilionSavedData::new, CircusPavilionSavedData::load, null);
	private final Map<String, Site> sites = new HashMap<>();

	public static CircusPavilionSavedData get(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public Site site(ServerLevel level, BlockPos origin) {
		return sites.computeIfAbsent(key(level, origin), ignored -> new Site(origin));
	}

	public boolean hasSite(ServerLevel level, BlockPos origin) {
		return sites.containsKey(key(level, origin));
	}

	public void removeSite(ServerLevel level, BlockPos origin) {
		if (sites.remove(key(level, origin)) != null) setDirty();
	}

	/** Whether a pavilion whose carousel stood inside {@code box} has had its ruin laid (or is having it laid). */
	public boolean isRuinWithin(ServerLevel level, net.minecraft.world.level.levelgen.structure.BoundingBox box) {
		String prefix = level.dimension().location() + "|";
		return sites.entrySet().stream().anyMatch(entry -> entry.getKey().startsWith(prefix)
				&& entry.getValue().afterState == CircusSiteStateService.State.RUIN
				&& box.isInside(entry.getValue().origin));
	}

    public boolean isCompletedNear(ServerLevel level, BlockPos position) {
        String prefix = level.dimension().location() + "|";
        return sites.entrySet().stream().anyMatch(entry -> entry.getKey().startsWith(prefix)
                && (entry.getValue().outcome != CircusPavilionStateRules.Outcome.NEUTRAL || entry.getValue().vacant)
                && Math.abs(entry.getValue().origin.getX() - position.getX()) < 80
                && Math.abs(entry.getValue().origin.getZ() - position.getZ()) < 80);
    }

	/**
	 * Whether the ring whose ground {@code home} is on was settled as a ruin: its finale troupe leaves when it next ticks
	 * (the settlement is usually laid while the site is unloaded).
	 */
	public boolean isRuinedRingAt(ServerLevel level, BlockPos home) {
		String prefix = level.dimension().location() + "|";
		net.minecraft.world.phys.Vec3 at = net.minecraft.world.phys.Vec3.atBottomCenterOf(home);
		return sites.entrySet().stream().anyMatch(entry -> entry.getKey().startsWith(prefix)
				&& entry.getValue().outcome == CircusPavilionStateRules.Outcome.RUIN
				&& CircusArena.of(entry.getValue().origin).holds(at));
	}

	public boolean begin(ServerLevel level, BlockPos origin, UUID owner, CircusRouteRules.Route route) {
		return begin(level, origin, owner, route, CircusPavilionStateRules.Phase.RAFTERS);
	}

	public boolean beginPerformance(ServerLevel level, BlockPos origin, UUID owner,
			CircusRouteRules.Route route) {
		return begin(level, origin, owner, route, CircusPavilionStateRules.Phase.PERFORMANCE);
	}

	private boolean begin(ServerLevel level, BlockPos origin, UUID owner, CircusRouteRules.Route route,
			CircusPavilionStateRules.Phase phase) {
		Site site = site(level, origin);
		if (!CircusPavilionStateRules.canBegin(site.activeOwner, site.outcome)) return false;
		site.activeOwner = owner;
		CircusPavilionStateRules.beginAct(owner);
		site.route = route;
		site.phase = phase;
		site.severedMask = 0;
		site.brokenMask = 0;
		setDirty();
		return true;
	}

	/** D-36: a site whose Ringmaster is gone is vacant, and the Waybill passes it by like a finished one. */
	public void setVacant(ServerLevel level, BlockPos origin, boolean vacant) {
		Site site = site(level, origin);
		if (site.vacant == vacant) return;
		site.vacant = vacant;
		setDirty();
	}

	public void setPhase(ServerLevel level, BlockPos origin, CircusPavilionStateRules.Phase phase) {
		site(level, origin).phase = phase;
		setDirty();
	}

	public void setCarouselProgress(ServerLevel level, BlockPos origin, int severedMask, int brokenMask) {
		Site site = site(level, origin);
		site.severedMask = severedMask & 7;
		site.brokenMask = brokenMask & 7;
		setDirty();
	}

	public boolean complete(ServerLevel level, BlockPos origin, UUID owner,
			CircusPavilionStateRules.Outcome outcome) {
		Site site = site(level, origin);
		if (!CircusPavilionStateRules.canAct(site.activeOwner, owner)
				|| site.outcome != CircusPavilionStateRules.Outcome.NEUTRAL) return false;
		site.activeOwner = null;
		site.completionOwner = owner;
		site.outcome = outcome;
		site.phase = CircusPavilionStateRules.Phase.COMPLETE;
		setDirty();
		return true;
	}

	public void reset(ServerLevel level, BlockPos origin, UUID owner) {
		Site site = site(level, origin);
		if (!CircusPavilionStateRules.canAct(site.activeOwner, owner)) return;
		site.activeOwner = null;
		site.route = CircusRouteRules.Route.NEUTRAL;
		site.phase = CircusPavilionStateRules.resetPhase(site.outcome);
		site.severedMask = 0;
		site.brokenMask = 0;
		setDirty();
	}

	public void resetOwned(UUID owner) {
		boolean changed = false;
		for (Site site : sites.values()) {
			if (!owner.equals(site.activeOwner) || site.phase == CircusPavilionStateRules.Phase.CURTAIN) continue;
			site.activeOwner = null;
			site.route = CircusRouteRules.Route.NEUTRAL;
			site.phase = CircusPavilionStateRules.resetPhase(site.outcome);
			site.severedMask = 0;
			site.brokenMask = 0;
			changed = true;
		}
		if (changed) setDirty();
	}

	/**
	 * D-44 (chunk 15 T11): records the after-state a settled site is laid into, the pavilion's rotation and the chunks
	 * still to lay; {@link CircusSiteStateService} works the chunks off as they load.
	 */
	public void beginAfterState(ServerLevel level, BlockPos origin, CircusSiteStateService.State state, int rotation,
			java.util.Set<Long> chunks, String holderName) {
		Site site = site(level, origin);
		site.afterState = state;
		site.afterRotation = rotation;
		site.pendingChunks.clear();
		site.pendingChunks.addAll(chunks);
		site.holderName = holderName;
		setDirty();
	}

	/** One chunk of a site's after-state is laid. */
	public void afterChunkLaid(ServerLevel level, BlockPos origin, long chunk) {
		if (site(level, origin).pendingChunks.remove(chunk)) setDirty();
	}

	/** The sites in {@code level}'s dimension with after-state chunks still to lay. */
	public java.util.List<Site> sitesLayingAfterState(ServerLevel level) {
		String prefix = level.dimension().location() + "|";
		return sites.entrySet().stream().filter(entry -> entry.getKey().startsWith(prefix)
				&& entry.getValue().afterState != null && !entry.getValue().pendingChunks.isEmpty())
				.map(Map.Entry::getValue).toList();
	}

	private static String key(ServerLevel level, BlockPos origin) {
		return level.dimension().location() + "|" + origin.asLong();
	}

	public static CircusPavilionSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		CircusPavilionSavedData data = new CircusPavilionSavedData();
		ListTag entries = tag.getList("Sites", Tag.TAG_COMPOUND);
		for (int i = 0; i < entries.size(); i++) {
			CompoundTag entry = entries.getCompound(i);
			Site site = Site.load(entry);
			data.sites.put(entry.getString("Key"), site);
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag entries = new ListTag();
		sites.forEach((key, site) -> {
			CompoundTag entry = site.save();
			entry.putString("Key", key);
			entries.add(entry);
		});
		tag.put("Sites", entries);
		return tag;
	}

	public static final class Site {
		private final BlockPos origin;
		private UUID activeOwner;
		private UUID completionOwner;
		private CircusRouteRules.Route route = CircusRouteRules.Route.NEUTRAL;
		private CircusPavilionStateRules.Outcome outcome = CircusPavilionStateRules.Outcome.NEUTRAL;
		private CircusPavilionStateRules.Phase phase = CircusPavilionStateRules.Phase.IDLE;
		private int severedMask;
		private int brokenMask;
		private boolean vacant;
		private CircusSiteStateService.State afterState;
		private int afterRotation;
		private final java.util.Set<Long> pendingChunks = new java.util.LinkedHashSet<>();
		private String holderName;

		private Site(BlockPos origin) { this.origin = origin.immutable(); }

		public BlockPos origin() { return origin; }
		public UUID activeOwner() { return activeOwner; }
		public UUID completionOwner() { return completionOwner; }
		public CircusRouteRules.Route route() { return route; }
		public CircusPavilionStateRules.Outcome outcome() { return outcome; }
		public CircusPavilionStateRules.Phase phase() { return phase; }
		public int severedMask() { return severedMask; }
		public int brokenMask() { return brokenMask; }
		public boolean vacant() { return vacant; }
		public CircusSiteStateService.State afterState() { return afterState; }
		public int afterRotation() { return afterRotation; }
		public java.util.Set<Long> pendingChunks() { return java.util.Collections.unmodifiableSet(pendingChunks); }
		/** The successor's name, kept when a Succession settles the site. */
		public String holderName() { return holderName; }

		private CompoundTag save() {
			CompoundTag tag = new CompoundTag();
			tag.putLong("Origin", origin.asLong());
			if (activeOwner != null) tag.putUUID("ActiveOwner", activeOwner);
			if (completionOwner != null) tag.putUUID("CompletionOwner", completionOwner);
			tag.putString("Route", route.serializedName());
			tag.putString("Outcome", outcome.name());
			tag.putString("Phase", phase.name());
			tag.putInt("Severed", severedMask);
			tag.putInt("Broken", brokenMask);
			if (vacant) tag.putBoolean("Vacant", true);
			if (afterState != null) {
				tag.putString("AfterState", afterState.name());
				tag.putInt("AfterRotation", afterRotation);
				tag.putLongArray("AfterPending", pendingChunks.stream().mapToLong(Long::longValue).toArray());
			}
			if (holderName != null) tag.putString("HolderName", holderName);
			return tag;
		}

		private static Site load(CompoundTag tag) {
			Site site = new Site(BlockPos.of(tag.getLong("Origin")));
			if (tag.hasUUID("ActiveOwner")) site.activeOwner = tag.getUUID("ActiveOwner");
			if (tag.hasUUID("CompletionOwner")) site.completionOwner = tag.getUUID("CompletionOwner");
			site.route = CircusRouteRules.Route.fromSerializedName(tag.getString("Route"));
			try { site.outcome = CircusPavilionStateRules.Outcome.valueOf(tag.getString("Outcome")); }
			catch (IllegalArgumentException ignored) { }
			try { site.phase = CircusPavilionStateRules.Phase.valueOf(tag.getString("Phase")); }
			catch (IllegalArgumentException ignored) { }
			site.severedMask = tag.getInt("Severed") & 7;
			site.brokenMask = tag.getInt("Broken") & 7;
			site.vacant = tag.getBoolean("Vacant");
			try { site.afterState = tag.contains("AfterState") ? CircusSiteStateService.State.valueOf(tag.getString("AfterState")) : null; }
			catch (IllegalArgumentException ignored) { }
			site.afterRotation = tag.getInt("AfterRotation");
			for (long chunk : tag.getLongArray("AfterPending")) site.pendingChunks.add(chunk);
			if (tag.contains("HolderName")) site.holderName = tag.getString("HolderName");
			return site;
		}
	}
}

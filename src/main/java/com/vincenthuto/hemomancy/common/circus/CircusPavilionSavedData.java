package com.vincenthuto.hemomancy.common.circus;

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
		site.route = route;
		site.phase = phase;
		site.severedMask = 0;
		site.brokenMask = 0;
		setDirty();
		return true;
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
			if (!owner.equals(site.activeOwner)) continue;
			site.activeOwner = null;
			site.route = CircusRouteRules.Route.NEUTRAL;
			site.phase = CircusPavilionStateRules.resetPhase(site.outcome);
			site.severedMask = 0;
			site.brokenMask = 0;
			changed = true;
		}
		if (changed) setDirty();
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

		private Site(BlockPos origin) { this.origin = origin.immutable(); }

		public BlockPos origin() { return origin; }
		public UUID activeOwner() { return activeOwner; }
		public UUID completionOwner() { return completionOwner; }
		public CircusRouteRules.Route route() { return route; }
		public CircusPavilionStateRules.Outcome outcome() { return outcome; }
		public CircusPavilionStateRules.Phase phase() { return phase; }
		public int severedMask() { return severedMask; }
		public int brokenMask() { return brokenMask; }

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
			return site;
		}
	}
}

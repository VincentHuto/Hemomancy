package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolume;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The blood a block holds. {@link #getBloodVolume()} is the fresh blood, the only part that is fuel; {@link #clotted()}
 * is clot, which takes room in the tank until it is scraped out as Formations or woken by a reviving pour
 * ({@link #revive}). Nothing ticks: every read through
 * {@link ReservoirBlood#settled} calls {@link #settle} with the level's time, which clots what the time since the last
 * feeding says should have clotted. The rule is path-independent, so an unloaded chunk clots correctly when it loads.
 */
public class BlockBloodVolume extends BloodVolume {
	/** Not yet stamped. Game time can be small, and a backdated stamp negative, so no ordinary value is spare. */
	private static final long UNSET = Long.MIN_VALUE;
	private double clotted;
	/** Game time of the last feeding; unset until the first settle stamps it. */
	private long lastFed = UNSET;
	/** Game time of the last settle; unset before the first. */
	private long settledAt = UNSET;
	/**
	 * Game time blood last left the tank ({@link #drain}, {@link #subtractBloodVolume}): machine work, Absorption, a drip,
	 * a gutter. Clotting and a living vein's own move do not set it. A vein runs toward a tank drawn lately (CL-14).
	 */
	private long lastDrawn = UNSET;
	/** Who placed the block; null for blocks placed before Track 3, by structures, or by machines. */
	@Nullable private UUID owner;
	/**
	 * Fresh mL each player poured in with Blood Projection and has not drawn back (PT-1b). A player's draw takes from
	 * their own share first, and that part never counts toward the first Degree's absorption proof.
	 */
	private final java.util.Map<UUID, Double> poured = new java.util.HashMap<>();
	/** The suture's last state from the routing pass, so the machine's screen can say why it stalled. */
	private ReservoirReadout.Link link = ReservoirReadout.Link.NONE;

	public ReservoirReadout.Link link() {
		return link;
	}

	/** Sets the suture's state; returns whether it changed (and the block needs resending). */
	public boolean setLink(ReservoirReadout.Link link) {
		ReservoirReadout.Link next = link == null ? ReservoirReadout.Link.NONE : link;
		if (next == this.link) return false;
		this.link = next;
		return true;
	}

	/**
	 * Brings the volume up to {@code gameTime}. {@code held} means something alive keeps it (a living vessel): nothing
	 * clots and the blood stays freshly fed.
	 */
	public void settle(long gameTime, boolean held) {
		if (settledAt == UNSET) {
			settledAt = gameTime;
			if (lastFed == UNSET) lastFed = gameTime;
			return;
		}
		if (gameTime <= settledAt) return;
		if (held) {
			lastFed = gameTime;
			settledAt = gameTime;
			return;
		}
		double moved = CoagulationRules.clotBetween(getBloodVolume(), settledAt - lastFed, gameTime - lastFed);
		settledAt = gameTime;
		if (moved > 0.0D) {
			setBloodVolume(Math.max(0.0D, getBloodVolume() - moved));
			clotted += moved;
		}
	}

	/**
	 * Brings the volume up to {@code gameTime} with its clock stopped (a station held by an upgrade rite, NQ-01-6):
	 * nothing clots and the blood comes out exactly as old as it went in.
	 */
	public void pause(long gameTime) {
		if (settledAt == UNSET) {
			settle(gameTime, false);
			return;
		}
		if (gameTime <= settledAt) return;
		if (lastFed != UNSET) lastFed += gameTime - settledAt;
		settledAt = gameTime;
	}

	/** The tank's full size, clot included. */
	public double capacity() {
		return super.getMaxBloodVolume();
	}

	/** Room for fresh blood: the tank less its clot. */
	@Override
	public double getMaxBloodVolume() {
		return Math.max(0.0D, super.getMaxBloodVolume() - clotted);
	}

	@Override
	public void setMaxBloodVolume(double points) {
		super.setMaxBloodVolume(points);
		clotted = Math.clamp(clotted, 0.0D, Math.max(0.0D, points));
	}

	public double clotted() {
		return clotted;
	}

	/** Pours clot in (a clotted flask emptied into a machine). Returns what fit. */
	public double addClot(double amount) {
		double room = Math.max(0.0D, capacity() - clotted - getBloodVolume());
		double added = Math.clamp(amount, 0.0D, room);
		clotted += added;
		return added;
	}

	/** Takes up to {@code amount} of clot out and returns it. */
	public double scrapeClot(double amount) {
		double taken = Math.clamp(amount, 0.0D, clotted);
		clotted -= taken;
		if (clotted <= CoagulationRules.CLOT_EPSILON) clotted = 0.0D;
		return taken;
	}

	public long lastFed() {
		return lastFed;
	}

	/** When blood last left the tank, as of the settle before it; {@code Long.MIN_VALUE} when never. */
	public long lastDrawn() {
		return lastDrawn;
	}

	@Override
	public boolean drain(double points) {
		if (points > 0.0D) stampDrawn();
		return super.drain(points);
	}

	@Override
	public void subtractBloodVolume(double points) {
		if (points > 0.0D) stampDrawn();
		super.subtractBloodVolume(points);
	}

	/**
	 * Fresh blood taken out along a living vein: up to {@code points}, never below empty, without counting as a draw,
	 * so the tank a vein empties does not start drawing back. Returns what was taken.
	 */
	public double takeAlongVein(double points) {
		double taken = Math.clamp(points, 0.0D, getBloodVolume());
		setBloodVolume(getBloodVolume() - taken);
		return taken;
	}

	private void stampDrawn() {
		if (settledAt != UNSET) lastDrawn = settledAt;
	}

	/** 1 when just fed, 0 at the end of the fresh window; for display. */
	public double freshness(long gameTime) {
		return lastFed == UNSET ? 1.0D : CoagulationRules.freshness(gameTime - lastFed);
	}

	/** Test and fixture hook: as if the last feeding was {@code ticksAgo} ago and the volume settled then. */
	public void backdate(long gameTime, long ticksAgo) {
		lastFed = gameTime - Math.max(0L, ticksAgo);
		settledAt = lastFed;
	}

	/** Records {@code amount} mL poured in by {@code player}. */
	public void recordPoured(UUID player, double amount) {
		if (player == null || amount <= 0.0D) return;
		poured.merge(player, amount, Double::sum);
		trimPoured();
	}

	/** The player's own poured blood among {@code drawn} mL they just drew out; their share shrinks by it. */
	public double takePoured(UUID player, double drawn) {
		double share = player == null ? 0.0D : poured.getOrDefault(player, 0.0D);
		double own = Math.clamp(drawn, 0.0D, share);
		if (own > 0.0D) {
			if (share - own <= CoagulationRules.CLOT_EPSILON) poured.remove(player);
			else poured.put(player, share - own);
		}
		trimPoured();
		return own;
	}

	public double poured(UUID player) {
		return poured.getOrDefault(player, 0.0D);
	}

	/** No share is larger than the fresh blood left: what clotted or was drawn by others is gone from it. */
	private void trimPoured() {
		double fresh = getBloodVolume();
		poured.replaceAll((player, share) -> Math.min(share, fresh));
		poured.values().removeIf(share -> share <= CoagulationRules.CLOT_EPSILON);
	}

	@Nullable
	public UUID owner() {
		return owner;
	}

	public void setOwner(@Nullable UUID owner) {
		this.owner = owner;
	}

	/** Fresh blood poured into free room. It does not wake clot, and the tank's age blends by volume (NQ-01-3, NQ-01-4). */
	@Override
	public boolean fill(double points) {
		if (points <= 0.0D) return true;
		boolean fits = !wouldOverflow(points);
		addFresh(fits ? points : Math.max(0.0D, getMaxBloodVolume() - getBloodVolume()), 0L);
		return fits;
	}

	@Override
	public void addBloodVolume(double points) {
		if (points > 0.0D) blendAge(points, 0L);
		super.addBloodVolume(points);
	}

	/**
	 * A courier's delivery (NQ-01-5): blood {@code age} ticks old, into free room, never waking clot. Returns what fit.
	 */
	public double fillAged(double points, long age) {
		double accepted = Math.clamp(points, 0.0D, Math.max(0.0D, getMaxBloodVolume() - getBloodVolume()));
		addFresh(accepted, age);
		return accepted;
	}

	/** What a reviving pour of {@code offered} mL would do, with nothing changed. */
	public CoagulationRules.Pour pourOf(double offered, double share, double yield) {
		return CoagulationRules.pour(offered, getMaxBloodVolume() - getBloodVolume(), clotted, share, yield);
	}

	/**
	 * A pour that revives clot (Projection and the suture's draw, NQ-01-4): {@code share} of it wakes the clot at
	 * {@code yield} mL of clot for each mL spent, the rest goes in fresh. Returns what happened; the caller charges only
	 * {@link CoagulationRules.Pour#taken}.
	 */
	public CoagulationRules.Pour revive(double offered, double share, double yield) {
		CoagulationRules.Pour pour = pourOf(offered, share, yield);
		if (pour.taken() <= 0.0D) return pour;
		blendAge(pour.woken() + pour.fresh(), 0L);
		clotted = Math.max(0.0D, clotted - pour.woken());
		if (clotted <= CoagulationRules.CLOT_EPSILON) clotted = 0.0D;
		setBloodVolume(getBloodVolume() + pour.woken() + pour.fresh());
		return pour;
	}

	/** Ticks since the blood was fed, as of the last settle; 0 before the first. */
	public long age() {
		return settledAt == UNSET || lastFed == UNSET ? 0L : Math.max(0L, settledAt - lastFed);
	}

	@Override
	public boolean isFull() {
		return getBloodVolume() >= getMaxBloodVolume();
	}

	@Override
	public boolean wouldOverflow(double points) {
		return getBloodVolume() + points > getMaxBloodVolume();
	}

	private void addFresh(double points, long age) {
		if (points <= 0.0D) return;
		blendAge(points, age);
		setBloodVolume(getBloodVolume() + points);
	}

	/** Moves the feeding stamp so the tank's age is the volume-weighted age of what it holds and what joins it. */
	private void blendAge(double added, long addedAge) {
		if (settledAt == UNSET || lastFed == UNSET) return;
		lastFed = settledAt - CoagulationRules.blendedAge(getBloodVolume(), age(), added, addedAge);
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = super.serializeNBT(provider);
		tag.putDouble("Clotted", clotted);
		tag.putLong("LastFed", lastFed);
		tag.putLong("SettledAt", settledAt);
		if (lastDrawn != UNSET) tag.putLong("LastDrawn", lastDrawn);
		if (owner != null) tag.putUUID("Owner", owner);
		// Only a sutured tank carries its suture's state, so a tank without one keeps its exact tag.
		if (link != ReservoirReadout.Link.NONE) tag.putByte("Link", (byte) link.ordinal());
		if (!poured.isEmpty()) {
			CompoundTag shares = new CompoundTag();
			poured.forEach((player, share) -> shares.putDouble(player.toString(), share));
			tag.put("Poured", shares);
		}
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		super.deserializeNBT(provider, tag);
		clotted = Math.max(0.0D, tag.getDouble("Clotted"));
		lastFed = tag.contains("LastFed") ? tag.getLong("LastFed") : UNSET;
		settledAt = tag.contains("SettledAt") ? tag.getLong("SettledAt") : UNSET;
		lastDrawn = tag.contains("LastDrawn") ? tag.getLong("LastDrawn") : UNSET;
		owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
		link = tag.contains("Link") ? ReservoirReadout.Link.byId(tag.getByte("Link")) : ReservoirReadout.Link.NONE;
		poured.clear();
		CompoundTag shares = tag.getCompound("Poured");
		for (String key : shares.getAllKeys()) {
			try {
				poured.put(UUID.fromString(key), shares.getDouble(key));
			} catch (IllegalArgumentException ignored) {
			}
		}
	}
}

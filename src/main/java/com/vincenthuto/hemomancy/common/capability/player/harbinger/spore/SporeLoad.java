package com.vincenthuto.hemomancy.common.capability.player.harbinger.spore;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncSporeLoad;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** An Apotheos body's spore load: gathered in the Gardens, burned for healing. Not kept through death. */
public class SporeLoad implements INBTSerializable<CompoundTag> {
	private float load;
	private transient int stillTicks;
	private transient boolean syncDirty;
	private transient boolean hasLastPosition;
	private transient double lastX;
	private transient double lastZ;

	public float load() {
		return load;
	}

	public void add(float amount) {
		set(load + amount);
	}

	/** Spends {@code amount} only when the whole of it is there. */
	public boolean spend(float amount) {
		if (load < amount) return false;
		set(load - amount);
		return true;
	}

	/** Client mirror of the owner's load. */
	public void setFromSync(float value) {
		load = SporeLoadRules.withinCapacity(value);
	}

	private void set(float value) {
		float clamped = SporeLoadRules.withinCapacity(value);
		if (clamped != load) {
			load = clamped;
			syncDirty = true;
		}
	}

	/**
	 * Squared horizontal distance since the last call. The server never sees a walking player's own velocity, so
	 * stillness is judged from position.
	 */
	public double moveSinceLastTick(double x, double z) {
		double moved = hasLastPosition ? (x - lastX) * (x - lastX) + (z - lastZ) * (z - lastZ) : 0.0;
		lastX = x;
		lastZ = z;
		hasLastPosition = true;
		return moved;
	}

	public int stillTicks() {
		return stillTicks;
	}

	public void setStillTicks(int ticks) {
		stillTicks = ticks;
	}

	public boolean isSyncDirty() {
		return syncDirty;
	}

	public void clearSyncDirty() {
		syncDirty = false;
	}

	public void sync(ServerPlayer player, boolean force) {
		if (!force && !syncDirty) return;
		PacketHandler.sendToPlayer(player, new PacketSyncSporeLoad(load));
		syncDirty = false;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putFloat("load", load);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		load = SporeLoadRules.withinCapacity(tag.getFloat("load"));
		stillTicks = 0;
		syncDirty = true;
	}
}

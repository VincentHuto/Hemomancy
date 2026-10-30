package com.vincenthuto.hemomancy.common.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

/**
 * What an owner's sanctum shows: presentation only, never a rule. Benefits, privacy, the cache and the respawn read
 * none of it, which is why the owner may set it from the client. Each toggle is one bit, in the order of the
 * Heart-node screen's rows ({@link #SPREAD} is bit 0, {@link #EDGE_WARNING} bit 6); intensity runs 0 to 4. The
 * layout rides {@code HeartNodeEnvelopePacket} and {@code HeartNodeSettingsPacket}, so moving a bit is a protocol
 * change.
 */
public record HeartNodeSettings(int bits, int intensity) {
	public static final int SPREAD = 1;
	public static final int EDGE = 1 << 1;
	public static final int THREADS = 1 << 2;
	public static final int PULSE = 1 << 3;
	public static final int MOTES = 1 << 4;
	public static final int SHOW_STRANGERS = 1 << 5;
	public static final int EDGE_WARNING = 1 << 6;
	public static final int ALL = (1 << 7) - 1;
	public static final int MAX_INTENSITY = 4;
	public static final HeartNodeSettings DEFAULT = new HeartNodeSettings(ALL & ~SHOW_STRANGERS, 2);

	public HeartNodeSettings {
		bits &= ALL;
		intensity = Mth.clamp(intensity, 0, MAX_INTENSITY);
	}

	public boolean has(int bit) {
		return (bits & bit) != 0;
	}

	public HeartNodeSettings with(int bit, boolean on) {
		return new HeartNodeSettings(on ? bits | bit : bits & ~bit, intensity);
	}

	public HeartNodeSettings withIntensity(int next) {
		return new HeartNodeSettings(bits, next);
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("Bits", bits);
		tag.putInt("Intensity", intensity);
		return tag;
	}

	public static HeartNodeSettings load(CompoundTag tag) {
		if (!tag.contains("Bits")) return DEFAULT;
		return new HeartNodeSettings(tag.getInt("Bits"), tag.getInt("Intensity"));
	}
}

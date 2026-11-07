package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.network.boss.SaintMeterPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * The Saint this client is fighting, from its last {@link SaintMeterPacket}: the Saint's entity id, the player's own
 * value, secondary, flags and countdown. The Saint's type and bar frame are remembered from the entity while it is in
 * range, so the meter still draws from the packet alone when it is not. Cleared by {@code CLEAR}, by a change of level
 * (a dimension change or a new world), on logout, and when the keep-alive stops for {@link #STALE_TICKS}.
 */
public final class SaintMeterClientState {
	/** Three keep-alives missed: the Saint is gone (unloaded, or the CLEAR was lost). */
	public static final int STALE_TICKS = 60;

	@Nullable
	private static SaintMeterPacket last;
	@Nullable
	private static ClientLevel level;
	private static long receivedAt;
	@Nullable
	private static EnumSaintType type;
	@Nullable
	private static ResourceLocation frame;
	private static float saintValue;
	private static float saintMax = 1.0F;

	private SaintMeterClientState() {
	}

	public static void accept(SaintMeterPacket packet) {
		Minecraft minecraft = Minecraft.getInstance();
		if (packet.clears()) {
			if (last == null || last.saintId() == packet.saintId()) clear();
			return;
		}
		if (last == null || last.saintId() != packet.saintId()) {
			type = null;
			frame = null;
			saintValue = 0.0F;
			saintMax = 1.0F;
		}
		last = packet;
		level = minecraft.level;
		receivedAt = level == null ? 0L : level.getGameTime();
		saint();
	}

	/** True while a Saint is named and its packets are fresh in the current level. */
	public static boolean active() {
		ClientLevel current = Minecraft.getInstance().level;
		if (last == null || current == null || current != level) return false;
		return current.getGameTime() - receivedAt <= STALE_TICKS;
	}

	@Nullable
	public static SaintMeterPacket packet() {
		return active() ? last : null;
	}

	/** The Saint's body, when it is loaded on this client; it also refreshes the remembered type, frame and meter. */
	@Nullable
	public static SaintTrialEntity saint() {
		if (last == null || level == null) return null;
		if (!(level.getEntity(last.saintId()) instanceof SaintTrialEntity saint) || saint.isRemoved()) return null;
		type = saint.saintType();
		frame = saint.barFrame();
		saintValue = saint.syncedMeter();
		saintMax = Math.max(1.0E-4F, saint.syncedMeterMax());
		return saint;
	}

	@Nullable
	public static EnumSaintType type() {
		return type;
	}

	@Nullable
	public static ResourceLocation frame() {
		return frame;
	}

	/** The Saint-owned meter as last seen on the body. */
	public static float saintValue() {
		return saintValue;
	}

	public static float saintMax() {
		return saintMax;
	}

	/** The countdown the packet carried, run down by the ticks since it arrived. */
	public static int stateTicks() {
		if (last == null || level == null) return 0;
		return (int) Math.max(0L, last.stateTicks() - (level.getGameTime() - receivedAt));
	}

	public static void clear() {
		last = null;
		level = null;
		type = null;
		frame = null;
		saintValue = 0.0F;
		saintMax = 1.0F;
	}
}

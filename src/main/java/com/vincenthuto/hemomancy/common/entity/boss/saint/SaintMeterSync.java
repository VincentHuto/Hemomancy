package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.boss.SaintMeterPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Sends each challenger their {@link SaintMeterPacket}: when they join the roster, when their state changes (the send
 * rules in {@link SaintMeter.PlayerState#packetDue}), as a keep-alive, and with {@code CLEAR} when they leave the roster
 * or the trial ends or resets. A Saint-owned meter with no per-player part still sends on join and as a keep-alive:
 * that is how the client knows which Saint it is fighting. One per Saint; the set of players sent to is not saved (a
 * reloaded Saint sends afresh, and a client whose Saint vanished times its state out).
 */
public final class SaintMeterSync {
	private final Set<UUID> sent = new LinkedHashSet<>();

	/** One tick against the arena's roster. */
	public void tick(SaintTrialEntity saint) {
		SaintArena arena = saint.arena();
		if (arena == null || !(saint.level() instanceof ServerLevel level)) return;
		SaintMeter meter = saint.meter();
		List<Player> roster = arena.challengers();
		Set<UUID> present = new LinkedHashSet<>();
		for (Player player : roster) {
			if (!(player instanceof ServerPlayer serverPlayer)) continue;
			UUID id = player.getUUID();
			present.add(id);
			SaintMeter.PlayerState state = meter.player(id);
			if (sent.add(id)) state.forgetSent();
			if (!state.packetDue(meter.playerMax())) continue;
			PacketHandler.sendToPlayer(serverPlayer, packet(saint, meter, state));
			state.markSent();
		}
		for (UUID id : List.copyOf(sent)) {
			if (present.contains(id)) continue;
			sent.remove(id);
			ServerPlayer gone = find(level, id);
			if (gone != null) PacketHandler.sendToPlayer(gone, SaintMeterPacket.clear(saint.getId()));
		}
	}

	/** The trial ended, reset or the Saint was removed: everyone sent to is cleared. */
	public void clearAll(SaintTrialEntity saint) {
		if (sent.isEmpty() || !(saint.level() instanceof ServerLevel level)) {
			sent.clear();
			return;
		}
		for (UUID id : sent) {
			ServerPlayer player = find(level, id);
			if (player != null) PacketHandler.sendToPlayer(player, SaintMeterPacket.clear(saint.getId()));
		}
		sent.clear();
	}

	/** Whether this player has been sent the meter and not yet cleared. */
	public boolean sentTo(UUID player) {
		return sent.contains(player);
	}

	/** The player in the Saint's level, else anywhere on the server (they changed dimension). */
	@javax.annotation.Nullable
	private static ServerPlayer find(ServerLevel level, UUID id) {
		if (level.getPlayerByUUID(id) instanceof ServerPlayer here) return here;
		return level.getServer().getPlayerList().getPlayer(id);
	}

	static SaintMeterPacket packet(SaintTrialEntity saint, SaintMeter meter, SaintMeter.PlayerState state) {
		return new SaintMeterPacket(saint.getId(), state.value(), meter.playerMax(), state.secondary(),
				state.secondaryMax(), state.flags() & SaintMeterPacket.SAINT_FLAGS, state.stateTicks());
	}
}

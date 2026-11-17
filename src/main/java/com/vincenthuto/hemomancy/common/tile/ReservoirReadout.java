package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.DirectBloodLinkData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * What the Vascular Status Gauge says about a machine's blood (core-loop Track 3): how much is fresh and how much has
 * clotted, how fresh it reads, and whether a suture keeps it fed or why it has stalled.
 */
public final class ReservoirReadout {
	/** How a tank reads: the colour its blood shows, never a countdown. */
	public enum State {
		LIVING, FRESH, TURNING, CLOTTING, EMPTY, NO_TANK
	}

	/** Whether the tank's feeding line runs: no suture, sutured and drawing, or sutured and stalled. */
	public enum Feed {
		NONE, DRAWING, STALLED;

		public static Feed of(Link link) {
			return link == Link.NONE ? NONE : link.drawing() ? DRAWING : STALLED;
		}
	}

	/**
	 * A suture's state, and why a stalled one does not draw. Worked out on the server's routing pass
	 * ({@code BloodRoutingHelper.linkStatus}) and kept on the tank ({@link BlockBloodVolume#link}) so the machine's own
	 * screen can say it. Appended only: the ordinal is what the tank stores.
	 */
	public enum Link {
		NONE, DRAWING, OWNER_OFFLINE, OWNER_DEAD, BELOW_DEGREE, BLOOD_DORMANT, OUT_OF_REACH, BELOW_FANE_DEGREE, OUTSIDE_FANE,
		/** The owner is offline and a member of their bloodline nearby stands in (CN-9). */
		KIN_DRAWING;

		public boolean drawing() {
			return this == DRAWING || this == KIN_DRAWING;
		}

		public boolean stalled() {
			return this != NONE && !drawing();
		}

		/** The reason's lang suffix: {@code owner_offline}, {@code out_of_reach} and so on. */
		public String key() {
			return name().toLowerCase(java.util.Locale.ROOT);
		}

		public static Link byId(int id) {
			Link[] all = values();
			return id >= 0 && id < all.length ? all[id] : NONE;
		}
	}

	private ReservoirReadout() {
	}

	public static State state(BlockEntity blockEntity) {
		if (blockEntity instanceof IBloodReservoir reservoir && !reservoir.holdsBlood()) return State.NO_TANK;
		BlockBloodVolume volume = ReservoirBlood.settled(blockEntity);
		return state(ReservoirBlood.held(blockEntity), volume.getBloodVolume(), volume.clotted(),
				blockEntity.getLevel() == null ? 1.0D : volume.freshness(blockEntity.getLevel().getGameTime()));
	}

	/** The pure reading: living vessels first, then what the fresh blood and its freshness say. */
	public static State state(boolean living, double fresh, double clotted, double freshness) {
		if (living) return State.LIVING;
		if (fresh <= 0.0D) return clotted > CoagulationRules.CLOT_EPSILON ? State.CLOTTING : State.EMPTY;
		if (clotted > CoagulationRules.CLOT_EPSILON || freshness <= 0.0D) return State.CLOTTING;
		return freshness >= 0.5D ? State.FRESH : State.TURNING;
	}

	public static Feed feed(ServerLevel level, BlockEntity blockEntity) {
		return Feed.of(link(level, blockEntity));
	}

	/** The suture's state for the block, worked out now. */
	public static Link link(ServerLevel level, BlockEntity blockEntity) {
		DirectBloodLinkData link = BloodRoutingSavedData.get(level).getLink(blockEntity.getBlockPos());
		return BloodRoutingHelper.linkStatus(level, blockEntity.getBlockPos(), link);
	}

	/** The Gauge's lines for a machine. */
	public static List<Component> lines(ServerLevel level, BlockEntity blockEntity) {
		List<Component> lines = new ArrayList<>();
		lines.add(Component.translatable("message.hemomancy.living_blood.gauge.header",
				blockEntity.getBlockState().getBlock().getName()).withStyle(ChatFormatting.DARK_RED));
		State state = state(blockEntity);
		if (state == State.NO_TANK) {
			lines.add(Component.translatable("message.hemomancy.living_blood.gauge.no_tank").withStyle(ChatFormatting.GRAY));
			return lines;
		}
		BlockBloodVolume volume = ReservoirBlood.settled(blockEntity);
		lines.add(Component.translatable("message.hemomancy.living_blood.gauge.amounts",
				Math.round(volume.getBloodVolume()), Math.round(volume.clotted()), Math.round(volume.capacity()))
				.withStyle(ChatFormatting.GRAY));
		lines.add(Component.translatable("message.hemomancy.living_blood.state." + state.name().toLowerCase(java.util.Locale.ROOT))
				.withStyle(state == State.CLOTTING ? ChatFormatting.DARK_GRAY : ChatFormatting.RED));
		if (state != State.LIVING && volume.clotted() > CoagulationRules.CLOT_EPSILON) {
			lines.add(Component.translatable("message.hemomancy.living_blood.gauge.revive",
					number(CoagulationRules.reviveShare() * 100.0D), number(CoagulationRules.reviveYield()),
					number(CoagulationRules.suturedReviveYield())).withStyle(ChatFormatting.GRAY));
		}
		if (state != State.LIVING) {
			Link link = link(level, blockEntity);
			Feed feed = Feed.of(link);
			String key = "message.hemomancy.living_blood.feed." + feed.name().toLowerCase(java.util.Locale.ROOT)
					+ (link.stalled() ? "." + link.key() : link == Link.KIN_DRAWING ? ".kin" : "");
			lines.add(Component.translatable(key).withStyle(feed == Feed.STALLED ? ChatFormatting.GOLD : ChatFormatting.GRAY));
		}
		return lines;
	}

	private static String number(double value) {
		return value == Math.rint(value) ? Long.toString((long) value) : String.format(java.util.Locale.ROOT, "%.1f", value);
	}
}

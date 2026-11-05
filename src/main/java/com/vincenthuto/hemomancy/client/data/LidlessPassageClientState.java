package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import com.vincenthuto.hemomancy.common.network.lidless.LidlessPassageStatePacket;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/** Who is crossing a Lidless Door on this client, by entity id, from the last state sync. */
public final class LidlessPassageClientState {
	/** One crossing as the client sees it; {@code previousProgress} is the sync before {@code progress}. */
	public record View(BlockPos door, Direction facing, boolean towardFront, LidlessPassageService.Mode mode,
			com.vincenthuto.hemomancy.common.block.shared.LidlessTheme theme,
			float length, long token, float previousProgress, float progress) {
		/** The world direction from the entry cell to the far cell. */
		public Direction passageForward() {
			return towardFront ? facing : facing.getOpposite();
		}
	}

	private static final Map<Integer, View> CROSSING = new HashMap<>();

	private LidlessPassageClientState() {
	}

	public static void update(LidlessPassageStatePacket packet) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || !mc.level.dimension().location().equals(packet.dimension())) return;
		if (!packet.active()) {
			View ended = CROSSING.remove(packet.playerId());
			// The local player's crossing into or out of the Chamber ends a tick before the level changes: the transition
			// screen draws on from where it left off.
			if (ended != null && mc.player != null && packet.playerId() == mc.player.getId() && ended.mode().leavesLevel()
					&& packet.progress() >= ended.length() - 0.01F) {
				// The hole as the walk left it, and the walk's last live frame, so the veil draws on without a seam.
				ChamberVeil.arm(ended.theme(), com.vincenthuto.hemomancy.client.render.tile.shared.LidlessDoorRenderer.seed(ended.door()), ended.mode(),
						com.vincenthuto.hemomancy.client.screen.overlay.LidlessHoleRules.gatewayRadius(1.0F), 1.0F,
						com.vincenthuto.hemomancy.client.render.world.chamberofwill.ChamberProxyView.detachTarget(ended.door()));
			}
			// The hold mixin set noPhysics on every client; the local player is released by the client events.
			if (mc.level.getEntity(packet.playerId()) instanceof Player player && player != mc.player)
				player.noPhysics = player.isSpectator();
			return;
		}
		View before = CROSSING.get(packet.playerId());
		float previous = before == null || before.token() != packet.token() ? packet.progress() : before.progress();
		CROSSING.put(packet.playerId(), new View(packet.door(), packet.facing(), packet.towardFront(), packet.mode(),
				packet.theme(), packet.length(), packet.token(), previous, packet.progress()));
	}

	public static boolean isCrossing(Player player) {
		return CROSSING.containsKey(player.getId());
	}

	/** The local player's crossing, or null when they are not crossing. */
	@Nullable
	public static View local() {
		Player player = Minecraft.getInstance().player;
		return player == null ? null : CROSSING.get(player.getId());
	}

	/** The local player's progress between the last two syncs. */
	public static float progress(float partialTick) {
		View view = local();
		return view == null ? 0 : HemoEasing.lerp(view.previousProgress(), view.progress(), partialTick);
	}

	/** Forgets a player who left the level, so they are drawn again wherever they went. */
	public static void forget(int entityId) {
		CROSSING.remove(entityId);
	}

	public static void clear() {
		CROSSING.clear();
	}
}

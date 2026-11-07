package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.BossHold;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

import javax.annotation.Nullable;

/**
 * The held player's side of a kit Saint's {@link BossHold}: the Saint syncs the held entity id, the offset in its body's
 * frame and the slack (negative when input stays unlocked). Player movement belongs to the client, so the held client
 * draws itself back within the slack of the point the server holds it to (from the Saint's latest synced place and
 * facing), loses its fall, and while the hold locks input, drops its movement input. The server's own pin is left for a
 * client that has fallen out of step.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class BossHoldClientEvents {
	/** How far a tick the held client closes on the slack's edge. */
	private static final double PULL = 0.45D;
	/** Below this gap (blocks) the pull lands exactly. */
	private static final double SETTLED = 0.03D;
	private static final double SEARCH = 16.0D;

	private BossHoldClientEvents() {
	}

	@SubscribeEvent
	public static void pinHeldPlayer(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (minecraft.level == null || player == null || minecraft.isPaused()) return;
		SaintTrialEntity holder = holder(player);
		if (holder == null) return;
		Vec3 point = BossHold.point(new Vec3(holder.lerpTargetX(), holder.lerpTargetY(), holder.lerpTargetZ()),
				holder.lerpTargetYRot(), holder.syncedHoldOffset());
		double slack = Math.abs(holder.syncedHoldSlack());
		boolean locked = holder.syncedHoldSlack() >= 0.0F;
		Vec3 at = player.position();
		Vec3 limit = pinned(at, point, slack);
		if (limit != at) {
			player.setPos(limit.distanceToSqr(at) < SETTLED * SETTLED ? limit : at.lerp(limit, PULL));
		}
		if (locked) player.setDeltaMovement(Vec3.ZERO);
		player.resetFallDistance();
	}

	@SubscribeEvent
	public static void holdInput(MovementInputUpdateEvent event) {
		SaintTrialEntity holder = holder(event.getEntity());
		if (holder == null || holder.syncedHoldSlack() < 0.0F) return;
		Input input = event.getInput();
		input.forwardImpulse = 0.0F;
		input.leftImpulse = 0.0F;
		input.up = input.down = input.left = input.right = false;
		input.jumping = false;
		input.shiftKeyDown = false;
	}

	/** The nearest point within {@code slack} of {@code point}; {@code at} itself when it is already inside. */
	static Vec3 pinned(Vec3 at, Vec3 point, double slack) {
		if (!BossHold.pinned(at, point, slack)) return at;
		Vec3 away = at.subtract(point);
		double length = away.length();
		return length < 1.0E-6D ? point : point.add(away.scale(slack / length));
	}

	@Nullable
	private static SaintTrialEntity holder(Player player) {
		for (SaintTrialEntity saint : player.level().getEntitiesOfClass(SaintTrialEntity.class,
				player.getBoundingBox().inflate(SEARCH))) {
			if (saint.isAlive() && saint.syncedHoldPlayer() == player.getId()) return saint;
		}
		return null;
	}
}

package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantClaimRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantStrikeRules;
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
 * The held player's side of the Mycophant's holds: the embrace's sac, the claw pin and the cocoon, whose kind is synced
 * with the claimed id and picks the point. Player movement belongs to the client, so a hold the server enforced by
 * teleporting alone left the client falling and walking between snaps and shook the player back and forth. Here the
 * held client draws itself in to the hold point, stays there with no motion or fall, and drops its movement input; the
 * server's snap is left for a client that has fallen out of step.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MycophantHoldClientEvents {
	/** Below this gap (blocks) the pull lands exactly on the hold point. */
	private static final double SETTLED = 0.03D;

	private MycophantHoldClientEvents() {
	}

	/**
	 * After the player ticks: with no motion and no input, the next tick moves (and reports) nothing, and the frames in
	 * between interpolate from where the player was to the pinned point, so the pull in reads as a smooth draw.
	 */
	@SubscribeEvent
	public static void pinHeldPlayer(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (minecraft.level == null || player == null || minecraft.isPaused()) return;
		MycophantEntity holder = holder(player);
		if (holder == null) return;
		// her latest synced place and facing, which is what the server holds the player against; the kind picks the sac or the claw
		Vec3 point = MycophantEntity.holdPoint(new Vec3(holder.lerpTargetX(), holder.lerpTargetY(), holder.lerpTargetZ()),
				holder.lerpTargetYRot(), holder.getHoldKind());
		Vec3 at = player.position();
		player.setPos(at.distanceToSqr(point) < SETTLED * SETTLED ? point : at.lerp(point, MycophantClaimRules.holdPullPerTick()));
		player.setDeltaMovement(Vec3.ZERO);
		player.resetFallDistance();
	}

	@SubscribeEvent
	public static void holdInput(MovementInputUpdateEvent event) {
		// a landed lattice net holds the feet: no jumping out of it
		if (MycophantStrikeRules.onlySurvival(event.getEntity().isSpectator(), event.getEntity().isCreative())
				&& MycophantHazardEntity.nets(event.getEntity().level(), event.getEntity())) event.getInput().jumping = false;
		if (holder(event.getEntity()) == null) return;
		Input input = event.getInput();
		input.forwardImpulse = 0.0F;
		input.leftImpulse = 0.0F;
		input.up = input.down = input.left = input.right = false;
		input.jumping = false;
		input.shiftKeyDown = false;
	}

	@Nullable
	private static MycophantEntity holder(Player player) {
		for (MycophantEntity boss : player.level().getEntitiesOfClass(MycophantEntity.class, player.getBoundingBox().inflate(12.0D)))
			if (boss.isAlive() && boss.getClaimedId() == player.getId()) return boss;
		return null;
	}
}

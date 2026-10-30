package com.vincenthuto.hemomancy.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.PossessionSceneClientState;
import com.vincenthuto.hemomancy.client.data.PossessionSceneClientState.BodyPose;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Bodies in a possession scene: hidden while the echo or a scene body stands for them, and posed (thinned, sunk,
 * tipped over, shuddering) while they dissolve, arrive or collapse. The pose wraps the entity's own render, so the
 * stand-in a possessed player is drawn as is posed the same way as a real mob.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class PossessionSceneClientEvents {
	/**
	 * The entities whose render this class pushed a pose for, innermost last. A Post pops only for the entity on top,
	 * so an entry left by a render cancelled after the push never pops another entity's pose. Nothing in this mod
	 * cancels a living render at LOWEST.
	 */
	private static final Deque<LivingEntity> PUSHED = new ArrayDeque<>();

	private PossessionSceneClientEvents() {
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void hide(RenderLivingEvent.Pre<?, ?> event) {
		if (PossessionSceneClientState.hides(event.getEntity(), event.getPartialTick())) event.setCanceled(true);
	}

	/** Last, so nothing can cancel the render after the pose is pushed and leave the stack unbalanced. */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void pose(RenderLivingEvent.Pre<?, ?> event) {
		LivingEntity entity = event.getEntity();
		// Renders nest only a few deep; more entries are strays from cancelled renders piling up.
		if (PUSHED.size() > 8) PUSHED.clear();
		BodyPose body = PossessionSceneClientState.pose(entity, event.getPartialTick());
		if (body == BodyPose.NONE) return;
		PoseStack pose = event.getPoseStack();
		pose.pushPose();
		PUSHED.push(entity);
		float time = AnimationTime.age(entity, event.getPartialTick());
		if (body.shudder() > 0.0F) {
			pose.translate(Mth.sin(time * 2.9F) * body.shudder(), 0.0F, Mth.cos(time * 3.7F) * body.shudder());
		}
		pose.translate(0.0F, -body.sink(), 0.0F);
		if (body.tipDeg() != 0.0F) {
			// Tipped sideways across the body's own facing, pivoting at the feet.
			float yaw = Mth.lerp(event.getPartialTick(), entity.yBodyRotO, entity.yBodyRot);
			pose.mulPose(Axis.YP.rotationDegrees(-yaw));
			pose.mulPose(Axis.ZP.rotationDegrees(body.tipDeg()));
			pose.mulPose(Axis.YP.rotationDegrees(yaw));
		}
		pose.scale(body.scaleXZ(), body.scaleY(), body.scaleXZ());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void unpose(RenderLivingEvent.Post<?, ?> event) {
		if (!PUSHED.isEmpty() && PUSHED.peek() == event.getEntity()) {
			PUSHED.pop();
			event.getPoseStack().popPose();
		}
	}
}

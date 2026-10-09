package com.vincenthuto.hemomancy.client.render.item.harbinger.crossbar;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Item renderers are not told who holds the stack, so this remembers which living entity is being drawn, the frame
 * the dispatcher placed it in, and whether that draw belongs to the level. Inventory-screen paper dolls also fire
 * render events; only level draws may move a thread anchor.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CrossbarHolderTracker {
	public record Holder(LivingEntity entity, Matrix4f root, boolean inLevel) {
	}

	private static final Deque<Holder> RENDERING = new ArrayDeque<>();
	private static boolean levelEntities;
	private static long frame;

	private CrossbarHolderTracker() {
	}

	/** The entity whose layers are drawing right now, or null outside a living-entity render. */
	public static Holder current() {
		return RENDERING.peek();
	}

	/** Increments once per rendered level frame; anchors stamped with an older frame are stale. */
	public static long frame() {
		return frame;
	}

	@SubscribeEvent
	public static void onStage(RenderLevelStageEvent event) {
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
			frame++;
			RENDERING.clear();
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
			levelEntities = true;
		} else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
			levelEntities = false;
		}
	}

	// Lowest priority without receiving cancelled events, so every push is matched by a Post.
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onPre(RenderLivingEvent.Pre<?, ?> event) {
		RENDERING.push(new Holder(event.getEntity(), new Matrix4f(event.getPoseStack().last().pose()), levelEntities));
	}

	@SubscribeEvent
	public static void onPost(RenderLivingEvent.Post<?, ?> event) {
		RENDERING.poll();
	}
}

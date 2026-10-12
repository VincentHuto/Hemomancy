package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The later beats of a multi-beat echo cast (restrikes, slams, collapses) and the expiry of its dummy entities,
 * run on the client tick. Everything is dropped when the level changes.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class EchoScheduler {
	private static final int CAP = 256;
	private static final List<Task> TASKS = new ArrayList<>();
	private static ClientLevel world;
	private static long tick;

	private record Task(long due, Runnable work) {}

	private EchoScheduler() {
	}

	static void later(int ticks, Runnable work) {
		if (ticks <= 0) {
			work.run();
			return;
		}
		if (TASKS.size() < CAP) TASKS.add(new Task(tick + ticks, work));
	}

	@SubscribeEvent
	public static void onTick(ClientTickEvent.Post event) {
		ClientLevel current = Minecraft.getInstance().level;
		if (current != world) {
			TASKS.clear();
			EchoDummies.clear(world);
			world = current;
		}
		if (current == null) return;
		tick++;
		List<Task> due = new ArrayList<>();
		for (Iterator<Task> it = TASKS.iterator(); it.hasNext(); ) {
			Task task = it.next();
			if (task.due() <= tick) {
				due.add(task);
				it.remove();
			}
		}
		for (Task task : due) {
			try {
				task.work().run();
			} catch (RuntimeException failure) {
				Hemomancy.LOGGER.debug("An echo cast beat failed", failure);
			}
		}
		EchoDummies.tick(current);
	}
}

package com.vincenthuto.hemomancy.client.render.world.rite;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.client.data.CardinalRiteEventClientState.RiteEvent;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Remembers each offering drawn into a rite's daemon for the rest of the rite. The beat itself expires from
 * {@link CardinalRiteEventClientState} after a few seconds, but the daemon carries what it has eaten until the rite
 * ends, so its nodules outlive the beat.
 */
public final class CardinalRiteOfferingMemory {
	private static final Map<BlockPos, List<RiteEvent>> ABSORBED = new HashMap<>();
	private static final Set<RiteEvent> SEEN = Collections.newSetFromMap(new IdentityHashMap<>());
	private static final Map<Item, Integer> COLOURS = new HashMap<>();
	private static final int FALLBACK_COLOUR = 0xB3141E;
	private static long lastRefreshTick = Long.MIN_VALUE;

	private CardinalRiteOfferingMemory() {
	}

	/** Absorbed offerings at a rite centre, oldest first; empty when none. */
	public static List<RiteEvent> absorbed(BlockPos center) {
		refresh();
		List<RiteEvent> list = ABSORBED.get(center);
		return list == null ? List.of() : list;
	}

	/** Folds new draw beats into memory and forgets rites that have ended. Runs at most once per game tick. */
	public static void refresh() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			ABSORBED.clear();
			SEEN.clear();
			return;
		}
		long tick = mc.level.getGameTime();
		if (tick == lastRefreshTick) return;
		lastRefreshTick = tick;

		for (RiteEvent event : CardinalRiteEventClientState.all()) {
			if (event.kind() != CardinalRiteEventKind.OFFERING_DRAWN || !SEEN.add(event)) continue;
			List<RiteEvent> list = ABSORBED.computeIfAbsent(event.center(), key -> new ArrayList<>());
			if (list.size() >= CardinalRiteVisualRules.MAX_NODULES) list.remove(0);
			list.add(event);
		}
		SEEN.removeIf(event -> event.age(0) > CardinalRiteEventClientState.EVENT_LIFETIME_TICKS + 20);

		Iterator<Map.Entry<BlockPos, List<RiteEvent>>> iterator = ABSORBED.entrySet().iterator();
		while (iterator.hasNext()) {
			BlockPos center = iterator.next().getKey();
			if (!isLive(center) && CardinalRiteEventClientState.ghostAt(center) == null) iterator.remove();
		}
	}

	private static boolean isLive(BlockPos center) {
		for (ActiveRiteData.RiteEntry rite : ActiveRiteClientData.getActiveRites()) {
			if (rite.getCenter().equals(center)) return true;
		}
		return false;
	}

	/** Average opaque colour of an item's icon as {@code 0xRRGGBB}, cached per item. */
	public static int colour(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return FALLBACK_COLOUR;
		Integer cached = COLOURS.get(stack.getItem());
		if (cached != null) return cached;
		int colour = FALLBACK_COLOUR;
		try {
			Minecraft mc = Minecraft.getInstance();
			TextureAtlasSprite sprite = mc.getItemRenderer().getModel(stack, mc.level, null, 0).getParticleIcon();
			int width = sprite.contents().width(), height = sprite.contents().height();
			long red = 0, green = 0, blue = 0, samples = 0;
			for (int px = 0; px < 8; px++) {
				for (int py = 0; py < 8; py++) {
					int pixel = sprite.getPixelRGBA(0, Math.min(width - 1, px * width / 8),
							Math.min(height - 1, py * height / 8));
					if (((pixel >>> 24) & 255) < 40) continue;
					// Atlas pixels are ABGR.
					red += pixel & 255;
					green += (pixel >> 8) & 255;
					blue += (pixel >> 16) & 255;
					samples++;
				}
			}
			if (samples > 0) {
				colour = (int) (red / samples) << 16 | (int) (green / samples) << 8 | (int) (blue / samples);
			}
		} catch (RuntimeException ignored) {
			// A model without a readable sprite keeps the blood fallback.
		}
		COLOURS.put(stack.getItem(), colour);
		return colour;
	}

	public static void clear() {
		ABSORBED.clear();
		SEEN.clear();
		COLOURS.clear();
	}
}

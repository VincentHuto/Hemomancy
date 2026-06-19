package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.EnumMap;
import java.util.Map;

public final class VeinMasonScarLesson {
	private static final Map<EnumBloodTendency, Lesson> LESSONS = new EnumMap<>(EnumBloodTendency.class);

	static {
		LESSONS.put(EnumBloodTendency.ANIMUS, lesson(ItemInit.scar_pattern_heart, Items.GOLDEN_APPLE, ItemInit.scar_heart));
		LESSONS.put(EnumBloodTendency.FLAMMEUS, lesson(ItemInit.scar_pattern_pyre, Items.BLAZE_POWDER, ItemInit.scar_pyre));
		LESSONS.put(EnumBloodTendency.DUCTILIS, lesson(ItemInit.scar_pattern_feral, Items.LEATHER, ItemInit.scar_feral));
		LESSONS.put(EnumBloodTendency.LUX, lesson(ItemInit.scar_pattern_halo, Items.END_ROD, ItemInit.scar_halo));
		LESSONS.put(EnumBloodTendency.MORTEM, lesson(ItemInit.scar_pattern_blight, Items.FERMENTED_SPIDER_EYE, ItemInit.scar_blight));
		LESSONS.put(EnumBloodTendency.CONGEATIO, lesson(ItemInit.scar_pattern_rime, Items.PACKED_ICE, ItemInit.scar_rime));
		LESSONS.put(EnumBloodTendency.FERRIC, lesson(ItemInit.scar_pattern_thorn, Items.IRON_INGOT, ItemInit.scar_thorn));
		LESSONS.put(EnumBloodTendency.TENEBRIS, lesson(ItemInit.scar_pattern_shade, Items.COAL, ItemInit.scar_shade));
	}

	private VeinMasonScarLesson() {
	}

	public static Lesson forPlayer(Player player) {
		return rankedForPlayer(player, 0);
	}

	public static Lesson continuationForPlayer(Player player) {
		return rankedForPlayer(player, 1);
	}

	private static Lesson rankedForPlayer(Player player, int rank) {
		EnumBloodTendency best = EnumBloodTendency.ANIMUS;
		EnumBloodTendency second = EnumBloodTendency.FLAMMEUS;
		float bestValue = Float.NEGATIVE_INFINITY;
		float secondValue = Float.NEGATIVE_INFINITY;
		for (EnumBloodTendency candidate : EnumBloodTendency.values()) {
			float value = HemoCapabilityAccess.getBloodTendency(player)
					.map(tendency -> tendency.getAlignmentByTendency(candidate))
					.orElse(0f);
			if (value > bestValue) {
				second = best;
				secondValue = bestValue;
				best = candidate;
				bestValue = value;
			} else if (candidate != best && value > secondValue) {
				second = candidate;
				secondValue = value;
			}
		}
		return LESSONS.get(rank <= 0 ? best : second);
	}

	private static Lesson lesson(DeferredHolder<Item, Item> pattern, Item catalyst,
			DeferredHolder<Item, Item> scar) {
		return new Lesson(pattern, catalyst, scar);
	}

	public record Lesson(DeferredHolder<Item, Item> pattern, Item catalyst,
			DeferredHolder<Item, Item> scar) {
	}
}

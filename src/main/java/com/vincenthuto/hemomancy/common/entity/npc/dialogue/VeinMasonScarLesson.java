package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class VeinMasonScarLesson {
	private static final Map<EnumBloodTemper, Lesson> TIER_ONE = new EnumMap<>(EnumBloodTemper.class);
	private static final Map<EnumBloodTemper, Lesson> TIER_TWO = new EnumMap<>(EnumBloodTemper.class);
	private static final Map<EnumBloodTemper, Lesson> TIER_THREE = new EnumMap<>(EnumBloodTemper.class);
	private static final Map<ResourceLocation, Lesson> BY_PATTERN = new HashMap<>();

	static {
		put(EnumBloodTemper.ANIMUS, Items.GOLDEN_APPLE, Items.BONE, "heart", ItemInit.scar_heart, "marrow", ItemInit.scar_marrow, "phoenix", ItemInit.scar_phoenix);
		put(EnumBloodTemper.FLAMMEUS, Items.BLAZE_POWDER, Items.GLOWSTONE_DUST, "pyre", ItemInit.scar_pyre, "sol", ItemInit.scar_sol, "corona", ItemInit.scar_corona);
		put(EnumBloodTemper.DUCTILIS, Items.LEATHER, Items.SLIME_BALL, "feral", ItemInit.scar_feral, "flux", ItemInit.scar_flux, "chimera", ItemInit.scar_chimera);
		put(EnumBloodTemper.LUX, Items.END_ROD, Items.ENDER_PEARL, "halo", ItemInit.scar_halo, "veil", ItemInit.scar_veil, "transcendence", ItemInit.scar_transcendence);
		put(EnumBloodTemper.MORTEM, Items.FERMENTED_SPIDER_EYE, Items.WITHER_ROSE, "blight", ItemInit.scar_blight, "wither", ItemInit.scar_wither, "oblivion", ItemInit.scar_oblivion);
		put(EnumBloodTemper.CONGEATIO, Items.PACKED_ICE, Items.BLUE_ICE, "rime", ItemInit.scar_rime, "glacier", ItemInit.scar_glacier, "descendence", ItemInit.scar_descendence);
		put(EnumBloodTemper.FERRIC, Items.IRON_INGOT, Items.IRON_BLOCK, "thorn", ItemInit.scar_thorn, "anvil", ItemInit.scar_anvil, "crucible", ItemInit.scar_crucible);
		put(EnumBloodTemper.TENEBRIS, Items.COAL, Items.SMOOTH_QUARTZ, "shade", ItemInit.scar_shade, "moon", ItemInit.scar_moon, "eye", ItemInit.scar_eye);
	}

	private VeinMasonScarLesson() {
	}

	public static Lesson forPlayer(Player player) {
		return rankedForPlayer(player, 0);
	}

	public static Lesson continuationForPlayer(Player player) {
		return rankedForPlayer(player, 1);
	}

	public static Lesson strongestForPlayer(Player player, int tier) {
		return rankedForPlayer(player, 0, tier);
	}

	/** PT-14: the lesson the Mason gave for {@code slot}, as stored then; before he gives it, the live lesson. */
	public static Lesson issued(ServerPlayer player, Slot slot) {
		return VeinMasonAssignments.issuedLesson(player, slot.key).map(BY_PATTERN::get)
				.orElseGet(() -> slot.live.apply(player));
	}

	/** Picks the lesson for {@code slot} from the player's temper now and stores it as the one given. */
	public static Lesson issue(ServerPlayer player, Slot slot) {
		Lesson lesson = slot.live.apply(player);
		VeinMasonAssignments.recordIssuedLesson(player, slot.key, lesson.patternScarId());
		return lesson;
	}

	public static boolean needsReplacement(Player player, Lesson lesson) {
		boolean known = HemoCapabilityAccess.getScarState(player).knowsCerebralScar(lesson.patternScarId());
		if (known) return false;
		for (ItemStack stack : player.getInventory().items) {
			if (ItemScarPattern.getScarIds(stack).contains(lesson.patternScarId())) return false;
		}
		return true;
	}

	private static Lesson rankedForPlayer(Player player, int rank) {
		return rankedForPlayer(player, rank, 1);
	}

	private static Lesson rankedForPlayer(Player player, int rank, int tier) {
		EnumBloodTemper best = EnumBloodTemper.ANIMUS;
		EnumBloodTemper second = EnumBloodTemper.FLAMMEUS;
		float bestValue = Float.NEGATIVE_INFINITY;
		float secondValue = Float.NEGATIVE_INFINITY;
		for (EnumBloodTemper candidate : EnumBloodTemper.values()) {
			float value = HemoCapabilityAccess.getBloodTemperament(player).getAlignmentByTemper(candidate);
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
		Map<EnumBloodTemper, Lesson> lessons = tier >= 3 ? TIER_THREE : tier == 2 ? TIER_TWO : TIER_ONE;
		return lessons.get(rank <= 0 ? best : second);
	}

	private static void put(EnumBloodTemper temper, Item catalyst, Item tierTwoCatalyst,
			String one, DeferredHolder<Item, Item> oneItem, String two, DeferredHolder<Item, Item> twoItem,
			String three, DeferredHolder<Item, Item> threeItem) {
		TIER_ONE.put(temper, lesson("scar_" + one, catalyst, oneItem));
		TIER_TWO.put(temper, lesson("scar_" + two, tierTwoCatalyst, twoItem));
		TIER_THREE.put(temper, lesson("scar_" + three, catalyst, threeItem));
		for (Lesson lesson : List.of(TIER_ONE.get(temper), TIER_TWO.get(temper), TIER_THREE.get(temper))) {
			BY_PATTERN.put(lesson.patternScarId(), lesson);
		}
	}

	private static Lesson lesson(String patternScarId, Item catalyst,
			DeferredHolder<Item, Item> scar) {
		return new Lesson(Hemomancy.rloc(patternScarId), catalyst, scar);
	}

	/** The four lessons the Mason gives: the first, the continuation, and the D5 and D6 rewards. */
	public enum Slot {
		FIRST("first", VeinMasonScarLesson::forPlayer),
		CONTINUATION("continuation", VeinMasonScarLesson::continuationForPlayer),
		D5("d5", player -> strongestForPlayer(player, 2)),
		D6("d6", player -> strongestForPlayer(player, 3));

		private final String key;
		private final Function<Player, Lesson> live;

		Slot(String key, Function<Player, Lesson> live) {
			this.key = key;
			this.live = live;
		}
	}

	public record Lesson(ResourceLocation patternScarId, Item catalyst, DeferredHolder<Item, Item> scar) {
		public ItemStack patternStack() {
			return ItemScarPattern.createTemplatePattern(patternScarId);
		}
	}
}

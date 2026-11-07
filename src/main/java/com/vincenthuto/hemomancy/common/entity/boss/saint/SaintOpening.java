package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.EnumSet;
import java.util.Set;

/**
 * A punish window: what counts during it, how much more the Saint takes, and how many hits it admits. The Saint
 * decides when it is open ({@link SaintOpenings}); the player decides whether to take it.
 */
public final class SaintOpening {
	/** How a hit reached the Saint, classified by the kit's damage pipeline. */
	public enum Delivery { MELEE, PROJECTILE, SCHOOL_CAST, CHANNEL, OTHER }

	/** What an opening made of a hit: counted, over its cap, of a delivery it ignores, or no opening at all. */
	public enum Admit { COUNTED, CAPPED, NOT_COUNTED, CLOSED }

	/**
	 * @param id                    1..255 within the Saint, synced as {@code DATA_OPENING}
	 * @param key                   lower_snake, for lang and tests
	 * @param ticks                 the default length
	 * @param damageTaken           the damage-taken factor while open
	 * @param hitCap                hits it counts with one challenger (0 = no cap)
	 * @param capPerExtraChallenger more hits for each challenger past the first
	 * @param counts                the deliveries it counts
	 */
	public record Spec(int id, String key, int ticks, float damageTaken, int hitCap, int capPerExtraChallenger,
			Set<Delivery> counts) {
		public Spec {
			counts = counts.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(counts));
		}

		/** The hit cap for {@code challengers} challengers; 0 = uncapped. */
		public int cap(int challengers) {
			return hitCap <= 0 ? 0 : hitCap + capPerExtraChallenger * Math.max(0, challengers - 1);
		}

		public CompoundTag save() {
			CompoundTag tag = new CompoundTag();
			tag.putInt("id", id);
			tag.putString("key", key);
			tag.putInt("ticks", ticks);
			tag.putFloat("damage_taken", damageTaken);
			tag.putInt("hit_cap", hitCap);
			tag.putInt("cap_per_extra", capPerExtraChallenger);
			ListTag list = new ListTag();
			for (Delivery delivery : counts) list.add(StringTag.valueOf(delivery.name()));
			tag.put("counts", list);
			return tag;
		}

		public static Spec load(CompoundTag tag) {
			EnumSet<Delivery> counts = EnumSet.noneOf(Delivery.class);
			ListTag list = tag.getList("counts", Tag.TAG_STRING);
			for (int i = 0; i < list.size(); i++) {
				try {
					counts.add(Delivery.valueOf(list.getString(i)));
				} catch (IllegalArgumentException unknown) {
					// a delivery that no longer exists counts nothing
				}
			}
			return new Spec(tag.getInt("id"), tag.getString("key"), tag.getInt("ticks"), tag.getFloat("damage_taken"),
					tag.getInt("hit_cap"), tag.getInt("cap_per_extra"), counts);
		}
	}

	private SaintOpening() {
	}
}

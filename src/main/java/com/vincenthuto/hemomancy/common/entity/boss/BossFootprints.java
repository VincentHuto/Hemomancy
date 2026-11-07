package com.vincenthuto.hemomancy.common.entity.boss;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint.Height;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

/** NBT for footprints: ground hazards save theirs and the synced move frame carries it. */
public final class BossFootprints {
	private BossFootprints() {
	}

	public static CompoundTag save(BossFootprint shape) {
		CompoundTag tag = new CompoundTag();
		switch (shape) {
			case BossFootprint.Lane s -> {
				tag.putString("type", "lane");
				put(tag, "length", s.length(), "half_width", s.halfWidth(), "spacing", s.spacing(), "fan", s.fanDegrees(),
						"front_speed", s.frontSpeed());
				tag.putInt("side_lanes", s.sideLanes());
				tag.putInt("side_delay", s.sideDelay());
			}
			case BossFootprint.Spokes s -> {
				tag.putString("type", "spokes");
				tag.putInt("count", s.count());
				put(tag, "half_width", s.halfWidth(), "inner", s.inner(), "length", s.length(), "offset", s.offsetDegrees(),
						"front_speed", s.frontSpeed());
			}
			case BossFootprint.Cone s -> {
				tag.putString("type", "cone");
				put(tag, "inner", s.inner(), "radius", s.radius(), "degrees", s.degrees(), "offset", s.offsetDegrees());
			}
			case BossFootprint.Sweep s -> {
				tag.putString("type", "sweep");
				put(tag, "inner", s.inner(), "radius", s.radius(), "degrees", s.degrees(), "band", s.bandDegrees());
				tag.putBoolean("clockwise", s.clockwise());
				tag.putInt("sweep_ticks", s.sweepTicks());
			}
			case BossFootprint.Ring s -> {
				tag.putString("type", "ring");
				put(tag, "from", s.from(), "to", s.to(), "band", s.band());
				tag.putInt("waves", s.waves());
				tag.putInt("wave_gap", s.waveGap());
				tag.putInt("travel_ticks", s.travelTicks());
			}
			case BossFootprint.Disc s -> {
				tag.putString("type", "disc");
				tag.putString("at", s.at().name());
				tag.putDouble("radius", s.radius());
			}
			case BossFootprint.Annulus s -> {
				tag.putString("type", "annulus");
				put(tag, "inner", s.inner(), "outer", s.outer(), "arc_degrees", s.arcDegrees(), "revolutions", s.revolutions());
				tag.putInt("arcs", s.arcs());
				tag.putInt("active_ticks", s.activeTicks());
			}
			case BossFootprint.Cross s -> {
				tag.putString("type", "cross");
				tag.putString("at", s.at().name());
				put(tag, "arm", s.arm(), "half_width", s.halfWidth());
			}
			case BossFootprint.Discs s -> {
				tag.putString("type", "discs");
				tag.putDouble("radius", s.radius());
				tag.putInt("landing_gap", s.landingGap());
			}
			case BossFootprint.Path s -> {
				tag.putString("type", "path");
				put(tag, "half_width", s.halfWidth(), "front_speed", s.frontSpeed(), "front_length", s.frontLength(),
						"spacing", s.spacing());
				tag.putInt("walkers", s.walkers());
			}
			case BossFootprint.Region s -> {
				tag.putString("type", "region");
				tag.putString("area", s.area().name());
				tag.putDouble("radius", s.radius());
			}
			case BossFootprint.Tether s -> {
				tag.putString("type", "tether");
				tag.putString("from", s.from().name());
				tag.putString("to", s.to().name());
				tag.putInt("anchor", s.anchor());
				put(tag, "half_width", s.halfWidth(), "range", s.range());
			}
			case BossFootprint.Union s -> {
				tag.putString("type", "union");
				ListTag parts = new ListTag();
				for (BossFootprint.Union.Part part : s.parts()) {
					CompoundTag entry = new CompoundTag();
					entry.put("shape", save(part.shape()));
					entry.putInt("delay", part.delay());
					parts.add(entry);
				}
				tag.put("parts", parts);
			}
			case BossFootprint.Occluded s -> {
				tag.putString("type", "occluded");
				tag.put("shape", save(s.shape()));
			}
			case BossFootprint.None s -> tag.putString("type", "none");
		}
		if (!(shape instanceof BossFootprint.Union) && !(shape instanceof BossFootprint.Occluded)
				&& !(shape instanceof BossFootprint.None)) {
			Height height = shape.height();
			tag.putDouble("height_min", height.min());
			tag.putDouble("height_max", height.max());
			tag.putBoolean("ground_only", height.groundOnly());
		}
		return tag;
	}

	public static BossFootprint load(CompoundTag tag) {
		Height height = new Height(tag.getDouble("height_min"), tag.getDouble("height_max"), tag.getBoolean("ground_only"));
		return switch (tag.getString("type")) {
			case "lane" -> new BossFootprint.Lane(tag.getDouble("length"), tag.getDouble("half_width"),
					tag.getInt("side_lanes"), tag.getDouble("spacing"), tag.getDouble("fan"), tag.getDouble("front_speed"),
					tag.getInt("side_delay"), height);
			case "spokes" -> new BossFootprint.Spokes(tag.getInt("count"), tag.getDouble("half_width"),
					tag.getDouble("inner"), tag.getDouble("length"), tag.getDouble("offset"), tag.getDouble("front_speed"),
					height);
			case "cone" -> new BossFootprint.Cone(tag.getDouble("inner"), tag.getDouble("radius"),
					tag.getDouble("degrees"), tag.getDouble("offset"), height);
			case "sweep" -> new BossFootprint.Sweep(tag.getDouble("inner"), tag.getDouble("radius"),
					tag.getDouble("degrees"), tag.getDouble("band"), tag.getBoolean("clockwise"), tag.getInt("sweep_ticks"),
					height);
			case "ring" -> new BossFootprint.Ring(tag.getDouble("from"), tag.getDouble("to"), tag.getDouble("band"),
					tag.getInt("waves"), tag.getInt("wave_gap"), tag.getInt("travel_ticks"), height);
			case "disc" -> new BossFootprint.Disc(anchor(tag.getString("at")), tag.getDouble("radius"), height);
			case "annulus" -> new BossFootprint.Annulus(tag.getDouble("inner"), tag.getDouble("outer"),
					tag.getInt("arcs"), tag.getDouble("arc_degrees"), tag.getDouble("revolutions"), tag.getInt("active_ticks"),
					height);
			case "cross" -> new BossFootprint.Cross(anchor(tag.getString("at")), tag.getDouble("arm"),
					tag.getDouble("half_width"), height);
			case "discs" -> new BossFootprint.Discs(tag.getDouble("radius"), tag.getInt("landing_gap"), height);
			case "path" -> new BossFootprint.Path(tag.getDouble("half_width"), tag.getDouble("front_speed"),
					tag.getDouble("front_length"), tag.getInt("walkers"), tag.getDouble("spacing"), height);
			case "region" -> new BossFootprint.Region(area(tag.getString("area")), tag.getDouble("radius"), height);
			case "tether" -> new BossFootprint.Tether(end(tag.getString("from")), end(tag.getString("to")),
					tag.getInt("anchor"), tag.getDouble("half_width"), tag.getDouble("range"), height);
			case "union" -> {
				List<BossFootprint.Union.Part> parts = new ArrayList<>();
				ListTag list = tag.getList("parts", Tag.TAG_COMPOUND);
				for (int i = 0; i < list.size(); i++) {
					CompoundTag entry = list.getCompound(i);
					parts.add(new BossFootprint.Union.Part(load(entry.getCompound("shape")), entry.getInt("delay")));
				}
				yield new BossFootprint.Union(parts);
			}
			case "occluded" -> new BossFootprint.Occluded(load(tag.getCompound("shape")));
			default -> BossFootprint.None.INSTANCE;
		};
	}

	private static void put(CompoundTag tag, Object... pairs) {
		for (int i = 0; i + 1 < pairs.length; i += 2) tag.putDouble((String) pairs[i], (Double) pairs[i + 1]);
	}

	private static BossFootprint.Anchor anchor(String name) {
		try {
			return BossFootprint.Anchor.valueOf(name);
		} catch (IllegalArgumentException unknown) {
			return BossFootprint.Anchor.ORIGIN;
		}
	}

	private static BossFootprint.Area area(String name) {
		try {
			return BossFootprint.Area.valueOf(name);
		} catch (IllegalArgumentException unknown) {
			return BossFootprint.Area.BOSS_FLOOR;
		}
	}

	private static BossFootprint.End end(String name) {
		try {
			return BossFootprint.End.valueOf(name);
		} catch (IllegalArgumentException unknown) {
			return BossFootprint.End.BODY;
		}
	}
}

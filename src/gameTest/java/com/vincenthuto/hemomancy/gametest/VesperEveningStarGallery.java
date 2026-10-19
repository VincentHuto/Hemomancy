package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code es_gallery} review scene: a frozen Evening Star walked through each weapon's stance and a set of its
 * moves, the move clock driven tick by tick from here so every frame lands on the beat it names. Each move is shot
 * on its wind-up, its first contact and its follow-through.
 */
final class VesperEveningStarGallery {
	private static final int SETTLE = 36;
	private static final int GAP = 6;

	private record Entry(EnumBloodTendency tendency, boolean raging, VesperWeaponAction action, int start) {
		int moveStart() {
			return start + SETTLE;
		}

		int end() {
			return moveStart() + action.durationTicks() + GAP;
		}
	}

	private record Shot(int tick, String label) {
	}

	private static final List<Entry> ENTRIES = new ArrayList<>();
	private static final List<Shot> SHOTS = new ArrayList<>();
	static final int LENGTH;

	static {
		Object[][] plan = {
				{ EnumBloodTendency.ANIMUS, false, VesperWeaponAction.ICHIMONJI },
				{ EnumBloodTendency.ANIMUS, false, VesperWeaponAction.CROSSCUT },
				{ EnumBloodTendency.MORTEM, false, VesperWeaponAction.REAPER_SWEEP },
				{ EnumBloodTendency.MORTEM, false, VesperWeaponAction.AXE_GRAVE_SPLITTER },
				{ EnumBloodTendency.LUX, false, VesperWeaponAction.LANCE_FLURRY },
				{ EnumBloodTendency.LUX, false, VesperWeaponAction.SPEAR_PINNING_THRUST },
				{ EnumBloodTendency.TENEBRIS, false, VesperWeaponAction.TWIN_REND },
				{ EnumBloodTendency.TENEBRIS, false, VesperWeaponAction.CLAW_EVISCERATE },
				{ EnumBloodTendency.DUCTILIS, false, VesperWeaponAction.STORM_LOCK },
				{ EnumBloodTendency.FLAMMEUS, false, VesperWeaponAction.TORCH_CINDER_SWEEP },
				{ EnumBloodTendency.CONGEATIO, false, VesperWeaponAction.CHAIN_SWEEP },
				{ EnumBloodTendency.CONGEATIO, false, VesperWeaponAction.FLAIL_SHATTER_SLAM },
				{ EnumBloodTendency.FERRIC, false, VesperWeaponAction.STAFF_LODESTONE_SWEEP },
				{ EnumBloodTendency.FERRIC, false, VesperWeaponAction.MAGNETIC_AXIS },
				{ EnumBloodTendency.FERRIC, true, VesperWeaponAction.SICKLE_REAP_R },
				{ EnumBloodTendency.FERRIC, true, VesperWeaponAction.SICKLE_CROSS_REND },
				{ EnumBloodTendency.FERRIC, true, VesperWeaponAction.SICKLE_CYCLONE },
				{ EnumBloodTendency.FERRIC, true, VesperWeaponAction.SICKLE_CRESCENT_STAGGER },
				{ EnumBloodTendency.FERRIC, true, VesperWeaponAction.SICKLE_HOOK_REEL },
		};
		int at = 2;
		EnumBloodTendency lastTendency = null;
		boolean lastRaging = false;
		for (Object[] row : plan) {
			Entry entry = new Entry((EnumBloodTendency) row[0], (Boolean) row[1], (VesperWeaponAction) row[2], at);
			ENTRIES.add(entry);
			if (entry.tendency() != lastTendency || entry.raging() != lastRaging) {
				SHOTS.add(new Shot(entry.moveStart() - 2, "stance-" + (entry.raging() ? "sickles"
						: entry.tendency().name().toLowerCase())));
			}
			lastTendency = entry.tendency();
			lastRaging = entry.raging();
			VesperWeaponAction action = entry.action();
			String name = action.name().toLowerCase();
			int first = action.contactTick(0);
			SHOTS.add(new Shot(entry.moveStart() + Math.max(1, first - 5), name + "-a-windup"));
			SHOTS.add(new Shot(entry.moveStart() + first, name + "-b-contact"));
			SHOTS.add(new Shot(entry.moveStart() + first + 3, name + "-c-follow"));
			if (action.contactCount() > 1) {
				SHOTS.add(new Shot(entry.moveStart() + action.contactTick(action.contactCount() - 1), name + "-d-last"));
			}
			at = entry.end();
		}
		LENGTH = at + 4;
	}

	private VesperEveningStarGallery() {
	}

	static int[] shots() {
		return SHOTS.stream().mapToInt(Shot::tick).toArray();
	}

	static String label(int tick) {
		for (Shot shot : SHOTS) if (shot.tick() == tick) return shot.label();
		return Integer.toString(tick);
	}

	/** Puts the frozen Evening Star into whatever the plan holds at {@code tick}. */
	static void drive(VesperTheEveningStarEntity vesper, int tick) {
		for (Entry entry : ENTRIES) {
			if (tick < entry.start() || tick >= entry.end()) continue;
			set(vesper, "DATA_TENDENCY", entry.tendency().ordinal());
			set(vesper, "DATA_RAGING", entry.raging());
			// A fresh stance only for the first move on a weapon, so its flourish is seen once and then settles.
			int stanceStart = ENTRIES.stream().filter(e -> e.tendency() == entry.tendency()
					&& e.raging() == entry.raging()).findFirst().orElse(entry).start();
			set(vesper, "DATA_STANCE_TICK", tick - stanceStart);
			int moveTick = tick - entry.moveStart();
			VesperWeaponAction action = entry.action();
			if (moveTick == 0) {
				invoke(vesper, "beginWeaponAction", new Class<?>[] { VesperWeaponAction.class, Vec3.class }, action,
						vesper.position().add(0.0D, 0.0D, -6.0D));
			}
			if (moveTick >= 0 && moveTick <= action.durationTicks()) set(vesper, "DATA_ACTION_TICK", moveTick);
			if (moveTick > action.durationTicks()) {
				invoke(vesper, "clearWeaponAction", new Class<?>[0]);
			}
			return;
		}
	}

	@SuppressWarnings("unchecked")
	private static void set(VesperTheEveningStarEntity entity, String accessor, Object value) {
		try {
			Field field = VesperTheEveningStarEntity.class.getDeclaredField(accessor);
			field.setAccessible(true);
			entity.getEntityData().set((EntityDataAccessor<Object>) field.get(null), value);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Gallery data " + accessor + " unavailable", exception);
		}
	}

	private static void invoke(Object target, String name, Class<?>[] types, Object... values) {
		try {
			Method method = VesperTheEveningStarEntity.class.getDeclaredMethod(name, types);
			method.setAccessible(true);
			method.invoke(target, values);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Gallery hook " + name + " unavailable", exception);
		}
	}
}

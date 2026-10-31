package com.vincenthuto.hemomancy.client.model.entity.morphling;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingActiveState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * What a strain model needs for one frame: the maturity, the synced active bits, animation time and the light the
 * wearer stands in. State motions (a sail raising, a bell closing, roots growing) settle toward their bits instead
 * of snapping; the settled values are kept per wearer, so a Court NPC wearing the same model settles on its own.
 */
public final class MorphlingAnimContext {
	private static final Map<Entity, Settled> SETTLED = new WeakHashMap<>();

	private final Entity wearer;
	private final int maturity;
	private final int flags;
	private final float time;
	private final float daylight;

	private MorphlingAnimContext(Entity wearer, int maturity, int flags, float time, float daylight) {
		this.wearer = wearer;
		this.maturity = maturity;
		this.flags = flags;
		this.time = time;
		this.daylight = daylight;
	}

	/**
	 * @param time animation time in ticks, from {@code AnimationTime} (the render's age-in-ticks is already that)
	 */
	public static MorphlingAnimContext of(Entity wearer, int maturity, int flags, float time) {
		return new MorphlingAnimContext(wearer, maturity, flags, time, daylight(wearer));
	}

	private static float daylight(Entity wearer) {
		if (wearer == null || wearer.level() == null) return 0.0F;
		BlockPos pos = BlockPos.containing(wearer.getEyePosition());
		float sky = wearer.level().getBrightness(LightLayer.SKY, pos) / 15.0F;
		float day = Mth.clamp((float) Math.cos(wearer.level().getTimeOfDay(1.0F) * Math.PI * 2.0D) * 2.0F + 0.5F, 0.0F, 1.0F);
		return sky * day;
	}

	public int maturity() {
		return maturity;
	}

	public int flags() {
		return flags;
	}

	public float time() {
		return time;
	}

	public float daylight() {
		return daylight;
	}

	public boolean has(int bits) {
		return (flags & bits) != 0;
	}

	/** Mermaid's Purse's banked blood, 0..1. */
	public float fill() {
		return (float) MorphlingActiveState.fill(flags);
	}

	/** 0..1, settling toward whether any of {@code bits} is on. */
	public float settled(int bits) {
		if (bits == 0) return 0.0F;
		boolean on = has(bits);
		if (wearer == null) return on ? 1.0F : 0.0F;
		Settled state = SETTLED.computeIfAbsent(wearer, entity -> new Settled());
		float[] value = state.values.computeIfAbsent(bits, key -> new float[] {on ? 1.0F : 0.0F, time});
		float dt = Mth.clamp(time - value[1], 0.0F, 20.0F);
		value[0] = MorphlingStrainMotion.settle(value[0], on, dt);
		value[1] = time;
		return value[0];
	}

	private static final class Settled {
		private final Map<Integer, float[]> values = new HashMap<>();
	}
}

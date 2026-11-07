package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.client.render.world.HemorathTelegraphStyle;
import com.vincenthuto.hemomancy.client.render.world.PutricielTelegraphStyle;
import com.vincenthuto.hemomancy.client.render.world.SaintTelegraphStyle;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeTelegraphStyle;
import com.vincenthuto.hemomancy.client.render.world.VelorumTelegraphStyle;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterStyle;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.HemorathMeterStyle;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.PutricielMeterStyle;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.SeraphaeMeterStyle;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.VelorumMeterStyle;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphFamily;

import java.util.EnumMap;
import java.util.Map;

/**
 * Each Saint's client art for the combat kit: its meter style (under its bar), its telegraph style, and the families
 * its footprints take when a move's {@code TelegraphSpec} names none. One row per Saint; a Saint's builder rewrites its
 * two style classes in place and never edits this table.
 */
public final class SaintClientStyles {
	public record Entry(SaintMeterStyle meter, SaintTelegraphStyle telegraph, TelegraphFamily fill, TelegraphFamily edge) {
	}

	private static final Map<EnumSaintType, Entry> STYLES = new EnumMap<>(EnumSaintType.class);

	static {
		STYLES.put(EnumSaintType.HEMORATH, new Entry(new HemorathMeterStyle(), new HemorathTelegraphStyle(),
				TelegraphFamily.ANIMUS, TelegraphFamily.ANIMUS));
		STYLES.put(EnumSaintType.SERAPHAE, new Entry(new SeraphaeMeterStyle(), new SeraphaeTelegraphStyle(),
				TelegraphFamily.LUX, TelegraphFamily.DUCTILIS));
		STYLES.put(EnumSaintType.PUTRICIEL, new Entry(new PutricielMeterStyle(), new PutricielTelegraphStyle(),
				TelegraphFamily.PUTRICIEL_ROT, TelegraphFamily.PUTRICIEL_ROT));
		STYLES.put(EnumSaintType.VELORUM, new Entry(new VelorumMeterStyle(), new VelorumTelegraphStyle(),
				TelegraphFamily.RIME, TelegraphFamily.UMBRA));
	}

	private SaintClientStyles() {
	}

	public static Entry get(EnumSaintType saint) {
		Entry entry = STYLES.get(saint);
		if (entry == null) throw new IllegalStateException("No client styles for Saint " + saint);
		return entry;
	}

	public static SaintMeterStyle meter(EnumSaintType saint) {
		return get(saint).meter();
	}

	public static SaintTelegraphStyle telegraph(EnumSaintType saint) {
		return get(saint).telegraph();
	}

	public static TelegraphFamily fill(EnumSaintType saint) {
		return get(saint).fill();
	}

	public static TelegraphFamily edge(EnumSaintType saint) {
		return get(saint).edge();
	}
}

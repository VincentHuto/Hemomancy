package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.google.common.base.Suppliers;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

/**
 * One row of the Morphling strain table. Everything that used to be a hand-kept per-strain list (passive effect,
 * Cradle whitelist, Last Rite, polyp family, HUD art, creative order) reads it. The item and passive effect are both
 * registered as {@code morphling_<id>}; their holders are looked up on first use, so the table loads without a
 * Minecraft bootstrap.
 */
public final class MorphlingStrain {
	public static final String NO_LAST_RITE = "";

	private final String id;
	private final EnumBloodTendency preferred;
	private final EnumBloodTendency secondary;
	private final CourtHouse house;
	private final MorphlingHomeCondition homeCondition;
	private final boolean cradlePrimal;
	private final String lastRiteId;
	private final int lastRiteStage;
	private final MorphlingPolypLayer polypFamily;
	private final int accentColor;
	private final int creativeOrder;
	private final int tintColor;
	private final Supplier<DeferredHolder<Item, Item>> item;
	private final Supplier<DeferredHolder<MobEffect, MobEffect>> passiveEffect;

	/**
	 * @param lastRiteId    the shared Last Rite source this strain arms, or {@link #NO_LAST_RITE}
	 * @param lastRiteStage the maturity level ({@link PrimalMorphlingRules}) at which it arms
	 * @param accentColor   ARGB HUD accent; its RGB is also the passive effect colour
	 * @param tintColor     ARGB body tint at Apex for the Unfed-Fledgling look (the spec's appearance tables); alpha is
	 *                      the overlay's opacity
	 */
	MorphlingStrain(String id, EnumBloodTendency preferred, EnumBloodTendency secondary, CourtHouse house,
			MorphlingHomeCondition homeCondition, boolean cradlePrimal, String lastRiteId, int lastRiteStage, MorphlingPolypLayer polypFamily,
			int accentColor, int creativeOrder, int tintColor) {
		this.id = id;
		this.preferred = preferred;
		this.secondary = secondary;
		this.house = house;
		this.homeCondition = homeCondition;
		this.cradlePrimal = cradlePrimal;
		this.lastRiteId = lastRiteId;
		this.lastRiteStage = lastRiteStage;
		this.polypFamily = polypFamily;
		this.accentColor = accentColor;
		this.creativeOrder = creativeOrder;
		this.tintColor = tintColor;
		String path = "morphling_" + id;
		this.item = Suppliers.memoize(() -> DeferredHolder.create(Registries.ITEM, Hemomancy.rloc(path)));
		this.passiveEffect = Suppliers.memoize(() -> DeferredHolder.create(Registries.MOB_EFFECT, Hemomancy.rloc(path)));
	}

	public String id() {
		return this.id;
	}

	public EnumBloodTendency preferred() {
		return this.preferred;
	}

	public EnumBloodTendency secondary() {
		return this.secondary;
	}

	public CourtHouse house() {
		return this.house;
	}

	/** Where the strain is at home (spec, "Home condition"); read it through {@link MorphlingHomeConditions#isHome}. */
	public MorphlingHomeCondition homeCondition() {
		return this.homeCondition;
	}

	public boolean cradlePrimal() {
		return this.cradlePrimal;
	}

	public String lastRiteId() {
		return this.lastRiteId;
	}

	public int lastRiteStage() {
		return this.lastRiteStage;
	}

	public boolean hasLastRite() {
		return !this.lastRiteId.isEmpty();
	}

	public MorphlingPolypLayer polypFamily() {
		return this.polypFamily;
	}

	/** HUD sprite name under {@code textures/gui/morphling_overlay/}. */
	public String hudTexture() {
		return this.id;
	}

	public int accentColor() {
		return this.accentColor;
	}

	/** ARGB body tint the strain gives its host; alpha is the overlay opacity at Apex. */
	public int tintColor() {
		return this.tintColor;
	}

	public int creativeOrder() {
		return this.creativeOrder;
	}

	public DeferredHolder<Item, Item> item() {
		return this.item.get();
	}

	public DeferredHolder<MobEffect, MobEffect> passiveEffect() {
		return this.passiveEffect.get();
	}

	/** Registry path of the item and of the passive effect. */
	public String itemPath() {
		return "morphling_" + this.id;
	}

	public String folkNameKey() {
		return "item.hemomancy." + itemPath();
	}

	public String binomialKey() {
		return "morphling.hemomancy." + this.id + ".binomial";
	}

	/** The Order's one-line description, shown under the binomial. */
	public String descriptionKey() {
		return "morphling.hemomancy." + this.id + ".description";
	}

	/** What the organism looks like on its host at {@code maturity} (0 Unfed to 5 Primal). */
	public String stageKey(int maturity) {
		return "morphling.hemomancy." + this.id + ".stage." + STAGE_NAMES[Math.max(0, Math.min(STAGE_NAMES.length - 1, maturity))];
	}

	/** The house line, shown only once the Court is known (decision M7). */
	public String courtKey() {
		return "morphling.hemomancy." + this.id + ".court";
	}

	/** Stage names in maturity order, as the text keys spell them. */
	public static final String[] STAGE_NAMES = {"unfed", "fledgling", "developing", "mature", "apex", "primal"};

	public String passiveEffectKey() {
		return "effect.hemomancy." + itemPath();
	}

	@Override
	public String toString() {
		return "MorphlingStrain[" + this.id + "]";
	}
}

package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import javax.annotation.Nullable;

/**
 * The surrogates a Silentium can drag into the rite, and the facsimile body each one gives the will in the Gardens.
 * Each mob body brings no equipment; only the strong ones (enderman, blaze, golem) carry a further cost.
 */
public enum SurrogateForm {
	BAT("minecraft:bat", "hemomancy:facsimile_bat", 3, 0.0, 0.0, true, false, false, false, 100),
	ENDERMAN("minecraft:enderman", "hemomancy:facsimile_enderman", 6, 0.0, 0.0, false, false, false, true, 40),
	BLAZE("minecraft:blaze", "hemomancy:facsimile_blaze", 6, 0.0, 0.0, true, false, false, true, 15),
	GOLEM("minecraft:iron_golem", "hemomancy:facsimile_golem", 14, -0.4, -0.1, false, false, false, false, 60),
	HORSE("minecraft:horse", "hemomancy:facsimile_horse", 10, 0.2, 0.6, false, false, false, false, 40),
	DONKEY("minecraft:donkey", "hemomancy:facsimile_donkey", 8, -0.15, 0.0, false, true, false, false, 0),
	WOLF("minecraft:wolf", "hemomancy:facsimile_wolf", 6, 0.3, 0.0, false, false, false, false, 30),
	SPIDER("minecraft:spider", "hemomancy:facsimile_spider", 6, 0.0, 0.0, false, false, false, false, 0),
	ARMOR_STAND("minecraft:armor_stand", "", 8, 0.0, 0.0, false, false, true, false, 0);

	private final String surrogateId;
	private final String facsimileId;
	private final int pool;
	private final double speed;
	private final double jump;
	private final boolean flies;
	private final boolean keepsInventory;
	private final boolean wearsStandGear;
	private final boolean waterHurts;
	private final int abilityCooldown;

	SurrogateForm(String surrogateId, String facsimileId, int pool, double speed, double jump, boolean flies,
			boolean keepsInventory, boolean wearsStandGear, boolean waterHurts, int abilityCooldown) {
		this.surrogateId = surrogateId;
		this.facsimileId = facsimileId;
		this.pool = pool;
		this.speed = speed;
		this.jump = jump;
		this.flies = flies;
		this.keepsInventory = keepsInventory;
		this.wearsStandGear = wearsStandGear;
		this.waterHurts = waterHurts;
		this.abilityCooldown = abilityCooldown;
	}

	/** The form a surrogate of this exact vanilla type gives, or null: facsimiles and look-alikes never count. */
	@Nullable
	public static SurrogateForm fromEntityTypeId(String id) {
		for (SurrogateForm form : values()) {
			if (form.surrogateId.equals(id)) return form;
		}
		return null;
	}

	public String surrogateId() { return surrogateId; }
	/** The facsimile the form is drawn as; empty when the player is drawn as themselves (the armor stand). */
	public String facsimileId() { return facsimileId; }
	public int pool() { return pool; }
	/** Multiplier added to movement speed (-0.4 is 40% slower). */
	public double speed() { return speed; }
	/** Multiplier added to jump strength. */
	public double jump() { return jump; }
	public boolean flies() { return flies; }
	public boolean keepsInventory() { return keepsInventory; }
	public boolean wearsStandGear() { return wearsStandGear; }
	public boolean waterHurts() { return waterHurts; }
	/** Ticks the form's triggered wheel gift rests; 0 when the form has none. */
	public int abilityCooldown() { return abilityCooldown; }
}

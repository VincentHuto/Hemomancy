package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;

public enum EnumSaintType {

	HEMORATH("Hemorath",
			EnumBloodTendency.MORTEM, EnumBloodTendency.ANIMUS,
			EnumDoctrineTag.JUDGMENT, EnumDoctrineTag.SILENCE),

	SERAPHAE("Seraphae",
			EnumBloodTendency.LUX, EnumBloodTendency.DUCTILIS,
			EnumDoctrineTag.WITNESS, EnumDoctrineTag.ABSOLUTION),

	PUTRICIEL("Putriciel",
			EnumBloodTendency.MORTEM, EnumBloodTendency.FLAMMEUS,
			EnumDoctrineTag.ABSOLUTION, EnumDoctrineTag.WITNESS),

	VELORUM("Velorum",
			EnumBloodTendency.CONGEATIO, EnumBloodTendency.TENEBRIS,
			EnumDoctrineTag.MARTYRDOM, EnumDoctrineTag.SILENCE);

	private final String displayName;
	private final EnumBloodTendency primaryTendency;
	private final EnumBloodTendency secondaryTendency;
	private final EnumDoctrineTag primaryDoctrine;
	private final EnumDoctrineTag secondaryDoctrine;

	EnumSaintType(String displayName, EnumBloodTendency primaryTendency, EnumBloodTendency secondaryTendency,
			EnumDoctrineTag primaryDoctrine, EnumDoctrineTag secondaryDoctrine) {
		this.displayName = displayName;
		this.primaryTendency = primaryTendency;
		this.secondaryTendency = secondaryTendency;
		this.primaryDoctrine = primaryDoctrine;
		this.secondaryDoctrine = secondaryDoctrine;
	}

	public String getDisplayName() {
		return displayName;
	}

	public EnumBloodTendency getPrimaryTendency() {
		return primaryTendency;
	}

	public EnumBloodTendency getSecondaryTendency() {
		return secondaryTendency;
	}

	public EnumDoctrineTag getPrimaryDoctrine() {
		return primaryDoctrine;
	}

	public EnumDoctrineTag getSecondaryDoctrine() {
		return secondaryDoctrine;
	}

	/**
	 * Calculates the extraction success chance based on the player's alignment
	 * with this saint's tendencies. Higher alignment with either tendency
	 * increases success rate.
	 *
	 * @param primaryAlignment  player's alignment with primary tendency (0-100)
	 * @param secondaryAlignment player's alignment with secondary tendency (0-100)
	 * @return success chance from 0.0 to 1.0
	 */
	public float getBaseSuccessChance(float primaryAlignment, float secondaryAlignment) {
		// Base 15% chance, plus up to 55% from tendency alignment
		float tendencyBonus = (primaryAlignment * 0.35f + secondaryAlignment * 0.20f) / 100.0f;
		return Math.min(0.70f, 0.15f + tendencyBonus);
	}

	/**
	 * Returns true if the given tendency is aligned (same as primary or secondary)
	 * with this saint.
	 */
	public boolean isAligned(EnumBloodTendency tendency) {
		return tendency == primaryTendency || tendency == secondaryTendency;
	}

	/**
	 * Returns true if the given tendency is opposed to this saint's tendencies.
	 * Opposition means neither the primary nor secondary tendency matches.
	 */
	public boolean isOpposed(EnumBloodTendency tendency) {
		return !isAligned(tendency);
	}
}

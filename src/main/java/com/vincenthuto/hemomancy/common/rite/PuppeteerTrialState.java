package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * The Puppeteer trial's own state inside an {@link ActiveCardinalRite}: the summoned puppet, the crossbar it answers
 * to and how far it has been beaten down. Saved flat in the rite's tag under its {@code PuppeteerTrial*} keys.
 */
public final class PuppeteerTrialState {
	private String summonName = "";
	private UUID entityId;
	private UUID crossbarId;
	private boolean manifested;
	private boolean defeated;
	private float progress;
	private int missingTicks;

	/** Records the manifested puppet; false (and nothing changes) for a missing name, puppet or crossbar. */
	boolean begin(String summonName, UUID entityId, UUID crossbarId) {
		if (summonName == null || summonName.isBlank() || entityId == null || crossbarId == null) return false;
		this.summonName = summonName;
		this.entityId = entityId;
		this.crossbarId = crossbarId;
		manifested = true;
		defeated = false;
		progress = 0.0F;
		missingTicks = 0;
		return true;
	}

	public void updateHealth(float health, float maxHealth) {
		if (!manifested || defeated || maxHealth <= 0.0F) return;
		progress = Math.clamp(1.0F - Math.max(0.0F, health) / maxHealth, 0.0F, 1.0F);
	}

	/** Marks the tracked puppet beaten; false if it already was or another entity died. */
	boolean markDefeated(UUID entityId) {
		if (defeated || entityId == null || !entityId.equals(this.entityId)) return false;
		defeated = true;
		progress = 1.0F;
		return true;
	}

	public int incrementMissingTicks() {
		return ++missingTicks;
	}

	public void resetMissingTicks() {
		missingTicks = 0;
	}

	public String summonName() { return summonName; }
	public UUID entityId() { return entityId; }
	public UUID crossbarId() { return crossbarId; }
	public boolean isManifested() { return manifested; }
	public boolean isDefeated() { return defeated; }
	public float progress() { return progress; }
	public int missingTicks() { return missingTicks; }

	void write(CompoundTag tag) {
		if (!summonName.isBlank()) tag.putString("PuppeteerTrialSummon", summonName);
		if (entityId != null) tag.putUUID("PuppeteerTrialEntity", entityId);
		if (crossbarId != null) tag.putUUID("PuppeteerTrialCrossbar", crossbarId);
		tag.putBoolean("PuppeteerTrialManifested", manifested);
		tag.putBoolean("PuppeteerTrialDefeated", defeated);
		tag.putFloat("PuppeteerTrialProgress", progress);
		tag.putInt("PuppeteerTrialMissingTicks", missingTicks);
	}

	void read(CompoundTag tag) {
		summonName = tag.getString("PuppeteerTrialSummon");
		entityId = tag.hasUUID("PuppeteerTrialEntity") ? tag.getUUID("PuppeteerTrialEntity") : null;
		crossbarId = tag.hasUUID("PuppeteerTrialCrossbar") ? tag.getUUID("PuppeteerTrialCrossbar") : null;
		manifested = tag.getBoolean("PuppeteerTrialManifested");
		defeated = tag.getBoolean("PuppeteerTrialDefeated");
		progress = tag.getFloat("PuppeteerTrialProgress");
		missingTicks = tag.getInt("PuppeteerTrialMissingTicks");
	}
}

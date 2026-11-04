package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

public interface IBloodVolume {

	public void addBloodVolume(double points);

	/** Owner sync (player volumes only): a change raises the flag; {@code PlayerStateSync.flushDirty} sends and clears it. */
	default boolean isSyncDirty() {
		return false;
	}

	default void markSyncDirty() {
	}

	default void clearSyncDirty() {
	}

	/** Game time of the last diagnostics send, or a negative number when none was sent; player volumes only. */
	default long lastDiagnosticsSync() {
		return -1L;
	}

	default void setLastDiagnosticsSync(long gameTime) {
	}

	public void addMaxBloodVolume(double points);

	public boolean drain(double points);

	public boolean fill(double points);

	public Bloodline getBloodLine();

	public double getBloodVolume();

	public double getMaxBloodVolume();

	public boolean isActive();

	public boolean isEmpty();

	public boolean isFull();

	public void setActive(boolean set);

	public void setBloodLine(Bloodline bloodLine);

	public void setBloodVolume(double points);

	public void setMaxBloodVolume(double points);

	public void subtractBloodVolume(double points);

	public void subtractMaxBloodVolume(double points);

	public void toggleActive();

	public boolean wouldOverflow(double points);

	public boolean wouldOverstrain(double points);

	// ── Blood Debt Tracking (Hemorath Encounter) ──

	public void addDamage(double amount);

	public void addBloodSpend(double amount);

	public double consumeDebt();

	public double getBloodDebt();

	public void resetBloodDebt();

	// ── Bloodline Pool Donation & Auto-Draw Settings ──

	public boolean isTrickleEnabled();

	public void setTrickleEnabled(boolean enabled);

	public double getTrickleRate();

	public void setTrickleRate(double rate);

	public boolean isAutoDrawEnabled();

	public void setAutoDrawEnabled(boolean enabled);

	public double getAutoDrawThreshold();

	public void setAutoDrawThreshold(double threshold);

	public boolean isBloodRoutingOptInEnabled();

	public void setBloodRoutingOptInEnabled(boolean enabled);

}

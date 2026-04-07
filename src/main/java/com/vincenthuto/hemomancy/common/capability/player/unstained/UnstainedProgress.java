package com.vincenthuto.hemomancy.common.capability.player.unstained;

public class UnstainedProgress implements IUnstainedProgress {

    private boolean begunPurification = false;
    private float purity = 0.0f;
    private boolean clarityUnlocked = false;
    private float clarity = 0.0f;

    @Override
    public boolean hasBegunPurification() {
        return begunPurification;
    }

    @Override
    public void setBegunPurification(boolean begun) {
        this.begunPurification = begun;
    }

    @Override
    public float getPurity() {
        return purity;
    }

    @Override
    public void setPurity(float purity) {
        this.purity = Math.max(0.0f, Math.min(100.0f, purity));
    }

    @Override
    public void addPurity(float amount) {
        setPurity(this.purity + amount);
    }

    @Override
    public boolean isPurified() {
        return purity >= 100.0f;
    }

    @Override
    public boolean hasClarityUnlocked() {
        return clarityUnlocked;
    }

    @Override
    public void setClarityUnlocked(boolean unlocked) {
        this.clarityUnlocked = unlocked;
    }

    @Override
    public float getClarity() {
        return clarity;
    }

    @Override
    public void setClarity(float clarity) {
        this.clarity = Math.max(0.0f, Math.min(100.0f, clarity));
    }

    @Override
    public void addClarity(float amount) {
        setClarity(this.clarity + amount);
    }

    @Override
    public boolean isEnlightened() {
        return clarity >= 100.0f;
    }

    @Override
    public float getSilverWardStrength() {
        return purity / 100.0f;
    }

    @Override
    public float getVerdigrisAura() {
        return clarity / 100.0f;
    }
}

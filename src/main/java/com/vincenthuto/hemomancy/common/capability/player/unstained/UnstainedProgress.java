package com.vincenthuto.hemomancy.common.capability.player.unstained;

public class UnstainedProgress implements IUnstainedProgress {

    private boolean begunPurification = false;
    private float purity = 0.0f;
    private boolean clarityUnlocked = false;
    private float clarity = 0.0f;
    private long lastManipulationTick = 0L;

    // Bonus toggle state
    private boolean silverWardEnabled = true;
    private boolean verdigrisAuraEnabled = true;

    // Milestone counters
    private int hemoMobKills = 0;
    private int undeadKills = 0;
    private int hostileKills = 0;
    private int flawlessKills = 0;
    private int animalsBreed = 0;
    private int cropsPlanted = 0;
    private int advancementsEarned = 0;
    private int nightsSlept = 0;
    private int petsHealed = 0;

    // One-time milestone flags
    private boolean sleptWithHemolysis = false;
    private boolean killedFirstHemoMob = false;
    private boolean reachedAbstinence = false;
    private boolean emptiedBlood = false;
    private boolean earnedAdvancement = false;
    private boolean usedAltarOfCleansing = false;

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
        return silverWardEnabled ? purity / 100.0f : 0.0f;
    }

    @Override
    public float getVerdigrisAura() {
        return verdigrisAuraEnabled ? clarity / 100.0f : 0.0f;
    }

    @Override
    public boolean isSilverWardEnabled() {
        return silverWardEnabled;
    }

    @Override
    public void setSilverWardEnabled(boolean enabled) {
        this.silverWardEnabled = enabled;
    }

    @Override
    public boolean isVerdigrisAuraEnabled() {
        return verdigrisAuraEnabled;
    }

    @Override
    public void setVerdigrisAuraEnabled(boolean enabled) {
        this.verdigrisAuraEnabled = enabled;
    }

    @Override
    public long getLastManipulationTick() {
        return lastManipulationTick;
    }

    @Override
    public void setLastManipulationTick(long tick) {
        this.lastManipulationTick = tick;
    }

    // ── Milestone counters ──

    @Override public int getHemoMobKills() { return hemoMobKills; }
    @Override public void addHemoMobKill() { hemoMobKills++; }

    @Override public int getUndeadKills() { return undeadKills; }
    @Override public void addUndeadKill() { undeadKills++; }

    @Override public int getHostileKills() { return hostileKills; }
    @Override public void addHostileKill() { hostileKills++; }

    @Override public int getFlawlessKills() { return flawlessKills; }
    @Override public void addFlawlessKill() { flawlessKills++; }

    @Override public int getAnimalsBreed() { return animalsBreed; }
    @Override public void addAnimalBreed() { animalsBreed++; }

    @Override public int getCropsPlanted() { return cropsPlanted; }
    @Override public void addCropPlanted() { cropsPlanted++; }

    @Override public int getAdvancementsEarned() { return advancementsEarned; }
    @Override public void addAdvancementEarned() { advancementsEarned++; }

    @Override public int getNightsSlept() { return nightsSlept; }
    @Override public void addNightSlept() { nightsSlept++; }

    @Override public int getPetsHealed() { return petsHealed; }
    @Override public void addPetHealed() { petsHealed++; }

    // ── One-time milestone flags ──

    @Override public boolean hasSleptWithHemolysis() { return sleptWithHemolysis; }
    @Override public void setSleptWithHemolysis(boolean value) { sleptWithHemolysis = value; }

    @Override public boolean hasKilledFirstHemoMob() { return killedFirstHemoMob; }
    @Override public void setKilledFirstHemoMob(boolean value) { killedFirstHemoMob = value; }

    @Override public boolean hasReachedAbstinence() { return reachedAbstinence; }
    @Override public void setReachedAbstinence(boolean value) { reachedAbstinence = value; }

    @Override public boolean hasEmptiedBlood() { return emptiedBlood; }
    @Override public void setEmptiedBlood(boolean value) { emptiedBlood = value; }

    @Override public boolean hasEarnedAdvancement() { return earnedAdvancement; }
    @Override public void setEarnedAdvancement(boolean value) { earnedAdvancement = value; }

    @Override public boolean hasUsedAltarOfCleansing() { return usedAltarOfCleansing; }
    @Override public void setUsedAltarOfCleansing(boolean value) { usedAltarOfCleansing = value; }
}

package com.vincenthuto.hemomancy.common.capability.player.unstained;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class UnstainedProgress implements IUnstainedProgress, INBTSerializable<CompoundTag> {

    private boolean begunPurification = false;
	private boolean infectionSuppressed = false;
	private boolean clarityPrepared = false;
	private boolean annettaSeveranceUnlocked = false;
	private boolean claimedChurchStarterSupply = false;
	private boolean claimedPaleSilverBellReward;
	private boolean offeredSilverChalice;
	private boolean offeredPoppyWreath;
	private boolean offeredPallidIcon;
	private int letheanBrewOfferings;
	private long lastXpRewardGameTime;
	private long lastCropRewardGameTime;
	private long lastPetHealRewardGameTime;
	private long lastEmptyBloodRewardGameTime;
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
    private int acceptedObservances = 0;
    private int claimedObservances = 0;

    @Override
    public boolean hasBegunPurification() {
        return begunPurification;
    }

    @Override
    public void setBegunPurification(boolean begun) {
        this.begunPurification = begun;
    }

	@Override public boolean isInfectionSuppressed() { return infectionSuppressed; }
	@Override public void setInfectionSuppressed(boolean suppressed) { infectionSuppressed = suppressed; }
	@Override public boolean isClarityPrepared() { return clarityPrepared; }
	@Override public void setClarityPrepared(boolean prepared) { clarityPrepared = prepared; }
	@Override public boolean isAnnettaSeveranceUnlocked() { return annettaSeveranceUnlocked; }
	@Override public void setAnnettaSeveranceUnlocked(boolean unlocked) { annettaSeveranceUnlocked = unlocked; }
	@Override public boolean hasClaimedChurchStarterSupply() { return claimedChurchStarterSupply; }
	@Override public void setClaimedChurchStarterSupply(boolean claimed) { claimedChurchStarterSupply = claimed; }
	@Override public boolean hasClaimedPaleSilverBellReward() { return claimedPaleSilverBellReward; }
	@Override public void setClaimedPaleSilverBellReward(boolean claimed) { claimedPaleSilverBellReward = claimed; }
	@Override public boolean hasOfferedSilverChalice() { return offeredSilverChalice; }
	@Override public void setOfferedSilverChalice(boolean offered) { offeredSilverChalice = offered; }
	@Override public boolean hasOfferedPoppyWreath() { return offeredPoppyWreath; }
	@Override public void setOfferedPoppyWreath(boolean offered) { offeredPoppyWreath = offered; }
	@Override public boolean hasOfferedPallidIcon() { return offeredPallidIcon; }
	@Override public void setOfferedPallidIcon(boolean offered) { offeredPallidIcon = offered; }
	@Override public int getLetheanBrewOfferings() { return letheanBrewOfferings; }
	@Override public void setLetheanBrewOfferings(int offerings) { letheanBrewOfferings = Math.max(0, offerings); }
	@Override public long getLastXpRewardGameTime() { return lastXpRewardGameTime; }
	@Override public void setLastXpRewardGameTime(long gameTime) { lastXpRewardGameTime = gameTime; }
	@Override public long getLastCropRewardGameTime() { return lastCropRewardGameTime; }
	@Override public void setLastCropRewardGameTime(long gameTime) { lastCropRewardGameTime = gameTime; }
	@Override public long getLastPetHealRewardGameTime() { return lastPetHealRewardGameTime; }
	@Override public void setLastPetHealRewardGameTime(long gameTime) { lastPetHealRewardGameTime = gameTime; }
	@Override public long getLastEmptyBloodRewardGameTime() { return lastEmptyBloodRewardGameTime; }
	@Override public void setLastEmptyBloodRewardGameTime(long gameTime) { lastEmptyBloodRewardGameTime = gameTime; }

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
        return silverWardEnabled ? clarity / 100.0f : 0.0f;
    }

    @Override
    public float getVerdigrisAura() {
        return verdigrisAuraEnabled ? purity / 100.0f : 0.0f;
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
    @Override public int getAcceptedObservances() { return acceptedObservances; }
    @Override public void setAcceptedObservances(int mask) { acceptedObservances = mask; }
    @Override public int getClaimedObservances() { return claimedObservances; }
    @Override public void setClaimedObservances(int mask) { claimedObservances = mask; }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("begunPurification", begunPurification);
		tag.putBoolean("infectionSuppressed", infectionSuppressed);
		tag.putBoolean("clarityPrepared", clarityPrepared);
        tag.putBoolean("annettaSeveranceUnlocked", annettaSeveranceUnlocked);
		tag.putBoolean("claimedChurchStarterSupply", claimedChurchStarterSupply);
		tag.putBoolean("claimedPaleSilverBellReward", claimedPaleSilverBellReward);
		tag.putBoolean("offeredSilverChalice", offeredSilverChalice);
		tag.putBoolean("offeredPoppyWreath", offeredPoppyWreath);
		tag.putBoolean("offeredPallidIcon", offeredPallidIcon);
		tag.putInt("letheanBrewOfferings", letheanBrewOfferings);
		tag.putLong("lastXpRewardGameTime", lastXpRewardGameTime);
		tag.putLong("lastCropRewardGameTime", lastCropRewardGameTime);
		tag.putLong("lastPetHealRewardGameTime", lastPetHealRewardGameTime);
		tag.putLong("lastEmptyBloodRewardGameTime", lastEmptyBloodRewardGameTime);
        tag.putFloat("purity", purity);
        tag.putBoolean("clarityUnlocked", clarityUnlocked);
        tag.putFloat("clarity", clarity);
        tag.putLong("lastManipulationTick", lastManipulationTick);
        tag.putInt("hemoMobKills", hemoMobKills);
        tag.putInt("undeadKills", undeadKills);
        tag.putInt("hostileKills", hostileKills);
        tag.putInt("flawlessKills", flawlessKills);
        tag.putInt("animalsBreed", animalsBreed);
        tag.putInt("cropsPlanted", cropsPlanted);
        tag.putInt("advancementsEarned", advancementsEarned);
        tag.putInt("nightsSlept", nightsSlept);
        tag.putInt("petsHealed", petsHealed);
        tag.putBoolean("sleptWithHemolysis", sleptWithHemolysis);
        tag.putBoolean("killedFirstHemoMob", killedFirstHemoMob);
        tag.putBoolean("reachedAbstinence", reachedAbstinence);
        tag.putBoolean("emptiedBlood", emptiedBlood);
        tag.putBoolean("earnedAdvancement", earnedAdvancement);
        tag.putBoolean("silverWardEnabled", silverWardEnabled);
        tag.putBoolean("verdigrisAuraEnabled", verdigrisAuraEnabled);
        tag.putBoolean("usedAltarOfCleansing", usedAltarOfCleansing);
        tag.putInt("acceptedObservances", acceptedObservances);
        tag.putInt("claimedObservances", claimedObservances);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        begunPurification = tag.getBoolean("begunPurification");
		infectionSuppressed = tag.getBoolean("infectionSuppressed");
		clarityPrepared = tag.getBoolean("clarityPrepared");
        annettaSeveranceUnlocked = tag.getBoolean("annettaSeveranceUnlocked");
		claimedChurchStarterSupply = tag.getBoolean("claimedChurchStarterSupply");
		claimedPaleSilverBellReward = tag.getBoolean("claimedPaleSilverBellReward");
		offeredSilverChalice = tag.getBoolean("offeredSilverChalice");
		offeredPoppyWreath = tag.getBoolean("offeredPoppyWreath");
		offeredPallidIcon = tag.getBoolean("offeredPallidIcon");
		letheanBrewOfferings = tag.getInt("letheanBrewOfferings");
		lastXpRewardGameTime = tag.getLong("lastXpRewardGameTime");
		lastCropRewardGameTime = tag.getLong("lastCropRewardGameTime");
		lastPetHealRewardGameTime = tag.getLong("lastPetHealRewardGameTime");
		lastEmptyBloodRewardGameTime = tag.getLong("lastEmptyBloodRewardGameTime");
        purity = tag.getFloat("purity");
        clarityUnlocked = tag.getBoolean("clarityUnlocked");
        clarity = tag.getFloat("clarity");
        lastManipulationTick = tag.getLong("lastManipulationTick");
        for (int i = 0; i < tag.getInt("hemoMobKills"); i++) addHemoMobKill();
        for (int i = 0; i < tag.getInt("undeadKills"); i++) addUndeadKill();
        for (int i = 0; i < tag.getInt("hostileKills"); i++) addHostileKill();
        for (int i = 0; i < tag.getInt("flawlessKills"); i++) addFlawlessKill();
        for (int i = 0; i < tag.getInt("animalsBreed"); i++) addAnimalBreed();
        for (int i = 0; i < tag.getInt("cropsPlanted"); i++) addCropPlanted();
        for (int i = 0; i < tag.getInt("advancementsEarned"); i++) addAdvancementEarned();
        for (int i = 0; i < tag.getInt("nightsSlept"); i++) addNightSlept();
        for (int i = 0; i < tag.getInt("petsHealed"); i++) addPetHealed();
        sleptWithHemolysis = tag.getBoolean("sleptWithHemolysis");
        killedFirstHemoMob = tag.getBoolean("killedFirstHemoMob");
        reachedAbstinence = tag.getBoolean("reachedAbstinence");
        emptiedBlood = tag.getBoolean("emptiedBlood");
        earnedAdvancement = tag.getBoolean("earnedAdvancement");
        silverWardEnabled = !tag.contains("silverWardEnabled") || tag.getBoolean("silverWardEnabled");
        verdigrisAuraEnabled = !tag.contains("verdigrisAuraEnabled") || tag.getBoolean("verdigrisAuraEnabled");
        usedAltarOfCleansing = tag.getBoolean("usedAltarOfCleansing");
        acceptedObservances = tag.getInt("acceptedObservances");
        claimedObservances = tag.getInt("claimedObservances");
    }
}

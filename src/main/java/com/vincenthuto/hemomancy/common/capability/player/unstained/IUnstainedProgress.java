package com.vincenthuto.hemomancy.common.capability.player.unstained;

/**
 * Player capability that tracks progress along the Unstained path —
 * the divergence from hemomancy toward purification and enlightenment.
 */
public interface IUnstainedProgress {

    // --- Core Path State ---

    /** Has the player started the Unstained path? */
    boolean hasBegunPurification();

    void setBegunPurification(boolean begun);

    // --- Purity System (Phase 1) ---
    // Purity ranges from 0.0 (fully corrupted) to 100.0 (fully pure)

    float getPurity();

    void setPurity(float purity);

    /** Add purity, clamped to 0–100. */
    void addPurity(float amount);

    /** Returns true if purity >= 100.0. */
    boolean isPurified();

    // --- Clarity System (Phase 2, unlocked after purity achieved + ritual) ---

    /** Has the Rite of Clarity been performed? */
    boolean hasClarityUnlocked();

    void setClarityUnlocked(boolean unlocked);

    float getClarity();

    void setClarity(float clarity);

    /** Add clarity, clamped to 0–100. */
    void addClarity(float amount);

    /** Returns true if clarity >= 100.0 — the final enlightened state. */
    boolean isEnlightened();

    // --- Anti-Hemomancy Properties ---

    /** Resistance to blood magic (scales with purity: purity / 100). */
    float getSilverWardStrength();

    /** Copper-based anti-blood field radius (scales with clarity: clarity / 100). */
    float getVerdigrisAura();

    // --- Abstinence Tracking (for blood magic restraint purity) ---

    /** Game tick when the player last used a blood manipulation. 0 = never. */
    long getLastManipulationTick();

    void setLastManipulationTick(long tick);

    // --- Milestone Tracking (for checklist / progress UI) ---

    // Progress counters (repeatable milestones)
    int getHemoMobKills();
    void addHemoMobKill();

    int getUndeadKills();
    void addUndeadKill();

    int getHostileKills();
    void addHostileKill();

    int getFlawlessKills();
    void addFlawlessKill();

    int getAnimalsBreed();
    void addAnimalBreed();

    int getCropsPlanted();
    void addCropPlanted();

    int getAdvancementsEarned();
    void addAdvancementEarned();

    int getNightsSlept();
    void addNightSlept();

    int getPetsHealed();
    void addPetHealed();

    // One-time milestone flags
    boolean hasSleptWithHemolysis();
    void setSleptWithHemolysis(boolean value);

    boolean hasKilledFirstHemoMob();
    void setKilledFirstHemoMob(boolean value);

    boolean hasReachedAbstinence();
    void setReachedAbstinence(boolean value);

    boolean hasEmptiedBlood();
    void setEmptiedBlood(boolean value);

    boolean hasEarnedAdvancement();
    void setEarnedAdvancement(boolean value);
}

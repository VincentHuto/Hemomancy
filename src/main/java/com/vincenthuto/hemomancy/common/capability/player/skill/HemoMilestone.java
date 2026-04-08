package com.vincenthuto.hemomancy.common.capability.player.skill;

/**
 * Gameplay milestones that award skill points as the player progresses
 * through the Hemomancy / Harbinger path. Each milestone has a unique ID,
 * a skill-point reward, and a short description.
 *
 * <h3>Categories</h3>
 * <ul>
 *   <li><b>Discovery</b> — finding Hemomancy items/blocks for the first time</li>
 *   <li><b>Degree</b>    — ascending through the seven initiatory degrees</li>
 *   <li><b>Combat</b>    — tiered kill-count thresholds</li>
 *   <li><b>Practice</b>  — tiered manipulation-use thresholds</li>
 *   <li><b>Ritual</b>    — tiered cardinal-rite completion thresholds</li>
 *   <li><b>Scholarship</b> — earning Hemomancy advancements</li>
 * </ul>
 */
public enum HemoMilestone {

    // ═══════════════════════════════════════════════════
    //  DISCOVERY — Hemomancy advancement-triggered
    // ═══════════════════════════════════════════════════

    /** Find Gourd Seeds from breaking grass. */
    STRANGE_SEEDS("strange_seeds", 1, "Find Gourd Seeds"),

    /** Craft Befouling Ash. */
    ASHEN_BEGINNINGS("ashen_beginnings", 1, "Craft Befouling Ash"),

    /** Obtain the Liber Sanguinum guide book. */
    SANCTUM_SANGUINIUM("sanctum_sanguinium", 1, "Obtain the Liber Sanguinum"),

    /** Activate a Blood Temple's Mortal Display. */
    THE_FIRST_AWAKENING("the_first_awakening", 2, "Activate a Blood Temple"),

    /** Create a Hematic Iron Block via blood structure recipe. */
    IRON_IN_THE_BLOOD("iron_in_the_blood", 2, "Create Hematic Iron Block"),

    /** Obtain a Blood Gourd. */
    BOTTLED_VITALITY("bottled_vitality", 1, "Obtain a Blood Gourd"),

    /** Craft a Living Staff. */
    EXTENSION_OF_ONESELF("extension_of_oneself", 2, "Craft a Living Staff"),

    /** Obtain any Hematic Memory item. */
    INHERITED_MEMORY("inherited_memory", 2, "Obtain a Hematic Memory"),

    /** Obtain the Charm of Vascularium. */
    SCARLET_TRADITION("scarlet_tradition", 2, "Obtain Charm of Vascularium"),

    /** Equip full Hematic Iron armor. */
    ARMED_TO_THE_VEINS("armed_to_the_veins", 3, "Equip full Hematic Iron armor"),

    /** Obtain a Morphling Jar. */
    CULTIVATOR("cultivator", 2, "Obtain a Morphling Jar"),

    /** Obtain a Living Blade. */
    THE_BLOOD_REMEMBERS("the_blood_remembers", 2, "Obtain a Living Blade"),

    /** Obtain any enzyme. */
    OLD_HABITS("old_habits", 1, "Obtain any enzyme"),

    /** Craft a Juiceinator. */
    BLEEDING_A_STONE("bleeding_a_stone", 1, "Craft a Juiceinator"),

    // ═══════════════════════════════════════════════════
    //  DEGREE — Initiatory Degree advancement
    // ═══════════════════════════════════════════════════

    /** Reach Degree 1 — Neophyte of the Crimson Veil. */
    SANGUINE_INITIATION("sanguine_initiation", 3, "Complete the Rite of Sanguine Initiation"),

    /** Reach Degree 2 — Votary of the Hematic Covenant. */
    VOTARY_ASCENSION("votary_ascension", 4, "Ascend to Votary"),

    /** Reach Degree 3 — Initiate of the Scarlet Sanctum. */
    INITIATE_ASCENSION("initiate_ascension", 5, "Ascend to Initiate"),

    /** Reach Degree 4 — Adept of the Sanguine Brotherhood. */
    ADEPT_ASCENSION("adept_ascension", 6, "Ascend to Adept"),

    /** Reach Degree 5 — Illuminatus of the Crimson Lodge. */
    ILLUMINATUS_ASCENSION("illuminatus_ascension", 7, "Ascend to Illuminatus"),

    /** Reach Degree 6 — Sanctified of the Bloodline Covenant. */
    SANCTIFIED_ASCENSION("sanctified_ascension", 8, "Ascend to Sanctified"),

    /** Reach Degree 7 — Archon of the Hematic Order. */
    ARCHON_ASCENSION("archon_ascension", 10, "Ascend to Archon"),

    // ═══════════════════════════════════════════════════
    //  FIRST-TIME ACTIONS
    // ═══════════════════════════════════════════════════

    /** Use any blood manipulation for the first time. */
    FIRST_MANIPULATION("first_manipulation", 2, "Use a blood manipulation"),

    /** Complete any cardinal rite for the first time. */
    FIRST_RITE("first_rite", 3, "Complete a cardinal rite"),

    /** Equip a morphling companion for the first time. */
    FIRST_MORPHLING_BOND("first_morphling_bond", 2, "Equip a morphling"),

    /** Complete a Visceral Recaller recipe. */
    FIRST_RECALLER_CRAFT("first_recaller_craft", 3, "Complete a Visceral Recaller recipe"),

    /** Join or create a bloodline. */
    FIRST_BLOODLINE("first_bloodline", 3, "Join or create a bloodline"),

    // ═══════════════════════════════════════════════════
    //  COMBAT — Tiered kill-count milestones
    // ═══════════════════════════════════════════════════

    /** Kill 10 mobs while blood volume is active. */
    BLOOD_SPILLER_I("blood_spiller_i", 2, "Slay 10 creatures with active blood"),

    /** Kill 50 mobs while blood volume is active. */
    BLOOD_SPILLER_II("blood_spiller_ii", 3, "Slay 50 creatures with active blood"),

    /** Kill 100 mobs while blood volume is active. */
    BLOOD_SPILLER_III("blood_spiller_iii", 5, "Slay 100 creatures with active blood"),

    // ═══════════════════════════════════════════════════
    //  PRACTICE — Tiered manipulation-use milestones
    // ═══════════════════════════════════════════════════

    /** Use 25 blood manipulations total. */
    MANIPULATION_NOVICE("manipulation_novice", 2, "Use 25 blood manipulations"),

    /** Use 100 blood manipulations total. */
    MANIPULATION_ADEPT("manipulation_adept", 3, "Use 100 blood manipulations"),

    /** Use 500 blood manipulations total. */
    MANIPULATION_MASTER("manipulation_master", 5, "Use 500 blood manipulations"),

    // ═══════════════════════════════════════════════════
    //  RITUAL — Tiered rite-completion milestones
    // ═══════════════════════════════════════════════════

    /** Complete 3 cardinal rites. */
    RITE_PRACTITIONER("rite_practitioner", 3, "Complete 3 cardinal rites"),

    /** Complete 10 cardinal rites. */
    RITE_VETERAN("rite_veteran", 5, "Complete 10 cardinal rites"),

    // ═══════════════════════════════════════════════════
    //  SCHOLARSHIP — Hemomancy advancement milestones
    // ═══════════════════════════════════════════════════

    /** Earn 5 Hemomancy advancements. */
    HEMO_SCHOLAR_I("hemo_scholar_i", 2, "Earn 5 Hemomancy advancements"),

    /** Earn 10 Hemomancy advancements. */
    HEMO_SCHOLAR_II("hemo_scholar_ii", 3, "Earn 10 Hemomancy advancements");

    // ─────────────────────────────────────────────────

    private final String id;
    private final int skillPointReward;
    private final String description;

    HemoMilestone(String id, int skillPointReward, String description) {
        this.id = id;
        this.skillPointReward = skillPointReward;
        this.description = description;
    }

    /** Unique string identifier, used for serialisation and lang keys. */
    public String getId() {
        return id;
    }

    /** Skill points awarded when this milestone is completed. */
    public int getSkillPointReward() {
        return skillPointReward;
    }

    /** Short English description (fallback; prefer the lang key). */
    public String getDescription() {
        return description;
    }

    /** Lang key for the milestone name: {@code hemomancy.milestone.<id>} */
    public String getLangKey() {
        return "hemomancy.milestone." + id;
    }

    /** Lang key for the milestone description: {@code hemomancy.milestone.<id>.desc} */
    public String getDescLangKey() {
        return "hemomancy.milestone." + id + ".desc";
    }

    /** Look up a milestone by its string ID, or null if not found. */
    public static HemoMilestone byId(String id) {
        for (HemoMilestone m : values()) {
            if (m.id.equals(id)) return m;
        }
        return null;
    }
}

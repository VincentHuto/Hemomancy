package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

import net.minecraft.network.chat.Component;

/**
 * Gameplay milestones that award skill points as the player progresses
 * through the Hemomancy / Harbinger path. Each milestone has a unique ID,
 * a skill-point reward, a category for UI grouping, and a required degree
 * that gates visibility in the skill-tree drawer.
 *
 * <h3>Categories</h3>
 * <ul>
 *   <li><b>Discovery</b>  — finding Hemomancy items/blocks for the first time</li>
 *   <li><b>Degree</b>     — ascending through the initiatory degrees</li>
 *   <li><b>Action</b>     — first-time gameplay actions</li>
 *   <li><b>Combat</b>     — tiered kill-count thresholds</li>
 *   <li><b>Practice</b>   — tiered manipulation-use thresholds</li>
 *   <li><b>Ritual</b>     — tiered cardinal-rite completion thresholds</li>
 *   <li><b>Scholarship</b> — earning Hemomancy advancements</li>
 * </ul>
 */
public enum HemoMilestone {

    // ═══════════════════════════════════════════════════
    //  DISCOVERY — Hemomancy advancement-triggered
    // ═══════════════════════════════════════════════════

    /** Find Gourd Seeds from breaking grass. */
    STRANGE_SEEDS("strange_seeds", 1, Category.DISCOVERY, 0),

    /** Craft Befouling Ash. */
    ASHEN_BEGINNINGS("ashen_beginnings", 1, Category.DISCOVERY, 0),

    /** Obtain the Liber Sanguinium guide book. */
    LIBER_SANGUINIUM("liber_sanguinium", 1, Category.DISCOVERY, 0),

    /** Activate a Blood Temple's Mortal Display. */
    THE_FIRST_AWAKENING("the_first_awakening", 2, Category.DISCOVERY, 0),

    /** Create a Hematic Iron Block via blood structure recipe. */
    IRON_IN_THE_BLOOD("iron_in_the_blood", 2, Category.DISCOVERY, 0),

    /** Obtain a Blood Gourd. */
    BOTTLED_VITALITY("bottled_vitality", 1, Category.DISCOVERY, 0),

    /** Craft a Living Staff. */
    EXTENSION_OF_ONESELF("extension_of_oneself", 2, Category.DISCOVERY, 1),

    /** Obtain any Hematic Memory item. */
    INHERITED_MEMORY("inherited_memory", 2, Category.DISCOVERY, 1),

    /** Obtain the Charm of Vascularium. */
    SCARLET_TRADITION("scarlet_tradition", 2, Category.DISCOVERY, 1),

    /** Equip full Hematic Iron armor. */
    ARMED_TO_THE_VEINS("armed_to_the_veins", 3, Category.DISCOVERY, 2),

    /** Obtain a Morphling Jar. */
    CULTIVATOR("cultivator", 2, Category.DISCOVERY, 1),

    /** Obtain a Living Blade. */
    THE_BLOOD_REMEMBERS("the_blood_remembers", 2, Category.DISCOVERY, 2),

    /** Obtain any enzyme. */
    OLD_HABITS("old_habits", 1, Category.DISCOVERY, 0),

    /** Craft a Ghastly Alembic. */
    BLEEDING_A_STONE("bleeding_a_stone", 1, Category.DISCOVERY, 0),

    // ═══════════════════════════════════════════════════
    //  DEGREE — Initiatory Degree advancement
    // ═══════════════════════════════════════════════════

    /** Reach Degree 1 — Neophyte of the Crimson Veil. */
    SANGUINE_INITIATION("sanguine_initiation", 3, Category.DEGREE, 0),

    /** Reach Degree 2 — Votary of the Hematic Covenant. */
    VOTARY_ASCENSION("votary_ascension", 4, Category.DEGREE, 1),

    /** Reach Degree 3 — Initiate of the Incarnadine Fane. */
    INITIATE_ASCENSION("initiate_ascension", 5, Category.DEGREE, 2),

    /** Reach Degree 4 — Adept of the Sanguine Brotherhood. */
    ADEPT_ASCENSION("adept_ascension", 6, Category.DEGREE, 3),

    /** Reach Degree 5 — Illuminatus of the Crimson Lodge. */
    ILLUMINATUS_ASCENSION("illuminatus_ascension", 7, Category.DEGREE, 4),

    /** Reach Degree 6 — Sanctified of the Bloodline Covenant. */
    SANCTIFIED_ASCENSION("sanctified_ascension", 8, Category.DEGREE, 5),

    /** Reach Degree 7 — Archon of the Hematic Order. */
    ARCHON_ASCENSION("archon_ascension", 10, Category.DEGREE, 6),

    /** Reach Degree 8 — Apotheos of the Hematic Order. */
    APOTHEOS_ASCENSION("apotheos_ascension", 12, Category.DEGREE, 7),

    // ═══════════════════════════════════════════════════
    //  FIRST-TIME ACTIONS
    // ═══════════════════════════════════════════════════

    /** Use any blood manipulation for the first time. */
    FIRST_MANIPULATION("first_manipulation", 2, Category.ACTION, 0),

    /** Complete any cardinal rite for the first time. */
    FIRST_RITE("first_rite", 3, Category.ACTION, 1),

    /** Equip a morphling companion for the first time. */
    FIRST_MORPHLING_BOND("first_morphling_bond", 2, Category.ACTION, 1),

    /** Complete a Somatic Loom recipe. */
    FIRST_MEMORY_WEAVING("first_memory_weaving", 3, Category.ACTION, 2),

    /** Join or create a bloodline. */
    FIRST_BLOODLINE("first_bloodline", 3, Category.ACTION, 2),

    // ═══════════════════════════════════════════════════
    //  COMBAT — Tiered kill-count milestones
    // ═══════════════════════════════════════════════════

    /** Kill 10 mobs while blood volume is active. */
    BLOOD_SPILLER_I("blood_spiller_i", 2, Category.COMBAT, 0),

    /** Kill 50 mobs while blood volume is active. */
    BLOOD_SPILLER_II("blood_spiller_ii", 3, Category.COMBAT, 1),

    /** Kill 100 mobs while blood volume is active. */
    BLOOD_SPILLER_III("blood_spiller_iii", 5, Category.COMBAT, 3),

    // ═══════════════════════════════════════════════════
    //  PRACTICE — Tiered manipulation-use milestones
    // ═══════════════════════════════════════════════════

    /** Use 25 blood manipulations total. */
    MANIPULATION_NOVICE("manipulation_novice", 2, Category.PRACTICE, 0),

    /** Use 100 blood manipulations total. */
    MANIPULATION_ADEPT("manipulation_adept", 3, Category.PRACTICE, 2),

    /** Use 500 blood manipulations total. */
    MANIPULATION_MASTER("manipulation_master", 5, Category.PRACTICE, 4),

    // ═══════════════════════════════════════════════════
    //  RITUAL — Tiered rite-completion milestones
    // ═══════════════════════════════════════════════════

    /** Complete 3 cardinal rites. */
    RITE_PRACTITIONER("rite_practitioner", 3, Category.RITUAL, 1),

    /** Complete 10 cardinal rites. */
    RITE_VETERAN("rite_veteran", 5, Category.RITUAL, 3),

    // ═══════════════════════════════════════════════════
    //  SCHOLARSHIP — Hemomancy advancement milestones
    // ═══════════════════════════════════════════════════

    /** Earn 5 Hemomancy advancements. */
    HEMO_SCHOLAR_I("hemo_scholar_i", 2, Category.SCHOLARSHIP, 0),

    /** Earn 10 Hemomancy advancements. */
    HEMO_SCHOLAR_II("hemo_scholar_ii", 3, Category.SCHOLARSHIP, 2);

    // ─────────────────────────────────────────────────

    /** UI grouping categories for milestones. */
    public enum Category {
        DISCOVERY("discovery", 0xFFCC3333),
        DEGREE("degree", 0xFF8844CC),
        ACTION("actions", 0xFFCC8833),
        COMBAT("combat", 0xFFBB4444),
        PRACTICE("practice", 0xFF44AA88),
        RITUAL("ritual", 0xFF8866CC),
        SCHOLARSHIP("scholarship", 0xFFDDAA33);

        private final String id;
        private final int color;

        Category(String id, int color) {
            this.id = id;
            this.color = color;
        }

        public Component label() { return Component.translatable("hemomancy.milestone.category." + id); }
        public int getColor() { return color; }
    }

    private final String id;
    private final int skillPointReward;
    private final Category category;
    private final int requiredDegree;

    HemoMilestone(String id, int skillPointReward, Category category, int requiredDegree) {
        this.id = id;
        this.skillPointReward = skillPointReward;
        this.category = category;
        this.requiredDegree = requiredDegree;
    }

    /** Unique string identifier, used for serialisation and lang keys. */
    public String getId() {
        return id;
    }

    /** Skill points awarded when this milestone is completed. */
    public int getSkillPointReward() {
        return skillPointReward;
    }

    /** UI category this milestone belongs to. */
    public Category getCategory() {
        return category;
    }

    /**
     * Minimum initiatory degree required for this milestone to be visible
     * in the skill-tree drawer. 0 means always visible.
     */
    public int getRequiredDegree() {
        return requiredDegree;
    }

    /** Milestone description, from {@link #getDescLangKey()}. */
    public Component getDescription() {
        return Component.translatable(getDescLangKey());
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
        // Preserve earned credit from saves using the old guide milestone ID.
        if ("fane_sanguinium".equals(id)) return LIBER_SANGUINIUM;
        for (HemoMilestone m : values()) {
            if (m.id.equals(id)) return m;
        }
        return null;
    }
}

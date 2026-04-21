package com.vincenthuto.hemomancy.common.capability.player.skill;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncSkills;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Forge event handlers that award skill points through diverse gameplay
 * milestones on the Hemomancy / Harbinger path. This complements the
 * per-use skill-point drip from {@code KnownManipulationEvents} with
 * larger one-time and tiered milestone rewards.
 *
 * <h3>Advancement-Triggered Milestones</h3>
 * Earning a Hemomancy advancement (under the {@code hemomancy:hemomancy/} path)
 * awards skill points for the corresponding discovery milestone. Tiered
 * scholarship milestones trigger at 5 and 10 total Hemomancy advancements.
 *
 * <h3>Combat Milestones</h3>
 * Killing hostile mobs while the player's blood volume is active increments
 * a kill counter. Tiered milestones trigger at 10, 50, and 100 kills.
 *
 * <h3>Programmatic Milestones</h3>
 * Degree advancement, rite completion, manipulation use, morphling equipping,
 * Memory weaving, and bloodline joining are awarded by calling
 * {@link #onDegreeReached}, {@link #onRiteCompleted},
 * {@link #onManipulationUsed}, {@link #onMorphlingEquipped},
 * {@link #onMemoryWeavingCompleted}, and {@link #onBloodlineJoined}
 * from the relevant systems.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class SkillPointGainEvents {

    /** Hemomancy advancement path prefix used to identify mod-specific advancements. */
    private static final String HEMO_ADV_PREFIX = "hemomancy:hemomancy/";

    /**
     * Maps advancement names (the final path segment) to milestone IDs when they
     * differ. For example, the {@code root} advancement corresponds to finding
     * gourd seeds, which is the {@code strange_seeds} milestone.
     */
    private static final java.util.Map<String, String> ADV_TO_MILESTONE = java.util.Map.of(
            "root", "strange_seeds"
    );

    // ════════════════════════════════════════════════════════════
    //  SYNC HELPER
    // ════════════════════════════════════════════════════════════

    /** Sends the current skill tree state (including milestones) to the given client. */
    private static void syncSkills(ServerPlayer player) {
        PacketHandler.CHANNELBLOODVOLUME.send(
                PacketDistributor.PLAYER.with(() -> player),
                new PacketSyncSkills(SkillPointInit.serializeAll()));
    }

    // ════════════════════════════════════════════════════════════
    //  ADVANCEMENT-TRIGGERED MILESTONES
    // ════════════════════════════════════════════════════════════

    /**
     * When a player earns a Hemomancy advancement, check whether a matching
     * discovery milestone exists and award it. Also increment the Hemomancy
     * advancement counter and check scholarship milestones.
     */
    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ResourceLocation advId = event.getAdvancement().getId();
        String fullId = advId.toString(); // e.g. "hemomancy:hemomancy/iron_in_the_blood"

        if (!fullId.startsWith(HEMO_ADV_PREFIX)) return;

        boolean changed = false;

        // Extract the advancement name after the prefix
        String advName = fullId.substring(HEMO_ADV_PREFIX.length()); // e.g. "iron_in_the_blood"

        // Map advancement names that differ from milestone IDs
        String milestoneId = ADV_TO_MILESTONE.getOrDefault(advName, advName);

        // Try to match to a discovery milestone by ID
        HemoMilestone milestone = HemoMilestone.byId(milestoneId);
        if (milestone != null) {
            changed = SkillPointInit.tryAwardMilestone(milestone);
        }

        // Increment Hemomancy advancement counter and check scholarship tiers
        SkillPointInit.totalHemoAdvancements++;
        if (SkillPointInit.totalHemoAdvancements >= 5) {
            changed |= SkillPointInit.tryAwardMilestone(HemoMilestone.HEMO_SCHOLAR_I);
        }
        if (SkillPointInit.totalHemoAdvancements >= 10) {
            changed |= SkillPointInit.tryAwardMilestone(HemoMilestone.HEMO_SCHOLAR_II);
        }

        if (changed) {
            syncSkills(serverPlayer);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  COMBAT — Kill Counter Milestones
    // ════════════════════════════════════════════════════════════

    /**
     * Killing a hostile or hemomancy mob while the player's blood volume is
     * active increments the kill counter and checks tiered milestones.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        // Only count hostile mob kills
        if (victim.getType().getCategory() != MobCategory.MONSTER) return;

        // Only count when blood volume is active
        boolean bloodActive = HemoCapabilityAccess.getBloodVolume(player)
                .map(vol -> vol.isActive() && vol.getBloodVolume() > 0)
                .orElse(false);
        if (!bloodActive) return;

        SkillPointInit.totalKillsWithBlood++;
        boolean changed = false;

        if (SkillPointInit.totalKillsWithBlood >= 10) {
            changed |= SkillPointInit.tryAwardMilestone(HemoMilestone.BLOOD_SPILLER_I);
        }
        if (SkillPointInit.totalKillsWithBlood >= 50) {
            changed |= SkillPointInit.tryAwardMilestone(HemoMilestone.BLOOD_SPILLER_II);
        }
        if (SkillPointInit.totalKillsWithBlood >= 100) {
            changed |= SkillPointInit.tryAwardMilestone(HemoMilestone.BLOOD_SPILLER_III);
        }

        if (changed) {
            syncSkills(player);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  PUBLIC API — Called by other systems
    // ════════════════════════════════════════════════════════════

    /**
     * Called from {@code KnownManipulationEvents.onManipulationUsed} after a
     * blood manipulation is used. Increments the manipulation counter and
     * checks first-use and tiered milestones.
     */
    public static void onManipulationUsed(ServerPlayer player) {
        SkillPointInit.totalManipulationUses++;
        boolean changed = false;

        changed |= SkillPointInit.tryAwardMilestone(HemoMilestone.FIRST_MANIPULATION);

        if (SkillPointInit.totalManipulationUses >= 25) {
            changed |= SkillPointInit.tryAwardMilestone(HemoMilestone.MANIPULATION_NOVICE);
        }
        if (SkillPointInit.totalManipulationUses >= 100) {
            changed |= SkillPointInit.tryAwardMilestone(HemoMilestone.MANIPULATION_ADEPT);
        }
        if (SkillPointInit.totalManipulationUses >= 500) {
            changed |= SkillPointInit.tryAwardMilestone(HemoMilestone.MANIPULATION_MASTER);
        }

        if (changed) {
            syncSkills(player);
        }
    }

    /**
     * Called from {@code CardinalRiteEvents.completeRite} when a cardinal rite
     * finishes successfully. Awards the first-rite milestone and checks tiered
     * rite-completion milestones.
     */
    public static void onRiteCompleted(ServerPlayer player) {
        SkillPointInit.totalRitesCompleted++;
        boolean changed = false;

        changed |= SkillPointInit.tryAwardMilestone(HemoMilestone.FIRST_RITE);

        if (SkillPointInit.totalRitesCompleted >= 3) {
            changed |= SkillPointInit.tryAwardMilestone(HemoMilestone.RITE_PRACTITIONER);
        }
        if (SkillPointInit.totalRitesCompleted >= 10) {
            changed |= SkillPointInit.tryAwardMilestone(HemoMilestone.RITE_VETERAN);
        }

        if (changed) {
            syncSkills(player);
        }
    }

    /**
     * Called from {@code CardinalRiteEvents.completeRite} when a degree rite
     * grants a new initiatory degree. Awards the corresponding degree milestone.
     * Also converts any excess skill_manip_slots levels into skill points
     * when the hard cap of 9 slots is reached.
     */
    public static void onDegreeReached(ServerPlayer player, int degree) {
        boolean changed = switch (degree) {
            case 1 -> SkillPointInit.tryAwardMilestone(HemoMilestone.SANGUINE_INITIATION);
            case 2 -> SkillPointInit.tryAwardMilestone(HemoMilestone.VOTARY_ASCENSION);
            case 3 -> SkillPointInit.tryAwardMilestone(HemoMilestone.INITIATE_ASCENSION);
            case 4 -> SkillPointInit.tryAwardMilestone(HemoMilestone.ADEPT_ASCENSION);
            case 5 -> SkillPointInit.tryAwardMilestone(HemoMilestone.ILLUMINATUS_ASCENSION);
            case 6 -> SkillPointInit.tryAwardMilestone(HemoMilestone.SANCTIFIED_ASCENSION);
            case 7 -> SkillPointInit.tryAwardMilestone(HemoMilestone.ARCHON_ASCENSION);
            case 8 -> SkillPointInit.tryAwardMilestone(HemoMilestone.APOTHEOS_ASCENSION);
            default -> false;
        };

        // Convert excess skill_manip_slots levels into skill points
        int excess = com.vincenthuto.hemomancy.common.capability.player.manip.ManipSlotHelper
                .getExcessSkillLevels(player);
        if (excess > 0 && SkillPointInit.skill_manip_slots != null) {
            int currentLevel = SkillPointInit.skill_manip_slots.getCurrentLevel();
            SkillPointInit.skill_manip_slots.setCurrentLevel(currentLevel - excess);
            SkillPointInit.skillPoints += excess;
            changed = true;
        }

        if (changed) {
            syncSkills(player);
        }
    }

    /**
     * Called when a player equips a morphling for the first time.
     * Awards the morphling bond milestone.
     */
    public static void onMorphlingEquipped(ServerPlayer player) {
        if (SkillPointInit.tryAwardMilestone(HemoMilestone.FIRST_MORPHLING_BOND)) {
            syncSkills(player);
        }
    }

    /**
     * Called when a player completes a Somatic Loom recipe.
     * Awards the first-memory-weaving milestone.
     */
    public static void onMemoryWeavingCompleted(ServerPlayer player) {
        if (SkillPointInit.tryAwardMilestone(HemoMilestone.FIRST_MEMORY_WEAVING)) {
            syncSkills(player);
        }
    }

    /**
     * Called when a player joins or creates a bloodline.
     * Awards the first-bloodline milestone.
     */
    public static void onBloodlineJoined(ServerPlayer player) {
        if (SkillPointInit.tryAwardMilestone(HemoMilestone.FIRST_BLOODLINE)) {
            syncSkills(player);
        }
    }
}

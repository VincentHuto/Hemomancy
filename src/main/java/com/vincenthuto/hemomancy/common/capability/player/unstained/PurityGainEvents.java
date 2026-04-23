package com.vincenthuto.hemomancy.common.capability.player.unstained;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.MobType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Forge event handlers that grant purity to players on the Unstained path
 * through diverse gameplay actions. All handlers are gated behind
 * {@code hasBegunPurification() && !isPurified()}.
 *
 * <h3>Combat — Kill Rewards</h3>
 * <ul>
 *   <li>Hemomancy mob (tagged) → +2.0</li>
 *   <li>Undead (MobType.UNDEAD) → +0.5</li>
 *   <li>Other hostile (MobCategory.MONSTER) → +0.25</li>
 *   <li>Flawless kill bonus (kill without having taken damage recently) → +0.5 extra</li>
 * </ul>
 *
 * <h3>Survival &amp; Exploration</h3>
 * <ul>
 *   <li>XP orb pickup while Hemolysis active → +0.1</li>
 *   <li>Sleep through the night while Hemolysis active → +3.0</li>
 *   <li>Complete an advancement → +1.5</li>
 *   <li>Breed animals → +0.3</li>
 *   <li>Place crops / saplings / flowers → +0.05</li>
 *   <li>Heal self naturally with empty blood → +0.1</li>
 * </ul>
 *
 * <h3>Restraint &amp; Discipline</h3>
 * <ul>
 *   <li>Blood magic abstinence — every 5 minutes without using a manipulation → +0.5</li>
 *   <li>Blood volume empty/inactive → +0.15 per minute (renunciation)</li>
 * </ul>
 *
 * <h3>Mercy</h3>
 * <ul>
 *   <li>Healing a tamed animal → +0.2</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class PurityGainEvents {

    // ── Kill rewards ──
    private static final float PURITY_KILL_HEMOMANCY_MOB = 2.0f;
    private static final float PURITY_KILL_UNDEAD        = 0.5f;
    private static final float PURITY_KILL_HOSTILE       = 0.25f;
    private static final float PURITY_FLAWLESS_KILL      = 0.5f;

    // ── Survival / exploration ──
    private static final float PURITY_XP_PICKUP          = 0.1f;
    private static final float PURITY_SLEEP              = 3.0f;
    private static final float PURITY_ADVANCEMENT        = 1.5f;
    private static final float PURITY_BREED_ANIMAL       = 0.3f;
    private static final float PURITY_PLANT_CROP         = 0.05f;
    private static final float PURITY_HEAL               = 0.1f;

    // ── Restraint / discipline ──
    private static final float PURITY_ABSTINENCE         = 0.5f;
    private static final long  ABSTINENCE_INTERVAL_TICKS = 6000L;  // 5 minutes (5 * 60 * 20)
    private static final float PURITY_EMPTY_BLOOD        = 0.15f;
    private static final long  EMPTY_BLOOD_INTERVAL      = 1200L;  // 1 minute

    // ── Mercy ──
    private static final float PURITY_HEAL_TAMED         = 0.2f;

    // ── Flawless kill — "recently" = damaged within last 5 seconds ──
    private static final int FLAWLESS_THRESHOLD_TICKS    = 100;

    // ────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────

    /** Add purity if the player has begun purification and isn't yet fully purified. */
    private static void tryAddPurity(ServerPlayer player, float amount) {
        HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
            if (progress.hasBegunPurification() && !progress.isPurified()) {
                progress.addPurity(amount);
                UnstainedProgressEvents.syncProgress(player, progress);
            }
        });
    }

    /** Whether the player has the Hemolysis effect active. */
    private static boolean hasHemolysis(Player player) {
        return player.hasEffect(EffectInit.hemolysis.get());
    }

    /** Whether the player is on the unstained path and not yet fully purified. */
    private static boolean isOnUnstainedPath(IUnstainedProgress progress) {
        return progress.hasBegunPurification() && !progress.isPurified();
    }

    // ════════════════════════════════════════════════════════════
    //  COMBAT — Kill Rewards
    // ════════════════════════════════════════════════════════════

    /**
     * Tiered purity for kills:
     * 1. Hemomancy mob → +2.0
     * 2. Undead → +0.5
     * 3. Other hostile → +0.25
     * Bonus: +0.5 if the player hasn't been hurt recently (flawless kill).
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        float reward = 0f;
        boolean isHemo = false, isUndead = false, isHostile = false;

        // Tiered priority
        if (victim.getType().is(EntityInit.HEMOMANCY_MOB)) {
            reward = PURITY_KILL_HEMOMANCY_MOB;
            isHemo = true;
        } else if (victim.getMobType() == MobType.UNDEAD) {
            reward = PURITY_KILL_UNDEAD;
            isUndead = true;
        } else if (victim.getType().getCategory() == MobCategory.MONSTER) {
            reward = PURITY_KILL_HOSTILE;
            isHostile = true;
        }

        if (reward <= 0f) return;

        // Flawless kill bonus — player hasn't taken damage recently
        boolean flawless = player.getLastDamageSource() == null
                || (player.tickCount - player.getLastHurtByMobTimestamp()) > FLAWLESS_THRESHOLD_TICKS;
        if (flawless) {
            reward += PURITY_FLAWLESS_KILL;
        }

        // Track milestones
        final float finalReward = reward;
        final boolean fHemo = isHemo, fUndead = isUndead, fHostile = isHostile, fFlawless = flawless;
        HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
            if (!progress.hasBegunPurification() || progress.isPurified()) return;
            if (fHemo) {
                progress.addHemoMobKill();
                if (!progress.hasKilledFirstHemoMob()) progress.setKilledFirstHemoMob(true);
            }
            if (fUndead) progress.addUndeadKill();
            if (fHostile) progress.addHostileKill();
            if (fFlawless) progress.addFlawlessKill();
            progress.addPurity(finalReward);
            UnstainedProgressEvents.syncProgress(player, progress);
        });
    }

    // ════════════════════════════════════════════════════════════
    //  SURVIVAL — XP Pickup (requires Hemolysis)
    // ════════════════════════════════════════════════════════════

    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (hasHemolysis(player)) {
            tryAddPurity(serverPlayer, PURITY_XP_PICKUP);
        }
    }

    // ════════════════════════════════════════════════════════════
    //  SURVIVAL — Sleep (requires Hemolysis)
    // ════════════════════════════════════════════════════════════

    @SubscribeEvent
    public static void onPlayerWake(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (event.wakeImmediately()) return;

        if (hasHemolysis(player)) {
            HemoCapabilityAccess.getUnstainedProgress(serverPlayer).ifPresent(progress -> {
                if (!progress.hasBegunPurification() || progress.isPurified()) return;
                progress.addNightSlept();
                if (!progress.hasSleptWithHemolysis()) progress.setSleptWithHemolysis(true);
                progress.addPurity(PURITY_SLEEP);
                UnstainedProgressEvents.syncProgress(serverPlayer, progress);
            });
        }
    }

    // ════════════════════════════════════════════════════════════
    //  EXPLORATION — Advancement Completion
    // ════════════════════════════════════════════════════════════

    /**
     * Earning any advancement grants +1.5 purity — covers boss kills
     * (dragon, wither, elder guardian), exploration milestones,
     * Nether entry, elytra, etc.
     */
    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        HemoCapabilityAccess.getUnstainedProgress(serverPlayer).ifPresent(progress -> {
            if (!progress.hasBegunPurification() || progress.isPurified()) return;
            progress.addAdvancementEarned();
            if (!progress.hasEarnedAdvancement()) progress.setEarnedAdvancement(true);
            progress.addPurity(PURITY_ADVANCEMENT);
            UnstainedProgressEvents.syncProgress(serverPlayer, progress);
        });
    }

    // ════════════════════════════════════════════════════════════
    //  FARMING — Breeding Animals
    // ════════════════════════════════════════════════════════════

    /** Breeding animals — creating life rather than taking it — grants +0.3 purity. */
    @SubscribeEvent
    public static void onBreedAnimal(BabyEntitySpawnEvent event) {
        Player causer = event.getCausedByPlayer();
        if (causer == null) return;
        if (causer.level().isClientSide) return;
        if (!(causer instanceof ServerPlayer serverPlayer)) return;

        HemoCapabilityAccess.getUnstainedProgress(serverPlayer).ifPresent(progress -> {
            if (!progress.hasBegunPurification() || progress.isPurified()) return;
            progress.addAnimalBreed();
            progress.addPurity(PURITY_BREED_ANIMAL);
            UnstainedProgressEvents.syncProgress(serverPlayer, progress);
        });
    }

    // ════════════════════════════════════════════════════════════
    //  FARMING — Planting Crops / Saplings / Flowers
    // ════════════════════════════════════════════════════════════

    /** Placing crops, saplings, or flowers grants a tiny +0.05 purity. */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        var state = event.getPlacedBlock();
        if (state.is(net.minecraft.tags.BlockTags.CROPS)
                || state.is(net.minecraft.tags.BlockTags.SAPLINGS)
                || state.is(net.minecraft.tags.BlockTags.FLOWERS)) {
            HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
                if (!progress.hasBegunPurification() || progress.isPurified()) return;
                progress.addCropPlanted();
                progress.addPurity(PURITY_PLANT_CROP);
                UnstainedProgressEvents.syncProgress(player, progress);
            });
        }
    }

    // ════════════════════════════════════════════════════════════
    //  MERCY — Healing Tamed Animals + Natural Self-Healing
    // ════════════════════════════════════════════════════════════

    /**
     * Healing a tamed animal → owner gets +0.2 purity (compassion).
     * Healing yourself significantly (2+ hearts) with empty/inactive blood → +0.1 (restraint).
     */
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        // Case 1: tamed animal heals — reward the owner
        if (entity instanceof net.minecraft.world.entity.TamableAnimal tamed && tamed.isTame()) {
            LivingEntity owner = tamed.getOwner();
            if (owner instanceof ServerPlayer serverOwner) {
                HemoCapabilityAccess.getUnstainedProgress(serverOwner).ifPresent(progress -> {
                    if (!progress.hasBegunPurification() || progress.isPurified()) return;
                    progress.addPetHealed();
                    progress.addPurity(PURITY_HEAL_TAMED);
                    UnstainedProgressEvents.syncProgress(serverOwner, progress);
                });
            }
            return;
        }

        // Case 2: player self-healing without blood magic
        if (entity instanceof ServerPlayer player && event.getAmount() >= 2.0f) {
            HemoCapabilityAccess.getBloodVolume(player).ifPresent(blood -> {
                if (!blood.isActive() || blood.getBloodVolume() <= 0) {
                    tryAddPurity(player, PURITY_HEAL);
                }
            });
        }
    }

    // ════════════════════════════════════════════════════════════
    //  RESTRAINT — Abstinence Timer & Empty Blood Renunciation
    // ════════════════════════════════════════════════════════════

    /**
     * Per-tick check (throttled to once per second):
     * <ul>
     *   <li>Abstinence: 5+ minutes since last blood manipulation → +0.5, resets timer</li>
     *   <li>Empty blood: blood volume zero or inactive → +0.15 per minute</li>
     * </ul>
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // Throttle: only check once per second
        if (player.tickCount % 20 != 0) return;

        HemoCapabilityAccess.getUnstainedProgress(serverPlayer).ifPresent(progress -> {
            if (!isOnUnstainedPath(progress)) return;

            long currentTick = player.tickCount;
            boolean changed = false;

            // ── Abstinence timer ──
            long lastManip = progress.getLastManipulationTick();
            if (lastManip > 0 && (currentTick - lastManip) >= ABSTINENCE_INTERVAL_TICKS) {
                progress.addPurity(PURITY_ABSTINENCE);
                progress.setLastManipulationTick(currentTick); // reset timer
                if (!progress.hasReachedAbstinence()) progress.setReachedAbstinence(true);
                changed = true;
            } else if (lastManip == 0 && progress.hasBegunPurification()) {
                // Initialize on first check
                progress.setLastManipulationTick(currentTick);
            }

            // ── Empty blood renunciation ──
            if (currentTick % EMPTY_BLOOD_INTERVAL < 20) {
                HemoCapabilityAccess.getBloodVolume(serverPlayer).ifPresent(blood -> {
                    if (!blood.isActive() || blood.getBloodVolume() <= 0) {
                        progress.addPurity(PURITY_EMPTY_BLOOD);
                        if (!progress.hasEmptiedBlood()) progress.setEmptiedBlood(true);
                    }
                });
                changed = true;
            }

            if (changed) {
                UnstainedProgressEvents.syncProgress(serverPlayer, progress);
            }
        });
    }

    // ════════════════════════════════════════════════════════════
    //  PUBLIC API — Reset abstinence timer on blood magic use
    // ════════════════════════════════════════════════════════════

    /**
     * Call this from blood manipulation execution code whenever a player
     * uses any blood manipulation. Resets the abstinence timer.
     */
    public static void onBloodManipulationUsed(ServerPlayer player) {
        HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
            if (progress.hasBegunPurification()) {
                progress.setLastManipulationTick(player.tickCount);
                UnstainedProgressEvents.syncProgress(player, progress);
            }
        });
    }
}

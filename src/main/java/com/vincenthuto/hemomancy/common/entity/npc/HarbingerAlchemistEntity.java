package com.vincenthuto.hemomancy.common.entity.npc;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * A learned practitioner of the Harbinger order stationed within the Harbinger
 * Outpost. The Alchemist instructs players on the functional machines, crafting
 * stations, and mechanical systems available to Harbinger members.
 * <p>
 * Dialogue is gated by the player's initiatory degree so that more advanced
 * machine knowledge is revealed as the player progresses.
 * <p>
 * Special behaviour based on the player's Unstained path progress:
 * <ul>
 *   <li>If the player has begun purification, the Alchemist dismisses them —
 *       they have no patience for someone abandoning blood mastery.</li>
 *   <li>If the player has attained Clarity (entered Phase 2 of the Unstained
 *       path), the Alchemist ignores them entirely with a cold-shoulder response
 *       and no further engagement.</li>
 * </ul>
 */
public class HarbingerAlchemistEntity extends PathfinderMob {

    public final AnimationState idleAnimationState = new AnimationState();

    public HarbingerAlchemistEntity(EntityType<? extends HarbingerAlchemistEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setInvulnerable(true);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player && player.isCreative()) {
            return super.hurt(source, amount);
        }
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
        super.tick();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!player.level().isClientSide && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            DialogueTree tree;

            if (hasClarityUnlocked(player)) {
                // Clarity-bearing players are ignored — cold shoulder, no engagement
                tree = HarbingerAlchemistDialogueTrees.clarity(this.getId());
            } else if (isPurifying(player)) {
                // Purifying players are dismissed — the Alchemist has no time for them
                tree = HarbingerAlchemistDialogueTrees.purifying(this.getId());
            } else {
                int degree = InitiatoryDegreeProvider.getPlayerDegreeNumber(player);
                tree = HarbingerAlchemistDialogueTrees.forDegree(degree, this.getId());
            }

            PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(tree));
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    /** Returns true if the given player has unlocked the Clarity phase (Unstained Phase 2). */
    private static boolean hasClarityUnlocked(Player player) {
        return HemoCapabilityAccess.getUnstainedProgress(player)
                .map(IUnstainedProgress::hasClarityUnlocked)
                .orElse(false);
    }

    /** Returns true if the given player has begun purification but not yet entered Clarity. */
    private static boolean isPurifying(Player player) {
        return HemoCapabilityAccess.getUnstainedProgress(player)
                .map(IUnstainedProgress::hasBegunPurification)
                .orElse(false);
    }
}

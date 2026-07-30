package com.vincenthuto.hemomancy.common.entity.npc.unstained;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.AcolyteDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A humble parishioner of the Unstained Church. Acolytes offer stage-aware
 * dialogue with lore, guidance, and repeatable task suggestions for players
 * on the Unstained path. They differ from Zealots (who recruit) by providing
 * ongoing mentorship and deeper lore as the player progresses.
 * <p>
 * Between three and five acolytes spawn per church structure.
 */
public class UnstainedAcolyteEntity extends PathfinderMob {

    public final AnimationState idleAnimationState = new AnimationState();

    public UnstainedAcolyteEntity(EntityType<? extends UnstainedAcolyteEntity> type, Level worldIn) {
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
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.5D));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.GENERIC_KILL)) {
            return super.hurt(source, amount);
        }
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
            DialogueTree tree = selectDialogue(serverPlayer);
            tree = DialogueHubFactory.decorate(tree, "acolyte", serverPlayer);

            PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(tree));
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    /**
     * Selects the appropriate dialogue tree based on the player's current
     * Unstained progression state. Returns increasingly detailed lore and
     * task suggestions as the player advances.
     */
    private DialogueTree selectDialogue(ServerPlayer player) {
        return HemoCapabilityAccess.getUnstainedProgress(player).map(progress -> {
            if (!progress.hasBegunPurification()) {
                return AcolyteDialogueTrees.notOnPath(this.getId());
            }

            // Enlightened — full path completed
            if (progress.isEnlightened()) {
                return AcolyteDialogueTrees.enlightenedStage(this.getId());
            }

            // Clarity phase — purified and clarity unlocked
            if (progress.hasClarityUnlocked()) {
                return AcolyteDialogueTrees.clarityPhase(this.getId());
            }

            // Purity stage progression
            EnumPurityStage stage = EnumPurityStage.byPurity(progress.getPurity());
            return switch (stage) {
                case PURIFIED -> AcolyteDialogueTrees.purifiedStage(this.getId());
                case ABSOLVED -> AcolyteDialogueTrees.absolvedStage(this.getId());
                case CLEANSING -> AcolyteDialogueTrees.cleansingStage(this.getId());
                case TAINTED -> AcolyteDialogueTrees.taintedStage(this.getId());
                default -> AcolyteDialogueTrees.corruptedStage(this.getId());
            };
        }).orElse(AcolyteDialogueTrees.notOnPath(this.getId()));
    }
}

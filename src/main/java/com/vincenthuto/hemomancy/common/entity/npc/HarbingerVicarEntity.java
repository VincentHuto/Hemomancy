package com.vincenthuto.hemomancy.common.entity.npc;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerVicarDialogueTrees;
import com.vincenthuto.hemomancy.common.item.QliphothPomeItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * A keeper of Harbinger lore and sacred doctrine stationed within the Harbinger
 * Outpost. The Vicar speaks of the order's history, the nature of blood magic,
 * and provides progression-based hints to guide players along the initiatory path.
 * <p>
 * Dialogue is gated by the player's initiatory degree, revealing deeper lore
 * and more specific guidance as the player advances.
 * <p>
 * Special behaviour based on the player's Unstained path progress:
 * <ul>
 *   <li>If the player has begun purification, the Vicar delivers a stern warning
 *       that they are straying from the blood path and their power is waning.</li>
 *   <li>If the player has attained Clarity (entered Phase 2 of the Unstained path),
 *       the Vicar becomes immediately hostile and attacks on sight — no dialogue
 *       is offered to an enemy of the Covenant.</li>
 * </ul>
 */
public class HarbingerVicarEntity extends PathfinderMob {

    public final AnimationState idleAnimationState = new AnimationState();

    public HarbingerVicarEntity(EntityType<? extends HarbingerVicarEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setInvulnerable(true);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        // Attack clarity-bearing players on sight
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(
                this, Player.class, 10, true, false,
                HarbingerVicarEntity::hasClarityUnlocked));
    }

    private static boolean hasClarityUnlocked(LivingEntity entity) {
        if(entity instanceof Player player) {
            return HemoCapabilityAccess.getUnstainedProgress(player)
                    .map(IUnstainedProgress::hasClarityUnlocked)
                    .orElse(false);
        }
        return false;
    }



    /** Returns true if the given player has begun purification but not yet entered Clarity. */
    private static boolean isPurifying(Player player) {
        return HemoCapabilityAccess.getUnstainedProgress(player)
                .map(IUnstainedProgress::hasBegunPurification)
                .orElse(false);
    }

    /**
     * Returns true if the player has an active Qliphoth Pome empowerment
     * (the 3-minute manipulation-cost-reduction window from eating a pome).
     * Checked server-side via persistent data.
     */
    private static boolean hasPomeEmpowerment(Player player) {
        long expiry = player.getPersistentData().getLong(QliphothPomeItem.POME_EMPOWERMENT_KEY);
        return expiry > 0 && player.level().getGameTime() < expiry;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Allow full damage while the Vicar is actively fighting a clarity-bearing player
        if (this.getTarget() != null) {
            return super.hurt(source, amount);
        }
        // Creative players can always remove it
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
            // Clarity-bearing players are enemies — attack, no dialogue
            if (hasClarityUnlocked(player)) {
                this.setTarget(player);
                return InteractionResult.sidedSuccess(false);
            }

            int degree = InitiatoryDegreeProvider.getPlayerDegreeNumber(player);
            DialogueTree tree;

            if (isPurifying(player)) {
                // Purifying players receive a stern Harbinger warning
                tree = HarbingerVicarDialogueTrees.purifying(this.getId());
            } else if (degree >= 7 && hasPomeEmpowerment(player)) {
                // Archon players with active pome empowerment receive the unsettled reaction
                tree = HarbingerVicarDialogueTrees.archonPomeEmpowered(this.getId());
            } else {
                tree = HarbingerVicarDialogueTrees.forDegree(degree, this.getId());
            }

            PacketHandler.CHANNELBLOODVOLUME.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new OpenDialoguePacket(tree));
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}

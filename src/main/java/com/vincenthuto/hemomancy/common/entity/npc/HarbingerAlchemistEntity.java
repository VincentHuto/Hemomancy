package com.vincenthuto.hemomancy.common.entity.npc;

import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
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
import net.minecraftforge.network.PacketDistributor;

/**
 * A learned practitioner of the Harbinger order stationed within the Harbinger
 * Outpost. The Alchemist instructs players on the functional machines, crafting
 * stations, and mechanical systems available to Harbinger members.
 * <p>
 * Dialogue is gated by the player's initiatory degree so that more advanced
 * machine knowledge is revealed as the player progresses.
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
            int degree = InitiatoryDegreeProvider.getPlayerDegreeNumber(player);
            DialogueTree tree = HarbingerAlchemistDialogueTrees.forDegree(degree, this.getId());

            PacketHandler.CHANNELBLOODVOLUME.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new OpenDialoguePacket(tree));
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}

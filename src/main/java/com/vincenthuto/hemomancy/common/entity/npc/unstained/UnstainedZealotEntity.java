package com.vincenthuto.hemomancy.common.entity.npc.unstained;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
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

public class UnstainedZealotEntity extends PathfinderMob implements ProgressionDialogueNpc {

    public final AnimationState idleAnimationState = new AnimationState();

    public UnstainedZealotEntity(EntityType<? extends UnstainedZealotEntity> type, Level worldIn) {
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
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6D));
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
			float purity = HemoCapabilityAccess.getUnstainedProgress(player)
					.map(progress -> progress.getPurity()).orElse(0f);
			DialogueTree tree = progressionDialogue(serverPlayer);
            tree = DialogueItemInquiryNodes.withInventoryItemInquiries(tree, serverPlayer, "zealot", 0, purity);
            tree = DialogueHubFactory.decorate(tree, "zealot", serverPlayer);

            PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(tree));
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public DialogueTree progressionDialogue(ServerPlayer player) {
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
        IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
        var progress = HemoCapabilityAccess.getUnstainedProgress(player).orElse(null);
        DialogueTree tree;
        if (progress != null && progress.hasBegunPurification()) {
            tree = ZealotDialogueTrees.alreadyOnPath(this.getId(), progress.getPurity(),
                    progress.hasClarityUnlocked(), progress.isEnlightened());
        } else if (volume == null || !volume.isActive()) {
            tree = ZealotDialogueTrees.noBlood(this.getId());
        } else if (degree <= EnumInitiatoryDegree.ILLUMINATUS.getNumber()) {
            tree = ZealotDialogueTrees.pleaDialogue(this.getId(), degree);
        } else {
            tree = ZealotDialogueTrees.tooDeep(this.getId());
        }
        return UnstainedObservanceDialogueDecorator.decorate(tree, player, UnstainedObservances.Issuer.ZEALOT);
    }

    @Override
    public String progressionDialogueId() {
        return "zealot";
    }
}

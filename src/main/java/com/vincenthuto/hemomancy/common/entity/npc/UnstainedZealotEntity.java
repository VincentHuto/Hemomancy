package com.vincenthuto.hemomancy.common.entity.npc;

import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.ZealotDialogueTrees;
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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class UnstainedZealotEntity extends PathfinderMob {

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
            IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);

            // Gracefully check for the UnstainedProgress capability which may not be
            // present if its PR has not been merged yet.
            boolean hasBegunPurification = false;
            try {
                Class<?> providerClass = Class.forName(
                        "com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider");
                Object capa = providerClass.getField("UNSTAINED_CAPA").get(null);
                @SuppressWarnings("unchecked")
                net.minecraftforge.common.capabilities.Capability<Object> unstainedCapa =
                        (net.minecraftforge.common.capabilities.Capability<Object>) capa;
                hasBegunPurification = player.getCapability(unstainedCapa)
                        .map(obj -> {
                            try {
                                return (Boolean) obj.getClass().getMethod("hasBegunPurification").invoke(obj);
                            } catch (NoSuchMethodException | IllegalAccessException
                                    | java.lang.reflect.InvocationTargetException ex) {
                                return false;
                            }
                        }).orElse(false);
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                // Capability not yet registered — skip this check branch
            }

            DialogueTree tree;
            if (hasBegunPurification) {
                tree = ZealotDialogueTrees.alreadyOnPath(this.getId());
            } else if (volume == null || !volume.isActive()) {
                tree = ZealotDialogueTrees.noBlood(this.getId());
            } else if (degree >= EnumInitiatoryDegree.VOTARY.getNumber()) {
                tree = ZealotDialogueTrees.pleaDialogue(this.getId());
            } else if (degree >= 1) {
                tree = ZealotDialogueTrees.tooEarly(this.getId());
            } else {
                tree = ZealotDialogueTrees.uninitiated(this.getId());
            }

            PacketHandler.CHANNELBLOODVOLUME.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new OpenDialoguePacket(tree));
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}

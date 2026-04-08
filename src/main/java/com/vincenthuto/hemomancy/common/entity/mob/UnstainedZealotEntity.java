package com.vincenthuto.hemomancy.common.entity.mob;

import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
        if (!player.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
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

            if (hasBegunPurification) {
                sendDialogue(player, "hemomancy.zealot.already_on_path");
            } else if (volume == null || !volume.isActive()) {
                // No blood magic active — dismissive/generic
                sendDialogue(player, "hemomancy.zealot.no_blood");
            } else if (degree >= EnumInitiatoryDegree.VOTARY.getNumber()) {
                // VOTARY or higher with active blood — deliver the Plea of the Unstained
                sendPleaDialogue(player);
            } else if (degree >= 1) {
                // Initiated but not yet VOTARY — too early
                sendDialogue(player, "hemomancy.zealot.too_early");
            } else {
                // Uninitiated (degree 0) with active blood
                sendDialogue(player, "hemomancy.zealot.uninitiated");
            }
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    private void sendPleaDialogue(Player player) {
        player.displayClientMessage(Component.translatable("hemomancy.zealot.plea.line1")
                .withStyle(ChatFormatting.GRAY), false);
        player.displayClientMessage(Component.translatable("hemomancy.zealot.plea.line2")
                .withStyle(ChatFormatting.WHITE), false);
        player.displayClientMessage(Component.translatable("hemomancy.zealot.plea.line3")
                .withStyle(ChatFormatting.AQUA), false);
        player.displayClientMessage(Component.translatable("hemomancy.zealot.plea.line4")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
    }

    private void sendDialogue(Player player, String key) {
        player.displayClientMessage(Component.translatable(key)
                .withStyle(ChatFormatting.GRAY), false);
    }
}

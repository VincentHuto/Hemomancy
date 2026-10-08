package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitatRules.Species;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public class PelagicHerringEntity extends AbstractSchoolingFish {
    private Vec3 schoolMotion = Vec3.ZERO;
    public PelagicHerringEntity(EntityType<? extends PelagicHerringEntity> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder setAttributes() { return createAttributes().add(Attributes.MOVEMENT_SPEED, 1.35); }
    @Override public int getMaxSchoolSize() { return 10; }
    @Override public ItemStack getBucketItemStack() { return ItemStack.EMPTY; }
    @Override protected net.minecraft.sounds.SoundEvent getFlopSound() { return net.minecraft.sounds.SoundEvents.COD_FLOP; }
    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        return player.getItemInHand(hand).is(Items.WATER_BUCKET) ? InteractionResult.PASS : super.mobInteract(player, hand);
    }
    @Override protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(3, new Goal() {
            Vec3 target;
            int remaining;
            { setFlags(EnumSet.of(Flag.MOVE)); }
            @Override public boolean canUse() {
                if (isFollower() || random.nextInt(20) != 0) return false;
                target = PelagicHabitat.target(PelagicHerringEntity.this, Species.HERRING, false);
                return target != null;
            }
            @Override public void start() { remaining = 80; navigation.moveTo(target.x, target.y, target.z, 1.2); }
            @Override public boolean canContinueToUse() { return !isFollower() && --remaining > 0 && !navigation.isDone(); }
            @Override public void stop() { navigation.stop(); }
        });
    }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide || !isInWater() || isNoAi()) return;
        if (tickCount % 10 == 0) {
            var neighbors = level().getEntitiesOfClass(PelagicHerringEntity.class, getBoundingBox().inflate(4),
                    fish -> fish != this && fish.isAlive() && fish.isInWater());
            neighbors.sort(Comparator.comparingDouble(this::distanceToSqr));
            Vec3 heading = Vec3.ZERO, center = Vec3.ZERO, separation = Vec3.ZERO;
            int count = Math.min(9, neighbors.size());
            for (int i = 0; i < count; i++) {
                var fish = neighbors.get(i);
                heading = heading.add(fish.getDeltaMovement()); center = center.add(fish.position());
                if (distanceToSqr(fish) < 1) separation = separation.add(position().subtract(fish.position()));
            }
            schoolMotion = count == 0 ? Vec3.ZERO : heading.scale(1D / count).normalize().scale(.005)
                    .add(center.scale(1D / count).subtract(position()).scale(.002)).add(separation.scale(.015));
        }
        var ahead = blockPosition().offset((int)Math.signum(getDeltaMovement().x), 0, (int)Math.signum(getDeltaMovement().z));
        if (PelagicHabitat.waterClearance(level(), ahead, 0, 0, 0))
            setDeltaMovement(getDeltaMovement().add(schoolMotion)
                    .add(0, .005 + PelagicHabitat.depthCorrection(level(), blockPosition(), Species.HERRING), 0));
    }
}

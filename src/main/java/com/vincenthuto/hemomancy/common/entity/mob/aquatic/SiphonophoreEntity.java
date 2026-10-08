package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitatRules.Species;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import java.util.Comparator;

public class SiphonophoreEntity extends PelagicAnimal {
    private static final LightningTestConfig FEEDING_ZAP = new LightningTestConfig(
            LightningTestConfig.Backend.BOLT, 0xE8FFE84A, 0xE8FFE84A, 0xFFFFFFFF,
            32F, 0F, 0F, 0F, 42F, 1.4F, 12, 5, .12F, .025F, false, 0L, false, 8);
    public SiphonophoreEntity(EntityType<? extends SiphonophoreEntity> type, Level level) { super(type, level); }
    @Override public Species habitat() { return Species.SIPHONOPHORE; }
    @Override public AABB getBoundingBoxForCulling() { return getBoundingBox().expandTowards(0, -2.5, 0).inflate(.25); }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide || isNoAi() || !isInWater() || actionCooldown > 0 || tickCount % 10 != 0) return;
        // Feeding filaments hang below the swimming float; the search follows that visible volume.
        AABB net = new AABB(getX() - .8, getY() - 2.8, getZ() - .8, getX() + .8, getY() + .6, getZ() + .8);
        level().getEntitiesOfClass(AbstractFish.class, net, fish -> fish.isAlive() && fish.isInWater()
                && fish.getBbWidth() < .7F && hasLineOfSight(fish)).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr)).ifPresent(fish -> {
                    if (fish.hurt(damageSources().mobAttack(this), 4)) {
                        LightningTesterSpawner.spawn((ServerLevel) level(), position().add(0, -1, 0),
                                fish.position().add(0, fish.getBbHeight() * .5, 0), FEEDING_ZAP);
                        actFor(30); actionCooldown = 100; navigation.stop();
                    }
                });
    }
}

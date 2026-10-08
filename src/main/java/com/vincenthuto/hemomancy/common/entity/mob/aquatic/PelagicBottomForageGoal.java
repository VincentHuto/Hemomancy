package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.*;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;
import java.util.function.BooleanSupplier;

/** Existing crawlers choose nearby supported surfaces without being pinned to biome borders. */
public final class PelagicBottomForageGoal extends Goal {
    private final PathfinderMob animal;
    private final PelagicHabitatRules.Species species;
    private final BooleanSupplier freeToMove;
    private Vec3 target;
    private int remaining;
    public PelagicBottomForageGoal(PathfinderMob animal, PelagicHabitatRules.Species species, BooleanSupplier freeToMove) {
        this.animal = animal; this.species = species; this.freeToMove = freeToMove;
        setFlags(EnumSet.of(Flag.MOVE));
    }
    @Override public boolean canUse() {
        if (!freeToMove.getAsBoolean() || animal.getRandom().nextInt(25) != 0
                || PelagicHabitat.layer(animal.level(), animal.blockPosition()) == null) return false;
        target = PelagicHabitat.target(animal, species, true);
        return target != null;
    }
    @Override public void start() { remaining = 100; }
    @Override public boolean canContinueToUse() { return remaining > 0 && freeToMove.getAsBoolean() && animal.distanceToSqr(target) > .4; }
    @Override public void tick() {
        remaining--;
        // Floor crawlers already apply their own sinking and collision response in travel().
        var direction = target.subtract(animal.position()).multiply(1, 0, 1).normalize();
        var next = animal.blockPosition().offset((int)Math.signum(direction.x), 0, (int)Math.signum(direction.z));
        if (PelagicHabitat.loaded(animal.level(), next) && PelagicHabitat.floorDistance(animal.level(), next, 2) <= 2) {
            animal.setDeltaMovement(animal.getDeltaMovement().add(direction.scale(.008)));
            animal.setYRot((float)(Math.atan2(direction.z, direction.x) * 180 / Math.PI) - 90);
        }
    }
}

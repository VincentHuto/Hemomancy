package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.entity.mob.arthropod.BombardierFlameGeometry;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.BombardierState;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.PhlegethonticBombardier;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class PhlegethonticBombardierModel extends HierarchicalModel<PhlegethonticBombardier> {
    private final ModelPart bombardier = PhlegethonticBombardierMesh.createRoot();
    private final ModelPart root = new ModelPart(List.of(), Map.of("bombardier", bombardier));
    private final ModelPart abdomen = bombardier.getChild("abdomen");
    private final ModelPart valve = abdomen.getChild("spray_valve");
    private final Map<PhlegethonticBombardier, ClipTransition> transitions = new WeakHashMap<>();
    private final Vector3f animationScratch = new Vector3f();

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(PhlegethonticBombardier entity, float swing, float swingAmount,
                          float age, float yaw, float pitch) {
        bombardier.getAllParts().forEach(ModelPart::resetPose);
        BombardierState state = entity.getBombardierState();
        boolean firing = state == BombardierState.FIRING;
        boolean aimLocked = firing || state == BombardierState.WINDUP
                && entity.getStateTick() > PhlegethonticBombardier.AIM_TRACKING_TICKS;
        AnimationDefinition animation = entity.deathTime > 0 ? PhlegethonticBombardierAnimations.DEATH
                : entity.hurtTime > 0 && !aimLocked ? PhlegethonticBombardierAnimations.HURT : switch (state) {
                    case IDLE -> PhlegethonticBombardierAnimations.IDLE;
                    case CAMOUFLAGED -> PhlegethonticBombardierAnimations.FLATTEN_REST;
                    case WARNING -> PhlegethonticBombardierAnimations.WAKE_WARNING;
                    case GRAZING -> PhlegethonticBombardierAnimations.GRAZE;
                    case WINDUP -> PhlegethonticBombardierAnimations.ABDOMEN_WINDUP;
                    case FIRING -> PhlegethonticBombardierAnimations.ABDOMEN_FIRE;
                    case COOLING -> PhlegethonticBombardierAnimations.VENT_COOLDOWN;
                };
        applyBlendedAnimation(entity, animation, age);
        if (entity.deathTime == 0 && !aimLocked) {
            animateWalk(PhlegethonticBombardierAnimations.WALK, swing, walkBlend(swingAmount), 2, 2.5F);
        }
        if (aimLocked && entity.deathTime == 0) {
            float partialTick = Mth.clamp(age - entity.tickCount, 0, 1);
            float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
            Vec3 direction = firing
                    ? BombardierFlameGeometry.sweepDirection(entity.getLockedDirection(),
                            entity.getStateTick() + (double) partialTick, PhlegethonticBombardier.FLAME_DURATION,
                            BombardierFlameGeometry.SWEEP_DEGREES)
                    : entity.getLockedDirection();
            aimValve(bodyYaw, direction);
        }
    }

    void aimValve(float bodyYaw, Vec3 direction) {
        bombardier.loadPose(bombardier.getInitialPose());
        abdomen.xRot = BombardierFlameGeometry.RAISED_ABDOMEN_DEGREES * Mth.DEG_TO_RAD;
        // Undo the renderer's facing and axis flips, then the raised abdomen's pitch.
        Vec3 localDirection = direction.yRot(bodyYaw * Mth.DEG_TO_RAD)
                .multiply(1, -1, -1).xRot(abdomen.xRot);
        valve.yRot = (float) Mth.atan2(localDirection.x, localDirection.z);
        valve.xRot = (float) Mth.atan2(-localDirection.y, localDirection.horizontalDistance());
        valve.zRot = 0;
    }

    static float walkBlend(float swingAmount) {
        float progress = Mth.clamp(swingAmount / .2F, 0.0F, 1.0F);
        return progress * progress * (3.0F - 2.0F * progress);
    }

    private void applyBlendedAnimation(PhlegethonticBombardier entity, AnimationDefinition animation, float age) {
        ClipTransition transition = transitions.computeIfAbsent(entity,
                ignored -> new ClipTransition(animation, age));
        if (transition.current != animation) transition.begin(animation, age);

        float elapsed = Math.max(0.0F, age - transition.transitionAge);
        float incoming = BombardierAnimationBlend.incomingWeight(elapsed);
        if (transition.previous != null && incoming < 1.0F) {
            KeyframeAnimations.animate(this, transition.previous, ticksToMillis(transition.previousTime),
                    BombardierAnimationBlend.outgoingWeight(elapsed), animationScratch);
        } else {
            transition.previous = null;
        }
        KeyframeAnimations.animate(this, transition.current,
                ticksToMillis(Math.max(0.0F, age - transition.currentAge)), incoming, animationScratch);
    }

    private static long ticksToMillis(float ticks) {
        return (long) (ticks * 50.0F);
    }

    private static final class ClipTransition {
        private AnimationDefinition current;
        private AnimationDefinition previous;
        private float currentAge;
        private float previousTime;
        private float transitionAge;

        private ClipTransition(AnimationDefinition current, float age) {
            this.current = current;
            currentAge = age;
            transitionAge = age - BombardierAnimationBlend.TRANSITION_TICKS;
        }

        private void begin(AnimationDefinition next, float age) {
            previous = current;
            previousTime = Math.max(0.0F, age - currentAge);
            current = next;
            currentAge = age;
            transitionAge = age;
        }
    }
}

"""The Sanguine Hound's true self (2026-11-17): what the Hound is inside the Beast Tamer's Chamber of Will, the hound
before the bridle. Wilder than its physical form: bone hackles standing along its back, its harness torn and hanging,
more muscle at the scruff, the ribs raw, its blood seams and eyes lit, and a hinged jaw full of teeth. The ordeal makes
it bigger too (`SanguineHoundEntity.setTrueSelf`, 1.5 times scale). The physical model is untouched; this is its own rig
on the same joints, with a galloping bound for its run and a pose for each ordeal move (`OrdealHoundMoves`).
"""
import puppet_rig
from puppet_rig import build as build_rig, noise

import build_sanguine_hound as physical

# The physical palette, then the lit blood (12) and its hot core (13).
PALETTE = physical.PALETTE + ['#ff4a3a', '#ff9a7a']
GLOW = {12, 13}


def anatomy():
    parts = physical.anatomy()

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    # More muscle at the scruff, heaped over the shoulders.
    joint('scruff', 'body', (0, -3, -3), [box((-2.5, -1.5, -2.5), (5, 2, 4), 'muscle')])
    # Bone hackles standing along the spine, longest over the shoulders.
    for i, (z, length) in enumerate(((-4, 3), (-2, 3.5), (0, 2.5), (2, 2), (4, 1.5))):
        joint('hackle_' + str(i), 'body', (0, -4, z), [box((-.5, -length, -.5), (1, length, 1), 'bone')], (-25, 0, 0))
    # The harness torn: a strap hanging loose from each side.
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_strap', 'body', (sign * 2.5, 0, -3), [box((-.5, 0, -.5), (1, 4, 1), 'harness')],
              (12, 0, -sign * 14))
    # A hinged jaw under the snout, with teeth above and below.
    joint('jaw', 'head', (0, 1.5, -2), [
        box((-1, 0, -4.5), (2, 1, 4.5), 'jaw'),
        box((-1, -.5, -4.25), (2, .5, 1), 'tooth')])
    joint('upper_teeth', 'head', (0, 1.5, -4.75), [box((-1, 0, -.25), (2, .5, 2.5), 'tooth')])
    return parts


def shade(material, side, px, py, w, h, gx, gy):
    if material == 'jaw':
        if side == 'up':
            return 1
        return physical.shade('snout', side, px, py, w, h, gx, gy)
    if material == 'tooth':
        return 7 if side in ('north', 'down', 'up') else 6
    base = physical.shade(material, side, px, py, w, h, gx, gy)
    if material == 'skull' and base == 5:
        # Its eyes lit.
        return 13 if side == 'north' else 12
    if material == 'muscle':
        # The stitched rupture seams lit with its blood, and rawer hide.
        if base == 7:
            return 12 if py % 2 == 0 else 13
        if noise(gx, gy, 41) < .06:
            return 12
        return min(base + 1, 5) if base in (3, 4) else base
    if material == 'chest' and base in (6, 7):
        # Raw ribs, blood between them.
        return 7 if noise(gx, gy, 43) > .25 else 12
    return base


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.BeastTamerMoves;
import com.vincenthuto.hemomancy.common.entity.summon.SanguineHoundEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_true_sanguine_hound.py (the Hound's true self, 2026-11-17); regenerate the model, atlas and BBModel together.
/**
 * The Sanguine Hound as its true self in the Beast Tamer's Chamber: a galloping bound for its run (front and hind
 * pairs together, the spine rocking, the head low and steady, ears pinned) and a pose for each of its ordeal moves
 * ({@code OrdealHoundMoves}), each a gather, a fast blow and a recovery: the lunge's crouch and rush, the pounce's coil
 * and leap, the shake's gape and side-to-side worrying, and the howl with its head thrown back.
 */
public class TrueSanguineHoundModel extends EntityModel<SanguineHoundEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("true_sanguine_hound"), "main");
    private final ModelPart root, body, neck, head, jaw, tail, rightEar, leftEar;
    /** Front left, front right, hind left, hind right. */
    private final ModelPart[] legs = new ModelPart[4], shins = new ModelPart[4];
    private final ModelPart[] hackles = new ModelPart[5];

    public TrueSanguineHoundModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        body = root.getChild("body");
        neck = body.getChild("neck");
        head = root.getChild("head");
        jaw = head.getChild("jaw");
        tail = root.getChild("tail");
        rightEar = head.getChild("right_ear");
        leftEar = head.getChild("left_ear");
        String[] names = {"front_left", "front_right", "hind_left", "hind_right"};
        for (int i = 0; i < 4; i++) {
            legs[i] = root.getChild(names[i] + "_leg");
            shins[i] = legs[i].getChild(names[i] + "_shin");
        }
        for (int i = 0; i < hackles.length; i++) hackles[i] = body.getChild("hackle_" + i);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
    }

    @Override
    public void setupAnim(SanguineHoundEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        float partialTick = Mth.clamp(age - entity.tickCount, 0.0F, 1.0F);
        pose(limbSwing, limbSwingAmount, age, yaw, pitch, entity.ordealMove(), entity.ordealMoveAge(partialTick));
    }

    public void pose(float limbSwing, float movement, float age, float yaw, float pitch, int move, float since) {
        root.getAllParts().forEach(ModelPart::resetPose);
        movement = Mth.clamp(movement, 0.0F, 1.0F);
        float phase = limbSwing * .6662F;
        // A trot when it walks, a galloping bound when it runs: the front pair together, then the hind.
        float bound = HemoEasing.smoothstep(.35F, .8F, movement);
        float gallop = phase * .8F;
        for (int i = 0; i < 4; i++) {
            boolean front = i < 2;
            float trot = Mth.cos(phase + (i == 0 || i == 3 ? 0.0F : Mth.PI)) * movement;
            float run = Mth.cos(gallop + (front ? 0.0F : Mth.PI) + (i % 2 == 0 ? .25F : 0.0F)) * movement;
            legs[i].xRot = Mth.lerp(bound, trot * 1.0F, run * 1.25F);
            // The forearm folds back as a paw lifts; the hock folds the other way.
            shins[i].xRot = front ? Math.max(0.0F, -legs[i].xRot) * .9F : -Math.max(0.0F, legs[i].xRot) * .7F;
        }
        body.xRot += Mth.sin(gallop) * .14F * bound;
        root.y += -Math.abs(Mth.sin(gallop)) * 1.4F * bound;
        // The head held low and steady through the run, the ears pinned and the tail streaming.
        head.y += 1.2F * movement;
        head.yRot = Mth.clamp(yaw, -50, 50) * Mth.DEG_TO_RAD;
        head.xRot = Mth.clamp(pitch, -30, 30) * Mth.DEG_TO_RAD + .15F * movement - body.xRot * .5F;
        rightEar.xRot += .7F * movement;
        leftEar.xRot += .7F * movement;
        tail.xRot = Mth.lerp(movement, tail.xRot, -.15F) + Mth.sin(gallop) * .2F * bound;
        tail.yRot = Mth.sin(age * .12F) * .2F * (1.0F - movement);
        // Panting, and the hackles always half up.
        float pant = Mth.sin(age * .35F);
        body.y += pant * .15F;
        jaw.xRot = .18F + pant * .06F + .2F * movement;
        for (ModelPart hackle : hackles) hackle.xRot -= .1F;
        if (move >= 0) move(move, since);
    }

    private void move(int move, float since) {
        switch (move) {
            case BeastTamerMoves.LUNGE -> {
                float w = BeastTamerMoves.LUNGE_WINDUP, run = BeastTamerMoves.LUNGE_RUN;
                float crouch = HemoEasing.smoothstep(0, w - 3, since) * (1 - HemoEasing.smoothstep(w - 1, w + 1, since));
                float rush = HemoEasing.smoothstep(w - 1, w + 1, since) * (1 - HemoEasing.smoothstep(w + run, w + run + 4, since));
                float settle = HemoEasing.smoothstep(w + run, w + run + 3, since) * (1 - HemoEasing.smoothstep(w + run + 3, w + run + 14, since));
                // Down on its haunches, hackles up and lips back, then flung out full length.
                root.y += 2.2F * crouch + 1.2F * settle;
                body.xRot += .1F * crouch - .12F * rush;
                head.y += 1.5F * crouch;
                head.xRot += .2F * crouch - .25F * rush;
                jaw.xRot += .35F * crouch + .75F * rush;
                for (int i = 0; i < 4; i++) {
                    boolean front = i < 2;
                    legs[i].xRot = Mth.lerp(crouch, legs[i].xRot, front ? -.45F : .7F);
                    legs[i].xRot = Mth.lerp(rush, legs[i].xRot, front ? -1.35F : 1.15F);
                    shins[i].xRot = Mth.lerp(crouch, shins[i].xRot, front ? .5F : -.9F) * (1 - rush);
                }
                for (ModelPart hackle : hackles) hackle.xRot -= .5F * (crouch + rush);
                tail.xRot += -.3F * crouch;
            }
            case BeastTamerMoves.POUNCE -> {
                float w = BeastTamerMoves.POUNCE_WINDUP, flight = BeastTamerMoves.POUNCE_FLIGHT;
                float coil = HemoEasing.smoothstep(0, w - 3, since) * (1 - HemoEasing.smoothstep(w - 1, w + 1, since));
                float air = HemoEasing.smoothstep(w - 1, w + 1, since) * (1 - HemoEasing.smoothstep(w + flight - 1, w + flight + 1, since));
                float t = Mth.clamp((since - w) / flight, 0, 1);
                float land = HemoEasing.smoothstep(w + flight - 1, w + flight + 1, since) * (1 - HemoEasing.smoothstep(w + flight + 2, w + flight + 16, since));
                // The coil: chest to the floor, haunches up and quivering; the leap: forelegs reaching, hind trailing,
                // the body pitching up off the floor and down onto the mark; the landing: splayed, sinking.
                float wiggle = Mth.sin(since * 1.4F) * .12F * HemoEasing.smoothstep(w - 6, w - 2, since) * coil;
                root.y += 2.0F * coil + 1.6F * land;
                body.xRot += .22F * coil + air * Mth.lerp(t, -.4F, .45F);
                body.zRot += wiggle;
                head.y += 1.6F * coil;
                head.xRot += air * Mth.lerp(t, -.35F, .3F);
                jaw.xRot += .2F * coil + .8F * air + .3F * land;
                for (int i = 0; i < 4; i++) {
                    boolean front = i < 2;
                    legs[i].xRot = Mth.lerp(coil, legs[i].xRot, front ? -.25F : .95F);
                    legs[i].xRot = Mth.lerp(air, legs[i].xRot, front ? -1.6F : 1.3F);
                    legs[i].xRot = Mth.lerp(land, legs[i].xRot, front ? -.6F : .5F);
                    shins[i].xRot = Mth.lerp(coil, shins[i].xRot, front ? .2F : -1.0F) * (1 - air * .7F);
                }
                tail.xRot += -.4F * air;
            }
            case BeastTamerMoves.SHAKE -> {
                float w = BeastTamerMoves.SHAKE_WINDUP, hold = BeastTamerMoves.SHAKE_HOLD;
                float gape = HemoEasing.smoothstep(0, w - 2, since) * (1 - HemoEasing.smoothstep(w - 1, w, since));
                float held = HemoEasing.smoothstep(w - 1, w + 1, since) * (1 - HemoEasing.smoothstep(w + hold, w + hold + 3, since));
                float fling = HemoEasing.smoothstep(w + hold - 1, w + hold + 1, since) * (1 - HemoEasing.smoothstep(w + hold + 3, w + hold + 12, since));
                // Jaws wide, then clamped on its catch and worried hard from side to side (the server's own swing).
                float worry = Mth.sin((since - w) * .8F) * held;
                head.xRot += .3F * gape + .15F * held - .5F * fling;
                head.yRot += .75F * worry + .5F * fling;
                head.y += .8F * gape;
                jaw.xRot = Mth.lerp(gape, jaw.xRot, 1.05F);
                jaw.xRot = Mth.lerp(held, jaw.xRot, .3F);
                jaw.xRot = Mth.lerp(fling, jaw.xRot, .9F);
                body.zRot += -.18F * worry;
                body.yRot += -.12F * worry;
                for (int i = 0; i < 4; i++) {
                    boolean front = i < 2;
                    // Braced: forelegs planted forward, haunches low.
                    legs[i].xRot = Mth.lerp(Math.max(gape, held), legs[i].xRot, front ? -.5F : .45F);
                }
                root.y += 1.0F * held;
                for (ModelPart hackle : hackles) hackle.xRot -= .6F * held;
            }
            case BeastTamerMoves.HOWL -> {
                float w = BeastTamerMoves.HOWL_WINDUP, wave = BeastTamerMoves.HOWL_WAVE;
                float rise = HemoEasing.smoothstep(0, w - 2, since) * (1 - HemoEasing.smoothstep(w + wave + 2, w + wave + 14, since));
                float open = HemoEasing.smoothstep(w - 6, w, since) * (1 - HemoEasing.smoothstep(w + wave, w + wave + 8, since));
                float heave = Mth.sin(since * .5F) * (1 - open) * rise;
                // Sat back on its haunches, chest heaving, the head thrown back, then the jaws wide for the howl.
                body.xRot += -.3F * rise + .03F * heave;
                root.y += .6F * rise;
                head.xRot += -1.05F * rise;
                head.y += -1.2F * rise;
                jaw.xRot = Mth.lerp(rise, jaw.xRot, .15F + .95F * open);
                for (int i = 0; i < 4; i++) {
                    boolean front = i < 2;
                    legs[i].xRot = Mth.lerp(rise, legs[i].xRot, front ? -.2F : -.6F);
                    shins[i].xRot = Mth.lerp(rise, shins[i].xRot, front ? 0.0F : .9F);
                }
                rightEar.xRot += .5F * rise;
                leftEar.xRot += .5F * rise;
                tail.xRot += .3F * rise;
            }
            default -> {
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
'''


def build():
    # The hackles, straps, jaw and teeth do not fit the physical Hound's 128-pixel atlas alongside it.
    puppet_rig.ATLAS = 128
    return build_rig('true_sanguine_hound', 'TrueSanguineHoundModel', anatomy(), JAVA, shade, PALETTE, glow=GLOW)


if __name__ == '__main__':
    build()

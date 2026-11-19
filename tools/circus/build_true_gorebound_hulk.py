"""The Gorebound Hulk's true self (2026-11-17): what the Hulk is inside the Strongman's Chamber of Will. Wilder than
its physical form: the yoke burst (its straps hang broken), chains trailing from the iron bindings, more mass on the
shoulders and back, bone spurs through the hide, glowing blood-vents steaming along the spine, and a lower, more animal
stance with the head thrust forward. The physical model is untouched; this is its own rig, on the same joints so the
animation code reads alike.
"""
import puppet_rig
from puppet_rig import build as build_rig, noise

import build_gorebound_hulk as physical

# The physical palette, a hotter flesh, and a vent glow (index 12).
PALETTE = physical.PALETTE + ['#ff6a4a']


def anatomy():
    parts = physical.anatomy()
    by_name = {part['name']: part for part in parts}

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    # A lower, forward stance: the chest pitched further over the pelvis, the head thrust out.
    by_name['chest']['rotation'] = (28, 0, 0)
    by_name['head']['rotation'] = (-22, 0, 0)
    # The yoke burst: each strap snapped and hanging from the shoulder.
    for side, sign in (('left', -1), ('right', 1)):
        strap = by_name[side + '_strap']
        strap['cubes'] = [box((-1, -5, -.5), (2, 5, 1), 'strap')]
        strap['rotation'] = (0, 0, -sign * 32)
    # More mass: a heaped back and a second ridge of trapezius.
    joint('hump', 'chest', (0, -11, 2), [
        box((-6, -4, -2), (12, 5, 6), 'muscle'),
        box((-4, -6, -1), (8, 2, 4), 'muscle')])
    for side, sign in (('left', -1), ('right', 1)):
        joint(side + '_shoulder_mass', 'chest', (sign * 7, -11, 0), [
            box((-3, -3, -3.5), (6, 4, 7), 'muscle')], (0, 0, sign * 14))
        # Bone spurs through the hide of the forearms.
        joint(side + '_spur', side + '_forearm', (sign * 3.5, 2, 0), [
            box((-.5, -3, -.5), (1, 4, 1), 'bone')], (0, 0, sign * 35))
        # A chain trailing from each iron binding, its last links on the ground.
        joint(side + '_chain', side + '_binding', (0, 2, -3.5), [
            box((-.5, 0, -.5), (1, 3, 1), 'iron'),
            box((-.75, 3, -.75), (1.5, 1, 1.5), 'iron'),
            box((-.5, 4, -.5), (1, 3, 1), 'iron')], (20, 0, 0))
    # Blood-vents steaming along the spine.
    for i, y in enumerate((-9.5, -6.5, -3.5)):
        joint('vent_' + str(i), 'chest', (0, y, 4), [box((-1.5, -1, -.5), (3, 2, 1.5), 'vent')])
    return parts


def shade(material, side, px, py, w, h, gx, gy):
    if material == 'vent':
        if side == 'south': return 12 if (px + py) % 2 == 0 else 7
        return 6
    if material in ('flesh', 'fist', 'muscle'):
        # Hotter, rawer hide than the physical body: the base shade a step redder, with open weals.
        base = physical.shade(material, side, px, py, w, h, gx, gy)
        if noise(gx, gy, 11) < .07: return 8
        return min(base + 1, 8) if base in (5, 6) else base
    return physical.shade(material, side, px, py, w, h, gx, gy)


POSE = r'''    /**
     * The true self's weight: a heavy gait (deeper strides, the body dropping on each footfall and rolling over the
     * planted leg) and its ordeal moves (OrdealHulkMoves), each a slow gather, a fast blow and a heavy recovery.
     */
    private void ordealPose(float limbSwing, float movement, int move, float since) {
        movement = Mth.clamp(movement, 0, 1);
        float stride = limbSwing * .6662F;
        root.y += Math.abs(Mth.sin(stride)) * 1.2F * movement;
        chest.zRot += Mth.cos(stride) * .09F * movement;
        chest.yRot += Mth.cos(stride) * .07F * movement;
        for (int i = 0; i < 2; i++) {
            thighs[i].xRot *= 1.5F;
            shins[i].xRot *= 1.4F;
            feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            arms[i].xRot -= Mth.cos(stride + i * Mth.PI) * .3F * movement;
        }
        if (move < 0) return;
        switch (move) {
            case OrdealHulkMoves.HAMMER -> {
                float w = OrdealHulkMoves.HAMMER_WINDUP;
                float gather = HemoEasing.smoothstep(0, w - 3, since), down = fall(since, w), held = 1 - HemoEasing.smoothstep(w + 5, w + 16, since);
                float tremble = Mth.sin(since * 3.1F) * HemoEasing.smoothstep(w - 5, w - 2, since) * (1 - down);
                for (int i = 0; i < 2; i++) {
                    arms[i].xRot = Mth.lerp(held, arms[i].xRot, Mth.lerp(down, -2.9F + .06F * tremble, -.85F));
                    arms[i].zRot *= 1 - .7F * held;
                    forearms[i].xRot = Mth.lerp(held, forearms[i].xRot, Mth.lerp(down, -1.0F, -.15F));
                    thighs[i].xRot += -.35F * down * held;
                    shins[i].xRot += .6F * down * held;
                    feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
                }
                chest.xRot += held * (-.32F * gather * (1 - down) + .5F * down);
                head.xRot += .2F * down * held;
                jaw.xRot += .3F * gather * (1 - down) * held;
            }
            case OrdealHulkMoves.CHARGE -> {
                float w = OrdealHulkMoves.CHARGE_WINDUP, run = OrdealHulkMoves.CHARGE_RUN;
                float crouch = HemoEasing.smoothstep(0, w - 4, since) * (1 - HemoEasing.smoothstep(w + run, w + run + 14, since));
                float running = HemoEasing.smoothstep(w - 1, w + 1, since) * (1 - HemoEasing.smoothstep(w + run, w + run + 4, since));
                // Pawing the floor with its right foot through the wind-up, then a lowered-head rush.
                float paw = Mth.sin(since * .9F) * .35F * crouch * (1 - running);
                thighs[1].xRot += paw;
                chest.xRot += .45F * crouch + .3F * running;
                head.xRot += -.35F * crouch * (1 - running) + .2F * running;
                jaw.xRot += .25F * crouch;
                for (int i = 0; i < 2; i++) {
                    arms[i].xRot = Mth.lerp(crouch, arms[i].xRot, Mth.lerp(running, .6F, -.7F));
                    forearms[i].xRot = Mth.lerp(crouch, forearms[i].xRot, -.4F);
                    float sprint = Mth.cos(since * 1.4F + i * Mth.PI) * running;
                    thighs[i].xRot += sprint * .9F;
                    shins[i].xRot += Math.max(0, -sprint) * .9F;
                    feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
                }
                // It stumbles out of the rush.
                float stumble = HemoEasing.smoothstep(w + run, w + run + 3, since) * (1 - HemoEasing.smoothstep(w + run + 3, w + run + 14, since));
                chest.zRot += Mth.sin(since * 1.3F) * .15F * stumble;
            }
            case OrdealHulkMoves.GRAB -> {
                float w = OrdealHulkMoves.GRAB_WINDUP, hold = OrdealHulkMoves.GRAB_HOLD;
                float reach = HemoEasing.smoothstep(0, w - 2, since), clamp = HemoEasing.smoothstep(w - 1, w + 1, since);
                float lift = HemoEasing.smoothstep(w + 1, w + 5, since) * (1 - HemoEasing.smoothstep(w + hold - 1, w + hold + 1, since));
                float thrown = HemoEasing.smoothstep(w + hold - 1, w + hold + 1, since) * (1 - HemoEasing.smoothstep(w + hold + 4, w + hold + 14, since));
                float any = reach * (1 - HemoEasing.smoothstep(w + hold + 4, w + hold + 14, since));
                for (int i = 0; i < 2; i++) {
                    float side = i == 0 ? -1 : 1;
                    float x = Mth.lerp(lift, -1.6F, -2.9F);
                    x = Mth.lerp(thrown, x, -.9F);
                    arms[i].xRot = Mth.lerp(any, arms[i].xRot, x);
                    arms[i].zRot = Mth.lerp(any, arms[i].zRot, Mth.lerp(clamp, -side * .65F, -side * .12F));
                    forearms[i].xRot = Mth.lerp(any, forearms[i].xRot, Mth.lerp(lift, -.1F, -.45F));
                }
                chest.xRot += .25F * reach * (1 - lift) - .25F * lift + .45F * thrown;
                jaw.xRot += .35F * lift;
            }
            case OrdealHulkMoves.STOMP -> {
                float w = OrdealHulkMoves.STOMP_WINDUP;
                float raise = HemoEasing.smoothstep(0, w - 3, since), down = fall(since, w), held = 1 - HemoEasing.smoothstep(w + 4, w + 16, since);
                float up = raise * (1 - down) * held;
                thighs[1].xRot = Mth.lerp(up, thighs[1].xRot, -1.45F);
                shins[1].xRot = Mth.lerp(up, shins[1].xRot, 1.3F);
                feet[1].xRot = -thighs[1].xRot - shins[1].xRot;
                for (int i = 0; i < 2; i++) {
                    float side = i == 0 ? -1 : 1;
                    arms[i].zRot += -side * .9F * up;
                    arms[i].xRot += -.4F * up + .5F * down * held;
                    thighs[i].xRot += -.3F * down * held;
                    shins[i].xRot += .5F * down * held;
                    feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
                }
                chest.xRot += -.2F * up + .4F * down * held;
                head.xRot += .25F * down * held;
            }
            default -> {
            }
        }
    }

    /** 0 until two ticks before {@code at}, then 1 at {@code at}, accelerating like a falling weight. */
    private static float fall(float since, float at) {
        float t = Mth.clamp((since - (at - 2.0F)) / 2.0F, 0.0F, 1.0F);
        return t * t;
    }

    @Override
    public void renderToBuffer('''


JAVA = (physical.JAVA
        .replace('// Authored by tools/circus/build_gorebound_hulk.py', '// Authored by tools/circus/build_true_gorebound_hulk.py (the Hulk\'s true self, 2026-11-17)')
        .replace('public class GoreboundHulkModel', 'public class TrueGoreboundHulkModel')
        .replace('"gorebound_hulk"), "main")', '"true_gorebound_hulk"), "main")')
        .replace('public GoreboundHulkModel(ModelPart bakedRoot)', 'public TrueGoreboundHulkModel(ModelPart bakedRoot)')
        .replace('ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "true_gorebound_hulk")', 'Hemomancy.rloc("true_gorebound_hulk")')
        .replace('import net.minecraft.resources.ResourceLocation;\n', '')
        .replace("""        setupPose(limbSwing, limbSwingAmount, age, yaw, pitch,
                windup > 0 ? windup + partialTick : 0, attackTime);
    }""", """        setupPose(limbSwing, limbSwingAmount, age, yaw, pitch,
                windup > 0 ? windup + partialTick : 0, attackTime);
        ordealPose(limbSwing, limbSwingAmount, entity.ordealMove(), entity.ordealMoveAge(partialTick));
    }""")
        .replace("""    @Override
    public void renderToBuffer(""", POSE)
        .replace('import net.minecraft.util.Mth;\n', 'import net.minecraft.util.Mth;\nimport com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealHulkMoves;\nimport com.vincenthuto.hemomancy.common.util.HemoEasing;\n'))


def build():
    # The true self's extra mass, spurs, chains and vents do not fit the physical Hulk's 128-pixel atlas.
    puppet_rig.ATLAS = 256
    return build_rig('true_gorebound_hulk', 'TrueGoreboundHulkModel', anatomy(), JAVA, shade, PALETTE, glow={12})


if __name__ == '__main__':
    build()

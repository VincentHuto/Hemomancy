"""The Cinder Bellows' true self (2026-11-17): what the Bellows is inside the Fire Breather's Chamber of Will. Wilder than
its physical form, a bellows-beast of ember and breath: it rears higher on its coil, a pleated bellows hood flares
round its head with fire in the folds, bone horns sweep back off the skull, fangs hang from the muzzle, ember vents
burn along its spine, cinder spines stand up off the coil, and the torch it carries burns wild. The physical model is
untouched; this is its own rig on the same joints, so the animation code reads alike, posed for its ordeal moves
(CinderBellowsOrdealMoves): the inhale and blast, the vent, the coil lash and the lunge.
"""
import puppet_rig
from puppet_rig import build as build_rig, noise

import build_cinder_bellows as physical

# The physical palette and a white-hot core (index 12).
PALETTE = physical.PALETTE + ['#fff1c2']
GLOW = physical.EMBER | {12}
HEIGHTS = [7, 6, 6, 5, 5, 4, 4, 3, 3, 2, 2]


def anatomy():
    parts = physical.anatomy()
    by_name = {part['name']: part for part in parts}

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    # Rearing higher: the trunk straighter over the coil, the head thrust forward.
    by_name['pelvis']['rotation'] = (-14, 0, 0)
    by_name['abdomen']['rotation'] = (6, 0, 0)
    by_name['head']['rotation'] = (-15, 0, 0)
    # The torch burning wild: a bigger head of fire on it.
    by_name['torch']['cubes'] = [
        box((-.5, -5, -.5), (1, 7, 1), 'wood'),
        box((-1.5, -8, -1.5), (3, 3, 3), 'ember'),
        box((-1, -10, -1), (2, 2, 2), 'hot'),
        box((-.5, -11.5, -.5), (1, 1.5, 1), 'hot')]
    for side, sign in (('left', -1), ('right', 1)):
        # The bellows hood: pleated leather flared round the head, fire in its folds.
        joint(side + '_hood', 'neck', (sign * 1, -4, .5), [
            box((0 if sign > 0 else -6, -7, -.5), (6, 9, 1), 'hood')], (0, sign * -35, sign * -12))
        # Bone horns swept back off the skull, curling up at the tips.
        joint(side + '_horn', 'head', (sign * 2, -4.5, 1), [box((-.5, -.5, 0), (1, 1, 4), 'bone')], (20, sign * 20, 0))
        joint(side + '_horn_tip', side + '_horn', (0, 0, 4), [box((-.5, -2.5, -.5), (1, 3, 1), 'bone')], (-30, 0, 0))
        # Fangs hanging from the muzzle.
        joint(side + '_fang', 'head', (sign * 1, -.5, -5), [box((-.25, 0, -.25), (.5, 1.5, .5), 'teeth')])
    # Ember vents burning along the spine.
    for i, y in enumerate((-8, -5.5, -3)):
        joint('vent_' + str(i), 'chest', (0, y, 2.5), [box((-1.5, -1, -.5), (3, 1.5, 1.5), 'vent')])
    joint('vent_3', 'abdomen', (0, -4, 2.5), [box((-1.5, -1, -.5), (3, 1.5, 1.5), 'vent')])
    # Cinder spines up off the coil, shorter toward the tail.
    for i in range(7):
        name = 'coil' if i == 0 else 'tail_' + str(i - 1)
        height = max(1, 3 - i // 3)
        joint('spine_' + str(i), name, (0, -HEIGHTS[i], 1), [box((-.5, -height, -.5), (1, height, 1), 'spike')], (-25, 0, 0))
    return parts


def shade(material, side, px, py, w, h, gx, gy):
    if material == 'hood':
        if side in ('north', 'south'):
            # Pleats across the hood, leather over fire: every other row a fold with embers showing.
            if py % 2 == 1:
                return 11 if noise(gx, gy, 31) > .6 else 10 if noise(gx, gy, 32) > .3 else 9
            return 8 if px in (0, w - 1) else 7
        return 7
    if material == 'vent':
        if side in ('up', 'south'):
            return 12 if (px + py) % 2 == 0 else 11
        return 9
    if material == 'hot':
        return 12 if side != 'down' else 11
    if material == 'spike':
        if side == 'up' or py == 0:
            return 10
        return 6 if py < h - 1 else 5
    return physical.shade(material, side, px, py, w, h, gx, gy)


POSE = r'''    /**
     * The true self's weight: a slither (the coil undulating harder, the trunk swaying over it) and its ordeal moves
     * (CinderBellowsOrdealMoves), each a slow gather, a fast blow and a heavy recovery.
     */
    private void ordealPose(float limbSwing, float movement, float age, int move, float since) {
        float travel = Mth.clamp(movement, 0, 1);
        float wave = age * .11F + limbSwing * .9F;
        for (int i = 0; i < tail.length; i++) tail[i].yRot += Mth.sin(wave - i * .7F) * .12F * travel;
        pelvis.yRot += Mth.sin(wave + .6F) * .12F * travel;
        root.y -= Math.abs(Mth.sin(wave)) * .6F * travel;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            hoods[i].yRot += sign * Mth.sin(age * .09F) * .05F;
        }
        if (move < 0) return;
        switch (move) {
            case CinderBellowsOrdealMoves.BLAST -> {
                float w = CinderBellowsOrdealMoves.BLAST_WINDUP, burn = CinderBellowsOrdealMoves.BLAST_BURN;
                float inhale = HemoEasing.smoothstep(0, w - 3, since), down = fall(since, w);
                float hold = 1 - HemoEasing.smoothstep(w + burn + 2, w + burn + 18, since);
                float tremble = Mth.sin(since * 3.1F) * HemoEasing.smoothstep(w - 6, w - 2, since) * (1 - down);
                float fill = inhale * (1 - down) * hold, blow = down * hold;
                // Rearing back to fill, the bellows swelling and the hood spreading; then thrown into the blast.
                abdomen.xRot += -.25F * fill + .2F * blow;
                chest.xRot += -.3F * fill + .3F * blow;
                neck.xRot += -.4F * fill + .02F * tremble + .55F * blow;
                head.xRot += .25F * fill - .4F * blow;
                jaw.xRot += .1F * fill + 1.0F * blow;
                for (ModelPart lung : lungs) {
                    float swell = 1 + .5F * fill - .15F * blow;
                    lung.xScale *= swell;
                    lung.zScale *= swell;
                }
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    hoods[i].yRot += sign * -.5F * Math.max(fill, blow);
                    arms[i].xRot += .6F * fill - .9F * blow;
                    arms[i].zRot += -sign * .3F * fill;
                    forearms[i].xRot += -.3F * blow;
                }
            }
            case CinderBellowsOrdealMoves.VENT -> {
                float w = CinderBellowsOrdealMoves.VENT_WINDUP;
                float stoke = HemoEasing.smoothstep(0, w - 2, since), down = fall(since, w);
                float hold = 1 - HemoEasing.smoothstep(w + 4, w + 18, since);
                float hunch = stoke * (1 - down) * hold, burst = down * hold;
                // Hunched over the furnace in its ribs, arms crossed in; then thrown open, ribs and hood flared.
                chest.xRot += .35F * hunch - .35F * burst;
                neck.xRot += .3F * hunch - .3F * burst;
                head.xRot += -.2F * hunch + .25F * burst;
                jaw.xRot += .9F * burst;
                for (ModelPart lung : lungs) {
                    float swell = 1 + .35F * hunch + .25F * burst;
                    lung.xScale *= swell;
                    lung.zScale *= swell;
                }
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    arms[i].zRot += sign * .5F * hunch - sign * 1.2F * burst;
                    arms[i].xRot += -.5F * hunch - .3F * burst;
                    forearms[i].xRot += -.6F * hunch + .5F * burst;
                    hoods[i].yRot += sign * -.7F * burst;
                }
                for (ModelPart rib : ribs) rib.xRot += -.5F * burst;
            }
            case CinderBellowsOrdealMoves.LASH -> {
                float w = CinderBellowsOrdealMoves.LASH_WINDUP, spin = FireEaterOrdealGeometry.LASH_TICKS;
                float draw = HemoEasing.smoothstep(0, w - 2, since), down = fall(since, w);
                float hold = 1 - HemoEasing.smoothstep(w + spin + 2, w + spin + 14, since);
                // The coil drawn tight, then whipped all the way round under it.
                float whip = HemoEasing.smoothstep(w - 1, w + spin, since);
                for (int i = 0; i < tail.length; i++) tail[i].yRot += (.14F * draw * (1 - down) - .08F * down) * hold;
                coil.yRot += -Mth.TWO_PI * whip * hold;
                pelvis.zRot += (.15F * draw * (1 - down) - .2F * down) * hold;
                chest.xRot += -.15F * draw * hold;
                jaw.xRot += .5F * down * hold;
                for (int i = 0; i < 2; i++) arms[i].zRot += (i == 0 ? 1 : -1) * .6F * down * hold;
            }
            case CinderBellowsOrdealMoves.LUNGE -> {
                float w = CinderBellowsOrdealMoves.LUNGE_WINDUP, run = CinderBellowsOrdealMoves.LUNGE_RUN;
                float coilUp = HemoEasing.smoothstep(0, w - 2, since), down = fall(since, w);
                float hold = 1 - HemoEasing.smoothstep(w + run + 2, w + run + 16, since);
                float drawn = coilUp * (1 - down) * hold, strike = down * hold;
                // Drawn back in an S like a striking snake, then all of it thrown forward jaws first.
                abdomen.xRot += .35F * drawn + .45F * strike;
                chest.xRot += -.45F * drawn + .35F * strike;
                neck.xRot += -.55F * drawn + .45F * strike;
                head.xRot += .4F * drawn - .35F * strike;
                jaw.xRot += .2F * drawn + 1.1F * strike;
                for (int i = 0; i < 2; i++) {
                    arms[i].xRot += .5F * drawn - 1.4F * strike;
                    forearms[i].xRot += .4F * strike;
                    hoods[i].yRot += (i == 0 ? 1 : -1) * -.5F * strike;
                }
            }
            default -> {
            }
        }
    }

    /** 0 until two ticks before {@code at}, then 1 at {@code at}, accelerating like a thrown thing. */
    private static float fall(float since, float at) {
        float t = Mth.clamp((since - (at - 2.0F)) / 2.0F, 0.0F, 1.0F);
        return t * t;
    }

    @Override
    public void renderToBuffer('''


JAVA = (physical.JAVA
        # The physical model's Java now eases through HemoEasing; its generator still writes a private smooth().
        .replace('''    private static float smooth(float value) {
        float t = Mth.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }

''', '')
        .replace('smooth(', 'HemoEasing.smoothstep(')
        .replace('// Authored by tools/circus/build_cinder_bellows.py', '// Authored by tools/circus/build_true_cinder_bellows.py (the Bellows\' true self, 2026-11-17)')
        .replace('public class CinderBellowsModel', 'public class TrueCinderBellowsModel')
        .replace('ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "cinder_bellows"), "main")',
                 'Hemomancy.rloc("true_cinder_bellows"), "main")')
        .replace('public CinderBellowsModel(ModelPart bakedRoot)', 'public TrueCinderBellowsModel(ModelPart bakedRoot)')
        .replace('import net.minecraft.resources.ResourceLocation;\n', '')
        .replace('    private final ModelPart[] lungs, arms, forearms, hands;',
                 '    private final ModelPart[] lungs, arms, forearms, hands, hoods, ribs;\n    private final ModelPart coil;')
        .replace('        ModelPart parent = root.getChild("coil");',
                 '        coil = root.getChild("coil");\n'
                 '        hoods = new ModelPart[]{neck.getChild("left_hood"), neck.getChild("right_hood")};\n'
                 '        ribs = new ModelPart[]{chest.getChild("left_rib_0"), chest.getChild("left_rib_1"), chest.getChild("left_rib_2"),\n'
                 '                chest.getChild("right_rib_0"), chest.getChild("right_rib_1"), chest.getChild("right_rib_2")};\n'
                 '        ModelPart parent = coil;')
        .replace("""        setupPose(limbSwing, limbSwingAmount, age, yaw, pitch,
                cycle == 0 ? 0 : Math.min(100, cycle + Mth.frac(age)));
    }""", """        setupPose(limbSwing, limbSwingAmount, age, yaw, pitch,
                cycle == 0 ? 0 : Math.min(100, cycle + Mth.frac(age)));
        ordealPose(limbSwing, limbSwingAmount, age, entity.ordealMove(), entity.ordealMoveAge(Mth.frac(age)));
    }""")
        .replace('extends EntityModel<CinderBellowsEntity>', 'extends EntityModel<CinderBellowsEntity>')
        .replace("""    @Override
    public void renderToBuffer(""", POSE)
        .replace('import net.minecraft.util.Mth;\n', 'import net.minecraft.util.Mth;\n'
                 'import com.vincenthuto.hemomancy.common.encounter.ordeal.CinderBellowsOrdealMoves;\n'
                 'import com.vincenthuto.hemomancy.common.encounter.ordeal.FireEaterOrdealGeometry;\n'
                 'import com.vincenthuto.hemomancy.common.util.HemoEasing;\n'))


def build():
    puppet_rig.ATLAS = 256
    return build_rig('true_cinder_bellows', 'TrueCinderBellowsModel', anatomy(), JAVA, shade, PALETTE, glow=GLOW)


if __name__ == '__main__':
    build()

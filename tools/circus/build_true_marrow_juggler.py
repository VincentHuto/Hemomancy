"""The Marrow Juggler's true self (2026-11-17): what the Juggler is inside the Dagger Thrower's Chamber of Will. Wilder
than its physical form: its three masks cracked with marrow-light in the eye holes, the hood torn into ribbons, the
harlequin doublet split down the front over a cage of ribs lit by the marrow inside, the six arms bare bone below torn
sleeve stubs, the skirt's panels turned to hanging blades trailing threads of blood, and it juggles three daggers and
three marrow bones at once. The physical model is untouched; this is its own rig, on the same joints so the animation
code reads alike.
"""
import puppet_rig
from puppet_rig import build as build_rig, noise

import build_marrow_juggler as physical

# The physical palette and two marrow glows (12 and 13).
PALETTE = physical.PALETTE + ['#ff3a2a', '#ffb070']
GLOW = {12, 13}
# The masks cracked: 'k' a crack, 'g' marrow-light in an eye hole.
MASKS = {
    'mourn_cracked': ['99k99', '9gkg9', '959k9', '90009', '9k909'],
    'laugh_cracked': ['99999', 'gk9g9', '9k999', '05550', '9k009'],
    'blank_cracked': ['9k999', '9gkg9', '99k99', '999k9', '98889'],
}
MASK_KEYS = {'0': 0, '5': 5, '8': 8, '9': 9, 'k': 0, 'g': 12}


def anatomy():
    parts = [p for p in physical.anatomy() if not p['name'].startswith(('hood_point', 'hood_tip'))]
    by_name = {part['name']: part for part in parts}

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    # The doublet split down the front, over a cage of ribs and the marrow lit inside it.
    by_name['body']['cubes'] = [box((-4, -9, -2.5), (8, 6, 5), 'doublet_torn'), box((-3, -3, -2), (6, 3, 4), 'doublet')]
    joint('marrow', 'body', (0, -6, -2.25), [box((-1.5, -2.5, -.25), (3, 4, .5), 'marrow')])
    joint('ribs', 'body', (0, -6, -2.75), [
        box((-.5, -3, -.25), (1, 4.5, .5), 'bone'),
        box((-2.5, -2.5, -.25), (5, .5, .5), 'bone'),
        box((-2.5, -1, -.25), (5, .5, .5), 'bone'),
        box((-2, .5, -.25), (4, .5, .5), 'bone')])
    by_name['hood']['cubes'] = [box((-3, -6, -2.5), (6, 6, 5), 'hood_torn')]
    for name in ('mourn', 'laugh', 'blank'):
        mask = by_name['mask_' + name]
        mask['cubes'] = [box((-2.5, -2.5, -1), (5, 5, 1), name + '_cracked')]
    # The hood torn into ribbons streaming behind.
    for k, x in enumerate((-2, -.75, .75, 2)):
        joint('ribbon_' + str(k), 'hood', (x, -1, 2.5), [box((-.5, 0, -.25), (1, 6 + k % 2 * 2, .5), 'rag')], (30, 0, x * 6))
    # Six bare bone arms below torn sleeve stubs, a little longer than the physical ones.
    for i in range(6):
        by_name['arm_' + str(i)]['cubes'] = [box((-.5, 0, -.5), (1, 6, 1), 'bone'), box((-1, -.5, -1), (2, 1.5, 2), 'sleeve')]
        forearm = by_name['forearm_' + str(i)]
        forearm['pivot'] = (0, 5.5, 0)
        forearm['cubes'] = [box((-.5, 0, -.5), (1, 5, 1), 'bone'), box((-.75, 3.5, -.75), (1.5, 1, 1.5), 'hematic_iron')]
        by_name['hand_' + str(i)]['pivot'] = (0, 5, 0)
    # The skirt's panels are hanging blades now, each trailing a thread of blood.
    for part in parts:
        if part['parent'] == 'skirt' and part['name'] != 'sash':
            length = part['cubes'][0]['size'][1]
            part['cubes'] = [box((-.75, 0, -.25), (1.5, length + 2, .5), 'blade')]
        if part['name'].endswith('_rag'):
            part['pivot'] = (0, part['pivot'][1] + 2, 0)
            part['cubes'] = [box((-.25, 0, -.25), (.5, 4, .5), 'thread')]
    # Three marrow bones juggled with the three daggers.
    for i in range(3):
        joint('bone_' + str(i), 'body', (0, -8, -6), [
            box((-.5, -3, -.5), (1, 6, 1), 'bone'),
            box((-1, -4.5, -1), (2, 1.5, 2), 'bone'),
            box((-1, 3, -1), (2, 1.5, 2), 'marrow_end')])
    return parts


def shade(material, side, px, py, w, h, gx, gy):
    if material == 'doublet_torn':
        # The split down the front: the dark inside where the ribs show.
        if side == 'north' and 2 <= px <= w - 3 and py > 0:
            return 0
        return physical.shade('doublet', side, px, py, w, h, gx, gy)
    if material == 'hood_torn':
        base = physical.shade('hood', side, px, py, w, h, gx, gy)
        if side not in ('up', 'down') and noise(gx, gy, 21) < .12:
            return 0
        return base
    if material in MASKS:
        if side == 'north':
            return MASK_KEYS[MASKS[material][py][px]]
        return 7
    if material == 'marrow':
        return 13 if (px + py) % 3 == 0 else 12
    if material == 'marrow_end':
        if side == 'down' or side == 'up':
            return 12
        return 12 if py >= h - 1 else 7
    if material == 'thread':
        return 4 if noise(gx, gy, 23) > .3 else 3
    return physical.shade(material, side, px, py, w, h, gx, gy)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.MarrowJugglerOrdealMoves;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

// Authored by tools/circus/build_true_marrow_juggler.py (the Juggler's true self, 2026-11-17); regenerate together.
/**
 * The Marrow Juggler's true self: it flies higher and wilder than its physical body, banking into its motion and
 * trailing its ribbons and blades, and juggles three daggers and three marrow bones at once. Its ordeal moves
 * ({@link MarrowJugglerOrdealMoves}) pose from an entity event, each a gather, a fast blow and a recovery: the bones
 * tossed high for the hail, three arms cocked and whipped through for the volley, and the spin of six blades for the
 * whirl.
 */
public class TrueMarrowJugglerModel extends EntityModel<MarrowSpitterEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("true_marrow_juggler"), "main");
    private final ModelPart root, body, hood;
    private final ModelPart[] arms = new ModelPart[6], forearms = new ModelPart[6], hands = new ModelPart[6];
    private final ModelPart[] knives = new ModelPart[3], bones = new ModelPart[3], ribbons = new ModelPart[4];
    private final ModelPart[] hems;

    public TrueMarrowJugglerModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        body = root.getChild("body");
        hood = body.getChild("hood");
        for (int i = 0; i < arms.length; i++) {
            arms[i] = body.getChild("arm_" + i);
            forearms[i] = arms[i].getChild("forearm_" + i);
            hands[i] = forearms[i].getChild("hand_" + i);
        }
        for (int i = 0; i < 3; i++) {
            knives[i] = body.getChild("knife_" + i);
            bones[i] = body.getChild("bone_" + i);
        }
        for (int i = 0; i < ribbons.length; i++) ribbons[i] = hood.getChild("ribbon_" + i);
        ModelPart skirt = body.getChild("skirt");
        hems = new ModelPart[]{skirt.getChild("front_left"), skirt.getChild("front_right"),
                skirt.getChild("rear_left"), skirt.getChild("rear_right"),
                skirt.getChild("side_left"), skirt.getChild("side_right"), skirt.getChild("sash")};
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
    }

    @Override
    public void setupAnim(MarrowSpitterEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        float facing = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot) * Mth.DEG_TO_RAD;
        double dx = entity.getX() - entity.xo, dz = entity.getZ() - entity.zo;
        // Its motion in its own frame: forward along its facing, and to its side.
        float forward = (float) (-dx * Mth.sin(facing) + dz * Mth.cos(facing));
        float lateral = (float) (dx * Mth.cos(facing) + dz * Mth.sin(facing));
        setupPose(age, yaw, pitch, forward, lateral, entity.ordealMove(), entity.ordealMoveAge(partialTick));
    }

    /** Poses the rig; {@code move} is -1 between moves, {@code since} the ticks since its wind-up began. */
    public void setupPose(float age, float yaw, float pitch, float forward, float lateral, int move, float since) {
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.visible = true;
        });
        float lean = Mth.clamp(forward * 2.2F, -.45F, .55F), bank = Mth.clamp(lateral * 2.6F, -.55F, .55F);
        float speed = Math.min(1.0F, (Math.abs(forward) + Math.abs(lateral)) * 4.0F);
        root.y += -1.5F + Mth.sin(age * .09F) * .6F;
        body.xRot += lean;
        body.zRot += Mth.sin(age * .05F) * .04F - bank;
        hood.yRot += Mth.clamp(yaw, -40, 40) * Mth.DEG_TO_RAD * .6F;
        hood.xRot += Mth.clamp(pitch, -25, 25) * Mth.DEG_TO_RAD * .4F - lean * .5F;
        for (int i = 0; i < hems.length; i++) hems[i].xRot += Mth.sin(age * .11F + i * 1.1F) * .1F + .5F * speed;
        for (int i = 0; i < ribbons.length; i++) ribbons[i].xRot += Mth.sin(age * .23F + i * 1.7F) * .25F + .6F * speed;
        // The cascade, faster and wider than the physical body's, quicker still while it gathers a hail.
        float hurry = move == MarrowJugglerOrdealMoves.HAIL ? HemoEasing.smoothstep(0.0F, MarrowJugglerOrdealMoves.HAIL_TOSS, since) : 0.0F;
        float juggle = age * .2F + since * .25F * hurry;
        for (int i = 0; i < arms.length; i++) {
            int pair = i / 2;
            float side = i % 2 == 0 ? -1 : 1;
            float phase = juggle * .9F + pair * Mth.TWO_PI / 3;
            arms[i].zRot -= side * Mth.sin(phase) * .16F;
            arms[i].xRot += Mth.cos(phase + i % 2 * Mth.PI) * .14F;
            forearms[i].xRot -= Mth.cos(phase + i % 2 * Mth.PI) * .2F;
        }
        for (int i = 0; i < 3; i++) {
            toss(knives[i], juggle + i * Mth.TWO_PI / 3, i * 2, i * 2 + 1, 11 + i * 2);
            toss(bones[i], juggle + i * Mth.TWO_PI / 3 + Mth.PI, i * 2 + 1, i * 2, 9 + i * 2);
        }
        if (move >= 0) move(move, since);
    }

    /** One juggled thing in the cascade, from one hand's palm to another's in an arc {@code height} high. */
    private void toss(ModelPart thing, float phase, int from, int to, float height) {
        float t = (1 - Mth.cos(phase)) * .5F;
        float arc = Mth.sin(phase);
        Vector3f a = palm(from), b = palm(to);
        thing.x = Mth.lerp(t, a.x, b.x);
        thing.y = Mth.lerp(t, a.y, b.y) - arc * arc * height;
        thing.z = Mth.lerp(t, a.z, b.z);
        thing.zRot = phase * 2;
    }

    /** 0 until 2.5 ticks before {@code at}, then 1 at {@code at}, accelerating like a whip. */
    private static float whip(float since, float at) {
        float t = Mth.clamp((since - (at - 2.5F)) / 2.5F, 0.0F, 1.0F);
        return t * t;
    }

    private void move(int move, float since) {
        switch (move) {
            case MarrowJugglerOrdealMoves.HAIL -> {
                // Arms whirling faster, all six thrown up together to loose the bones, then brought down hard on the fall.
                float w = MarrowJugglerOrdealMoves.HAIL_WINDUP, at = MarrowJugglerOrdealMoves.HAIL_TOSS;
                float gather = HemoEasing.smoothstep(0.0F, at, since), tossed = whip(since, at), down = whip(since, w);
                float hold = 1.0F - HemoEasing.smoothstep(w + 4.0F, w + 16.0F, since);
                for (int i = 0; i < arms.length; i++) {
                    float target = Mth.lerp(down, Mth.lerp(tossed, -.4F * gather, -2.6F), .5F);
                    arms[i].xRot = Mth.lerp(hold, arms[i].xRot, arms[i].getInitialPose().xRot + target);
                    forearms[i].xRot = Mth.lerp(hold, forearms[i].xRot, Mth.lerp(tossed, forearms[i].xRot, -.15F));
                }
                body.xRot += hold * (-.25F * tossed * (1.0F - down) + .4F * down);
                hood.xRot += hold * (-.3F * tossed * (1.0F - down) + .2F * down);
                for (ModelPart bone : bones) bone.visible = since < at || since > w + 14.0F;
            }
            case MarrowJugglerOrdealMoves.VOLLEY -> {
                // Three arms on one side cocked back with a dagger each, whipped through one after another.
                float w = MarrowJugglerOrdealMoves.VOLLEY_WINDUP;
                float hold = 1.0F - HemoEasing.smoothstep(w + 2 * MarrowJugglerOrdealMoves.VOLLEY_GAP + 4.0F,
                        w + 2 * MarrowJugglerOrdealMoves.VOLLEY_GAP + 16.0F, since);
                float turn = HemoEasing.smoothstep(0.0F, 6.0F, since) * hold;
                body.yRot += .4F * turn;
                for (int j = 0; j < 3; j++) {
                    float release = w + j * MarrowJugglerOrdealMoves.VOLLEY_GAP;
                    float cock = HemoEasing.smoothstep(0.0F, w - 2.0F, since) * (1.0F - whip(since, release));
                    float snap = whip(since, release) * (1.0F - HemoEasing.smoothstep(release + 2.0F, release + 10.0F, since));
                    int a = j * 2;
                    arms[a].xRot += hold * (2.1F * cock - 1.4F * snap);
                    forearms[a].xRot += hold * (-.9F * cock + .4F * snap);
                    body.xRot += .08F * snap;
                    if (cock > .3F) hold(knives[j], a, 1.6F);
                    knives[j].visible = since < release || since > release + 14.0F;
                }
            }
            case MarrowJugglerOrdealMoves.WHIRL -> {
                // Arms spread straight out with a blade in each hand, the whole body spinning faster as it climbs,
                // tucking on the dive, whirling on the floor, then slowing as it climbs back.
                float w = MarrowJugglerOrdealMoves.WHIRL_WINDUP, dive = MarrowJugglerOrdealMoves.WHIRL_DIVE;
                float spin = MarrowJugglerOrdealMoves.WHIRL_SPIN, recover = MarrowJugglerOrdealMoves.WHIRL_RECOVER;
                float spread = HemoEasing.smoothstep(0.0F, 10.0F, since)
                        * (1.0F - HemoEasing.smoothstep(w + dive + spin, w + dive + spin + 12.0F, since));
                float diving = HemoEasing.smoothstep(w - 1.0F, w + 1.0F, since) * (1.0F - HemoEasing.smoothstep(w + dive - 1.0F, w + dive, since));
                body.yRot += spinAngle(since, w, dive + spin, recover);
                body.xRot += .6F * diving;
                for (int i = 0; i < arms.length; i++) {
                    float side = i % 2 == 0 ? -1 : 1;
                    arms[i].zRot = Mth.lerp(spread, arms[i].zRot, -side * (1.5F + .1F * (i / 2 - 1)));
                    arms[i].xRot = Mth.lerp(spread, arms[i].xRot, (i / 2 - 1) * .35F);
                    forearms[i].xRot = Mth.lerp(spread, forearms[i].xRot, 0.0F);
                }
                if (spread > .3F) {
                    for (int i = 0; i < 3; i++) {
                        hold(knives[i], i * 2, 0.0F);
                        hold(bones[i], i * 2 + 1, 0.0F);
                    }
                }
            }
            default -> {
            }
        }
    }

    /** The whirl's turn so far: spun up through the climb, at full speed through the dive and spin, slowed after. */
    private static float spinAngle(float since, float climb, float floor, float recover) {
        float top = 1.3F;
        if (since < climb) return .5F * top * since * since / climb;
        float spun = .5F * top * climb;
        if (since < climb + floor) return spun + top * (since - climb);
        spun += top * floor;
        float t = Math.min(recover, since - climb - floor);
        return spun + top * t - .5F * top * t * t / recover;
    }

    /** Holds a juggled thing in one hand's palm, its point out along the arm, tipped {@code back} radians. */
    private void hold(ModelPart thing, int hand, float back) {
        Vector3f at = palm(hand);
        thing.x = at.x;
        thing.y = at.y;
        thing.z = at.z;
        thing.zRot = arms[hand].zRot;
        thing.xRot = arms[hand].xRot + back;
    }

    private Vector3f palm(int index) {
        PoseStack pose = new PoseStack();
        arms[index].translateAndRotate(pose);
        forearms[index].translateAndRotate(pose);
        hands[index].translateAndRotate(pose);
        return pose.last().pose().transformPosition(new Vector3f(0, .75F / 16, 0)).mul(16);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
'''


def build():
    puppet_rig.ATLAS = 128
    return build_rig('true_marrow_juggler', 'TrueMarrowJugglerModel', anatomy(), JAVA, shade, PALETTE, glow=GLOW)


if __name__ == '__main__':
    build()

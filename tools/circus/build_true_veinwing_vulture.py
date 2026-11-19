"""The Veinwing Vulture's true self (2026-11-17): what the Vulture is inside the Acrobat's Chamber of Will. Wilder and
less tamed than its physical form, and drawn twice over: the wing membranes longer and torn through, their veins lit,
bone spurs grown out at the wing joints, a bare red neck in a ruff of crimson quills, the harness strap snapped and
hanging, heavier hooked talons and a longer tail needle. The physical model is untouched; this is its own rig on the
same joints, so the animation code reads alike.
"""
import puppet_rig
from puppet_rig import build as build_rig, noise

import build_veinwing_vulture as physical

# The physical palette, the veins' glow (12), a hole torn through a membrane (13, made clear) and raw neck skin (14).
PALETTE = physical.PALETTE + ['#ff4a3a', '#010203', '#b8394a']
GLOW, HOLE, RAW = 12, 13, 14


def anatomy():
    parts = physical.anatomy()
    by_name = {part['name']: part for part in parts}

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    # A bare red neck thrust out of a heavier ruff.
    by_name['head']['cubes'][0] = box((-1, -2, -1), (2, 3, 2), 'raw')
    for i, (x, z, tilt) in enumerate(((-2.5, -1, -35), (2.5, -1, 35), (-2, 2, -25), (2, 2, 25), (0, 2.5, 0))):
        joint('quill_' + str(i), 'ruff', (x, -1, z), [box((-.5, -4, -.5), (1, 4, 1), 'quill')],
              (-20 if z > 0 else 10, 0, tilt))
    for side, sign in (('left', 1), ('right', -1)):
        lo = lambda length: 0 if sign > 0 else -length
        # Longer membranes, torn through.
        for i, length in enumerate((8, 11, 14)):
            membrane = by_name[f'{side}_membrane_{i}']
            membrane['cubes'] = [box((lo(3), 0, 0), (3, length, 1), 'torn'),
                                 box((sign * 2 if sign > 0 else -3, -.5, -.5), (1, length + 1, 1), 'vein')]
        # Bone spurs out of the wing joints.
        joint(side + '_spur', side + '_forewing', (0, -1, 0), [box((-.5, -3, -.5), (1, 3, 1), 'bone')], (0, 0, sign * 25))
        joint(side + '_elbow_spur', side + '_wing', (sign * 1, -1, 0), [box((-.5, -2, -.5), (1, 2, 1), 'bone')], (-30, 0, 0))
        # Heavier hooked talons.
        talon = by_name[side + '_talon']
        talon['cubes'] = [box((-.5, 0, -.5), (1, 3, 1), 'raw'),
                          box((-1.5, 3, -3.5), (1, 1, 4), 'talon'),
                          box((.5, 3, -3.5), (1, 1, 4), 'talon'),
                          box((-.5, 3, .5), (1, 1, 2), 'talon')]
        joint(side + '_hook', side + '_talon', (0, 3, -3.5), [
            box((-1.5, 0, -.5), (1, 2, 1), 'hook'), box((.5, 0, -.5), (1, 2, 1), 'hook')], (20, 0, 0))
    # The harness strap snapped and hanging off the back.
    joint('strap', 'torso', (0, -3, 2), [box((-.5, 0, 0), (1, 6, 1), 'hematic_iron')], (-15, 0, 10))
    # A longer tail needle.
    by_name['tail_needle']['cubes'] = [box((-.5, 0, -.5), (1, 5, 1), 'needle')]
    return parts


def shade(material, side, px, py, w, h, gx, gy):
    if material == 'raw':
        if side in ('up', 'down'): return 3
        return RAW if (py + px) % 3 else 4
    if material == 'quill':
        if py == 0: return GLOW if side == 'north' else 5
        return 4 if py < h // 2 else 3
    if material == 'torn':
        # Membrane torn through in ragged holes toward its trailing edge; the rest as the physical blade.
        if side in ('north', 'south') and py > 2 and noise(gx, gy, 71) < .18 + .25 * (py / max(1, h)):
            return HOLE
        return physical.shade('blade', side, px, py, w, h, gx, gy)
    if material == 'vein':
        if side in ('up', 'down'): return 3
        return GLOW if py % 3 == 0 or noise(gx, gy, 72) > .6 else 5
    return physical.shade(material, side, px, py, w, h, gx, gy)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.VultureOrdealMoves;
import com.vincenthuto.hemomancy.common.entity.summon.VeinwingVultureEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_true_veinwing_vulture.py (the Vulture's true self, 2026-11-17).
/**
 * The Veinwing Vulture's true self in the Acrobat's Chamber: a bird of prey that flies with weight, a fast downstroke
 * lifting the body and a slow folding recovery, banking into its turns and pitching with its climbs. Its ordeal moves
 * ({@link VultureOrdealMoves}) pose from an entity event: the stoop's climb, fold and head-first dive and its sprawl on
 * the floor after, the rake's drawn-up talons and swept-back glide, the downdraft's raised wings slammed down, and the
 * carry's laboured climb with its talons clenched.
 */
public class TrueVeinwingVultureModel extends EntityModel<VeinwingVultureEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("true_veinwing_vulture"), "main");

	private final ModelPart root, head, tail, tailCord;
	private final ModelPart[] wings = new ModelPart[2], forewings = new ModelPart[2], talons = new ModelPart[2],
			hooks = new ModelPart[2];
	private final ModelPart[][] membranes = new ModelPart[2][3];

	public TrueVeinwingVultureModel(ModelPart bakedRoot) {
		root = bakedRoot.getChild("root");
		head = root.getChild("head");
		tail = root.getChild("tail");
		tailCord = tail.getChild("tail_cord");
		for (int i = 0; i < 2; i++) {
			String side = i == 0 ? "left" : "right";
			wings[i] = root.getChild(side + "_wing");
			forewings[i] = wings[i].getChild(side + "_forewing");
			talons[i] = root.getChild(side + "_talon");
			hooks[i] = talons[i].getChild(side + "_hook");
			for (int m = 0; m < 3; m++) membranes[i][m] = forewings[i].getChild(side + "_membrane_" + m);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
	}

	@Override
	public void setupAnim(VeinwingVultureEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
			float pitch) {
		float partialTick = age - entity.tickCount;
		float turn = Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO);
		float climb = (float) (entity.getY() - entity.yo);
		pose(age, yaw, pitch, turn, climb, entity.ordealMove(), entity.ordealMoveAge(partialTick));
	}

	/** The whole pose; {@code move} is -1 between moves, {@code since} the ticks since its wind-up began. */
	public void pose(float age, float yaw, float pitch, float turn, float climb, int move, float since) {
		root.getAllParts().forEach(ModelPart::resetPose);
		// A beat with weight: a fast downstroke over a third of the cycle, a slow folding recovery over the rest.
		float rate = move == VultureOrdealMoves.STOOP && since < VultureOrdealMoves.STOOP_WINDUP - 6 ? .7F
				: move == VultureOrdealMoves.CARRY && since > VultureOrdealMoves.CARRY_WINDUP ? .32F : .45F;
		float beat = beat(age * rate);
		boolean down = (age * rate / Mth.TWO_PI) % 1.0F < .35F;
		root.y += -1.1F * beat;
		root.zRot += Mth.clamp(turn * .06F, -.55F, .55F);
		root.xRot += Mth.clamp(-climb * 1.4F, -.6F, .6F);
		head.yRot = Mth.clamp(yaw, -60, 60) * Mth.DEG_TO_RAD;
		head.xRot = Mth.clamp(pitch, -40, 40) * Mth.DEG_TO_RAD * .7F;
		for (int i = 0; i < 2; i++) {
			float sign = i == 0 ? 1 : -1;
			wings[i].zRot = sign * (.15F + beat);
			wings[i].yRot = -sign * .18F;
			// The hand of the wing folds in on the way up and opens through the stroke.
			forewings[i].zRot += -sign * (down ? .1F : .55F) - sign * beat * .2F;
			for (int m = 0; m < 3; m++) membranes[i][m].zRot += -sign * (down ? 0.0F : .15F * (m + 1));
			talons[i].xRot = .35F + Mth.cos(age * .18F + i) * .1F;
		}
		tail.xRot = .25F + Mth.sin(age * .22F) * .1F;
		tailCord.xRot += Mth.sin(age * .22F - .7F) * .15F;
		if (move >= 0) move(move, since, age);
	}

	/** The wing's angle through a beat: up at -0.9, down at +0.6. */
	private static float beat(float phase) {
		float w = (phase / Mth.TWO_PI) % 1.0F;
		if (w < 0.0F) w += 1.0F;
		if (w < .35F) return Mth.lerp(HemoEasing.smoothstep(w / .35F), -.9F, .6F);
		return Mth.lerp(HemoEasing.smoothstep((w - .35F) / .65F), .6F, -.9F);
	}

	/** 0 until two ticks before {@code at}, then 1 at {@code at}, accelerating. */
	private static float fall(float since, float at) {
		float t = Mth.clamp((since - (at - 2.0F)) / 2.0F, 0.0F, 1.0F);
		return t * t;
	}

	private void wingsTo(float weight, float z, float y, float fore) {
		for (int i = 0; i < 2; i++) {
			float sign = i == 0 ? 1 : -1;
			wings[i].zRot = Mth.lerp(weight, wings[i].zRot, sign * z);
			wings[i].yRot = Mth.lerp(weight, wings[i].yRot, -sign * y);
			forewings[i].zRot = Mth.lerp(weight, forewings[i].zRot, -sign * fore);
		}
	}

	private void talonsTo(float weight, float x) {
		for (int i = 0; i < 2; i++) {
			talons[i].xRot = Mth.lerp(weight, talons[i].xRot, x);
			hooks[i].xRot = Mth.lerp(weight, hooks[i].xRot, x < -.5F ? -.3F : .6F);
		}
	}

	private void move(int move, float since, float age) {
		switch (move) {
			case VultureOrdealMoves.STOOP -> {
				float w = VultureOrdealMoves.STOOP_WINDUP, dive = VultureOrdealMoves.STOOP_DIVE, down = VultureOrdealMoves.STOOP_DOWN;
				float climbing = 1.0F - HemoEasing.smoothstep(w - 8.0F, w - 5.0F, since);
				float fold = HemoEasing.smoothstep(w - 7.0F, w - 1.0F, since) * (1.0F - HemoEasing.smoothstep(w + dive - 1.0F, w + dive, since));
				float diving = HemoEasing.smoothstep(w - 1.0F, w, since) * (1.0F - HemoEasing.smoothstep(w + dive - 1.0F, w + dive, since));
				float sprawl = HemoEasing.smoothstep(w + dive - 1.0F, w + dive, since)
						* (1.0F - HemoEasing.smoothstep(w + dive + down, w + dive + down + 8.0F, since));
				root.xRot = Mth.lerp(climbing * HemoEasing.smoothstep(0.0F, 4.0F, since), root.xRot, -.45F);
				// Wings folding back tight, head tipping down, then head first.
				wingsTo(fold, -1.1F, 1.0F, .9F);
				root.xRot = Mth.lerp(fold, root.xRot, Mth.lerp(diving, .9F, 1.45F));
				head.xRot = Mth.lerp(fold, head.xRot, .5F);
				talonsTo(diving, -1.3F);
				// Sprawled on the floor where it hit, the wings flung out flat, beating weakly.
				float weak = Mth.sin(age * .5F) * .15F;
				wingsTo(sprawl, .35F + weak, .05F, -.2F);
				root.xRot = Mth.lerp(sprawl, root.xRot, .25F);
				root.y = Mth.lerp(sprawl, root.y, 17.5F);
				head.xRot = Mth.lerp(sprawl, head.xRot, .6F);
				talonsTo(sprawl, .9F);
			}
			case VultureOrdealMoves.RAKE -> {
				float w = VultureOrdealMoves.RAKE_WINDUP, run = VultureOrdealMoves.RAKE_RUN;
				float drawn = HemoEasing.smoothstep(0.0F, w - 3.0F, since) * (1.0F - HemoEasing.smoothstep(w - 1.0F, w, since));
				float gliding = HemoEasing.smoothstep(w - 1.0F, w + 1.0F, since) * (1.0F - HemoEasing.smoothstep(w + run, w + run + 2.0F, since));
				float flare = HemoEasing.smoothstep(w + run, w + run + 2.0F, since) * (1.0F - HemoEasing.smoothstep(w + run + 4.0F, w + run + 12.0F, since));
				root.xRot = Mth.lerp(drawn, root.xRot, -.55F);
				talonsTo(drawn, -1.7F);
				wingsTo(drawn, -.55F + .1F * Mth.sin(since * 1.4F), .2F, .3F);
				// The pass: wings swept back in a glide, body level, talons thrust low ahead.
				wingsTo(gliding, .1F, .75F, .2F);
				root.xRot = Mth.lerp(gliding, root.xRot, .15F);
				talonsTo(gliding, -1.1F);
				head.xRot = Mth.lerp(gliding, head.xRot, .35F);
				// The flare out of it: wings thrown up, body reared.
				wingsTo(flare, -1.1F, -.1F, .5F);
				root.xRot = Mth.lerp(flare, root.xRot, -.6F);
				talonsTo(flare, .2F);
			}
			case VultureOrdealMoves.DOWNDRAFT -> {
				float w = VultureOrdealMoves.DOWNDRAFT_WINDUP;
				float raise = HemoEasing.smoothstep(0.0F, w - 6.0F, since), slam = fall(since, w);
				float held = 1.0F - HemoEasing.smoothstep(w + 6.0F, w + 16.0F, since);
				float tremble = Mth.sin(since * 3.2F) * HemoEasing.smoothstep(w - 8.0F, w - 5.0F, since) * (1.0F - slam);
				wingsTo(raise * held, Mth.lerp(slam, -1.4F + .06F * tremble, 1.05F), Mth.lerp(slam, -.15F, .1F), Mth.lerp(slam, .9F, -.35F));
				root.xRot = Mth.lerp(held, root.xRot, Mth.lerp(slam, -.35F * raise, .35F));
				root.y += held * slam * 1.5F;
				head.xRot = Mth.lerp(held, head.xRot, Mth.lerp(slam, -.3F * raise, .55F));
				talonsTo(raise * held, Mth.lerp(slam, .6F, -.4F));
			}
			case VultureOrdealMoves.CARRY -> {
				float w = VultureOrdealMoves.CARRY_WINDUP, hold = VultureOrdealMoves.CARRY_HOLD;
				float reach = HemoEasing.smoothstep(0.0F, w - 2.0F, since) * (1.0F - HemoEasing.smoothstep(w - 1.0F, w + 1.0F, since));
				float clutch = HemoEasing.smoothstep(w - 1.0F, w + 1.0F, since) * (1.0F - HemoEasing.smoothstep(w + hold, w + hold + 3.0F, since));
				float open = HemoEasing.smoothstep(w + hold, w + hold + 2.0F, since) * (1.0F - HemoEasing.smoothstep(w + hold + 3.0F, w + hold + 10.0F, since));
				root.xRot = Mth.lerp(reach, root.xRot, -.5F);
				talonsTo(reach, -1.6F);
				wingsTo(reach, -.3F, .1F, .2F);
				// Labouring up with the fighter clutched under it: talons down and closed, the body hanging back.
				root.xRot = Mth.lerp(clutch, root.xRot, -.25F);
				talonsTo(clutch, .15F);
				for (int i = 0; i < 2; i++) hooks[i].xRot = Mth.lerp(clutch, hooks[i].xRot, 1.1F);
				talonsTo(open, -.9F);
			}
			default -> {
			}
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
'''


def paint(model, materials, shade_fn, palette, lit=()):
    """The shared painter, then the torn holes made clear (the cutout render leaves them open)."""
    image, glow = _paint(model, materials, shade_fn, palette, lit)
    hole = tuple(bytes.fromhex(palette[HOLE][1:])) + (255,)
    for y in range(image.height):
        for x in range(image.width):
            if image.getpixel((x, y)) == hole:
                image.putpixel((x, y), (0, 0, 0, 0))
    return image, glow


_paint = puppet_rig.paint


def build():
    puppet_rig.ATLAS = 256
    puppet_rig.paint = paint
    try:
        return build_rig('true_veinwing_vulture', 'TrueVeinwingVultureModel', anatomy(), JAVA, shade, PALETTE, glow={GLOW})
    finally:
        puppet_rig.paint = _paint


if __name__ == '__main__':
    build()

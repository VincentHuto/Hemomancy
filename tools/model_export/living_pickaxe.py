"""Authored Living Pickaxe: the Living Staff's ninth form (core-loop Track 8).

A bone haft wrapped in sinew, ending in a clotted-flesh knob, carries a pick head of hematic iron: a socket clamped
round the haft's top and two arms that curve down to points, one a long spike and the other a short chisel. The
iron is dark and lacquered with blood, the edges worn pale where they bite. No copper, brass or silver anywhere: the
blood side's metal is hematic iron, and its other material is bone.

Shares the box-UV painter and preview from unstained_weapons.py, as living_sickle.py does.

    python tools/model_export/living_pickaxe.py            # LivingPickaxeModel.java + model_living_pickaxe.png
    python tools/model_export/living_pickaxe.py --preview  # also build/living_pickaxe_preview.png

It also regenerates the Blockbench project through java_model_to_bbmodel.mjs and embeds the texture in it.
"""
from pathlib import Path
import argparse
import base64
import json
import math
import subprocess

from PIL import Image

import unstained_weapons as uw

ROOT = uw.ROOT
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/item/LivingPickaxeModel.java'
TEXTURE = ROOT / 'src/main/resources/assets/hemomancy/textures/entity/model_living_pickaxe.png'
BBMODEL = ROOT / 'src/main/resources/assets/hemomancy/models/item/bbmodel/LivingPickaxeModel.bbmodel'

# Model space: x along the head, y down the haft (the grip runs down +y from the head), z across the flat.
HEAD_Y = -3.0
# The head's arms follow a circle below the socket so both points hook down toward the hand.
ARC_RADIUS = 11.0
ARC_CENTER = (0.0, HEAD_Y + ARC_RADIUS)


def arc_point(phi):
    a = math.radians(phi)
    return ARC_CENTER[0] + ARC_RADIUS * math.sin(a), ARC_CENTER[1] - ARC_RADIUS * math.cos(a)


def arm(r, side, reach, segments, base_w, tip_w, prefix):
    """One arm of the head, as rotated segments along the arc from the socket out to its point."""
    phis = [side * (6 + (reach - 6) * i / segments) for i in range(segments + 1)]
    for i, (a0, a1) in enumerate(zip(phis, phis[1:])):
        t = i / max(1, segments - 1)
        w = base_w + (tip_w - base_w) * t
        p0, p1 = arc_point(a0), arc_point(a1)
        dx, dy = p1[0] - p0[0], p1[1] - p0[1]
        alpha = math.degrees(math.atan2(dy, dx))
        length = round(math.hypot(dx, dy) + 0.3, 4)
        name = f'{prefix}_{i:02d}'
        r.part(name, 'head', pivot=(round(p0[0], 4), round(p0[1], 4), 0), rotation=(0, 0, round(alpha, 4)))
        depth = round(1.5 - 0.6 * t, 4)
        r.cube(name, (-0.15, round(-w / 2, 4), round(-depth / 2, 4), length, round(w, 4), depth), 'iron')
        if i == segments - 1:
            # The worn edge at the point: pale bone-grey where the iron bites.
            r.cube(name, (length - 0.6, round(-w / 2 - 0.05, 4), round(-depth / 2 - 0.05, 4), 1.2,
                          round(w + 0.1, 4), round(depth + 0.1, 4)), 'edge')


def pickaxe():
    r = uw.Rig('living_pickaxe', 'LivingPickaxeModel', 'living_pickaxe')
    # Haft: bone from the socket down, wrapped in sinew where the hand goes, ending in a flesh knob.
    r.part('haft')
    r.cube('haft', (-0.875, HEAD_Y, -0.875, 1.75, 9.0, 1.75), 'bone')
    r.cube('haft', (-1.0, HEAD_Y + 9.0, -1.0, 2.0, 0.75, 2.0), 'bone_band')
    r.cube('haft', (-1.125, HEAD_Y + 9.75, -1.125, 2.25, 8.0, 2.25), 'sinew')
    r.cube('haft', (-1.0, HEAD_Y + 17.75, -1.0, 2.0, 0.75, 2.0), 'bone_band')
    r.part('pommel')
    r.cube('pommel', (-1.5, HEAD_Y + 18.5, -1.5, 3.0, 1.5, 3.0), 'flesh_knob')
    r.cube('pommel', (-1.0, HEAD_Y + 20.0, -1.0, 2.0, 0.75, 2.0), 'flesh_knob')
    # Head: an iron socket clamped round the haft's top, with a bone wedge driven through it.
    r.part('head')
    r.cube('head', (-1.75, HEAD_Y - 1.5, -1.25, 3.5, 3.5, 2.5), 'iron')
    r.cube('head', (-1.25, HEAD_Y - 2.25, -0.75, 2.5, 0.75, 1.5), 'bone_band')
    r.cube('head', (-1.9, HEAD_Y + 1.5, -1.4, 3.8, 0.75, 2.8), 'iron_band')
    # The long spike and the short chisel.
    arm(r, 1, 42, 5, 2.2, 0.7, 'spike')
    arm(r, -1, 30, 3, 2.4, 1.6, 'chisel')
    return r


# Hematic iron: near-black red iron, lacquered with dried blood; bone; sinew; clotted flesh.
IRON = (44, 14, 18)
IRON_LIGHT = (66, 22, 26)
IRON_SHEEN = (98, 34, 36)
BLOOD = (128, 14, 26)
VEIN = (181, 29, 42)
BONE = (226, 212, 192)
BONE_DARK = (176, 160, 142)
FLESH = (34, 8, 13)
CRIMSON = (111, 10, 24)


def paint(material, t):
    face, u, v, fw, fh, n = t['face'], t['u'], t['v'], t['fw'], t['fh'], t['n']
    flat = face in ('north', 'south')
    if material in ('iron', 'iron_band'):
        rgb = IRON if n < 0.6 else IRON_LIGHT
        if face == 'up':
            rgb = IRON_SHEEN if n > 0.3 else IRON_LIGHT        # the struck top catches the light
        if flat and n > 0.9:
            rgb = BLOOD                                         # lacquered blood in the hammer marks
        if material == 'iron_band' and t['side'] and v % 2 == 0:
            rgb = IRON_SHEEN
        return rgb + (255,), None
    if material == 'edge':
        rgb = BONE_DARK if (u + v) % 2 == 0 else (204, 190, 172)
        if face == 'up':
            rgb = BONE
        return rgb + (255,), None
    if material == 'bone':
        rgb = BONE if n > 0.25 else BONE_DARK
        if t['side'] and v % 4 == 3:
            rgb = BONE_DARK                                     # growth lines in the haft
        return rgb + (255,), None
    if material == 'bone_band':
        rgb = BONE_DARK if n > 0.4 else (150, 134, 118)
        return rgb + (255,), None
    if material == 'sinew':
        k = (u + t['gy']) % 3
        rgb = (70, 20, 24) if k == 0 else (49, 12, 18) if k == 1 else CRIMSON
        if n > 0.95:
            rgb = VEIN
        return rgb + (255,), None
    if material == 'flesh_knob':
        rgb = CRIMSON if n < 0.5 else (88, 10, 20)
        if n > 0.88:
            rgb = VEIN
        if t['side'] and v == 0:
            rgb = FLESH
        return rgb + (255,), None
    raise KeyError(material)


def java_source(rig, size):
    lines = []
    for part in rig.parts:
        if part['parent'] is None:
            continue
        chain = 'CubeListBuilder.create()'
        for cube in part['cubes']:
            u, v = cube['uv']
            chain += f'\n\t\t\t\t.texOffs({u}, {v}).addBox({", ".join(uw.f(c) for c in cube["box"])})'
        rot = part['rotation']
        if any(rot):
            radians = ', '.join(uw.f(math.radians(a)) for a in rot)
            pose = f'PartPose.offsetAndRotation({", ".join(uw.f(p) for p in part["pivot"])}, {radians})'
        elif any(part['pivot']):
            pose = f'PartPose.offset({", ".join(uw.f(p) for p in part["pivot"])})'
        else:
            pose = 'PartPose.ZERO'
        declare = 'PartDefinition ' + part['name'] + ' = ' if any(p['parent'] == part['name'] for p in rig.parts) else ''
        lines.append(f'\t\t{declare}{part["parent"]}.addOrReplaceChild("{part["name"]}", {chain}, {pose});')
    body = '\n'.join(lines)
    return f'''package com.vincenthuto.hemomancy.client.model.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

/**
 * The Living Staff's pickaxe form: a bone haft in sinew under a hematic iron head, a long spike and a short chisel.
 * Generated by tools/model_export/living_pickaxe.py; edit the rig there and re-run it.
 */
public final class LivingPickaxeModel extends Model {{
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("living_pickaxe"), "main");
	private final ModelPart root;

	public LivingPickaxeModel(ModelPart root) {{
		super(RenderType::entityCutoutNoCull);
		this.root = root;
	}}

	public static LayerDefinition createBodyLayer() {{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
{body}
		return LayerDefinition.create(mesh, {size[0]}, {size[1]});
	}}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {{
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}}
}}
'''


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = pickaxe()
    size = uw.layout(rig)
    image, _ = uw.paint_atlas(rig, size, paint)
    image.save(TEXTURE)
    JAVA.write_text(java_source(rig, size), encoding='utf-8')
    print(f'living_pickaxe: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]}')
    if args.preview:
        quads = uw.rig_quads(rig)
        tiles = [uw.render_quads(quads, image, 0.0, 0.0), uw.render_quads(quads, image, 0.7, 0.35),
                 uw.render_quads(quads, image, 2.5, -0.25), uw.render_quads(quads, image, math.pi / 2, 0.0)]
        sheet = Image.new('RGB', (360 * len(tiles), 360), (30, 32, 40))
        for i, tile in enumerate(tiles):
            sheet.paste(tile, (i * 360, 0))
        out = ROOT / 'build/living_pickaxe_preview.png'
        out.parent.mkdir(exist_ok=True)
        sheet.save(out)
        print('preview ->', out)
    write_bbmodel()


def write_bbmodel():
    subprocess.run(['node', 'tools/model_export/java_model_to_bbmodel.mjs', str(JAVA.relative_to(ROOT)),
                    '--texture', 'textures/entity/model_living_pickaxe.png',
                    '--out', str(BBMODEL.parent.relative_to(ROOT))], cwd=ROOT, check=True)
    project = json.loads(BBMODEL.read_text(encoding='utf-8'))
    # Embed the atlas so the project opens textured without the resource tree beside it.
    project['textures'][0]['source'] = 'data:image/png;base64,' + base64.b64encode(TEXTURE.read_bytes()).decode()
    BBMODEL.write_text(json.dumps(project, indent=2) + '\n', encoding='utf-8')


if __name__ == '__main__':
    main()

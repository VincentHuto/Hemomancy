"""Authored Living Sickle: a fist-length living handle and a broad crescent blade, not a short scythe.

Proportioned after a forged European sickle. The handle points straight at the centre of the blade's circle,
and the blade grows out of the ferrule in line with it. A broad heel sweeps out to one side, the blade arcs
over the top, and the point hooks just past the apex, back across the handle's line. It tapers evenly from
heel to point, with a broad honed band along the whole inner curve. The blade is a chain of rotated
segments around that circle.
Shares the box-UV painter and preview from unstained_weapons.py.

    python tools/model_export/living_sickle.py            # LivingSickleModel.java + model_living_sickle.png
    python tools/model_export/living_sickle.py --preview  # also build/living_sickle_preview.png

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
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/item/LivingSickleModel.java'
TEXTURE = ROOT / 'src/main/resources/assets/hemomancy/textures/entity/model_living_sickle.png'
BBMODEL = ROOT / 'src/main/resources/assets/hemomancy/models/item/bbmodel/LivingSickleModel.bbmodel'

# Model space: x toward the heel side, y down; the grip runs down +y from the ferrule.
# The spine circle sits directly above the handle and touches the top of the tang.
RADIUS = 7.0
SPINE_FOOT = 8.0
CENTER = (0.0, SPINE_FOOT - RADIUS)
# phi is measured from the handle's forward axis toward +x: 180 is the heel at the ferrule, 0 the apex.
START, END, SEGMENTS = 180.0, -40.0, 16


def progress(phi):
    return (START - phi) / (START - END)


def width(phi):
    """Radial blade width: a broad heel drawn evenly down to a needle point."""
    return 0.35 + 2.95 * (1 - progress(phi)) ** 0.6


def thickness(phi):
    return 0.8 - 0.3 * progress(phi)


def arc_point(phi, radius=RADIUS):
    a = math.radians(phi)
    return CENTER[0] + radius * math.sin(a), CENTER[1] - radius * math.cos(a)


def segment(r, name, p0, p1, w, edge, th, overlap=0.35):
    """One blade segment from p0 to p1 on the spine, extending w toward the circle's centre."""
    dx, dy = p1[0] - p0[0], p1[1] - p0[1]
    alpha = math.atan2(dy, dx)
    inward = 1 if -math.sin(alpha) * (CENTER[0] - p0[0]) + math.cos(alpha) * (CENTER[1] - p0[1]) > 0 else -1
    length = round(math.hypot(dx, dy) + overlap, 4)
    r.part(name, 'blade', pivot=(round(p0[0], 4), round(p0[1], 4), 0), rotation=(0, 0, round(math.degrees(alpha), 4)))
    body = max(0.25, w - edge + 0.1)
    y0 = 0 if inward > 0 else -body
    r.cube(name, (-0.15, round(y0, 4), round(-th / 2, 4), length, round(body, 4), round(th, 4)), 'blade')
    e0 = w - edge if inward > 0 else -w
    r.cube(name, (-0.15, round(e0, 4), -0.2, length, round(edge, 4), 0.4), 'edge')


def sickle():
    r = uw.Rig('living_sickle', 'LivingSickleModel', 'living_sickle')
    # Handle: a sinew grip that swells toward the hand, necks in, and ends in a mushroom-cap pommel.
    r.part('haft')
    r.cube('haft', (-1.5, 9, -1.5, 3, 1.5, 3), 'bone')                      # ferrule
    r.cube('haft', (-1.125, 10.5, -1.125, 2.25, 4.5, 2.25), 'sinew')
    r.cube('haft', (-1.3, 15, -1.3, 2.6, 3, 2.6), 'sinew')
    r.cube('haft', (-1, 18, -1, 2, 0.75, 2), 'bone')
    r.part('pommel')
    r.cube('pommel', (-1.75, 18.75, -1.75, 3.5, 1.5, 3.5), 'flesh_knob')
    r.cube('pommel', (-1.25, 20.25, -1.25, 2.5, 0.75, 2.5), 'flesh_knob')
    # A narrow tang carries the heel out of the ferrule.
    r.part('blade_root')
    r.cube('blade_root', (-0.6, 7.75, -0.4, 1.2, 1.5, 0.8), 'bolster')

    r.part('blade')
    step = (END - START) / SEGMENTS
    phis = [START + step * i for i in range(SEGMENTS + 1)]
    for i, (a0, a1) in enumerate(zip(phis, phis[1:])):
        mid = (a0 + a1) / 2
        w = width(mid)
        segment(r, f'blade_{i:02d}', arc_point(a0), arc_point(a1), w, max(0.4, 0.38 * w), thickness(mid))
    # The point: a short needle continuing the curve past the last segment, centred on the blade.
    w = width(END)
    p0 = arc_point(END, RADIUS - w / 2 + 0.2)
    p1 = arc_point(END - 9, RADIUS - w / 2 - 0.4)
    alpha = math.degrees(math.atan2(p1[1] - p0[1], p1[0] - p0[0]))
    r.part('blade_tip', 'blade', pivot=(round(p0[0], 4), round(p0[1], 4), 0), rotation=(0, 0, round(alpha, 4)))
    r.cube('blade_tip', (-0.2, -0.25, -0.2, 1.5, 0.5, 0.4), 'edge')
    return r


# Palette carried over from the original sickle: clotted near-black flesh, crimson spine and veins, bone edge.
FLESH = (34, 8, 13)
FLESH_LIGHT = (49, 12, 18)
VEIN = (181, 29, 42)
CRIMSON = (111, 10, 24)
BONE = (226, 212, 192)
BONE_DARK = (176, 160, 142)


def paint(material, t):
    face, u, v, fw, fh, n = t['face'], t['u'], t['v'], t['fw'], t['fh'], t['n']
    flat = face in ('north', 'south')
    if material in ('blade', 'blade_root', 'bolster'):
        rgb = FLESH if n < 0.55 else FLESH_LIGHT
        if n > 0.93:
            rgb = VEIN                                  # red flecks in the clotted blade
        if flat and v == 0:
            rgb = CRIMSON if u % 3 else VEIN            # crimson spine along the outer curve
        elif face == 'down':
            rgb = CRIMSON                               # outer spine face
        elif flat and material != 'bolster' and v == max(1, fh // 2) and u % 2 == 0:
            rgb = (82, 14, 24)                          # vein running down the blade
        if material == 'bolster' and t['side'] and v % 3 == 0:
            rgb = CRIMSON
        return rgb + (255,), None
    if material in ('edge', 'serrated'):
        rgb = BONE if (u + v) % 2 == 0 else (239, 226, 208)
        if material == 'serrated' and flat and v == fh - 1 and u % 2 == 1:
            return (0, 0, 0, 0), None                   # bone teeth near the bolster
        if face == 'up':
            rgb = (246, 236, 220)
        return rgb + (255,), None
    if material == 'bone':
        rgb = BONE if n > 0.2 else BONE_DARK
        if t['side'] and (v == 0 or v == fh - 1):
            rgb = BONE_DARK
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
            rgb = FLESH_LIGHT
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
 * A fist-length living handle and a crescent blade that hooks its point back toward the hand.
 * Generated by tools/model_export/living_sickle.py; edit the rig there and re-run it.
 */
public final class LivingSickleModel extends Model {{
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("living_sickle"), "main");
	private final ModelPart root;

	public LivingSickleModel(ModelPart root) {{
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
    rig = sickle()
    size = uw.layout(rig)
    image, _ = uw.paint_atlas(rig, size, paint)
    image.save(TEXTURE)
    JAVA.write_text(java_source(rig, size), encoding='utf-8')
    print(f'living_sickle: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]}')
    if args.preview:
        quads = uw.rig_quads(rig)
        tiles = [uw.render_quads(quads, image, 0.0, 0.0), uw.render_quads(quads, image, 0.7, 0.35),
                 uw.render_quads(quads, image, 2.5, -0.25), uw.render_quads(quads, image, math.pi / 2, 0.0)]
        sheet = Image.new('RGB', (360 * len(tiles), 360), (30, 32, 40))
        for i, tile in enumerate(tiles):
            sheet.paste(tile, (i * 360, 0))
        out = ROOT / 'build/living_sickle_preview.png'
        out.parent.mkdir(exist_ok=True)
        sheet.save(out)
        print('preview ->', out)
    write_bbmodel()


def write_bbmodel():
    subprocess.run(['node', 'tools/model_export/java_model_to_bbmodel.mjs', str(JAVA.relative_to(ROOT)),
                    '--texture', 'textures/entity/model_living_sickle.png',
                    '--out', str(BBMODEL.parent.relative_to(ROOT))], cwd=ROOT, check=True)
    project = json.loads(BBMODEL.read_text(encoding='utf-8'))
    # Embed the atlas so the project opens textured without the resource tree beside it.
    project['textures'][0]['source'] = 'data:image/png;base64,' + base64.b64encode(TEXTURE.read_bytes()).decode()
    BBMODEL.write_text(json.dumps(project, indent=2) + '\n', encoding='utf-8')


if __name__ == '__main__':
    main()

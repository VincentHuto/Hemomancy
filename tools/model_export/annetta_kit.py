"""Shared rig, box-UV, painting, Java and preview helpers for the Annetta encounter entity models.

Each entity script (annetta_knowles.py, stained_priestess.py, latent_annetta_infection.py) builds a Rig, packs it
with layout(), paints one or more 2x atlases with paint_atlases() and writes a HierarchicalModel with
entity_java_source(). Painters receive the cube dict (material plus any extra keys) and the texel geometry.
"""
from pathlib import Path
import math

from PIL import Image

from unstained_weapons import add, ceil, f, face_rects, h, local_point, mix, render_quads, rot_matrix, verdigris

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
MODEL_DIR = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/annetta'
TEXTURE_DIR = ASSETS / 'textures/entity/boss/annetta'
SCALE = 2           # texture pixels per model unit

__all__ = ['Rig', 'layout', 'paint_atlases', 'entity_java_source', 'preview_sheet', 'camel', 'ROOT', 'ASSETS',
           'MODEL_DIR', 'TEXTURE_DIR', 'SCALE', 'h', 'smooth', 'add', 'mix', 'verdigris', 'clamp', 'flesh', 'vessel',
           'fungus', 'infected_ichor', 'blood', 'bone', 'robe_white', 'f']


class Rig:
    def __init__(self, name, java_class, layer):
        self.name, self.java_class, self.layer = name, java_class, layer
        self.parts = [dict(name='root', parent=None, pivot=(0, 0, 0), rotation=(0, 0, 0), cubes=[])]

    def part(self, name, parent='root', pivot=(0, 0, 0), rotation=(0, 0, 0)):
        if any(p['name'] == name for p in self.parts):
            raise ValueError('duplicate part ' + name)
        self.parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=[]))
        return name

    def cube(self, part, box, material, **extra):
        next(p for p in self.parts if p['name'] == part)['cubes'].append(dict(box=box, material=material, **extra))

    def mirrored(self, part, box, material, **extra):
        """Add a cube on a left_ part and its x-flipped twin on the matching right_ part."""
        self.cube(part, box, material, side=1, **extra)
        x, y, z, w, hgt, d = box
        self.cube(part.replace('left_', 'right_'), (-x - w, y, z, w, hgt, d), material, side=-1, **extra)


def clamp(value, lo=0.0, hi=1.0):
    return max(lo, min(hi, value))


def smooth(x, y, z, scale=3.0, seed=0):
    """Trilinear value noise in [0, 1) with a feature size of `scale` model units."""
    x, y, z = x / scale, y / scale, z / scale
    x0, y0, z0 = math.floor(x), math.floor(y), math.floor(z)
    fx, fy, fz = x - x0, y - y0, z - z0
    fx, fy, fz = (k * k * (3 - 2 * k) for k in (fx, fy, fz))

    def corner(i, j, k):
        return h((x0 + i) * 0.25, (y0 + j) * 0.25, (z0 + k) * 0.25 + seed)

    def lerp(a, b, t):
        return a + (b - a) * t

    return lerp(lerp(lerp(corner(0, 0, 0), corner(1, 0, 0), fx), lerp(corner(0, 1, 0), corner(1, 1, 0), fx), fy),
                lerp(lerp(corner(0, 0, 1), corner(1, 0, 1), fx), lerp(corner(0, 1, 1), corner(1, 1, 1), fx), fy), fz)


# ---------------------------------------------------------------- box UV

def layout(rig, width):
    """Assign texOffs to every cube in rows of the given atlas width; returns (width, height) in model units."""
    u = v = row = 0
    for part in rig.parts:
        for cube in part['cubes']:
            _, _, _, w, hgt, d = cube['box']
            cw, ch = ceil(2 * (w + d)) + 1, ceil(hgt + d) + 1
            if cw > width:
                raise ValueError(f'{rig.name}: cube {cube["box"]} wider than atlas {width}')
            if u + cw > width:
                u, v, row = 0, v + row, 0
            cube['uv'] = (u, v)
            u += cw
            row = max(row, ch)
    height = 32
    while height < v + row:
        height *= 2
    if height > 1024:
        raise ValueError(f'{rig.name} does not fit a {width}x1024 atlas')
    return width, height


def part_world(rig):
    world = {None: (0, 0, 0)}
    for part in rig.parts:
        world[part['name']] = tuple(a + b for a, b in zip(world[part['parent']], part['pivot']))
    return world


def paint_atlases(rig, size, painters):
    """Paint one SCALE-x atlas per painter in a single pass; painter(cube, t) -> RGBA tuple."""
    images = [Image.new('RGBA', (size[0] * SCALE, size[1] * SCALE)) for _ in painters]
    world = part_world(rig)
    for part in rig.parts:
        origin = world[part['name']]
        for cube in part['cubes']:
            u, v = cube['uv']
            _, _, _, w, hgt, d = cube['box']
            for face in ('down', 'up', 'west', 'east', 'north', 'south'):
                rect = face_rects(u, v, w, hgt, d)[face]
                lo_u, hi_u = sorted((rect[0], rect[2]))
                lo_v, hi_v = sorted((rect[1], rect[3]))
                if hi_u - lo_u < 1e-6 or hi_v - lo_v < 1e-6:
                    continue
                pu0, pv0 = math.floor(lo_u * SCALE), math.floor(lo_v * SCALE)
                pu1, pv1 = ceil(hi_u * SCALE), ceil(hi_v * SCALE)
                for py in range(pv0, pv1):
                    for px in range(pu0, pu1):
                        fu = clamp(((px + 0.5) / SCALE - lo_u) / (hi_u - lo_u))
                        fv = clamp(((py + 0.5) / SCALE - lo_v) / (hi_v - lo_v))
                        lx, ly, lz = local_point(face, cube['box'], fu, fv)
                        gx, gy, gz = lx + origin[0], ly + origin[1], lz + origin[2]
                        t = dict(face=face, u=px - pu0, v=py - pv0, fw=pu1 - pu0, fh=pv1 - pv0, fu=fu, fv=fv,
                                 side=face not in ('up', 'down'), lx=lx, ly=ly, lz=lz, gx=gx, gy=gy, gz=gz,
                                 n=h(gx * SCALE, gy * SCALE, gz * SCALE))
                        for image, painter in zip(images, painters):
                            rgba = painter(cube, t)
                            if rgba is not None:
                                image.putpixel((px, py), rgba)
    return images


# ---------------------------------------------------------------- palette

def flesh(t, base=(222, 196, 180)):
    return add(base, (t['n'] - 0.5) * 10)


def robe_white(t):
    rgb = add((232, 230, 224), (t['n'] - 0.5) * 8)
    if t['side'] and t['u'] % 12 == 6:
        rgb = (214, 220, 230)         # faint pale-silver thread down the seams
    elif t['side'] and t['u'] % 12 in (2, 9):
        rgb = add(rgb, -9)            # fold shadow
    return rgb


def blood(t):
    return mix((118, 18, 24), (70, 8, 14), t['n'])


def infected_ichor(t):
    return mix((214, 150, 48), (168, 92, 30), t['n'])


def vessel(t):
    rgb = mix((132, 24, 34), (92, 14, 24), t['n'])
    if (t['u'] + t['v']) % 5 == 0:
        rgb = add(rgb, 22)            # wet highlight running along the vessel
    return rgb


def fungus(t):
    rgb = mix((170, 160, 146), (118, 108, 98), t['n'])
    if t['n'] > 0.9:
        rgb = (208, 196, 170)         # mycelial fleck
    return rgb


def bone(t):
    return add((214, 202, 174), (t['n'] - 0.5) * 16)


# ---------------------------------------------------------------- Java

def camel(name):
    head, *rest = name.split('_')
    return head + ''.join(word.capitalize() for word in rest)


def entity_java_source(rig, size, entity_class, script, armed=False, extra_fields='', extra_ctor='',
                       extra_methods='', extra_imports='',
                       package='com.vincenthuto.hemomancy.client.model.entity.boss.annetta',
                       entity_package='com.vincenthuto.hemomancy.common.entity.boss.annetta',
                       plain_layer_location=False):
    """HierarchicalModel source: every rig part becomes a camelCase field resolved through its parent."""
    lines = []
    for part in rig.parts:
        chain = 'CubeListBuilder.create()'
        for cube in part['cubes']:
            u, v = cube['uv']
            box = ', '.join(f(c) for c in cube['box'])
            chain += f'\n\t\t\t\t.texOffs({u}, {v}).addBox({box}, new CubeDeformation(0.0F))'
        parent = 'mesh' if part['parent'] is None else camel(part['parent'])
        rot = part['rotation']
        pivot = ', '.join(f(p) for p in part['pivot'])
        if any(rot):
            pose = f'PartPose.offsetAndRotation({pivot}, {", ".join(f(math.radians(a)) for a in rot)})'
        elif any(part['pivot']):
            pose = f'PartPose.offset({pivot})'
        else:
            pose = 'PartPose.ZERO'
        has_children = any(p['parent'] == part['name'] for p in rig.parts)
        decl = f'PartDefinition {camel(part["name"])} = ' if has_children else ''
        lines.append(f'\t\t{decl}{parent}.addOrReplaceChild("{part["name"]}", {chain}, {pose});')
    body = '\n\n'.join(lines)
    fields = '\n'.join(f'\tprotected final ModelPart {camel(p["name"])};' for p in rig.parts)
    ctor = '\n'.join(
        f'\t\tthis.{camel(p["name"])} = '
        + ('root.getChild("root")' if p['parent'] is None else f'this.{camel(p["parent"])}.getChild("{p["name"]}")')
        + ';' for p in rig.parts)
    # Unit tests bake some models without a mod bootstrap; those use a plain ResourceLocation for their layer.
    layer_location = (f'ResourceLocation.fromNamespaceAndPath("hemomancy", "{rig.layer}")' if plain_layer_location
                      else f'Hemomancy.rloc("{rig.layer}")')
    if plain_layer_location:
        extra_imports = 'import net.minecraft.resources.ResourceLocation;\n' + extra_imports
    implements = ' implements ArmedModel' if armed else ''
    armed_import = 'import net.minecraft.client.model.ArmedModel;\n' if armed else ''
    return f'''package {package};

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import {entity_package}.{entity_class};
{armed_import}import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
{extra_imports}
/** Generated by tools/model_export/{script}; edit the rig there and re-run it. */
public class {rig.java_class} extends HierarchicalModel<{entity_class}>{implements} {{
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation({layer_location}, "main");

{fields}{extra_fields}

	public {rig.java_class}(ModelPart root) {{
{ctor}{extra_ctor}
	}}

	public static LayerDefinition createBodyLayer() {{
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition mesh = meshdefinition.getRoot();

{body}

		return LayerDefinition.create(meshdefinition, {size[0]}, {size[1]});
	}}

	@Override
	public ModelPart root() {{
		return this.root;
	}}
{extra_methods}}}
'''


# ---------------------------------------------------------------- preview

def transforms(rig, overrides=None):
    import numpy as np
    overrides = overrides or {}
    out = {}
    for part in rig.parts:
        m = np.eye(4)
        rotation = tuple(a + b for a, b in zip(part['rotation'], overrides.get(part['name'], (0, 0, 0))))
        m[:3, :3] = rot_matrix(rotation)
        m[:3, 3] = part['pivot']
        out[part['name']] = out.get(part['parent'], np.eye(4)) @ m
    return out


def quads(rig, hidden=(), overrides=None, scales=None):
    """World-space quads; hidden hides a part and its children; overrides add degrees per part; scales per part."""
    import numpy as np
    parents = {p['name']: p['parent'] for p in rig.parts}
    m_all = transforms(rig, overrides)
    scales = scales or {}

    def skip(name):
        while name is not None:
            if name in hidden:
                return True
            name = parents[name]
        return False

    result = []
    for part in rig.parts:
        if skip(part['name']):
            continue
        m = m_all[part['name']]
        s = np.array(scales.get(part['name'], (1, 1, 1)), float)
        for cube in part['cubes']:
            u, v = cube['uv']
            _, _, _, w, hgt, d = cube['box']
            rects = face_rects(u, v, w, hgt, d)
            for face in ('down', 'up', 'west', 'east', 'north', 'south'):
                corners, uvs = [], []
                for fu, fv in ((0, 0), (1, 0), (1, 1), (0, 1)):
                    corners.append(np.array(local_point(face, cube['box'], fu, fv)) * s)
                    rect = rects[face]
                    uvs.append(((rect[0] + (rect[2] - rect[0]) * fu) * SCALE,
                                (rect[1] + (rect[3] - rect[1]) * fv) * SCALE))
                pts = np.array([list(c) + [1] for c in corners]) @ m.T
                result.append((pts[:, :3], np.array(uvs, float)))
    return result


def preview_sheet(tiles, out, cols=4):
    """tiles: list of (quads, texture, yaw, pitch). Writes a contact sheet PNG."""
    size = 420
    images = [render_quads(q, tex, yaw, pitch, size) for q, tex, yaw, pitch in tiles]
    sheet = Image.new('RGB', (size * cols, size * math.ceil(len(images) / cols)), (30, 32, 40))
    for i, tile in enumerate(images):
        sheet.paste(tile, ((i % cols) * size, (i // cols) * size))
    out = Path(out)
    out.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(out)
    print('preview ->', out)

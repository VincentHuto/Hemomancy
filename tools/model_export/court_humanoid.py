"""The Brined Court's jointed body: one rig every Court role's generator builds on, then dresses.

The hierarchy is the Hemomancy NPC body plan (annetta_knowles.py): root -> hips (hip joints, y 12) -> body (waist,
y 10) -> head and arms; hips -> legs. Arms break at the elbow four pixels below the shoulder pivot (arm -> forearm),
legs at the knee halfway down (leg -> shin). Each lower segment carries an inset filler reaching back into its joint,
so a bent elbow or knee closes over instead of opening a gap. Boxes are the player's sizes (arms four wide), so a
strain attachment authored on the player arm or leg lands on the same pixels here; Track 3 tags each arm and leg
growth UPPER or LOWER and CourtStrainLayer draws the LOWER ones on the forearm or shin.

A role generator calls humanoid(), adds its clothing with dress helpers (thin panels, never inflated shells), and
passes the rig to write(), which emits:
  src/main/java/.../client/model/entity/npc/court/<JavaClass>.java   extends CourtHumanoidModel, + <role>_anim.java
  assets/hemomancy/textures/entity/npc/court/<role>.png              (+ <role>_glow.png when something glows)
  assets/hemomancy/models/entity/bbmodel/court/<JavaClass>.bbmodel
  build/court-npc-preview/<role>.png                                  (--preview)

One texture pixel per model unit: 16 px per block, like the mod's blocks and items. Palettes come from each house's
strain palette row; cloth, cord, bone, lacquer and stone only (no copper, brass or bronze on the Court).
"""
import base64
import hashlib
import json
import math
from pathlib import Path

import annetta_kit
from annetta_kit import ROOT, ASSETS, Rig, add, camel, f, flesh, h, layout, mix, paint_atlases, preview_sheet, quads, smooth

annetta_kit.SCALE = 1           # one texture pixel per model unit: 16 px per block

MODEL_DIR = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc/court'
TEXTURE_DIR = ASSETS / 'textures/entity/npc/court'
BBMODEL_DIR = ASSETS / 'models/entity/bbmodel/court'
PREVIEW_DIR = ROOT / 'build/court-npc-preview'
ATLAS_WIDTH = 128

# The parts every Court humanoid has, in the order CourtHumanoidModel resolves them.
STANDARD = ('root', 'hips', 'body', 'head', 'right_arm', 'right_forearm', 'left_arm', 'left_forearm',
            'right_leg', 'right_shin', 'left_leg', 'left_shin')

# Pivots the strain anchor adapter and the rig check rely on (part-local, as Rig.part takes them).
ELBOW = 4.0                     # below the shoulder pivot
KNEE = 6.0                      # halfway down the twelve-pixel leg
ARM_PIVOT = (5.0, -8.0, 0.0)    # from the waist: the player's shoulder at (5, 2, 0)
FOREARM_PIVOT = (1.0, ELBOW, 0.0)
LEG_PIVOT = (1.9, 0.0, 0.0)     # from the hip joints: the player's hip at (1.9, 12, 0)
SHIN_PIVOT = (0.0, KNEE, 0.0)


# ---------------------------------------------------------------- palettes (from the strain palette rows)

PALETTES = {
    # Sunburst: gold-orange crust, warm glow; undyed linen and saffron over warm sunlit skin.
    'giving_light': dict(skin=(214, 168, 124), hair=((70, 46, 30), (48, 30, 20), (104, 70, 42)),
                         cloth=(222, 206, 168), cloth2=(214, 142, 46), trim=(120, 152, 64), cord=(176, 128, 62),
                         lacquer=(120, 54, 26), eye=(92, 120, 54)),
    # Holdfast: stone grey-brown flecked with coralline pink; kelp-fibre cloth, coralline-dyed wraps, slate and kelp cord.
    'holding_stone': dict(skin=(170, 136, 110), hair=((52, 46, 40), (34, 30, 26), (84, 76, 66)),
                          cloth=(124, 114, 92), cloth2=(184, 104, 112), trim=(96, 98, 102), cord=(98, 78, 48),
                          lacquer=(46, 36, 30), eye=(118, 100, 78)),
    # Mermaid's Purse: cobalt back, silver flank, crimson lateral line and purse leather; woven sea-grass, sinew cord.
    'shared_wake': dict(skin=(184, 192, 204), hair=((30, 40, 72), (18, 24, 46), (54, 70, 112)),
                        cloth=(96, 110, 98), cloth2=(150, 28, 40), trim=(190, 200, 214), cord=(126, 108, 82),
                        lacquer=(24, 34, 70), eye=(40, 70, 170), back=(40, 70, 170), line=(172, 30, 52)),
    # Dead Man's Rope: translucent violet-blue veins; indigo-dyed line cloth, violet wraps, pale fibre line, bone hooks.
    'woven_current': dict(skin=(178, 172, 198), hair=((40, 34, 64), (24, 20, 42), (70, 62, 104)),
                          cloth=(66, 60, 104), cloth2=(118, 104, 172), trim=(206, 200, 226), cord=(158, 136, 102),
                          lacquer=(34, 28, 58), eye=(156, 136, 255), vein=(156, 136, 255), vein_core=(224, 216, 255)),
    # Ink Cap: ink black with bone-white highlights; ink-dyed screens and veils, grey wraps, bone pins.
    'veiled_fold': dict(skin=(206, 200, 192), hair=((26, 24, 28), (14, 12, 16), (52, 48, 54)),
                        cloth=(34, 32, 38), cloth2=(72, 68, 76), trim=(224, 216, 200), cord=(88, 78, 68),
                        lacquer=(16, 14, 18), eye=(206, 200, 188), ink=(12, 10, 16)),
    # Hushbell: frost blue-grey; felted grey wool against the cold, slate-blue folds, pale rime trim, kelp cord.
    'still_bell': dict(skin=(188, 194, 204), hair=((150, 156, 166), (116, 122, 134), (196, 202, 210)),
                       cloth=(150, 162, 176), cloth2=(88, 100, 122), trim=(214, 226, 236), cord=(104, 96, 84),
                       lacquer=(44, 50, 64), eye=(136, 168, 200), frost=(207, 230, 246)),
    # Marrowplume: bone white beaded with blood red; undyed hide, red-dyed wraps, bone fittings, sinew cord.
    'last_marrow': dict(skin=(198, 168, 146), hair=((84, 52, 40), (56, 34, 26), (120, 80, 60)),
                        cloth=(176, 156, 128), cloth2=(146, 38, 40), trim=(232, 222, 200), cord=(124, 94, 70),
                        lacquer=(62, 30, 28), eye=(140, 62, 52), marrow=(216, 52, 60)),
    # Kinbud: pale translucent body, amber to crimson mane; basalt-black lacquer, deep red cord, ash grey.
    'kindling': dict(skin=(226, 208, 200), hair=((236, 222, 214), (206, 186, 178), (248, 240, 234)),
                     cloth=(46, 38, 40), cloth2=(126, 24, 26), trim=(214, 132, 44), cord=(150, 34, 30),
                     lacquer=(28, 22, 24), eye=(232, 150, 52)),
}


# ---------------------------------------------------------------- rig

def humanoid(role, java_class):
    """The bare jointed body, before any clothing."""
    r = Rig(f'court_{role}', java_class, f'court_{role}')
    r.part('hips', 'root', (0, 12, 0))
    r.cube('hips', (-4.0, -3.0, -2.0, 8.0, 3.0, 4.0), 'cloth')
    r.part('body', 'hips', (0, -2, 0))
    r.cube('body', (-4.0, -10.0, -2.0, 8.0, 11.0, 4.0), 'torso')
    r.part('head', 'body', (0, -10, 0))
    r.cube('head', (-4.0, -8.0, -4.0, 8.0, 8.0, 8.0), 'face')

    sx, sy, sz = ARM_PIVOT
    for side, sign in (('right', -1), ('left', 1)):
        arm, fore = f'{side}_arm', f'{side}_forearm'
        r.part(arm, 'body', (sign * sx, sy, sz))
        x0 = -3.0 if sign < 0 else -1.0
        r.cube(arm, (x0, -2.0, -2.0, 4.0, ELBOW + 2.2, 4.0), 'arm', seg=0.0)
        r.part(fore, arm, (sign * FOREARM_PIVOT[0], FOREARM_PIVOT[1], 0))
        r.cube(fore, (-2.0, 0.0, -2.0, 4.0, 12.0 - ELBOW - 2.0, 4.0), 'arm', seg=ELBOW + 2.0)
        r.cube(fore, (-1.9, -1.2, -1.9, 3.8, 1.3, 3.8), 'arm', seg=ELBOW + 2.0, filler=True)
        leg, shin = f'{side}_leg', f'{side}_shin'
        r.part(leg, 'hips', (sign * LEG_PIVOT[0], 0, 0))
        r.cube(leg, (-2.0, 0.0, -2.0, 4.0, KNEE + 0.2, 4.0), 'leg', seg=0.0)
        r.part(shin, leg, SHIN_PIVOT)
        r.cube(shin, (-2.0, 0.0, -2.0, 4.0, 12.0 - KNEE, 4.0), 'leg', seg=KNEE)
        r.cube(shin, (-1.9, -1.2, -1.9, 3.8, 1.3, 3.8), 'leg', seg=KNEE, filler=True)
    return r


def check_rig(r):
    """The rig check: forearm and shin joints at the rig's pivots, fillers in place, standard parts present."""
    parts = {p['name']: p for p in r.parts}
    for name in STANDARD:
        assert name in parts, (r.name, 'missing', name)
    for side, sign in (('right', -1), ('left', 1)):
        assert tuple(parts[f'{side}_arm']['pivot']) == (sign * ARM_PIVOT[0], ARM_PIVOT[1], ARM_PIVOT[2]), r.name
        assert tuple(parts[f'{side}_forearm']['pivot']) == (sign * FOREARM_PIVOT[0], FOREARM_PIVOT[1], 0), r.name
        assert tuple(parts[f'{side}_leg']['pivot']) == (sign * LEG_PIVOT[0], 0, 0), r.name
        assert tuple(parts[f'{side}_shin']['pivot']) == SHIN_PIVOT, r.name
        for lower in (f'{side}_forearm', f'{side}_shin'):
            assert any(c.get('filler') for c in parts[lower]['cubes']), (r.name, lower, 'needs a joint filler')


# ---------------------------------------------------------------- dress helpers (thin panels)

def wrap_skirt(r, length, material='cloth2', front_split=False):
    """A wrapped skirt as hanging panels from the hips: sides plumb, front and back free to swing with the thighs."""
    r.part('skirt', 'hips')
    r.cube('skirt', (-4.3, -0.5, -2.2, 0.4, length, 4.4), material)
    r.cube('skirt', (3.9, -0.5, -2.2, 0.4, length, 4.4), material)
    r.part('skirt_front', 'skirt')
    if front_split:
        r.cube('skirt_front', (-4.1, -0.5, -2.45, 3.9, length, 0.4), material)
        r.cube('skirt_front', (0.2, -0.5, -2.45, 3.9, length - 1.0, 0.4), material)
    else:
        r.cube('skirt_front', (-4.1, -0.5, -2.45, 8.2, length, 0.4), material)
    r.part('skirt_back', 'skirt')
    r.cube('skirt_back', (-4.1, -0.5, 2.05, 8.2, length + 0.5, 0.4), material)


def sash(r, material='trim'):
    r.cube('hips', (-4.3, -3.6, -2.3, 8.6, 1.2, 4.6), material)


def tabard(r, length, material='cloth', back=True):
    """A front (and back) panel over the torso, hanging past the waist."""
    r.part('tabard', 'body', (0, -10, 0))
    r.cube('tabard', (-3.2, 0.2, -2.35, 6.4, length, 0.35), material)
    if back:
        r.cube('tabard', (-3.6, 0.2, 2.0, 7.2, length + 0.5, 0.35), material)


def cuffs(r, material='cord'):
    for side in ('right', 'left'):
        r.cube(f'{side}_forearm', (-2.1, 5.0, -2.1, 4.2, 0.8, 4.2), material)


def anklets(r, material='cord'):
    for side in ('right', 'left'):
        r.cube(f'{side}_shin', (-2.1, 4.4, -2.1, 4.2, 0.7, 4.2), material)


def hair_cap(r, back=4.0):
    """Short hair: a cap over the crown and down the back of the head."""
    r.part('hair', 'head')
    r.cube('hair', (-4.25, -8.3, -4.25, 8.5, 1.6, 8.5), 'hair')
    r.cube('hair', (-4.25, -6.9, 3.6, 8.5, back, 0.7), 'hair')
    r.cube('hair', (-4.3, -6.9, -3.6, 0.6, 2.6, 7.2), 'hair')
    r.cube('hair', (3.7, -6.9, -3.6, 0.6, 2.6, 7.2), 'hair')


# ---------------------------------------------------------------- paint

def weave(rgb, t, step=3):
    """Plain-weave cloth: alternate texels lift, fold shadows down the long faces."""
    if (t['u'] + t['v']) % 2 == 0:
        rgb = add(rgb, 5)
    if t['side'] and t['u'] % 7 == 3:
        rgb = add(rgb, -12)
    return add(rgb, (t['n'] - 0.5) * 10)


def eye_face(t, pal, closed=False):
    """The north face on the 8x8 grid: brows, eyes, nose shadow and mouth. Mirrored per side."""
    skin = flesh(t, pal['skin'])
    if t['face'] != 'north':
        return skin
    px, py = min(7, int(t['lx'] + 4)), min(7, int(t['ly'] + 8))
    side = px if px < 4 else 7 - px
    if py == 3 and side in (1, 2):
        return add(pal['hair'][1], 10)
    if py == 4 and side in (1, 2):
        if closed:
            return add(skin, -40)
        return (238, 232, 224) if side == 1 else pal['eye']
    if py == 5 and px in (3, 4):
        return add(skin, -18)
    if py == 6 and px in (3, 4):
        return mix(skin, (150, 70, 66), 0.5)
    return skin


def network(t, width=0.045, scale=2.6):
    """Slime-mould veins for the Woven Current: the contour lines of a smooth noise field, so they meander and fork
    instead of reading as dots. width is how thick a thread is in noise units."""
    a = abs(smooth(t['gx'], t['gy'], t['gz'], scale, 3) - 0.5) < width
    b = abs(smooth(t['gx'] + 7.0, t['gy'] * 0.7, t['gz'] - 3.0, scale * 1.7, 9) - 0.5) < width * 0.8
    return a or b


def countershade(t, pal, strength=1.0):
    """Open-ocean countershading for the Shared Wake: cobalt down the back, silver on the flank and belly, and a
    crimson lateral line along the side. strength 0..1 is how far the change has gone (less on the Nursery tender)."""
    skin = flesh(t, pal['skin'])
    back = t['gz'] + (0.0 if t['face'] != 'south' else 1.5) - (1.5 if t['face'] == 'north' else 0.0)
    k = max(0.0, min(1.0, (back + 0.5) / 2.5)) * strength
    if t['face'] == 'up':
        k = strength
    rgb = mix(skin, add(pal['back'], (t['n'] - 0.5) * 14), k)
    if strength > 0.5 and (t['u'] + 2 * (t['v'] % 2)) % 4 == 0:
        rgb = add(rgb, 12)            # fine scale, offset row by row
    elif strength > 0.5 and (t['u'] + 2 * (t['v'] % 2)) % 4 == 2:
        rgb = add(rgb, -8)
    if t['face'] in ('east', 'west') and abs(t['lz']) < 0.6 and strength > 0.5 and 2.0 < t['gy'] < 11.0:
        rgb = mix(rgb, pal['line'], 0.75 * strength)
    return rgb


def paint_base(cube, t, pal):
    """The shared materials; returns None for a material the role paints itself."""
    m = cube['material']
    if m == 'face':
        return eye_face(t, pal, cube.get('closed', False))
    if m in ('skin', 'torso', 'arm', 'leg'):
        return flesh(t, pal['skin'])
    if m == 'hair':
        base, dark, light = pal['hair']
        k = (t['u'] + t['v'] // 3) % 5
        return dark if k == 0 else light if k == 3 else base
    if m == 'cloth':
        return weave(pal['cloth'], t)
    if m == 'cloth2':
        rgb = weave(pal['cloth2'], t)
        if t['side'] and t['fv'] > 0.9:
            rgb = add(rgb, -24)       # a dyed hem
        return rgb
    if m == 'trim':
        return pal['trim'] if (t['u'] // 2 + t['v']) % 3 else add(pal['trim'], 28)
    if m == 'cord':
        return pal['cord'] if (t['u'] + t['v']) % 3 else add(pal['cord'], -30)
    if m == 'bone':
        return add((214, 202, 174), (t['n'] - 0.5) * 16)
    if m == 'lacquer':
        rgb = add(pal['lacquer'], (t['n'] - 0.5) * 6)
        if t['side'] and t['fv'] < 0.12:
            rgb = add(rgb, 40)        # the gloss along the top edge
        return rgb
    if m == 'stone':
        return add((88, 84, 82), (t['n'] - 0.5) * 34)
    if m == 'reed':
        k = (t['u'] // 2 + t['v'] // 2) % 2
        return add((196, 164, 98) if k else (170, 136, 74), (t['n'] - 0.5) * 12)
    return None


def finish(rgb):
    return rgb if len(rgb) == 4 else tuple(rgb) + (255,)


# ---------------------------------------------------------------- Java

def java_source(r, size, script, anim, texture, glow_texture, extra_imports=''):
    lines = []
    for part in r.parts:
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
        has_children = any(p['parent'] == part['name'] for p in r.parts)
        decl = f'PartDefinition {camel(part["name"])} = ' if has_children else ''
        lines.append(f'\t\t{decl}{parent}.addOrReplaceChild("{part["name"]}", {chain}, {pose});')
    extra = [p for p in r.parts if p['name'] not in STANDARD]
    fields = ''.join(f'\n\tprotected final ModelPart {camel(p["name"])};' for p in extra)
    ctor = ''.join(f'\n\t\tthis.{camel(p["name"])} = this.{camel(p["parent"])}.getChild("{p["name"]}");' for p in extra)
    glow = f'Hemomancy.rloc("textures/entity/npc/court/{glow_texture}")' if glow_texture else 'null'
    body = '\n\n'.join(lines)
    return f'''package com.vincenthuto.hemomancy.client.model.entity.npc.court;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
{extra_imports}
/** Generated by tools/model_export/{script} on the court_humanoid.py rig; edit the generator and re-run it. */
public class {r.java_class} extends CourtHumanoidModel {{
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("{r.layer}"), "main");
	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/npc/court/{texture}");
	public static final ResourceLocation GLOW_TEXTURE = {glow};
{fields}

	public {r.java_class}(ModelPart mesh) {{
		super(mesh);{ctor}
	}}

	public static LayerDefinition createBodyLayer() {{
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition mesh = meshdefinition.getRoot();

{body}

		return LayerDefinition.create(meshdefinition, {size[0]}, {size[1]});
	}}
{anim}}}
'''


# ---------------------------------------------------------------- Blockbench

def uid(text):
    d = hashlib.md5(text.encode()).hexdigest()
    return f'{d[:8]}-{d[8:12]}-{d[12:16]}-{d[16:20]}-{d[20:32]}'


def bbmodel(r, size, texture_path, out_path):
    elements, groups, world = [], {}, {None: (0, 0, 0)}
    for part in r.parts:
        absolute = tuple(a + b for a, b in zip(world[part['parent']], part['pivot']))
        world[part['name']] = absolute
        rot = part['rotation']
        group = dict(name=part['name'], origin=[absolute[0], 24 - absolute[1], absolute[2]],
                     rotation=[-rot[0], rot[1], -rot[2]], uuid=uid(r.name + '/' + part['name']), children=[],
                     export=True, isOpen=False, visibility=True)
        groups[part['name']] = group
        if part['parent'] is not None:
            groups[part['parent']]['children'].append(group)
        for i, cube in enumerate(part['cubes']):
            x, y, z, w, hgt, d = cube['box']
            eid = uid(f'{r.name}/{part["name"]}/{i}')
            group['children'].append(eid)
            elements.append(dict(name=f'{part["name"]}_{i}', uuid=eid, type='cube', box_uv=True,
                                 **{'from': [absolute[0] + x, 24 - (absolute[1] + y + hgt), absolute[2] + z]},
                                 to=[absolute[0] + x + w, 24 - (absolute[1] + y), absolute[2] + z + d],
                                 origin=group['origin'], uv_offset=list(cube['uv'])))
    source = 'data:image/png;base64,' + base64.b64encode(Path(texture_path).read_bytes()).decode()
    bb = dict(meta=dict(format_version='4.10', model_format='modded_entity', box_uv=True), name=r.java_class,
              model_identifier=r.layer, resolution=dict(width=size[0], height=size[1]), elements=elements,
              outliner=[groups['root']],
              textures=[dict(name=Path(texture_path).name, uuid=uid(r.name + '/texture'), id='0', source=source,
                             mode='bitmap', render_mode='default', visible=True)], animations=[])
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text(json.dumps(bb, indent=1) + '\n', encoding='utf-8')


# ---------------------------------------------------------------- write

def write(r, role, script, painter, glow_painter=None, poses=(), preview=False, base_rig=True):
    """Pack, paint and write the role's model, textures, Blockbench project and (with preview) a contact sheet.

    painter(cube, t) returns an RGB(A) tuple or None for an empty texel; glow_painter likewise for the emissive layer.
    poses: [(label, overrides, hidden)] for the preview, overrides in degrees per part.
    """
    if base_rig:
        check_rig(r)
    size = layout(r, ATLAS_WIDTH)
    painters = [lambda c, t: (lambda rgb: None if rgb is None else finish(rgb))(painter(c, t))]
    if glow_painter:
        painters.append(lambda c, t: (lambda rgb: None if rgb is None else finish(rgb))(glow_painter(c, t)))
    images = paint_atlases(r, size, painters)
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    texture = f'{role}.png'
    images[0].save(TEXTURE_DIR / texture)
    glow_name = None
    glow_path = TEXTURE_DIR / f'{role}_glow.png'
    if glow_painter and images[1].getbbox() is not None:
        glow_name = glow_path.name
        images[1].save(glow_path)
    elif glow_path.exists():
        glow_path.unlink()
    anim = Path(__file__).with_name(f'court_{role}_anim.java').read_text(encoding='utf-8')
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    if base_rig:
        source = java_source(r, size, script, anim, texture, glow_name)
    else:
        source = new_body_java_source(r, size, script, anim, texture, glow_name)
    (MODEL_DIR / f'{r.java_class}.java').write_text(source, encoding='utf-8')
    bbmodel(r, size, TEXTURE_DIR / texture, BBMODEL_DIR / f'{r.java_class}.bbmodel')
    print(f'{role}: {sum(len(p["cubes"]) for p in r.parts)} cubes, atlas {size[0]}x{size[1]}'
          + (', glow' if glow_name else ''))
    if preview:
        tiles = []
        for label, overrides, hidden in poses or [('rest', {}, ())]:
            q = quads(r, hidden=hidden, overrides=overrides)
            tiles += [(q, images[0], 0.0, 0.1), (q, images[0], math.pi * 0.75, 0.15)]
        preview_sheet(tiles, PREVIEW_DIR / f'{role}.png')


def new_body_java_source(r, size, script, anim, texture, glow_texture):
    """A Court body that is not the humanoid rig (the Kindled elder): every part is a field, resolved by parent."""
    lines = []
    for part in r.parts:
        chain = 'CubeListBuilder.create()'
        for cube in part['cubes']:
            u, v = cube['uv']
            chain += f'\n\t\t\t\t.texOffs({u}, {v}).addBox({", ".join(f(c) for c in cube["box"])}, new CubeDeformation(0.0F))'
        parent = 'mesh' if part['parent'] is None else camel(part['parent'])
        rot = part['rotation']
        pivot = ', '.join(f(p) for p in part['pivot'])
        if any(rot):
            pose = f'PartPose.offsetAndRotation({pivot}, {", ".join(f(math.radians(a)) for a in rot)})'
        elif any(part['pivot']):
            pose = f'PartPose.offset({pivot})'
        else:
            pose = 'PartPose.ZERO'
        has_children = any(p['parent'] == part['name'] for p in r.parts)
        decl = f'PartDefinition {camel(part["name"])} = ' if has_children else ''
        lines.append(f'\t\t{decl}{parent}.addOrReplaceChild("{part["name"]}", {chain}, {pose});')
    fields = '\n'.join(f'\tprotected final ModelPart {camel(p["name"])};' for p in r.parts)
    ctor = '\n'.join(f'\t\tthis.{camel(p["name"])} = '
                     + ('mesh.getChild("root")' if p['parent'] is None else f'this.{camel(p["parent"])}.getChild("{p["name"]}")')
                     + ';' for p in r.parts)
    glow = f'Hemomancy.rloc("textures/entity/npc/court/{glow_texture}")' if glow_texture else 'null'
    body = '\n\n'.join(lines)
    return f'''package com.vincenthuto.hemomancy.client.model.entity.npc.court;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Generated by tools/model_export/{script} on the court_humanoid.py joint conventions; edit the generator and re-run it. */
public class {r.java_class} extends CourtMemberModel {{
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("{r.layer}"), "main");
	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/npc/court/{texture}");
	public static final ResourceLocation GLOW_TEXTURE = {glow};

{fields}

	public {r.java_class}(ModelPart mesh) {{
{ctor}
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
{anim}}}
'''

"""Bake Saint Hemorath's Blockbench project and combat layers into runtime assets."""

import base64
from copy import deepcopy
import io
import json
from math import asin, atan2, degrees, dist

from PIL import Image
from export_choir_keeper_java import ASSETS, ROOT, export_mesh, resolve_outliner

PROJECT = ASSETS / "models/entity/bbmodel/bosses/HemorathModel.bbmodel"
TEXTURES = ASSETS / "textures/entity/boss/hemorath"
PACKAGE = "com.vincenthuto.hemomancy.client.model.entity.boss.hemorath"
JAVA = ROOT / "src/main/java" / PACKAGE.replace(".", "/") / "HemorathMesh.java"
# The authoring rig is two units to one Java unit. The renderer still uses scale 2.
SCALE = 0.5


def prepare(project):
    resolve_outliner(project)
    cubes = {cube["uuid"]: cube for cube in project["elements"]}
    root, = project["outliner"]
    authored = {}

    def index(node):
        authored[node["name"]] = node
        for child in node["children"]:
            if isinstance(child, dict):
                index(child)

    index(root)

    def group(parent, name, origin, children=()):
        node = dict(name=name, origin=origin, children=list(children))
        parent["children"].append(node)
        return node

    def move(parent, name):
        key = next(k for k in parent["children"] if isinstance(k, str) and cubes[k]["name"] == name)
        parent["children"].remove(key)
        return key

    def beam(parent, name, source, a, b, width, depth):
        length = dist(a, b)
        dx, dy, dz = (v - u for u, v in zip(a, b))
        x, y, z = a
        cube = dict(name=name, uuid=name, origin=a, rotation=(degrees(asin(dz / length)), 0, degrees(atan2(-dx, dy))),
                    faces=deepcopy(source["faces"]))
        cube["from"] = (x - width / 2, y - .12, z - depth / 2)
        cube["to"] = (x + width / 2, y + length + .12, z + depth / 2)
        parent["children"].append(name)
        cubes[name] = cube
        project["elements"].append(cube)

    bindings = {
        "Saint Hemorath": "root", "Pelvis": "hips", "Thorax": "chest", "Bowed skull": "head",
        "Facial cords": "face_cords", "Open ribcage": "ribcage", "Vertebral column": "spine",
        "Ferric halo": "halo", "Leaking marrow vessel": "vessel_cords", "Broken chain of debt": "claw_chain",
        "Iron tithe staff": "staff", "Staff reliquary": "staff_crown", "Staff chain, outer": "staff_chain_outer",
        "Staff chain, inner": "staff_chain_inner", "Burial vestments": "vestments"
    }
    for label, side in (("Staff", "left"), ("Raking", "right")):
        bindings.update({f"{label} upper arm": f"{side}_arm", f"{label} forearm": f"{side}_forearm",
                         f"{label} hand": f"{side}_hand"})
        for i in range(4):
            bindings[f"{label} finger {i + 1}"] = f"{side}_finger_{i}"
    for label in ("Left", "Right"):
        side = label.lower()
        for suffix, runtime in (("thigh", "leg"), ("shin", "shin"), ("foot", "foot"),
                                ("shoulder remnant", "shroud")):
            bindings[f"{label} {suffix}"] = f"{side}_{runtime}"
        for i in range(6):
            bindings[f"{label} rib {i + 1}"] = f"{side}_rib_{i}"
    for i in range(10):
        bindings[f"Vestment panel {i + 1:02}"] = f"robe_strip_{i}"
    assert bindings.keys() == authored.keys()
    for name, runtime in bindings.items():
        authored[name]["name"] = runtime

    vessel = authored["Leaking marrow vessel"]
    cage = authored["Open ribcage"]
    source = next(cubes[k] for k in vessel["children"] if cubes[k]["name"] == "Clotted knot")
    lobes = group(cage, "vessel_lobes", (0, 37, -1.9))
    mass = group(cage, "vessel_mass", (0, 37, -2.8))
    for i, (x, y) in enumerate(((-1.2, 35.6), (1.05, 36.7), (-.85, 38.0), (.90, 39.1))):
        beam(lobes, f"lobe_{i}", source, (x, y, -1.55), (x * .72, y + 2.2, -2.15), 1.35, 1.15)
    beam(mass, "swollen_core", source, (-.28, 34.3, -2.3), (.28, 40.2, -2.85), 3.65, 2.6)
    beam(mass, "swollen_aorta", source, (.35, 39.5, -2.65), (-.85, 42.0, -.55), 1.7, 1.55)
    for label, side, sign in (("Staff", "left", -1), ("Raking", "right", 1)):
        host = authored[f"{label} upper arm"]
        arm_vessel = group(host, side + "_arm_vessel", host["origin"])
        beam(arm_vessel, side + "_sinew", source, (sign * 5.85, 43.7, -2.65),
             (sign * 8.35, 37.65, -3.1 if sign > 0 else -1.8), .74, .67)

    for side in ("left", "right"):
        hand = authored["Staff hand" if side == "left" else "Raking hand"]
        thumb = [move(hand, f"Opposed thumb {i:02}") for i in (1, 2)]
        group(hand, side + "_thumb", cubes[thumb[0]]["origin"], thumb)
    for i in range(4):
        finger = authored[f"Raking finger {i + 1}"]
        tip = [move(finger, name) for name in ("Phalanx 02", "Iron hook")]
        group(finger, f"right_finger_tip_{i}", cubes[tip[0]]["origin"], tip)

    staff = authored["Iron tithe staff"]
    hand = authored["Staff hand"]
    fingers = [child for child in hand["children"] if child is not staff]
    hand["children"] = [staff]
    group(hand, "staff_grip", staff["origin"], fingers)
    group(staff, "staff_foot", (staff["origin"][0], 0, staff["origin"][2]))
    crown = authored["Staff reliquary"]
    for label in ("outer", "inner"):
        chain = authored[f"Staff chain, {label}"]
        for index, cube_id in enumerate(chain["children"][:]):
            group(chain, f"{chain['name']}_link_{index}", cubes[cube_id]["origin"], (cube_id,))
        chain["children"] = [child for child in chain["children"] if isinstance(child, dict)]
        pendant = move(crown, label.capitalize() + " iron seal")
        group(chain, f"{chain['name']}_pendant", cubes[pendant]["origin"], (pendant,))

    for i in range(10):
        robe = authored[f"Vestment panel {i + 1:02}"]
        hem = min(cubes[key]["from"][1] for key in robe["children"])
        x, _, z = robe["origin"]
        group(robe, "robe_hem_" + str(i), (x, hem, z))

    claw = authored["Broken chain of debt"]
    claw["children"].append(move(authored["Raking forearm"], "Splintered chain link"))

    cords = authored["Facial cords"]
    for name, select in (("face_cords_left", range(1, 4)), ("face_cords_right", range(4, 7))):
        strands = [move(cords, f"Face strand {i} {j:02}") for i in select for j in (1, 2)]
        group(cords, name, cubes[strands[0]]["origin"], strands)

    spine = authored["Vertebral column"]
    bottom = move(spine, "Spine 01")
    cube = cubes[bottom]
    lumbar = group(spine, "lumbar", cube["origin"], (bottom,))
    lumbar["rotation"], cube["rotation"] = cube["rotation"], (0, 0, 0)
    group(authored["Pelvis"], "sacrum_joint", lumbar["origin"])
    group(spine, "lumbar_top", cubes[next(k for k in spine["children"]
                                           if isinstance(k, str) and cubes[k]["name"] == "Spine 02")]["origin"])

    # Extra ferric points emerge over the silhouette's fixed iron deposits.
    spark_source = cubes[next(k for k in authored["Raking forearm"]["children"]
                             if isinstance(k, str) and cubes[k]["name"] == "Iron tip")]
    sites = (
        ("Raking forearm", (9.18, 33.7, -6.45), (11.10, 35.0, -7.5)),
        ("Raking upper arm", (7.23, 41.0, -2.68), (8.55, 42.5, -3.2)),
        ("Left rib 3", (4.95, 39.10, -1.25), (6.65, 40.46, -2.3)),
        ("Right rib 6", (-3.9, 33.55, -1.25), (-5.65, 34.5, -2.0)),
        ("Staff forearm", (-10.55, 37.05, -1.95), (-12.25, 38.35, -1.15)),
        ("Staff upper arm", (-6.92, 41.2, -1.45), (-8.25, 42.65, -.8)),
        ("Right shin", (-3, 7.75, 1.05), (-4.4, 9.05, .72)),
        ("Left shin", (3.08, 8.65, .15), (4.75, 10.15, -.05)),
        ("Bowed skull", (1.87, 52.15, -4.5), (3.22, 54.0, -4.43)),
    )
    for index, (name, a, b) in enumerate(sites):
        patch = group(authored[name], f"spike_patch_{index}", a)
        beam(patch, f"spike_{index}", spark_source, a, b, .65, .75)
        c = (a[0] * .99, a[1] - .32, a[2] + .3)
        d = tuple((u + v) / 2 for u, v in zip(c, b))
        beam(patch, f"spike_splinter_{index}", spark_source, c, d, .39, .51)

    def scale_node(node):
        node["origin"] = tuple(v * SCALE for v in node["origin"])
        for child in node["children"]:
            if isinstance(child, dict):
                scale_node(child)

    # The sacrum joint is in the hips' frame, so transform the author's thorax-space seat first.
    import math
    x, y, z = map(math.radians, authored["Thorax"]["rotation"])
    ox, oy, oz = authored["Thorax"]["origin"]
    ax, ay, az = lumbar["origin"]
    dx, dy, dz = ax - ox, ay - oy, az - oz
    assert y == 0
    uy, uz = math.cos(x) * dy - math.sin(x) * dz, math.sin(x) * dy + math.cos(x) * dz
    authored["Pelvis"]["children"][-1]["origin"] = (
        ox + math.cos(z) * dx - math.sin(z) * uy,
        oy + math.sin(z) * dx + math.cos(z) * uy, oz + uz)

    scale_node(root)
    for cube in project["elements"]:
        for key in ("origin", "from", "to"):
            cube[key] = tuple(v * SCALE for v in cube[key])
    return source, spark_source


def main():
    project = json.loads(PROJECT.read_text(encoding="utf-8"))
    png = base64.b64decode(project["textures"][0]["source"].split(",", 1)[1], validate=True)
    source, tip = prepare(project)
    java = export_mesh(project, package=PACKAGE, mesh_name="HemorathMesh", split_parts=True,
                       source="tools/model_export/hemorath.py from HemorathModel.bbmodel")
    # The detached ring uses the same baked geometry at the flight path's origin.
    java = java.replace("    private static ModelPart build_halo()", "    static ModelPart build_halo()")
    JAVA.write_text(java, encoding="utf-8")
    (TEXTURES / "hemorath.png").write_bytes(png)
    atlas = Image.open(io.BytesIO(png)).convert("RGBA")
    mask = Image.new("RGBA", atlas.size)
    for cube in (source, tip):
        for face in cube["faces"].values():
            x0, y0, x1, y1 = face["uv"]
            for y in range(y0, y1):
                for x in range(x0, x1):
                    if atlas.getpixel((x, y))[3] and (x == (x0 + x1) // 2 or y == y0):
                        mask.putpixel((x, y), (255, 210, 196, 255))
    mask.save(TEXTURES / "hemorath_overload.png", optimize=True)


if __name__ == "__main__":
    main()

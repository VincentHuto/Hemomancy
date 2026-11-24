"""Export the Ash Hunter's editable Excoriated project into native Java model parts."""
from base64 import b64decode
import json
from math import asin, atan2, cos, degrees, radians, sin

from export_choir_keeper_java import ASSETS, ROOT, export_mesh

PROJECT = ASSETS / "models/entity/bbmodel/excoriated.bbmodel"
TEXTURE = ASSETS / "textures/entity/excoriated/ash_hunter.png"
JAVA = ROOT / "src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/monster/ExcoriatedMesh.java"
SCALE = 2.125
BOW = (6.75, 21.5, -12)
NOCK = (2.5, 21.25, -7.25)

def native_rotation(angles):
    # Blockbench applies XYZ. FaceUvCube applies ZYX after reflecting Y.
    x, y, z = map(radians, angles)
    a, b, c, d, e, f = cos(x), sin(x), cos(y), sin(y), cos(z), sin(z)
    return [degrees(atan2(b*e + a*d*f, a*c)),
            degrees(asin(max(-1, min(1, a*d*e - b*f)))),
            degrees(atan2(a*f + b*d*e, c*e))]

def prepare(project):
    root, = project["outliner"]
    body, torso = root["children"]
    root["name"] = "root"
    root["children"] = [body]
    body["children"].append(torso)
    body["name"], torso["name"] = "horse_body", "human_torso"
    parts = {cube["uuid"]: cube for cube in project["elements"]}

    def named(parent, name):
        return next(node for node in parent["children"] if isinstance(node, dict) and node["name"] == name)

    def add(parent, name, origin, children=()):
        node = {"name": name, "origin": list(origin), "children": list(children)}
        parent["children"].append(node)
        return node

    def move(parent, select):
        ids = [node for node in parent["children"] if isinstance(node, str) and select(parts[node]["name"])]
        parent["children"] = [node for node in parent["children"] if node not in ids]
        return ids

    def segment(parent, name, cube_id):
        # A unit-length part lets the animation place both segment endpoints exactly.
        cube = parts[cube_id]
        widths = [(.5 * (b - a)) for a, b in zip(cube["from"], cube["to"])]
        widths[1] = .5 / SCALE
        cube["origin"], cube["rotation"] = [0, 0, 0], [0, 0, 0]
        cube["from"], cube["to"] = [-w for w in widths], widths
        add(parent, name, (0, 0, 0), [cube_id])

    for label, key, knee, hoof in (
        ("Left Foreleg", "front_left", (2.5, 9, -4.75), (3.25, 1.5, -5.5)),
        ("Right Foreleg", "front_right", (-2.25, 8.5, -5.75), (-2.5, 1.25, -8)),
        ("Left Hindleg", "hind_left", (2.5, 9.25, 5), (3.25, 1.5, 7.75)),
        ("Right Hindleg", "hind_right", (-2, 9.5, 4.75), (-2.5, 1.25, 5.75)),
    ):
        leg = named(body, label)
        leg["name"] = key
        lower = add(leg, key + "_lower", knee,
                    move(leg, lambda name: name not in ("Drawn muscle", "Exposed tendon")))
        add(lower, key + "_hoof", hoof, move(lower, lambda name: name in ("Fetlock", "Split basalt hoof")))

    head = named(body, "Equine Skull")
    head["name"] = "horse_head"
    named(body, "Tail")["name"] = "tail"
    named(torso, "Hunter Head")["name"] = "human_head"
    add(body, "vessels", body["origin"], move(body, lambda name: name == "Taut belly cord"))
    tackle = named(body, "Basin Tether and Spare Harpoons")
    tackle["name"] = "tackle"
    line = add(body, "return_line", body["origin"])
    for index, cube in enumerate(move(tackle, lambda name: name.startswith("Return line from the harpoon"))):
        segment(line, "line_" + str(index), cube)

    left = named(torso, "Left Arm")
    left["name"] = "left_arm"
    bow = named(left, "Left Forearm Bow")
    bow["name"] = "fused_bow"
    left["children"].remove(bow)
    forearm = add(left, "left_forearm", (5.25, 20.75, -6.5),
                  move(left, lambda name: name in ("Ulna fused into stave", "Red flexor")))
    forearm["children"].append(bow)
    string = named(bow, "Bowstring, Drawn")
    string["name"] = "bowstring"
    upper, lower = move(string, lambda name: name.endswith("tendon string"))
    segment(string, "upper_string", upper)
    segment(string, "lower_string", lower)
    add(bow, "upper_limb", BOW, move(bow, lambda name: name.startswith("Upper ")))
    add(bow, "lower_limb", BOW, move(bow, lambda name: name.startswith("Lower ")))

    right = named(torso, "Right Arm")
    right["name"] = "right_arm"
    harpoon = named(right, "Nocked Hematic Iron Harpoon")
    harpoon["name"], harpoon["origin"] = "harpoon", list(NOCK)
    right["children"].remove(harpoon)
    bow["children"].append(harpoon)
    forearm = add(right, "right_forearm", (-3.5, 19.5, -5.75),
                  move(right, lambda name: name != "Right humerus"))
    add(forearm, "right_hand", NOCK,
        move(forearm, lambda name: name == "Draw hand" or name.startswith("Drawn finger")))

    def scale_group(group):
        group["origin"] = [v*SCALE for v in group["origin"]]
        for child in group["children"]:
            if isinstance(child, dict):
                scale_group(child)

    scale_group(root)
    for cube in parts.values():
        for key in ("from", "to", "origin"):
            cube[key] = [v*SCALE for v in cube[key]]
        cube["rotation"] = native_rotation(cube["rotation"])

def main():
    project = json.loads(PROJECT.read_text(encoding="utf-8"))
    texture, = project["textures"]
    png = b64decode(texture["source"].split(",", 1)[1], validate=True)
    prepare(project)
    java = export_mesh(project, package="com.vincenthuto.hemomancy.client.model.entity.mob.monster",
                       mesh_name="ExcoriatedMesh",
                       source="tools/model_export/export_excoriated_java.py from excoriated.bbmodel")
    # The Recall Barb renderer draws the same harpoon with its own projectile transform.
    start = java.index("        ModelPart harpoon = part(")
    end = java.index("        ModelPart fused_bow", start)
    harpoon = java[start:end]
    java = (java[:start] + "        ModelPart harpoon = createHarpoon();\n\n" + java[end:])
    anchor = java.index("    private static ModelPart part(")
    java = (java[:anchor] + "    static ModelPart createHarpoon() {\n" + harpoon
            + "        return harpoon;\n    }\n\n" + java[anchor:])
    JAVA.write_text(java, encoding="utf-8")
    TEXTURE.write_bytes(png)

if __name__ == "__main__":
    main()


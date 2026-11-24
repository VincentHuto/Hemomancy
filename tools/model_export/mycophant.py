"""Bake the Spawn Mother's Blockbench project into the Minecraft rig and atlases."""
from base64 import b64decode
from io import BytesIO
import json
import re

from PIL import Image

from export_choir_keeper_java import ASSETS, ROOT, export_mesh, resolve_outliner

ART = ROOT / "docs/art/mycophant_spawn_mother/MycophantSpawnMother.bbmodel"
PACKAGE = "com.vincenthuto.hemomancy.client.model.entity.boss.endgame"
JAVA = ROOT / "src/main/java" / PACKAGE.replace(".", "/") / "MycophantMesh.java"
TEXTURES = ASSETS / "textures/entity/boss/endgame"


def child(parent, name):
    return next(node for node in parent["children"] if isinstance(node, dict) and node["name"] == name)


def take(parent, name):
    node = child(parent, name)
    parent["children"].remove(node)
    return node


def bone(name, origin, children=()):
    return {"name": name, "origin": origin, "children": list(children)}


def bind(node, name):
    node["name"] = name
    return node


def chain(parent, source, prefix, count, tail=()):
    """Nest the beam pivots while preserving their authored absolute rest positions."""
    beams = [take(parent, f"{source} {i + 1}") for i in range(count)]
    nodes = [bone(f"{prefix}_{i}", beam["origin"], [beam]) for i, beam in enumerate(beams)]
    parent["children"].append(nodes[0])
    for joint, link in zip(nodes, nodes[1:]):
        joint["children"].append(link)
    nodes[-1]["children"].extend(tail)


def make_rig(project, cubes):
    root, = project["outliner"]
    bind(root, "root")
    body = bind(child(root, "Belly and thorax"), "body")
    thorax = bone("thorax", (0, 32, -7))
    body["children"].append(thorax)
    upper_cubes = {"Scapular rise", "Breastplate", "Shoulder saddle", "Nape"}
    for key in body["children"][:]:
        if isinstance(key, str) and cubes[key]["name"] in upper_cubes:
            body["children"].remove(key)
            thorax["children"].append(key)

    abdomen = bind(child(body, "Abdomen and brood vents"), "abdomen")
    vent = bind(child(abdomen, "Rear petal vent"), "rear_vent")
    for i in range(5):
        petal = take(vent, f"Petal {i + 1}")
        vent["children"].append(bone(f"vent_{i}", petal["origin"], [petal]))
    bind(child(body, "Great gestation sac"), "sac")

    for label, side in (("L", "l"), ("R", "r")):
        chain(abdomen, f"{label} trailing root", f"tail_{side}", 3)
        for position in ("Fore", "Hind"):
            claw = bind(child(body, f"{position} hauling claw {label}"), f"claw_{position.lower()}_{side}")
            fore = take(claw, "Hauling forelimb")
            pincer = bind(take(claw, "Pincer"), f"pincer_{position.lower()}_{side}")
            bind(child(pincer, "Moving jaw"), f"jaw_{position.lower()}_{side}")
            elbow = next(key for key in claw["children"] if isinstance(key, str)
                         and cubes[key]["name"] == "Elbow carapace")
            claw["children"].remove(elbow)
            claw["children"].append(bone(f"elbow_{position.lower()}_{side}", fore["origin"], [fore, elbow, pincer]))

    hand = take(body, "Grasping arm L")
    feeding = take(body, "Feeding arm R")
    for arm, key, source, end in ((hand, "hand", "Grasping arm", "Palm"),
                                  (feeding, "feeding", "Siphon arm", "Feeding siphon")):
        bind(arm, key)
        wrist = bind(take(arm, end), key + "_wrist")
        fore = take(arm, source + " 2")
        stub = take(arm, source + " 3")
        arm["children"].append(bone(key + "_elbow", fore["origin"], [fore, stub, wrist]))
        thorax["children"].append(arm)
        if key == "hand":
            for i in range(3):
                finger = bind(child(wrist, f"Grasping finger {i + 1}"), f"finger_{i}")
                chain(finger, "Phalanx", f"finger_{i}_link", 3)
            chain(wrist, "Opposed thumb", "thumb", 3)
            wrist["children"].append(bone("brood_socket", (-43, 30, -51)))
        else:
            mouth = [key for key in wrist["children"] if isinstance(key, str)]
            for key in mouth:
                wrist["children"].remove(key)
            socket = bone("siphon_mouth", (19, 35, -71))
            chain(wrist, "Feeding tube", "siphon", 6, [*mouth, socket])

    neck = bind(take(body, "Neck"), "neck")
    thorax["children"].append(neck)
    head = bind(child(neck, "Head"), "head")
    collar = bind(child(head, "Frilled collar"), "collar")
    for label, side in (("L", "l"), ("R", "r")):
        for i in range(8):
            lobe = take(collar, f"{label} frill lobe {i + 1}")
            collar["children"].append(bone(f"frill_{side}_{i}", lobe["origin"], [lobe]))
        for i in range(4):
            feeler = bind(child(head, f"{label} facial feeler {i + 1}"), f"feeler_{side}_{i}")
            chain(feeler, "Tendril", f"feeler_{side}_{i}_link", 3)

    # Use the closed skull's cubes through the split. The Joining copies show
    # the target orientation in Blockbench, so the runtime needs only one set.
    claim = bind(child(head, "Claim | closed skull"), "skull")
    pulp = bind(child(head, "Joining | split skull and pulp"), "pulp")
    pulp["origin"] = (0, 79, -34)
    for side in ("L", "R"):
        hinge = bind(child(claim, f"Skull hinge {side}"), f"skull_{side.lower()}")
        opened = take(pulp, f"Skull hinge {side}")
        if opened["origin"] != hinge["origin"]:
            raise ValueError(f"The closed and opened {side} skull must share a pivot")
    maw, = [key for key in claim["children"] if isinstance(key, str)]
    claim["children"].remove(maw)
    claim["children"].append(bone("maw", (0, 71, -35), [maw]))
    for i in range(7):
        petal = bind(child(pulp, f"Sensory petal {i + 1}"), f"pulp_petal_{i}")
        chain(petal, "Pulp", f"pulp_{i}_link", 2)
    pulp["children"].append(bone("gaze_eye", (0, 79, -43)))

    wings = bind(take(body, "Joining | hyphae wings"), "wings")
    thorax["children"].append(wings)
    for side in ("L", "R"):
        bind(child(wings, f"Wing {side}"), f"wing_{side.lower()}")

    # export_mesh generates one factory per name. Keep the named joints
    # addressable, and disambiguate recurring decorative groups.
    def label_details(node, counter):
        if not re.fullmatch("[a-z][a-z0-9_]*", node["name"]):
            slug = re.sub("[^a-z0-9]+", "_", node["name"].lower()).strip("_")
            node["name"] = f"detail_{next(counter)}_{slug}"
        for item in node["children"]:
            if isinstance(item, dict):
                label_details(item, counter)

    label_details(root, iter(range(len(cubes))))


def verify_uv(cubes):
    for cube in cubes.values():
        x, y, z = [b - a for a, b in zip(cube["from"], cube["to"])]
        expected = {"north": (x, y), "south": (x, y), "east": (z, y),
                    "west": (z, y), "up": (x, z), "down": (x, z)}
        for name, (w, h) in expected.items():
            u, v, u2, v2 = cube["faces"][name]["uv"]
            if (abs(u2 - u), abs(v2 - v)) != (w, h):
                raise ValueError(f"{cube['name']} {name} does not have one texel per grid unit")


def paint_glow(atlas, cubes, root):
    glow = Image.new("RGBA", atlas.size)
    luminous = {"Recessed eye patch", "Nectar window", "Vent cavity", "Siphon throat",
                "Gold sensory pit", "Central eye field", "Upper sac shoulder",
                "Broad sac chamber", "Lower sac chamber"}

    def paint(node):
        for key in node["children"]:
            if isinstance(key, dict):
                paint(key)
                continue
            cube = cubes[key]
            if cube["name"] not in luminous:
                continue
            for face in cube["faces"].values():
                u, v, u2, v2 = face["uv"]
                for y in range(min(v, v2), max(v, v2)):
                    for x in range(min(u, u2), max(u, u2)):
                        red, green, blue, alpha = atlas.getpixel((x, y))
                        if alpha and red >= 220 and green >= 140 and red - blue >= 64:
                            glow.putpixel((x, y), (red, green, blue, 255))

    paint(root)
    return glow


def main():
    project = json.loads(ART.read_text(encoding="utf-8"))
    if "groups" in project:
        resolve_outliner(project)
    cubes = {cube["uuid"]: cube for cube in project["elements"]}
    verify_uv(cubes)
    make_rig(project, cubes)
    mesh = export_mesh(project, package=PACKAGE, mesh_name="MycophantMesh",
                       source="tools/model_export/mycophant.py from MycophantSpawnMother.bbmodel",
                       split_parts=True, reverse_winding=True)
    texture, = project["textures"]
    png = b64decode(texture["source"].split(",", 1)[1], validate=True)
    if png != ART.with_name("mycophant_spawn_mother.png").read_bytes():
        raise ValueError("Export the same atlas from Blockbench as mycophant_spawn_mother.png")
    with Image.open(BytesIO(png)) as atlas:
        atlas = atlas.convert("RGBA")
        if atlas.size != (project["resolution"]["width"], project["resolution"]["height"]):
            raise ValueError("The atlas dimensions differ from the Blockbench project")
        paint_glow(atlas, cubes, project["outliner"][0]).save(TEXTURES / "mycophant_glow.png")
    JAVA.write_text(mesh, encoding="utf-8")
    (TEXTURES / "mycophant.png").write_bytes(png)


if __name__ == "__main__":
    main()

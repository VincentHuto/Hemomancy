"""Bake the Mnemonist's authored Blockbench rig and embedded atlas into runtime assets."""
from base64 import b64decode
import json
from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "model_export"))

from export_choir_keeper_java import ASSETS, ROOT, export_mesh, resolve_outliner

PROJECT = ASSETS / "models/entity/bbmodel/MnemonistPuppetModel.bbmodel"
TEXTURE = ASSETS / "textures/entity/puppeteer_summon/mnemonist_puppet.png"
PACKAGE = "com.vincenthuto.hemomancy.client.model.entity.summon"
JAVA = ROOT / "src/main/java" / PACKAGE.replace(".", "/") / "MnemonistPuppetMesh.java"


def main():
    project = json.loads(PROJECT.read_text(encoding="utf-8"))
    resolve_outliner(project)
    root, = project["outliner"]
    # The editable project keeps its lines together. On the moving rig each line
    # pivots independently from the fitting it attaches to.
    groups = {}

    def index(node):
        groups[node["name"]] = node
        for child in node["children"]:
            if isinstance(child, dict):
                index(child)

    index(root)
    cubes = {cube["uuid"]: cube for cube in project["elements"]}
    leads = groups["Control Lines"]
    root["children"].remove(leads)
    for uid in leads["children"]:
        cube = cubes[uid]
        parent, name = {
            "Shoulder lead -1": ("Right Shoulder", "right_lead"),
            "Shoulder lead 1": ("Left Shoulder", "left_lead"),
            "Dorsal lead": ("Rib Cage", "dorsal_lead"),
        }[cube["name"]]
        groups[parent]["children"].append(dict(name=name, origin=cube["origin"], children=[uid]))

    spool = groups["Memory Spool"]["children"]
    for uid in list(spool):
        if cubes[uid]["name"] in ("Dorsal guide", "Spool mounting"):
            spool.remove(uid)
            groups["Rib Cage"]["children"].append(uid)

    for name, node in groups.items():
        node["name"] = name.lower().replace(" ", "_")

    texture, = project["textures"]
    png = b64decode(texture["source"].split(",", 1)[1], validate=True)
    java = export_mesh(project, package=PACKAGE, mesh_name="MnemonistPuppetMesh",
                       source="tools/circus/build_mnemonist_puppet.py from MnemonistPuppetModel.bbmodel")
    if "--check" in sys.argv:
        if JAVA.read_text(encoding="utf-8") != java or TEXTURE.read_bytes() != png:
            raise SystemExit("Mnemonist Puppet runtime mesh or atlas is stale")
    else:
        JAVA.write_text(java, encoding="utf-8")
        TEXTURE.write_bytes(png)


if __name__ == "__main__":
    main()

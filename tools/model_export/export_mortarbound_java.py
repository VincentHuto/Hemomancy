"""Bake the Mortarbound's Blockbench rig and embedded atlas."""
from base64 import b64decode
import json

from export_choir_keeper_java import ASSETS, ROOT, export_mesh


PROJECT = ASSETS / "models/entity/bbmodel/MortarboundModel.bbmodel"
TEXTURE = ASSETS / "textures/entity/mortarbound.png"
PACKAGE = "com.vincenthuto.hemomancy.client.model.entity.mob.monster"
JAVA = ROOT / "src/main/java" / PACKAGE.replace(".", "/") / "MortarboundMesh.java"
GROUP_NAMES = {
    "Mortarbound": "mortarbound",
    "Emerged": "emerged",
    "Embedded - toggle visibility": "embedded",
    "Wall root and ribcage": "body",
    "Inkcap and hanging veil": "head",
    "Wall grip": "wall_grip",
    "Ambush claw": "ambush_claw",
}


def name_groups(group):
    group["name"] = GROUP_NAMES[group["name"]]
    for child in group["children"]:
        if isinstance(child, dict):
            name_groups(child)


def main():
    project = json.loads(PROJECT.read_text(encoding="utf-8"))
    root, = project["outliner"]
    name_groups(root)
    texture, = project["textures"]
    png = b64decode(texture["source"].split(",", 1)[1], validate=True)
    java = export_mesh(project, package=PACKAGE, mesh_name="MortarboundMesh",
                       source="tools/model_export/export_mortarbound_java.py")
    JAVA.write_text(java, encoding="utf-8")
    TEXTURE.write_bytes(png)


if __name__ == "__main__":
    main()

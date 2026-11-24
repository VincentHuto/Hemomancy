"""Bake the Barbed Urchin Blockbench rig and its embedded texture."""

from base64 import b64decode
import json
import re

from export_choir_keeper_java import ASSETS, ROOT, export_mesh


PROJECT = ROOT / "docs/art/barbed_urchin/BarbedUrchinRedesign.bbmodel"
TEXTURE = ASSETS / "textures/entity/barbed_urchin/model_barbed_urchin.png"
PACKAGE = "com.vincenthuto.hemomancy.client.model.entity.mob.aquatic"
JAVA = ROOT / "src/main/java" / PACKAGE.replace(".", "/") / "BarbedUrchinMesh.java"


def normalize_names(group, parent=""):
    name = re.sub(r"[^a-z0-9]+", "_", group["name"].lower())
    if name in ("spine_hinge", "faceted_apex", "recurved_tooth"):
        name = f"{parent}_{name}"
    group["name"] = name
    for child in group["children"]:
        if isinstance(child, dict):
            normalize_names(child, name)


def main():
    project = json.loads(PROJECT.read_text(encoding="utf-8"))
    root, = project["outliner"]
    normalize_names(root)
    java = export_mesh(project, package=PACKAGE, mesh_name="BarbedUrchinMesh",
                       source="tools/model_export/export_barbed_urchin_java.py")
    texture, = project["textures"]
    png = b64decode(texture["source"].split(",", 1)[1], validate=True)
    assert png == PROJECT.with_name("barbed_urchin.png").read_bytes()
    JAVA.write_text(java, encoding="utf-8")
    TEXTURE.write_bytes(png)


if __name__ == "__main__":
    main()

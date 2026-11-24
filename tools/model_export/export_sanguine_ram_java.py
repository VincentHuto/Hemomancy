"""Bake the Sanguine Ram's editable rig and embedded atlas into runtime assets."""
from base64 import b64decode
import json

from export_choir_keeper_java import ASSETS, ROOT, export_mesh, resolve_outliner


PROJECT = ASSETS / "models/entity/bbmodel/SanguineRamModel.bbmodel"
TEXTURE = ASSETS / "textures/entity/sanguine_ram.png"
MESH = ROOT / "src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/animal/SanguineRamMesh.java"


def main():
    project = json.loads(PROJECT.read_text(encoding="utf-8-sig"))
    if "groups" in project:
        resolve_outliner(project)
    java = export_mesh(project, mesh_name="SanguineRamMesh",
                       source="tools/model_export/export_sanguine_ram_java.py from SanguineRamModel.bbmodel")
    texture, = project["textures"]
    png = b64decode(texture["source"].split(",", 1)[1], validate=True)
    MESH.write_text(java, encoding="utf-8")
    TEXTURE.write_bytes(png)


if __name__ == "__main__":
    main()

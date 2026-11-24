"""Bake the Phlegethontic Bombardier Blockbench rig and embedded atlas."""

from base64 import b64decode
import json

from export_choir_keeper_java import ASSETS, ROOT, export_mesh, resolve_outliner


PROJECT = ASSETS / "models/entity/bbmodel/phlegethontic_bombardier.bbmodel"
TEXTURE = ASSETS / "textures/entity/phlegethontic_bombardier/phlegethontic_bombardier.png"
JAVA = ROOT / "src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/PhlegethonticBombardierMesh.java"


def main():
    project = json.loads(PROJECT.read_text(encoding="utf-8"))
    resolve_outliner(project)
    java = export_mesh(
        project,
        package="com.vincenthuto.hemomancy.client.model.entity.mob.arthropod",
        mesh_name="PhlegethonticBombardierMesh",
        source="tools/model_export/export_phlegethontic_bombardier_java.py",
    )
    texture, = project["textures"]
    png = b64decode(texture["source"].split(",", 1)[1], validate=True)
    JAVA.write_text(java, encoding="utf-8")
    TEXTURE.write_bytes(png)


if __name__ == "__main__":
    main()

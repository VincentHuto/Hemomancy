"""Bake the authored Chalybeate Snail model into Java without repacking its face UVs."""
import json
from base64 import b64decode

from export_choir_keeper_java import ASSETS, ROOT, export_mesh, resolve_outliner

PROJECT = ASSETS / "models/entity/bbmodel/ChalybeateScleritesSnail.bbmodel"
JAVA = (ROOT / "src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/aquatic"
        / "ChalybeateSnailMesh.java")


def main():
    project = json.loads(PROJECT.read_text(encoding="utf-8"))
    texture, = project["textures"]
    atlas = (PROJECT.parent / texture["relative_path"]).read_bytes()
    if b64decode(texture["source"].split(",", 1)[1], validate=True) != atlas:
        raise ValueError("The embedded Blockbench texture differs from the runtime PNG")

    resolve_outliner(project)
    JAVA.write_text(export_mesh(
        project,
        package="com.vincenthuto.hemomancy.client.model.entity.mob.aquatic",
        mesh_name="ChalybeateSnailMesh",
        source="tools/model_export/chalybeate_snail.py from ChalybeateScleritesSnail.bbmodel",
    ), encoding="utf-8")


if __name__ == "__main__":
    main()


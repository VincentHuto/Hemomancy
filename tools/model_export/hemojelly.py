"""Export the authored Hemojelly Blockbench rig to Minecraft model parts and an emissive atlas."""
import argparse
import base64
import json
import math
from io import BytesIO
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "src/main"
ASSETS = SRC / "resources/assets/hemomancy"
PROJECT = ASSETS / "models/entity/bbmodel/HemojellyManOWar.bbmodel"
MESH = SRC / "java/com/vincenthuto/hemomancy/client/model/entity/mob/aquatic/HemojellyMesh.java"
PACKAGE = "com.vincenthuto.hemomancy.client.model.entity.mob.aquatic"


def floats(values):
    return ", ".join(f"{float(value):.8f}".rstrip("0").removesuffix(".") +
                     (".0F" if float(value).is_integer() else "F") for value in values)


def minecraft_offset(point, parent):
    return [point[0] - parent[0], parent[1] - point[1], point[2] - parent[2]]


def mesh_source(project):
    elements = {cube["uuid"]: cube for cube in project["elements"]}
    lines = []

    def emit(name, parent, pivot, parent_pivot, cubes, children=(), rotation=None):
        rotation = rotation or [0, 0, 0]
        offset = minecraft_offset(pivot, parent_pivot)
        pose = f"PartPose.offset({floats(offset)})"
        if any(rotation):
            x, y, z = rotation
            radians = [math.radians(-x), math.radians(y), math.radians(-z)]
            pose = f"PartPose.offsetAndRotation({floats(offset + radians)})"
        declaration = f"PartDefinition {name} = " if children else ""
        lines.append(f'\t\t{declaration}{parent}.addOrReplaceChild("{name}", CubeListBuilder.create()')
        for cube in cubes:
            lo, hi = cube["from"], cube["to"]
            xyz = minecraft_offset([lo[0], hi[1], lo[2]], pivot)
            size = [b - a for a, b in zip(lo, hi)]
            u, v = cube["uv_offset"]
            deformation = f", new CubeDeformation({floats([cube['inflate']])})" if cube.get("inflate") else ""
            lines.append(f"\t\t\t\t.texOffs({u}, {v}).addBox({floats(xyz + size)}{deformation})")
        lines[-1] += f", {pose});"
        for child in children:
            if isinstance(child, str):
                cube = elements[child]
                emit(cube["name"], name, cube["origin"], pivot, [cube], rotation=cube["rotation"])
            else:
                group(child, name, pivot)

    def group(node, parent, parent_pivot):
        cubes = [elements[item] for item in node["children"] if isinstance(item, str)]
        children = [item for item in node["children"]
                    if isinstance(item, dict) or any(elements[item].get("rotation", []))]
        emit(node["name"], parent, node["origin"], parent_pivot,
             [cube for cube in cubes if not any(cube.get("rotation", []))],
             children, node.get("rotation"))

    for node in project["outliner"]:
        group(node, "mesh.getRoot()", [0, 24, 0])
    width, height = project["resolution"]["width"], project["resolution"]["height"]
    parts = "\n".join(lines)
    return f"""package {PACKAGE};

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Generated from HemojellyManOWar.bbmodel by tools/model_export/hemojelly.py. */
public final class HemojellyMesh {{
	private HemojellyMesh() {{
	}}

	public static LayerDefinition createBodyLayer() {{
		MeshDefinition mesh = new MeshDefinition();
{parts}
		return LayerDefinition.create(mesh, {width}, {height});
	}}
}}
"""


def glow_atlas(project, image):
    glow = Image.new("RGBA", image.size)
    for cube in project["elements"]:
        if not cube["name"].startswith("capture_tendril_"):
            continue
        for side in ("north", "south", "east", "west"):
            x0, y0, x1, y1 = cube["faces"][side]["uv"]
            for y in range(y0, y1):
                for x in range(x0, x1):
                    red, _, _, alpha = image.getpixel((x, y))
                    if alpha and red >= 150:
                        glow.putpixel((x, y), (red - 72, 24, 53, 255))
    buffer = BytesIO()
    glow.save(buffer, format="PNG", optimize=True)
    return buffer.getvalue()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="verify the checked-in exports")
    args = parser.parse_args()
    project = json.loads(PROJECT.read_text(encoding="utf-8"))
    texture = project["textures"][0]
    texture_path = (PROJECT.parent / texture["relative_path"]).resolve()
    embedded = base64.b64decode(texture["source"].split(",", 1)[1])
    if texture_path.read_bytes() != embedded:
        parser.error("the embedded Blockbench texture differs from the standalone atlas")
    image = Image.open(BytesIO(embedded)).convert("RGBA")
    outputs = {
        MESH: mesh_source(project).encode(),
        texture_path.with_name(f"{texture_path.stem}_glow.png"): glow_atlas(project, image),
    }
    for path, content in outputs.items():
        if args.check:
            if not path.exists() or path.read_bytes().replace(b"\r\n", b"\n") != content.replace(b"\r\n", b"\n"):
                parser.error(f"{path.name} is out of date")
        else:
            path.write_bytes(content)
        print(path.relative_to(ROOT))


if __name__ == "__main__":
    main()

import json
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
MODEL_DIR = ROOT / "src/main/resources/assets/hemomancy/models/item"
TEXTURE_DIR = ROOT / "src/main/resources/assets/hemomancy/textures/item"
VARIANTS = (
    "worn_vow",
    "barbed_fitting",
    "chitinite_fitting",
    "prismatic_fitting",
    "crimson_vestment",
    "monolithic_frame",
    "assumed_limb",
)


def main() -> None:
    failures = []
    base_model = json.loads((MODEL_DIR / "living_staff.json").read_text(encoding="utf-8"))
    base_min = [min(element["from"][axis] for element in base_model["elements"]) for axis in range(3)]
    base_max = [max(element["to"][axis] for element in base_model["elements"]) for axis in range(3)]
    for variant in VARIANTS:
        model_path = MODEL_DIR / f"living_staff_{variant}.json"
        texture_path = TEXTURE_DIR / f"living_staff_{variant}.png"
        model = json.loads(model_path.read_text(encoding="utf-8"))

        if "parent" in model:
            failures.append(f"{model_path.name}: must be an independent model")
        elements = model.get("elements", [])
        if len(elements) < 12:
            failures.append(f"{model_path.name}: expected at least 12 elements, got {len(elements)}")
        if model.get("display") != base_model.get("display"):
            failures.append(f"{model_path.name}: display transforms differ from standard Living Staff")
        expected_texture = f"hemomancy:item/living_staff_{variant}"
        if model.get("textures", {}).get("0") != expected_texture:
            failures.append(f"{model_path.name}: texture 0 must be {expected_texture}")
        if not texture_path.exists():
            failures.append(f"{texture_path.name}: missing")
        else:
            with Image.open(texture_path) as image:
                if image.size != (64, 64):
                    failures.append(f"{texture_path.name}: expected 64x64, got {image.size}")
                if image.mode not in ("RGBA", "P"):
                    failures.append(f"{texture_path.name}: expected alpha-capable pixel texture")

        for index, element in enumerate(elements):
            start, end = element.get("from", []), element.get("to", [])
            if len(start) != 3 or len(end) != 3 or any(a >= b for a, b in zip(start, end)):
                failures.append(f"{model_path.name}: invalid cuboid at element {index}")
                continue
            if any(end[axis] - start[axis] > 32 for axis in range(3)):
                failures.append(f"{model_path.name}: oversized cuboid at element {index}")
            if any(start[axis] < base_min[axis] - 0.001 or end[axis] > base_max[axis] + 0.001 for axis in range(3)):
                failures.append(f"{model_path.name}: element {index} exceeds standard Living Staff bounds")
            rotation = element.get("rotation")
            if rotation and any(rotation["origin"][axis] < base_min[axis] - 0.001 or rotation["origin"][axis] > base_max[axis] + 0.001 for axis in range(3)):
                failures.append(f"{model_path.name}: rotation origin {index} exceeds standard Living Staff bounds")
            for face_name, face in element.get("faces", {}).items():
                uv = face.get("uv", [])
                if len(uv) != 4 or any(value < 0 or value > 16 for value in uv):
                    failures.append(f"{model_path.name}: {face_name} UV at element {index} escapes vanilla 0-16 space")
                elif (uv[2] - uv[0], uv[3] - uv[1]) != (4, 4) or any(value % 4 for value in uv):
                    failures.append(f"{model_path.name}: {face_name} UV at element {index} is not a deliberate 16px material tile")

        material_tiles = {
            tuple(face["uv"])
            for element in elements
            for face in element.get("faces", {}).values()
            if "uv" in face
        }
        if len(material_tiles) < 4:
            failures.append(f"{model_path.name}: expected at least four designed material tiles, got {len(material_tiles)}")

    launch_path = ROOT / ".vscode/launch.json"
    launch = json.loads(launch_path.read_text(encoding="utf-8"))
    expected_resources = str(ROOT / "build/resources/main")
    for configuration in launch.get("configurations", []):
        vm_args = " ".join(configuration.get("vmArgs", []))
        if expected_resources not in vm_args:
            failures.append(f"VS Code {configuration.get('name', '<unnamed>')} launch omits processed resources")
        if configuration.get("preLaunchTask") != "gradle: processResources":
            failures.append(f"VS Code {configuration.get('name', '<unnamed>')} launch does not process resources first")

    if failures:
        raise SystemExit("\n".join(failures))
    print(f"Validated {len(VARIANTS)} independent fitting models and textures")


if __name__ == "__main__":
    main()

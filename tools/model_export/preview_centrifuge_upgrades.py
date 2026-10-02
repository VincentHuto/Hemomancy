"""Preview the actual static bodies together with the inherited animated rotor."""

import copy
import json
import subprocess

from PIL import Image, ImageDraw, ImageFont

from export_centrifuge_upgrades import assets, NAMES
from export_scriptorium_upgrades import ROOT
from preview_scriptorium_upgrades import render


OUTPUT = ROOT / "build/model-previews"
JAVA = ROOT / "src/main/java/com/vincenthuto/hemomancy/client/model/tile/crafting"


def legacy_part(kind):
    output = OUTPUT / ("centrifuge_" + kind + ".bbmodel")
    subprocess.run(["node", str(ROOT / "tools/model_export/java_model_to_bbmodel.mjs"),
                    "--source", str(JAVA / ("Centrifuge" + ("Arms" if kind == "arms" else "Stand") + "Model.java")),
                    "--texture", "textures/entity/model_centrifuge_arms.png", "--output", str(output)], check=True)
    source = json.loads(output.read_text(encoding="utf-8"))
    elements = {e["uuid"]: e for e in source["elements"]}
    result = []
    lift = 7.2 if kind == "arms" else 0
    for group in source["outliner"]:
        if group["name"].endswith("Empty"): continue
        angle = group.get("rotation", [0, 0, 0])[1]
        for child in group["children"]:
            original = elements[child]
            e = {"name": group["name"],
                 "from": [8+original["from"][0], lift+original["from"][1], 8-original["to"][2]],
                 "to": [8+original["to"][0], lift+original["to"][1], 8-original["from"][2]],
                 "faces": {side: {"texture": "#rotor", "uv": [uv/2 for uv in face["uv"]]}
                           for side, face in original["faces"].items()}}
            if angle:
                ox, oy, oz = original["origin"]
                e["rotation"] = {"axis": "y", "angle": -angle, "origin": [8+ox, lift+oy, 8-oz]}
            result.append(e)
    return result


def framed(model):
    result = copy.deepcopy(model)
    # The shared renderer uses the Scriptorium's framing. Center this short machine.
    def transform(point):
        return [8 + (point[0]-8)*1.6, 14 + (point[1]-10.75)*1.6, 10 + (point[2]-8)*1.6]
    for e in result["elements"]:
        for field in ("from", "to"): e[field] = transform(e[field])
        if "rotation" in e: e["rotation"]["origin"] = transform(e["rotation"]["origin"])
    return result


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    rotor = legacy_part("arms")
    stand = legacy_part("stand")
    models = assets()
    base = {"textures": {"rotor": "hemomancy:entity/model_centrifuge_arms"}, "elements": rotor + stand}
    entries = [("Base", "Open eight-vial spindle", base)]
    for name, title, subtitle in zip(NAMES, ("D4 - Calibrated Rotor", "D6 - Fractionating Rotor"),
                                     ("Braced bearing + governor dial", "Split manifold + paired receivers")):
        model = copy.deepcopy(models[name])
        model["textures"]["rotor"] = "hemomancy:entity/model_centrifuge_arms"
        model["elements"] += copy.deepcopy(rotor)
        entries.append((title, subtitle, model))
    sheet = Image.new("RGB", (1260, 1040), (24, 20, 25))
    draw = ImageDraw.Draw(sheet)
    title_font = ImageFont.truetype("C:/Windows/Fonts/seguisb.ttf", 23)
    font = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 16)
    for col, (title, subtitle, model) in enumerate(entries):
        draw.text((col*420+20, 14), title, fill=(245, 229, 216), font=title_font)
        draw.text((col*420+20, 47), subtitle, fill=(185, 170, 174), font=font)
        for row, yaw in enumerate((30, 150)):
            view = render(framed(model), yaw).convert("RGB")
            sheet.paste(view, (col*420, 80+row*430))
            view.save(OUTPUT / (f"centrifuge_{col}_{'front' if row == 0 else 'rear'}.png"))
        # Small view makes excessive detail and tier silhouette ambiguity obvious.
        small = render(framed(model), 30).resize((64, 64), Image.Resampling.NEAREST)
        sheet.paste(small.convert("RGB"), (col*420+325, 935))
    draw.text((20, 1010), "Actual asset textures - filled rotor samples shown. Front above, rear below.",
              fill=(185, 170, 174), font=font)
    output = OUTPUT / "centrifuge_upgrades.png"
    sheet.save(output)
    print(output)


if __name__ == "__main__":
    main()

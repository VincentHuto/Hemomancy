"""Build and inspect the two native, explicitly authored 16x16 kit sprites."""

import argparse
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/hemomancy"
SOURCE = Path(__file__).with_name("centrifuge_kit_sprites.json")
PREVIEW = ROOT / "build/sprite-previews/centrifuge_upgrade_kits.png"


def sprites():
    data = json.loads(SOURCE.read_text(encoding="utf-8"))
    palette = {key: (*bytes.fromhex(value[1:]), 255) for key, value in data["palette"].items()}
    palette["."] = (0, 0, 0, 0)
    result = {}
    for name, source in data["sprites"].items():
        grid = source["grid"]
        assert len(grid) == 16 and all(len(row) == 16 for row in grid), name + " is not a native 16x16 grid"
        image = Image.new("RGBA", (16, 16))
        image.putdata([palette[pixel] for row in grid for pixel in row])
        result[name] = (source["title"], image)
    return result


def preview(entries):
    sheet = Image.new("RGB", (760, 455), (30, 25, 31))
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 19)
    small = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 15)
    for index, (title, image) in enumerate(entries.values()):
        x = index * 380
        draw.text((x+28, 16), title, fill=(239, 225, 214), font=font)
        large = image.resize((288, 288), Image.Resampling.NEAREST)
        sheet.paste(large, (x+45, 60), large)
        # Inventory-sized samples on dark and light surfaces reveal contour issues.
        draw.rectangle((x+45, 370, x+175, 421), fill=(66, 62, 70))
        draw.rectangle((x+200, 370, x+330, 421), fill=(219, 209, 193))
        native = image.resize((32, 32), Image.Resampling.NEAREST)
        for offset in (94, 249): sheet.paste(native, (x+offset, 380), native)
    draw.text((28, 432), "Native 16x16 RGBA - nearest-neighbor previews; dark/light inventory samples below.",
              fill=(177, 164, 173), font=small)
    PREVIEW.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(PREVIEW)


def validate(path, expected):
    installed = Image.open(path).convert("RGBA")
    assert installed.size == (16, 16), path
    assert installed.tobytes() == expected.tobytes(), path.name + " source/export mismatch"
    pixels = [installed.getpixel((x, y)) for y in range(16) for x in range(16)]
    assert {p[3] for p in pixels} == {0, 255}, path.name + " requires binary alpha"
    colors = {p[:3] for p in pixels if p[3]}
    assert len(colors) <= 12, path.name + " palette exceeds 12 opaque colors"
    opaque = sum(p[3] == 255 for p in pixels)
    assert 70 <= opaque <= 180, path.name + " silhouette occupancy"
    name = path.stem
    model = json.loads((ASSETS / "models/item" / (name + ".json")).read_text(encoding="utf-8"))
    assert model["textures"]["layer0"] == "hemomancy:item/" + name, name + " references another kit sprite"
    print(f"Validated {name}: 16x16 RGBA, {len(colors)} opaque colors, binary alpha, {opaque} visible pixels, correct item model")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="validate existing exports without writing")
    args = parser.parse_args()
    entries = sprites()
    for name, (_, image) in entries.items():
        path = ASSETS / "textures/item" / (name + ".png")
        if not args.check: image.save(path)
        validate(path, image)
    images = [image.tobytes() for _, image in entries.values()]
    assert len(set(images)) == len(images), "Upgrade sprites must be distinct"
    condenser = Image.open(ASSETS / "textures/item/hematic_condenser_kit.png").convert("RGBA")
    assert all(image.tobytes() != condenser.tobytes() for _, image in entries.values()), "Condenser sprite was reused"
    if not args.check:
        preview(entries)
        print(PREVIEW)


if __name__ == "__main__":
    main()

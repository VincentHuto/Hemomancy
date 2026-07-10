from pathlib import Path
import json

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/assets/hemomancy/textures/gui/sprites/dialogue"

PALETTES = {
    "blood": ("#100507", "#52121b", "#b93447", "#ffc07e", "#35282c"),
    "unstained": ("#0d1422", "#526785", "#8ea9e8", "#dde8ff", "#333d4e"),
    "fungal": ("#190c04", "#754117", "#d17b32", "#ffc66d", "#42352b"),
}


def save_nine_slice(image: Image.Image, path: Path, border: int) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)
    meta = {"gui": {"scaling": {"type": "nine_slice", "width": image.width,
                                  "height": image.height, "border": border}}}
    path.with_suffix(path.suffix + ".mcmeta").write_text(json.dumps(meta, indent=2) + "\n", encoding="utf-8")


def panel(size: tuple[int, int], colors: tuple[str, ...], selected=False, disabled=False,
          ornate=False) -> Image.Image:
    w, h = size
    bg, edge, accent, highlight, muted = colors
    if disabled:
        edge, accent, highlight = muted, muted, muted
    elif selected:
        edge, accent = highlight, highlight
    image = Image.new("RGBA", size, bg)
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, w - 1, h - 1), outline=edge, width=1)
    draw.rectangle((2, 2, w - 3, h - 3), outline=accent, width=1)
    if ornate:
        draw.line((8, 6, w - 9, 6), fill=highlight if selected else edge)
        draw.line((8, h - 7, w - 9, h - 7), fill=muted)
        for x, y, sx, sy in ((3, 3, 1, 1), (w - 4, 3, -1, 1),
                             (3, h - 4, 1, -1), (w - 4, h - 4, -1, -1)):
            draw.line((x, y, x + sx * 6, y), fill=highlight)
            draw.line((x, y, x, y + sy * 6), fill=highlight)
            draw.point((x + sx * 2, y + sy * 2), fill=accent)
    return image


def render_themes() -> None:
    for theme, colors in PALETTES.items():
        folder = OUT / theme
        save_nine_slice(panel((64, 64), colors, ornate=True), folder / "frame.png", 8)
        save_nine_slice(panel((64, 64), colors, selected=True, ornate=True), folder / "portrait_frame.png", 8)
        save_nine_slice(panel((32, 32), colors), folder / "card.png", 4)
        save_nine_slice(panel((32, 32), colors, selected=True), folder / "card_selected.png", 4)
        save_nine_slice(panel((32, 32), colors, disabled=True), folder / "card_disabled.png", 4)
        save_nine_slice(panel((32, 16), colors), folder / "button.png", 3)
        save_nine_slice(panel((32, 16), colors, selected=True), folder / "button_selected.png", 3)
        save_nine_slice(panel((32, 16), colors, disabled=True), folder / "button_disabled.png", 3)


def icon(lines=(), polygons=(), rectangles=(), ellipses=(), color="#f2d4a4") -> Image.Image:
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    for line in lines:
        draw.line(line, fill=color, width=1)
    for poly in polygons:
        draw.polygon(poly, outline=color)
    for rect in rectangles:
        draw.rectangle(rect, outline=color)
    for ellipse in ellipses:
        draw.ellipse(ellipse, outline=color)
    return image


def render_icons() -> None:
    folder = OUT / "icons"
    folder.mkdir(parents=True, exist_ok=True)
    icons = {
        "quests": icon(lines=[(6, 2, 10, 2), (6, 6, 11, 6), (6, 9, 11, 9), (6, 12, 11, 12)], rectangles=[(3, 3, 13, 14)]),
        "inquiries": icon(lines=[(6, 2, 10, 2), (7, 3, 7, 6), (9, 3, 9, 6)], polygons=[[(7, 6), (4, 13), (12, 13), (9, 6)]]),
        "lore": icon(lines=[(8, 4, 8, 14)], polygons=[[(2, 3), (7, 4), (8, 6), (9, 4), (14, 3), (14, 13), (9, 12), (8, 14), (7, 12), (2, 13)]]),
        "conversation": icon(lines=[(5, 12, 5, 15), (5, 15, 8, 12)], polygons=[[(2, 3), (14, 3), (14, 12), (2, 12)]]),
        "leave": icon(lines=[(4, 2, 12, 2), (4, 2, 4, 14), (4, 14, 12, 14), (8, 8, 15, 8), (12, 5, 15, 8), (12, 11, 15, 8)]),
        "back": icon(lines=[(14, 8, 3, 8), (3, 8, 7, 4), (3, 8, 7, 12)]),
        "reward": icon(polygons=[[(8, 1), (10, 6), (15, 6), (11, 9), (13, 15), (8, 12), (3, 15), (5, 9), (1, 6), (6, 6)]]),
        "locked": icon(lines=[(5, 7, 5, 5), (5, 5, 6, 3), (6, 3, 10, 3), (10, 3, 11, 5), (11, 5, 11, 7)], rectangles=[(3, 7, 13, 14)]),
        "complete": icon(lines=[(2, 8, 6, 12), (6, 12, 14, 3)], color="#9fd68b"),
        "turn_in": icon(lines=[(2, 8, 6, 12), (6, 12, 14, 3), (10, 13, 15, 13)], color="#9fd68b"),
        "unread": icon(lines=[(8, 1, 8, 15), (1, 8, 15, 8), (3, 3, 13, 13), (13, 3, 3, 13)], color="#c8a6ff"),
        "active": icon(lines=[(8, 3, 8, 8), (8, 8, 12, 10)], ellipses=[(2, 2, 14, 14)], color="#f2c36d"),
        "disabled": icon(lines=[(3, 3, 13, 13), (13, 3, 3, 13)], ellipses=[(2, 2, 14, 14)], color="#746a6a"),
    }
    for name, image in icons.items():
        image.save(folder / f"{name}.png")


def render_crests() -> None:
    folder = OUT / "crests"
    folder.mkdir(parents=True, exist_ok=True)
    crests = {
        "default": icon(polygons=[[(8, 1), (14, 8), (8, 15), (2, 8)]], ellipses=[(6, 6, 10, 10)]),
        "alchemist": icon(lines=[(6, 2, 10, 2), (7, 3, 7, 6), (9, 3, 9, 6)], polygons=[[(7, 6), (4, 13), (12, 13), (9, 6)]]),
        "artificer": icon(lines=[(3, 13, 12, 4), (8, 3, 13, 8), (2, 14, 5, 15)]),
        "cicatrix_anchorite": icon(lines=[(8, 1, 8, 15), (3, 5, 13, 5), (4, 12, 8, 15), (12, 12, 8, 15)]),
        "hermit": icon(lines=[(1, 8, 4, 4), (4, 4, 8, 3), (8, 3, 12, 4), (12, 4, 15, 8), (15, 8, 12, 12), (12, 12, 8, 13), (8, 13, 4, 12), (4, 12, 1, 8)], ellipses=[(6, 6, 10, 10)]),
        "mnemonist": icon(polygons=[[(8, 1), (14, 8), (8, 15), (2, 8)]], lines=[(8, 4, 8, 12), (5, 8, 11, 8)]),
        "vicar": icon(lines=[(8, 1, 8, 15), (3, 5, 13, 5), (4, 12, 12, 12)]),
        "votary_wayfarer": icon(lines=[(8, 1, 8, 15), (1, 8, 15, 8)], ellipses=[(2, 2, 14, 14)]),
        "voyager": icon(lines=[(1, 5, 4, 8), (4, 8, 8, 5), (8, 5, 12, 8), (12, 8, 15, 5), (1, 10, 4, 13), (4, 13, 8, 10), (8, 10, 12, 13), (12, 13, 15, 10)]),
        "acolyte": icon(polygons=[[(8, 1), (13, 9), (12, 13), (8, 15), (4, 13), (3, 9)]]),
        "guardian": icon(polygons=[[(8, 1), (14, 4), (13, 11), (8, 15), (3, 11), (2, 4)]]),
        "zealot": icon(lines=[(8, 0, 8, 3), (8, 13, 8, 16), (0, 8, 3, 8), (13, 8, 16, 8), (2, 2, 4, 4), (12, 12, 14, 14), (14, 2, 12, 4), (4, 12, 2, 14)], ellipses=[(4, 4, 12, 12)]),
    }
    for name, image in crests.items():
        image.save(folder / f"{name}.png")


if __name__ == "__main__":
    render_themes()
    render_icons()
    render_crests()

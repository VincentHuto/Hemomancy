from pathlib import Path
import json

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/assets/hemomancy/textures/gui/sprites/dialogue"
SCALE = 2

PALETTES = {
    "blood": {
        "bg": "#0d0406", "void": "#050204", "shadow": "#21070c", "metal": "#471019",
        "edge": "#8f2638", "accent": "#d34b5d", "highlight": "#ffc184", "muted": "#4a373b",
        "patina": "#74452e", "chip": "#e39a64",
    },
    "unstained": {
        "bg": "#0a101a", "void": "#04070c", "shadow": "#192330", "metal": "#35465e",
        "edge": "#6e86aa", "accent": "#a4bce8", "highlight": "#eef3df", "muted": "#3c4653",
        "patina": "#60746c", "chip": "#d7d0b3",
    },
    "fungal": {
        "bg": "#120905", "void": "#050302", "shadow": "#2d1809", "metal": "#59401b",
        "edge": "#9a6725", "accent": "#d38a35", "highlight": "#ffd082", "muted": "#4b4031",
        "patina": "#557053", "chip": "#dca65c",
    },
}


def save_nine_slice(image: Image.Image, path: Path, logical_size: tuple[int, int], border: int) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)
    meta = {"gui": {"scaling": {"type": "nine_slice", "width": logical_size[0],
                                  "height": logical_size[1], "border": border}}}
    path.with_suffix(path.suffix + ".mcmeta").write_text(json.dumps(meta, indent=2) + "\n", encoding="utf-8")


def ornate_corner(draw: ImageDraw.ImageDraw, x: int, y: int, sx: int, sy: int,
                  p: dict[str, str], portrait: bool) -> None:
    def pt(dx: int, dy: int) -> tuple[int, int]:
        return x + sx * dx, y + sy * dy

    def box(a: tuple[int, int], b: tuple[int, int]) -> tuple[int, int, int, int]:
        return min(a[0], b[0]), min(a[1], b[1]), max(a[0], b[0]), max(a[1], b[1])

    # Compact squared knotwork, matching the restrained concept-art corners.
    draw.line([pt(0, 11), pt(3, 11), pt(3, 5), pt(6, 5), pt(6, 2), pt(12, 2)],
              fill=p["edge"], width=2)
    draw.line([pt(2, 14), pt(2, 8), pt(8, 8), pt(8, 4), pt(14, 4)],
              fill=p["metal"], width=2)
    draw.line([pt(1, 6), pt(5, 6), pt(5, 1)], fill=p["highlight"], width=1)
    draw.rectangle(box(pt(4, 4), pt(7, 7)), outline=p["accent"], width=1)
    draw.point(pt(3, 12), fill=p["patina"])
    draw.point(pt(10, 3), fill=p["chip"])
    if portrait:
        draw.line([pt(1, 13), pt(5, 9), pt(9, 9), pt(13, 5)], fill=p["accent"], width=1)


def frame(logical_size: tuple[int, int], p: dict[str, str], portrait: bool = False) -> Image.Image:
    w, h = (logical_size[0] * SCALE, logical_size[1] * SCALE)
    b = 8 * SCALE
    image = Image.new("RGBA", (w, h), p["bg"])
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, w - 1, h - 1), outline=p["void"], width=2)
    draw.rectangle((2, 2, w - 3, h - 3), outline=p["metal"], width=2)
    draw.rectangle((5, 5, w - 6, h - 6), outline=p["edge"], width=1)
    draw.rectangle((b - 3, b - 3, w - b + 2, h - b + 2), outline=p["accent"], width=1)
    draw.rectangle((b - 1, b - 1, w - b, h - b), outline=p["shadow"], width=1)

    ornate_corner(draw, 2, 2, 1, 1, p, portrait)
    ornate_corner(draw, w - 3, 2, -1, 1, p, portrait)
    ornate_corner(draw, 2, h - 3, 1, -1, p, portrait)
    ornate_corner(draw, w - 3, h - 3, -1, -1, p, portrait)

    # Aged metal flecks stay inside the non-stretching border bands.
    for x in (24, 35, w - 38, w - 27):
        draw.line((x, 3, x + 3, 3), fill=p["chip"], width=1)
        draw.point((x + 2, 6), fill=p["patina"])
        draw.line((x, h - 4, x + 3, h - 4), fill=p["muted"], width=1)
    for y in (25, 39, h - 42, h - 28):
        draw.line((3, y, 3, y + 4), fill=p["chip"], width=1)
        draw.line((w - 4, y, w - 4, y + 3), fill=p["patina"], width=1)
    return image


def surface(logical_size: tuple[int, int], p: dict[str, str], border: int,
            selected: bool = False, disabled: bool = False) -> Image.Image:
    w, h = logical_size[0] * SCALE, logical_size[1] * SCALE
    b = border * SCALE
    bg = p["bg"]
    edge = p["muted"] if disabled else (p["highlight"] if selected else p["edge"])
    accent = p["muted"] if disabled else (p["chip"] if selected else p["accent"])
    image = Image.new("RGBA", (w, h), bg)
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, w - 1, h - 1), outline=p["void"], width=2)
    draw.rectangle((2, 2, w - 3, h - 3), outline=edge, width=2)
    draw.rectangle((5, 5, w - 6, h - 6), outline=accent, width=1)
    draw.rectangle((b - 2, b - 2, w - b + 1, h - b + 1), outline=p["shadow"], width=1)

    # Corner caps, rivets, scratches, and patina. Nothing enters the stretch center.
    cap = max(2, b - 2)
    for x, y, sx, sy in ((2, 2, 1, 1), (w - 3, 2, -1, 1),
                          (2, h - 3, 1, -1), (w - 3, h - 3, -1, -1)):
        draw.line((x, y, x + sx * cap, y), fill=accent, width=2)
        draw.line((x, y, x, y + sy * cap), fill=edge, width=2)
        draw.rectangle((x + sx * 2 - (1 if sx < 0 else 0), y + sy * 2 - (1 if sy < 0 else 0),
                        x + sx * 2 + (1 if sx > 0 else 0), y + sy * 2 + (1 if sy > 0 else 0)),
                       fill=p["chip"] if not disabled else p["muted"])
    draw.line((b + 2, 3, min(w - b - 3, b + 9), 3), fill=p["patina"], width=1)
    draw.line((w - b - 10, h - 4, w - b - 3, h - 4), fill=p["muted"], width=1)
    return image


def render_themes() -> None:
    for theme, palette in PALETTES.items():
        folder = OUT / theme
        save_nine_slice(frame((64, 64), palette), folder / "frame.png", (64, 64), 8)
        save_nine_slice(frame((64, 64), palette, portrait=True), folder / "portrait_frame.png", (64, 64), 8)
        for name, selected, disabled in (("card", False, False), ("card_selected", True, False),
                                         ("card_disabled", False, True)):
            save_nine_slice(surface((32, 32), palette, 4, selected, disabled),
                            folder / f"{name}.png", (32, 32), 4)
        for name, selected, disabled in (("button", False, False), ("button_selected", True, False),
                                         ("button_disabled", False, True)):
            save_nine_slice(surface((32, 16), palette, 3, selected, disabled),
                            folder / f"{name}.png", (32, 16), 3)

    # The reference differentiates the four hub cards primarily through their aged border color.
    category_palettes = {
        "quests": {**PALETTES["fungal"], "edge": "#8d6c2f", "accent": "#c89a4d", "highlight": "#eccb83"},
        "inquiries": {**PALETTES["fungal"], "edge": "#4e6b2b", "accent": "#78953e", "highlight": "#b7cf70"},
        "lore": {**PALETTES["blood"], "edge": "#633768", "accent": "#9861a1", "highlight": "#c88fd1"},
        "conversation": {**PALETTES["fungal"], "edge": "#885323", "accent": "#c27b38", "highlight": "#e9ad67"},
    }
    folder = OUT / "categories"
    for category, palette in category_palettes.items():
        save_nine_slice(surface((32, 32), palette, 4), folder / f"{category}.png", (32, 32), 4)
        save_nine_slice(surface((32, 32), palette, 4, selected=True),
                        folder / f"{category}_selected.png", (32, 32), 4)


ICON = {
    "dark": "#211615", "shadow": "#513326", "metal": "#a96b38", "gold": "#f2c07b",
    "pale": "#f5dfb1", "red": "#8d2432", "green": "#78945a", "blue": "#8fb4d9",
    "purple": "#a184c8", "grey": "#766c68",
}


def icon_canvas() -> tuple[Image.Image, ImageDraw.ImageDraw]:
    image = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    return image, ImageDraw.Draw(image)


def render_icon(name: str) -> Image.Image:
    image, d = icon_canvas()
    if name == "quests":
        d.polygon([(7, 4), (24, 4), (27, 7), (24, 10), (25, 25), (8, 25), (6, 22), (8, 19)],
                  fill="#b58b55", outline=ICON["pale"])
        d.line((11, 10, 21, 10), fill=ICON["shadow"], width=2); d.line((11, 15, 20, 15), fill=ICON["shadow"], width=2)
        d.ellipse((4, 19, 13, 28), fill=ICON["red"], outline=ICON["gold"], width=1)
    elif name == "inquiries":
        d.rectangle((12, 3, 20, 8), fill=ICON["metal"], outline=ICON["pale"], width=1)
        d.polygon([(13, 8), (13, 13), (6, 25), (8, 28), (24, 28), (26, 25), (19, 13), (19, 8)],
                  fill="#37463b", outline=ICON["pale"])
        d.polygon([(9, 23), (23, 23), (25, 26), (7, 26)], fill="#75a84c")
        d.rectangle((11, 20, 21, 22), fill="#a7d96a")
    elif name == "lore":
        d.polygon([(5, 5), (23, 3), (27, 7), (26, 26), (8, 28), (5, 25)],
                  fill="#3e2948", outline=ICON["gold"])
        d.line((9, 5, 10, 27), fill=ICON["metal"], width=3)
        d.line((18, 9, 18, 21), fill=ICON["pale"], width=2); d.line((12, 15, 24, 15), fill=ICON["pale"], width=2)
        d.line((14, 11, 22, 19), fill=ICON["gold"], width=1); d.line((22, 11, 14, 19), fill=ICON["gold"], width=1)
    elif name == "conversation":
        d.rounded_rectangle((4, 5, 28, 23), radius=4, fill="#c2a36d", outline=ICON["pale"], width=2)
        d.polygon([(9, 22), (9, 29), (16, 22)], fill="#c2a36d", outline=ICON["pale"])
        for x in (10, 16, 22): d.ellipse((x - 1, 13, x + 1, 15), fill=ICON["dark"])
    elif name == "leave":
        d.rounded_rectangle((7, 3, 25, 29), radius=2, fill="#5c3924", outline=ICON["gold"], width=2)
        d.rectangle((10, 6, 22, 28), fill="#34231c", outline=ICON["metal"])
        d.ellipse((18, 16, 20, 18), fill=ICON["gold"])
    elif name == "back":
        d.polygon([(3, 16), (14, 6), (14, 12), (28, 12), (28, 20), (14, 20), (14, 26)],
                  fill="#b78950", outline=ICON["pale"])
        d.line((7, 16, 25, 16), fill=ICON["metal"], width=2)
    elif name == "reward":
        d.polygon([(16, 2), (20, 11), (30, 11), (22, 17), (25, 28), (16, 22), (7, 28), (10, 17), (2, 11), (12, 11)],
                  fill="#c58a38", outline=ICON["pale"])
        d.polygon([(16, 7), (18, 14), (24, 14), (19, 18), (20, 23), (16, 19)], fill=ICON["gold"])
    elif name == "locked":
        d.arc((8, 2, 24, 18), 180, 360, fill=ICON["pale"], width=4)
        d.rounded_rectangle((5, 12, 27, 29), radius=3, fill="#8a643b", outline=ICON["gold"], width=2)
        d.ellipse((14, 17, 18, 21), fill=ICON["dark"]); d.rectangle((15, 20, 17, 25), fill=ICON["dark"])
    elif name in ("complete", "turn_in"):
        d.line((5, 16, 13, 24, 28, 6), fill="#355237", width=7)
        d.line((5, 15, 13, 22, 27, 6), fill="#a8d080", width=3)
        if name == "turn_in": d.line((7, 28, 27, 28), fill=ICON["gold"], width=3)
    elif name == "unread":
        d.polygon([(16, 1), (19, 11), (28, 5), (22, 14), (31, 16), (21, 19), (27, 28),
                   (18, 22), (16, 31), (13, 21), (4, 27), (10, 18), (1, 16), (11, 13), (5, 4), (14, 10)],
                  fill="#745d98", outline="#d6b8ff")
        d.ellipse((13, 13, 19, 19), fill="#eee0ff")
    elif name == "active":
        d.ellipse((3, 3, 29, 29), fill="#5b4226", outline=ICON["gold"], width=2)
        d.ellipse((8, 8, 24, 24), fill=ICON["dark"], outline="#d6a04c")
        d.line((16, 9, 16, 17, 22, 20), fill=ICON["pale"], width=2)
    elif name == "disabled":
        d.ellipse((3, 3, 29, 29), fill="#302b2c", outline=ICON["grey"], width=3)
        d.line((8, 8, 24, 24), fill="#a18e8e", width=4)
    return image


def render_icons() -> None:
    folder = OUT / "icons"
    folder.mkdir(parents=True, exist_ok=True)
    for name in ("quests", "inquiries", "lore", "conversation", "leave", "back", "reward", "locked",
                 "complete", "turn_in", "unread", "active", "disabled"):
        render_icon(name).save(folder / f"{name}.png")


def crest(name: str, faction: str) -> Image.Image:
    image, d = icon_canvas()
    edge = "#d19a58" if faction == "blood" else "#c5d3df" if faction == "unstained" else "#b68a45"
    fill = "#4b1520" if faction == "blood" else "#27374d" if faction == "unstained" else "#39402a"
    d.polygon([(16, 2), (27, 7), (25, 23), (16, 30), (7, 23), (5, 7)], fill=fill, outline=edge)
    d.line((8, 9, 16, 5, 24, 9), fill=ICON["pale"], width=1)
    if name in ("default", "mnemonist"):
        d.polygon([(16, 6), (23, 16), (16, 26), (9, 16)], outline=edge, fill=ICON["dark"])
        d.line((16, 9, 16, 23), fill=ICON["pale"], width=2)
    elif name == "alchemist":
        d.rectangle((13, 6, 19, 11), fill=edge); d.polygon([(13, 11), (9, 24), (23, 24), (19, 11)], fill="#648344", outline=edge)
    elif name == "artificer":
        d.line((9, 23, 22, 10), fill=edge, width=4); d.polygon([(17, 7), (25, 15), (22, 18), (14, 10)], fill=ICON["pale"])
    elif name == "cicatrix_anchorite":
        d.line((16, 6, 16, 26), fill=ICON["pale"], width=2); d.line((9, 12, 23, 12), fill=edge, width=3)
        d.line((11, 22, 16, 27, 21, 22), fill=edge, width=2)
    elif name == "hermit":
        d.ellipse((8, 11, 24, 21), fill=ICON["dark"], outline=edge); d.ellipse((13, 13, 19, 19), fill=ICON["pale"])
        d.ellipse((15, 15, 17, 17), fill=ICON["dark"])
    elif name == "vicar":
        d.line((16, 5, 16, 27), fill=ICON["pale"], width=3); d.line((9, 11, 23, 11), fill=edge, width=3)
    elif name == "votary_wayfarer":
        d.ellipse((8, 8, 24, 24), outline=edge, width=2); d.polygon([(16, 6), (19, 16), (16, 26), (13, 16)], fill=ICON["pale"])
    elif name == "voyager":
        d.line((7, 13, 11, 10, 16, 14, 21, 10, 25, 13), fill=edge, width=2)
        d.line((7, 20, 11, 17, 16, 21, 21, 17, 25, 20), fill=ICON["pale"], width=2)
    elif name == "acolyte":
        d.polygon([(16, 5), (23, 18), (21, 24), (16, 27), (11, 24), (9, 18)], fill="#7899b8", outline=edge)
    elif name == "guardian":
        d.polygon([(16, 6), (24, 10), (22, 22), (16, 27), (10, 22), (8, 10)], fill="#556b82", outline=ICON["pale"])
    elif name == "zealot":
        d.ellipse((9, 9, 23, 23), fill="#d0d7d5", outline=edge); d.line((16, 4, 16, 28), fill=edge, width=2)
        d.line((4, 16, 28, 16), fill=edge, width=2)
    return image


def render_crests() -> None:
    folder = OUT / "crests"
    folder.mkdir(parents=True, exist_ok=True)
    blood = {"default", "alchemist", "artificer", "cicatrix_anchorite", "hermit", "mnemonist", "vicar",
             "votary_wayfarer", "voyager"}
    unstained = {"acolyte", "guardian", "zealot"}
    for name in sorted(blood | unstained):
        crest(name, "blood" if name in blood else "unstained").save(folder / f"{name}.png")


if __name__ == "__main__":
    render_themes()
    render_icons()
    render_crests()

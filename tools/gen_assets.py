#!/usr/bin/env python3
"""VenderLive asset generator.

Change the values in CONFIG, run this file, and every bitmap in
app/src/main/res/drawable-*dpi plus the launcher mipmaps is rebuilt.

    python3 tools/gen_assets.py

Vector icons are not touched here: they live as XML in res/drawable and as
editable SVG in design/icons. Edit those directly.
"""
import math
import os

from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")
SRC = os.path.join(ROOT, "design", "source")

CONFIG = {
    # seat frames: (ring start, ring end, inner disc, glyph colour, locked)
    "seat_empty": ((110, 66, 240), (170, 120, 255), (28, 22, 42, 238), (235, 228, 255), False),
    "seat_occupied": ((255, 77, 141), (255, 170, 205), (52, 26, 58, 240), (255, 226, 238), False),
    "seat_locked": ((104, 100, 120), (150, 146, 168), (30, 27, 40, 232), (198, 194, 210), True),
    # avatar rings
    "ring_host": ((255, 196, 84), (255, 236, 180)),
    "ring_noble": ((190, 150, 255), (120, 77, 255)),
    "ring_guard": ((90, 214, 255), (40, 150, 255)),
    # level badge tiers, index 0 is level 1
    "levels": [(124, 77, 255), (140, 92, 255), (158, 110, 255), (176, 128, 255),
               (255, 77, 141), (255, 110, 164), (255, 150, 190), (255, 196, 84),
               (255, 214, 120), (255, 236, 180)],
    "background_darken": 0.35,
    "background_size": (1080, 1920),
}

DENSITIES = {"xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
LAUNCHER = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}


def _lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def _ring(size, c1, c2, inner, glyph_color, locked=False, glow=0.28, glyph=True):
    ss = 4
    s = size * ss
    im = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    pad = int(s * 0.03)
    ring_w = int(s * 0.075)
    box = [pad, pad, s - pad, s - pad]

    halo = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    ImageDraw.Draw(halo).ellipse(box, outline=c2 + (int(255 * glow),), width=ring_w)
    im = Image.alpha_composite(im, halo.filter(ImageFilter.GaussianBlur(s * 0.035)))

    if inner[3] > 0:
        disc = Image.new("RGBA", (s, s), (0, 0, 0, 0))
        ImageDraw.Draw(disc).ellipse(box, fill=inner)
        im = Image.alpha_composite(im, disc)
        shade = Image.new("RGBA", (s, s), (255, 255, 255, 16))
        mask = Image.new("L", (s, s), 0)
        ImageDraw.Draw(mask).ellipse(box, fill=255)
        shade.putalpha(Image.composite(shade.split()[3], Image.new("L", (s, s), 0), mask))
        im = Image.alpha_composite(im, shade.crop((0, 0, s, s // 2)).resize((s, s)))

    d = ImageDraw.Draw(im)
    steps = 360
    for i in range(steps):
        t = (math.sin(i / steps * math.pi * 2 - math.pi / 2) + 1) / 2
        a = i / steps * 360
        d.arc(box, a, a + 1.6, fill=_lerp(c1, c2, t) + (255,), width=ring_w)
    d.arc(box, 195, 300, fill=(255, 255, 255, 120), width=int(ring_w * 0.42))

    cx = cy = s // 2
    if glyph and not locked:
        cw = int(s * 0.075)
        ch = int(s * 0.20)
        cy2 = int(s * 0.43)
        d.rounded_rectangle([cx - cw, cy2 - ch // 2, cx + cw, cy2 + ch // 2], radius=cw, fill=glyph_color)
        d.arc([cx - int(s * 0.115), cy2 + int(s * 0.03), cx + int(s * 0.115), cy2 + int(s * 0.21)],
              start=0, end=180, fill=glyph_color, width=int(s * 0.038))
        d.line([cx, cy2 + int(s * 0.205), cx, cy2 + int(s * 0.27)], fill=glyph_color, width=int(s * 0.038))
    if locked:
        bw = int(s * 0.105)
        bh = int(s * 0.085)
        ly = int(s * 0.55)
        d.rounded_rectangle([cx - bw, ly - bh // 2, cx + bw, ly + bh], radius=int(s * 0.018), fill=glyph_color)
        d.arc([cx - int(s * 0.06), ly - int(s * 0.135), cx + int(s * 0.06), ly - int(s * 0.005)],
              start=180, end=360, fill=glyph_color, width=int(s * 0.03))
    return im.resize((size, size), Image.LANCZOS)


def background(src_name, out_name, darken):
    w, h = CONFIG["background_size"]
    im = Image.open(os.path.join(SRC, src_name)).convert("RGB")
    scale = max(w / im.width, h / im.height)
    im = im.resize((int(im.width * scale) + 1, int(im.height * scale) + 1), Image.LANCZOS)
    left = (im.width - w) // 2
    top = (im.height - h) // 2
    im = im.crop((left, top, left + w, top + h))
    im = Image.blend(im, Image.new("RGB", im.size, (10, 7, 18)), darken)
    im.save(os.path.join(RES, "drawable-nodpi", out_name), "WEBP", quality=88, method=6)


def hexagon(size, color, pips):
    s = size * 4
    im = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    cx = cy = s // 2
    r = s * 0.46
    pts = [(cx + r * math.cos(math.radians(90 + 60 * i)), cy - r * math.sin(math.radians(90 + 60 * i)))
           for i in range(6)]
    d.polygon(pts, fill=(20, 16, 30, 235), outline=color + (255,))
    d.line(pts + [pts[0]], fill=color + (255,), width=int(s * 0.045), joint="curve")
    r2 = r * 0.74
    pts2 = [(cx + r2 * math.cos(math.radians(90 + 60 * i)), cy - r2 * math.sin(math.radians(90 + 60 * i)))
            for i in range(6)]
    d.line(pts2 + [pts2[0]], fill=color + (140,), width=int(s * 0.02), joint="curve")
    n = pips + 1
    span = s * 0.42
    rad = s * 0.018
    for i in range(n):
        x = cx - span / 2 + (span / max(n - 1, 1)) * i
        y = cy + s * 0.20
        d.ellipse([x - rad, y - rad, x + rad, y + rad], fill=color + (255,))
    return im.resize((size, size), Image.LANCZOS)


def launcher(size):
    s = size * 4
    im = Image.new("RGBA", (s, s), (14, 11, 22, 255))
    d = ImageDraw.Draw(im)
    d.ellipse([-s * 0.2, -s * 0.2, s * 1.2, s * 1.2], fill=(42, 27, 74, 255))
    cw = int(s * 0.085)
    ch = int(s * 0.24)
    cx = s // 2
    cy = int(s * 0.44)
    d.rounded_rectangle([cx - cw, cy - ch // 2, cx + cw, cy + ch // 2], radius=cw, fill=(255, 77, 141))
    d.arc([cx - int(s * 0.145), cy + int(s * 0.02), cx + int(s * 0.145), cy + int(s * 0.24)],
          start=0, end=180, fill=(243, 239, 250), width=int(s * 0.038))
    d.line([cx, cy + int(s * 0.235), cx, cy + int(s * 0.30)], fill=(243, 239, 250), width=int(s * 0.038))
    for r in (0.24, 0.33):
        d.arc([cx - s * r, cy - s * r, cx + s * r, cy + s * r], start=140, end=220,
              fill=(124, 77, 255), width=int(s * 0.034))
        d.arc([cx - s * r, cy - s * r, cx + s * r, cy + s * r], start=-40, end=40,
              fill=(124, 77, 255), width=int(s * 0.034))
    return im.resize((size, size), Image.LANCZOS)


def main():
    for name, px in DENSITIES.items():
        out = os.path.join(RES, f"drawable-{name}")
        os.makedirs(out, exist_ok=True)
        for key, file in (("seat_empty", "seat_frame_empty"), ("seat_occupied", "seat_frame_occupied"),
                          ("seat_locked", "seat_frame_locked")):
            c1, c2, inner, gc, locked = CONFIG[key]
            _ring(px, c1, c2, inner, gc, locked).save(os.path.join(out, file + ".png"))
        for key, file in (("ring_host", "avatar_ring_host"), ("ring_noble", "avatar_ring_noble"),
                          ("ring_guard", "avatar_ring_guard")):
            c1, c2 = CONFIG[key]
            _ring(px, c1, c2, (0, 0, 0, 0), (0, 0, 0), False, 0.28, glyph=False).save(
                os.path.join(out, file + ".png"))

    for i, color in enumerate(CONFIG["levels"], start=1):
        hexagon(216, color, i - 1).save(os.path.join(RES, "drawable-xxhdpi", f"level_badge_{i}.png"))

    for name, px in LAUNCHER.items():
        out = os.path.join(RES, f"mipmap-{name}")
        os.makedirs(out, exist_ok=True)
        launcher(px).save(os.path.join(out, "ic_launcher.png"))
        launcher(px).save(os.path.join(out, "ic_launcher_round.png"))

    os.makedirs(os.path.join(RES, "drawable-nodpi"), exist_ok=True)
    background("room_bg_source.png", "room_bg_default.webp", CONFIG["background_darken"])
    background("login_bg_source.png", "login_bg.webp", 0.25)
    print("assets regenerated")


if __name__ == "__main__":
    main()

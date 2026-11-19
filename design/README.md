# Editing VenderLive assets

Two kinds of assets, two ways to change them.

## 1. Icons (XML vectors in res/drawable)

Files like `ic_mic_on.xml`, `ic_tab_home.xml`, `ic_menu_wallet.xml` are plain
vector XML. Open the file, change `android:fillColor` to restyle, or replace the
`android:pathData` to change the shape. Every density is covered automatically.

Editable SVG copies of the same icons are in `design/icons/`. Edit them in Figma,
Illustrator or Inkscape, then copy the resulting path data back into the matching
XML file.

## 2. Bitmaps (seat frames, rings, badges, backgrounds, launcher)

These are generated, not hand-drawn. Open `tools/gen_assets.py`, change the
values in `CONFIG` at the top, and run:

```
python3 tools/gen_assets.py
```

What you can change there:

1. `seat_empty`, `seat_occupied`, `seat_locked` - ring gradient start/end, inner disc, glyph colour
2. `ring_host`, `ring_noble`, `ring_guard` - avatar ring gradients
3. `levels` - the ten level badge colours, one per level
4. `background_darken` - how dark the room and login backgrounds are
5. `background_size` - output size of the backgrounds

The background source art is in `design/source/` (1024x1024 PNG). Replace those
two files with new artwork and rerun the script to get fresh 1080x1920 WebP
backgrounds.

Requires Pillow: `pip install pillow`.

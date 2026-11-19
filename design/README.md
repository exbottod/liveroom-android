# Editing VenderLive assets

Two kinds of assets, two ways to change them.

## 1. Icons (XML vectors in res/drawable)

Files use the same naming style as the reference app, so slots are easy to
recognise: `room_ic_bottom_mic_un_mute.xml`, `home_ic_tab_home.xml`, `ic_wallet.xml`.
They are plain vector XML. Open the file, change `android:fillColor` to restyle, or replace the
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

## Naming convention

Prefixes say where an asset belongs, so a slot is recognisable at a glance:

1. `room_ic_` - anything drawn inside the room: mic seats, rings, bottom bar, right rail
2. `home_ic_` - bottom navigation tabs
3. `ic_lv_` - level badges, `ic_lv_1` to `ic_lv_10`
4. `ic_` - standalone icons: wallet, svip, noble, level, medal, cp, relationship, family, guard, coin, diamond, gift
5. `bg_` - full screen backgrounds

Artwork is always ours. The names only mirror the reference layout so swapping or
adding a slot later is predictable.

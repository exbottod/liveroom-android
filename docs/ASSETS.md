# VenderLive asset slots

Every file below must be original VenderLive artwork. Nothing is taken from
another app. The list exists so a designer (or generated art) can fill exact
slots with the right size and name, and the app can reference them without
layout changes.

## Density rule

Vector drawables (XML) cover every density and need no folder variants. Bitmap
slots below follow the standard multipliers from a 24dp base:
mdpi 1x, hdpi 1.5x, xhdpi 2x, xxhdpi 3x, xxxhdpi 4x.

## 1. Launcher

```
mipmap-mdpi/ic_launcher.png        48x48
mipmap-hdpi/ic_launcher.png        72x72
mipmap-xhdpi/ic_launcher.png       96x96
mipmap-xxhdpi/ic_launcher.png     144x144
mipmap-xxxhdpi/ic_launcher.png    192x192
mipmap-anydpi-v26/ic_launcher.xml  (adaptive, vector foreground)
```

## 2. Login and onboarding

```
drawable-nodpi/login_bg.webp       1080x1920
assets/mp4/login.mp4               optional motion intro, own render
```

## 3. Room (phase 2)

```
drawable-nodpi/room_bg_default.webp      1080x1920
drawable-nodpi/room_bg_night.webp        1080x1920
drawable-xxhdpi/seat_frame_empty.webp    144x144
drawable-xxhdpi/seat_frame_occupied.webp 144x144
drawable-xxhdpi/seat_frame_locked.webp   144x144
drawable-xxhdpi/avatar_ring_host.webp    160x160
drawable-xxhdpi/avatar_ring_noble.webp   160x160
drawable/ic_mic_on.xml, ic_mic_muted.xml, ic_seat_empty.xml, ic_seat_locked.xml
```

## 4. Gifts and effects

```
drawable-xxhdpi/gift_placeholder.webp      96x96
assets/svga/gift_combo.svga                own animation, ~200KB
assets/svga/room_speaking.svga             own animation
assets/mp4/pk_start.mp4                    own render
assets/mp3/gift_send.mp3                   own sound
```

## 5. Levels, badges, wallet

```
drawable-xxhdpi/level_badge_1.webp ... level_badge_10.webp   72x72
drawable-xxhdpi/coin_icon.webp          48x48
drawable-xxhdpi/diamond_icon.webp       48x48
```

## 6. Navigation

```
drawable/ic_nav_room.xml, ic_nav_message.xml, ic_nav_profile.xml
```

All XML slots are ours already. Bitmap, svga, mp4 and mp3 slots are filled only
with VenderLive originals.

## Layout previews

`tools/render_preview.py` renders the login, room and profile layouts to PNG using the
real res assets, so the design can be reviewed without building the app. It needs
Pillow, and the vector icons rasterised first:

```
for f in design/icons/*.svg; do rsvg-convert -w 192 -h 192 "$f" -o "art/icons_png/$(basename "$f" .svg).png"; done
python3 tools/render_preview.py
```

Output goes to `art/ui/`. These are design renders, not device screenshots.

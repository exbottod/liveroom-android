# Asset map: reference slots vs VenderLive files

The reference APK's drawable folder holds 1,060 images: 41 belong to Android
libraries (abc_, mtrl_, notification_, exo_) and 1,019 are app artwork. None of
them are used in this project. This file tracks which slot each screen needs, and
which VenderLive file already covers it.

Rule: slots are rebuilt from scratch. The reference is a checklist of what a
screen needs, never a source of pixels. File names follow the reference layout so
slots are easy to recognise, but every pixel is ours.

## Covered by our own files

| Slot | VenderLive file |
| --- | --- |
| Mic on / muted | res/drawable/room_ic_bottom_mic_un_mute.xml, room_ic_bottom_mic_mute.xml |
| Seat empty / occupied / locked | res/drawable-*dpi/room_ic_mic_empty.png, room_ic_mic_occupied.png, room_ic_mic_lock.png |
| Host / noble / guard ring | res/drawable-*dpi/avatar_ring_host.png, avatar_ring_noble.png, avatar_ring_guard.png |
| Level badge 1-10 | res/drawable-xxhdpi/ic_lv_1.png .. ic_lv_10.png |
| Coin / diamond | res/drawable-xxhdpi/ic_coin_big.png, ic_diamond_big.png |
| Gift slot | res/drawable-xxhdpi/ic_gift.png |
| Bottom tabs | res/drawable/home_ic_tab_home, home_ic_tab_game, home_ic_tab_moment, home_ic_tab_msg, home_ic_tab_mine |
| Profile menu | res/drawable/ic_wallet, ic_svip, ic_noble, ic_level, ic_medal, ic_cp, ic_relationship, ic_family, ic_guard |
| Room chrome | res/drawable/ic_rocket, ic_vault, ic_game, ic_gift, ic_volume, ic_chat, ic_emoji, ic_grid_menu, ic_power, ic_trophy, ic_crown, ic_chevron_right |
| Backgrounds | res/drawable-nodpi/bg_room_default.webp, bg_login.webp |
| Launcher | res/mipmap-*/ic_launcher.png, ic_launcher_round.png |

## Still to build (by screen, counted from the reference)

1. Kursi & mic: 32 slots, e.g. mic seat waves, speaking pulse, PK score panels. Needed for phase 3 audio.
2. Profil & level: 89 slots, e.g. level card, medal rank list, noble and SVIP panels.
3. Dompet & koin: 23 slots, e.g. wallet header, recharge tiles, coin animation frames.
4. Keluarga: 59 slots, e.g. family top card, level shields bronze to mythic, lottery button.
5. CP & hubungan: 103 slots, e.g. CP ring display, CP invite card, CP tab states.
6. Gift: 38 slots, e.g. gift panel, lucky bag entry and open states.
7. Tab & nav: 38 slots, e.g. rank tabs (charm, honor), home rank cards.
8. Room: 93 slots, e.g. room rank top, room tags, room list cards.
9. Login & splash: 4 slots, e.g. logo splash, login background (ours done).

## How each new slot gets built

Small icons: vector XML in res/drawable plus an editable SVG in design/icons.
Illustrations, panels and frames: parameters in tools/gen_assets.py CONFIG, or a
new Pillow function in the same file, so colours and sizes stay adjustable.

from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os, math

RES='app/src/main/res'
XX=f'{RES}/drawable-xxhdpi'
DR=f'{RES}/drawable'
S=2.75                      # dp -> px
W,H=1080,2340

BRAND=(124,77,255); ACCENT=(255,77,141); CARD=(26,21,38); DARK=(14,11,22)
WHITE=(243,239,250); MUTED=(180,172,200)

def font(px, bold=True):
    p='/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf' if bold else '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'
    return ImageFont.truetype(p, px)

def dp(v): return int(v*S)

ICON_DIR='../art/icons_png'
def ip(name):
    q=f'{ICON_DIR}/{name}.png'
    return q if os.path.exists(q) else f'{XX}/{name}.png'


def rr(d, box, r, fill=None, outline=None, w=2):
    d.rounded_rectangle(box, radius=r, fill=fill, outline=outline, width=w)

def txt(d, xy, s, px, fill=WHITE, bold=True, anchor='la'):
    d.text(xy, s, font=font(px, bold), fill=fill, anchor=anchor)

def paste(im, path, box, size):
    a=Image.open(path).convert('RGBA').resize(size, Image.LANCZOS)
    im.paste(a, box, a)

def vgrad(size, top, bottom):
    g=Image.new('RGB',(1,size[1]))
    for y in range(size[1]):
        t=y/max(size[1]-1,1)
        g.putpixel((0,y), tuple(int(top[i]+(bottom[i]-top[i])*t) for i in range(3)))
    return g.resize(size)

def avatar(d, cx, cy, r, seed_color=(90,60,140)):
    d.ellipse([cx-r,cy-r,cx+r,cy+r], fill=seed_color)

def bg(path):
    im=Image.open(path).convert('RGB')
    # crop to fill
    tr=W/H; sr=im.width/im.height
    if sr>tr:
        nw=int(im.height*tr); im=im.crop(((im.width-nw)//2,0,(im.width-nw)//2+nw,im.height))
    else:
        nh=int(im.width/tr); im=im.crop((0,(im.height-nh)//2,im.width,(im.height-nh)//2+nh))
    return im.resize((W,H), Image.LANCZOS).convert('RGBA')

def overlay(im, color=(8,6,14), alpha=120):
    o=Image.new('RGBA',(W,H), color+(alpha,))
    return Image.alpha_composite(im,o)

# ---------------- LOGIN ----------------
def login():
    im=bg(f'{RES}/drawable-nodpi/bg_login.webp')
    im=overlay(im,(6,4,12),110)
    d=ImageDraw.Draw(im)
    txt(d,(W//2,dp(150)),'VenderLive',dp(42),WHITE,True,'mm')
    txt(d,(W//2,dp(196)),'Masuk untuk mulai ngobrol',dp(15),MUTED,False,'mm')

    card=[dp(24),H-dp(300),W-dp(24),H-dp(40)]
    rr(d,card,dp(26),(20,16,30,235))
    y=card[1]+dp(28)
    txt(d,(card[0]+dp(24),y),'Nomor HP',dp(13),MUTED,False); y+=dp(26)
    rr(d,[card[0]+dp(18),y,card[2]-dp(18),y+dp(56)],dp(16),(38,30,56))
    txt(d,(card[0]+dp(38),y+dp(28)),'+62',dp(16),WHITE,True,'lm')
    d.line([card[0]+dp(92),y+dp(14),card[0]+dp(92),y+dp(42)],fill=(90,78,120),width=2)
    txt(d,(card[0]+dp(108),y+dp(28)),'812 3456 7890',dp(16),(150,142,175),False,'lm')
    y+=dp(76)
    btn=[card[0]+dp(18),y,card[2]-dp(18),y+dp(56)]
    g=vgrad((btn[2]-btn[0],btn[3]-btn[1]),BRAND,ACCENT).convert('RGBA')
    mask=Image.new('L',g.size,0); ImageDraw.Draw(mask).rounded_rectangle([0,0,g.size[0]-1,g.size[1]-1],radius=dp(16),fill=255)
    im.paste(g,(btn[0],btn[1]),mask)
    txt(d,((btn[0]+btn[2])//2,y+dp(28)),'Kirim Kode OTP',dp(16),WHITE,True,'mm')
    y+=dp(76)
    txt(d,((card[0]+card[2])//2,y),'Saya setuju dengan syarat dan ketentuan',dp(12),MUTED,False,'ma')
    return im

# ---------------- ROOM ----------------
def seat(im, cx, cy, size_px, frame, ring=None, nick=None, level=None, host=False, muted=False, speaking=False):
    d=ImageDraw.Draw(im)
    paste(im, f'{XX}/{frame}.png', (cx-size_px//2, cy-size_px//2), (size_px,size_px))
    if ring: paste(im, f'{XX}/{ring}.png', (cx-size_px//2, cy-size_px//2), (size_px,size_px))
    if nick:
        r=int(size_px*0.31)
        avatar(d, cx, cy, r, (86,58,140) if not speaking else (120,50,110))
        txt(d,(cx,cy),nick[:2].upper(),int(size_px*0.13),WHITE,True,'mm')
        if level is not None:
            chip=f'{XX}/ic_lv_{min(level//10*10,100)}.png'
            cw=int(size_px*0.52); ch=int(cw/2.9)
            paste(im, chip, (cx-size_px//2, cy+size_px//2-ch-2), (cw,ch))
        if muted:
            bx,by=cx+size_px//2-int(size_px*0.22), cy+size_px//2-int(size_px*0.22)
            d.ellipse([bx-size_px*0.11,by-size_px*0.11,bx+size_px*0.11,by+size_px*0.11], fill=(14,11,22))
            paste(im, ip("room_ic_bottom_mic_mute"), (int(bx-size_px*0.06),int(by-size_px*0.06)), (int(size_px*0.12),int(size_px*0.12)))
    if host:
        paste(im, ip("room_ic_crown"), (cx-dp(11), cy-size_px//2-dp(20)), (dp(22),dp(22)))

def icon(im, path, cx, cy, size, tint=None):
    a=Image.open(path).convert('RGBA').resize((size,size), Image.LANCZOS)
    if tint:
        px=a.load()
        for y in range(a.height):
            for x in range(a.width):
                r,g,b,al=px[x,y]
                if al>10: px[x,y]=(tint[0],tint[1],tint[2],al)
    im.paste(a,(cx-size//2, cy-size//2),a)

def room():
    im=bg(f'{RES}/drawable-nodpi/bg_room_default.webp')
    im=overlay(im,(8,6,14),70)
    d=ImageDraw.Draw(im)
    x,y=dp(12),dp(14)
    # header pill
    rr(d,[x,y,x+dp(230),y+dp(34)],dp(17),(26,21,38,200))
    d.rectangle([x+dp(12),y+dp(9),x+dp(32),y+dp(25)], fill=(206,32,40)); d.rectangle([x+dp(12),y+dp(17),x+dp(32),y+dp(21)], fill=WHITE)
    txt(d,(x+dp(42),y+dp(17)),'ID',dp(12),WHITE,True,'lm')
    txt(d,(x+dp(68),y+dp(17)),'ID: 613583246',dp(11),(200,192,215),False,'lm')
    txt(d,(x+dp(176),y+dp(17)),'1',dp(11),(215,208,228),False,'lm')
    icon(im,ip("room_ic_grid_menu"),W-dp(96),y+dp(17),dp(22),(235,230,245))
    icon(im,ip("room_ic_rocket"),W-dp(62),y+dp(17),dp(22),(235,230,245))
    icon(im,ip("room_ic_power"),W-dp(28),y+dp(17),dp(22),(235,230,245))
    icon(im,ip("room_ic_trophy"),x+dp(8),y+dp(58),dp(16),(255,196,84))
    txt(d,(x+dp(24),y+dp(58)),'0',dp(12),(225,218,238),False,'lm')

    # host
    hy=y+dp(130)
    seat(im,W//2,hy,dp(84),'room_ic_mic_occupied','room_ic_ring_owner','BANG EX',12,host=True)
    txt(d,(W//2,hy+dp(56)),'BANG EX',dp(12),WHITE,True,'ma')
    txt(d,(W//2,hy+dp(74)),'0',dp(11),(255,77,141),False,'ma')

    # grid 3x4
    gy=hy+dp(112)
    frames={1:('room_ic_mic_pk_red',None,'Vivi',3,False,False),
            2:('room_ic_mic_pk_blue',None,'Rian',1,True,False),
            3:('room_ic_mic_occupied','room_ic_mic_cp','Dewi',4,False,False),
            5:('room_ic_mic_lock',None,None,None,False,False),
            7:('room_ic_mic_speaking',None,'Sari',5,False,True)}
    n=1
    for row in range(3):
        for col in range(4):
            cx=int(dp(20)+ (W-dp(40))/4*(col+0.5)); cy=gy+row*dp(88)
            f,ring,nick,lv,mu,sp = frames.get(n,('room_ic_mic_empty',None,None,None,False,False))
            seat(im,cx,cy,dp(62),f,ring,nick,lv,muted=mu,speaking=sp)
            txt(d,(cx,cy+dp(46)),str(n),dp(11),ACCENT if nick else (185,178,205),False,'ma')
            n+=1

    # tabs
    ty=gy+dp(3*88)+dp(18)
    tabs=['Ngobrol','Musik','Game','PK']
    tw=(W-dp(56))//len(tabs)
    for i,t in enumerate(tabs):
        bx=dp(28)+i*tw
        if i==0:
            rr(d,[bx,ty,bx+tw-dp(8),ty+dp(32)],dp(16),BRAND)
            txt(d,(bx+(tw-dp(8))//2,ty+dp(16)),t,dp(12),WHITE,True,'mm')
        else:
            txt(d,(bx+tw//2,ty+dp(16)),t,dp(12),(200,192,218),False,'mm')

    # exp banner
    by=ty+dp(48)
    g=vgrad((W-dp(28),dp(52)),(60,40,110),(120,60,140)).convert('RGBA')
    mask=Image.new('L',g.size,0); ImageDraw.Draw(mask).rounded_rectangle([0,0,g.size[0]-1,g.size[1]-1],radius=dp(14),fill=255)
    im.paste(g,(dp(14),by),mask)
    d=ImageDraw.Draw(im)
    txt(d,(dp(34),by+dp(26)),'Naikkan EXP room, buka hadiah harian',dp(12),WHITE,False,'lm')
    txt(d,(W-dp(36),by+dp(26)),'Buka',dp(12),(255,212,116),True,'rm')

    # announcement
    ay=by+dp(68)
    rr(d,[dp(14),ay,W-dp(14),ay+dp(34)],dp(17),(26,21,38,190))
    txt(d,(dp(34),ay+dp(17)),'Selamat datang di room VenderLive',dp(11),(205,198,222),False,'lm')

    # bottom bar
    bar_y=H-dp(96)
    rr(d,[dp(14),bar_y,W-dp(14),bar_y+dp(72)],dp(36),(20,16,30,225))
    labels=[('room_ic_emoji','Emoji'),('room_ic_chat','Chat'),('room_ic_gift','Hadiah'),('room_ic_game','Game')]
    for i,(ic,lb) in enumerate(labels):
        cx=dp(46)+i*dp(70)
        d.ellipse([cx-dp(22),bar_y+dp(24)-dp(22),cx+dp(22),bar_y+dp(24)+dp(22)], fill=(42,33,62))
        icon(im,ip(ic),cx,bar_y+dp(24),dp(20),(240,236,250))
        txt(d,(cx,bar_y+dp(56)),lb,dp(9),(198,190,215),False,'ma')
    # message input above the bar
    iy=bar_y-dp(56)
    rr(d,[dp(14),iy,W-dp(14),iy+dp(44)],dp(22),(26,21,38,215))
    txt(d,(dp(34),iy+dp(22)),'Ketik pesan...',dp(12),(160,152,182),False,'lm')
    icon(im,ip('room_ic_emoji'),W-dp(38),iy+dp(22),dp(20),(200,192,218))
    # right action cluster
    for i,(ic,lb) in enumerate([('room_ic_volume','Speaker'),('room_ic_bottom_mic_mute','Mic')]):
        cx=W-dp(48)-i*dp(0); cy=bar_y+dp(12)-dp(52)-i*dp(78)
        d.ellipse([cx-dp(26),cy-dp(26),cx+dp(26),cy+dp(26)], fill=(32,25,50))
        icon(im,ip(ic),cx,cy,dp(24),(240,236,250))
        txt(d,(cx,cy+dp(38)),lb,dp(9),(198,190,215),False,'ma')

    # side rail
    for i,(ic,lb) in enumerate([('room_ic_vault','Rank'),('room_ic_trophy','Peringkat')]):
        cx=W-dp(34); cy=H//2-dp(40)+i*dp(78)
        d.ellipse([cx-dp(24),cy-dp(24),cx+dp(24),cy+dp(24)], fill=(26,21,38))
        icon(im,ip(ic),cx,cy,dp(22),(255,212,116) if i==0 else (240,236,250))
        txt(d,(cx,cy+dp(34)),lb,dp(8),(198,190,215),False,'ma')
    return im

# ---------------- PROFILE ----------------
def profile():
    im=Image.new('RGBA',(W,H),DARK)
    head=vgrad((W,dp(250)),(20,120,120),(16,72,96)).convert('RGBA')
    im.paste(head,(0,0))
    d=ImageDraw.Draw(im)
    txt(d,(dp(20),dp(30)),'Aku',dp(20),WHITE,True,'la')
    icon(im,ip("room_ic_grid_menu"),W-dp(30),dp(38),dp(22),(240,236,250))
    avatar(d,W//2,dp(110),dp(40),(24,50,70))
    txt(d,(W//2,dp(110)),'BE',dp(24),WHITE,True,'mm')
    txt(d,(W//2,dp(172)),'BANG EX',dp(18),WHITE,True,'mm')
    txt(d,(W//2,dp(198)),'ID: 613583246',dp(12),(220,235,240),False,'mm')
    # stats
    stats=[('Teman-teman','12'),('Mengikuti','8'),('Pengikut','24'),('Pengunjung','156')]
    for i,(lb,v) in enumerate(stats):
        cx=int(W/4*(i+0.5))
        txt(d,(cx,dp(228)),v,dp(15),WHITE,True,'mm')
        txt(d,(cx,dp(246)),lb,dp(10),(225,240,245),False,'mm')
    # menu cards 3x3
    items=[('ic_wallet','Dompet'),('ic_svip','SVIP'),('ic_noble','Bangsawan'),
           ('ic_level','Level'),('ic_medal','Medali'),('ic_relationship','Hubungan'),
           ('ic_family','Keluarga'),('ic_guard','Penjaga'),('ic_cp','CP')]
    gw=(W-dp(40)-dp(24)*2)//3
    gy=dp(290)
    for i,(ic,lb) in enumerate(items):
        r_,c_=divmod(i,3)
        bx=dp(20)+c_*(gw+dp(24)); by=gy+r_*(dp(110)+dp(16))
        rr(d,[bx,by,bx+gw,by+dp(110)],dp(16),CARD)
        icon(im,ip(ic),bx+gw//2,by+dp(40),dp(34),(255,255,255))
        txt(d,(bx+gw//2,by+dp(84)),lb,dp(12),WHITE,True,'ma')
    ly=gy+3*(dp(110)+dp(16))+dp(16)
    rr(d,[dp(20),ly,W-dp(20),ly+dp(52)],dp(26),(38,26,40))
    txt(d,(W//2,ly+dp(26)),'Keluar',dp(14),(255,120,140),True,'mm')
    # bottom tab
    tab_y=H-dp(72)
    d.rectangle([0,tab_y,W,H], fill=(18,14,28))
    tabs=[('home_ic_tab_home','Beranda'),('home_ic_tab_game','Permainan'),('home_ic_tab_moment','Momen'),('home_ic_tab_msg','Pesan'),('home_ic_tab_mine','Aku')]
    for i,(ic,lb) in enumerate(tabs):
        cx=int(W/5*(i+0.5))
        col=(255,255,255) if lb=='Aku' else (150,142,175)
        icon(im,ip(ic),cx,tab_y+dp(24),dp(24),col)
        txt(d,(cx,tab_y+dp(48)),lb,dp(10),col,False,'ma')
    return im

for name,fn in (('login',login),('room',room),('profile',profile)):
    out=fn()
    out.convert('RGB').resize((720,1560), Image.LANCZOS).save(f'art/ui/{name}.png')
    print('ok',name)

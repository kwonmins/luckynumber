from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import math

ROOT = Path(r"C:\Users\USER\Downloads\unum_local_10k_chunked_app_bundle\unum_local_10k_chunked_app")
ASSET_DIR = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
OUT_DIR = ROOT / "design_previews"
OUT_DIR.mkdir(exist_ok=True)
OUT_PATH = OUT_DIR / "app_flow_8x2_storyboard.png"

W, H = 3200, 1880
canvas = Image.new("RGBA", (W, H), (246, 249, 252, 255))
d = ImageDraw.Draw(canvas)


def font(size, bold=False):
    path = Path(r"C:\Windows\Fonts\malgunbd.ttf" if bold else r"C:\Windows\Fonts\malgun.ttf")
    if not path.exists():
        path = Path(r"C:\Windows\Fonts\arial.ttf")
    return ImageFont.truetype(str(path), size)


F_TITLE = font(54, True)
F_SUB = font(24)
F_STEP = font(20, True)
F_H = font(24, True)
F_M = font(18)
F_S = font(15)
F_XS = font(13)

COL = {
    "ink": (15, 23, 42, 255),
    "muted": (96, 112, 134, 255),
    "line": (205, 216, 230, 255),
    "blue": (74, 115, 201, 255),
    "gold": (210, 166, 68, 255),
    "rose": (229, 120, 136, 255),
    "mint": (73, 172, 128, 255),
    "paper": (255, 255, 255, 255),
    "dark": (22, 29, 40, 255),
}


def load_asset(name, max_size):
    img = Image.open(ASSET_DIR / name).convert("RGBA")
    img.thumbnail(max_size, Image.Resampling.LANCZOS)
    return img


assets = {
    "love": load_asset("numerology_love.png", (150, 150)),
    "career": load_asset("numerology_career.png", (150, 150)),
    "money": load_asset("numerology_money.png", (150, 150)),
    "study": load_asset("numerology_study.png", (150, 150)),
    "self": load_asset("numerology_self.png", (140, 140)),
}


def rr(draw, box, r, fill, outline=None, width=1):
    draw.rounded_rectangle(box, radius=r, fill=fill, outline=outline, width=width)


def text_wrap(draw, x, y, s, fnt, fill, max_width, line_gap=4, align="left"):
    words = list(s) if " " not in s else s.split(" ")
    lines, line = [], ""
    sep = "" if " " not in s else " "
    for word in words:
        test = word if not line else line + sep + word
        if draw.textlength(test, font=fnt) <= max_width:
            line = test
        else:
            if line:
                lines.append(line)
            line = word
    if line:
        lines.append(line)
    for line in lines:
        lx = x
        if align == "center":
            lx = x + max_width / 2 - draw.textlength(line, font=fnt) / 2
        draw.text((lx, y), line, font=fnt, fill=fill)
        y += fnt.size + line_gap
    return y


def paste_center(base, img, box):
    x, y, w, h = box
    px = int(x + (w - img.width) / 2)
    py = int(y + (h - img.height) / 2)
    base.alpha_composite(img, (px, py))


def phone_shadow(x, y, w, h):
    sh = Image.new("RGBA", (w + 44, h + 44), (0, 0, 0, 0))
    sd = ImageDraw.Draw(sh)
    sd.rounded_rectangle((22, 22, w + 22, h + 22), radius=36, fill=(15, 23, 42, 34))
    sh = sh.filter(ImageFilter.GaussianBlur(14))
    canvas.alpha_composite(sh, (x - 22, y - 16))


def draw_phone(i, title):
    col = i % 8
    row = i // 8
    phone_w, phone_h = 340, 680
    gap_x = 38
    start_x = 56
    start_y = 150
    gap_y = 120
    x = start_x + col * (phone_w + gap_x)
    y = start_y + row * (phone_h + gap_y)
    phone_shadow(x, y, phone_w, phone_h)
    rr(d, (x, y, x + phone_w, y + phone_h), 34, COL["paper"], COL["line"], 2)
    rr(d, (x + 18, y + 18, x + phone_w - 18, y + phone_h - 18), 24, (255, 255, 255, 255), None)
    d.text((x + 22, y - 34), f"{i + 1:02d}. {title}", font=F_STEP, fill=COL["ink"])
    d.text((x + 34, y + 34), "U", font=F_H, fill=COL["ink"])
    return x, y, phone_w, phone_h


def draw_wave_numbers(x, y, w, h):
    for n in range(28):
        xx = x + 40 + (n * 43) % (w - 80)
        yy = y + 95 + int(math.sin(n * 0.8) * 24) + (n * 19) % (h - 190)
        val = str((n * 7 + 3) % 10)
        d.text((xx, yy), val, font=F_XS, fill=(74, 115, 201, 55))


def draw_card(x, y, w, h, title, subtitle, tint, icon=None, active=False):
    sh = Image.new("RGBA", (int(w) + 22, int(h) + 22), (0, 0, 0, 0))
    sd = ImageDraw.Draw(sh)
    sd.rounded_rectangle((11, 11, w + 11, h + 11), radius=20, fill=(15, 23, 42, 24 if active else 12))
    sh = sh.filter(ImageFilter.GaussianBlur(7 if active else 2))
    canvas.alpha_composite(sh, (int(x) - 11, int(y) - 8))
    rr(d, (x, y, x + w, y + h), 22, (255, 255, 255, 255), (190, 205, 224, 255), 2 if active else 1)
    rr(d, (x + 12, y + 12, x + w - 12, y + 92), 16, tint, None)
    if icon:
        paste_center(canvas, icon, (x + 28, y + 22, w - 56, 70))
    d.text((x + w / 2, y + 108), title, font=F_H, fill=COL["ink"], anchor="mm")
    d.text((x + w / 2, y + 136), subtitle, font=F_XS, fill=COL["muted"], anchor="mm")


def draw_book(x, y, w, h, title="운세노트", tint=(255, 251, 242, 255), dark=False):
    fill = (83, 44, 57, 255) if dark else tint
    line = (210, 166, 68, 255)
    rr(d, (x, y, x + w, y + h), 14, fill, line, 2)
    d.line((x + 18, y + 20, x + 18, y + h - 20), fill=line, width=2)
    d.line((x + w - 18, y + 20, x + w - 18, y + h - 20), fill=line, width=1)
    d.text((x + w / 2, y + 40), "U", font=F_H, fill=line, anchor="mm")
    d.multiline_text(
        (x + w / 2, y + h / 2),
        title,
        font=F_M,
        fill=(255, 255, 255, 255) if dark else COL["ink"],
        anchor="mm",
        align="center",
        spacing=6,
    )
    d.text((x + w / 2, y + h - 38), "number note", font=F_XS, fill=line, anchor="mm")


d.text((56, 42), "앱 실행부터 결과까지 8 x 2 플로우", font=F_TITLE, fill=COL["ink"])
d.text(
    (58, 105),
    "홈 → 입력 → AI 생성 → 책자 결과 → 저장/재열람까지, 현재 리디자인 방향을 한 장으로 정리했습니다.",
    font=F_SUB,
    fill=COL["muted"],
)

# 01
x, y, w, h = draw_phone(0, "실행")
draw_wave_numbers(x, y, w, h)
d.text((x + w / 2, y + 230), "U", font=font(74, True), fill=COL["ink"], anchor="mm")
d.text((x + w / 2, y + 304), "수리 운세", font=F_H, fill=COL["ink"], anchor="mm")
d.text((x + w / 2, y + 340), "quiet number ritual", font=F_M, fill=COL["muted"], anchor="mm")
for r, a in [(130, 35), (92, 40), (56, 60)]:
    d.ellipse((x + w / 2 - r, y + 276 - r, x + w / 2 + r, y + 276 + r), outline=(74, 115, 201, a), width=2)

# 02
x, y, w, h = draw_phone(1, "홈 히어로")
rr(d, (x + 28, y + 86, x + w - 28, y + 520), 30, (250, 252, 255, 255), (220, 229, 240, 255), 2)
d.text((x + w / 2, y + 135), "your number room", font=F_H, fill=COL["ink"], anchor="mm")
d.text((x + w / 2, y + 166), "오늘의 숫자 파동을 여는 방", font=F_S, fill=COL["muted"], anchor="mm")
d.ellipse((x + 98, y + 220, x + 242, y + 364), fill=(255, 255, 255, 240), outline=(210, 221, 234, 255), width=2)
d.text((x + w / 2, y + 254), "DESTINY", font=F_XS, fill=COL["muted"], anchor="mm")
d.text((x + w / 2, y + 305), "7", font=font(56, True), fill=COL["blue"], anchor="mm")
paste_center(canvas, assets["self"], (x + 210, y + 302, 90, 100))
for tx, ty, t in [(54, 210, "birth"), (222, 210, "destiny"), (56, 390, "wave"), (228, 390, "note")]:
    rr(d, (x + tx, y + ty, x + tx + 72, y + ty + 30), 15, (255, 255, 255, 220), COL["line"], 1)
    d.text((x + tx + 36, y + ty + 15), t, font=F_XS, fill=COL["muted"], anchor="mm")
rr(d, (x + 54, y + 552, x + w - 54, y + 608), 28, COL["ink"], None)
d.text((x + w / 2, y + 580), "number reading start", font=F_M, fill=(255, 255, 255, 255), anchor="mm")

# 03
x, y, w, h = draw_phone(2, "방 메뉴")
d.text((x + 32, y + 92), "your numerology room", font=F_H, fill=COL["ink"])
entries = [("birth number", "생년월일 리딩", COL["blue"]), ("fortune note", "운세노트", COL["gold"]), ("my room", "보관함", COL["mint"])]
for idx, (a, b, c) in enumerate(entries):
    yy = y + 150 + idx * 140
    rr(d, (x + 34, yy, x + w - 34, yy + 110), 22, (255, 255, 255, 255), COL["line"], 1)
    d.ellipse((x + 54, yy + 28, x + 106, yy + 80), fill=(c[0], c[1], c[2], 32), outline=(c[0], c[1], c[2], 120), width=1)
    d.text((x + 130, yy + 28), a, font=F_S, fill=COL["muted"])
    d.text((x + 130, yy + 58), b, font=F_H, fill=COL["ink"])
    d.text((x + w - 70, yy + 58), "start", font=F_XS, fill=COL["muted"])

# 04
x, y, w, h = draw_phone(3, "카드 선택")
d.text((x + 32, y + 92), "number cards", font=F_H, fill=COL["ink"])
d.text((x + 32, y + 124), "중앙 카드가 살짝 떠오르는 자기장 효과", font=F_XS, fill=COL["muted"])
draw_card(x + 18, y + 180, 94, 226, "연애", "open", (255, 241, 242, 255), assets["love"], False)
draw_card(x + 122, y + 156, 114, 260, "이직", "active", (239, 246, 255, 255), assets["career"], True)
draw_card(x + 248, y + 180, 94, 226, "금전", "open", (236, 253, 245, 255), assets["money"], False)
rr(d, (x + 62, y + 486, x + w - 62, y + 542), 28, (255, 255, 255, 255), COL["line"], 1)
d.text((x + w / 2, y + 514), "선택 주제로 운세노트 이동", font=F_S, fill=COL["ink"], anchor="mm")

# 05
x, y, w, h = draw_phone(4, "입력")
d.text((x + 32, y + 92), "무엇을 알고 싶나요?", font=F_H, fill=COL["ink"])
for i, (label, c) in enumerate([("연애", COL["rose"]), ("일", COL["blue"]), ("돈", COL["mint"]), ("나", COL["gold"])]):
    bx = x + 32 + i * 72
    by = y + 142
    rr(d, (bx, by, bx + 62, by + 42), 20, (c[0], c[1], c[2], 32), (c[0], c[1], c[2], 120), 1)
    d.text((bx + 31, by + 21), label, font=F_S, fill=COL["ink"], anchor="mm")
for i, label in enumerate(["생년월일", "태어난 시간", "고민 한 줄"]):
    yy = y + 230 + i * 88
    rr(d, (x + 34, yy, x + w - 34, yy + 56), 14, (255, 255, 255, 255), COL["line"], 1)
    d.text((x + 54, yy + 18), label, font=F_S, fill=COL["muted"])
rr(d, (x + 42, y + 544, x + w - 42, y + 600), 28, COL["blue"], None)
d.text((x + w / 2, y + 572), "다음", font=F_M, fill=(255, 255, 255, 255), anchor="mm")

# 06
x, y, w, h = draw_phone(5, "질문 조립")
d.text((x + 32, y + 92), "나만의 운세노트 재료", font=F_H, fill=COL["ink"])
parts = [("분야", "이직"), ("고민", "전환의 타이밍"), ("생년월일", "1994.08.12")]
for i, (a, b) in enumerate(parts):
    yy = y + 160 + i * 92
    rr(d, (x + 48, yy, x + w - 48, yy + 64), 18, (255, 255, 255, 255), COL["line"], 1)
    d.text((x + 70, yy + 14), a, font=F_XS, fill=COL["muted"])
    d.text((x + 70, yy + 36), b, font=F_M, fill=COL["ink"])
    d.ellipse((x + w - 86, yy + 18, x + w - 58, yy + 46), fill=(74, 115, 201, 40), outline=(74, 115, 201, 140), width=1)
rr(d, (x + 72, y + 484, x + w - 72, y + 548), 24, (248, 250, 252, 255), COL["line"], 1)
d.text((x + w / 2, y + 516), "질문 카드 완성", font=F_H, fill=COL["ink"], anchor="mm")

# 07
x, y, w, h = draw_phone(6, "질문 확인")
d.text((x + 32, y + 92), "수리가 잡은 핵심 질문", font=F_H, fill=COL["ink"])
rr(d, (x + 34, y + 160, x + w - 34, y + 386), 26, (255, 255, 255, 255), COL["line"], 2)
d.text((x + 60, y + 198), "Q.", font=font(32, True), fill=COL["blue"])
text_wrap(d, x + 60, y + 250, "지금 이직을 준비해도 괜찮은 흐름인가요?", F_H, COL["ink"], w - 120, 8)
d.text((x + 60, y + 340), "질문의 방향을 확인하면 책자 생성으로 넘어갑니다.", font=F_XS, fill=COL["muted"])
rr(d, (x + 42, y + 520, x + w - 42, y + 578), 28, COL["ink"], None)
d.text((x + w / 2, y + 549), "이 질문으로 생성", font=F_M, fill=(255, 255, 255, 255), anchor="mm")

# 08
x, y, w, h = draw_phone(7, "생성 중")
d.text((x + 32, y + 92), "AI / 수리 상담 연출", font=F_H, fill=COL["ink"])
paste_center(canvas, assets["study"], (x + 112, y + 130, 116, 120))
stages = ["수리가 숫자를 해석 중", "고민의 질문을 정리 중", "프리미엄 책자 제본 중"]
for i, s in enumerate(stages):
    yy = y + 302 + i * 70
    d.ellipse((x + 50, yy + 8, x + 82, yy + 40), fill=(74, 115, 201, 35), outline=(74, 115, 201, 120), width=1)
    d.text((x + 66, yy + 24), str(i + 1), font=F_XS, fill=COL["blue"], anchor="mm")
    d.text((x + 98, yy + 14), s, font=F_M, fill=COL["ink"])
rr(d, (x + 54, y + 548, x + w - 54, y + 586), 18, (239, 246, 255, 255), None)
d.rectangle((x + 54, y + 548, x + 228, y + 586), fill=(74, 115, 201, 255))
d.text((x + w / 2, y + 567), "book binding 72%", font=F_XS, fill=COL["ink"], anchor="mm")

# 09
x, y, w, h = draw_phone(8, "표지 등장")
d.text((x + 32, y + 92), "책 표지 등장", font=F_H, fill=COL["ink"])
draw_book(x + 78, y + 150, 184, 300, "이직\n운세노트", dark=True)
for r, a in [(150, 36), (190, 24), (230, 12)]:
    d.ellipse((x + w / 2 - r, y + 300 - r, x + w / 2 + r, y + 300 + r), outline=(210, 166, 68, a), width=2)
d.text((x + w / 2, y + 510), "금박 라인 → 제목 → 리본 순서로 등장", font=F_XS, fill=COL["muted"], anchor="mm")

# 10
x, y, w, h = draw_phone(9, "목차")
d.text((x + 32, y + 92), "contents", font=F_H, fill=COL["ink"])
for i, ch in enumerate(["1. 현재 흐름", "2. 숨은 패턴", "3. 추천 시기", "4. 조언과 행동"]):
    yy = y + 154 + i * 86
    rr(d, (x + 36, yy, x + w - 36, yy + 58), 16, (255, 255, 255, 255), COL["line"], 1)
    d.text((x + 58, yy + 17), ch, font=F_M, fill=COL["ink"])
    d.text((x + w - 70, yy + 17), "open", font=F_XS, fill=COL["muted"])
d.text((x + w / 2, y + 548), "누르면 장 카드가 확대되어 이동", font=F_XS, fill=COL["muted"], anchor="mm")

# 11
x, y, w, h = draw_phone(10, "페이지 넘김")
d.text((x + 32, y + 92), "book reader", font=F_H, fill=COL["ink"])
rr(d, (x + 44, y + 150, x + 164, y + 478), 14, (255, 252, 246, 255), (222, 208, 182, 255), 1)
rr(d, (x + 164, y + 150, x + 296, y + 478), 14, (255, 252, 246, 255), (222, 208, 182, 255), 1)
d.line((x + 164, y + 154, x + 164, y + 474), fill=(180, 160, 130, 255), width=2)
d.polygon([(x + 236, y + 150), (x + 296, y + 174), (x + 296, y + 478), (x + 236, y + 450)], fill=(244, 235, 219, 255), outline=(222, 208, 182, 255))
d.text((x + 74, y + 192), "현재 흐름", font=F_M, fill=COL["ink"])
text_wrap(d, x + 74, y + 236, "변화 욕구가 커지는 시기입니다.", F_S, COL["muted"], 82)
d.text((x + 192, y + 210), "다음 장", font=F_M, fill=COL["ink"])
d.text((x + w / 2, y + 540), "종이 그림자와 가장자리 하이라이트", font=F_XS, fill=COL["muted"], anchor="mm")

# 12
x, y, w, h = draw_phone(11, "상세 해석")
d.text((x + 32, y + 92), "문장 순차 등장", font=F_H, fill=COL["ink"])
blocks = [("핵심 문장", "지금은 방향을 바꾸기 전 기준을 세울 때입니다."), ("이유", "숫자 흐름은 확장보다 정리에 힘을 줍니다."), ("조언", "오늘은 조건 3가지를 먼저 적어보세요.")]
for i, (a, b) in enumerate(blocks):
    yy = y + 154 + i * 122
    rr(d, (x + 34, yy, x + w - 34, yy + 88), 18, (255, 255, 255, 255), COL["line"], 1)
    d.text((x + 56, yy + 14), a, font=F_XS, fill=COL["blue"])
    text_wrap(d, x + 56, yy + 40, b, F_S, COL["ink"], w - 112, 4)
d.text((x + w / 2, y + 556), "0.2~0.4초 간격으로 상담처럼 표시", font=F_XS, fill=COL["muted"], anchor="mm")

# 13
x, y, w, h = draw_phone(12, "시기 분석")
d.text((x + 32, y + 92), "추천 / 주의 월", font=F_H, fill=COL["ink"])
for i, (m, label, c) in enumerate([("8월", "추천", COL["mint"]), ("11월", "주의", COL["rose"])]):
    yy = y + 152 + i * 118
    rr(d, (x + 44, yy, x + w - 44, yy + 84), 18, (255, 255, 255, 255), COL["line"], 1)
    d.text((x + 68, yy + 20), m, font=F_H, fill=COL["ink"])
    rr(d, (x + w - 116, yy + 24, x + w - 56, yy + 54), 15, (c[0], c[1], c[2], 35), (c[0], c[1], c[2], 130), 1)
    d.text((x + w - 86, yy + 39), label, font=F_XS, fill=COL["ink"], anchor="mm")
pts = [(x + 58, y + 450), (x + 110, y + 410), (x + 162, y + 430), (x + 214, y + 370), (x + 270, y + 398)]
d.line(pts, fill=COL["blue"], width=3)
for px, py in pts:
    d.ellipse((px - 5, py - 5, px + 5, py + 5), fill=COL["blue"])
d.text((x + w / 2, y + 536), "월별 흐름을 책자 안에서 확인", font=F_XS, fill=COL["muted"], anchor="mm")

# 14
x, y, w, h = draw_phone(13, "액션 플랜")
d.text((x + 32, y + 92), "오늘의 행동", font=F_H, fill=COL["ink"])
for i, t in enumerate(["이직 조건 3가지 적기", "지원 마감일 하나 확인", "지금 직장의 소진 원인 기록"]):
    yy = y + 160 + i * 94
    rr(d, (x + 34, yy, x + w - 34, yy + 62), 16, (255, 255, 255, 255), COL["line"], 1)
    d.ellipse((x + 54, yy + 19, x + 78, yy + 43), outline=COL["blue"], width=2)
    if i == 0:
        d.line((x + 59, yy + 31, x + 66, yy + 38, x + 76, yy + 23), fill=COL["blue"], width=3)
    d.text((x + 94, yy + 19), t, font=F_S, fill=COL["ink"])
rr(d, (x + 52, y + 510, x + w - 52, y + 568), 28, (236, 253, 245, 255), None)
d.text((x + w / 2, y + 539), "작은 실행으로 흐름 고정", font=F_M, fill=COL["ink"], anchor="mm")

# 15
x, y, w, h = draw_phone(14, "저장")
d.text((x + 32, y + 92), "saved notes", font=F_H, fill=COL["ink"])
d.text((x + 32, y + 122), "책자가 방에 쌓입니다", font=F_XS, fill=COL["muted"])
for i, (tx, dy) in enumerate([("이직", 0), ("연애", 18), ("금전", 36)]):
    draw_book(x + 56 + i * 76, y + 200 + dy, 72, 148, tx, dark=True)
d.line((x + 44, y + 410, x + w - 44, y + 410), fill=(200, 210, 222, 255), width=3)
paste_center(canvas, assets["self"], (x + 210, y + 412, 82, 90))
rr(d, (x + 52, y + 540, x + w - 52, y + 590), 25, (255, 255, 255, 255), COL["line"], 1)
d.text((x + w / 2, y + 565), "보관함에서 다시 열기", font=F_S, fill=COL["ink"], anchor="mm")

# 16
x, y, w, h = draw_phone(15, "결과 완료")
d.text((x + 32, y + 92), "완성된 운세노트", font=F_H, fill=COL["ink"])
draw_book(x + 92, y + 146, 156, 238, "프리미엄\n책자", dark=True)
paste_center(canvas, assets["love"], (x + 64, y + 396, 90, 100))
rr(d, (x + 150, y + 414, x + w - 42, y + 466), 18, (255, 255, 255, 255), COL["line"], 1)
d.text((x + 244, y + 440), "결과 읽기", font=F_M, fill=COL["ink"], anchor="mm")
rr(d, (x + 42, y + 520, x + 158, y + 574), 26, (239, 246, 255, 255), None)
d.text((x + 100, y + 547), "공유", font=F_S, fill=COL["ink"], anchor="mm")
rr(d, (x + 178, y + 520, x + w - 42, y + 574), 26, COL["ink"], None)
d.text((x + 258, y + 547), "다시 보기", font=F_S, fill=(255, 255, 255, 255), anchor="mm")

arrow_color = (148, 163, 184, 180)
for row in range(2):
    y_arrow = 150 + row * (680 + 120) + 340
    for col in range(7):
        x1 = 56 + col * (340 + 38) + 342
        x2 = x1 + 30
        d.line((x1, y_arrow, x2, y_arrow), fill=arrow_color, width=3)
        d.polygon([(x2, y_arrow), (x2 - 9, y_arrow - 6), (x2 - 9, y_arrow + 6)], fill=arrow_color)

canvas.convert("RGB").save(OUT_PATH, quality=95)
print(OUT_PATH)

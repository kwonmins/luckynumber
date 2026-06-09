from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "design_previews" / "code_audit_8x2_status_board.png"
ASSET_DIR = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"

W, H = 3200, 1700
MARGIN_X = 54
TOP = 178
GAP = 18
COLS = 8
ROWS = 2
CARD_W = (W - MARGIN_X * 2 - GAP * (COLS - 1)) // COLS
CARD_H = (H - TOP - 58 - GAP) // ROWS


def font(size, bold=False):
    candidates = [
        Path("C:/Windows/Fonts/malgunbd.ttf" if bold else "C:/Windows/Fonts/malgun.ttf"),
        Path("C:/Windows/Fonts/NotoSansKR-Regular.otf"),
        Path("C:/Windows/Fonts/arial.ttf"),
    ]
    for path in candidates:
        if path.exists():
            return ImageFont.truetype(str(path), size)
    return ImageFont.load_default()


FONT_TITLE = font(56, True)
FONT_SUB = font(26)
FONT_PANEL_TITLE = font(28, True)
FONT_BODY = font(21)
FONT_SMALL = font(18)
FONT_BADGE = font(18, True)
FONT_MONO = font(18)
FONT_BIG = font(72, True)


COLORS = {
    "bg": (248, 250, 252),
    "ink": (15, 23, 42),
    "muted": (100, 116, 139),
    "line": (226, 232, 240),
    "card": (255, 255, 255),
    "blue": (37, 99, 235),
    "gold": (245, 158, 11),
    "green": (16, 185, 129),
    "rose": (239, 68, 68),
    "paper": (255, 253, 248),
    "cover": (30, 41, 59),
}


def lerp(a, b, t):
    return int(a + (b - a) * t)


def rounded(draw, xy, fill, outline=None, width=1, radius=24):
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)


def text(draw, xy, value, fill=COLORS["ink"], fnt=FONT_BODY, anchor=None, align="left"):
    draw.text(xy, value, fill=fill, font=fnt, anchor=anchor, align=align)


def wrap_lines(draw, value, fnt, max_w):
    words = value.split()
    lines = []
    current = ""
    for word in words:
        test = word if not current else current + " " + word
        if draw.textlength(test, font=fnt) <= max_w:
            current = test
        else:
            if current:
                lines.append(current)
            current = word
    if current:
        lines.append(current)
    return lines


def draw_wrapped(draw, xy, value, fnt, fill, max_w, line_h):
    x, y = xy
    for line in wrap_lines(draw, value, fnt, max_w):
        text(draw, (x, y), line, fill, fnt)
        y += line_h
    return y


def draw_badge(draw, x, y, label, kind):
    palette = {
        "actual": ((219, 234, 254), COLORS["blue"], "실제 반영"),
        "partial": ((254, 243, 199), COLORS["gold"], "부분 반영"),
        "note": ((241, 245, 249), COLORS["muted"], "주의"),
    }
    fill, fg, default = palette[kind]
    label = label or default
    tw = int(draw.textlength(label, font=FONT_BADGE))
    rounded(draw, (x, y, x + tw + 22, y + 30), fill, fg, 1, 999)
    text(draw, (x + 11, y + 4), label, fg, FONT_BADGE)


def draw_phone(draw, x, y, w, h, fill=(255, 255, 255), outline=(203, 213, 225)):
    rounded(draw, (x, y, x + w, y + h), fill, outline, 2, 34)
    rounded(draw, (x + 10, y + 10, x + w - 10, y + h - 10), fill, (241, 245, 249), 1, 26)


def paste_asset(canvas, name, box):
    path = ASSET_DIR / name
    if not path.exists():
        return
    asset = Image.open(path).convert("RGBA")
    bw = box[2] - box[0]
    bh = box[3] - box[1]
    asset.thumbnail((bw, bh), Image.Resampling.LANCZOS)
    px = box[0] + (bw - asset.width) // 2
    py = box[1] + (bh - asset.height) // 2
    canvas.alpha_composite(asset, (px, py))


def draw_panel(canvas, draw, idx, title, status, body, file_ref, painter):
    col = idx % COLS
    row = idx // COLS
    x = MARGIN_X + col * (CARD_W + GAP)
    y = TOP + row * (CARD_H + GAP)

    shadow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    sd = ImageDraw.Draw(shadow)
    rounded(sd, (x + 5, y + 8, x + CARD_W + 5, y + CARD_H + 8), (15, 23, 42, 25), None, 0, 24)
    canvas.alpha_composite(shadow)

    rounded(draw, (x, y, x + CARD_W, y + CARD_H), COLORS["card"], COLORS["line"], 2, 24)
    draw_badge(draw, x + 20, y + 18, None, status)
    text(draw, (x + CARD_W - 22, y + 20), f"{idx + 1:02}", COLORS["muted"], FONT_BADGE, anchor="ra")
    text(draw, (x + 20, y + 58), title, COLORS["ink"], FONT_PANEL_TITLE)

    art_x, art_y = x + 20, y + 106
    art_w, art_h = CARD_W - 40, 260
    rounded(draw, (art_x, art_y, art_x + art_w, art_y + art_h), (248, 250, 252), (226, 232, 240), 1, 18)
    painter(draw, canvas, art_x, art_y, art_w, art_h)

    by = draw_wrapped(draw, (x + 20, y + 386), body, FONT_BODY, COLORS["ink"], CARD_W - 40, 30)
    if file_ref:
        rounded(draw, (x + 20, y + CARD_H - 52, x + CARD_W - 20, y + CARD_H - 18), (241, 245, 249), (226, 232, 240), 1, 10)
        text(draw, (x + 32, y + CARD_H - 44), file_ref, COLORS["muted"], FONT_SMALL)


def art_home(draw, canvas, x, y, w, h):
    draw_phone(draw, x + 68, y + 18, w - 136, h - 36)
    text(draw, (x + w // 2, y + 54), "your number room", COLORS["ink"], FONT_SMALL, anchor="mm")
    draw.ellipse((x + w // 2 - 48, y + 92, x + w // 2 + 48, y + 188), fill=(255, 255, 255), outline=(203, 213, 225), width=2)
    text(draw, (x + w // 2, y + 136), "7", COLORS["blue"], FONT_BIG, anchor="mm")
    paste_asset(canvas, "numerology_self.png", (x + w - 120, y + 142, x + w - 30, y + 242))


def art_wave(draw, canvas, x, y, w, h):
    for i in range(4):
        yy = y + 64 + i * 42
        pts = []
        for k in range(44):
            xx = x + 22 + k * (w - 44) / 43
            pts.append((xx, yy + 16 * __import__("math").sin(k / 4 + i)))
        draw.line(pts, fill=(37, 99, 235, 90), width=3)
    for i, n in enumerate(["1", "3", "5", "7", "9", "11"]):
        text(draw, (x + 46 + i * 48, y + 176 + (i % 2) * 20), n, (148, 163, 184), FONT_PANEL_TITLE)
    text(draw, (x + w // 2, y + 236), "홈은 현재 OFF", COLORS["gold"], FONT_BADGE, anchor="mm")


def art_cards(draw, canvas, x, y, w, h):
    names = ["numerology_love.png", "numerology_career.png", "numerology_money.png"]
    labels = ["연애", "이직", "금전"]
    for i in range(3):
        cx = x + 22 + i * 92
        cy = y + 32 - (10 if i == 1 else 0)
        rounded(draw, (cx, cy, cx + 82, cy + 198), (255, 255, 255), (203, 213, 225), 2, 16)
        rounded(draw, (cx + 7, cy + 9, cx + 75, cy + 105), (239, 246, 255), None, 0, 12)
        paste_asset(canvas, names[i], (cx + 7, cy + 14, cx + 75, cy + 112))
        text(draw, (cx + 41, cy + 128), labels[i], COLORS["ink"], FONT_SMALL, anchor="mm")
        text(draw, (cx + 41, cy + 158), "선택", COLORS["blue"] if i == 1 else COLORS["muted"], FONT_SMALL, anchor="mm")


def art_input(draw, canvas, x, y, w, h):
    labels = [("01", "기준"), ("02", "성별"), ("03", "생년"), ("04", "월일"), ("05", "조립")]
    for i, item in enumerate(labels):
        yy = y + 24 + i * 43
        rounded(draw, (x + 28, yy, x + w - 28, yy + 34), (219, 234, 254) if i < 4 else (254, 243, 199), (191, 219, 254), 1, 10)
        text(draw, (x + 44, yy + 7), item[0], COLORS["blue"], FONT_BADGE)
        text(draw, (x + 96, yy + 7), item[1], COLORS["ink"], FONT_BADGE)
    rounded(draw, (x + 34, y + 234, x + w - 34, y + 242), (37, 99, 235), None, 0, 999)


def art_question(draw, canvas, x, y, w, h):
    rounded(draw, (x + 34, y + 38, x + w - 34, y + 122), (255, 255, 255), (37, 99, 235), 2, 16)
    text(draw, (x + w // 2, y + 72), "질문 확인", COLORS["blue"], FONT_PANEL_TITLE, anchor="mm")
    draw_wrapped(draw, (x + 54, y + 130), "수리가 고민을 한 문장으로 압축", FONT_BODY, COLORS["ink"], w - 108, 28)
    rounded(draw, (x + 56, y + 214, x + w - 56, y + 248), COLORS["blue"], None, 0, 999)
    text(draw, (x + w // 2, y + 221), "제책하기", (255, 255, 255), FONT_BADGE, anchor="ma")


def art_loading(draw, canvas, x, y, w, h):
    for i, rot in enumerate([-9, 6, 0]):
        px = x + w // 2 - 36 + i * 8
        py = y + 50 + i * 12
        rounded(draw, (px, py, px + 72, py + 96), COLORS["paper"], (230, 218, 201), 2, 8)
    stages = ["숫자 해석", "질문 정리", "책자 제본"]
    for i, s in enumerate(stages):
        rounded(draw, (x + 42, y + 170 + i * 34, x + w - 42, y + 196 + i * 34), (241, 245, 249), (226, 232, 240), 1, 999)
        text(draw, (x + w // 2, y + 174 + i * 34), s, COLORS["ink"], FONT_SMALL, anchor="ma")


def art_cover(draw, canvas, x, y, w, h):
    bx, by = x + 82, y + 16
    bw, bh = w - 164, h - 32
    rounded(draw, (bx, by, bx + bw, by + bh), COLORS["cover"], (245, 158, 11), 2, 10)
    rounded(draw, (bx + 14, by + 14, bx + bw - 14, by + bh - 14), COLORS["cover"], (245, 158, 11), 1, 8)
    text(draw, (bx + bw // 2, by + 62), "수리의", (253, 230, 138), FONT_PANEL_TITLE, anchor="mm")
    text(draw, (bx + bw // 2, by + 105), "운세노트", (253, 230, 138), FONT_PANEL_TITLE, anchor="mm")
    draw.ellipse((bx + bw // 2 - 34, by + 140, bx + bw // 2 + 34, by + 208), outline=(253, 230, 138), width=2)


def art_toc(draw, canvas, x, y, w, h):
    rounded(draw, (x + 54, y + 20, x + w - 54, y + h - 20), COLORS["paper"], (230, 218, 201), 2, 10)
    text(draw, (x + w // 2, y + 52), "목차", COLORS["ink"], FONT_PANEL_TITLE, anchor="mm")
    for i, s in enumerate(["지혜의 본질", "상황별 해석", "주의할 장면", "행동 지침"]):
        yy = y + 92 + i * 42
        rounded(draw, (x + 76, yy, x + w - 76, yy + 32), (239, 246, 255) if i == 1 else (255, 255, 255), (226, 232, 240), 1, 8)
        text(draw, (x + 92, yy + 7), s, COLORS["ink"], FONT_SMALL)


def art_detail(draw, canvas, x, y, w, h):
    labels = ["던지신 질문", "주의할 장면", "이번 주 실천"]
    for i, s in enumerate(labels):
        yy = y + 42 + i * 66
        rounded(draw, (x + 44, yy, x + w - 44, yy + 52), (255, 255, 255), (226, 232, 240), 1, 12)
        draw.rectangle((x + 44, yy, x + 50, yy + 52), fill=[COLORS["blue"], COLORS["rose"], COLORS["gold"]][i])
        text(draw, (x + 62, yy + 10), s, COLORS["ink"], FONT_BADGE)


def art_reader(draw, canvas, x, y, w, h):
    for i in range(3):
        px = x + 48 + i * 52
        skew = 14 if i == 1 else 0
        rounded(draw, (px, y + 36 + skew, px + 112, y + 224 - skew), COLORS["paper"], (230, 218, 201), 2, 12)
    text(draw, (x + w // 2, y + 236), "rotationY page flip", COLORS["blue"], FONT_BADGE, anchor="mm")


def art_library(draw, canvas, x, y, w, h):
    for i in range(3):
        yy = y + 32 + i * 68
        rounded(draw, (x + 44, yy, x + w - 44, yy + 52), (255, 255, 255), (226, 232, 240), 1, 12)
        rounded(draw, (x + 58, yy + 10, x + 94, yy + 42), COLORS["cover"], (245, 158, 11), 1, 6)
        text(draw, (x + 108, yy + 12), ["연애 노트", "돈의 흐름", "나 자신"][i], COLORS["ink"], FONT_SMALL)


def art_alpha(draw, canvas, x, y, w, h):
    # checker grid only in a small corner, then transparent character over a white card.
    for gx in range(0, 80, 16):
        for gy in range(0, 80, 16):
            fill = (229, 231, 235) if (gx // 16 + gy // 16) % 2 == 0 else (255, 255, 255)
            draw.rectangle((x + 22 + gx, y + 22 + gy, x + 38 + gx, y + 38 + gy), fill=fill)
    paste_asset(canvas, "numerology_love.png", (x + 88, y + 20, x + w - 38, y + h - 18))
    text(draw, (x + 68, y + 120), "alpha 0~255", COLORS["green"], FONT_BADGE, anchor="mm")


def art_note(draw, canvas, x, y, w, h):
    text(draw, (x + w // 2, y + 74), "8x2 이미지는", COLORS["ink"], FONT_PANEL_TITLE, anchor="mm")
    text(draw, (x + w // 2, y + 116), "실제 캡처가 아닌", COLORS["gold"], FONT_PANEL_TITLE, anchor="mm")
    text(draw, (x + w // 2, y + 158), "코드 기준 설명도", COLORS["ink"], FONT_PANEL_TITLE, anchor="mm")
    rounded(draw, (x + 54, y + 210, x + w - 54, y + 244), (254, 243, 199), COLORS["gold"], 1, 999)
    text(draw, (x + w // 2, y + 218), "실앱 캡처는 별도 필요", COLORS["gold"], FONT_BADGE, anchor="ma")


panels = [
    ("앱 시작 홈", "actual", "홈 구조는 흰 배경의 수리 운세 방으로 바뀌었습니다.", "HomeScreen.kt", art_home),
    ("숫자 파동 배경", "partial", "파동 코드는 있지만 홈에서는 꺼져 있고 입력/온보딩에서 켜집니다.", "CommonComponents.kt", art_wave),
    ("방 메뉴 카드", "actual", "생년월일, 운세노트, 보관함 카드가 가로 흐름으로 배치됩니다.", "HomeScreen.kt", art_cards),
    ("숫자 카드 선택", "actual", "연애, 이직, 금전, 학업, 자아 카드가 실제 PNG와 연결됩니다.", "HomeScreen.kt", art_cards),
    ("자기장 효과", "actual", "중앙/선택 카드가 scale, lift, shadow, alpha로 떠오릅니다.", "HomeScreen.kt", art_cards),
    ("입력 조립", "actual", "입력값이 채워질수록 운세노트 재료 카드가 하나씩 완성됩니다.", "InputScreen.kt", art_input),
    ("질문 확인", "actual", "고민을 핵심 질문으로 압축해 확인하는 단계가 있습니다.", "PremiumScreen.kt", art_question),
    ("생성 중 연출", "actual", "숫자 해석, 질문 정리, 책자 제본 3단계 애니메이션입니다.", "PremiumScreen.kt", art_loading),
    ("표지 등장", "actual", "표지가 scale, alpha, 금박 라인으로 등장합니다.", "PremiumScreen.kt", art_cover),
    ("목차 이동", "actual", "목차 항목을 누르면 상세 해석으로 이어집니다.", "PremiumScreen.kt", art_toc),
    ("상세 해석", "actual", "질문, 주의 장면, 실천 지침이 책 페이지처럼 배치됩니다.", "PremiumScreen.kt", art_detail),
    ("책장 넘김", "actual", "Reader는 HorizontalPager와 rotationY로 페이지를 넘깁니다.", "PremiumComponents.kt", art_reader),
    ("보관함 저장", "actual", "생성된 책자는 saveNewBook을 통해 보관함에 쌓입니다.", "AppViewModel.kt", art_library),
    ("캐릭터 투명", "actual", "새 numerology PNG들은 실제 alpha 투명 픽셀을 갖습니다.", "drawable-nodpi", art_alpha),
    ("실제 캡처 여부", "note", "지난 8x2는 실제 앱 화면 캡처가 아니라 구현 흐름 설명도입니다.", "design_previews", art_note),
    ("남은 차이", "partial", "홈 파동 ON, 실제 기기 캡처, 문장 순차등장은 추가 확인/보강 대상입니다.", "next pass", art_wave),
]


def main():
    canvas = Image.new("RGBA", (W, H), COLORS["bg"] + (255,))
    draw = ImageDraw.Draw(canvas)

    for yy in range(H):
        t = yy / H
        color = (
            lerp(248, 255, t),
            lerp(250, 253, t),
            lerp(252, 248, t),
            255,
        )
        draw.line((0, yy, W, yy), fill=color)

    draw.ellipse((-220, -180, 720, 600), fill=(219, 234, 254, 120))
    draw.ellipse((W - 760, 80, W + 260, 840), fill=(254, 243, 199, 110))
    draw.ellipse((980, H - 520, 1900, H + 220), fill=(220, 252, 231, 100))

    text(draw, (MARGIN_X, 42), "코드 기준 실제 반영 상태", COLORS["ink"], FONT_TITLE)
    text(
        draw,
        (MARGIN_X, 110),
        "8x2 스토리보드를 다시 검증한 결과입니다. 파란 배지는 실제 코드 반영, 노란 배지는 부분 반영/추가 확인 대상입니다.",
        COLORS["muted"],
        FONT_SUB,
    )
    draw_badge(draw, W - 420, 54, "BUILD SUCCESS", "actual")
    text(draw, (W - 54, 58), ":app:assembleDebug", COLORS["muted"], FONT_SMALL, anchor="ra")

    for idx, panel in enumerate(panels):
        draw_panel(canvas, draw, idx, *panel)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    canvas.convert("RGB").save(OUT, quality=95)
    print(OUT)


if __name__ == "__main__":
    main()

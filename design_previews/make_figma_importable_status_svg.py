from html import escape
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "design_previews" / "figma_importable_code_status_board.svg"

W, H = 3200, 1700
MX, TOP, GAP = 54, 178, 18
COLS, ROWS = 8, 2
CW = (W - MX * 2 - GAP * (COLS - 1)) // COLS
CH = (H - TOP - 58 - GAP) // ROWS


def tspan_lines(text, max_chars=17):
    words = text.split()
    lines, cur = [], ""
    for word in words:
        test = word if not cur else cur + " " + word
        if len(test) <= max_chars:
            cur = test
        else:
            if cur:
                lines.append(cur)
            cur = word
    if cur:
        lines.append(cur)
    return lines


def svg_text(x, y, value, size=22, weight=400, color="#0f172a", anchor="start", lines=False, max_chars=17):
    if not lines:
        return f'<text x="{x}" y="{y}" fill="{color}" font-family="Malgun Gothic, Inter, Arial" font-size="{size}" font-weight="{weight}" text-anchor="{anchor}">{escape(value)}</text>'
    out = [f'<text x="{x}" y="{y}" fill="{color}" font-family="Malgun Gothic, Inter, Arial" font-size="{size}" font-weight="{weight}" text-anchor="{anchor}">']
    for i, line in enumerate(tspan_lines(value, max_chars)):
        dy = 0 if i == 0 else size * 1.38
        out.append(f'<tspan x="{x}" dy="{dy}">{escape(line)}</tspan>')
    out.append("</text>")
    return "\n".join(out)


def badge(x, y, label, kind):
    colors = {
        "actual": ("#dbeafe", "#2563eb", "실제 반영"),
        "partial": ("#fef3c7", "#f59e0b", "부분 반영"),
        "note": ("#f1f5f9", "#64748b", "주의"),
    }
    fill, stroke, default = colors[kind]
    label = label or default
    width = 78 if len(label) <= 4 else 122
    return f'''
<rect x="{x}" y="{y}" width="{width}" height="30" rx="15" fill="{fill}" stroke="{stroke}" stroke-width="1"/>
{svg_text(x + width / 2, y + 21, label, 16, 700, stroke, "middle")}
'''


def mini_phone(x, y, w, h):
    return f'''
<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="32" fill="#ffffff" stroke="#cbd5e1" stroke-width="2"/>
<rect x="{x+10}" y="{y+10}" width="{w-20}" height="{h-20}" rx="24" fill="#f8fafc" stroke="#e2e8f0"/>
'''


def art(kind, x, y, w, h):
    cx = x + w / 2
    if kind == "home":
        return f'''
{mini_phone(x+68, y+18, w-136, h-36)}
{svg_text(cx, y+55, "your number room", 16, 400, "#64748b", "middle")}
<circle cx="{cx}" cy="{y+142}" r="48" fill="#fff" stroke="#cbd5e1" stroke-width="2"/>
{svg_text(cx, y+164, "7", 72, 700, "#2563eb", "middle")}
<circle cx="{x+w-74}" cy="{y+184}" r="34" fill="#fdebd2" stroke="#8b5e3c" stroke-width="2"/>
<rect x="{x+w-102}" y="{y+218}" width="56" height="46" rx="12" fill="#1e293b"/>
'''
    if kind == "wave":
        paths = []
        for i in range(4):
            yy = y + 64 + i * 42
            d = f"M {x+24} {yy}"
            for k in range(1, 9):
                px = x + 24 + k * (w - 48) / 8
                py = yy + (18 if k % 2 else -12)
                d += f" Q {px-20} {py} {px} {yy}"
            paths.append(f'<path d="{d}" fill="none" stroke="#2563eb" stroke-width="3" opacity=".72"/>')
        nums = "".join(svg_text(x+54+i*44, y+204+(i%2)*20, n, 28, 700, "#94a3b8", "middle") for i, n in enumerate(["1","3","5","7","9","11"]))
        return "\n".join(paths) + nums + svg_text(cx, y+254, "홈은 현재 OFF", 18, 700, "#f59e0b", "middle")
    if kind == "cards":
        s = []
        labels = ["연애", "이직", "금전"]
        colors = ["#fecdd3", "#dbeafe", "#dcfce7"]
        for i, label in enumerate(labels):
            px = x + 22 + i * 92
            py = y + 32 - (10 if i == 1 else 0)
            s.append(f'<rect x="{px}" y="{py}" width="82" height="198" rx="16" fill="#fff" stroke="#cbd5e1" stroke-width="2"/>')
            s.append(f'<rect x="{px+8}" y="{py+10}" width="66" height="96" rx="12" fill="{colors[i]}"/>')
            s.append(f'<circle cx="{px+41}" cy="{py+52}" r="24" fill="#fdebd2" stroke="#8b5e3c" stroke-width="2"/>')
            s.append(svg_text(px+41, py+136, label, 18, 700, "#0f172a", "middle"))
            s.append(svg_text(px+41, py+166, "선택", 16, 400, "#2563eb" if i == 1 else "#64748b", "middle"))
        return "\n".join(s)
    if kind == "input":
        labels = [("01", "기준"), ("02", "성별"), ("03", "생년"), ("04", "월일"), ("05", "조립")]
        s = []
        for i, (num, label) in enumerate(labels):
            yy = y + 24 + i * 43
            fill = "#dbeafe" if i < 4 else "#fef3c7"
            s.append(f'<rect x="{x+28}" y="{yy}" width="{w-56}" height="34" rx="9" fill="{fill}" stroke="#bfdbfe"/>')
            s.append(svg_text(x+46, yy+23, num, 16, 700, "#2563eb"))
            s.append(svg_text(x+96, yy+23, label, 16, 700, "#0f172a"))
        s.append(f'<rect x="{x+34}" y="{y+234}" width="{w-68}" height="8" rx="4" fill="#2563eb"/>')
        return "\n".join(s)
    if kind == "question":
        return f'''
<rect x="{x+34}" y="{y+38}" width="{w-68}" height="84" rx="16" fill="#fff" stroke="#2563eb" stroke-width="2"/>
{svg_text(cx, y+88, "질문 확인", 30, 700, "#2563eb", "middle")}
{svg_text(cx, y+156, "수리가 고민을 한 문장으로 압축", 21, 400, "#0f172a", "middle")}
<rect x="{x+56}" y="{y+214}" width="{w-112}" height="36" rx="18" fill="#2563eb"/>
{svg_text(cx, y+238, "제책하기", 18, 700, "#fff", "middle")}
'''
    if kind == "loading":
        return f'''
<rect x="{cx-52}" y="{y+44}" width="72" height="96" rx="8" fill="#fffdf8" stroke="#e6dac9" transform="rotate(-8 {cx-16} {y+92})"/>
<rect x="{cx-24}" y="{y+50}" width="72" height="96" rx="8" fill="#fffdf8" stroke="#e6dac9" transform="rotate(6 {cx+12} {y+98})"/>
<rect x="{cx-36}" y="{y+62}" width="72" height="96" rx="8" fill="#fffdf8" stroke="#e6dac9"/>
<rect x="{x+42}" y="{y+170}" width="{w-84}" height="26" rx="13" fill="#f1f5f9"/>
<rect x="{x+42}" y="{y+204}" width="{w-84}" height="26" rx="13" fill="#f1f5f9"/>
<rect x="{x+42}" y="{y+238}" width="{w-84}" height="26" rx="13" fill="#f1f5f9"/>
{svg_text(cx, y+189, "숫자 해석", 16, 400, "#0f172a", "middle")}
{svg_text(cx, y+223, "질문 정리", 16, 400, "#0f172a", "middle")}
{svg_text(cx, y+257, "책자 제본", 16, 400, "#0f172a", "middle")}
'''
    if kind == "cover":
        bx, by, bw, bh = x + 82, y + 18, w - 164, h - 36
        return f'''
<rect x="{bx}" y="{by}" width="{bw}" height="{bh}" rx="10" fill="#1e293b" stroke="#f59e0b" stroke-width="2"/>
<rect x="{bx+14}" y="{by+14}" width="{bw-28}" height="{bh-28}" rx="8" fill="none" stroke="#f59e0b"/>
{svg_text(cx, by+74, "수리의", 28, 700, "#fde68a", "middle")}
{svg_text(cx, by+118, "운세노트", 28, 700, "#fde68a", "middle")}
<circle cx="{cx}" cy="{by+178}" r="34" fill="none" stroke="#fde68a" stroke-width="2"/>
'''
    if kind == "toc":
        s = [f'<rect x="{x+54}" y="{y+20}" width="{w-108}" height="{h-40}" rx="10" fill="#fffdf8" stroke="#e6dac9" stroke-width="2"/>',
             svg_text(cx, y+58, "목차", 30, 700, "#0f172a", "middle")]
        for i, label in enumerate(["지혜의 본질", "상황별 해석", "주의할 장면", "행동 지침"]):
            yy = y + 92 + i * 42
            fill = "#eff6ff" if i == 1 else "#fff"
            s.append(f'<rect x="{x+76}" y="{yy}" width="{w-152}" height="32" rx="8" fill="{fill}" stroke="#e2e8f0"/>')
            s.append(svg_text(x+92, yy+22, label, 16, 400, "#0f172a"))
        return "\n".join(s)
    if kind == "detail":
        s = []
        labels = [("던지신 질문", "#2563eb"), ("주의할 장면", "#ef4444"), ("이번 주 실천", "#f59e0b")]
        for i, (label, col) in enumerate(labels):
            yy = y + 42 + i * 66
            s.append(f'<rect x="{x+44}" y="{yy}" width="{w-88}" height="52" rx="12" fill="#fff" stroke="#e2e8f0"/>')
            s.append(f'<rect x="{x+44}" y="{yy}" width="6" height="52" rx="3" fill="{col}"/>')
            s.append(svg_text(x+62, yy+32, label, 18, 700, "#0f172a"))
        return "\n".join(s)
    if kind == "reader":
        return f'''
<rect x="{x+48}" y="{y+52}" width="112" height="188" rx="12" fill="#fffdf8" stroke="#e6dac9" stroke-width="2"/>
<rect x="{x+104}" y="{y+38}" width="112" height="204" rx="12" fill="#fffdf8" stroke="#e6dac9" stroke-width="2" opacity=".86"/>
<rect x="{x+164}" y="{y+52}" width="112" height="188" rx="12" fill="#fffdf8" stroke="#e6dac9" stroke-width="2"/>
{svg_text(cx, y+260, "rotationY page flip", 17, 700, "#2563eb", "middle")}
'''
    if kind == "library":
        s = []
        for i, label in enumerate(["연애 노트", "돈의 흐름", "나 자신"]):
            yy = y + 32 + i * 68
            s.append(f'<rect x="{x+44}" y="{yy}" width="{w-88}" height="52" rx="12" fill="#fff" stroke="#e2e8f0"/>')
            s.append(f'<rect x="{x+58}" y="{yy+10}" width="36" height="32" rx="6" fill="#1e293b" stroke="#f59e0b"/>')
            s.append(svg_text(x+108, yy+32, label, 16, 400, "#0f172a"))
        return "\n".join(s)
    if kind == "alpha":
        checker = []
        for gx in range(0, 80, 16):
            for gy in range(0, 80, 16):
                fill = "#e5e7eb" if (gx // 16 + gy // 16) % 2 == 0 else "#ffffff"
                checker.append(f'<rect x="{x+22+gx}" y="{y+22+gy}" width="16" height="16" fill="{fill}"/>')
        return "\n".join(checker) + f'''
<circle cx="{x+w-110}" cy="{y+92}" r="42" fill="#fdebd2" stroke="#8b5e3c" stroke-width="2"/>
<rect x="{x+w-146}" y="{y+134}" width="72" height="78" rx="16" fill="#1e293b"/>
{svg_text(x+66, y+138, "alpha 0~255", 18, 700, "#10b981", "middle")}
'''
    if kind == "note":
        return f'''
{svg_text(cx, y+78, "8x2 이미지는", 26, 700, "#0f172a", "middle")}
{svg_text(cx, y+120, "실제 캡처가 아닌", 26, 700, "#f59e0b", "middle")}
{svg_text(cx, y+162, "코드 기준 설명도", 26, 700, "#0f172a", "middle")}
<rect x="{x+54}" y="{y+210}" width="{w-108}" height="36" rx="18" fill="#fef3c7" stroke="#f59e0b"/>
{svg_text(cx, y+234, "실앱 캡처는 별도 필요", 16, 700, "#f59e0b", "middle")}
'''
    return ""


panels = [
    ("앱 시작 홈", "actual", "홈 구조는 흰 배경의 수리 운세 방으로 바뀌었습니다.", "HomeScreen.kt", "home"),
    ("숫자 파동 배경", "partial", "파동 코드는 있지만 홈에서는 꺼져 있고 입력/온보딩에서 켜집니다.", "CommonComponents.kt", "wave"),
    ("방 메뉴 카드", "actual", "생년월일, 운세노트, 보관함 카드가 가로 흐름으로 배치됩니다.", "HomeScreen.kt", "cards"),
    ("숫자 카드 선택", "actual", "연애, 이직, 금전, 학업, 자아 카드가 실제 PNG와 연결됩니다.", "HomeScreen.kt", "cards"),
    ("자기장 효과", "actual", "중앙/선택 카드가 scale, lift, shadow, alpha로 떠오릅니다.", "HomeScreen.kt", "cards"),
    ("입력 조립", "actual", "입력값이 채워질수록 운세노트 재료 카드가 하나씩 완성됩니다.", "InputScreen.kt", "input"),
    ("질문 확인", "actual", "고민을 핵심 질문으로 압축해 확인하는 단계가 있습니다.", "PremiumScreen.kt", "question"),
    ("생성 중 연출", "actual", "숫자 해석, 질문 정리, 책자 제본 3단계 애니메이션입니다.", "PremiumScreen.kt", "loading"),
    ("표지 등장", "actual", "표지가 scale, alpha, 금박 라인으로 등장합니다.", "PremiumScreen.kt", "cover"),
    ("목차 이동", "actual", "목차 항목을 누르면 상세 해석으로 이어집니다.", "PremiumScreen.kt", "toc"),
    ("상세 해석", "actual", "질문, 주의 장면, 실천 지침이 책 페이지처럼 배치됩니다.", "PremiumScreen.kt", "detail"),
    ("책장 넘김", "actual", "Reader는 HorizontalPager와 rotationY로 페이지를 넘깁니다.", "PremiumComponents.kt", "reader"),
    ("보관함 저장", "actual", "생성된 책자는 saveNewBook을 통해 보관함에 쌓입니다.", "AppViewModel.kt", "library"),
    ("캐릭터 투명", "actual", "새 numerology PNG들은 실제 alpha 투명 픽셀을 갖습니다.", "drawable-nodpi", "alpha"),
    ("실제 캡처 여부", "note", "지난 8x2는 실제 앱 화면 캡처가 아니라 구현 흐름 설명도입니다.", "design_previews", "note"),
    ("남은 차이", "partial", "홈 파동 ON, 실제 기기 캡처, 문장 순차등장은 추가 확인/보강 대상입니다.", "next pass", "wave"),
]


def panel_svg(i, title, status, body, ref, kind):
    col, row = i % COLS, i // COLS
    x, y = MX + col * (CW + GAP), TOP + row * (CH + GAP)
    ax, ay, aw, ah = x + 20, y + 106, CW - 40, 260
    parts = [
        f'<rect x="{x+5}" y="{y+8}" width="{CW}" height="{CH}" rx="24" fill="#0f172a" opacity=".10"/>',
        f'<rect x="{x}" y="{y}" width="{CW}" height="{CH}" rx="24" fill="#fff" stroke="#e2e8f0" stroke-width="2"/>',
        badge(x+20, y+18, None, status),
        svg_text(x+CW-22, y+40, f"{i+1:02}", 16, 700, "#94a3b8", "end"),
        svg_text(x+20, y+83, title, 28, 700, "#0f172a"),
        f'<rect x="{ax}" y="{ay}" width="{aw}" height="{ah}" rx="18" fill="#f8fafc" stroke="#e2e8f0"/>',
        art(kind, ax, ay, aw, ah),
        svg_text(x+20, y+410, body, 21, 400, "#0f172a", "start", True, 20),
        f'<rect x="{x+20}" y="{y+CH-52}" width="{CW-40}" height="34" rx="10" fill="#f1f5f9" stroke="#e2e8f0"/>',
        svg_text(x+32, y+CH-30, ref, 17, 400, "#94a3b8"),
    ]
    return "\n".join(parts)


def main():
    panels_svg = "\n".join(panel_svg(i, *p) for i, p in enumerate(panels))
    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">
<defs>
  <linearGradient id="bg" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="#f8fafc"/>
    <stop offset="1" stop-color="#fffdf8"/>
  </linearGradient>
</defs>
<rect width="{W}" height="{H}" fill="url(#bg)"/>
<circle cx="120" cy="60" r="600" fill="#dbeafe" opacity=".75"/>
<circle cx="2940" cy="360" r="560" fill="#fef3c7" opacity=".76"/>
<circle cx="1450" cy="1620" r="500" fill="#dcfce7" opacity=".62"/>
{svg_text(MX, 88, "코드 기준 실제 반영 상태", 56, 700, "#0f172a")}
{svg_text(MX, 136, "Figma 커넥터 인증이 막혀서 만든 Figma import용 SVG 보드입니다. 파란 배지는 실제 코드 반영, 노란 배지는 부분 반영/추가 확인 대상입니다.", 25, 400, "#64748b")}
{badge(W-440, 54, "BUILD SUCCESS", "actual")}
{svg_text(W-54, 78, ":app:assembleDebug", 17, 400, "#94a3b8", "end")}
{panels_svg}
</svg>'''
    OUT.write_text(svg, encoding="utf-8")
    print(OUT)


if __name__ == "__main__":
    main()

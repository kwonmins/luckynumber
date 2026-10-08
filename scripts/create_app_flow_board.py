"""Build a reviewable app storyboard from screenshots and explicit screen mockups."""
from pathlib import Path
from base64 import b64encode
from html import escape
import json

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'output' / 'suri-redesign-2026-10-06'
OUT.mkdir(parents=True, exist_ok=True)
W, H = 2300, 3220
parts = []
def add(s): parts.append(s)
def rect(x,y,w,h,fill='#fffcf8',r=16,stroke='#e8dfe7'):
    add(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" fill="{fill}" stroke="{stroke}"/>')
def text(x,y,s,size=19,color='#47436b',weight=400):
    add(f'<text x="{x}" y="{y}" font-size="{size}" fill="{color}" font-weight="{weight}">{escape(s)}</text>')
def lines(x,y,ls,size=17,color='#767085',gap=27):
    for i,line in enumerate(ls):text(x,y+i*gap,line,size,color)
def image_uri(file):
    return 'data:image/png;base64,'+b64encode(file.read_bytes()).decode()
def arrow(x1,y1,x2,y2,dashed=False):
    add(f'<path d="M{x1} {y1} L{x2} {y2}" fill="none" stroke="#a896bc" stroke-width="2" marker-end="url(#arrow)"'+(' stroke-dasharray="6 5"' if dashed else '')+'/>')
def button(x,y,label,filled=True):
    rect(x,y,248,43,'url(#button)' if filled else '#fffcf8',22)
    text(x+18,y+28,label,16,'#fffaf5' if filled else '#65557f')
def garden(x,y,w=248,h=115):
    rect(x,y,w,h,'url(#garden)',14)
    add(f'<circle cx="{x+42}" cy="{y+30}" r="18" fill="#f8d8a5"/><circle cx="{x+50}" cy="{y+25}" r="16" fill="#e7dfed"/>')
    add(f'<path d="M{x} {y+h} L{x} {y+h*.75} L{x+50} {y+h*.64} L{x+100} {y+h*.84} L{x+175} {y+h*.68} L{x+w} {y+h*.82} L{x+w} {y+h}Z" fill="#bcb1c8" opacity=".5"/>')
    add(f'<path d="M{x+w-5} {y+h} L{x+w-28} {y+10}" stroke="#b59baa" stroke-width="2"/>')
    for j in range(5):
        cx,cy=x+w-25+(j%2)*10,y+18+j*17
        for dx,dy in [(0,-4),(4,0),(0,4),(-4,0)]:add(f'<circle cx="{cx+dx}" cy="{cy+dy}" r="4" fill="#edbcc9"/>')
def phone(col,row,title,caption,screenshot=None,mock=None):
    x=85+col*365;y=285+row*820
    text(x,y-42,title,23,weight=600)
    badge='앱 캡처' if screenshot else '화면 모형'
    text(x+210,y-42,badge,14,'#88749e')
    rect(x,y,300,620,'#fffcf8',29,'#c6bad1')
    add(f'<clipPath id="clip{col}-{row}"><rect x="{x+5}" y="{y+5}" width="290" height="610" rx="25"/></clipPath>')
    if screenshot:
        uri=image_uri(ROOT/'design_previews'/'pastel_redesign'/screenshot)
        add(f'<image href="{uri}" x="{x+5}" y="{y+5}" width="290" height="610" preserveAspectRatio="xMidYMid slice" clip-path="url(#clip{col}-{row})"/>')
    else:
        text(x+21,y+29,'9:41',12)
        text(x+235,y+29,'● ▰',12)
        if mock:mock(x+26,y+64)
    lines(x,y+650,caption,size=17)
    return x,y
def mock_input(x,y):
    text(x,y,'생년월일 입력',23,weight=500); garden(x,y+23)
    text(x+14,y+80,'당신의 이야기를 들려주세요',15)
    for i,(a,b) in enumerate([('달력','양력    /    음력'),('성별','남성    여성    무관'),('생년월일','1999     03     13')]):
        text(x,y+170+i*76,a,17);rect(x,y+185+i*76,248,43,'#f3edf5',14);text(x+14,y+212+i*76,b,16)
    button(x,y+435,'나의 수리 확인하기')
def mock_popup(x,y):
    text(x,y,'무료 운세 · 애정운',23);garden(x,y+35)
    rect(x,y+175,248,214,'#fffcf8',20);lines(x+17,y+210,['오늘의 관계 흐름','서두르기보다 작은 관심을','차분히 전해보세요.'],18,gap=34)
    button(x,y+421,'확인')
def mock_confirm(x,y):
    text(x,y,'이 질문이 맞나요?',23);lines(x,y+43,['궁금한 이야기를','이렇게 정리했어요.'],17)
    rect(x,y+112,248,197,'#f3edf5',18);lines(x+16,y+150,['수리에게 물어볼 질문','학업과 시험에 관한 고민을','어떻게 준비하면 좋을까요?'],17,gap=39)
    button(x,y+408,'네, 이 질문이 맞아요');button(x,y+465,'고민 다시 수정하기',False)
def mock_login(x,y):
    text(x,y,'마이 · 계정',23);garden(x,y+28);text(x,y+185,'나의 기록을 이어가세요',20)
    lines(x,y+227,['상담 생성 전 로그인이 필요해요.','기록과 책자를 계정에 보관해요.'],16)
    button(x,y+323,'카카오로 로그인');lines(x,y+415,['로그인 후 질문으로 돌아가','이용 절차를 계속 진행해요.'],16)
def mock_payment(x,y):
    text(x,y,'프리미엄 시작하기',22)
    for i,(label,price) in enumerate([('월간 구독','₩4,900'),('단건 구매','₩2,900')]):
        rect(x,y+53+i*108,248,90,'#f3edf5' if i==0 else '#fffcf8',18);text(x+16,y+88+i*108,label,18);text(x+16,y+118+i*108,price,19,'#88749e')
    lines(x,y+305,['맞춤 상담 · 상세 결과','책자 저장 · 다시 읽기'],17)
    button(x,y+431,'선택한 이용 방법으로 진행')
    text(x,y+510,'현재 UI 단계 · 결제 연동 별도',14,'#ac7182')
def mock_loading(x,y):
    text(x,y,'수리가 이야기를 읽는 중',21);garden(x,y+40)
    text(x+55,y+239,'✦  ·  ✦',37,'#b6a1c4');lines(x+20,y+315,['숫자 흐름을 해석하고','고민에 맞는 답을 정리해요.'],18)
    rect(x,y+430,248,7,'#eee6f3',4);rect(x,y+430,152,7,'#a491b9',4)
    text(x,y+485,'실패 시 질문 화면으로 복귀',15,'#ac7182')
def mock_reader(x,y):
    text(x,y,'운세 결과       ♡  ↗',23);garden(x,y+32)
    text(x,y+182,'당신에게 전하는 이야기',19);lines(x,y+219,['지금의 고민을 천천히 읽고,','좋은 흐름을 만들어가세요.'],17)
    rect(x,y+287,248,173,'#f3edf5',18);lines(x+15,y+321,['01 · 현재의 흐름','02 · 기회와 주의점','03 · 현실적인 행동'],17,gap=43)
    text(x,y+510,'스크랩 · 글자 크기 · PDF 공유',14,'#88749e')
def mock_library(x,y):
    text(x,y,'나의 보관함',23);text(x,y+41,'전체   운세노트   궁합노트',16,'#88749e')
    for i in range(2):
        yy=y+76+i*171;rect(x,yy,248,149,'#fffcf8',18);garden(x+8,yy+8,232,60);text(x+15,yy+96,['나의 학업 이야기','두 사람의 관계 이야기'][i],18);text(x+15,yy+126,'다시 읽기    ·    스크랩',15,'#88749e')
    text(x,y+479,'필터 · 정렬 · 삭제',17)
def mock_share(x,y):
    text(x,y,'PDF 저장 및 공유',22);garden(x,y+30)
    rect(x+37,y+185,172,198,'#fffcf8',10);text(x+57,y+225,'수리의 운세노트',17)
    for j in range(5):add(f'<path d="M{x+57} {y+258+j*19}h132" stroke="#ddd0e2"/>')
    button(x,y+431,'Android 공유 창 열기')
def mock_settings(x,y):
    text(x,y,'마이페이지',23);rect(x,y+35,248,95,'#f3edf5',18);text(x+17,y+74,'나의 프로필',19);text(x+17,y+108,'생년월일 · 나의 숫자',15)
    for i,l in enumerate(['계정 · 로그인 / 로그아웃','책자 동기화','알림 받기','글자 크기 설정','개발자 이야기']):
        rect(x,y+158+i*64,248,53,'#fffcf8',13);text(x+14,y+192+i*64,l,17)
def mock_error(x,y):
    text(x,y,'다시 시도할 수 있어요',22);rect(x,y+78,248,160,'#faedf0',18);lines(x+18,y+118,['입력 날짜를 확인해주세요.','연결이 잠시 불안정해요.','상담 요청에 실패했어요.'],17,gap=38)
    button(x,y+315,'입력 수정 / 다시 시도');lines(x,y+410,['기존 저장 결과는 보관함에서','계속 읽을 수 있어요.'],16)

add(f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">')
add('''<defs><linearGradient id="button"><stop stop-color="#a18bb5"/><stop offset="1" stop-color="#78658f"/></linearGradient><linearGradient id="garden"><stop stop-color="#e6dfed"/><stop offset="1" stop-color="#fbe6da"/></linearGradient><marker id="arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0 0L10 5L0 10" fill="#a896bc"/></marker></defs>''')
add('<g font-family="Malgun Gothic, sans-serif">')
rect(0,0,W,H,'#fff9f4',0,'none')
text(85,68,'수리운세 · 전체 앱 플로우 시안',43,weight=600)
text(85,113,'크림 · 연보라 · 달과 벚꽃 · 승인된 수리 캐릭터',23,'#88749e')
text(85,153,'실제 캡처 + 코드 기준 화면 모형  |  2026.10.06  |  실선: 주 경로 / 점선: 조건·선택 경로',18,'#767085')

text(85,200,'01  시작과 무료 운세',23,'#88749e',600)
phone(0,0,'시작',['게스트 / 카카오 시작','저장된 입력이 있으면 홈으로'],screenshot='onboarding.png')
phone(1,0,'홈',['오늘 배너 · 무료 6개 · 프리미엄','하단 메뉴로 언제든 이동'],screenshot='home.png')
phone(2,0,'생년월일 입력',['입력이 없을 때만 필요','양력/음력 · 성별 · 날짜'],mock=mock_input)
phone(3,0,'무료 결과',['핵심 수리 · 생애 흐름','무료 결과는 다시 생성'],screenshot='result.png')
phone(4,0,'분야 메시지',['입력 이후 홈의 무료 카드 선택','총운·애정·금전·직장·건강·행운'],mock=mock_popup)
phone(5,0,'공통 이동',['홈 / 운세 / 스크랩 / 프리미엄 / 마이','예비 알림 안내는 자동 진입 없음'],mock=mock_settings)
for a,b in [(0,1),(1,2),(2,3)]:arrow(85+a*365+304,590,85+b*365-8,590)
arrow(85+3*365+304,590,85+4*365-8,590,True)

text(85,1020,'02  프리미엄 · 분야 선택과 질문',23,'#88749e',600)
phone(0,1,'일반 분야',['학업 · 재물 · 시험','기존 추가 분야도 유지'],screenshot='premium.png')
phone(1,1,'연애 분야',['짝사랑 · 커플 · 재회','상대 생년월일도 입력'],screenshot='romance.png')
phone(2,1,'질문 입력',['직접 입력 / 추천 질문','내 정보가 없으면 여기서 확인'],screenshot='question.png')
phone(3,1,'질문 확인',['질문을 확인하거나 수정','확인 후 로그인 상태 확인'],mock=mock_confirm)
phone(4,1,'계정 확인',['미로그인 → 마이페이지','로그인 상태면 바로 이용 화면'],mock=mock_login)
phone(5,1,'이용 방법',['현재 결제 UI 모형','실결제 검증은 포함하지 않음'],mock=mock_payment)
arrow(85+365+304,1410,85+2*365-8,1410)
text(85,1808,'일반 또는 연애 분야 선택 → 질문 입력 → 질문 확인 → 로그인 확인 → 이용 방법',18,'#88749e')
for a,b in [(2,3),(3,4),(4,5)]:arrow(85+a*365+304,1410,85+b*365-8,1410,a in (3,4))

text(85,1840,'03  생성 결과 · 저장 · 공유 · 설정',23,'#88749e',600)
phone(0,2,'상담 생성',['성공 → 결과 읽기 + 책자 저장','실패 → 질문 화면 + 오류 안내'],mock=mock_loading)
phone(1,2,'결과 읽기',['단락형 상담 결과','스크랩 · 공유 · 글자 크기'],mock=mock_reader)
phone(2,2,'보관함',['책자를 다시 열기 / 삭제','로그인 사용자 원격 동기화'],mock=mock_library)
phone(3,2,'PDF 공유',['결과 화면에서 공유 버튼','Android 저장·공유 창'],mock=mock_share)
phone(4,2,'마이 · 설정',['프로필 · 계정 · 알림','글자 크기 · 동기화'],mock=mock_settings)
phone(5,2,'오류와 재시도',['날짜 오류 / 연결·생성 실패','수정 후 기존 단계에서 재시도'],mock=mock_error)
arrow(389,2230,442,2230)
arrow(807,2230,754,2230,True)

text(85,2640,'저장·공유는 결과 읽기에서 분기하며, 보관함에서 결과 화면으로 다시 진입합니다.',18,'#767085')

rect(85,2700,2130,380,'#f3edf5',24)
text(115,2745,'보조·예비 화면과 연결 상태',25,weight=600)
optional=[('알림 안내','화면은 존재','현재 자동 진입 없음'),('음성 선택','읽기 / 건너뛰기','주 생성 경로는 결과 직행'),('수리 음성 읽기','상담 음성·수리 장면','선택 상태로 유지'),('책 표지 → 목차 → 본문','기존 책자 단계','현재 주 경로는 단락형 리더'),('계정·결제 복귀','미로그인 시 마이로 이동','자동 복귀는 추가 개선 가능')]
for i,(title,a,b) in enumerate(optional):
    x=115+i*418;rect(x,2780,394,155,'#fffcf8',18);text(x+17,2820,title,19);lines(x+17,2865,[a,b],17)
lines(115,2985,['범례  앱 캡처 = 에뮬레이터에서 확인한 화면 / 화면 모형 = 실제 코드의 기능과 버튼을 바탕으로 그린 시안',
                '주의  결제 화면은 현재 버튼이 상담 생성으로 연결되며, 실제 스토어 결제 SDK 연동은 확인되지 않았습니다.'],18)
text(85,3150,'기존 서버·DB 구조를 변경하지 않은 UI/UX 개편  ·  설치 파일: suri-fortune-pastel-debug.apk',19,'#88749e')
add('</g></svg>')
svg='\n'.join(parts)
(OUT/'suri-app-flow.svg').write_text(svg,encoding='utf-8')
(OUT/'suri-app-flow.html').write_text('<!doctype html><meta charset="utf-8"><title>수리운세 전체 플로우</title><style>body{margin:0;background:#fff9f4}svg{display:block;width:100%;height:auto}</style>'+svg,encoding='utf-8')
print(json.dumps({'svg':str(OUT/'suri-app-flow.svg'),'html':str(OUT/'suri-app-flow.html'),'width':W,'height':H},ensure_ascii=False))

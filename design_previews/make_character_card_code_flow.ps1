Add-Type -AssemblyName System.Drawing

$root = "C:\Users\USER\Downloads\unum_local_10k_chunked_app_bundle\unum_local_10k_chunked_app"
$outDir = Join-Path $root "design_previews"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$out = Join-Path $outDir "character_card_code_flow_clean_en.png"

$bmp = [System.Drawing.Bitmap]::new(1800, 1200)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit

function B($hex) { [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml($hex)) }
function P($hex, $w = 2) { [System.Drawing.Pen]::new([System.Drawing.ColorTranslator]::FromHtml($hex), $w) }

$g.FillRectangle((B "#F7FAFE"), 0, 0, 1800, 1200)

$titleFont = [System.Drawing.Font]::new("Arial", 44, [System.Drawing.FontStyle]::Bold)
$hFont = [System.Drawing.Font]::new("Arial", 24, [System.Drawing.FontStyle]::Bold)
$bodyFont = [System.Drawing.Font]::new("Arial", 18, [System.Drawing.FontStyle]::Regular)
$smallFont = [System.Drawing.Font]::new("Arial", 14, [System.Drawing.FontStyle]::Regular)
$monoFont = [System.Drawing.Font]::new("Consolas", 14, [System.Drawing.FontStyle]::Regular)
$dark = B "#0F172A"
$muted = B "#617089"
$blue = B "#2563EB"
$white = B "#FFFFFF"
$red = B "#9A3412"
$green = B "#166534"

function RoundRect($x, $y, $w, $h, $r) {
  $p = [System.Drawing.Drawing2D.GraphicsPath]::new()
  $d = $r * 2
  $p.AddArc($x, $y, $d, $d, 180, 90)
  $p.AddArc($x + $w - $d, $y, $d, $d, 270, 90)
  $p.AddArc($x + $w - $d, $y + $h - $d, $d, $d, 0, 90)
  $p.AddArc($x, $y + $h - $d, $d, $d, 90, 90)
  $p.CloseFigure()
  return $p
}

function DrawCard($x, $y, $w, $h, $num, $title, $subtitle) {
  $path = RoundRect $x $y $w $h 22
  $g.FillPath($white, $path)
  $g.DrawPath((P "#D7E1EE" 2), $path)

  $badge = RoundRect ($x + 26) ($y + 24) 48 48 12
  $g.FillPath($blue, $badge)
  $bf = [System.Drawing.Font]::new("Arial", 20, [System.Drawing.FontStyle]::Bold)
  $sf = [System.Drawing.StringFormat]::new()
  $sf.Alignment = "Center"
  $sf.LineAlignment = "Center"
  $g.DrawString($num, $bf, $white, [System.Drawing.RectangleF]::new([float]($x + 26), [float]($y + 24), 48, 48), $sf)

  $g.DrawString($title, $hFont, $dark, [float]($x + 92), [float]($y + 28))
  $g.DrawString($subtitle, $bodyFont, $muted, [float]($x + 92), [float]($y + 66))
}

function DrawArrow($x1, $y1, $x2, $y2) {
  $pen = P "#93A4BA" 4
  $pen.CustomEndCap = [System.Drawing.Drawing2D.AdjustableArrowCap]::new(5, 6)
  $g.DrawLine($pen, $x1, $y1, $x2, $y2)
}

function DrawImageFit($path, $x, $y, $w, $h) {
  if (Test-Path $path) {
    $img = [System.Drawing.Image]::FromFile($path)
    $ratio = [Math]::Min($w / $img.Width, $h / $img.Height)
    $dw = [int]($img.Width * $ratio)
    $dh = [int]($img.Height * $ratio)
    $dx = [int]($x + ($w - $dw) / 2)
    $dy = [int]($y + ($h - $dh) / 2)
    $g.DrawImage($img, $dx, $dy, $dw, $dh)
    $img.Dispose()
  }
}

function Wrap($text, $font, $brush, $x, $y, $w, $lineH) {
  $words = $text -split " "
  $line = ""
  foreach ($word in $words) {
    $test = if ($line.Length -eq 0) { $word } else { "$line $word" }
    if ($g.MeasureString($test, $font).Width -gt $w -and $line.Length -gt 0) {
      $g.DrawString($line, $font, $brush, [float]$x, [float]$y)
      $y += $lineH
      $line = $word
    } else {
      $line = $test
    }
  }
  if ($line.Length -gt 0) {
    $g.DrawString($line, $font, $brush, [float]$x, [float]$y)
    $y += $lineH
  }
  return $y
}

$g.DrawString("Character Card Code Flow", $titleFont, $dark, 70, 50)
$g.DrawString("From your image sheets to the current HomeScreen result, and where the black-card issue comes from.", $bodyFont, $muted, 74, 118)

DrawCard 70 180 390 290 "1" "Source sheets" "Field images from Downloads"
DrawImageFit (Join-Path $root "app\src\main\res\drawable-nodpi\numerology_love.png") 138 288 105 130
DrawImageFit (Join-Path $root "app\src\main\res\drawable-nodpi\numerology_money.png") 263 288 105 130
$g.DrawString("love, money, study, career, self", $smallFont, $muted, 112, 432)
DrawArrow 470 325 535 325

DrawCard 545 180 390 290 "2" "Cutout pose" "One representative pose per field"
DrawImageFit (Join-Path $root "app\src\main\res\drawable-nodpi\numerology_career.png") 665 290 140 140
$g.DrawString("checkerboard removed as much as possible", $smallFont, $muted, 610, 432)
DrawArrow 945 325 1010 325

DrawCard 1020 180 390 290 "3" "App resources" "app/src/main/res/drawable-nodpi"
$g.DrawString("numerology_love.png", $monoFont, $dark, 1115, 300)
$g.DrawString("numerology_career.png", $monoFont, $dark, 1115, 330)
$g.DrawString("numerology_money.png", $monoFont, $dark, 1115, 360)
$g.DrawString("numerology_study.png", $monoFont, $dark, 1115, 390)
$g.DrawString("numerology_self.png", $monoFont, $dark, 1115, 420)
DrawArrow 1420 325 1485 325

DrawCard 1495 180 240 290 "4" "Mapping" "imageRes"
$g.DrawString("LOVE -> love", $monoFont, $dark, 1532, 305)
$g.DrawString("CAREER -> career", $monoFont, $dark, 1532, 335)
$g.DrawString("MONEY -> money", $monoFont, $dark, 1532, 365)
$g.DrawString("STUDY -> study", $monoFont, $dark, 1532, 395)
$g.DrawString("SELF -> self", $monoFont, $dark, 1532, 425)

$path = RoundRect 70 535 790 480 24
$g.FillPath($white, $path)
$g.DrawPath((P "#D7E1EE" 2), $path)
$g.DrawString("Current coded result", $hFont, $dark, 110, 575)
$g.DrawString("Why it feels wrong now", $bodyFont, $red, 110, 625)
$y = 660
$y = Wrap "1. NumberReadingCard has a fixed dark background: Color(0xFF101418)." $bodyFont $dark 110 $y 700 31
$y = Wrap "2. The app screen is white, so the dark mini cards feel disconnected." $bodyFont $dark 110 $y 700 31
$y = Wrap "3. Text is placed inside a small card, so preview compression can make labels look cropped." $bodyFont $dark 110 $y 700 31

$labels = @(
  @("LOVE", "love", "numerology_love.png"),
  @("CAREER", "career", "numerology_career.png"),
  @("MONEY", "money", "numerology_money.png"),
  @("STUDY", "study", "numerology_study.png")
)

$x = 135
foreach ($l in $labels) {
  $p = RoundRect $x 865 135 120 18
  $g.FillPath((B "#101418"), $p)
  $g.DrawPath((P "#293241" 2), $p)
  $sf = [System.Drawing.StringFormat]::new()
  $sf.Alignment = "Center"
  $g.DrawString($l[0], [System.Drawing.Font]::new("Arial", 11, [System.Drawing.FontStyle]::Bold), $white, [System.Drawing.RectangleF]::new($x, 875, 135, 24), $sf)
  DrawImageFit (Join-Path $root ("app\src\main\res\drawable-nodpi\" + $l[2])) ($x + 25) 895 85 55
  $g.DrawString($l[1], [System.Drawing.Font]::new("Arial", 15, [System.Drawing.FontStyle]::Bold), $white, [System.Drawing.RectangleF]::new($x, 950, 135, 25), $sf)
  $x += 170
}

$path = RoundRect 940 535 790 480 24
$g.FillPath($white, $path)
$g.DrawPath((P "#D7E1EE" 2), $path)
$g.DrawString("Recommended result", $hFont, $dark, 980, 575)
$g.DrawString("Keep the ritual-card UX, but make it belong to the white app.", $bodyFont, $muted, 980, 625)

$x = 1015
$pastels = @("#FFF1F2", "#EFF6FF", "#ECFDF5", "#FFFBEB")
$i = 0
foreach ($l in $labels) {
  $p = RoundRect $x 705 135 205 18
  $g.FillPath((B "#FFFFFF"), $p)
  $g.DrawPath((P "#CBD5E1" 2), $p)
  $pp = RoundRect ($x + 10) 718 115 75 14
  $g.FillPath((B $pastels[$i]), $pp)
  $sf = [System.Drawing.StringFormat]::new()
  $sf.Alignment = "Center"
  $g.DrawString($l[0], [System.Drawing.Font]::new("Arial", 11, [System.Drawing.FontStyle]::Bold), $dark, [System.Drawing.RectangleF]::new($x, 730, 135, 22), $sf)
  DrawImageFit (Join-Path $root ("app\src\main\res\drawable-nodpi\" + $l[2])) ($x + 22) 765 90 80
  $g.DrawString($l[1], [System.Drawing.Font]::new("Arial", 16, [System.Drawing.FontStyle]::Bold), $dark, [System.Drawing.RectangleF]::new($x, 852, 135, 26), $sf)
  $g.DrawString("more", $smallFont, $muted, [System.Drawing.RectangleF]::new($x, 880, 135, 22), $sf)
  $x += 170
  $i++
}

$y = 930
$y = Wrap "Fix direction: use white or ivory cards, separate image area from text area, increase card height, and keep the selected-card lift / magnetic motion." $bodyFont $green 980 $y 690 31

$p = RoundRect 70 1050 1660 95 20
$g.FillPath((B "#0F172A"), $p)
$g.DrawString("Code path: NumberCard(..., R.drawable.numerology_love)  ->  NumberReadingCard()  ->  Image(painterResource(card.imageRes))", $bodyFont, $white, 105, 1083)
$g.DrawString("Problem line: NumberReadingCard background = Color(0xFF101418) + tight text layout", $bodyFont, (B "#FDE68A"), 105, 1115)

$bmp.Save($out, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose()
$bmp.Dispose()
Write-Output $out

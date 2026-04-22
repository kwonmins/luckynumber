$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

$sourcePath = Get-ChildItem "C:\Users\USER\Pictures\Screenshots" -File -Filter "*.png" |
    Sort-Object LastWriteTime -Descending |
    Where-Object {
        try {
            $img = [System.Drawing.Image]::FromFile($_.FullName)
            $match = $img.Width -eq 502 -and $img.Height -eq 471
            $img.Dispose()
            $match
        } catch {
            $false
        }
    } |
    Select-Object -First 1 -ExpandProperty FullName

$targetDir = Join-Path $PSScriptRoot "..\app\src\main\res\drawable-nodpi"

if ([string]::IsNullOrWhiteSpace($sourcePath) -or -not (Test-Path $sourcePath)) {
    throw "Source image not found: $sourcePath"
}

$source = [System.Drawing.Bitmap]::new($sourcePath)

$sprites = @(
    @{ Name = "suri_scroll.png"; X = 28; Y = 20; Width = 188; Height = 176 },
    @{ Name = "suri_writer.png"; X = 246; Y = 20; Width = 194; Height = 178 },
    @{ Name = "suri_hanbok.png"; X = 166; Y = 164; Width = 166; Height = 152 },
    @{ Name = "suri_coins.png"; X = 18; Y = 228; Width = 220; Height = 224 },
    @{ Name = "suri_tea.png"; X = 258; Y = 232; Width = 184; Height = 210 }
)

function Remove-NearWhiteBackground {
    param([System.Drawing.Bitmap]$bitmap)

    for ($x = 0; $x -lt $bitmap.Width; $x++) {
        for ($y = 0; $y -lt $bitmap.Height; $y++) {
            $pixel = $bitmap.GetPixel($x, $y)
            if ($pixel.R -ge 245 -and $pixel.G -ge 245 -and $pixel.B -ge 245) {
                $bitmap.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, $pixel.R, $pixel.G, $pixel.B))
            } elseif ($pixel.R -ge 236 -and $pixel.G -ge 236 -and $pixel.B -ge 236) {
                $bitmap.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(32, $pixel.R, $pixel.G, $pixel.B))
            }
        }
    }
}

foreach ($sprite in $sprites) {
    $rect = [System.Drawing.Rectangle]::new($sprite.X, $sprite.Y, $sprite.Width, $sprite.Height)
    $cropped = [System.Drawing.Bitmap]::new($rect.Width, $rect.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($cropped)
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $graphics.DrawImage($source, [System.Drawing.Rectangle]::new(0, 0, $rect.Width, $rect.Height), $rect, [System.Drawing.GraphicsUnit]::Pixel)
    $graphics.Dispose()

    Remove-NearWhiteBackground -bitmap $cropped
    $outPath = Join-Path $targetDir $sprite.Name
    $cropped.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $cropped.Dispose()
}

$source.Dispose()
Write-Host "Cropped mascot assets saved to $targetDir"

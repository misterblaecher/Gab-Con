$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

# Télécharge Gradle dans ce dossier s'il n'est pas déjà présent.
$version = '9.2.1'
$folder = Join-Path $PSScriptRoot '.build-tools'
$cmd = Join-Path $folder "gradle-$version\bin\gradle.bat"

if (-not (Test-Path $cmd)) {
    New-Item -ItemType Directory -Force -Path $folder | Out-Null
    $zip = Join-Path $folder "gradle-$version-bin.zip"
    Write-Host "Telechargement de Gradle $version depuis services.gradle.org..."
    Invoke-WebRequest "https://services.gradle.org/distributions/gradle-$version-bin.zip" -OutFile $zip
    Expand-Archive -LiteralPath $zip -DestinationPath $folder -Force
    Remove-Item $zip
}
Write-Host 'Compilation de GabCon Admin Chunk Loader pour NeoForge 1.21.1 (Java 21 requis)...'
& $cmd --no-daemon build
if ($LASTEXITCODE -ne 0) {
    throw "Compilation echouee, code Gradle: $LASTEXITCODE"
}
Write-Host 'Le mod compile se trouve dans build\libs\gabcon-admin-chunk-loader-1.0.0.jar'

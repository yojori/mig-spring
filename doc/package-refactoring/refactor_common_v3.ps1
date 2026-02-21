$base = "c:\java\vs-project\mig-spring\java\mig-common\src\main\java"
$old = "$base\com\yojori"
$new = "$base\c\y\mig"

if (!(Test-Path $new)) { New-Item -ItemType Directory -Path $new -Force }

foreach ($dir in @("db", "manager", "model")) {
    $src = "$old\$dir"
    $dst = "$new\"
    if (Test-Path $src) {
        Write-Host "Copying $src to $dst"
        Copy-Item -Path $src -Destination $dst -Recurse -Force
        Remove-Item -Path $src -Recurse -Force
    }
}

if (Test-Path $new) {
    Get-ChildItem -Path $new -Filter *.java -Recurse | ForEach-Object {
        $content = Get-Content $_.FullName
        $content = $content -replace "com.yojori", "c.y.mig"
        $content | Set-Content $_.FullName
    }
}

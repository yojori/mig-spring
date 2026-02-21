$baseDir = "java/mig-common/src/main/java"
if (Test-Path "$baseDir/com/yojori") {
    New-Item -ItemType Directory -Path "$baseDir/c/y/mig" -Force
    Move-Item -Path "$baseDir/com/yojori/*" -Destination "$baseDir/c/y/mig/" -Force
    Remove-Item -Path "$baseDir/com" -Recurse -Force
}

$targetDir = "$baseDir/c/y/mig"
if (Test-Path $targetDir) {
    Get-ChildItem -Path $targetDir -Filter *.java -Recurse | ForEach-Object {
        $content = Get-Content $_.FullName
        $content = $content -replace 'com\.yojori\.migration\.worker', 'c.y.mig.worker'
        $content = $content -replace 'com\.yojori\.migration\.controller', 'c.y.mig.controller'
        $content = $content -replace 'com\.yojori\.model', 'c.y.mig.model'
        $content = $content -replace 'com\.yojori\.manager', 'c.y.mig.manager'
        $content = $content -replace 'com\.yojori\.db', 'c.y.mig.db'
        $content = $content -replace 'com\.yojori\.util', 'c.y.mig.util'
        $content = $content -replace 'com\.yojori', 'c.y.mig'
        $content | Set-Content $_.FullName
    }
}

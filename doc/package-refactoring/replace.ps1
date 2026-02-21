$path = "c:\java\vs-project\mig-spring\java\mig-controller\src\main\webapp"
Get-ChildItem -Path $path -Filter "*.jsp" -Recurse | ForEach-Object {
    Write-Host "Processing $($_.FullName)"
    $content = Get-Content $_.FullName -Raw
    $newContent = $content.Replace("com.yojori.migration", "c.y.mig")
    $newContent = $newContent.Replace("com.yojori", "c.y.mig")
    Set-Content $_.FullName $newContent
}

$legacyDirs = @(
    "c:\java\vs-project\mig-spring\java\mig-controller\src\main\java\com",
    "c:\java\vs-project\mig-spring\java\mig-worker\src\main\java\com",
    "c:\java\vs-project\mig-spring\java\mig-common\src\main\java\com"
)

foreach ($dir in $legacyDirs) {
    if (Test-Path $dir) {
        Write-Host "Removing legacy directory: $dir"
        Remove-Item -Path $dir -Recurse -Force
    }
}

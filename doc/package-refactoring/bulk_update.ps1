$log = "c:\java\vs-project\mig-spring\update.log"
"Starting update at $(Get-Date)" | Out-File $log
$path = "c:\java\vs-project\mig-spring\java\mig-controller\src\main\webapp"
$files = Get-ChildItem -Path $path -Filter "*.jsp" -Recurse
foreach ($file in $files) {
    "Processing $($file.FullName)" | Out-File $log -Append
    $content = Get-Content $file.FullName -Raw
    $newContent = $content.Replace("com.yojori.migration", "c.y.mig").Replace("com.yojori", "c.y.mig")
    if ($content -ne $newContent) {
        "Changing $($file.FullName)" | Out-File $log -Append
        $newContent | Set-Content $file.FullName -NoNewline
    }
}

$legacyDirs = @(
    "c:\java\vs-project\mig-spring\java\mig-controller\src\main\java\com",
    "c:\java\vs-project\mig-spring\java\mig-worker\src\main\java\com",
    "c:\java\vs-project\mig-spring\java\mig-common\src\main\java\com"
)

foreach ($dir in $legacyDirs) {
    if (Test-Path $dir) {
        "Removing legacy directory: $dir" | Out-File $log -Append
        Remove-Item -Path $dir -Recurse -Force
    }
}
"Update finished at $(Get-Date)" | Out-File $log -Append

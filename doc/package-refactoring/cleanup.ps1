Remove-Item -Path "java\mig-common\src\main\java\com" -Recurse -Force
Remove-Item -Path "java\mig-worker\src\main\java\com" -Recurse -Force
Remove-Item -Path "java\mig-controller\src\main\java\com" -Recurse -Force
"Cleanup Done" | Out-File -FilePath "cleanup_done.txt"

@echo off

powershell compress-archive -Path "Database\Build\prod.yml,app_assets\src\main\assets\media,app\build.gradle" -DestinationPath "backup-%date:.=-%.zip" 
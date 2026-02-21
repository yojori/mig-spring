@echo off
set BASE=c:\java\vs-project\mig-spring\java\mig-common\src\main\java
mkdir %BASE%\c
mkdir %BASE%\c\y
mkdir %BASE%\c\y\mig
move %BASE%\com\yojori\db %BASE%\c\y\mig\
move %BASE%\com\yojori\manager %BASE%\c\y\mig\
move %BASE%\com\yojori\model %BASE%\c\y\mig\

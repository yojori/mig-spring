@echo off
set BASE=java\mig-common\src\main\java
mkdir %BASE%\c\y\mig
move %BASE%\com\yojori\db %BASE%\c\y\mig\
move %BASE%\com\yojori\manager %BASE%\c\y\mig\
move %BASE%\com\yojori\model %BASE%\c\y\mig\
rmdir %BASE%\com\yojori
rmdir %BASE%\com

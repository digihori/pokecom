# ポケコンエミュのプロジェクト
# PokecomGO - Emulator of SHARP's Pocket computer(sc61860 series)
#
# It is an emulator of SHARP's Pocket Computer(sc61860 series).
# Supported models:pc-1245/1251/1261/1350/1401/1402/1450/1460/1470U
# ROM image is not included, so it is necessary to prepare own.
#
# When you start the emulator for the first time,
# /sdcard/pokecom/rom directory (the path may be different depending on the device) is created,
# and is created a dummy ROM image file(pc1245mem.bin) there.
# Please arrange various ROM images in this folder.
# 
# ROM image file,
# for example, in the case of PC-1245,
# 8K of internal ROM:0x0000-0x1fff and 16K of external ROM:0x4000-0x7fff are arranged in 64K space of 0x0000-0xffff,
# Other addresses are created as a binary image filled with dummy data,
# Please create with file name pc1245mem.bin.
# The same applies to PC-1251/1261/1350/1401/1402/1450.
#
# PC-1460 and 1470U have external ROM in bank format, make 2 file configuration.
# Please create internal ROM as pc1460mem.bin. Only the part of 0x0000 - 0x1fff is necessary.
# Create external ROM as pc1460bank.bin and arrange the bank data in order as it is.
#
# If the file is recognized correctly, the target model will be valid in the list on the initial screen.
#
# Memory map information
# [pc-1245/1251]
# 0x0000-0x1fff : internal ROM
# 0x4000-0x7fff : external ROM
# 
# [pc-1261/1350/1401/1402/1450]
# 0x0000-0x1fff : internal ROM
# 0x8000-0xffff : external ROM
# 
# [pc-1460/1470U]
# 0x0000-0x1fff : internal ROM
# 0x4000-0x7fff : external ROM(BANK   1460:0-3, 1470U:0-7)
#!/bin/sh -x

set -e

# http://stackoverflow.com/questions/6687630/c-c-gcc-ld-remove-unused-symbols
OPTFLAGS="-Os -fdata-sections -ffunction-sections -Wl,--gc-sections -Werror"

PROGRAM=pinball_buttons_mapper
arm-linux-gnueabi-gcc -static $OPTFLAGS $PROGRAM.c daemon.c -o $PROGRAM
arm-linux-gnueabi-strip $PROGRAM

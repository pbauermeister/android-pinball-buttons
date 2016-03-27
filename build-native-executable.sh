#!/bin/sh

set -e

cd app
ndk-build 
mv -f libs/armeabi/pinball_buttons_mapper src/main/res/raw/pinball_buttons_mapper_armeabi
echo "=> app/src/main/res/raw/pinball_buttons_mapper_armeabi"
echo "*** OK"

#!/bin/sh -x

set -e

./build.sh
adb push pinball_buttons_mapper /data/local/tmp/
adb shell su -c /data/local/tmp/pinball_buttons_mapper

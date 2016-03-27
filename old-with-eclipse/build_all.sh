#!/bin/sh -x

set -e

# Needed for ant
export JAVA_HOME=/usr/lib/jvm/java-6-openjdk-amd64/jre
echo "JAVA_HOME=$JAVA_HOME"

# svn
svn update

# Build the C program, and copy it into raw/
( cd c; ./build.sh)
mkdir -p res/raw/
cp -f c/pinball_buttons_mapper res/raw/

# Re-create project files needed by ant for cmdline build
android update project --path . --target android-15

# Create version info from SVN
mkdir -p assets/config
echo "Revision=$(svnversion)" > assets/config/Version.config

# Determine SCM version
#rev=`svnversion`
rev=`git log --pretty=format:'%ad-%h' --abbrev-commit --date=short -1`
me=$(basename `pwd`)

# Build the Whole project (Java)
ant clean debug
cp bin/SettingsActivity-debug.apk PinballButtons-$rev.apk

# Install into target
ant installd

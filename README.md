Android app for the mini pinball cabinet project
================================================

(C) 2013-2016 by Pascal Bauermeister.
Released under the GNU General Public License (GPL) version 3.
No warranties of any kind are given.

Summary
-------

This is the Android software used for the mini pinball cabinet.


![Pinball cabinet](http://cdn.instructables.com/FQB/AM1A/HFSHUX0K/FQBAM1AHFSHUX0K.MEDIUM.jpg "Pinball cabinet")

It essentially does this:
 * Listens to keyboard events, and forward them as touchscreen events. This part is written in C, and expects the device to be rooted.
 * Provides an app caring for the configuration (selection of device drivers). This part is written in Java.

Documentation
-------------

* See http://www.instructables.com/id/Mini-Pinball-1 for project
  description

* UML class diagram: see architecture_classes.graphml (open with yEd).


Building
--------

* An old (unmaintained) version under Eclipse is in the folder old-with-eclipse/.

* It now builds under Android Studio.
  - You must have the NDK installed in addition to the Android SDK.
  - first, run: ./build-native-executable.sh
  - then, build: ./gradlew assemble
  - or build+run from Android Studio.

* Currently, the native code supports only ARM-based targets.

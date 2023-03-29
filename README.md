Android app for the mini pinball cabinet project
================================================

(C) 2013-2016 by Pascal Bauermeister.
Released under the GNU General Public License (GPL) version 3.
No warranties of any kind are given.

Summary
-------

This is the Android software used for the mini pinball cabinet.


![Pinball cabinet](https://content.instructables.com/FQB/AM1A/HFSHUX0K/FQBAM1AHFSHUX0K.jpg?auto=webp&frame=1&width=512&height=512&fit=bounds&md=85bc3572c570912c584acf67b0cd3c17 "Pinball cabinet")

It essentially does this:
 * Listens to keyboard events, and forward them as touchscreen events. This part is written in C, and expects the device to **be rooted**.
 * Provides an app caring for the configuration (selection of device drivers). This part is written in Java.

Documentation
-------------

* See http://www.instructables.com/id/Mini-Pinball-1 for project
  description

* UML class diagram: see architecture_classes.graphml (open with yEd).

  ![Class diagram](https://content.instructables.com/F7N/55VS/HFSH5560/F7N55VSHFSH5560.png?auto=webp&frame=1&width=800&height=800&fit=bounds&md=285048f50656395df82e17408e55fcde "Class diagram")

Building
--------

* An old (unmaintained) version under Eclipse is in the folder old-with-eclipse/.

* It now builds under Android Studio.
  - You must have the NDK installed in addition to the Android SDK.
  - first, run: ./build-native-executable.sh
  - then, build: ./gradlew assemble
  - or build+run from Android Studio.

* Currently, the native code supports only ARM-based targets.

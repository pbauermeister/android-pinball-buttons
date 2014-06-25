Android app for the mini pinball cabinet project
================================================

(C) 2013 by Pascal Bauermeister.
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

* Run build_all.sh to build the project. It needs JDK, the Android
  SDK, and the arm-linux-gnueabi-gcc cross compiler.

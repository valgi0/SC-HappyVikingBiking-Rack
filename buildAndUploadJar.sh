#!/usr/bin/env bash

./gradlew shadowJar

scp ./build/libs/*.jar pi@192.168.1.156:/home/pi/rack/
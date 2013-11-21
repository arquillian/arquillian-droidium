#!/bin/sh
#
# Starts emulator and waits until it is online
#
# https://arquillian.ci.cloudbees.com
#
# Author: Stefan Miklosovic
# e-mail: smikloso@redhat.com

AVD_NAME=test01
MEMORY=343
ADB_CMD=/opt/android/android-sdk-linux/platform-tools/adb
EMULATOR_CMD=/opt/android/android-sdk-linux/tools/emulator

echo "Starting emulator $AVD_NAME"
echo "executing: 'emulator -avd $AVD_NAME -no-skin -no-audio -no-window -memory $MEMORY -wipe-data -no-snapshot-save -no-snapstorage'"

$EMULATOR_CMD -avd $AVD_NAME -no-skin -no-audio -no-window -memory $MEMORY -wipe-data -no-snapshot-save -no-snapstorage &

echo "Waiting until emulator $AVD_NAME is booted"

booted=""
until [[ "$booted" =~ "stopped" ]]; do
    # -e directs command to the only running emulator
    booted=`$ADB_CMD -e shell getprop init.svc.bootanim`
    echo -n $booted
    if [[ "$booted" =~ "not found" ]]; then
        echo "Could not boot emulator"
        exit 1
    fi
    sleep 2
done
echo "Emulator $AVD_NAME booted"

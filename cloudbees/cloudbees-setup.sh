#!/bin/sh
#
# Cloudbees Android bootstrapping script
#
# https://arquillian.ci.cloudbees.com
#
# Author: Stefan Miklosovic
# e-mail: smikloso@redhat.com

echo "Bootstrapping Android environment"
echo
echo "ARCH        : $(uname -m)"
echo "WORKSPACE   : $WORKSPACE"
echo "PWD         : $PWD"
echo "JAVA_HOME   : $JAVA_HOME"
echo "ANDROID_HOME: $ANDROID_HOME"
echo "PATH        : $PATH"


if [[ -z $WORKSPACE ]]; then
    WORKSPACE=$(pwd)
fi

echo "WORKSPACE   : $WORKSPACE"

ls -la /opt/android
ls -la /opt/android/android-sdk-linux
ls -la /opt/android/android-sdk-linux/platforms
ls -la /opt/android/android-sdk-linux/tools
ls -la /tmp/

for i in $(ls $ANDROID_HOME/system-images); do echo -n "$i -> "; ls $ANDROID_HOME/system-images/$i; done

TEST_EMULATOR_NAME=test01
TEST_EMULATOR_PLATFORM=android-10
TEST_EMULATOR_ABI=x86
TEST_EMULATOR_MEMORY=343M
ANDROID_CMD=/opt/android/android-sdk-linux/tools/android

echo "Creating of Android emulator test01 for underlying tests ... "
echo no | $ANDROID_CMD create avd -n $TEST_EMULATOR_NAME -t $TEST_EMULATOR_PLATFORM -f -p "$WORKSPACE/$TEST_EMULATOR_NAME-emulator" -c $TEST_EMULATOR_MEMORY --abi $TEST_EMULATOR_ABI

if [ -d "$WORKSPACE"/$TEST_EMULATOR_NAME-emulator ]; then
    echo "Emulator $TEST_EMULATOR_NAME for testing purposes was successfully created";
else
    echo "Failed to create $TEST_EMULATOR_NAME for testing purposes";
    exit 1
fi

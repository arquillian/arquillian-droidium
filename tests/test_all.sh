#!/bin/bash

ROOT=$(pwd)
ADB_CMD=$ANDROID_HOME/platform-tools/adb
EMULATOR_CMD=$ANDROID_HOME/tools/emulator
EMULATOR_RAM=343

SELENDROID_SERVER_APK=selendroid-server-0.6.0.apk
SELENDROID_TEST_APP=selendroid-test-app-0.6.0.apk

function help
{
    echo "help: "
    echo "    $1 _avd_name _ip_address_"
}

if [ "x$1" == "x" ]; then
    echo "You have not set avd name to run tests on."
    help $0
    exit
fi

if [ "x$2" == "x" ]; then
    echo "You have not set your external IP address."
    help $0
    exit
fi

function copy_server
{
    cp ../$SELENDROID_SERVER_APK $1
}

function copy_test_app
{
    cp ../$SELENDROID_TEST_APP $1
}

function copy_all
{
    copy_server $1
    copy_test_app $1
}

#
# Starts emulator
#
# start_emulator avd_name emulator_ram
function start_emulator
{
    $EMULATOR_CMD -avd $1 -memory $2 -wipe-data -no-snapshot-save -no-snapstorage &
}

#
# Waits until emulator is booted after start_emulator
#
function wait_until_started
{
    emulator_state=""
    until [[ "$emulator_state" =~ "stopped" ]]; do
        emulator_state=`$ADB_CMD -e shell getprop init.svc.bootanim 2> /dev/null`
        sleep 5
    done
}

#
# Kills emulator
#
function stop_emulator
{
    $ADB_CMD -s emulator-5554 emu kill
}

function clean_env
{
    rm selendroid* 2> /dev/null
}

function check_status
{
    if [ "$2" -ne  "0" ]; then
        echo "======================"
        echo "$1 FAILED"
        echo "======================"
        exit
    fi
}

function droidium-hybrid-01
{
    cd $ROOT
    cd droidium-hybrid-01
    copy_all .
    mvn clean test -Dandroid.avd.name=$1
    check_status $0 $?
    clean_env
}

function droidium-multiple-containers-01
{
    cd $ROOT
    cd droidium-multiple-containers-01
    copy_test_app .
    mvn clean test -Dandroid.avd.name=$1
    check_status $0 $?
    clean_env
}

function droidium-multiple-containers-02
{
    cd $ROOT
    cd droidium-multiple-containers-02
    mvn clean test
    check_status $0 $?
}

function droidium-multiple-containers-03
{
    cd $ROOT
    cd droidium-multiple-containers-03
    mvn clean test
    check_status $0 $?
}

function droidium-multiple-deployments-01
{
    cd $ROOT
    cd droidium-multiple-deployments-01
    copy_all .
    mvn clean test -Dandroid.avd.name=$1
    check_status $0 $?
    clean_env
}

function droidium-native-01
{
    cd $ROOT
    cd droidium-native-01
    copy_all .
    mvn clean test -Dandroid.avd.name=$1
    check_status $0 $?
    clean_env
}

function droidium-native-01-scala
{
    cd $ROOT
    cd droidium-native-01-scala
    copy_all .
    mvn clean test -Dandroid.avd.name=$1
    check_status $0 $?
    clean_env
}

function droidium-native-02
{
    cd $ROOT
    cd droidium-native-02
    copy_test_app .
    mvn clean test -Dandroid.avd.name=$1
    check_status $0 $?
    clean_env
}

function droidium-screenshooter-01
{
    cd $ROOT
    cd droidium-screenshooter-01
    copy_all .
    mvn clean test -Dandroid.avd.name=$1
    check_status $0 $?
    clean_env
}

function droidium-web-01
{
    cd $ROOT
    cd droidium-web-01
    mvn test -Dip.jboss=$2 -Dandroid.avd.name=$1
    check_status $0 $?
}

function clean_all
{
    cd $ROOT
    mvn clean
}

start_emulator $1 $EMULATOR_RAM
wait_until_started

droidium-hybrid-01 $1
droidium-multiple-containers-01 $1
droidium-multiple-containers-02
droidium-multiple-containers-03
droidium-multiple-deployments-01 $1
droidium-native-01 $1
droidium-native-01-scala $1
droidium-native-02 $1
droidium-screenshooter-01 $1
droidium-web-01 $1 $2

clean_all

stop_emulator

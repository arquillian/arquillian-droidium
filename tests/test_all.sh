#!/bin/bash

# Tests all demos in this directory in one run
# You do not have to start emulator, it will be started automatically
# Execute this script as:
# ./test_all.sh <name of avd you wan to run tests on> <your external ip address>

# Tips:
#   1) Be sure port 8080 is free.

ROOT=$(pwd)
ADB_CMD=$ANDROID_HOME/platform-tools/adb
EMULATOR_CMD=$ANDROID_HOME/tools/emulator
MAVEN_CMD=mvn
EMULATOR_RAM=343
SERVER_PORT=5037

SELENDROID_VERSION=0.10.0
SELENDROID_TEST_APP=selendroid-test-app-$SELENDROID_VERSION.apk

DEBUG="false"
STAGING=""

function help
{
    echo "help: "
    echo "    $1 _avd_name_1_ _avd_name_2_ _ip_address_ [debug (true|false)] [staging (true|false)]"
    echo "    debug is false by default"
    echo "    staging is false by default"
    echo "    example: ./test_all.sh android-4.2.2a android-4.2.2b 192.168.0.1 true true"
}

if [ "x$1" == "x" ]; then
    echo "You have not set avd name of the first emulator"
    help $0
    exit
fi

if [ "x$2" == "x" ]; then
    echo "You have not set avd name of the second emulator"
    help $0
    exit
fi

if [ "x$3" == "x" ]; then
    echo "You have not set your external IP address."
    help $0
    exit
fi

if [ "$4" == "true" ]; then
    DEBUG="true"
fi

if [ "$5" == "true" ]; then
    STAGING="-Pjboss-staging-repository-group"
fi

function die {
    echo $@
    exit 1
}

function mvn {
    echo "mvn -Dselendroid.version=$SELENDROID_VERSION $@"
    command $MAVEN_CMD -Dselendroid.version=$SELENDROID_VERSION $@
}

function copy_test_app
{
    cp -v ../$SELENDROID_TEST_APP $1 || die "Unable to copy Selendroid Test App"
}

function copy_aerogear
{
    cp -v ../droidium-native-01/aerogear-test-android.apk $1
}

function copy_all
{
    copy_test_app $1
    copy_aerogear $1
}

#
# Starts emulator
#
# start_emulator avd_name emulator_ram
function start_emulator
{
    $EMULATOR_CMD -avd $1 -memory $2 -no-snapshot-save -no-snapstorage &
}

#
# Waits until emulator is booted after start_emulator
#
function wait_until_started
{
    emulator_state=""
    until [[ "$emulator_state" =~ "stopped" ]]; do
        emulator_state=`$ADB_CMD -s $1 -e shell getprop init.svc.bootanim 2> /dev/null`
        sleep 5
    done
}

#
# Kills emulator
#
function stop_emulator
{
    $ADB_CMD -s $1 emu kill
}

function clean_env
{
    rm selendroid*.apk 2> /dev/null
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

function droidium-multiple-androids-with-jboss-01
{
    cd $ROOT
    cd droidium-multiple-androids-with-jboss-01
    copy_all .
    mvn clean test -Dandroid.avd.name_1=$1 -Dandroid.avd.name_2=$2 -Darquillian.debug=$DEBUG $STAGING
    check_status ${FUNCNAME[0]} $?
    clean_env
    rm aerogear-test-android.apk
}

function droidium-multiple-androids-01
{
    cd $ROOT
    cd droidium-multiple-androids-01
    copy_test_app .
    copy_aerogear .
    mvn clean test -Dandroid.avd.name_1=$1 -Dandroid.avd.name_2=$2 -Darquillian.debug=$DEBUG $STAGING
    check_status ${FUNCNAME[0]} $?
    clean_env
    rm aerogear-test-android.apk
}

function droidium-multiple-androids-02
{
    cd $ROOT
    cd droidium-multiple-androids-02
    copy_all .
    mvn clean test -Dandroid.avd.name_1=$1 -Dandroid.avd.name_2=$2 -Darquillian.debug=$DEBUG $STAGING
    check_status ${FUNCNAME[0]} $?
    clean_env
    rm aerogear-test-android.apk
}

function droidium-hybrid-01
{
    cd $ROOT
    cd droidium-hybrid-01
    copy_test_app .
    mvn clean test -Dandroid.avd.name=$1 -Darquillian.debug=$DEBUG $STAGING
    check_status ${FUNCNAME[0]} $?
    clean_env
}

function droidium-multiple-containers-01
{
    cd $ROOT
    cd droidium-multiple-containers-01
    copy_test_app .
    mvn clean test -Dandroid.avd.name=$1 -Darquillian.debug=$DEBUG $STAGING
    check_status ${FUNCNAME[0]} $?
    clean_env
}

function droidium-multiple-containers-02
{
    cd $ROOT
    cd droidium-multiple-containers-02
    mvn clean test -Darquillian.debug=$DEBUG $STAGING
    check_status ${FUNCNAME[0]} $?
}

function droidium-multiple-containers-03
{
    cd $ROOT
    cd droidium-multiple-containers-03
    mvn clean test -Darquillian.debug=$DEBUG $STAGING
    check_status ${FUNCNAME[0]} $?
}

function droidium-multiple-deployments-01
{
    cd $ROOT
    cd droidium-multiple-deployments-01
    copy_all .
    mvn clean test -Dandroid.avd.name=$1 -Darquillian.debug=$DEBUG $STAGING
    check_status ${FUNCNAME[0]} $?
    clean_env
}

function droidium-native-01
{
    cd $ROOT
    cd droidium-native-01
    copy_test_app .
    mvn clean test -Dandroid.avd.name=$1 -Darquillian.debug=$DEBUG $STAGING
    check_status ${FUNCNAME[0]} $?
    clean_env
}

function droidium-native-01-scala
{
    cd $ROOT
    cd droidium-native-01-scala
    copy_test_app .
    mvn clean test -Dandroid.avd.name=$1 -Darquillian.debug=$DEBUG $STAGING
    check_status ${FUNCNAME[0]} $?
    clean_env
}

function droidium-native-02
{
    cd $ROOT
    cd droidium-native-02
    copy_test_app .
    mvn clean test -Dandroid.avd.name=$1 -Darquillian.debug=$DEBUG $STAGING
    check_status ${FUNCNAME[0]} $?
    clean_env
}

function droidium-screenshooter-01
{
    cd $ROOT
    cd droidium-screenshooter-01
    copy_test_app .
    mvn clean test -Dandroid.avd.name=$1 -Darquillian.debug=$DEBUG $STAGING
    check_status ${FUNCNAME[0]} $?
    clean_env
}

function droidium-web-01
{
    cd $ROOT
    cd droidium-web-01
    mvn test -Dip.jboss=$2 -Dandroid.avd.name=$1 -Darquillian.debug=$DEBUG $STAGING
    check_status ${FUNCNAME[0]} $?
}

function clean_all
{
    cd $ROOT
    mvn clean
}

#prepare_selendroid $SELENDROID_VERSION

export ANDROID_ADB_SERVER_PORT=$SERVER_PORT

echo "Starting the first emulator $1"
start_emulator $1 $EMULATOR_RAM

echo "Starting the second emualtor $2"
start_emulator $2 $EMULATOR_RAM

echo "Waiting for the first emulator to start"
wait_until_started emulator-5554

echo "Waiting for the second emulator to start"
wait_until_started emulator-5556

droidium-multiple-androids-with-jboss-01 $1 $2 
droidium-multiple-androids-01 $1 $2 
droidium-multiple-androids-02 $1 $2 
droidium-multiple-containers-01 $1
droidium-multiple-containers-02
droidium-multiple-containers-03
droidium-multiple-deployments-01 $1 
droidium-native-01 $1 
droidium-native-01-scala $1 
droidium-native-02 $1
droidium-hybrid-01 $1 
droidium-screenshooter-01 $1 
#droidium-web-01 $1 $2

clean_all

stop_emulator emulator-5554
stop_emulator emulator-5556

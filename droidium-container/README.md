[![Build Status](https://travis-ci.org/smiklosovic/arquillian-container-android.png)](https://travis-ci.org/smiklosovic/arquillian-container-android)

Android container for Arquillian platform
=========================================

The aim of this document is to describe how to use Android container for Arquillian platform. The reader can expect 
various use cases of arquillian.xml configuration as well as all needed artifact dependecies for Maven in order to 
be able to start some container successfuly.

Concepts
--------

The significant difference between ordinary container adapter for Arquillian and Android container is that while
using the ordinary one, you have to use only that one type of the container you are deploying the archives to. 
There is not the support for multiple container implementations in 1.0.x version of Arquillian out of the box 
so you are normaly forced not to mix two different container adapter implementations together.

The container is event driven, when some 
event is fired, all registered observers which listens to such kind of event executes appropriate method. This 
model is tree-like. The control is not returned to the initial fire point until all subsequent logic is 
executed as well. Events can be chained, meaning when one event is fired and some method listens to it 
(via `@Observes` annotation), that method can fire another event to which some other method of some observer 
is listening as well. 

Observers are registered in `AndroidManagedContainerExtension` class in `android-container-managed` module.

While inspecting the code, it is handy to have [this](https://raw.github.com/smiklosovic/arquillian-thesis/master/resources/container_model.png) chart in mind.

Javadoc Documentation
---------------------

Javadoc documentation is located on github pages of this repository. You are welcome to check it at 
[smiklosovic.github.com/arquillian-container-android/](http://smiklosovic.github.com/arquillian-container-android/).

Setup
-----

When you want to try Android container, you have to basically put into `pom.xml` just this dependency:

    <dependency>
        <groupId>org.jboss.arquillian.container</groupId>
        <artifactId>android-container-depchain</artifactId>
        <type>pom</type>
        <scope>test</scope>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>

Android container uses arquillian-multiple-containers module in this repository. That module, while on the 
classpath, is able to register containers of various types, so you can mix two (three, four ...) different 
container implementations. This module has to know what container configuration stands for what container adapter 
so in order to make the difference there is property `adapterImplClass` in the container configuration.

Android container implementation class is of name `AndroidManagedDeployableContainer` at this moment. JBoss AS has 
implementation class of name `ManagedDeployableContainer`.

Lets see the very basic setup in `arquillian.xml` (which is located in `src/test/resources` for every test) 
to grasp the basics:

    <group qualifier="containers" default="true">
        <container qualifier="jbossas">
            <configuration>
                <property name="adapterImplClass">
                    org.jboss.as.arquillian.container.managed.ManagedDeployableContainer
                </property>
            </configuration>
        </container>
        <container qualifier="android">
            <configuration>
                <property name="adapterImplClass">
                    org.jboss.arquillian.container.android.managed.AndroidManagedDeployableContainer
                </property>
            </configuration>
        </container>
    </group>

You have to provide `adapterImplClass` property per container configuration only when you are using more then one 
container adapter on your classpath. When you are using Android container alone, you do not have to specify 
that property since it is the only class on the classpath which implemets Arquillian container API.

So theoretically you can use just Android container itself as:

    <container qualifier="android"/>

You can also use more then one Android container as well:

    <group qualifier="containers" default="true">
        <container qualifier="android1"/>
        <container qualifier="android2"/>
    </group>

Basic usage
----------

There are some problems regarding of injecting of Android Container into the tests. It is because 
`AndroidDevice` is `ContainerScoped`. Classes which are registered in `ContainerScoped` are available 
in tests only when:

* the test is running on the client side
* and
* is running in the context of a deployment that targets the container
  * or
* you use `@OperatesOnDeployment` as a qualifier on the ArquillianResource injection point

`@Deployment` has to be specified.

valid examples:

    @RunWith(Arquillian.class)
    @RunAsClient
    public class ContainerTest {

        @ArquillianResource
        AndroidDevice android;

        @Deployment(testable = false)
        public static Archive<?> createArchive() {
             return ShrinkWrap.create(GenericArchive.class);
        }

        @Test
        public void test01() {
            assertTrue(android != null);
        }
    }
    
You can use `@OperateOnDeployment` as well: 

    @RunWith(Arquillian.class)
    @RunAsClient
    public class ContainerTest {

        @ArquillianResource
        AndroidDevice android;

        @Deployment(name = "android1", testable = false)
        public static Archive<?> createArchive() {
           return ShrinkWrap.create(GenericArchive.class);
        }

        @Test
        @OperateOnDeployment("android1")
        public void test01() {
            assertTrue(android != null);
        }
    }

When you want to use multiple `AndroidDevice`s you have to specify against which deployments and containers 
these tests have to be executed:

    @RunWith(Arquillian.class)
    @RunAsClient
    public class ContainerTest {

        @ArquillianResource
        AndroidDevice android;

        @Deployment(name = "android1", testable = false)
        @TargetsContainer("android1")
        public static Archive<?> createArchive() {
            return ShrinkWrap.create(GenericArchive.class);
        }

        @Deployment(name = "android2", testable = false)
        @TargetsContainer("android2")
        public static Archive<?> createArchive2() {
            return ShrinkWrap.create(GenericArchive.class);
        }

        @Test
        @OperateOnDeployment("android1")
        public void test01() {
            assertTrue(android != null);
        }

        @Test
        @OperateOnDeployment("android2")
        public void test02() {
            assertTrue(android != null);
        }
    }

As you spotted, we were using `@Deployment(testable = false)`. That means we are testing in so called _client mode_.
We are looking on the test from the outside of the container, we are not modifying archive in order to be 
able to communicate with test which runs in container. We are dealing with the container as it appears to us.
It does not repackage your `@Deployment` nor does it forward the test execution to a remote server (our device).

The second run mode is called _in container_ mode. It means that while using it, we want to repackage our 
`@Deployment` to add our own classes and infrastructure to have the ability to communicate with the test and 
enrich the test and run the test remotely. This mode is used by Arquillian by default.

The example which explains the above is following:

    @RunWith(Arquillian.class)
    public class ContainerTest {

        @Deployment(name = "android1", testable = true)
        @TargetsContainer("android1")
        public static Archive<?> createArchive() {
            return ShrinkWrap.create(GenericArchive.class);
        }

        @Test
        @InSequence(1)
        @OperateOnDeployment("android1")
        public void test01(@ArquillianResource AndroidDevice device) {
            assertTrue(device != null);
        }
    }

Right now, Android container does not provide any way how to modify our deployment when we are running in the 
_in container_ mode. This feature is about to be implemented. Testing of the web application is finished because 
we are not using the `AndroidDevice` itself in the tests. When we want to use injection of `AndroidDevice`, 
we have to specify `@Deployment` against which it acts because `AndroidDevice` is `@ContainerScoped` and without 
telling Arquillian what `@Deployment` our `@Test` operates on, it is unable to find the injected resource.

When we are writing web related tests, some JBoss AS is running and we are using only WebDriver injection 
and `AndroidDevice` is absolutely useless here since we are not interacting with it at all. Because of that, 
`@Deployment` in WebDriver-related tests which `@TargetsContainer` of Android are not necessary nor needed. Only 
`@Deployment` for JBoss AS is required (which is ideally bunch of classes representing isolated use case or the 
whole war file of the web application).

Modification of the `@Deployment` is required to be done in order to be able to test native Android applications.
This is going to be implemented or at least we are trying to do so, in connection with the framework called 
[Transfuse](http://androidtransfuse.org/).

Android Container Configuration
-------------------------------

After seeing how to put containers in `arquillian.xml`, the configuration of the Android container itself is 
following. The list of all properties and its meaning is following, accompanied with examples. The division 
between physical Android Device and emulator is done.

### General properties

#### home
##### default: $ANDROID_HOME

`home` property holds the directory where your Android SDK is installed. It is not necessary to specify it 
since it is automatically read from the system environment property `$ANDROID_HOME`, which can be easily exported 
as `export $ANDROID_HOME=/path/to/your/sdk` for the current shell on it can be put into `~/.bash_profile` to be 
persisted.

    <configuration>
        <property name="home">/path/to/your/android/sdk</property>
    </configuration>

#### forceNewBridge
##### default: true

`forceNewBridge` boolean property specifies if Android Debug Bridge (ADB) should be forced to be created even it 
already is. It can have only `true` or `false` value. When not used, it is set to true.

    <configuration>
        <property name="forceNewBridge">false</property>
    </configuration>

### Real Android Device Configuration

#### serialId
##### default: not defined

`serialId` property holds the identification number of your physical mobile phone. That number can be find out 
by command `adb devices -l` after your mobile phone is connected via usb cable with your computer.

    <configuration>
        <property name="serialId">42583930325742351234</property>
    </configuration>

### Virtual Android Device Configuration

#### avdName
##### default: not defined

`avdName` property is about telling Android container which Android Virtual Device it should use. When you are 
creating some AVD, you have to enter its name. This property is that name.

    <configuration>
        <property name="avdName">my-avd</property>
    </configuration>

#### abi
##### default: as `android` uses

Pretty straightforward. Which ABI you container should use.

    <configuration>
        <property name="abi">armeabi-v7a</property>
    </configuration>

#### emulatorBootupTimeoutInSeconds
#### default: 120 seconds

Specifies a timeout after which container is considered to be unsuccessfuly started. When emulator is not 
started after this amount of time, the whole test fails. It can be used as a prevention to wait for the 
start of the container for ever in case it somehow hangs or your computer is slow to start it faster. 
The value has to be positive non-zero integer.

    <configuration>
        <property name="emulatorBootupTimeoutInSeconds">180</property>
    </configuration>

#### emulatorShutdownTimeoutInSeconds
##### default: 60 seconds

Similar as `emulatorBootupTimeoutInSeconds` but regarding of the emulator shutdown process. The value 
has to be positive non-zero integer.

    <configuration>
        <property name="emulatorShutdownTimeoutInSeconds">45</property>
    </configuration>

#### 

#### emulatorOptions
##### default: empty string

All other configuration switches you want to use for your emulator instance but there is not the configuration 
property for it. It is the string which is append to the `emulator` command. Strings with quotes shoud work as 
well but its number has to be even. (They have to logically enclose some string).

    <configuration>
        <property name="emulatorOptions">-memory 256 -nocache</property>
    </configuration>

#### consolePort
##### default: not specified, selected by `emulator` automatically

Specifies which console port an emulator should use. It has to be even number in range 5554 - 5584. When this 
property is used and `adbPort` property is not, `adb` automatically selects as `adbPort` number `consolePort + 1`. 

    <configuration>
        <property name="consolePort">5558</property>
    </configuration>
    
#### adbPort
##### default: console port + 1

Specifies which adb port should emulator connect to. It has to be odd number in range 5555 - 5585.

    <configuration>
        <property name="consolePort">5559</property>
    </configuration>

#### generatedAvdPath
##### default: `/tmp/` plus `avdName`

This property instructs Android container adapter that the newly generated AVD should be saved in this directory.
Directory has to exist and user has to have write and read permissions. Newly created AVD is placed under this 
directory. The directory files are saved in has the name of `avdName`. By default, all newly created AVDs are 
saved in `/tmp/avdName` as well.

    <configuration>
        <property name="generatedAvdPath">/tmp/generated_avds/</property>
    </configuration>

SD Card configuration
---------------------

It is possible to use SD card while creating some emulator instance. When we are using more then one emulator 
and SD card is used, these emulators are using the same SD card which results in the clash. Creation of 
a SD card is backed by command `mksdcard` which is bundled in Android SDK. All inputs are validated. Size
constrains are the same as for the `mksdcard` itself and are check for its validity on the container side.

Options you can use in connection with SD card configuration are as follows:

#### sdSize
##### default: 128M

Specifies that SD card of size `sdSize` is going to be used. In order to create SD card of size 512MB you have to
put this in the configuration:

    <configuration>
        <property name="sdSize">512M</property>
    </configuration>

#### sdCard
##### default: `android` specifies

Specifies filename where `sdCard` is placed or where it should be created when it does not exist. The suffix 
of the sdCard *has* to end with `.img`.

    <configuration>
        <property name="sdCard">/tmp/my_sdcard.img</property>
    </configuration>

#### sdCardLabel
##### default: generated randomly

Specifies label to use for a SD card we are want to be created automatically. It does not have to be used.

    <configuration>
        <property name="sdCardLabel">my_sdcard_label</property>
    </configuration>

#### generateSDCard
##### default: false

Tells Arquillian that we want to generate card we specified. When this flag is used, the card is deleted after 
tests.

    <configuration>
        <property name="generateSDCard">true</property>
    </configuration>

Connection logic
----------------

When the container you want to use, of some particular `avdName`, is not started, it is automatically started 
for you. You can look on this feature as the "managed" container adapted does. The emulator is started upon 
every Arquillian test and it is also automatically shutted down after your tests are finished. Just as any 
managed container adapter.

If your Android emulator is already started, just use its `avdName`. Android container implementation is
automatically connected to it. This container is not shutted down afterwards. You can look at this as the
"remote" version of the ordinary container adapter.

In general, we could sum up the logic which is used while trying to connect to an emulator instance or to 
create the new one as follows.

1. If `serialId` was specified, we try to connect to that running physical device.
2. If `consolePort` was specified but `avdName` name was not, we try to connect to running emulator which listens to specified `consolePort`. If we fail to connect, exception is thrown.
3. If `avdName` was specified but `consolePort` was not, we try to connect to the first running emulator of such `avdName`.
4. If both `avdName` and `consolePort` were specified, we try to connect to this combination or to start such emulator.

If we fail to get the device in the all above steps:

1. If `avdName` was not specified, random AVD indentifier is generated.
2. If there is not such `avdName` in the system, (generated from the step 1) the AVD of name `avdName` is automatically created.
3. Emulator of AVD (possibly just created from scratch) is started.

After the test run, when `avdName` was generated randomly, this AVD is going to be deleted automatically.

Identifies are simple UUID strings. SD card identifiers are UUID strings as well. UUID identifiers are generated 
only when there is a need for them. 

SD Card usage logic
-------------------

Creation of SD card depends on the combination of a few facts. Let's check the logic:

    1. If generateSDCard property is specified
        1.1. If sdCard is not specified
            1.1.1. Generate random sdCard identifier
            1.1.2. Create the card and use it
        1.2. If sdCard is specified
            1.2.1. If such sdCard already exists, use that card
            1.2.2. Create such sdCard and use it
    2. If generateSDCard property is not specified
        2.1. If sdCard is not specified
            2.1.1 use default system SD card from Android
        2.2. If sdCard is specified
            2.2.1. If it exists, use it
            2.2.2. If it does not exist, use default system one.

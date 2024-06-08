# home-automation-platform
A home automation system made from scratch.

The system aims to control and monitor home devices such as lamps, wall sockets, air conditioners, and others using an Android device. A Raspberry Pi runs the TCP server. Any Android device can be used to control the home devices.

The Android device connects to the TCP server and gets the list of installed devices registered in the main database. These are the devices that can be controlled and monitored. The registration of new devices, as well as their configuration, can be done through the Android app.

At this moment, there is only one fully developed module, named the ON-OFF Module. Using this module, it is possible to control lamps and any other device that operates in two states (ON and OFF).

The system is under development, and in the future, it will be able to control and monitor devices such as cameras, air conditioners, fans, and others.
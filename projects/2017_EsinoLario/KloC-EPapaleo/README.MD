# KloC
**Why counting forwards when you can do it BACKWARDS?**<br />
by: Enea Papaleo (aka TheSilkMiner)

## Description
Kloc is a digital clock with three modes: [Clock](#Clock-mode), [Timer](#Timer-mode) and [Serial](#Serial-mode). It presents also a standby mode, where KloC waits for a &ldquo;wake up&rdquo; signal either from KloC itself or from a computer running its companion app (only available if KloC is connected to the computer).

### Clock mode
When in clock mode, it can count both minutes and seconds. The mode can be chosen directly from KloC.
If KloC is connected to a computer running its companion app, then the time is also always in sync to the one specified by the computer.

### Timer mode
When in timer mode, KloC counts backwards for a specific time. It signals then the end of the timer with a color explosion. The user can control the time through KloC itself: using the potentiometer, they can set the time between ten seconds and five minutes.
If KloC is connected to a computer running its companion app, then this limitation is removed.

### Serial mode
When in serial mode, KloC receives orders through the serial port and executes them immediately. In this mode, every limitation is removed and KloC becomes a mere executor. This mode can only be used if KloC is connected to a computer running its companion app. If this device is not available, then KloC exits from this mode automatically.
Some examples of functions that KloC can do are: reminder alarm, music equaliser and lamp. Refer to the app documentation for more information.

## Technical details
// TODO


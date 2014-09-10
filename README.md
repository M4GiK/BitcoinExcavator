BitcoinExcavator
================

#Java implementation of a Bitcoin miner with WIF, utilizing GPU#
    Copyright (C) 2014 Michał Szczygieł     <michal.szczygiel@wp.pl>
		       Aleksander Śmierciak <aleksander.smierciak@gmail.com>

    This program is free software: you can redistribute it and_or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org_licenses/>.

#Instalation#
To install BitcoinExcavator requires the SDK for Java version 1.8 or higher installed,
as well as Apache Maven 3.x. Maven will download the rest of the dependencies.
At directory bitcoin-excavator is installation script. You need only type in terminal
`./install.sh`

If you have a problem with lunch script you need add execution rights.
`chmod +x install.sh`

BitcoinExcavator uses launch4j to build Windows binaries. launch4j requires and
partially includes a build chain for Win32. launch4j cannot be ran on any
64-bit only version of OSX (10.6 and up) and is difficult to run on Windows.

BitcoinExcavator's git repo does not contian binaries, so if you want to build
BitcoinExcavator from source, heed the above warning and run `mvn package`.

#Warning#
BitcoinExcavator is not run only graphical mode it also has command line program. 
This means you need to open a terminal (cmd.exe, Terminal.app/iTerm2, xterm, etc)
and run the program with the proper arguments from there.

You __require__ hardware that is capable of running OpenCL.

#MacOSX Warning#
Apple's OpenCL drivers often exhibit strange bugs.

If BitcoinExcavator is displaying warnings about `CL_INVALID_WORK_GROUP_SIZE`, add
`-w 64` to your arguments.


#How to use terminal mode#
`./BitcoinExcavator.sh -u username -p password -o host -r port`

or

`./BitcoinExcavator.sh -l http://username:password@host:port`

For solo mining, your host is `localhost` your port is `8332`, and your
username and password is what you set in your bitcoind's `bitcoin.conf`.
See bitcoin documentation for further information on how to enable the RPC
API.


#Optimization#
##MacOSX on any hardware##
It is recommended you use the default settings, see also the MacOSX warning
above.

##Nvidia hardware on any OS##
It is recommended you use the default settings, however if your desktop is
lagging badly try adding `-f 120`, `-f 180`, or `-f 240`.

##Intel hardware on any OS##
BitcoinExcavator has not been tested on Intel GPUs. Default is most likely correct.

##AMD Radeon VLIW4##
R7xx

* Radeon discrete: 43xx, 45xx, 46xx, 47xx, 48xx
* Radeon mobile: 43xxM, 45xxM, 46xxM, 48xxM, 5xxV, 51xxM
* FirePro: V3750, V7750, V8700, V8750, 2450, RG220
* FirePro mobile: M7740
* FireStream: 9250, 9270

`-v 2`

##AMD Radeon VLIW5##
Cedar, Redwood, Juniper, Cypress, Hemlock

* Radeon discrete: 54xx, 55xx, 56xx, 57xx, 58xx, 59xx, 63xx, 6750, 6770, 73xx
* Radeon mobile: 54xxM, 56xxM, 57xxM, 58xxM, 63xxM, 65xxM, 68xxM,
* FirePro: V3800, V4800, V5800, V7800, V8800, V9800, 2270, 2460
* FirePro mobile: M5800, M7820
* FireStream: 9350, 9370

On some cards `-v 2,1` is faster, on others `-v 2`. Try both.

##AMD Radeon VLIW5 Gen2##
Caicos, Turks, Barts, Wrestler, Ontario, Zacate, WinterPark, BeaverCreek

* Radeon discrete: 64xx, 65xx, 66xx, 6790, 68xx, 74xx, 75xx, 76xx
* Radeon mobile: 64xxM, 66xxM, 67xxM, 69xxM, 74xxM, 75xxM, 76xxM
* Radeon mobile IGP: 63xxG, 64xxG, 65xxG, 66xxG
* Fusion APU: 62xx, 63xx, 64xxD, 65xxD, 74xxD, 75xxD, 76xxD
* FirePro: V3900, V4900
* FirePro mobile: M5950, M8900, M2000

Use `-v 2`

##AMD Radeon VLIW4
Cayman, Antilles, Devastator

* Radeon discrete: 69xx
* Radeon mobile: 64xxM, 66xxM, 67xxM
* Fusion APU: 74xxD
* FirePro: V5900, V7900

Use `-v 2`

##AMD Radeon GCN
Cape Verde, Pitcairn, Tahiti, New Zealand, Malta, Bonaire
* Radeon discrete: 77xx, 78xx, 79xx
* Radeon mobile: 77xxM, 78xxM, 79xxM
* FirePro: W600, W5000, W7000, W8000, W9000, S7000, S9000, S10000, R5000
* FirePro mobile: M4000, M6000
* Sky: 500, 700, 900

Use defaults.

#Command line arugments#
* __-u, -p, -o, -r__ Username, password, host, port
* __-l__ Fully fledged URL, ex: http://username:password@host:port/
* __-x__ Proxy, ex: host:port<:username:password>
* __-d__ Debug output
* __-D__ Use specific devices. Default is all.
* __-f__ FPS, controls how many kernel executions a second happen, default is 30
* __-w__ Controls OpenCL workgroup size, default is hardware detected maximum
* __-v__: Change manual SIMD parallel alignment.
 * -v 1: The same as off/single hash, and is the default (`uint`)
 * -v 2 through 16: Tries more than one hash via SIMD (`uint2` through `uint16`)
 * -v 1,1, etc: Non-SIMD interleaving on top of SIMD, faster on a very small
   minority of hardware (`-v 2,1` is faster than `-v 2` on some Radeon VLIW5)


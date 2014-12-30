PVStar for Spigot 1.8
=============

(PV* - Player versus anything) 

*Under heavy development, expect large changes.*

A versatile arena framework designed to be extended via modules and extensions.

## History

PV-Star originally started out as a private arena plugin whose main extensibility was through extending an abstract class to create different types of arenas. Unfortunately, as requirements and features were added, the plugin became more difficult to manage and less flexible.

This new iteration of PV-Star solves many of the problems of the old one by trying to be extremely simple and providing a framework for extending the functionality via modularity. Instead of extending a base class, PV-Star has one arena type that does almost nothing except basic arena functions. Customizing an arenas functionality is done by adding extensions.

Many of the features that were removed are currently being developed as separate modules in the [PV-Star-Modules](https://github.com/JCThePants/PV-Star-Modules) project.

## Definitions

 * Module - Extends PV-Star functionality by adding new types, extensions, commands or anything else.
 * Extension - Can be applied to one or more arenas to modify and extend its behavior.

## Features of PV-Star

  * Arena designer can easily create new arena types by mixing, matching and or adding extensions.

  * Scripting supported via NucleusFramework.

  * Built in team support.
  
  * Built in localization support via NucleusFramework.

## Resources
  * [PVStar-API](https://github.com/JCThePants/PV-StarAPI) - The API that module and extension developers should use.
  * [PVStar Modules](https://github.com/JCThePants/PV-Star-Modules) - A collection of pre-made modules and extensions.

## Plugin dependencies

  * [NucleusFramework](https://github.com/JCThePants/NucleusFramework)

## Build dependencies

See the [gradle build script](https://github.com/JCThePants/PV-Star/blob/master/build.gradle) for build dependencies.


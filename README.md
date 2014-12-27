PVStar for Spigot 1.8
=============

(PV* - Player versus anything) 

*Under heavy development, expect large changes.*

A basic arena framework designed to be extended via modules and extensions. 

PV-Star originally started out as a feature rich arena plugin whose main extensibility was through extending an abstract arena class to create different types of arenas. Unfortunately, as requirements and features were added, the plugin became more difficult to manage and less flexible.

This new iteration of PV-Star solves many of the problems of the old one by trying to be extremely simple and providing a framework for extending the functionality via modularity. Instead of extending a base class, PV-Star has one arena type that does almost nothing except basic arena functions. Customizing an arenas functionality is done by adding extensions.

Many of the features that were removed are currently being developed as separate modules in the [PV-Star-Modules](https://github.com/JCThePants/PV-Star-Modules) project.

**Definitions**

 * Module - Extends PV-Star functionality by adding new types, extensions, commands or anything else.
 * Extension - Can be applied to one or more arenas to modify and extend its behavior.



**Features of PV-Star**

  * Add functionality through modules and arena extensions.

  * Modules can specify hard and soft dependencies for both Bukkit plugins and other PV-Star Modules. 

  * Uses its own module loader instead of relying on Bukkit plugins. This gives greater control over the loading process and ensures that modules are not enabled until all modules are ready.
  
  * Uses Nucleus event manager. This allows each arena to have its own event manager. Registering event handlers with the arenas event manager ensures the handler will only be called if the event is called on the arena. The arena event managers are children of the PV-Star event manager, which receives calls from the arena event managers. An event handler registered with PV-Star's event manager will receive events from all arenas.
  
  * Scripting supported by adding a script api to NucleusFramework for scripts to use.
  
  * Handles core statistics but leaves adding statistics types and incrementing to modules/extensions.
  
  * Handles core points system but leaves adding new points types and incrementing to modules/extensions.
   
  * Uses 3 integrated spawn types: Lobby, Game, and Spectator. Allows adding new types of spawns, which are primarily used to mark locations, but can also spawn entities depending on the spawn types implementation. The spawns can be used by scripts.
  
  * New commands can be added and sub commands can be added to existing commands.
  
  * Built in team support.
  
  * Built in localization support via Nucleus.



*Dependencies*

  * [PV-StarAPI](https://github.com/JCThePants/PV-StarAPI) (compile dependency)
  
  * [NucleusFramework](https://github.com/JCThePants/NucleusFramework) (provided dependency)
  
  * Spigot 1.8  (provided dependency)


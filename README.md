PVStar for Bukkit 1.7.9
=============

(PV* - Player versus anything) 

*Under heavy development, expect large changes.*

A basic arena framework designed to be extended via modules and extensions. 

PV-Star originally started out as a feature rich arena plugin whose main extensibility was through extending an abstract arena class to create different types of arenas. Unfortunately, as requirements and features were added, the plugin became more difficult to manage and less flexible.

This new iteration of PV-Star solves many of the problems of the old one by trying to be extremely simple and providing a framework for extending the functionality via modularity.

*Major Features of PV-Star:*

  * Add functionality through jar file modules.
   
  * Modules can add arena extensions. Arena extensions can be selectively added to an arena in order to extend or modify its behavior. 

  * Modules can specify hard and soft dependencies for both Bukkit plugins and other PV-Star Modules. 

  * Uses its own module loader instead of relying on Bukkit plugins. This gives greater control over the loading process and ensures that modules are not enabled until all modules are ready.
  
  * Uses GenericsLib event manager. This allows each arena to have its own event manager. Registering event handlers with the arenas event manager ensures the handler will only be called if the event is called on the arena. The arena event managers are children of the PV-Star event manager, which receives calls from the arena event managers. An event handler registered with PV-Star's event manager will receive events from all arenas.
  
  * Uses GenericsLib scripting and scripting api. Scripts can be loaded and evaluated for a specific arena. Script API's can be registered via modules which gives scripts access to the modules features. The scripts also have access to the GenericsLib script repository so they can get access to scripts registered by other Bukkit plugins that utilize GenericsLib.
  
  * Handles core statistics recording. New statistics types can be registered.
   
  * Allows adding new types of spawns, which are primarily used to mark locations, but can also spawn entities depending on the spawn types implementation.
  
  * Handles core points. New point types can be registered.
  
  * New commands can be added and sub commands can be added to existing commands.
  
  * Built in team support.


*Dependencies*

  * PV-StarAPI (compile)
  
  * GenericsLib (provided)
  
  * Bukkit API 1.7.9  (provided)


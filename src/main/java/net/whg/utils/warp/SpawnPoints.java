package net.whg.utils.warp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

/**
 * Contains a list of spawn locations for each existing world that is
 * synchronized with a corresponding config file.
 */
public class SpawnPoints {
    /**
     * A very small event listener that triggers the spawn point list to load once
     * the server has finished starting, then unregisters itself.
     */
    private class LoadSpawnPoints implements Listener {
        @EventHandler
        public void onServerLoad(ServerLoadEvent e) {
            loadList();
            HandlerList.unregisterAll(this);
        }

        /**
         * Loads all spawn points from the config file.
         */
        private void loadList() {
            var plugin = Bukkit.getPluginManager().getPlugin("WraithLib");
            configFile = new File(plugin.getDataFolder(), "spawns.yml");
            config = YamlConfiguration.loadConfiguration(configFile);

            var savedSpawnPoints = config.getConfigurationSection("");
            if (savedSpawnPoints != null) {
                for (var worldName : savedSpawnPoints.getKeys(false)) {
                    var spawnPoint = (Location) savedSpawnPoints.get(worldName);
                    locations.put(worldName, spawnPoint);
                }
            }

            globalSpawn = config.getLocation("Global_Spawn");
        }
    }

    private final Map<String, Location> locations = new HashMap<>();
    private Location globalSpawn;
    private YamlConfiguration config;
    private File configFile;

    /**
     * Creates a new SpawnPoints instance and loads all existing spawn points from
     * the config file.
     */
    public SpawnPoints() {
        var plugin = Bukkit.getPluginManager().getPlugin("WraithLib");
        Bukkit.getPluginManager().registerEvents(new LoadSpawnPoints(), plugin);
    }

    /**
     * Gets the spawn location of the given world. If the world has no currently
     * saved spawn location, then the default world spawn location is returned.
     * 
     * @param worldName - The world name.
     * @return The spawn location, or null if the world does not exist.
     */
    public Location getSpawnPoint(String worldName) {
        if (locations.containsKey(worldName))
            return locations.get(worldName);

        var world = Bukkit.getWorld(worldName);
        if (world == null)
            return null;

        return world.getSpawnLocation();
    }

    /**
     * Gets the spawn location of the given world. If the world has no currently
     * saved spawn location, then the default world spawn location is returned.
     * 
     * @param world - The world.
     * @return The spawn location.
     */
    public Location getSpawnPoint(World world) {
        var worldName = world.getName();
        if (locations.containsKey(worldName))
            return locations.get(worldName);

        return world.getSpawnLocation();
    }

    /**
     * Sets the spawn location for the given world.
     * 
     * @param worldName - The name of the world to adjust.
     * @param location  - The location within that world to be the new spawn point.
     * @throws IOException If there is an IO issue while updating the config file.
     */
    public void setSpawnPoint(String worldName, Location location) throws IOException {
        locations.put(worldName, location);

        config.set(worldName, location);
        config.save(configFile);
    }

    /**
     * Gets the global spawn location that is assigned to this server. This is a
     * spawn location that ideally represents the "hub" point of the server. This is
     * usually where players will spawn when joining the server for the first time.
     * 
     * @return The global spawn location.
     */
    public Location getGlobalSpawn() {
        return globalSpawn;
    }

    /**
     * Sets the global spawn location and saves it to the config.
     * 
     * @param location - The new global spawn location.
     * @see #getGlobalSpawn()
     */
    public void setGlobalSpawn(Location location) throws IOException {
        globalSpawn = location;

        config.set("Global_Spawn", location);
        config.save(configFile);
    }
}

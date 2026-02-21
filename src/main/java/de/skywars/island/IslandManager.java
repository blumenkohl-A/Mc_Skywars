package de.skywars.island;

import de.skywars.SkyWarsPlugin;
import de.skywars.team.SkyTeam;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class IslandManager {

    private final SkyWarsPlugin plugin;

    public IslandManager(SkyWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public Location getLobbySpawn() {
        return loadLocation("lobby-spawn");
    }

    public void setLobbySpawn(Location loc) {
        saveLocation("lobby-spawn", loc);
        plugin.saveConfig();
    }

    public Location getTeamSpawn(SkyTeam team) {
        return loadLocation("island-spawns." + team.name().toLowerCase());
    }

    public void setTeamSpawn(SkyTeam team, Location loc) {
        saveLocation("island-spawns." + team.name().toLowerCase(), loc);
        plugin.saveConfig();
    }

    public boolean allSpawnsSet() {
        if (getLobbySpawn() == null) return false;
        for (SkyTeam t : SkyTeam.values()) {
            if (getTeamSpawn(t) == null) return false;
        }
        return true;
    }

    // ---- Internal ----------------------------------------------------------

    private Location loadLocation(String path) {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.contains(path + ".world")) return null;
        String worldName = cfg.getString(path + ".world");
        if (worldName == null) return null;
        var world = plugin.getServer().getWorld(worldName);
        if (world == null) return null;
        double x = cfg.getDouble(path + ".x");
        double y = cfg.getDouble(path + ".y");
        double z = cfg.getDouble(path + ".z");
        float yaw = (float) cfg.getDouble(path + ".yaw");
        float pitch = (float) cfg.getDouble(path + ".pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    private void saveLocation(String path, Location loc) {
        FileConfiguration cfg = plugin.getConfig();
        cfg.set(path + ".world", loc.getWorld().getName());
        cfg.set(path + ".x", loc.getX());
        cfg.set(path + ".y", loc.getY());
        cfg.set(path + ".z", loc.getZ());
        cfg.set(path + ".yaw", (double) loc.getYaw());
        cfg.set(path + ".pitch", (double) loc.getPitch());
    }
}

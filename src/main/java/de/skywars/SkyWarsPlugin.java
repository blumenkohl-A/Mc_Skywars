package de.skywars;

import de.skywars.command.SkyWarsCommand;
import de.skywars.game.GameManager;
import de.skywars.island.IslandManager;
import de.skywars.kit.KitManager;
import de.skywars.listener.GeneralProtectionListener;
import de.skywars.listener.PlayerDeathListener;
import de.skywars.listener.PlayerJoinListener;
import de.skywars.team.TeamManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SkyWarsPlugin extends JavaPlugin {

    private GameManager gameManager;
    private TeamManager teamManager;
    private IslandManager islandManager;
    private KitManager kitManager;

    @Override
    public void onEnable() {
        getLogger().info("---------------------------------------");
        getLogger().info("SkyWars wird geladen...");

        saveDefaultConfig();

        // Initialisierung
        try {
            teamManager = new TeamManager();
            islandManager = new IslandManager(this);
            kitManager = new KitManager();
            gameManager = new GameManager(this, teamManager, islandManager, kitManager);

            // Command Registrierung
            SkyWarsCommand swCmd = new SkyWarsCommand(gameManager, islandManager);
            
            String[] commands = {"skywars", "setlobby", "setisland", "swstart"};
            for (String cmdName : commands) {
                PluginCommand pc = getCommand(cmdName);
                if (pc != null) {
                    pc.setExecutor(swCmd);
                    pc.setTabCompleter(swCmd);
                    getLogger().info("Registriert: /" + cmdName);
                } else {
                    getLogger().warning("Konnte /" + cmdName + " NICHT in plugin.yml finden!");
                }
            }

            // Events
            getServer().getPluginManager().registerEvents(new PlayerJoinListener(gameManager), this);
            getServer().getPluginManager().registerEvents(new PlayerDeathListener(gameManager), this);
            getServer().getPluginManager().registerEvents(new GeneralProtectionListener(gameManager), this);

            getLogger().info("SkyWars Plugin erfolgreich aktiviert!");
        } catch (Exception e) {
            getLogger().severe("FEHLER beim Starten von SkyWars: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
        getLogger().info("---------------------------------------");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.stopGame();
        }
        getLogger().info("SkyWars Plugin deaktiviert.");
    }
}

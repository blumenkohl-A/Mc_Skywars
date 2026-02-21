package de.skywars.listener;

import de.skywars.game.GameManager;
import de.skywars.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerDeathListener implements Listener {

    private final GameManager gameManager;

    public PlayerDeathListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        if (gameManager.getState() != GameState.RUNNING) return;

        var player = event.getPlayer();
        // Drop items naturally
        event.setKeepInventory(false);
        event.setKeepLevel(false);

        gameManager.eliminatePlayer(player);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        if (gameManager.getState() == GameState.RUNNING && !gameManager.isAlive(player)) {
            // Respawn in spectator — GameManager handles putting them in spectator mode
            // We just ensure they respawn at their current position (handled by game manager's delayed task)
        }
    }
}

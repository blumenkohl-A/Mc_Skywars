package de.skywars.listener;

import de.skywars.game.GameManager;
import de.skywars.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final GameManager gameManager;

    public PlayerJoinListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        // Only send to lobby if game is not currently running
        if (gameManager.getState() != GameState.RUNNING) {
            gameManager.sendToLobby(player);
        } else {
            // Spectator for late joiners
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);
            player.sendMessage("§cEin Spiel lauft bereits. Du bist Zuschauer.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        gameManager.handlePlayerLeave(event.getPlayer());
    }
}

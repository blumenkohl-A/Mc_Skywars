package de.skywars.listener;

import de.skywars.game.GameManager;
import de.skywars.game.GameState;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class GeneralProtectionListener implements Listener {

    private final GameManager gameManager;

    public GeneralProtectionListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    /** No PvP in lobby or countdown */
    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (gameManager.getState() != GameState.RUNNING) {
            if (event.getDamager() instanceof org.bukkit.entity.Player) {
                event.setCancelled(true);
            }
        }
    }

    /** No hunger loss in lobby / countdown */
    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (gameManager.getState() != GameState.RUNNING) {
            event.setFoodLevel(20);
            event.setCancelled(true);
        }
    }

    /** No block breaking in lobby / countdown */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (gameManager.getState() != GameState.RUNNING) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }

    /** No block placing in lobby / countdown */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (gameManager.getState() != GameState.RUNNING) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        }
    }

    /** No item dropping in lobby */
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (gameManager.getState() == GameState.LOBBY ||
                gameManager.getState() == GameState.COUNTDOWN) {
            event.setCancelled(true);
        }
    }
}

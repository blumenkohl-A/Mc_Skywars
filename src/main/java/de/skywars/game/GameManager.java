package de.skywars.game;

import de.skywars.SkyWarsPlugin;
import de.skywars.island.IslandManager;
import de.skywars.kit.KitManager;
import de.skywars.team.SkyTeam;
import de.skywars.team.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {

    private final SkyWarsPlugin plugin;
    private final TeamManager teamManager;
    private final IslandManager islandManager;
    private final KitManager kitManager;

    private GameState state = GameState.LOBBY;
    private final Set<UUID> alivePlayers = new HashSet<>();
    private BukkitTask countdownTask;
    private int countdownSeconds;

    public GameManager(SkyWarsPlugin plugin,
                       TeamManager teamManager,
                       IslandManager islandManager,
                       KitManager kitManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
        this.islandManager = islandManager;
        this.kitManager = kitManager;

        teamManager.setupScoreboard(
                Objects.requireNonNull(Bukkit.getScoreboardManager()));
    }

    // =========================================================================
    // State accessors
    // =========================================================================

    public GameState getState() { return state; }

    public boolean isAlive(Player player) {
        return alivePlayers.contains(player.getUniqueId());
    }

    public Set<UUID> getAlivePlayers() {
        return Collections.unmodifiableSet(alivePlayers);
    }

    // =========================================================================
    // Lobby
    // =========================================================================

    public void sendToLobby(Player player) {
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        Location lobby = islandManager.getLobbySpawn();
        if (lobby != null) player.teleport(lobby);

        if (state == GameState.LOBBY) {
            // Assign team in lobby phase
            teamManager.assignTeam(player);
        }

        // Start lobby countdown if enough players
        if (state == GameState.LOBBY) {
            checkLobbyStart();
        }
    }

    public void handlePlayerLeave(Player player) {
        teamManager.removePlayer(player);
        alivePlayers.remove(player.getUniqueId());

        if (state == GameState.RUNNING) {
            checkWinCondition();
        } else if (state == GameState.LOBBY || state == GameState.COUNTDOWN) {
            checkLobbyStart();
        }
    }

    private void checkLobbyStart() {
        int online = Bukkit.getOnlinePlayers().size();
        int min = plugin.getConfig().getInt("min-players", 2);

        if (online >= min && state == GameState.LOBBY) {
            startLobbyCountdown();
        } else if (online < min && state == GameState.COUNTDOWN) {
            cancelCountdown();
            broadcast(Component.text("[SkyWars] Zu wenige Spieler. Warte auf mehr Spieler...",
                    NamedTextColor.YELLOW));
        }
    }

    // =========================================================================
    // Lobby-Countdown
    // =========================================================================

    private void startLobbyCountdown() {
        state = GameState.COUNTDOWN;
        int lobbySeconds = plugin.getConfig().getInt("lobby-countdown-seconds", 60);
        countdownSeconds = lobbySeconds;

        broadcast(Component.text("[SkyWars] Spiel startet in " + lobbySeconds + " Sekunden!",
                NamedTextColor.GREEN));

        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (countdownSeconds <= 0) {
                    startGame();
                    cancel();
                    return;
                }
                if (countdownSeconds <= 10 || countdownSeconds % 10 == 0) {
                    broadcast(Component.text("[SkyWars] Start in " + countdownSeconds + "s...",
                            NamedTextColor.GOLD));
                }
                countdownSeconds--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        state = GameState.LOBBY;
        countdownSeconds = 0;
    }

    // =========================================================================
    // Game Start
    // =========================================================================

    public void startGame() {
        if (!islandManager.allSpawnsSet()) {
            broadcast(Component.text("[SkyWars] Fehler: Nicht alle Spawn-Punkte gesetzt! Benutze /skywars setisland.",
                    NamedTextColor.RED));
            cancelCountdown();
            return;
        }

        state = GameState.RUNNING;
        alivePlayers.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            SkyTeam team = teamManager.getTeam(player);
            if (team == null) {
                team = teamManager.assignTeam(player);
            }
            Location spawn = islandManager.getTeamSpawn(team);
            if (spawn != null) {
                player.teleport(spawn);
            }
            player.setGameMode(GameMode.SURVIVAL);
            kitManager.giveKit(player, team);
            alivePlayers.add(player.getUniqueId());
        }

        broadcast(Component.text("=====================================", NamedTextColor.GOLD));
        broadcast(Component.text("       SKYWARS - Kampf beginnt!      ", NamedTextColor.YELLOW));
        broadcast(Component.text("  Ziel: Eliminiere alle anderen Teams!", NamedTextColor.WHITE));
        broadcast(Component.text("=====================================", NamedTextColor.GOLD));
    }

    // =========================================================================
    // Player Death / Elimination
    // =========================================================================

    public void eliminatePlayer(Player player) {
        if (!alivePlayers.contains(player.getUniqueId())) return;

        alivePlayers.remove(player.getUniqueId());
        SkyTeam team = teamManager.getTeam(player);

        String teamPrefix = team != null ? team.getColor() + "[" + team.getDisplayName() + "] " : "";
        broadcast(Component.text(teamPrefix + player.getName() + " wurde eliminiert!",
                NamedTextColor.GRAY));

        // Put into spectator after a short delay (to show death animation)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.setGameMode(GameMode.SPECTATOR);
                Location lobby = islandManager.getLobbySpawn();
                if (lobby != null) player.teleport(lobby);
                player.sendMessage("§cDu wurdest eliminiert! Du bist jetzt Zuschauer.");
            }
        }, 40L);

        checkWinCondition();
    }

    private void checkWinCondition() {
        if (state != GameState.RUNNING) return;
        SkyTeam winner = teamManager.checkWinner(alivePlayers);
        if (winner != null) {
            endGame(winner);
        } else if (alivePlayers.isEmpty()) {
            endGame(null);
        }
    }

    // =========================================================================
    // Game End
    // =========================================================================

    public void endGame(SkyTeam winner) {
        state = GameState.ENDED;

        broadcast(Component.text("=====================================", NamedTextColor.GOLD));
        if (winner != null) {
            broadcast(Component.text("  " + winner.getColor() + "Team " + winner.getDisplayName() +
                    " §ehat gewonnen!", NamedTextColor.YELLOW));
        } else {
            broadcast(Component.text("  §cUnentschieden! Kein Sieger!", NamedTextColor.RED));
        }
        broadcast(Component.text("=====================================", NamedTextColor.GOLD));

        // Reset after 10 seconds
        Bukkit.getScheduler().runTaskLater(plugin, this::resetGame, 200L);
    }

    public void stopGame() {
        cancelCountdown();
        alivePlayers.clear();
        resetGame();
        broadcast(Component.text("[SkyWars] Spiel wurde abgebrochen.", NamedTextColor.RED));
    }

    private void resetGame() {
        state = GameState.LOBBY;
        alivePlayers.clear();
        teamManager.reset();
        teamManager.setupScoreboard(
                Objects.requireNonNull(Bukkit.getScoreboardManager()));

        // Send all players back to lobby
        Location lobby = islandManager.getLobbySpawn();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();
            player.setHealth(20.0);
            player.setFoodLevel(20);
            if (lobby != null) player.teleport(lobby);
            teamManager.assignTeam(player);
            player.sendMessage("§aNeues Spiel beginnt bald. Du wurdest einem Team zugewiesen!");
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void broadcast(Component msg) {
        Bukkit.broadcast(msg);
    }

    public TeamManager getTeamManager() { return teamManager; }
}

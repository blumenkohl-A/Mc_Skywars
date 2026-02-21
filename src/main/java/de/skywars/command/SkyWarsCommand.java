package de.skywars.command;

import de.skywars.game.GameManager;
import de.skywars.game.GameState;
import de.skywars.island.IslandManager;
import de.skywars.team.SkyTeam;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SkyWarsCommand implements CommandExecutor, TabCompleter {

    private final GameManager gameManager;
    private final IslandManager islandManager;

    public SkyWarsCommand(GameManager gameManager, IslandManager islandManager) {
        this.gameManager = gameManager;
        this.islandManager = islandManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        String name = command.getName().toLowerCase();
        
        // Konsolen-Log zur Diagnose
        Bukkit.getLogger().info("[SkyWars-Debug] Befehl empfangen: /" + label + " " + String.join(" ", args));

        // 1. Shortcuts
        if (name.equals("setlobby")) {
            handleSetLobby(sender);
            return true;
        }
        if (name.equals("setisland")) {
            handleSetIsland(sender, args);
            return true;
        }
        if (name.equals("swstart")) {
            handleStart(sender);
            return true;
        }

        // 2. Hauptbefehl /skywars
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "info" -> {
                msg(sender, "§6[SkyWars] §eStatus: §f" + gameManager.getState());
                msg(sender, "§6[SkyWars] §eSpieler: §f" + gameManager.getAlivePlayers().size());
            }
            case "debug" -> msg(sender, "§a[SkyWars] Plugin Version 1.0.2 ist aktiv!");
            case "setlobby" -> handleSetLobby(sender);
            case "setisland" -> handleSetIsland(sender, shift(args));
            case "start" -> handleStart(sender);
            case "stop" -> handleStop(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void handleSetLobby(CommandSender sender) {
        if (!sender.hasPermission("skywars.admin")) {
            msg(sender, "§cKeine Rechte (skywars.admin).");
            return;
        }
        if (!(sender instanceof Player p)) {
            msg(sender, "§cNur fuer Spieler.");
            return;
        }
        islandManager.setLobbySpawn(p.getLocation());
        msg(sender, "§a[SkyWars] Lobby-Spawn gesetzt!");
        Bukkit.getLogger().info("[SkyWars] Lobby gesetzt von " + p.getName());
    }

    private void handleSetIsland(CommandSender sender, String[] args) {
        if (!sender.hasPermission("skywars.admin")) {
            msg(sender, "§cKeine Rechte.");
            return;
        }
        if (!(sender instanceof Player p)) {
            msg(sender, "§cNur fuer Spieler.");
            return;
        }
        if (args.length < 1) {
            msg(sender, "§cNutze: /setisland <red|blue|green|yellow>");
            return;
        }
        SkyTeam team = SkyTeam.fromString(args[0]);
        if (team == null) {
            msg(sender, "§cTeam '" + args[0] + "' existiert nicht.");
            return;
        }
        islandManager.setTeamSpawn(team, p.getLocation());
        msg(sender, "§a[SkyWars] Spawn fuer Team " + team.getColoredName() + " §agesetzt!");
    }

    private void handleStart(CommandSender sender) {
        if (!sender.hasPermission("skywars.admin")) return;
        msg(sender, "§eVersuche Spiel zu starten...");
        gameManager.startGame();
    }

    private void handleStop(CommandSender sender) {
        if (!sender.hasPermission("skywars.admin")) return;
        gameManager.stopGame();
        msg(sender, "§cSpiel abgebrochen.");
    }

    private void msg(CommandSender sender, String text) {
        // Nutze die klassische Methode zur Sicherheit
        sender.sendMessage(text);
    }

    private void sendHelp(CommandSender sender) {
        msg(sender, "§6--- SkyWars Befehle ---");
        msg(sender, "§e/sw info §7- Status");
        msg(sender, "§e/sw setlobby §7- Lobby setzen");
        msg(sender, "§e/sw setisland <team> §7- Insel setzen");
        msg(sender, "§e/sw start §7- Starten");
    }

    private String[] shift(String[] args) {
        if (args.length <= 1) return new String[0];
        return Arrays.copyOfRange(args, 1, args.length);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.addAll(Arrays.asList("info", "debug", "setlobby", "setisland", "start", "stop"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("setisland")) {
            for (SkyTeam t : SkyTeam.values()) list.add(t.name().toLowerCase());
        }
        return list.stream().filter(s -> s.startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
    }
}

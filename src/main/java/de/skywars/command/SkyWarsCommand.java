package de.skywars.command;

import de.skywars.game.GameManager;
import de.skywars.game.GameState;
import de.skywars.island.IslandManager;
import de.skywars.team.SkyTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

        // 1. Check Shortcuts
        if (name.equals("setlobby")) return handleSetLobby(sender);
        if (name.equals("setisland")) return handleSetIsland(sender, args);
        if (name.equals("swstart")) return handleStart(sender);

        // 2. Handle /skywars <subcommand>
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
            case "debug" -> {
                msg(sender, "§a[SkyWars] Plugin ist aktiv und reagiert!");
                msg(sender, "§7Version: 1.0.0 | JDK 21+ Build");
            }
            case "setlobby" -> handleSetLobby(sender);
            case "setisland" -> handleSetIsland(sender, shift(args));
            case "start" -> handleStart(sender);
            case "stop" -> handleStop(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    private boolean handleSetLobby(CommandSender sender) {
        if (!sender.hasPermission("skywars.admin")) {
            msg(sender, "§cKeine Berechtigung (skywars.admin).");
            return true;
        }
        if (!(sender instanceof Player p)) {
            msg(sender, "§cNur Spieler koennen Spawns setzen.");
            return true;
        }
        islandManager.setLobbySpawn(p.getLocation());
        msg(sender, "§a[SkyWars] Lobby-Spawn erfolgreich gesetzt!");
        return true;
    }

    private boolean handleSetIsland(CommandSender sender, String[] args) {
        if (!sender.hasPermission("skywars.admin")) {
            msg(sender, "§cKeine Berechtigung.");
            return true;
        }
        if (!(sender instanceof Player p)) {
            msg(sender, "§cNur Spieler.");
            return true;
        }
        if (args.length < 1) {
            msg(sender, "§cBenutzung: /skywars setisland <red|blue|green|yellow>");
            return true;
        }
        SkyTeam team = SkyTeam.fromString(args[0]);
        if (team == null) {
            msg(sender, "§cTeam '" + args[0] + "' nicht gefunden.");
            return true;
        }
        islandManager.setTeamSpawn(team, p.getLocation());
        msg(sender, "§a[SkyWars] Spawn fuer Team " + team.getColoredName() + " §agesetzt!");
        return true;
    }

    private boolean handleStart(CommandSender sender) {
        if (!sender.hasPermission("skywars.admin")) return true;
        if (gameManager.getState() == GameState.RUNNING) {
            msg(sender, "§cDas Spiel lauft bereits.");
            return true;
        }
        gameManager.startGame();
        return true;
    }

    private boolean handleStop(CommandSender sender) {
        if (!sender.hasPermission("skywars.admin")) return true;
        gameManager.stopGame();
        msg(sender, "§eSpiel wurde abgebrochen.");
        return true;
    }

    private void msg(CommandSender sender, String text) {
        sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(text));
    }

    private void sendHelp(CommandSender sender) {
        msg(sender, "§6--- SkyWars Hilfe ---");
        msg(sender, "§e/sw info §7- Status");
        msg(sender, "§e/sw debug §7- Test");
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
        List<String> completions = new ArrayList<>();
        String name = command.getName().toLowerCase();

        if (name.equals("skywars") || name.equals("sw")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList("info", "debug", "setlobby", "setisland", "start", "stop"));
            } else if (args.length == 2 && args[0].equalsIgnoreCase("setisland")) {
                for (SkyTeam t : SkyTeam.values()) completions.add(t.name().toLowerCase());
            }
        } else if (name.equals("setisland") && args.length == 1) {
            for (SkyTeam t : SkyTeam.values()) completions.add(t.name().toLowerCase());
        }

        return completions.stream().filter(s -> s.startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
    }
}

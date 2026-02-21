package de.skywars.team;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public class TeamManager {

    private final Map<SkyTeam, Set<UUID>> teams = new EnumMap<>(SkyTeam.class);
    private final Map<UUID, SkyTeam> playerTeam = new HashMap<>();
    private Scoreboard scoreboard;

    public TeamManager() {
        for (SkyTeam t : SkyTeam.values()) {
            teams.put(t, new HashSet<>());
        }
    }

    // ---- Board Setup -------------------------------------------------------

    public void setupScoreboard(ScoreboardManager manager) {
        scoreboard = manager.getNewScoreboard();
        for (SkyTeam skyTeam : SkyTeam.values()) {
            Team sbTeam = scoreboard.registerNewTeam(skyTeam.name());
            sbTeam.setDisplayName(skyTeam.getColoredName());
            sbTeam.setColor(skyTeam.getColor());
            sbTeam.setPrefix(skyTeam.getColor() + "[" + skyTeam.getDisplayName() + "] ");
            sbTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        }
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    // ---- Team Assignment ---------------------------------------------------

    /**
     * Assigns the given player to the team with the fewest members.
     */
    public SkyTeam assignTeam(Player player) {
        SkyTeam smallest = null;
        int min = Integer.MAX_VALUE;
        for (SkyTeam t : SkyTeam.values()) {
            int size = teams.get(t).size();
            if (size < min) {
                min = size;
                smallest = t;
            }
        }
        return addToTeam(player, smallest);
    }

    public SkyTeam addToTeam(Player player, SkyTeam team) {
        // Remove from old team first
        SkyTeam old = playerTeam.get(player.getUniqueId());
        if (old != null) {
            teams.get(old).remove(player.getUniqueId());
            Team sbOld = scoreboard.getTeam(old.name());
            if (sbOld != null) sbOld.removeEntry(player.getName());
        }

        teams.get(team).add(player.getUniqueId());
        playerTeam.put(player.getUniqueId(), team);

        Team sbTeam = scoreboard.getTeam(team.name());
        if (sbTeam != null) sbTeam.addEntry(player.getName());

        player.setScoreboard(scoreboard);
        player.sendMessage(team.getColor() + "Du wurdest Team " + team.getColoredName() + team.getColor() + " zugewiesen!");
        return team;
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        SkyTeam team = playerTeam.remove(uuid);
        if (team != null) {
            teams.get(team).remove(uuid);
            Team sbTeam = scoreboard.getTeam(team.name());
            if (sbTeam != null) sbTeam.removeEntry(player.getName());
        }
    }

    public SkyTeam getTeam(Player player) {
        return playerTeam.get(player.getUniqueId());
    }

    public Set<UUID> getTeamMembers(SkyTeam team) {
        return Collections.unmodifiableSet(teams.get(team));
    }

    // ---- Win Condition -----------------------------------------------------

    /**
     * Returns the winning team if only one team still has alive players, else null.
     */
    public SkyTeam checkWinner(Set<UUID> alivePlayers) {
        SkyTeam winner = null;
        for (SkyTeam t : SkyTeam.values()) {
            Set<UUID> members = teams.get(t);
            boolean hasAlive = members.stream().anyMatch(alivePlayers::contains);
            if (hasAlive) {
                if (winner != null) return null; // More than one team alive
                winner = t;
            }
        }
        return winner;
    }

    public void reset() {
        for (SkyTeam t : SkyTeam.values()) {
            teams.get(t).clear();
        }
        playerTeam.clear();
        if (scoreboard != null) {
            for (SkyTeam t : SkyTeam.values()) {
                Team sbTeam = scoreboard.getTeam(t.name());
                if (sbTeam != null) sbTeam.unregister();
            }
        }
    }
}

package de.skywars.team;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

public enum SkyTeam {
    RED("Rot", ChatColor.RED, DyeColor.RED, '\u2764'),
    BLUE("Blau", ChatColor.BLUE, DyeColor.BLUE, '\u2666'),
    GREEN("Grun", ChatColor.GREEN, DyeColor.GREEN, '\u2663'),
    YELLOW("Gelb", ChatColor.YELLOW, DyeColor.YELLOW, '\u2605');

    private final String displayName;
    private final ChatColor color;
    private final DyeColor dyeColor;
    private final char symbol;

    SkyTeam(String displayName, ChatColor color, DyeColor dyeColor, char symbol) {
        this.displayName = displayName;
        this.color = color;
        this.dyeColor = dyeColor;
        this.symbol = symbol;
    }

    public String getDisplayName() { return displayName; }
    public ChatColor getColor() { return color; }
    public DyeColor getDyeColor() { return dyeColor; }
    public char getSymbol() { return symbol; }

    public String getColoredName() {
        return color + displayName;
    }

    public static SkyTeam fromString(String s) {
        for (SkyTeam t : values()) {
            if (t.name().equalsIgnoreCase(s)) return t;
        }
        return null;
    }
}

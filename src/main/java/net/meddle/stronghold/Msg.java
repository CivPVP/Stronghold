package net.meddle.stronghold;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.meddle.stronghold.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.command.CommandSender;

public final class Msg {

    public static final TextColor WHITE      = TextColor.color(0xFFFFFF);
    public static final TextColor LIGHT_BLUE = TextColor.color(0x55FFFF);
    public static final TextColor RED        = TextColor.color(0xFF5555);

    // [CivPvP)] — "Civ" white, "PvP)" light blue
    public static final Component PREFIX =
        Component.text("[", LIGHT_BLUE)
            .append(Component.text("Civ", WHITE))
            .append(Component.text("PvP)", LIGHT_BLUE))
            .append(Component.text("] ", LIGHT_BLUE));

    private Msg() {}

    // ── Broadcast ─────────────────────────────────────────────────────────────

    public static void broadcast(Component body) {
        Bukkit.broadcast(PREFIX.append(body));
    }

    /** Broadcast a §-encoded string prepended with the CivPvP) prefix. */
    public static void broadcast(String legacyMsg) {
        broadcast(LegacyComponentSerializer.legacySection().deserialize(legacyMsg));
    }

    // ── Send (op / private — no prefix) ──────────────────────────────────────

    public static void send(CommandSender s, Component c) {
        s.sendMessage(c);
    }

    public static void send(CommandSender s, String legacyMsg) {
        s.sendMessage(LegacyComponentSerializer.legacySection().deserialize(legacyMsg));
    }

    // ── Team name component ───────────────────────────────────────────────────

    public static Component teamName(Team t) {
        return Component.text(t.getName(), dyeToTextColor(t.getColor()));
    }

    // ── Color helpers ─────────────────────────────────────────────────────────

    public static TextColor dyeToTextColor(DyeColor dye) {
        return switch (dye) {
            case WHITE      -> TextColor.color(0xFFFFFF);
            case ORANGE     -> TextColor.color(0xFF8C00);
            case MAGENTA    -> TextColor.color(0xFF55FF);
            case LIGHT_BLUE -> TextColor.color(0x55FFFF);
            case YELLOW     -> TextColor.color(0xFFFF55);
            case LIME       -> TextColor.color(0x55FF55);
            case PINK       -> TextColor.color(0xFF69B4);
            case GRAY       -> TextColor.color(0x555555);
            case LIGHT_GRAY -> TextColor.color(0xAAAAAA);
            case CYAN       -> TextColor.color(0x00AAAA);
            case PURPLE     -> TextColor.color(0xAA00AA);
            case BLUE       -> TextColor.color(0x5555FF);
            case BROWN      -> TextColor.color(0xAA5500);
            case GREEN      -> TextColor.color(0x00AA00);
            case RED        -> TextColor.color(0xFF5555);
            case BLACK      -> TextColor.color(0x000000);
        };
    }

    /** §-code for a DyeColor — used in TAB API prefix strings. */
    public static String dyeToSectionCode(DyeColor dye) {
        return switch (dye) {
            case WHITE      -> "§f";
            case ORANGE     -> "§6";
            case MAGENTA    -> "§d";
            case LIGHT_BLUE -> "§b";
            case YELLOW     -> "§e";
            case LIME       -> "§a";
            case PINK       -> "§d";
            case GRAY       -> "§8";
            case LIGHT_GRAY -> "§7";
            case CYAN       -> "§3";
            case PURPLE     -> "§5";
            case BLUE       -> "§9";
            case BROWN      -> "§4";
            case GREEN      -> "§2";
            case RED        -> "§c";
            case BLACK      -> "§0";
        };
    }
}

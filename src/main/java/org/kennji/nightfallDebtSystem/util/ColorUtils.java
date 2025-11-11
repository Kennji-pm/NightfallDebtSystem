package org.kennji.nightfallDebtSystem.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

public final class ColorUtils {
    private ColorUtils() {}

    public static String colorize(@Nullable String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}


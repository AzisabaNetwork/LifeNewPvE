package net.azisaba.lifenewpve.commands;

import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class WorldCreateCommand implements TabExecutor {

    private final LifeNewPvE plugin;

    public WorldCreateCommand(LifeNewPvE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(args.length >= 2)) return help(sender);
        String name = args[0];
        if (!Pattern.compile("[a-zA-Z0-9/._-]+").matcher(name).matches()) return failed(sender, "そのワールド名は無効です。");

        String environment = args[1];
        World.Environment we;
        try {
            we = World.Environment.valueOf(environment.toUpperCase());
        } catch (IllegalArgumentException e) {
            return failed(sender, "environmentを正しく指定してください。");
        }

        Difficulty difficulty = Difficulty.EASY;
        WorldType type = WorldType.NORMAL;
        String generator = "";
        String seed = UUID.randomUUID().toString();
        boolean allowStractures = true;

        int count = 0;
        try {
            for (String arg : args) {
                if (arg.equals("-g")) {
                    generator = args[count + 1];
                }
                if (arg.equals("-t")) {
                    type = WorldType.valueOf(args[count + 1].toUpperCase());
                }
                if (arg.equals("-a")) {
                    allowStractures = Boolean.parseBoolean(args[count + 1]);
                }
                if (arg.equals("-s")) {
                    seed = args[count + 1];
                }
                if (arg.equals("-d")) {
                    difficulty = Difficulty.valueOf(args[count + 1]);
                }
                count++;
            }
        } catch (IllegalArgumentException e) {
            return failed(sender, "オプションを正しく入力してください。");
        }

        plugin.createWorld(name, generator, difficulty, type, we, seed, allowStractures);
        return true;
    }

    private boolean help(@NotNull CommandSender sender) {
        sender.sendMessage("§e§l/wc [<name>] [<environment>] <-g [<generator>]> <-t [<worldType>]> <-s [<seed>]> <-a [<false or true>]> <-d [<difficulty>]>");
        return true;
    }

    private boolean failed(@NotNull CommandSender sender, String reason) {
        sender.sendMessage("§c" + reason);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 2 && args[1].isEmpty()) {
            return List.of("NORMAL", "NETHER", "THE_END", "CUSTOM");
        }
        for (int count = 0; count < args.length; count++) {
            if (args.length == count + 1 && args[count].isEmpty() && count > 2) {
                if (args[count - 1].equals("-g")) {
                    return List.of("Terra:OVERWORLD", "Terra:ORIGEN", "Terra:HYDRAXIA", "VoidWorldGenerator", "Terra:OVERWORLD_NO_CAVE", "LifeGen");
                }
                if (args[count - 1].equals("-t")) {
                    return List.of("NORMAL", "FLAT", "LARGE_BIOMES", "AMPLIFIED", "CUSTOM");
                }
                if (args[count - 1].equals("-a")) {
                    return List.of("true", "false");
                }
                if (args[count - 1].equals("-s")) {
                    return List.of(UUID.randomUUID().toString());
                }
                if (args[count - 1].equals("-d")) {
                    return List.of("EASY", "NORMAL", "HARD");
                }
            }
        }
        if (args.length == 1) {
            return List.of("ワールド名を入力してください");
        }
        return null;
    }
}

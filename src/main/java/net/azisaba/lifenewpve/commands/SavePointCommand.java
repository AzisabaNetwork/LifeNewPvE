package net.azisaba.lifenewpve.commands;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class SavePointCommand implements TabExecutor {

    private final LifeNewPvE plugin;
    private static final Set<Point> TAGS = new HashSet<>();

    public SavePointCommand(LifeNewPvE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return sendFailureMessage(sender, "プレイヤー限定コマンドです。");
        if (args[0].equalsIgnoreCase("reload")) {
            updateTags(plugin);
            player.sendMessage(Component.text("リロードしました。"));
            return true;
        }
        if (args.length > 2) {
            return switch (args[0].toLowerCase()) {
                case "search", "remove", "tp" -> handleSearchCommand(args, sender, player);
                case "add" -> handleSimpleCommand(args, sender, player);
                default -> sendFailureMessage(sender, "§c/spo <add|remove|tp|search> <固有名> <検索Tag>");
            };
        }
        return false;
    }

    public static void updateTags(@NotNull LifeNewPvE plugin) {
        plugin.runAsync(() -> {
            ConfigurationSection savePointSection = plugin.getConfig().getConfigurationSection("SavePoint");
            if (savePointSection == null) return;
            Set<String> keys = savePointSection.getKeys(true);

            keys.forEach(key -> {
                if (countPeriods(key) != 2) return;
                Point point = createPointFromKey(key, plugin);
                if (point == null) return;
                TAGS.add(point);
            });
        });
    }

    @Contract(pure = true)
    private static int countPeriods(@NotNull String str) {
        int count = 0;
        for (char ch : str.toCharArray()) {
            if (ch == '.') count++;
        }
        return count;
    }

    @Nullable
    private static Point createPointFromKey(@NotNull String key, @NotNull LifeNewPvE plugin) {
        String path = Point.getRootKey() + "." + key;
        if (plugin.getConfig().isSet(path)) {

            Location location = plugin.getConfig().getLocation(path);
            String[] parts = key.split("\\.");
            String world = parts[0];
            String tag = parts[1];
            String unique = parts[2];

            return new Point(world, tag, unique, location);
        } else {
            return null;
        }
    }

    private boolean handleSearchCommand(@NotNull String[] args, CommandSender sender, Player player) {
        Map<String, String> options = extractOptions(args);
        String unique = options.get("unique");
        String tag = options.get("tag");

        if (unique == null && tag == null) {
            return sendFailureMessage(sender, "オプション1つ以上必要です。");
        }

        Set<Point> points = TAGS;
        if (tag != null) {
            points = points.stream().filter(point -> tag.equalsIgnoreCase(point.tag)).collect(Collectors.toSet());
        }
        if (unique != null) {
            points = points.stream().filter(point -> unique.equalsIgnoreCase(point.unique)).collect(Collectors.toSet());
        }
        switch (args[0].toLowerCase()) {
            case "search" -> displayPoints(points, player);
            case "remove" -> removePoints(points, player);
            case "tp" -> teleportPoints(points, player);
        }
        return true;
    }

    private boolean handleSimpleCommand(@NotNull String[] args, CommandSender sender, Player player) {
        if (args[1].isBlank() || args[2].isBlank() || args[1].isEmpty() || args[2].isEmpty() || args.length != 3) {
            return sendFailureMessage(sender, "§c/spo add| <固有名> <検索Tag>");
        }

        Point point = createPoint(args, player);
        String key = Point.getRootKey() + "." + point.getSaveKey();
        plugin.getConfig().set(key, point.getSaveData());
        plugin.saveConfig();
        player.sendMessage(Component.text("§a固有名: " + point.unique + " 検索Tag: " + point.tag + " で、セーブポイントを作成しました。"));
        return true;
    }

    private boolean sendFailureMessage(@NotNull CommandSender sender, String message) {
        sender.sendMessage("§c" + message);
        return true;
    }

    @NotNull
    private Map<String, String> extractOptions(@NotNull String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-u") && i + 1 < args.length) {
                options.put("unique", args[i + 1]);
            }
            if (args[i].equals("-t") && i + 1 < args.length) {
                options.put("tag", args[i + 1]);
            }
        }
        return options;
    }

    private void displayPoints(@NotNull Set<Point> points, @NotNull Player player) {
        points.stream()
                .sorted(Comparator.comparing(Point::world))
                .map(point -> String.format("§fワールド: %s  §f名前: §b%s §fタグ: §a%s", point.world(), point.unique(), point.tag()))
                .forEach(s -> player.sendMessage(Component.text(s)));
    }

    private void teleportPoints(@NotNull Set<Point> points, @NotNull Player player) {
        points.stream()
                .sorted(Comparator.comparing(Point::world))
                .forEach(point -> {
                    Location location = point.loc();
                    double x = location.getBlockX() + 0.5;
                    double y = location.getBlockY();
                    double z = location.getBlockZ() + 0.5;
                    String message = String.format("§fワールド: %s  §f名前: §b%s §fタグ: §a%s §f場所: %s, %s, %s", point.world(), point.unique(), point.tag(), x, y, z);
                    player.sendMessage(Component.text(message).clickEvent(ClickEvent.runCommand("/tp " + x + " " + y + " " + z)));
                });
    }

    private void removePoints(@NotNull Set<Point> points, @NotNull Player player) {
        points.forEach(point -> {
            String key = Point.getRootKey() + "." + point.getSaveKey();
            if (plugin.getConfig().isSet(key)) {
                plugin.getConfig().set(key, null);
            }
        });
        if (!points.isEmpty()) {
            plugin.saveConfig();
            player.sendMessage(Component.text("§fデータを§c§l削除§fしました。"));
        }
    }

    @NotNull
    private static Point createPoint(@NotNull String[] args, @NotNull Player player) {
        String unique = args[1];
        String tag = args[2];
        String world = player.getWorld().getName();
        return new Point(world, tag, unique, player.getLocation());
    }

    private static final List<String> COMMAND_SUGGESTIONS = List.of("search", "add", "remove", "tp", "reload");
    private static final List<String> SEARCH_SUGGESTIONS = List.of("<検索タグ>");
    private static final List<String> UNIQUE_SUGGESTIONS = List.of("<固有名>");
    private static final String TAG_OPTION = "-t";
    private static final String UNIQUE_OPTION = "-u";

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        if (args.length == 1) {
            return COMMAND_SUGGESTIONS;
        } else if (args.length >= 2) {

            if (args[0].equalsIgnoreCase("add")) {
                if (args.length == 2) {
                    return UNIQUE_SUGGESTIONS;
                }
                if (args.length == 3) {
                    return SEARCH_SUGGESTIONS;
                }
            }

            int count = args.length - 1;
            String mainCommand = args[0];
            String lastArg = args[count - 1];

            if (mainCommand.equalsIgnoreCase("search") || mainCommand.equalsIgnoreCase("remove") || mainCommand.equalsIgnoreCase("tp")) {
                if (lastArg.equals(TAG_OPTION)) {
                    return filterByTag(args[count]);
                }
                if (lastArg.equals(UNIQUE_OPTION)) {
                    return filterByUnique(args[count]);
                }
            }
            if (args.length == 2 || args.length == 4) {
                return List.of(TAG_OPTION, UNIQUE_OPTION);
            }
        }
        return List.of();
    }

    private List<String> filterByTag(String tag) {
        return TAGS.stream()
                .map(Point::tag)
                .filter(t -> t.contains(tag.toLowerCase()))
                .distinct()
                .toList();
    }

    private List<String> filterByUnique(String unique) {
        return TAGS.stream()
                .map(Point::unique)
                .filter(uniqued -> uniqued.contains(unique.toLowerCase()))
                .distinct()
                .toList();
    }

    public record Point(String world, String tag, String unique, Location loc) {
        @NotNull
        public String getSaveKey() {
            return world + "." + tag + "." + unique;
        }
        @NotNull
        @Contract(pure = true)
        public Location getSaveData() {
            return  loc;
        }

        @NotNull
        @Contract(pure = true)
        public static String getRootKey() {
            return "SavePoint";
        }
    }
}
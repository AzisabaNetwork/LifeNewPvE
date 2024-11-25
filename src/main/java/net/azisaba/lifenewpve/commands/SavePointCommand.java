package net.azisaba.lifenewpve.commands;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.utils.LifeTime;
import net.azisaba.lifenewpve.libs.Point;
import net.azisaba.lifenewpve.libs.SavePoint;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class SavePointCommand implements TabExecutor {

    private final LifeNewPvE plugin;
    private static final Set<Point> POINTS = new HashSet<>();
    private static final Set<String> TAGS = new HashSet<>();

    public SavePointCommand(LifeNewPvE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return sendFailureMessage(sender, "プレイヤー限定コマンドです。");
        if (args.length < 1) {
           return sendFailureMessage(player,
                    "§c/spo remove <[消す対象の、Tag(-t) もしくは固有名(-u) もしくはその両方] <そのフラグに対応したパラメータ>>",
                    "§c/spo search <[探す対象の、Tag(-t) もしくは固有名(-u) もしくはその両方] <そのフラグに対応したパラメータ>>",
                    "§c/spo search <[tpする対象の、Tag(-t) もしくは固有名(-u) もしくはその両方] <そのフラグに対応したパラメータ>>",
                    "§c/spo task <[タスク化する対象の、Tag(-t) もしくは固有名(-u) もしくはその両方] <そのフラグに対応したパラメータ>> <作成期限(1d、10m、1h、180s など>",
                    "§c/spo addTag <固有名> <1つまたは、複数の検索タグ 例. RareMob,RareBoss,Others>",
                    "§c/spo removeTag <固有名> <1つまたは、複数の検索タグ 例. RareMob,RareBoss,Others>");
        }

        String subCommand = args[0].toLowerCase();
        player.sendMessage(Component.text("§7処理中です..."));
        if (subCommand.equalsIgnoreCase("reload")) {
            return handleReload(player);

        } else if (subCommand.equalsIgnoreCase("tasks")) {
            return handleTasks(player);

        } else if (subCommand.equalsIgnoreCase("add")) {
            return handleAdd(args, player);

        } else if (subCommand.equalsIgnoreCase("listTag")) {
            return handleListTag(player);

        } else if (args.length > 2) {
            return handleDynamicCommands(subCommand, args, player);
        }
        sendFailureMessage(player, "値を正しく入力してください。");
        return false;
    }

    private boolean handleListTag(Player player) {
        if (TAGS.isEmpty()) {
            player.sendMessage(Component.text("§cタグは存在しません。"));
        } else {
            TAGS.forEach(t -> player.sendMessage(Component.text(t)));
        }
        return true;
    }

    private boolean handleAdd(@NotNull String[] args, Player player) {
        if (args.length > 3 || args.length < 2 || args[1].isEmpty() || args[1].isBlank()) return false;
       return createPoint(player, args);
    }

    private boolean handleReload(@NotNull Player player) {
        updateTags();
        player.sendMessage(Component.text("リロードしました。"));
        return true;
    }

    private boolean handleTasks(Player player) {
        getTaskMessage(player);
        return true;
    }

    private boolean handleDynamicCommands(@NotNull String command, String[] args, Player player) {
        return switch (command) {
            case "search", "remove", "tp", "task" -> handleComplexCommand(args, player);
            case "addtag", "removetag" -> handleTagsCommand(args, player);
            default -> sendFailureMessage(player,
                    "§c/spo remove <[消す対象の、Tag(-t) もしくは固有名(-u) もしくはその両方] <そのフラグに対応したパラメータ>>",
                    "§c/spo search <[探す対象の、Tag(-t) もしくは固有名(-u) もしくはその両方] <そのフラグに対応したパラメータ>>",
                    "§c/spo search <[tpする対象の、Tag(-t) もしくは固有名(-u) もしくはその両方] <そのフラグに対応したパラメータ>>",
                    "§c/spo task <[タスク化する対象の、Tag(-t) もしくは固有名(-u) もしくはその両方] <そのフラグに対応したパラメータ>> <作成期限(1d、10m、1h、180s など>",
                    "§c/spo addTag <固有名> <1つまたは、複数の検索タグ 例. RareMob,RareBoss,Others>",
                    "§c/spo removeTag <固有名> <1つまたは、複数の検索タグ 例. RareMob,RareBoss,Others>"
            );
        };
    }

    private void getTaskMessage(Player p) {
        POINTS.forEach(point -> {
            if (plugin.getConfig().isSet(point.getTaskPath())) {
                long l = plugin.getConfig().getLong(point.getTaskPath());
                if (l == 0) return;
                long time = l - Instant.now().getEpochSecond();
                LifeTime lifeTime = new LifeTime();
                String m = lifeTime.getTimer(time);
                p.sendMessage(new SavePoint(plugin, lifeTime).getInfoMessage(point, m, p));
            }
        });
    }

    private boolean createPoint(Player player, String[] args) {
        Point point = SavePoint.createPoint(args, player);
        plugin.getConfig().set(point.getLocationPath(), point.loc());
        if (!point.tags().isEmpty()) {
            plugin.getConfig().set(point.getTagPath(), new ArrayList<>(point.tags()));
        }
        plugin.saveConfig();
        player.sendMessage(Component.text("§a§lセーブポイントを作成しました！" +  "§b固有名§f: " + point.unique()));
        updateTags();
        return true;
    }

    private boolean handleComplexCommand(@NotNull String[] args, Player player) {
        Map<String, String> options = extractOptions(args);
        String uniqueOption = options.get("unique");
        String tagOption = options.get("tag");

        if (uniqueOption == null && tagOption == null) {
            return sendFailureMessage(player, "オプション1つ以上必要です。");
        }


        Set<Point> filteredPoints = filterPointsByOptions(uniqueOption, tagOption);

        SavePoint savePoint = new SavePoint(plugin, new LifeTime());
        processCommand(args[0].toLowerCase(), savePoint, filteredPoints, player, args);

        return true;
    }

    private boolean handleTagsCommand(@NotNull String[] args, Player player) {
        if (args.length != 3) {
            return sendFailureMessage(player, "§cコマンドの規格通りに記述してください。",
                    "§c/spo addTag <固有名> <1つまたは、複数の検索タグ 例. RareMob,RareBoss,Others>",
                    "§c/spo removeTag <固有名> <1つまたは、複数の検索タグ 例. RareMob,RareBoss,Others>");
        }

        String commandType = args[0].toLowerCase();
        if (!commandType.contains("add") && !commandType.contains("remove")) {
            return sendFailureMessage(player, "§c無効なコマンドタイプです。");
        }

        String unique = validateParameter(args[1]);
        String tag = validateParameter(args[2]);

        if (unique == null || tag == null) {
            sendFailureMessage(player, "§c無効なパラメータです。",
                    "§c/spo addTag <固有名> <1つまたは、複数の検索タグ 例. RareMob,RareBoss,Others>",
                    "§c/spo removeTag <固有名> <1つまたは、複数の検索タグ 例. RareMob,RareBoss,Others>");
            return false;
        }

        Set<String> tags = parseTags(tag);

        if (commandType.contains("add")) {
            executeAddTag(unique, tags);
        } else {
            executeRemoveTag(unique, tags);
        }
        player.sendMessage(Component.text("§aデータの操作が完了しました。"));
        if (!unique.equals("*")) {
            player.sendMessage(Component.text("§7addTag、removeTagでのみ<固有名>の部分を「*」で全選択にできます。"));
        }
        return true;
    }

    @Nullable
    @Contract(pure = true)
    private String validateParameter(@NotNull String param) {
        return param.isEmpty() ? null : param;
    }

    @NotNull
    private Set<String> parseTags(@NotNull String tag) {
        Set<String> tags = new HashSet<>();
        if (tag.contains(",")) {
            tags.addAll(Arrays.asList(tag.split(",")));
        } else {
            tags.add(tag);
        }
        return tags;
    }

    private void executeAddTag(String unique, Set<String> tags) {
        executeTagModification(unique, tags, this::addTagsToList, true);
    }

    private void executeRemoveTag(String unique, Set<String> tags) {
        executeTagModification(unique, tags, this::removeTagsFromList, false);
    }

    private void executeTagModification(String unique, Set<String> tags, BiConsumer<List<String>, Set<String>> tagOperation, boolean isAddOperation) {
        plugin.runAsync(() -> {
            ConfigurationSection cs = plugin.getConfig().getConfigurationSection("SavePoint");
            if (cs == null) return;

            cs.getKeys(true).stream()
                    .filter(s -> isValidKey(s, unique))
                    .forEach(s -> {
                        String tagPath = "SavePoint." + s + ".Tags";
                        if (plugin.getConfig().isSet(tagPath)) {
                            List<String> tagList = new ArrayList<>(plugin.getConfig().getStringList(tagPath));
                            tagOperation.accept(tagList, tags);
                            if (tagList.isEmpty()) {
                                plugin.getConfig().set(tagPath, null);
                            } else {
                                plugin.getConfig().set(tagPath, tagList);
                            }
                        } else if (isAddOperation) {
                            plugin.getConfig().set(tagPath, new ArrayList<>(tags));
                        }
                    });

            plugin.saveConfig();
            SavePointCommand.updateTags();
        });
    }

    private boolean isValidKey(String key, @NotNull String unique) {
        int periodCount = countPeriods(key);
        if (unique.equals("*")) return periodCount == 1;
        if (key.contains(unique) && periodCount == 1) {
            String[] parts = key.split("\\.");
            return Arrays.asList(parts).contains(unique);
        }
        return false;
    }

    private void addTagsToList(List<String> tagList, @NotNull Set<String> tags) {
        tags.forEach(tag -> {
            if (!tagList.contains(tag)) {
                tagList.add(tag);
            }
        });
    }

    private void removeTagsFromList(@NotNull List<String> tagList, @NotNull Set<String> tags) {
        tags.forEach(tagList::remove);
    }

    private Set<Point> filterPointsByOptions(String uniqueOption, String tagOption) {
        Set<Point> points = POINTS;
        if (tagOption != null) {
            Set<String> tags = parseTags(tagOption);
            points = points.stream()
                    .filter(point -> point.tags().containsAll(tags))
                    .collect(Collectors.toSet());
        }
        if (uniqueOption != null) {
            points = points.stream()
                    .filter(point -> uniqueOption.equalsIgnoreCase(point.unique()))
                    .collect(Collectors.toSet());
        }
        return points;
    }

    private void processCommand(@NotNull String command, SavePoint savePoint, Set<Point> points, Player player, String[] args) {
        switch (command) {
            case "search" -> savePoint.displayPoints(points, player);
            case "remove" -> savePoint.removePoints(points, player);
            case "tp" -> savePoint.teleportPoints(points, player);
            case "task" -> savePoint.taskPoints(points, player, args);
        }
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

    public static void updateTags() {
        LifeNewPvE plugin = JavaPlugin.getPlugin(LifeNewPvE.class);
        POINTS.clear();
        plugin.runAsync(() -> {
            ConfigurationSection savePointSection = plugin.getConfig().getConfigurationSection("SavePoint");
            if (savePointSection == null) return;
            Set<String> keys = savePointSection.getKeys(true);

            keys.forEach(key -> {
                if (countPeriods(key) != 1) return;
                Point point = createPointFromKey(key, plugin);
                if (point == null) return;
                POINTS.add(point);
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
        if (plugin.getConfig().isSet("SavePoint." + key)) {
            String[] parts = key.split("\\.");
            String world = parts[0];
            String unique = parts[1];

            Location loc;
            if (plugin.getConfig().isSet("SavePoint." + key + ".Location")) {
                loc = plugin.getConfig().getLocation("SavePoint." + key + ".Location");
            } else {
                return null;
            }

            Set<String> tags;
            if (plugin.getConfig().isSet("SavePoint." + key + ".Tags")) {
                tags = new HashSet<>(plugin.getConfig().getStringList("SavePoint." + key + ".Tags"));
                TAGS.addAll(tags);
            } else {
                tags = new HashSet<>();
            }
            return new Point(world, tags, unique, loc);
        } else {
            return null;
        }
    }

    private static final List<String> COMMAND_SUGGESTIONS = List.of("search", "add", "remove", "tp", "reload", "task", "tasks", "addTag", "removeTag", "listTag");
    private static final List<String> SEARCH_SUGGESTIONS = List.of("<1つまたは、複数の検索タグ 例. RareMob,RareBoss,Others>");
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
            if (args[0].isEmpty() || args[0].isBlank()) {
                return COMMAND_SUGGESTIONS;
            } else {
                List<String> list;
                try {
                    String string = args[0].toLowerCase();
                    list = new ArrayList<>(COMMAND_SUGGESTIONS);
                    list.stream().filter(s -> s.contains(string)).forEach(list::add);
                } catch (Exception e) {
                    return COMMAND_SUGGESTIONS;
                }
                return list;
            }
        } else if (args.length >= 2) {

            if (args[0].equalsIgnoreCase("add") ||
                    args[0].equalsIgnoreCase("removetag") ||
                    args[0].equalsIgnoreCase("addtag")) {

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

            if (mainCommand.equalsIgnoreCase("search") ||
                    mainCommand.equalsIgnoreCase("remove") ||
                    mainCommand.equalsIgnoreCase("tp") ||
                    mainCommand.equalsIgnoreCase("task")) {
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
                .filter(t -> t.contains(tag.toLowerCase()))
                .distinct()
                .toList();
    }

    private List<String> filterByUnique(String unique) {
        return POINTS.stream()
                .map(Point::unique)
                .filter(uniqued -> uniqued.contains(unique.toLowerCase()))
                .distinct()
                .toList();
    }

    public static boolean sendFailureMessage(@NotNull CommandSender sender, String... message) {
        Arrays.stream(message).forEach((m) -> sender.sendMessage(Component.text(m)));
        return true;
    }

    public static boolean sendFailureMessage(@NotNull CommandSender sender, String message) {
        sender.sendMessage("§c" + message);
        return true;
    }

    public static void addTag(@NotNull String tag) {
        TAGS.add(tag.toLowerCase());
    }
}
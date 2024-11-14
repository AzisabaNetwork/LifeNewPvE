package net.azisaba.lifenewpve.libs.attributes;

import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.commands.SavePointCommand;
import net.azisaba.lifenewpve.libs.LifeTime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;

public class SavePoint {

    private final LifeNewPvE plugin;

    private final LifeTime lifeTime;

    public SavePoint(LifeNewPvE plugin, LifeTime lifeTime) {
        this.plugin = plugin;
        this.lifeTime = lifeTime;
    }


    public void displayPoints(@NotNull Set<Point> points, @NotNull Player player) {
        points.stream().sorted(Comparator.comparing(Point::world))
                .forEach(point -> {
                    long taskDurationInSeconds = plugin.getConfig().getLong(point.getTaskPath());
                    long remainingTime = calculateRemainingTime(taskDurationInSeconds);

                    String formattedTimeMessage = lifeTime.getTimer(remainingTime);
                    ClickEvent event = ClickEvent.copyToClipboard(point.unique());
                    player.sendMessage(getInfoMessage(point, formattedTimeMessage).clickEvent(event));
                });
        player.sendMessage(Component.text("§7メッセージをクリックで、固有名をcopyできます。"));
    }

    public void teleportPoints(@NotNull Set<Point> points, @NotNull Player player) {
        points.stream()
                .sorted(Comparator.comparing(Point::world))
                .forEach(point -> {
                    long taskDurationInSeconds = plugin.getConfig().getLong(point.getTaskPath());
                    long remainingTime = calculateRemainingTime(taskDurationInSeconds);

                    String formattedTimeMessage = lifeTime.getTimer(remainingTime);

                    Location loc = point.loc();
                    ClickEvent event = ClickEvent.runCommand("/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
                    player.sendMessage(getInfoMessage(point, formattedTimeMessage).clickEvent(event));
                });
    }

    private long calculateRemainingTime(long taskDurationInSeconds) {
        long currentTimeInSeconds = Instant.now().getEpochSecond();
        long remainingTime = taskDurationInSeconds - currentTimeInSeconds;
        return taskDurationInSeconds == 0 ? 0 : remainingTime;
    }

    public void taskPoints(@NotNull Set<Point> points, Player player, @NotNull String[] args) {
        int last = args.length - 1;
        long time = lifeTime.getTime(args[last]);
        points.forEach(point -> {
            plugin.getConfig().set(point.getTaskPath(), time + Instant.now().getEpochSecond());
            plugin.saveConfig();
            player.sendMessage(Component.text("§aTaskをセットしました。"));

        });
    }

    public void removePoints(@NotNull Set<Point> points, @NotNull Player player) {
        clearPointsFromConfig(points);
        notifyPlayerIfPointsRemoved(points, player);
    }

    private void clearPointsFromConfig(@NotNull Set<Point> points) {
        points.forEach(point -> {
            String rootPath = point.getRootPath();
            if (plugin.getConfig().isSet(rootPath)) {
                plugin.getConfig().set(rootPath, null);
            }
        });
    }

    private void notifyPlayerIfPointsRemoved(@NotNull Set<Point> points, @NotNull Player player) {
        if (!points.isEmpty()) {
            plugin.saveConfig();
            player.sendMessage(Component.text("§fデータを§c§l削除§fしました。"));
            SavePointCommand.updateTags();
        }
    }

    @NotNull
    public static Point createPoint(@NotNull String[] args, @NotNull Player player) {
        String unique = args[1];
        String world = player.getWorld().getName();

        Set<String> tags = new HashSet<>();
        if (args.length > 2) {
            String tag = args[2];
            if (tag.contains(",")) {
                List<String> l = Arrays.asList(tag.split(","));
                tags.addAll(l);
                l.forEach(SavePointCommand::addTag);
            } else {
                tags.add(tag);
                SavePointCommand.addTag(tag);
            }
        }
        return new Point(world, tags, unique, player.getLocation());
    }

    @NotNull
    public Component getInfoMessage(@NotNull Point point, @NotNull String message) {
        Location location = point.loc();
        double xCoordinate = location.getBlockX() + 0.5;
        double yCoordinate = location.getBlockY();
        double zCoordinate = location.getBlockZ() + 0.5;

        String hoverText = createHoverText(point, xCoordinate, yCoordinate, zCoordinate);
        HoverEvent<Component> hoverEvent = HoverEvent.showText(Component.text(hoverText));

        String s = " §fName: §b" + point.unique();
        if (!message.equals("§a")) {
            s+= " §cExpires: " + message;
        }

        return Component.text(s).hoverEvent(hoverEvent);
    }

    @NotNull
    private String createHoverText(@NotNull Point point, double x, double y, double z) {
        return "§f§lWorld  §a§l➜　§f§l" + point.world() + "\n" +
                "§f§lLocation  §a§l➜　§a§l" + x + " " + y + " " + z + "\n" +
                "§f§lTags§: \n" + getTagsToString(point.tags());
    }

    @NotNull
    private String getTagsToString(@NotNull Set<String> tags) {
        StringBuilder tagsString = new StringBuilder();
        for (String tag : tags) {
            tagsString.append("§f- §l").append(tag).append("\n");
        }
        return tagsString.toString();
    }
}

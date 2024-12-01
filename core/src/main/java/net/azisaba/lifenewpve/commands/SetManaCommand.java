package net.azisaba.lifenewpve.commands;

import net.azisaba.lifenewpve.mana.ManaUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetManaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            return sendMessage(sender, "§c/setmana <MCID> <mana>");
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            return sendMessage(sender, "§cプレイヤーが見つかりません。");
        }

        long mana;
        try {
            mana = parseMana(args[1]);
        } catch (NumberFormatException e) {
            return sendMessage(sender, "§cマナが無効です。");
        }

        ManaUtil.setMana(player, mana);
        player.sendMessage(Component.text("§b操作が完了しました。"));
        return true;
    }

    private long parseMana(String maxManaArg) {
        return Long.parseLong(maxManaArg);
    }

    private boolean sendMessage(@NotNull CommandSender sender, String message) {
        sender.sendMessage(Component.text(message));
        return true;
    }
}

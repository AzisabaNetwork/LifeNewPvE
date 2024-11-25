package net.azisaba.lifenewpve.commands;

import net.azisaba.lifenewpve.libs.mana.Mana;
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
        if (args.length != 2) return sendMessage(sender);
        Player p = Bukkit.getPlayer(args[0]);
        if (p == null) return sendMessage(sender);

        long maxmana;
        try {
            maxmana = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            return sendMessage(sender);
        }

        Mana.setMaxMana(p, maxmana);
        p.sendMessage(Component.text("§b操作が完了しました。"));
        return true;
    }

    private boolean sendMessage(@NotNull CommandSender sender) {
        sender.sendMessage(Component.text("§c/setmana <MCID> <Maxmana>"));
        return true;
    }
}

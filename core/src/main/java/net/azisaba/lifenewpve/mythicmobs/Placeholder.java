package net.azisaba.lifenewpve.mythicmobs;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import io.lumine.mythic.bukkit.BukkitAdapter;
import net.azisaba.lifenewpve.mana.ManaUtil;
import org.bukkit.entity.Player;

import java.text.NumberFormat;

public class Placeholder {

    private final PlaceholderManager manager;

    public Placeholder(PlaceholderManager manager) {
        this.manager = manager;
    }

    public void init() {
        targetX();
        targetXDouble();
        targetY();
        targetYDouble();
        targetZ();
        targetZDouble();
        manaDouble();
        maxManaDouble();
        mana();
        maxMana();
        manaPercentDouble();
        manaPercent();
    }

    private void targetX() {
        manager.register("life.l.x", io.lumine.mythic.core.skills.placeholders.Placeholder.location((l, s) ->
                String.valueOf(l.getBlockX() + 0.5)));
    }

    private void targetXDouble() {
        manager.register("life.l.x.double", io.lumine.mythic.core.skills.placeholders.Placeholder.location((l, s) ->
                String.valueOf(l.getX())));
    }

    private void targetY() {
        manager.register("life.l.y", io.lumine.mythic.core.skills.placeholders.Placeholder.location((l, s) ->
                String.valueOf(l.getBlockY())));
    }

    private void targetYDouble() {
        manager.register("life.l.y.double", io.lumine.mythic.core.skills.placeholders.Placeholder.location((l, s) ->
                String.valueOf(l.getY())));
    }

    private void targetZ() {
        manager.register("life.l.z", io.lumine.mythic.core.skills.placeholders.Placeholder.location((l, s) ->
                String.valueOf(l.getBlockZ() + 0.5)));
    }

    private void targetZDouble() {
        manager.register("life.l.z.double", io.lumine.mythic.core.skills.placeholders.Placeholder.location((l, s) ->
                String.valueOf(l.getZ())));
    }

    private void manaDouble() {
        manager.register("life.mana.double", io.lumine.mythic.core.skills.placeholders.Placeholder.entity((l, s) -> {
            if (!l.isPlayer()) return "null";
            Player p = BukkitAdapter.adapt(l.asPlayer());
            return String.valueOf(ManaUtil.getMana(p));
        }));
    }

    private void mana() {
        manager.register("life.mana", io.lumine.mythic.core.skills.placeholders.Placeholder.entity((l, s) -> {
            if (!l.isPlayer()) return "null";
            Player p = BukkitAdapter.adapt(l.asPlayer());
            NumberFormat num = NumberFormat.getInstance();
            num.setMaximumFractionDigits(0);
            return num.format(ManaUtil.getMana(p));
        }));
    }

    private void maxManaDouble() {
        manager.register("life.max_mana.double", io.lumine.mythic.core.skills.placeholders.Placeholder.entity((l, s) -> {
            if (!l.isPlayer()) return "null";
            Player p = BukkitAdapter.adapt(l.asPlayer());
            return String.valueOf(ManaUtil.getMana(p));
        }));
    }

    private void maxMana() {
        manager.register("life.max_mana", io.lumine.mythic.core.skills.placeholders.Placeholder.entity((l, s) -> {
            if (!l.isPlayer()) return "null";
            Player p = BukkitAdapter.adapt(l.asPlayer());
            NumberFormat num = NumberFormat.getInstance();
            num.setMaximumFractionDigits(0);
            return num.format(ManaUtil.getMaxMana(p));
        }));
    }

    private void manaPercentDouble() {
        manager.register("life.mana_percent.double", io.lumine.mythic.core.skills.placeholders.Placeholder.entity((l, s) -> {
            if (!l.isPlayer()) return "null";
            Player p = BukkitAdapter.adapt(l.asPlayer());
            double mana = ManaUtil.getMana(p) / ManaUtil.getMaxMana(p);
            return String.valueOf(mana);
        }));
    }

    private void manaPercent() {
        manager.register("life.mana_percent", io.lumine.mythic.core.skills.placeholders.Placeholder.entity((l, s) -> {
            if (!l.isPlayer()) return "null";
            Player p = BukkitAdapter.adapt(l.asPlayer());
            double mana = ManaUtil.getMana(p) / ManaUtil.getMaxMana(p);
            NumberFormat num = NumberFormat.getInstance();
            num.setMaximumFractionDigits(2);
            return num.format(mana);
        }));
    }
}

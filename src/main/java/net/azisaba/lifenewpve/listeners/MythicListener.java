package net.azisaba.lifenewpve.listeners;

import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.mythicmobs.MythicInRadius;
import net.azisaba.lifenewpve.mythicmobs.Rotate;
import net.azisaba.lifenewpve.mythicmobs.SetFallDistance;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class MythicListener implements Listener {

    public void initialize(LifeNewPvE plugin) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new MythicListener.Conditions(), plugin);
        pm.registerEvents(new MythicListener.Mechanics(), plugin);
        pm.registerEvents(new MythicListener.Damage(), plugin);
    }

    public static double damageMath(double damage, double a, double t) {
        if (damage == 0) return 0;
        double armor = a + t * 2;
        double f = 1 + damage;
        return damage / (armor + f) * damage;
    }

    public static boolean isMythic() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
        return plugin != null && plugin.isEnabled();
    }

    public static void reloadMythic(long delay) {
        call();
        JavaPlugin.getPlugin(LifeNewPvE.class).runSyncDelayed(()-> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm re -a"), delay);
    }

    public static void call() {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(Component.text("§a§lMythicMobsのリロードが行われいます。")));
    }

    public static class Conditions extends MythicListener {

        @EventHandler
        public void onConditions(@NotNull MythicConditionLoadEvent e) {
            String s = e.getConditionName();
            if (s.equalsIgnoreCase("mythicInRadius")) {
                e.register(new MythicInRadius(e.getConfig()));
            }
        }
    }

    public static class Mechanics extends MythicListener {

        @EventHandler
        public void onMechanics(@NotNull MythicMechanicLoadEvent e) {
            String s = e.getMechanicName();
            if (s.equalsIgnoreCase("setFallDistance")) {
                e.register(new SetFallDistance(e.getConfig()));
            } else if (s.equalsIgnoreCase("rotate")) {
                e.register(new Rotate(e.getConfig()));
            }
        }
    }

    public static class Damage extends MythicListener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onDamage(@NotNull MythicDamageEvent e) {
            double a = e.getTarget().getArmor();
            double t = e.getTarget().getArmorToughness();

            e.setDamage(damageMath(e.getDamage(), a, t));
        }
    }
}

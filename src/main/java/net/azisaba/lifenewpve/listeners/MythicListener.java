package net.azisaba.lifenewpve.listeners;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.mythicmobs.ContainRegion;
import net.azisaba.lifenewpve.mythicmobs.FromSurface;
import net.azisaba.lifenewpve.mythicmobs.MythicInRadius;
import net.azisaba.lifenewpve.mythicmobs.SetFallDistance;
import net.azisaba.lifenewpve.packet.PacketHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Random;

public class MythicListener implements Listener {

    public void initialize(LifeNewPvE plugin) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new MythicListener.Conditions(), plugin);
        pm.registerEvents(new MythicListener.Mechanics(), plugin);
        pm.registerEvents(new MythicListener.Damage(), plugin);
    }

    public static double damageMath(double damage, double a, double t) {
        if (damage <= 0) return 0;
        double armor = a + t * 2;
        if (armor < 0) {
            return damage * Math.pow(1.025, Math.abs(armor));
        } else {
            damage *= Math.pow(0.9995, Math.abs(a));
        }
        double f = 1 + damage;
        double math = Math.max(damage / (armor + f) * f, 0);
        return Double.isInfinite(math) || Double.isNaN(math) ? damage : math;
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
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(Component.text("§a§lMythicMobsのリロードが行われます。")));
    }

    public static class Conditions extends MythicListener {

        @EventHandler
        public void onConditions(@NotNull MythicConditionLoadEvent e) {
            String s = e.getConditionName();
            if (s.equalsIgnoreCase("mythicInRadius")) {
                e.register(new MythicInRadius(e.getConfig()));
            } else if (s.equalsIgnoreCase("fromSurface")) {
                e.register(new FromSurface(e.getConfig()));
            } else if (s.equalsIgnoreCase("containRegion")) {
                e.register(new ContainRegion(e.getConfig()));
            }
        }
    }

    public static class Mechanics extends MythicListener {

        @EventHandler
        public void onMechanics(@NotNull MythicMechanicLoadEvent e) {
            String s = e.getMechanicName();
            if (s.equalsIgnoreCase("setFallDistance")) {
                e.register(new SetFallDistance(e.getConfig()));
            }
        }
    }

    public static class Damage extends MythicListener {

        private static final Random RANDOM = new Random();

        @EventHandler(priority = EventPriority.LOWEST)
        public void onDamage(@NotNull MythicDamageEvent event) {
            AbstractEntity target = event.getTarget();
            if (!target.isDamageable() || !target.isLiving()) return;

            double armor = target.getArmor();
            double toughness = target.getArmorToughness();
            event.setDamage(damageMath(event.getDamage(), armor, toughness));
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onDisplay(@NotNull MythicDamageEvent event) {
            AbstractEntity attacker = event.getCaster().getEntity();
            if (!attacker.isPlayer()) return;

            Player player = (Player) attacker.asPlayer().getBukkitEntity();
            AbstractEntity victim = event.getTarget();
            if (victim.isPlayer()) return;

            ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(victim.getUniqueId()).orElse(null);
            if (mob == null) return;

            String element = event.getDamageMetadata().getElement();
            double multiplier = mob.getType().getDamageModifiers().getOrDefault(element, 1.0);
            double damage = formatDamage(event.getDamage() * multiplier);
            Location location = getRandomLocation(victim.getBukkitEntity().getLocation());
            Component component = MiniMessage.miniMessage().deserialize(getDamageElement(element) + damage);

            int id = RANDOM.nextInt(Integer.MAX_VALUE);
            PacketHandler.spawnTextDisplay(player, location.getX(), location.getY(), location.getZ(), id);
            PacketHandler.setTextDisplayMeta(player, id, component);

            JavaPlugin.getPlugin(LifeNewPvE.class).runSyncDelayed(() -> PacketHandler.removeTextDisplay(player, id), 30);
        }

        private double formatDamage(double amount) {
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            return Double.parseDouble(numberFormat.format(amount));
        }

        @NotNull
        private Location getRandomLocation(@NotNull Location location) {
            return location.add(RANDOM.nextInt(4) * 0.5 - 1, RANDOM.nextInt(5) * 0.1 + 1.8 , RANDOM.nextInt(4) * 0.5 - 1);
        }

        @NotNull
        private String getDamageElement(String element) {
            String prefix = "<red><bold>⚔";
            if (element == null) return prefix;

            return LifeNewPvE.getColors().entrySet().stream()
                    .filter(entry -> element.equalsIgnoreCase(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst().map(prefix::concat).orElse(prefix);
        }
    }
}

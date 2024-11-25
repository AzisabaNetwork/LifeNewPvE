package net.azisaba.lifenewpve.listeners;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.*;
import io.lumine.mythic.core.items.MythicItem;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.CoolTime;
import net.azisaba.lifenewpve.libs.event.ManaModifyEvent;
import net.azisaba.lifenewpve.libs.mana.Mana;
import net.azisaba.lifenewpve.mythicmobs.*;
import net.azisaba.lifenewpve.mythicmobs.conditons.ContainRegion;
import net.azisaba.lifenewpve.mythicmobs.conditons.FromSurface;
import net.azisaba.lifenewpve.mythicmobs.conditons.HasMana;
import net.azisaba.lifenewpve.mythicmobs.conditons.MythicInRadius;
import net.azisaba.lifenewpve.mythicmobs.mechanics.ModifyMana;
import net.azisaba.lifenewpve.mythicmobs.mechanics.RaidBoss;
import net.azisaba.lifenewpve.mythicmobs.mechanics.SetFallDistance;
import net.azisaba.lifenewpve.packet.PacketHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.core.component.DataComponents;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.*;

public class MythicListener implements Listener {

    public void initialize(LifeNewPvE plugin) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new MythicListener.Conditions(), plugin);
        pm.registerEvents(new MythicListener.Mechanics(), plugin);
        pm.registerEvents(new MythicListener.Damage(), plugin);
        pm.registerEvents(new MythicListener.Reload(), plugin);
        pm.registerEvents(new MythicListener.ItemGen(), plugin);
        pm.registerEvents(new MythicListener.Death(), plugin);
    }

    public static double damageMath(double damage, double a, double t) {
        if (damage <= 0) return 0;
        double armor = a + t * 2;
        if (armor < 0) {
            return damage * Math.pow(1.025, Math.abs(armor));
        } else {
            damage *= Math.pow(0.9995, Math.abs(t));
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

    public static class Reload extends MythicListener {

        @EventHandler
        public void onReload(@NotNull MythicReloadedEvent e) {
            new Placeholder(e.getInstance().getPlaceholderManager()).init();
        }
    }

    public static class Conditions extends MythicListener {

        @EventHandler
        public void onConditions(@NotNull MythicConditionLoadEvent event) {
            String conditionName = event.getConditionName();
            switch (conditionName.toLowerCase()) {
                case "mythicinradius":
                    event.register(new MythicInRadius(event.getConfig()));
                    break;
                case "fromsurface":
                    event.register(new FromSurface(event.getConfig()));
                    break;
                case "containregion":
                    event.register(new ContainRegion(event.getConfig()));
                    break;
                case "hasmana":
                    event.register(new HasMana(event.getConfig()));
                    break;
                default:
                    // 未知の条件には何もしません
                    break;
            }
        }
    }

    public static class Mechanics extends MythicListener {

        @EventHandler
        public void onMechanics(@NotNull MythicMechanicLoadEvent event) {
            String mechanicName = event.getMechanicName();
            switch (mechanicName.toLowerCase()) {
                case "setfalldistance":
                    event.register(new SetFallDistance(event.getConfig()));
                    break;
                case "raidboss":
                    event.register(new RaidBoss());
                case "modifymana":
                    event.register(new ModifyMana(event.getConfig()));
                default:
                    // 未知の条件には何もしません
                    break;
            }
        }
    }

    public static class ItemGen extends MythicListener {

        @EventHandler
        public void onGen(@NotNull MythicMobItemGenerateEvent event) {
            MythicItem mi = event.getItem();
            String g = mi.getGroup();
            if (g == null) return;
            if (!g.equals("Main-Weapon")) return;

            ItemStack item = event.getItemStack();
            if (item == null || !item.hasItemMeta()) return;
            net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
            nms.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            event.setItemStack(CraftItemStack.asBukkitCopy(nms));
        }
    }

    @SuppressWarnings("ConstantValue")
    public static class Damage extends MythicListener {

        private static final Random RANDOM = new Random();
        private static final int DISPLAY_DURATION_TICKS = 30;
        private static final int RANDOM_BOUND = 4;
        private static final double RANDOM_SCALE = 0.5;
        private static final double BASE_Y_OFFSET = 1.8;
        private static final String DAMAGE_PREFIX = "<red><bold>⚔";

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

            showDamageDisplay(event, player, victim, mob);
        }

        private void showDamageDisplay(@NotNull MythicDamageEvent event, Player player, @NotNull AbstractEntity victim, @NotNull ActiveMob mob) {
            String element = event.getDamageMetadata().getElement();
            double multiplier = mob.getType().getDamageModifiers().getOrDefault(element, 1.0);
            double damage = formatDamage(event.getDamage() * multiplier);
            Location location = getRandomLocation(victim.getBukkitEntity().getLocation());
            Component component = MiniMessage.miniMessage().deserialize(getDamageElement(element) + damage);

            int id = RANDOM.nextInt(Integer.MAX_VALUE);
            PacketHandler.spawnTextDisplay(player, location.getX(), location.getY(), location.getZ(), id);
            PacketHandler.setTextDisplayMeta(player, id, component);

            JavaPlugin.getPlugin(LifeNewPvE.class).runSyncDelayed(() -> PacketHandler.removeTextDisplay(player, id), DISPLAY_DURATION_TICKS);
        }

        private double formatDamage(double amount) {
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            return Double.parseDouble(numberFormat.format(amount));
        }

        @NotNull
        private Location getRandomLocation(@NotNull Location location) {
            return location.add(
                    RANDOM.nextInt(RANDOM_BOUND) * RANDOM_SCALE - 1,
                    RANDOM.nextInt(RANDOM_BOUND + 1) * 0.1 + BASE_Y_OFFSET,
                    RANDOM.nextInt(RANDOM_BOUND) * RANDOM_SCALE - 1
            );
        }

        @NotNull
        private String getDamageElement(String element) {
            if (element == null) return DAMAGE_PREFIX;
            return LifeNewPvE.getColors().entrySet().stream()
                    .filter(entry -> element.equalsIgnoreCase(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst().map(DAMAGE_PREFIX::concat).orElse(DAMAGE_PREFIX);
        }

        private static final Multimap<Class<?>, UUID> ct = HashMultimap.create();

        @EventHandler(priority = EventPriority.LOWEST)
        public void onCombatRaidBoss(@NotNull MythicDamageEvent e) {
            AbstractEntity ab = e.getTarget();
            ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(ab.getUniqueId()).orElse(null);
            if (mob == null) return;
            if (!mob.hasThreatTable()) return;

            NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), "raid_boss");
            if (!ab.getDataContainer().has(key)) return;

            int coolTime = 20;
            if (CoolTime.isCoolTime(getClass(), ab.getUniqueId(), ct)) return;
            CoolTime.setCoolTime(getClass(), ab.getUniqueId(), ct, coolTime);

            JavaPlugin.getPlugin(LifeNewPvE.class).runSyncDelayed(() -> {
                if (ab == null) return;
                if (mob == null) return;

                Set<Player> players = getNearByPlayers(ab, getPlayerAmount(mob.getThreatTable()));

                double max = mob.getType().getHealth(mob) * getHealthPerPlayer(players.size());
                double now = ab.getHealth() * getHealthPerPlayer(players.size());

                if (ab.getMaxHealth() == max) return;
                double scale = max / ab.getMaxHealth();
                double d = now * scale;

                ab.setMaxHealth(max);

                double set = ab.getHealth() * scale;
                if (scale < 1) set = d;
                ab.setHealth(set);

            }, 1);
        }

        private @NotNull Set<Player> getNearByPlayers(AbstractEntity ab, @NotNull Set<AbstractPlayer> set) {
            Set<Player> players = new HashSet<>();
            for (AbstractPlayer ap : set) {
                Player p = BukkitAdapter.adapt(ap);
                if (!p.getWorld().getName().equals(ab.getWorld().getName())) continue;
                if (!p.isOnline()) continue;
                if (p.getGameMode().equals(GameMode.CREATIVE) || p.getGameMode().equals(GameMode.SPECTATOR)) continue;
                if (ab.getBukkitEntity().getLocation().distance(p.getLocation()) <= 64) {
                    players.add(p);
                }
            }
            return players;
        }

        private @NotNull Set<AbstractPlayer> getPlayerAmount(@NotNull ActiveMob.ThreatTable table) {
            Set<AbstractPlayer> players = new HashSet<>();
            for (AbstractEntity entity : table.getAllThreatTargets()) {
                if (!entity.isPlayer()) continue;
                players.add(entity.asPlayer());
            }
            return players;
        }

        private double getHealthPerPlayer(double n) {
            return n - 0.25 * (n - 1);
        }
    }

    public static class Death extends MythicListener {

        @EventHandler
        public void onDeath(@NotNull MythicMobDeathEvent e) {
            if (!(e.getKiller() instanceof Player p)) return;
            if (e.getEntity() instanceof Player) return;
            if (!(e.getEntity() instanceof LivingEntity)) return;
            if (LifeNewPvE.RANDOM.nextInt(100) + 1 > 10) return;

            Mana.modifyMana(p, Mana.getManaSteal(p), ManaModifyEvent.Type.MYTHIC_KILL);
        }
    }
}

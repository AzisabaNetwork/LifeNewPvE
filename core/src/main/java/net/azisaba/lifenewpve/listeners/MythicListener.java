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
import net.azisaba.lifenewpve.libs.damage.DamageMath;
import net.azisaba.lifenewpve.mana.ManaUtil;
import net.azisaba.lifenewpve.mythicmobs.Placeholder;
import net.azisaba.lifenewpve.mythicmobs.conditons.*;
import net.azisaba.lifenewpve.mythicmobs.mechanics.*;
import net.azisaba.lifenewpve.utils.CoolTime;
import net.kyori.adventure.text.Component;
import net.minecraft.core.component.DataComponents;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MythicListener implements Listener {

    public void initialize(LifeNewPvE plugin) {
        PluginManager pm = Bukkit.getPluginManager();
        registerAllEvents(pm, plugin);
    }

    private void registerAllEvents(@NotNull PluginManager pm, LifeNewPvE plugin) {
        pm.registerEvents(new Conditions(), plugin);
        pm.registerEvents(new Mechanics(), plugin);
        pm.registerEvents(new Damage(), plugin);
        pm.registerEvents(new Reload(), plugin);
        pm.registerEvents(new ItemGen(), plugin);
        pm.registerEvents(new Death(), plugin);
    }

    public static boolean isMythic() {
        return isPluginEnabled();
    }

    private static boolean isPluginEnabled() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
        return plugin != null && plugin.isEnabled();
    }

    public static void reloadMythic(long delay) {
        notifyPlayers();
        executeReloadCommand(delay);
    }

    private static void executeReloadCommand(long delay) {
        JavaPlugin.getPlugin(LifeNewPvE.class)
                .runSyncDelayed(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm re -a"), delay);
    }

    public static void notifyPlayers() {
        Bukkit.getOnlinePlayers()
                .forEach(p -> p.sendMessage(Component.text("§a§lMythicMobsのリロードが行われます。")));
    }

    public static class Conditions extends MythicListener {
        @EventHandler
        public void onConditions(@NotNull MythicConditionLoadEvent event) {
            registerCondition(event);
        }

        private void registerCondition(@NotNull MythicConditionLoadEvent event) {
            String conditionName = event.getConditionName().toLowerCase();
            switch (conditionName) {
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
                case "worldmatch":
                    event.register(new WorldMatch(event.getConfig()));
                    break;
                case "canattack":
                    event.register(new CanAttack(event.getConfig()));
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
            registerMechanic(event);
        }

        private void registerMechanic(@NotNull MythicMechanicLoadEvent event) {
            String mechanicName = event.getMechanicName().toLowerCase();
            switch (mechanicName) {
                case "setfalldistance":
                    event.register(new SetFallDistance(event.getConfig()));
                    break;
                case "raidboss":
                    event.register(new RaidBoss());
                case "modifymana":
                    event.register(new ModifyMana(event.getConfig()));
                    break;
                case "menuprotect":
                    event.register(new MenuProtect());
                    break;
                case "addlifepotion":
                    event.register(new AddLifePotion(event.getConfig()));
                    break;
                case "setscale":
                    event.register(new SetScale(event.getConfig()));
                    break;
                case "fake":
                    event.register(new Fake(event.getConfig()));
                    break;
                default:
                    // 未知の条件には何もしません
                    break;
            }
        }
    }

    public static class Reload extends MythicListener {
        @EventHandler
        public void onReload(@NotNull MythicReloadedEvent e) {
            new Placeholder(e.getInstance().getPlaceholderManager()).init();
        }
    }

    public static class ItemGen extends MythicListener {
        @EventHandler
        public void onGen(@NotNull MythicMobItemGenerateEvent event) {
            handleItemGen(event);
        }

        private void handleItemGen(@NotNull MythicMobItemGenerateEvent event) {
            MythicItem mi = event.getItem();
            String itemGroup = mi != null ? mi.getGroup() : null;
            if (!"Main-Weapon".equals(itemGroup)) return;

            ItemStack item = event.getItemStack();
            if (item != null && item.hasItemMeta()) {
                net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
                nms.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                event.setItemStack(CraftItemStack.asBukkitCopy(nms));
            }
        }
    }

    @SuppressWarnings("ConstantValue")
    public static class Damage extends MythicListener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onDamage(@NotNull MythicDamageEvent event) {
            if (!isTargetConditionValid(event)) return;
            double finalDamage = calculateFinalDamage(event);
            event.setDamage(finalDamage);
        }

        private boolean isTargetConditionValid(@NotNull MythicDamageEvent event) {
            AbstractEntity target = event.getTarget();
            return target.isDamageable() && target.isLiving();
        }

        private double calculateFinalDamage(@NotNull MythicDamageEvent event) {
            AbstractEntity target = event.getTarget();
            return DamageMath.getCalculatedDamage(event.getDamage(), target.getArmor(), target.getArmorToughness(), target, event.getCaster().getEntity(), event.getDamageMetadata().getElement());
        }

        private static final Multimap<Class<?>, UUID> ct = HashMultimap.create();

        @EventHandler(priority = EventPriority.LOWEST)
        public void onCombatRaidBoss(@NotNull MythicDamageEvent e) {
            AbstractEntity ab = e.getTarget();
            if (!isRaidBoss(ab, this.getClass())) return;
            scheduleRaidBossHealthUpdate(ab);
        }

        private boolean isRaidBoss(@NotNull AbstractEntity ab, Class<?> clazz) {
            ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(ab.getUniqueId()).orElse(null);
            if (mob == null || !mob.hasThreatTable()) return false;
            NamespacedKey key = new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), "raid_boss");
            return ab.getDataContainer().has(key) && !CoolTime.isCoolTime(clazz, ab.getUniqueId(), ct);
        }

        private void scheduleRaidBossHealthUpdate(AbstractEntity ab) {
            JavaPlugin.getPlugin(LifeNewPvE.class).runSyncDelayed(() -> {
                ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(ab.getUniqueId()).orElse(null);
                if (ab != null && mob != null) {
                    updateRaidBossHealth(ab, mob);
                }
            }, 1);
        }

        private void updateRaidBossHealth(AbstractEntity ab, @NotNull ActiveMob mob) {
            Set<Player> players = getNearByPlayers(ab, getPlayerAmount(mob.getThreatTable()));
            double max = mob.getType().getHealth(mob) * getHealthPerPlayer(players.size());
            double now = ab.getHealth() * getHealthPerPlayer(players.size());
            if (ab.getMaxHealth() == max) return;
            double scale = max / ab.getMaxHealth();
            double setHealth = Math.min(now * scale, ab.getMaxHealth() * scale);
            ab.setMaxHealth(max);
            ab.setHealth(setHealth);
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

            ManaUtil.addMana(p, ManaUtil.getManaSteal(p));
        }
    }
}

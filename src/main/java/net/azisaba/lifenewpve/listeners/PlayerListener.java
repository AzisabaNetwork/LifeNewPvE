package net.azisaba.lifenewpve.listeners;

import com.google.common.collect.Maps;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.adapters.SkillAdapter;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.damage.DamageMetadata;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.libs.VectorTask;
import net.azisaba.lifenewpve.commands.WorldTeleportCommand;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {
    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener.Command(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.Quit(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.ChangeWorld(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.Interact(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.Pre(), lifeNewPvE);
    }

    public static class Interact extends PlayerListener implements VectorTask {

        @EventHandler
        public void onInteract(@NotNull PlayerInteractEvent e) {
            if (!e.getAction().equals(Action.PHYSICAL)) return;
            Block block = e.getClickedBlock();
            if (block == null) return;
            Player player = e.getPlayer();
            applyVelocityAndRunTask(player, player, getVector(block, player));

        }
    }

    public static class Quit extends PlayerListener {

        @EventHandler
        public void onQuit(@NotNull PlayerQuitEvent e) {
            WorldTeleportCommand.clearTeleporting(e.getPlayer());
        }
    }

    public static class ChangeWorld extends PlayerListener {

        @EventHandler
        public void onWorldChange(@NotNull PlayerChangedWorldEvent e) {
            Player p = e.getPlayer();
            if (MultiverseListener.isResetWorld(p.getWorld().getName())) {
                p.teleport(e.getFrom().getSpawnLocation());
            }
        }
    }

    public static class Command extends PlayerListener {

        @EventHandler(ignoreCancelled = true)
        public void onCommand(@NotNull PlayerCommandPreprocessEvent e) {
            Player p = e.getPlayer();
            if (!p.hasPermission("lifenewpve.reload.mythicmobs")) return;
            if (e.getMessage().contains("mm r") || e.getMessage().contains("mythicmobs r")) {
                MythicListener.call();
            }
        }
    }

    public static class Pre extends PlayerListener {

        @EventHandler
        public void onPre(@NotNull PrePlayerAttackEntityEvent e) {
            if (!e.willAttack()) return;
            ActiveMob mob = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(e.getAttacked());
            if (mob == null) return;
            Player p = e.getPlayer();
            AbstractPlayer player = BukkitAdapter.adapt(p).asPlayer();
            if (p.getAttackCooldown() != 1) {
                e.setCancelled(true);
                return;
            }

            SkillCaster caster = MythicBukkit.inst().getSkillManager().getCaster(player);
            double amount = player.getDamage();

            DamageMetadata meta = getDamageMetadata(caster, amount);
            SkillAdapter.get().doDamage(meta, mob.getEntity());
            e.setCancelled(true);
        }

        @NotNull
        private static DamageMetadata getDamageMetadata(SkillCaster caster, double amount) {
            DamageMetadata meta = new DamageMetadata(caster, amount, Maps.newTreeMap(), Maps.newTreeMap(), null, amount, false, true, false, false, EntityDamageEvent.DamageCause.ENTITY_ATTACK);
            meta.putBoolean("trigger_skills", false);
            meta.putBoolean("ignore_invulnerability", true);
            meta.putBoolean("damages_helmet", false);
            meta.putBoolean("no_anger", false);
            meta.putBoolean("ignore_shield", false);
            meta.putBoolean("no_impact", false);
            meta.putBoolean("ignore_effects", false);
            meta.putBoolean("ignore_resistance", false);
            return meta;
        }
    }
}

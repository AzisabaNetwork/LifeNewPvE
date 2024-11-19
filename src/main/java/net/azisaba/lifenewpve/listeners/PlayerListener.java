package net.azisaba.lifenewpve.listeners;

import com.google.common.collect.Maps;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.adapters.SkillAdapter;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.damage.DamageMetadata;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.azisaba.lifenewpve.LifeNewPvE;
import net.azisaba.lifenewpve.commands.ModeCommand;
import net.azisaba.lifenewpve.libs.Mana;
import net.azisaba.lifenewpve.libs.VectorTask;
import net.azisaba.lifenewpve.commands.WorldTeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerListener implements Listener {

    public void initialize(LifeNewPvE lifeNewPvE) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener.Command(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.Quit(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.ChangeWorld(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.Interact(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.Pre(), lifeNewPvE);
        pm.registerEvents(new PlayerListener.Join(), lifeNewPvE);
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

        @EventHandler
        public void onInteractFireWork(@NotNull PlayerInteractEvent e) {
            ItemStack item = e.getItem();
            if (item == null) return;
            if (item.getType() == Material.FIREWORK_ROCKET) {
                e.setCancelled(true);
            }
        }
    }

    public static class Quit extends PlayerListener {

        @EventHandler
        public void onQuit(@NotNull PlayerQuitEvent e) {
            Player p = e.getPlayer();
            WorldTeleportCommand.clearTeleporting(p);
            ManaListener.Modify.removeBossBar(p.getUniqueId());
        }
    }

    public static class ChangeWorld extends PlayerListener {

        @EventHandler
        public void onWorldChange(@NotNull PlayerChangedWorldEvent e) {
            Player p = e.getPlayer();
            //リセット中ワールドに入れないようにする。
            if (MultiverseListener.isResetWorld(p.getWorld().getName())) {
                p.teleport(e.getFrom().getSpawnLocation());
            }

            //ワールドを変えたら運営モードをoffにする。
            ModeCommand.switchMode(p, false);
        }
    }

    public static class Join extends PlayerListener {

        @EventHandler
        public void onJoin(@NotNull PlayerJoinEvent e) {
            Player p = e.getPlayer();
            JavaPlugin.getPlugin(LifeNewPvE.class).runSyncDelayed(()-> {

                ModeCommand.switchMode(p, false);
                new Mana(p).runTaskLaterAsynchronously(JavaPlugin.getPlugin(LifeNewPvE.class), 100);
            }, 40);
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

        @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
        public void onPre(@NotNull PrePlayerAttackEntityEvent event) {
            ActiveMob mob = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(event.getAttacked());
            if (mob == null) return;

            Player player = event.getPlayer();
            AbstractPlayer adaptedPlayer = BukkitAdapter.adapt(player).asPlayer();

            if (player.getAttackCooldown() != 1) {
                event.setCancelled(true);
                return;
            }

            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            boolean isCriticalHit = player.getFallDistance() > 0.25F;
            boolean isSweepingAttack = mainHandItem.getType().toString().toUpperCase().endsWith("SWORD");
            SkillCaster caster = MythicBukkit.inst().getSkillManager().getCaster(adaptedPlayer);
            double damageAmount = adaptedPlayer.getDamage();

            if (isCriticalHit) {
                player.playSound(player, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
                damageAmount*= 1.5;
            } else if (isSweepingAttack) {
                handleSweepingAttack(event, caster, mainHandItem, damageAmount, mob.getUniqueId());
            } else {
                player.playSound(player, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1, 1);
            }

            applyDamage(caster, calculateEnchantDamage(player, mainHandItem, event.getAttacked(), (float) damageAmount), mob.getEntity());
            event.setCancelled(true);
            Mana.modifyMana(player, 0.05);
        }

        private double calculateEnchantDamage(Player player, ItemStack itemStack, Entity entity, float damageAmount) {
            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            ServerLevel level = serverPlayer.serverLevel();
            return EnchantmentHelper.modifyDamage(level, CraftItemStack.asNMSCopy(itemStack), ((CraftEntity) entity).getHandle(), level.damageSources().playerAttack(serverPlayer), damageAmount);
        }

        private void handleSweepingAttack(@NotNull PrePlayerAttackEntityEvent event, SkillCaster caster, @NotNull ItemStack item, double damageAmount, UUID uuid) {
            double sweepingDamage = item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.SWEEPING_EDGE) ?
                    calculateSweepingDamage(item, damageAmount) : 0;
            if (sweepingDamage <= 0) return;
            DamageMetadata metaData = getDamageMetadata(caster, sweepingDamage, EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK);
            event.getAttacked().getLocation().getNearbyLivingEntities(1, 0, 1)
                    .stream()
                    .filter(l -> !l.getUniqueId().equals(uuid))
                    .forEach(entity -> SkillAdapter.get().doDamage(metaData, BukkitAdapter.adapt(entity)));
        }

        private double calculateSweepingDamage(@NotNull ItemStack item, double initialDamage) {
            double enchantLevel = item.getItemMeta().getEnchantLevel(Enchantment.SWEEPING_EDGE);
            return initialDamage * enchantLevel / (enchantLevel + 1);
        }

        private void applyDamage(SkillCaster caster, double damageAmount, AbstractEntity attackedEntity) {
            DamageMetadata meta = getDamageMetadata(caster, damageAmount, EntityDamageEvent.DamageCause.ENTITY_ATTACK);
            SkillAdapter.get().doDamage(meta, attackedEntity);

        }

        @NotNull
        private static DamageMetadata getDamageMetadata(SkillCaster caster, double amount, EntityDamageEvent.DamageCause cause) {
            DamageMetadata meta = new DamageMetadata(caster, amount, Maps.newTreeMap(), Maps.newTreeMap(), null, 1, false, true, false, false, cause);
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

package net.azisaba.lifenewpve.listeners.player;

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
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerPreAttackListener extends PlayerListener {

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
            player.playSound(player, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1);
        } else {
            player.playSound(player, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1, 1);
        }

        applyDamage(caster, damageAmount, mob.getEntity());
        event.setCancelled(true);
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

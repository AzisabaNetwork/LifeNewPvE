package net.azisaba.lifenewpve.listeners.entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EntityDamageListener extends EntityListener {

    @EventHandler
    public void onDamage(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.POISON)) return;
        if (!livingEntity.hasPotionEffect(PotionEffectType.POISON)) return;

        PotionEffect effect = livingEntity.getPotionEffect(PotionEffectType.POISON);
        int level = Objects.requireNonNull(effect).getAmplifier();

        event.setDamage(event.getDamage() + Math.sqrt(level));
    }
}

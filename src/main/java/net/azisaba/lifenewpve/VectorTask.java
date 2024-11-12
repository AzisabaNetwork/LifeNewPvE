package net.azisaba.lifenewpve;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public interface VectorTask {

    default Vector getVector(@NotNull Block b, LivingEntity p) {
        if (b.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            return p.getEyeLocation().getDirection().clone().normalize().multiply(3).setY(4);
        } else if (b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            return p.getEyeLocation().getDirection().clone().normalize().multiply(4).setY(3);
        }
        return new Vector(0, 0, 0);
    }

    default void applyVelocityAndRunTask(LivingEntity livingEntity, Player player, Vector vector) {
        applyVelocityWithSound(livingEntity, player, vector);

        AtomicBoolean cancel = new AtomicBoolean(false);
        for (int i = 1; i <= 120; i++) {
            if (cancel.get()) break;
            JavaPlugin.getPlugin(LifeNewPvE.class).runSyncDelayed(() -> {
                if (livingEntity.isOnGround()) {
                    cancel.set(true);
                    return;
                }
                Vector currentVelocity = livingEntity.getVelocity();
                double currentY = currentVelocity.getY();
                livingEntity.setVelocity(currentVelocity.clone().add(player.getEyeLocation().getDirection().clone().multiply(0.1)).multiply(1.05).setY(currentY));
                livingEntity.setFallDistance(0);
                player.setFallDistance(0);
            }, i);
        }
    }

    default void applyVelocityWithSound(@NotNull LivingEntity livingEntity, @NotNull Player player, Vector vector) {
        livingEntity.setVelocity(vector);
        player.playSound(player, Sound.BLOCK_PISTON_EXTEND, 1, 1);
    }
}

package net.azisaba.lifenewpve.listeners.potion;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.azisaba.lifenewpve.libs.event.PotionEffectEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PotionEffectListener extends PotionListener {

    private static final Multimap<UUID, Effect> POTION_EFFECT = ArrayListMultimap.create();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEffect(@NotNull PotionEffectEvent event) {
        if (event.isRemove()) {
            POTION_EFFECT.remove(event.getLivingEntity().getUniqueId(), new Effect(event.getName(), event.getLevel()));
        } else {
            POTION_EFFECT.put(event.getLivingEntity().getUniqueId(), new Effect(event.getName(), event.getLevel()));
        }
    }

    public static int getPotionEffectLevel(@NotNull LivingEntity entity, @NotNull String name) {
        return POTION_EFFECT.get(entity.getUniqueId()).stream().filter(effect -> effect.name().equals(name)).mapToInt(Effect::level).max().orElse(0);
    }

    protected record Effect(String name, int level) {}
}

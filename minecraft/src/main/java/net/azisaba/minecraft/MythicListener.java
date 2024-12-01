package net.azisaba.minecraft;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.azisaba.common.DamageColor;
import net.azisaba.common.NewPvE;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Random;

public class MythicListener implements Listener {

    private static final Random RANDOM = new Random();
    private static final int DISPLAY_DURATION_TICKS = 30;
    private static final int RANDOM_BOUND = 4;
    private static final double RANDOM_SCALE = 0.5;
    private static final double BASE_Y_OFFSET = 1.8;
    private static final String DAMAGE_PREFIX = "<white><bold>⚔";

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisplay(@NotNull MythicDamageEvent event) {
        AbstractEntity attacker = event.getCaster().getEntity();
        if (!attacker.isPlayer()) return;

        Player player = (Player) attacker.asPlayer().getBukkitEntity();
        AbstractEntity victim = event.getTarget();

        if (!isDisplayConditionValid(victim)) return;

        ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(victim.getUniqueId()).orElse(null);
        if (mob == null) return;

        showDamageDisplay(event, player, victim, mob);
    }

    private boolean isDisplayConditionValid(@NotNull AbstractEntity victim) {
        return !victim.isPlayer() && victim.isLiving();
    }

    private void showDamageDisplay(@NotNull MythicDamageEvent event, Player player, @NotNull AbstractEntity victim, @NotNull ActiveMob mob) {
        String element = event.getDamageMetadata().getElement();
        double multiplier = mob.getType().getDamageModifiers().getOrDefault(element, 1.0);
        double damage = formatDamage(event.getDamage() * multiplier);
        Location location = getRandomLocation(victim.getBukkitEntity().getLocation());
        Component component = MiniMessage.miniMessage().deserialize(getDamageElement(element) + damage);
        displayDamageText(player, location, component);
    }

    private void displayDamageText(Player player, @NotNull Location location, Component component) {
        int id = RANDOM.nextInt(Integer.MAX_VALUE);
        PacketHandler.spawnTextDisplay(player, location.getX(), location.getY(), location.getZ(), id);
        PacketHandler.setTextDisplayMeta(player, id, component);
        NewPvE.getTask().runSyncDelayed(() -> PacketHandler.removeTextDisplay(player, id), DISPLAY_DURATION_TICKS);
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
        return DamageColor.getColors().entrySet().stream()
                .filter(entry -> element.equalsIgnoreCase(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst().map(v -> DAMAGE_PREFIX.replaceAll("<white><bold>", v)).orElse(DAMAGE_PREFIX);
    }
}

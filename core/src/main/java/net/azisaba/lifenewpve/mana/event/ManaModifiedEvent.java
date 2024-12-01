package net.azisaba.lifenewpve.mana.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ManaModifiedEvent extends Event {

        private static final HandlerList handler = new HandlerList();

        private final Player player;


        private final double result;


        public ManaModifiedEvent(Player player, double result) {
            this.player = player;
            this.result = result;
        }

        public Player getPlayer() {
            return player;
        }


        public double getResult() {
            return result;
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return handler;
        }

        @SuppressWarnings("unused")
        public static HandlerList getHandlerList() {
            return handler;
        }

}

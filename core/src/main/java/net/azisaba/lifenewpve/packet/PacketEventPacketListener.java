package net.azisaba.lifenewpve.packet;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PacketEventPacketListener extends SimplePacketListenerAbstract {

    public PacketEventPacketListener() {
        super(PacketListenerPriority.LOWEST);
    }

    private static final Set<UUID> set = new HashSet<>();

    public void onPacketPlaySend(@NotNull PacketPlaySendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.BLOCK_CHANGE) return;
        if (set.contains(event.getUser().getUUID())) return;

        Player player = event.getPlayer();
        if (player == null) return;

        if (!player.getWorld().getName().contains("oak_wood_land")) return;
        event.setCancelled(true);
    }
}

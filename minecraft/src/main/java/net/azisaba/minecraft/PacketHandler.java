package net.azisaba.minecraft;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketHandler {

    private static void sendPacket(Player p, net.minecraft.network.protocol.Packet<?> packet) {
        ((CraftPlayer) p).getHandle().connection.sendPacket(packet);
    }

    public static void spawnTextDisplay(@NotNull Player p, double x, double y, double z, int id) {
        ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(id, UUID.randomUUID(), x, y, z, 0f, 0f,
                EntityType.TEXT_DISPLAY, id, Vec3.ZERO, 0);
        sendPacket(p, packet);

    }

    public static void setTextDisplayMeta(Player p, int id, Component text) {
        net.minecraft.network.chat.Component comp = PaperAdventure.asVanilla(text);
        List<SynchedEntityData.DataValue<?>> list = new ArrayList<>();

        list.add(SynchedEntityData.DataValue.create(EntityDataSerializers.COMPONENT.createAccessor(23), comp));
        list.add(SynchedEntityData.DataValue.create(EntityDataSerializers.BYTE.createAccessor(15), (byte) 3));
        list.add(SynchedEntityData.DataValue.create(EntityDataSerializers.BYTE.createAccessor(26), (byte) 255));
        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(id, list);

        sendPacket(p, packet);
    }

    public static void removeTextDisplay(Player p, int id) {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(id);
        sendPacket(p, packet);
    }
}

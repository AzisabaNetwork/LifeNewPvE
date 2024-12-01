package net.azisaba.lifenewpve.mythicmobs;

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;

public class Placeholder {

    private final PlaceholderManager manager;

    public Placeholder(PlaceholderManager manager) {
        this.manager = manager;
    }

    public void init() {
        targetX();
        targetXDouble();
        targetY();
        targetYDouble();
        targetZ();
        targetZDouble();
    }

    private void targetX() {
        manager.register("life.l.x", io.lumine.mythic.core.skills.placeholders.Placeholder.location((l, s) ->
                String.valueOf(l.getBlockX() + 0.5)));
    }

    private void targetXDouble() {
        manager.register("life.l.x.double", io.lumine.mythic.core.skills.placeholders.Placeholder.location((l, s) ->
                String.valueOf(l.getX())));
    }

    private void targetY() {
        manager.register("life.l.y", io.lumine.mythic.core.skills.placeholders.Placeholder.location((l, s) ->
                String.valueOf(l.getBlockY())));
    }

    private void targetYDouble() {
        manager.register("life.l.y.double", io.lumine.mythic.core.skills.placeholders.Placeholder.location((l, s) ->
                String.valueOf(l.getY())));
    }

    private void targetZ() {
        manager.register("life.l.z", io.lumine.mythic.core.skills.placeholders.Placeholder.location((l, s) ->
                String.valueOf(l.getBlockZ() + 0.5)));
    }

    private void targetZDouble() {
        manager.register("life.l.z.double", io.lumine.mythic.core.skills.placeholders.Placeholder.location((l, s) ->
                String.valueOf(l.getZ())));
    }
}

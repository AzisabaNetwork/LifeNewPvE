package net.azisaba.api.mana;

import org.bukkit.entity.Player;

public interface IManaBase  {

    double getMana();

    double getMaxMana();

    void setMana(double mana);

    void setMaxMana(double mana);

    boolean isManaFull();

    Player getPlayer();
}

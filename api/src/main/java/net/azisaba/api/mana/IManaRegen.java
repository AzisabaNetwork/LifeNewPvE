package net.azisaba.api.mana;

public interface IManaRegen {

    void startRegen(Runnable runnable, long delay, long period);

    void autoRegen();

    double getManaRegen();

    void setManaRegen(double manaRegen);

    void addManaRegen(double manaRegen);

    double getManaRegenBase();

    void stopRegen();
}

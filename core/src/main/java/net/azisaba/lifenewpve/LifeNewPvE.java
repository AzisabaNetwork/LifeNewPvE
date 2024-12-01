package net.azisaba.lifenewpve;

import com.onarandombox.MultiverseCore.MultiverseCore;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import net.azisaba.api.utils.SchedulerTask;
import net.azisaba.lifenewpve.commands.*;
import net.azisaba.lifenewpve.libs.enchantments.LifeEnchantment;
import net.azisaba.lifenewpve.libs.potion.LifePotion;
import net.azisaba.lifenewpve.listeners.*;
import net.azisaba.lifenewpve.mana.*;
import net.azisaba.lifenewpve.mana.listener.ManaListener;
import net.azisaba.lifenewpve.utils.key.LifeKey;
import net.azisaba.loreeditor.api.event.EventBus;
import net.azisaba.loreeditor.api.event.ItemEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class LifeNewPvE extends JavaPlugin implements SchedulerTask {

    public static final Random RANDOM = new Random();

    private static LifeNewPvE instance;

    private LifeKey key;

    @Override
    public void onEnable() {
        instance = this;
        key = new LifeKey(this);
        saveDefaultConfig();
        registerListeners();
        registerCommands();

        MythicListener.reloadMythic(20);
        spawnNotification();
        updatePointData();

        Bukkit.getOnlinePlayers().forEach(p -> {
            new Mana(p, this, ()-> ManaUtil.multiplyMana(p, 0.05), 200, 200);
            new LifePotion(this, p).init();
        });
        registerLore();
    }
    @Override
    public void onDisable() {
        ManaListener.Modify.removeAll();
    }

    private void updatePointData() {
       SavePointCommand.updateTags();
    }

    public static LifeNewPvE getInstance() {
        return instance;
    }

    public LifeKey getKey() {
        return key;
    }

    private void registerListeners() {
        new MythicListener().initialize(this);
        new MultiverseListener().initialize(this);
        new PlayerListener().initialize(this);
        new EntityListener().initialize(this);
        new WorldListener().initialize(this);
        new EnchantListener().initialize(this);
        new PrepareListener(this).initialize();
        new ChunkListener().initialize(this);
        new ManaListener().initialize(this);
        new InventoryListener().initialize(this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("worldregen")).setExecutor(new WorldRegenCommand(this));
        Objects.requireNonNull(getCommand("worldset")).setExecutor(new WorldSetCommand());
        Objects.requireNonNull(getCommand("worldcreate")).setExecutor(new WorldCreateCommand(this));
        Objects.requireNonNull(getCommand("worldteleport")).setExecutor(new WorldTeleportCommand());
        Objects.requireNonNull(getCommand("savepoint")).setExecutor(new SavePointCommand(this));
        Objects.requireNonNull(getCommand("mode")).setExecutor(new ModeCommand());
        Objects.requireNonNull(getCommand("setmana")).setExecutor(new SetManaCommand());
        Objects.requireNonNull(getCommand("setmaxmana")).setExecutor(new SetMaxManaCommand());
    }

    private void registerLore() {
        EventBus.INSTANCE.register(this, ItemEvent.class, 0, e -> {
            manaDescription(e);
            manaRegister(e);
            weaponRegister(e);
        });
    }

    private void manaDescription(@NotNull ItemEvent e) {
        List<Enchantment> get = ManaUtil.getContainsEnchantment(e.getBukkitItem());
        if (get.isEmpty()) return;

        Player player = e.getPlayer();
        for (Enchantment enchantment : get) {
            for (String message : LifeEnchantment.getEnchantmentDescription(enchantment)) {
                e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(getManaDescription(player, message)));
            }
        }
    }

    @NotNull
    private String getManaDescription(Player player, @NotNull String message) {
        if (message.contains("%{mana_regen}")) {
            double value = new ManaRegen(player, this).getManaRegen();
            message = message.replace("%{mana_regen}", getStealValue(value) + value);

        } else if (message.contains("%{mana_steal}")) {
            double value = new ManaSteal(player, this).getManaSteal();
            message = message.replace("%{mana_steal}", getStealValue(value) + value);

        } else if (message.contains("%{mana_booster}")) {
            double value = new ManaBooster(player, this).getManaBoost();
            message = message.replace("%{mana_booster}", getStealValue(value) + value);
        }
        return message;
    }

    private String getStealValue(double value) {
        String pre = "";
        if (value >= 0) {
            pre+= "+";
        }
        if (value <= 0) {
            pre+= "-";
        }
        return pre;
    }

    private void manaRegister(@NotNull ItemEvent e) {
        double mana = ManaUtil.getItemMana(e.getPlayer());
        if (mana == -1) return;

        String manaMessage = "§bマナ +V".replace("V", String.valueOf(mana));
        e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(""));
        e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text("§7装備したとき："));
        e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(manaMessage));
    }

    private void weaponRegister(@NotNull ItemEvent e) {
        String mmid = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(e.getBukkitItem());
        if (mmid == null) return;
        MythicItem mi = MythicBukkit.inst().getItemManager().getItem(mmid).orElse(null);
        if (mi == null) return;
        String group = mi.getGroup();
        if (group == null) return;
        e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text("§fカテゴリー: §7" + group));
    }


    @NotNull
    @Override
    public BukkitTask runAsync(Runnable runnable) {return Bukkit.getScheduler().runTaskAsynchronously(this, runnable);}


    @NotNull
    @Override
    public BukkitTask runSync(Runnable runnable) {return Bukkit.getScheduler().runTask(this, runnable);}


    @NotNull
    @Override
    public BukkitTask runSyncDelayed(Runnable runnable, long delay) {return Bukkit.getScheduler().runTaskLater(this, runnable, delay);}


    @NotNull
    @Override
    public BukkitTask runAsyncDelayed(Runnable runnable, long delay) {return Bukkit.getScheduler().runTaskLaterAsynchronously(this, runnable, delay);}

    @NotNull
    @Override
    public BukkitTask runSyncTimer(Runnable runnable, long delay, long loop) {return Bukkit.getScheduler().runTaskTimer(this, runnable, delay, loop);}


    @NotNull
    @Override
    public BukkitTask runAsyncTimer(Runnable runnable, long delay, long loop) {return Bukkit.getScheduler().runTaskTimerAsynchronously(this, runnable, delay, loop);}

    public void createWorld(String name, String gen, Difficulty dif, WorldType type, World.Environment environment, String seed, boolean generate) {
        MultiverseCore core = JavaPlugin.getPlugin(MultiverseCore.class);
        if (core.getMVWorldManager()
                .addWorld(name, environment, seed, type, generate, gen)) {

           runSyncDelayed(()-> {
               World w = core.getMVWorldManager().getMVWorld(name).getCBWorld();
               if (w == null) return;
               MultiverseWorldDeleteListener.configureWorldSettings(w, dif);

               if (MythicListener.isMythic()) {
                   MythicListener.reloadMythic(100);
               }
           }, 20);
        }
    }

    private void spawnNotification() {
        Component comp = Component.text("コマンド「/spawn」をすることでサーバーのスポーン地点へとテレポートできます。").clickEvent(ClickEvent.suggestCommand("/spawn"));
        runAsyncTimer(()-> {
            for (World world : Bukkit.getWorlds()) {
                if (!world.getName().contains("resource")) continue;
                for (Player player : world.getPlayers()) {
                    if (WorldTeleportCommand.isTeleporting(player)) {
                        player.sendMessage(comp);
                        WorldTeleportCommand.clearTeleporting(player);
                    }
                }
            }
        }, 36000, 36000);
    }
}

package net.azisaba.lifenewpve.libs.attributes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.azisaba.lifenewpve.LifeNewPvE;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.*;

public class AttributeBuilder {

    private static final Random RANDOM = new Random();
    private static final Map<String, Attribute> ATTRIBUTE_MAP = initializeAttributeMap();

    private static double getRandomValue(double max, int level) {
        NumberFormat numFormat = NumberFormat.getNumberInstance();
        numFormat.setMaximumFractionDigits(2);
        return Double.parseDouble(numFormat.format(max / level * (RANDOM.nextInt(level) + 1)));
    }

    protected record AttributeCreator(String key, double max, int level, AttributeModifier.Operation operation) {
    }

    @NotNull
    @Contract(" -> new")
    private static AttributeCreator[] getAttributeCreators() {
        return new AttributeCreator[]{
                new AttributeCreator("lifenewpve_attack_damage_num",5, 20, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_attack_damage_scalar",0.1, 20, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_movement_speed_scalar",0.1, 20, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_armor_num",20, 20, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_armor_scalar",0.5, 20, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_toughness_num",10, 20, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_toughness_scalar",0.25, 20, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_knockback_resistance_num",0.1, 20, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_knockback_resistance_scalar",0.2, 20, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_max_health_num", 10,20, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_max_health_scalar",0.1, 20, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_attack_knockback_num",5, 20, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_attack_speed_scalar",0.5, 50, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_fall_damage_multiplier_scalar",0.25, 25, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_luck_num", 5,20, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_safe_fall_distance_num",5, 20, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_step_height_num",2, 20, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_gravity_scalar",0.25, 25, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_jump_strength_scalar",0.5, 50, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_jump_strength_num",2, 50, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_burning_time_scalar",0.25 , 25, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_explosion_knockback_resistance_scalar",0.5 , 25, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_movement_efficiency_scalar",0.1 ,10, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_oxygen_bonus_scalar",0.25 ,25,AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_water_movement_efficiency_scalar",0.15 ,15, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_block_interaction_range_num",1,5, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_entity_interaction_range_num",1,5, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_block_break_speed_num",5,10, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_mining_efficiency_num", 5 ,10, AttributeModifier.Operation.ADD_NUMBER),
                new AttributeCreator("lifenewpve_mining_efficiency_scalar",0.1 ,20, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_sneaking_speed_scalar",0.5,10, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_submerged_mining_speed_scalar",0.25 ,25, AttributeModifier.Operation.ADD_SCALAR),
                new AttributeCreator("lifenewpve_sweeping_damage_ratio_scalar",0.5 ,10, AttributeModifier.Operation.ADD_SCALAR),
        };
    }

    @NotNull
    private static Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotGroup group) {
        AttributeCreator[] creators = getAttributeCreators();
        Multimap<Attribute, AttributeModifier> modifierMultimap = ArrayListMultimap.create();

        for (AttributeCreator creator : creators) {
            String keyLowerCase = creator.key.toLowerCase();
            ATTRIBUTE_MAP.forEach((key, attribute) -> {
                if (keyLowerCase.contains(key)) {
                    addModifier(modifierMultimap, attribute, creator, group);
                }
            });
        }
        return modifierMultimap;
    }

    private static void addModifier(@NotNull Multimap<Attribute, AttributeModifier> modifierMultimap, Attribute attribute, @NotNull AttributeCreator creator, EquipmentSlotGroup group) {
        modifierMultimap.put(attribute, new AttributeModifier(
                getKey(creator.key),
                getRandomValue(creator.max(), creator.level()),
                creator.operation(),
                group
        ));
    }

    @NotNull
    @Contract("_ -> new")
    private static NamespacedKey getKey(@NotNull String key) {
        return new NamespacedKey(JavaPlugin.getPlugin(LifeNewPvE.class), key.toLowerCase());
    }

    public static ItemStack getItemStack(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        removeModifiers(meta);
        EquipmentSlotGroup group = determineEquipmentSlotGroup(item.getType().toString().toLowerCase());
        Multimap<Attribute, AttributeModifier> modifiers = getAttributeModifiers(group);

        applyRandomModifiers(meta, modifiers);

        item.setItemMeta(meta);
        return item;
    }

    private static void removeModifiers(ItemMeta meta) {
        if (meta == null || meta.getAttributeModifiers() == null) return;
        Collection<Map.Entry<Attribute, AttributeModifier>> entries = meta.getAttributeModifiers().entries();
        for (Map.Entry<Attribute, AttributeModifier> attribute : entries) {
            if (attribute.getValue().getName().contains("lifenewpve")) {
                meta.removeAttributeModifier(attribute.getKey(), attribute.getValue());
            }
        }
    }

    @NotNull
    private static Map<String, Attribute> initializeAttributeMap() {
        Map<String, Attribute> attributeMap = new HashMap<>();
        attributeMap.put("attack_damage", Attribute.GENERIC_ATTACK_DAMAGE);
        attributeMap.put("movement_speed", Attribute.GENERIC_MOVEMENT_SPEED);
        attributeMap.put("armor", Attribute.GENERIC_ARMOR);
        attributeMap.put("toughness", Attribute.GENERIC_ARMOR_TOUGHNESS);
        attributeMap.put("knockback_resistance", Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        attributeMap.put("max_health", Attribute.GENERIC_MAX_HEALTH);
        attributeMap.put("attack_knockback", Attribute.GENERIC_ATTACK_KNOCKBACK);
        attributeMap.put("attack_speed", Attribute.GENERIC_ATTACK_SPEED);
        attributeMap.put("fall_damage_multiplier", Attribute.GENERIC_FALL_DAMAGE_MULTIPLIER);
        attributeMap.put("luck", Attribute.GENERIC_LUCK);
        attributeMap.put("safe_fall_distance", Attribute.GENERIC_SAFE_FALL_DISTANCE);
        attributeMap.put("step_height", Attribute.GENERIC_STEP_HEIGHT);
        attributeMap.put("gravity", Attribute.GENERIC_GRAVITY);
        attributeMap.put("jump_strength", Attribute.GENERIC_JUMP_STRENGTH);
        attributeMap.put("burning_time", Attribute.GENERIC_BURNING_TIME);
        attributeMap.put("explosion_knockback_resistance", Attribute.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE);
        attributeMap.put("movement_efficiency", Attribute.GENERIC_MOVEMENT_EFFICIENCY);
        attributeMap.put("oxygen_bonus", Attribute.GENERIC_OXYGEN_BONUS);
        attributeMap.put("water_movement_efficiency", Attribute.GENERIC_WATER_MOVEMENT_EFFICIENCY);
        attributeMap.put("block_interaction_range", Attribute.PLAYER_BLOCK_INTERACTION_RANGE);
        attributeMap.put("entity_interaction_range", Attribute.PLAYER_ENTITY_INTERACTION_RANGE);
        attributeMap.put("block_break_speed", Attribute.PLAYER_BLOCK_BREAK_SPEED);
        attributeMap.put("mining_efficiency", Attribute.PLAYER_MINING_EFFICIENCY);
        attributeMap.put("sneaking_speed", Attribute.PLAYER_SNEAKING_SPEED);
        attributeMap.put("submerged_mining_speed", Attribute.PLAYER_SUBMERGED_MINING_SPEED);
        attributeMap.put("sweeping_damage_ratio", Attribute.PLAYER_SWEEPING_DAMAGE_RATIO);

        return attributeMap;
    }

    private static EquipmentSlotGroup determineEquipmentSlotGroup(@NotNull String itemType) {
        if (itemType.contains("helmet")) {
            return EquipmentSlotGroup.HEAD;
        } else if (itemType.contains("chestplate")) {
            return EquipmentSlotGroup.CHEST;
        } else if (itemType.contains("leggings")) {
            return EquipmentSlotGroup.LEGS;
        } else if (itemType.contains("boots")) {
            return EquipmentSlotGroup.FEET;
        } else {
            return EquipmentSlotGroup.MAINHAND;
        }
    }

    private static void applyRandomModifiers(ItemMeta meta, @NotNull Multimap<Attribute, AttributeModifier> modifiers) {
        int loopCount = determineLoopCount();

        List<Attribute> attributes = new ArrayList<>(modifiers.keySet());
        for (int i = 0; i < loopCount; i++) {
            int randomAttrIndex = RANDOM.nextInt(attributes.size());
            Attribute attribute = attributes.get(randomAttrIndex);
            List<AttributeModifier> modifierList = new ArrayList<>(modifiers.get(attribute));
            int randomModIndex = RANDOM.nextInt(modifierList.size());
            meta.addAttributeModifier(attribute, modifierList.get(randomModIndex));
        }
    }

    private static int determineLoopCount() {
        int randomValue = RANDOM.nextInt(100) + 1;
        if (randomValue < 60) {
            return 1;
        } else if (randomValue < 85) {
            return 2;
        } else if (randomValue < 95) {
            return 3;
        } else if (randomValue < 99) {
            return 4;
        } else {
            return 5;
        }
    }
}
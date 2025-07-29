package dev.attackeight.black_market_tweaks.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

public class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> BASE_TRADES;

    public static final ForgeConfigSpec.ConfigValue<String> ITEM;
    public static final ForgeConfigSpec.ConfigValue<Integer> COST;

    static {
        BASE_TRADES = BUILDER.comment("The number of base trades available in the black market")
                .defineInRange("baseTrades", 3, 0, 6);
        ITEM = BUILDER.comment("The item to be used as a reroll item in the black market (namespace:key)")
                .define("item", "minecraft:diamond", ModConfig::validateItemName);
        COST = BUILDER.comment("The cost of rerolling a black market trade")
                .defineInRange("rerollCost", 1, 0, Integer.MAX_VALUE);
        SPEC = BUILDER.build();
    }

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && ForgeRegistries.ITEMS.containsKey(ResourceLocation.tryParse(itemName));
    }
}

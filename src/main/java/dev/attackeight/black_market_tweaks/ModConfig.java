package dev.attackeight.black_market_tweaks;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> ITEM;

    static {
        ITEM = BUILDER.comment("The item to be used as a reroll item in the black market (rl)")
                .define("item", "minecraft:diamond");
        SPEC = BUILDER.build();
    }
}

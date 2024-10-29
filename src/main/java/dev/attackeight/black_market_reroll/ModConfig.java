package dev.attackeight.black_market_reroll;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ModConfig {

    ModConfig(ForgeConfigSpec.Builder builder) {
        ForgeConfigSpec.ConfigValue<String> item =
                builder.define("item", "minecraft:diamond");
    }

    static {
        Pair<ModConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder()
                .configure(ModConfig::new);
        // Store pair values in some constant field
    }
}

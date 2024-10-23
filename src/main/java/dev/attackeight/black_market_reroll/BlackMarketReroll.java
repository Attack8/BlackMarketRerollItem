package dev.attackeight.black_market_reroll;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(BlackMarketReroll.ID)
public class BlackMarketReroll {

    public static final String ID = "black_market_reroll";

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public BlackMarketReroll() {}

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(BlackMarketReroll.ID, path);
    }

}

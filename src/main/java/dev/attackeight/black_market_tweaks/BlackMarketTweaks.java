package dev.attackeight.black_market_tweaks;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import org.slf4j.Logger;

@Mod(BlackMarketTweaks.ID)
public class BlackMarketTweaks {

    public static final String ID = "black_market_tweaks";

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static ItemLike item;

    public BlackMarketTweaks() {
        ModLoadingContext.get().registerConfig(Type.SERVER, ModConfig.SPEC, ID + "-server.toml");
    }

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(BlackMarketTweaks.ID, path);
    }

}

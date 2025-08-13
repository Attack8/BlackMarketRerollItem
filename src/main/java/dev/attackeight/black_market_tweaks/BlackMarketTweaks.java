package dev.attackeight.black_market_tweaks;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(BlackMarketTweaks.ID)
public class BlackMarketTweaks {

    public static final String ID = "black_market_tweaks";

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static ItemLike item;

    public BlackMarketTweaks() {
        ModLoadingContext.get().registerConfig(Type.SERVER, ModConfig.SPEC, ID + "-server.toml");
    }

    private static final Map<UUID, BlockPos> LAST_CLICKED_POS = new HashMap<>();

    public static void setLastClickedPos(UUID id, BlockPos pos) {
        LAST_CLICKED_POS.put(id, pos);
    }

    public static BlockPos getLastClickedPos(UUID id) {
        return LAST_CLICKED_POS.get(id);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }
}

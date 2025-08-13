package dev.attackeight.black_market_tweaks.init;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.init.ModTextureAtlases;

public class ModTextures {

    public static final TextureAtlasRegion BLACK_MARKET_BACKGROUND = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, BlackMarketTweaks.id("gui/screen/black_market"));
    public static final TextureAtlasRegion BLACK_MARKET_ORNAMENT = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, BlackMarketTweaks.id("gui/screen/black_market_ornament"));
    public static final TextureAtlasRegion BLACK_MARKET_REROLL_ORNAMENT = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, BlackMarketTweaks.id("gui/screen/black_market_reroll_ornament"));
}

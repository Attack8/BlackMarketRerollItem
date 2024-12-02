package dev.attackeight.black_market_tweaks;

import iskallia.vault.VaultMod;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.init.ModTextureAtlases;

public class TextureDefinitions {

    public static final TextureAtlasRegion BLACK_MARKET_BACKGROUND;
    public static final TextureAtlasRegion BLACK_MARKET_REROLL_ORNAMENT;
    static {
        BLACK_MARKET_BACKGROUND = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/black_market"));
        BLACK_MARKET_REROLL_ORNAMENT = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/black_market_reroll_ornament"));
    }
}

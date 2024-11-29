package dev.attackeight.black_market_tweaks;

import iskallia.vault.VaultMod;
import iskallia.vault.client.atlas.TextureAtlasRegion;
import iskallia.vault.init.ModTextureAtlases;

public class Client {

    public static final TextureAtlasRegion BLACK_MARKET_BACKGROUND;
    static {
        BLACK_MARKET_BACKGROUND = TextureAtlasRegion.of(ModTextureAtlases.SCREEN, VaultMod.id("gui/screen/black_market"));
    }
}

package dev.attackeight.black_market_tweaks.gui;

import iskallia.vault.util.LegendaryScreenParticle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class LateInitLegendaryParticle extends LegendaryScreenParticle {
    private final int xOffset;
    private final int yOffset;

    public LateInitLegendaryParticle(AbstractContainerScreen<?> screen, int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        init(screen);
    }

    public LateInitLegendaryParticle init(AbstractContainerScreen<?> screen) {
        spawnedPosition(screen.getGuiLeft() + xOffset, screen.getGuiTop() + yOffset);
        return this;
    }
}

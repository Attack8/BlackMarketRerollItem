package dev.attackeight.black_market_tweaks.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.ClientShardTradeData;
import iskallia.vault.client.gui.framework.element.DynamicLabelElement;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.client.gui.framework.spatial.spi.ISize;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 'Borrowed' from iskallia.vault.client.gui.screen.ShardTradeScreen$CountDownElement
 */
public final class CountDownElement extends DynamicLabelElement<Component, CountDownElement> {
    public CountDownElement(IPosition position, ISize size) {
        super(position, size, () -> TextComponent.EMPTY, LabelTextStyle.shadow());
        update();
    }

    @Override
    public void render(IElementRenderer renderer, @NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(renderer, poseStack, mouseX, mouseY, partialTick);
        update();
    }

    public void update() {
        LocalDateTime endTime = ClientShardTradeData.getNextReset();
        LocalDateTime nowTime = LocalDateTime.now(ZoneId.of("UTC")).withNano(0);
        LocalTime diff = LocalTime.MIN.plusSeconds(ChronoUnit.SECONDS.between(nowTime, endTime));
        Component component = new TextComponent(diff.format(DateTimeFormatter.ISO_LOCAL_TIME));
        this.onValueChanged(component);
    }

    @Override
    protected void onValueChanged(Component component) {
        this.set(component);
    }
}

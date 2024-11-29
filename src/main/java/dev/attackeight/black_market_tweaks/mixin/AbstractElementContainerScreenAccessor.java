package dev.attackeight.black_market_tweaks.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.client.gui.framework.element.spi.ElementStore;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nonnull;

@Mixin(value = AbstractElementContainerScreen.class, remap = false)
public interface AbstractElementContainerScreenAccessor {

    @Accessor
    ElementStore getElementStore();

    @Invoker
    void invokeRenderElements(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick);

    @Invoker
    void invokeRenderSlotItems(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick);

    @Invoker
    void invokeRenderDebug(@Nonnull PoseStack poseStack);

    @Invoker
    void invokeRenderTooltips(@Nonnull PoseStack poseStack, int mouseX, int mouseY);

    @Invoker
    void invokeRenderBackgroundFill(@Nonnull PoseStack stack);

    @Invoker(value = "getGuiSpatial")
    ISpatial getGuiSpacial();

    @Accessor
    boolean getNeedsLayout();

    @Invoker
    void invokeLayout(ISpatial parentSpatial);

    @Accessor
    void setNeedsLayout(boolean bool);
}

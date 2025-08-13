package dev.attackeight.black_market_tweaks.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import dev.attackeight.black_market_tweaks.CountDownElement;
import dev.attackeight.black_market_tweaks.TextureDefinitions;
import iskallia.vault.client.ClientExpertiseData;
import iskallia.vault.client.ClientShardTradeData;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.*;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.render.Tooltips;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IMutableSpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import iskallia.vault.client.gui.framework.text.TextBorder;
import iskallia.vault.client.gui.screen.ShardTradeScreen;
import iskallia.vault.container.inventory.ShardTradeContainer;
import iskallia.vault.init.ModItems;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.init.ModSounds;
import iskallia.vault.item.ItemShardPouch;
import iskallia.vault.network.message.ServerboundResetBlackMarketTradesMessage;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.expertise.type.BlackMarketExpertise;
import iskallia.vault.util.LegendaryScreenParticle;
import iskallia.vault.util.ScreenParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Mixin(value = ShardTradeScreen.class, remap = false)
public abstract class ShardTradeScreenMixin extends AbstractElementContainerScreen<ShardTradeContainer> {

    public ShardTradeScreenMixin(ShardTradeContainer container, Inventory inventory, Component title, IElementRenderer elementRenderer, ITooltipRendererFactory tooltipRendererFactory) {
        super(container, inventory, title, elementRenderer, tooltipRendererFactory);
    }

    @Shadow protected LegendaryScreenParticle screenParticleLeft;

    @Shadow protected LegendaryScreenParticle screenParticleRight;

    @Unique protected ScreenParticle bmt$screenParticleLeft;

    @Unique protected ScreenParticle bmt$screenParticleRight;

    @Shadow private ButtonElement<?> omegaButton;

    @Shadow protected abstract boolean canBuyTrade(int tradeIndex);

    @Shadow protected abstract void buyTrade(int tradeIndex);

    @Shadow protected abstract void updateTradeLabels();

    @Shadow protected abstract boolean canBuyRandomTrade();

    @Shadow protected abstract void buyRandomTrade();

    @Shadow @Final private LabelElement<?> labelRandomTrade;

    @Shadow @Final private LabelElement<?>[] labelShopTrades;

    @Unique protected LabelElement<?> blackMarketTweaks$soulShardCount;

    @Shadow private float dt;

    @SuppressWarnings({"RawUseOfParameterized", "unchecked"})
    @Inject(method = "<init>", at = @At(value = "RETURN"), cancellable = true)
    private void allowSixTrades(ShardTradeContainer container, Inventory inventory, Component title, CallbackInfo ci) {

        Field labelShopTradesReflection = null;
        Field labelRandomTradeReflection = null;

        try {
            labelShopTradesReflection = ((ShardTradeScreen) (Object) this).getClass().getDeclaredField("labelShopTrades");
            labelShopTradesReflection.setAccessible(true);
            labelShopTradesReflection.set(this, new LabelElement[6]);
            labelRandomTradeReflection = ((ShardTradeScreen) (Object) this).getClass().getDeclaredField("labelRandomTrade");
            labelRandomTradeReflection.setAccessible(true);
        } catch (Exception exception) {
            BlackMarketTweaks.LOGGER.error("Exception thrown while tweaking black market: ", exception);
        }

        if (labelShopTradesReflection == null || labelRandomTradeReflection == null) {
            ci.cancel();
        }

        this.elementStore.removeAllElements();

        this.screenParticleLeft = (new LegendaryScreenParticle()).angleRange(150.0F, 210.0F).quantityRange(1, 2).delayRange(0, 10).lifespanRange(10, 50).sizeRange(1, 4).speedRange(0.05F, 0.45F).spawnedPosition(leftPos + 76, topPos + 76).spawnedWidthHeight(0, 28);
        this.screenParticleRight = (new LegendaryScreenParticle()).angleRange(-30.0F, 30.0F).quantityRange(1, 2).delayRange(0, 10).lifespanRange(10, 50).sizeRange(1, 4).speedRange(0.05F, 0.45F).spawnedPosition(leftPos + 77 + 90, topPos + 76).spawnedWidthHeight(0, 28);

        this.bmt$screenParticleLeft = (new LegendaryScreenParticle()).angleRange(150.0F, 210.0F).quantityRange(1, 2).delayRange(0, 10).lifespanRange(10, 50).sizeRange(1, 4).speedRange(0.05F, 0.45F).spawnedPosition(leftPos + 76, topPos + 76).spawnedWidthHeight(0, 28);
        this.bmt$screenParticleRight = (new LegendaryScreenParticle()).angleRange(-30.0F, 30.0F).quantityRange(1, 2).delayRange(0, 10).lifespanRange(10, 50).sizeRange(1, 4).speedRange(0.05F, 0.45F).spawnedPosition(leftPos + 77 + 90, topPos + 76).spawnedWidthHeight(0, 28);

        this.addElement((new TextureAtlasElement(this.getGuiSpatial(), TextureDefinitions.BLACK_MARKET_BACKGROUND))
                .layout((screen, gui, parent, world) -> world.translateXY(gui.left() - 55, gui.top()).size(Spatials.copy(gui))));
        this.addElement((new SlotsElement((ShardTradeScreen) (Object) this))
                .layout((screen, gui, parent, world) -> world.positionXY(gui)));
        this.addElement((new LabelElement(Spatials.positionXY(-47, 7), (new TextComponent("Black Market")).withStyle(Style.EMPTY.withColor(-12632257)), LabelTextStyle.defaultStyle())).layout((screen, gui, parent, world) ->
            world.translateXY(gui)));
        MutableComponent inventoryName = inventory.getDisplayName().copy();
        inventoryName.withStyle(Style.EMPTY.withColor(-12632257));
        this.addElement((LabelElement)(new LabelElement(Spatials.positionXY(8, 110), inventoryName, LabelTextStyle.defaultStyle()))
                .layout((screen, gui, parent, world) -> world.translateXY(gui)));
        this.addElement((new TextureAtlasElement(Spatials.positionXY(-37, 18), ScreenTextures.SOUL_SHARD_TRADE_ORNAMENT))
                .layout((screen, gui, parent, world) -> world.translateXY(gui)));
        this.addElement((FakeItemSlotElement)(new FakeItemSlotElement(Spatials.positionXY(-23, 34),
                () -> new ItemStack(ModItems.UNKNOWN_ITEM), () -> !this.canBuyRandomTrade()))
                .layout((screen, gui, parent, world) -> world.translateXY(gui)))
                .whenClicked(this::buyRandomTrade)
                .tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                    tooltipRenderer.renderTooltip(poseStack, new ItemStack(ModItems.UNKNOWN_ITEM), mouseX, mouseY, TooltipDirection.RIGHT);
                    return true;
                });
        this.addElement((new ItemStackDisplayElement(this.getGuiSpatial(), new ItemStack(ModItems.SOUL_SHARD)))
                .layout((screen, gui, parent, world) -> world.positionXY(gui.left() - 21, gui.top() + 54)));
        try {
            labelRandomTradeReflection.set(this, this.addElement((LabelElement)(new LabelElement(Spatials.positionXYZ(-13, 64, 200), TextComponent.EMPTY, LabelTextStyle.border8().center()))
                    .layout((screen, gui, parent, world) -> world.translateXYZ(gui))));
        } catch (IllegalAccessException e) {
            BlackMarketTweaks.LOGGER.error("Exception thrown while tweaking black market: ", e);
        }
        this.addElement((new TextureAtlasElement(this.getGuiSpatial(), TextureDefinitions.BLACK_MARKET_ORNAMENT))
                .layout((screen, gui, parent, world) -> world.positionXY(gui.left() + 24, gui.top() + 6)));
        this.addElement((new TextureAtlasElement(this.getGuiSpatial(), ScreenTextures.OMEGA_BLACK_MARKET_ORNAMENT))
                .layout((screen, gui, parent, world) -> world.positionXY(gui.left() + 22, gui.top() + 70)));
        this.addElement((new TextureAtlasElement(this.getGuiSpatial(), TextureDefinitions.BLACK_MARKET_ORNAMENT))
                .layout((screen, gui, parent, world) -> world.positionXY(gui.left() + 129, gui.top() + 6)));
        this.addElement((new TextureAtlasElement(this.getGuiSpatial(), ScreenTextures.OMEGA_BLACK_MARKET_ORNAMENT))
                .layout((screen, gui, parent, world) -> world.positionXY(gui.left() + 127, gui.top() + 70)));
        this.addElement((new TextureAtlasElement(this.getGuiSpatial(), TextureDefinitions.BLACK_MARKET_REROLL_ORNAMENT))
                .layout((screen, gui, parent, world) -> world.positionXY(gui.left() - 27, gui.top() + 80)));

        for(int i = 0; i < 2; ++i) {
            int tradeIndex = i;
            int yOffsetTrade = 10 + i * 33;
            (this.addElement(((ButtonElement)(new ButtonElement(Spatials.positionXY(28, yOffsetTrade), ScreenTextures.BUTTON_TRADE_WIDE_TEXTURES, () -> {}))
                    .layout((screen, gui, parent, world) -> world.translateXY(gui))).setDisabled(() -> !this.canBuyTrade(tradeIndex))))
                    .setEnabled(false);
            int yOffset = 14 + i * 33;
            this.addElement((FakeItemSlotElement)(new FakeItemSlotElement(Spatials.positionXY(91, yOffset), () -> {
                Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
                return trade == null ? ItemStack.EMPTY : trade.getA().copy();
            }, () -> !this.canBuyTrade(tradeIndex)
            )).setLabelStackCount().layout((screen, gui, parent, world) -> world.translateXY(gui)))
            .whenClicked(() -> this.buyTrade(tradeIndex))
            .tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
                if (trade != null && !trade.getA().isEmpty()) {
                    tooltipRenderer.renderTooltip(poseStack, trade.getA(), mouseX, mouseY, TooltipDirection.RIGHT);
                }

                return true;
            });
            this.addElement((ItemStackDisplayElement)(new ItemStackDisplayElement(this.getGuiSpatial(), new ItemStack(ModItems.SOUL_SHARD)))
                    .layout((screen, gui, parent, world) -> world.translateXY(gui.left() + 39, gui.top() + yOffset)));
            this.labelShopTrades[i] = (LabelElement)this.addElement((LabelElement)(new LabelElement(Spatials.positionXYZ(47, yOffset + 10, 200), TextComponent.EMPTY, LabelTextStyle.border8().center()))
                    .layout((screen, gui, parent, world) -> world.translateXYZ(gui)));
        }

        for(int i = 3; i < 5; ++i) {
            int tradeIndex = i;
            int yOffsetTrade = 10 + i * 33 - 99;
            (this.addElement(((ButtonElement)(new ButtonElement(Spatials.positionXY(133, yOffsetTrade), ScreenTextures.BUTTON_TRADE_WIDE_TEXTURES, () -> {
            })).layout((screen, gui, parent, world) -> world.translateXY(gui)))
            .setDisabled(() -> !this.canBuyTrade(tradeIndex))
            .tooltip(Tooltips.multi(() -> List.of(new TextComponent("Learn Marketer Expertise to Unlock"))))))
            .setEnabled(!this.blackMarketTweaks$checkExpertiseUtil(tradeIndex - 2));
            int yOffset = 14 + i * 33 - 99;
            this.addElement((FakeItemSlotElement)(new FakeItemSlotElement(Spatials.positionXY(196, yOffset), () -> {
                Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
                return trade == null ? ItemStack.EMPTY : trade.getA().copy();
            }, () -> !this.canBuyTrade(tradeIndex)))
            .setLabelStackCount().layout((screen, gui, parent, world) -> world.translateXY(gui)))
            .whenClicked(() -> this.buyTrade(tradeIndex))
            .tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
                if (trade != null && !trade.getA().isEmpty()) {
                    tooltipRenderer.renderTooltip(poseStack, trade.getA(), mouseX, mouseY, TooltipDirection.RIGHT);
                }

                return true;
            });
            this.addElement((ItemStackDisplayElement)(new ItemStackDisplayElement(this.getGuiSpatial(), new ItemStack(ModItems.SOUL_SHARD)))
                    .layout((screen, gui, parent, world) -> world.translateXY(gui.left() + 144, gui.top() + yOffset)));
            this.labelShopTrades[i] = (LabelElement)this.addElement((LabelElement)(new LabelElement(Spatials.positionXYZ(152, yOffset + 10, 200), TextComponent.EMPTY, LabelTextStyle.border8().center()))
                    .layout((screen, gui, parent, world) -> world.translateXYZ(gui)));
        }

        int tradeIndex = 2;
        int yOffsetTrade = 76;
        (this.addElement(this.omegaButton = ((ButtonElement)(new ButtonElement(Spatials.positionXY(27, yOffsetTrade - 1), ScreenTextures.OMEGA_BUTTON_TRADE_WIDE_TEXTURES, () -> {}))
        .layout((screen, gui, parent, world) -> world.translateXY(gui)))
        .setDisabled(() -> !this.canBuyTrade(tradeIndex))))
        .setEnabled(false);
        int yOffset = 80;
        this.addElement((FakeItemSlotElement)(new FakeItemSlotElement(Spatials.positionXY(91, yOffset), () -> {
            Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
            return trade == null ? ItemStack.EMPTY : trade.getA().copy();
        }, () -> !this.canBuyTrade(tradeIndex)))
        .setLabelStackCount().layout((screen, gui, parent, world) -> world.translateXY(gui)))
        .whenClicked(() -> {
            this.buyTrade(tradeIndex);
            this.screenParticleLeft.pop(4.0F, 20.0F);
            this.screenParticleRight.pop(4.0F, 20.0F);
        }).tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
            Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
            if (trade != null && !trade.getA().isEmpty()) {
                tooltipRenderer.renderTooltip(poseStack, trade.getA(), mouseX, mouseY, TooltipDirection.RIGHT);
            }

            return true;
        });
        this.addElement((ItemStackDisplayElement)(new ItemStackDisplayElement(this.getGuiSpatial(), new ItemStack(ModItems.SOUL_SHARD)))
                .layout((screen, gui, parent, world) -> world.positionXY(gui.left() + 39, gui.top() + yOffset)));
        this.labelShopTrades[2] = (LabelElement)this.addElement((LabelElement)(new LabelElement(Spatials.positionXYZ(47, yOffset + 10, 200), TextComponent.EMPTY, LabelTextStyle.border8().center()))
                .layout((screen, gui, parent, world) -> world.translateXYZ(gui)));

        int newTradeIndex = 5;
        (this.addElement(this.omegaButton = (ButtonElement) ((ButtonElement)(new ButtonElement(Spatials.positionXY(132, yOffsetTrade - 1), ScreenTextures.OMEGA_BUTTON_TRADE_WIDE_TEXTURES, () -> {}))
        .layout((screen, gui, parent, world) -> world.translateXY(gui)))
        .setDisabled(() -> !this.canBuyTrade(newTradeIndex))
        .tooltip(Tooltips.multi(() -> List.of(new TextComponent("Learn Marketer Expertise to Unlock"))))))
        .setEnabled(!this.blackMarketTweaks$checkExpertiseUtil(newTradeIndex - 2));
        this.addElement((FakeItemSlotElement)(new FakeItemSlotElement(Spatials.positionXY(196, yOffset), () -> {
            Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(newTradeIndex);
            return trade == null ? ItemStack.EMPTY : trade.getA().copy();
        }, () -> !this.canBuyTrade(newTradeIndex)))
        .setLabelStackCount().layout((screen, gui, parent, world) -> world.translateXY(gui)))
        .whenClicked(() -> {
            this.buyTrade(newTradeIndex);
            this.bmt$screenParticleLeft.pop(4.0F, 20.0F);
            this.bmt$screenParticleRight.pop(4.0F, 20.0F);
        }).tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
            Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(newTradeIndex);
            if (trade != null && !trade.getA().isEmpty()) {
                tooltipRenderer.renderTooltip(poseStack, trade.getA(), mouseX, mouseY, TooltipDirection.RIGHT);
            }

            return true;
        });
        this.addElement((ItemStackDisplayElement)(new ItemStackDisplayElement(this.getGuiSpatial(), new ItemStack(ModItems.SOUL_SHARD)))
                .layout((screen, gui, parent, world) -> world.positionXY(gui.left() + 144, gui.top() + yOffset)));
        this.labelShopTrades[5] = (LabelElement)this.addElement((LabelElement)(new LabelElement(Spatials.positionXYZ(152, yOffset + 10, 200), TextComponent.EMPTY, LabelTextStyle.border8().center()))
                .layout((screen, gui, parent, world) -> world.translateXYZ(gui)));

        LocalDateTime endTime = ClientShardTradeData.getNextReset();
        LocalDateTime nowTime = LocalDateTime.now(ZoneId.of("UTC")).withNano(0);
        LocalTime diff = LocalTime.MIN.plusSeconds(ChronoUnit.SECONDS.between(nowTime, endTime));
        Component component = new TextComponent(diff.format(DateTimeFormatter.ISO_LOCAL_TIME));
        IMutableSpatial var10003 = Spatials.positionXYZ(this.getGuiSpatial().width() / 2 - TextBorder.DEFAULT_FONT.get().width(component) / 2 - 11, -10, 200);
        int var10004 = TextBorder.DEFAULT_FONT.get().width(component);
        Objects.requireNonNull(TextBorder.DEFAULT_FONT.get());
        this.addElement((new CountDownElement(var10003, Spatials.size(var10004, 9), () -> component,
                LabelTextStyle.shadow())).layout((screen, gui, parent, world) -> world.translateXY(gui.x(), gui.y())));
        this.addElement((TextureAtlasElement)((TextureAtlasElement<?>)(new TextureAtlasElement(Spatials.positionXY(this.getGuiSpatial().width() / 2 - ScreenTextures.TAB_COUNTDOWN_BACKGROUND.width() / 2 - 10, -ScreenTextures.TAB_COUNTDOWN_BACKGROUND.height()), ScreenTextures.TAB_COUNTDOWN_BACKGROUND))
                .layout((screen, gui, parent, world) -> world.translateXY(gui)))
                .tooltip(Tooltips.multi(() -> List.of(new TextComponent("Shop resets in")))));

        Slot rerollSlot = this.menu.slots.get(36);
        this.addElement((ButtonElement)((ButtonElement)(new ButtonElement(Spatials.positionXY( this.getGuiSpatial().width() / 2 - ScreenTextures.TAB_COUNTDOWN_BACKGROUND.width() / 2 + 50, -ScreenTextures.TAB_COUNTDOWN_BACKGROUND.height()), ScreenTextures.BUTTON_RESET_TRADES_TEXTURES, () -> {
            ModNetwork.CHANNEL.sendToServer(ServerboundResetBlackMarketTradesMessage.INSTANCE);
            this.menu.getPlayer().level.playSound(this.menu.getPlayer(), this.menu.getPlayer().getX(), this.menu.getPlayer().getY(), this.menu.getPlayer().getZ(), ModSounds.SKILL_TREE_LEARN_SFX, SoundSource.BLOCKS, 0.75F, 1.0F);
        })).layout((screen, gui, parent, world) -> world.translateXY(gui)))
            .setDisabled(() -> !rerollSlot.hasItem()
        ).tooltip(Tooltips.multi(() -> {
            int numOfRollsLeft = rerollSlot.getItem().getCount();

            if (numOfRollsLeft > 0) {
                return List.of(new TextComponent("Rolls Left: " + numOfRollsLeft));
            } else {
                return List.of(new TextComponent("Put an item in the re-roll slot to re-roll"));
            }
        })));

        this.addElement(((new TextureAtlasElement(Spatials.positionXY(this.getGuiSpatial().width() / 2 - ScreenTextures.TAB_COUNTDOWN_BACKGROUND.width() - 40, -ScreenTextures.TAB_COUNTDOWN_BACKGROUND.height()), ScreenTextures.TAB_COUNTDOWN_BACKGROUND))
                .layout((screen, gui, parent, world) -> world.translateXY(gui))).tooltip(Tooltips.multi(() -> List.of(new TextComponent("Number of Soul Shards in Inventory")))));
        this.addElement((ItemStackDisplayElement)(new ItemStackDisplayElement(Spatials.positionXY(this.getGuiSpatial().width() / 2 - ScreenTextures.TAB_COUNTDOWN_BACKGROUND.width() - 37, -ScreenTextures.TAB_COUNTDOWN_BACKGROUND.height() + 3), new ItemStack(ModItems.SOUL_SHARD)))
                .layout((screen, gui, parent, world) -> world.translateXY(gui))).setScale(0.8f);

        this.blackMarketTweaks$soulShardCount = this.addElement((LabelElement)(new LabelElement(Spatials.positionXYZ(this.getGuiSpatial().width() / 2 - ScreenTextures.TAB_COUNTDOWN_BACKGROUND.width() - 8, -ScreenTextures.TAB_COUNTDOWN_BACKGROUND.height() + 5, 200), TextComponent.EMPTY, LabelTextStyle.shadow().center()))
                .layout((screen, gui, parent, world) -> world.translateXYZ(gui)));

        this.updateTradeLabels();
    }

    @Inject(method = "updateTradeLabels", at = @At("HEAD"), cancellable = true)
    private void allowMoreTradeLabels(CallbackInfo ci) {
        int playerShards = ItemShardPouch.getShardCount(Minecraft.getInstance().player);
        int randomCost = ClientShardTradeData.getRandomTradeCost();
        LocalDateTime nextReset = ClientShardTradeData.getNextReset();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC")).withNano(0);
        LocalTime diff = LocalTime.MIN.plusSeconds(ChronoUnit.SECONDS.between(now, nextReset));
        new TextComponent(diff.format(DateTimeFormatter.ISO_LOCAL_TIME));
        int randomCostColor = playerShards >= randomCost ? 16777215 : 8257536;
        Component randomCostComponent = (new TextComponent(String.valueOf(randomCost))).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(randomCostColor)));
        this.labelRandomTrade.set(randomCostComponent);
        int length = labelShopTrades.length;

        for(int i = 0; i < length; ++i) {
            Tuple<ItemStack, Integer> tradeInfo = ClientShardTradeData.getTradeInfo(i);
            if (tradeInfo == null) {
                this.labelShopTrades[i].set(TextComponent.EMPTY);
            } else {
                int tradeCost = tradeInfo.getB();
                int tradeCostColor = playerShards >= tradeCost ? 16777215 : 8257536;
                Component tradeCostComponent = (new TextComponent(String.valueOf(tradeCost))).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(tradeCostColor)));
                this.labelShopTrades[i].set(tradeCostComponent);
            }
        }

        if (blackMarketTweaks$soulShardCount != null) {
            char unit = ',';
            double shardCount = playerShards;
            if (playerShards >= 1000000) {
                unit = 'M';
                shardCount = shardCount / 1000000;
            } else if (playerShards >= 1000) {
                unit = 'k';
                shardCount = shardCount / 1000;
            }

            BigDecimal bd = BigDecimal.valueOf(shardCount).setScale(1, RoundingMode.HALF_UP);
            shardCount = bd.doubleValue();
            TextComponent text = new TextComponent(String.valueOf(shardCount));
            if (unit != ',') text.append(String.valueOf(unit));
            this.blackMarketTweaks$soulShardCount.set(text);
        }
        ci.cancel();
    }

    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true, remap = true)
    private void addMoreParticles(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        this.dt += partialTick;
        this.screenParticleLeft.spawnedPosition(leftPos + 27, topPos + 74).spawnedWidthHeight(0, 29);
        this.screenParticleRight.spawnedPosition(leftPos + 27 + 90, topPos + 74).spawnedWidthHeight(0, 29);
        this.bmt$screenParticleLeft.spawnedPosition(leftPos + 77 + 55, topPos + 74).spawnedWidthHeight(0, 29);
        this.bmt$screenParticleRight.spawnedPosition(leftPos + 77 + 90 + 55, topPos + 74).spawnedWidthHeight(0, 29);

        for(; this.dt >= 0.5F; this.dt -= 0.5F) {
            this.screenParticleLeft.tick();
            this.screenParticleRight.tick();
            this.bmt$screenParticleLeft.tick();
            this.bmt$screenParticleRight.tick();
            if (ClientShardTradeData.getAvailableTrades().containsKey(2)) {
                this.screenParticleLeft.pop();
                this.screenParticleRight.pop();
            }
            if (ClientShardTradeData.getAvailableTrades().containsKey(5)) {
                this.bmt$screenParticleLeft.pop();
                this.bmt$screenParticleRight.pop();
            }
        }

        if (this.needsLayout) {
            this.layout(Spatials.zero());
            this.needsLayout = false;
        }

        this.renderBackgroundFill(poseStack);
        this.renderElements(poseStack, mouseX, mouseY, partialTick);
        this.renderSlotItems(poseStack, mouseX, mouseY, partialTick);
        this.renderDebug(poseStack);
        this.screenParticleLeft.render(poseStack, partialTick);
        this.screenParticleRight.render(poseStack, partialTick);
        this.bmt$screenParticleLeft.render(poseStack, partialTick);
        this.bmt$screenParticleRight.render(poseStack, partialTick);
        this.renderTooltips(poseStack, mouseX, mouseY);
        ci.cancel();
    }

    @Inject(method = "canBuyTrade", at = @At("HEAD"), cancellable = true)
    private void checkExpertise(int tradeIndex, CallbackInfoReturnable<Boolean> cir) {
        Tuple<ItemStack, Integer> tradeInfo = ClientShardTradeData.getTradeInfo(tradeIndex);
        if (tradeInfo == null) {
            cir.setReturnValue(false);
        } else {
            cir.setReturnValue(ItemShardPouch.getShardCount(Minecraft.getInstance().player) >= tradeInfo.getB());
            if (cir.getReturnValue() && tradeIndex >= 3) {
                cir.setReturnValue(false);
                int lvlRequired = tradeIndex - 2;
                cir.setReturnValue(blackMarketTweaks$checkExpertiseUtil(lvlRequired));
            }
        }
    }

    @Unique
    private boolean blackMarketTweaks$checkExpertiseUtil(int lvlRequired) {
        List<TieredSkill> skills = ClientExpertiseData.getLearnedTalentNodes();

        for (TieredSkill skill : skills) {
            if (skill.getChild() instanceof BlackMarketExpertise && skill.getSpentLearnPoints() >= lvlRequired) {
                return true;
            }
        }
        return false;
    }
}

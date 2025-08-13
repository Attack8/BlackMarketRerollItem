package dev.attackeight.black_market_tweaks.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.attackeight.black_market_tweaks.gui.CountDownElement;
import dev.attackeight.black_market_tweaks.gui.LateInitLegendaryParticle;
import dev.attackeight.black_market_tweaks.init.ModConfig;
import dev.attackeight.black_market_tweaks.init.ModTextures;
import iskallia.vault.client.ClientExpertiseData;
import iskallia.vault.client.ClientShardTradeData;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.*;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.render.spi.ITooltipRendererFactory;
import iskallia.vault.client.gui.framework.screen.AbstractElementContainerScreen;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IMutableSpatial;
import iskallia.vault.client.gui.framework.spatial.spi.ISize;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
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

import java.text.NumberFormat;
import java.util.Locale;

@Mixin(value = ShardTradeScreen.class, priority = 1, remap = false)
public abstract class ShardTradeScreenMixin extends AbstractElementContainerScreen<ShardTradeContainer> {
    @Unique private static final int[] bmt$PARTICLE_COLORS = { -8185907, -9037875, -9758771, -10545203, -11397171 };
    @Unique private static final Style bmt$TITLE_TEXT = Style.EMPTY.withColor(-12632257);
    @Unique private static final Style bmt$ENABLED_TEXT = Style.EMPTY.withColor(16777215);
    @Unique private static final Style bmt$DISABLED_TEXT = Style.EMPTY.withColor(8257536);
    @Unique private static final NumberFormat bmt$NUMBER_FORMAT = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
    @Unique private static final Component RESET_TIME = new TextComponent("00:00:00");
    static {
        bmt$NUMBER_FORMAT.setMaximumFractionDigits(1);
    }

    @Shadow @Mutable @Final private LabelElement<?> labelRandomTrade;
    @Shadow protected LegendaryScreenParticle screenParticleLeft;
    @Shadow protected LegendaryScreenParticle screenParticleRight;
    @Shadow private ButtonElement<?> omegaButton;

    @Shadow @Mutable @Final private LabelElement<?>[] labelShopTrades;
    @Unique private LateInitLegendaryParticle[] bmt$particles;
    @Unique private ButtonElement<?>[] bmt$omegaButtons;
    @Unique protected LabelElement<?> bmt$soulShardCount;

    @Shadow private float dt;

    private ShardTradeScreenMixin(ShardTradeContainer container, Inventory inventory, Component title, IElementRenderer elementRenderer, ITooltipRendererFactory<AbstractElementContainerScreen<ShardTradeContainer>> tooltipRendererFactory) {
        super(container, inventory, title, elementRenderer, tooltipRendererFactory);
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void init(ShardTradeContainer container, Inventory inventory, Component title, CallbackInfo ci) {
        this.elementStore.removeAllElements();
        this.labelShopTrades = new LabelElement[6];
        this.bmt$particles = new LateInitLegendaryParticle[] {
                bmt$createParticle(27, 74, 150.0F, 210.0F),
                bmt$createParticle(77 + 55, 74, 150.0F, 210.0F),
                bmt$createParticle(27 + 90, 74, -30.0F, 30.0F),
                bmt$createParticle(77 + 55 + 90, 74, -30.0F, 30.0F)
        };
        this.bmt$omegaButtons = new ButtonElement[2];

        ISpatial guiSpatial = this.getGuiSpatial();

        // Core Gui
        this.addElement(new TextureAtlasElement<>(guiSpatial, ModTextures.BLACK_MARKET_BACKGROUND)
                .layout((screen, gui, parent, world) -> world.translateXY(gui.left() - 55, gui.top()).size(Spatials.copy(gui))));
        this.addElement(new LabelElement<>(Spatials.positionXY(-47, 7), new TextComponent("Black Market").withStyle(bmt$TITLE_TEXT), LabelTextStyle.defaultStyle())
                .layout(this::bmt$translateToGui));
        this.addElement(new SlotsElement<>(this).layout(this::bmt$translateToGui));

        // Reset Timer
        int countdownWidth = ScreenTextures.TAB_COUNTDOWN_BACKGROUND.width();
        int countdownHeight = ScreenTextures.TAB_COUNTDOWN_BACKGROUND.height();
        int resetWidth = TextBorder.DEFAULT_FONT.get().width(RESET_TIME);
        IMutableSpatial resetPosition = Spatials.positionXYZ(guiSpatial.width() / 2 - resetWidth / 2 - 11, -10, 200);
        this.addElement(new CountDownElement(resetPosition, Spatials.size(resetWidth, 9)).layout(this::bmt$translateToGui));
        this.addElement(new TextureAtlasElement<>(Spatials.positionXY(guiSpatial.width() / 2 - countdownWidth / 2 - 10, -countdownHeight), ScreenTextures.TAB_COUNTDOWN_BACKGROUND))
                .tooltip(() -> new TextComponent("Shop resets in"))
                .layout(this::bmt$translateToGui);
        this.addElement((new ItemStackDisplayElement<>(Spatials.positionXY(guiSpatial.width() / 2 - countdownWidth - 37, -countdownHeight + 3), new ItemStack(ModItems.SOUL_SHARD)))
                .layout(this::bmt$translateToGui)).setScale(0.8f);

        // ReRoll
        Slot rerollSlot = this.menu.slots.get(36);
        this.addElement(new ButtonElement<>(
                Spatials.positionXY( guiSpatial.width() / 2 - countdownWidth / 2 + 50, -countdownHeight),
                ScreenTextures.BUTTON_RESET_TRADES_TEXTURES,
                () -> {
                    ModNetwork.CHANNEL.sendToServer(ServerboundResetBlackMarketTradesMessage.INSTANCE);
                    this.menu.getPlayer().level.playSound(this.menu.getPlayer(), this.menu.getPlayer().getX(), this.menu.getPlayer().getY(), this.menu.getPlayer().getZ(), ModSounds.SKILL_TREE_LEARN_SFX, SoundSource.BLOCKS, 0.75F, 1.0F);
                })
                .setDisabled(() -> !rerollSlot.hasItem())
                .tooltip(() -> {
                    int numOfRollsLeft = rerollSlot.getItem().getCount();
                    return numOfRollsLeft > 0
                            ? new TextComponent("Rolls Left: " + numOfRollsLeft)
                            : new TextComponent("Put an item in the re-roll slot to re-roll");
                })
                .layout(this::bmt$translateToGui));
        this.addElement(new TextureAtlasElement<>(Spatials.positionXY(-26, 80), ModTextures.BLACK_MARKET_REROLL_ORNAMENT)
                .layout(this::bmt$translateToGui));

        // Soul Shard Count
        this.addElement(new TextureAtlasElement<>(Spatials.positionXY(guiSpatial.width() / 2 - countdownWidth - 40, -countdownHeight), ScreenTextures.TAB_COUNTDOWN_BACKGROUND)
                .tooltip(() -> new TextComponent("Number of Soul Shards in Inventory"))
                .layout(this::bmt$translateToGui));
        this.bmt$soulShardCount = this.addElement(new LabelElement<>(Spatials.positionXYZ(guiSpatial.width() / 2 - countdownWidth - 8, -countdownHeight + 5, 200), TextComponent.EMPTY, LabelTextStyle.shadow().center()))
                .layout(this::bmt$translateToGui);

        // Random Trade
        labelRandomTrade = this.addElement(new LabelElement<>(Spatials.positionXYZ(-13, 64, 200), TextComponent.EMPTY, LabelTextStyle.border8().center()))
                .layout(this::bmt$translateToGui);
        this.addElement(new ItemStackDisplayElement<>(guiSpatial, new ItemStack(ModItems.SOUL_SHARD))
                .layout((screen, gui, parent, world) -> world.positionXY(gui.left() - 21, gui.top() + 54)));
        this.addElement(new TextureAtlasElement<>(Spatials.positionXY(-37, 18), ScreenTextures.SOUL_SHARD_TRADE_ORNAMENT)
                .layout(this::bmt$translateToGui));
        this.addElement(new FakeItemSlotElement<>(Spatials.positionXY(-21, 34), () -> new ItemStack(ModItems.UNKNOWN_ITEM), () -> !this.canBuyRandomTrade())
                .whenClicked(this::buyRandomTrade)
                .tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                    tooltipRenderer.renderTooltip(poseStack, new ItemStack(ModItems.UNKNOWN_ITEM), mouseX, mouseY, TooltipDirection.RIGHT);
                    return true;
                })
                .layout(this::bmt$translateToGui));

        // Player Inventory
        this.addElement(new LabelElement<>(Spatials.positionXY(8, 110), playerInventoryTitle, LabelTextStyle.defaultStyle())
                .layout(this::bmt$translateToGui));

        // Trades
        for (int trade = 1; trade <= 6; ++trade) {
            int index = trade - 1;
            int xIndex = index < 3 ? 0 : 1;
            int yIndex = index % 3;
            boolean omega = yIndex == 2;

            int xOffset = (index < 3 ? 0 : 105) - (omega ? 1 : 0);
            int yOffset = yIndex * 33 - (omega ? 1 : 0);

            // Ornament
            if (yIndex == 0) {
                this.addElement((new TextureAtlasElement<>(guiSpatial, ModTextures.BLACK_MARKET_ORNAMENT))
                        .layout((screen, gui, parent, world) -> world.positionXY(gui.left() + 24 + xOffset, gui.top() + 6)));
            } else if (omega) {
                this.addElement((new TextureAtlasElement<>(guiSpatial, ScreenTextures.OMEGA_BLACK_MARKET_ORNAMENT))
                        .layout((screen, gui, parent, world) -> world.positionXY(gui.left() + 23 + xOffset, gui.top() + 70)));
            }

            // Background Button
            ButtonElement<?> button = this.addElement(new ButtonElement<>(
                    Spatials.positionXY(28 + xOffset, 10 + yOffset),
                    omega ? ScreenTextures.OMEGA_BUTTON_TRADE_WIDE_TEXTURES : ScreenTextures.BUTTON_TRADE_WIDE_TEXTURES,
                    () -> {}
            ));
            button.setDisabled(() -> !this.canBuyTrade(index));
            button.tooltip(() -> new TextComponent("Learn Marketer Expertise to Unlock"));
            button.layout(this::bmt$translateToGui);
            button.setEnabled(!this.bmt$hasTrade(trade));
            if (omega) {
                this.bmt$omegaButtons[xIndex] = button;
            }

            // Trade Cost Label
            this.addElement((new ItemStackDisplayElement<>(guiSpatial, new ItemStack(ModItems.SOUL_SHARD)))
                    .layout((screen, gui, parent, world) -> world.translateXY(gui.left() + 39 + xOffset, gui.top() + 14 + yOffset)));
            this.labelShopTrades[index] = this.addElement(new LabelElement<>(Spatials.positionXYZ(47 + xOffset, 14 + yOffset + 10, 200), TextComponent.EMPTY, LabelTextStyle.border8().center()))
                    .layout(this::bmt$translateToGui);

            // Trade Item Slot
            this.addElement(new FakeItemSlotElement<>(Spatials.positionXY(91 + xOffset, 14 + yOffset), () -> {
                Tuple<ItemStack, Integer> info = ClientShardTradeData.getTradeInfo(index);
                return info == null ? ItemStack.EMPTY : info.getA().copy();
            }, () -> !this.canBuyTrade(index)))
                    .setLabelStackCount()
                    .whenClicked(() -> {
                        if (omega) {
                            bmt$particles[xIndex].pop(4, 20);
                            bmt$particles[xIndex + 2].pop(4, 20);
                        }
                        this.buyTrade(index);
                    })
                    .tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                        Tuple<ItemStack, Integer> info = ClientShardTradeData.getTradeInfo(index);
                        if (info != null && !info.getA().isEmpty()) {
                            tooltipRenderer.renderTooltip(poseStack, info.getA(), mouseX, mouseY, TooltipDirection.RIGHT);
                        }
                        return true;
                    })
                    .layout(this::bmt$translateToGui);
        }

        this.updateTradeLabels();

        // Set Vanilla Fields
        screenParticleLeft = bmt$particles[0];
        screenParticleRight = bmt$particles[2];
        omegaButton = bmt$omegaButtons[0];
    }

    @Inject(method = "updateTradeLabels", at = @At("HEAD"), cancellable = true)
    private void updateLabels(CallbackInfo ci) {
        int playerShards = ItemShardPouch.getShardCount(menu.getPlayer());
        bmt$setCostLabel(this.labelRandomTrade, playerShards, ClientShardTradeData.getRandomTradeCost());

        for(int i = 0; i < labelShopTrades.length; ++i) {
            Tuple<ItemStack, Integer> tradeInfo = ClientShardTradeData.getTradeInfo(i);
            if (tradeInfo == null) {
                this.labelShopTrades[i].set(TextComponent.EMPTY);
            } else {
                bmt$setCostLabel(this.labelShopTrades[i], playerShards, tradeInfo.getB());
            }
        }

        if (bmt$soulShardCount != null) {
            this.bmt$soulShardCount.set(bmt$NUMBER_FORMAT.format(playerShards));
        }
        ci.cancel();
    }

    @Unique
    private void bmt$setCostLabel(LabelElement<?> label, int shards, int cost) {
        label.set(new TextComponent(String.valueOf(cost)).withStyle(shards >= cost ? bmt$ENABLED_TEXT : bmt$DISABLED_TEXT));
    }

    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true, remap = true)
    private void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.needsLayout) {
            this.layout(Spatials.zero());
            this.needsLayout = false;
        }

        this.renderBackgroundFill(poseStack);
        this.renderElements(poseStack, mouseX, mouseY, partialTick);
        this.renderSlotItems(poseStack, mouseX, mouseY, partialTick);
        this.renderDebug(poseStack);

        this.dt += partialTick;
        for(; this.dt >= 0.5F; this.dt -= 0.5F) {
            for (int i = 0; i < this.bmt$particles.length / 2; i++) {
                LateInitLegendaryParticle left = this.bmt$particles[i];
                LateInitLegendaryParticle right = this.bmt$particles[i + 2];
                left.init(this).tick();
                right.init(this).tick();

                int trade = (i + 1) * 3;
                if (ClientShardTradeData.getTradeInfo(trade - 1) != null) {
                    left.pop();
                    right.pop();
                }
                left.render(poseStack, partialTick);
                right.render(poseStack, partialTick);
            }
        }

        this.renderTooltips(poseStack, mouseX, mouseY);
        ci.cancel();
    }

    @Inject(method = "canBuyTrade", at = @At("HEAD"), cancellable = true)
    private void canBuyTrade(int tradeIndex, CallbackInfoReturnable<Boolean> cir) {
        Tuple<ItemStack, Integer> tradeInfo = ClientShardTradeData.getTradeInfo(tradeIndex);
        cir.setReturnValue(tradeInfo != null
                && ItemShardPouch.getShardCount(menu.getPlayer()) >= tradeInfo.getB()
                && bmt$hasTrade(tradeIndex + 1));
    }

    @Unique
    private boolean bmt$hasTrade(int trade) {
        if (trade <= ModConfig.BASE_TRADES.get()) {
            return true;
        }

        int level = trade - ModConfig.BASE_TRADES.get();
        for (TieredSkill skill : ClientExpertiseData.getLearnedTalentNodes()) {
            if (skill.getChild() instanceof BlackMarketExpertise && skill.getSpentLearnPoints() >= level) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private LateInitLegendaryParticle bmt$createParticle(int xOffset, int yOffset, float min, float max) {
        return (LateInitLegendaryParticle) new LateInitLegendaryParticle(this, xOffset, yOffset)
                .spawnedWidthHeight(0, 28).angleRange(min, max)
                .quantityRange(1, 2).delayRange(0, 10).lifespanRange(10, 50).sizeRange(1, 4).speedRange(0.05F, 0.45F)
                .particleColours(bmt$PARTICLE_COLORS);
    }

    @Unique
    private void bmt$translateToGui(ISize screen, ISpatial gui, ISpatial parent, IMutableSpatial world) {
        world.translateXY(gui);
    }

    @Shadow protected abstract void updateTradeLabels();

    @Shadow protected abstract boolean canBuyTrade(int tradeIndex);
    @Shadow protected abstract void buyTrade(int tradeIndex);

    @Shadow protected abstract boolean canBuyRandomTrade();
    @Shadow protected abstract void buyRandomTrade();
}

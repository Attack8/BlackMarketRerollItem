package dev.attackeight.black_market_tweaks.mixin;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import dev.attackeight.black_market_tweaks.CountDownElement;
import iskallia.vault.client.ClientExpertiseData;
import iskallia.vault.client.ClientShardTradeData;
import iskallia.vault.client.gui.framework.ScreenTextures;
import iskallia.vault.client.gui.framework.element.*;
import iskallia.vault.client.gui.framework.render.TooltipDirection;
import iskallia.vault.client.gui.framework.render.Tooltips;
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
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.expertise.type.BlackMarketExpertise;
import iskallia.vault.util.ScreenParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Mixin(value = ShardTradeScreen.class, remap = false)
public abstract class ShardTradeScreenMixin {

    @Shadow protected ScreenParticle screenParticleLeft;

    @Shadow protected ScreenParticle screenParticleRight;

    @Shadow private ButtonElement<?> omegaButton;

    @Shadow protected abstract boolean canBuyTrade(int tradeIndex);

    @Shadow protected abstract void buyTrade(int tradeIndex);

    @Shadow protected abstract void updateTradeLabels();

    @Shadow protected abstract boolean canBuyRandomTrade();

    @Shadow protected abstract void buyRandomTrade();

    @Shadow @Final private LabelElement<?> labelRandomTrade;

    @Shadow @Final private LabelElement<?>[] labelShopTrades;

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

        BlackMarketTweaks.LOGGER.info("--------- Available Trades ---------");
        ClientShardTradeData.getAvailableTrades().forEach((i, t) -> {
            BlackMarketTweaks.LOGGER.info("{} : {}, {}", i, t.getA(), t.getB());
        });
        BlackMarketTweaks.LOGGER.info("------------------------------------");

        ((AbstractElementContainerScreenAccessor) this).getElementStore().removeAllElements();

        this.screenParticleLeft = (new ScreenParticle()).angleRange(150.0F, 210.0F).quantityRange(1, 2).delayRange(0, 10).lifespanRange(10, 50).sizeRange(1, 4).speedRange(0.05F, 0.45F).spawnedPosition(((AbstractContainerScreenAccessor)this).getLeftPos() + 76, ((AbstractContainerScreenAccessor)this).getTopPos() + 43).spawnedWidthHeight(0, 28);
        this.screenParticleRight = (new ScreenParticle()).angleRange(-30.0F, 30.0F).quantityRange(1, 2).delayRange(0, 10).lifespanRange(10, 50).sizeRange(1, 4).speedRange(0.05F, 0.45F).spawnedPosition(((AbstractContainerScreenAccessor)this).getLeftPos() + 77 + 90, ((AbstractContainerScreenAccessor)this).getLeftPos() + 43).spawnedWidthHeight(0, 28);

        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((NineSliceElement)(new NineSliceElement(((AbstractElementContainerScreenAccessor) this).getGuiSpacial(), ScreenTextures.DEFAULT_WINDOW_BACKGROUND)).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        }));
        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((SlotsElement)(new SlotsElement((ShardTradeScreen) (Object) this)).layout((screen, gui, parent, world) -> {
            world.positionXY(gui);
        }));
        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((LabelElement)(new LabelElement(Spatials.positionXY(8, 7), (new TextComponent("Black Market")).withStyle(Style.EMPTY.withColor(-12632257)), LabelTextStyle.defaultStyle())).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        }));
        MutableComponent inventoryName = inventory.getDisplayName().copy();
        inventoryName.withStyle(Style.EMPTY.withColor(-12632257));
        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((LabelElement)(new LabelElement(Spatials.positionXY(8, 100), inventoryName, LabelTextStyle.defaultStyle())).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        }));
        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((TextureAtlasElement)(new TextureAtlasElement(Spatials.positionXY(13, 33), ScreenTextures.SOUL_SHARD_TRADE_ORNAMENT)).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        }));
        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((FakeItemSlotElement)(new FakeItemSlotElement(Spatials.positionXY(29, 49), () -> {
            return new ItemStack(ModItems.UNKNOWN_ITEM);
        }, () -> {
            return !this.canBuyRandomTrade();
        })).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        })).whenClicked(this::buyRandomTrade).tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
            tooltipRenderer.renderTooltip(poseStack, new ItemStack(ModItems.UNKNOWN_ITEM), mouseX, mouseY, TooltipDirection.RIGHT);
            return true;
        });
        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((ItemStackDisplayElement)(new ItemStackDisplayElement(Spatials.positionXY(29, 69), new ItemStack(ModItems.SOUL_SHARD))).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        }));
        try {
            labelRandomTradeReflection.set(this, (LabelElement)((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((LabelElement)(new LabelElement(Spatials.positionXYZ(37, 79, 200), TextComponent.EMPTY, LabelTextStyle.border8().center())).layout((screen, gui, parent, world) -> {
                world.translateXYZ(gui);})));
        } catch (IllegalAccessException e) {
            BlackMarketTweaks.LOGGER.error("Exception thrown while tweaking black market: ", e);
        }
        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((TextureAtlasElement)(new TextureAtlasElement(Spatials.positionXY(74, 6), ScreenTextures.BLACK_MARKET_ORNAMENT)).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        }));
        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((TextureAtlasElement)(new TextureAtlasElement(Spatials.positionXY(72, 37), ScreenTextures.OMEGA_BLACK_MARKET_ORNAMENT)).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        }));

        for(int i = 0; i < 2; ++i) {
            if (i == 1) {
                i = 2;
            }

            int tradeIndex = i;
            int yOffsetTrade = 10 + i * 33;
            ((ButtonElement)((AbstractElementContainerScreenAccessor) this).getElementStore().addElement(((ButtonElement)(new ButtonElement(Spatials.positionXY(78, yOffsetTrade), ScreenTextures.BUTTON_TRADE_WIDE_TEXTURES, () -> {
            })).layout((screen, gui, parent, world) -> {
                world.translateXY(gui);
            })).setDisabled(() -> {
                return !this.canBuyTrade(tradeIndex);
            }))).setEnabled(false);
            int yOffset = 14 + i * 33;
            ((FakeItemSlotElement)((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((FakeItemSlotElement)(new FakeItemSlotElement(Spatials.positionXY(141, yOffset), () -> {
                Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
                return trade == null ? ItemStack.EMPTY : trade.getA().copy();
            }, () -> {
                return !this.canBuyTrade(tradeIndex);
            })).setLabelStackCount().layout((screen, gui, parent, world) -> {
                world.translateXY(gui);
            }))).whenClicked(() -> {
                this.buyTrade(tradeIndex);
            }).tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
                if (trade != null && !trade.getA().isEmpty()) {
                    tooltipRenderer.renderTooltip(poseStack, trade.getA(), mouseX, mouseY, TooltipDirection.RIGHT);
                }

                return true;
            });
            ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((ItemStackDisplayElement)(new ItemStackDisplayElement(Spatials.positionXY(89, yOffset), new ItemStack(ModItems.SOUL_SHARD))).layout((screen, gui, parent, world) -> {
                world.translateXY(gui);
            }));
            this.labelShopTrades[i] = (LabelElement)((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((LabelElement)(new LabelElement(Spatials.positionXYZ(97, yOffset + 10, 200), TextComponent.EMPTY, LabelTextStyle.border8().center())).layout((screen, gui, parent, world) -> {
                world.translateXYZ(gui);
            }));
        }

        for(int i = 0; i < 2; ++i) {
            if (i == 1) {
                i = 2;
            }

            int tradeIndex = i;
            int yOffsetTrade = 10 + i * 33;
            ((ButtonElement)((AbstractElementContainerScreenAccessor) this).getElementStore().addElement(((ButtonElement)(new ButtonElement(Spatials.positionXY(108, yOffsetTrade), ScreenTextures.BUTTON_TRADE_WIDE_TEXTURES, () -> {
            })).layout((screen, gui, parent, world) -> {
                world.translateXY(gui);
            })).setDisabled(() -> {
                return !this.canBuyTrade(tradeIndex);
            }))).setEnabled(false);
            int yOffset = 14 + i * 33;
            ((FakeItemSlotElement)((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((FakeItemSlotElement)(new FakeItemSlotElement(Spatials.positionXY(171, yOffset), () -> {
                Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
                return trade == null ? ItemStack.EMPTY : trade.getA().copy();
            }, () -> {
                return !this.canBuyTrade(tradeIndex);
            })).setLabelStackCount().layout((screen, gui, parent, world) -> {
                world.translateXY(gui);
            }))).whenClicked(() -> {
                this.buyTrade(tradeIndex);
            }).tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
                Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
                if (trade != null && !trade.getA().isEmpty()) {
                    tooltipRenderer.renderTooltip(poseStack, trade.getA(), mouseX, mouseY, TooltipDirection.RIGHT);
                }

                return true;
            });
            ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((ItemStackDisplayElement)(new ItemStackDisplayElement(Spatials.positionXY(119, yOffset), new ItemStack(ModItems.SOUL_SHARD))).layout((screen, gui, parent, world) -> {
                world.translateXY(gui);
            }));
            this.labelShopTrades[i] = (LabelElement)((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((LabelElement)(new LabelElement(Spatials.positionXYZ(127, yOffset + 10, 200), TextComponent.EMPTY, LabelTextStyle.border8().center())).layout((screen, gui, parent, world) -> {
                world.translateXYZ(gui);
            }));
        }

        int tradeIndex = 1;
        int yOffsetTrade = 43;
        ((ButtonElement)((AbstractElementContainerScreenAccessor) this).getElementStore().addElement(this.omegaButton = ((ButtonElement)(new ButtonElement(Spatials.positionXY(77, yOffsetTrade - 1), ScreenTextures.OMEGA_BUTTON_TRADE_WIDE_TEXTURES, () -> {
        })).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        })).setDisabled(() -> {
            return !this.canBuyTrade(tradeIndex);
        }))).setEnabled(false);
        int yOffset = 47;
        ((FakeItemSlotElement)((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((FakeItemSlotElement)(new FakeItemSlotElement(Spatials.positionXY(141, yOffset), () -> {
            Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
            return trade == null ? ItemStack.EMPTY : trade.getA().copy();
        }, () -> {
            return !this.canBuyTrade(tradeIndex);
        })).setLabelStackCount().layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        }))).whenClicked(() -> {
            this.buyTrade(tradeIndex);
            this.screenParticleLeft.pop(4.0F, 20.0F);
            this.screenParticleRight.pop(4.0F, 20.0F);
        }).tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
            Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
            if (trade != null && !((ItemStack)trade.getA()).isEmpty()) {
                tooltipRenderer.renderTooltip(poseStack, (ItemStack)trade.getA(), mouseX, mouseY, TooltipDirection.RIGHT);
            }

            return true;
        });
        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((ItemStackDisplayElement)(new ItemStackDisplayElement(Spatials.positionXY(89, yOffset), new ItemStack(ModItems.SOUL_SHARD))).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        }));
        this.labelShopTrades[1] = (LabelElement)((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((LabelElement)(new LabelElement(Spatials.positionXYZ(97, yOffset + 10, 200), TextComponent.EMPTY, LabelTextStyle.border8().center())).layout((screen, gui, parent, world) -> {
            world.translateXYZ(gui);
        }));

        ((ButtonElement)((AbstractElementContainerScreenAccessor) this).getElementStore().addElement(this.omegaButton = ((ButtonElement)(new ButtonElement(Spatials.positionXY(107, yOffsetTrade - 1), ScreenTextures.OMEGA_BUTTON_TRADE_WIDE_TEXTURES, () -> {
        })).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        })).setDisabled(() -> {
            return !this.canBuyTrade(tradeIndex);
        }))).setEnabled(false);
        ((FakeItemSlotElement)((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((FakeItemSlotElement)(new FakeItemSlotElement(Spatials.positionXY(171, yOffset), () -> {
            Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
            return trade == null ? ItemStack.EMPTY : trade.getA().copy();
        }, () -> {
            return !this.canBuyTrade(tradeIndex);
        })).setLabelStackCount().layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        }))).whenClicked(() -> {
            this.buyTrade(tradeIndex);
            this.screenParticleLeft.pop(4.0F, 20.0F);
            this.screenParticleRight.pop(4.0F, 20.0F);
        }).tooltip((tooltipRenderer, poseStack, mouseX, mouseY, tooltipFlag) -> {
            Tuple<ItemStack, Integer> trade = ClientShardTradeData.getTradeInfo(tradeIndex);
            if (trade != null && !((ItemStack)trade.getA()).isEmpty()) {
                tooltipRenderer.renderTooltip(poseStack, (ItemStack)trade.getA(), mouseX, mouseY, TooltipDirection.RIGHT);
            }

            return true;
        });
        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((ItemStackDisplayElement)(new ItemStackDisplayElement(Spatials.positionXY(119, yOffset), new ItemStack(ModItems.SOUL_SHARD))).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        }));
        this.labelShopTrades[1] = (LabelElement)((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((LabelElement)(new LabelElement(Spatials.positionXYZ(127, yOffset + 10, 200), TextComponent.EMPTY, LabelTextStyle.border8().center())).layout((screen, gui, parent, world) -> {
            world.translateXYZ(gui);
        }));

        LocalDateTime endTime = ClientShardTradeData.getNextReset();
        LocalDateTime nowTime = LocalDateTime.now(ZoneId.of("UTC")).withNano(0);
        LocalTime diff = LocalTime.MIN.plusSeconds(ChronoUnit.SECONDS.between(nowTime, endTime));
        Component component = new TextComponent(diff.format(DateTimeFormatter.ISO_LOCAL_TIME));
        IMutableSpatial var10003 = Spatials.positionXYZ(((AbstractElementContainerScreenAccessor) this).getGuiSpacial().width() / 2 - ((Font) TextBorder.DEFAULT_FONT.get()).width(component) / 2 - 11, -10, 200);
        int var10004 = ((Font)TextBorder.DEFAULT_FONT.get()).width(component);
        Objects.requireNonNull((Font)TextBorder.DEFAULT_FONT.get());
        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((new CountDownElement(var10003, Spatials.size(var10004, 9), () -> {
            return component;
        }, LabelTextStyle.shadow())).layout((screen, gui, parent, world) -> {
            world.translateXY(gui.x(), gui.y());
        }));
        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((TextureAtlasElement)((TextureAtlasElement<?>)(new TextureAtlasElement(Spatials.positionXY(((AbstractElementContainerScreenAccessor) this).getGuiSpacial().width() / 2 - ScreenTextures.TAB_COUNTDOWN_BACKGROUND.width() / 2 - 10, -ScreenTextures.TAB_COUNTDOWN_BACKGROUND.height()), ScreenTextures.TAB_COUNTDOWN_BACKGROUND)).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        })).tooltip(Tooltips.multi(() -> List.of(new TextComponent("Shop resets in")))));
        ((AbstractElementContainerScreenAccessor) this).getElementStore().addElement((ButtonElement)((ButtonElement)(new ButtonElement(Spatials.positionXY(((AbstractElementContainerScreenAccessor) this).getGuiSpacial().width() / 2 - ScreenTextures.TAB_COUNTDOWN_BACKGROUND.width() / 2 + 50, -ScreenTextures.TAB_COUNTDOWN_BACKGROUND.height()), ScreenTextures.BUTTON_RESET_TRADES_TEXTURES, () -> {
            ModNetwork.CHANNEL.sendToServer(ServerboundResetBlackMarketTradesMessage.INSTANCE);
            ((ShardTradeContainer) ((AbstractContainerScreenAccessor) this).invokeGetMenu()).getPlayer().level.playSound(((ShardTradeContainer)((AbstractContainerScreenAccessor) this).invokeGetMenu()).getPlayer(), ((ShardTradeContainer)((AbstractContainerScreenAccessor) this).invokeGetMenu()).getPlayer().getX(), ((ShardTradeContainer)((AbstractContainerScreenAccessor) this).invokeGetMenu()).getPlayer().getY(), ((ShardTradeContainer)((AbstractContainerScreenAccessor) this).invokeGetMenu()).getPlayer().getZ(), ModSounds.SKILL_TREE_LEARN_SFX, SoundSource.BLOCKS, 0.75F, 1.0F);
        })).layout((screen, gui, parent, world) -> {
            world.translateXY(gui);
        })).setDisabled(() -> {
            Iterator var0 = ClientExpertiseData.getLearnedTalentNodes().iterator();

            LearnableSkill patt13782$temp;
            do {
                if (!var0.hasNext()) {
                    return true;
                }

                TieredSkill learnedTalentNode = (TieredSkill)var0.next();
                patt13782$temp = learnedTalentNode.getChild();
            } while(!(patt13782$temp instanceof BlackMarketExpertise));

            BlackMarketExpertise blackMarketExpertise = (BlackMarketExpertise)patt13782$temp;
            return blackMarketExpertise.getNumberOfRolls() <= ClientShardTradeData.getRerollsUsed();
        }).tooltip(Tooltips.multi(() -> {
            int numOfRollsLeft = 0;
            boolean hasExpertise = false;
            Iterator var2 = ClientExpertiseData.getLearnedTalentNodes().iterator();

            while(var2.hasNext()) {
                TieredSkill learnedTalentNode = (TieredSkill)var2.next();
                LearnableSkill patt14341$temp = learnedTalentNode.getChild();
                if (patt14341$temp instanceof BlackMarketExpertise blackMarketExpertise) {
                    numOfRollsLeft = blackMarketExpertise.getNumberOfRolls() - ClientShardTradeData.getRerollsUsed();
                    hasExpertise = true;
                }
            }

            if (hasExpertise) {
                return List.of(new TextComponent("Rolls Left: " + numOfRollsLeft));
            } else {
                return List.of(new TextComponent("Unlock Marketer Expertise to Re-roll"));
            }
        })));
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

        for(int i = 0; i < 3; ++i) {
            Tuple<ItemStack, Integer> tradeInfo = ClientShardTradeData.getTradeInfo(i);
            if (tradeInfo == null) {
                this.labelShopTrades[i].set(TextComponent.EMPTY);
            } else {
                int tradeCost = (Integer)tradeInfo.getB();
                int tradeCostColor = playerShards >= tradeCost ? 16777215 : 8257536;
                Component tradeCostComponent = (new TextComponent(String.valueOf(tradeCost))).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(tradeCostColor)));
                this.labelShopTrades[i].set(tradeCostComponent);
            }
        }
        ci.cancel();
    }
}

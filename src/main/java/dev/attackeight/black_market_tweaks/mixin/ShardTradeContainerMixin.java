package dev.attackeight.black_market_tweaks.mixin;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import dev.attackeight.black_market_tweaks.extension.BlackMarketInventory;
import dev.attackeight.black_market_tweaks.init.ModConfig;
import iskallia.vault.container.inventory.ShardTradeContainer;
import iskallia.vault.container.oversized.OverSizedContainerSynchronizer;
import iskallia.vault.container.oversized.OverSizedTabSlot;
import iskallia.vault.container.spi.AbstractElementContainer;
import iskallia.vault.init.ModContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static iskallia.vault.container.oversized.OverSizedSlotContainer.canAddItemToSlot;

@Mixin(value = ShardTradeContainer.class, remap = false)
public abstract class ShardTradeContainerMixin extends AbstractElementContainer {
    @Unique private final Set<Slot> dragSlots = new HashSet<>();
    @Unique private int dragMode = -1;
    @Unique private int dragEvent;

    private ShardTradeContainerMixin(int id, Inventory inventory) {
        super(ModContainers.SHARD_TRADE_CONTAINER, id, inventory.player);
    }

    @Redirect(method = "initSlots", at = @At(value = "NEW", target = "(Lnet/minecraft/world/Container;III)Lnet/minecraft/world/inventory/Slot;"))
    private Slot moveSlotsDown(Container inv, int id, int x, int y) {
        return new Slot(inv, id, x, y + 10);
    }

    @Inject(method = "initSlots", at = @At("TAIL"))
    private void addReRollSlot(Inventory playerInventory, CallbackInfo ci) {
        BlockPos lookingPos = BlackMarketTweaks.getLastClickedPos(playerInventory.player.getUUID());
        BlockEntity be = playerInventory.player.level.getBlockEntity(lookingPos);

        if (be instanceof BlackMarketInventory inventory) {
            this.addSlot(new OverSizedTabSlot(inventory.bmt$get(), 0, -21, 85)
                    .setFilter(stack -> ModConfig.ITEM.get().equals(String.valueOf(stack.getItem().getRegistryName()))));
        }
    }

    // Copied from OverSizedSlotContainer, Ideally ShardTradeContainer should extend it, but we cannot do that with a mixin

    @Override
    public boolean moveItemStackTo(@NotNull ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return this.moveOverSizedItemStackTo(stack, (Slot)null, startIndex, endIndex, reverseDirection);
    }

    @Inject(method = "quickMoveStack", at = @At(value = "HEAD"), cancellable = true, remap = true)
    private void quickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        ItemStack slotStack = slot.getItem();
        ItemStack itemStack = slotStack.copy();
        if (index >= 0 && index < 36 && this.moveOverSizedItemStackTo(slotStack, slot, 36, this.slots.size(), false)) {
            cir.setReturnValue(itemStack);
            return;
        }

        if (index >= 0 && index < 27) {
            if (!this.moveOverSizedItemStackTo(slotStack, slot, 27, 36, false)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
        } else if (index >= 27 && index < 36) {
            if (!this.moveOverSizedItemStackTo(slotStack, slot, 0, 27, false)) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
        } else if (!this.moveOverSizedItemStackTo(slotStack, slot, 0, 36, false)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        if (slotStack.getCount() == 0) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount() == itemStack.getCount()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        slot.onTake(player, slotStack);
        cir.setReturnValue(itemStack);
    }

    @Unique
    protected boolean moveOverSizedItemStackTo(ItemStack sourceStack, @Nullable Slot sourceSlot, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }

        Slot slot1;
        ItemStack slotStack;
        for(; !sourceStack.isEmpty(); i += reverseDirection ? -1 : 1) {
            if (reverseDirection) {
                if (i < startIndex) {
                    break;
                }
            } else if (i >= endIndex) {
                break;
            }

            slot1 = this.slots.get(i);
            slotStack = slot1.getItem();
            if (!slotStack.isEmpty() && slotStack.getItem() == sourceStack.getItem() && ItemStack.tagMatches(sourceStack, slotStack) && slot1.mayPlace(sourceStack)) {
                int j = slotStack.getCount() + sourceStack.getCount();
                int maxSize = slot1.getMaxStackSize(slotStack);
                if (j <= maxSize) {
                    sourceStack.setCount(0);
                    if (sourceSlot != null) {
                        sourceSlot.set(sourceStack);
                    }

                    slotStack.setCount(j);
                    slot1.set(slotStack);
                    slot1.setChanged();
                    flag = true;
                } else if (slotStack.getCount() < maxSize) {
                    sourceStack.shrink(maxSize - slotStack.getCount());
                    if (sourceSlot != null) {
                        sourceSlot.set(sourceStack);
                    }

                    slotStack.setCount(maxSize);
                    slot1.set(slotStack);
                    slot1.setChanged();
                    flag = true;
                }
            }
        }

        if (!sourceStack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while(true) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                slot1 = this.slots.get(i);
                slotStack = slot1.getItem();
                if (slotStack.isEmpty() && slot1.mayPlace(sourceStack)) {
                    if (sourceStack.getCount() > slot1.getMaxStackSize(sourceStack)) {
                        slot1.set(sourceStack.split(slot1.getMaxStackSize(sourceStack)));
                    } else {
                        slot1.set(sourceStack.split(sourceStack.getCount()));
                    }

                    if (sourceSlot != null) {
                        sourceSlot.set(sourceStack);
                    }

                    slot1.setChanged();
                    flag = true;
                    break;
                }

                i += reverseDirection ? -1 : 1;
            }
        }

        return flag;
    }

    @Override
    public void clicked(int slotId, int dragType, @NotNull ClickType clickTypeIn, @NotNull Player player) {
        ItemStack slotStack;
        ItemStack mouseStack;
        int k3;
        int k1;
        if (clickTypeIn == ClickType.QUICK_CRAFT) {
            int j1 = this.dragEvent;
            this.dragEvent = getQuickcraftHeader(dragType);
            if ((j1 != 1 || this.dragEvent != 2) && j1 != this.dragEvent) {
                this.resetQuickCraft();
            } else if (this.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.dragEvent == 0) {
                this.dragMode = getQuickcraftType(dragType);
                if (isValidQuickcraftType(this.dragMode, player)) {
                    this.dragEvent = 1;
                    this.dragSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.dragEvent == 1) {
                Slot slot = this.slots.get(slotId);
                mouseStack = this.getCarried();
                if (slot != null && canAddItemToSlot(slot, mouseStack, true) && slot.mayPlace(mouseStack) && (this.dragMode == 2 || mouseStack.getCount() > this.dragSlots.size()) && this.canDragTo(slot)) {
                    this.dragSlots.add(slot);
                }
            } else if (this.dragEvent == 2) {
                if (!this.dragSlots.isEmpty()) {
                    slotStack = this.getCarried().copy();
                    k1 = this.getCarried().getCount();
                    Iterator var21 = this.dragSlots.iterator();

                    label314:
                    while(true) {
                        Slot dragSlot;
                        ItemStack mouseStack2;
                        do {
                            do {
                                do {
                                    do {
                                        if (!var21.hasNext()) {
                                            slotStack.setCount(k1);
                                            this.setCarried(slotStack);
                                            break label314;
                                        }

                                        dragSlot = (Slot)var21.next();
                                        mouseStack2 = this.getCarried();
                                    } while(dragSlot == null);
                                } while(!canAddItemToSlot(dragSlot, mouseStack2, true));
                            } while(!dragSlot.mayPlace(mouseStack2));
                        } while(this.dragMode != 2 && mouseStack2.getCount() < this.dragSlots.size());

                        if (this.canDragTo(dragSlot)) {
                            ItemStack itemstack14 = slotStack.copy();
                            int j3 = dragSlot.hasItem() ? dragSlot.getItem().getCount() : 0;
                            getQuickCraftSlotCount(this.dragSlots, this.dragMode, itemstack14, j3);
                            k3 = dragSlot.getMaxStackSize(itemstack14);
                            if (itemstack14.getCount() > k3) {
                                itemstack14.setCount(k3);
                            }

                            k1 -= itemstack14.getCount() - j3;
                            dragSlot.set(itemstack14);
                        }
                    }
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.dragEvent != 0) {
            this.resetQuickCraft();
        } else {
            Slot slot;
            int j2;
            int k;
            if (clickTypeIn != ClickType.PICKUP && clickTypeIn != ClickType.QUICK_MOVE || dragType != 0 && dragType != 1) {
                if (clickTypeIn != ClickType.SWAP || dragType < 0 || dragType >= 9) {
                    if (clickTypeIn == ClickType.CLONE && player.getAbilities().instabuild && this.getCarried().isEmpty() && slotId >= 0) {
                        slot = this.slots.get(slotId);
                        if (slot != null && slot.hasItem()) {
                            slotStack = slot.getItem().copy();
                            slotStack.setCount(slotStack.getMaxStackSize());
                            this.setCarried(slotStack);
                        }
                    } else if (clickTypeIn == ClickType.THROW && this.getCarried().isEmpty() && slotId >= 0) {
                        slot = this.slots.get(slotId);
                        if (slot != null && slot.hasItem() && slot.mayPickup(player)) {
                            int removeCount = Math.min(dragType == 0 ? 1 : slot.getItem().getCount(), slot.getItem().getMaxStackSize());
                            mouseStack = slot.remove(removeCount);
                            slot.onTake(player, mouseStack);
                            player.drop(mouseStack, true);
                        }
                    } else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
                        slot = this.slots.get(slotId);
                        slotStack = this.getCarried();
                        if (!slotStack.isEmpty() && (slot == null || !slot.hasItem() || !slot.mayPickup(player))) {
                            k1 = dragType == 0 ? 0 : this.slots.size() - 1;
                            j2 = dragType == 0 ? 1 : -1;

                            for(k = 0; k < 2; ++k) {
                                for(int l = k1; l >= 0 && l < this.slots.size() && slotStack.getCount() < slotStack.getMaxStackSize(); l += j2) {
                                    Slot slot1 = this.slots.get(l);
                                    if (slot1.hasItem() && canAddItemToSlot(slot1, slotStack, true) && slot1.mayPickup(player) && this.canTakeItemForPickAll(slotStack, slot1)) {
                                        ItemStack itemstack2 = slot1.getItem();
                                        if (k != 0 || itemstack2.getCount() < slot1.getMaxStackSize(itemstack2)) {
                                            k3 = Math.min(slotStack.getMaxStackSize() - slotStack.getCount(), itemstack2.getCount());
                                            ItemStack itemstack3 = slot1.remove(k3);
                                            slotStack.grow(k3);
                                            if (itemstack3.isEmpty()) {
                                                slot1.set(ItemStack.EMPTY);
                                            }

                                            slot1.onTake(player, itemstack3);
                                        }
                                    }
                                }
                            }
                        }

                        this.broadcastChanges();
                    }
                }
            } else if (slotId == -999) {
                if (!this.getCarried().isEmpty()) {
                    if (dragType == 0) {
                        player.drop(this.getCarried(), true);
                        this.setCarried(ItemStack.EMPTY);
                    }

                    if (dragType == 1) {
                        player.drop(this.getCarried().split(1), true);
                    }
                }
            } else if (clickTypeIn == ClickType.QUICK_MOVE) {
                if (slotId < 0) {
                    return;
                }

                slot = this.slots.get(slotId);
                if (slot == null || !slot.mayPickup(player)) {
                    return;
                }

                for(slotStack = this.quickMoveStack(player, slotId); !slotStack.isEmpty() && ItemStack.isSame(slot.getItem(), slotStack); slotStack = this.quickMoveStack(player, slotId)) {
                }
            } else {
                if (slotId < 0) {
                    return;
                }

                slot = this.slots.get(slotId);
                if (slot != null) {
                    slotStack = slot.getItem();
                    mouseStack = this.getCarried();
                    if (slotStack.isEmpty()) {
                        if (!mouseStack.isEmpty() && slot.mayPlace(mouseStack)) {
                            j2 = dragType == 0 ? mouseStack.getCount() : 1;
                            if (j2 > slot.getMaxStackSize(mouseStack)) {
                                j2 = slot.getMaxStackSize(mouseStack);
                            }

                            slot.set(mouseStack.split(j2));
                        }
                    } else if (slot.mayPickup(player)) {
                        if (mouseStack.isEmpty()) {
                            if (slotStack.isEmpty()) {
                                slot.set(ItemStack.EMPTY);
                                this.setCarried(ItemStack.EMPTY);
                            } else {
                                j2 = Math.min(slotStack.getCount(), slotStack.getMaxStackSize());
                                k = dragType == 0 ? j2 : (j2 + 1) / 2;
                                this.setCarried(slot.remove(k));
                                if (slotStack.isEmpty()) {
                                    slot.set(ItemStack.EMPTY);
                                }

                                slot.onTake(player, this.getCarried());
                            }
                        } else if (slot.mayPlace(mouseStack)) {
                            if (slotStack.getItem() == mouseStack.getItem() && ItemStack.tagMatches(slotStack, mouseStack)) {
                                j2 = dragType == 0 ? mouseStack.getCount() : 1;
                                if (j2 > slot.getMaxStackSize(mouseStack) - slotStack.getCount()) {
                                    j2 = slot.getMaxStackSize(mouseStack) - slotStack.getCount();
                                }

                                mouseStack.shrink(j2);
                                slotStack.grow(j2);
                                slot.set(slotStack);
                            } else if (mouseStack.getCount() <= slot.getMaxStackSize(mouseStack) && slotStack.getCount() <= slotStack.getMaxStackSize()) {
                                slot.set(mouseStack);
                                this.setCarried(slotStack);
                            }
                        } else if (slotStack.getItem() == mouseStack.getItem() && mouseStack.getMaxStackSize() > 1 && ItemStack.tagMatches(slotStack, mouseStack) && !slotStack.isEmpty()) {
                            j2 = slotStack.getCount();
                            if (j2 + mouseStack.getCount() <= mouseStack.getMaxStackSize()) {
                                mouseStack.grow(j2);
                                slotStack = slot.remove(j2);
                                if (slotStack.isEmpty()) {
                                    slot.set(ItemStack.EMPTY);
                                }

                                slot.onTake(player, this.getCarried());
                            }
                        }
                    }

                    slot.setChanged();
                }
            }
        }
    }

    @Override
    protected void resetQuickCraft() {
        this.dragEvent = 0;
        this.dragSlots.clear();
    }

    @Override
    public void setSynchronizer(@NotNull ContainerSynchronizer sync) {
        if (this.getPlayer() instanceof ServerPlayer sPlayer) {
            super.setSynchronizer(new OverSizedContainerSynchronizer(sync, sPlayer));
        } else {
            super.setSynchronizer(sync);
        }
    }
}

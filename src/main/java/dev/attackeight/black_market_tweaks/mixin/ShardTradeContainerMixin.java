package dev.attackeight.black_market_tweaks.mixin;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import dev.attackeight.black_market_tweaks.ModConfig;
import iskallia.vault.block.entity.BlackMarketTileEntity;
import iskallia.vault.container.inventory.ShardTradeContainer;
import iskallia.vault.container.oversized.OverSizedTabSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = ShardTradeContainer.class, remap = false)
public abstract class ShardTradeContainerMixin {

    @Redirect(method = "initSlots", at = @At(value = "NEW", target = "(Lnet/minecraft/world/Container;III)Lnet/minecraft/world/inventory/Slot;"))
    private Slot moveSlotsDown(Container inv, int id, int x, int y) {
        return new Slot(inv, id, x, y + 10);
    }

    @Inject(method = "initSlots", at = @At("TAIL"))
    private void addRerollSlot(Inventory playerInventory, CallbackInfo ci) {

        BlockPos lookingPos = BlackMarketTweaks.getLastClickedPos(playerInventory.player.getUUID());
        BlockEntity be = playerInventory.player.level.getBlockEntity(lookingPos);

        if (be instanceof BlackMarketTileEntity blackMarketTile) {
            try {
                Object o = blackMarketTile.getClass().getDeclaredField("inventory").get(blackMarketTile);
                if (o instanceof Container invContainer) {
                    ((ShardTradeContainer) (Object) this).addSlot((new OverSizedTabSlot(invContainer, 0, -21, 85)).setFilter((stack) ->
                        stack.getItem().getRegistryName().equals(ResourceLocation.tryParse(ModConfig.ITEM.get()))));
                }
            } catch (Exception e) {
                BlackMarketTweaks.LOGGER.error(e.toString());
            }

        }
    }

    @Inject(method = "quickMoveStack", at = @At(value = "HEAD"), cancellable = true)
    private void quickMoveMoreStacks(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)((AbstractContainerMenu) (Object)this).slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();
            if (index >= 0 && index < 36 && this.moveOverSizedItemStackTo(slotStack, slot, 36, ((AbstractContainerMenu) (Object)this).slots.size(), false)) {
                cir.setReturnValue(itemstack);
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

            if (slotStack.getCount() == itemstack.getCount()) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }

            slot.onTake(player, slotStack);
        }

        cir.setReturnValue(itemstack);
        cir.cancel();
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

            slot1 = (Slot)((AbstractContainerMenu) (Object)this).slots.get(i);
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

                slot1 = (Slot)((AbstractContainerMenu) (Object)this).slots.get(i);
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
}

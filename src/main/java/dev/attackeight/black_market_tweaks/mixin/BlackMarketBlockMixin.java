package dev.attackeight.black_market_tweaks.mixin;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import iskallia.vault.block.BlackMarketBlock;
import iskallia.vault.block.base.InventoryRetainerBlock;
import iskallia.vault.block.entity.base.InventoryRetainerTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = BlackMarketBlock.class, remap = false)
public abstract class BlackMarketBlockMixin extends HorizontalDirectionalBlock implements EntityBlock, InventoryRetainerBlock {

    protected BlackMarketBlockMixin(Properties p_54120_) {
        super(p_54120_);
    }

    @Inject(method = "use", at = @At("HEAD"), remap = true)
    private void saveUsing(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        BlackMarketTweaks.setLastClickedPos(player.getUUID(), pos);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        this.onInventoryBlockDestroy(level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        this.addInventoryTooltip(stack, tooltip, (is, tag) -> InventoryRetainerTileEntity.displayContentsOverSized(1, stacks -> {
            stacks.addAll(OverSizedInventory.loadContents("inventory", (CompoundTag) tag));
        }));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.onInventoryBlockPlace(level, pos, stack);
    }
}

package dev.attackeight.black_market_tweaks.mixin;

import dev.attackeight.black_market_tweaks.BlackMarketTweaks;
import iskallia.vault.block.BlackMarketBlock;
import iskallia.vault.block.entity.BlackMarketTileEntity;
import iskallia.vault.container.oversized.OverSizedInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlackMarketBlock.class, remap = false)
public class BlackMarketBlockMixin  {

    @Inject(method = "use", at = @At("HEAD"))
    private void saveUsing(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        BlackMarketTweaks.setLastClickedPos(player.getUUID(), pos);
    }

    @Unique
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof BlackMarketTileEntity entity) {
                try {
                    OverSizedInventory c = (OverSizedInventory) entity.getClass().getDeclaredField("inventory").get(entity);
                    c.getOverSizedContents().forEach((overSizedStack) ->
                        overSizedStack.splitByStackSize().forEach((splitStack) ->
                            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), splitStack)
                        )
                    );
                    c.clearContent();
                } catch (Exception e) {
                    BlackMarketTweaks.LOGGER.error(e.toString());
                }
                level.updateNeighbourForOutputSignal(pos, (Block) (Object) this);
            }
        }
        if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
            level.removeBlockEntity(pos);
        }

    }

}

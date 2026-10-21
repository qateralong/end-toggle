package com.qateralong.endtoggle.mixin;

import com.qateralong.endtoggle.EndToggle;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void endtoggle$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (EndToggle.blocks(level, entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "getPortalDestination", at = @At("HEAD"), cancellable = true)
    private void endtoggle$destination(ServerLevel level, Entity entity, BlockPos pos, CallbackInfoReturnable<DimensionTransition> cir) {
        if (EndToggle.blocks(level, entity)) {
            cir.setReturnValue(null);
        }
    }
}

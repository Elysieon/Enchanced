package net.collective.enchanced.common.mixin.anvil;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilBlock.class)
public class AnvilBlockMixin {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void enchanced$anvil(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (player.getEntityWorld().isClient()) {
            player.sendMessage(Text.literal(""), false);
            player.sendMessage(Text.literal("Anvils are temporarily disabled, as they are being reworked.").formatted(Formatting.RED), false);
            player.sendMessage(Text.literal("Renaming items have been moved to nametags.").formatted(Formatting.GRAY).formatted(Formatting.ITALIC), false);
            player.sendMessage(Text.literal(""), false);

        }
        cir.setReturnValue(ActionResult.FAIL);
    }
}

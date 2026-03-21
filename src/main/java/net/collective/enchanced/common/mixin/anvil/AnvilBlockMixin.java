package net.collective.enchanced.common.mixin.anvil;

import moriyashiine.enchancement.common.ModConfig;
import moriyashiine.enchancement.common.util.config.OverhaulMode;
import net.collective.enchanced.common.gui.AnvilScreenHandler;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilBlock.class)
public class AnvilBlockMixin {
    // @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    // private void enchanced$anvil(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
    //     if (player.getEntityWorld().isClient()) {
    //         player.sendMessage(Text.literal(""), false);
    //         player.sendMessage(Text.literal("Anvils are temporarily disabled, as they are being reworked.").formatted(Formatting.RED), false);
    //         player.sendMessage(Text.literal("Renaming items have been moved to nametags.").formatted(Formatting.GRAY).formatted(Formatting.ITALIC), false);
    //         player.sendMessage(Text.literal(""), false);
    //
    //     }
    //     cir.setReturnValue(ActionResult.FAIL);
    // }

    @Inject(
            method = {"createScreenHandlerFactory"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/SimpleNamedScreenHandlerFactory;<init>(Lnet/minecraft/screen/ScreenHandlerFactory;Lnet/minecraft/text/Text;)V"
            )},
            cancellable = true
    )
    private void enchancement$overhaulEnchanting(BlockState state, World world, BlockPos pos, CallbackInfoReturnable<NamedScreenHandlerFactory> cir) {
        if (ModConfig.overhaulEnchanting != OverhaulMode.DISABLED) {
            cir.setReturnValue(new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> new AnvilScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos), world), Text.literal("Skibidi Station")));
        }
    }
}

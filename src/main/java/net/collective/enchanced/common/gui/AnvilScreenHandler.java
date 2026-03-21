package net.collective.enchanced.common.gui;

import net.collective.enchanced.common.index.ModScreenHandlerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.world.World;

public class AnvilScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;

    public AnvilScreenHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext screenHandlerContext, World world) {
        super(ModScreenHandlerTypes.ANVIL, syncId);

        PlayerEntity player = inventory.player;
        this.context = screenHandlerContext;
    }

    public AnvilScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY, playerInventory.player.getEntityWorld());
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}

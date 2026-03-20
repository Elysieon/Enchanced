package net.collective.enchanced.common.payload;

import net.collective.enchanced.Enchanced;
import net.collective.enchanced.common.entity.ThrownSpearEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ThrownSpearSyncS2CPayload(int id, ItemStack itemStack) implements CustomPayload {
    private static final Identifier IDENTIFIER = Enchanced.id("thrown_spear_sync_s2c");
    public static final Id<ThrownSpearSyncS2CPayload> ID = new Id<>(IDENTIFIER);
    public static final PacketCodec<RegistryByteBuf, ThrownSpearSyncS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ThrownSpearSyncS2CPayload::id,
            ItemStack.PACKET_CODEC, ThrownSpearSyncS2CPayload::itemStack,
            ThrownSpearSyncS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static ThrownSpearSyncS2CPayload of(ThrownSpearEntity entity) {
        return new ThrownSpearSyncS2CPayload(entity.getId(), entity.getRenderedItemStack());
    }
}

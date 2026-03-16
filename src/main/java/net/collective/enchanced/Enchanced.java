package net.collective.enchanced;

import moriyashiine.enchancement.common.Enchancement;
import moriyashiine.enchancement.common.ModConfig;
import moriyashiine.enchancement.common.init.ModEnchantments;
import net.collective.enchanced.common.index.ModLootConditionTypes;
import net.collective.enchanced.common.index.ModSoundEvent;
import net.collective.enchanced.common.index.EnchancedEnchantments;
import net.collective.enchanced.common.payload.LungeC2SPayload;
import net.collectively.geode.Geode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Enchanced implements ModInitializer {
    public static final String MOD_ID = "enchanced";
    public static final Geode geode = Geode.create(MOD_ID);

    public static Identifier id(String identifier) {
        return Identifier.of(MOD_ID, identifier);
    }

    @Override
    public void onInitialize() {
        Enchancement.LOGGER.info("Loading Enchancement");

        // Init
        EnchancedEnchantments.init();
        ModSoundEvent.init();
        ModLootConditionTypes.init();

        // Remove Enchantments!
        if (!ModConfig.disallowedEnchantments.contains(Enchantments.FIRE_ASPECT.getValue().toString())) ModConfig.disallowedEnchantments.add(Enchantments.FIRE_ASPECT.getValue().toString());
        if (!ModConfig.disallowedEnchantments.contains(ModEnchantments.STICKY.getValue().toString())) ModConfig.disallowedEnchantments.add(ModEnchantments.STICKY.getValue().toString());

        // Register Packet Stuff
        PayloadTypeRegistry.playC2S().register(LungeC2SPayload.ID, LungeC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(LungeC2SPayload.ID, (payload, context) -> {
            context.player().sendMessage(Text.literal("Lunged UwU"), false);
            var item = context.player().getActiveOrMainHandStack();
            if (item.isIn(ItemTags.SPEARS)) {
            }
        });
    }
}

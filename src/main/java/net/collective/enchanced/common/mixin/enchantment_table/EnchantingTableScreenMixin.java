package net.collective.enchanced.common.mixin.enchantment_table;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import moriyashiine.enchancement.client.gui.screen.ingame.EnchantingTableScreen;
import moriyashiine.enchancement.common.Enchancement;
import moriyashiine.enchancement.common.screenhandler.EnchantingTableScreenHandler;
import moriyashiine.enchancement.common.util.EnchancementUtil;
import moriyashiine.strawberrylib.api.module.SLibClientUtils;
import net.collective.enchanced.Enchanced;
import net.collective.enchanced.common.util.EnchantUtils;
import net.collective.enchanced.common.util.StringUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(EnchantingTableScreen.class)
public abstract class EnchantingTableScreenMixin {
    // region resources
    @Unique
    private static final Identifier UP_ARROW_TEXTURE = Enchancement.id("container/enchanting_table/up_arrow");
    @Unique
    private static final Identifier UP_ARROW_HIGHLIGHTED_TEXTURE = Enchancement.id("container/enchanting_table/up_arrow_highlighted");
    @Unique
    private static final Identifier DOWN_ARROW_TEXTURE = Enchancement.id("container/enchanting_table/down_arrow");
    @Unique
    private static final Identifier DOWN_ARROW_HIGHLIGHTED_TEXTURE = Enchancement.id("container/enchanting_table/down_arrow_highlighted");
    @Unique
    private static final Identifier CHECKMARK_TEXTURE = Enchancement.id("container/enchanting_table/checkmark");
    @Unique
    private static final Identifier CHECKMARK_HIGHLIGHTED_TEXTURE = Enchancement.id("container/enchanting_table/checkmark_highlighted");
    @Unique
    private static final Identifier LOCK_TEXTURE = Enchanced.id("container/enchanting_table/lock");
    @Unique
    private static final Identifier LOCKED_TAB_TEXTURE = Enchanced.id("container/enchanting_table/locked_tab");
    @Unique
    private static final Identifier ENTRY_LINE_1_TEXTURE = Enchanced.id("container/enchanting_table/entry_line_1");
    @Unique
    private static final Identifier ENTRY_LINE_2_TEXTURE = Enchanced.id("container/enchanting_table/entry_line_2");
    @Unique
    private static final Identifier ENTRY_LINE_3_TEXTURE = Enchanced.id("container/enchanting_table/entry_line_3");
    @Unique
    private static final Identifier ENTRY_LINE_4_TEXTURE = Enchanced.id("container/enchanting_table/entry_line_4");
    @Unique
    private static final Identifier[] ENTRY_LINE_TEXTURES = new Identifier[]{ENTRY_LINE_1_TEXTURE, ENTRY_LINE_2_TEXTURE, ENTRY_LINE_3_TEXTURE, ENTRY_LINE_4_TEXTURE};
    @Unique
    private static final StyleSpriteSource.Font GALACTIC_FONT = new StyleSpriteSource.Font(Identifier.ofVanilla("alt"));
    // endregion

    // region settings
    @Unique
    private static final int MAX_ENCHANTMENT_NAME_WIDTH = 60;
    @Unique
    private static final boolean DRAW_ENCHANTMENT_ENTRY_HOR_SEPARATOR = false;
    @Unique
    private static final boolean OBFUSCATE_LOCKED_ENCHANTMENT_DESCRIPTION = true;
    @Unique
    private static final int LOCK_ICON_WIDTH = 9;
    @Unique
    private static final int LOCK_ICON_HEIGHT = 12;
    // endregion

    // region shadow
    @Shadow
    private static boolean isInUpButtonBounds(int posX, int posY, int mouseX, int mouseY) {
        return false;
    }

    @Shadow
    private static boolean isInDownButtonBounds(int posX, int posY, int mouseX, int mouseY) {
        return false;
    }

    @Shadow
    private static boolean isInEnchantButtonBounds(int posX, int posY, int mouseX, int mouseY) {
        return false;
    }

    @Shadow
    private static boolean isInBounds(int posX, int posY, int mouseX, int mouseY, int startX, int endX, int startY, int endY) {
        return false;
    }

    @Shadow
    private List<Text> infoTexts;

    @Shadow
    private int materialIndex;

    @Shadow
    private int highlightedEnchantmentIndex;

    @Shadow
    private float nextPageAngle;

    // endregion

    @WrapMethod(method = "mouseScrolled")
    public boolean mouseScrolled$allowForLockedEnchantments(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, Operation<Boolean> original) {
        EnchantingTableScreen screen = (EnchantingTableScreen) (Object) this;
        EnchantingTableScreenHandler handler = screen.getScreenHandler();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        ClientWorld clientWorld = client.world;

        if (interactionManager == null || clientWorld == null) {
            return original.call(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        ItemStack enchantingStack = handler.getSlot(0).getStack();
        List<RegistryEntry.Reference<Enchantment>> allEnchantments = EnchantUtils.getAllEnchantmentsForStack(clientWorld.getRegistryManager(), handler.validEnchantments::contains, enchantingStack);

        if (allEnchantments.size() > 4) {
            int delta = verticalAmount > (double) 0.0F ? -1 : 1;
            handler.updateViewIndex(verticalAmount > (double) 0.0F);
            interactionManager.clickButton(handler.syncId, verticalAmount > (double) 0.0F ? 1 : 2);
            this.nextPageAngle += (float) delta;
            return true;
        }

        return original.call(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @WrapMethod(method = "mouseClicked")
    public boolean mouseClicked$disallowLockedEnchantments(Click click, boolean doubled, Operation<Boolean> original) {
        EnchantingTableScreen screen = (EnchantingTableScreen) (Object) this;
        EnchantingTableScreenHandler handler = screen.getScreenHandler();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity clientPlayer = client.player;
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        ClientWorld clientWorld = client.world;

        if (clientPlayer == null || interactionManager == null || clientWorld == null) {
            return original.call(click, doubled);
        }

        int posX = (screen.width - 176) / 2;
        int posY = (screen.height - 166) / 2 - 16;

        if (handler.canEnchant(clientPlayer, clientPlayer.isCreative())
                && isInEnchantButtonBounds(posX, posY, (int) click.x(), (int) click.y())
                && !handler.selectedEnchantments.isEmpty()
                && handler.onButtonClick(clientPlayer, 0)) {

            interactionManager.clickButton(handler.syncId, 0);
            return true;
        }

        ItemStack enchantingStack = handler.getSlot(0).getStack();
        List<RegistryEntry.Reference<Enchantment>> allEnchantments = EnchantUtils.getAllEnchantmentsForStack(clientWorld.getRegistryManager(), handler.validEnchantments::contains, enchantingStack);

        if (allEnchantments.size() > 4) {
            if (isInUpButtonBounds(posX, posY, (int) click.x(), (int) click.y()) && handler.onButtonClick(clientPlayer, 1)) {
                interactionManager.clickButton(handler.syncId, 1);
                client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                ++this.nextPageAngle;
                return true;
            }

            if (isInDownButtonBounds(posX, posY, (int) click.x(), (int) click.y()) && handler.onButtonClick(clientPlayer, 2)) {
                interactionManager.clickButton(handler.syncId, 2);
                client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                --this.nextPageAngle;
                return true;
            }
        }

        if (this.highlightedEnchantmentIndex >= 0 && handler.onButtonClick(clientPlayer, this.highlightedEnchantmentIndex + 4)) {
            interactionManager.clickButton(handler.syncId, this.highlightedEnchantmentIndex + 4);
            client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }

        return original.call(click, doubled);
    }

    @WrapMethod(method = "drawMain")
    private void drawMain$drawLockedEnchantments(DrawContext context, int mouseX, int mouseY, int posX, int posY, Operation<Void> original) {
        EnchantingTableScreen screen = (EnchantingTableScreen) (Object) this;
        EnchantingTableScreenHandler handler = screen.getScreenHandler();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        ClientPlayerEntity clientPlayer = client.player;

        if (world == null || clientPlayer == null) {
            original.call(context, mouseX, mouseY, posX, posY);
            return;
        }

        TextRenderer textRenderer = screen.getTextRenderer();

        ItemStack enchantingStack = handler.getSlot(0).getStack();
        List<RegistryEntry.Reference<Enchantment>> allEnchantments = EnchantUtils.getAllEnchantmentsForStack(world.getRegistryManager(), handler.validEnchantments::contains, enchantingStack);

        if (allEnchantments.size() > 4) {
            if (isInUpButtonBounds(posX, posY, mouseX, mouseY)) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, UP_ARROW_HIGHLIGHTED_TEXTURE, posX + 154, posY + 34, 16, 16);
            } else {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, UP_ARROW_TEXTURE, posX + 154, posY + 34, 16, 16);
            }

            if (isInDownButtonBounds(posX, posY, mouseX, mouseY)) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, DOWN_ARROW_HIGHLIGHTED_TEXTURE, posX + 154, posY + 51, 16, 16);
            } else {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, DOWN_ARROW_TEXTURE, posX + 154, posY + 51, 16, 16);
            }
        }

        if (isInEnchantButtonBounds(posX, posY, mouseX, mouseY)) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, CHECKMARK_HIGHLIGHTED_TEXTURE, posX + 154, posY + 72, 16, 16);

            if (this.infoTexts == null) {
                MutableText xpCost = Text.translatable("tooltip.enchancement.experience_level_cost", handler.getCost()).formatted(Formatting.GREEN);
                MutableText lapisCost = Text.translatable("tooltip.enchancement.material_cost", handler.getCost(), Text.translatable(Items.LAPIS_LAZULI.getTranslationKey())).formatted(Formatting.GREEN);
                MutableText materialCost = null;

                if (!handler.getEnchantingMaterial().isEmpty()) {
                    MutableText itemName = Text.translatable(handler.getEnchantingMaterial().get(this.materialIndex).value().getTranslationKey());

                    if (handler.slots.get(2).getStack().isOf(Items.ENCHANTED_BOOK)) {
                        itemName = Text.translatable(handler.slots.get(2).getStack().getItem().getTranslationKey());
                    }

                    materialCost = Text.translatable("tooltip.enchancement.material_cost", handler.getCost(), itemName).formatted(Formatting.GREEN);
                }

                if (!clientPlayer.isCreative()) {
                    if (clientPlayer.experienceLevel < handler.getCost()) {
                        xpCost.formatted(Formatting.RED);
                    }

                    if (handler.getSlot(1).getStack().getCount() < handler.getCost()) {
                        lapisCost.formatted(Formatting.RED);
                    }

                    if (materialCost != null && handler.getSlot(2).getStack().getCount() < handler.getCost()) {
                        materialCost.formatted(Formatting.RED);
                    }
                }

                if (materialCost == null) {
                    this.infoTexts = List.of(xpCost, lapisCost);
                } else {
                    this.infoTexts = List.of(xpCost, lapisCost, materialCost);
                }
            }

            context.drawTooltip(textRenderer, this.infoTexts, mouseX, mouseY);
        } else {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, CHECKMARK_TEXTURE, posX + 154, posY + 72, 16, 16);
            this.infoTexts = null;
        }

        this.highlightedEnchantmentIndex = -1;

        for (int i = 0; i < allEnchantments.size() && i < 4; ++i) {
            RegistryEntry<Enchantment> enchantment;
            if (allEnchantments.size() <= 4) {
                enchantment = allEnchantments.get(i);
            } else {
                enchantment = allEnchantments.get((i + handler.viewIndex) % allEnchantments.size());
            }

            drawEnchantmentEntry(context, mouseX, mouseY, posX, posY, handler, enchantment, enchantingStack, textRenderer, i);
        }
    }

    @Unique
    private boolean canEnchantmentBeAdded(EnchantingTableScreenHandler handler, ItemStack itemStack, RegistryEntry<Enchantment> enchantment) {
        if (!EnchantmentHelper.isCompatible(handler.selectedEnchantments, enchantment)) {
            return false;
        }

        int enchantmentCount = itemStack.getEnchantments().getSize() + handler.selectedEnchantments.size() + 1;
        return !EnchancementUtil.exceedsLimit(itemStack, enchantmentCount);
    }

    @Unique
    private boolean isEnchantmentLocked(EnchantingTableScreenHandler handler, RegistryEntry<Enchantment> enchantment) {
        return !handler.validEnchantments.contains(enchantment);
    }

    @Unique
    private boolean isEnchantmentSelected(EnchantingTableScreenHandler handler, RegistryEntry<Enchantment> enchantment) {
        return handler.selectedEnchantments.contains(enchantment);
    }

    @Unique
    private MutableText styleEnchantmentName(MutableText enchantmentName, boolean isLocked, boolean isSelected, boolean isAllowed) {
        if (isLocked) return enchantmentName.formatted(Formatting.DARK_GRAY, Formatting.STRIKETHROUGH);
        if (isSelected) return enchantmentName.formatted(Formatting.DARK_GREEN);
        if (isAllowed) return enchantmentName.formatted(Formatting.BLACK);
        return enchantmentName.formatted(Formatting.DARK_RED, Formatting.STRIKETHROUGH);
    }

    @Unique
    private void drawEnchantmentEntry(DrawContext context, int mouseX, int mouseY, int posX, int posY, EnchantingTableScreenHandler handler, RegistryEntry<Enchantment> enchantment, ItemStack enchantingStack, TextRenderer textRenderer, int i) {
        boolean isLocked = isEnchantmentLocked(handler, enchantment);
        boolean isAllowed = canEnchantmentBeAdded(handler, enchantingStack, enchantment);
        boolean isSelected = isEnchantmentSelected(handler, enchantment);

        MutableText nameText = enchantment.value().description().copy();
        String enchantmentName = nameText.getString();

        if (textRenderer.getWidth(enchantmentName) > MAX_ENCHANTMENT_NAME_WIDTH) {
            nameText = Text.literal(trimAndScrollText(textRenderer, enchantmentName));
        }

        nameText = styleEnchantmentName(nameText, isLocked, isSelected, isAllowed);
        context.drawText(textRenderer, nameText, posX + 66, posY + 16 + i * 19, -1, false);

        if (isLocked) {
            int lockPositionX = posX + 66 + MAX_ENCHANTMENT_NAME_WIDTH + 3;
            int lockPositionY = posY + 16 + i * 19 - 2;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, LOCK_TEXTURE, lockPositionX, lockPositionY, LOCK_ICON_WIDTH, LOCK_ICON_HEIGHT);
            // "Locked" tooltip when hovering over the lock.
            if (isInBounds(lockPositionX, lockPositionY, mouseX, mouseY, 0, LOCK_ICON_WIDTH, 0, LOCK_ICON_HEIGHT)) {
                context.drawTooltip(textRenderer, List.of(Text.literal("Locked").formatted(Formatting.GRAY)), mouseX, mouseY);
            }

            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, LOCKED_TAB_TEXTURE, posX + 66 - 7, posY + 16 + i * 19 - 2, 5, 11);
        }

        if (DRAW_ENCHANTMENT_ENTRY_HOR_SEPARATOR) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ENTRY_LINE_TEXTURES[i], posX + 66 - 7, posY + 16 + i * 19 - 2, 93, 19);
        }

        if (isInBounds(posX, posY + 11 + i * 19, mouseX, mouseY, 64, 67 + textRenderer.getWidth(nameText), 0, 16)) {
            if (isAllowed || isLocked || isSelected) {
                // Can only select non-locked enchantments.
                this.highlightedEnchantmentIndex = isLocked ? -1 : i;
            }

            // Update info texts.
            if (this.infoTexts == null) {
                MutableText tooltipEnchantmentName = enchantment.value().description().copy().formatted(Formatting.GRAY);
                MutableText tooltipEnchantmentDescription = Text.translatable(EnchancementUtil.getTranslationKey(enchantment) + ".desc").formatted(Formatting.DARK_GRAY);
                // If the enchantment is locked, obfuscate its description in the tooltip.
                if (OBFUSCATE_LOCKED_ENCHANTMENT_DESCRIPTION && isLocked) {
                    tooltipEnchantmentDescription.styled(style -> style.withFont(GALACTIC_FONT));
                }

                this.infoTexts = new ArrayList<>();

                if (tooltipEnchantmentDescription.getString().isEmpty()) {
                    this.infoTexts.add(tooltipEnchantmentName);
                } else {
                    this.infoTexts.add(tooltipEnchantmentName);
                    this.infoTexts.addAll(SLibClientUtils.wrapText(Text.literal(" - ").formatted(Formatting.GRAY).append(tooltipEnchantmentDescription)));
                }

                if (isLocked) {
                    this.infoTexts.add(Text.literal("Add to your bookshelves to unlock.").formatted(Formatting.RED));
                }
            }

            context.drawTooltip(textRenderer, this.infoTexts, mouseX, mouseY);
            return;
        }

        this.infoTexts = null;
    }

    @Unique
    private static @NonNull String trimAndScrollText(TextRenderer textRenderer, String fullName) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld clientWorld = client.world;
        long time = clientWorld == null ? 0 : clientWorld.getTime();
        return StringUtil.scrollingTextFromTime(textRenderer, MAX_ENCHANTMENT_NAME_WIDTH, fullName, time, 10d);
    }
}

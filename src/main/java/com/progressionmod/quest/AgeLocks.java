package com.progressionmod.quest;

import com.progressionmod.ModConfig;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.ToIntFunction;

/**
 * Stops players from using items that belong to an age they haven't reached.
 *
 * Locked items can't be picked up ({@code AgeLockPickupMixin}), taken out of any
 * container, crafting grid or smithing table ({@code AgeLockSlotMixin}), used,
 * attacked with, or worn. Creative and spectator players are exempt.
 */
public final class AgeLocks {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    /** Supplies the age of client-side players; set by the client initializer. */
    public static ToIntFunction<Player> clientAge = player -> Age.COUNT;

    private static final Map<UUID, Long> lastWarning = new HashMap<>();

    private AgeLocks() {}

    /** Index of the age an item belongs to, or -1 if it isn't age-locked. */
    public static int requiredAge(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        for (int i = Age.COUNT - 1; i >= 0; i--) {
            if (stack.is(Age.values()[i].itemTag)) return i;
        }
        return -1;
    }

    public static int currentAge(Player player) {
        if (player instanceof ServerPlayer serverPlayer) return QuestData.get(serverPlayer).age;
        return clientAge.applyAsInt(player);
    }

    public static boolean isLocked(Player player, ItemStack stack) {
        if (player == null || player.isCreative() || player.isSpectator()) return false;
        if (!ModConfig.get().ageItemLocking) return false;
        int required = requiredAge(stack);
        return required >= 0 && required > currentAge(player);
    }

    /** Shows "this item belongs to the X Age" on the action bar, at most once a second. */
    public static void warn(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer)) return;
        long now = player.level().getGameTime();
        Long last = lastWarning.get(player.getUUID());
        if (last != null && now - last < 20) return;
        lastWarning.put(player.getUUID(), now);
        Age age = Age.byIndex(requiredAge(stack));
        String ageName = age != null ? age.displayName : "a later age";
        player.sendOverlayMessage(Component.literal("🔒 ")
                .append(stack.getHoverName())
                .append(" belongs to the " + ageName + ". Complete your quests to unlock it!")
                .withStyle(ChatFormatting.RED));
    }

    private static InteractionResult check(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isLocked(player, stack)) {
            warn(player, stack);
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    public static void register() {
        UseItemCallback.EVENT.register((player, level, hand) -> check(player, hand));
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> check(player, hand));
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> check(player, hand));
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> check(player, InteractionHand.MAIN_HAND));
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> check(player, InteractionHand.MAIN_HAND));
    }

    /** Moves locked armor out of armor slots (back into the inventory, or dropped if full). */
    public static void enforceEquipment(ServerPlayer player) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (isLocked(player, stack)) {
                player.setItemSlot(slot, ItemStack.EMPTY);
                warn(player, stack);
                player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
            }
        }
    }
}

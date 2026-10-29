package com.progressionmod.quest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.progressionmod.items.ModItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

/**
 * {@code /quests} opens the quest book. {@code /quests book} gives a new book.
 * Operators also get {@code age}, {@code complete}, {@code reset} and {@code info}.
 */
public final class QuestCommands {

    private static final SuggestionProvider<CommandSourceStack> AGES = (ctx, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(Age.values()).map(a -> a.id), builder);

    private static final SuggestionProvider<CommandSourceStack> QUESTS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(QuestRegistry.SERVER.all().stream().map(QuestDefinition::id), builder);

    private QuestCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("quests")
                .executes(ctx -> {
                    ServerPlayNetworking.send(ctx.getSource().getPlayerOrException(), new QuestNetworking.OpenBookPayload());
                    return 1;
                })
                .then(Commands.literal("book").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.QUEST_BOOK), Prediction.SERVER_ONLY);
                    return 1;
                }))
                .then(Commands.literal("info")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("player", EntityArgument.player()).executes(QuestCommands::info)))
                .then(Commands.literal("age")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("age", StringArgumentType.word()).suggests(AGES)
                                        .executes(QuestCommands::setAge))))
                .then(Commands.literal("complete")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("quest", StringArgumentType.greedyString()).suggests(QUESTS)
                                        .executes(QuestCommands::complete))))
                .then(Commands.literal("reset")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("player", EntityArgument.player()).executes(ctx -> {
                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                            QuestManager.reset(player);
                            ctx.getSource().sendSuccess(() -> Component.literal("Reset quest progress of ")
                                    .append(player.getDisplayName()), true);
                            return 1;
                        }))));
    }

    private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        QuestData data = QuestData.get(player);
        long inAge = QuestRegistry.SERVER.forAge(data.age).stream().filter(q -> data.completed.contains(q.id())).count();
        int ageTotal = QuestRegistry.SERVER.forAge(data.age).size();
        ctx.getSource().sendSuccess(() -> Component.empty().append(player.getDisplayName())
                .append(": " + Age.nameOf(data.age) + " (" + inAge + "/" + ageTotal + " quests), "
                        + data.completed.size() + "/" + QuestRegistry.SERVER.size() + " total, "
                        + data.visitedBiomes.size() + " biomes visited"), false);
        return 1;
    }

    private static int setAge(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        String name = StringArgumentType.getString(ctx, "age");
        int index;
        if (name.equals("ascended")) {
            index = Age.COUNT;
        } else {
            Age age = Age.byId(name);
            if (age == null) {
                ctx.getSource().sendFailure(Component.literal("Unknown age: " + name));
                return 0;
            }
            index = age.ordinal();
        }
        QuestManager.setAge(player, index);
        ctx.getSource().sendSuccess(() -> Component.literal("Set age of ").append(player.getDisplayName())
                .append(" to " + Age.nameOf(index)), true);
        return 1;
    }

    private static int complete(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        String id = StringArgumentType.getString(ctx, "quest");
        if (!QuestManager.forceComplete(player, id)) {
            ctx.getSource().sendFailure(Component.literal("Unknown quest: " + id));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.literal("Completed quest " + id + " for ")
                .append(player.getDisplayName()), true);
        return 1;
    }
}

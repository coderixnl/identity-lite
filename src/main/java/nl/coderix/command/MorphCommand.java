package nl.coderix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import nl.coderix.IMorphedPlayer;
import nl.coderix.network.MorphSyncPayload;

public class MorphCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(Commands.literal("morph")
                .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.argument("type", IdentifierArgument.id())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                BuiltInRegistries.ENTITY_TYPE.keySet(), builder
                        ))
                        .executes(context -> morph(context.getSource(), IdentifierArgument.getId(context, "type"), false))
                        .then(Commands.argument("baby", BoolArgumentType.bool())
                                .executes(context -> morph(
                                        context.getSource(),
                                        IdentifierArgument.getId(context, "type"),
                                        BoolArgumentType.getBool(context, "baby")
                                ))))
        );

        dispatcher.register(Commands.literal("unmorph")
                .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayerOrException();

                    if (player instanceof IMorphedPlayer morphedPlayer) {
                        morphedPlayer.setMorph(null);

                        MorphSyncPayload payload = new MorphSyncPayload(player.getUUID(), "", false);

                        ServerPlayNetworking.send(player, payload);
                        for (ServerPlayer tracker : PlayerLookup.tracking(player)) {
                            ServerPlayNetworking.send(tracker, payload);
                        }

                        source.sendSuccess(() -> Component.literal("Unmorphed"), true);
                    }
                    return 1;
                })
        );
    }

    private static int morph(CommandSourceStack source, Identifier id, boolean baby) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id);

        if (type != null && player instanceof IMorphedPlayer morphedPlayer) {
            morphedPlayer.setMorph(type, baby);

            String morphId = EntityType.getKey(type).toString();
            boolean actualBaby = morphedPlayer.isBabyMorph();
            MorphSyncPayload payload = new MorphSyncPayload(player.getUUID(), morphId, actualBaby);

            ServerPlayNetworking.send(player, payload);
            for (ServerPlayer tracker : PlayerLookup.tracking(player)) {
                ServerPlayNetworking.send(tracker, payload);
            }

            String suffix = actualBaby ? " (baby)" : "";
            source.sendSuccess(() -> Component.literal("Morphed into " + morphId + suffix), true);
        } else {
            source.sendFailure(Component.literal("Unknown entity type"));
        }
        return 1;
    }
}

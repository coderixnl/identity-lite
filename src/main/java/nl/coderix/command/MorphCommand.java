package nl.coderix.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player = source.getPlayerOrException();

                            Identifier id = IdentifierArgument.getId(context, "type");
                            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id);

                            if (type != null && player instanceof IMorphedPlayer morphedPlayer) {
                                morphedPlayer.setMorph(type);

                                String morphId = EntityType.getKey(type).toString();
                                MorphSyncPayload payload = new MorphSyncPayload(player.getUUID(), morphId);

                                ServerPlayNetworking.send(player, payload);
                                for (ServerPlayer tracker : PlayerLookup.tracking(player)) {
                                    ServerPlayNetworking.send(tracker, payload);
                                }

                                source.sendSuccess(() -> Component.literal("Morphed into " + morphId), true);
                            } else {
                                source.sendFailure(Component.literal("Unknown entity type"));
                            }
                            return 1;
                        }))
        );

        dispatcher.register(Commands.literal("unmorph")
                .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayerOrException();

                    if (player instanceof IMorphedPlayer morphedPlayer) {
                        morphedPlayer.setMorph(null);

                        MorphSyncPayload payload = new MorphSyncPayload(player.getUUID(), "");

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
}

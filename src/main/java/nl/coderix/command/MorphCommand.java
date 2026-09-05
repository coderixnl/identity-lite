package nl.coderix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import nl.coderix.IMorphedPlayer;
import nl.coderix.network.MorphSyncPayload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MorphCommand {
    private static volatile List<Identifier> livingEntityTypeIds;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(Commands.literal("morph")
                .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("type", IdentifierArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                        getLivingEntityTypeIds(context.getSource()), builder
                                ))
                                .executes(context -> morph(
                                        context.getSource(),
                                        EntityArgument.getPlayers(context, "targets"),
                                        IdentifierArgument.getId(context, "type"),
                                        false
                                ))
                                .then(Commands.argument("baby", BoolArgumentType.bool())
                                        .executes(context -> morph(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, "targets"),
                                                IdentifierArgument.getId(context, "type"),
                                                BoolArgumentType.getBool(context, "baby")
                                        )))))
        );

        dispatcher.register(Commands.literal("unmorph")
                .requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");

                            for (ServerPlayer player : targets) {
                                IMorphedPlayer morphedPlayer = (IMorphedPlayer) player;
                                morphedPlayer.setMorph(null);
                                syncMorph(player);
                            }

                            source.sendSuccess(() -> Component.literal(
                                    "Unmorphed " + targets.size() + " player(s)"
                            ), true);
                            return targets.size();
                        }))
        );
    }

    private static int morph(CommandSourceStack source, Collection<ServerPlayer> targets, Identifier id, boolean baby) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        if (type == null || !isLivingEntityType(source, type)) {
            source.sendFailure(Component.literal("Unknown or non-living entity type: " + id));
            return 0;
        }

        boolean actualBaby = false;
        boolean firstTarget = true;
        for (ServerPlayer player : targets) {
            IMorphedPlayer morphedPlayer = (IMorphedPlayer) player;
            morphedPlayer.setMorph(type, baby);
            syncMorph(player);
            if (firstTarget) {
                actualBaby = morphedPlayer.isBabyMorph();
                firstTarget = false;
            }
        }

        String morphId = EntityType.getKey(type).toString();
        String suffix = actualBaby ? " (baby)" : "";
        source.sendSuccess(() -> Component.literal(
                "Morphed " + targets.size() + " player(s) into " + morphId + suffix
        ), true);
        return targets.size();
    }

    private static boolean isLivingEntityType(CommandSourceStack source, EntityType<?> type) {
        Entity entity;
        try {
            entity = type.create(source.getLevel(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        } catch (RuntimeException exception) {
            return false;
        }

        boolean living = entity instanceof LivingEntity;
        if (entity != null) {
            entity.discard();
        }
        return living;
    }

    private static List<Identifier> getLivingEntityTypeIds(CommandSourceStack source) {
        List<Identifier> cachedIds = livingEntityTypeIds;
        if (cachedIds != null) {
            return cachedIds;
        }

        synchronized (MorphCommand.class) {
            if (livingEntityTypeIds == null) {
                List<Identifier> ids = new ArrayList<>();
                for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                    if (isLivingEntityType(source, type)) {
                        ids.add(EntityType.getKey(type));
                    }
                }
                livingEntityTypeIds = List.copyOf(ids);
            }
            return livingEntityTypeIds;
        }
    }

    private static void syncMorph(ServerPlayer player) {
        IMorphedPlayer morphedPlayer = (IMorphedPlayer) player;
        EntityType<?> type = morphedPlayer.getMorph();
        String morphId = type == null ? "" : EntityType.getKey(type).toString();
        MorphSyncPayload payload = new MorphSyncPayload(
                player.getUUID(),
                morphId,
                type != null && morphedPlayer.isBabyMorph()
        );

        ServerPlayNetworking.send(player, payload);
        for (ServerPlayer tracker : PlayerLookup.tracking(player)) {
            ServerPlayNetworking.send(tracker, payload);
        }
    }
}

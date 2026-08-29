package nl.coderix;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import nl.coderix.command.MorphCommand;
import nl.coderix.network.MorphRequestPayload;
import nl.coderix.network.MorphSyncPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentityLite implements ModInitializer {
	public static final String MOD_ID = "identity-lite";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Identity Lite...");

		PayloadTypeRegistry.clientboundPlay().register(MorphSyncPayload.ID, MorphSyncPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(MorphRequestPayload.ID, MorphRequestPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(MorphRequestPayload.ID, (payload, context) -> {
			Player player = context.player();
			// Reject non-operators before scheduling any work on the server thread.
			if (!player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
				return;
			}
			player.level().getServer().execute(() -> {
				if (player instanceof IMorphedPlayer morphedPlayer) {
					if (payload.morphTypeId() == null || payload.morphTypeId().isEmpty()) {
						morphedPlayer.setMorph(null);
						MorphSyncPayload syncPayload = new MorphSyncPayload(player.getUUID(), "", false);
						ServerPlayNetworking.send((net.minecraft.server.level.ServerPlayer) player, syncPayload);
						for (net.minecraft.server.level.ServerPlayer tracker : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(player)) {
							ServerPlayNetworking.send(tracker, syncPayload);
						}
					} else {
						try {
							net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.parse(payload.morphTypeId());
							EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id);
							if (type == null) {
								LOGGER.warn("Unknown morph type id from client: {}", payload.morphTypeId());
								return;
							}
							morphedPlayer.setMorph(type, payload.baby());

							// Sync to clients
							MorphSyncPayload syncPayload = new MorphSyncPayload(player.getUUID(), payload.morphTypeId(), morphedPlayer.isBabyMorph());
							ServerPlayNetworking.send((net.minecraft.server.level.ServerPlayer) player, syncPayload);
							for (net.minecraft.server.level.ServerPlayer tracker : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(player)) {
								ServerPlayNetworking.send(tracker, syncPayload);
							}
						} catch (Exception e) {
							LOGGER.error("Failed to parse morph id from client: {}", payload.morphTypeId(), e);
						}
					}
				}
			});
		});

		// Sync morph when a player starts tracking another player
		EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
			if (trackedEntity instanceof Player targetPlayer && targetPlayer instanceof IMorphedPlayer morphed) {
				EntityType<?> morphType = morphed.getMorph();
				if (morphType != null) {
					String morphId = BuiltInRegistries.ENTITY_TYPE.getKey(morphType).toString();
					ServerPlayNetworking.send(player, new MorphSyncPayload(targetPlayer.getUUID(), morphId, morphed.isBabyMorph()));
				}
			}
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, _)
				-> MorphCommand.register(dispatcher, registryAccess));
	}
}

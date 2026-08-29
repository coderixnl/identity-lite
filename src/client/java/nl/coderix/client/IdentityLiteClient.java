package nl.coderix.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import nl.coderix.IMorphedPlayer;
import nl.coderix.IdentityLite;
import nl.coderix.network.MorphSyncPayload;

public class IdentityLiteClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(MorphSyncPayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				if (context.client().level == null) return;

				Player player = context.client().level.getPlayerByUUID(payload.playerId());
				if (player instanceof IMorphedPlayer morphedPlayer) {
					if (payload.morphTypeId() == null || payload.morphTypeId().isEmpty()) {
						morphedPlayer.setMorph(null);
					} else {
						try {
							Identifier id = Identifier.parse(payload.morphTypeId());
                            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id);
                            morphedPlayer.setMorph(type, payload.baby());
                        } catch (Exception ignored) {
							// Ignore parse errors
						}
					}
				}
			});
		});

		net.minecraft.client.KeyMapping morphKeyBinding = net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(new net.minecraft.client.KeyMapping(
				"key.identity_lite.morph",
				com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
				org.lwjgl.glfw.GLFW.GLFW_KEY_GRAVE_ACCENT,
				net.minecraft.client.KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(IdentityLite.MOD_ID, "morph"))
		));

		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (morphKeyBinding.consumeClick()) {
				if (client.player != null && client.player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
					client.setScreenAndShow(new MorphScreen());
				} else if (client.player != null) {
					client.player.sendOverlayMessage(net.minecraft.network.chat.Component.literal("Вы должны быть администратором для использования этого меню."));
				}
			}
		});
	}
}

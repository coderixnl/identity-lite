package nl.coderix.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import nl.coderix.IdentityLite;

import java.util.UUID;

public record MorphSyncPayload(UUID playerId, String morphTypeId, boolean baby) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MorphSyncPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(IdentityLite.MOD_ID, "morph_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MorphSyncPayload> CODEC = StreamCodec.composite(
            net.minecraft.core.UUIDUtil.STREAM_CODEC, MorphSyncPayload::playerId,
            ByteBufCodecs.stringUtf8(256), MorphSyncPayload::morphTypeId,
            ByteBufCodecs.BOOL, MorphSyncPayload::baby,
            MorphSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

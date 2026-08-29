package nl.coderix.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import nl.coderix.IdentityLite;

public record MorphRequestPayload(String morphTypeId, boolean baby) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MorphRequestPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(IdentityLite.MOD_ID, "morph_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MorphRequestPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256), MorphRequestPayload::morphTypeId,
            ByteBufCodecs.BOOL, MorphRequestPayload::baby,
            MorphRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

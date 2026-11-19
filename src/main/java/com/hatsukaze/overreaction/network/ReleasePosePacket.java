package com.hatsukaze.overreaction.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 保持中のポーズをfadeTicksかけて手放させる
 */
public record ReleasePosePacket(int entityId, int fadeTicks)
        implements CustomPacketPayload {

    public static final Type<ReleasePosePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("overreaction", "release_pose"));

    public static final StreamCodec<ByteBuf, ReleasePosePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, ReleasePosePacket::entityId,
                    ByteBufCodecs.INT, ReleasePosePacket::fadeTicks,
                    ReleasePosePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
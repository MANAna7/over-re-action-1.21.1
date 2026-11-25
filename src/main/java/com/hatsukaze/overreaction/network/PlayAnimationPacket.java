package com.hatsukaze.overreaction.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlayAnimationPacket(int entityId, String animationName, float totalAttackingTime,
                                  float animationNaturalTime, int blendInTicks, String fpAnimationName)
        implements CustomPacketPayload {

    //PlayAnimationPacketのタイプ（種類？）を名前空間とそのパスで作成、ネットワーク上でどのパケットか識別するために使用
    public static final Type<PlayAnimationPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("overreaction", "play_animation"));

    //ストリームコーデック、ネットワークのパケット通信用に型を指定するクラス
    //バイト列に変換する/バイト列から復元する をしてくれる
    public static final StreamCodec<ByteBuf, PlayAnimationPacket> STREAM_CODEC =
            StreamCodec.composite(
                    // エンティティのID
                    ByteBufCodecs.INT, PlayAnimationPacket::entityId,
                    // アニメーションの名前
                    ByteBufCodecs.STRING_UTF8, PlayAnimationPacket::animationName,
                    // 攻撃アニメーションの時間
                    ByteBufCodecs.FLOAT, PlayAnimationPacket::totalAttackingTime,
                    // アニメーション自体の時間
                    ByteBufCodecs.FLOAT, PlayAnimationPacket::animationNaturalTime,
                    //　設定ティック数
                    ByteBufCodecs.INT, PlayAnimationPacket::blendInTicks,
                    // 一人称アニメーションの名前
                    ByteBufCodecs.STRING_UTF8, PlayAnimationPacket::fpAnimationName,
                    PlayAnimationPacket::new
            );

    //作ったやつの返答用？これってどのタイプ？ってときに返事するメソッド
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
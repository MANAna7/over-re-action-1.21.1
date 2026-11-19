package com.hatsukaze.overreaction.network;

import com.hatsukaze.overreaction.manager.ClientHitStopManager;
import com.hatsukaze.overreaction.manager.ClientPoseController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class AnimationPacketHandler {

    public static void handlePlayAnimation(
            PlayAnimationPacket packet,
            IPayloadContext context) {

        context.enqueueWork(() -> {
            // クライアント処理はここで分岐
            if (context.flow() == PacketFlow.CLIENTBOUND) {
                handleClient(packet);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PlayAnimationPacket packet) {
        // 元のクライアント処理をここに移動
        Minecraft.getInstance().player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("パケット受信: " + packet.animationName())
        );
        Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
        if (!(entity instanceof AbstractClientPlayer player)) return;

        float speed = packet.animationNaturalTime() / packet.totalAttackingTime();

        // 保持付き再生＋前のポーズからのフェードはClientPoseControllerに任せる
        ClientPoseController.play(
                player,
                ResourceLocation.fromNamespaceAndPath("overreaction", packet.animationName()),
                speed,
                packet.blendInTicks()
        );
    }

    @OnlyIn(Dist.CLIENT)
    public static void handleHitStop(HitStopPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> handleHitStopClient(packet));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleHitStopClient(HitStopPacket packet) {
        Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
        if (!(entity instanceof AbstractClientPlayer player)) return;

        ClientHitStopManager.start(player, packet.duration());
    }

    public static void handleReleasePose(ReleasePosePacket packet, IPayloadContext context) {
        //TIPS: context.emqueueWork ゲームのデータ操作系は必ずメインスレッドで行う必要がある　処理予約をする　重い処理は注意？
        //neoforgeなら一応デフォでメインスレッドで出てるけど、明示的宣言をするのは　まる！
        context.enqueueWork(() -> {
            if (context.flow() == PacketFlow.CLIENTBOUND) {
                handleReleasePoseClient(packet);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleReleasePoseClient(ReleasePosePacket packet) {
        Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
        // エンティティがプレイヤーではないときreturn
        if (!(entity instanceof AbstractClientPlayer player)) return;

        ClientPoseController.release(player, packet.fadeTicks());
    }
}
package de.melanx.datatrader.network.handler;

import de.melanx.datatrader.DataTrader;
import de.melanx.datatrader.trader.TraderMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class SelectTrade extends PacketHandler<SelectTrade.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(DataTrader.getInstance().resource("select_trade"));

    public SelectTrade() {
        super(TYPE, PacketFlow.SERVERBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
        }

        AbstractContainerMenu abstractMenu = player.containerMenu;
        if (abstractMenu instanceof TraderMenu menu) {
            if (!menu.stillValid(player)) {
                DataTrader.getInstance().logger.debug("Player {} interacted with invalid menu {}", player, menu);
                return;
            }

            menu.setSelectionHint(msg.item);
            menu.tryMoveItems(msg.item);
        }
    }

    public record Message(int item) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, Message::item, Message::new
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}

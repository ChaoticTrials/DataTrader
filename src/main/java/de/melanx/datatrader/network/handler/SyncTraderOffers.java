package de.melanx.datatrader.network.handler;

import de.melanx.datatrader.DataTrader;
import de.melanx.datatrader.trader.TraderMenu;
import de.melanx.datatrader.trader.TraderOffers;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.moddingx.libx.network.PacketHandler;

import javax.annotation.Nonnull;

public class SyncTraderOffers extends PacketHandler<SyncTraderOffers.Message> {

    public static final CustomPacketPayload.Type<Message> TYPE = new CustomPacketPayload.Type<>(DataTrader.getInstance().resource("sync_trader_offers"));

    public SyncTraderOffers() {
        super(TYPE, PacketFlow.CLIENTBOUND, Message.CODEC, HandlerThread.MAIN);
    }

    @Override
    public void handle(Message msg, IPayloadContext ctx) {
        AbstractContainerMenu containerMenu = Minecraft.getInstance().player.containerMenu;
        if (msg.containerId == containerMenu.containerId && containerMenu instanceof TraderMenu menu) {
            menu.setOffers(msg.offers);
        }
    }

    public record Message(int containerId, TraderOffers offers) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, Message> CODEC = StreamCodec.of(
                (buffer, msg) -> {
                    buffer.writeVarInt(msg.containerId);
                    TraderOffers.STREAM_CODEC.encode(buffer, msg.offers);
                }, buffer -> new Message(buffer.readVarInt(), TraderOffers.STREAM_CODEC.decode(buffer))
        );

        @Nonnull
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}

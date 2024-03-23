package de.melanx.datatrader.network;

import de.melanx.datatrader.DataTrader;
import de.melanx.datatrader.trader.TraderMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public record SelectTrade(int item) {

    public static class Handler implements PacketHandler<SelectTrade> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(SelectTrade msg, Supplier<NetworkEvent.Context> supplier) {
            ServerPlayer player = supplier.get().getSender();
            if (player == null) {
                return true;
            }

            AbstractContainerMenu abstractMenu = player.containerMenu;
            if (abstractMenu instanceof TraderMenu menu) {
                if (!menu.stillValid(player)) {
                    DataTrader.getInstance().logger.debug("Player {} interacted with invalid menu {}", player, menu);
                    return true;
                }

                menu.setSelectionHint(msg.item);
                menu.tryMoveItems(msg.item);
            }

            return true;
        }
    }

    public static class Serializer implements PacketSerializer<SelectTrade> {

        @Override
        public Class<SelectTrade> messageClass() {
            return SelectTrade.class;
        }

        @Override
        public void encode(SelectTrade msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.item);
        }

        @Override
        public SelectTrade decode(FriendlyByteBuf buf) {
            return new SelectTrade(buf.readInt());
        }
    }
}

package de.melanx.datatrader.network;

import de.melanx.datatrader.trader.TraderMenu;
import de.melanx.datatrader.trader.TraderOffers;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import org.moddingx.libx.network.PacketHandler;
import org.moddingx.libx.network.PacketSerializer;

import java.util.function.Supplier;

public record SyncTraderOffers(int containerId, TraderOffers offers) {

    public static class Handler implements PacketHandler<SyncTraderOffers> {

        @Override
        public Target target() {
            return Target.MAIN_THREAD;
        }

        @Override
        public boolean handle(SyncTraderOffers msg, Supplier<NetworkEvent.Context> ctx) {
            //noinspection DataFlowIssue
            AbstractContainerMenu containerMenu = Minecraft.getInstance().player.containerMenu;
            if (msg.containerId == containerMenu.containerId && containerMenu instanceof TraderMenu menu) {
                menu.setOffers(msg.offers);
            }

            return true;
        }
    }

    public static class Serializer implements PacketSerializer<SyncTraderOffers> {

        @Override
        public Class<SyncTraderOffers> messageClass() {
            return SyncTraderOffers.class;
        }

        @Override
        public void encode(SyncTraderOffers msg, FriendlyByteBuf buffer) {
            buffer.writeInt(msg.containerId);
            buffer.writeNbt(msg.offers.createTag());
        }

        @Override
        public SyncTraderOffers decode(FriendlyByteBuf buffer) {
            //noinspection DataFlowIssue
            return new SyncTraderOffers(buffer.readInt(), new TraderOffers(buffer.readNbt()));
        }
    }
}

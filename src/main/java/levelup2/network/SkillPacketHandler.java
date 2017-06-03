package levelup2.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import levelup2.LevelUp2;
import levelup2.api.IPlayerSkill;
import levelup2.config.LevelUpConfig;
import levelup2.player.IPlayerClass;
import levelup2.player.PlayerExtension;
import levelup2.skills.SkillRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;
import java.util.Map;

public class SkillPacketHandler {
    public static final String[] CHANNELS = {"levelupinit", "levelupclasses", "levelupskills", "levelupcfg"};
    public static FMLEventChannel initChannel, classChannel, skillChannel, configChannel;

    public static void init() {
        SkillPacketHandler handler = new SkillPacketHandler();
        initChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNELS[0]);
        initChannel.register(handler);
        classChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNELS[1]);
        classChannel.register(handler);
        skillChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNELS[2]);
        skillChannel.register(handler);
        configChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNELS[3]);
        configChannel.register(handler);
        MinecraftForge.EVENT_BUS.register(handler);
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent evt) {
        ByteBuf in = evt.getPacket().payload();
        EntityPlayerMP player = ((NetHandlerPlayServer)evt.getHandler()).playerEntity;
        if (evt.getPacket().channel().equals(CHANNELS[1])) {
            addTask(evt.getHandler(), () -> handleClassChange(in.readByte(), player));
        } else if (evt.getPacket().channel().equals(CHANNELS[2])) {
            addTask(evt.getHandler(), () -> handlePacket(in, player));
        }
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent evt) {
        ByteBuf in = evt.getPacket().payload();
        if (evt.getPacket().channel().equals(CHANNELS[0])) {
            addTask(evt.getHandler(), () -> handlePacket(in, LevelUp2.proxy.getPlayer()));
        } else if (evt.getPacket().channel().equals(CHANNELS[3])) {
            addTask(evt.getHandler(), () -> handleConfig(in));
        }
    }

    private void addTask(INetHandler netHandler, Runnable runnable) {
        FMLCommonHandler.instance().getWorldThread(netHandler).addScheduledTask(runnable);
    }

    private void handleClassChange(byte newClass, EntityPlayerMP player) {
        if (newClass >= 0) {
            SkillRegistry.getPlayer(player).setSpecialization(newClass);
            SkillRegistry.loadPlayer(player);
        }
    }

    private void handlePacket(ByteBuf buf, EntityPlayer player) {
        boolean isInit = player.world.isRemote;
        byte button = buf.readByte();
        int levelSpend = buf.readInt();
        String[] skills = null;
        int[] data = null;
        int sum = 0;
        if (isInit || button == -1) {
            data = new int[SkillRegistry.getSkillRegistry().size()];
            skills = new String[SkillRegistry.getSkillRegistry().size()];
            for (int i = 0; i < data.length; i++) {
                skills[i] = ByteBufUtils.readUTF8String(buf);
                data[i] = buf.readInt();
                sum += data[i];
            }
        }
        IPlayerClass properties = SkillRegistry.getPlayer(player);
        if (!isInit) {
            if (data != null && button == -1 && sum == 0) {
                for (int i = 0; i < data.length; i++) {
                    if (data[i] != 0)
                        properties.addToSkill(skills[i], data[i]);
                }
                SkillRegistry.loadPlayer(player);
            }
        }
        else if (data != null) {
            properties.setSpecialization(button);
            properties.setPlayerData(skills, data);
        }
        if (levelSpend > 0)
            player.removeExperienceLevel(levelSpend);
    }

    public static FMLProxyPacket getPacket(Side side, int channel, byte ID, Object... data) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(ID);
        if ((ID < 0 || channel == 0) && data != null) {
            if (data.length == 1 && data[0] instanceof Map) {
                Map<String, Integer> map = (Map)data[0];
                buf.writeInt(0);
                for (String str : map.keySet()) {
                    ByteBufUtils.writeUTF8String(buf, str);
                    buf.writeInt(map.get(str));
                }/*
                for (IPlayerSkill skill : (List<IPlayerSkill>)data[0]) {
                    ByteBufUtils.writeUTF8String(buf, skill.getSkillName());
                    buf.writeInt(skill.getSkillLevel());
                }*/
            } else if (data.length == 2 && data[0] instanceof Map && data[1] instanceof Integer) {
                buf.writeInt((int)data[1]);
                Map<String, Integer> map = (Map)data[0];
                for (String str : map.keySet()) {
                    ByteBufUtils.writeUTF8String(buf, str);
                    buf.writeInt(map.get(str));
                }/*
                for (IPlayerSkill skill : (List<IPlayerSkill>)data[0]) {
                    ByteBufUtils.writeUTF8String(buf, skill.getSkillName());
                    buf.writeInt(skill.getSkillLevel());
                }*/
            } else {
                for (Object dat : data) {
                    if (dat instanceof String) {
                        ByteBufUtils.writeUTF8String(buf, (String) dat);
                    } else if (dat instanceof Integer) {
                        buf.writeInt((int) dat);
                    }
                }
            }
        }
        FMLProxyPacket pkt = new FMLProxyPacket(new PacketBuffer(buf), CHANNELS[channel]);
        pkt.setTarget(side);
        return pkt;
    }

    public static FMLProxyPacket getConfigPacket(Property... dat) {
        ByteBuf buf = Unpooled.buffer();
        for (int i = 0; i < dat.length; i++) {
            buf.writeBoolean(dat[i].getBoolean());
        }
        FMLProxyPacket pkt = new FMLProxyPacket(new PacketBuffer(buf), CHANNELS[3]);
        pkt.setTarget(Side.CLIENT);
        return pkt;
    }

    private void handleConfig(ByteBuf buf) {
        Property[] properties = LevelUpConfig.getServerProperties();
        for (int i = 0; i < properties.length; i++) {
            properties[i].set(buf.readBoolean());
        }
        LevelUpConfig.useServerProperties();
    }
}

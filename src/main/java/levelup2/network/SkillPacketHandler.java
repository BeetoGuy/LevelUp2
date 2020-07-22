package levelup2.network;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import levelup2.LevelUp2;
import levelup2.api.BaseClass;
import levelup2.api.BaseSkill;
import levelup2.api.ICharacterClass;
import levelup2.api.IPlayerSkill;
import levelup2.config.LevelUpConfig;
import levelup2.items.ItemRespecBook;
import levelup2.player.IPlayerClass;
import levelup2.skills.SkillRegistry;
import levelup2.util.ClassProperties;
import levelup2.util.SkillProperties;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

public class SkillPacketHandler {
    public static final String[] CHANNELS = {"levelupinit", "levelupclasses", "levelupskills", "levelupcfg", "levelupproperties", "leveluprefresh", "levelupclass", "leveluptoggle", "levelchange", "levelreturn"};
    public static FMLEventChannel initChannel, classChannel, skillChannel, configChannel, propertyChannel, refreshChannel, classPropChannel, toggleChannel, levelChannel, levelReturn;

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
        propertyChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNELS[4]);
        propertyChannel.register(handler);
        refreshChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNELS[5]);
        refreshChannel.register(handler);
        classPropChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNELS[6]);
        classPropChannel.register(handler);
        toggleChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNELS[7]);
        toggleChannel.register(handler);
        levelChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNELS[8]);
        levelChannel.register(handler);
        levelReturn = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNELS[9]);
        levelReturn.register(handler);
        MinecraftForge.EVENT_BUS.register(handler);
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent evt) {
        ByteBuf in = evt.getPacket().payload();
        EntityPlayerMP player = ((NetHandlerPlayServer)evt.getHandler()).player;
        if (evt.getPacket().channel().equals(CHANNELS[1])) {
            addTask(evt.getHandler(), () -> handleClassChange(in, player));
        } else if (evt.getPacket().channel().equals(CHANNELS[2])) {
            addTask(evt.getHandler(), () -> handlePacket(in, player));
        } else if (evt.getPacket().channel().equals(CHANNELS[7])) {
            addTask(evt.getHandler(), () -> toggleActive(player));
        } else if (evt.getPacket().channel().equals(CHANNELS[8])) {
            addTask(evt.getHandler(), () -> addSkillLevel(in, player));
        }
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent evt) {
        ByteBuf in = evt.getPacket().payload();
        if (evt.getPacket().channel().equals(CHANNELS[0])) {
            addTask(evt.getHandler(), () -> handleSkillsPacket(in, LevelUp2.proxy.getPlayer()));
        } else if (evt.getPacket().channel().equals(CHANNELS[3])) {
            addTask(evt.getHandler(), () -> handleConfig(in));
        } else if (evt.getPacket().channel().equals(CHANNELS[4])) {
            addTask(evt.getHandler(), () -> handleProperties(in));
        } else if (evt.getPacket().channel().equals(CHANNELS[5]))
            addTask(evt.getHandler(), () -> refreshValues());
        else if (evt.getPacket().channel().equals(CHANNELS[6]))
            addTask(evt.getHandler(), () -> handleClassProps(in));
        else if (evt.getPacket().channel().equals(CHANNELS[9]))
            addTask(evt.getHandler(), () -> handleLevelPacket(in, LevelUp2.proxy.getPlayer()));
    }

    private void addTask(INetHandler netHandler, Runnable runnable) {
        FMLCommonHandler.instance().getWorldThread(netHandler).addScheduledTask(runnable);
    }

    private void addSkillLevel(ByteBuf buf, EntityPlayerMP player) {
        int level = buf.readInt();
        if (level == -1) {
            if (SkillRegistry.getPlayer(player).addLevelFromExperience(player)) {
                sendReturnPacket(player);
            }
        } else {
            SkillRegistry.getPlayer(player).changeLevelBank(level);
            sendReturnPacket(player);
        }
    }

    private void sendReturnPacket(EntityPlayerMP player) {
        levelReturn.sendTo(getLevelPacket(player), player);
    }

    private FMLProxyPacket getLevelPacket(EntityPlayerMP player) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(SkillRegistry.getPlayer(player).getLevelBank());
        FMLProxyPacket pkt = new FMLProxyPacket(new PacketBuffer(buf), CHANNELS[9]);
        pkt.setTarget(Side.CLIENT);
        return pkt;
    }

    private void handleLevelPacket(ByteBuf buf, EntityPlayer player) {
        SkillRegistry.getPlayer(player).changeLevelBank(buf.readInt());
    }

    public static FMLProxyPacket getLevelUpPacket(int level) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(level);
        FMLProxyPacket pkt = new FMLProxyPacket(new PacketBuffer(buf), CHANNELS[8]);
        pkt.setTarget(Side.SERVER);
        return pkt;
    }

    public static FMLProxyPacket getActivationPacket() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBoolean(false);
        FMLProxyPacket pkt = new FMLProxyPacket(new PacketBuffer(buf), CHANNELS[7]);
        pkt.setTarget(Side.SERVER);
        return pkt;
    }

    private void toggleActive(EntityPlayerMP player) {
        SkillRegistry.getPlayer(player).toggleActive();
        String active = SkillRegistry.getPlayer(player).isActive() ? "levelup.skill.active" : "levelup.skill.inactive";
        player.sendStatusMessage(new TextComponentTranslation(active), true);
    }

    public static FMLProxyPacket getClassChangePacket(ResourceLocation name, boolean reclass) {
        ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeUTF8String(buf, name.toString());
        buf.writeBoolean(reclass);
        FMLProxyPacket pkt = new FMLProxyPacket(new PacketBuffer(buf), CHANNELS[1]);
        pkt.setTarget(Side.SERVER);
        return pkt;
    }

    private void handleClassChange(ByteBuf buf, EntityPlayerMP player) {
        ResourceLocation cl = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
        boolean reclass = buf.readBoolean();
        if (SkillRegistry.getPlayer(player).getPlayerClass() == null || (reclass && !SkillRegistry.getPlayer(player).getPlayerClass().equals(cl))) {
            SkillRegistry.getPlayer(player).setPlayerClass(cl);
            SkillRegistry.loadPlayer(player);
            if (reclass && !player.capabilities.isCreativeMode) {
                if (player.inventory.mainInventory.get(player.inventory.currentItem).getItem() instanceof ItemRespecBook) {
                    player.inventory.mainInventory.set(player.inventory.currentItem, ItemStack.EMPTY);
                } else {
                    for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                        if (player.inventory.mainInventory.get(i).getItem() instanceof ItemRespecBook) {
                            player.inventory.mainInventory.set(i, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void handlePacket(ByteBuf buf, EntityPlayer player) {
        handleSkillsPacket(buf, player);
        SkillRegistry.loadPlayer(player);
    }

    public static FMLProxyPacket getSkillPacket(Side side, int channel, Map<ResourceLocation, Integer> map, int levels, ResourceLocation cl) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(levels);
        NBTTagCompound tag = writeSkillsAsNBT(map);
        if (cl != null && channel == 0)
            tag.setString("class", cl.toString());
        ByteBufUtils.writeTag(buf, tag);
        FMLProxyPacket pkt = new FMLProxyPacket(new PacketBuffer(buf), CHANNELS[channel]);
        pkt.setTarget(side);
        return pkt;
    }

    public static FMLProxyPacket getConfigPacket(NBTTagCompound dat) {
        ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeTag(buf, dat);
        FMLProxyPacket pkt = new FMLProxyPacket(new PacketBuffer(buf), CHANNELS[3]);
        pkt.setTarget(Side.CLIENT);
        return pkt;
    }

    private void handleConfig(ByteBuf buf) {
        LevelUpConfig.useServerProperties(ByteBufUtils.readTag(buf));
    }

    public static FMLProxyPacket getPropertyPackets(IPlayerSkill skill) {
        SkillProperties prop = SkillRegistry.getProperty(skill);
        ByteBuf buf = Unpooled.buffer();
        if (prop != null) {
            prop.writeToBytes(buf);
        }
        FMLProxyPacket pkt = new FMLProxyPacket(new PacketBuffer(buf), CHANNELS[4]);
        pkt.setTarget(Side.CLIENT);
        return pkt;
    }

    public static FMLProxyPacket getClassPackets(ICharacterClass cl) {
        ClassProperties prop = SkillRegistry.getProperty(cl);
        ByteBuf buf = Unpooled.buffer();
        if (prop != null)
            prop.writeToBytes(buf);
        FMLProxyPacket pkt = new FMLProxyPacket(new PacketBuffer(buf), CHANNELS[6]);
        pkt.setTarget(Side.CLIENT);
        return pkt;
    }

    private void handleProperties(ByteBuf buf) {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        SkillProperties prop = SkillProperties.fromNBT(tag);
        SkillRegistry.addSkill(BaseSkill.fromProps(prop));
    }

    private void handleClassProps(ByteBuf buf) {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        ClassProperties prop = ClassProperties.fromNBT(tag);
        SkillRegistry.addClass(BaseClass.fromProperties(prop));
    }

    public static FMLProxyPacket getRefreshPacket() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeBoolean(true);
        FMLProxyPacket pkt = new FMLProxyPacket(new PacketBuffer(buf), CHANNELS[5]);
        pkt.setTarget(Side.CLIENT);
        return pkt;
    }

    private void refreshValues() {
        SkillRegistry.calculateHighLow();
    }

    private void handleSkillsPacket(ByteBuf buf, EntityPlayer player) {
        IPlayerClass p = SkillRegistry.getPlayer(player);
        p.changeLevelBank(buf.readInt());
        Map<ResourceLocation, Integer> skills = readSkillsFromNBT(player, ByteBufUtils.readTag(buf));
        if (!skills.isEmpty()) {
            for (ResourceLocation name : skills.keySet()) {
                p.setSkillLevel(name, skills.get(name));
            }
        }
    }

    private static NBTTagCompound writeSkillsAsNBT(Map<ResourceLocation, Integer> map) {
        NBTTagCompound tag = new NBTTagCompound();
        if (!map.isEmpty()) {
            for (ResourceLocation name : map.keySet()) {
                tag.setInteger(name.toString(), map.get(name));
            }
        }
        return tag;
    }

    private Map<ResourceLocation, Integer> readSkillsFromNBT(EntityPlayer player, NBTTagCompound tag) {
        Map<ResourceLocation, Integer> skills = Maps.newHashMap();
        IPlayerClass cl = SkillRegistry.getPlayer(player);
        for (ResourceLocation name : cl.getSkills().keySet()) {
            if (tag.hasKey(name.toString())) {
                skills.put(name, tag.getInteger(name.toString()));
            }
        }
        if (tag.hasKey("class")) {
            SkillRegistry.getPlayer(player).setPlayerClass(new ResourceLocation(tag.getString("class")));
        }
        return skills;
    }
}

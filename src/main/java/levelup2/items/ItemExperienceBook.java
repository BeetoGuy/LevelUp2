package levelup2.items;

import com.google.common.collect.Maps;
import levelup2.LevelUp2;
import levelup2.network.SkillPacketHandler;
import levelup2.skills.SkillRegistry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemExperienceBook extends Item {
    public ItemExperienceBook() {
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!player.isSneaking() && world.isRemote) {
            LevelUp2.proxy.displayGuiForPlayer(player);
        } else if (player.isSneaking() && !world.isRemote) {
            if (SkillRegistry.getPlayer(player).addLevelFromExperience(player)) {
                FMLProxyPacket pkt = SkillPacketHandler.getSkillPacket(Side.CLIENT, 0, Maps.newHashMap(), SkillRegistry.getPlayer(player).getLevelBank(), null);
                SkillPacketHandler.initChannel.sendTo(pkt, (EntityPlayerMP)player);
            }
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.translateToLocal("levelup.howto.getlevels.0"));
        tooltip.add(I18n.translateToLocal("levelup.howto.getlevels.1"));
    }
}

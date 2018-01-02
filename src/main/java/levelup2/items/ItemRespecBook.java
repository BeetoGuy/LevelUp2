package levelup2.items;

import levelup2.config.LevelUpConfig;
import levelup2.gui.GuiSpecialization;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemRespecBook extends Item {
    public ItemRespecBook() {
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (world.isRemote) {
            if (player.experienceLevel >= LevelUpConfig.reclassCost) {
                Minecraft.getMinecraft().displayGuiScreen(GuiSpecialization.withRespec());
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            } else {
                player.sendStatusMessage(new TextComponentTranslation("levelup.respec.lowlevel", LevelUpConfig.reclassCost), true);
            }
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }
}

package levelup2.items;

import levelup2.skills.SkillRegistry;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemOreChunk extends Item {
    private List<String> oreTypes;

    public ItemOreChunk(List<String> oreTypes) {
        super();
        setHasSubtypes(true);
        setCreativeTab(CreativeTabs.MATERIALS);
        this.oreTypes = oreTypes;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> list) {
        if (!oreTypes.get(0).equals("null")) {
            for (int i = 0; i < oreTypes.size(); i++)
                list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (!oreTypes.get(0).equals("null")) {
            return I18n.format("item.levelup:orechunk.name", getOreName(stack));
        }
        return I18n.format("item.levelup:orechunk_null.name");
    }

    private String getOreName(ItemStack stack) {
        int meta = stack.getMetadata();
        if (meta < oreTypes.size()) {
            ItemStack check = SkillRegistry.getOreEntry(oreTypes.get(meta));
            if (!check.isEmpty()) {
                return I18n.format(check.getUnlocalizedName() + ".name");
            }
        }
        return "Ore";
    }
}

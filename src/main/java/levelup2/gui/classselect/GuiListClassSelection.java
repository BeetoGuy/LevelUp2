package levelup2.gui.classselect;

import com.google.common.collect.Lists;
import levelup2.api.ICharacterClass;
import levelup2.skills.SkillRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class GuiListClassSelection extends GuiListExtended {
    private final GuiClassSelect classSelect;
    private final List<ClassSelectionEntry> entries = Lists.newArrayList();
    private int selected = -1;

    public GuiListClassSelection(GuiClassSelect parent, Minecraft client, int width, int height, int top, int bottom, int slotHeight) {
        super(client, width, height, top, bottom, slotHeight);
        classSelect = parent;
        this.refreshList();
    }

    @Override
    public ClassSelectionEntry getListEntry(int selected) {
        return entries.get(selected);
    }

    @Override
    protected int getSize() {
        return entries.size();
    }

    private void refreshList() {
        List<ICharacterClass> list = Lists.newArrayList();
        for (ResourceLocation loc : SkillRegistry.getClasses().keySet()) {
            list.add(SkillRegistry.getClassFromName(loc));
        }
        for (ICharacterClass cl : list) {
            entries.add(new ClassSelectionEntry(this, this.mc, cl));
        }
    }

    public void selectClass(int index) {
        selected = index;
        classSelect.selectClass(getSelectedClass());
    }

    public ClassSelectionEntry getSelectedClass() {
        return selected >= 0 && selected < getSize() ? entries.get(selected) : null;
    }
}

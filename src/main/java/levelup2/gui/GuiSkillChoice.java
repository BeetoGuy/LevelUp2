package levelup2.gui;

import levelup2.api.IPlayerSkill;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import java.util.Map;

public class GuiSkillChoice extends GuiScreen {
    private IPlayerSkill skill;
    private GuiSkills parent;
    private int skillLevel;
    private boolean canSpendLevels;
    private boolean canDowngrade;
    public GuiSkillChoice (IPlayerSkill skill, int skillLevel, GuiSkills parent) {
        this.skill = skill;
        this.parent = parent;
        this.skillLevel = skillLevel;
        canDowngrade = parent.player.getSkillLevel(skill.getSkillName()) < skillLevel && isDowngradable(skill, parent);
        canSpendLevels = skill.getLevelCost(skillLevel) <= parent.availableLevels - parent.levelSpend && skill.getLevelCost(skillLevel) > -1 && parent.canUnlock(skill);
    }

    private boolean isDowngradable(IPlayerSkill skill, GuiSkills parent) {
        Map<String, Integer> skillList = parent.skills;
        for (String st : skillList.keySet()) {
            IPlayerSkill check = parent.player.getSkillFromName(st);
            if (skillList.get(check.getSkillName()) > 0 && check.getPrerequisites() != null && check.getPrerequisites().length > 0) {
                for (String checkStr : check.getPrerequisites()) {
                    if (checkStr.equals(skill.getSkillName()) && skillList.get(skill.getSkillName()) - 1 == 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        GuiButton button = new GuiButton(0, this.width / 2 - 65, this.height / 2 + 35, 130, 20, I18n.format("levelup.skill.increase"));
        button.enabled = canSpendLevels;
        this.buttonList.add(button);
        button = new GuiButton(1, this.width / 2 - 65, this.height / 2 + 60, 130, 20, I18n.format("levelup.skill.decrease"));
        button.enabled = canDowngrade;
        this.buttonList.add(button);
        this.buttonList.add(new GuiButton(2, this.width / 2 - 65, this.height / 2 + 85, 130, 20, I18n.format("gui.cancel")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawCenteredString(I18n.format("skill." + this.skill.getSkillName() + ".name"), this.width / 2, this.height / 2 - 100, -8355712);
        drawCenteredString(I18n.format(this.skill.getSkillName() + ".desc"), this.width / 2, this.height / 2 - 82, -8355712);
        int lineIncrement = 0;
        if (!I18n.format(this.skill.getSkillName() + ".desc.1").equals(this.skill.getSkillName() + ".desc.1")) {
            lineIncrement++;
            drawCenteredString(I18n.format(this.skill.getSkillName() + ".desc.1"), this.width / 2, this.height / 2 - 64, -8355712);
            if (!I18n.format(this.skill.getSkillName() + ".desc.2").equals(this.skill.getSkillName() + ".desc.2")) {
                lineIncrement++;
                drawCenteredString(I18n.format(this.skill.getSkillName() + ".desc.2"), this.width / 2, this.height / 2 - 46, -8355712);
            }
        }
        if (this.skill.getLevelCost(skillLevel) > -1) {
            int offset = 64 - (18 * lineIncrement);
            drawCenteredString(I18n.format("levelup.cost", this.skill.getLevelCost(skillLevel)), this.width / 2, this.height / 2 - offset, -8355712);
        }
        drawCenteredString(I18n.format("levelup.levels.track", skillLevel, parent.player.getSkillLevel(skill.getSkillName())), this.width / 2, this.height / 2 + 15, -8355712);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            changeSkillLevel(1);
            parent.levelSpend += this.skill.getLevelCost(skillLevel);
        } else if (button.id == 1) {
            changeSkillLevel(-1);
            parent.levelSpend -= this.skill.getLevelCost(skillLevel - 1);
        }
        mc.displayGuiScreen(parent);
    }

    private void drawCenteredString(String str, int posX, int posY, int color) {
        int centerWidth = this.fontRenderer.getStringWidth(str) / 2;
        this.fontRenderer.drawString(str, posX - centerWidth, posY, color);
    }

    private void changeSkillLevel(int add) {
        int orig = parent.skills.get(skill.getSkillName());
        parent.skills.put(skill.getSkillName(), orig + add);
    }
}

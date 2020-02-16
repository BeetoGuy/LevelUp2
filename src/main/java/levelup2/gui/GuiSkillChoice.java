package levelup2.gui;

import levelup2.api.ICharacterClass;
import levelup2.api.IPlayerSkill;
import levelup2.network.SkillPacketHandler;
import levelup2.skills.SkillRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class GuiSkillChoice extends GuiScreen {
    private IPlayerSkill skill;
    private ICharacterClass cl;
    private GuiSkills parent;
    private int skillLevel;
    private boolean canSpendLevels;
    private boolean canDowngrade;
    public GuiSkillChoice (IPlayerSkill skill, int skillLevel, GuiSkills parent) {
        this.skill = skill;
        this.parent = parent;
        cl = SkillRegistry.getClassFromName(parent.player.getPlayerClass());
        this.skillLevel = skillLevel;
        canDowngrade = isDowngradable(skill, parent);
        canSpendLevels = skill.getLevelCost(skillLevel) <= parent.player.getLevelBank() && skill.getLevelCost(skillLevel) > -1 && parent.canUnlock(skill);
    }

    private boolean isDowngradable(IPlayerSkill skill, GuiSkills parent) {
        if (parent.skillTrees.contains(skill.getSkillName())) return false;
        Map<ResourceLocation, Integer> skillList = parent.skills;
        for (ResourceLocation resource : skillList.keySet()) {
            IPlayerSkill check = parent.player.getSkillFromName(resource);
            int skillLevel = cl != null ? cl.getBonusSkillLevel(skill) : 0;
            if (skillList.get(check.getSkillName()) > skillLevel && check.getPrerequisites() != null && check.getPrerequisites().length > 0) {
                for (ResourceLocation checkStr : check.getPrerequisites()) {
                    if (checkStr.equals(skill.getSkillName()) && skillList.get(skill.getSkillName()) - 1 == 0) {
                        return false;
                    }
                }
            }
        }
        return skillLevel > cl.getBonusSkillLevel(skill);
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
        if (this.skill.isActive()) {
            String line = I18n.format(this.skill.getSkillName() + ".desc");
            drawCenteredString(checkFormatting(line), this.width / 2, this.height / 2 - 82, -8355712);
            int lineIncrement = 0;
            line = I18n.format(this.skill.getSkillName() + ".desc.1");
            if (!line.equals(this.skill.getSkillName() + ".desc.1")) {
                lineIncrement++;
                drawCenteredString(checkFormatting(line), this.width / 2, this.height / 2 - 64, -8355712);
                line = I18n.format(this.skill.getSkillName() + ".desc.2");
                if (!line.equals(this.skill.getSkillName() + ".desc.2")) {
                    lineIncrement++;
                    drawCenteredString(checkFormatting(line), this.width / 2, this.height / 2 - 46, -8355712);
                }
            }
            if (this.skill.getLevelCost(skillLevel) > -1) {
                int offset = 64 - (18 * lineIncrement);
                drawCenteredString(I18n.format("levelup.cost", this.skill.getLevelCost(skillLevel)), this.width / 2, this.height / 2 - offset, -8355712);
            }
        } else {
            drawCenteredString(I18n.format("skill.inactive"), this.width / 2, this.height / 2 - 82, -8355712);
            if (this.skill.getLevelCost(skillLevel) > -1) {
                drawCenteredString(I18n.format("levelup.cost", this.skill.getLevelCost(skillLevel)), this.width / 2, this.height / 2 - 64, -8355712);
            }
        }
        drawCenteredString(I18n.format("levelup.levels.track", skillLevel, parent.player.getSkillLevel(skill.getSkillName(), false)), this.width / 2, this.height / 2 + 15, -8355712);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String checkFormatting(String str) {
        if (str.startsWith("Format error: ")) {
            str = str.replace("Format error: ", "");
        }
        return str;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        int levels = parent.player.getLevelBank();
        if (button.id == 0) {
            changeSkillLevel(1);
            //parent.player.changeLevelBank(levels - this.skill.getLevelCost(skillLevel));
            SkillPacketHandler.levelChannel.sendToServer(SkillPacketHandler.getLevelUpPacket(levels - this.skill.getLevelCost(skillLevel)));
            //parent.availableLevels -= this.skill.getLevelCost(skillLevel);
        } else if (button.id == 1) {
            changeSkillLevel(-1);
            //parent.player.changeLevelBank(levels + this.skill.getLevelCost(skillLevel - 1));
            SkillPacketHandler.levelChannel.sendToServer(SkillPacketHandler.getLevelUpPacket(levels + this.skill.getLevelCost(skillLevel - 1)));
            //parent.availableLevels += this.skill.getLevelCost(skillLevel - 1);
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

package levelup2.util;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import levelup2.api.IPlayerSkill;
import levelup2.api.PlayerSkillStorage;
import levelup2.skills.SkillRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.List;

public class ClassProperties {
    private ResourceLocation className;
    private ResourceLocation specSkill;
    private List<BonusSkill> bonusSkills;
    private String localizedName;
    private String description;

    public static ClassProperties fromJson(JsonObject obj) {
        ResourceLocation name = new ResourceLocation(JsonUtils.getString(obj, "name"));
        ResourceLocation bonus = new ResourceLocation(JsonUtils.getString(obj, "bonus"));
        List<BonusSkill> skills = Lists.newArrayList();
        if (obj.has("skills")) {
            for (JsonElement json : JsonUtils.getJsonArray(obj, "skills")) {
                JsonObject o = json.getAsJsonObject();
                skills.add(new BonusSkill(new ResourceLocation(JsonUtils.getString(o, "name")), JsonUtils.getInt(o, "level", 1)));
            }
        }
        String locName = JsonUtils.getString(obj, "localized_name", "");
        String description = JsonUtils.getString(obj, "description", "");
        return new ClassProperties(name, bonus, skills, locName, description);
    }

    public ClassProperties(ResourceLocation name, ResourceLocation spec, List<BonusSkill> skills, String localizedName, String description) {
        className = name;
        specSkill = spec;
        bonusSkills = skills;
        this.localizedName = localizedName;
        this.description = description;
    }

    public ResourceLocation getClassName() {
        return className;
    }

    public ResourceLocation getSpecSkill() {
        return specSkill;
    }

    public List<PlayerSkillStorage> getBonusSkills() {
        List<PlayerSkillStorage> storage = Lists.newArrayList();
        for (BonusSkill skill : bonusSkills) {
            IPlayerSkill sk = SkillRegistry.getSkillFromName(skill.getSkill());
            if (sk != null) {
                storage.add(new PlayerSkillStorage(sk, skill.getPoints()));
            }
        }
        return storage;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public String getDescription() {
        return description;
    }

    public void writeToBytes(ByteBuf buf) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Class", className.toString());
        tag.setString("Spec", specSkill.toString());
        if (!bonusSkills.isEmpty()) {
            NBTTagCompound bonus = new NBTTagCompound();
            bonus.setInteger("Size", bonusSkills.size());
            for (int i = 0; i < bonusSkills.size(); i++) {
                BonusSkill b = bonusSkills.get(i);
                bonus.setString("name_" + i, b.getSkill().toString());
                bonus.setInteger("level_" + i, b.getPoints());
            }
            tag.setTag("Bonus", bonus);
        }
        tag.setString("LocName", localizedName);
        tag.setString("Desc", description);
        ByteBufUtils.writeTag(buf, tag);
    }

    public static ClassProperties fromNBT(NBTTagCompound tag) {
        ResourceLocation name = new ResourceLocation(tag.getString("Class"));
        ResourceLocation spec = new ResourceLocation(tag.getString("Spec"));
        List<BonusSkill> bSkills = Lists.newArrayList();
        if (tag.hasKey("Bonus")) {
            NBTTagCompound bonus = tag.getCompoundTag("Bonus");
            for (int i = 0; i < bonus.getInteger("Size"); i++) {
                ResourceLocation bName = new ResourceLocation(bonus.getString("name_" + i));
                int level = bonus.getInteger("level_" + i);
                bSkills.add(new BonusSkill(bName, level));
            }
        }
        String locName = tag.getString("LocName");
        String desc = tag.getString("Desc");
        return new ClassProperties(name, spec, bSkills, locName, desc);
    }

    private static class BonusSkill {
        private ResourceLocation skill;
        private int points;

        public BonusSkill(ResourceLocation skill, int points) {
            this.skill = skill;
            this.points = points;
        }

        public ResourceLocation getSkill() {
            return skill;
        }

        public int getPoints() {
            return points;
        }
    }
}

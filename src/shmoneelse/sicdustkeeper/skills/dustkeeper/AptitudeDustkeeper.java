package shmoneelse.sicdustkeeper.skills.dustkeeper;


import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import second_in_command.SCData;
import second_in_command.specs.SCAptitudeSection;
import second_in_command.specs.SCBaseAptitudePlugin;

import java.util.Objects;

public class AptitudeDustkeeper extends SCBaseAptitudePlugin {
    @Override
    public String getOriginSkillId() {
        return "dustkeeper_humanwarmind";
    }

    @Override
    public void createSections() {
        SCAptitudeSection section1 = new SCAptitudeSection(true, 0, "technology1");
        section1.addSkill("dustkeeper_optimization");
        section1.addSkill("dustkeeper_ew");
        section1.addSkill("dustkeeper_specialized_construction");
        section1.addSkill("dustkeeper_cooperation");
        addSection(section1);

        SCAptitudeSection section2 = new SCAptitudeSection(true, 2, "technology3");
        section2.addSkill("dustkeeper_network");
        section2.addSkill("dustkeeper_deadly");
        section2.addSkill("dustkeeper_wide");
        addSection(section2);

        SCAptitudeSection section3 = new SCAptitudeSection(false, 4, "technology4");
        section3.addSkill("dustkeeper_cyberwarfare_training");
        section3.addSkill("dustkeeper_escort");
        addSection(section3);

    }

    @Override
    public Float getNPCFleetSpawnWeight(SCData scData, CampaignFleetAPI campaignFleetAPI) {
        if(Objects.equals(campaignFleetAPI.getFaction().getId(), "sotf_dustkeepers")) return Float.MAX_VALUE;
        return 0f;
    }
}

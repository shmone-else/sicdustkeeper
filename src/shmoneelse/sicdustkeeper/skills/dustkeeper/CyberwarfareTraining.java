package shmoneelse.sicdustkeeper.skills.dustkeeper;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.OfficerLevelupPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

import java.util.ArrayList;
import java.util.List;

public class CyberwarfareTraining extends SCBaseSkillPlugin{

    @Override
    public String getAffectsString() {
        return "all AI cores";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("Teaches AI cores of level 5 or higher the Cyberwarfare Protocols skill", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("Increases the level of AI cores that already have Cyberwarfare Protocols by 1", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("   - If this officer is unassigned, the level is reduced back to the default and the skill is unlearned", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("   - If the core has more skills than possible at that level, it removes them automatically.", 0f, Misc.getTextColor(), Misc.getHighlightColor());
    }

    @Override
    public void advance(SCData data, Float amount)
    {
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) return;
        if (data.isNPC()) return;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        List<FleetMemberAPI> fleetList = fleet.getMembersWithFightersCopy();

        for (FleetMemberAPI member : fleetList) {
            if(member.getCaptain() == null) continue;
            if(!member.getCaptain().isAICore()) continue;
            if(member.getCaptain().getMemoryWithoutUpdate().getBoolean("$CyberwarfareTrainingLevel") || member.getCaptain().getMemoryWithoutUpdate().getBoolean("$CyberwarfareTrainingSkill")) continue;

            if(member.getCaptain().getStats().hasSkill("sotf_cyberwarfare"))
            {
                member.getCaptain().getStats().setLevel(member.getCaptain().getStats().getLevel()+1);
                member.getCaptain().getMemoryWithoutUpdate().set("$CyberwarfareTrainingLevel", true);
            }
            else if(member.getCaptain().getStats().getLevel() >= 5)
            {
                member.getCaptain().getStats().setLevel(member.getCaptain().getStats().getLevel()+1); // Accomodate the bonus skill
                member.getCaptain().getStats().setSkillLevel("sotf_cyberwarfare", 1f);
                member.getCaptain().getMemoryWithoutUpdate().set("$CyberwarfareTrainingSkill", true);
            }
        }
    }

    @Override
    public void onActivation(SCData data) {
        if(data.isNPC()) { // NPCs only
            for (FleetMemberAPI fleet : data.getFleet().getFleetData().getMembersListCopy())
            {
                if(fleet.getCaptain() == null) continue;
                if(!fleet.getCaptain().isAICore()) continue; // AI cores only
                if(Global.getSector().getImportantPeople().containsPerson(fleet.getCaptain())) continue; // Also not IBB bounties
                if(fleet.getCaptain().hasTag("cyberwarfare_update")) continue; // Don't duplicate

                fleet.getCaptain().addTag("cyberwarfare_update");

                if(fleet.getCaptain().getStats().getLevel() >= 5)
                    fleet.getCaptain().getStats().setSkillLevel("sotf_cyberwarfare", 1f);
            }
        }
    }

    @Override
    public void onDeactivation(SCData data) {
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) return;
        if (data.isNPC()) return;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        List<FleetMemberAPI> fleetList = fleet.getMembersWithFightersCopy();

        for (FleetMemberAPI member : fleetList) {
            if(member.getCaptain() == null) continue;
            if(!member.getCaptain().isAICore()) continue;

            if(!member.getCaptain().getMemoryWithoutUpdate().getBoolean("$CyberwarfareTrainingLevel") && !member.getCaptain().getMemoryWithoutUpdate().getBoolean("$CyberwarfareTrainingSkill")) continue;

            if(member.getCaptain().getMemoryWithoutUpdate().getBoolean("$CyberwarfareTrainingLevel"))
            {
                member.getCaptain().getStats().setLevel(member.getCaptain().getStats().getLevel()-1);
                member.getCaptain().getMemoryWithoutUpdate().set("$CyberwarfareTrainingLevel", false);
            }
            else
            {
                member.getCaptain().getStats().setSkillLevel("sotf_cyberwarfare", 0f);
                member.getCaptain().getStats().setLevel(member.getCaptain().getStats().getLevel()-1);
                member.getCaptain().getMemoryWithoutUpdate().set("$CyberwarfareTrainingSkill", false);
            }

            List<MutableCharacterStatsAPI.SkillLevelAPI> skills = member.getCaptain().getStats().getSkillsCopy();
            for(MutableCharacterStatsAPI.SkillLevelAPI skill : new ArrayList<>(skills))
            {
                if(skill.getSkill().isAptitudeEffect() || member.getCaptain().getStats().getSkillLevel(skill.getSkill().getName()) == 0f)
                    skills.remove(skill);
            }

            if(skills.size() <= member.getCaptain().getStats().getLevel()) continue;

            for(MutableCharacterStatsAPI.SkillLevelAPI skill : new ArrayList<>(skills))
            {
                if(skill.getSkill().hasTag("npc_only") || skill.getSkill().hasTag("player_only") || skill.getSkill().hasTag("ai_core_only"))
                    skills.remove(skill);
            }

            member.getCaptain().getStats().setSkillLevel(skills.get(skills.size()-1).getSkill().getId(), 0f);

        }
    }

}

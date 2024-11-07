package shmoneelse.sicdustkeeper.skills.dustkeeper;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
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

    /*@Override
    public void applyEffectsAfterShipCreation(SCData data, ShipAPI ship, ShipVariantAPI variant, String id) { // Adds listener for our version of Cyberwarfare
        if (ship.getCaptain() == null) return;
        if (!ship.getCaptain().isAICore()) return;
        if (ship.getCaptain().getStats() == null) return;
        if (ship.getCaptain().getStats().hasSkill("sotf_cyberwarfare")) return; // Don't do anything if they already have the skill, that's covered under advance
        if (ship.isFighter()) return;
        if (ship.isStationModule()) return; // Doesn't apply to submodules or fighters either.

        ship.addListener(new SotfCyberwarfareNerfed.SotfCyberwarfareShipHackScriptNerfed(ship));
    } */

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("Grants AI cores an inferior version of the Cyberwarfare Protocols skill*", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("Increases the level of AI cores that already have Cyberwarfare Protocols by 1", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("   - If this officer is unassigned, the level is reduced back to the default and the skill is unlearned", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("   - If the core has more skills than possible at that level, it removes them automatically.", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addSpacer(10f);
        tooltipMakerAPI.addPara("* The Cyberwarfare Training skill has reduced chance intrusion effectiveness and a longer cooldown than normal", 0f, Misc.getGrayColor(), Misc.getHighlightColor());

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
            if(member.getCaptain().getMemoryWithoutUpdate().getBoolean("$CyberwarfareTraining")) continue;

            if(member.getCaptain().getStats().hasSkill("sotf_cyberwarfare"))
            {
                member.getCaptain().getStats().setLevel(member.getCaptain().getStats().getLevel()+1);
            }
            else
            {
                member.getCaptain().getStats().setLevel(member.getCaptain().getStats().getLevel()+1); // Accomodate the bonus skill
                member.getCaptain().getStats().setSkillLevel("sotf_cyberwarfare_nerfed", 1f);
            }

            member.getCaptain().getMemoryWithoutUpdate().set("$CyberwarfareTraining", true);

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

                if(fleet.getCaptain().getStats().hasSkill("sotf_cyberwarfare"))
                    ; // Currently not implemented, should fix... eventually...
                else
                    fleet.getCaptain().getStats().setSkillLevel("sotf_cyberwarfare_nerfed", 1f);
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

            if(!member.getCaptain().getMemoryWithoutUpdate().getBoolean("$CyberwarfareTraining")) continue;

            if(member.getCaptain().getStats().hasSkill("sotf_cyberwarfare"))
            {
                member.getCaptain().getStats().setLevel(member.getCaptain().getStats().getLevel()-1);
            }
            else
            {
                member.getCaptain().getStats().setSkillLevel("sotf_cyberwarfare_nerfed", 0f);
                member.getCaptain().getStats().setLevel(member.getCaptain().getStats().getLevel()-1);
            }

            member.getCaptain().getMemoryWithoutUpdate().set("$CyberwarfareTraining", false);

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

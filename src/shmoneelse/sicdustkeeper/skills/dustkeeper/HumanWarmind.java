package shmoneelse.sicdustkeeper.skills.dustkeeper;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import second_in_command.SCData;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import second_in_command.specs.SCBaseSkillPlugin;
import second_in_command.scripts.AutomatedShipsManager;
import second_in_command.skills.automated.SCBaseAutoPointsSkillPlugin;

public class HumanWarmind extends SCBaseAutoPointsSkillPlugin { // Now called Warmind Socialization

    @Override
    public int getProvidedPoints() {
        if(Global.getSector() == null) return 60;
        if(Global.getSector().getPlayerFleet() == null) return 60;
        if(Global.getSector().getPlayerFleet().getCargo() == null) return 60;
        if(Global.getSector().getPlayerFleet().getFleetData() == null) return 60; // Everyone loves null checks

        float crewSurplus = Global.getSector().getPlayerFleet().getCargo().getCrew() - Global.getSector().getPlayerFleet().getFleetData().getMinCrew();

        float retval = 60f;
        if(crewSurplus <= 0)
            ; // Do nothing, prevents negative adjustment
        else if(crewSurplus <= 500)
            retval += crewSurplus * .08f; // +40 points at 500 crew, scales linearly
        else if(crewSurplus <= 1500)
            retval += 40f + (crewSurplus - 500f) * .04f; // +80 points at 1500 crew, scaling factor doubles between 500-1500.
        else if(crewSurplus <= 3000)
            retval += 80 + (crewSurplus - 1500f) * 2f / 75f; // +120 points at 3000 crew, scaling factor doubles again
        else
            retval += 120f;

        return (int) retval; // Cast to int and return
    }

    @Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
        tooltip.addPara("Provides automated ship points that scale with surplus crew (maximum of 180)", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltip.addPara("Automated ship points start at %s, and gains an additional %s points at %s, %s and %s crew", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "60", "40", "500", "1500", "3000");
        tooltip.addPara("Bonus points scale linearly between breakpoints", 0f, Misc.getGrayColor(), Misc.getHighlightColor());

        super.addTooltip(data, tooltip);
    }

}

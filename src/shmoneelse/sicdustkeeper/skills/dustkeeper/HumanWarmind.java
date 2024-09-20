package shmoneelse.sicdustkeeper.skills.dustkeeper;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import second_in_command.SCData;
import com.fs.starfarer.api.util.Misc;

public class HumanWarmind extends DustkeeperBaseAutoPointsSkillPlugin { // Now called Warmind Socialization

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
            retval += crewSurplus * .06f; // +30 points at 500 crew, scales linearly
        else if(crewSurplus <= 1500)
            retval += 30f + (crewSurplus - 500f) * .03f; // +60 points at 1500 crew, scaling factor doubles between 500-1500.
        else if(crewSurplus <= 3000)
            retval += 60f + (crewSurplus - 1500f) * .02f; // +90 points at 3000 crew, scaling factor increases again
        else
            retval += 90f;

        return (int) retval; // Cast to int and return
    }

    @Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
        if(data.isPlayer())
        {
            tooltip.addPara("Provides automated ship points that scale with surplus crew (maximum of 150)", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
            tooltip.addPara("Automated ship points start at %s, and gains an additional %s points at %s, %s and %s crew*", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "60", "30", "500", "1500", "3000");
            tooltip.addPara("*Bonus points scale linearly between breakpoints", 0f, Misc.getGrayColor(), Misc.getHighlightColor());

            int provided = getProvidedPoints();

            tooltip.addPara("Currently provides +" + Integer.toString(provided) + " automated ship points", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        }

        super.addTooltip(data, tooltip);
    }

}

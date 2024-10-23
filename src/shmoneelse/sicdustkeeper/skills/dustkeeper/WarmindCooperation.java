package shmoneelse.sicdustkeeper.skills.dustkeeper;

import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.HullRestoration;
import com.fs.starfarer.api.ui.LabelAPI;
import second_in_command.specs.SCBaseSkillPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import second_in_command.SCData;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import java.awt.*;


public class WarmindCooperation extends SCBaseSkillPlugin{
    @Override
    public String getAffectsString() {
        return "all automated ships";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("Ships are almost always recoverable if lost in combat", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("Provides bonuses which scale with surplus crew", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());

        tooltipMakerAPI.addPara("   - Up to %s ship repair rate out of combat", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "+50%");
        tooltipMakerAPI.addPara("   - Ships without the %s hullmod lost in combat are up to %s less likely to acquire d-mods", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Rugged Construction", "66%");
        //tooltipMakerAPI.addPara("   - Other ships lost in combat are up to %s less likely to acquire d-mods", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "66%");
        tooltipMakerAPI.addPara("   - Bonuses scale linearly with surplus crew up to their maximum value at %s crew", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "3000");
    }


    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
        if(!Misc.isAutomated(stats)) return;

        if(Global.getSector() == null) return;
        if(Global.getSector().getPlayerFleet() == null) return;
        if(Global.getSector().getPlayerFleet().getCargo() == null) return;
        if(Global.getSector().getPlayerFleet().getFleetData() == null) return; // Everyone loves null checks

        stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, HullRestoration.RECOVERY_PROB);

        float crewSurplus = Global.getSector().getPlayerFleet().getCargo().getCrew() - Global.getSector().getPlayerFleet().getFleetData().getMinCrew();
        float effectStrength;

        if(crewSurplus <= 0) return; // If we're understrength the rest of the effects don't fire
        else if(crewSurplus <= 3000) effectStrength = crewSurplus / 3000f;
        else effectStrength = 1.0f; // Stops scaling at 3000 crew surplus


        float rBonus = 50f * effectStrength; // Max 50%
        float rDMod = 1f + effectStrength * 2f; // max 3
        float oDMod = 1f - effectStrength * .66f; // max .33

        stats.getRepairRatePercentPerDay().modifyPercent(id, rBonus);
        stats.getBaseCRRecoveryRatePercentPerDay().modifyPercent(id, rBonus);

        /*if (variant.getHullMods().contains("rugged"))  // If we're rugged, increase dmod chance
            stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, rDMod);
        else // Otherwise, decrease it
            stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, oDMod);*/
        if (!variant.getHullMods().contains("rugged"))
            stats.getDynamic().getMod(Stats.DMOD_ACQUIRE_PROB_MOD).modifyMult(id, oDMod);

    }

}

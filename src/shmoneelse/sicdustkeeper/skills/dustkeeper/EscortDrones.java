package shmoneelse.sicdustkeeper.skills.dustkeeper;

import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.HullRestoration;
import second_in_command.specs.SCBaseSkillPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import second_in_command.SCData;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;


public class EscortDrones extends SCBaseSkillPlugin{
    @Override
    public String getAffectsString() {
        return "all automated ships";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("+15%% Armor on ships with the Rugged Construction built-in Hullmod", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("+10%% Shield Efficiency on other ships", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
    }

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
        if(!Misc.isAutomated(stats)) return;
        //stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, HullRestoration.RECOVERY_PROB);
        if (variant.getHullMods().contains("rugged"))  // If we're rugged, boost armor
            stats.getArmorBonus().modifyMult(id, 1f + 15f / 100f);
        else
            stats.getShieldDamageTakenMult().modifyMult(id, 1f - 10f / 100f); // Otherwise, boost shields
    }

    @Override
    public void applyEffectsAfterShipCreation(SCData data, ShipAPI ship, ShipVariantAPI variant, String id) {
        if(!Misc.isAutomated(ship)) return;
        String sprite = Global.getSettings().getSpriteName("damage","dmod_overlay_none");
        ship.setDHullOverlay(sprite);
    }

}

package shmoneelse.sicdustkeeper.skills.dustkeeper;

import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.HullRestoration;
import com.fs.starfarer.combat.entities.Ship;
import second_in_command.specs.SCBaseSkillPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import second_in_command.SCData;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

public class SpreadWings extends SCBaseSkillPlugin {
    float PDBONUS = 100f;

    @Override
    public String getAffectsString() {
        return "all drone fighters";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("+100 point defense weapon range", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("+10%% weapon fire rate", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("+15%% top speed", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());

    }

    @Override
    public void applyEffectsToFighterSpawnedByShip(SCData data, ShipAPI fighter, ShipAPI ship, String id) {
        boolean isAlwaysValid = ship.getVariant().hasHullMod("SKR_remote") || ship.getVariant().hasHullMod("rat_autonomous_bays");
        if (isAlwaysValid || fighter.getHullSpec().getMinCrew() == 0) {
            MutableShipStatsAPI stats = fighter.getMutableStats();
            stats.getBeamPDWeaponRangeBonus().modifyFlat(id, PDBONUS);
            stats.getNonBeamPDWeaponRangeBonus().modifyFlat(id, PDBONUS);

            stats.getMaxSpeed().modifyPercent(id, 15f);
            stats.getAcceleration().modifyPercent(id, 15f * 2f);
            stats.getDeceleration().modifyPercent(id, 15f * 2f);

            stats.getBallisticRoFMult().modifyPercent(id, 10f);
            stats.getEnergyRoFMult().modifyPercent(id, 10f);
            stats.getMissileRoFMult().modifyPercent(id, 10f);
        }
    }
}

package shmoneelse.sicdustkeeper.skills.dustkeeper;

import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.HullRestoration;
import org.magiclib.subsystems.MagicSubsystemsManager;
import second_in_command.specs.SCBaseSkillPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import second_in_command.SCData;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import shmoneelse.sicdustkeeper.skills.dustkeeper.scripts.EscortDroneSubsystem;


public class EscortDrones extends SCBaseSkillPlugin{
    @Override
    public String getAffectsString() {
        return "all automated ships";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("Ships gain 0/1/2/3 escort Terminator Drones based on their hullsize", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("Drones will not respawn if destroyed", 0f, Misc.getGrayColor(), Misc.getHighlightColor());

    }

    @Override
    public void applyEffectsAfterShipCreation(SCData data, ShipAPI ship, ShipVariantAPI variant, String id) {
        if(!Misc.isAutomated(ship)) return;
        MagicSubsystemsManager.addSubsystemToShip(ship, new EscortDroneSubsystem(ship));
    }

}


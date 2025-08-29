package shmoneelse.sicdustkeeper.skills.dustkeeper

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.ids.Strings
import com.fs.starfarer.api.impl.campaign.skills.ElectronicWarfareScript
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import second_in_command.SCData
import second_in_command.specs.SCBaseSkillPlugin

// Originally by Lukas
// Modified version by shmone-else

class JammingStations : SCBaseSkillPlugin() {

    var PER_SHIP_BONUS = 2f
    var AUTOFIRE_BONUS = 40;

    override fun getAffectsString(): String {
        return "all ships with officers"
    }

    override fun addTooltip(data: SCData, tooltip: TooltipMakerAPI) {

        tooltip.addPara("Ships with officers contribute ${PER_SHIP_BONUS.toInt()}%% to the ECM rating* of the fleet", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("+$AUTOFIRE_BONUS%% target leading accuracy", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltip.addSpacer(10f)

        var max = ElectronicWarfareScript.BASE_MAXIMUM.toInt()

        tooltip.addPara("*Enemy weapon range is reduced by the total ECM rating of your deployed ships, " +
                "up to a maximum of $max%%. This penalty is reduced by the ratio " +
                "of the enemy ECM rating to yours." + "Does not apply to fighters, affects all weapons including missiles.", 0f, Misc.getGrayColor(), Misc.getHighlightColor(),
            "$max%")
    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI?, variant: ShipVariantAPI, hullSize: ShipAPI.HullSize?, id: String?) {
        val captain = stats!!.fleetMember?.captain ?: return;
        if(captain.isDefault()) return;

        stats!!.dynamic.getMod(Stats.ELECTRONIC_WARFARE_FLAT).modifyFlat(id, PER_SHIP_BONUS)
        stats.autofireAimAccuracy.modifyFlat(id, AUTOFIRE_BONUS * .01f)

    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI?, variant: ShipVariantAPI, id: String?) {
        if (Misc.isAutomated(ship)) {

        }
    }


}
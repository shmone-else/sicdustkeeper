package shmoneelse.sicdustkeeper.skills.dustkeeper.scripts;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.drones.DroneFormation;
import org.magiclib.subsystems.drones.HoveringFormation;
import org.magiclib.subsystems.drones.MagicDroneSubsystem;

public class EscortDroneSubsystem extends MagicDroneSubsystem {
    int numDrones = 0;

    public EscortDroneSubsystem(ShipAPI ship) {
        super(ship);

        if(!ship.isStationModule() && !ship.isFrigate() && !ship.isFighter()) {
            if (ship.isDestroyer())
                numDrones = 1;
            else if (ship.isCruiser())
                numDrones = 2;
            else if (ship.isCapital())
                numDrones = 3;
            else if (ship.isStation())
                numDrones = 4;
        }
        setDronesToSpawn(numDrones);
    }

    @Override
    public boolean canAssignKey() {
        return false;
    }

    @Override
    public float getBaseActiveDuration() {
        return 0;
    }

    @Override
    public float getBaseCooldownDuration() {
        return 2f;
    }

    @Override
    public boolean shouldActivateAI(float amount) {
        return canActivate();
    }

    /*@Override
    public float getBaseChargeRechargeDuration() {
        return 0f;
    }*/ // Not needed, identical to super

    @Override
    public boolean canActivate() {
        return false;
    }

    @Override
    public String getDisplayText() {
        return "Escort Drones";
    }

    @Override
    public String getStateText() {
        return "";
    }

    @Override
    public float getBarFill() {
        float fill = 0f;
        if (charges < calcMaxCharges()) {
            fill = chargeInterval.getElapsed() / chargeInterval.getIntervalDuration();
        }

        return fill;
    }

    @Override
    public int getMaxCharges() {
        return 0;
    }

    @Override
    public int getMaxDeployedDrones() {
        return numDrones;
    }

    @Override
    public boolean usesChargesOnActivate() {
        return false;
    }

    @Override
    public @NotNull String getDroneVariant() {
        return "sotf_terminator_single";
    }

    @Override
    public void drawHUDBar(@NotNull ViewportAPI viewport, @NotNull Vector2f rootLoc, @NotNull Vector2f barLoc, boolean displayAdditionalInfo, float longestNameWidth) {
        // We don't need the subsystem information to be shown
    }

    /*@Override
    public @NotNull DroneFormation getDroneFormation()
    {
        return new HoveringFormation();
    }*/
}

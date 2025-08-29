package shmoneelse.sicdustkeeper.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;

import java.util.HashMap;
import java.util.Random;

import com.fs.starfarer.api.characters.PersonAPI;
import data.scripts.campaign.ids.SotfIDs;
import data.scripts.utils.SotfMisc;
import second_in_command.specs.SCOfficer;

import shmoneelse.sicdustkeeper.scripts.sicdk_utils;

public class addXO implements EconomyTickListener {

    @Override
    public void reportEconomyTick(int iterIndex) {
    }



    @Override
    public void reportEconomyMonthEnd() {
        sicdk_utils.addToHoldout();
    }
}

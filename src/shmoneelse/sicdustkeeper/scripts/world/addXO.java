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

public class addXO implements EconomyTickListener {
    String prev;

    @Override
    public void reportEconomyTick(int iterIndex) {

    }

    @Override
    public void reportEconomyMonthEnd() {
        MarketAPI market = Global.getSector().getEconomy().getMarket("sotf_holdout_market");
        if(market == null) return; // If the market doesn't exist, just quit

        if(prev != null)
            market.getCommDirectory().removeEntry(prev); // Clear last month's XO
        prev = null;

        Random rand = new Random();
        if(rand.nextFloat() > .7f) return; // 70% chance to have XO in market each month

        // Make a random Dustkeeper
        PersonAPI person = market.getFaction().createRandomPerson(); // Random gender
        SotfMisc.dustkeeperifyAICore(person); // Proper Dustkeeper name for our XO
        person.setFaction(SotfIDs.DUSTKEEPERS);
        float pic = rand.nextFloat();
        if(pic < .25f)
            person.setPortraitSprite(Global.getSettings().getSpriteName("sotf_dustkeepers", "red"));
        else if (pic < .5f)
            person.setPortraitSprite(Global.getSettings().getSpriteName("sotf_dustkeepers", "yellow"));
        else if (pic < .75f)
            person.setPortraitSprite(Global.getSettings().getSpriteName("sotf_dustkeepers", "white"));
        else
            person.setPortraitSprite(Global.getSettings().getSpriteName("sotf_dustkeepers", "blue"));
        person.getMemoryWithoutUpdate().set("$sc_officer_aptitude","sc_dustkeeper");
        person.getMemoryWithoutUpdate().set("$sc_hireable", true);
        person.setPostId("executive_officer_sc_dustkeeper");

        prev = market.getCommDirectory().addPerson(person);
    }
}

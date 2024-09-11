package shmoneelse.sicdustkeeper;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import shmoneelse.sicdustkeeper.scripts.world.addXO;

public class sicdustkeeper extends BaseModPlugin {
    //@Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);

        if(!Global.getSector().getListenerManager().hasListenerOfClass(addXO.class))
            Global.getSector().getListenerManager().addListener(new addXO(), false);
    }

}

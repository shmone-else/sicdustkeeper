// OH, HOW IT ALL SPEAKS TO ME. Ship gains the ability to hack nearby enemies based on its ECM advantage
// LUDD ALMIGHTY HOW DID THIS SCRIPT GET SO LONG
// Same script is used for Cyberwarfare Protocols and Jubilant Tech-Siren
// Originally by Inventor Raccoon - implemented a nerfed version for the Dustkeeper XO

package shmoneelse.sicdustkeeper.skills.dustkeeper.scripts;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.AfterShipCreationSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.impl.combat.CRPluginImpl;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.campaign.ids.SotfIDs;
import data.scripts.combat.SotfAuraVisualScript;
import data.scripts.combat.SotfRingTimerVisualScript;
import org.magiclib.plugins.MagicFakeBeamPlugin;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.*;
import java.util.List;

public class SotfCyberwarfareNerfed {

    public static float RANGE = 900f;
    public static float HACK_COOLDOWN = 75f;
    public static float FIGHTER_HACK_COOLDOWN = 20f;
    public static float OVERRIDE_DURATION = 7.5f;
    public static float TSEQ_DAMAGE = 1.25f; // energy damage = fighter's max hull * this

    public static float BASE_ECM_RATING = 10f;
    //public static float CHANCE_PER_ECM_DIFF = 0.05f;
    public static float ECM_PENALTY = 3f;

    // each deducts 1 point of ECM rating
    public static Set<String> VULNERABLE_DMODS = new HashSet<>();
    static {
        VULNERABLE_DMODS.add("faulty_auto");
        VULNERABLE_DMODS.add(HullMods.ILL_ADVISED);
        VULNERABLE_DMODS.add(HullMods.GLITCHED_SENSORS);
        VULNERABLE_DMODS.add(HullMods.FRAGILE_SUBSYSTEMS);
        VULNERABLE_DMODS.add(HullMods.MALFUNCTIONING_COMMS);
    }

    //public static Color HACK_COLOR = new Color(85, 125, 255);
    public static Color HACK_COLOR = Global.getSettings().getSkillSpec(SotfIDs.SKILL_CYBERWARFARE).getGoverningAptitudeColor().brighter();

    public static String TRIED_CYBER_ON_OMEGA = "$sotf_triedCyberOnOmega";
    public static String TRIED_CYBER_ON_CONVICTION = "$sotf_triedCyberOnALConviction";

    public static String SHIP_HACK_KEY = "sotf_cyberwarfare_shiphack";
    public static String MINOR_HACK_KEY = "sotf_cyberwarfare_minorhack";
    public static String FIGHTER_HACK_KEY = "sotf_cyberwarfare_fighterhack";

    public static final String AURA_VISUAL_KEY = "sotf_cyberwarfare_auravisual";
    public static final String SHIP_HACK_CD_KEY = "sotf_cyberwarfare_shipcdvisual";
    public static final String FIGHTER_HACK_CD_KEY = "sotf_cyberwarfare_fightercdvisual";

    public static String DRONE_TSEQUENCE_KEY = "sotf_cyberwarfare_tseq";

    public static Map<ShipAPI.HullSize, Integer> VFX_BEAMS = new HashMap<ShipAPI.HullSize, Integer>();
    static {
        VFX_BEAMS.put(ShipAPI.HullSize.FIGHTER, 2);
        VFX_BEAMS.put(ShipAPI.HullSize.FRIGATE, 3);
        VFX_BEAMS.put(ShipAPI.HullSize.DESTROYER, 4);
        VFX_BEAMS.put(ShipAPI.HullSize.CRUISER, 5);
        VFX_BEAMS.put(ShipAPI.HullSize.CAPITAL_SHIP, 7);
    }
    public static float VFX_BEAM_FULL = 0.25f;
    public static float VFX_BEAM_FADEOUT = 0.5f;

    public static class Level1 extends BaseSkillEffectDescription implements AfterShipCreationSkillEffect {

        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            if (ship.isStationModule()) return;
            if (ship.isFighter()) return;
            ship.addListener(new SotfCyberwarfareShipHackScriptNerfed(ship));
        }

        public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
            if (ship.isStationModule()) return;
            if (ship.isFighter()) return;
            ship.removeListenerOfClass(SotfCyberwarfareShipHackScriptNerfed.class);
        }

        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {

        }
        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

        }

        public String getEffectDescription(float level) {
            return null;
        }

        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill,
                                            TooltipMakerAPI info, float width) {
            init(stats, skill);

            Color c = hc;
            info.addPara("Enables execution of cyberwarfare attacks on a targeted hostile ship every %s seconds",
                    0f, c, c, "" + (int) HACK_COOLDOWN);
            info.addPara("Base range of %s units, increased by modifiers to energy weapon range",
                    0f, c, c, "" + (int) RANGE);
            //info.addPara("Intrusion chance is %s modified by %s per 1 point difference in the ships' ECM ratings*", 0f, c, c,
            //        "" + (int) (BASE_HACK_CHANCE * 100f) + "%",
            //        "" + (int) (CHANCE_PER_ECM_DIFF * 100f) + "%");
            info.addPara("Successful intrusion disrupts the target's weapons, engines, defenses or ship system",
                    c, 0f);
            info.addPara("Ship's ECM advantage improves intrusion effectiveness*; intrusion can be mitigated by " +
                            "targets with ECM advantage or an ECCM package, to a minimum of 35% effectiveness and maximum of 200%",
                    c, 0f);
            if (Global.getSector() != null) {
                boolean haveDaemonShip = false;
                if (stats.getFleet() != null) {
                    for (FleetMemberAPI member : stats.getFleet().getMembersWithFightersCopy()) {
                        if (member.getVariant().hasHullMod("tahlan_daemoncore")) {
                            haveDaemonShip = true;
                        }
                    }
                }
                if (haveDaemonShip) {
                    info.addPara("Mitigates remote override attempts by Legio Infernalis command ships against piloted ship and nearby allies",
                            c, 0f);
                }
            }

            info.addPara("\n*Hack effectiveness is multiplied by the attacker's ECM score and divided " +
                    "by the defender's ECM score. Base ECM score is equal to %s, increased by 1 per %s of the ship's ECM rating, " +
                    "reduced by 3 for non-militarized civilian ships, reduced by 1 for d-mods " +
                    "affecting subsystems, sensors or comms. Due to inferior training, ships with " +
                    "this skill have their ECM score reduced by %s.", 0f, dtc, dtc, "" + (int) BASE_ECM_RATING, "1%", "" + (int) ECM_PENALTY);
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }

    public static class SotfCyberwarfareShipHackScriptNerfed implements AdvanceableListener {
        protected ShipAPI ship;
        protected float checkTimer = 0f;
        protected float internalCDTimer = HACK_COOLDOWN / 2f;
        protected int timesHacked = 0;

        public float getHackRange() {
            return RANGE * ship.getMutableStats().getEnergyWeaponRangeBonus().getBonusMult();
        }

        public float getHackCooldown() {
            return HACK_COOLDOWN * ship.getMutableStats().getDynamic().getValue(SotfIDs.STAT_CYBERWARFARE_COOLDOWN_MULT, 1f);
        }

        public float getHackPenetration() {
            return ship.getMutableStats().getDynamic().getValue(SotfIDs.STAT_CYBERWARFARE_PENETRATION_MULT, 1f);
        }

        public SotfCyberwarfareShipHackScriptNerfed(ShipAPI ship) {
            this.ship = ship;
            internalCDTimer = getHackCooldown() / 2;
        }

        public void advance(float amount) {
            if (!Global.getCurrentState().equals(GameState.COMBAT)) {
                return;
            }
            if (!ship.isAlive() || ship.isFighter() || ship.isStationModule()) {
                return;
            }

            // create AoE visual
            if (!ship.getCustomData().containsKey(AURA_VISUAL_KEY)) {
                SotfAuraVisualScript.AuraParams p = new SotfAuraVisualScript.AuraParams();
                p.color = Misc.setAlpha(HACK_COLOR, 125);
                p.ship = ship;
                p.radius = getHackRange();
                SotfAuraVisualScript plugin = new SotfAuraVisualScript(p);
                Global.getCombatEngine().addLayeredRenderingPlugin(plugin);
                ship.setCustomData(AURA_VISUAL_KEY, plugin);
            } else {
                SotfAuraVisualScript visual = (SotfAuraVisualScript) ship.getCustomData().get(AURA_VISUAL_KEY);
                visual.p.baseAlpha = 0.25f;
                visual.p.playerAlpha = 0.5f;
                visual.p.radius = getHackRange();
            }

            if (!ship.getCustomData().containsKey(SHIP_HACK_CD_KEY)) {
                SotfRingTimerVisualScript.AuraParams p = new SotfRingTimerVisualScript.AuraParams();
                p.color = HACK_COLOR;
                p.ship = ship;
                p.radius = ship.getShieldRadiusEvenIfNoShield() + 22f;
                p.thickness = 11f;
                p.baseAlpha = 0.4f;
                p.maxArc = 60f;
                //p.followFacing = true;
                p.renderDarkerCopy = true;
                if (ship == Global.getCombatEngine().getPlayerShip()) {
                    p.reverseRing = false;
                } else {
                    p.reverseRing = true;
                }
                p.degreeOffset = 15f;
                p.layer = CombatEngineLayers.JUST_BELOW_WIDGETS;
                SotfRingTimerVisualScript plugin = new SotfRingTimerVisualScript(p);
                Global.getCombatEngine().addLayeredRenderingPlugin(plugin);
                ship.setCustomData(SHIP_HACK_CD_KEY, plugin);
            } else {
                SotfRingTimerVisualScript visual = (SotfRingTimerVisualScript) ship.getCustomData().get(SHIP_HACK_CD_KEY);
                visual.p.totalArc = 1f - (internalCDTimer / getHackCooldown());
                if (internalCDTimer <= 0) {
                    visual.p.baseAlpha = 0.8f;
                }
                if (ship == Global.getCombatEngine().getPlayerShip()) {
                    visual.p.reverseRing = false;
                } else {
                    visual.p.reverseRing = true;
                }
            }

            boolean player = false;
            boolean targetInvalid = false;
            boolean playerTargetTooFar = false;
            ShipAPI playerTarget = null;

            boolean canCooldown = true;
            if (ship.getFluxTracker().isOverloaded()) {
                canCooldown = false;
            }

            float timeMult = (Global.getCombatEngine().getTimeMult().getModifiedValue() * ship.getMutableStats().getTimeMult().getModifiedValue());
            if (canCooldown) {
                internalCDTimer -= amount * timeMult;
            }
            if (internalCDTimer < 0) {
                internalCDTimer = 0;
            }
            // if player ship, ONLY hack their selected target
            if (ship == Global.getCombatEngine().getPlayerShip()) {
                player = true;
                playerTarget = ship.getShipTarget();
                if (playerTarget != null) {
                    targetInvalid = !isValidTarget(ship, playerTarget);
                    playerTargetTooFar = Misc.getDistance(ship.getLocation(), playerTarget.getLocation()) > getHackRange();
                }
            }
            if (ship == Global.getCombatEngine().getPlayerShip()) {
                String status = "";
                if (internalCDTimer <= 0f) {
                    if (playerTarget != null && targetInvalid) {
                        status = "Invalid target [R]";
                    } else if (playerTarget != null && playerTargetTooFar) {
                        status = "Target [R] out of range";
                    } else {
                        status = "Select target [R]";
                    }
                } else if (internalCDTimer <= 3) {
                    status = "Attempting network breach";
                } else if (timesHacked == 0) {
                    status = "Establishing connection - " + (int) internalCDTimer;
                } else {
                    status = "Simulating vulnerability - " + (int) internalCDTimer;
                }
                Global.getCombatEngine().maintainStatusForPlayerShip(SHIP_HACK_KEY, "graphics/icons/hullsys/entropy_amplifier.png", "Cyberwarfare - Ship Intrusion", status, false);
            }
            if (internalCDTimer > 0f || ship.isPhased() || ship.getFluxTracker().isOverloadedOrVenting()) {
                return;
            }
            ShipAPI target = null;
            if (player && playerTarget != null && !targetInvalid && !playerTargetTooFar) {
                target = playerTarget;
            } else if (player) {
                return;
            }
            else {
                target = findTarget(ship);
            }
            if (target != null) {
                executeHack(target);
                internalCDTimer = getHackCooldown();
                timesHacked++;
            }
        }

        public void executeHack(ShipAPI target) {
            WeightedRandomPicker<String> hackPicker = new WeightedRandomPicker<String>();
            if (target.getUsableWeapons().size() > 0) {
                hackPicker.add("weapons");
            }
            if (target.getEngineController().getShipEngines().size() > 0 && !target.getEngineController().isFlamedOut()) {
                float enginesWeight = 0.8f;
                // prioritise engine hacks against fleeing ships
                if (target.getShipAI() != null) {
                    if (target.getShipAI().getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF)) {
                        enginesWeight *= 2.5f;
                    }
                }
                // always hack engines against retreating targets
                if (target.isRetreating()) {
                    enginesWeight = 100f;
                }
                hackPicker.add("engines", enginesWeight);
            }
            if (target.getShield() != null || target.getPhaseCloak() != null) {
                float defenseWeight = 0.8f;
                // prioritise defense hacks against ships that are being attacked
                if (target.getShipAI() != null) {
                    if (target.getShipAI().getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.HAS_INCOMING_DAMAGE)) {
                        defenseWeight *= 2.5f;
                    }
                }
                hackPicker.add("defense", defenseWeight);
            }
            if (target.getSystem() != null) {
                float systemWeight = 1f;
                if (target.getSystem().isOutOfAmmo()) {
                    systemWeight = 0f;
                }
                hackPicker.add("system", systemWeight);
            }
            if (hackPicker.isEmpty()) {
                return;
            }
            String hackType = hackPicker.pick();

            float attackerRating = ship.getMutableStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_FLAT);
            float defenderRating = target.getMutableStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_FLAT);
            defenderRating = (defenderRating / getHackPenetration());

            attackerRating += BASE_ECM_RATING - ECM_PENALTY;
            defenderRating += BASE_ECM_RATING;

            float defenderECCM = target.getMutableStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_PENALTY_MULT);
            if (defenderECCM < 1) {
                defenderECCM = 1 - (defenderECCM / getHackPenetration());
            }
            if (defenderECCM > 1) {
                defenderECCM = 1;
            }

            // penalty for civ grade
            if (ship.getVariant().hasHullMod(HullMods.CIVGRADE) && !ship.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) {
                attackerRating -= 3f;
            }
            // penalty for subsystem dmods
            for (String hullMod : ship.getVariant().getHullMods()) {
                if (VULNERABLE_DMODS.contains(hullMod)) {
                    attackerRating--;
                }
            }
            // ditto for defenders
            if (target.getVariant().hasHullMod(HullMods.CIVGRADE) && !target.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) {
                defenderRating -= 3f;
            }
            for (String hullMod : target.getVariant().getHullMods()) {
                if (VULNERABLE_DMODS.contains(hullMod)) {
                    defenderRating--;
                }
            }
            if (attackerRating < 1) {
                attackerRating = 1f;
            }
            if (defenderRating < 1) {
                defenderRating = 1f;
            }
            float ECMscore = attackerRating - defenderRating;

            float effectiveness = attackerRating / defenderRating;
            effectiveness = effectiveness * defenderECCM;

            if (effectiveness > 2f) {
                effectiveness = 2f;
            } else if (effectiveness < 0.35f) {
                effectiveness = 0.35f;
            }
            Global.getSoundPlayer().playSound("system_recall_device", 1f, 1f, ship.getLocation(), ship.getVelocity());

            float numBeams = VFX_BEAMS.get(target.getHullSize());
            if (effectiveness <= 0.65f) {
                numBeams--;
            }
            if (effectiveness >= 1.35f) {
                numBeams++;
            }

            boolean ice = false;
            // what, like Glaceon?
            String iceType = "";
            if (target.getCaptain() != null) {
                ice = target.getCaptain().getStats().hasSkill("AL_convictionfirewall");
                iceType = "conviction";
            }
            if (target.getHullStyleId().contains("OMEGA")) {
                ice = true;
                iceType = "omega";
            }

            // Omega ICE resistance and backlash - Approlight's Graven also has this effect
            if (ice) {
                Global.getSoundPlayer().playSound("system_entropy_off", 1f, 1f, target.getLocation(), target.getVelocity());
                target.getFluxTracker().showOverloadFloatyIfNeeded("Exotic ICE: intrusion failed!", HACK_COLOR, 4f, true);
                for (int i = 0; i < numBeams; i++) {
                    Vector2f from = Misc.getPointWithinRadius(ship.getShieldCenterEvenIfNoShield(), ship.getCollisionRadius() * 0.35f);
                    Vector2f to = Misc.getPointWithinRadius(target.getShieldCenterEvenIfNoShield(), target.getCollisionRadius() * 0.35f);
                    MagicFakeBeamPlugin.addBeam(
                            1f,
                            VFX_BEAM_FADEOUT,
                            4f,
                            from,
                            Misc.getAngleInDegrees(from, to),
                            Misc.getDistance(from, to),
                            Color.WHITE,
                            HACK_COLOR
                    );
                }
                for (int i = 0; i < 4; i++) {
                    Vector2f from = Misc.getPointWithinRadius(target.getShieldCenterEvenIfNoShield(), target.getCollisionRadius() * 0.35f);
                    Vector2f to = Misc.getPointWithinRadius(ship.getShieldCenterEvenIfNoShield(), ship.getCollisionRadius() * 0.35f);
                    MagicFakeBeamPlugin.addBeam(
                            1f,
                            VFX_BEAM_FADEOUT,
                            8f,
                            from,
                            Misc.getAngleInDegrees(from, to),
                            Misc.getDistance(from, to),
                            Color.WHITE,
                            Misc.getNegativeHighlightColor()
                    );
                }
                if (ship.getCaptain() != null) {
                    if (iceType.equals("omega")) {
                        ship.getCaptain().getMemoryWithoutUpdate().set(TRIED_CYBER_ON_OMEGA, true);
                    } else if (iceType.equals("conviction")) {
                        ship.getCaptain().getMemoryWithoutUpdate().set(TRIED_CYBER_ON_CONVICTION, true);
                    }
                }
                ship.getFluxTracker().beginOverloadWithTotalBaseDuration(1f);
                ship.getFluxTracker().showOverloadFloatyIfNeeded("Exotic ICE: backlash!", HACK_COLOR, 4f, true);
                return;
            }

            String hackText = "";
            float textSize = Math.max(6f * effectiveness, 3f);

            switch (hackType) {
                case "weapons":
                    hackText = "Gunnery control";
                    List<WeaponAPI> hackable = getHackableWeapons(target);
                    WeightedRandomPicker weaponPicker = new WeightedRandomPicker<WeaponAPI>();
                    for (WeaponAPI weapon : hackable) {
                        weaponPicker.add(weapon, weapon.getSize().ordinal() + 1);
                    }
                    float fractionToHack = 0.3f * effectiveness;

                    int numToHack = Math.round(hackable.size() * fractionToHack);
                    for (int i = 0; i < numToHack + 1; i++) {
                        if (weaponPicker.isEmpty()) break;
                        WeaponAPI targetWeapon = (WeaponAPI) weaponPicker.pickAndRemove();
                        Vector2f from = Misc.getPointWithinRadius(ship.getShieldCenterEvenIfNoShield(), ship.getCollisionRadius() * 0.35f);
                        // skip the collision detection/damage parts of the fake beam, we don't want them
                        MagicFakeBeamPlugin.addBeam(
                                VFX_BEAM_FULL,
                                VFX_BEAM_FADEOUT,
                                5f,
                                from,
                                Misc.getAngleInDegrees(from, targetWeapon.getLocation()),
                                Misc.getDistance(from, targetWeapon.getLocation()),
                                Color.WHITE,
                                Misc.getNegativeHighlightColor());
                        targetWeapon.disable();
                    }
                    break;
                case "engines":
                    hackText = "Engine control";
                    ShipEngineControllerAPI ec = target.getEngineController();
                    float limit = ec.getFlameoutFraction() * 0.75f * effectiveness;

                    float disabledSoFar = 0f;
                    boolean disabledAnEngine = false;
                    List<ShipEngineControllerAPI.ShipEngineAPI> engines = new ArrayList<ShipEngineControllerAPI.ShipEngineAPI>(ec.getShipEngines());
                    Collections.shuffle(engines);

                    for (ShipEngineControllerAPI.ShipEngineAPI engine : engines) {
                        if (engine.isDisabled()) continue;
                        float contrib = engine.getContribution();
                        if (disabledSoFar + contrib <= limit) {
                            Vector2f from = Misc.getPointWithinRadius(ship.getShieldCenterEvenIfNoShield(), ship.getCollisionRadius() * 0.35f);
                            // skip the collision detection/damage parts of the fake beam, we don't want them
                            MagicFakeBeamPlugin.addBeam(
                                    VFX_BEAM_FULL,
                                    VFX_BEAM_FADEOUT,
                                    5f,
                                    from,
                                    Misc.getAngleInDegrees(from, engine.getLocation()),
                                    Misc.getDistance(from, engine.getLocation()),
                                    Color.WHITE,
                                    HACK_COLOR);
                            disabledSoFar += contrib;
                            disabledAnEngine = true;
                            engine.disable();
                        }
                    }
                    if (!disabledAnEngine) {
                        for (ShipEngineControllerAPI.ShipEngineAPI engine : engines) {
                            if (engine.isDisabled()) continue;
                            engine.disable();
                            break;
                        }
                    }
                    ec.computeEffectiveStats(target == Global.getCombatEngine().getPlayerShip());
                    break;
                case "defense":
                    hackText = "Shields";
                    if (target.getPhaseCloak() != null) {
                        hackText = target.getPhaseCloak().getDisplayName();
                    }
                    boolean fireBeamsAtShield = target.getShield() != null && target.getShield().isOn();
                    for (int i = 0; i < numBeams; i++) {
                        Vector2f from = Misc.getPointWithinRadius(ship.getShieldCenterEvenIfNoShield(), ship.getCollisionRadius() * 0.35f);
                        if (fireBeamsAtShield) {
                            float angleBonus = (target.getShield().getActiveArc() * 0.5f);
                            if (Math.random() > 0.5f) {
                                angleBonus *= -1f;
                            }
                            Vector2f to = MathUtils.getPoint(target.getShieldCenterEvenIfNoShield(), target.getShieldRadiusEvenIfNoShield() - 5f, target.getShield().getFacing() + angleBonus);
                            MagicFakeBeamPlugin.addBeam(
                                    VFX_BEAM_FULL,
                                    VFX_BEAM_FADEOUT,
                                    5f,
                                    from,
                                    Misc.getAngleInDegrees(from, to),
                                    Misc.getDistance(from, to),
                                    Color.WHITE,
                                    HACK_COLOR);
                        } else {
                            Vector2f to = Misc.getPointWithinRadius(target.getShieldCenterEvenIfNoShield(), target.getCollisionRadius() * 0.35f);
                            MagicFakeBeamPlugin.addBeam(
                                    VFX_BEAM_FULL,
                                    VFX_BEAM_FADEOUT,
                                    5f,
                                    from,
                                    Misc.getAngleInDegrees(from, to),
                                    Misc.getDistance(from, to),
                                    Color.WHITE,
                                    HACK_COLOR);
                        }
                    }
                    target.getFluxTracker().beginOverloadWithTotalBaseDuration(1.5f * effectiveness);
                    //target.addListener(new SotfCyberwarfareSystemHack(target, false));
                    break;
                case "system":
                    hackText = target.getSystem().getDisplayName();
                    for (int i = 0; i < numBeams; i++) {
                        Vector2f from = Misc.getPointWithinRadius(ship.getShieldCenterEvenIfNoShield(), ship.getCollisionRadius() * 0.35f);
                        Vector2f to = Misc.getPointWithinRadius(target.getShieldCenterEvenIfNoShield(), target.getCollisionRadius() * 0.35f);
                        MagicFakeBeamPlugin.addBeam(
                                1f,
                                VFX_BEAM_FADEOUT,
                                5f,
                                from,
                                Misc.getAngleInDegrees(from, to),
                                Misc.getDistance(from, to),
                                Color.WHITE,
                                HACK_COLOR
                        );
                    }
                    target.getFluxTracker().beginOverloadWithTotalBaseDuration(1f);
                    if (target.getSystem().isActive()) {
                        target.getSystem().deactivate();
                    }
                    // remove active system charges
                    if (target.getSystem().getAmmoPerSecond() > 0f && target.getSystem().getAmmoPerSecond() > 0f) {
                        target.getSystem().setAmmo(0);
                    }
                    target.getSystem().setCooldownRemaining(6f * effectiveness);
                    //target.addListener(new SotfCyberwarfareSystemHack(target, true));
                    break;
            }
            String hackSoundId = "disabled_medium_crit";
            if (effectiveness <= 0.65f) {
                hackText += " disrupted";
                hackSoundId = "disabled_small_crit";
            } else if (effectiveness >= 1.35f) {
                hackText += " disabled!";
            } else {
                hackText += " disrupted!";
                hackSoundId = "disabled_large_crit";
            }
            Global.getSoundPlayer().playSound("disabled_large_crit", 1f, 2f, target.getLocation(), target.getVelocity());
            target.getFluxTracker().showOverloadFloatyIfNeeded(hackText, HACK_COLOR, textSize, true);
        }

        public List<WeaponAPI> getHackableWeapons(ShipAPI ship) {
            List<WeaponAPI> weapons = new ArrayList<>();
            for (WeaponAPI weapon : ship.getUsableWeapons()) {
                if (CRPluginImpl.isOkToPermanentlyDisableStatic(ship, weapon)) {
                    weapons.add(weapon);
                }
            }
            return weapons;
        }

        public ShipAPI findTarget(ShipAPI ship) {
            float range = getHackRange();
            Vector2f from = ship.getLocation();

            Iterator<Object> iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from,
                    range * 2f, range * 2f);
            int owner = ship.getOwner();
            ShipAPI best = null;
            float minScore = -9999f;

            while (iter.hasNext()) {
                Object o = iter.next();
                if (!(o instanceof ShipAPI)) continue;
                ShipAPI other = (ShipAPI) o;
                if (owner == other.getOwner()) continue;
                if (Misc.getDistance(from, other.getLocation()) > range) continue;

                ShipAPI otherShip = (ShipAPI) other;

                if (!isValidTarget(ship, otherShip)) continue;

                float radius = Misc.getTargetingRadius(from, other, false);
                float score = range - (Misc.getDistance(from, other.getLocation()) - radius);

                // apply bonuses to priority targets

                // bonus to larger ships
                score += (400f * otherShip.getHullSize().ordinal() - 2);

                // bonus for ships that are easier to hack
                float attackerRating = ship.getMutableStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_FLAT);
                float defenderRating = otherShip.getMutableStats().getDynamic().getValue(Stats.ELECTRONIC_WARFARE_FLAT);
                float ECMscore = attackerRating - defenderRating;
                score += ECMscore * 50f;

                ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();

                // prioritise the ship's target
                float shipTargetBonus = 1000f;
                // if player ship, always hack player target if not invalid
                if (ship == playerShip) {
                    shipTargetBonus = 99999f;
                }
                if (ship.getShipTarget() == otherShip || otherShip.getChildModulesCopy().contains(ship.getShipTarget())) {
                    score += shipTargetBonus;
                }
                // player fleet's ships prioritise ASB strikes on player targets
                if (ship.getOwner() == 0 && playerShip != null && playerShip.getShipTarget() != null) {
                    if (playerShip.getShipTarget() == otherShip || otherShip.getChildModulesCopy().contains(ship.getShipTarget())) {
                        score *= 1.5f;
                    }
                } else if (ship.getOwner() == 0 && otherShip.isRecentlyShotByPlayer()) {
                    score *= 1.5f;
                }

                if (score > minScore) {
                    minScore = score;
                    best = other;
                }
            }
            return best;
        }

        public boolean isValidTarget(ShipAPI ship, ShipAPI target) {
            boolean isValid = true;
            if (!target.isAlive() ||
                    target.getOwner() == ship.getOwner() ||
                    target.isFighter() ||
                    target.isPhased() ||
                    target.getCollisionClass() == CollisionClass.NONE || target.getVariant().hasHullMod(HullMods.VASTBULK)) {
                isValid = false;
            }
            if (target.isStationModule()) {
                ShipAPI station = target.getParentStation();
                if (!station.getVariant().hasHullMod(HullMods.VASTBULK)) {
                    isValid = false;
                }
            }
            // don't try to hack Omega if we've ever tried it before
            if (ship.getCaptain() != null && target.getHullStyleId().contains("OMEGA")) {
                if (ship.getCaptain().getMemoryWithoutUpdate().contains(TRIED_CYBER_ON_OMEGA)) {
                    isValid = false;
                }
            }
            if (ship.getCaptain() != null && target.getCaptain() != null && target.getCaptain().getStats().hasSkill("AL_convictionfirewall")) {
                if (ship.getCaptain().getMemoryWithoutUpdate().contains(TRIED_CYBER_ON_CONVICTION)) {
                    isValid = false;
                }
            }
            // must be valid for at least one intrusion type
            if (target.getUsableWeapons().size() == 0 &&
                    target.getEngineController().getShipEngines().size() == 0 &&
                    target.getShield() == null && target.getPhaseCloak() == null &&
                    target.getSystem() == null) {
                isValid = false;
            }
            return isValid;
        }
    }

}

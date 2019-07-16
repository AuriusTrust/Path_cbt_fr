package org.pathfinderfr.app.treasure;

import android.content.Context;
import android.util.Log;

import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class TreasureUtil {

    private static final String PROPS_NAME = "magicitems.properties";

    public static final int TABLE_SOURCE_MJ            = 1;
    public static final int TABLE_SOURCE_MJRA          = 2;

    public static final String TABLE_MAIN              = "root";
    public static final String TABLE_ARMOR_MAIN        = "armor";
    public static final String TABLE_ARMOR_PROPS       = "armor.props";
    public static final String TABLE_ARMOR_PROPS_MJRA  = "armor.props.mjra";
    public static final String TABLE_SHIELD_PROPS      = "shield.props";
    public static final String TABLE_ARMOR_SPEC        = "armor.specific";
    public static final String TABLE_ARMOR_SPEC_MJRA   = "armor.specific.mjra";
    public static final String TABLE_SHIELD_SPEC       = "shield.specific";
    public static final String TABLE_SHIELD_SPEC_MJRA  = "shield.specific.mjra";



    private static TreasureUtil instance;
    private Properties properties;

    public static synchronized TreasureUtil getInstance(Context context) {
        if(instance == null) {
            instance = new TreasureUtil(context);
        }
        return instance;
    }

    public TreasureUtil(Context context) {
        properties = new Properties();
        try {
            if(context != null) {
                properties.load(context.getAssets().open(PROPS_NAME));
            }
        } catch (IOException e) {
            Log.e(ConfigurationUtil.class.getSimpleName(),
                    String.format("Properties %s couldn't be found!", PROPS_NAME), e);
        }
    }

    /**
     * Reads magicitems.properties (see file) and fills Treasure Table
     *
     * @param tableId identifier of the table
     * @return TreasureTable object
     */
    public TreasureTable generateTable(String tableId) {
        int idx = 1;
        TreasureTable table = new TreasureTable();
        while(properties.containsKey("table." + tableId + "." + idx)) {
            table.addRow(properties.get("table." + tableId + "." + idx).toString());
            idx++;
        }
        return table;
    }


    private static boolean entryExists(List<Pair<String,String>> history, String choice) {
        for(Pair<String, String> entry : history) {
            if (entry.second.equals(choice)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of choices made in specific table
     */
    private static int countTableEntries(List<Pair<String,String>> history, String tablename) {
        int count = 0;
        for(Pair<String, String> entry : history) {
            if (entry.first.equals(tablename)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns true if give choice was made
     */
    public static String nextTable(int curSource, List<Pair<String,String>> history, String curTable, String choice) {
        if(history == null) {
            return null;
        }

        switch (curTable) {
            case TreasureUtil.TABLE_MAIN:
                if("Armures et boucliers".equals(choice)) {
                    return TreasureUtil.TABLE_ARMOR_MAIN;
                } else {
                    return null;
                }
            case TreasureUtil.TABLE_ARMOR_MAIN:
                if("Armure spécifique".equals(choice)) {
                    return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_ARMOR_SPEC : TreasureUtil.TABLE_ARMOR_SPEC_MJRA;
                } else if("Bouclier spécifique".equals(choice)) {
                    return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_SHIELD_SPEC : TreasureUtil.TABLE_SHIELD_SPEC_MJRA;
                } else if(choice.startsWith("Propriété spéciale")) {
                    // make sure that not already in history
                    if(entryExists(history, choice)) {
                        throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.special"));
                    }
                    return TreasureUtil.TABLE_ARMOR_MAIN; // rejouer le dé
                } else {
                    // check if history has special property
                    if(entryExists(history,"Propriété spéciale (armure ou bouclier)")) {
                        // armor or shield
                        if(choice.toLowerCase().indexOf("armure") >= 0) {
                            return curSource == TreasureUtil.TABLE_SOURCE_MJ ? TreasureUtil.TABLE_ARMOR_PROPS : TreasureUtil.TABLE_ARMOR_PROPS_MJRA;
                        } else {
                            return TreasureUtil.TABLE_SHIELD_PROPS;
                        }
                    }
                    return null;
                }
            case TreasureUtil.TABLE_ARMOR_PROPS:
                if("Relancez deux fois le dé".equals(choice)) {
                    // make sure that not already in history
                    if(entryExists(history, choice)) {
                        throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.2properties"));
                    }
                    return TreasureUtil.TABLE_ARMOR_PROPS;
                } else if(entryExists(history, "Relancez deux fois le dé")) {
                    // history must have 3x choices
                    return countTableEntries(history, TreasureUtil.TABLE_ARMOR_PROPS) >= 2 ? null : TreasureUtil.TABLE_ARMOR_PROPS;
                }
            case TreasureUtil.TABLE_ARMOR_PROPS_MJRA:
                if("Relancez deux fois le dé".equals(choice)) {
                    // make sure that not already in history
                    if(entryExists(history, choice)) {
                        throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.2properties"));
                    }
                    return TreasureUtil.TABLE_ARMOR_PROPS_MJRA;
                } else if(entryExists(history, "Relancez deux fois le dé")) {
                    // history must have 3x choices
                    return countTableEntries(history, TreasureUtil.TABLE_ARMOR_PROPS_MJRA) >= 2 ? null : TreasureUtil.TABLE_ARMOR_PROPS_MJRA;
                }
            case TreasureUtil.TABLE_SHIELD_PROPS:
                if("Relancez deux fois le dé".equals(choice)) {
                    // make sure that not already in history
                    if(entryExists(history, choice)) {
                        throw new IllegalArgumentException(ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.duplicate.2properties"));
                    }
                    return TreasureUtil.TABLE_SHIELD_PROPS;
                } else if(entryExists(history, "Relancez deux fois le dé")) {
                    // history must have 3x choices
                    return countTableEntries(history, TreasureUtil.TABLE_SHIELD_PROPS) >= 2 ? null : TreasureUtil.TABLE_SHIELD_PROPS;
                }

            default:
                return null;
        }
    }

}

package org.pathfinderfr.app.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.CharacterItem;
import org.pathfinderfr.app.database.entity.Modification;
import org.pathfinderfr.app.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class MigrationHelper {

    /**
     * Executes sql queries for migrating Character.inventory => CharacterItem[]
     */
    public static void migrateCharacterItems(SQLiteDatabase db) {
        Cursor res =  db.rawQuery( String.format("SELECT id, inventory FROM characters"), null );
        // not found?
        if(res.getCount()<1) {
            res.close();
            return;
        }
        res.moveToFirst();
        while (!res.isAfterLast()) {
            int index = 0;
            long characterId = res.getLong(res.getColumnIndex("id"));
            String inventory = res.getString(res.getColumnIndex("inventory"));
            if(inventory != null && inventory.length() > 0 ) {
                String[] items = inventory.split("#");
                for(String item : items) {
                    String[] props = item.split("\\|");
                    if (props.length >= 2) {
                        String name = props[0];
                        int weight = 0;
                        try {
                            weight = Integer.parseInt(props[1]);
                        } catch (NumberFormatException nfe) {
                            Log.e(CharacterFactory.class.getSimpleName(), "Stored inventory weight '" + props[1] + "' is invalid (NFE)!");
                        }
                        // item reference was introduced later
                        long objId = 0;
                        if (props.length >= 3) {
                            try {
                                objId = Long.parseLong(props[2]);
                            } catch (NumberFormatException nfe) {
                                Log.e(CharacterFactory.class.getSimpleName(), "Stored inventory référence '" + props[2] + "' is invalid (NFE)!");
                            }
                        }
                        // item additional info (ex: ammo)
                        String infos = null;
                        if (props.length >= 4) {
                            infos = props[3];
                        }
                        // item cost was introduced later
                        long price = 0;
                        if (props.length >= 5) {
                            try {
                                price = Long.parseLong(props[4]);
                            } catch (NumberFormatException nfe) {
                                Log.e(CharacterFactory.class.getSimpleName(), "Stored inventory price '" + props[4] + "' is invalid (NFE)!");
                            }
                        }
                        CharacterItem cItem = new CharacterItem();
                        cItem.setCharacterId(characterId);
                        cItem.setOrder(index);
                        cItem.setName(name);
                        cItem.setPrice(price);
                        cItem.setWeight(weight);
                        cItem.setItemRef(objId);
                        cItem.setAmmo(infos);
                        cItem.setLocation(CharacterItem.LOCATION_NOLOC);
                        ContentValues contentValues = cItem.getFactory().generateContentValuesFromEntity(cItem);
                        db.insert(cItem.getFactory().getTableName(), null, contentValues);
                        index++;
                    }
                }
            }
            res.moveToNext();
        }
        res.close();
    }


    /**
     * Executes sql queries for migrating Character.modifs => Modification[]
     */
    public static void migrateModifs(SQLiteDatabase db) {
        Cursor res =  db.rawQuery( String.format("SELECT id, inventory, modifs FROM characters"), null );
        // not found?
        if(res.getCount()<1) {
            res.close();
            return;
        }
        res.moveToFirst();
        while (!res.isAfterLast()) {
            int index = 0;
            long characterId = res.getLong(res.getColumnIndex("id"));
            String modifsValue = res.getString(res.getColumnIndex("modifs"));
            String inventoryValue = res.getString(res.getColumnIndex("inventory"));
            // build weapon list
            List<CharacterItem> weapons = new ArrayList<>();
            String[] items = inventoryValue == null ? new String[0] : inventoryValue.split("#");
            for(String item : items) {
                String[] props = item.split("\\|");
                if (props.length >= 3) {
                    try {
                        CharacterItem weapon = new CharacterItem();
                        weapon.setName(props[0]);
                        weapon.setItemRef(Long.parseLong(props[2]));
                        if(weapon.isWeapon()) {
                            weapons.add(weapon);
                        }
                    } catch (NumberFormatException nfe) {
                        Log.e(CharacterFactory.class.getSimpleName(), "Stored inventory référence '" + props[2] + "' is invalid (NFE)!");
                    }
                }
            }
            if (modifsValue != null && modifsValue.length() > 0) {
                for (String modif : modifsValue.split("#")) {
                    String[] modElements = modif.split(":");
                    if (modElements.length >= 3) {
                        String source = modElements[0];
                        String icon = modElements[2];
                        int linkToWeapon = modElements.length >= 4 ? Integer.parseInt(modElements[3]) : 0;
                        List<Pair<Integer, Integer>> bonuses = new ArrayList<>();
                        for (String bonusVal : modElements[1].split(",")) {
                            String[] bonusElements = bonusVal.split("\\|");
                            if (bonusElements.length == 2) {
                                try {
                                    Integer bonusIdx = Integer.parseInt(bonusElements[0]);
                                    Integer bonusValue = Integer.parseInt(bonusElements[1]);
                                    bonuses.add(new Pair<>(bonusIdx, bonusValue));
                                } catch (NumberFormatException nfe) {
                                    Log.e(CharacterFactory.class.getSimpleName(), "Stored modif '" + bonusVal + "' is invalid (NFE)!");
                                }
                            }
                        }
                        Modification modification = new Modification(source, bonuses, icon, false);
                        modification.setCharacterId(characterId);
                        if(linkToWeapon > 0) {
                            // find item ID based on name
                            long itemId = 0;
                            if(linkToWeapon <= weapons.size()) {
                                Cursor resW = db.rawQuery("SELECT id FROM characitems WHERE characterid=? AND name=?", new String[] { String.valueOf(characterId), weapons.get(linkToWeapon-1).getName()});
                                // not found?
                                if(resW.getCount()>=1) {
                                    resW.moveToFirst();
                                    itemId = resW.getLong(res.getColumnIndex("id"));
                                }
                                resW.close();
                            }
                            modification.setItemId(itemId);
                        }
                        ContentValues contentValues = modification.getFactory().generateContentValuesFromEntity(modification);
                        db.insert(modification.getFactory().getTableName(), null, contentValues);
                    }
                }
            }
            res.moveToNext();
        }
        res.close();
    }
}

package com.androiddev.thirtyseven.studybuddy.Sessions.Document;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Nathan on 4/18/2016.
 */
public class Delete extends Mutation {

    /**
     * subDelete is used in cases when an this Mutation is transformed against and Insert which
     * inserts a string in the middle of the characters deleted. This Delete changes to delete all
     * characters before the Inserts, while subDelete is created to delete all characters after
     * the Insert.
     */

    protected int numChars; // number of characters to be deleted
    protected Delete subDelete; // used for when an insert splits the delete

    /**
     * Constructs a new Delete
     * @param index - the index that the mutation begins at
     * @param numChars - number of characters to delete
     * @param version - the version when this Mutation was created
     * @param senderID - the ID of the user who created this Mutation
     * @param sessionID - the ID of the session that this Mutation is from
     */
    public Delete(int index, int numChars, HashMap<String, Integer> version, String senderID, String sessionID) {
        super(MUTATION_DELETE, index, version, senderID, sessionID);
        this.numChars = numChars;
        subDelete = null;
    }

    /**
     * Parses the given JSONObject to construct the Delete
     * @param json - the JSONObject representing the Delete
     */
    public Delete(JSONObject json) {
        super(json);
        try {
            numChars = json.getInt("numChars");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int length() {
        return numChars;
    }

    @Override
    public int end() {
        return index + numChars;
    }

    @Override
    public void transform(Mutation mutation, String receiverID) {
        if (subDelete != null) {
            subDelete.transform(mutation, receiverID);
        }
        switch (mutation.type) {
            case MUTATION_INSERT:
                if (mutation.index <= index) { // this needs to be modified
                    index += mutation.length();
                }
                else if (mutation.index - index < length()) { // mutation splits thisx
                    HashMap<String, Integer> subVersion = (HashMap<String, Integer>) version.clone();
                    subVersion.put(receiverID, version.get(receiverID) + 1);
                    subDelete = new Delete(mutation.end(), length() - (mutation.index - index), subVersion, senderID, sessionID);
                    numChars -= subDelete.length();
                }
                break;
            case MUTATION_DELETE:
                Delete del = (Delete) mutation;
                if (del.subDelete != null) {
                    transform(del.subDelete, receiverID);
                }

                // Several cases where this modified to avoid deleting the same character twice
                // For cases: . = not modified, | = del, - = this, + = both
                if (del.index <= index) {
                    if (del.end() <= index) {
                        // Case: ....||||..---.. or ..||||----...
                        index -= del.length();
                    }
                    else if (del.end() < end()) {
                        // Case: ....|||+++----.. or ....++++----....
                        numChars -= del.end() - index;
                        index += del.end() - index;
                    }
                    else {
                        // Case: ....|||++++++.... or ...||++++||... or .....++++|||
                        numChars = 0;
                    }
                }
                else {
                    if (mutation.index <= end()) {
                        // Case: ...----++++||||||... or ....-------+||||||||....
                        numChars -= end() - mutation.index;
                    }
                    // The only cases remaining are ..-----|||||... and ...-----..|||||||..
                    // These don't require a change
                }
                break;
            default:
                break;
        }
    }

    @Override
    public JSONObject getJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("index", index);
            json.put("senderID", senderID);
            json.put("sessionID", sessionID);
            json.put("numChars", numChars);
            json.put("version", new JSONObject(version));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}

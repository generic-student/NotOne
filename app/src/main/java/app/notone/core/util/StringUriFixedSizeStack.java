package app.notone.core.util;
import android.net.Uri;
import android.util.Log;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StringUriFixedSizeStack extends LinkedHashMap<String, Uri> {
   private int maxSize;
   private static final String TAG = "Stack";

   public StringUriFixedSizeStack(int size) {
      super();
      this.maxSize = size;
   }

   public StringUriFixedSizeStack(int size, String cereal) throws ArrayIndexOutOfBoundsException{
      super();
      this.maxSize = size;
      fromJson(cereal, this);

//      if(cereal.equals("")) {
//         Log.d(TAG, "StringUriFixedSizeStack: No Recent Files");
//         return;
//      }
//
//      Log.d(TAG, "StringUriFixedSizeStack: creating LinkedHashMap from String: " + cereal);
//      cereal = cereal.replace("{","");
//      cereal = cereal.replace("}","");
////      Log.d(TAG, "StringUriFixedSizeStack: " + cereal.split(",").toString());
//      for(String pair : cereal.split(",")) {
//         if(pair.equals("")){
//            continue;
//         }
//         Log.d(TAG, "StringUriFixedSizeStack: adding pair: " + pair);
//         String[] splitPair = pair.split(":");
//         Uri uri = Uri.parse(String.join("", Arrays.copyOfRange(splitPair, 1, splitPair.length)).replace("^0",""));
//         Log.d(TAG, "StringUriFixedSizeStack: uri of pair: " + uri);
//         this.push((String) splitPair[0], (Uri) uri);
//      }
   }

   public void push(String key, Uri uri) {
      while (this.size() >= maxSize) {
         this.remove(this.entrySet().stream().findFirst().get().getKey()); // remove old elements
      }
      super.put(key, uri); // add element to top
   }

   @NonNull
   public String toStringCereal() {
      String s = toJson(this).toString();
      Log.d(TAG, "toStringJson: " + s);
      return s;
//      Log.d(TAG, "toStringCereal: " + this);
//      String result = "{";
//
//      for (Entry<String, Uri> pair : this.entrySet()){
//         Log.d(TAG, "toStringCereal: " + pair);
//         result += pair.getKey().toString() + ":" + pair.getValue().toString();
//         result += ",";
//      }
//
//      result += "}";
//      return result;
   }

   public Map.Entry<String, Uri> getFirst() {
      for(Entry<String, Uri> it : super.entrySet()) {
         return it;
      }
      return null;
   }

   private static JSONObject entryToJson(Entry<String, Uri> entry) {
      JSONObject entryJson = new JSONObject();
      try {
         entryJson.put("key", entry.getKey());
         entryJson.put("value", entry.getValue().toString());
      } catch (JSONException e) {
         e.printStackTrace();
      }
      return entryJson;
   }

   public static void fromJson(String jsonString, StringUriFixedSizeStack stack) {
      JSONObject json = null;
      try {
         json = new JSONObject(jsonString);
      } catch (JSONException e) {
         e.printStackTrace();
         return;
      }

      try {
         Map<String, Uri> map = (Map<String, Uri>) json.get("entries");
         map.entrySet().stream().forEach(e -> {
            stack.push(e.getKey(), e.getValue());
         });
      } catch (JSONException e) {
         e.printStackTrace();
      }
   }

   public static JSONObject toJson(StringUriFixedSizeStack stack) {
      JSONObject json = new JSONObject();

      try {
         json.put("entries: ", (Map) stack);
      } catch (JSONException e) {
         e.printStackTrace();
      }

      return json;
   }
}

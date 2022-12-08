package app.notone.core.util;
import android.net.Uri;
import android.util.Log;

import java.util.LinkedHashMap;

import androidx.annotation.NonNull;

public class StringUriFixedSizeStack<K, V> extends LinkedHashMap<K, V> {
   private int maxSize;
   private static final String TAG = "Stack";

   public StringUriFixedSizeStack(int size) {
      super();
      this.maxSize = size;
   }

   public StringUriFixedSizeStack(int size, String cereal) throws ArrayIndexOutOfBoundsException{
      super();
      this.maxSize = size;
      if(cereal.equals("")) {
         Log.d(TAG, "StringUriFixedSizeStack: No Recent Files");
         return;
      }
      Log.d(TAG, "StringUriFixedSizeStack: creating LinkedHashMap from String: " + cereal);
      cereal = cereal.replace("{","");
      cereal = cereal.replace("}","");
      Log.d(TAG, "StringUriFixedSizeStack: " + cereal.split(",").toString());
      for(String pair : cereal.split(",")) {
         if(pair.equals("")){
            continue;
         }
         Log.d(TAG, "StringUriFixedSizeStack: adding pair: " + pair);
         String[] splitPair = pair.split(":");
         this.push((K) splitPair[0], (V) Uri.parse(splitPair[1]));
      }
   }

   public void push(K key, V uri) {
      while (this.size() >= maxSize) {
         this.remove(this.entrySet().stream().findFirst().get().getKey()); // remove old elements
      }
      super.put(key, uri); // add element to top
   }

   @NonNull
   public String toStringCereal() {
      String result = "{";

      for (Entry<K, V> pair : this.entrySet()){
         result += pair.getKey().toString() + ":" + pair.getValue().toString();
         result += ",";
      }

      result += "}";
      return result;
   }
}

package app.notone.core.util;
import java.util.LinkedHashMap;

public class StringUriFixedSizeStack<String,Uri> extends LinkedHashMap<String,Uri> {
   private int maxSize;

   public StringUriFixedSizeStack(int size) {
      super();
      this.maxSize = size;
   }

   public void push(String key, Uri uri) {
      //If the stack is too big, remove elements from the bottom until it's the right size.
      while (this.size() >= maxSize) {
         this.remove(this.entrySet().stream().findFirst().get().getKey());
      }
      super.put(key, uri); // add element to top
   }
}

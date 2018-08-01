package wilson.com.project_linechart;

import java.io.Serializable;

public class Storage implements Serializable {

   private String date;
   private float lux;

   public Storage(String date, float lux) {
      this.date = date;
      this.lux = lux;
   }

   public String getDate() {
      return date;
   }

   public float getLux() {
      return lux;
   }
}

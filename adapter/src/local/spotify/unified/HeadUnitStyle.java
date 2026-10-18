package local.spotify.unified;
import android.content.res.Configuration;
/**
 * The "car" UI-mode type, applied together with the UI scale to our screens' configuration (the
 * New style of test.10–test.12 selected its resources by it; Spotify's resources follow it too).
 */
public final class HeadUnitStyle {
 private HeadUnitStyle(){}
 static Configuration apply(Configuration config){
  config.uiMode=(config.uiMode&~Configuration.UI_MODE_TYPE_MASK)|Configuration.UI_MODE_TYPE_CAR;
  return config;
 }
 static boolean applied(Configuration config){return (config.uiMode&Configuration.UI_MODE_TYPE_MASK)==Configuration.UI_MODE_TYPE_CAR;}
}

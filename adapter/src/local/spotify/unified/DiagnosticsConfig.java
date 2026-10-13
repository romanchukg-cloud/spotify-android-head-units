package local.spotify.unified;
/** Public client configuration only; credentials belong on the shared server. */
public final class DiagnosticsConfig {
 public static final String BASE_URL="https://diagnostics.localcharge.app";
 public static final String PROJECT_ID="spotify-android-head-units";
 public static final int SCHEMA_VERSION=2;
 public static boolean automaticSupported(){return true;} // Every automatic POST still requires a live capability check.
 public static final String DISCLOSURE="Reports go to diagnostics.localcharge.app. A limited technical summary may be published in this project's PUBLIC GitHub Issues. Reports exclude account information, media titles, routes, coordinates, screenshots and device identifiers. Technical metadata can still identify an unusual configuration.";
 public static boolean issueUrl(String text){return text!=null && text.matches("https://github\\.com/romanchukg-cloud/spotify-android-head-units/issues/[1-9][0-9]*");}
 public static boolean uuid(String text){try{java.util.UUID u=java.util.UUID.fromString(text);return u.version()==4 && u.variant()==2 && u.toString().equals(text);}catch(RuntimeException e){return false;}}
}

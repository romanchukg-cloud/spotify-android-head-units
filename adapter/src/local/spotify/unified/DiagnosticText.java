package local.spotify.unified;
/** Support text is deliberately English in every application locale. */
public final class DiagnosticText {
 public static String get(String key){return "title".equals(key)?"Support":key;}
}

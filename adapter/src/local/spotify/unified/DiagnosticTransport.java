package local.spotify.unified;
import org.json.JSONObject;
public interface DiagnosticTransport {
 final class Reply {
  public final int http;public final JSONObject json;public final long retryAfterMs;
  public Reply(int http,JSONObject json,long retryAfterMs){this.http=http;this.json=json;this.retryAfterMs=retryAfterMs;}
 }
 Reply call(String method,String receipt,String key,String token,String body)throws Exception;
 default Reply capabilities()throws Exception{return new Reply(503,new JSONObject(),3600000);}
 default void cancel(){}
}

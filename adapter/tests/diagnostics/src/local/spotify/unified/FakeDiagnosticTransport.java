package local.spotify.unified;
import org.json.*;import java.io.IOException;import java.util.*;import java.util.concurrent.CountDownLatch;
/** Synthetic transport only. Never calls a network API or creates a GitHub issue. */
public final class FakeDiagnosticTransport implements DiagnosticTransport {
 static int totalCalls;int posts,gets,deletes,checks;boolean loseFirst,conflictDelete,badUrl,block,capable=true,blockCapabilities;int firstHttp;long retry;
 final List<String> bodies=new ArrayList<>(),keys=new ArrayList<>(),tokens=new ArrayList<>();final Map<String,String> receipts=new HashMap<>();
 final CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
 public Reply capabilities()throws Exception{checks++;if(blockCapabilities){entered.countDown();release.await();}return new Reply(200,new JSONObject().put("automatic_reports",capable).put("schema_versions",new JSONArray().put(1).put(2)).put("automatic_grouping","project_build_failure_utc_day"),0);}
 public Reply call(String method,String receipt,String key,String token,String body)throws Exception{
  totalCalls++;if("POST".equals(method)){posts++;bodies.add(body);keys.add(key);tokens.add(token);if(!receipts.containsKey(key))receipts.put(key,UUID.randomUUID().toString());receipt=receipts.get(key);entered.countDown();if(block)release.await();if(loseFirst && posts==1)throw new IOException("Synthetic lost reply");if(firstHttp!=0 && posts==1)return new Reply(firstHttp,new JSONObject(),retry);return new Reply(202,new JSONObject().put("report_id",receipt).put("status","queued").put("poll_after_seconds",10),0);}
  if("DELETE".equals(method)){deletes++;if(conflictDelete)return new Reply(409,new JSONObject(),0);return new Reply(200,new JSONObject().put("report_id",receipt).put("status","cancelled"),0);}
  gets++;return new Reply(200,new JSONObject().put("report_id",receipt).put("status","submitted").put("issue_url",badUrl?"https://evil.invalid/stolen":"https://github.com/romanchukg-cloud/spotify-android-head-units/issues/123456789"),0);
 }
}

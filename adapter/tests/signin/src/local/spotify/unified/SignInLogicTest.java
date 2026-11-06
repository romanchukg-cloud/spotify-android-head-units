package local.spotify.unified;
import java.util.Arrays;
/** Plain-JVM checks of the sign-in decisions; see adapter/tests/signin/run.sh. */
public final class SignInLogicTest {
 private static int passed;
 private static void check(String name,boolean ok){if(!ok)throw new AssertionError(name);passed++;}
 public static void main(String[] args){
  // A first run pins the current ID; later runs keep it even if the system ID changes.
  check("pin first run",Arrays.equals(HeadUnitIdentity.pin(null,"a1"),new String[]{"a1","a1",null}));
  check("pin unchanged",Arrays.equals(HeadUnitIdentity.pin("a1","a1"),new String[]{"a1",null,null}));
  check("pin changed keeps original",Arrays.equals(HeadUnitIdentity.pin("a1","b2"),new String[]{"a1",null,"changed"}));
  check("pin missing system ID",Arrays.equals(HeadUnitIdentity.pin("a1",null),new String[]{"a1",null,null}));
  check("no pin without an ID",Arrays.equals(HeadUnitIdentity.pin(null,""),new String[]{"",null,null}));

  check("removal wins",HeadUnitAccounts.classify("USER_INITIATED",true,false,null).equals("account_removed"));
  check("invalid credentials",HeadUnitAccounts.classify("FORCED_LOGOUT",false,true,"INVALID_CREDENTIALS").equals("invalid_credentials"));
  check("not allowed",HeadUnitAccounts.classify("FORCED_LOGOUT",false,true,"NOT_ALLOWED").equals("not_allowed"));
  check("unsupported protocol",HeadUnitAccounts.classify("FORCED_LOGOUT",false,true,"UNSUPPORTED_LOGIN_PROTOCOL").equals("unsupported_protocol"));
  check("unknown server code",HeadUnitAccounts.classify("FORCED_LOGOUT",false,true,null).equals("forced_logout"));
  check("user sign-out",HeadUnitAccounts.classify("USER_INITIATED",false,false,null).equals("user_logout"));
  check("other SDK reason",HeadUnitAccounts.classify("REMOTE_LOGOUT",false,false,null).equals("remote_logout"));

  check("stale account ignored",HeadUnitAccounts.stale("old@example","new@example","new-canonical"));
  check("active name signs out",!HeadUnitAccounts.stale("new@example","new@example",null));
  check("active Android name signs out",!HeadUnitAccounts.stale("new-canonical","new@example","new-canonical"));
  check("unknown active user keeps SDK behavior",!HeadUnitAccounts.stale("old@example",null,null));
  check("missing name keeps SDK behavior",!HeadUnitAccounts.stale(null,"new@example",null));

  check("recover after storage loss",HeadUnitAccounts.recoverable("u",false,"count:4","count:5"));
  check("once per boot",!HeadUnitAccounts.recoverable("u",false,"count:5","count:5"));
  check("QR switch is not a loss",!HeadUnitAccounts.recoverable("u",true,null,"count:5"));
  check("signed-out user is not recovered",!HeadUnitAccounts.recoverable(null,false,null,"count:5"));

  for(String r:new String[]{"invalid_credentials","not_allowed","forced_logout","remote_logout","unsupported_protocol","deprecated","account_removed","storage_missing","not_logged_in"})
   check("schema code "+r,DiagnosticSchema.CODES.contains(HeadUnitAccounts.code(r)));
  for(String r:new String[]{"user_logout","invalid_credentials","not_allowed","unsupported_protocol","deprecated","account_removed","storage_missing","storage_restored","forced_logout"})
   check("text "+r,!SignInText.get(r).equals(SignInText.get("other")));
  // Failed sign-ins: the SDK's result objects, as the fixtures ur.b and ur.d shape them.
  try{
   check("server error facts",Arrays.equals(HeadUnitAccounts.loginFacts(new ur.b(403,"auth_device_blocked"),200),new String[]{"ur.b","403","auth_device_blocked"}));
   check("server error without a message",Arrays.equals(HeadUnitAccounts.loginFacts(new ur.b(500,null),-1),new String[]{"ur.b","500",null}));
   check("other results keep the response's status",Arrays.equals(HeadUnitAccounts.loginFacts(new ur.d(),403),new String[]{"ur.d","403",null}));
  }catch(ReflectiveOperationException e){throw new AssertionError(e);}
  for(String[] known:new String[][]{{"401","auth_bad_credentials"},{"401","login_region_mismatch"},{"401","auth_network"},{"401","ap_network_disabled"},{"429","rate_limited"}})
   check("known "+known[1],"login_known".equals(HeadUnitAccounts.loginReason("ur.b",Integer.parseInt(known[0]),known[1])));
  check("known text with another status","login_unknown_server".equals(HeadUnitAccounts.loginReason("ur.b",403,"auth_network")));
  check("unknown server error","login_unknown_server".equals(HeadUnitAccounts.loginReason("ur.b",401,"something_new")));
  check("no server message","login_unknown_server".equals(HeadUnitAccounts.loginReason("ur.b",500,null)));
  check("refused response","login_failed".equals(HeadUnitAccounts.loginReason("ur.d",403,null)));
  check("no response seen","login_failed".equals(HeadUnitAccounts.loginReason("ur.d",-1,null)));
  check("accepted but not saved","login_not_saved".equals(HeadUnitAccounts.loginReason("ur.d",200,null)));
  check("signed in is no failure",HeadUnitAccounts.loginReason("ur.c",200,null)==null);
  check("deprecated recorded elsewhere",HeadUnitAccounts.loginReason("ur.e",-1,null)==null);
  check("detail with a code",("403 auth_device_blocked").equals(HeadUnitAccounts.loginDetail("login_unknown_server",403,"auth_device_blocked")));
  check("free text stays out",("403").equals(HeadUnitAccounts.loginDetail("login_unknown_server",403,"Sorry, user a@b.c")));
  check("no detail without a status",HeadUnitAccounts.loginDetail("login_failed",-1,null)==null);
  check("no detail once accepted",HeadUnitAccounts.loginDetail("login_not_saved",200,null)==null);
  check("failure codes tell the kinds apart","schema_mismatch".equals(HeadUnitAccounts.code("login_unknown_server")) && "unavailable".equals(HeadUnitAccounts.code("login_not_saved")) && "internal_error".equals(HeadUnitAccounts.code("login_failed")));
  for(String r:new String[]{"login_known","login_failed","login_unknown_server","login_not_saved"})check("text "+r,!SignInText.get(r).equals(SignInText.get("other")));
  System.out.println("Sign-in logic: "+passed+" checks passed");
 }
}

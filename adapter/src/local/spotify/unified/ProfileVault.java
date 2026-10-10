package local.spotify.unified;
import android.content.Context;
import android.security.keystore.*;
import android.util.Base64;
import org.json.*;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
/** One encrypted, private, atomically committed vault. Never logs its contents. */
final class ProfileVault {
 private final Context context;
 private static final String ALIAS="local.spotify.driver.profiles.v1";
 ProfileVault(Context c){context=c;}
 private javax.crypto.SecretKey key()throws Exception {
  KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);
  if(ks.containsAlias(ALIAS))return (javax.crypto.SecretKey)ks.getKey(ALIAS,null);
  KeyGenerator generator=KeyGenerator.getInstance("AES","AndroidKeyStore");
  generator.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
   .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
  return generator.generateKey();
 }
 JSONObject read()throws Exception {
  String value=context.getSharedPreferences("driver_profiles_v1",0).getString("vault",null);
  if(value==null)return new JSONObject().put("version",1).put("profiles",new JSONArray());
  byte[] raw=Base64.decode(value,Base64.NO_WRAP);
  if(raw.length<29 || raw[0]!=1)throw new IllegalStateException("Invalid vault format");
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
  cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,raw,1,12));
  cipher.updateAAD(ALIAS.getBytes("UTF-8"));
  JSONObject result=new JSONObject(new String(cipher.doFinal(raw,13,raw.length-13),"UTF-8"));
  if(result.getInt("version")!=1)throw new IllegalStateException("Unsupported vault");return result;
 }
 void write(JSONObject value)throws Exception {
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key());
  cipher.updateAAD(ALIAS.getBytes("UTF-8"));
  byte[] encrypted=cipher.doFinal(value.toString().getBytes("UTF-8"));
  byte[] raw=new byte[13+encrypted.length];raw[0]=1;
  System.arraycopy(cipher.getIV(),0,raw,1,12);System.arraycopy(encrypted,0,raw,13,encrypted.length);
  if(!context.getSharedPreferences("driver_profiles_v1",0).edit().putString("vault",Base64.encodeToString(raw,Base64.NO_WRAP)).commit())throw new IllegalStateException("Vault save failed");
 }
}

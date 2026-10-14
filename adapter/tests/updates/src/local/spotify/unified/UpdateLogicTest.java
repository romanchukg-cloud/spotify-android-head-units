package local.spotify.unified;
import java.io.*;import java.nio.file.*;import java.util.*;import java.util.regex.*;import org.json.*;
/** Updater decisions without Android: the feed contract, download limits/resume/hashes and text formats. */
public final class UpdateLogicTest {
 static int checks;static final byte[] DATA=new byte[180000];
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 interface Attempt {void run()throws Exception;}
 static void rejects(Attempt call,String message)throws Exception{boolean failed=false;try{call.run();}catch(Exception e){failed=true;}check(failed,message);}
 static final String RELEASES="https://github.com/romanchukg-cloud/spotify-android-head-units/releases/";
 static JSONObject release(String hash,int build)throws Exception{
  String version="test."+build,tag="v5.5.0-headunit-"+version;
  return new JSONObject().put("schema_version",1).put("project_id","spotify-android-head-units").put("build",build).put("version",version)
   .put("version_code",95477L+build).put("package_name","com.spotify.music").put("apk_url",RELEASES+"download/"+tag+"/Spotify-5.5.0-HeadUnit-"+version+".apk")
   .put("sha256",hash).put("size",DATA.length).put("notes","Update test").put("notes_url",RELEASES+"tag/"+tag).put("min_sdk",28);
 }
 static class Progress implements UpdateDownload.Progress {
  boolean stop;public void update(long count,long total){check(count<=total,"Bounded progress");}
  public boolean cancelled(){return stop;}
 }
 static UpdateDownload.Response response(int code,String range,byte[] bytes){return new UpdateDownload.Response(code,range,new ByteArrayInputStream(bytes),()->{});}
 public static void main(String[] args)throws Exception{
  new Random(9).nextBytes(DATA);
  int next=HeadUnitBuild.BUILD+1;
  check(HeadUnitBuild.VERSION_CODE==UpdateFeed.CODE_BASE+HeadUnitBuild.BUILD && HeadUnitBuild.LABEL.equals("headunit-test."+HeadUnitBuild.BUILD),"Build identity follows the server formula");
  // The exact record the server returned for test.8 parses, and is not an update for this or a later build.
  UpdateFeed live=new UpdateFeed(new JSONObject("{\"apk_url\":\"https://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/v5.5.0-headunit-test.8/Spotify-5.5.0-HeadUnit-test.8.apk\",\"build\":8,\"min_sdk\":28,\"notes\":\"Improved sign-in persistence on head units, recovery of lost local login state, and sign-in diagnostics. Update without uninstalling. Experimental prerelease.\",\"notes_url\":\"https://github.com/romanchukg-cloud/spotify-android-head-units/releases/tag/v5.5.0-headunit-test.8\",\"package_name\":\"com.spotify.music\",\"project_id\":\"spotify-android-head-units\",\"schema_version\":1,\"sha256\":\"caa026818b27aa08de38c4c76dab35a02a6ec975095ed5cba9ed0d57c7dc8a93\",\"size\":57962496,\"version\":\"test.8\",\"version_code\":95485}"));
  check(live.build==8 && live.versionCode==95485 && live.size==57962496L,"Live test.8 record");
  check(!live.newer(95485) && !live.newer(1),"An older build is never offered");
  File root=Files.createTempDirectory("spotify-updater-tests").toFile();
  try{
   File source=new File(root,"original");Files.write(source.toPath(),DATA);
   String hash=UpdateDownload.hash(source);UpdateFeed info=new UpdateFeed(release(hash,next));
   check(info.newer(HeadUnitBuild.VERSION_CODE),"Next build accepted");
   check(!info.newer(info.versionCode),"Same versionCode rejected");check(!new UpdateFeed(release(hash,HeadUnitBuild.BUILD)).newer(1),"Same build rejected");
   for(String key:Arrays.asList("schema_version","project_id","build","version","version_code","package_name","apk_url","sha256","size","notes","notes_url","min_sdk")){
    JSONObject j=release(hash,next);j.remove(key);rejects(()->new UpdateFeed(j),"Missing "+key);
   }
   String tag="v5.5.0-headunit-test."+next,asset="Spotify-5.5.0-HeadUnit-test."+next+".apk";
   Object[][] invalid={{"schema_version",2},{"project_id","google-maps-for-byd"},{"build",next+0.5},{"build",true},{"version_code",95477L+next+1},{"version_code",true},{"version_code",95477.0+next},
    {"version","test.0"+next},{"version",tag},{"version","test."+(next+1)},{"package_name","com.spotify.music.debug"},{"min_sdk",29},{"min_sdk",27},{"size",0},{"size",UpdateFeed.MAX_SIZE+1},{"size","180000"},
    {"sha256",hash.toUpperCase(Locale.ROOT)},{"sha256",hash.substring(1)},{"notes","x".repeat(2001)},{"notes",JSONObject.NULL},{"extra",1},
    {"apk_url","https://github.com/romanchukg-cloud/google-maps-for-byd/releases/download/test."+next+"/Google-Maps-for-BYD-test."+next+".apk"},
    {"apk_url",RELEASES+"download/test."+next+"/"+asset},{"apk_url","http://github.com/romanchukg-cloud/spotify-android-head-units/releases/download/"+tag+"/"+asset},
    {"apk_url",RELEASES+"download/"+tag+"/"+asset+"?raw=1"},{"apk_url",RELEASES+"download/"+tag+"/other.apk"},{"apk_url",RELEASES.replace("romanchukg-cloud","other")+"download/"+tag+"/"+asset},
    {"notes_url",RELEASES+"tag/test."+next},{"notes_url","https://example.org/"}};
   for(Object[] pair:invalid){JSONObject j=release(hash,next).put((String)pair[0],pair[1]);rejects(()->new UpdateFeed(j),"Invalid "+pair[0]+"="+pair[1]);}
   check(new UpdateFeed(release(hash,next).put("notes","")).notes.isEmpty(),"Empty notes allowed");
   for(String url:Arrays.asList("https://github.com/x","https://objects.githubusercontent.com/x?a=b","https://release-assets.githubusercontent.com:443/x"))check(UpdateFeed.downloadOrigin(url),"Trusted GitHub redirect "+url);
   for(String url:Arrays.asList("http://github.com/x","https://github.com.evil/x","https://user@github.com/x","https://github.com:444/x","https://example.com/x","https://github.com/x#fragment","not a url"))check(!UpdateFeed.downloadOrigin(url),"Untrusted URL rejected "+url);
   File dir=new File(root,"download");int[] calls={0};
   UpdateDownload full=new UpdateDownload(dir,(url,offset)->{calls[0]++;check(url.startsWith(RELEASES+"download/v5.5.0-headunit-test.") && offset==0,"Full download from the feed URL");return response(200,null,DATA);});
   check(Arrays.equals(Files.readAllBytes(full.download(info,new Progress()).toPath()),DATA),"Full verified download");
   full.download(info,new Progress());check(calls[0]==1,"Verified file reused without network");
   // A lost connection leaves the exact private partial file, resumed only with a matching Content-Range.
   Files.delete(full.ready().toPath());int split=70000;
   rejects(()->new UpdateDownload(dir,(url,offset)->response(200,null,Arrays.copyOf(DATA,split))).download(info,new Progress()),"Truncated response fails");
   check(new File(dir,"update.part").length()==split,"Partial kept");
   rejects(()->new UpdateDownload(dir,(url,offset)->response(206,"bytes 1-179999/180000",Arrays.copyOfRange(DATA,split,DATA.length))).download(info,new Progress()),"Mismatched range rejected");
   check(new File(dir,"update.part").length()==split,"Range error keeps the partial file");
   UpdateDownload resumed=new UpdateDownload(dir,(url,offset)->{check(offset==split,"Resume offset");return response(206,"bytes 70000-179999/180000",Arrays.copyOfRange(DATA,split,DATA.length));});
   check(UpdateDownload.hash(resumed.download(info,new Progress())).equals(hash),"Resumed file verified");
   Files.delete(full.ready().toPath());Files.write(new File(dir,"update.part").toPath(),Arrays.copyOf(DATA,split));
   check(new UpdateDownload(dir,(url,offset)->response(200,null,DATA)).download(info,new Progress()).length()==DATA.length,"Ignored Range restarts without appending");
   Files.delete(full.ready().toPath());byte[] corrupt=DATA.clone();corrupt[7]^=1;
   rejects(()->new UpdateDownload(dir,(url,offset)->response(200,null,corrupt)).download(info,new Progress()),"Wrong digest rejected");
   check(!new File(dir,"update.part").exists() && !full.ready().exists(),"Corrupt download discarded");
   rejects(()->new UpdateDownload(dir,(url,offset)->response(200,null,new byte[DATA.length+1])).download(info,new Progress()),"Oversized response rejected");
   check(new File(dir,"update.part").length()<=DATA.length,"Disk writes bounded by the feed size");
   rejects(()->new UpdateDownload(dir,(url,offset)->response(404,null,new byte[0])).download(new UpdateFeed(release(hash,next+5)),new Progress()),"HTTP error rejected");
   Files.deleteIfExists(new File(dir,"update.part").toPath());
   Progress pause=new Progress(){public void update(long n,long total){super.update(n,total);stop=true;}};
   rejects(()->full.download(info,pause),"Pause interrupts");long kept=new File(dir,"update.part").length();check(kept>0 && kept<DATA.length,"Pause keeps a resumable partial file");
   full.download(new UpdateFeed(release(hash,next+1)),new Progress());check(calls[0]==3,"A new release identity starts from zero");
   full.clear();check(dir.list().length==0,"Clear removes staged files");
  }finally{try(java.util.stream.Stream<Path> paths=Files.walk(root.toPath())){paths.sorted(Comparator.reverseOrder()).forEach(p->p.toFile().delete());}}
  // Every language has every string, with the same format arguments as English.
  check(UpdateText.rows()==5,"Five languages");
  Pattern format=Pattern.compile("%(\\d\\$)?[sd%]");
  for(int row=0;row<UpdateText.rows();row++){
   check(UpdateText.columns(row)==UpdateText.keys(),"Complete language row "+row);
  }
  Locale saved=Locale.getDefault();
  try{
   List<String> english=new ArrayList<>();Locale.setDefault(Locale.ENGLISH);
   String[] keys={"current","available","wifi_wait","downloading","ready","installed","ready_title","checked","note","restart"};
   for(String key:keys){List<String> tokens=new ArrayList<>();Matcher m=format.matcher(UpdateText.get(key));while(m.find())tokens.add(m.group());Collections.sort(tokens);english.add(tokens.toString());}
   for(String lang:new String[]{"uk","ru","ar","zh"}){
    Locale.setDefault(new Locale(lang));
    for(int i=0;i<keys.length;i++){List<String> tokens=new ArrayList<>();Matcher m=format.matcher(UpdateText.get(keys[i]));while(m.find())tokens.add(m.group());Collections.sort(tokens);check(tokens.toString().equals(english.get(i)),lang+" format of "+keys[i]);}
    check(UpdateText.format("downloading","test."+next,42).contains("42") && UpdateText.format("available","test."+next,58L).contains("58"),lang+" formats");
    check(!UpdateText.get("updates").equals(UpdateText.get("unknown-key")),lang+" unknown keys fall back");
   }
  }finally{Locale.setDefault(saved);}
  System.out.println("Updater checks passed: "+checks);
 }
}

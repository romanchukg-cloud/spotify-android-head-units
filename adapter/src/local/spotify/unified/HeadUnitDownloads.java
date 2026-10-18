package local.spotify.unified;
/**
 * Offline downloads: Spotify Automotive publishes the download state of a playlist or album as one
 * of its custom browser actions (download, downloading, downloaded) and performs the action it
 * is sent; downloaded items appear under Library → Downloads (pages: HuDetail).
 */
public final class HeadUnitDownloads {
 static final String DOWNLOAD="download",DOWNLOADING="downloading",DOWNLOADED="downloaded";
 private HeadUnitDownloads(){}
}

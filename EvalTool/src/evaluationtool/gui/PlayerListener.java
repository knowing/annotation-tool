package evaluationtool.gui;

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.events.MediaPlayerEventType;


public class PlayerListener implements MediaPlayerEventListener{

	EvalGUI gui;
	
	PlayerListener(EvalGUI gui){
		this.gui = gui;
	}
	
	public void backward(MediaPlayer arg0) {}


	public void buffering(MediaPlayer mp) {
		mp.setMarqueeText("Buffering...");
	}


	public void endOfSubItems(MediaPlayer arg0) {}

	public void error(MediaPlayer mp) {
		System.err.println(mp.mrl() + " could not be played.");
	}

	public void finished(MediaPlayer arg0) {}
	public void forward(MediaPlayer arg0) {}
	public void lengthChanged(MediaPlayer arg0, long arg1) {}
	public void mediaChanged(MediaPlayer arg0) {}
	public void mediaDurationChanged(MediaPlayer arg0, long arg1) {}
	public void mediaFreed(MediaPlayer arg0) {}
	public void mediaMetaChanged(MediaPlayer arg0, int arg1) {}
	public void mediaParsedChanged(MediaPlayer arg0, int arg1) {}
	public void mediaStateChanged(MediaPlayer arg0, int arg1) {}
	public void mediaSubItemAdded(MediaPlayer arg0, libvlc_media_t arg1) {}
	public void newMedia(MediaPlayer mp) {}
	public void opening(MediaPlayer arg0) {}
	public void pausableChanged(MediaPlayer arg0, int arg1) {}
	public void paused(MediaPlayer arg0) {}
	public void playing(MediaPlayer arg0) {}
	public void positionChanged(MediaPlayer arg0, float arg1) {}
	public void seekableChanged(MediaPlayer arg0, int arg1) {}
	public void snapshotTaken(MediaPlayer arg0, String arg1) {}
	public void stopped(MediaPlayer arg0) {}
	public void subItemFinished(MediaPlayer arg0, int arg1) {}
	public void subItemPlayed(MediaPlayer arg0, int arg1) {}
	public void timeChanged(MediaPlayer arg0, long arg1) {}
	public void titleChanged(MediaPlayer arg0, int arg1) {}
	public void videoOutput(MediaPlayer arg0, int arg1) {}

}

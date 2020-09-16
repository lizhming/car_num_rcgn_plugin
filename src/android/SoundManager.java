package com.cardcam.scantrans;


import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;


// nib 03/15/13 changed channel : STREAM_RING -> STREAM_MUSIC

public class SoundManager {

	private  SoundPool mSoundPool=null;
	private  AudioManager  mAudioManager;
	private  int mSoundID;

	public SoundManager(Context context) {
		mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);

		mSoundID = mSoundPool.load(context, context.getResources().getIdentifier("qtranslator", "raw", context.getPackageName()), AudioManager.STREAM_MUSIC);
		mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
	}

	public void playSound() {
		if (mSoundPool == null || mAudioManager == null) return;
		// nib 05/10/13 if virate or silent mode in system, then be quite!
		int ringerMode = mAudioManager.getRingerMode();

		if (ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
			return;
		}
		// --------------------------
		int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

		mSoundPool.play(mSoundID, streamVolume, streamVolume, 1, 0, 1f);
	}

	public void stop() {
		if (mSoundPool == null) return;
		mSoundPool.stop(mSoundID);
	}

	public void dispose() {
		if (mSoundPool == null) return;

		mSoundPool.release();
		mSoundPool = null;
		mAudioManager = null;
	}
}
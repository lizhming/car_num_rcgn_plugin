
package com.cardcam.scantrans;

import java.util.concurrent.CountDownLatch;

import com.cardcam.camera.CameraControl;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;


public final class MessageHandler extends Handler {

	private CameraActivity parent;

	class PreviewHandler extends Handler {
		private boolean running = true;
		PreviewHandler(CameraActivity activity) {
			parent = activity;
		}

		@Override
		public void handleMessage(Message message) {
			if (!running) {
				return;
			}
			if (message.what == parent.getResources().getIdentifier("preview_raw", "id", parent.getPackageName())) {
				parent.previewProc((byte[]) message.obj, message.arg1, message.arg2);
			} else if (message.what == parent.getResources().getIdentifier("quit", "id", parent.getPackageName())) {
				running = false;
				Looper.myLooper().quit();
			}
		}

	}


	class PreviewThread extends Thread {

		private Handler handler;
		private final CountDownLatch handlerInitLatch;

		PreviewThread(CameraActivity activity, String characterSet) {

			parent = activity;
			handlerInitLatch = new CountDownLatch(1);
		}

		Handler getHandler() {
			try {
				handlerInitLatch.await();
			} catch (InterruptedException ie) {
				// continue?
			}
			return handler;
		}

		@Override
		public void run() {
			Looper.prepare();
			handler = new PreviewHandler(parent);
			handlerInitLatch.countDown();
			Looper.loop();
		}

	}

	private PreviewThread previewThread;

	public MessageHandler(CameraActivity activity, String characterSet) {
		parent = activity;

		previewThread = new PreviewThread(activity, characterSet);
		previewThread.start();

		CameraControl.camControl.autoFocusMng.autoFocusing=0;
		CameraControl.camControl.requestPreviewFrame(previewThread.getHandler(), parent.getResources().getIdentifier("preview_raw", "id", parent.getPackageName()));
		parent.ViewInvalid();
	}

	@Override
	public void handleMessage(Message message) {

		int idv=message.what;

		if (idv == parent.getResources().getIdentifier("change_layput", "id", parent.getPackageName())) {

		} else if (idv == parent.getResources().getIdentifier("auto_focus", "id", parent.getPackageName())) {
			CameraControl.camControl.requestAutoFocus(this, idv);
		} else if (idv == parent.getResources().getIdentifier("recover_focus", "id", parent.getPackageName())) {

			CameraControl.camControl.recover_focus(this, idv);
		} else if (idv == parent.getResources().getIdentifier("surface_change", "id", parent.getPackageName())) {

			parent.Do_surfaceChange();
		} else if (idv == parent.getResources().getIdentifier("restart_preview", "id", parent.getPackageName())) {

		} else if (idv == parent.getResources().getIdentifier("restart_camera", "id", parent.getPackageName())) {
			parent.CheckScreenDirection(0);
			CameraControl.camControl.ChgPreviewResolution(parent.ScreenDirection);
			CameraControl.camControl.requestPreviewFrame(previewThread.getHandler(), parent.getResources().getIdentifier("preview_raw", "id", parent.getPackageName()));

			if ((parent.Tch_flashMode & parent.TchVal_Flash)!= 0)
				CameraControl.camControl.mCameraFlash(1);

			CameraControl.camControl.requestAutoFocus(this, parent.getResources().getIdentifier("auto_focus", "id", parent.getPackageName()));
			parent.ViewInvalid();
		} else if (idv == parent.getResources().getIdentifier("chkMessage", "id", parent.getPackageName())) {

			parent.check_MsgHandle();
			parent.ViewInvalid();
		} else if (idv == parent.getResources().getIdentifier("chk_chgScreen", "id", parent.getPackageName())) {
			//parent.check_CamPreviewScreen();
		} else if (idv == parent.getResources().getIdentifier("preview_raw", "id", parent.getPackageName())) {
			CameraControl.camControl.requestPreviewFrame(previewThread.getHandler(), idv);
			parent.ViewInvalid();
		} else if (idv == parent.getResources().getIdentifier("next_raw", "id", parent.getPackageName())) {
			CameraControl.camControl.requestPreviewFrame(previewThread.getHandler(), parent.getResources().getIdentifier("preview_raw", "id", parent.getPackageName()));
		} else if (idv == parent.getResources().getIdentifier("zoom_in", "id", parent.getPackageName())) {
			KeyEvent event_in = new KeyEvent(MotionEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_VOLUME_UP);
			parent.onKeyDown(KeyEvent.KEYCODE_VOLUME_UP, event_in);
			sendEmptyMessageDelayed(idv, 100);
		} else if (idv == parent.getResources().getIdentifier("zoom_out", "id", parent.getPackageName())) {
			KeyEvent event_out = new KeyEvent(MotionEvent.ACTION_DOWN,
					KeyEvent.KEYCODE_VOLUME_DOWN);
			parent.onKeyDown(KeyEvent.KEYCODE_VOLUME_DOWN, event_out);
			sendEmptyMessageDelayed(idv, 100);
		}
	}

	public void quitSynchronously() {
		removeMessages(parent.getResources().getIdentifier("auto_focus", "id", parent.getPackageName()));
		removeMessages(parent.getResources().getIdentifier("preview_raw", "id", parent.getPackageName()));
		removeMessages(parent.getResources().getIdentifier("chkMessage", "id", parent.getPackageName()));
	}

	public void RequestPreviewDecode() {
		CameraControl.camControl.requestPreviewFrame(previewThread.getHandler(), parent.getResources().getIdentifier("preview_raw", "id", parent.getPackageName()));
	}
}

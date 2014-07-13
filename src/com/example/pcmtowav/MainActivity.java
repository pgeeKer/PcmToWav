package com.example.pcmtowav;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;

public class MainActivity extends Activity {

	private Button button;
	private ProgressDialog pDialog = null;
	private Handler mHandler = new Handler();

	@SuppressLint("SdCardPath")
	public static final String SDCARDPATH = "tts_LZL_voice/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		button = (Button) findViewById(R.id.start_btn);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				startDialog();
				String filePath = getFilePath();
				String targetPath = android.os.Environment
						.getExternalStorageDirectory() + "/" + "1.wav";
				startPcmToWav(filePath, targetPath);
			}
		});
	}

	private String getFilePath() {
		String path = "";
		if (Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			path = Environment.getExternalStorageDirectory() + "/";
		}
		path += SDCARDPATH + "tts_0.pcm";
		return path;

	}

	private void startDialog() {
		if (pDialog != null) {
			return;
		}

		mHandler.post(new Runnable() {

			@Override
			public void run() {
				pDialog = new ProgressDialog(MainActivity.this);
				pDialog.setTitle("提示");
				pDialog.setMessage("转换中...");
				pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(true);
				pDialog.show();
			}
		});
	}

	private void startPcmToWav(String src, String target) {

		File file = new File(src);
		if (file != null) {
			try {
				@SuppressWarnings("resource")
				FileInputStream inputStream = new FileInputStream(file);
				// 计算长度
				byte[] buf = new byte[1024 * 100];
				int size = inputStream.read(buf);
				int pcmSize = 0;
				while (size != -1) {
					pcmSize += size;
					size = inputStream.read(buf);
				}
				inputStream.close();
				// 填入参数，比特率等。16位双声道，8000HZ
				WaveHeader header = new WaveHeader();
				// 长度字段 = 内容的大小（PCMSize) + 头部字段的大小
				// (不包括前面4字节的标识符RIFF以及fileLength本身的4字节
				header.fileLength = pcmSize + (44 - 8);
				header.FmtHdrLeth = 16;
				header.BitsPerSample = 16;
				header.Channels = 2;
				header.FormatTag = 0x0001;
				header.SamplesPerSec = 8000;
				header.BlockAlign = (short) (header.Channels
						* header.BitsPerSample / 8);
				header.AvgBytesPerSec = header.BlockAlign
						* header.SamplesPerSec;
				header.DataHdrLeth = pcmSize;

				byte[] h = header.getHeader();
				assert h.length == 44; // WAV标准，头部应该是44字节

				File targetFile = new File(target);
				if (!targetFile.exists()) {
					targetFile.createNewFile();
				}
				FileOutputStream outputStream = new FileOutputStream(targetFile);
				inputStream = new FileInputStream(file);
				byte[] buffer = new byte[1024 * 100];
				int tardetSize = inputStream.read(buffer);
				outputStream.write(h, 0, h.length);
				while (tardetSize != -1) {
					outputStream.write(buffer, 0, tardetSize);
					tardetSize = inputStream.read(buffer);
				}
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		stopProgressDialog();
	}

	private void stopProgressDialog() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				if (pDialog != null) {
					pDialog.dismiss();
					pDialog = null;
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		System.exit(0);
		super.onDestroy();
	}
}

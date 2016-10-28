package com.geone.inspect.threepart_ts.adapter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import com.geone.inspect.threepart_ts.activity.AppApplication;
import com.geone.inspect.threepart_ts.R;
import com.geone.inspect.threepart_ts.bean.Layer;
import com.geone.inspect.threepart_ts.sql.MyDatabase;
import com.geone.inspect.threepart_ts.util.CalendarUtils;
import com.geone.inspect.threepart_ts.util.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 *   离线包下载页面的数据适配器
 */
public class LayerListAdapter extends BaseAdapter implements OnClickListener {

	private Context mContext;
	private ArrayList<Layer> layerList = null;
	public ArrayList<String> checkedItemIDs;
	public HashMap<ImageButton, DownloadTask> btn_task_map = new HashMap<ImageButton, LayerListAdapter.DownloadTask>();;
	private MyDatabase myDatabase;
	public ListView mListView;

	private static Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			ViewHolder mHolder = (ViewHolder) msg.obj;
			ProgressBar pb = mHolder.pb_downloading;
			TextView tv = mHolder.tv_info;

			int progress = msg.arg1;
			pb.setProgress(progress);
			if (mHolder.layer.download_status != Layer.DOWNLOADING) {
				mHolder.pb_downloading.setVisibility(View.GONE);
				return;
			}
			tv.setText("(下载中 " + progress + "%)");
		};
	};

	public LayerListAdapter(Context context, ArrayList<Layer> objects,
			ArrayList<String> checkedIDs) {
		this.layerList = objects;
		this.checkedItemIDs = checkedIDs;
		this.mContext = context;

		myDatabase = new MyDatabase(context);
	}

	@Override
	public boolean isEnabled(int position) {
		// if (getItem(position) instanceof String) { // 分隔cell
		// return false;
		// }
		// return super.isEnabled(position);
		return false;// 设置item不可点击
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		Object item = getItem(position);
		holder = new ViewHolder();
		Layer mLayer = (Layer) item;
		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.group_list_item, null);
			holder.iv_new_icon = (ImageView) convertView
					.findViewById(R.id.iv_new_icon);
			holder.tv_layer_title = (TextView) convertView
					.findViewById(R.id.group_list_item_text);
			holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
			holder.tv_info = (TextView) convertView.findViewById(R.id.tv_info);
			holder.pb_downloading = (ProgressBar) convertView
					.findViewById(R.id.pb_downloading);
			holder.btn_download = (ImageButton) convertView
					.findViewById(R.id.ib_download);
			holder.btn_cancel_download = (ImageButton) convertView
					.findViewById(R.id.ib_cancel);
			holder.btn_delete = (ImageButton) convertView
					.findViewById(R.id.ib_delete);
			holder.iv_check = (ImageView) convertView
					.findViewById(R.id.iv_checkmark);
			holder.ckbox_check = (CheckBox) convertView
					.findViewById(R.id.ck_checkmark);
			holder.btn_download.setOnClickListener(this);
			holder.btn_cancel_download.setOnClickListener(this);
			holder.btn_delete.setOnClickListener(this);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.layer = mLayer;
		holder.position = position;
		holder.btn_download.setTag(holder);
		holder.btn_cancel_download.setTag(holder);
		holder.btn_delete.setTag(position);

		// convertView.setTag(holder);

		if (item instanceof Layer) {
			holder.tv_layer_title.setText(mLayer.name);
			boolean hasOffline = false;
			if (mLayer.offline_url != null && !"".equals(mLayer.offline_url)) {
				hasOffline = true;
				String fileSize = String.format("%.2f", mLayer.offline_size);
				String dateString = mLayer.offline_time + "";
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd",
						Locale.getDefault());
				String formattedTime;
				try {
					Date date = (Date) formatter.parse(dateString);
					formattedTime = CalendarUtils
							.formatDate(date, "yyyy/MM/dd");
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					formattedTime = dateString;
				}
				holder.tv_size.setTextColor(mContext.getResources().getColor(
						android.R.color.secondary_text_dark));
				holder.tv_size.setText("时间: " + formattedTime + ". 大小: "
						+ fileSize + "MB");
			}

			if (hasOffline) {
				// 已下载
				if (mLayer.download_status == Layer.DOWNLOADED) {
					showStatus_Downloaded(holder);
					if (mLayer.hasUpdate) {
						showUpdates(holder);
					}
				} else if (mLayer.download_status == Layer.DOWNLOADING) { // 下载中
					if (!holder.pb_downloading.isShown()) {
						showStatus_Downloading(holder);
					}
				} else { // 未下载
					showStatus_NotDownload(holder);
				}
			} else {
				showStatus_NoOfflineLayer(holder);
			}

		}
		return convertView;

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return layerList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return layerList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private void showUpdates(ViewHolder holder) {
		holder.tv_size.setVisibility(View.VISIBLE);
		holder.btn_download.setVisibility(View.VISIBLE);
		holder.tv_info.setTextColor(mContext.getResources().getColor(
				R.color.fontRed));
		holder.tv_info.setText("(有更新)");
		holder.btn_delete.setVisibility(View.GONE);
	}

	private void addDownloadRecord(Layer layer) {
		myDatabase.insertOrUpdateOfflineLayer(layer);
	}

	/**
	 * 更新列表
	 */
	private void updateData(ViewHolder holder) {
		layerList.set(holder.position, holder.layer);
		notifyDataSetChanged();
	}

	private void updateData(int pos, Layer layer) {
		layerList.set(pos, layer);
		notifyDataSetChanged();
	}

	private void deleteItem(int pos) {
		Layer mLayer = (Layer) getItem(pos);
		int rows = myDatabase.deleteOfflineLayerById(mLayer.id);
		if (rows > 0) {
			mLayer.download_status = Layer.NOT_DOWNLOAD;
			String PATH = Utils.getSDCardPath() + File.separator
					+ AppApplication.APP_ID + File.separator
					+ AppApplication.LOCAL_LAYERS + File.separator
					+ mLayer.offline_name;
			File f = new File(PATH);
			f.delete();
			updateData(pos, mLayer);
		}

	}

	/** 根据不同的状态，显示不同的内容 */
	private void showStatus_NoOfflineLayer(ViewHolder holder) {
		holder.btn_delete.setVisibility(View.GONE);
		holder.pb_downloading.setVisibility(View.GONE);
		holder.btn_cancel_download.setVisibility(View.GONE);
		holder.btn_download.setVisibility(View.GONE);
		holder.tv_size.setVisibility(View.GONE);
		holder.tv_info.setVisibility(View.GONE);

	}

	private void showStatus_NotDownload(ViewHolder holder) {
		holder.btn_delete.setVisibility(View.GONE);
		holder.pb_downloading.setVisibility(View.GONE);
		holder.btn_cancel_download.setVisibility(View.GONE);
		holder.btn_download.setVisibility(View.VISIBLE);
		holder.tv_size.setVisibility(View.VISIBLE);
		holder.tv_info.setVisibility(View.VISIBLE);
		holder.tv_info.setTextColor(mContext.getResources().getColor(
				R.color.fontLRed));
		holder.tv_info.setText("(未下载)");

	}

	private void showStatus_Downloading(ViewHolder holder) {
		holder.btn_delete.setVisibility(View.GONE);
		holder.btn_cancel_download.setVisibility(View.VISIBLE);
		holder.pb_downloading.setVisibility(View.VISIBLE);
		holder.btn_download.setVisibility(View.GONE);
		holder.tv_size.setVisibility(View.VISIBLE);
		holder.tv_info.setVisibility(View.VISIBLE);
		holder.tv_info.setTextColor(mContext.getResources().getColor(
				R.color.fontYellow));
		holder.tv_info.setText("(下载中)");

	}

	private void showStatus_Downloaded(ViewHolder holder) {
		holder.btn_delete.setVisibility(View.VISIBLE);
		holder.btn_cancel_download.setVisibility(View.GONE);
		holder.pb_downloading.setVisibility(View.GONE);
		holder.btn_download.setVisibility(View.GONE);
		holder.tv_size.setVisibility(View.VISIBLE);
		holder.tv_info.setVisibility(View.VISIBLE);
		holder.tv_info.setTextColor(mContext.getResources().getColor(
				R.color.fontBlue));
		holder.tv_info.setText("(已下载)");
	}

	public class ViewHolder implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3905803696727656462L;

		public ImageView iv_new_icon;
		public TextView tv_layer_title;
		public TextView tv_size;
		public TextView tv_info;
		public ProgressBar pb_downloading;
		public ImageButton btn_download;
		public ImageButton btn_cancel_download;
		public ImageButton btn_delete;
		public ImageView iv_check;
		public CheckBox ckbox_check;

		public int position;
		public Layer layer;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ib_download:
			ViewHolder holder = (ViewHolder) v.getTag();
			holder.layer.download_status = Layer.DOWNLOADING;
			DownloadTask downTask = new DownloadTask();
			// 并行执行
			downTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder);
			// downTask.execute(holder);
			btn_task_map.put(holder.btn_cancel_download, downTask);
			updateData(holder);
			break;
		case R.id.ib_cancel:
			ImageButton mImageButton = (ImageButton) v;
			DownloadTask mDownloadTask = btn_task_map.remove(mImageButton);
			if (mDownloadTask != null) {
				mDownloadTask.cancel(true);
			}
			mHandler.removeMessages(0);
			ViewHolder viewHolder = (ViewHolder) v.getTag();
			viewHolder.layer.download_status = Layer.NOT_DOWNLOAD;
			String PATH = Utils.getSDCardPath() + File.separator
					+ AppApplication.APP_ID + File.separator
					+ AppApplication.LOCAL_LAYERS + File.separator
					+ viewHolder.layer.offline_name;
			File f = new File(PATH);
			f.delete();
			updateData(viewHolder);
			break;
		case R.id.ib_delete:
			int pos = (Integer) v.getTag();
			buildDialogSureToDelete(mContext, pos).show();
			break;

		default:
			break;
		}

	}

	private Dialog buildDialogSureToDelete(final Context context, final int pos) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(R.string.hint);
		builder.setMessage(R.string.sure_to_delete);
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteItem(pos);
					}
				});
		builder.setNegativeButton(R.string.alert_dialog_cancel,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		return builder.create();
	}

	private class DownloadTask extends AsyncTask<Object, Integer, Integer> {
		int fileLength = 0;
		String fileName = "";
		ProgressBar pb = null;
		ViewHolder viewHolder = null;

		@Override
		protected Integer doInBackground(Object... params) {
			InputStream input;
			try {
				viewHolder = (ViewHolder) params[0];
				Log.d("LayerListAdapter", "doInBackground: "
						+ viewHolder.layer.name);
				pb = viewHolder.pb_downloading;

				URL url = new URL(viewHolder.layer.offline_url);
				URLConnection connection = url.openConnection();
				connection.connect();
				// this will be useful so that you can show a typical 0-100%
				// progress bar
				fileLength = connection.getContentLength();
				fileName = viewHolder.layer.offline_name;
				String PATH = Utils.getSDCardPath() + File.separator
						+ AppApplication.APP_ID + File.separator
						+ AppApplication.LOCAL_LAYERS + File.separator;
				File f = new File(PATH);
				if (!f.exists()) {
					f.mkdirs();
				}
				pb.setProgress(0);
				pb.setMax(100);
				// download the file
				input = new BufferedInputStream(url.openStream());
				File outputFile = new File(f, fileName);
				FileOutputStream fos = new FileOutputStream(outputFile);
				byte data[] = new byte[1024];
				int total = 0;
				int count = 0;
				while ((count = input.read(data)) != -1) {

					total += count;

					int percentage = (int) ((double) total / fileLength * 100);
					fos.write(data, 0, count);
					publishProgress(percentage);

					if (isCancelled()) {
						break;
					}
				}
				fos.close();
				input.close();
			} catch (Exception e) {
				// e.printStackTrace();
				return -1;
			}
			return fileLength;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			if (viewHolder != null) {
				viewHolder.pb_downloading.setVisibility(View.GONE);
			}

			// if (viewHolder != null) {
			// viewHolder.pb_downloading.setVisibility(View.GONE);
			// viewHolder.btn_cancel_download.setVisibility(View.GONE);
			// viewHolder.btn_download.setVisibility(View.VISIBLE);
			// if (viewHolder.layer.hasUpdate) {
			// viewHolder.iv_new_icon.setVisibility(View.VISIBLE);
			// }
			// }
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			// Log.d("LayerListAdapter", "onProgressUpdate: "
			// + viewHolder.layer.name + "--- position: "
			// + viewHolder.position);
			if (isCancelled()) {
				mHandler.removeMessages(0);
			}
			// if (!pb.isShown()) {
			// pb.setVisibility(View.VISIBLE);
			// }
			Message m = mHandler.obtainMessage(0);
			m.arg1 = progress[0];
			m.obj = viewHolder;

			mHandler.sendMessage(m);

		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			btn_task_map.remove(viewHolder.btn_cancel_download);
			if (result == -1) {
				Toast.makeText(mContext, "下载出现问题，下载失败.", Toast.LENGTH_SHORT)
						.show();
				// viewHolder.pb_downloading.setVisibility(View.GONE);
				// viewHolder.btn_cancel_download.setVisibility(View.GONE);
				// viewHolder.btn_download.setVisibility(View.VISIBLE);
				viewHolder.layer.download_status = Layer.NOT_DOWNLOAD;
				updateData(viewHolder);
				return;
			}
			Toast.makeText(mContext, viewHolder.layer.name + "离线包已下载完毕.",
					Toast.LENGTH_SHORT).show();

			viewHolder.layer.download_status = Layer.DOWNLOADED;
			viewHolder.layer.hasUpdate = false;

			addDownloadRecord(viewHolder.layer);
			updateData(viewHolder);
			// viewHolder.pb_downloading.setVisibility(View.GONE);
			// viewHolder.iv_check.setVisibility(View.VISIBLE);
			// viewHolder.btn_cancel_download.setVisibility(View.GONE);
			// viewHolder.btn_download.setVisibility(View.GONE);
			// viewHolder.tv_info.setTextColor(mContext.getResources().getColor(
			// R.color.fontBlue));
			// viewHolder.tv_info.setText("(已下载)");

			// 下载完成以后
			// Message msg = MainActivity.postHandler
			// .obtainMessage(MainActivity.UPDATE_LAYER);
			// msg.obj = viewHolder.layer;
			// MainActivity.postHandler.sendMessage(msg);

		}
	}

}

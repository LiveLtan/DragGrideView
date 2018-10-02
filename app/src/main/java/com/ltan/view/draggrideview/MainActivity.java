package com.ltan.view.draggrideview;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.ltan.view.draggrideview.DragGridView.DataObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ltan/MainActivity";
    private DragGridView mDragGridView;
    private DragGridAdapter mDragGridAdapter;

    private class DragGridAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<DragGridView.DataObject> mDatas;

        public DragGridAdapter(Context context) {
            super();
            mContext = context;
            mDatas = new ArrayList<DataObject>();
        }

        public void updateData(ArrayList<DataObject> newDatas) {
            mDatas.clear();
            mDatas.addAll(newDatas);
            notifyDataSetChanged();
        }

        public void release() {
            mDatas.clear();
            mDatas = null;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public int getCount() {
            return mDatas == null ? 0 : mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vholder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item, null, false);
                vholder = new ViewHolder();
                vholder.mTitleView = (TextView) convertView.findViewById(R.id.id_grid_title);
                vholder.mIconView = (ImageView) convertView.findViewById(R.id.id_grid_icon);
                convertView.setTag(vholder);
            } else {
                vholder = (ViewHolder) convertView.getTag();
            }
            DataObject itemData = mDatas.get(position);
            vholder.mIconView.setImageDrawable(itemData.getIcon());
            vholder.mTitleView.setText(itemData.getLabel());
            return convertView;
        }
    }

    private class ViewHolder {
        private TextView mTitleView;
        private ImageView mIconView;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDragGridView = (DragGridView) findViewById(R.id.id_drag_grid_view);
        init();
    }

    private void init() {
        mDragGridAdapter = new DragGridAdapter(this);
        mDragGridView.setAdapter(mDragGridAdapter);
    }

    private void initData() {
        //int[] ids = getResources().getIntArray(R.array.icon_drawable_res_id);
        int[] ids = new int[] {
                R.drawable.icons8_cloud_checked_48, R.drawable.icons8_facebook_messenger_48,
                R.drawable.icons8_happy_48, R.drawable.icons8_instagram_48, R.drawable.icons8_line_48,
                R.drawable.icons8_linux_48, R.drawable.icons8_location_48, R.drawable.icons8_nfc_48,
                R.drawable.icons8_playstore_48, R.drawable.icons8_rockstar_games_48, R.drawable.icons8_snapchat_48,
                R.drawable.icons8_speaker_48, R.drawable.icons8_system_report_48, R.drawable.icons8_whatsapp_48,
                R.drawable.icons8_rotate_right_48, R.drawable.icons8_circled_6_48, R.drawable.icons8_circled_t_48,
                R.drawable.icons8_speech_48,
        };
        String[] names = getResources().getStringArray(R.array.icon_drawable_res_name);
        if (ids.length != names.length) {
            Log.e(TAG, "initData: error: length of ids and name are not equal:" + ids.length + "vs" + names.length);
            return;
        }
        ArrayList<DataObject> datas = new ArrayList<DataObject>();
        for (int i = 0; i < ids.length; i++) {
            DataObject item = new DataObject();
            Log.v(TAG, "initData: label:" + names[i] + ", resId:" + ids[i]);
            item.setIcon(getResources().getDrawable(ids[i], getTheme()));
            //icons8_xxx_48, choose xxx
            item.setLabel(names[i].substring(7, names[i].length() - 3));
            datas.add(item);
        }
        mDragGridAdapter.updateData(datas);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDragGridAdapter.release();
    }

}

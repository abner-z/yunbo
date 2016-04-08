package com.qtfreet.yunbo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.qtfreet.yunbo.R;
import com.qtfreet.yunbo.bean.SearchBean;
import com.qtfreet.yunbo.jni.jni;
import com.qtfreet.yunbo.ui.adapter.listAdapter;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    @Bind(R.id.list_item)
    ListView listView;
    List<SearchBean> result = new ArrayList<>();
    listAdapter listAdapter;
    @Bind(R.id.btn_search)
    Button btn_search;
    @Bind(R.id.search)
    EditText text_search;
    @Bind(R.id.pb_search_wait)
    ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initview();
    }

    private void initview() {
        ButterKnife.bind(this);
        listAdapter = new listAdapter(this, result);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);
        btn_search.setOnClickListener(this);

    }


    private void search(String name) {
        OkHttpUtils.get().url("http://xr.zawr.cn/api/search.php?word=" + name + "&page=1").build().connTimeOut(10000).readTimeOut(10000).writeTimeOut(10000).execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                Message msg = Message.obtain();
                msg.obj = response;
                msg.what = 0;
                handler.sendMessage(msg);

            }
        });
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    SearchBean searchBean = null;
                    String[] r = msg.obj.toString().split("<br>");
                    for (int i = 0; i < r.length; i++) {
                        searchBean = new SearchBean();
                        String hash = r[i].substring(r[i].indexOf("Hash:") + 5, r[i].indexOf("--Name:"));
                        String name = r[i].substring(r[i].indexOf("Name:") + 5, r[i].indexOf(";xrvideo"));
                        searchBean.setHash(hash);
                        searchBean.setName(name);
                        result.add(searchBean);
                    }
                    listAdapter.notifyDataSetChanged();
                    break;
                case 1:
                    progressbar.setVisibility(View.GONE);
                    Log.e("TAG", msg.obj.toString());
                    if (!msg.obj.toString().contains("Success")) {
                        Toast.makeText(MainActivity.this, "获取连接失败", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    String obj = msg.obj.toString();
                    String url = obj.substring(obj.indexOf("Success#") + 8, obj.lastIndexOf("#"));
                    String cookie = obj.substring(obj.lastIndexOf("#") + 1);
                    Intent i = new Intent(MainActivity.this, VideoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(VideoActivity.NAME, name);
                    bundle.putString(VideoActivity.HASH, url);
                    bundle.putString(VideoActivity.COOKIE, cookie);
                    i.putExtras(bundle);
                    startActivity(i);
                    break;
            }

            return true;
        }
    });
    String name = "";

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String hash = result.get(position).getHash();
        progressbar.setVisibility(View.VISIBLE);
        name = result.get(position).getName();
        getVideoUrl(hash);
    }

    private void getVideoUrl(String hash) {
        OkHttpUtils.get().url("http://182.254.244.117:8080/CloudPlayer131/geturl?hash=" + hash + "&index=0").build().connTimeOut(10000).readTimeOut(10000).writeTimeOut(10000).execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                Message msg = Message.obtain();
                msg.obj = response;
                msg.what = 1;
                handler.sendMessage(msg);
                Log.e("TAG", response);

            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                result.clear();

                String name = text_search.getText().toString().trim();
                if (name.equals("")) {
                    return;
                }
                search(name);
                break;
        }
    }
}

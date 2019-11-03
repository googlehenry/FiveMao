package com.ex.fivemao.ui;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ex.fivemao.R;

/**
 * Created by smyhvae on 2015/5/4.
 */
public class MyBoardMsgAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Map<String,Object>> mDatas;

    //MyAdapter需要一个Context，通过Context获得Layout.inflater，然后通过inflater加载item的布局
    public MyBoardMsgAdapter(Context context, List<Map<String,Object>> datas) {

        mInflater = LayoutInflater.from(context);
        mDatas = datas;
    }

    //返回数据集的长度
    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //这个方法才是重点，我们要为它编写一个ViewHolder
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.items_messages, parent, false); //加载布局
            holder = new ViewHolder();

            holder.itemContent = (TextView) convertView.findViewById(R.id.itemContent);
            holder.itemCreatedTime = (TextView) convertView.findViewById(R.id.itemCreatedTime);

            convertView.setTag(holder);
        } else {   //else里面说明，convertView已经被复用了，说明convertView中已经设置过tag了，即holder
            holder = (ViewHolder) convertView.getTag();
        }
        
        Map<String,Object> bean = mDatas.get(position);
        
        //set style
        String createdTime = String.valueOf(bean.get("createdTime"));//YYYY-MM-DD
        if(createdTime!=null){
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        	String nowDate = sdf.format(new Date());
        	if(createdTime.equals(nowDate)){
        		holder.itemCreatedTime.setText(createdTime+"(新)");
        	}
        }
        //style
        String itemContent = String.valueOf(bean.get("content"));
//        if(itemContent.contains("[求片]")){
//        	holder.itemContent.setTextColor(Color.rgb(220, 20, 30));
//        }else if(itemContent.contains("[购买]")){
//        	holder.itemContent.setTextColor(Color.rgb(20, 220, 30));
//        }else{
//        	holder.itemContent.setTextColor(Color.rgb(20, 20, 30));
//        }
        
        //value
        if(itemContent.contains("《") && itemContent.contains("》")){
        	int start = itemContent.indexOf("《")+1;
        	int end = itemContent.lastIndexOf("》");
        	if(end-start>1){
        		String film = itemContent.substring(start, end);
        		itemContent = itemContent.replace(film, "<a href='###'>"+film+"</a>");
        	}
        }
        if(itemContent.contains("【") && itemContent.contains("】")){
        	int start = itemContent.indexOf("【")+1;
        	int end = itemContent.lastIndexOf("】");
        	if(end-start>1){
        		String film = itemContent.substring(start, end);
        		itemContent = itemContent.replace(film, "<a href='###'>"+film+"</a>");
        	}
        }
        
        
        holder.itemCreatedTime.setText(Html.fromHtml(createdTime));
        holder.itemContent.setText(Html.fromHtml(itemContent
        		.replace("[求片]", "<font color='DarkGreen'>[求片]</font>")
        		.replace("[购买]", "<font color='red'>[购买]</font>")
        		.replace("[中奖]", "<font color='blue'>[中奖]</font>")));
        
        return convertView;
    }

    //这个ViewHolder只能服务于当前这个特定的adapter，因为ViewHolder里会指定item的控件，不同的ListView，item可能不同，所以ViewHolder写成一个私有的类
    private class ViewHolder {
        public TextView itemContent;
		public TextView itemCreatedTime;
    }

}
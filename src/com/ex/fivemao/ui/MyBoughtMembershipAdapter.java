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
public class MyBoughtMembershipAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Map<String,Object>> mDatas;

    //MyAdapter需要一个Context，通过Context获得Layout.inflater，然后通过inflater加载item的布局
    public MyBoughtMembershipAdapter(Context context, List<Map<String,Object>> datas) {

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
            convertView = mInflater.inflate(R.layout.items_onedaymembership, parent, false); //加载布局
            holder = new ViewHolder();

            holder.membershipIdx = (TextView) convertView.findViewById(R.id.membershipIdx);
            holder.membershipBuyTitle = (TextView) convertView.findViewById(R.id.membershipBuyTitle);
            holder.membershipStartTime = (TextView) convertView.findViewById(R.id.membershipStartTime);
            holder.membershipEndTime = (TextView) convertView.findViewById(R.id.membershipEndTime);
            holder.membershipStatus = (TextView) convertView.findViewById(R.id.membershipStatus);

            convertView.setTag(holder);
        } else {   //else里面说明，convertView已经被复用了，说明convertView中已经设置过tag了，即holder
            holder = (ViewHolder) convertView.getTag();
        }
        
        Map<String,Object> bean = mDatas.get(position);
        String idx = String.valueOf(bean.get("idx"));
        String ur = String.valueOf(bean.get("subjectNameIfAny"));
        if(ur!=null && ur.replace("null", "").length()>0){
        	idx = ur;
        }
        holder.membershipIdx.setText(idx);
        holder.membershipBuyTitle.setText(String.valueOf(bean.get("buyTitle")==null?"暂无":bean.get("buyTitle")).replace("null", ""));
        holder.membershipStartTime.setText(String.valueOf(bean.get("createdExactTimeStr")==null?"暂无":bean.get("createdExactTimeStr")).replace(" ", "\n").replace("null", ""));
        holder.membershipEndTime.setText(String.valueOf(bean.get("expiryExactTimeStr")==null?"暂无":bean.get("expiryExactTimeStr")).replace(" ", "\n").replace("null", ""));
        String status = String.valueOf(bean.get("status")==null?"暂无":bean.get("status")).replace("null", "");
        if(status.contains("生效中")){
        	status = "<font color='red'>"+status+"</font>";
        }
        holder.membershipStatus.setText(Html.fromHtml(status));
        
        return convertView;
    }

    //这个ViewHolder只能服务于当前这个特定的adapter，因为ViewHolder里会指定item的控件，不同的ListView，item可能不同，所以ViewHolder写成一个私有的类
    private class ViewHolder {
        public TextView membershipIdx;
        public TextView membershipBuyTitle;
		public TextView membershipStartTime;
		public TextView membershipEndTime;
		public TextView membershipStatus;
    }

}
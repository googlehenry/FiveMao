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

    //MyAdapter��Ҫһ��Context��ͨ��Context���Layout.inflater��Ȼ��ͨ��inflater����item�Ĳ���
    public MyBoughtMembershipAdapter(Context context, List<Map<String,Object>> datas) {

        mInflater = LayoutInflater.from(context);
        mDatas = datas;
    }

    //�������ݼ��ĳ���
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

    //������������ص㣬����ҪΪ����дһ��ViewHolder
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.items_onedaymembership, parent, false); //���ز���
            holder = new ViewHolder();

            holder.membershipIdx = (TextView) convertView.findViewById(R.id.membershipIdx);
            holder.membershipBuyTitle = (TextView) convertView.findViewById(R.id.membershipBuyTitle);
            holder.membershipStartTime = (TextView) convertView.findViewById(R.id.membershipStartTime);
            holder.membershipEndTime = (TextView) convertView.findViewById(R.id.membershipEndTime);
            holder.membershipStatus = (TextView) convertView.findViewById(R.id.membershipStatus);

            convertView.setTag(holder);
        } else {   //else����˵����convertView�Ѿ��������ˣ�˵��convertView���Ѿ����ù�tag�ˣ���holder
            holder = (ViewHolder) convertView.getTag();
        }
        
        Map<String,Object> bean = mDatas.get(position);
        String idx = String.valueOf(bean.get("idx"));
        String ur = String.valueOf(bean.get("subjectNameIfAny"));
        if(ur!=null && ur.replace("null", "").length()>0){
        	idx = ur;
        }
        holder.membershipIdx.setText(idx);
        holder.membershipBuyTitle.setText(String.valueOf(bean.get("buyTitle")==null?"����":bean.get("buyTitle")).replace("null", ""));
        holder.membershipStartTime.setText(String.valueOf(bean.get("createdExactTimeStr")==null?"����":bean.get("createdExactTimeStr")).replace(" ", "\n").replace("null", ""));
        holder.membershipEndTime.setText(String.valueOf(bean.get("expiryExactTimeStr")==null?"����":bean.get("expiryExactTimeStr")).replace(" ", "\n").replace("null", ""));
        String status = String.valueOf(bean.get("status")==null?"����":bean.get("status")).replace("null", "");
        if(status.contains("��Ч��")){
        	status = "<font color='red'>"+status+"</font>";
        }
        holder.membershipStatus.setText(Html.fromHtml(status));
        
        return convertView;
    }

    //���ViewHolderֻ�ܷ����ڵ�ǰ����ض���adapter����ΪViewHolder���ָ��item�Ŀؼ�����ͬ��ListView��item���ܲ�ͬ������ViewHolderд��һ��˽�е���
    private class ViewHolder {
        public TextView membershipIdx;
        public TextView membershipBuyTitle;
		public TextView membershipStartTime;
		public TextView membershipEndTime;
		public TextView membershipStatus;
    }

}
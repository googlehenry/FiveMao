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
public class MyAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Map<String,Object>> mDatas;

    //MyAdapter��Ҫһ��Context��ͨ��Context���Layout.inflater��Ȼ��ͨ��inflater����item�Ĳ���
    public MyAdapter(Context context, List<Map<String,Object>> datas) {

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
            convertView = mInflater.inflate(R.layout.items, parent, false); //���ز���
            holder = new ViewHolder();

            holder.itemIcon = (MyImageView) convertView.findViewById(R.id.itemIcon);
            holder.itemTitle = (TextView) convertView.findViewById(R.id.itemTitle);
            holder.itemIdx = (TextView) convertView.findViewById(R.id.itemIdx);
            holder.itemDesc = (TextView) convertView.findViewById(R.id.itemDesc);

            convertView.setTag(holder);
        } else {   //else����˵����convertView�Ѿ��������ˣ�˵��convertView���Ѿ����ù�tag�ˣ���holder
            holder = (ViewHolder) convertView.getTag();
        }
        
        Map<String,Object> bean = mDatas.get(position);
        String title = String.valueOf(bean.get("title"));
        if((bean.get("minimumAge")!=null)&&(!String.valueOf(bean.get("minimumAge")).equalsIgnoreCase("null"))&&Integer.parseInt(String.valueOf(bean.get("minimumAge")))>=18){
        	title = title +"<font color='blue'>(���Ƽ�)</font>";
        }
        
        holder.itemIcon.setImageURL(String.valueOf(String.valueOf(bean.get("coverpageImageLink"))));
        holder.itemTitle.setText(Html.fromHtml(title));
        holder.itemIdx.setText(String.valueOf(bean.get("itemIdx")));
        holder.itemDesc.setText(Html.fromHtml(String.valueOf(bean.get("desc"))));
        
        //set style
        String createdTime = String.valueOf(bean.get("createdTime"));//YYYY-MM-DD
        if(createdTime!=null){
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        	String nowDate = sdf.format(new Date());
        	if(createdTime.equals(nowDate)){
        		holder.itemIdx.setText(holder.itemIdx.getText()+"(��)");
        	}
        }
        
        String desc = String.valueOf(bean.get("desc"));
        if(desc.contains("�ϴο�������")){
        	holder.itemDesc.setTextColor(Color.rgb(220, 20, 30));
        	holder.itemIdx.setTextColor(Color.rgb(220, 20, 30));
        	holder.itemTitle.setTextColor(Color.rgb(220, 20, 30));
        }else{
        	holder.itemIdx.setTextColor(Color.BLACK);
        	holder.itemTitle.setTextColor(Color.BLACK);
        	if(desc.contains("�ѹ���")||desc.contains("һ�ջ�Ա")){
        		holder.itemDesc.setTextColor(Color.rgb(220, 160, 100));
        	}else if(desc.contains("����һ��")){
        		holder.itemDesc.setTextColor(Color.rgb(220, 20, 30));
        	}else{
        		holder.itemDesc.setTextColor(Color.BLACK);
        	}
        }
        
        return convertView;
    }

    //���ViewHolderֻ�ܷ����ڵ�ǰ����ض���adapter����ΪViewHolder���ָ��item�Ŀؼ�����ͬ��ListView��item���ܲ�ͬ������ViewHolderд��һ��˽�е���
    private class ViewHolder {
        public TextView itemDesc;
		public TextView itemIdx;
		public TextView itemTitle;
		public MyImageView itemIcon;
    }

}
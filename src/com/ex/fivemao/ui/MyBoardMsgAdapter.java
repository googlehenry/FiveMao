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

    //MyAdapter��Ҫһ��Context��ͨ��Context���Layout.inflater��Ȼ��ͨ��inflater����item�Ĳ���
    public MyBoardMsgAdapter(Context context, List<Map<String,Object>> datas) {

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
            convertView = mInflater.inflate(R.layout.items_messages, parent, false); //���ز���
            holder = new ViewHolder();

            holder.itemContent = (TextView) convertView.findViewById(R.id.itemContent);
            holder.itemCreatedTime = (TextView) convertView.findViewById(R.id.itemCreatedTime);

            convertView.setTag(holder);
        } else {   //else����˵����convertView�Ѿ��������ˣ�˵��convertView���Ѿ����ù�tag�ˣ���holder
            holder = (ViewHolder) convertView.getTag();
        }
        
        Map<String,Object> bean = mDatas.get(position);
        
        //set style
        String createdTime = String.valueOf(bean.get("createdTime"));//YYYY-MM-DD
        if(createdTime!=null){
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        	String nowDate = sdf.format(new Date());
        	if(createdTime.equals(nowDate)){
        		holder.itemCreatedTime.setText(createdTime+"(��)");
        	}
        }
        //style
        String itemContent = String.valueOf(bean.get("content"));
//        if(itemContent.contains("[��Ƭ]")){
//        	holder.itemContent.setTextColor(Color.rgb(220, 20, 30));
//        }else if(itemContent.contains("[����]")){
//        	holder.itemContent.setTextColor(Color.rgb(20, 220, 30));
//        }else{
//        	holder.itemContent.setTextColor(Color.rgb(20, 20, 30));
//        }
        
        //value
        if(itemContent.contains("��") && itemContent.contains("��")){
        	int start = itemContent.indexOf("��")+1;
        	int end = itemContent.lastIndexOf("��");
        	if(end-start>1){
        		String film = itemContent.substring(start, end);
        		itemContent = itemContent.replace(film, "<a href='###'>"+film+"</a>");
        	}
        }
        if(itemContent.contains("��") && itemContent.contains("��")){
        	int start = itemContent.indexOf("��")+1;
        	int end = itemContent.lastIndexOf("��");
        	if(end-start>1){
        		String film = itemContent.substring(start, end);
        		itemContent = itemContent.replace(film, "<a href='###'>"+film+"</a>");
        	}
        }
        
        
        holder.itemCreatedTime.setText(Html.fromHtml(createdTime));
        holder.itemContent.setText(Html.fromHtml(itemContent
        		.replace("[��Ƭ]", "<font color='DarkGreen'>[��Ƭ]</font>")
        		.replace("[����]", "<font color='red'>[����]</font>")
        		.replace("[�н�]", "<font color='blue'>[�н�]</font>")));
        
        return convertView;
    }

    //���ViewHolderֻ�ܷ����ڵ�ǰ����ض���adapter����ΪViewHolder���ָ��item�Ŀؼ�����ͬ��ListView��item���ܲ�ͬ������ViewHolderд��һ��˽�е���
    private class ViewHolder {
        public TextView itemContent;
		public TextView itemCreatedTime;
    }

}
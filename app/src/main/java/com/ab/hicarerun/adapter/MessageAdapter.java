//package com.ab.hicarerun.adapter;
//
//import android.app.Activity;
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.TextView;
//
//import com.ab.hicarerun.R;
//import com.ab.hicarerun.network.models.MessageModel.Message;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Arjun Bhatt on 5/9/2020.
// */
//public class MessageAdapter extends BaseAdapter {
//
//    List<Message> messages = new ArrayList<Message>();
//    Context context;
//
//    public MessageAdapter(Context context) {
//        this.context = context;
//    }
//
//    public void add(Message message) {
//        this.messages.add(message);
//        notifyDataSetChanged(); // to render the list we need to notify
//    }
//
//    @Override
//    public int getCount() {
//        return messages.size();
//    }
//
//    @Override
//    public Object getItem(int i) {
//        return messages.get(i);
//    }
//
//    @Override
//    public long getItemId(int i) {
//        return i;
//    }
//
//    // This is the backbone of the class, it handles the creation of single ListView row (chat bubble)
//    @Override
//    public View getView(int i, View convertView, ViewGroup viewGroup) {
//        MessageViewHolder holder = new MessageViewHolder();
//        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//        Message message = messages.get(i);
//
//        if (message.isBelongsToCurrentUser()) { // this message was sent by us so let's create a basic chat bubble on the right
//            convertView = messageInflater.inflate(R.layout.layout_my_message, null);
//            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
//            convertView.setTag(holder);
//            holder.messageBody.setText(message.getText());
//        } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
//            convertView = messageInflater.inflate(R.layout.layout_their_message, null);
//            holder.name = (TextView) convertView.findViewById(R.id.name);
//            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
//            convertView.setTag(holder);
//
//            holder.messageBody.setText(message.getText());
//        }
//
//        return convertView;
//    }
//
//}
//
//class MessageViewHolder {
//    public View avatar;
//    public TextView name;
//    public TextView messageBody;
//}
//

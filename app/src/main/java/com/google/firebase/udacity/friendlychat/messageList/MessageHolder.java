package com.google.firebase.udacity.friendlychat.messageList;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.udacity.friendlychat.R;
import com.google.firebase.udacity.friendlychat.model.Message;

public class MessageHolder
        extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {

    final private ItemClickListener clickListener;
    final private ItemClickListener longClickListener;

    private TextView messageTextView, authorTextView;
    private ImageView photoImageView;

    public MessageHolder(View itemView, ItemClickListener clickListener, ItemClickListener longClickListener) {
        super(itemView);
        messageTextView = itemView.findViewById(R.id.messageTextView);
        authorTextView = itemView.findViewById(R.id.nameTextView);
        photoImageView = itemView.findViewById(R.id.photoImageView);

        this.clickListener = clickListener;
        this.longClickListener = longClickListener;

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public void bind(Message message) {
        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            messageTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            Glide.with(photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        authorTextView.setText(message.getName());
    }

    @Override
    public void onClick(View view) {
        int clickedPosition = getAdapterPosition();
        clickListener.onItemClick(clickedPosition);
    }

    @Override
    public boolean onLongClick(View view) {
        int clickedPosition = getAdapterPosition();
        longClickListener.onItemClick(clickedPosition);
        return true;
    }
}

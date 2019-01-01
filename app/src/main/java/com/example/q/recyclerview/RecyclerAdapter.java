package com.example.q.recyclerview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ImageViewHolder> {

    private int[] images;
    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public RecyclerAdapter(int[] images){
        this.images = images;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picture_list_row, parent,false);
        ImageViewHolder imageViewHolder = new ImageViewHolder(view,mListener);

        return imageViewHolder;
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        int image_id = images[position];
        holder.image.setImageResource(image_id);
        holder.imageText.setText("Image: " + position);
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView imageText;

        public ImageViewHolder(View itemView, final OnItemClickListener listener){
            super(itemView);
            image = itemView.findViewById(R.id.imageView2);
            imageText = itemView.findViewById(R.id.image_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    if (listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

}

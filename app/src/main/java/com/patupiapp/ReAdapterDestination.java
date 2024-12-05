package com.patupiapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

public class ReAdapterDestination extends RecyclerView.Adapter<ReAdapterDestination.MyViewHolder> {

    Context context;
    ArrayList<Destination> list;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ViewHolder(View itemView){
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public ReAdapterDestination(Context context, ArrayList<Destination> list) {
        this.context = context;
        this.list = list;
    }

    public void setFilteredList(ArrayList<Destination> filteredList){
        this.list = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_distanation, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Destination destination= list.get(position);
        holder.placeName.setText(destination.getPlace());
        holder.longitude.setText(String.valueOf(destination.getLongitude()));
        holder.lattitude.setText(String.valueOf(destination.getLattitude()));
        holder.info.setText(destination.getInfo());


        String img = destination.getBackground();

        Picasso.get().load(Uri.parse(img)).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                holder.backgroundImg.setBackground(new BitmapDrawable(context.getResources(), bitmap));
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                // Handle failure
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // Handle loading
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return Math.min(list.size(), 100);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView lattitude, longitude, info;
        TextView placeName;
        LinearLayout backgroundImg;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            lattitude = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);
            placeName = itemView.findViewById(R.id.placeloc);
            info = itemView.findViewById(R.id.info);
            backgroundImg = itemView.findViewById(R.id.backgroundLocation);


        }
    }
}

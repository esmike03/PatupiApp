package com.patupiapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(Booking booking);
    }

    public BookingAdapter(Context context, List<Booking> bookingList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.bookingList = bookingList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_item, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        // Bind the booking data to the ViewHolder
        Booking booking = bookingList.get(position);
        holder.userName.setText(booking.getUserName());
        holder.placeName.setText(booking.getPlaceName());
        holder.date.setText(booking.getDate());
        holder.userEmail.setText(booking.getUserEmail());

        // Set click listener for each item
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    // ViewHolder class to hold references to the views
    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView userName, placeName, date, userEmail;

        public BookingViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            placeName = itemView.findViewById(R.id.placeName);
            date = itemView.findViewById(R.id.date);
            userEmail = itemView.findViewById(R.id.userEmail);
        }
    }
}

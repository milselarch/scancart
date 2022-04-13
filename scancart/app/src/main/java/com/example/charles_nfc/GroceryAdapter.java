package com.example.charles_nfc;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class GroceryAdapter extends RecyclerView.Adapter<GroceryAdapter.ContactViewHolder>{
    Context context;
    LayoutInflater inflater;
    Model.DataSource datasource;

    public GroceryAdapter(Context context, Model.DataSource datasource) {
        this.context = context;
        this.datasource = datasource;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View contactView = inflater.inflate(R.layout.cardview, parent, false);
        return new ContactViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.textViewItem.setText(this.datasource.get(position).name);
        holder.textViewCost.setText(this.datasource.get(position).cost);
        holder.textViewId.setText(this.datasource.get(position).tag_id);
        holder.textViewQty.setText(this.datasource.get(position).quantity);
        new DownloadImageTask(new DownloadImageTask.customListener() {
            @Override
            public void postExecute(Bitmap result) {
                holder.imageId.setImageBitmap(result);
            }
        }).execute(this.datasource.get(position).image_url);
    }

    @Override
    public int getItemCount() {
        return this.datasource.count();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder{
        TextView textViewItem;
        TextView textViewCost;
        TextView textViewQty;
        TextView textViewId;
        ImageView imageId;

        ContactViewHolder(View view){
            super(view);
            textViewItem = view.findViewById(R.id.textItem);
            textViewCost = view.findViewById(R.id.textPrice);
            textViewQty = view.findViewById(R.id.textQty);
            textViewId = view.findViewById(R.id.textID);
            imageId = view.findViewById(R.id.image);
        }
    }
}

package com.example.charles_nfc;

import java.util.ArrayList;
import java.util.Comparator;

import android.annotation.SuppressLint;

import android.graphics.Bitmap;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ShoppingCartAdapterClass extends RecyclerView.Adapter<ShoppingCartAdapterClass.ViewHolder> {
    ArrayList<ShoppingCartItemModel> cart = new ArrayList<>();

    private final Integer userID;
    private final FirebaseHandler firebaseManager = new FirebaseHandler();

    public ShoppingCartAdapterClass(
            ArrayList<ShoppingCartItemModel> cart,
            Integer userID
    ) {
        this.cart = cart;
        this.userID = userID;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void sort() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.cart.sort(new Comparator<ShoppingCartItemModel>() {
                @Override
                public int compare(ShoppingCartItemModel t1, ShoppingCartItemModel t2) {
                    return t1.name.compareTo(t2.name);
                }
            });
        }
        this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity, cost;
        ImageView item_image;
        ShoppingCartItemModel shoppingCartItemModel;
        String tagID;
        Long block_remove;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            quantity = itemView.findViewById(R.id.item_quantity);
            cost = itemView.findViewById(R.id.item_cost);
            item_image = itemView.findViewById((R.id.item_image));

            itemView.findViewById(R.id.minus).setOnClickListener(new SingleClickListener() {
                @Override
                public void Click(View v) {
                    firebaseManager.minusQuantity(tagID, block_remove, userID, name);
                }
            });

            itemView.findViewById(R.id.remove_item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firebaseManager.removeItem(name, userID, tagID);
                }
            });

            itemView.findViewById(R.id.plus).setOnClickListener(new SingleClickListener() {
                @Override
                public void Click(View v) {
                    firebaseManager.plusQuantity(tagID, userID);
                }
            });

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_cart_item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingCartAdapterClass.ViewHolder holder, int position) {
        String quantity_string = "Quantity: " + cart.get(position).quantity.toString();
        String cost_formatting = String.format("%.2f",cart.get(position).cost);
        String cost_string = "Cost: $ " + cost_formatting;
        String imageUrl = cart.get(position).image_url;
        ShoppingCartItemModel shoppingCartItemModel = cart.get(position);

        holder.name.setText(cart.get(position).name);
        holder.quantity.setText(quantity_string);
        holder.cost.setText(cost_string);
        holder.shoppingCartItemModel = shoppingCartItemModel;
        holder.tagID = cart.get(position).tag_id;
        holder.block_remove = cart.get(position).quantity;

        new DownloadImageTask(new DownloadImageTask.customListener() {
            @Override
            public void postExecute(Bitmap result) {
                holder.item_image.setImageBitmap(result);
            }
        }).execute(imageUrl);
    }

    @Override
    public int getItemCount() {
        return cart.size();
    }
}

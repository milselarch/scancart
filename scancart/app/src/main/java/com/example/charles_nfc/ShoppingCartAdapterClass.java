package com.example.charles_nfc;

import java.util.ArrayList;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ShoppingCartAdapterClass extends RecyclerView.Adapter<ShoppingCartAdapterClass.ViewHolder> {
    ArrayList<ShoppingCartItemModel> cart = new ArrayList<>();

    //TODO: Remove this eventually
    private final Integer userID = 1;

    public ShoppingCartAdapterClass(ArrayList<ShoppingCartItemModel> cart) {
        this.cart = cart;
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                    db.collection("shopping_cart")
                            .whereEqualTo("user_id", userID)
                            .whereEqualTo("tag_id", tagID)
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    if (block_remove == 1) {
                                        Toast.makeText(
                                                name.getContext(),
                                                "Quantity cannot be less than 1",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    } else {
                                        db.collection("shopping_cart").document(document.getId()).update("quantity", FieldValue.increment(-1));
                                    }
                                }
                            } else {
                                Log.d("Delete Item", "Error getting documents: ", task.getException());
                            }
                        }
                    });
                }
            });

            itemView.findViewById(R.id.remove_item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Confirmation Dialog Box
                    AlertDialog.Builder builder = new AlertDialog.Builder(name.getContext());
                    builder.setTitle("Item will be removed from cart");
                    builder.setMessage("Are you sure you would like to remove item?");

                    // If user click "Yes"
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            db.collection("shopping_cart")
                                    .whereEqualTo("user_id", userID)
                                    .whereEqualTo("tag_id", tagID)
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (DocumentSnapshot document : task.getResult()) {
                                            db.collection("shopping_cart").document(document.getId()).delete();
                                        }
                                    } else {
                                        Log.d("Delete Item", "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                            dialog.dismiss();
                        }

                    });

                    // If user click "No"
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });

            itemView.findViewById(R.id.plus).setOnClickListener(new SingleClickListener() {
                @Override
                public void Click(View v) {
                    db.collection("shopping_cart")
                            .whereEqualTo("user_id", userID)
                            .whereEqualTo("tag_id", tagID)
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    db.collection("shopping_cart").document(document.getId()).update("quantity", FieldValue.increment(1));;
                                }
                            } else {
                                Log.d("Delete Item", "Error getting documents: ", task.getException());
                            }
                        }
                    });
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

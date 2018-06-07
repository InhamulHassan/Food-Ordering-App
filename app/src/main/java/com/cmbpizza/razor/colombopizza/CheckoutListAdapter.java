package com.cmbpizza.razor.colombopizza;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by RazR on 1/31/2018, as a list adapter method
 */


//we create a new base adapter to assign a custom layout to the list view
public class CheckoutListAdapter extends BaseAdapter {

    private Context context; //this is the context of the adapter (which allows to access this adapter's resources)
    private int layout; //this defines the layout of the adapter
    private SQLLiteHelperProducts sqlLiteHelperProducts;
    //this list will store all the relevant data of the products that will be displayed in the list view
    //(this array is of type object Products, a class I created earlier)

    private ArrayList<CartItems> cartItems;

    CheckoutListAdapter(Context context, int layout, ArrayList<CartItems> cartItems) {
        //we are initializing the values that we introduced earlier (so that it can be accessed by another class)
        this.context = context;
        this.layout = layout;
        this.cartItems = cartItems;
        this.sqlLiteHelperProducts = new SQLLiteHelperProducts(context, "ProductDB", null, 1);
    }

    @Override
    public int getCount() {
        //this gets the total count of the products in the array
        return cartItems.size();
    }

    @Override
    public Object getItem(int itemPosition) {
        //using this we can target specific elements in the array
        return cartItems.get(itemPosition);
    }

    @Override
    public long getItemId(int itemPosition) {
        //we can get the id of the current item element in the array
        return itemPosition;
    }

    private class ViewHolder{
        //this holder will help assign the data to the xml layout elements from the database
        ImageView productImage;
        TextView productId, productTitle, productTotal, productTotalCalculation, productNetTotal;
    }

    @Override
    public View getView(final int itemPosition, View view, ViewGroup viewGroup) {
        //we get to assign the view of each individual list items using this method
        View row = view; //we are assigning the view of a single product so that we can add its layout and its data
        ViewHolder viewHolder = new ViewHolder(); //we are initializing a constructor of the view holder method to access its children (ie. the layout elements)

        //we check if the view is filled already, if not then we fill it with the necessary layout and data
        if(row == null){
            //layout inflater allows to assign the selected view the xml layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //this will inflate the specified xml layout onto the context
            row = inflater.inflate(layout, null);

            row.setTag(viewHolder); //the set tag is used to reference the object of the view (so that we can reference the views as a single object)
        } else {
            //we get the tag of the current instance of the view
            viewHolder = (ViewHolder) row.getTag();
        }

        //we are assigning the layout elements of the xml file onto our current view holder, so that it can correctly fill them will the correct values
        viewHolder.productId = row.findViewById(R.id.productIdCheckout);
        viewHolder.productTitle = row.findViewById(R.id.productTitleCheckout);
        viewHolder.productTotal = row.findViewById(R.id.productPriceTotalCheckout);
        viewHolder.productTotalCalculation = row.findViewById(R.id.productQuantityCalculationCheckout);
        viewHolder.productImage = row.findViewById(R.id.productImageCheckout);


        //we reference the product class and get the position of the product that is displayed in the current instance
        final CartItems items = cartItems.get(itemPosition);

        final String cartId = String.valueOf(items.getCartId());
        final int productId = items.getProductId();
        int productQuantity = items.getProductQuantity();
        int productPrice = sqlLiteHelperProducts.getProductPrice(productId);
        int totalPrice = productPrice * productQuantity;
        final int rowId = sqlLiteHelperProducts.getCartRowID(productId);

        String productTitle = sqlLiteHelperProducts.getProductTitle(productId);

        //using that we assign the data to the layout of the current instance of the view (the layouts which were defined earlier)
        viewHolder.productId.setText(String.valueOf(productId));
        viewHolder.productTitle.setText(productTitle);
        viewHolder.productTotal.setText(context.getResources().getString(R.string.txtProductPricePrefix, totalPrice));
        viewHolder.productTotalCalculation.setText(context.getResources().getString(R.string.txtProductTotalCalculationPrefix, productQuantity, productPrice));


        byte[] productImageByte = sqlLiteHelperProducts.getProductImage(productId);
        if(productImageByte != null){
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(productImageByte, 0, productImageByte.length);
            viewHolder.productImage.setImageBitmap(imageBitmap);
        }

        ImageButton RemoveItemButton = row.findViewById(R.id.btnRemoveCartItem);
        RemoveItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, "Item Position: " + String.valueOf(rowId), Toast.LENGTH_SHORT).show();
                cartItems.remove(itemPosition);
                sqlLiteHelperProducts.clearCartItem(rowId, productId);
                notifyDataSetChanged();
                Log.d("cartItem Length", String.valueOf(cartItems.size()));
                Log.d("db Length", String.valueOf(sqlLiteHelperProducts.getCartCount(cartId)));
            }
        });


        //we ae returning the view so that it can be added to the app screen
        return row;
    }
}

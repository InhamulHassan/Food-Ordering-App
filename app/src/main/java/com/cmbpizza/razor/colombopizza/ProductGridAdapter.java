package com.cmbpizza.razor.colombopizza;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by RazR on 1/31/2018, as a grid adapter method
 */


//we create a new base adapter to assign a custom layout to the list view
public class ProductGridAdapter extends BaseAdapter {

    private Context context; //this is the context of the adapter (which allows to access this adapter's resources)
    private int deviceWidth; //this will store the device's width value
    private int layout; //this defines the layout of the adapter
    //this list will store all the relevant data of the products that will be displayed in the list view
    //(this array is of type object Products, a class I created earlier)
    private ArrayList<Products> productList;

    ProductGridAdapter(Context context, int layout, ArrayList<Products> productList, int deviceWidth) {
        //we are initializing the values that we introduced earlier (so that it can be accessed by another class)
        this.context = context;
        this.layout = layout;
        this.productList = productList;
        this.deviceWidth = deviceWidth;
    }

    @Override
    public int getCount() {
        //this gets the total count of the products in the array
        return productList.size();
    }

    @Override
    public Object getItem(int itemPosition) {
        //using this we can target specific elements in the array
        return productList.get(itemPosition);
    }

    @Override
    public long getItemId(int itemPosition) {
        //we can get the id of the current item element in the array
        return itemPosition;
    }

    private class ViewHolder{
        //this holder will help assign the data to the xml layout elements from the database
        ImageView productImage;
        TextView productId, productTitle;
    }

    @Override
    public View getView(int itemPosition, View view, ViewGroup viewGroup) {
        //we get to assign the view of each individual list items using this method
        View row = view; //we are assigning the view of a single product so that we can add its layout and its data
        ViewHolder viewHolder = new ViewHolder(); //we are initializing a constructor of the view holder method to access its children (ie. the layout elements)

        //we check if the view is filled already, if not then we fill it with the necessary layout and data
        if(row == null){
            //layout inflater allows to assign the selected view the xml layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //this will inflate the specified xml layout onto the context
            row = inflater.inflate(layout, null);
            //we are assigning the layout elements of the xml file onto our current view holder, so that it can correctly fill them will the correct values
            viewHolder.productId = row.findViewById(R.id.productIdGrid);
            viewHolder.productTitle = row.findViewById(R.id.productTitleGrid);
            viewHolder.productImage = row.findViewById(R.id.productImageGrid);
            row.setTag(viewHolder); //the set tag is used to reference the object of the view (so that we can reference the views as a single object)
        } else {
            //we get the tag of the current instance of the view
            viewHolder = (ViewHolder) row.getTag();
        }

        //i am dividing the device's width by half so that we can use it to set the grid view image size
        int gridSize = deviceWidth / 2;
        viewHolder.productImage.getLayoutParams().height = gridSize;
        viewHolder.productImage.getLayoutParams().width = gridSize;

        //we reference the product class and get the position of the product that is displayed in the current instance
        Products product = productList.get(itemPosition);
        //using that we assign the data to the layout of the current instance of the view (the layouts which were defined earlier)
        viewHolder.productId.setText(String.valueOf(product.getProductId()));
        viewHolder.productTitle.setText(product.getProductTitle());
        //here the image is converted from byte[] into a bitmap to display (since it is stored as bytes in the database)
        byte[] productImageByte = product.getProductImage();
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(productImageByte, 0, productImageByte.length);
        viewHolder.productImage.setImageBitmap(imageBitmap);

        //we ae returning the view so that it can be added to the app screen
        return row;
    }
}

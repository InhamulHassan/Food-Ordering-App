package com.cmbpizza.razor.colombopizza;

import android.app.SearchManager;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by RazR on the 2/5/2018. for use as a search view's suggestion adapter
 */

public class SearchSuggestionsAdapter extends CursorAdapter {

    private int layout;
    private boolean roundedImage;
    SearchSuggestionsAdapter(Context context, Cursor c, int layout, boolean roundedImage) {
        super(context,  c, 0);
        this.layout = layout;
        this.roundedImage = roundedImage;

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(layout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = view.findViewById(R.id.productTitleSuggestion);
        ImageView iv = view.findViewById(R.id.productImageSuggestion);
        final int textIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
        final int imgIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_1);
        String title = cursor.getString(textIndex);
        tv.setText(title);
        Resources res = context.getResources();
        byte[] imgByte = Base64.decode(cursor.getString(imgIndex), Base64.DEFAULT);
        Bitmap img = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
        if(roundedImage){
            RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res, img);
            dr.setCircular(true);
            iv.setImageDrawable(dr);
        } else {
            iv.setImageBitmap(img);
        }
    }
}

package com.cmbpizza.razor.colombopizza;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class AdminMenu extends Activity implements View.OnClickListener, SearchView.OnQueryTextListener, SearchView.OnSuggestionListener, SearchView.OnCloseListener, ListView.OnItemClickListener  {

    private static final String[] COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_ICON_1
    };
    private static SQLLiteHelperProducts sqLiteHelper;
    final int REQUEST_CODE_GALLERY = 999;
    public int orderCount = 0;
    public Dialog addProductDialog;
    public Dialog orderDialog;
    FloatingActionButton FabAddProduct;
    Button AddProductButton, CancelButton, ChooseImageButton, CheckOrderButton;
    EditText AddProductTitle, AddProductPrice, AddProductDescription;
    Spinner SpinnerProductCategory;
    ImageView ImgProductImageView;
    SearchView AdminSearchView;
    ListView ProductListView, OrderListView;
    ArrayList<Products> ProductList;
    ArrayList<Integer> ProductIDList;
    ArrayList<Orders> OrderList;
    ProductListAdapter ListAdapter;
    OrderListAdapter OrderListAdapter;
    private SearchSuggestionsAdapter mSuggestionsAdapter;

    //this method runs when the activity is created and run for the first time
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);
        initializeComponents();
        initializeListeners();
        sqlLiteDB();
        getAllData();
        getAllSearchSuggestions();
    }

    //this method will run when this activity is started(or resumed) again
    @Override
    protected void onStart() {
        super.onStart();
        //these statements will ensure that the search view is not focused when the activity starts
        AdminSearchView.setIconified(true);
        AdminSearchView.clearFocus();
        getOrderCount(); //get the number of orders which are to be confirmed
        checkOrderCount();
        getAllData();
    }

    //this method is used to initialize all the components and variables used in this class
    private void initializeComponents() {
        orderDialog = new Dialog(AdminMenu.this);
        orderDialog.setContentView(R.layout.admin_check_order_dialog);

        ProductListView = findViewById(R.id.adminListView);
        AdminSearchView = findViewById(R.id.adminSearchView);
        OrderListView = orderDialog.findViewById(R.id.adminOrderListView);

        //we are casting the SearchView into an AutoCompleteTextView so that we can set its threshold at which it will start showing suggestions)
        AutoCompleteTextView searchAutoCompleteTextView = AdminSearchView.findViewById(AdminSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
        searchAutoCompleteTextView.setThreshold(0);
        ProductList = new ArrayList<>();
        ProductIDList = new ArrayList<>();
        OrderList = new ArrayList<>();
        ListAdapter = new ProductListAdapter(AdminMenu.this, R.layout.admin_product_items_list, ProductList);
        ProductListView.setAdapter(ListAdapter);
        OrderListAdapter = new OrderListAdapter(orderDialog.getContext(), R.layout.admin_check_order_list_view, OrderList);
        OrderListView.setAdapter(OrderListAdapter);


        FabAddProduct = findViewById(R.id.add_product_fab);
        CheckOrderButton = findViewById(R.id.adminOrderCheckButton);
        //creating a new instance of the Dialog class object, and specifying its context as the current class
        addProductDialog = new Dialog(AdminMenu.this);//specifying the layout/content of the dialog by referring to the XML file
        addProductDialog.setContentView(R.layout.admin_add_product_dialog);

        AddProductButton = addProductDialog.findViewById(R.id.btnAdminAddProduct);
        CancelButton = addProductDialog.findViewById(R.id.btnAdminCancel);
        ChooseImageButton = addProductDialog.findViewById(R.id.btnAdminAddProductImage);
        ImgProductImageView = addProductDialog.findViewById(R.id.imgProductImage);
        AddProductTitle = addProductDialog.findViewById(R.id.txtNewProductTitle);
        AddProductPrice = addProductDialog.findViewById(R.id.txtNewProductPrice);
        AddProductDescription = addProductDialog.findViewById(R.id.txtNewProductDescription);
        SpinnerProductCategory = addProductDialog.findViewById(R.id.spinnerAddProductCategory);
    }

    //setting a click event listener to the components
    private void initializeListeners() {
        AddProductButton.setOnClickListener(this);
        CancelButton.setOnClickListener(this);
        ChooseImageButton.setOnClickListener(this);
        FabAddProduct.setOnClickListener(this);
        AdminSearchView.setOnQueryTextListener(this);
        AdminSearchView.setOnSuggestionListener(this);
        AdminSearchView.setOnCloseListener(this);
        ProductListView.setOnItemClickListener(this);
        CheckOrderButton.setOnClickListener(this);

        OrderListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                getOrderCount();
                checkOrderCount();
            }
        });
    }

    //this is the onClick listener
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAdminAddProduct:
                addProductToDB();
                break;
            case R.id.btnAdminCancel:
                AddProductTitle.getText().clear();
                AddProductPrice.getText().clear();
                AddProductDescription.getText().clear();
                ImgProductImageView.setImageBitmap(null);
                addProductDialog.cancel();
                getAllData();
                break;
            case R.id.btnAdminAddProductImage:
                ActivityCompat.requestPermissions(
                        AdminMenu.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
                break;
            case R.id.add_product_fab:
                addProductFAB();
                break;
            case R.id.adminOrderCheckButton:
                showOrderDialog();
                break;
        }
    }

    private void showOrderDialog() {
        getOrderDetails();
        orderDialog.setCancelable(true); //specifying that whether the dialog box is able to be cancelled
        orderDialog.setCanceledOnTouchOutside(true);//specifying that the dialog box should close when and if the user touches outside it
        orderDialog.show();
    }

    private void getOrderDetails() {
        OrderList.clear();
        //Toast.makeText(this, "ArrayLength: " + String.valueOf(sqLiteHelper.getCartCount(randomID)), Toast.LENGTH_SHORT).show();
        OrderList.addAll(sqLiteHelper.getAllOrderItems());//we only get the orders that are not confirmed
        //Toast.makeText(this, "ArrayLength: " + String.valueOf(CartItemsList.size()), Toast.LENGTH_SHORT).show();
        OrderListAdapter.notifyDataSetChanged();
    }

    private void getOrderCount() {
        orderCount = sqLiteHelper.getOrderCount();
    }

    //this method is used to set item click listener to the layout items
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //we get the hidden text view that stores the product id, we use it to get the id
        //so that we can retrieve it in the next activity
        TextView ListProductId = view.findViewById(R.id.productId);
        Intent newIntent = new Intent(AdminMenu.this, AdminProductView.class);

        //these shared views are used to make a scene transition animation where the elements
        //inside the given views will be shared to the next intent which will have the same elements
        //thus both the views layout files will be given a common transition name, which we have included as a string parameter
        //both the view and the transition name will be included in a Pair and declared as scene transition elements, and then
        //bundled into the start activity method
        Pair<View, String> pairImage = Pair.create(view.findViewById(R.id.productImage), "productImageAdmin");
        Pair<View, String> pairTitle = Pair.create(view.findViewById(R.id.productTitle), "productTitleAdmin");
        Pair<View, String> pairDescription = Pair.create(view.findViewById(R.id.productDescription), "productDescriptionAdmin");
        Pair<View, String> pairCategory = Pair.create(view.findViewById(R.id.productCategory), "productCategoryAdmin");
        Pair<View, String> pairPrice = Pair.create(view.findViewById(R.id.productPrice), "productPriceAdmin");

        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                AdminMenu.this,
                pairImage,
                pairTitle,
                pairDescription,
                pairCategory,
                pairPrice);
        newIntent.putExtra("productId", ListProductId.getText().toString());
        //we are sending the location of the product in the list to the next activity
        //because I wanted to fill it with the the Product data
        startActivity(newIntent, activityOptions.toBundle());
    }

    //gets the file path of an image using its URI
    public static String getPath(Uri uri, Context context) {
        //I am checking if the URI is null, just in case, as an added security feature
        if (uri == null) {
            //Displaying a message to the user that something has gone wrong
            Toast.makeText(context, "Unable to load image, try again", Toast.LENGTH_SHORT).show();
            return null; //we are returning null so that the image view is empty
        }
        //try to retrieve the image from the media store (NOTE: first this will only work for images selected from gallery)
        String[] projection = {MediaStore.Images.Media.DATA};
        //we are running a query for the image URI in the projection(ie. all the media files in gallery) to get a match
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        //we are checking if the cursor is null before proceeding just in case
        if (cursor != null) {
            //here we get the column index of the DATA column (ie. which contains all the image file path data in the gallery)
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path; //we then return that file path
        }
        // this is our fallback here (ie. backup if all the return values above fail)
        //if that happens we send the URI's decoded path
        return uri.getPath();
    }

    //we decode the image from its file path using a set of predefined options to scale the image properly
    public static Bitmap decodeBitmapFromFilePath(String path, int reqHeight, int reqWidth) {
        //the bitmap factory options is used to set certain parameters while decoding a file to a bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        /*if we set the inJustDecodeBounds in the options and pass them while decoding the file to bitmap
        *the Bitmap factory will only load the outer bounds of the image without the actual image itself,
         * which is very useful if the image is a very large one (ie. an image with a high resolution count
         * such as 8M, 12M or 1980*1080, 4096*2160 or something like that) which would require large memory from
         * the device which in turn would most probably freeze our app since it would require memory resources.
         * Therefore a solution to this is to do this method, a method that is recommended by android developer
         * community.*/
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        /*
        * After we decoded the file with the inJustDecodeBounds option, we calculate the Sample size
        * of the image using the calculateInSampleSize() method, which would set the sampleSize (ie. scale).
        * of the image. Check for that methods comment for further details about the method and its functions*/
        options.inSampleSize = calculateInSampleSize(options, reqHeight, reqWidth);
        /*we disable inJustDecodeBounds option so that the image will be displayed on the imageView as it
        * and not the outer bounds of the image. Then we decode the file path return the image Bitmap according
        * to the new options that we have specified*/
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    //this method calculates the sampleSize required of the image by using the requiredHeight and requiredWidth that we provide
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqHeight, int reqWidth) {
        /*we get the outer bounds of the image we decoded in the parent method, then we get its height
        * and width. Then we create an initial inSampleSize value as an integer*/
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        /*we check the height and width of the images outer bounds with the required user-given height
        * and width, we compare it (and power the value of the sample size by 2, if the width and height
        * of the outer bounds are smaller that what is required)*/
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            /*we halve the values and compare them again( if they are still bigger than the required size
             * we increase the sample size by the power of two (Sample size is basically the scaling of the
             * bitmap's pixel values to make them smaller) thus for example if we get a high-res image of
             * 4096*2048, this resolution will be halved to 2048*1028 if the inSampleSized is doubled
             * (or powered up by 2). Therefore as an end result we get an image that is scaled to be
             * smaller that its original full-res image, thus taking less memory resources*/
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        //we return the inSampleSze integer we calculated above so that it can be applied in the image's bitmap decoding options
        return inSampleSize;
    }

    //the use of this method is to assign the suggestions that will be displayed when the user clicks on the search bar
    private void getAllSearchSuggestions() {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        for(int i: ProductIDList){
            cursor.addRow(new String[]{String.valueOf(sqLiteHelper.getProductID(i)), sqLiteHelper.getProductTitle(i), android.util.Base64.encodeToString(sqLiteHelper.getProductImage(i), Base64.DEFAULT)});
        }
        mSuggestionsAdapter = new SearchSuggestionsAdapter(AdminSearchView.getContext(), cursor, R.layout.search_suggestion_view, false);
        AdminSearchView.setSuggestionsAdapter(mSuggestionsAdapter);
    }

    //this method s used to get search suggestion when the user starts typing
    public void getSearchTextSuggestions(String searchString) {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        ArrayList<Products> filterProductList = new ArrayList<>();
        filterProductList.clear();
        if (!searchString.isEmpty()) {
            String searchStringLower = searchString.toLowerCase();
            for (Products list : ProductList) {
                String ProductName = list.getProductTitle().toLowerCase();
                if (ProductName.startsWith(searchStringLower)) {
                    filterProductList.add(list);
                }
            }
            for(Products filterList: filterProductList){
                cursor.addRow(new String[]{String.valueOf(filterList.getProductId()), filterList.getProductTitle(), android.util.Base64.encodeToString(filterList.getProductImage(), Base64.DEFAULT)});
            }
        } else {
            for(int i: ProductIDList){
                cursor.addRow(new String[]{String.valueOf(sqLiteHelper.getProductID(i)), sqLiteHelper.getProductTitle(i), android.util.Base64.encodeToString(sqLiteHelper.getProductImage(i), Base64.DEFAULT)});
            }
        }
        mSuggestionsAdapter = new SearchSuggestionsAdapter(AdminSearchView.getContext(), cursor, R.layout.search_suggestion_view, false);
        AdminSearchView.setSuggestionsAdapter(mSuggestionsAdapter);
    }

    //this method is used to get the a drawable bitmap from the image view and convert in into a byte array so that we can insert it into the database
    public byte[] imageViewToByte(ImageView image) {
        //we get the image from the image view as a drawable and get its bitmap
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        //we create a new object of a bitmap output steam so that it can filled with the bitmap of the image
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //we compress the bitmap image as a png into the byte output stream (the quality of the image is also specified)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //we then convert the stream into a byte array so that it can be inserted into the database
        return stream.toByteArray();
    }

    //this method will run when the user gives the app permission to access external storage to access the device gallery
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //here i am checking if the phone has granted permission to the app to access gallery
        if (requestCode == REQUEST_CODE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //since the user has given permission to access gallery the app will open the gallery
                Intent intent = new Intent(Intent.ACTION_PICK); //here i am picking a data from the device
                intent.setType("image/*"); //assigning what kind of data we are picking from the device
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            } else {
                Toast.makeText(AdminMenu.this, "You do not have the permission to access the gallery", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //this method will fire when the user has chosen an image from the gallery, thus the activity will return a result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //here we are checking whether the activity result we got was a successful one, and we check whether the data that was pulled is not null
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            //here we get the uri (uniform resource identifier), something like a url to get the info about the image we chose from gallery
            Uri uri = data.getData();
            /*the decodeBitmapFromResource has a very important role in my app
             *it solves a bug-like problem, it enables me to load high quality images from the gallery
             * whereas if i tru to load it in the conventional way my app would mos probably freeze or it will
             * run out of memory. I have given further details about the function in that function's method comments
             * the getPath method is used to get the file path of the image we choose from the gallery*/
            //here we are setting our image product view's source image as the image that we chose from gallery
            ImgProductImageView.setImageBitmap(decodeBitmapFromFilePath(getPath(uri, this), 150, 150));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //this method will open the add product dialog when the floating action button is clicked
    private void addProductFAB() {
        addProductDialog.setCancelable(true); //specifying that whether the dialog box is able to be cancelled
        addProductDialog.setCanceledOnTouchOutside(true);//specifying that the dialog box should close when and if the user touches outside it
        addProductDialog.show();
    }

    //this method is used to create a database (or at least an instance of it)
    private void sqlLiteDB() {
        sqLiteHelper = new SQLLiteHelperProducts(AdminMenu.this, "ProductDB", null, 1);
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productTable (productId INTEGER PRIMARY KEY AUTOINCREMENT, productTitle VARCHAR, productCategory VARCHAR, productPrice INTEGER, productDescription VARCHAR, productImage BLOB)");
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productOrderTable (orderId INTEGER PRIMARY KEY, memberId INTEGER, idList VARCHAR, quantityList VARCHAR, totalPrice INTEGER, orderStatus INTEGER)");
    }

    //this method would get all the data from the sqlLite database
    private void getAllData() {
        ProductIDList.clear();
        ProductList.clear();//this is to clear the view of previous search
        for (Products product: sqLiteHelper.getAllData()) {
            ProductIDList.add(product.getProductId());
            ProductList.add(new Products(product.getProductId(), product.getProductTitle(), product.getProductCategory(), product.getProductPrice(), product.getProductDescription(), product.getProductImage()));
        }
        ListAdapter.notifyDataSetChanged(); //we notify the list adapter class that there has been a change in its data set
    }

    //this method will handle the insertion of the product details into the database
    private void addProductToDB() {
        if (inputValidation()) {
            try {
                sqLiteHelper.insertData(
                        AddProductTitle.getText().toString().trim(),
                        SpinnerProductCategory.getSelectedItem().toString(),
                        Integer.parseInt(AddProductPrice.getText().toString()),
                        AddProductDescription.getText().toString(),
                        imageViewToByte(ImgProductImageView)
                );
                Toast.makeText(AdminMenu.this, "Product Added Successfully!", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            getAllData();

            AddProductTitle.getText().clear();
            AddProductPrice.getText().clear();
            AddProductDescription.getText().clear();
            ImgProductImageView.setImageBitmap(null);
            addProductDialog.cancel();
        }
    }

    //this method handles the input validation for the add product form
    private boolean inputValidation() {
        boolean valid;
        if (ImgProductImageView.getDrawable() == null) {
            valid = false;
            Toast.makeText(this, "Please include the product image", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(AddProductTitle.getText()).isEmpty()) {
            valid = false;
            Toast.makeText(this, "Please enter the product title", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(AddProductPrice.getText()).isEmpty()) {
            valid = false;
            Toast.makeText(this, "Please enter the product price", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(AddProductDescription.getText()).isEmpty()) {
            valid = false;
            Toast.makeText(this, "Please enter the product description", Toast.LENGTH_SHORT).show();
        } else {
            valid = true;
        }

        return valid;
    }

    //this listener will fire when we submit a search query into the search button
    @Override
    public boolean onQueryTextSubmit(String s) {
        String searchStringLower = s.toLowerCase();
        ArrayList<Products> filterProductList = new ArrayList<>();
        for (Products list : ProductList) {
            String ProductName = list.getProductTitle().toLowerCase();
            if (ProductName.startsWith(searchStringLower)) {
                filterProductList.add(list);
            }
        }
        ProductList.clear();
        ProductList.addAll(filterProductList);
        ListAdapter.notifyDataSetChanged();
        filterProductList.clear();
        Toast.makeText(this, "You searched for: " + s, Toast.LENGTH_LONG).show();
        return true;
    }

    //this listener will fire when the text query (while we type it) changes
    @Override
    public boolean onQueryTextChange(String searchString) {
        if (!searchString.isEmpty()) {
            getSearchTextSuggestions(searchString);
        } else {
            getAllSearchSuggestions();
            getAllData();
        }
        return true;
    }

    @Override
    public boolean onSuggestionSelect(int i) {
        return false;
    }

    //this method is fired when we click an item from the search suggestions meny
    @Override
    public boolean onSuggestionClick(int i) {
        Cursor c = (Cursor) mSuggestionsAdapter.getItem(i);
        String query = c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
        AdminSearchView.setQuery(query, true);
        return true;
    }

    //this method will run when we close the search view in the activity
    @Override
    public boolean onClose() {
        getAllData();
        return true;
    }

    //this method will check the order count and rearrange the order button layout accordingly
    private void checkOrderCount() {
        RelativeLayout relativeLayoutButton, relativeLayoutBadge;
        relativeLayoutButton = findViewById(R.id.layoutRelativeOrderCheckButton);
        relativeLayoutBadge = findViewById(R.id.layoutRelativeOrderButtonBadge);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


        if (orderCount > 0) {
            layoutParams.setMargins(layoutParams.getMarginStart(), 0, layoutParams.getMarginEnd(), 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.layout.activity_admin_menu);
            relativeLayoutButton.setLayoutParams(layoutParams); //the layout params is used to set the margin and the alignment

            RelativeLayout.LayoutParams layoutParamsBadge = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParamsBadge.setMargins(0, 0, 0, 0);
            layoutParamsBadge.addRule(RelativeLayout.ALIGN_START, R.id.layoutLinearOrderCheckButton);
            layoutParamsBadge.addRule(RelativeLayout.ALIGN_TOP, R.id.layoutLinearOrderCheckButton);
            relativeLayoutBadge.setLayoutParams(layoutParamsBadge);//the layout params is used to set the margin and the alignment
            relativeLayoutBadge.setVisibility(View.VISIBLE);
            TextView cartBadgeCount = findViewById(R.id.txtOrderButtonBadge);
            cartBadgeCount.setText(String.valueOf(orderCount));
        } else {
            //the typed value class is used to convert the pixels to dip (density independent pixels) so that it will hide the
            //order button when the cart is empty
            int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -50.0f, getResources().getDisplayMetrics());
            layoutParams.setMargins(layoutParams.getMarginStart(), 0, layoutParams.getMarginEnd(), value);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.layout.activity_admin_menu);
            relativeLayoutButton.setLayoutParams(layoutParams); //the layout params is used to set the margin and the alignment
        }
    }
}

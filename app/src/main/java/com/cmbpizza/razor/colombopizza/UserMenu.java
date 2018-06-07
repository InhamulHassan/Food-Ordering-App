package com.cmbpizza.razor.colombopizza;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.os.Bundle;
import android.app.Activity;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class UserMenu extends Activity implements View.OnClickListener, SearchView.OnQueryTextListener, SearchView.OnSuggestionListener, SearchView.OnCloseListener, AdapterView.OnItemClickListener {

    static final int VIEW_MODE_LISTVIEW = 0;
    static final int VIEW_MODE_GRIDVIEW = 1;
    private static final String[] COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_ICON_1
    };
    public String memberId;
    public static boolean isUserLoggedIn = false;
    private static SQLLiteHelperProducts sqLiteHelper;
    public int cartCount = 0;
    public String randomID;
    public Dialog checkoutDialog;
    FloatingActionButton FilterFab, FilterCheapFab, FilterExpensiveFab, SwitchGridView, SwitchListView, ClearCartFab;
    ViewSwitcher FabViewSwitcher;
    TextView CartNetTotal;
    Animation FabOpen, FabClose, FabTranslateUp, FabTranslateDown;
    Button CheckoutButton, ConfirmCheckoutButton;
    SearchView UserSearchView;
    ViewStub GridViewStub;
    ViewStub ListViewStub;
    ListView ProductListView, CheckoutItemsListView;
    GridView ProductGridView;
    ArrayList<CartItems> CartItemsList;
    ArrayList<Products> ProductList;
    ArrayList<Integer> ProductIDList;
    CheckoutListAdapter CheckoutItemsListAdapter;
    ProductListAdapter ListAdapter;
    ProductGridAdapter GridAdapter;
    boolean isOpen = false;
    private SearchSuggestionsAdapter mSuggestionsAdapter;
    private int deviceWidth;
    private int currentViewMode = 0;

    //this method would run when the activity is created and run fro the first time
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_menu);
        getDeviceWidth();
        initializeComponents();
        initializeViewModes();
        initializeListeners();
        checkLoginStatus();
        checkView();
        generateRandomID();
        sqlLiteDB();
        getAllData();
        floatingButtonAnimation();
        getAllSearchSuggestions();
        getCartCounter();
        checkCartCount();
        clearCart();
    }

    //this method will run when this activity is started(or resumed) again
    @Override
    protected void onStart() {
        super.onStart();
        getCartCounter();
        checkLoginStatus();
        checkCartCount();
        getAllData();
    }

    private void getCartCounter() {
        cartCount = sqLiteHelper.getCartCount(randomID);
    }

    private void checkLoginStatus() {
        if(getIntent().getExtras() == null){
            isUserLoggedIn = false;
        } else {
            isUserLoggedIn = true;
            memberId = getIntent().getExtras().get("userId").toString();
        }
    }

    //this method is used to initialize all the components and variables used in this class
    private void initializeComponents() {
        ListViewStub = findViewById(R.id.userListViewStub);
        GridViewStub = findViewById(R.id.userGridViewStub);
        FabViewSwitcher = findViewById(R.id.fab_view_switcher);

        checkoutDialog = new Dialog(UserMenu.this);//specifying the layout/content of the dialog by referring to the XML file
        checkoutDialog.setContentView(R.layout.user_checkout_dialog);
        CartNetTotal = checkoutDialog.findViewById(R.id.txtNetTotal);

        CheckoutItemsListView = checkoutDialog.findViewById(R.id.userCheckoutList);
        CartItemsList = new ArrayList<>();
        CheckoutItemsListAdapter = new CheckoutListAdapter(checkoutDialog.getContext(), R.layout.user_checkout_product_list_view, CartItemsList);
        CheckoutItemsListView.setAdapter(CheckoutItemsListAdapter);

        ConfirmCheckoutButton = checkoutDialog.findViewById(R.id.btnConfirmCheckout);

        FilterFab = findViewById(R.id.filter_fab);
        FilterCheapFab = findViewById(R.id.filter_cheap_fab);
        FilterExpensiveFab = findViewById(R.id.filter_expensive_fab);
        ClearCartFab = findViewById(R.id.clear_cart_fab);
        SwitchGridView = findViewById(R.id.fab_view_grid);
        SwitchListView = findViewById(R.id.fab_view_list);
        CheckoutButton = findViewById(R.id.userCheckoutButton);
        UserSearchView = findViewById(R.id.userSearchView);
        //we are casting the SearchView into an AutoCompleteTextView so that we can set its threshold at which it will start showing suggestions)
        AutoCompleteTextView searchAutoCompleteTextView = UserSearchView.findViewById(UserSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
        searchAutoCompleteTextView.setThreshold(0);
        ProductList = new ArrayList<>();
        ProductIDList = new ArrayList<>();
        ListAdapter = new ProductListAdapter(UserMenu.this, R.layout.user_product_items_list_view, ProductList);
        GridAdapter = new ProductGridAdapter(UserMenu.this, R.layout.user_product_items_grid_view, ProductList, deviceWidth);
    }

    //setting a click event listener to the buttons
    private void initializeListeners() {
        FilterFab.setOnClickListener(this);
        FilterCheapFab.setOnClickListener(this);
        FilterExpensiveFab.setOnClickListener(this);
        ProductGridView.setOnItemClickListener(this);
        ProductListView.setOnItemClickListener(this);
        UserSearchView.setOnQueryTextListener(this);
        UserSearchView.setOnSuggestionListener(this);
        UserSearchView.setOnCloseListener(this);
        CheckoutButton.setOnClickListener(this);
        ConfirmCheckoutButton.setOnClickListener(this);
        SwitchGridView.setOnClickListener(this);
        SwitchListView.setOnClickListener(this);
        ClearCartFab.setOnClickListener(this);

        ProductListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int currentVisibleItemCount;
            private int currentScrollState;
            private int currentFirstVisibleItem;
            private int totalItem;

            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                this.currentScrollState = scrollState;
                if(currentScrollState == SCROLL_STATE_FLING){
                    UserSearchView.setAlpha(0.0f);
                } else if(currentScrollState == SCROLL_STATE_TOUCH_SCROLL){
                    UserSearchView.setAlpha(0.5f);
                } else if(currentScrollState == SCROLL_STATE_IDLE){
                    UserSearchView.setAlpha(1);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
                this.totalItem = totalItemCount;
            }
        });

        CheckoutItemsListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                //Toast.makeText(UserMenu.this, "Data Set changed", Toast.LENGTH_SHORT).show();
                getCheckoutNetTotal();
                getCartCounter();
                checkCartCount();
            }
        });
    }

    //this method is to initialize both the view modes on activity start
    private void initializeViewModes() {
        //inflates the following layout resources to be used
        //when the view stub is inflated we get the view that we defined for that particular view stub
        //in the XML file
        ProductListView = (ListView) ListViewStub.inflate();
        ProductGridView = (GridView) GridViewStub.inflate();
        //getting the current view modes in the shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("com.cmbpizza.razor.colombopizza.viewMode", MODE_PRIVATE);
        //setting grid view as the default mode
        currentViewMode = sharedPreferences.getInt("com.cmbpizza.razor.colombopizza.currentViewMode", VIEW_MODE_LISTVIEW);
    }

    //this is the onClick listener
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.filter_fab:
                filterFab();
                break;
            case R.id.filter_cheap_fab:
                sortProductList(0);
                break;
            case R.id.filter_expensive_fab:
                sortProductList(1);
                //clearCart();
                //checkCartCount();
                break;
            case R.id.fab_view_grid:
                switchViewMode();
                break;
            case R.id.fab_view_list:
                switchViewMode();
                break;
            case R.id.clear_cart_fab:
                clearCart();
                checkCartCount();
                break;
            case R.id.userCheckoutButton:
                checkoutDialog();
                break;
            case R.id.btnConfirmCheckout:
                createOrder();
                break;
        }
    }

    //this method is used to set item click listener to the layout items
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //we get the hidden text view that stores the product id, we use it to get the id
        //so that we can retrieve it in the next activity

        //these shared views are used to make a scene transition animation where the elements
        //inside the given views will be shared to the next intent which will have the same elements
        //thus both the views layout files will be given a common transition name, which we have included as a string parameter
        //both the view and the transition name will be included in a Pair and declared as scene transition elements, and then
        //bundled into the start activity method

        //the reason for checking the current view mode before starting the activity is to send the correct shared views to the
        //next activity, since both the modes have different views, they have to be sent differently
        if (currentViewMode == VIEW_MODE_GRIDVIEW) {
            TextView ListProductId = view.findViewById(R.id.productIdGrid);
            Intent newIntent = new Intent(UserMenu.this, UserProductView.class);

            Pair<View, String> pairImage = Pair.create(view.findViewById(R.id.productImageGrid), "productImage");
            Pair<View, String> pairTitle = Pair.create(view.findViewById(R.id.productTitleGrid), "productTitle");

            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                    UserMenu.this,
                    pairImage,
                    pairTitle);
            newIntent.putExtra("userId", memberId);
            newIntent.putExtra("productId", ListProductId.getText().toString());
            newIntent.putExtra("cartId", randomID);
            //we are sending the location of the product in the list to the next activity
            //because I wanted to fill it with the the Product data
            startActivity(newIntent, activityOptions.toBundle());
        } else {
            TextView ListProductId = view.findViewById(R.id.productId);
            Intent newIntent = new Intent(UserMenu.this, UserProductView.class);

            Pair<View, String> pairImage = Pair.create(view.findViewById(R.id.productImage), "productImage");
            Pair<View, String> pairTitle = Pair.create(view.findViewById(R.id.productTitle), "productTitle");
            Pair<View, String> pairDescription = Pair.create(view.findViewById(R.id.productDescription), "productDescription");
            Pair<View, String> pairCategory = Pair.create(view.findViewById(R.id.productCategory), "productCategory");
            Pair<View, String> pairPrice = Pair.create(view.findViewById(R.id.productPrice), "productPrice");

            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                    UserMenu.this,
                    pairImage,
                    pairTitle,
                    pairDescription,
                    pairCategory,
                    pairPrice);
            newIntent.putExtra("userId", memberId);
            newIntent.putExtra("productId", ListProductId.getText().toString());
            newIntent.putExtra("cartId", randomID);
            //we are sending the location of the product in the list to the next activity
            //because I wanted to fill it with the the Product data
            startActivity(newIntent, activityOptions.toBundle());
        }
    }

    //this method is used to check the current view mode and then set the visibility of the view stubs accordingly
    private void checkView() {
        if (currentViewMode == VIEW_MODE_GRIDVIEW) {
            //displaying the grid view and hiding the list view
            GridViewStub.setVisibility(View.VISIBLE);
            ListViewStub.setVisibility(View.GONE);
            ProductGridView.setAdapter(GridAdapter);
            if(FabViewSwitcher.getCurrentView() == SwitchGridView){
                FabViewSwitcher.showNext();
            }
        } else {
            //displaying the list view and hiding the grid view
            ListViewStub.setVisibility(View.VISIBLE);
            GridViewStub.setVisibility(View.GONE);
            ProductListView.setAdapter(ListAdapter);
            FabViewSwitcher.showNext();
            if(FabViewSwitcher.getCurrentView() == SwitchListView){
                FabViewSwitcher.showNext();
            }
        }
    }

    //this method switches the view mode
    private void switchViewMode() {
        if (currentViewMode == VIEW_MODE_GRIDVIEW) {
            //switching the view from grid view to list view
            currentViewMode = VIEW_MODE_LISTVIEW;
        } else {
            //switching the view from list view to grid view
            currentViewMode = VIEW_MODE_GRIDVIEW;
        }
        //we call the method to set the view mode's visibility and set their respective adapters
        checkView();
        //then we edit the values in the shared preferences so that we can easily keep track of the current view mode
        SharedPreferences sharedPreferences = getSharedPreferences("com.cmbpizza.razor.colombopizza.viewMode", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("com.cmbpizza.razor.colombopizza.currentViewMode", currentViewMode);
        editor.apply();
    }

    //method used to get the device width
    private void getDeviceWidth() {
        /*i am getting the device's width to pass it to the grid adapter
        * so that it can use the device's width to calculate the grid's width,
        * which should be exactly half of the screen size*/
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        deviceWidth = size.x;
    }

    //this method initializes the animations for the fab buttons
    private void floatingButtonAnimation() {
        FabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        FabTranslateUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_translate_up);
        FabTranslateDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_translate_down);
    }

    //this method is used to display the addition fab buttons on pressing the main fab button
    private void filterFab() {
        if (isOpen) {
            FilterFab.startAnimation(FabTranslateDown);
            FilterCheapFab.startAnimation(FabClose);
            FilterExpensiveFab.startAnimation(FabClose);
            FilterCheapFab.setClickable(false);
            FilterExpensiveFab.setClickable(false);
            isOpen = false;
        } else {
            FilterFab.startAnimation(FabTranslateUp);
            FilterCheapFab.startAnimation(FabOpen);
            FilterExpensiveFab.startAnimation(FabOpen);
            FilterCheapFab.setClickable(true);
            FilterExpensiveFab.setClickable(true);
            isOpen = true;
        }
    }

    //this method is used to create a database
    private void sqlLiteDB() {
        sqLiteHelper = new SQLLiteHelperProducts(UserMenu.this, "ProductDB", null, 1);
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productTable (productId INTEGER PRIMARY KEY AUTOINCREMENT, productTitle VARCHAR, productCategory VARCHAR, productPrice INTEGER, productDescription VARCHAR, productImage BLOB)");
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productCartTable (id INTEGER PRIMARY KEY AUTOINCREMENT, cartId VARCHAR, productId INTEGER, productQuantity INTEGER)");
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productOrderTable (orderId INTEGER PRIMARY KEY, memberId INTEGER, idList VARCHAR, quantityList VARCHAR, totalPrice INTEGER, orderStatus INTEGER)");
    }

    //this method is used to create a database
    private void clearCart() {
        sqLiteHelper.dataQuery("DELETE FROM productCartTable");
        sqLiteHelper.dataQuery("DELETE FROM sqlite_sequence WHERE name='productCartTable'");
        cartCount = 0;
        Log.d("sql", "cart cleared");
    }

    //this method would get all the data from the sqlLite database
    private void getAllData() {
        ProductIDList.clear();
        ProductList.clear();//this is to clear the view of previous search
        for (Products product : sqLiteHelper.getAllData()) {
            ProductIDList.add(product.getProductId());
            ProductList.add(new Products(product.getProductId(), product.getProductTitle(), product.getProductCategory(), product.getProductPrice(), product.getProductDescription(), product.getProductImage()));
        }
        GridAdapter.notifyDataSetChanged(); //we notify the grid adapter class that there has been a change in its data set
        ListAdapter.notifyDataSetChanged(); //we notify the list adapter class that there has been a change in its data set
    }

    //the use of this method is to assign the suggestions that will be displayed when the user clicks on the search bar
    private void getAllSearchSuggestions() {
        MatrixCursor cursor = new MatrixCursor(COLUMNS);
        for (int i : ProductIDList) {
            cursor.addRow(new String[]{String.valueOf(sqLiteHelper.getProductID(i)), sqLiteHelper.getProductTitle(i), android.util.Base64.encodeToString(sqLiteHelper.getProductImage(i), Base64.DEFAULT)});
        }
        mSuggestionsAdapter = new SearchSuggestionsAdapter(UserSearchView.getContext(), cursor, R.layout.search_suggestion_view_user, true);
        UserSearchView.setSuggestionsAdapter(mSuggestionsAdapter);
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
            for (Products filterList : filterProductList) {
                cursor.addRow(new String[]{String.valueOf(filterList.getProductId()), filterList.getProductTitle(), android.util.Base64.encodeToString(filterList.getProductImage(), Base64.DEFAULT)});
            }
        } else {
            for (int i : ProductIDList) {
                cursor.addRow(new String[]{String.valueOf(sqLiteHelper.getProductID(i)), sqLiteHelper.getProductTitle(i), android.util.Base64.encodeToString(sqLiteHelper.getProductImage(i), Base64.DEFAULT)});
            }
        }
        mSuggestionsAdapter = new SearchSuggestionsAdapter(UserSearchView.getContext(), cursor, R.layout.search_suggestion_view, false);
        UserSearchView.setSuggestionsAdapter(mSuggestionsAdapter);
    }

    //this method will run when we close the search view in the activity
    @Override
    public boolean onClose() {
        getAllData();
        return true;
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
        GridAdapter.notifyDataSetChanged();
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
        UserSearchView.setQuery(query, true);
        return true;
    }

    //this method will check the cart count and rearrange the checkout button layout accordingly
    private void checkCartCount() {
        RelativeLayout relativeLayoutButton, relativeLayoutBadge;
        relativeLayoutButton = findViewById(R.id.layoutRelativeCheckoutButton);
        relativeLayoutBadge = findViewById(R.id.layoutRelativeButtonBadge);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


        if (cartCount > 0) {
            ClearCartFab.setVisibility(View.VISIBLE);
            ClearCartFab.setClickable(true);
            layoutParams.setMargins(layoutParams.getMarginStart(), 0, layoutParams.getMarginEnd(), 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.layout.activity_user_menu);
            relativeLayoutButton.setLayoutParams(layoutParams); //the layout params is used to set the margin and the alignment

            //the margin is calculated by getting the device width and dividing it by 8 then reducing by 40
            //the reason for diving by eight is - the button's layout weight sum is 4, and the button has a weight of  3
            //this is because i wanted the button to span 3/4 of the device width. Since the badge has to be on the outer edge of
            //the button, iam calculating the space that is outside the button (which is 1/4 of device width)
            //but the 1/4 is on both sides, hence we divide it by 2 to get the space required by one side, which is
            //1/8 of device width. We set this as the badge left margin, so that the badge and the button's top-left corner are aligned
            //then we reduce that value by 40, which is the width of the badge
            int margin = (deviceWidth / 8) - 40;

            RelativeLayout.LayoutParams layoutParamsBadge = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParamsBadge.setMargins(margin, 0, 0, 0);
            layoutParamsBadge.addRule(RelativeLayout.ALIGN_START, R.id.layoutLinearCheckoutButton);
            layoutParamsBadge.addRule(RelativeLayout.ALIGN_TOP, R.id.layoutLinearCheckoutButton);
            relativeLayoutBadge.setLayoutParams(layoutParamsBadge);//the layout params is used to set the margin and the alignment
            relativeLayoutBadge.setVisibility(View.VISIBLE);
            TextView cartBadgeCount = findViewById(R.id.txtButtonBadge);
            cartBadgeCount.setText(String.valueOf(cartCount));
        } else {
            ClearCartFab.setVisibility(View.GONE);
            ClearCartFab.setClickable(false);
            //the typed value class is used to convert the pixels to dip (density independent pixels) so that it will hide the
            //checkout button when the cart is empty
            int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -60.0f, getResources().getDisplayMetrics());
            layoutParams.setMargins(layoutParams.getMarginStart(), 0, layoutParams.getMarginEnd(), value);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, R.layout.activity_user_menu);
            relativeLayoutButton.setLayoutParams(layoutParams); //the layout params is used to set the margin and the alignment
        }
    }

    //this method is used to generate a random 10-key integer to be used in the database
    private void generateRandomID() {
        //generating a random 10-digit number to be used as the shopping cart ID
        String randomNumbers = null;
        StringBuilder stringBuilder = new StringBuilder();//used to build string literals
        for (int i = 0; i <= 10; i++) {
            Random random = new Random();//class to generate random number
            stringBuilder.append(random.nextInt(9));//generating a random number that is from 0 to 9
            randomNumbers = stringBuilder.toString();
        }
        randomID = randomNumbers;
    }

    //i am clearing the cart once the user goes back just in case to prevent duplicate values
    @Override
    public void onBackPressed() {
        sqLiteHelper.dataQuery("DELETE FROM productCartTable");
        cartCount = 0;
        finish();
    }

    private void checkoutDialog() {
        getCheckoutDetails();
        checkoutDialog.setCancelable(true); //specifying that whether the dialog box is able to be cancelled
        checkoutDialog.setCanceledOnTouchOutside(true);//specifying that the dialog box should close when and if the user touches outside it
        checkoutDialog.show();
    }

    private void getCheckoutDetails() {
        CartItemsList.clear();
        int productNetTotalPrice = 0;
        //Toast.makeText(this, "ArrayLength: " + String.valueOf(sqLiteHelper.getCartCount(randomID)), Toast.LENGTH_SHORT).show();
        for (int i = 1; i <= sqLiteHelper.getCartCount(randomID); i++) {
            String cartId = randomID;
            int productId = sqLiteHelper.getCartProductId(i);
            int productQuantity = sqLiteHelper.getCartProductQuantity(i);
            int productPrice = sqLiteHelper.getProductPrice(productId);
            productNetTotalPrice += (productPrice * productQuantity);
            CartItemsList.add(new CartItems(cartId, productId, productQuantity));
        }

        //Toast.makeText(this, "ArrayLength: " + String.valueOf(CartItemsList.size()), Toast.LENGTH_SHORT).show();

        CartNetTotal.setText(getResources().getString(R.string.txtNetTotalPricePrefix, productNetTotalPrice));
        CheckoutItemsListAdapter.notifyDataSetChanged();
    }

    private void getCheckoutNetTotal(){
        int productNetTotalPrice = 0;
        for (int i = 1; i <= sqLiteHelper.getCartCount(randomID); i++) {
            int productId = sqLiteHelper.getCartProductId(i);
            int productQuantity = sqLiteHelper.getCartProductQuantity(i);
            int productPrice = sqLiteHelper.getProductPrice(productId);
            productNetTotalPrice += (productPrice * productQuantity);
        }

        CartNetTotal.setText(getResources().getString(R.string.txtNetTotalPricePrefix, productNetTotalPrice));
    }

    private void createOrder(){
        if(cartCount >= 1){
            int arrayLength = CartItemsList.size();
            String[] productIdList = new String[arrayLength];
            String[] productQuantityList = new String[arrayLength];
            int productNetTotalPrice = 0;

            for(int i = 0; i < arrayLength; i++){
                int productId = CartItemsList.get(i).getProductId();
                int productQuantity = CartItemsList.get(i).getProductQuantity();
                int productPrice = sqLiteHelper.getProductPrice(productId);

                productIdList[i] = String.valueOf(productId);
                productQuantityList[i] = String.valueOf(productQuantity);
                productNetTotalPrice += (productPrice * productQuantity);
            }
            sqLiteHelper.createOrder(randomID, memberId, productIdList, productQuantityList, productNetTotalPrice, 0);
            Toast.makeText(this, "Order Number: " + randomID + " has been sent for processing!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "The Checkout cart is empty", Toast.LENGTH_SHORT).show();
        }
        checkoutDialog.cancel();
    }

    //this will sort the product list according to the value provided
    //if value is 0, the list will be sorted according to the product price in ascending order
    //if value is 1, the list will be sorted according to the product price in descending order
    private void sortProductList(int value){
        //the collections class is used to reference the method that is implement by the array list object class - Products
        //i have provided the sort method in the product class to sort it by the product price in an ascending order
        Collections.sort(ProductList);
        //i just reverse the list to get it in the normal order
        if(value == 1){
            Collections.reverse(ProductList);
        }
        GridAdapter.notifyDataSetChanged();
        ListAdapter.notifyDataSetChanged();
    }
}

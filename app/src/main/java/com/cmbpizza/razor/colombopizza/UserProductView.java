package com.cmbpizza.razor.colombopizza;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

public class UserProductView extends Activity implements AppCompatCallback, View.OnClickListener {

    private static SQLLiteHelperProducts sqLiteHelper;
    public int productID;
    public static boolean isUserLoggedIn = false;
    String randomCartID;
    TextView ProductTitle, ProductDescription, ProductPrice;
    Button NumberStepperUp, NumberStepperDown, AddToCart;
    TextView NumberStepperView;
    EditText NumberStepperEdit;
    ImageView ProductImage;
    ViewSwitcher ViewSwitcherNumberStepper;
    Toolbar toolbar;
    ArrayList<Products> ProductList;
    CollapsingToolbarLayout collapsingToolbarLayout;

    //this method runs when the activity is created and run for the first time
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delegateAppcompat(savedInstanceState);
        initializeComponents();
        initializeListeners();
        sqlLiteDB();
        fillData();
        checkLoginStatus();
    }

    //this method will run when this activity is started(or resumed) again
    @Override
    protected void onStart() {
        super.onStart();
        checkLoginStatus();
    }

    //this method is used to help this activity support AppCompat elements
    //which is usually not supported since all my activities implement the old Activity class
    //therefore I am setting my content view in the AppCompat delegate mode(support mode)
    //in order to ensure that it supports some material design elements which only AppCompat supports
    private void delegateAppcompat(Bundle savedInstanceState) {
        AppCompatDelegate delegate;
        //let's create the delegate, passing the activity at both arguments (Activity, AppCompatCallback)
        delegate = AppCompatDelegate.create(this, this);

        //we need to call the onCreate() of the AppCompatDelegate
        delegate.onCreate(savedInstanceState);

        //we use the delegate to inflate the layout
        delegate.setContentView(R.layout.activity_user_product_view);

        //Finally, I add the toolbar so that it can initialize it in delegate mode
        toolbar = findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
    }

    //this method is used to initialize all the components and variables used in this class
    public void initializeComponents() {
        ProductList = new ArrayList<>();
        ProductImage = findViewById(R.id.imgUserViewProductImage);
        ProductTitle = findViewById(R.id.txtUserProductTitle);
        ProductDescription = findViewById(R.id.txtUserProductDescription);
        ProductPrice = findViewById(R.id.txtUserProductPrice);
        productID = Integer.parseInt(getIntent().getExtras().get("productId").toString());
        randomCartID = getIntent().getExtras().get("cartId").toString();
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapseToolbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandToolbar);
        ViewSwitcherNumberStepper = findViewById(R.id.viewSwitcherNumberStepper);
        AddToCart = findViewById(R.id.btnAddProductToCart);
        NumberStepperUp = findViewById(R.id.btnNumberStepperUp);
        NumberStepperDown = findViewById(R.id.btnNumberStepperDown);
        NumberStepperEdit = findViewById(R.id.txtNumberStepperEdit);
        NumberStepperView = findViewById(R.id.txtNumberStepperView);
        NumberStepperView.setText("1");
        NumberStepperEdit.setText("1");
    }

    //setting a click event listener to the components
    public void initializeListeners() {
        NumberStepperUp.setOnClickListener(this);
        NumberStepperDown.setOnClickListener(this);
        NumberStepperView.setOnClickListener(this);
        AddToCart.setOnClickListener(this);


        //this text listener is set on the number stepper's edit text to ensure that the it only accepts the proper number threshold
        NumberStepperEdit.addTextChangedListener(new TextWatcher() {
            private String value = NumberStepperEdit.getText().toString();

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                value = charSequence.toString();//this get the value that was present before the edit was taken place
                //this is taken so that we can replace it with the edited character if it does not conform to the threshold
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                //first i am checking if the edit text is empty
                if (String.valueOf(charSequence).isEmpty()) {
                    //if it is empty then we set it to the value that it was before it was empty
                    //this ensures that the edit text is never empty
                    NumberStepperEdit.setText(value);
                } else {
                    //if it is not empty then we check whether it is between 0 and 30, not including zero
                    //so if the user types a number other than the number between the threshold
                    //then the edit text will set it to the previous value that was there before the edit took place
                    int numValue = Integer.valueOf(charSequence.toString());
                    if (!(numValue > 0 && numValue <= 30)) {
                        NumberStepperEdit.setText(value);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    //this will check if the user that is currently viewing this form is a registered user or not
    private void checkLoginStatus() {
        isUserLoggedIn = getIntent().getExtras().get("userId") != null;
    }

    //this method is used to fill the product details on the relevant fields
    public void fillData() {
        ProductList = sqLiteHelper.getAllProductData(productID);
        toolbar.setTitle(ProductList.get(0).getProductCategory() + " | " + ProductList.get(0).getProductTitle());
        ProductTitle.setText(ProductList.get(0).getProductTitle());
        ProductDescription.setText(ProductList.get(0).getProductDescription());
        int productPrice = ProductList.get(0).getProductPrice();
        String priceText = getResources().getString(R.string.txtProductPricePrefix, productPrice);
        ProductPrice.setText(priceText);

        //here the image is converted from byte[] into a bitmap to display (since it is stored as bytes in the database)
        byte[] productImageByte = ProductList.get(0).getProductImage();
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(productImageByte, 0, productImageByte.length);
        ProductImage.setImageBitmap(imageBitmap);
    }

    //the following three methods are the instance method of the AppCompatDelegate class
    @Override
    public void onSupportActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {

    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    //this method is used to create a database (or at least an instance of it)
    private void sqlLiteDB() {
        sqLiteHelper = new SQLLiteHelperProducts(UserProductView.this, "ProductDB", null, 1);
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productTable (productId INTEGER PRIMARY KEY AUTOINCREMENT, productTitle VARCHAR, productCategory VARCHAR, productPrice INTEGER, productDescription VARCHAR, productImage BLOB)");
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productCartTable (cartId VARCHAR PRIMARY KEY, productCount INTEGER, idList VARCHAR, titleList INTEGER, priceList VARCHAR, quantityList VARCHAR, totalPrice INTEGER)");
    }

    /*I created this method to run a custom-made number switcher that can be used to easily
    * add or minus the number value which will be used to calculate the product quantity that
    * is added to the cart. The number Stepper uses a view switcher that contains both a
    * text view (to show the number) and an edit text (to edit the number) elements, which
    * switches view accordingly. The view switcher is flanked by two buttons to the left and right,
    * both the button are used to increment or decrement the number switcher(i.e. step the numbers)*/
    private void NumberSwitcher(char sign) {
        int number = 0;
        //we check the current view that is visible in the view switcher
        //then we get the number value that is displayed in that view
        //this is to ensure that we get the correct number value
        if (ViewSwitcherNumberStepper.getCurrentView().equals(NumberStepperView)) {
            number = Integer.valueOf(NumberStepperView.getText().toString());
        } else if (ViewSwitcherNumberStepper.getCurrentView().equals(NumberStepperEdit)) {
            //since this view is editable, if the edit text is empty we assign zero value to it
            //if it is not empty we get the value that is entered in the edit text
            if (String.valueOf(NumberStepperEdit.getText().toString()).isEmpty()) {
                number = 0;
            } else {
                number = Integer.valueOf(NumberStepperEdit.getText().toString());
            }
            ViewSwitcherNumberStepper.showNext();
            //this is to switch the view to text view when either of the button is pressed
        }
        //we check the sign char that will be provide on method call, and determine how the number will be
        //affected according to the sign, if its a plus number is incremented, if it is a minus it is decremented
        //we also set a min value of 1 and a max value of 30 for the number stepper
        if (sign == '+') {
            //if the number value is bigger than 30, the value will not be incremented
            if (number < 30) {
                number++;
            }
        } else if (sign == '-') {
            //if the number value is smaller than 1, the value will not be decremented
            if (number > 1) {
                number--;
            }
        }
        //we get the value of the number
        String numValue = String.valueOf(number);
        //then we set the number value to both the edit text and the text view to keep them in sync
        NumberStepperView.setText(numValue);
        NumberStepperEdit.setText(numValue);
    }

    //this method is used to add the currently shown product to the shopping cart
    private void addProductToCart() {
        String EditValue = NumberStepperEdit.getText().toString();
        String ViewValue = NumberStepperView.getText().toString();

        //first we check whether the number stepper value is empty (highly unlikely to happen since i made some extra
        //security measures inside the number stepper method itself
        //then we check whether both the number stepper values (edit text & text view) are equal => as an extra security measure
        //then we update the already created record (which was created on the user menu, so that we can keep track of the count)
        //before updating we calculate the product total with the quantity specified in the number stepper
        //for the first record we start with a single product count
        //For the records that are after the first record we just increment the total count,
        //then we also calculate the total using the number stepper value
        //most of the methods used below are from the sql lite helper class which i created to ease the db process

        if(isUserLoggedIn){
            int productQuantity = Integer.valueOf(EditValue);
            if (!String.valueOf(EditValue).isEmpty()) {
                if (ViewValue.equals(EditValue)) {
                    sqLiteHelper.createCart(randomCartID, productID, productQuantity);
                }
            }
        } else {
            Toast.makeText(this, "Please register or login to order products", Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtNumberStepperView:
                ViewSwitcherNumberStepper.showNext();
                NumberStepperEdit.requestFocus();
                break;
            case R.id.btnNumberStepperUp:
                NumberSwitcher('+');
                break;
            case R.id.btnNumberStepperDown:
                NumberSwitcher('-');
                break;
            case R.id.btnAddProductToCart:
                addProductToCart();
                finish();
                break;
        }
    }
}

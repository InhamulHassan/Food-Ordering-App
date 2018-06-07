package com.cmbpizza.razor.colombopizza;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.IOException;
import java.util.ArrayList;

public class AdminProductView extends Activity implements View.OnClickListener {

    private static SQLLiteHelperProducts sqLiteHelper;
    final int REQUEST_CODE_GALLERY = 999;
    public int productID;
    ArrayList<Products> ProductList;
    Button EditButton, CancelButton, DeleteButton, UpdateButton;
    TextView ProductTitleAdmin, ProductDescriptionAdmin, ProductCategoryAdmin, ProductPriceAdmin;
    EditText EditProductTitle, EditProductDescription, EditProductPrice;
    Spinner EditProductCategory;
    ImageView ProductImageAdmin, EditProductImage;
    RelativeLayout EditImageLayout;

    //this method runs when the activity is created and run for the first time
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_view);
        initializeComponents();
        initializeListeners();
        sqlLiteDB();
        fillData();
    }

    //this method is used to fill the product details on the relevant fields
    public void fillData() {
        ProductList = sqLiteHelper.getAllProductData(productID);
        ProductTitleAdmin.setText(ProductList.get(0).getProductTitle());
        ProductDescriptionAdmin.setText(ProductList.get(0).getProductDescription());
        ProductCategoryAdmin.setText(ProductList.get(0).getProductCategory());
        int productPrice = ProductList.get(0).getProductPrice();
        String priceText = getResources().getString(R.string.txtProductPricePrefix, productPrice);
        ProductPriceAdmin.setText(priceText);

        //here the image is converted from byte[] into a bitmap to display (since it is stored as bytes in the database)
        byte[] productImageByte = ProductList.get(0).getProductImage();
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(productImageByte, 0, productImageByte.length);
        ProductImageAdmin.setImageBitmap(imageBitmap);
    }

    //this method is used to initialize all the components and variables used in this class
    private void initializeComponents() {
        ProductList = new ArrayList<>();
        ProductTitleAdmin = findViewById(R.id.txtMainProductTitle);
        ProductDescriptionAdmin = findViewById(R.id.txtMainProductDescription);
        ProductCategoryAdmin = findViewById(R.id.txtMainProductCategory);
        ProductPriceAdmin = findViewById(R.id.txtMainProductPrice);
        ProductImageAdmin = findViewById(R.id.imgMainProductImage);
        EditButton = findViewById(R.id.btnEditProductDetailsAdmin);
        CancelButton = findViewById(R.id.btnEditCancelProductDetailsAdmin);
        DeleteButton = findViewById(R.id.btnDeleteProductAdmin);
        UpdateButton = findViewById(R.id.btnUpdateProductDetailsAdmin);
        productID = Integer.parseInt(getIntent().getExtras().get("productId").toString());
        EditProductImage = findViewById(R.id.imgMainProductImageUpdate);
        EditImageLayout = findViewById(R.id.EditImageLayout);
        EditProductTitle = findViewById(R.id.txtMainProductTitleUpdate);
        EditProductCategory = findViewById(R.id.txtMainProductCategoryUpdate);
        EditProductDescription = findViewById(R.id.txtMainProductDescriptionUpdate);
        EditProductPrice = findViewById(R.id.txtMainProductPriceUpdate);
    }

    //setting a click event listener to the components
    private void initializeListeners() {
        EditButton.setOnClickListener(this);
        CancelButton.setOnClickListener(this);
        DeleteButton.setOnClickListener(this);
        UpdateButton.setOnClickListener(this);
        EditImageLayout.setOnClickListener(this);
    }

    //this is the onClick listener
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnEditProductDetailsAdmin:
                switchView();
                break;
            case R.id.btnEditCancelProductDetailsAdmin:
                switchView();
                break;
            case R.id.btnDeleteProductAdmin:
                deleteConfirmationDialog();
                break;
            case R.id.btnUpdateProductDetailsAdmin:
                updateProduct();
                break;
            case R.id.EditImageLayout:
                chooseImage();
                break;
        }
    }

    //asking the device permission to access external storage (or gallery to choose images)
    private void chooseImage() {
        ActivityCompat.requestPermissions(
                AdminProductView.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_GALLERY
        );
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
                Toast.makeText(AdminProductView.this, "You do not have the permission to access the gallery", Toast.LENGTH_SHORT).show();
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
            //we use the display metrics to get he width of the image view layout on runtime
            //(since I have specified the width as FILL_PARENT)
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            //here we are setting our image product view's source image as the image that we chose from gallery
            EditProductImage.setImageBitmap(AdminMenu.decodeBitmapFromFilePath(AdminMenu.getPath(uri, this), 200, displayMetrics.widthPixels));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //this activity is used to switch the view between the details view and the edit details view
    private void switchView() {
        fillData();
        EditProductTitle.setText(ProductList.get(0).getProductTitle());
        EditProductDescription.setText(ProductList.get(0).getProductDescription());
        EditProductPrice.setText(String.valueOf(ProductList.get(0).getProductPrice()));
        String CategoryName = ProductList.get(0).getProductCategory();
        for (int i = 0; i < EditProductCategory.getCount(); i++) {
            if (EditProductCategory.getItemAtPosition(i).equals(CategoryName)) {
                EditProductCategory.setSelection(i);
            }
        }
        byte[] productImageByte = ProductList.get(0).getProductImage();
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(productImageByte, 0, productImageByte.length);
        EditProductImage.setImageBitmap(imageBitmap);
        ViewSwitcher viewSwitcherImage = findViewById(R.id.ViewSwitcherImage);
        viewSwitcherImage.showNext();
        ViewSwitcher viewSwitcherTitle = findViewById(R.id.ViewSwitcherTitle);
        viewSwitcherTitle.showNext();
        ViewSwitcher viewSwitcherDescription = findViewById(R.id.ViewSwitcherDescription);
        viewSwitcherDescription.showNext();
        ViewSwitcher viewSwitcherCategory = findViewById(R.id.ViewSwitcherCategory);
        viewSwitcherCategory.showNext();
        ViewSwitcher viewSwitcherPrice = findViewById(R.id.ViewSwitcherPrice);
        viewSwitcherPrice.showNext();
        ViewSwitcher viewSwitcherButton = findViewById(R.id.ViewSwitcherButton);
        viewSwitcherButton.showNext();
    }

    //this method will delete the data from the sql db
    private void deleteProduct() {
        try {
            sqLiteHelper.deleteData(productID);
            Toast.makeText(this, "Product deleted successfully!", Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            Toast.makeText(this, "Delete Unsuccessful!\nError: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        finish();//then it will close the activity
    }

    //this will make a delete confirmation dialog
    private void deleteConfirmationDialog() {
        final AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(AdminProductView.this);
        confirmationDialog.setCancelable(false);
        confirmationDialog.setMessage("Are you sure you want to delete this product?");
        confirmationDialog.setTitle("Delete Product");
        confirmationDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteProduct();
            }
        });
        confirmationDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        confirmationDialog.show();
    }

    //this method is used to update the product details in the sql db
    private void updateProduct() {
        AdminMenu adminMenu = new AdminMenu();
        try {
            if (inputValidation()) {
                sqLiteHelper.updateData(
                        productID,
                        EditProductTitle.getText().toString(),
                        EditProductCategory.getSelectedItem().toString(),
                        Integer.parseInt(EditProductPrice.getText().toString()),
                        EditProductDescription.getText().toString(),
                        adminMenu.imageViewToByte(EditProductImage));
            }
            Toast.makeText(this, "Product details updated successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Update Unsuccessful!\nError: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        switchView();
    }

    //this method will check the input fields and validate them before submitting them
    private boolean inputValidation() {
        boolean valid;
        Drawable drawable = EditProductImage.getDrawable();//this will get the drawable from the image view
        //we check if the drawable in the image view is not null (ie. that it contains an image)
        //to do that we check whether the Drawable is an instance of type Bitmap Drawable
        //what that means is that if the drawable is of instance bitmap drawable then that means that the drawable
        //contains a bitmap, thus it is not empty, we use a tertiary conditional operator to check this
        BitmapDrawable bitmapDrawable = drawable instanceof BitmapDrawable ? (BitmapDrawable)drawable : null;
        if (drawable == null || bitmapDrawable.getBitmap() == null || bitmapDrawable == null) {
            valid = false;
            Toast.makeText(this, "Please include the product image", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(EditProductTitle.getText()).isEmpty()) {
            valid = false;
            Toast.makeText(this, "Please enter the product title", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(EditProductDescription.getText()).isEmpty()) {
            valid = false;
            Toast.makeText(this, "Please enter the product description", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(EditProductPrice.getText()).isEmpty()) {
            valid = false;
            Toast.makeText(this, "Please enter the product price", Toast.LENGTH_SHORT).show();
        } else {
            valid = true;
        }

        return valid;
    }

    //this method is used to create a database (or at least an instance of it)
    private void sqlLiteDB() {
        sqLiteHelper = new SQLLiteHelperProducts(AdminProductView.this, "ProductDB", null, 1);
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS productTable (productId INTEGER PRIMARY KEY AUTOINCREMENT, productTitle VARCHAR, productCategory VARCHAR, productPrice INTEGER, productDescription VARCHAR, productImage BLOB)");
    }
}

package com.cmbpizza.razor.colombopizza;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class LoginScreen extends Activity implements View.OnClickListener, GestureDetector.OnGestureListener{

    Button LoginAccessButton, MainLoginButton, RegisterButton;
    AutoCompleteTextView LoginEmail;
    EditText FirstName, LastName, Mobile, Email, Password, ConfirmPassword, LoginPassword;
    CheckBox CheckTnC;
    ImageView LoginBackgroundImage, LoginBottomImage;
    GestureDetector gestureDetector;
    private static SQLLiteHelperProducts sqLiteHelper;
    public Dialog loginDialog; //creating a new instance of the Dialog class object, and specifying its context as the current class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        //this is to prevent the keyboard from showing when the app starts
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initializeComponents();
        initializeListeners();
        initializeAnimators();
        initializeGestures();
        sqlLiteDB();
        setBackgroundImage();
    }

    //initializing the buttons and views that is being used in this class
    private void initializeComponents(){
        //initializing and declaring button to this java class by referring to the button ID specified in the XML
        LoginAccessButton = findViewById(R.id.btnLogin);
        RegisterButton = findViewById(R.id.btnRegister);
        LoginBottomImage = findViewById(R.id.login_bottom_image);
        LoginBackgroundImage = findViewById(R.id.loginBackgroundImage);

        FirstName = findViewById(R.id.txtFirstName);
        LastName = findViewById(R.id.txtLastName);
        Mobile = findViewById(R.id.txtMobileNumber);
        Email = findViewById(R.id.txtEmail);
        Password = findViewById(R.id.txtPassword);
        ConfirmPassword = findViewById(R.id.txtConfirmPassword);
        CheckTnC = findViewById(R.id.chkTermsAndConditions);

        loginDialog = new Dialog(LoginScreen.this);
        //specifying the layout/content of the dialog by referring to the XML file
        loginDialog.setContentView(R.layout.login_dialog_screen);
        MainLoginButton = loginDialog.findViewById(R.id.btnLoginMain);
        LoginEmail = loginDialog.findViewById(R.id.txtLoginEmail);
        LoginPassword = loginDialog.findViewById(R.id.txtLoginPassword);
        loginDialog.setCancelable(true); //specifying that whether the dialog box is able to be cancelled
        loginDialog.setCanceledOnTouchOutside(true);//specifying that the dialog box should close when and if the user touches outside it
    }

    //used to initialize the animations that is used in this activity
    private void initializeAnimators(){
        Drawable drawable =  LoginBottomImage.getDrawable();
        if(drawable instanceof Animatable){
            ((Animatable)drawable).start();
        }
    }

    //setting a click event listener to these buttons
    private void initializeListeners(){
        LoginAccessButton.setOnClickListener(this);
        RegisterButton.setOnClickListener(this);
        MainLoginButton.setOnClickListener(this);
    }

    //used to initialize the gestures used in this activity
    private void initializeGestures(){
        //this gesture detector will monitor for any gestures performed in this activity
        //the on touch listener is used to restrain the gesture monitoring activity to
        //be constrained to a particular view in this case the bottom of the screen layout)
        gestureDetector = new GestureDetector(LoginScreen.this ,this);
        findViewById(R.id.login_bottom_image).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
    }

    //set login's background image
    private void setBackgroundImage(){
        /* adapt the image to the size of the display */
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Bitmap bmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                getResources(),R.drawable.pizza_main_background),size.x,size.y,true);

    /* fill the background ImageView with the resized image */
        LoginBackgroundImage.setImageBitmap(bmp);
    }

    //this method is used to set the onClick listeners that is used in this activity
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnLogin:
                loginDialog.show();
                break;
            case R.id.btnRegister:
                registerUser();
                break;
            case R.id.btnLoginMain:
                checkUser();
                break;
        }
    }

    //this event will fire when the user touches any point inside a certain element(view) on the screen
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    //this event will fire when the user does the fling motion on the screen
    //when a user drags and lifts their finger quickly
    @Override
    public boolean onFling(MotionEvent start, MotionEvent end, float v, float v1) {
        if(start.getY() > end.getY()){
            //if the user flings from bottom to top (slide up)
            Intent newIntent = new Intent(LoginScreen.this, UserMenu.class);
            startActivity(newIntent);
            overridePendingTransition(R.anim.layout_slide_up, R.anim.back_layout_slide_up);
            return true;
        } else if(start.getX() > end.getX() || start.getY() < end.getY() || start.getX() < end.getX()){
            //ignore other fling motions in other directions
            return false;
        } else{
            return false;
        }
    }

    //this method is used to create a database
    private void sqlLiteDB() {
        sqLiteHelper = new SQLLiteHelperProducts(LoginScreen.this, "ProductDB", null, 1);
        sqLiteHelper.dataQuery("CREATE TABLE IF NOT EXISTS userTable (userId INTEGER PRIMARY KEY AUTOINCREMENT, userFirstName VARCHAR, userLastName VARCHAR, userMobile VARCHAR, userEmail VARCHAR, userPassword VARCHAR)");

    }

    private void checkUser(){
        String AdminEmail = "admin";
        String AdminPass = "pass";
        if(inputLoginValidation()){
            String Email = LoginEmail.getText().toString();
            String Password = LoginPassword.getText().toString();
            if(Email.equals(AdminEmail) && Password.equals(AdminPass)){
                Intent newIntent = new Intent(LoginScreen.this, AdminMenu.class);
                startActivity(newIntent);
                clearEditTextValues();
                loginDialog.cancel();
            } else {
                if(sqLiteHelper.checkEmail(Email)){
                    if(sqLiteHelper.checkPassword(Email, Password)){
                        int UserId = sqLiteHelper.getUserId(Email);
                        Intent newIntent = new Intent(LoginScreen.this, UserMenu.class);
                        newIntent.putExtra("userId", UserId);
                        startActivity(newIntent);
                        clearEditTextValues();
                        loginDialog.cancel();
                    } else {
                        Toast.makeText(LoginScreen.this, "Your password is incorrect, please try again", Toast.LENGTH_SHORT).show();
                        LoginPassword.requestFocus();
                    }
                } else {
                    Toast.makeText(LoginScreen.this, "Your email does not exist", Toast.LENGTH_SHORT).show();
                    LoginEmail.requestFocus();
                }
            }
        }
    }

    private void clearEditTextValues(){
        LoginEmail.getText().clear();
        LoginPassword.getText().clear();
    }

    private void registerUser(){
        if(inputValidation()){
            String UserFirstName = FirstName.getText().toString();
            String UserLastName = LastName.getText().toString();
            String UserMobile = Mobile.getText().toString();
            String UserEmail = Email.getText().toString();
            String UserPassword = Password.getText().toString();
            sqLiteHelper.registerUser(UserFirstName, UserLastName, UserMobile, UserEmail, UserPassword);
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
            clearRegistrationForm();
            int UserId = sqLiteHelper.getUserId(UserEmail);
            Intent newIntent = new Intent(LoginScreen.this, UserMenu.class);
            newIntent.putExtra("userId", UserId);
            startActivity(newIntent);
            overridePendingTransition(R.anim.layout_slide_up, R.anim.back_layout_slide_up);
        }
    }

    private void clearRegistrationForm() {
        FirstName.getText().clear();
        LastName.getText().clear();
        Mobile.getText().clear();
        Email.getText().clear();
        Password.getText().clear();
        ConfirmPassword.getText().clear();
        CheckTnC.setSelected(false);
    }

    //this method will check the input fields and validate them before submitting them
    private boolean inputValidation() {
        boolean valid;
        String UserFirstName = FirstName.getText().toString();
        String UserLastName = LastName.getText().toString();
        String UserMobile = Mobile.getText().toString();
        String UserEmail = Email.getText().toString();
        String UserPassword = Password.getText().toString();
        String UserConfirmPassword = ConfirmPassword.getText().toString();
        Boolean TnCConfirmed = CheckTnC.isChecked();
        if (UserFirstName.isEmpty()) {
            valid = false;
            FirstName.requestFocus();
            Toast.makeText(this, "Please enter your First Name", Toast.LENGTH_SHORT).show();
        } else if (UserLastName.isEmpty()) {
            valid = false;
            LastName.requestFocus();
            Toast.makeText(this, "Please enter your Last Name", Toast.LENGTH_SHORT).show();
        } else if (UserMobile.isEmpty()) {
            valid = false;
            Mobile.requestFocus();
            Toast.makeText(this, "Please enter Mobile Number", Toast.LENGTH_SHORT).show();
        } else if (UserEmail.isEmpty()) {
            valid = false;
            Email.requestFocus();
            Toast.makeText(this, "Please enter your Email Address", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(UserEmail).matches()) {
            valid = false;
            LoginEmail.requestFocus();
            Toast.makeText(this, "Please enter a valid Email Address", Toast.LENGTH_SHORT).show();
        } else if (UserPassword.isEmpty()) {
            valid = false;
            Password.requestFocus();
            Toast.makeText(this, "Please provide a Password", Toast.LENGTH_SHORT).show();
        } else if (UserConfirmPassword.isEmpty()) {
            valid = false;
            ConfirmPassword.requestFocus();
            Toast.makeText(this, "Re-enter your Password in the Confirm Password field", Toast.LENGTH_SHORT).show();
        } else if (!UserPassword.equals(UserConfirmPassword)) {
            valid = false;
            ConfirmPassword.requestFocus();
            Toast.makeText(this, "Your Confirm Password value does not match your Password value", Toast.LENGTH_SHORT).show();
        } else if (!TnCConfirmed) {
            valid = false;
            Toast.makeText(this, "You have to accept out Terms and Conditions before registering", Toast.LENGTH_SHORT).show();
        } else {
            valid = true;
        }
        return valid;
    }

    private boolean inputLoginValidation() {
        boolean valid;
        String Email = LoginEmail.getText().toString();
        String Password = LoginPassword.getText().toString();
        if (String.valueOf(Email).isEmpty()) {
            valid = false;
            LoginEmail.requestFocus();
            Toast.makeText(this, "Please enter your Email Address", Toast.LENGTH_SHORT).show();
        } else if (String.valueOf(Password).isEmpty()) {
            valid = false;
            LoginPassword.requestFocus();
            Toast.makeText(this, "Please enter your Password", Toast.LENGTH_SHORT).show();
        } else if(Email.equals("admin")) {
            valid = true; //this is to ensure that when the admin enters the admin username (which is not an email), that no validation error occurs
        } else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            valid = false;
            LoginEmail.requestFocus();
            Toast.makeText(this, "Please enter a valid Email Address", Toast.LENGTH_SHORT).show();
        } else {
            valid = true;
        }

        return valid;
    }
}

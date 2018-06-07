package com.cmbpizza.razor.colombopizza;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by RazR created on 1/31/2018.
 * SQL Helper will allow me to use all the sql query easily
 */

public class SQLLiteHelperProducts extends SQLiteOpenHelper {

    SQLLiteHelperProducts(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //this method converts array to string to be put into db
    private static String convertArrayToString(String[] array) {
        String str = "";
        for (int i = 0; i < array.length; i++) {
            str = str + array[i];
            if (i < (array.length - 1)) {
                str = str + ",";
            }
        }
        return str;
    }

    //this method converts string to array to be put into db
    private static String[] convertStringToArray(String string) {
        String[] array;
        array = string.split(",");
        return array;
    }

    void dataQuery(String sql) {
        SQLiteDatabase sqlDB = getWritableDatabase();
        sqlDB.execSQL(sql);
    }

    void insertData(String productTitle, String productCategory, int productPrice, String productDescription, byte[] productImage) {
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sql = "INSERT INTO productTable VALUES (NULL, ?, ?, ?, ?, ?)";
        SQLiteStatement statement = sqlDB.compileStatement(sql);
        statement.clearBindings();
        statement.bindString(1, productTitle);
        statement.bindString(2, productCategory);
        statement.bindDouble(3, productPrice);
        statement.bindString(4, productDescription);
        statement.bindBlob(5, productImage);

        statement.executeInsert();
    }

    void updateData(int productId, String productTitle, String productCategory, int productPrice, String productDescription, byte[] productImage) {
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sql = "UPDATE productTable SET productTitle=?, productCategory=?, productPrice=?, productDescription=?, productImage=? WHERE productId=?";
        SQLiteStatement statement = sqlDB.compileStatement(sql);
        statement.clearBindings();
        statement.bindString(1, productTitle);
        statement.bindString(2, productCategory);
        statement.bindDouble(3, productPrice);
        statement.bindString(4, productDescription);
        statement.bindBlob(5, productImage);
        statement.bindDouble(6, productId);

        statement.executeUpdateDelete();
    }

    void deleteData(int productId) {
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sql = "DELETE FROM productTable WHERE productId=?";
        SQLiteStatement statement = sqlDB.compileStatement(sql);
        statement.clearBindings();
        statement.bindDouble(1, productId);
        statement.executeUpdateDelete();
        statement.close();
    }

    Cursor getData(String sql) {
        SQLiteDatabase sqlDB = getReadableDatabase();
        return sqlDB.rawQuery(sql, null);
    }

    /*long getCount(String tableName) {
        long count;
        SQLiteDatabase db = getReadableDatabase();
        count = DatabaseUtils.queryNumEntries(db, tableName);
        db.close();
        return count;
    }*/

    void createCart(String cartId, int productId, int productQuantity) {
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sql = "INSERT INTO productCartTable VALUES (NULL, ?, ?, ?)";
        SQLiteStatement statement = sqlDB.compileStatement(sql);
        statement.clearBindings();
        statement.bindString(1, cartId);
        statement.bindDouble(2, productId);
        statement.bindDouble(3, productQuantity);

        statement.executeInsert();
    }

    void updateCart(int id, String cartId, int productId, int productQuantity) {
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sql = "UPDATE productCartTable SET cartId=?, productId=?, productQuantity=? WHERE id=?";
        SQLiteStatement statement = sqlDB.compileStatement(sql);
        statement.clearBindings();
        statement.bindString(1, cartId);
        statement.bindDouble(2, productId);
        statement.bindDouble(3, productQuantity);
        statement.bindDouble(4, id);
        statement.executeUpdateDelete();
    }

    void clearCartItem(int id, int productId) {
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sql;
        sql = "DELETE FROM sqlite_sequence WHERE name='productCartTable';";
        SQLiteStatement DeleteStatement = sqlDB.compileStatement(sql);
        DeleteStatement.executeUpdateDelete();
        DeleteStatement.close();
        if(id == 1){
            //this one will execute when the id value is equal to one, so
            sql = "DELETE FROM productCartTable WHERE productId=?;";
            SQLiteStatement statement = sqlDB.compileStatement(sql);
            statement.clearBindings();
            statement.bindDouble(1, productId);
            statement.executeUpdateDelete();
            statement.close();
            String updateSql = "UPDATE productCartTable SET id=(id-1);";
            SQLiteStatement statement2 = sqlDB.compileStatement(updateSql);
            statement2.clearBindings();
            statement2.executeUpdateDelete();
            statement2.close();
        } else{
            sql = "DELETE FROM productCartTable WHERE productId=?;";
            SQLiteStatement statement = sqlDB.compileStatement(sql);
            statement.clearBindings();
            statement.bindDouble(1, productId);
            statement.executeUpdateDelete();
            statement.close();
            String updateSql = "UPDATE productCartTable SET id=(id-1) WHERE id>=?;";
            SQLiteStatement statement2 = sqlDB.compileStatement(updateSql);
            statement2.clearBindings();
            statement2.bindDouble(1, id);
            statement2.executeUpdateDelete();
            statement2.close();
        }
    }

    int getCartCount(String cartId) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM productCartTable WHERE cartId=?";
        SQLiteDatabase sqlDB = this.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{cartId});
        while (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        sqlDB.close();
        return count;
    }

    String getCartId(int id) {
        String cartId = null;
        String sql = "SELECT cardId FROM productCartTable WHERE id=?";
        SQLiteDatabase sqlDB = this.getReadableDatabase();
        String idValue = String.valueOf(id);
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{idValue});
        while (cursor.moveToNext()) {
            cartId = cursor.getString(0);
        }
        cursor.close();
        sqlDB.close();
        return cartId;
    }

    int getCartProductQuantity(int id) {
        int quantity = 0;
        String sql = "SELECT productQuantity FROM productCartTable WHERE id=?";
        SQLiteDatabase sqlDB = this.getReadableDatabase();
        String idValue = String.valueOf(id);
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{idValue});
        while (cursor.moveToNext()) {
            quantity = cursor.getInt(0);
        }
        cursor.close();
        sqlDB.close();
        return quantity;
    }

    int getCartProductId(int id) {
        int productId = 0;
        String sql = "SELECT productId FROM productCartTable WHERE id=?";
        SQLiteDatabase sqlDB = this.getReadableDatabase();
        String idValue = String.valueOf(id);
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{idValue});
        while (cursor.moveToNext()) {
            productId = cursor.getInt(0);
        }
        cursor.close();
        sqlDB.close();
        return productId;
    }

    int getCartRowID(int productId) {
        int rowId = 0;
        String sql = "SELECT id FROM productCartTable WHERE productId=?";
        SQLiteDatabase sqlDB = this.getReadableDatabase();
        String idValue = String.valueOf(productId);
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{idValue});
        while (cursor.moveToNext()) {
            rowId = cursor.getInt(0);
        }
        cursor.close();
        sqlDB.close();
        return rowId;
    }

    /*ArrayList<CheckoutItems> getAllCheckoutItems(String cartId){
        ArrayList<CheckoutItems> checkoutItems = new ArrayList<>();
        SQLiteDatabase sqlDB = getReadableDatabase();
        String sql = "SELECT * FROM productCartTable WHERE cartId = ?";
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{cartId});
        checkoutItems.clear();//this is to clear the view of previous search
        //we go through each element in the cursor (i.e our sql query result data)
        //we go through each element in the cursor (i.e our sql query result data)
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String[] idList = convertStringToArray(cursor.getString(2));
            String[] titleList = convertStringToArray(cursor.getString(3));
            String[] priceList = convertStringToArray(cursor.getString(4));
            String[] quantityList = convertStringToArray(cursor.getString(5));
            int priceTotal = cursor.getInt(6);
            //we add each of the table elements that we get from the cursor into the checkout array
            checkoutItems.add(new CheckoutItems(id, idList, titleList, priceList, quantityList, priceTotal));
        }
        cursor.close();
        return checkoutItems;
    }*/


    void createOrder(String orderID, String memberId, String[] idList, String[] quantityList, int netTotal, int orderStatus) {
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sql = "INSERT INTO productOrderTable VALUES (?, ?, ?, ?, ?, ?)";
        SQLiteStatement statement = sqlDB.compileStatement(sql);
        statement.clearBindings();
        statement.bindString(1, orderID);
        statement.bindDouble(2, Integer.valueOf(memberId));
        statement.bindString(3, convertArrayToString(idList));
        statement.bindString(4, convertArrayToString(quantityList));
        statement.bindDouble(5, netTotal);
        statement.bindDouble(6, orderStatus); //0:ordered   1:confirmed

        statement.executeInsert();
    }

    //orderStatus 0:just-ordered 1:order-confirmed
    void confirmOrderStatus(String orderId){
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sql = "UPDATE productOrderTable SET orderStatus=1 WHERE orderId=?";
        SQLiteStatement statement = sqlDB.compileStatement(sql);
        statement.clearBindings();
        statement.bindString(1, orderId);
        statement.executeUpdateDelete();
    }

    int getOrderCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM productOrderTable WHERE orderStatus=0";
        SQLiteDatabase sqlDB = this.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        sqlDB.close();
        return count;
    }

    ArrayList<Orders> getAllOrderItems(){
        ArrayList<Orders> ordersArrayList = new ArrayList<>();
        SQLiteDatabase sqlDB = getReadableDatabase();
        String sql = "SELECT * FROM productOrderTable WHERE orderStatus=0";
        Cursor cursor = sqlDB.rawQuery(sql,null);
        ordersArrayList.clear();//this is to clear the view of previous search
        //we go through each element in the cursor (i.e our sql query result data)
        //we go through each element in the cursor (i.e our sql query result data)
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            int memberId = cursor.getInt(1);
            String[] idList = convertStringToArray(cursor.getString(2));
            String[] quantityList = convertStringToArray(cursor.getString(3));
            int priceNetTotal = cursor.getInt(4);
            int orderStatus = cursor.getInt(5);
            //we add each of the table elements that we get from the cursor into the checkout array
            ordersArrayList.add(new Orders(id, memberId, idList, quantityList, priceNetTotal, orderStatus));
        }
        cursor.close();
        return ordersArrayList;
    }

    int getProductID(int productId) {
        int count = 0;
        String sql = "SELECT productID FROM ProductTable WHERE productId=?";
        SQLiteDatabase sqlDB = this.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{String.valueOf(productId)});
        while (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    String getProductTitle(int productId) {
        String name = null;
        String sql = "SELECT productTitle FROM ProductTable WHERE productId=?";
        SQLiteDatabase sqlDB = this.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{String.valueOf(productId)});
        while (cursor.moveToNext()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    int getProductPrice(int productId) {
        int price = 0;
        String sql = "SELECT productPrice FROM ProductTable WHERE productId=?";
        SQLiteDatabase sqlDB = this.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{String.valueOf(productId)});
        while (cursor.moveToNext()) {
            price = cursor.getInt(0);
        }
        cursor.close();
        return price;
    }

    void registerUser(String firstName, String lastName, String mobileNumber, String email, String password){
        SQLiteDatabase sqlDB = getWritableDatabase();
        String sql = "INSERT INTO userTable VALUES (NULL, ?, ?, ?, ?, ?)";
        SQLiteStatement statement = sqlDB.compileStatement(sql);
        statement.clearBindings();
        statement.bindString(1, firstName);
        statement.bindString(2, lastName);
        statement.bindString(3, mobileNumber);
        statement.bindString(4, email);
        statement.bindString(5, password);

        statement.executeInsert();
    }

    //this method gets the email and password and checks it against the db and
    //returns true if the email is present and the password matches, it return false if
    // the email is not there in db or if the password doesn't match

    boolean checkPassword(String email, String password){
        String sql = "SELECT userPassword FROM userTable WHERE userEmail=?";
        SQLiteDatabase sqlDB = this.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{email});
        boolean isValid = false;
        while (cursor.moveToNext()){
            String pass = cursor.getString(0);
            isValid = password.equals(pass);
        }
        cursor.close();
        return isValid;
    }

    //this method checks if the supplied email is in the db
    boolean checkEmail(String email){
        String sql = "SELECT userEmail FROM userTable WHERE userEmail=?";
        SQLiteDatabase sqlDB = this.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{email});
        boolean isValid = cursor.moveToFirst();
        cursor.close();
        return isValid;
    }

    int getUserId(String email){
        String sql = "SELECT userId FROM userTable WHERE userEmail=?";
        SQLiteDatabase sqlDB = this.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{email});
        int userId = 0;
        while (cursor.moveToNext()){
            userId = cursor.getInt(0);
        }
        cursor.close();
        return userId;
    }

    byte[] getProductImage(int productId) {
        byte[] imgByte = null;
        String sql = "SELECT productImage FROM ProductTable WHERE productId=?";
        SQLiteDatabase sqlDB = this.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{String.valueOf(productId)});
        while (cursor.moveToNext()) {
            imgByte = cursor.getBlob(0);
        }
        cursor.close();
        return imgByte;
    }

    ArrayList<Products> getAllProductData(int productId) {
        ArrayList<Products> ProductList = new ArrayList<>();
        SQLiteDatabase sqlDB = getReadableDatabase();
        String sql = "SELECT * FROM productTable WHERE productId=?";
        Cursor cursor = sqlDB.rawQuery(sql, new String[]{String.valueOf(productId)});

        //we go through each element in the cursor (i.e our sql query result data)
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String category = cursor.getString(2);
            int price = cursor.getInt(3);
            String description = cursor.getString(4);
            byte[] image = cursor.getBlob(5);

            //we add each of the table elements that we get from the cursor into the product array
            ProductList.add(new Products(id, title, category, price, description, image));
        }
        cursor.close();
        return ProductList;
    }

    ArrayList<Products> getAllData() {
        ArrayList<Products> ProductList = new ArrayList<>();
        SQLiteDatabase sqlDB = getReadableDatabase();
        String sql = "SELECT * FROM productTable";
        Cursor cursor = sqlDB.rawQuery(sql, null);
        ProductList.clear();//this is to clear the view of previous search
        //we go through each element in the cursor (i.e our sql query result data)
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String category = cursor.getString(2);
            int price = cursor.getInt(3);
            String description = cursor.getString(4);
            byte[] image = cursor.getBlob(5);
            //we add each of the table elements that we get from the cursor into the product array
            ProductList.add(new Products(id, title, category, price, description, image));
        }
        cursor.close();
        return ProductList;
    }




    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

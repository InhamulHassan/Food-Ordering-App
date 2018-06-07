Food Ordering App
=================
A Food Ordering Android application, with SQLLite as the back-end RDBMS. This app has built-in administrative access. 

Functions:
- General functions:
	- User registration.
	- View food menu.
	- Filter food menu by price.
	- View product details.
- Member functions:
	- Login as a member.
	- View food menu and product information.
	- Place food orders.
- Admin functions:
	- Add new product to the menu.
	- Delete products from the menu.
	- Modify product information.
	- Confirm orders placed by members.

Requirements:
- Android Studio (3.0+)
- SQLLite

Setup: 
1. Open the folder in Android Studio as a project.
2. Build and run the project using the android simulator.
3. Once the app loads, do the following steps:
	- Open the Device File Explorer.
	- Goto data/data/[src location]/databases.
	- Upload the 'ProductDB' file to this folder.
	- Restart the app, to initialize the Database.


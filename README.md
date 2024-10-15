Project Overview:
The Local Tourism Guide application offers a comprehensive platform for users to discover nearby tourist spots, view weather forecasts, access hotel and event information, and contribute feedback. The application is designed to assist users in exploring their surroundings by leveraging geolocation and public APIs for real-time data on places, hotels, events, and weather. In addition, the app allows registered users to interact with the platform by adding notes and sharing information, making it a community-driven app.

Key Features:
Map View for Tourist Spots:
The app uses OSMDroid for map integration to show the user's current location and nearby tourist spots. Users can explore attractions around them based on real-time data.

Weather Forecasting:
The app integrates the WeatherAPI to provide current and 7-day weather forecasts based on the user’s location. It also shows detailed weather information, such as humidity, visibility, and the UV index.

Hotel and Event Listings:
Using the Foursquare API, the app fetches hotel names and live concert/event details near the user. This feature also updates in real-time, showing whether these establishments are open or closed.

RecyclerView Implementation:
RecyclerViews are used throughout the app to display lists of tourist spots, hotels, events, weather forecasts, and user feedback in a clean, scrollable format.

Facial Recognition Login (Planned):
For enhanced security, the app will use OpenCV to integrate facial recognition for login, matching user images with those stored during registration.

Technology Stack & Tools Used:
Android Studio & Kotlin: The main platform for development.
OSMDroid API: Used for map integration to show user location and tourist spots.
Retrofit & WeatherAPI: Implemented for making API calls to fetch weather data.
Foursquare API: Fetches data on nearby hotels and events.
SQLite Database: Used for storing user data and notes, ensuring offline access to certain features.
RecyclerView: Manages the display of multiple lists like weather data, hotel listings, and user notes.
OpenCV (Planned): For implementing facial recognition for secure login.


Idea Generation and Implementation:
Local Tourism Guide: The primary idea was to create an app that would help tourists and locals explore their surroundings by providing real-time information on tourist spots, weather conditions, and available accommodations.
Daily Activity Tracker: Another idea involved tracking users' daily activities and offering insights. Although I implemented features like weather updates and feedback management, this idea can be expanded into a separate feature for tracking daily tourism activities.
UI and User Experience:
Layout: The main layout consists of a map view at the top, weather tiles below it, and a list of tourist spots, hotels, and events at the bottom. Users can scroll through this layout easily, with weather updates and events constantly refreshing based on their location.
Interactive Features: User interactions are kept simple with click-based navigation for adding notes, searching places, and exploring hotels/events. The Admin panel also has a smooth interface for managing users and their data.
Future Aspects & Expansion:
Facial Recognition:
Implementing OpenCV for facial recognition login will provide a higher level of security for users.

Cloud Integration (AWS):
Moving some data storage and API handling to the cloud using AWS can further optimize performance and scalability. AWS features like Lambda or S3 could be used to manage image storage or background tasks.

Offline Mode:
Expanding offline capabilities could allow users to access previously fetched data even without an internet connection. This can be achieved by caching data in SQLite for later use.

Additional APIs for Expanded Features:
Additional APIs like Zomato (for restaurants) or Eventbrite (for events) could be integrated to provide a more comprehensive guide for tourists.

User Feedback and Ratings:
Enabling users to rate hotels, events, and tourist spots can make the app more interactive and community-driven, giving users a more personalized experience.

Daily Activity Tracker:
As part of future expansion, incorporating a feature that tracks users' daily tourism activities and provides recommendations or summaries based on their behavior could be a valuable addition.

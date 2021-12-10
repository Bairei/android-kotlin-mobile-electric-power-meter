# MobileElectricPowerMeter

A (very) simple Android application, written in Kotlin, used for keeping track of pre-paid power
collection readings, allowing you to create entries one-by-one, or mass-create them using a
specifically formatted data. It currently uses in-memory SQLite database, with Android Room ORM
implemented for simplified, and more "reactive" access.

## Example of data format

```
// Hour-and-second day-and-month measured-value-in-kWph
1920 2311 412,2
1924 2411 410,1
1905 2511 408,5
...
```

# Running the application

Currently the simplest way to run the application using the Android Studio IDE (`Shift + F10` while
browsing the project, or `Run` button on tool bar).

# TODOs

Currently, I'm planning to implement statistics page, allowing to display data in more user-friendly
way, allowing them to e.g. estimate average power consumption a bit easier.

If possible, I'd also like to work on app's layout a bit, however I never was any good in UX design,
so it might be a very-very far thinking plan.
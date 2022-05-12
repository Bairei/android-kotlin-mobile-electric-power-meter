# MobileElectricPowerMeter

A (very) simple Android application, written in Kotlin, used for keeping track of pre-paid power
collection readings, allowing you to create entries one-by-one, or mass-create them using a
specifically formatted data. It currently uses an in-memory SQLite database, with Android Room ORM
implemented for simplified, and more "reactive" access.

## Example of data format

### a) JSON

```json
[
  {
    "businessId": "57c7bbcb-ae54-4f45-a3e3-4f78f8505f96",
    "id": 1,
    "meterReading": 3142,
    "readingDate": "2022-03-28 19:00"
  },
  {
    "businessId": "fd7c2341-5852-4bdf-86f8-65caa449eb5d",
    "id": 2,
    "meterReading": 3122,
    "readingDate": "2022-03-29 19:00"
  },
  {
    "businessId": "be1154db-f1d0-4b48-bbcc-eb0a30507650",
    "id": 3,
    "meterReading": 3098,
    "readingDate": "2022-03-30 19:00"
  }
]
```

An example JSON structure for the collection of measurement entries - created using "export"
functionality, it's also possible to import it back to the application itself - think of it as a
backup.

### b) CSV

**NOTE**: The format displayed below is a legacy one, adopted really early, before the first version
of the application was created - it does not support loading precise dates (only month + day of the
month), so it's going to be replaced with the regular JSON format.

```csv
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
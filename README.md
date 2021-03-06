# Body Measurement Api

[![Build Status](https://travis-ci.org/JanRudert/body-measurement-api.svg?branch=master)]( https://travis-ci.org/JanRudert/body-measurement-api )

Provides a REST Api to store and read measurements of patients.

# Links

* [Live instance]( http://body-measurement-api.herokuapp.com/ )
* [Repository]( https://github.com/JanRudert/body-measurement-api )
* [Build Pipeline]( https://travis-ci.org/JanRudert/body-measurement-api/builds )


# API

## JSON-Objects 

### create

    POST       /measurements
    {   
        "timestamp": "2015-05-01T13:00:00+02:00", 
        "patientId": 4711, 
        "type": "BloodPressure", 
        "value": 5.5
    }

The `patientId` is the reference to the person whose metadata is stored in other parts of the system.
The `type` value can be chosen freely.
There is one `timestamp` format allowed currently.
The `value` property is a decimal value.

This is an example curl call:

    curl -X POST -H "Content-type: application/json" http://localhost:9000/measurements -d '{ "timestamp": "2015-05-01T13:00:00+02:00", "patientId": 4711, "type": "BloodPressure", "value": 5.5 }'

### lookup

As the client application is expected to display measurements for one user only the `patientId` is part of the path: 

    GET        /measurements/:patientId
    

Following filters can be applied as query parameters:
    
 * type - one single measurement type
 * from - time range start, if omitted no lower boundary
 * to - time range start, if omitted no upper boundary

For demo reasons the time range filter only supports the format `yyyyMMdd` currently. They are inclusive and assume UTC.

The response will contain a sequence of measurements:

      [
          {   
              "timestamp": "2015-05-01T13:00:00+02:00", 
              "patientId": 4711, 
              "type": "BloodPressure", 
              "value": 5.5
          },
          {   
              "timestamp": "2017-05-01T13:00:00+02:00", 
              "patientId": 4712, 
              "type": "Temperature", 
              "value": 36.8
          }
      ]
      
This is the curl example which retrieves all `Temperature` entries for the patient:

    curl http://localhost:9000/measurements/4711?type=Temperature 


# Prerequisites

* Git
* Open JDK 1.8 or Oracle JDK 1.8

# Development

## Cloning the repository

    $ git clone git@github.com:JanRudert/body-measurement-api.git
    $ cd body-measurement-api

## Start the application locally

    ./sbt run
    
This will download sbt and project dependencies on the first run. Afterwards the web application will be reachable at [http://localhost:9000](http://localhost:9000). 


## Test 

    ./sbt test


## Continuous Deployment

On every push on branch `master` the application will be built and tested on [travis](https://travis-ci.org/JanRudert/body-measurement-api/builds). If the build succeeds, it will be deployed to heroku and will be public available at [heroku](http://body-measurement-api.herokuapp.com/).
    
# Persistence

The application uses a classical SQL database. The expected amount of data and the fixed (row-based) structure of it make a 
relational database a good choice for the start. 

For demo reasons the API runs on H2 in-memory database only at the moment. Extending it to use Postgres should be a simple configuration change. 
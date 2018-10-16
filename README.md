# Social Media Site

## About 

This is a semester-long project for the CSE 3330, Database and File 
Systems, class at UTA. The goal of this project is to create a simple 
social media site from a set of requirements, letting us learn how to 
setup and interact with databases.

As usual, this is not meant to be used for anything serious.

## Dependencies

* **Gradle** is the build system
* **PostgreSQL** is the backing RDBMS
* **Spring MVC** is the web server running the site 
* **Pebble Templates** is a templating engine used for the pages
  * [Template examples](https://github.com/PebbleTemplates/pebble-example-spring/tree/master/spring-boot/src/main/resources/templates)
  * [Documentation](https://github.com/PebbleTemplates/pebble/wiki/documentation) 

## Setup 

1. Install PostgreSQL 10.5 on your machine.
    * See [here](https://www.postgresql.org/download/) for general instructions
    * Ubuntu: `$ sudo apt install postgresql postgresql-contrib`
2. Create a new PostgreSQL database called "SocialMedia"
    1. Log into the postgres user account `$ sudo -i -u postgres`
    2. Create the database `$ createdb SocialMedia`
    3. Log into the database `$ psql SocialMedia`
    4. You can exit the `SocialMedia=#` prompt at any time with `EOF`
       (i.e. `^D`/`ctrl+d` on unix systems)
3. Run `src/main/resources/db/up.sql` on the SocialMedia db
    1. `$ psql SocialMedia -f "$pathToThisProject/src/main/resources/db/up.sql"`
    2. This will create the db schema and add starting data to the db
    3. This can be undone with the corresponding `down.sql` file
4. Import the Gradle project with IntelliJ 
5. Add a new run configuration for running the application
    1. Create a new gradle configuration
    2. Name is something like "bootRun"
    3. Select the current project directory as the gradle project
    4. Type "bootRun" as the gradle task
    5. Save It
    6. Run it and go to `localhost:8080` in the web browser to see 
       the application 

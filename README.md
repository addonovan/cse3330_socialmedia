# Social Media Site

## About 

This is a semester-long project for the CSE 3330, Database and File 
Systems, class at UTA. The goal of this project is to create a simple 
social media site from a set of requirements, letting us learn how to 
setup and interact with databases.

This project uses PostgreSQL as the RDBMS and Spring MVC via 
springboot as the webserver.

As usual, this is not meant to be used for anything serious.

## Setup 

1. Install PostgreSQL 10.5 on your machine.
    * See [here](https://www.postgresql.org/download/)
2. Import the Gradle project with IntelliJ 
3. Add a new run configuration for running the application
    1. Create a new gradle configuration
    2. Name is something like "bootRun"
    3. Select the current project directory as the gradle project
    4. Type "bootRun" as the gradle task
    5. Save It
    6. Run it and go to `localhost:8080` in the web browser to see 
       the application 

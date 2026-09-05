# SportsDB-Manager-Java-MySQL
A Java-based application that programmatically sets up, populates, and queries two relational sports databases using JDBC and MySQL. Designed for coursework collaboration, it reads structured data from CSV files, performs SQL DDL and DML operations, and outputs query results with aggregation.

##  Features
- Auto-creates two databases
- Builds normalized schema with foreign key relationships
- Populates `Sports`, `Teams`, `Players`, `Motorsport`, `Team`, and `Driver` tables from CSV
- Performs advanced SELECT and DELETE queries with aggregation
- Uses prepared statements and batch inserts for performance

## Tech Stack
- Java (JDBC)
- MySQL
- CSV Parsing
- SQL: DDL, DML, Joins, Aggregation

## How to Run
1. Ensure MySQL is running locally on port 3306.
2. Add your `.csv` files to the root directory.
3. Compile and run the `code.java` file:
```bash
javac code.java
java code

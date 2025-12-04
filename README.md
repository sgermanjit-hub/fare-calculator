# LittlePay Fare Calculator
Fare Calculator is a spring boot service that processes customer tap on and tap off events from a csv file and generate the trips, it can be completed trip, incomplete trip or cancelled trip.

# Overview
- Customer Tap on in bus 37
- Customer Tap out from bus 37
- Compute fare based on the trip.
- Trip type can be completed, incomplete or cancelled.

# Assumptions
- Input CSV file is well formed and is not missing any data.
- PAN values are customer credit/debit card numbers
- Unknown stops that are not provided to configure in fare rule system, any trips belong to it should be ignored
- If we have Orphan off, with tap on, ignore the trip.
- Due to one missing data or unknow stop, rest of the file processing shouldn't be impacted.
- Invalid CSV rows should be ignored and rest of the processing should be continued.
- Fares are loaded in application via configuration file fares.yml
- Input/ Output paths are provided in API request.
- Processing of trip should be group by pan, company and bus id.

# Tech Stack
- Java 21
- Spring Boot 3
- Maven
- Lombok

## Running Application
```
 git clone git@github.com:sgermanjit-hub/fare-calculator.git
 
 cd fare-calculator
 
 mvn clean compile install
 
 mvn spring-boot:run
````
# Api
POST /trips/process
Request:
````
{
"inputFile": "path/to/taps.csv",
"outputFile": "path/to/trips.csv"
}
````

## Testing
- Integration Tests
    - Test Api using mock mvc
- Unit Tests
```    
   mvn test
```
# Example Input -> Output
Input
````
ID, DateTimeUTC, TapType, StopId, CompanyId, BusID, PAN
1, 22-01-2023 13:00:00, ON, Stop1, Company1, Bus37, 5500005555555559
2, 22-01-2023 13:05:00, OFF, Stop2, Company1, Bus37, 5500005555555559
3, 22-01-2023 09:20:00, ON, Stop3, Company1, Bus36, 4111111111111111
4, 23-01-2023 08:00:00, ON, Stop1, Company1, Bus37, 4111111111111111
5, 23-01-2023 08:02:00, OFF, Stop1, Company1, Bus37, 4111111111111111
6, 24-01-2023 16:30:00, OFF, Stop2, Company1, Bus37, 5500005555555559

````

Output
````
"Started","Finished","DurationSecs","FromStopId","ToStopId","ChargeAmount","CompanyId","BusId","PAN","Status"
"22-01-2023 13:00:00","22-01-2023 13:05:00","300","Stop1","Stop2","$3.25","Company1","Bus37","5500005555555559","COMPLETED"
"23-01-2023 08:00:00","23-01-2023 08:02:00","120","Stop1","Stop1","$0","Company1","Bus37","4111111111111111","CANCELLED"
"22-01-2023 09:20:00","","null","Stop3","","$7.3","Company1","Bus36","4111111111111111","INCOMPLETE"

````
## Api Documentation
```bash
Swagger UI
👉 http://localhost:8080/swagger-ui/index.html




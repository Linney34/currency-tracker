# 💱 Currency Tracker

**Currency Tracker** is a Spring Boot application for fetching and analyzing currency exchange rates against the **Polish Zloty (PLN)**.  
It provides fast and reliable access to:

- ✅ Latest currency rates
- 📊 Historical rates for a specific period
- 🧮 Average rates over a period
- 📈 Trend analysis (up, down, stable)

---

## 📚 Table of Contents

- [Technologies](#technologies)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Testing](#testing)
- [Notes](#notes)
- [License](#license)

---

## 🛠 Technologies

- **Java 21**
- **Spring Boot 3.4** (Web, Data JPA, Cache)
- **Hibernate ORM**
- **PostgreSQL**
- **JUnit 5 + Mockito** for testing
- **RestTemplate** for HTTP calls

---

## 🔗 API Endpoints

The main controller exposes endpoints to:
- **Get the latest currency rate** 

    postman http: http://localhost:8080/api/rates/latest?currency=USD     

- **Get historical rates** for a range of dates
    
    postman http: http://localhost:8080/api/rates/history?currency=USD&from=2025-01-01&to=2025-09-29 

- **Get the average rate** for a number of past days
    
    postman http: http://localhost:8080/api/rates/avg?currency=USD&days=7

- **Get the trend** for a number of past days
    
    postman http: http://localhost:8080/api/rates/trend?currency=USD&days=5

### Example DTO returned by the API:

json
{
"currency": "USD",
"rate": 5.123,
"timestamp": "2025-12-09T16:00:00"
}

---

## 🚀 Getting Started
### 1. Clone the repository
git clone https://github.com/Linney34/currency-tracker

## Build the project
   ./mvnw clean install

## Configure PostgreSQL
Update application-dev.properties with your PostgreSQL credentials:
spring.datasource.url=jdbc:postgresql://localhost:5432/currency_db
spring.datasource.username=postgres
spring.datasource.password=Tt1231232811
spring.jpa.hibernate.ddl-auto=update

## Run the app
./mvnw spring-boot:run


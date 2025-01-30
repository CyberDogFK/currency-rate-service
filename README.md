## Currency Rate Service

A service with an endpoint that retrieves currency rates from a given API (find a
mock with details here: https://github.com/illenko/currencies-mocks), combines it, and
returns in a specified format.

## Description

Request data from given service, but, if service is unavailable
returns latest historic data saved in database.

## Supported currencies

| Currencies Types |    Example    |
|:----------------:|:-------------:|
|       Fiat       | USD, EUR, GBP |
|      Crypto      | BTC, ETH, LTC |

## Endpoints

- :chart_with_upwards_trend: Retrieves latest currency's rate
 
|     Endpoint      | Method |                                  Description                                   |
|:-----------------:|:------:|:------------------------------------------------------------------------------:|
| `/currency-rates` |  GET   | Request newest data from currencies-mock service, or fetch oldest data from db |

## How to start

### Start Currencies Mock

Follow the link above and start application. Default port of that application is 8080, so
this app will work on port 8085

### Start application
- Using Docker

1. Download repo to you computer: </br>
`git clone git@github.com:CyberDogFK/currency-rate-service.git`

[//]: # (2. Application uses environment variables to store properties for application)

[//]: # (   So, please, create `.evn-local` file in root directory, following by example from `.env-example` and add DB properties there </br>)

[//]: # (   Then run: </br>`export $&#40;cat .env-local | xargs&#41;`</br> to add properties to environment)
3. Use
````
./mvnw clean package
````
4. Use, and wait booting
````
docker-compose up
````
5. Application will be available on port 6868, send request: `curl localhost:6868/currency-rates`

- Without Docker

1. Download repo to you computer with git:
`git clone git@github.com:CyberDogFK/currency-rate-service.git`
2. Create database in PostgreSQL, recommend name `currency_rate`
3. Application uses environment variables to store properties for application
So, please, create `.evn-local` file in root directory, following by example from `.env-example` and add DB properties there </br>
Then run: </br>`export $(cat .env-local | xargs)`</br> to add properties to environment. </br> You can skip test variables.
4. Use in command line, from directory.
```
./mvnw clean package
./mvnw spring-boot:run
```
5. Application by default uses 8085 port, send request: `curl localhost:8085/currency-rates`

## Test application

1. Create database in PostgreSQL, recommend name `currency_rate_test`
2. Application uses environment variables to store properties for application
   So, please, create `.evn-local` file in root directory, following by example from `.env-example` and add DB properties there </br>
   Then run: </br>`export $(cat .env-local | xargs)`</br> to add properties to environment. </br> You can skip prod variables
3. Start docker, which will be used for repository tests
4. Run command
```
./mvnw -Dmaven.test.skip=false clean package
```

## Technologies

- JDK 21
- Kotlin 2.10
- Spring Boot
- Spring WebFlux
- PostgresSQL
- R2DBC
- Liquibase
- Jackson
- Netty

## Project Structure

| Tier       | Description                                                         |
|------------|---------------------------------------------------------------------|
| Repository | Tier for work only with database, fetching and saving data          |
| Service    | All logic of computing and communication with application data here |
| Controller | User can use this application by this tier                          |

## Future possible features

- Getting historic data for a specific currency

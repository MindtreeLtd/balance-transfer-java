This is an example project (Balance transfer application) where Hyperledger Java SDK is used to create a basic hyperledger 1.0 application

Made by using Fabric sdk java Project in hyperledger Fabric
  https://github.com/hyperledger/fabric-sdk-java/
  What have we used:
   Sdk Integration classes, 
   Sample chaincode, 
   End to end test cases for reference.
   
   This application has been tested on Ubuntu 16.04 .
## Sample Application

A sample Java Springboot app to demonstrate **__fabric-client__** & **__fabric-ca-client__** Java SDK APIs

### Prerequisites and setup:

* [Docker](https://www.docker.com/products/overview) - v1.12 or higher
* Docker-compose
* Java 8
* Maven


#### Go inside the artifacts directory of this project and pull the required docker images:
```
cd balance-transfer-java/artifacts
docker-compose -f docker-compose.yaml pull
```


#### Artifacts
* Crypto material has been generated using the **cryptogen** tool from Hyperledger Fabric and mounted to all peers, the orderering node and CA containers. More details regarding the cryptogen tool are available [here](http://hyperledger-fabric.readthedocs.io/en/latest/build_network.html#crypto-generator).
* An Orderer genesis block (genesis.block) and channel configuration transaction (mychannel.tx) has been pre generated using the **configtxgen** tool from Hyperledger Fabric and placed within the artifacts folder. More details regarding the configtxgen tool are available [here](http://hyperledger-fabric.readthedocs.io/en/latest/build_network.html#configuration-transaction-generator).

## Running the sample program


##### Terminal Window 1

Go inside the artifacts directory in the project.

```
cd balance-transfer-java/artifacts
```

* Launch the network using docker-compose

```
docker-compose -f docker-compose.yaml up -d
```

Once you have completed the above setup, you will have provisioned a local network with the following docker container configuration:

* 1 CA
* A SOLO orderer
* 4 peers (2 peers in each organization)

* Run the spring boot application (By default it runs on port 8080) 

come back to the balance-transfer-java home

```
cd ..
```

Install all the jars and start the application

```
mvn clean install
mvn spring-boot:run
```

##### Terminal Window 2

* Execute the REST APIs 

* You can directly send the api requests through swagger which is integrated with this spring boot application
  Access the link http://localhost:8080/swagger-ui.html after running the application.



## Sample REST APIs Requests

### Login Request

* Register and enroll new users 

```
curl -X POST --header 'Content-Type: application/json' --header 'Accept: text/plain' -d '{
  "username": "Swati Raj"
}' 'http://localhost:8080/enroll'
```

**OUTPUT:**


User Swati Raj Enrolled Successfuly  jwt:eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTd2F0aSIsInJvbGVzIjoidXNlciIsImlhdCI6MTUxNTczNzU2MiwiZXhwIjoxNTE1NzM3NjgyfQ.kKp7G60TInAl68pmgadfEayU35x_nLadIoR72n9MTpU

#### Please note down the JWT returned and put it after the Bearer token in all requests

#### The JWT will expire in 15 minutes, if you want to change the time period , change it in the enroll function in the ChaincodeController class. 


### Create Channel request

Please note that the Header **authorization** must contain the JWT returned from the `POST /enroll` call

```
curl -X POST --header 'Content-Type: application/json' --header 'Accept: */*' --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTd2F0aSIsInJvbGVzIjoidXNlciIsImlhdCI6MTUxNTczNzU2MiwiZXhwIjoxNTE1NzM3NjgyfQ.kKp7G60TInAl68pmgadfEayU35x_nLadIoR72n9MTpU' 'http://localhost:8080/api/construct'
```

**OUTPUT:**
channel created successfully




### Recreate Channel request

```
curl -X PUT --header 'Content-Type: application/json' --header 'Accept: */*' --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTd2F0aSIsInJvbGVzIjoidXNlciIsImlhdCI6MTUxNTczNzU2MiwiZXhwIjoxNTE1NzM3NjgyfQ.kKp7G60TInAl68pmgadfEayU35x_nLadIoR72n9MTpU' 'http://localhost:8080/api/reconstruct'
```
**OUTPUT:**
channel recreated successfully


please note that if you create a channel once , you cannot create the channel with same name  again, you can recreate it if you want to use it anywhere.


### Install chaincode

```
curl -X POST --header 'Content-Type: application/json' --header 'Accept: */*' --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTd2F0aSIsInJvbGVzIjoidXNlciIsImlhdCI6MTUxNTczNzU2MiwiZXhwIjoxNTE1NzM3NjgyfQ.kKp7G60TInAl68pmgadfEayU35x_nLadIoR72n9MTpU' -d '{
  "chaincodeName": "myChaincode"
}' 'http://localhost:8080/api/install'
```
**OUTPUT:**
Chaincode installed successfully


### Instantiate chaincode

Please be patient, This request may take some time.

```
curl -X POST --header 'Content-Type: application/json' --header 'Accept: */*' --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTd2F0aSIsInJvbGVzIjoidXNlciIsImlhdCI6MTUxNTczNzU2MiwiZXhwIjoxNTE1NzM3NjgyfQ.kKp7G60TInAl68pmgadfEayU35x_nLadIoR72n9MTpU' -d '{
  "args": [
    "a", "500", "b", "200"
  ],
  "chaincodeName": "myChaincode",
  "function": "init"
}' 'http://localhost:8080/api/instantiate'
```
**OUTPUT:**
Chaincode instantiated Successfully

### Invoke request

```
curl -X POST --header 'Content-Type: application/json' --header 'Accept: */*' --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTd2F0aSIsInJvbGVzIjoidXNlciIsImlhdCI6MTUxNTczNzU2MiwiZXhwIjoxNTE1NzM3NjgyfQ.kKp7G60TInAl68pmgadfEayU35x_nLadIoR72n9MTpU' -d '{
  "args": [
    "move", "a", "b", "100"
  ],
  "chaincodeName": "myChaincode",
  "function": "invoke"
}' 'http://localhost:8080/api/invoke'
```
**OUTPUT:**
Transaction invoked successfully

### Chaincode Query

```
curl -X GET --header 'Accept: */*' --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTd2F0aSIsInJvbGVzIjoidXNlciIsImlhdCI6MTUxNTczNzU2MiwiZXhwIjoxNTE1NzM3NjgyfQ.kKp7G60TInAl68pmgadfEayU35x_nLadIoR72n9MTpU' 'http://localhost:8080/api/query?ChaincodeName=myChaincode&function=invoke&args=query%2Cb'
```
**OUTPUT:**
300


### All the properties are stored in config.properties and hyperledger.properties file in  src/main/resources package, if you want to change any file location or other properties, change it from there.
   If you want to change any network related settings, you can change them from config.properties file.


### Please note that for developing JWT Bearer token we are using 'username' provided in the Enroll User call for demo purpose, which is not secure a secure approach , if you want to this in your application , you have to make it more secure using password or some other mechanism according to your logic.

## License

This Project's source code files are made available under the Apache License, Version 2.0 (Apache-2.0), located in the license file under this repo.


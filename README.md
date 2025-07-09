# samarone-wallet

This is a REST API project for a digital wallet, developed with Java and Helidon. It allows creating wallets, checking balances (current and historical), depositing, withdrawing, and transferring funds between users.

## Build and run


With JDK21
```bash
mvn package
java -jar target/samarone-wallet.jar
```

## Exercise the application

### Create Wallets
First, let's create two wallets for user `1` and user `2`.

**Create a wallet for user 1:**
```bash
curl -X POST http://localhost:8080/wallets/1
```

**Create a wallet for user 2:**
```bash
curl -X POST http://localhost:8080/wallets/2
```

### Deposit Funds
Now, let's deposit `100` into user `1`'s wallet.

```bash
curl -X POST -H "Content-Type: application/json" -d "100" http://localhost:8080/wallets/1/deposit
```

### Retrieve Balance
Check the balance for user `1`. It should be `100`.

```bash
curl -X GET http://localhost:8080/wallets/1/balance
```

### Withdraw Funds
Withdraw `30` from user `1`'s wallet.

```bash
curl -X POST -H "Content-Type: application/json" -d "30" http://localhost:8080/wallets/1/withdraw
```
After this, the balance for user `1` will be `70`. You can verify by calling the balance endpoint again.

### Transfer Funds
Transfer `20` from user `1` to user `2`.

```bash
curl -X POST -H "Content-Type: application/json" -d "20" "http://localhost:8080/wallets/transfer?fromUserId=1&toUserId=2"
```
After the transfer:
*   User `1`'s balance will be `50`.
*   User `2`'s balance will be `20`.

You can check their balances:
```bash
curl -X GET http://localhost:8080/wallets/1/balance
curl -X GET http://localhost:8080/wallets/2/balance
```

### Retrieve Historical Balance
To get the balance at a specific point in time, you need to provide a timestamp in ISO-8601 format. For example, to see user `1`'s balance before any transactions, you could use a timestamp from before you started.

```bash
# Replace {timestamp} with a valid ISO-8601 timestamp, e.g., 2025-07-09T12:50:00
curl -X GET "http://localhost:8080/wallets/1/balance/historical?timestamp={timestamp}"
```



## Try health

```
curl -s -X GET http://localhost:8080/health
{"outcome":"UP",...

```


## Building a Native Image

The generation of native binaries requires an installation of GraalVM 22.1.0+.

You can build a native binary using Maven as follows:

```
mvn -Pnative-image install -DskipTests
```

The generation of the executable binary may take a few minutes to complete depending on
your hardware and operating system. When completed, the executable file will be available
under the `target` directory and be named after the artifact ID you have chosen during the
project generation phase.



## Try metrics

```
# Prometheus Format
curl -s -X GET http://localhost:8080/metrics
# TYPE base:gc_g1_young_generation_count gauge
. . .

# JSON Format
curl -H 'Accept: application/json' -X GET http://localhost:8080/metrics
{"base":...
. . .
```



## Building the Docker Image

```
docker build -t samarone-wallet .
```

## Running the Docker Image

```
docker run --rm -p 8080:8080 samarone-wallet:latest
```

Exercise the application as described above.
                                

## Run the application in Kubernetes

If you don’t have access to a Kubernetes cluster, you can [install one](https://helidon.io/docs/latest/#/about/kubernetes) on your desktop.

### Verify connectivity to cluster

```
kubectl cluster-info                        # Verify which cluster
kubectl get pods                            # Verify connectivity to cluster
```

### Deploy the application to Kubernetes

```
kubectl create -f app.yaml                              # Deploy application
kubectl get pods                                        # Wait for quickstart pod to be RUNNING
kubectl get service  samarone-wallet                     # Get service info
kubectl port-forward service/samarone-wallet 8081:8080   # Forward service port to 8081
```

You can now exercise the application as you did before but use the port number 8081.

After you’re done, cleanup.

```
kubectl delete -f app.yaml
```


## Building a Custom Runtime Image

Build the custom runtime image using the jlink image profile:

```
mvn package -Pjlink-image
```

This uses the helidon-maven-plugin to perform the custom image generation.
After the build completes it will report some statistics about the build including the reduction in image size.

The target/samarone-wallet-jri directory is a self contained custom image of your application. It contains your application,
its runtime dependencies and the JDK modules it depends on. You can start your application using the provide start script:

```
./target/samarone-wallet-jri/bin/start
```

Class Data Sharing (CDS) Archive
Also included in the custom image is a Class Data Sharing (CDS) archive that improves your application’s startup
performance and in-memory footprint. You can learn more about Class Data Sharing in the JDK documentation.

The CDS archive increases your image size to get these performance optimizations. It can be of significant size (tens of MB).
The size of the CDS archive is reported at the end of the build output.

If you’d rather have a smaller image size (with a slightly increased startup time) you can skip the creation of the CDS
archive by executing your build like this:

```
mvn package -Pjlink-image -Djlink.image.addClassDataSharingArchive=false
```

For more information on available configuration options see the helidon-maven-plugin documentation.

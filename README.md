# Authors 
  Joaquim Tiago (up201908075)
  Luís Leite (up201906750)

# Distributed Systems
Implementation of 3 different types of distributed systems:
-   Token Ring
-   Gossip-based data dissemination
-   Totally Ordered Multicast

Details are available at [Practical Assignment](/practical_assignment_PT.pdf) (Although only in Portuguese).

# Table of Contents
- [Authors](#authors)
- [Distributed Systems](#distributed-systems)
- [Table of Contents](#table-of-contents)
- [TokenRing](#tokenring)
  - [Starting The Service](#starting-the-service)
    - [Example of the service start](#example-of-the-service-start)
  - [Commands](#commands)
- [Gossiping](#gossiping)
  - [Starting The Service](#starting-the-service-1)
    - [Normal Start](#normal-start)
    - [Example of the service start](#example-of-the-service-start-1)
    - [Starting with some users already registered](#starting-with-some-users-already-registered)
  - [Commands](#commands-1)
- [Multicast](#multicast)
  - [Starting The Service](#starting-the-service-2)
    - [Example of the service start](#example-of-the-service-start-2)
  - [Commands](#commands-2)


# TokenRing

## Starting The Service

Assuming you're in the root directory of the project, you can start the service with the following command:
```
./mvnw exec:java -D"exec.mainClass"="ds.assignment.tokenring.TokenService" -D"exec.args"="{host_ipv4} {next_host_ipv4}"
```
<b>host_ipv4</b>: The ipv4 where you want to expose your service. <br>
<b>next_host_ipv4</b>: The ipv4 of the next host in the ring.

### Example of the service start
``` 
./mvnw exec:java -D"exec.mainClass"="ds.assignment.tokenring.TokenService" -D"exec.args"="127.0.0.1 127.0.0.2" 
```

## Commands

The TokenRing Service accepts the following commands:
<ul>
<li><b>lock</b>: locks the token (as soon as it's received).
<li><b>unlock</b>: unlocks the token (as soon as it's received).
<li><b>startToken</b>: Creates a token.
</ul>

# Gossiping

## Starting The Service
Assuming you're in the root directory of the project, you can start the service with the following command:

### Normal Start
```
./mvnw exec:java -Dexec.mainClass="ds.assignment.gossiping.GossipService" -Dexec.args="{host_ipv4} {request_generator_bool}"
```
<b>host_ipv4</b>: The ipv4 where you want to expose your service. <br>
<b>request_generator_bool</b>: "true" if you want to generate random requests with a poisson distribution, "false" otherwise.

### Example of the service start
```
./mvnw exec:java -Dexec.mainClass="ds.assignment.gossiping.GossipService" -Dexec.args="127.0.0.6 true"
```

### Starting with some users already registered
It is also possible to start with a predefined set of registered hosts.
```
./mvnw exec:java -Dexec.mainClass="ds.assignment.gossiping.GossipService" -Dexec.args="{host_ipv4} {request_generator_bool} {registered_hosts_list}"
```
<b>host_ipv4</b>: The ipv4 where you want to expose your service. <br>
<b>request_generator_bool</b>: "true" if you want to generate random requests with a poisson distribution, "false" otherwise. <br>
<b>registered_hosts_list</b>: A list of all the hosts you want to register (separated by spaces), e.g: "192.168.0.166" "192.168.0.167" "192.168.0.168".



## Commands

The Gossiping Service accepts the following commands:
<ul>
<li><b>register {ipv4}/unregister {ipv4}</b>: Adds/removes the host.
<li><b>send {ipv4} {msg}</b>: Sends a msg to a certain host (Used for testing
purposes).
<li><b>get all</b>: Equivalent to executing all get commands minus <b>get
channels</b>.
<li><b>get words</b>: List of the received words.
<li><b>get hosts</b>: List of all registered hosts.
<li><b>get banned</b>: List of words that are not propagated.
<li><b>get gossipRate</b>: nº of randomly chosen hosts a message is
propagated to.
<li><b>get dropChance</b>: chance of a word retransmission being ignored.
<li><b>get channels</b>: print all io.grpc.ManagedChannel stored.
<li><b>set gossipRate {int}</b>: changes gossipRate param.
<li><b>set dropChance {int}</b>: changes dropChance param.
<li><b>set poisson start</b>: starts the poisson thread.
<li><b>set poisson on</b>: resumes the poisson thread.
<li><b>set poisson off</b>: pauses the poisson thread.
</ul>

# Multicast

## Starting The Service

Assuming you're in the root directory of the project, you can start the service with the following command:
```
./mvnw exec:java -D"exec.mainClass"="ds.assignment.multicast.MulticastService" -D"exec.args"="{host_ipv4} {request_generator_bool} {PID} {other_hosts_lists}"
```
<b>host_ipv4</b>: The ipv4 where you want to expose your service. <br>
<b>request_generator_bool</b>: "true" if you want to generate random requests with a poisson distribution, "false" otherwise.<br>
<b>PID</b>: An integer serving as a unique identifier of your server, starts at 0 and increments 1 each time you add a server. All PIDs together must form the interval [0, 1, 2, .., {highest PID}]. <br>
<b>other_hosts_list</b>: A list of all the other hosts (separated by spaces), e.g: "192.168.0.166" "192.168.0.167" "192.168.0.168".<br>
<br>
<b>Note<b>: Do not pass your own host in the {registered_hosts_list}.<br>

### Example of the service start
```
./mvnw exec:java -D"exec.mainClass"="ds.assignment.multicast.MulticastService" -D"exec.args"="127.0.0.3 false 2 127.0.0.1 127.0.0.2 127.0.0.4 127.0.0.5 127.0.0.6"
```


## Commands

The Multicast Service accepts the following commands:
<ul>
<li><b>"send"</b>: Generates a random request.
<li><b>"queue"</b>: Prints the delay queue (queue that contains all the requests that have not yet been delivered/discarded).
<li><b>"delivered"</b>: Prints all delivered requests.
<li><b>"startPoisson"</b>: Starts the poisson request generator (Assuming it was not started by default).
<li><b>"pausePoisson"</b>: Pauses the poisson request generator.
<li><b>"resumePoisson"</b>: Resumes the poisson request generator.
</ul>


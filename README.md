# SpringAMQP ConnectionListener Issue example

Sample project to show an issue on SpringAMQP ConnectionListener

### Problem

The problem is happening when using CachingConnectionFactory with cache 
mode CacheMode.CHANNEL.
Too see the problem you need to run this app and stop the rabbitmq server,
then the method SimpleConnectionListener::onClose is called printing the 
'Connection CLOSED' message, but if you start and stop again the method 
isn't called.

### Running

To run the app just use the following command filling the parameters:

    ./mvnw spring-boot:run -Dspring.rabbitmq.host= -Dspring.rabbitmq.username= -Dspring.rabbitmq.password=


### Solution

The class ChannelCachingConnectionProxy has a parameter closeNotified 
that when connection is closed for the first time it is changed to true, 
but when the connection is recreated this value still true and for new 
close events the listener is not triggered.
So I added a line on createConnection() method of the class and the 
problem was solved, see the snippet below.

    // spring-amqp/spring-rabbit/src/main/java/org/springframework/amqp/rabbit/connection/CachingConnectionFactory.java
    
    ...
    
    @Override
    public final Connection createConnection() throws AmqpException {
        Assert.state(!this.stopped, "The ApplicationContext is closed and the ConnectionFactory can no longer create connections.");
        synchronized (this.connectionMonitor) {
            if (this.cacheMode == CacheMode.CHANNEL) {
                if (this.connection.target == null) {
                    this.connection.target = super.createBareConnection();
                    // invoke the listener *after* this.connection is assigned
                    if (!this.checkoutPermits.containsKey(this.connection)) {
                        this.checkoutPermits.put(this.connection, new Semaphore(this.channelCacheSize));
                    }
                    connection.closeNotified.set(false); // This line was added
                    getConnectionListener().onCreate(this.connection);
                }
                return this.connection;

    ...

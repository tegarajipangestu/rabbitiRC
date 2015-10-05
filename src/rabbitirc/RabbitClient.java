/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbitirc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tegar
 */
public class RabbitClient {

    private Channel channel;
    private ConnectionFactory factory;
    private Connection connection;
    private final static String QUEUE_NAME = "hello";
    private Consumer consumer;

    public RabbitClient() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);

    }

    public void Send() throws IOException, TimeoutException {
        String message = "Hello World!";
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
    }

    public static void main(String[] argv) throws IOException, TimeoutException {
        RabbitClient rabbitClient = new RabbitClient();
        User u = new User();
        Scanner sc = new Scanner(System.in);
        
        String command = sc.nextLine();
        while (!command.equals("/EXIT")) {
            if (command.length() >= 5 && command.substring(0, 5).equals("/NICK")) {
                if (command.length() == 5) { //default username
                    //Implementation here
                } else if (command.charAt(5) == ' ' && command.length() >= 7) {
                    //Implementation here
                }
            } else if (command.length() >= 5 && command.substring(0, 5).equals("/JOIN") && !u.isEmpty()) {
                if (command.length() == 5) { //default username
                    //Implementation here
                } else if (command.charAt(5) == ' ' && command.length() >= 7) {
                    //Implementation here
                } else {
                    System.out.println("Wrong format");
                }
            } else if (command.length() >= 6 && command.substring(0, 6).equals("/LEAVE") && !u.isEmpty()) {
                if (command.charAt(6) == ' ' && command.length() >= 8) {
                    //Implementation here
                }
            } else if (command.length() >= 4 && command.charAt(0) == ('@') && !u.isEmpty()) {
                    //Implementation here
            } else if (!u.isEmpty()) {
                    //Implementation here
            }

            command = sc.nextLine();
        }
        rabbitClient.Send();
    }

}

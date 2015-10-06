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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author tegar
 */
public class RabbitClient {

    private static Channel channel;
    private final ConnectionFactory factory;
    private static Connection connection;
    private final static String QUEUE_NAME = "hello";
    private final static String NOTIFICATIONS_EX_NAME = "log";
    private final Consumer consumer;
    private static User user;
    private static final List<String> defaultUsernames = new ArrayList<>(
            Arrays.asList("Kucing", "Sapi", "Rusa", "Kambing", "Platipus", "Kucing", "Naga", "Panda")
    );

    public RabbitClient() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
        user = new User();

//        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.exchangeDeclare(NOTIFICATIONS_EX_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, NOTIFICATIONS_EX_NAME, "");

        consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(message);

            }
        };
        channel.basicConsume(queueName, true, consumer);
    }

    public void Send() throws IOException, TimeoutException {
        String message = "Hello World!";
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
    }

    public void pushNotifications(String message, String header) throws IOException {
        String messageToSend = header + " Notifications : " + message;
        channel.basicPublish(NOTIFICATIONS_EX_NAME, "", null, messageToSend.getBytes());
    }

    public void broadcastMessage(String _message) throws IOException {
        String message = "[BROADCAST] "+_message;
        channel.basicPublish(NOTIFICATIONS_EX_NAME, "", null, message.getBytes());
    }

    public void pushWarning(String message, String header) throws IOException {
        String messageToSend = header + " Notifications : " + message;
        channel.basicPublish(NOTIFICATIONS_EX_NAME, "", null, messageToSend.getBytes());
    }

    public static void main(String[] argv) throws IOException, TimeoutException {
        RabbitClient rabbitClient = new RabbitClient();
        Scanner sc = new Scanner(System.in);

        String command = sc.nextLine();
        while (!command.equals("/EXIT")) {
            if (command.length() >= 5 && command.substring(0, 5).equals("/NICK")) {
                String name = "";
                if (command.length() <= 6) { //default username
                    int rndIdx = new Random().nextInt((defaultUsernames.size() - 0));
                    name = defaultUsernames.get(rndIdx);
                } else if (command.charAt(5) == ' ' && command.length() >= 7) {
                    name = command.substring(6);
                    name = name.trim(); //remove trailing whitespace
                }
                String message = name + " has joined";
                user.setName(name);
                rabbitClient.pushNotifications(message, "[NICK]");

            } else if (command.length() >= 5 && command.substring(0, 5).equals("/JOIN")) {
                String channelName = "";
                if (command.length() <= 6) { //default username
                    channelName = "channelname";

                } else {
                    channelName = command.substring(command.indexOf(" "));
                }
                String message = user.getName() + " has joined channel " + channelName;
                rabbitClient.pushNotifications(message, "[JOIN]");

            } else if (command.length() >= 6 && command.substring(0, 6).equals("/LEAVE")) {
                if (command.charAt(6) == ' ' && command.length() >= 8) {
                    String channelName = command.substring(command.indexOf(" "));
                    String message = user.getName() + " left channel " + channelName;
                    rabbitClient.pushNotifications(message, "[LEAVE]");
                }
            } else if (command.length() >= 4 && command.charAt(0) == ('@')) {
                //Implementation here
            } else {
                //Implementation here
                System.out.println("sini");
                rabbitClient.broadcastMessage(command);
            }

            command = sc.nextLine();
        }
        channel.close();
        connection.close();
    }

}

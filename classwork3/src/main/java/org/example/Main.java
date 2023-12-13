package org.example;

public class Main {
    public static void main(String[] args) {
        MiniApplicationContext context = new MiniApplicationContext("beans.xml");
        UserService userService = (UserService) context.getBean("userService");
        userService.displayMessage(); // This will display the message set in the XML configuration.
    }
}

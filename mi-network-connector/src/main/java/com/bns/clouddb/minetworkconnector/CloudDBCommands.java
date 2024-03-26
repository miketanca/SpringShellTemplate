package com.bns.clouddb.minetworkconnector;

import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.net.InetAddress;

@ShellComponent
public class CloudDBCommands {
    @ShellMethod
    public void connect(String host, @ShellOption(defaultValue = "10") int count, @ShellOption(defaultValue = "5") int timeout) {

        System.out.println("Connecting to host " + host + ".");
        try {
            var total = 0l;
            for (var i = 0; i < Math.max(1, count); i++) {
                total += getLatency(host, timeout);
            }
            System.out.println("Host " + host + " is reachable in " + total / count + " milliseconds.");
        } catch (Exception e) {
            System.out.println("Host " + host + " is not reachable.");
        }
    }

    @SneakyThrows
    private long getLatency(String host, int timeout) {
        val inetAddress = InetAddress.getByName(host);
        val startTime = System.currentTimeMillis();
        if (!inetAddress.isReachable(timeout * 1000)) {
            throw new RuntimeException("Host not reachable");
        }

        return System.currentTimeMillis() - startTime;
    }
}
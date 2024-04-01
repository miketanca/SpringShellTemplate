package com.bns.clouddb.minetworkconnector;

import com.bns.clouddb.minetworkconnector.model.ErrorResult;
import com.bns.clouddb.minetworkconnector.model.SuccessResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;

@ShellComponent
public class CloudDBCommands {
    @Value("${fqdn}")
    private String fqdn;

    private ObjectMapper objectMapper = new ObjectMapper();

    @ShellMethod
    @SneakyThrows
    public void connect(
            @ShellOption(defaultValue = "") String fqdn,
            @ShellOption(defaultValue = "1433") int port,
            @ShellOption(defaultValue = "10") int count,
            @ShellOption(defaultValue = "5") int timeout) {

        val host = StringUtils.isBlank(fqdn) ? this.fqdn : fqdn;

        InetAddress addr;
        try {
            addr = InetAddress.getByName(host);
        } catch (Exception ex) {
            val s = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new ErrorResult(
                    "SQL MI FQDN name " +
                            host +
                            " is not resolvable from the application. Check this link " +
                            "<URL>" +
                            " for troubleshooting"
            ));
            System.out.println(s);
            return;
        }

        System.out.println("Connecting to " + host + ":" + port);
        try {
            var total = 0l;
            for (var i = 0; i < Math.max(1, count); i++) {
                total += getLatency(addr, port, timeout);
            }
            val result = new SuccessResult(
                    String.format("%dms", total / count), host, LocalDateTime.now().toString()
            );
            val s = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            System.out.println(s);
        } catch (Exception e) {
            val s = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new ErrorResult(
                    "SQL MI " +
                            host +
                            " IP address is not accessible for the application via TCP:" +
                            port +
                            ". Check this " +
                            "<URL>" +
                            " for troubleshooting"
            ));
            System.out.println(s);
        }
    }

    @SneakyThrows
    private long getLatency(InetAddress addr, int port, int timeout) {
        val startTime = System.currentTimeMillis();

        val socketAddr = new InetSocketAddress(addr, port);

        try (val socket = new Socket()) {
            socket.connect(socketAddr, timeout * 1000);
        }

        return System.currentTimeMillis() - startTime;
    }
}

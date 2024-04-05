package com.bns.clouddb.minetworkconnector;

import com.bns.clouddb.minetworkconnector.model.ErrorResult;
import com.bns.clouddb.minetworkconnector.model.SuccessResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Properties;

@ShellComponent
public class CloudDBCommands {
    private ObjectMapper objectMapper = new ObjectMapper();

    @ShellMethod("Test connectivity to a SQL server managed instance and get latency.")
    @SneakyThrows
    public void minc(
            @ShellOption(defaultValue = "", help = "Fully qualified domain name of the SQL server managed instance")
            String fqdn,
            @ShellOption(defaultValue = "1433", help = "The TCP port of the SQL server managed instance, between 1 and 65535")
            @Min(value = 1, message = "Port number should be greater or equal to 1")
            @Max(value = 65535, message = "Port number should be less or equal to 65535")
            int port,
            @ShellOption(defaultValue = "10", help = "The count of connections to calculate average latency, between 1 and 20")
            @Min(value = 1, message = "Count of connections should be greater or equal to 1")
            @Max(value = 20, message = "Count of connections should be less or equal to 20")
            int count,
            @ShellOption(defaultValue = "5", help = "Timeout for connection to the SQL server managed instance, between 1 and 10 seconds")
            @Min(value = 1, message = "Timeout should be greater or equal to 1 second")
            @Max(value = 10, message = "Timeout should be less or equal to 10 seconds")
            int timeout) {

//        val path = Paths.get(".").toAbsolutePath().normalize().toString();
        val props = new Properties();
        val file = new File("minc.properties");
        String hostProp = null;
        if (file.exists()) {
            props.load(new FileInputStream(file));
            hostProp = props.getProperty("sqlmi.default.fqdn");
        }

        val host = StringUtils.isBlank(fqdn) ? hostProp : fqdn;

        if (StringUtils.isBlank(host)) {
            System.out.println("Fqdn not found in \"minc.properties\" and not provided by command options");
            return;
        }

        System.out.println("Connecting to " + host + ":" + port);

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

        try {
            long total = 0l;
            for (int i = 0; i < Math.max(1, count); i++) {
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

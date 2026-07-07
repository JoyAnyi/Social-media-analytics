package com.company.socialanalytics.system;

import com.company.socialanalytics.dashboard.SystemHealthView;
import com.company.socialanalytics.kafka.AppKafkaProperties;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import javax.sql.DataSource;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Service;

@Service
public class SystemHealthService {
    private final DataSource dataSource;
    private final KafkaProperties kafkaProperties;
    private final AppKafkaProperties appKafkaProperties;
    private final String redisHost;
    private final int redisPort;
    private final String elasticsearchUri;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();

    public SystemHealthService(
            DataSource dataSource,
            KafkaProperties kafkaProperties,
            AppKafkaProperties appKafkaProperties,
            @Value("${spring.data.redis.host}") String redisHost,
            @Value("${spring.data.redis.port}") int redisPort,
            @Value("${spring.elasticsearch.uris}") String elasticsearchUri
    ) {
        this.dataSource = dataSource;
        this.kafkaProperties = kafkaProperties;
        this.appKafkaProperties = appKafkaProperties;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.elasticsearchUri = elasticsearchUri;
    }

    public SystemHealthView snapshot() {
        Runtime runtime = Runtime.getRuntime();
        return new SystemHealthView(
                databaseStatus(),
                kafkaStatus(),
                redisStatus(),
                elasticsearchStatus(),
                cpuUsage(),
                runtime.totalMemory() - runtime.freeMemory(),
                runtime.maxMemory()
        );
    }

    private String databaseStatus() {
        try (var connection = dataSource.getConnection()) {
            return connection.isValid(1) ? "UP" : "DEGRADED";
        } catch (Exception ex) {
            return "DOWN";
        }
    }

    private String kafkaStatus() {
        if (!appKafkaProperties.isEnabled()) {
            return "DISABLED";
        }
        try (AdminClient adminClient = AdminClient.create(kafkaProperties.buildAdminProperties(null))) {
            DescribeClusterResult cluster = adminClient.describeCluster();
            cluster.clusterId().get(1, java.util.concurrent.TimeUnit.SECONDS);
            return "UP";
        } catch (Exception ex) {
            return "DOWN";
        }
    }

    private String redisStatus() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(redisHost, redisPort), 1000);
            return "UP";
        } catch (Exception ex) {
            return "DOWN";
        }
    }

    private String elasticsearchStatus() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(elasticsearchUri))
                    .timeout(Duration.ofSeconds(1))
                    .GET()
                    .build();
            int status = httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            return status >= 200 && status < 500 ? "UP" : "DOWN";
        } catch (Exception ex) {
            return "DOWN";
        }
    }

    private double cpuUsage() {
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        if (bean instanceof OperatingSystemMXBean operatingSystemMXBean) {
            double load = operatingSystemMXBean.getProcessCpuLoad();
            return load < 0 ? 0 : load;
        }
        return 0;
    }
}

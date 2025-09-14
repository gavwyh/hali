package com.cs301.communication_service.producer;

import com.cs301.shared.protobuf.C2C;
import com.cs301.shared.protobuf.A2C;
import com.cs301.shared.protobuf.Otp;
import com.cs301.shared.protobuf.U2C;
import com.cs301.shared.protobuf.CRUDInfo;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;

import java.util.Properties;

@Service
public class TestKafkaProducer {

    private final KafkaProducer<String, C2C> c2cProducer;
    private final KafkaProducer<String, Otp> otpProducer;
    private final KafkaProducer<String, U2C> u2cProducer;
    private final KafkaProducer<String, A2C> a2cProducer;

    public TestKafkaProducer(@Value("${kafka.bootstrap-servers}") String bootstrapAddress,
                             @Value("${kafka.schema.registry}") String schemaRegistryUrl) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class.getName());

        // Add schema registry if required
        props.put("schema.registry.url", schemaRegistryUrl);

        this.c2cProducer = new KafkaProducer<>(props);
        this.otpProducer = new KafkaProducer<>(props);
        this.u2cProducer = new KafkaProducer<>(props);
        this.a2cProducer = new KafkaProducer<>(props);
    }

    public void sendTestCreateMessage() {
        // Constructing Protobuf Message
        CRUDInfo crudInfo = CRUDInfo.newBuilder()
                .setAttribute("")
                .setBeforeValue("")
                .setAfterValue("")
                .build();

        C2C message = C2C.newBuilder()
                .setAgentId("532cefda21fc399f42bd")
                .setClientId("12dae21y4c321ha21")
                .setClientEmail("fqteo@example.com")
                .setCrudType("CREATE")
                .setCrudInfo(crudInfo)
                .build();

        // Send message to Kafka
        ProducerRecord<String, C2C> record = new ProducerRecord<>("c2c", message);
        c2cProducer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Sent Protobuf message to Kafka: " + message);
            } else {
                exception.printStackTrace();
            }
        });

        c2cProducer.flush();
    }

    public void sendTestUpdateMessage() {
        // Constructing Protobuf Message
        CRUDInfo crudInfo = CRUDInfo.newBuilder()
                .setAttribute("country,firstName,lastName,emailAddress,address,clientId,phoneNumber,city,postalCode,state")
                .setBeforeValue("Test Country,John,Doe,john.doe.1742641177@example.com,123 Test St,799ba132-2bd4-4257-b9fe-954cfd27dbfb,1234567890,Test City,123456,Test State")
                .setAfterValue("Updated Country,John Updated,Doe Updated,john.updated@example.com,456 Updated St,82aeb-478d72c3b-1256-f903-8ae902647bcd9ed,9876543210,Updated City,654321,Updated State")
                .build();

        C2C message = C2C.newBuilder()
                .setAgentId("system")
                .setClientId("799ba132-2bd4-4257-b9fe-954cfd27dbfb")
                .setClientEmail("jycheong.2023@example.com")
                .setCrudType("UPDATE")
                .setCrudInfo(crudInfo)
                .build();

        // CRUDInfo crudInfo = CRUDInfo.newBuilder()
        //         .setAttribute("name")
        //         .setBeforeValue("fufu")
        //         .setAfterValue("fq")
        //         .build();

        // C2C message = C2C.newBuilder()
        //         .setAgentId("532cefda21fc399f42bd")
        //         .setClientId("12dae21y4c321ha21")
        //         .setClientEmail("fqteo.2023@example.com")
        //         .setCrudType("UPDATE")
        //         .setCrudInfo(crudInfo)
        //         .build();

        // Send message to Kafka
        ProducerRecord<String, C2C> record = new ProducerRecord<>("c2c", message);
        c2cProducer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Sent Protobuf message to Kafka: " + message);
            } else {
                exception.printStackTrace();
            }
        });

        c2cProducer.flush();
    }

    public void sendTestDeleteMessage() {
        // Constructing Protobuf Message
        CRUDInfo crudInfo = CRUDInfo.newBuilder()
                .setAttribute("")
                .setBeforeValue("")
                .setAfterValue("")
                .build();

        C2C message = C2C.newBuilder()
                .setAgentId("532cefda21fc399f42bd")
                .setClientId("12dae21y4c321ha21")
                .setClientEmail("fqteo.2023@example.com")
                .setCrudType("DELETE")
                .setCrudInfo(crudInfo)
                .build();

        // Send message to Kafka
        ProducerRecord<String, C2C> record = new ProducerRecord<>("c2c", message);
        c2cProducer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Sent Protobuf message to Kafka: " + message);
            } else {
                exception.printStackTrace();
            }
        });

        c2cProducer.flush();
    }

    public void sendTestU2CMessage() {
        // Constructing Protobuf Message

        U2C message = U2C.newBuilder()
                .setUsername("fred")
                .setUserRole("AGENT")
                .setUserEmail("fqteo.2023@example.com")
                .setTempPassword("1x28da74zf5bce93y3a8")
                .build();

        // Send message to Kafka
        ProducerRecord<String, U2C> record = new ProducerRecord<>("notifications", message);
        u2cProducer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Sent Protobuf message to Kafka: " + message);
            } else {
                exception.printStackTrace();
            }
        });

        u2cProducer.flush();
    }

    public void sendTestOtpMessage() {
        // Constructing Protobuf Message

        Otp message = Otp.newBuilder()
                .setUserEmail("fqteo.2023@example.com")
                .setOtp(699718)
                .build();

        // Send message to Kafka
        ProducerRecord<String, Otp> record = new ProducerRecord<>("otps", message);
        otpProducer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Sent Protobuf message to Kafka: " + message);
            } else {
                exception.printStackTrace();
            }
        });

        otpProducer.flush();
    }

    public void sendTestAccountCreateMessage() {
        // Constructing Protobuf Message
        A2C message = A2C.newBuilder()
                .setAgentId("532cefda21fc399f42bd")
                .setClientId("12dae21y4c321ha21")
                .setClientEmail("jycheong.2023@example.com")
                .setCrudType("CREATE")
                .setAccountId("CLIENT40056_STE11783")
                .setAccountType("PERSONAL")
                .build();

        // Send message to Kafka
        ProducerRecord<String, A2C> record = new ProducerRecord<>("a2c", message);
        a2cProducer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Sent Protobuf message to Kafka: " + message);
            } else {
                exception.printStackTrace();
            }
        });

        a2cProducer.flush();
    }

    public void sendTestAccountDeleteMessage() {
        // Constructing Protobuf Message
        A2C message = A2C.newBuilder()
                .setAgentId("532cefda21fc399f42bd")
                .setClientId("12dae21y4c321ha21")
                .setClientEmail("jycheong.2023@example.com")
                .setCrudType("DELETE")
                .setAccountId("CLIENT40056_STE11783")
                .setAccountType("PERSONAL")
                .build();

        // Send message to Kafka
        ProducerRecord<String, A2C> record = new ProducerRecord<>("a2c", message);
        a2cProducer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Sent Protobuf message to Kafka: " + message);
            } else {
                exception.printStackTrace();
            }
        });

        a2cProducer.flush();
    }
}

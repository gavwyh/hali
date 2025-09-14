package com.cs301.client_service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;
    
    @Column(name = "agent_id")
    private String agentId;
    
    @Column(name = "client_id")
    private String clientId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "crud_type", nullable = false)
    private CrudType crudType;
    
    // For attribute names (can be multiple, comma-separated)
    @Column(name = "attribute_name")
    private String attributeName;
    
    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;
    
    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;
    
    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;
    
    public enum CrudType {
        CREATE, READ, UPDATE, DELETE
    }
}

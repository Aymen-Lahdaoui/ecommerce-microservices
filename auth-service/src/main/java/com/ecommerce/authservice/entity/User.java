package com.ecommerce.authservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "users") // Remplace @Entity et @Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String id; // Les ID dans MongoDB sont généralement des String (ObjectId)

    @Field("username")
    private String username; // Nom d'utilisateur

    private String email;

    private String password;

    private String role; // Champ indispensable pour l'authentification et les rôles (ex: USER, ADMIN)
}
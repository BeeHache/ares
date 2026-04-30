package net.blackhacker.ares.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "canceled_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CanceledUser implements Serializable {
    @Id
    @Column(name = "user_id")
    private Long userId;

    // You might add a timestamp here to track when the user was marked for deletion
    // private ZonedDateTime markedForDeletionAt;
}

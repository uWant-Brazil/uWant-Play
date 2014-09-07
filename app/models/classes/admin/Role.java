package models.classes.admin;

import play.db.ebean.Model;

import javax.persistence.*;

@Entity
@Table(name = "roles")
@SequenceGenerator(name = Role.SEQUENCE_NAME, sequenceName = Role.SEQUENCE_NAME, initialValue = 1, allocationSize = 3)
public class Role extends Model implements be.objectify.deadbolt.core.models.Role {

    public static final String SEQUENCE_NAME = "roles_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    @Column(nullable = false)
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}

package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Classe Ebean responsável por guardar informações referentes as notificações enviadas para os usuários.
 */
@Entity
@Table(name = "user_notifications")
@SequenceGenerator(name = Notification.SEQUENCE_NAME, sequenceName = Notification.SEQUENCE_NAME, initialValue = 1, allocationSize = 13)
public class Notification extends Model {

    public static final String SEQUENCE_NAME = "user_notifications_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    private boolean delivered;

    @Column(nullable = false, updatable = false)
    private String title;

    @Column(nullable = false, updatable = false)
    private String message;

    @Column(nullable = false, updatable = false)
    private String uniqueIdentifier;

    private String serviceIdentifier;
    private String extra;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Column(nullable = false, updatable = false)
    private User user;

    @Version
    private Date modifiedAt;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Column(nullable = false, updatable = false)
    private Action action;

    @JsonIgnore
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @JsonIgnore
    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    @JsonIgnore
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonIgnore
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonIgnore
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    @JsonIgnore
    public String getServiceIdentifier() {
        return serviceIdentifier;
    }

    public void setServiceIdentifier(String serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
    }

    @JsonIgnore
    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @JsonIgnore
    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}

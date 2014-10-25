package models.classes;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Classe Ebean responsável por persistir informações de autenticação do perfil na rede social do usuário.
 */
@Entity
@Table(name = "social_profile")
@SequenceGenerator(name = SocialProfile.SEQUENCE_NAME, sequenceName = SocialProfile.SEQUENCE_NAME, initialValue = 1, allocationSize = 53)
public class SocialProfile extends Model {

    public static final String SEQUENCE_NAME = "social_profile_id_seq";

    public enum Provider {
        GOOGLE_PLUS, FACEBOOK, TWITTER;
    }

    public enum Status {
        ACTIVE, REMOVED, WAITING_REGISTRATION;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    /**
     * Rede social provedor das informações solicitadas.
     */
    @Enumerated(EnumType.ORDINAL)
    private Provider provider;

    /**
     * Access Token gerado pela rede social para autenticação e verificação de existência.
     */
    private String accessToken;

    /**
     * Usuário vinculado ao perfil na rede social, caso já tenha sido registrado.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    private String login;

    private String facebookId;

    @Version
    private Date modifiedAt;

    public SocialProfile() {
        // Do nothing...
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }
}

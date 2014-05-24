package models.classes;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Hibernate Bean Model responsável por persistir informações de autenticação do perfil na rede social do usuário.
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
    @Enumerated(EnumType.STRING)
    private Provider provider;

    /**
     * Access Token gerado pela rede social para autenticação e verificação de existência.
     */
    private String accessToken;

    /**
     * Usuário vinculado ao perfil na rede social, caso já tenha sido registrado.
     */
    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * Lista de logins registrados para esse perfil social. Esta informação será fornecida no ato de autenticação.
     * Note que em alguns casos, o login poderá ser o próprio e-mail do usuário.
     */
    @OneToMany(mappedBy = "profile")
    private List<Login> logins;

    @Version
    private Date lastUpdate;

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

    public List<Login> getLogins() {
        return logins;
    }

    public void setLogins(List<Login> logins) {
        this.logins = logins;
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

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Entity
    @Table(name = "social_profile_logins")
    @SequenceGenerator(name = SocialProfile.Login.SEQUENCE_NAME, sequenceName = SocialProfile.Login.SEQUENCE_NAME, initialValue = 1, allocationSize = 53)
    public static class Login extends Model {

        public static final String SEQUENCE_NAME = "social_profile_id_seq";

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
        private long id;

        private String login;

        @ManyToOne(cascade = CascadeType.ALL)
        @Column(name = "social_profile_id")
        private SocialProfile profile;

        public Login() {
            // Do nothing...
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public SocialProfile getProfile() {
            return profile;
        }

        public void setProfile(SocialProfile profile) {
            this.profile = profile;
        }
    }

}

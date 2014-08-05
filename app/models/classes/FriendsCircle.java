package models.classes;

import play.db.ebean.Model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Classe Ebean responsável por relacionar dois usuários como adicionados em seu círculo de amigos.
 * Note que para que eles estejam realmente adicionados ao círculo, ambos devem ser tanto 'requester'
 * quanto 'target' no relacionamento da chave composta.
 *
 * A partir dessa relação será possível aplicar algoritmos para análise da rede de amigos entre
 * os usuário a fim de melhorar a disposição de informações no feed de ações.
 */
@Entity
@Table(name = "friends_circle")
public class FriendsCircle extends Model {

    public enum FriendshipLevel {
        MUTUAL, WAITING_ME, WAITING_YOU, NONE;
    }

    @EmbeddedId
    private Relation relation;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private boolean isBlocked;

    public FriendsCircle() {

    }

    public FriendsCircle(Relation relation) {
        this.relation = relation;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    /**
     * Classe Embeddable para criação da chave-composta no banco de dados.
     * FIXME Como podemos vincular a classe User ao invés do Long do seus ids? Será necessário?
     */
    @Embeddable
    @Access(AccessType.FIELD)
    public static class Relation implements Serializable {

        @Column(name = "requester_id")
        private Long requesterId;

        @Column(name = "target_id")
        private Long targetId;

        public Long getRequesterId() {
            return requesterId;
        }

        public void setRequesterId(Long requesterId) {
            this.requesterId = requesterId;
        }

        public Long getTargetId() {
            return targetId;
        }

        public void setTargetId(Long targetId) {
            this.targetId = targetId;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Relation)) {
                return false;
            }
            Relation other = (Relation) obj;
            if (requesterId == null) {
                if (other.requesterId != null) {
                    return false;
                }
            } else if (requesterId != other.requesterId) {
                return false;
            }
            if (targetId == null) {
                if (other.targetId != null) {
                    return false;
                }
            } else if (targetId != other.targetId) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((requesterId == null) ? 0 : requesterId.hashCode());
            result = prime * result + ((targetId == null) ? 0 : targetId.hashCode());
            return result;
        }
    }

}
